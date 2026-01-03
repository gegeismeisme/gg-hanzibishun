package com.yourstudio.hskstroke.bishun.data.word

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File

class WordRepository(
    private val context: Context,
) {
    private val databaseMutex = Mutex()
    @Volatile
    private var database: SQLiteDatabase? = null

    suspend fun getWord(symbol: String): WordEntry? {
        val normalized = symbol.trim().takeIf { it.isNotEmpty() } ?: return null
        return withContext(Dispatchers.IO) {
            val db = openDatabase()
            db.rawQuery(
                "SELECT word, oldword, strokes, pinyin, radicals, explanation, more FROM words WHERE word = ? LIMIT 1",
                arrayOf(normalized),
            ).use { cursor ->
                if (!cursor.moveToFirst()) return@withContext null
                cursor.toWordEntry()
            }
        }
    }

    suspend fun searchWords(query: String, limit: Int = 30): List<WordEntry> {
        val normalized = query.trim().takeIf { it.isNotEmpty() } ?: return emptyList()
        val safeLimit = limit.coerceAtLeast(0)
        if (safeLimit == 0) return emptyList()

        val isPinyinQuery = normalized.any { ch ->
            ch in 'a'..'z' || ch in 'A'..'Z' || ch == 'ü' || ch == 'Ü'
        }
        return withContext(Dispatchers.IO) {
            val db = openDatabase()
            if (isPinyinQuery) {
                searchByPinyin(db, normalized, safeLimit)
            } else {
                searchByWord(db, normalized, safeLimit)
            }
        }
    }

    suspend fun getWords(words: List<String>): List<WordEntry> {
        val normalized = words.asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .toList()
        if (normalized.isEmpty()) return emptyList()

        return withContext(Dispatchers.IO) {
            val db = openDatabase()
            val results = ArrayList<WordEntry>(normalized.size)
            normalized.chunked(IN_QUERY_CHUNK_SIZE).forEach { chunk ->
                val placeholders = chunk.joinToString(separator = ",") { "?" }
                db.rawQuery(
                    "SELECT word, oldword, strokes, pinyin, radicals, explanation, more FROM words WHERE word IN ($placeholders)",
                    chunk.toTypedArray(),
                ).use { cursor ->
                    while (cursor.moveToNext()) {
                        results.add(cursor.toWordEntry())
                    }
                }
            }
            results
        }
    }

    private suspend fun openDatabase(): SQLiteDatabase {
        database?.let { return it }
        return databaseMutex.withLock {
            database?.let { return it }
            val targetFile = ensureDatabaseFile()
            val opened = runCatching {
                SQLiteDatabase.openDatabase(
                    targetFile.absolutePath,
                    null,
                    SQLiteDatabase.OPEN_READONLY,
                )
            }.getOrElse { firstError ->
                runCatching {
                    targetFile.delete()
                    val refreshed = ensureDatabaseFile(forceRefresh = true)
                    SQLiteDatabase.openDatabase(
                        refreshed.absolutePath,
                        null,
                        SQLiteDatabase.OPEN_READONLY,
                    )
                }.getOrElse { secondError ->
                    secondError.addSuppressed(firstError)
                    throw secondError
                }
            }
            database = opened
            opened
        }
    }

    private fun ensureDatabaseFile(forceRefresh: Boolean = false): File {
        val dbFile = context.getDatabasePath(DB_FILE_NAME)
        val appLastUpdateTime = getAppLastUpdateTime()
        val needsRefresh = forceRefresh ||
            !dbFile.exists() ||
            dbFile.length() == 0L ||
            (appLastUpdateTime > 0L && dbFile.lastModified() < appLastUpdateTime)
        if (!needsRefresh) return dbFile

        dbFile.parentFile?.mkdirs()
        val tempFile = File(dbFile.absolutePath + ".tmp")
        if (tempFile.exists()) tempFile.delete()

        context.assets.open(DB_ASSET_PATH).use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        if (dbFile.exists()) dbFile.delete()
        if (!tempFile.renameTo(dbFile)) {
            tempFile.copyTo(dbFile, overwrite = true)
            tempFile.delete()
        }
        return dbFile
    }

    @Suppress("DEPRECATION")
    private fun getAppLastUpdateTime(): Long {
        return runCatching {
            context.packageManager.getPackageInfo(context.packageName, 0).lastUpdateTime
        }.getOrDefault(0L)
    }

    private fun searchByWord(
        db: SQLiteDatabase,
        query: String,
        limit: Int,
    ): List<WordEntry> {
        val results = LinkedHashMap<String, WordEntry>()
        db.rawQuery(
            "SELECT word, oldword, strokes, pinyin, radicals, explanation, more FROM words WHERE word = ? LIMIT 1",
            arrayOf(query),
        ).use { cursor ->
            if (cursor.moveToFirst()) {
                val entry = cursor.toWordEntry()
                results[entry.word] = entry
            }
        }
        db.rawQuery(
            """
                SELECT word, oldword, strokes, pinyin, radicals, explanation, more
                FROM words
                WHERE word LIKE ?
                ORDER BY length(word), word
                LIMIT ?
            """.trimIndent(),
            arrayOf("$query%", limit.toString()),
        ).use { cursor ->
            while (cursor.moveToNext() && results.size < limit) {
                val entry = cursor.toWordEntry()
                results.putIfAbsent(entry.word, entry)
            }
        }
        if (results.size < limit) {
            db.rawQuery(
                """
                    SELECT word, oldword, strokes, pinyin, radicals, explanation, more
                    FROM words
                    WHERE word LIKE ?
                    ORDER BY instr(word, ?), length(word), word
                    LIMIT ?
                """.trimIndent(),
                arrayOf("%$query%", query, limit.toString()),
            ).use { cursor ->
                while (cursor.moveToNext() && results.size < limit) {
                    val entry = cursor.toWordEntry()
                    results.putIfAbsent(entry.word, entry)
                }
            }
        }
        return results.values.take(limit)
    }

    private fun searchByPinyin(
        db: SQLiteDatabase,
        query: String,
        limit: Int,
    ): List<WordEntry> {
        val results = LinkedHashMap<String, WordEntry>()
        val candidates = normalizePinyinQueryCandidates(query)
            .mapNotNull { normalized ->
                val compactQuery = if (normalized.hasTone) normalized.toneCompact else normalized.plainCompact
                val column = if (normalized.hasTone) "pinyin_tone_compact" else "pinyin_plain_compact"
                compactQuery.takeIf { it.isNotBlank() }?.let { column to it }
            }
            .distinct()
        if (candidates.isEmpty()) return emptyList()

        candidates.forEach { (column, compactQuery) ->
            db.rawQuery(
                """
                    SELECT word, oldword, strokes, pinyin, radicals, explanation, more
                    FROM words
                    WHERE $column LIKE ?
                    ORDER BY length(word), word
                    LIMIT ?
                """.trimIndent(),
                arrayOf("$compactQuery%", limit.toString()),
            ).use { cursor ->
                while (cursor.moveToNext() && results.size < limit) {
                    val entry = cursor.toWordEntry()
                    results.putIfAbsent(entry.word, entry)
                }
            }
            if (results.size < limit) {
                db.rawQuery(
                    """
                        SELECT word, oldword, strokes, pinyin, radicals, explanation, more
                        FROM words
                        WHERE $column LIKE ?
                        ORDER BY instr($column, ?), length(word), word
                        LIMIT ?
                    """.trimIndent(),
                    arrayOf("%$compactQuery%", compactQuery, limit.toString()),
                ).use { cursor ->
                    while (cursor.moveToNext() && results.size < limit) {
                        val entry = cursor.toWordEntry()
                        results.putIfAbsent(entry.word, entry)
                    }
                }
            }
            if (results.size >= limit) return@forEach
        }
        return results.values.take(limit)
    }

    private companion object {
        private const val DB_ASSET_PATH = "word/word.db"
        private const val DB_FILE_NAME = "word.db"
        private const val IN_QUERY_CHUNK_SIZE = 400
    }
}

private fun Cursor.toWordEntry(): WordEntry {
    return WordEntry(
        word = getString(0).orEmpty(),
        oldword = getString(1).orEmpty(),
        strokes = getString(2).orEmpty(),
        pinyin = getString(3).orEmpty(),
        radicals = getString(4).orEmpty(),
        explanation = getString(5).orEmpty(),
        more = getString(6).orEmpty(),
    )
}
