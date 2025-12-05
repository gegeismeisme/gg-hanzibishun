package com.example.bishun.hanzi.render

import com.example.bishun.hanzi.model.CharacterDefinition
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.max
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class RenderState(
    character: CharacterDefinition,
    private val options: RenderStateOptions = RenderStateOptions(),
    dispatcher: CoroutineDispatcher = Dispatchers.Default,
) {

    private val mutationMutex = Mutex()
    private val scope = CoroutineScope(dispatcher + SupervisorJob())
    private val scopedAnimations = ConcurrentHashMap<String, Job>()

    private val _state = MutableStateFlow(RenderStateFactory.create(character, options))
    val state: StateFlow<RenderStateSnapshot> = _state.asStateFlow()

    suspend fun setCharacter(character: CharacterDefinition) {
        mutationMutex.withLock {
            _state.value = RenderStateFactory.create(character, options)
        }
    }

    suspend fun run(mutations: List<Mutation>) {
        for (mutation in mutations) {
            applyMutation(mutation)
        }
    }

    suspend fun run(vararg mutations: Mutation) = run(mutations.toList())

    private suspend fun applyMutation(mutation: Mutation) {
        val animationJob = mutationMutex.withLock {
            if (!mutation.force) {
                scopedAnimations.remove(mutation.scope)?.cancel()
            }
            val current = _state.value
            val target = mutation.targetState(current)
            if (mutation.durationMillis <= 0L) {
                _state.value = target
                null
            } else {
                scope.launch {
                    animateBetween(current, target, mutation.durationMillis)
                }.also { job ->
                    scopedAnimations[mutation.scope] = job
                }
            }
        }
        animationJob?.join()
    }

    private suspend fun animateBetween(
        start: RenderStateSnapshot,
        end: RenderStateSnapshot,
        durationMillis: Long,
    ) {
        val frameDuration = 16L
        val steps = max(1, (durationMillis / frameDuration).toInt())
        repeat(steps) { index ->
            val progress = (index + 1) / steps.toFloat()
            _state.update { start.interpolate(end, progress) }
            delay(frameDuration)
        }
        _state.value = end
    }

    fun dispose() {
        scope.coroutineContext.cancel()
    }
}
