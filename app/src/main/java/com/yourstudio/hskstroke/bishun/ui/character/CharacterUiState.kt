package com.yourstudio.hskstroke.bishun.ui.character

import com.yourstudio.hskstroke.bishun.hanzi.model.CharacterDefinition

sealed interface CharacterUiState {
    data object Loading : CharacterUiState
    data class Success(val definition: CharacterDefinition) : CharacterUiState
    data class Error(val error: CharacterLoadError) : CharacterUiState
}

sealed interface CharacterLoadError {
    data object NotFound : CharacterLoadError
    data object LoadFailed : CharacterLoadError
}
