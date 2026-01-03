package com.yourstudio.hskstroke.bishun.data.word

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray

class WordRepository(
    private val context: Context,
) {
    @Volatile
    private var cache: Map<String, WordEntry>? = null

    suspend fun getWord(symbol: String): WordEntry? {
        val normalized = symbol.trim().takeIf { it.isNotEmpty() } ?: return null
        if (cache == null) {
            load()
        }
        return cache?.get(normalized)
    }

    suspend fun searchWords(query: String, limit: Int = 30): List<WordEntry> {
        val normalized = query.trim().takeIf { it.isNotEmpty() } ?: return emptyList()
        if (cache == null) {
            load()
        }
        val entries = cache ?: return emptyList()
        val direct = entries[normalized]
        if (direct != null) return listOf(direct)

        val safeLimit = limit.coerceAtLeast(0)
        val isPinyinQuery = normalized.any { ch ->
            ch in 'a'..'z' || ch in 'A'..'Z' || ch == 'ü' || ch == 'Ü'
        }
        return if (isPinyinQuery) {
            searchByPinyin(entries.values, normalized, safeLimit)
        } else {
            searchByWord(entries.values, normalized, safeLimit)
        }
    }

    private fun searchByWord(
        entries: Collection<WordEntry>,
        query: String,
        limit: Int,
    ): List<WordEntry> {
        return entries.asSequence()
            .filter { entry -> entry.word.contains(query) }
            .sortedWith(compareBy<WordEntry>({ !it.word.startsWith(query) }, { it.word.length }, { it.word }))
            .take(limit)
            .toList()
    }

    private fun searchByPinyin(
        entries: Collection<WordEntry>,
        query: String,
        limit: Int,
    ): List<WordEntry> {
        val normalizedQuery = normalizePinyin(query)
        val hasTone = normalizedQuery.hasTone
        val compactQuery = (if (hasTone) normalizedQuery.withTone else normalizedQuery.plain)
            .replace(" ", "")
            .takeIf { it.isNotBlank() }
            ?: return emptyList()

        return entries.asSequence()
            .mapNotNull { entry ->
                val pinyin = entry.pinyin.takeIf { it.isNotBlank() } ?: return@mapNotNull null
                val normalizedEntry = normalizePinyin(pinyin)
                val compactTarget = (if (hasTone) normalizedEntry.withTone else normalizedEntry.plain).replace(" ", "")
                val index = compactTarget.indexOf(compactQuery)
                if (index < 0) null else SearchMatch(index, entry)
            }
            .sortedWith(compareBy<SearchMatch>({ it.index }, { it.entry.word.length }, { it.entry.word }))
            .take(limit)
            .map { it.entry }
            .toList()
    }

    private suspend fun load() = withContext(Dispatchers.IO) {
        if (cache != null) return@withContext
        val jsonText = context.assets.open(WORD_JSON_PATH).bufferedReader().use { it.readText() }
        val array = JSONArray(jsonText)
        val map = buildMap {
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                val entry = WordEntry(
                    word = obj.optString("word"),
                    oldword = obj.optString("oldword"),
                    strokes = obj.optString("strokes"),
                    pinyin = obj.optString("pinyin"),
                    radicals = obj.optString("radicals"),
                    explanation = obj.optString("explanation"),
                    more = obj.optString("more"),
                )
                if (entry.word.isNotEmpty()) {
                    put(entry.word, entry)
                }
            }
        }
        cache = map
    }

    companion object {
        private const val WORD_JSON_PATH = "word/word.json"
    }
}

private data class SearchMatch(val index: Int, val entry: WordEntry)

private data class NormalizedPinyin(
    val plain: String,
    val withTone: String,
    val hasTone: Boolean,
)

private fun normalizePinyin(raw: String): NormalizedPinyin {
    val parts = raw.trim().lowercase().split("\\s+".toRegex()).filter { it.isNotBlank() }
    if (parts.isEmpty()) return NormalizedPinyin(plain = "", withTone = "", hasTone = false)

    var anyTone = false
    val plainParts = mutableListOf<String>()
    val toneParts = mutableListOf<String>()
    parts.forEach { part ->
        val (plain, tone) = normalizePinyinSyllable(part)
        if (plain.isNotBlank()) {
            plainParts += plain
            toneParts += tone
            if (tone.any { it in '1'..'5' }) anyTone = true
        }
    }
    return NormalizedPinyin(
        plain = plainParts.joinToString(" "),
        withTone = toneParts.joinToString(" "),
        hasTone = anyTone,
    )
}

private fun normalizePinyinSyllable(raw: String): Pair<String, String> {
    var tone = 0
    val builder = StringBuilder()
    raw.lowercase().forEach { ch ->
        when {
            ch in '1'..'5' -> {
                tone = ch.digitToInt()
            }

            ch == ':' -> {
                val last = builder.lastOrNull()
                if (last == 'u') {
                    builder.setCharAt(builder.length - 1, 'v')
                }
            }

            else -> {
                val mapped = mapPinyinChar(ch)
                if (mapped.tone != 0) tone = mapped.tone
                if (mapped.base != null) builder.append(mapped.base)
            }
        }
    }
    val plain = builder.toString()
    val toneSuffix = if (tone in 1..5) tone.toString() else ""
    return plain to (plain + toneSuffix)
}

private data class PinyinChar(val base: Char?, val tone: Int)

private fun mapPinyinChar(ch: Char): PinyinChar {
    return when (ch) {
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z' -> PinyinChar(ch, 0)
        'ü' -> PinyinChar('v', 0)

        'ā' -> PinyinChar('a', 1)
        'á' -> PinyinChar('a', 2)
        'ǎ' -> PinyinChar('a', 3)
        'à' -> PinyinChar('a', 4)
        'ē' -> PinyinChar('e', 1)
        'é' -> PinyinChar('e', 2)
        'ě' -> PinyinChar('e', 3)
        'è' -> PinyinChar('e', 4)
        'ī' -> PinyinChar('i', 1)
        'í' -> PinyinChar('i', 2)
        'ǐ' -> PinyinChar('i', 3)
        'ì' -> PinyinChar('i', 4)
        'ō' -> PinyinChar('o', 1)
        'ó' -> PinyinChar('o', 2)
        'ǒ' -> PinyinChar('o', 3)
        'ò' -> PinyinChar('o', 4)
        'ū' -> PinyinChar('u', 1)
        'ú' -> PinyinChar('u', 2)
        'ǔ' -> PinyinChar('u', 3)
        'ù' -> PinyinChar('u', 4)
        'ǖ' -> PinyinChar('v', 1)
        'ǘ' -> PinyinChar('v', 2)
        'ǚ' -> PinyinChar('v', 3)
        'ǜ' -> PinyinChar('v', 4)

        else -> PinyinChar(null, 0)
    }
}
