package com.example.bishun.ui.character

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bishun.data.characters.CharacterDefinitionRepository
import com.example.bishun.data.characters.di.CharacterDataModule
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CharacterViewModel(
    private val repository: CharacterDefinitionRepository,
) : ViewModel() {

    private val _query = MutableStateFlow(DEFAULT_CHAR)
    val query: StateFlow<String> = _query.asStateFlow()

    private val _uiState = MutableStateFlow<CharacterUiState>(CharacterUiState.Loading)
    val uiState: StateFlow<CharacterUiState> = _uiState.asStateFlow()

    init {
        loadCharacter(DEFAULT_CHAR)
    }

    fun updateQuery(input: String) {
        _query.value = input.take(MAX_QUERY_LENGTH)
    }

    fun submitQuery() {
        loadCharacter(_query.value)
    }

    private fun loadCharacter(input: String) {
        val normalized = input.trim().ifEmpty { return }
        _uiState.value = CharacterUiState.Loading
        viewModelScope.launch {
            val result = repository.load(normalized)
            _uiState.value = result.fold(
                onSuccess = { CharacterUiState.Success(it) },
                onFailure = {
                    val message = it.message ?: "加载失败，请稍后再试"
                    CharacterUiState.Error(message)
                },
            )
        }
    }

    companion object {
        private const val DEFAULT_CHAR = "永"
        private const val MAX_QUERY_LENGTH = 2

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
