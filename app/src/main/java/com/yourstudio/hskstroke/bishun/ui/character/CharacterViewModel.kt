package com.yourstudio.hskstroke.bishun.ui.character

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yourstudio.hskstroke.bishun.data.characters.CharacterDefinitionRepository
import com.yourstudio.hskstroke.bishun.data.characters.di.CharacterDataModule
import com.yourstudio.hskstroke.bishun.data.word.WordEntry
import com.yourstudio.hskstroke.bishun.data.word.WordRepository
import com.yourstudio.hskstroke.bishun.data.hsk.HskEntry
import com.yourstudio.hskstroke.bishun.data.hsk.HskRepository
import com.yourstudio.hskstroke.bishun.data.hsk.HskProgressStore
import com.yourstudio.hskstroke.bishun.data.history.PracticeHistoryEntry
import com.yourstudio.hskstroke.bishun.data.history.PracticeHistoryStore
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferences
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import com.yourstudio.hskstroke.bishun.hanzi.core.HanziCounter
import com.yourstudio.hskstroke.bishun.hanzi.model.CharacterDefinition
import com.yourstudio.hskstroke.bishun.hanzi.model.Point
import com.yourstudio.hskstroke.bishun.hanzi.model.UserStroke
import com.yourstudio.hskstroke.bishun.hanzi.render.RenderState
import com.yourstudio.hskstroke.bishun.hanzi.render.RenderStateOptions
import com.yourstudio.hskstroke.bishun.hanzi.render.RenderStateSnapshot
import com.yourstudio.hskstroke.bishun.hanzi.render.actions.CharacterActions
import com.yourstudio.hskstroke.bishun.hanzi.render.actions.CharacterLayer
import com.yourstudio.hskstroke.bishun.hanzi.render.actions.QuizActions
import com.yourstudio.hskstroke.bishun.hanzi.quiz.StrokeMatcher
import com.yourstudio.hskstroke.bishun.ui.practice.BoardSettings
import com.yourstudio.hskstroke.bishun.ui.practice.PracticeGrid
import com.yourstudio.hskstroke.bishun.ui.practice.StrokeColorOption
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.min

class CharacterViewModel(
    private val appContext: Context,
    private val repository: CharacterDefinitionRepository,
    private val wordRepository: WordRepository,
    private val hskRepository: HskRepository,
    private val hskProgressStore: HskProgressStore,
    private val practiceHistoryStore: PracticeHistoryStore,
    private val userPreferencesStore: UserPreferencesStore,
) : ViewModel() {

    private val _query = MutableStateFlow(DEFAULT_CHAR)
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<CharacterUiState>(CharacterUiState.Loading)
    val uiState: StateFlow<CharacterUiState> = _uiState.asStateFlow()

    private val _renderSnapshot = MutableStateFlow<RenderStateSnapshot?>(null)
    val renderSnapshot: StateFlow<RenderStateSnapshot?> = _renderSnapshot.asStateFlow()

    private val _practiceState = MutableStateFlow(PracticeState())
    val practiceState: StateFlow<PracticeState> = _practiceState.asStateFlow()
    private val _demoState = MutableStateFlow(DemoState())
    val demoState: StateFlow<DemoState> = _demoState.asStateFlow()
    private val _wordEntry = MutableStateFlow<WordEntry?>(null)
    val wordEntry: StateFlow<WordEntry?> = _wordEntry.asStateFlow()
    private val _wordInfoUiState = MutableStateFlow<WordInfoUiState>(WordInfoUiState.Idle)
    val wordInfoUiState: StateFlow<WordInfoUiState> = _wordInfoUiState.asStateFlow()
    private val _hskEntry = MutableStateFlow<HskEntry?>(null)
    val hskEntry: StateFlow<HskEntry?> = _hskEntry.asStateFlow()
    private val _hskProgress = MutableStateFlow(HskProgressSummary())
    val hskProgress: StateFlow<HskProgressSummary> = _hskProgress.asStateFlow()
    private val _practiceHistory = MutableStateFlow<List<PracticeHistoryEntry>>(emptyList())
    val practiceHistory: StateFlow<List<PracticeHistoryEntry>> = _practiceHistory.asStateFlow()
    private val _courseSession = MutableStateFlow<CourseSession?>(null)
    val courseSession: StateFlow<CourseSession?> = _courseSession.asStateFlow()
    private val _boardSettings = MutableStateFlow(BoardSettings())
    val boardSettings: StateFlow<BoardSettings> = _boardSettings.asStateFlow()
    private val _courseCatalog = MutableStateFlow<Map<Int, List<String>>>(emptyMap())
    val courseCatalog: StateFlow<Map<Int, List<String>>> = _courseCatalog.asStateFlow()
    private val _completedSymbols = MutableStateFlow<Set<String>>(emptySet())
    val completedSymbols: StateFlow<Set<String>> = _completedSymbols.asStateFlow()
    private val _courseEvents = MutableSharedFlow<CourseEvent>()
    val courseEvents: SharedFlow<CourseEvent> = _courseEvents.asSharedFlow()
    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private var currentDefinition: CharacterDefinition? = null
    private var renderState: RenderState? = null
    private var renderStateJob: Job? = null
    private var completionResetJob: Job? = null
    private var activeUserStroke: UserStroke? = null
    private val userStrokeIds = mutableListOf<Int>()
    private var courseEntries: Map<Int, List<String>> = emptyMap()

    init {
        viewModelScope.launch {
            val catalog = loadCourseCatalogIfNeeded()
            updateHskProgress(hskProgressStore.completed.value, catalog)
        }
        observeHskProgress()
        observePracticeHistory()
        observeUserPreferences()
        loadCharacter(DEFAULT_CHAR)
    }

    fun updateQuery(input: String) {
        val trimmed = input.trim()
        if (trimmed.isEmpty()) {
            _query.value = ""
            return
        }
        val codePoint = Character.codePointAt(trimmed, 0)
        _query.value = String(Character.toChars(codePoint))
    }

    fun clearQuery() {
        _query.value = ""
    }

    fun submitQuery() {
        loadCharacter(_query.value)
    }

    fun jumpToCharacter(symbol: String) {
        loadCharacter(symbol)
    }

    fun startCourse(level: Int, symbol: String) {
        val symbols = courseEntries[level] ?: return
        if (symbols.isEmpty()) return
        val targetSymbol = symbol.takeIf { symbols.contains(it) } ?: symbols.first()
        val index = symbols.indexOf(targetSymbol).takeIf { it >= 0 } ?: 0
        _courseSession.value = CourseSession(level = level, symbols = symbols, index = index)
        viewModelScope.launch { userPreferencesStore.saveCourseSession(level, targetSymbol) }
        if (currentDefinition?.symbol != targetSymbol) {
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
        viewModelScope.launch { _courseEvents.emit(CourseEvent("Skipped character")) }
    }

    fun restartCourseLevel() {
        val session = _courseSession.value ?: return
        if (session.symbols.isEmpty()) return
        _courseSession.value = session.copy(index = 0)
        val symbol = session.symbols.first()
        viewModelScope.launch { userPreferencesStore.saveCourseSession(session.level, symbol) }
        loadCharacter(symbol)
    }

    fun markCourseCharacterLearned(symbol: String) {
        viewModelScope.launch {
            hskProgressStore.add(symbol)
            _courseEvents.emit(CourseEvent("Marked $symbol as learned"))
        }
    }

    fun playDemo(loop: Boolean = false) {
        val definition = currentDefinition ?: return
        val state = renderState ?: return
        if (_demoState.value.isPlaying) return
        _demoState.value = DemoState(isPlaying = true, loop = loop)
        viewModelScope.launch {
            if (_practiceState.value.isActive || _practiceState.value.completedStrokes.isNotEmpty()) {
                cancelCompletionReset()
                clearUserStrokes()
                resetPracticeState(definition)
            }
            do {
                state.run(CharacterActions.hideCharacter(CharacterLayer.MAIN, definition, DEMO_FADE_DURATION))
                state.run(CharacterActions.prepareLayerForAnimation(CharacterLayer.MAIN, definition))
                for (stroke in definition.strokes) {
                    if (!_demoState.value.isPlaying) {
                        _demoState.value = DemoState()
                        return@launch
                    }
                    state.run(
                        CharacterActions.showStroke(
                            CharacterLayer.MAIN,
                            stroke.strokeNum,
                            DEFAULT_ANIMATION_DURATION,
                        ),
                    )
                    delay(DELAY_BETWEEN_STROKES)
                }
                state.run(CharacterActions.showCharacter(CharacterLayer.MAIN, definition, DEMO_REVEAL_DURATION))
                if (_demoState.value.loop) {
                    delay(DEMO_LOOP_PAUSE)
                }
            } while (_demoState.value.loop && _demoState.value.isPlaying)
            _demoState.value = DemoState()
        }
    }

    fun stopDemo() {
        _demoState.value = DemoState()
    }

    fun resetCharacter() {
        val definition = currentDefinition ?: return
        val state = renderState ?: return
        viewModelScope.launch {
            state.run(CharacterActions.showCharacter(CharacterLayer.MAIN, definition, 200))
            cancelCompletionReset()
            resetPracticeState(definition)
        }
    }

    fun startPractice() {
        val definition = currentDefinition ?: return
        val state = renderState ?: return
        cancelCompletionReset()
        viewModelScope.launch {
            stopDemo()
            state.run(CharacterActions.showCharacter(CharacterLayer.MAIN, definition, PRACTICE_FADE_DURATION))
            clearUserStrokes()
            state.run(QuizActions.startQuiz(definition, PRACTICE_FADE_DURATION, 0))
            _practiceState.value = PracticeState(
                isActive = true,
                totalStrokes = definition.strokeCount,
                statusMessage = "Start from stroke 1",
                mistakeSinceHint = 0,
                completedStrokes = emptySet(),
            )
        }
    }

    fun onPracticeStrokeStart(charPoint: Point, externalPoint: Point) {
        val state = renderState ?: return
        if (!_practiceState.value.isActive) return
        val strokeId = HanziCounter.next()
        val userStroke = UserStroke(strokeId, charPoint, externalPoint)
        activeUserStroke = userStroke
        userStrokeIds.add(strokeId)
        viewModelScope.launch {
            state.run(QuizActions.startUserStroke(strokeId, charPoint))
        }
    }

    fun onPracticeStrokeMove(charPoint: Point, externalPoint: Point) {
        val state = renderState ?: return
        val stroke = activeUserStroke ?: return
        stroke.append(charPoint, externalPoint)
        viewModelScope.launch {
            state.run(QuizActions.updateUserStroke(stroke.id, stroke.points))
        }
    }

    fun onPracticeStrokeEnd() {
        val definition = currentDefinition ?: return
        val state = renderState ?: return
        val practice = _practiceState.value
        val stroke = activeUserStroke ?: return
        activeUserStroke = null
        val fadeDuration = _renderSnapshot.value?.options?.drawingFadeDuration ?: PRACTICE_FADE_DURATION
        viewModelScope.launch {
            state.run(QuizActions.hideUserStroke(stroke.id, fadeDuration))
        }
        if (!practice.isActive || stroke.points.size < 2) {
            return
        }
        viewModelScope.launch {
            val snapshot = _renderSnapshot.value
            val options = StrokeMatcher.Options(
                leniency = PRACTICE_LENIENCY,
                isOutlineVisible = snapshot?.character?.outline?.opacity ?: 0f > 0f,
                averageDistanceThreshold = PRACTICE_DISTANCE_THRESHOLD,
            )
            val result = StrokeMatcher.matches(
                stroke,
                definition,
                practice.currentStrokeIndex,
                options,
            )
            if (result.isMatch) {
                handleCorrectStroke(result.isStrokeBackwards)
            } else {
                handleMistake()
            }
        }
    }

    private fun loadCharacter(input: String) {
        val normalized = input.trim().ifEmpty { return }
        _uiState.value = CharacterUiState.Loading
        viewModelScope.launch {
            val result = repository.load(normalized)
            result.fold(
                onSuccess = {
                    _uiState.value = CharacterUiState.Success(it)
                    currentDefinition = it
                    setupRenderState(it)
                    cancelCompletionReset()
                    resetPracticeState(it)
                    _wordEntry.value = null
                    _wordInfoUiState.value = WordInfoUiState.Idle
                    loadHskInfo(it.symbol)
                    alignCourseSession(it.symbol)
                },
                onFailure = {
                    val message = it.message ?: "加载失败，请稍后再试"
                    _uiState.value = CharacterUiState.Error(message)
                    _wordEntry.value = null
                    _wordInfoUiState.value = WordInfoUiState.Idle
                    _hskEntry.value = null
                },
            )
        }
    }

    private fun setupRenderState(definition: CharacterDefinition) {
        renderStateJob?.cancel()
        renderState?.dispose()
        val newRenderState = RenderState(definition, RenderStateOptions())
        renderState = newRenderState
        renderStateJob = viewModelScope.launch {
            newRenderState.state.collect { snapshot ->
                _renderSnapshot.value = snapshot
            }
        }
    }

    private fun resetPracticeState(definition: CharacterDefinition) {
        _practiceState.value = PracticeState(
            isActive = false,
            isComplete = false,
            totalStrokes = definition.strokeCount,
            currentStrokeIndex = 0,
            totalMistakes = 0,
            statusMessage = "",
            mistakeSinceHint = 0,
            completedStrokes = emptySet(),
        )
        activeUserStroke = null
        userStrokeIds.clear()
    }

    fun requestWordInfo(symbol: String? = currentDefinition?.symbol) {
        val normalized = symbol?.trim().takeIf { !it.isNullOrEmpty() } ?: return
        val currentEntry = _wordEntry.value
        val currentState = _wordInfoUiState.value
        if (currentEntry?.word == normalized && currentState is WordInfoUiState.Loaded) return
        if (currentState is WordInfoUiState.Loading) return

        _wordInfoUiState.value = WordInfoUiState.Loading
        viewModelScope.launch {
            runCatching { wordRepository.getWord(normalized) }
                .onSuccess { entry ->
                    if (currentDefinition?.symbol != normalized) return@launch
                    if (entry == null) {
                        _wordEntry.value = null
                        _wordInfoUiState.value = WordInfoUiState.NotFound
                    } else {
                        _wordEntry.value = entry
                        _wordInfoUiState.value = WordInfoUiState.Loaded
                    }
                }
                .onFailure { throwable ->
                    if (currentDefinition?.symbol != normalized) return@launch
                    _wordEntry.value = null
                    _wordInfoUiState.value = WordInfoUiState.Error(
                        throwable.message ?: "Unable to load dictionary entry.",
                    )
                }
        }
    }

    private fun loadHskInfo(symbol: String) {
        viewModelScope.launch {
            runCatching { hskRepository.get(symbol) }
                .onSuccess { _hskEntry.value = it }
                .onFailure { _hskEntry.value = null }
        }
    }

    private fun observeHskProgress() {
        viewModelScope.launch {
            hskProgressStore.completed.collect { completed ->
                _completedSymbols.value = completed
                val catalog = loadCourseCatalogIfNeeded()
                updateHskProgress(completed, catalog)
            }
        }
    }

    private suspend fun loadCourseCatalogIfNeeded(): Map<Int, List<String>> {
        if (courseEntries.isNotEmpty()) {
            if (_courseCatalog.value.isEmpty()) {
                _courseCatalog.value = courseEntries
            }
            return courseEntries
        }

        val entries = runCatching { hskRepository.allEntries().toList() }
            .getOrElse { emptyList() }

        val catalog = entries
            .groupBy { it.level }
            .mapValues { (_, items) ->
                items
                    .sortedBy { it.writingLevel ?: Int.MAX_VALUE }
                    .map { it.symbol }
            }

        courseEntries = catalog
        _courseCatalog.value = catalog
        return catalog
    }

    private fun updateHskProgress(completed: Set<String>, catalog: Map<Int, List<String>>) {
        val perLevel = mutableMapOf<Int, HskLevelSummary>()
        val nextTargets = mutableMapOf<Int, String?>()
        catalog.forEach { (level, symbols) ->
            val done = symbols.count { completed.contains(it) }
            perLevel[level] = HskLevelSummary(done, symbols.size)
            nextTargets[level] = symbols.firstOrNull { !completed.contains(it) }
        }
        _hskProgress.value = HskProgressSummary(perLevel, nextTargets)
    }

    private fun observePracticeHistory() {
        viewModelScope.launch {
            practiceHistoryStore.history.collect { entries ->
                _practiceHistory.value = entries
            }
        }
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferencesStore.data.collect { prefs ->
                _userPreferences.value = prefs
                _boardSettings.value = BoardSettings(
                    grid = PracticeGrid.entries.getOrElse(prefs.gridMode) { PracticeGrid.NONE },
                    strokeColor = StrokeColorOption.entries.getOrElse(prefs.strokeColor) { StrokeColorOption.PURPLE },
                    showTemplate = prefs.showTemplate,
                )
                if (prefs.courseLevel != null && prefs.courseSymbol != null) {
                    val symbols = courseEntries[prefs.courseLevel]
                    if (symbols != null && symbols.contains(prefs.courseSymbol)) {
                        _courseSession.value = CourseSession(
                            level = prefs.courseLevel,
                            symbols = symbols,
                            index = symbols.indexOf(prefs.courseSymbol),
                        )
                    }
                }
            }
        }
    }

    private fun navigateCourse(delta: Int) {
        val session = _courseSession.value ?: return
        val newIndex = (session.index + delta).coerceIn(0, session.symbols.size - 1)
        if (newIndex == session.index) return
        val nextSymbol = session.symbols[newIndex]
        _courseSession.value = session.copy(index = newIndex)
        viewModelScope.launch {
            userPreferencesStore.saveCourseSession(session.level, nextSymbol)
            _courseEvents.emit(CourseEvent("Next up: $nextSymbol"))
        }
        loadCharacter(nextSymbol)
    }

    private fun alignCourseSession(symbol: String) {
        val session = _courseSession.value ?: return
        val index = session.symbols.indexOf(symbol)
        if (index == -1) {
            _courseSession.value = null
            viewModelScope.launch { userPreferencesStore.saveCourseSession(null, null) }
        } else if (index != session.index) {
            _courseSession.value = session.copy(index = index)
            viewModelScope.launch { userPreferencesStore.saveCourseSession(session.level, symbol) }
        }
    }

    fun clearLocalData() {
        viewModelScope.launch {
            practiceHistoryStore.clear()
            hskProgressStore.clear()
            userPreferencesStore.clearAll()
            _practiceHistory.value = emptyList()
            _completedSymbols.value = emptySet()
            _courseSession.value = null
            _userPreferences.value = UserPreferences()
            _boardSettings.value = BoardSettings()
            val catalog = loadCourseCatalogIfNeeded()
            updateHskProgress(emptySet(), catalog)
        }
    }

    fun updateGridMode(mode: PracticeGrid) {
        viewModelScope.launch {
            userPreferencesStore.setGridMode(mode.ordinal)
        }
    }

    fun updateStrokeColor(option: StrokeColorOption) {
        viewModelScope.launch {
            userPreferencesStore.setStrokeColor(option.ordinal)
        }
    }

    fun updateTemplateVisibility(show: Boolean) {
        viewModelScope.launch {
            userPreferencesStore.setShowTemplate(show)
        }
    }

    fun setLanguageOverride(localeTag: String?) {
        viewModelScope.launch {
            userPreferencesStore.setLanguageOverride(localeTag)
        }
    }

    fun clearCourseSession() {
        _courseSession.value = null
        viewModelScope.launch { userPreferencesStore.saveCourseSession(null, null) }
    }

    private suspend fun clearUserStrokes() {
        val ids = userStrokeIds.toList()
        if (ids.isNotEmpty()) {
            renderState?.run(QuizActions.removeAllUserStrokes(ids))
        }
        userStrokeIds.clear()
        activeUserStroke = null
    }

    private fun cancelCompletionReset() {
        completionResetJob?.cancel()
        completionResetJob = null
    }

    private fun scheduleCompletionReset(symbol: String) {
        completionResetJob?.cancel()
        completionResetJob = viewModelScope.launch {
            try {
                delay(PRACTICE_COMPLETION_RESET_DELAY)
                if (currentDefinition?.symbol != symbol) return@launch
                clearUserStrokes()
                val definition = currentDefinition ?: return@launch
                renderState?.run(
                    CharacterActions.showCharacter(
                        CharacterLayer.MAIN,
                        definition,
                        PRACTICE_FADE_DURATION,
                    ),
                )
                resetPracticeState(definition)
            } finally {
                completionResetJob = null
            }
        }
    }

    private suspend fun handleCorrectStroke(isBackwards: Boolean) {
        val definition = currentDefinition ?: return
        val state = renderState ?: return
        val practice = _practiceState.value
        val strokeIndex = practice.currentStrokeIndex
        state.run(
            CharacterActions.showStroke(
                CharacterLayer.MAIN,
                strokeIndex,
                PRACTICE_ANIMATION_DURATION,
            ),
        )
        val nextIndex = strokeIndex + 1
        val complete = nextIndex >= definition.strokeCount
        val message = when {
            complete -> "Practice complete!"
            isBackwards -> "Stroke direction reversed but accepted"
            else -> "Great! Continue to the next stroke"
        }
        _practiceState.value = practice.copy(
            currentStrokeIndex = min(nextIndex, definition.strokeCount - 1),
            isComplete = complete,
            isActive = !complete,
            statusMessage = message,
            mistakeSinceHint = 0,
            completedStrokes = practice.completedStrokes + strokeIndex,
        )
        if (complete) {
            viewModelScope.launch {
                hskProgressStore.add(definition.symbol)
                practiceHistoryStore.record(
                    PracticeHistoryEntry(
                        symbol = definition.symbol,
                        timestamp = System.currentTimeMillis(),
                        totalStrokes = definition.strokeCount,
                        mistakes = practice.totalMistakes,
                        completed = true,
                    ),
                )
            }
            state.run(QuizActions.highlightCompleteChar(definition, null, 600))
            advanceCourseAfterCompletion()
            scheduleCompletionReset(definition.symbol)
        }
    }

    private fun advanceCourseAfterCompletion() {
        val session = _courseSession.value ?: return
        val symbol = currentDefinition?.symbol ?: return
        if (session.symbols.getOrNull(session.index) != symbol) return
        if (session.index >= session.symbols.lastIndex) {
            _courseSession.value = null
            viewModelScope.launch { userPreferencesStore.saveCourseSession(null, null) }
            viewModelScope.launch {
                _courseEvents.emit(CourseEvent("HSK ${session.level} complete!"))
            }
            return
        }
        val nextIndex = session.index + 1
        val nextSymbol = session.symbols[nextIndex]
        _courseSession.value = session.copy(index = nextIndex)
        viewModelScope.launch { userPreferencesStore.saveCourseSession(session.level, nextSymbol) }
        viewModelScope.launch {
            _courseEvents.emit(CourseEvent("Auto-advanced to $nextSymbol"))
        }
        loadCharacter(nextSymbol)
    }

    private fun handleMistake() {
        val practice = _practiceState.value
        val updatedMistakes = practice.totalMistakes + 1
        val mistakesSinceHint = practice.mistakeSinceHint + 1
        val showHint = mistakesSinceHint >= HINT_THRESHOLD
        _practiceState.value = practice.copy(
            totalMistakes = updatedMistakes,
            statusMessage = "Try again (mistakes $updatedMistakes)",
            mistakeSinceHint = if (showHint) 0 else mistakesSinceHint,
        )
        if (showHint) {
            triggerHint()
        }
    }

    fun requestHint() {
        if (!_practiceState.value.isActive) return
        triggerHint()
    }

    private fun triggerHint() {
        val definition = currentDefinition ?: return
        val state = renderState ?: return
        val practice = _practiceState.value
        val stroke = definition.strokes.getOrNull(practice.currentStrokeIndex) ?: return
        viewModelScope.launch {
            state.run(CharacterActions.highlightStroke(stroke, null, PRACTICE_HINT_SPEED))
        }
        _practiceState.value = practice.copy(mistakeSinceHint = 0)
    }

    override fun onCleared() {
        super.onCleared()
        renderStateJob?.cancel()
        renderState?.dispose()
    }

    companion object {
        private const val DEFAULT_CHAR = "\u6c38"
        private const val MAX_QUERY_LENGTH = 2
        private const val DEFAULT_ANIMATION_DURATION = 600L
        private const val DELAY_BETWEEN_STROKES = 150L
        private const val PRACTICE_ANIMATION_DURATION = 400L
        private const val PRACTICE_FADE_DURATION = 150L
        private const val PRACTICE_LENIENCY = 1.0
        private const val PRACTICE_DISTANCE_THRESHOLD = 350.0
        private const val HINT_THRESHOLD = 3
        private const val PRACTICE_HINT_SPEED = 3.0
        private const val PRACTICE_COMPLETION_RESET_DELAY = 3_000L
        private const val DEMO_FADE_DURATION = 250L
        private const val DEMO_REVEAL_DURATION = 120L
        private const val DEMO_LOOP_PAUSE = 600L

        fun factory(appContext: Context): ViewModelProvider.Factory {
            val applicationContext = appContext.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = CharacterDataModule.provideDefinitionRepository(applicationContext)
                    val wordRepo = WordRepository(applicationContext)
                    val hskRepo = HskRepository(applicationContext)
                    val progressStore = HskProgressStore(applicationContext)
                    val historyStore = PracticeHistoryStore(applicationContext)
                    val prefsStore = UserPreferencesStore(applicationContext)
                    return CharacterViewModel(
                        applicationContext,
                        repo,
                        wordRepo,
                        hskRepo,
                        progressStore,
                        historyStore,
                        prefsStore,
                    ) as T
                }
            }
        }
    }
}

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

data class CourseEvent(val message: String)
