package com.yourstudio.hskstroke.bishun.data.history

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json

@Serializable
data class PracticeHistoryEntry(
    val symbol: String,
    val timestamp: Long,
    val totalStrokes: Int,
    val mistakes: Int,
    val completed: Boolean,
)

class PracticeHistoryStore(private val context: Context) {

    val history: Flow<List<PracticeHistoryEntry>> = context.practiceHistoryStore.data.map { prefs ->
        val raw = prefs[KEY_HISTORY] ?: return@map emptyList()
        runCatching { json.decodeFromString(ListSerializer(PracticeHistoryEntry.serializer()), raw) }
            .getOrElse { emptyList() }
    }

    suspend fun record(entry: PracticeHistoryEntry) {
        context.practiceHistoryStore.edit { prefs ->
            val current = prefs[KEY_HISTORY]
                ?.let { runCatching { json.decodeFromString(ListSerializer(PracticeHistoryEntry.serializer()), it) }.getOrElse { emptyList() } }
                ?: emptyList()
            val updated = (current + entry).takeLast(MAX_HISTORY_ENTRIES)
            prefs[KEY_HISTORY] = json.encodeToString(ListSerializer(PracticeHistoryEntry.serializer()), updated)
        }
    }

    suspend fun clear() {
        context.practiceHistoryStore.edit { prefs ->
            prefs.remove(KEY_HISTORY)
        }
    }

    companion object {
        private const val MAX_HISTORY_ENTRIES = 40
        private val Context.practiceHistoryStore: DataStore<Preferences> by preferencesDataStore(name = "practice_history")
        private val KEY_HISTORY = stringPreferencesKey("history")
        private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }
    }
}
