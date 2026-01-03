package com.yourstudio.hskstroke.bishun.data.daily

import android.content.Context
import com.yourstudio.hskstroke.bishun.data.hsk.HskEntry
import com.yourstudio.hskstroke.bishun.data.hsk.HskProgressStore
import com.yourstudio.hskstroke.bishun.data.hsk.HskRepository

object DailyPracticeGenerator {

    suspend fun suggestSymbol(context: Context): String? {
        val applicationContext = context.applicationContext
        val completed = HskProgressStore(applicationContext).completed.value
        val entries = runCatching { HskRepository(applicationContext).allEntries() }
            .getOrElse { emptyList() }
        return suggestSymbol(entries, completed)
    }

    fun suggestSymbol(entries: Collection<HskEntry>, completed: Set<String>): String? {
        if (entries.isEmpty()) return null

        val completedSet = completed
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .toHashSet()

        val entriesByLevel = entries
            .asSequence()
            .mapNotNull { entry ->
                val symbol = entry.symbol.trim()
                if (symbol.isEmpty()) return@mapNotNull null
                entry.copy(symbol = symbol)
            }
            .groupBy { it.level }

        entriesByLevel.keys.sorted().forEach { level ->
            val symbols = entriesByLevel[level].orEmpty()
                .sortedBy { it.writingLevel ?: Int.MAX_VALUE }
                .map { it.symbol }
            val nextTarget = symbols.firstOrNull { !completedSet.contains(it) }
            if (!nextTarget.isNullOrBlank()) return nextTarget
        }

        return null
    }
}

