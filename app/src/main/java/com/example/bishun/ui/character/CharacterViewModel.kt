package com.example.bishun.ui.character

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bishun.data.characters.CharacterDefinitionRepository
import com.example.bishun.data.characters.di.CharacterDataModule
import com.example.bishun.data.word.WordEntry
import com.example.bishun.data.word.WordRepository
import com.example.bishun.data.hsk.HskEntry
import com.example.bishun.data.hsk.HskRepository
import com.example.bishun.data.hsk.HskProgressStore
import com.example.bishun.data.settings.UserPreferences
import com.example.bishun.data.settings.UserPreferencesStore
import com.example.bishun.hanzi.core.HanziCounter
import com.example.bishun.hanzi.model.CharacterDefinition
import com.example.bishun.hanzi.model.Point
import com.example.bishun.hanzi.model.UserStroke
import com.example.bishun.hanzi.render.RenderState
import com.example.bishun.hanzi.render.RenderStateOptions
import com.example.bishun.hanzi.render.RenderStateSnapshot
import com.example.bishun.hanzi.render.actions.CharacterActions
import com.example.bishun.hanzi.render.actions.CharacterLayer
import com.example.bishun.hanzi.render.actions.QuizActions
import com.example.bishun.hanzi.quiz.StrokeMatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.min

class CharacterViewModel(
    private val repository: CharacterDefinitionRepository,
    private val wordRepository: WordRepository,
    private val hskRepository: HskRepository,
    private val hskProgressStore: HskProgressStore,
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
    private val _hskEntry = MutableStateFlow<HskEntry?>(null)
    val hskEntry: StateFlow<HskEntry?> = _hskEntry.asStateFlow()
    private val _hskProgress = MutableStateFlow(HskProgressSummary())
    val hskProgress: StateFlow<HskProgressSummary> = _hskProgress.asStateFlow()
    private val _userPreferences = MutableStateFlow(UserPreferences())
    val userPreferences: StateFlow<UserPreferences> = _userPreferences.asStateFlow()

    private var currentDefinition: CharacterDefinition? = null
    private var renderState: RenderState? = null
    private var renderStateJob: Job? = null
    private var activeUserStroke: UserStroke? = null
    private val userStrokeIds = mutableListOf<Int>()

    init {
        observeHskProgress()
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

    fun playDemo(loop: Boolean = false) {
        val definition = currentDefinition ?: return
        val state = renderState ?: return
        if (_demoState.value.isPlaying) return
        _demoState.value = DemoState(isPlaying = true, loop = loop)
        viewModelScope.launch {
            if (_practiceState.value.isActive || _practiceState.value.completedStrokes.isNotEmpty()) {
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
            resetPracticeState(definition)
        }
    }

    fun startPractice() {
        val definition = currentDefinition ?: return
        val state = renderState ?: return
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
                    resetPracticeState(it)
                    loadWordInfo(it.symbol)
                    loadHskInfo(it.symbol)
                },
                onFailure = {
                    val message = it.message ?: "加载失败，请稍后再试"
                    _uiState.value = CharacterUiState.Error(message)
                    _wordEntry.value = null
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

    private fun loadWordInfo(symbol: String) {
        viewModelScope.launch {
            runCatching { wordRepository.getWord(symbol) }
                .onSuccess { _wordEntry.value = it }
                .onFailure { _wordEntry.value = null }
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
                val entries = hskRepository.allEntries()
                val perLevel = mutableMapOf<Int, HskLevelSummary>()
                val nextTargets = mutableMapOf<Int, String?>()
                entries.groupBy { it.level }.forEach { (level, items) ->
                    val sorted = items.sortedBy { it.writingLevel ?: Int.MAX_VALUE }
                    val done = items.count { completed.contains(it.symbol) }
                    perLevel[level] = HskLevelSummary(done, items.size)
                    nextTargets[level] = sorted.firstOrNull { !completed.contains(it.symbol) }?.symbol
                }
                _hskProgress.value = HskProgressSummary(perLevel, nextTargets)
            }
        }
    }

    private fun observeUserPreferences() {
        viewModelScope.launch {
            userPreferencesStore.data.collect { prefs ->
                _userPreferences.value = prefs
            }
        }
    }

    fun setAnalyticsOptIn(enabled: Boolean) {
        viewModelScope.launch { userPreferencesStore.setAnalyticsOptIn(enabled) }
    }

    fun setCrashReportsOptIn(enabled: Boolean) {
        viewModelScope.launch { userPreferencesStore.setCrashReportsOptIn(enabled) }
    }

    fun setNetworkPrefetch(enabled: Boolean) {
        viewModelScope.launch { userPreferencesStore.setNetworkPrefetch(enabled) }
    }

    fun saveFeedbackDraft(topic: String, message: String, contact: String) {
        viewModelScope.launch { userPreferencesStore.saveFeedbackDraft(topic, message, contact) }
    }

    fun clearFeedbackDraft() {
        viewModelScope.launch { userPreferencesStore.clearFeedbackDraft() }
    }

    private suspend fun clearUserStrokes() {
        val ids = userStrokeIds.toList()
        if (ids.isNotEmpty()) {
            renderState?.run(QuizActions.removeAllUserStrokes(ids))
        }
        userStrokeIds.clear()
        activeUserStroke = null
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
            viewModelScope.launch { hskProgressStore.add(definition.symbol) }
            state.run(QuizActions.highlightCompleteChar(definition, null, 600))
        }
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
                    val prefsStore = UserPreferencesStore(applicationContext)
                    return CharacterViewModel(repo, wordRepo, hskRepo, progressStore, prefsStore) as T
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
