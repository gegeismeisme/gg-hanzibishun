package com.yourstudio.hskstroke.bishun.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.time.ZonedDateTime

object DailyReminderScheduler {
    private const val REQUEST_CODE = 20_001

    fun cancel(context: Context) {
        val applicationContext = context.applicationContext
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        alarmManager.cancel(buildPendingIntent(applicationContext))
    }

    fun schedule(context: Context, minutesOfDay: Int) {
        val applicationContext = context.applicationContext
        val alarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val normalizedMinutes = minutesOfDay.coerceIn(0, 23 * 60 + 59)
        val triggerAtMillis = computeNextTriggerAtMillis(ZonedDateTime.now(), normalizedMinutes)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            buildPendingIntent(applicationContext),
        )
    }

    fun scheduleOrCancel(context: Context, enabled: Boolean, minutesOfDay: Int) {
        if (enabled) schedule(context, minutesOfDay) else cancel(context)
    }

    fun computeNextTriggerAtMillis(now: ZonedDateTime, minutesOfDay: Int): Long {
        val hour = (minutesOfDay / 60).coerceIn(0, 23)
        val minute = (minutesOfDay % 60).coerceIn(0, 59)

        val todayTrigger = now.toLocalDate()
            .atTime(hour, minute)
            .atZone(now.zone)
        val nextTrigger = if (todayTrigger.isAfter(now)) todayTrigger else todayTrigger.plusDays(1)
        return nextTrigger.toInstant().toEpochMilli()
    }

    private fun buildPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, DailyReminderReceiver::class.java).apply {
            action = DailyReminderReceiver.ACTION_DAILY_REMINDER
        }
        return PendingIntent.getBroadcast(
            context,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }
}

