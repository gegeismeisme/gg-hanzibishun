package com.yourstudio.hskstroke.bishun.data.history

data class PracticeStreakUpdate(
    val days: Int,
    val lastEpochDay: Long,
)

fun updatePracticeStreak(
    currentDays: Int,
    lastEpochDay: Long?,
    todayEpochDay: Long,
): PracticeStreakUpdate {
    val normalizedDays = currentDays.coerceAtLeast(0)
    val last = lastEpochDay
        ?: return PracticeStreakUpdate(days = 1, lastEpochDay = todayEpochDay)

    return when {
        todayEpochDay == last -> PracticeStreakUpdate(days = normalizedDays.coerceAtLeast(1), lastEpochDay = last)
        todayEpochDay == last + 1 -> PracticeStreakUpdate(days = normalizedDays.coerceAtLeast(1) + 1, lastEpochDay = todayEpochDay)
        todayEpochDay > last + 1 -> PracticeStreakUpdate(days = 1, lastEpochDay = todayEpochDay)
        else -> PracticeStreakUpdate(days = normalizedDays.coerceAtLeast(1), lastEpochDay = last)
    }
}

fun effectivePracticeStreakDays(
    storedDays: Int,
    lastEpochDay: Long?,
    todayEpochDay: Long,
): Int {
    val normalizedDays = storedDays.coerceAtLeast(0)
    val last = lastEpochDay ?: return 0
    return if (todayEpochDay - last <= 1) normalizedDays else 0
}

