package com.yourstudio.hskstroke.bishun.ui.account

import android.Manifest
import android.app.Activity
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.yourstudio.hskstroke.bishun.data.billing.BillingRepository
import com.yourstudio.hskstroke.bishun.data.billing.InAppProduct
import com.yourstudio.hskstroke.bishun.data.settings.ThemeMode
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferences
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import com.yourstudio.hskstroke.bishun.ui.character.AccountStrings
import com.yourstudio.hskstroke.bishun.ui.character.LocalizedStrings
import com.yourstudio.hskstroke.bishun.ui.character.rememberLocalizedStrings
import com.yourstudio.hskstroke.bishun.ui.support.HelpDialog
import com.yourstudio.hskstroke.bishun.ui.support.PrivacyDialog
import com.yourstudio.hskstroke.bishun.ui.theme.AccentColorOption
import com.yourstudio.hskstroke.bishun.ui.practice.BrushWidthOption
import kotlinx.coroutines.launch
import java.util.Locale

@Composable
fun AccountScreen(
    modifier: Modifier = Modifier,
    onClearLocalData: () -> Unit,
    languageOverride: String?,
    onShowOnboarding: () -> Unit = {},
    billingRepository: BillingRepository,
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
    val activity = context as? Activity
    val hasPlayStore = remember {
        runCatching {
            if (Build.VERSION.SDK_INT >= 33) {
                context.packageManager.getPackageInfo(
                    "com.android.vending",
                    PackageManager.PackageInfoFlags.of(0),
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo("com.android.vending", 0)
            }
        }.isSuccess
    }
    val preferencesStore = remember { UserPreferencesStore(context.applicationContext) }
    val userPreferences by preferencesStore.data.collectAsState(initial = UserPreferences())
    val billingUiState by billingRepository.uiState.collectAsState()
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

        ProUpgradeCard(
            accountStrings = accountStrings,
            locale = strings.locale,
            isPro = userPreferences.isPro,
            product = billingUiState.proProduct,
            isConnecting = billingUiState.isConnecting,
            isReady = billingUiState.isReady,
            isPlayStoreAvailable = hasPlayStore,
            hasPendingPurchase = billingUiState.hasPendingPurchase,
            lastErrorCode = userPreferences.billingLastErrorCode,
            onUpgrade = { if (activity != null) billingRepository.launchProPurchase(activity) },
            upgradeEnabled = hasPlayStore && activity != null && !userPreferences.isPro && billingUiState.proProduct != null,
            onRestore = billingRepository::restorePurchases,
        )

        AppearanceCard(
            accountStrings = accountStrings,
            locale = strings.locale,
            themeMode = userPreferences.themeMode,
            accentColorIndex = userPreferences.accentColorIndex,
            brushWidthIndex = userPreferences.brushWidthIndex,
            isPro = userPreferences.isPro,
            onThemeModeChange = { mode ->
                scope.launch { preferencesStore.setThemeMode(mode) }
            },
            onAccentColorIndexChange = { index ->
                scope.launch { preferencesStore.setAccentColorIndex(index) }
            },
            onBrushWidthIndexChange = { index ->
                scope.launch { preferencesStore.setBrushWidthIndex(index) }
            },
        )

        AudioSafetyCard(
            accountStrings = accountStrings,
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
            accountStrings = accountStrings,
            locale = strings.locale,
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

        GuidanceCard(accountStrings = accountStrings, onShowOnboarding = onShowOnboarding)

        LanguageCard(
            accountStrings = accountStrings,
            locale = strings.locale,
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
            title = { Text(accountStrings.clearDictionaryHistoryDialogTitle) },
            text = { Text(accountStrings.clearDictionaryHistoryDialogBody) },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch { preferencesStore.clearLibraryRecentSearches() }
                        showClearRecentsDialog = false
                    },
                ) {
                    Text(accountStrings.clearDictionaryHistoryConfirmButton)
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
private fun ProUpgradeCard(
    accountStrings: AccountStrings,
    locale: Locale,
    isPro: Boolean,
    product: InAppProduct?,
    isConnecting: Boolean,
    isReady: Boolean,
    isPlayStoreAvailable: Boolean,
    hasPendingPurchase: Boolean,
    lastErrorCode: Int?,
    onUpgrade: () -> Unit,
    upgradeEnabled: Boolean,
    onRestore: () -> Unit,
) {
    val price = product?.formattedPrice
    val status = when {
        isPro -> accountStrings.proStatusPro
        !isPlayStoreAvailable -> accountStrings.proStatusPlayRequired
        hasPendingPurchase -> accountStrings.proStatusPending
        isConnecting -> accountStrings.proStatusConnecting
        isReady -> accountStrings.proStatusFree
        else -> accountStrings.proStatusNotAvailable
    }
    val description = if (isPro) {
        accountStrings.proDescriptionPro
    } else if (!isPlayStoreAvailable) {
        accountStrings.proDescriptionNoPlayStore
    } else {
        accountStrings.proDescriptionFree
    }

    SettingsCard(
        title = accountStrings.proCardTitle,
        description = description,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(text = status, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            if (price != null && !isPro) {
                Text(text = price, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
        }
        if (lastErrorCode != null && !isPro) {
            Text(
                text = String.format(locale, accountStrings.proBillingStatusCodeFormat, lastErrorCode),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onRestore, enabled = isPlayStoreAvailable) {
                Text(accountStrings.proRestorePurchasesButton)
            }
            Button(onClick = onUpgrade, enabled = upgradeEnabled) {
                val label = if (price != null) {
                    String.format(locale, accountStrings.proBuyButtonWithPriceFormat, price)
                } else {
                    accountStrings.proBuyButton
                }
                Text(label)
            }
        }
    }
}

@Composable
private fun AppearanceCard(
    accountStrings: AccountStrings,
    locale: Locale,
    themeMode: ThemeMode,
    accentColorIndex: Int,
    brushWidthIndex: Int,
    isPro: Boolean,
    onThemeModeChange: (ThemeMode) -> Unit,
    onAccentColorIndexChange: (Int) -> Unit,
    onBrushWidthIndexChange: (Int) -> Unit,
) {
    SettingsCard(
        title = accountStrings.appearanceCardTitle,
        description = accountStrings.appearanceCardDescription,
    ) {
        ThemeModeOptions(
            accountStrings = accountStrings,
            themeMode = themeMode,
            onThemeModeChange = onThemeModeChange,
        )
        Text(text = accountStrings.accentColorLabel, style = MaterialTheme.typography.bodyMedium)
        AccentColorOptions(
            accountStrings = accountStrings,
            locale = locale,
            themeMode = themeMode,
            accentColorIndex = accentColorIndex,
            isPro = isPro,
            onAccentColorIndexChange = onAccentColorIndexChange,
        )
        Text(text = accountStrings.brushThicknessLabel, style = MaterialTheme.typography.bodyMedium)
        BrushWidthOptions(
            accountStrings = accountStrings,
            locale = locale,
            brushWidthIndex = brushWidthIndex,
            isPro = isPro,
            onBrushWidthIndexChange = onBrushWidthIndexChange,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ThemeModeOptions(
    accountStrings: AccountStrings,
    themeMode: ThemeMode,
    onThemeModeChange: (ThemeMode) -> Unit,
) {
    val options = listOf(
        ThemeMode.System to accountStrings.themeModeSystemLabel,
        ThemeMode.Light to accountStrings.themeModeLightLabel,
        ThemeMode.Dark to accountStrings.themeModeDarkLabel,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AccentColorOptions(
    accountStrings: AccountStrings,
    locale: Locale,
    themeMode: ThemeMode,
    accentColorIndex: Int,
    isPro: Boolean,
    onAccentColorIndexChange: (Int) -> Unit,
) {
    val darkTheme = when (themeMode) {
        ThemeMode.System -> isSystemInDarkTheme()
        ThemeMode.Light -> false
        ThemeMode.Dark -> true
    }
    val storedOption = AccentColorOption.fromStoredIndex(accentColorIndex)
    val effectiveOption = if (!storedOption.requiresPro || isPro) storedOption else AccentColorOption.Lilac

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        AccentColorOption.entries.forEach { option ->
            val enabled = isPro || !option.requiresPro
            val baseLabel = accountStrings.accentColorOptionLabels.getOrNull(option.ordinal) ?: option.label
            val label = if (option.requiresPro) {
                String.format(locale, accountStrings.requiresProChipLabelFormat, baseLabel)
            } else {
                baseLabel
            }
            val dotColor = option.primary(darkTheme).let { if (enabled) it else it.copy(alpha = 0.35f) }
            FilterChip(
                selected = option == effectiveOption,
                onClick = { if (enabled) onAccentColorIndexChange(option.ordinal) },
                enabled = enabled,
                label = { Text(label) },
                leadingIcon = {
                    Canvas(modifier = Modifier.size(12.dp)) {
                        drawCircle(color = dotColor)
                    }
                },
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BrushWidthOptions(
    accountStrings: AccountStrings,
    locale: Locale,
    brushWidthIndex: Int,
    isPro: Boolean,
    onBrushWidthIndexChange: (Int) -> Unit,
) {
    val storedOption = BrushWidthOption.fromStoredIndex(brushWidthIndex)
    val effectiveOption = if (!storedOption.requiresPro || isPro) storedOption else BrushWidthOption.Regular

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        BrushWidthOption.entries.forEach { option ->
            val enabled = isPro || !option.requiresPro
            val baseLabel = accountStrings.brushWidthOptionLabels.getOrNull(option.ordinal) ?: option.label
            val label = if (option.requiresPro) {
                String.format(locale, accountStrings.requiresProChipLabelFormat, baseLabel)
            } else {
                baseLabel
            }
            FilterChip(
                selected = option == effectiveOption,
                onClick = { if (enabled) onBrushWidthIndexChange(option.ordinal) },
                enabled = enabled,
                label = { Text(label) },
            )
        }
    }
}

@Composable
private fun AudioSafetyCard(
    accountStrings: AccountStrings,
    enabled: Boolean,
    thresholdPercent: Int,
    lowerToPercent: Int,
    onEnabledChange: (Boolean) -> Unit,
    onThresholdPercentChange: (Int) -> Unit,
    onLowerToPercentChange: (Int) -> Unit,
) {
    SettingsCard(
        title = accountStrings.audioSafetyTitle,
        description = accountStrings.audioSafetyDescription,
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(text = accountStrings.audioSafetyVolumeReminderLabel, style = MaterialTheme.typography.bodyMedium)
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange,
            )
        }
        if (enabled) {
            VolumeSliderRow(
                title = accountStrings.audioSafetyThresholdLabel,
                value = thresholdPercent,
                onValueChange = onThresholdPercentChange,
                valueRange = 50..100,
            )
            VolumeSliderRow(
                title = accountStrings.audioSafetyLowerToLabel,
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
private fun GuidanceCard(accountStrings: AccountStrings, onShowOnboarding: () -> Unit) {
    SettingsCard(
        title = accountStrings.guidanceTitle,
        description = accountStrings.guidanceDescription,
    ) {
        Button(onClick = onShowOnboarding) {
            Text(accountStrings.guidanceOpenButton)
        }
    }
}

@Composable
private fun DailyReminderCard(
    accountStrings: AccountStrings,
    locale: Locale,
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
        title = accountStrings.dailyReminderTitle,
        description = accountStrings.dailyReminderDescription,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = accountStrings.dailyReminderEnabledLabel, style = MaterialTheme.typography.bodyMedium)
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
                    Text(
                        text = String.format(locale, accountStrings.dailyReminderTimeLabelFormat, formattedTime),
                        style = MaterialTheme.typography.bodyMedium,
                    )
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
                        Text(accountStrings.dailyReminderChangeTimeButton)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(text = accountStrings.dailyReminderOnlyWhenIncompleteLabel, style = MaterialTheme.typography.bodyMedium)
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
    accountStrings: AccountStrings,
    locale: Locale,
    languageOverride: String?,
    showMenu: Boolean,
    onToggleMenu: (Boolean) -> Unit,
    onLanguageChange: (String?) -> Unit,
) {
    val languageChoices = listOf(
        LanguageChoice(null, accountStrings.languageSystemOption),
        LanguageChoice("zh", accountStrings.languageChineseOption),
        LanguageChoice("en", accountStrings.languageEnglishOption),
        LanguageChoice("es", accountStrings.languageSpanishOption),
        LanguageChoice("ja", accountStrings.languageJapaneseOption),
    )
    val currentChoice = languageChoices.firstOrNull { it.tag == languageOverride } ?: languageChoices.first()
    SettingsCard(
        title = accountStrings.languageTitle,
        description = accountStrings.languageDescription,
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = String.format(locale, accountStrings.languageCurrentFormat, currentChoice.label),
                style = MaterialTheme.typography.bodyMedium,
            )
            Box {
                Button(onClick = { onToggleMenu(true) }) {
                    Text(accountStrings.languageChangeButton)
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
        title = accountStrings.dataTitle,
        description = accountStrings.dataDescription,
    ) {
        Button(onClick = onClearDictionaryHistory) {
            Text(accountStrings.clearDictionaryHistoryButton)
        }
        Button(onClick = onClearLocalData) {
            Text(accountStrings.clearDataButton)
        }
    }
}

private data class LanguageChoice(val tag: String?, val label: String)
