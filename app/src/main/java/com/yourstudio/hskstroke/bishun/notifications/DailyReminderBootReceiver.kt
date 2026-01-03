package com.yourstudio.hskstroke.bishun.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DailyReminderBootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        val action = intent?.action ?: return
        if (action != Intent.ACTION_BOOT_COMPLETED &&
            action != Intent.ACTION_MY_PACKAGE_REPLACED &&
            action != Intent.ACTION_TIME_CHANGED &&
            action != Intent.ACTION_TIMEZONE_CHANGED
        ) {
            return
        }

        val pendingResult = goAsync()
        val applicationContext = context.applicationContext
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = UserPreferencesStore(applicationContext).data.first()
                DailyReminderScheduler.scheduleOrCancel(
                    context = applicationContext,
                    enabled = prefs.dailyReminderEnabled,
                    minutesOfDay = prefs.dailyReminderTimeMinutes,
                )
            } finally {
                pendingResult.finish()
            }
        }
    }
}

