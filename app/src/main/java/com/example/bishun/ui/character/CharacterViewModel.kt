package com.example.bishun.ui.character

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bishun.data.characters.CharacterDefinitionRepository
import com.example.bishun.data.characters.di.CharacterDataModule
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
) : ViewModel() {

    private val _query = MutableStateFlow(DEFAULT_CHAR)
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<CharacterUiState>(CharacterUiState.Loading)
    val uiState: StateFlow<CharacterUiState> = _uiState.asStateFlow()

    private val _renderSnapshot = MutableStateFlow<RenderStateSnapshot?>(null)
    val renderSnapshot: StateFlow<RenderStateSnapshot?> = _renderSnapshot.asStateFlow()

    private val _practiceState = MutableStateFlow(PracticeState())
    val practiceState: StateFlow<PracticeState> = _practiceState.asStateFlow()

    private var currentDefinition: CharacterDefinition? = null
    private var renderState: RenderState? = null
    private var renderStateJob: Job? = null
    private var activeUserStroke: UserStroke? = null
    private val userStrokeIds = mutableListOf<Int>()

    init {
        loadCharacter(DEFAULT_CHAR)
    }

    fun updateQuery(input: String) {
        _query.value = input.take(MAX_QUERY_LENGTH)
    }

    fun submitQuery() {
        loadCharacter(_query.value)
    }

    fun playDemo() {
        val definition = currentDefinition ?: return
        val state = renderState ?: return
        viewModelScope.launch {
            state.run(CharacterActions.hideCharacter(CharacterLayer.MAIN, definition, 150))
            state.run(CharacterActions.showStrokes(CharacterLayer.MAIN, definition, 0))
            definition.strokes.forEach { stroke ->
                state.run(
                    CharacterActions.showStroke(
                        CharacterLayer.MAIN,
                        stroke.strokeNum,
                        DEFAULT_ANIMATION_DURATION,
                    ),
                )
                delay(DELAY_BETWEEN_STROKES)
            }
        }
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
            clearUserStrokes()
            state.run(QuizActions.startQuiz(definition, PRACTICE_FADE_DURATION, 0))
            _practiceState.value = PracticeState(
                isActive = true,
                totalStrokes = definition.strokeCount,
                statusMessage = "Start from stroke 1",
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
                },
                onFailure = {
                    val message = it.message ?: "加载失败，请稍后再试"
                    _uiState.value = CharacterUiState.Error(message)
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
        )
        activeUserStroke = null
        userStrokeIds.clear()
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
        )
        if (complete) {
            state.run(QuizActions.highlightCompleteChar(definition, null, 600))
        }
    }

    private fun handleMistake() {
        val practice = _practiceState.value
        _practiceState.value = practice.copy(
            totalMistakes = practice.totalMistakes + 1,
            statusMessage = "Try again (mistakes ${practice.totalMistakes + 1})",
        )
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

        fun factory(appContext: Context): ViewModelProvider.Factory {
            val applicationContext = appContext.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = CharacterDataModule.provideDefinitionRepository(applicationContext)
                    return CharacterViewModel(repo) as T
                }
            }
        }
    }
}
