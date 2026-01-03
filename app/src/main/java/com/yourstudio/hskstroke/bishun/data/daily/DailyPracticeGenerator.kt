package com.yourstudio.hskstroke.bishun.data.daily

import android.content.Context
import com.yourstudio.hskstroke.bishun.data.hsk.HskEntry
import com.yourstudio.hskstroke.bishun.data.hsk.HskCourseCatalogBuilder
import com.yourstudio.hskstroke.bishun.data.hsk.HskProgressCalculator
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
        val catalog = HskCourseCatalogBuilder.build(entries)
        if (catalog.isEmpty()) return null

        val summary = HskProgressCalculator.calculateSummary(completed, catalog)
        return summary.nextTargets.keys.sorted().firstNotNullOfOrNull { level ->
            summary.nextTargets[level]?.trim()?.takeIf { it.isNotBlank() }
        }
    }
}
