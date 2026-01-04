package com.yourstudio.hskstroke.bishun.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.yourstudio.hskstroke.bishun.data.history.updatePracticeStreak
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserPreferences(
    val gridMode: Int = 0,
    val strokeColor: Int = 0,
    val showTemplate: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.System,
    val accentColorIndex: Int = 0,
    val brushWidthIndex: Int = 1,
    val volumeSafetyEnabled: Boolean = true,
    val volumeSafetyThresholdPercent: Int = 80,
    val volumeSafetyLowerToPercent: Int = 30,
    val onboardingCompleted: Boolean = false,
    val dailyReminderEnabled: Boolean = false,
    val dailyReminderTimeMinutes: Int = 20 * 60,
    val dailyReminderOnlyWhenIncomplete: Boolean = true,
    val courseLevel: Int? = null,
    val courseSymbol: String? = null,
    val dailySymbol: String? = null,
    val dailyEpochDay: Long? = null,
    val dailyPinyin: String? = null,
    val dailyExplanationSummary: String? = null,
    val dailyPracticeCompletedSymbol: String? = null,
    val dailyPracticeCompletedEpochDay: Long? = null,
    val practiceStreakDays: Int = 0,
    val practiceStreakLastEpochDay: Long? = null,
    val languageOverride: String? = null,
    val libraryRecentSearches: List<String> = emptyList(),
    val libraryPinnedSearches: List<String> = emptyList(),
    val libraryFavorites: List<String> = emptyList(),
    val isPro: Boolean = false,
    val proEntitledProducts: List<String> = emptyList(),
    val billingLastSuccessfulSyncEpochMs: Long? = null,
    val billingLastErrorCode: Int? = null,
)

class UserPreferencesStore(private val context: Context) {

    val data: Flow<UserPreferences> = context.userPreferencesDataStore.data.map { prefs ->
        UserPreferences(
            gridMode = prefs[KEY_GRID_MODE] ?: 0,
            strokeColor = prefs[KEY_STROKE_COLOR] ?: 0,
            showTemplate = prefs[KEY_SHOW_TEMPLATE] ?: true,
            themeMode = ThemeMode.fromStoredValue(prefs[KEY_THEME_MODE]),
            accentColorIndex = prefs[KEY_ACCENT_COLOR_INDEX] ?: 0,
            brushWidthIndex = prefs[KEY_BRUSH_WIDTH_INDEX] ?: DEFAULT_BRUSH_WIDTH_INDEX,
            volumeSafetyEnabled = prefs[KEY_VOLUME_SAFETY_ENABLED] ?: true,
            volumeSafetyThresholdPercent = prefs[KEY_VOLUME_SAFETY_THRESHOLD] ?: 80,
            volumeSafetyLowerToPercent = prefs[KEY_VOLUME_SAFETY_LOWER_TO] ?: 30,
            onboardingCompleted = prefs[KEY_ONBOARDING_COMPLETED] ?: prefs.asMap().isNotEmpty(),
            dailyReminderEnabled = prefs[KEY_DAILY_REMINDER_ENABLED] ?: false,
            dailyReminderTimeMinutes = prefs[KEY_DAILY_REMINDER_TIME_MINUTES] ?: DEFAULT_DAILY_REMINDER_TIME_MINUTES,
            dailyReminderOnlyWhenIncomplete = prefs[KEY_DAILY_REMINDER_ONLY_WHEN_INCOMPLETE] ?: true,
            courseLevel = prefs[KEY_COURSE_LEVEL],
            courseSymbol = prefs[KEY_COURSE_SYMBOL],
            dailySymbol = prefs[KEY_DAILY_SYMBOL],
            dailyEpochDay = prefs[KEY_DAILY_EPOCH_DAY],
            dailyPinyin = prefs[KEY_DAILY_PINYIN],
            dailyExplanationSummary = prefs[KEY_DAILY_EXPLANATION_SUMMARY],
            dailyPracticeCompletedSymbol = prefs[KEY_DAILY_PRACTICE_COMPLETED_SYMBOL],
            dailyPracticeCompletedEpochDay = prefs[KEY_DAILY_PRACTICE_COMPLETED_EPOCH_DAY],
            practiceStreakDays = prefs[KEY_PRACTICE_STREAK_DAYS] ?: 0,
            practiceStreakLastEpochDay = prefs[KEY_PRACTICE_STREAK_LAST_EPOCH_DAY],
            languageOverride = prefs[KEY_LANGUAGE_OVERRIDE],
            libraryRecentSearches = decodeStringList(prefs[KEY_LIBRARY_RECENTS]),
            libraryPinnedSearches = decodeStringList(prefs[KEY_LIBRARY_PINNED_RECENTS]),
            libraryFavorites = decodeStringList(prefs[KEY_LIBRARY_FAVORITES]),
            isPro = prefs[KEY_PRO_ENTITLED] ?: false,
            proEntitledProducts = decodeStringList(prefs[KEY_PRO_PRODUCTS]),
            billingLastSuccessfulSyncEpochMs = prefs[KEY_BILLING_LAST_SUCCESSFUL_SYNC_EPOCH_MS],
            billingLastErrorCode = prefs[KEY_BILLING_LAST_ERROR_CODE],
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

    suspend fun setAccentColorIndex(index: Int) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_ACCENT_COLOR_INDEX] = index.coerceAtLeast(0)
        }
    }

    suspend fun setBrushWidthIndex(index: Int) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_BRUSH_WIDTH_INDEX] = index.coerceAtLeast(0)
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

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_DAILY_REMINDER_ENABLED] = enabled
        }
    }

    suspend fun setDailyReminderTimeMinutes(minutesOfDay: Int) {
        val normalized = minutesOfDay.coerceIn(0, 23 * 60 + 59)
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_DAILY_REMINDER_TIME_MINUTES] = normalized
        }
    }

    suspend fun setDailyReminderOnlyWhenIncomplete(enabled: Boolean) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_DAILY_REMINDER_ONLY_WHEN_INCOMPLETE] = enabled
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
            prefs.remove(KEY_DAILY_PINYIN)
            prefs.remove(KEY_DAILY_EXPLANATION_SUMMARY)
        }
    }

    suspend fun setDailyPracticeDetails(
        symbol: String,
        epochDay: Long,
        pinyin: String?,
        explanationSummary: String?,
    ) {
        val normalizedSymbol = symbol.trim()
        if (normalizedSymbol.isBlank()) return

        context.userPreferencesDataStore.edit { prefs ->
            if (prefs[KEY_DAILY_SYMBOL] != normalizedSymbol) return@edit
            if (prefs[KEY_DAILY_EPOCH_DAY] != epochDay) return@edit

            val normalizedPinyin = pinyin?.trim().takeIf { !it.isNullOrBlank() }
            val normalizedSummary = explanationSummary?.trim().takeIf { !it.isNullOrBlank() }

            if (normalizedPinyin == null) {
                prefs.remove(KEY_DAILY_PINYIN)
            } else {
                prefs[KEY_DAILY_PINYIN] = normalizedPinyin
            }

            if (normalizedSummary == null) {
                prefs.remove(KEY_DAILY_EXPLANATION_SUMMARY)
            } else {
                prefs[KEY_DAILY_EXPLANATION_SUMMARY] = normalizedSummary
            }
        }
    }

    suspend fun recordPracticeCompletion(symbol: String, epochDay: Long) {
        val normalizedSymbol = symbol.trim()
        if (normalizedSymbol.isBlank()) return

        context.userPreferencesDataStore.edit { prefs ->
            val currentDays = prefs[KEY_PRACTICE_STREAK_DAYS] ?: 0
            val lastEpochDay = prefs[KEY_PRACTICE_STREAK_LAST_EPOCH_DAY]
            val update = updatePracticeStreak(currentDays, lastEpochDay, epochDay)
            prefs[KEY_PRACTICE_STREAK_DAYS] = update.days
            prefs[KEY_PRACTICE_STREAK_LAST_EPOCH_DAY] = update.lastEpochDay

            val dailyEpochDay = prefs[KEY_DAILY_EPOCH_DAY]
            val dailySymbol = prefs[KEY_DAILY_SYMBOL]
            if (dailyEpochDay == epochDay && dailySymbol == normalizedSymbol) {
                prefs[KEY_DAILY_PRACTICE_COMPLETED_EPOCH_DAY] = epochDay
                prefs[KEY_DAILY_PRACTICE_COMPLETED_SYMBOL] = normalizedSymbol
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

    suspend fun updateProEntitlement(entitled: Boolean, products: List<String>, syncedAtEpochMs: Long) {
        context.userPreferencesDataStore.edit { prefs ->
            prefs[KEY_PRO_ENTITLED] = entitled
            if (products.isEmpty()) {
                prefs.remove(KEY_PRO_PRODUCTS)
            } else {
                prefs[KEY_PRO_PRODUCTS] = encodeStringList(products)
            }
            prefs[KEY_BILLING_LAST_SUCCESSFUL_SYNC_EPOCH_MS] = syncedAtEpochMs
            prefs.remove(KEY_BILLING_LAST_ERROR_CODE)
        }
    }

    suspend fun setBillingLastErrorCode(responseCode: Int?) {
        context.userPreferencesDataStore.edit { prefs ->
            if (responseCode == null) {
                prefs.remove(KEY_BILLING_LAST_ERROR_CODE)
            } else {
                prefs[KEY_BILLING_LAST_ERROR_CODE] = responseCode
            }
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
        private val KEY_ACCENT_COLOR_INDEX = intPreferencesKey("accent_color_index")
        private val KEY_BRUSH_WIDTH_INDEX = intPreferencesKey("brush_width_index")
        private val KEY_VOLUME_SAFETY_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("volume_safety_enabled")
        private val KEY_VOLUME_SAFETY_THRESHOLD = intPreferencesKey("volume_safety_threshold_percent")
        private val KEY_VOLUME_SAFETY_LOWER_TO = intPreferencesKey("volume_safety_lower_to_percent")
        private val KEY_ONBOARDING_COMPLETED = androidx.datastore.preferences.core.booleanPreferencesKey("onboarding_completed")
        private val KEY_DAILY_REMINDER_ENABLED = androidx.datastore.preferences.core.booleanPreferencesKey("daily_reminder_enabled")
        private val KEY_DAILY_REMINDER_TIME_MINUTES = intPreferencesKey("daily_reminder_time_minutes")
        private val KEY_DAILY_REMINDER_ONLY_WHEN_INCOMPLETE =
            androidx.datastore.preferences.core.booleanPreferencesKey("daily_reminder_only_when_incomplete")
        private val KEY_COURSE_LEVEL = intPreferencesKey("course_level")
        private val KEY_COURSE_SYMBOL = stringPreferencesKey("course_symbol")
        private val KEY_DAILY_SYMBOL = stringPreferencesKey("daily_symbol")
        private val KEY_DAILY_EPOCH_DAY = longPreferencesKey("daily_epoch_day")
        private val KEY_DAILY_PINYIN = stringPreferencesKey("daily_pinyin")
        private val KEY_DAILY_EXPLANATION_SUMMARY = stringPreferencesKey("daily_explanation_summary")
        private val KEY_DAILY_PRACTICE_COMPLETED_SYMBOL = stringPreferencesKey("daily_practice_completed_symbol")
        private val KEY_DAILY_PRACTICE_COMPLETED_EPOCH_DAY = longPreferencesKey("daily_practice_completed_epoch_day")
        private val KEY_PRACTICE_STREAK_DAYS = intPreferencesKey("practice_streak_days")
        private val KEY_PRACTICE_STREAK_LAST_EPOCH_DAY = longPreferencesKey("practice_streak_last_epoch_day")
        private val KEY_LANGUAGE_OVERRIDE = stringPreferencesKey("language_override")
        private val KEY_LIBRARY_RECENTS = stringPreferencesKey("library_recent_searches")
        private val KEY_LIBRARY_PINNED_RECENTS = stringPreferencesKey("library_pinned_recent_searches")
        private val KEY_LIBRARY_FAVORITES = stringPreferencesKey("library_favorites")
        private val KEY_PRO_ENTITLED = androidx.datastore.preferences.core.booleanPreferencesKey("pro_entitled")
        private val KEY_PRO_PRODUCTS = stringPreferencesKey("pro_entitled_products")
        private val KEY_BILLING_LAST_SUCCESSFUL_SYNC_EPOCH_MS = longPreferencesKey("billing_last_successful_sync_epoch_ms")
        private val KEY_BILLING_LAST_ERROR_CODE = intPreferencesKey("billing_last_error_code")
        private const val LIST_DELIMITER = "\u0001"
        private const val DEFAULT_DAILY_REMINDER_TIME_MINUTES = 20 * 60
        private const val DEFAULT_BRUSH_WIDTH_INDEX = 1

        private fun decodeStringList(raw: String?): List<String> {
            if (raw.isNullOrBlank()) return emptyList()
            return raw.split(LIST_DELIMITER).filter { it.isNotBlank() }
        }

        private fun encodeStringList(entries: List<String>): String {
            return entries.joinToString(separator = LIST_DELIMITER)
        }
    }
}
