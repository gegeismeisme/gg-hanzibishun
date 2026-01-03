package com.yourstudio.hskstroke.bishun.ui.character

fun pickDailySymbol(summary: HskProgressSummary): String? {
    val levels = summary.nextTargets.keys.sorted()
    levels.forEach { level ->
        val symbol = summary.nextTargets[level]
        if (!symbol.isNullOrBlank()) return symbol
    }
    return null
}

