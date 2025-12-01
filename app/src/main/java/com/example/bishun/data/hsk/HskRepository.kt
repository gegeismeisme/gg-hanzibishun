package com.example.bishun.data.hsk

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader

class HskRepository(
    private val context: Context,
) {

    @Volatile
    private var cache: Map<String, HskEntry>? = null

    suspend fun get(symbol: String): HskEntry? {
        val trimmed = symbol.trim()
        if (trimmed.isEmpty()) return null
        if (cache == null) {
            load()
        }
        return cache?.get(trimmed)
    }

    private suspend fun load() = withContext(Dispatchers.IO) {
        if (cache != null) return@withContext
        val map = buildMap<String, HskEntry> {
            context.assets.open(HSK_DATA_PATH).use { input ->
                BufferedReader(InputStreamReader(input, Charsets.UTF_8)).useLines { lines ->
                    lines.drop(1).forEach { line ->
                        if (line.isBlank()) return@forEach
                        val columns = line.split(',')
                        val entry = HskEntry.fromCsvRow(columns) ?: return@forEach
                        put(entry.symbol, entry)
                    }
                }
            }
        }
        cache = map
    }

    suspend fun allEntries(): Collection<HskEntry> {
        if (cache == null) {
            load()
        }
        return cache?.values ?: emptyList()
    }

    companion object {
        private const val HSK_DATA_PATH = "learn-datas/hsk30-chars-ext.csv"
    }
}
