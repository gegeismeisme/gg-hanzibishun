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
