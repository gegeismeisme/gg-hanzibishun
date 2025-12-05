package com.example.bishun.ui.practice

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay

data class CalligraphyDemoState(
    val isPlaying: Boolean = false,
    val strokeProgress: List<Float> = emptyList(),
)

private data class CalligraphyDemoCommand(val loop: Boolean, val token: Int)

class CalligraphyDemoController(
    val state: State<CalligraphyDemoState>,
    val play: (Boolean) -> Unit,
    val stop: () -> Unit,
)

@Composable
fun rememberCalligraphyDemoController(
    strokeCount: Int,
    definitionKey: Any,
): CalligraphyDemoController {
    val demoState = remember(definitionKey, strokeCount) {
        mutableStateOf(
            CalligraphyDemoState(
                isPlaying = false,
                strokeProgress = List(strokeCount) { 0f },
            ),
        )
    }
    var command by remember(definitionKey) { mutableStateOf<CalligraphyDemoCommand?>(null) }
    var tokenCounter by remember(definitionKey) { mutableStateOf(0) }

    LaunchedEffect(definitionKey, strokeCount) {
        command = null
        demoState.value = CalligraphyDemoState(
            isPlaying = false,
            strokeProgress = List(strokeCount) { 0f },
        )
    }

    LaunchedEffect(command, strokeCount, definitionKey) {
        val cmd = command ?: return@LaunchedEffect
        if (strokeCount <= 0) {
            demoState.value = CalligraphyDemoState(isPlaying = false, strokeProgress = emptyList())
            command = null
            return@LaunchedEffect
        }
        val progress = FloatArray(strokeCount) { 0f }
        demoState.value = CalligraphyDemoState(isPlaying = true, strokeProgress = progress.toList())
        outer@ while (command == cmd) {
            for (index in 0 until strokeCount) {
                val anim = Animatable(progress[index])
                anim.snapTo(0f)
                anim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = CALLIGRAPHY_DEMO_STROKE_DURATION, easing = LinearEasing),
                ) {
                    progress[index] = value
                    demoState.value = CalligraphyDemoState(isPlaying = true, strokeProgress = progress.toList())
                }
                if (command != cmd) break@outer
                delay(CALLIGRAPHY_DEMO_STROKE_GAP)
            }
            if (cmd.loop && command == cmd) {
                delay(CALLIGRAPHY_DEMO_LOOP_PAUSE)
                for (i in 0 until strokeCount) {
                    progress[i] = 0f
                }
                demoState.value = CalligraphyDemoState(isPlaying = true, strokeProgress = progress.toList())
            } else {
                break
            }
        }
        if (command == cmd) {
            command = null
            demoState.value = CalligraphyDemoState(
                isPlaying = false,
                strokeProgress = List(strokeCount) { 0f },
            )
        }
    }

    val play: (Boolean) -> Unit = { loop ->
        tokenCounter += 1
        command = CalligraphyDemoCommand(loop = loop, token = tokenCounter)
        demoState.value = demoState.value.copy(isPlaying = true)
    }
    val stop: () -> Unit = {
        command = null
        demoState.value = CalligraphyDemoState(
            isPlaying = false,
            strokeProgress = List(strokeCount) { 0f },
        )
    }

    return CalligraphyDemoController(state = demoState, play = play, stop = stop)
}

private const val CALLIGRAPHY_DEMO_STROKE_DURATION = 600
private const val CALLIGRAPHY_DEMO_STROKE_GAP = 120L
private const val CALLIGRAPHY_DEMO_LOOP_PAUSE = 400L
