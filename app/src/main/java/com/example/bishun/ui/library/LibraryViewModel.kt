package com.example.bishun.ui.library

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.bishun.data.word.WordEntry
import com.example.bishun.data.word.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LibraryUiState(
    val query: String = "",
    val result: WordEntry? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val recentSearches: List<String> = emptyList(),
)

class LibraryViewModel(
    private val wordRepository: WordRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    fun updateQuery(input: String) {
        val trimmed = input.trim()
        val limited = trimmed.take(MAX_QUERY_LENGTH)
        _uiState.value = _uiState.value.copy(query = limited)
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(result = null, errorMessage = null)
    }
    fun clearHistory() {
        _uiState.value = _uiState.value.copy(recentSearches = emptyList())
    }

    fun submitQuery() {
        val symbol = _uiState.value.query.trim()
        if (symbol.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Enter one character to search.", result = null)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching { wordRepository.getWord(symbol) }
                .onSuccess { entry ->
                    _uiState.value = if (entry != null) {
                        _uiState.value.copy(
                            isLoading = false,
                            result = entry,
                            errorMessage = null,
                            recentSearches = addRecent(symbol),
                        )
                    } else {
                        _uiState.value.copy(isLoading = false, result = null, errorMessage = "Character not found in the offline dictionary.")
                    }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, result = null, errorMessage = "Unable to read the dictionary. Please try again.")
                }
        }
    }

    fun loadCharacter(symbol: String) {
        val trimmed = symbol.trim().take(MAX_QUERY_LENGTH)
        if (trimmed.isEmpty()) return
        if (_uiState.value.query != trimmed) {
            _uiState.value = _uiState.value.copy(query = trimmed)
        }
        submitQuery()
    }

    private fun addRecent(symbol: String): List<String> {
        val current = _uiState.value.recentSearches.toMutableList()
        current.remove(symbol)
        current.add(0, symbol)
        return current.take(RECENT_LIMIT)
    }

    companion object {
        private const val MAX_QUERY_LENGTH = 2
        private const val RECENT_LIMIT = 6

        fun factory(context: Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = WordRepository(appContext)
                    return LibraryViewModel(repo) as T
                }
            }
        }
    }
}
