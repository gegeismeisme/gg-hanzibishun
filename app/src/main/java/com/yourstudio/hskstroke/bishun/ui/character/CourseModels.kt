package com.yourstudio.hskstroke.bishun.ui.character

data class CourseSession(
    val level: Int,
    val symbols: List<String>,
    val index: Int,
) {
    val progressText: String get() = "${(index + 1).coerceAtLeast(1)}/${symbols.size}"
    val currentSymbol: String? get() = symbols.getOrNull(index)
    val hasPrevious: Boolean get() = index > 0
    val hasNext: Boolean get() = index < symbols.lastIndex
}

data class PracticeQueueSession(
    val symbols: List<String>,
    val index: Int,
) {
    val progressText: String get() = "${(index + 1).coerceAtLeast(1)}/${symbols.size}"
    val currentSymbol: String? get() = symbols.getOrNull(index)
    val hasPrevious: Boolean get() = index > 0
    val hasNext: Boolean get() = index < symbols.lastIndex
}

data class CourseEvent(val message: String)

