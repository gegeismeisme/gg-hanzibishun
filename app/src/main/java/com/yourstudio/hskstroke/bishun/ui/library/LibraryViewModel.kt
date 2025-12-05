package com.yourstudio.hskstroke.bishun.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import com.yourstudio.hskstroke.bishun.data.word.WordEntry
import com.yourstudio.hskstroke.bishun.data.word.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

data class LibraryUiState(
    val query: String = "",
    val result: WordEntry? = null,
    val isLoading: Boolean = false,
    val error: LibraryError? = null,
    val recentSearches: List<String> = emptyList(),
)

class LibraryViewModel(
    private val wordRepository: WordRepository,
    private val userPreferencesStore: UserPreferencesStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    init {
        observeRecentSearches()
    }

    fun updateQuery(input: String) {
        val trimmed = input.trim()
        val limited = trimmed.take(MAX_QUERY_LENGTH)
        _uiState.value = _uiState.value.copy(query = limited)
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(result = null, error = null)
    }

    fun clearHistory() {
        persistRecentSearches(emptyList())
    }

    fun submitQuery() {
        val symbol = _uiState.value.query.trim()
        if (symbol.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = LibraryError.EmptyQuery,
                result = null,
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching { wordRepository.getWord(symbol) }
                .onSuccess { entry ->
                    _uiState.value = if (entry != null) {
                        val updatedRecents = addRecent(symbol)
                        persistRecentSearches(updatedRecents)
                        _uiState.value.copy(
                            isLoading = false,
                            result = entry,
                            error = null,
                        )
                    } else {
                        _uiState.value.copy(
                            isLoading = false,
                            result = null,
                            error = LibraryError.NotFound,
                        )
                    }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        result = null,
                        error = LibraryError.ReadFailure,
                    )
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

    private fun persistRecentSearches(entries: List<String>) {
        _uiState.value = _uiState.value.copy(recentSearches = entries)
        viewModelScope.launch {
            if (entries.isEmpty()) {
                userPreferencesStore.clearLibraryRecentSearches()
            } else {
                userPreferencesStore.setLibraryRecentSearches(entries)
            }
        }
    }

    private fun observeRecentSearches() {
        viewModelScope.launch {
            userPreferencesStore.data
                .map { it.libraryRecentSearches }
                .distinctUntilChanged()
                .collect { recents ->
                    _uiState.value = _uiState.value.copy(recentSearches = recents)
                }
        }
    }

    companion object {
        private const val MAX_QUERY_LENGTH = 2
        private const val RECENT_LIMIT = 50

        fun factory(context: android.content.Context): ViewModelProvider.Factory {
            val appContext = context.applicationContext
            return object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val repo = WordRepository(appContext)
                    val prefs = UserPreferencesStore(appContext)
                    return LibraryViewModel(repo, prefs) as T
                }
            }
        }
    }
}

sealed class LibraryError {
    data object EmptyQuery : LibraryError()
    data object NotFound : LibraryError()
    data object ReadFailure : LibraryError()
}
