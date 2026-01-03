package com.yourstudio.hskstroke.bishun.ui.character

sealed interface WordInfoUiState {
    data object Idle : WordInfoUiState
    data object Loading : WordInfoUiState
    data object Loaded : WordInfoUiState
    data object NotFound : WordInfoUiState
    data class Error(val error: WordInfoError) : WordInfoUiState
}

sealed interface WordInfoError {
    data object LoadFailed : WordInfoError
}
