package com.yourstudio.hskstroke.bishun.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
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
    val dailySymbol: String? = null,
    val dailyEpochDay: Long? = null,
    val languageOverride: String? = null,
    val libraryRecentSearches: List<String> = emptyList(),
    val libraryPinnedSearches: List<String> = emptyList(),
    val libraryFavorites: List<String> = emptyList(),
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
            dailySymbol = prefs[KEY_DAILY_SYMBOL],
            dailyEpochDay = prefs[KEY_DAILY_EPOCH_DAY],
            languageOverride = prefs[KEY_LANGUAGE_OVERRIDE],
            libraryRecentSearches = decodeStringList(prefs[KEY_LIBRARY_RECENTS]),
            libraryPinnedSearches = decodeStringList(prefs[KEY_LIBRARY_PINNED_RECENTS]),
            libraryFavorites = decodeStringList(prefs[KEY_LIBRARY_FAVORITES]),
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

    suspend fun setDailyPractice(symbol: String?, epochDay: Long) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_DAILY_EPOCH_DAY] = epochDay
            if (symbol.isNullOrBlank()) {
                prefs.remove(KEY_DAILY_SYMBOL)
            } else {
                prefs[KEY_DAILY_SYMBOL] = symbol
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
                prefs[KEY_LIBRARY_RECENTS] = encodeStringList(entries)
            }
        }
    }

    suspend fun clearLibraryRecentSearches() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs.remove(KEY_LIBRARY_RECENTS)
        }
    }

    suspend fun setLibraryPinnedSearches(entries: List<String>) {
        context.userPreferencesDataStore.edit { prefs ->
            if (entries.isEmpty()) {
                prefs.remove(KEY_LIBRARY_PINNED_RECENTS)
            } else {
                prefs[KEY_LIBRARY_PINNED_RECENTS] = encodeStringList(entries)
            }
        }
    }

    suspend fun clearLibraryPinnedSearches() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs.remove(KEY_LIBRARY_PINNED_RECENTS)
        }
    }

    suspend fun setLibraryFavorites(entries: List<String>) {
        context.userPreferencesDataStore.edit { prefs ->
            if (entries.isEmpty()) {
                prefs.remove(KEY_LIBRARY_FAVORITES)
            } else {
                prefs[KEY_LIBRARY_FAVORITES] = encodeStringList(entries)
            }
        }
    }

    suspend fun clearLibraryFavorites() {
        context.userPreferencesDataStore.edit { prefs ->
            prefs.remove(KEY_LIBRARY_FAVORITES)
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
        private val KEY_DAILY_SYMBOL = stringPreferencesKey("daily_symbol")
        private val KEY_DAILY_EPOCH_DAY = longPreferencesKey("daily_epoch_day")
        private val KEY_LANGUAGE_OVERRIDE = stringPreferencesKey("language_override")
        private val KEY_LIBRARY_RECENTS = stringPreferencesKey("library_recent_searches")
        private val KEY_LIBRARY_PINNED_RECENTS = stringPreferencesKey("library_pinned_recent_searches")
        private val KEY_LIBRARY_FAVORITES = stringPreferencesKey("library_favorites")
        private const val LIST_DELIMITER = "\u0001"

        private fun decodeStringList(raw: String?): List<String> {
            if (raw.isNullOrBlank()) return emptyList()
            return raw.split(LIST_DELIMITER).filter { it.isNotBlank() }
        }

        private fun encodeStringList(entries: List<String>): String {
            return entries.joinToString(separator = LIST_DELIMITER)
        }
    }
}
