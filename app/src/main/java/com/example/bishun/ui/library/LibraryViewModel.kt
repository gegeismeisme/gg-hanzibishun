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
    val query: String = "永",
    val result: WordEntry? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
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

    fun submitQuery() {
        val symbol = _uiState.value.query.trim()
        if (symbol.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "请输入汉字进行查询。", result = null)
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            runCatching { wordRepository.getWord(symbol) }
                .onSuccess { entry ->
                    _uiState.value = if (entry != null) {
                        _uiState.value.copy(isLoading = false, result = entry, errorMessage = null)
                    } else {
                        _uiState.value.copy(isLoading = false, result = null, errorMessage = "未在离线字典中找到该字。")
                    }
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(isLoading = false, result = null, errorMessage = "读取字典时出错，请稍后重试。")
                }
        }
    }

    fun loadCharacter(symbol: String) {
        updateQuery(symbol)
        submitQuery()
    }

    companion object {
        private const val MAX_QUERY_LENGTH = 2

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
