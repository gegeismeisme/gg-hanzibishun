package com.yourstudio.hskstroke.bishun.ui.character

import com.yourstudio.hskstroke.bishun.data.word.WordEntry
import com.yourstudio.hskstroke.bishun.data.word.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

internal class WordInfoController(
    private val wordRepository: WordRepository,
    private val scope: CoroutineScope,
    private val currentSymbol: () -> String?,
) {
    private val _wordEntry = MutableStateFlow<WordEntry?>(null)
    val wordEntry: StateFlow<WordEntry?> = _wordEntry.asStateFlow()

    private val _uiState = MutableStateFlow<WordInfoUiState>(WordInfoUiState.Idle)
    val uiState: StateFlow<WordInfoUiState> = _uiState.asStateFlow()

    private var job: Job? = null

    fun reset() {
        job?.cancel()
        job = null
        _wordEntry.value = null
        _uiState.value = WordInfoUiState.Idle
    }

    fun request(symbol: String?) {
        val normalized = symbol?.trim().takeIf { !it.isNullOrEmpty() } ?: return
        val currentEntry = _wordEntry.value
        val currentState = _uiState.value
        if (currentEntry?.word == normalized && currentState is WordInfoUiState.Loaded) return
        if (currentState is WordInfoUiState.Loading) return

        job?.cancel()
        _uiState.value = WordInfoUiState.Loading

        job = scope.launch {
            runCatching { wordRepository.getWord(normalized) }
                .onSuccess { entry ->
                    if (currentSymbol()?.trim() != normalized) return@launch
                    if (entry == null) {
                        _wordEntry.value = null
                        _uiState.value = WordInfoUiState.NotFound
                    } else {
                        _wordEntry.value = entry
                        _uiState.value = WordInfoUiState.Loaded
                    }
                }
                .onFailure { throwable ->
                    if (currentSymbol()?.trim() != normalized) return@launch
                    _wordEntry.value = null
                    _uiState.value = WordInfoUiState.Error(
                        throwable.message ?: "Unable to load dictionary entry.",
                    )
                }
        }
    }
}

