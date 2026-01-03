package com.yourstudio.hskstroke.bishun.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat

object NotificationUtils {
    const val DAILY_REMINDER_CHANNEL_ID = "daily_reminder"

    fun canPostNotifications(context: Context): Boolean {
        val applicationContext = context.applicationContext
        val manager = NotificationManagerCompat.from(applicationContext)
        if (!manager.areNotificationsEnabled()) return false
        if (Build.VERSION.SDK_INT < 33) return true
        return ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun ensureDailyReminderChannel(context: Context) {
        if (Build.VERSION.SDK_INT < 26) return
        val manager = context.applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            ?: return

        val existing = manager.getNotificationChannel(DAILY_REMINDER_CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            DAILY_REMINDER_CHANNEL_ID,
            "每日提醒",
            NotificationManager.IMPORTANCE_DEFAULT,
        ).apply {
            description = "提醒你练习今日一字"
        }
        manager.createNotificationChannel(channel)
    }
}

