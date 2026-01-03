package com.yourstudio.hskstroke.bishun.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import com.yourstudio.hskstroke.bishun.ui.navigation.AppLaunchRequests
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import java.time.ZoneId

class DailyHanziWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val preferences = UserPreferencesStore(context.applicationContext).data.first()
        val zone = ZoneId.systemDefault()
        val todayEpochDay = LocalDate.now(zone).toEpochDay()
        val symbol = preferences.dailySymbol?.trim()
            ?.takeIf { it.isNotBlank() && preferences.dailyEpochDay == todayEpochDay }
        val practiceIntent = symbol?.let { AppLaunchRequests.practiceIntent(context, it) }
            ?: AppLaunchRequests.openAppIntent(context)
        val dictionaryIntent = symbol?.let { AppLaunchRequests.dictionaryIntent(context, it) }
            ?: AppLaunchRequests.openAppIntent(context)

        provideContent {
            DailyHanziWidgetContent(
                symbol = symbol,
                practiceIntent = practiceIntent,
                dictionaryIntent = dictionaryIntent,
            )
        }
    }
}

@Composable
private fun DailyHanziWidgetContent(
    symbol: String?,
    practiceIntent: Intent,
    dictionaryIntent: Intent,
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .padding(12.dp),
        verticalAlignment = Alignment.Vertical.CenterVertically,
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
    ) {
        Text(text = "今日一字", style = TextStyle(fontSize = 12.sp))
        Spacer(modifier = GlanceModifier.height(8.dp))
        Text(
            text = symbol ?: "—",
            modifier = GlanceModifier.clickable(actionStartActivity(practiceIntent)),
            style = TextStyle(fontSize = 36.sp, fontWeight = FontWeight.Bold),
        )
        Spacer(modifier = GlanceModifier.height(12.dp))
        Row(
            verticalAlignment = Alignment.Vertical.CenterVertically,
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        ) {
            Text(
                text = "练习",
                modifier = GlanceModifier
                    .clickable(actionStartActivity(practiceIntent))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
            Spacer(modifier = GlanceModifier.width(6.dp))
            Text(
                text = "字典",
                modifier = GlanceModifier
                    .clickable(actionStartActivity(dictionaryIntent))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            )
        }
    }
}

class DailyHanziWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = DailyHanziWidget()
}

object DailyHanziWidgetUpdater {
    suspend fun updateAll(context: Context) {
        val manager = GlanceAppWidgetManager(context)
        manager.getGlanceIds(DailyHanziWidget::class.java).forEach { glanceId ->
            DailyHanziWidget().update(context, glanceId)
        }
    }
}
