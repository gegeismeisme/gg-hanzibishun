package com.yourstudio.hskstroke.bishun.ui.character

import com.yourstudio.hskstroke.bishun.hanzi.model.CharacterDefinition
import com.yourstudio.hskstroke.bishun.hanzi.render.RenderState
import com.yourstudio.hskstroke.bishun.hanzi.render.RenderStateOptions
import com.yourstudio.hskstroke.bishun.hanzi.render.RenderStateSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

internal class PracticeRenderController(
    private val scope: CoroutineScope,
    private val snapshots: MutableStateFlow<RenderStateSnapshot?>,
) {
    private var job: Job? = null
    private var renderState: RenderState? = null

    val state: RenderState?
        get() = renderState

    fun setup(definition: CharacterDefinition) {
        job?.cancel()
        renderState?.dispose()
        val newState = RenderState(definition, RenderStateOptions())
        renderState = newState
        job = scope.launch {
            newState.state.collect { snapshot ->
                snapshots.value = snapshot
            }
        }
    }

    fun dispose() {
        job?.cancel()
        renderState?.dispose()
        job = null
        renderState = null
    }
}

