package com.example.bishun.ui.character

import com.example.bishun.hanzi.model.CharacterDefinition

sealed interface CharacterUiState {
    data object Loading : CharacterUiState
    data class Success(val definition: CharacterDefinition) : CharacterUiState
    data class Error(val message: String) : CharacterUiState
}
