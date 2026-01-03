package com.yourstudio.hskstroke.bishun.ui.account

import android.Manifest
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.yourstudio.hskstroke.bishun.data.settings.ThemeMode
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferences
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import com.yourstudio.hskstroke.bishun.ui.character.AccountStrings
import com.yourstudio.hskstroke.bishun.ui.character.LocalizedStrings
import com.yourstudio.hskstroke.bishun.ui.character.rememberLocalizedStrings
import com.yourstudio.hskstroke.bishun.ui.support.HelpDialog
import com.yourstudio.hskstroke.bishun.ui.support.PrivacyDialog
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    onClearLocalData: () -> Unit,
    languageOverride: String?,
    onShowOnboarding: () -> Unit = {},
) {
    var showHelpDialog by rememberSaveable { mutableStateOf(false) }
    var showPrivacyDialog by rememberSaveable { mutableStateOf(false) }
    var showClearDataDialog by rememberSaveable { mutableStateOf(false) }
    var showClearRecentsDialog by rememberSaveable { mutableStateOf(false) }
    var showLanguageMenu by rememberSaveable { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val strings = rememberLocalizedStrings(languageOverride)
    val accountStrings = strings.account
    val context = LocalContext.current
    val preferencesStore = remember { UserPreferencesStore(context.applicationContext) }
    val userPreferences by preferencesStore.data.collectAsState(initial = UserPreferences())
    val scope = rememberCoroutineScope()
    val requestNotificationPermission = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        scope.launch { preferencesStore.setDailyReminderEnabled(granted) }
    }

    Column(
        modifier = modifier
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        Text(text = accountStrings.title, style = MaterialTheme.typography.headlineSmall)

        AppearanceCard(
            themeMode = userPreferences.themeMode,
            onThemeModeChange = { mode ->
                scope.launch { preferencesStore.setThemeMode(mode) }
            },
        )

        AudioSafetyCard(
            enabled = userPreferences.volumeSafetyEnabled,
            thresholdPercent = userPreferences.volumeSafetyThresholdPercent,
            lowerToPercent = userPreferences.volumeSafetyLowerToPercent,
            onEnabledChange = { enabled ->
                scope.launch { preferencesStore.setVolumeSafetyEnabled(enabled) }
            },
            onThresholdPercentChange = { percent ->
                scope.launch { preferencesStore.setVolumeSafetyThresholdPercent(percent) }
            },
            onLowerToPercentChange = { percent ->
                scope.launch { preferencesStore.setVolumeSafetyLowerToPercent(percent) }
            },
        )

        DailyReminderCard(
            enabled = userPreferences.dailyReminderEnabled,
            minutesOfDay = userPreferences.dailyReminderTimeMinutes,
            onlyWhenIncomplete = userPreferences.dailyReminderOnlyWhenIncomplete,
            onEnabledChange = { enabled ->
                if (!enabled) {
                    scope.launch { preferencesStore.setDailyReminderEnabled(false) }
                } else if (Build.VERSION.SDK_INT >= 33) {
                    val granted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS,
                    ) == PackageManager.PERMISSION_GRANTED
                    if (!granted) {
                        requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    } else {
                        scope.launch { preferencesStore.setDailyReminderEnabled(true) }
                    }
                } else {
                    scope.launch { preferencesStore.setDailyReminderEnabled(true) }
                }
            },
            onTimeChange = { minutes ->
                scope.launch { preferencesStore.setDailyReminderTimeMinutes(minutes) }
            },
            onOnlyWhenIncompleteChange = { enabled ->
                scope.launch { preferencesStore.setDailyReminderOnlyWhenIncomplete(enabled) }
            },
        )

        GuidanceCard(onShowOnboarding = onShowOnboarding)

        LanguageCard(
            languageOverride = userPreferences.languageOverride,
            showMenu = showLanguageMenu,
            onToggleMenu = { showLanguageMenu = it },
            onLanguageChange = { tag ->
                scope.launch { preferencesStore.setLanguageOverride(tag) }
            },
        )

        SupportCard(
            accountStrings = accountStrings,
            strings = strings,
            onHelpClick = { showHelpDialog = true },
            onPrivacyClick = { showPrivacyDialog = true },
        )

        DataCard(
            accountStrings = accountStrings,
            onClearDictionaryHistory = { showClearRecentsDialog = true },
            onClearLocalData = { showClearDataDialog = true },
        )
    }

    if (showHelpDialog) {
        HelpDialog(strings = strings, onDismiss = { showHelpDialog = false })
    }
    if (showPrivacyDialog) {
        PrivacyDialog(onDismiss = { showPrivacyDialog = false }, strings = strings)
    }
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text(accountStrings.clearDataDialogTitle) },
            text = { Text(accountStrings.clearDataDialogBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearLocalData()
                        showClearDataDialog = false
                    },
                ) {
                    Text(accountStrings.clearDataButton)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text(accountStrings.cancelLabel)
                }
            },
        )
    }
    if (showClearRecentsDialog) {
        AlertDialog(
            onDismissRequest = { showClearRecentsDialog = false },
            title = { Text("Clear dictionary history") },
            text = { Text("Remove recent dictionary searches saved on this device.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { preferencesStore.clearLibraryRecentSearches() }
                        showClearRecentsDialog = false
                    },
                ) {
                    Text("Clear")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearRecentsDialog = false }) {
                    Text(accountStrings.cancelLabel)
                }
            },
        )
    }
}

@Composable
private fun SettingsCard(
    title: String,
    description: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            description?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            content()
        }
    }
}

@Composable
private fun AppearanceCard(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    SettingsCard(
        title = "Appearance",
        description = "Choose how the app looks on this device.",
    ) {
        ThemeModeOptions(themeMode = themeMode, onThemeModeChange = onThemeModeChange)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeModeOptions(
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    val options = listOf(
        ThemeMode.System to "System",
        ThemeMode.Light to "Light",
        ThemeMode.Dark to "Dark",
    )
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        options.forEach { (mode, label) ->
            FilterChip(
                selected = themeMode == mode,
                onClick = { onThemeModeChange(mode) },
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun AudioSafetyCard(
    enabled: Boolean,
    thresholdPercent: Int,
    lowerToPercent: Int,
    onEnabledChange: (Boolean) -> Unit,
    onThresholdPercentChange: (Int) -> Unit,
    onLowerToPercentChange: (Int) -> Unit,
) {
    SettingsCard(
        title = "Audio safety",
        description = "Show a reminder before playing pronunciation at high volume.",
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = "Volume reminder", style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
            )
        }
        if (enabled) {
            VolumeSliderRow(
                title = "Reminder threshold",
                value = thresholdPercent,
                onValueChange = onThresholdPercentChange,
                valueRange = 50..100,
            )
            VolumeSliderRow(
                title = "Lower to",
                value = lowerToPercent,
                onValueChange = onLowerToPercentChange,
                valueRange = 0..60,
            )
        }
    }
}

@Composable
private fun VolumeSliderRow(
    title: String,
    value: Int,
    valueRange: IntRange,
    onValueChange: (Int) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text(text = title, style = MaterialTheme.typography.bodySmall)
            Text(text = "$value%", style = MaterialTheme.typography.bodySmall)
        }
        val steps = (valueRange.last - valueRange.first - 1).coerceAtLeast(0)
        Slider(
            value = value.coerceIn(valueRange.first, valueRange.last).toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = steps,
        )
    }
}

@Composable
private fun GuidanceCard(onShowOnboarding: () -> Unit) {
    SettingsCard(
        title = "Getting started",
        description = "Review the quick guide for first-time users.",
    ) {
        Button(onClick = onShowOnboarding) {
            Text("Open guide")
        }
    }
}

@Composable
private fun DailyReminderCard(
    enabled: Boolean,
    minutesOfDay: Int,
    onlyWhenIncomplete: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    onTimeChange: (Int) -> Unit,
    onOnlyWhenIncompleteChange: (Boolean) -> Unit,
) {
    val context = LocalContext.current
    val normalizedMinutes = minutesOfDay.coerceIn(0, 23 * 60 + 59)
    val hour = normalizedMinutes / 60
    val minute = normalizedMinutes % 60
    val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hour, minute)

    SettingsCard(
        title = "每日提醒",
        description = "每天在指定时间提醒你练习今日一字。",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "开启提醒", style = MaterialTheme.typography.bodyMedium)
                Switch(
                    checked = enabled,
                    onCheckedChange = onEnabledChange,
                )
            }

            if (enabled) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "提醒时间：$formattedTime", style = MaterialTheme.typography.bodyMedium)
                    Button(
                        onClick = {
                            TimePickerDialog(
                                context,
                                { _, pickedHour, pickedMinute ->
                                    onTimeChange(pickedHour.coerceIn(0, 23) * 60 + pickedMinute.coerceIn(0, 59))
                                },
                                hour,
                                minute,
                                true,
                            ).show()
                        },
                    ) {
                        Text("修改")
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = "仅未完成时提醒", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = onlyWhenIncomplete,
                        onCheckedChange = onOnlyWhenIncompleteChange,
                    )
                }
            }
        }
    }
}

@Composable
private fun LanguageCard(
    languageOverride: String?,
    showMenu: Boolean,
    onToggleMenu: (Boolean) -> Unit,
    onLanguageChange: (String?) -> Unit,
) {
    val currentChoice = languageChoices.firstOrNull { it.tag == languageOverride } ?: languageChoices.first()
    SettingsCard(
        title = "Language",
        description = "Overrides in-app content language.",
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(text = "Current: ${currentChoice.label}", style = MaterialTheme.typography.bodyMedium)
            Box {
                Button(onClick = { onToggleMenu(true) }) {
                    Text("Change")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { onToggleMenu(false) }) {
                    languageChoices.forEach { choice ->
                        DropdownMenuItem(
                            text = { Text(choice.label) },
                            onClick = {
                                onToggleMenu(false)
                                onLanguageChange(choice.tag)
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SupportCard(
    accountStrings: AccountStrings,
    onHelpClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    strings: LocalizedStrings,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = accountStrings.supportTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            )
            Text(
                text = accountStrings.supportDescription,
                style = MaterialTheme.typography.bodyMedium,
            )
            Button(onClick = onHelpClick) {
                Text(strings.helpTitle)
            }
            Button(onClick = onPrivacyClick) {
                Text(strings.privacyTitle)
            }
        }
    }
}

@Composable
private fun DataCard(
    accountStrings: AccountStrings,
    onClearDictionaryHistory: () -> Unit,
    onClearLocalData: () -> Unit,
) {
    SettingsCard(
        title = "Data",
        description = "Manage what is stored on this device.",
    ) {
        Button(onClick = onClearDictionaryHistory) {
            Text("Clear dictionary history")
        }
        Button(onClick = onClearLocalData) {
            Text(accountStrings.clearDataButton)
        }
    }
}

private data class LanguageChoice(val tag: String?, val label: String)

private val languageChoices = listOf(
    LanguageChoice(null, "System"),
    LanguageChoice("en", "English"),
    LanguageChoice("es", "Español"),
    LanguageChoice("ja", "日本語"),
)
