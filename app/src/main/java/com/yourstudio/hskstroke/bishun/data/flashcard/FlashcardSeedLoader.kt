package com.yourstudio.hskstroke.bishun.data.flashcard

import android.content.Context
import org.json.JSONArray

class FlashcardSeedLoader(private val context: Context) {

    fun load(): List<SeedWord> {
        val json = context.assets.open(SEED_PATH).bufferedReader().use { it.readText() }
        val rows = JSONArray(json)
        val result = mutableListOf<SeedWord>()
        for (i in 0 until rows.length()) {
            val obj = rows.getJSONObject(i)
            result.add(
                SeedWord(
                    id = obj.getInt("id"),
                    hskLevel = obj.getInt("hskLevel"),
                    hanzi = obj.getString("hanzi"),
                    pinyin = obj.optString("pinyin", ""),
                    english = obj.optString("english", ""),
                    example = obj.optString("example", ""),
                )
            )
        }
        return result
    }

    companion object {
        private const val SEED_PATH = "flashcard/hsk_seed_words.json"
    }
}
