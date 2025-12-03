package com.example.bishun.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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
    val gridMode: Int = 0,
    val strokeColor: Int = 0,
    val showTemplate: Boolean = true,
    val courseLevel: Int? = null,
    val courseSymbol: String? = null,
    val languageOverride: String? = null,
    val isAccountSignedIn: Boolean = false,
    val unlockedCourseLevels: Set<Int> = emptySet(),
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
            gridMode = prefs[KEY_GRID_MODE] ?: 0,
            strokeColor = prefs[KEY_STROKE_COLOR] ?: 0,
            showTemplate = prefs[KEY_SHOW_TEMPLATE] ?: true,
            courseLevel = prefs[KEY_COURSE_LEVEL],
            courseSymbol = prefs[KEY_COURSE_SYMBOL],
            languageOverride = prefs[KEY_LANGUAGE_OVERRIDE],
            isAccountSignedIn = prefs[KEY_ACCOUNT_SIGNED_IN] ?: false,
            unlockedCourseLevels = prefs[KEY_UNLOCKED_LEVELS]
                ?.mapNotNull { it.toIntOrNull() }
                ?.toSet()
                ?: emptySet(),
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

    suspend fun setGridMode(index: Int) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_GRID_MODE] = index
        }
    }

    suspend fun setStrokeColor(index: Int) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_STROKE_COLOR] = index
        }
    }

    suspend fun setShowTemplate(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_SHOW_TEMPLATE] = enabled
        }
    }

    suspend fun saveCourseSession(level: Int?, symbol: String?) {
        context.userPreferencesDataStore.edit { prefs ->
            if (level == null || symbol == null) {
                prefs.remove(KEY_COURSE_LEVEL)
                prefs.remove(KEY_COURSE_SYMBOL)
            } else {
                prefs[KEY_COURSE_LEVEL] = level
                prefs[KEY_COURSE_SYMBOL] = symbol
            }
        }
    }

    suspend fun setLanguageOverride(localeTag: String?) {
        context.userPreferencesDataStore.edit { prefs ->
            if (localeTag.isNullOrBlank()) {
                prefs.remove(KEY_LANGUAGE_OVERRIDE)
            } else {
                prefs[KEY_LANGUAGE_OVERRIDE] = localeTag
            }
        }
    }

    suspend fun setAccountSignedIn(signedIn: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_ACCOUNT_SIGNED_IN] = signedIn
            if (!signedIn) {
                prefs.remove(KEY_UNLOCKED_LEVELS)
            }
        }
    }

    suspend fun unlockCourseLevels(levels: Set<Int>) {
        if (levels.isEmpty()) return
        context.userPreferencesDataStore.edit { prefs ->
            val current = prefs[KEY_UNLOCKED_LEVELS] ?: emptySet()
            val updated = current + levels.map { it.toString() }
            prefs[KEY_UNLOCKED_LEVELS] = updated.toSet()
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
        private val KEY_GRID_MODE = intPreferencesKey("grid_mode_index")
        private val KEY_STROKE_COLOR = intPreferencesKey("stroke_color_index")
        private val KEY_SHOW_TEMPLATE = booleanPreferencesKey("show_template")
        private val KEY_COURSE_LEVEL = intPreferencesKey("course_level")
        private val KEY_COURSE_SYMBOL = stringPreferencesKey("course_symbol")
        private val KEY_LANGUAGE_OVERRIDE = stringPreferencesKey("language_override")
        private val KEY_ACCOUNT_SIGNED_IN = booleanPreferencesKey("account_signed_in")
        private val KEY_UNLOCKED_LEVELS = stringSetPreferencesKey("unlocked_course_levels")
    }
}
