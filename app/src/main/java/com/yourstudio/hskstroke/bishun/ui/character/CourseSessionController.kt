package com.yourstudio.hskstroke.bishun.ui.character

import com.yourstudio.hskstroke.bishun.data.hsk.HskProgressStore
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

internal class CourseSessionController(
    private val scope: CoroutineScope,
    private val userPreferencesStore: UserPreferencesStore,
    private val hskProgressStore: HskProgressStore,
    private val courseSession: MutableStateFlow<CourseSession?>,
    private val practiceQueueSession: MutableStateFlow<PracticeQueueSession?>,
    private val courseEvents: MutableSharedFlow<CourseEvent>,
    private val loadCharacter: (String) -> Unit,
) {
    private var courseEntries: Map<Int, List<String>> = emptyMap()

    fun updateCatalog(entries: Map<Int, List<String>>) {
        courseEntries = entries
    }

    fun restoreCourseSession(level: Int?, symbol: String?) {
        val safeLevel = level ?: return
        val safeSymbol = symbol ?: return
        val symbols = courseEntries[safeLevel] ?: return
        if (!symbols.contains(safeSymbol)) return

        courseSession.value = CourseSession(
            level = safeLevel,
            symbols = symbols,
            index = symbols.indexOf(safeSymbol),
        )
    }

    fun clearCourseSession() {
        courseSession.value = null
        scope.launch { userPreferencesStore.saveCourseSession(null, null) }
    }

    fun startCourse(level: Int, symbol: String, currentSymbol: String?) {
        val symbols = courseEntries[level] ?: return
        if (symbols.isEmpty()) return
        val targetSymbol = symbol.takeIf { symbols.contains(it) } ?: symbols.first()
        val index = symbols.indexOf(targetSymbol).takeIf { it >= 0 } ?: 0
        courseSession.value = CourseSession(level = level, symbols = symbols, index = index)
        scope.launch { userPreferencesStore.saveCourseSession(level, targetSymbol) }
        if (currentSymbol != targetSymbol) {
            loadCharacter(targetSymbol)
        }
    }

    fun goToNextCourseCharacter() {
        navigateCourse(1)
    }

    fun goToPreviousCourseCharacter() {
        navigateCourse(-1)
    }

    fun skipCourseCharacter() {
        navigateCourse(1)
        scope.launch { courseEvents.emit(CourseEvent.SkippedCharacter) }
    }

    fun restartCourseLevel() {
        val session = courseSession.value ?: return
        if (session.symbols.isEmpty()) return
        courseSession.value = session.copy(index = 0)
        val symbol = session.symbols.first()
        scope.launch { userPreferencesStore.saveCourseSession(session.level, symbol) }
        loadCharacter(symbol)
    }

    fun markCourseCharacterLearned(symbol: String) {
        scope.launch {
            hskProgressStore.add(symbol)
            courseEvents.emit(CourseEvent.MarkedLearned(symbol))
        }
    }

    fun startPracticeQueue(symbols: List<String>) {
        val queue = symbols.asSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .map(::firstCodePoint)
            .distinct()
            .toList()
        if (queue.isEmpty()) return

        practiceQueueSession.value = PracticeQueueSession(symbols = queue, index = 0)
        loadCharacter(queue.first())
    }

    fun goToNextPracticeQueueCharacter() {
        navigatePracticeQueue(1)
    }

    fun goToPreviousPracticeQueueCharacter() {
        navigatePracticeQueue(-1)
    }

    fun restartPracticeQueue() {
        val session = practiceQueueSession.value ?: return
        if (session.symbols.isEmpty()) return
        practiceQueueSession.value = session.copy(index = 0)
        loadCharacter(session.symbols.first())
    }

    fun exitPracticeQueue() {
        practiceQueueSession.value = null
    }

    fun alignSessions(symbol: String) {
        alignCourseSession(symbol)
        alignPracticeQueueSession(symbol)
    }

    fun advanceAfterPracticeCompletion(currentSymbol: String) {
        val session = courseSession.value ?: return
        if (session.symbols.getOrNull(session.index) != currentSymbol) return

        if (session.index >= session.symbols.lastIndex) {
            courseSession.value = null
            scope.launch { userPreferencesStore.saveCourseSession(null, null) }
            scope.launch {
                courseEvents.emit(CourseEvent.LevelCompleted(session.level))
            }
            return
        }

        val nextIndex = session.index + 1
        val nextSymbol = session.symbols[nextIndex]
        courseSession.value = session.copy(index = nextIndex)
        scope.launch { userPreferencesStore.saveCourseSession(session.level, nextSymbol) }
        scope.launch { courseEvents.emit(CourseEvent.AutoAdvanced(nextSymbol)) }
        loadCharacter(nextSymbol)
    }

    private fun navigateCourse(delta: Int) {
        val session = courseSession.value ?: return
        val newIndex = (session.index + delta).coerceIn(0, session.symbols.size - 1)
        if (newIndex == session.index) return
        val nextSymbol = session.symbols[newIndex]
        courseSession.value = session.copy(index = newIndex)
        scope.launch {
            userPreferencesStore.saveCourseSession(session.level, nextSymbol)
            courseEvents.emit(CourseEvent.NextUp(nextSymbol))
        }
        loadCharacter(nextSymbol)
    }

    private fun alignCourseSession(symbol: String) {
        val session = courseSession.value ?: return
        val index = session.symbols.indexOf(symbol)
        if (index == -1) {
            courseSession.value = null
            scope.launch { userPreferencesStore.saveCourseSession(null, null) }
        } else if (index != session.index) {
            courseSession.value = session.copy(index = index)
            scope.launch { userPreferencesStore.saveCourseSession(session.level, symbol) }
        }
    }

    private fun navigatePracticeQueue(delta: Int) {
        val session = practiceQueueSession.value ?: return
        val newIndex = (session.index + delta).coerceIn(0, session.symbols.size - 1)
        if (newIndex == session.index) return
        val nextSymbol = session.symbols[newIndex]
        practiceQueueSession.value = session.copy(index = newIndex)
        loadCharacter(nextSymbol)
    }

    private fun alignPracticeQueueSession(symbol: String) {
        val session = practiceQueueSession.value ?: return
        val index = session.symbols.indexOf(symbol)
        if (index == -1) {
            practiceQueueSession.value = null
        } else if (index != session.index) {
            practiceQueueSession.value = session.copy(index = index)
        }
    }

    private fun firstCodePoint(input: String): String {
        val codePoint = Character.codePointAt(input, 0)
        return String(Character.toChars(codePoint))
    }
}
