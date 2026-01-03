package com.yourstudio.hskstroke.bishun.notifications

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class DailyReminderSchedulerTest {

    @Test
    fun computeNextTriggerAtMillis_schedulesLaterTodayWhenNotPassed() {
        val zone = ZoneId.of("UTC")
        val now = ZonedDateTime.of(2026, 1, 3, 10, 0, 0, 0, zone)
        val triggerAt = DailyReminderScheduler.computeNextTriggerAtMillis(now, minutesOfDay = 11 * 60 + 30)

        val expected = ZonedDateTime.of(2026, 1, 3, 11, 30, 0, 0, zone).toInstant().toEpochMilli()
        assertEquals(expected, triggerAt)
        assertTrue(triggerAt > now.toInstant().toEpochMilli())
    }

    @Test
    fun computeNextTriggerAtMillis_rollsToTomorrowWhenPassed() {
        val zone = ZoneId.of("UTC")
        val now = ZonedDateTime.of(2026, 1, 3, 22, 0, 0, 0, zone)
        val triggerAt = DailyReminderScheduler.computeNextTriggerAtMillis(now, minutesOfDay = 21 * 60)

        val expected = ZonedDateTime.of(2026, 1, 4, 21, 0, 0, 0, zone).toInstant().toEpochMilli()
        assertEquals(expected, triggerAt)
    }
}

