package com.yourstudio.hskstroke.bishun.ui.navigation

import android.content.Context
import android.content.Intent
import com.yourstudio.hskstroke.bishun.MainActivity

sealed interface AppLaunchRequest {
    data class PracticeSymbol(val symbol: String) : AppLaunchRequest
    data class DictionaryQuery(val query: String) : AppLaunchRequest
}

object AppLaunchRequests {
    private const val EXTRA_ACTION = "launch_action"
    private const val EXTRA_VALUE = "launch_value"

    private const val ACTION_PRACTICE = "practice"
    private const val ACTION_DICTIONARY = "dictionary"

    fun parse(intent: Intent?): AppLaunchRequest? {
        return parse(
            action = intent?.getStringExtra(EXTRA_ACTION),
            value = intent?.getStringExtra(EXTRA_VALUE),
        )
    }

    fun parse(action: String?, value: String?): AppLaunchRequest? {
        val normalizedAction = action?.trim().orEmpty()
        val normalizedValue = value?.trim().orEmpty()
        if (normalizedAction.isBlank() || normalizedValue.isBlank()) return null

        return when (normalizedAction) {
            ACTION_PRACTICE -> AppLaunchRequest.PracticeSymbol(normalizedValue)
            ACTION_DICTIONARY -> AppLaunchRequest.DictionaryQuery(normalizedValue)
            else -> null
        }
    }

    fun openAppIntent(context: Context): Intent {
        return Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
    }

    fun practiceIntent(context: Context, symbol: String): Intent {
        return openAppIntent(context).apply {
            putExtra(EXTRA_ACTION, ACTION_PRACTICE)
            putExtra(EXTRA_VALUE, symbol)
        }
    }

    fun dictionaryIntent(context: Context, query: String): Intent {
        return openAppIntent(context).apply {
            putExtra(EXTRA_ACTION, ACTION_DICTIONARY)
            putExtra(EXTRA_VALUE, query)
        }
    }
}
