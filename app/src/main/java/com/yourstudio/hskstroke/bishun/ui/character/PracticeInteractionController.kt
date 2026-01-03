package com.yourstudio.hskstroke.bishun.ui.character

import com.yourstudio.hskstroke.bishun.hanzi.core.HanziCounter
import com.yourstudio.hskstroke.bishun.hanzi.model.CharacterDefinition
import com.yourstudio.hskstroke.bishun.hanzi.model.Point
import com.yourstudio.hskstroke.bishun.hanzi.model.UserStroke
import com.yourstudio.hskstroke.bishun.hanzi.render.RenderState
import com.yourstudio.hskstroke.bishun.hanzi.render.RenderStateSnapshot
import com.yourstudio.hskstroke.bishun.hanzi.render.actions.CharacterActions
import com.yourstudio.hskstroke.bishun.hanzi.render.actions.CharacterLayer
import com.yourstudio.hskstroke.bishun.hanzi.render.actions.QuizActions
import com.yourstudio.hskstroke.bishun.hanzi.quiz.StrokeMatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlin.math.min

internal class PracticeInteractionController(
    private val scope: CoroutineScope,
    private val practiceState: MutableStateFlow<PracticeState>,
    private val demoState: MutableStateFlow<DemoState>,
    private val definitionProvider: () -> CharacterDefinition?,
    private val renderStateProvider: () -> RenderState?,
    private val renderSnapshotProvider: () -> RenderStateSnapshot?,
    private val onPracticeCompleted: (PracticeCompletion) -> Unit,
) {
    data class PracticeCompletion(
        val symbol: String,
        val totalStrokes: Int,
        val mistakes: Int,
    )

    private var completionResetJob: Job? = null
    private var activeUserStroke: UserStroke? = null
    private val userStrokeIds = mutableListOf<Int>()

    fun onDefinitionLoaded(definition: CharacterDefinition) {
        stopDemo()
        cancelCompletionReset()
        resetPracticeState(definition)
        activeUserStroke = null
        userStrokeIds.clear()
    }

    fun playDemo(loop: Boolean) {
        val definition = definitionProvider() ?: return
        val state = renderStateProvider() ?: return
        if (demoState.value.isPlaying) return
        demoState.value = DemoState(isPlaying = true, loop = loop)
        scope.launch {
            val practice = practiceState.value
            if (practice.isActive || practice.completedStrokes.isNotEmpty()) {
                cancelCompletionReset()
                clearUserStrokes()
                resetPracticeState(definition)
            }
            do {
                state.run(CharacterActions.hideCharacter(CharacterLayer.MAIN, definition, DEMO_FADE_DURATION))
                state.run(CharacterActions.prepareLayerForAnimation(CharacterLayer.MAIN, definition))
                for (stroke in definition.strokes) {
                    if (!demoState.value.isPlaying) {
                        demoState.value = DemoState()
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
                if (demoState.value.loop) {
                    delay(DEMO_LOOP_PAUSE)
                }
            } while (demoState.value.loop && demoState.value.isPlaying)
            demoState.value = DemoState()
        }
    }

    fun stopDemo() {
        demoState.value = DemoState()
    }

    fun resetCharacter() {
        val definition = definitionProvider() ?: return
        val state = renderStateProvider() ?: return
        scope.launch {
            state.run(CharacterActions.showCharacter(CharacterLayer.MAIN, definition, 200))
            cancelCompletionReset()
            resetPracticeState(definition)
        }
    }

    fun startPractice() {
        val definition = definitionProvider() ?: return
        val state = renderStateProvider() ?: return
        cancelCompletionReset()
        scope.launch {
            stopDemo()
            state.run(CharacterActions.showCharacter(CharacterLayer.MAIN, definition, PRACTICE_FADE_DURATION))
            clearUserStrokes()
            state.run(QuizActions.startQuiz(definition, PRACTICE_FADE_DURATION, 0))
            practiceState.value = PracticeState(
                isActive = true,
                totalStrokes = definition.strokeCount,
                status = PracticeStatus.StartFromStroke(1),
                mistakeSinceHint = 0,
                completedStrokes = emptySet(),
            )
        }
    }

    fun onPracticeStrokeStart(charPoint: Point, externalPoint: Point) {
        val state = renderStateProvider() ?: return
        val currentPractice = practiceState.value
        if (!currentPractice.isActive) return
        if (currentPractice.status != PracticeStatus.None) {
            practiceState.value = currentPractice.copy(status = PracticeStatus.None)
        }
        val strokeId = HanziCounter.next()
        val userStroke = UserStroke(strokeId, charPoint, externalPoint)
        activeUserStroke = userStroke
        userStrokeIds.add(strokeId)
        scope.launch {
            state.run(QuizActions.startUserStroke(strokeId, charPoint))
        }
    }

    fun onPracticeStrokeMove(charPoint: Point, externalPoint: Point) {
        val state = renderStateProvider() ?: return
        val stroke = activeUserStroke ?: return
        stroke.append(charPoint, externalPoint)
        scope.launch {
            state.run(QuizActions.updateUserStroke(stroke.id, stroke.points))
        }
    }

    fun onPracticeStrokeEnd() {
        val definition = definitionProvider() ?: return
        val state = renderStateProvider() ?: return
        val practice = practiceState.value
        val stroke = activeUserStroke ?: return
        activeUserStroke = null
        val fadeDuration = renderSnapshotProvider()?.options?.drawingFadeDuration ?: PRACTICE_FADE_DURATION
        scope.launch { state.run(QuizActions.hideUserStroke(stroke.id, fadeDuration)) }
        if (!practice.isActive || stroke.points.size < 2) return

        scope.launch {
            val snapshot = renderSnapshotProvider()
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

    fun requestHint() {
        if (!practiceState.value.isActive) return
        triggerHint()
    }

    private fun triggerHint() {
        val definition = definitionProvider() ?: return
        val state = renderStateProvider() ?: return
        val practice = practiceState.value
        val stroke = definition.strokes.getOrNull(practice.currentStrokeIndex) ?: return
        scope.launch { state.run(CharacterActions.highlightStroke(stroke, null, PRACTICE_HINT_SPEED)) }
        practiceState.value = practice.copy(mistakeSinceHint = 0)
    }

    private fun resetPracticeState(definition: CharacterDefinition) {
        practiceState.value = PracticeState(
            isActive = false,
            isComplete = false,
            totalStrokes = definition.strokeCount,
            currentStrokeIndex = 0,
            totalMistakes = 0,
            status = PracticeStatus.None,
            mistakeSinceHint = 0,
            completedStrokes = emptySet(),
        )
    }

    private suspend fun clearUserStrokes() {
        val ids = userStrokeIds.toList()
        if (ids.isNotEmpty()) {
            renderStateProvider()?.run(QuizActions.removeAllUserStrokes(ids))
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
        completionResetJob = scope.launch {
            try {
                delay(PRACTICE_COMPLETION_RESET_DELAY)
                if (definitionProvider()?.symbol != symbol) return@launch
                clearUserStrokes()
                val definition = definitionProvider() ?: return@launch
                renderStateProvider()?.run(
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
        val definition = definitionProvider() ?: return
        val state = renderStateProvider() ?: return
        val practice = practiceState.value
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
        val status = when {
            complete -> PracticeStatus.Complete
            isBackwards -> PracticeStatus.BackwardsAccepted
            else -> PracticeStatus.GreatContinue
        }
        practiceState.value = practice.copy(
            currentStrokeIndex = min(nextIndex, definition.strokeCount - 1),
            isComplete = complete,
            isActive = !complete,
            status = status,
            mistakeSinceHint = 0,
            completedStrokes = practice.completedStrokes + strokeIndex,
        )
        if (complete) {
            onPracticeCompleted(
                PracticeCompletion(
                    symbol = definition.symbol,
                    totalStrokes = definition.strokeCount,
                    mistakes = practice.totalMistakes,
                ),
            )
            state.run(QuizActions.highlightCompleteChar(definition, null, 600))
            scheduleCompletionReset(definition.symbol)
        }
    }

    private fun handleMistake() {
        val practice = practiceState.value
        val updatedMistakes = practice.totalMistakes + 1
        val mistakesSinceHint = practice.mistakeSinceHint + 1
        val showHint = mistakesSinceHint >= HINT_THRESHOLD
        practiceState.value = practice.copy(
            totalMistakes = updatedMistakes,
            status = PracticeStatus.TryAgain(updatedMistakes),
            mistakeSinceHint = if (showHint) 0 else mistakesSinceHint,
        )
        if (showHint) {
            triggerHint()
        }
    }

    companion object {
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
    }
}
