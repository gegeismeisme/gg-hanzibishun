package com.yourstudio.hskstroke.bishun.data.hsk

data class HskLevelSummary(
    val completed: Int = 0,
    val total: Int = 0,
) {
    val remaining: Int get() = (total - completed).coerceAtLeast(0)
}

data class HskProgressSummary(
    val perLevel: Map<Int, HskLevelSummary> = emptyMap(),
    val nextTargets: Map<Int, String?> = emptyMap(),
) {
    val totalCompleted: Int get() = perLevel.values.sumOf { it.completed }
    val totalCharacters: Int get() = perLevel.values.sumOf { it.total }
}

