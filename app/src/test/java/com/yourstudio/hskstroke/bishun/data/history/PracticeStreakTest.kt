package com.yourstudio.hskstroke.bishun.data.history

import org.junit.Assert.assertEquals
import org.junit.Test

class PracticeStreakTest {

    @Test
    fun updatePracticeStreak_firstCompletionStartsAtOne() {
        val update = updatePracticeStreak(currentDays = 0, lastEpochDay = null, todayEpochDay = 100)
        assertEquals(1, update.days)
        assertEquals(100, update.lastEpochDay)
    }

    @Test
    fun updatePracticeStreak_sameDayDoesNotIncrement() {
        val update = updatePracticeStreak(currentDays = 3, lastEpochDay = 100, todayEpochDay = 100)
        assertEquals(3, update.days)
        assertEquals(100, update.lastEpochDay)
    }

    @Test
    fun updatePracticeStreak_consecutiveDayIncrements() {
        val update = updatePracticeStreak(currentDays = 3, lastEpochDay = 100, todayEpochDay = 101)
        assertEquals(4, update.days)
        assertEquals(101, update.lastEpochDay)
    }

    @Test
    fun updatePracticeStreak_gapResetsToOne() {
        val update = updatePracticeStreak(currentDays = 7, lastEpochDay = 100, todayEpochDay = 103)
        assertEquals(1, update.days)
        assertEquals(103, update.lastEpochDay)
    }

    @Test
    fun updatePracticeStreak_timeTravelBackwardsKeepsState() {
        val update = updatePracticeStreak(currentDays = 7, lastEpochDay = 100, todayEpochDay = 99)
        assertEquals(7, update.days)
        assertEquals(100, update.lastEpochDay)
    }

    @Test
    fun effectivePracticeStreakDays_requiresLastEpochDay() {
        assertEquals(0, effectivePracticeStreakDays(storedDays = 5, lastEpochDay = null, todayEpochDay = 100))
    }

    @Test
    fun effectivePracticeStreakDays_keepsStreakUntilTomorrow() {
        assertEquals(5, effectivePracticeStreakDays(storedDays = 5, lastEpochDay = 100, todayEpochDay = 100))
        assertEquals(5, effectivePracticeStreakDays(storedDays = 5, lastEpochDay = 100, todayEpochDay = 101))
        assertEquals(0, effectivePracticeStreakDays(storedDays = 5, lastEpochDay = 100, todayEpochDay = 102))
    }
}

