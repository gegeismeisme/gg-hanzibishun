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
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

data class LibraryUiState(
    val query: String = "",
    val results: List<WordEntry> = emptyList(),
    val selectedWord: String? = null,
    val isLoading: Boolean = false,
    val error: LibraryError? = null,
    val recentWords: List<String> = emptyList(),
    val pinnedRecentWords: List<String> = emptyList(),
    val favorites: List<String> = emptyList(),
    val wordDetails: Map<String, WordEntry> = emptyMap(),
)

class LibraryViewModel(
    private val wordRepository: WordRepository,
    private val userPreferencesStore: UserPreferencesStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    private val detailsMutex = Mutex()
    private val detailsInFlight = mutableSetOf<String>()

    init {
        observeLibraryData()
    }

    fun updateQuery(input: String) {
        val trimmed = input.trim()
        val limited = trimmed.take(MAX_QUERY_LENGTH)
        _uiState.value = _uiState.value.copy(query = limited)
    }

    fun clearResult() {
        _uiState.value = _uiState.value.copy(results = emptyList(), selectedWord = null, error = null)
    }

    fun clearHistory() {
        persistRecentSearches(emptyList())
        persistPinnedRecentWords(emptyList())
    }

    fun clearFavorites() {
        persistFavorites(emptyList())
    }

    fun recordRecentWord(word: String) {
        val normalized = normalizeSavedWord(word)
        if (normalized.isEmpty()) return
        persistRecentSearches(addRecentWord(normalized))
    }

    fun toggleFavorite(word: String) {
        val normalized = normalizeSavedWord(word)
        if (normalized.isEmpty()) return
        val current = _uiState.value.favorites.toMutableList()
        val removed = current.remove(normalized)
        if (!removed) current.add(0, normalized)
        persistFavorites(current.distinct().take(FAVORITES_LIMIT))
    }

    fun addFavorites(words: List<String>) {
        val normalized = words.asSequence()
            .map(::normalizeSavedWord)
            .filter { it.isNotBlank() }
            .distinct()
            .toList()
        if (normalized.isEmpty()) return

        val current = _uiState.value.favorites
        val currentSet = current.toSet()
        val toAdd = normalized.filterNot { it in currentSet }
        if (toAdd.isEmpty()) return

        val updated = (toAdd + current).distinct().take(FAVORITES_LIMIT)
        persistFavorites(updated)
    }

    fun removeFavorites(words: Collection<String>) {
        val normalized = words.asSequence()
            .map(::normalizeSavedWord)
            .filter { it.isNotBlank() }
            .toSet()
        if (normalized.isEmpty()) return

        val current = _uiState.value.favorites
        val updated = current.filterNot { it in normalized }
        if (updated.size == current.size) return
        persistFavorites(updated)
    }

    fun submitQuery() {
        val symbol = _uiState.value.query.trim()
        if (symbol.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                error = LibraryError.EmptyQuery,
                results = emptyList(),
                selectedWord = null,
            )
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            runCatching { wordRepository.searchWords(symbol) }
                .onSuccess { results ->
                    _uiState.value = if (results.isNotEmpty()) {
                        val selected = results.first().word
                        val updatedRecents = addRecentWord(selected)
                        persistRecentSearches(updatedRecents)
                        _uiState.value.copy(
                            isLoading = false,
                            results = results,
                            selectedWord = selected,
                            error = null,
                        )
                    } else {
                        _uiState.value.copy(
                            isLoading = false,
                            results = emptyList(),
                            selectedWord = null,
                            error = LibraryError.NotFound,
                        )
                    }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        results = emptyList(),
                        selectedWord = null,
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

    fun selectWord(word: String) {
        val normalized = normalizeSavedWord(word)
        if (normalized.isEmpty()) return
        if (_uiState.value.selectedWord != normalized) {
            _uiState.value = _uiState.value.copy(selectedWord = normalized)
            persistRecentSearches(addRecentWord(normalized))
        }
    }

    fun togglePinnedRecent(word: String) {
        val normalized = normalizeSavedWord(word)
        if (normalized.isEmpty()) return
        val current = _uiState.value.pinnedRecentWords.toMutableList()
        val wasPinned = current.remove(normalized)
        if (!wasPinned) {
            current.add(0, normalized)
        }
        val updatedPinned = current.distinct().take(PINNED_LIMIT)
        persistPinnedRecentWords(updatedPinned)
        val updatedRecent = ensureRecentContains(normalized)
        persistRecentSearches(trimRecentWords(updatedRecent, updatedPinned))
    }

    fun pinRecentWords(words: List<String>) {
        val normalized = words.asSequence()
            .map(::normalizeSavedWord)
            .filter { it.isNotBlank() }
            .distinct()
            .toList()
        if (normalized.isEmpty()) return

        val currentPinned = _uiState.value.pinnedRecentWords.toMutableList()
        val pinnedSet = currentPinned.toMutableSet()
        val toPin = normalized.filterNot { it in pinnedSet }
        if (toPin.isEmpty()) return

        toPin.asReversed().forEach { word ->
            currentPinned.add(0, word)
            pinnedSet.add(word)
        }
        val updatedPinned = currentPinned.distinct().take(PINNED_LIMIT)
        persistPinnedRecentWords(updatedPinned)
        val updatedRecent = ensureRecentContainsAll(toPin)
        persistRecentSearches(trimRecentWords(updatedRecent, updatedPinned))
    }

    fun unpinRecentWords(words: Collection<String>) {
        val normalized = words.asSequence()
            .map(::normalizeSavedWord)
            .filter { it.isNotBlank() }
            .toSet()
        if (normalized.isEmpty()) return

        val currentPinned = _uiState.value.pinnedRecentWords
        val updatedPinned = currentPinned.filterNot { it in normalized }
        if (updatedPinned.size == currentPinned.size) return
        persistPinnedRecentWords(updatedPinned)
        persistRecentSearches(trimRecentWords(_uiState.value.recentWords, updatedPinned))
    }

    fun removeRecentWord(word: String) {
        val normalized = normalizeSavedWord(word)
        if (normalized.isEmpty()) return
        val updatedRecent = _uiState.value.recentWords.filterNot { it == normalized }
        val updatedPinned = _uiState.value.pinnedRecentWords.filterNot { it == normalized }
        persistPinnedRecentWords(updatedPinned)
        persistRecentSearches(trimRecentWords(updatedRecent, updatedPinned))
    }

    fun removeRecentWords(words: Collection<String>) {
        val normalized = words.asSequence()
            .map(::normalizeSavedWord)
            .filter { it.isNotBlank() }
            .toSet()
        if (normalized.isEmpty()) return

        val currentRecent = _uiState.value.recentWords
        val currentPinned = _uiState.value.pinnedRecentWords
        val updatedRecent = currentRecent.filterNot { it in normalized }
        val updatedPinned = currentPinned.filterNot { it in normalized }
        if (updatedRecent.size == currentRecent.size && updatedPinned.size == currentPinned.size) return

        persistPinnedRecentWords(updatedPinned)
        persistRecentSearches(trimRecentWords(updatedRecent, updatedPinned))
    }

    private fun addRecentWord(word: String): List<String> {
        val current = _uiState.value.recentWords.toMutableList()
        current.remove(word)
        current.add(0, word)
        return trimRecentWords(current, _uiState.value.pinnedRecentWords)
    }

    private fun persistRecentSearches(entries: List<String>) {
        _uiState.value = _uiState.value.copy(recentWords = entries)
        viewModelScope.launch {
            if (entries.isEmpty()) {
                userPreferencesStore.clearLibraryRecentSearches()
            } else {
                userPreferencesStore.setLibraryRecentSearches(entries)
            }
        }
    }

    private fun persistPinnedRecentWords(entries: List<String>) {
        _uiState.value = _uiState.value.copy(pinnedRecentWords = entries)
        viewModelScope.launch {
            if (entries.isEmpty()) {
                userPreferencesStore.clearLibraryPinnedSearches()
            } else {
                userPreferencesStore.setLibraryPinnedSearches(entries)
            }
        }
    }

    private fun persistFavorites(entries: List<String>) {
        _uiState.value = _uiState.value.copy(favorites = entries)
        viewModelScope.launch {
            if (entries.isEmpty()) {
                userPreferencesStore.clearLibraryFavorites()
            } else {
                userPreferencesStore.setLibraryFavorites(entries)
            }
        }
    }

    private fun observeLibraryData() {
        viewModelScope.launch {
            userPreferencesStore.data
                .map { Triple(it.libraryRecentSearches, it.libraryPinnedSearches, it.libraryFavorites) }
                .distinctUntilChanged()
                .collect { (recents, pinned, favorites) ->
                    _uiState.value = _uiState.value.copy(
                        recentWords = recents,
                        pinnedRecentWords = pinned,
                        favorites = favorites,
                    )
                    prefetchWordDetails(recents + pinned + favorites)
                }
        }
    }

    private fun normalizeSavedWord(raw: String): String {
        return raw.trim().take(MAX_QUERY_LENGTH)
    }

    private fun prefetchWordDetails(words: List<String>) {
        val normalized = words.asSequence()
            .map { normalizeSavedWord(it) }
            .filter { it.isNotBlank() }
            .distinct()
            .toList()
        if (normalized.isEmpty()) return

        viewModelScope.launch {
            val pending = detailsMutex.withLock {
                val currentState = _uiState.value
                val missing = normalized.filterNot { word ->
                    currentState.wordDetails.containsKey(word) || detailsInFlight.contains(word)
                }
                if (missing.isNotEmpty()) {
                    detailsInFlight.addAll(missing)
                }
                missing
            }
            if (pending.isEmpty()) return@launch

            val fetched = runCatching { wordRepository.getWords(pending) }.getOrElse { emptyList() }
            detailsMutex.withLock {
                detailsInFlight.removeAll(pending.toSet())
            }
            if (fetched.isEmpty()) return@launch

            val updated = _uiState.value.wordDetails.toMutableMap()
            fetched.forEach { entry ->
                updated[entry.word] = entry
            }
            _uiState.value = _uiState.value.copy(wordDetails = updated)
        }
    }

    private fun ensureRecentContains(word: String): List<String> {
        val current = _uiState.value.recentWords.toMutableList()
        if (!current.contains(word)) {
            current.add(0, word)
        }
        return current
    }

    private fun ensureRecentContainsAll(words: List<String>): List<String> {
        if (words.isEmpty()) return _uiState.value.recentWords
        val current = _uiState.value.recentWords.toMutableList()
        words.asReversed().forEach { word ->
            if (!current.contains(word)) {
                current.add(0, word)
            }
        }
        return current
    }

    private fun trimRecentWords(words: List<String>, pinnedWords: List<String>): List<String> {
        val pinnedUnique = pinnedWords
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .take(RECENT_LIMIT)
            .toList()
        val pinnedSet = pinnedUnique.toSet()
        val normalized = words
            .asSequence()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .toList()
        val withPinned = normalized + pinnedUnique.filterNot { it in normalized }
        val allowedUnpinned = (RECENT_LIMIT - pinnedUnique.size).coerceAtLeast(0)

        val result = ArrayList<String>(RECENT_LIMIT)
        var unpinnedCount = 0
        withPinned.forEach { item ->
            if (item in pinnedSet) {
                result.add(item)
            } else if (unpinnedCount < allowedUnpinned) {
                result.add(item)
                unpinnedCount += 1
            }
        }
        return result
    }

    companion object {
        private const val MAX_QUERY_LENGTH = 32
        private const val RECENT_LIMIT = 50
        private const val PINNED_LIMIT = 20
        private const val FAVORITES_LIMIT = 200

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
