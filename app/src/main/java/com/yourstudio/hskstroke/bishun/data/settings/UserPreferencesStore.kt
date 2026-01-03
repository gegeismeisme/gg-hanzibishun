package com.yourstudio.hskstroke.bishun.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserPreferences(
    val gridMode: Int = 0,
    val strokeColor: Int = 0,
    val showTemplate: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.System,
    val volumeSafetyEnabled: Boolean = true,
    val volumeSafetyThresholdPercent: Int = 80,
    val volumeSafetyLowerToPercent: Int = 30,
    val onboardingCompleted: Boolean = false,
    val courseLevel: Int? = null,
    val courseSymbol: String? = null,
    val languageOverride: String? = null,
    val libraryRecentSearches: List<String> = emptyList(),
)

class UserPreferencesStore(private val context: Context) {

    val data: Flow<UserPreferences> = context.userPreferencesDataStore.data.map { prefs ->
        UserPreferences(
            gridMode = prefs[KEY_GRID_MODE] ?: 0,
            strokeColor = prefs[KEY_STROKE_COLOR] ?: 0,
            showTemplate = prefs[KEY_SHOW_TEMPLATE] ?: true,
            themeMode = ThemeMode.fromStoredValue(prefs[KEY_THEME_MODE]),
            volumeSafetyEnabled = prefs[KEY_VOLUME_SAFETY_ENABLED] ?: true,
            volumeSafetyThresholdPercent = prefs[KEY_VOLUME_SAFETY_THRESHOLD] ?: 80,
            volumeSafetyLowerToPercent = prefs[KEY_VOLUME_SAFETY_LOWER_TO] ?: 30,
            onboardingCompleted = prefs[KEY_ONBOARDING_COMPLETED] ?: prefs.asMap().isNotEmpty(),
            courseLevel = prefs[KEY_COURSE_LEVEL],
            courseSymbol = prefs[KEY_COURSE_SYMBOL],
            languageOverride = prefs[KEY_LANGUAGE_OVERRIDE],
            libraryRecentSearches = decodeRecentSearches(prefs[KEY_LIBRARY_RECENTS]),
        )
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

    suspend fun setThemeMode(themeMode: ThemeMode) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_THEME_MODE] = themeMode.storedValue
        }
    }

    suspend fun setVolumeSafetyEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_VOLUME_SAFETY_ENABLED] = enabled
        }
    }

    suspend fun setVolumeSafetyThresholdPercent(percent: Int) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_VOLUME_SAFETY_THRESHOLD] = percent.coerceIn(0, 100)
        }
    }

    suspend fun setVolumeSafetyLowerToPercent(percent: Int) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_VOLUME_SAFETY_LOWER_TO] = percent.coerceIn(0, 100)
        }
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_ONBOARDING_COMPLETED] = completed
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

    suspend fun setLibraryRecentSearches(entries: List<String>) {
        context.userPreferencesDataStore.edit { prefs ->
            if (entries.isEmpty()) {
                prefs.remove(KEY_LIBRARY_RECENTS)
            } else {
                prefs[KEY_LIBRARY_RECENTS] = encodeRecentSearches(entries)
            }
        }
    }

    suspend fun clearLibraryRecentSearches() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs.remove(KEY_LIBRARY_RECENTS)
        }
    }

    suspend fun clearAll() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs.clear()
        }
    }

    companion object {
        private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
        private val KEY_GRID_MODE = intPreferencesKey("grid_mode_index")
        private val KEY_STROKE_COLOR = intPreferencesKey("stroke_color_index")
        private val KEY_SHOW_TEMPLATE = androidx.datastore.preferences.core.booleanPreferencesKey("show_template")
        private val KEY_THEME_MODE = intPreferencesKey("theme_mode")
        private val KEY_VOLUME_SAFETY_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("volume_safety_enabled")
        private val KEY_VOLUME_SAFETY_THRESHOLD = intPreferencesKey("volume_safety_threshold_percent")
        private val KEY_VOLUME_SAFETY_LOWER_TO = intPreferencesKey("volume_safety_lower_to_percent")
        private val KEY_ONBOARDING_COMPLETED = androidx.datastore.preferences.core.booleanPreferencesKey("onboarding_completed")
        private val KEY_COURSE_LEVEL = intPreferencesKey("course_level")
        private val KEY_COURSE_SYMBOL = stringPreferencesKey("course_symbol")
        private val KEY_LANGUAGE_OVERRIDE = stringPreferencesKey("language_override")
        private val KEY_LIBRARY_RECENTS = stringPreferencesKey("library_recent_searches")
        private const val RECENTS_DELIMITER = "\u0001"

        private fun decodeRecentSearches(raw: String?): List<String> {
            if (raw.isNullOrBlank()) return emptyList()
            return raw.split(RECENTS_DELIMITER).filter { it.isNotBlank() }
        }

        private fun encodeRecentSearches(entries: List<String>): String {
            return entries.joinToString(separator = RECENTS_DELIMITER)
        }
    }
}
