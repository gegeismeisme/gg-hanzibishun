package com.example.bishun.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserPreferences(
    val analyticsOptIn: Boolean = true,
    val crashReportsOptIn: Boolean = true,
    val networkPrefetchEnabled: Boolean = false,
    val feedbackTopic: String = "",
    val feedbackMessage: String = "",
    val feedbackContact: String = "",
)

class UserPreferencesStore(private val context: Context) {

    val data: Flow<UserPreferences> = context.userPreferencesDataStore.data.map { prefs ->
        UserPreferences(
            analyticsOptIn = prefs[KEY_ANALYTICS_OPT_IN] ?: true,
            crashReportsOptIn = prefs[KEY_CRASH_OPT_IN] ?: true,
            networkPrefetchEnabled = prefs[KEY_PREFETCH] ?: false,
            feedbackTopic = prefs[KEY_FEEDBACK_TOPIC] ?: "",
            feedbackMessage = prefs[KEY_FEEDBACK_MESSAGE] ?: "",
            feedbackContact = prefs[KEY_FEEDBACK_CONTACT] ?: "",
        )
    }

    suspend fun setAnalyticsOptIn(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_ANALYTICS_OPT_IN] = enabled
        }
    }

    suspend fun setCrashReportsOptIn(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_CRASH_OPT_IN] = enabled
        }
    }

    suspend fun setNetworkPrefetch(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_PREFETCH] = enabled
        }
    }

    suspend fun saveFeedbackDraft(topic: String, message: String, contact: String) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_FEEDBACK_TOPIC] = topic
            prefs[KEY_FEEDBACK_MESSAGE] = message
            prefs[KEY_FEEDBACK_CONTACT] = contact
        }
    }

    suspend fun clearFeedbackDraft() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs.remove(KEY_FEEDBACK_TOPIC)
            prefs.remove(KEY_FEEDBACK_MESSAGE)
            prefs.remove(KEY_FEEDBACK_CONTACT)
        }
    }

    companion object {
        private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

        private val KEY_ANALYTICS_OPT_IN = booleanPreferencesKey("analytics_opt_in")
        private val KEY_CRASH_OPT_IN = booleanPreferencesKey("crash_opt_in")
        private val KEY_PREFETCH = booleanPreferencesKey("network_prefetch")
        private val KEY_FEEDBACK_TOPIC = stringPreferencesKey("feedback_topic")
        private val KEY_FEEDBACK_MESSAGE = stringPreferencesKey("feedback_message")
        private val KEY_FEEDBACK_CONTACT = stringPreferencesKey("feedback_contact")
    }
}
