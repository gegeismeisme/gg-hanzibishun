package com.yourstudio.hskstroke.bishun.data.daily

import android.content.Context
import com.yourstudio.hskstroke.bishun.data.history.effectivePracticeStreakDays
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferences
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import com.yourstudio.hskstroke.bishun.data.word.WordRepository
import com.yourstudio.hskstroke.bishun.data.word.buildExplanationSummary
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

data class DailyPracticeSnapshot(
    val epochDay: Long,
    val symbol: String?,
    val pinyin: String?,
    val explanationSummary: String?,
    val completedToday: Boolean,
    val streakDays: Int,
)

object DailyPracticeUseCase {

    fun todayEpochDay(zoneId: ZoneId = ZoneId.systemDefault()): Long {
        return LocalDate.now(zoneId).toEpochDay()
    }

    suspend fun ensureTodaySnapshot(
        context: Context,
        todayEpochDay: Long = todayEpochDay(),
        suggestedSymbol: String? = null,
        ensureDetails: Boolean = true,
        preferencesStore: UserPreferencesStore = UserPreferencesStore(context.applicationContext),
        wordRepository: WordRepository = WordRepository(context.applicationContext),
    ): DailyPracticeSnapshot {
        val applicationContext = context.applicationContext

        var preferences = preferencesStore.data.first()
        var symbol = resolveValidDailySymbol(preferences, todayEpochDay)

        if (symbol == null) {
            val candidate = suggestedSymbol?.trim()?.takeIf { it.isNotBlank() }
                ?: DailyPracticeGenerator.suggestSymbol(applicationContext)?.trim()?.takeIf { it.isNotBlank() }
            if (candidate != null) {
                preferencesStore.setDailyPractice(candidate, todayEpochDay)
                preferences = preferencesStore.data.first()
                symbol = resolveValidDailySymbol(preferences, todayEpochDay)
            }
        }

        var pinyin: String? = null
        var explanationSummary: String? = null

        if (symbol != null && preferences.dailyEpochDay == todayEpochDay && preferences.dailySymbol?.trim() == symbol) {
            pinyin = preferences.dailyPinyin?.trim().takeIf { !it.isNullOrBlank() }
            explanationSummary = preferences.dailyExplanationSummary?.trim().takeIf { !it.isNullOrBlank() }
        }

        if (ensureDetails && symbol != null && pinyin == null && explanationSummary == null) {
            val entry = runCatching { wordRepository.getWord(symbol) }.getOrNull()
            val fetchedPinyin = entry?.pinyin?.trim()?.takeIf { it.isNotBlank() }
            val fetchedSummary = entry?.explanation?.let { buildExplanationSummary(it) }
            if (fetchedPinyin != null || fetchedSummary != null) {
                preferencesStore.setDailyPracticeDetails(
                    symbol = symbol,
                    epochDay = todayEpochDay,
                    pinyin = fetchedPinyin,
                    explanationSummary = fetchedSummary,
                )
                pinyin = fetchedPinyin
                explanationSummary = fetchedSummary
            }
        }

        val completedToday = symbol != null &&
            preferences.dailyPracticeCompletedEpochDay == todayEpochDay &&
            preferences.dailyPracticeCompletedSymbol?.trim() == symbol

        val streakDays = effectivePracticeStreakDays(
            storedDays = preferences.practiceStreakDays,
            lastEpochDay = preferences.practiceStreakLastEpochDay,
            todayEpochDay = todayEpochDay,
        )

        return DailyPracticeSnapshot(
            epochDay = todayEpochDay,
            symbol = symbol,
            pinyin = pinyin,
            explanationSummary = explanationSummary,
            completedToday = completedToday,
            streakDays = streakDays,
        )
    }

    private fun resolveValidDailySymbol(preferences: UserPreferences, todayEpochDay: Long): String? {
        val storedSymbol = preferences.dailySymbol?.trim()?.takeIf { it.isNotBlank() } ?: return null
        return storedSymbol.takeIf { preferences.dailyEpochDay == todayEpochDay }
    }
}
