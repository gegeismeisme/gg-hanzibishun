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

    private suspend fun openDatabase(): SQLiteDatabase {
        database?.let { return it }
        return databaseMutex.withLock {
            database?.let { return it }
            val targetFile = ensureDatabaseFile()
            val opened = SQLiteDatabase.openDatabase(
                targetFile.absolutePath,
                null,
                SQLiteDatabase.OPEN_READONLY,
            )
            database = opened
            opened
        }
    }

    private fun ensureDatabaseFile(): File {
        val dbFile = context.getDatabasePath(DB_FILE_NAME)
        if (dbFile.exists()) return dbFile
        dbFile.parentFile?.mkdirs()
        context.assets.open(DB_ASSET_PATH).use { input ->
            dbFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return dbFile
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
        val normalized = normalizePinyin(query)
        val compactQuery = if (normalized.hasTone) normalized.toneCompact else normalized.plainCompact
        if (compactQuery.isBlank()) return emptyList()
        val column = if (normalized.hasTone) "pinyin_tone_compact" else "pinyin_plain_compact"

        val results = LinkedHashMap<String, WordEntry>()
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
        return results.values.take(limit)
    }

    private companion object {
        private const val DB_ASSET_PATH = "word/word.db"
        private const val DB_FILE_NAME = "word.db"
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

