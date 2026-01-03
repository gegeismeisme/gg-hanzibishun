package com.yourstudio.hskstroke.bishun.ui.character

import java.util.Locale

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

sealed interface CourseEvent {
    data object SkippedCharacter : CourseEvent
    data class MarkedLearned(val symbol: String) : CourseEvent
    data class LevelCompleted(val level: Int) : CourseEvent
    data class AutoAdvanced(val symbol: String) : CourseEvent
    data class NextUp(val symbol: String) : CourseEvent
}

fun CourseEvent.resolveMessage(strings: CoursesStrings, locale: Locale): String {
    return when (this) {
        CourseEvent.SkippedCharacter -> strings.toastSkippedLabel
        is CourseEvent.MarkedLearned -> String.format(locale, strings.toastMarkedLearnedFormat, symbol)
        is CourseEvent.LevelCompleted -> String.format(locale, strings.toastCourseCompleteFormat, level)
        is CourseEvent.AutoAdvanced -> String.format(locale, strings.toastAutoAdvancedFormat, symbol)
        is CourseEvent.NextUp -> String.format(locale, strings.toastNextUpFormat, symbol)
    }
}
