package com.example.bishun.ui.character

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bishun.data.characters.CharacterDefinitionRepository
import com.example.bishun.data.characters.di.CharacterDataModule
import com.example.bishun.hanzi.model.CharacterDefinition
import com.example.bishun.hanzi.render.RenderState
import com.example.bishun.hanzi.render.RenderStateOptions
import com.example.bishun.hanzi.render.RenderStateSnapshot
import com.example.bishun.hanzi.render.actions.CharacterActions
import com.example.bishun.hanzi.render.actions.CharacterLayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

class CharacterViewModel(
    private val repository: CharacterDefinitionRepository,
) : ViewModel() {

    private val _query = MutableStateFlow(DEFAULT_CHAR)
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<CharacterUiState>(CharacterUiState.Loading)
    val uiState: StateFlow<CharacterUiState> = _uiState.asStateFlow()

    private val _renderSnapshot = MutableStateFlow<RenderStateSnapshot?>(null)
    val renderSnapshot: StateFlow<RenderStateSnapshot?> = _renderSnapshot.asStateFlow()

    private var currentDefinition: CharacterDefinition? = null
    private var renderState: RenderState? = null
    private var renderStateJob: Job? = null

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
            // reset character then animate each stroke sequentially
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

    override fun onCleared() {
        super.onCleared()
        renderStateJob?.cancel()
        renderState?.dispose()
    }

    companion object {
        private const val DEFAULT_CHAR = "永"
        private const val MAX_QUERY_LENGTH = 2
        private const val DEFAULT_ANIMATION_DURATION = 600L
        private const val DELAY_BETWEEN_STROKES = 150L

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
