package com.yourstudio.hskstroke.bishun.ui.flashcard

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.yourstudio.hskstroke.bishun.data.flashcard.FlashcardDatabaseProvider
import com.yourstudio.hskstroke.bishun.data.flashcard.FlashcardRepository
import com.yourstudio.hskstroke.bishun.data.flashcard.FlashcardSeedLoader
import com.yourstudio.hskstroke.bishun.data.flashcard.FlashcardStats
import com.yourstudio.hskstroke.bishun.data.flashcard.StudyCard
import com.yourstudio.hskstroke.bishun.data.flashcard.StudyRating
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

sealed class FlashcardUiState {
    data object Loading : FlashcardUiState()
    data class Ready(
        val card: StudyCard?,
        val isRevealed: Boolean,
        val stats: FlashcardStats,
        val selectedLevel: Int,
        val isPro: Boolean,
        val sessionReviewed: Int,
    ) : FlashcardUiState()
    data class Done(
        val sessionReviewed: Int,
        val stats: FlashcardStats,
    ) : FlashcardUiState()
}

class FlashcardViewModel(
    private val repository: FlashcardRepository,
    private val preferencesStore: UserPreferencesStore,
) : ViewModel() {

    private val _state = MutableStateFlow<FlashcardUiState>(FlashcardUiState.Loading)
    val state: StateFlow<FlashcardUiState> = _state.asStateFlow()

    private var isPro = false
    private var selectedLevel = 0
    private var sessionReviewed = 0

    init {
        viewModelScope.launch {
            isPro = preferencesStore.data.first().isPro
            withContext(Dispatchers.IO) {
                repository.seedIfNeeded()
            }
            loadNextCard()
        }
    }

    fun selectLevel(level: Int) {
        selectedLevel = level
        sessionReviewed = 0
        viewModelScope.launch {
            loadNextCard()
        }
    }

    fun revealCard() {
        val current = _state.value
        if (current is FlashcardUiState.Ready) {
            _state.value = current.copy(isRevealed = true)
        }
    }

    fun submitRating(rating: StudyRating) {
        val current = _state.value
        if (current !is FlashcardUiState.Ready || current.card == null) return

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                repository.submitReview(current.card, rating)
            }
            sessionReviewed++
            loadNextCard()
        }
    }

    private suspend fun loadNextCard() {
        val card = withContext(Dispatchers.IO) {
            repository.getNextCard(isPro, selectedLevel)
        }
        val stats = withContext(Dispatchers.IO) {
            repository.getStats(isPro, selectedLevel)
        }

        if (card == null && sessionReviewed > 0) {
            _state.value = FlashcardUiState.Done(sessionReviewed, stats)
        } else {
            _state.value = FlashcardUiState.Ready(
                card = card,
                isRevealed = false,
                stats = stats,
                selectedLevel = selectedLevel,
                isPro = isPro,
                sessionReviewed = sessionReviewed,
            )
        }
    }

    companion object {
        fun factory(context: Context): ViewModelProvider.Factory {
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val db = FlashcardDatabaseProvider.get(context)
                    val dao = db.flashcardDao()
                    val seedLoader = FlashcardSeedLoader(context)
                    val repo = FlashcardRepository(dao, seedLoader)
                    val prefs = UserPreferencesStore(context)
                    @Suppress("UNCHECKED_CAST")
                    return FlashcardViewModel(repo, prefs) as T
                }
            }
        }
    }
}
