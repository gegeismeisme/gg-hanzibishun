package com.yourstudio.hskstroke.bishun.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.yourstudio.hskstroke.bishun.R
import com.yourstudio.hskstroke.bishun.data.daily.DailyPracticeUseCase
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import com.yourstudio.hskstroke.bishun.ui.navigation.AppLaunchRequests
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

class DailyReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action != ACTION_DAILY_REMINDER) return

        val pendingResult = goAsync()
        val applicationContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val preferencesStore = UserPreferencesStore(applicationContext)
                val prefs = preferencesStore.data.first()

                if (!prefs.dailyReminderEnabled) {
                    DailyReminderScheduler.cancel(applicationContext)
                    return@launch
                }

                DailyReminderScheduler.schedule(applicationContext, prefs.dailyReminderTimeMinutes)

                if (!NotificationUtils.canPostNotifications(applicationContext)) return@launch

                val zone = ZoneId.systemDefault()
                val todayEpochDay = LocalDate.now(zone).toEpochDay()
                val snapshot = DailyPracticeUseCase.ensureTodaySnapshot(
                    context = applicationContext,
                    todayEpochDay = todayEpochDay,
                    ensureDetails = true,
                    preferencesStore = preferencesStore,
                )
                val symbol = snapshot.symbol?.trim().takeIf { !it.isNullOrBlank() } ?: return@launch

                if (prefs.dailyReminderOnlyWhenIncomplete && snapshot.completedToday) {
                    return@launch
                }

                NotificationUtils.ensureDailyReminderChannel(applicationContext)

                val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                val practiceIntent = AppLaunchRequests.practiceIntent(applicationContext, symbol)
                val dictionaryIntent = AppLaunchRequests.dictionaryIntent(applicationContext, symbol)
                val practicePendingIntent = PendingIntent.getActivity(applicationContext, 1000, practiceIntent, flags)
                val dictionaryPendingIntent = PendingIntent.getActivity(applicationContext, 1001, dictionaryIntent, flags)

                val contentParts = buildList {
                    snapshot.pinyin?.trim()?.takeIf { it.isNotBlank() }?.let(::add)
                    snapshot.explanationSummary?.trim()?.takeIf { it.isNotBlank() }?.let(::add)
                }
                val contentText = when {
                    contentParts.isEmpty() -> "点击开始练习"
                    else -> contentParts.joinToString(separator = " · ")
                }

                val notification = NotificationCompat.Builder(applicationContext, NotificationUtils.DAILY_REMINDER_CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("今日一字：$symbol")
                    .setContentText(contentText)
                    .setSubText(snapshot.streakDays.takeIf { it > 0 }?.let { "连续 $it 天" })
                    .setContentIntent(practicePendingIntent)
                    .setAutoCancel(true)
                    .addAction(R.mipmap.ic_launcher, "练习", practicePendingIntent)
                    .addAction(R.mipmap.ic_launcher, "字典", dictionaryPendingIntent)
                    .build()

                NotificationManagerCompat.from(applicationContext).notify(NOTIFICATION_ID, notification)
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val ACTION_DAILY_REMINDER = "com.yourstudio.hskstroke.bishun.action.DAILY_REMINDER"
        private const val NOTIFICATION_ID = 10_001
    }
}

