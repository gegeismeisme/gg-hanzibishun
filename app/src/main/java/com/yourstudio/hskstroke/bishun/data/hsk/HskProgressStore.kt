package com.yourstudio.hskstroke.bishun.data.hsk

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext

class HskProgressStore(
    context: Context,
) {
    private val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    private val _completed = MutableStateFlow(
        prefs.getStringSet(KEY_COMPLETED, emptySet())?.toSet() ?: emptySet(),
    )
    val completed: StateFlow<Set<String>> = _completed

    suspend fun add(symbol: String) = withContext(Dispatchers.IO) {
        val normalized = symbol.trim()
        if (normalized.isEmpty()) return@withContext
        val current = _completed.value
        if (current.contains(normalized)) return@withContext
        val updated = current + normalized
        prefs.edit().putStringSet(KEY_COMPLETED, updated.toMutableSet()).apply()
        _completed.value = updated
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.edit().remove(KEY_COMPLETED).apply()
        _completed.value = emptySet()
    }

    companion object {
        private const val PREF_NAME = "hsk_progress"
        private const val KEY_COMPLETED = "completed_symbols"
    }
}
