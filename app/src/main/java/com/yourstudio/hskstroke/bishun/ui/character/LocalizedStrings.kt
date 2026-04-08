package com.yourstudio.hskstroke.bishun.ui.character

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import kotlinx.coroutines.flow.first
import java.util.Locale

data class HelpSectionText(
    val title: String,
    val description: String,
    val bullets: List<String>,
)

data class PolicySectionText(
    val title: String,
    val bullets: List<String>,
)

data class SummaryRowText(val title: String, val detail: String)

data class CoursesStrings(
    val title: String,
    val description: String,
    val emptyCatalogMessage: String,
    val filterAll: String,
    val filterRemaining: String,
    val filterCompleted: String,
    val legendTitle: String,
    val legendActive: String,
    val legendCompleted: String,
    val legendRemaining: String,
    val legendHint: String,
    val symbolEmptyMessage: String,
    val symbolShowAllLabel: String,
    val symbolCollapseLabel: String,
    val activeCourseTitleFormat: String,
    val activeCourseStatusFormat: String,
    val activeResume: String,
    val activeSkip: String,
    val activeRestart: String,
    val activeExit: String,
    val levelHeaderFormat: String,
    val levelChipFormat: String,
    val levelProgressFormat: String,
    val levelNextFormat: String,
    val levelCompletedLabel: String,
    val lockedSignInLabel: String,
    val lockedUnlockLabel: String,
    val lockedBrowseLabel: String,
    val lockedGreatJobLabel: String,
    val iconStartDescriptionFormat: String,
    val toastSkippedLabel: String,
    val toastMarkedLearnedFormat: String,
    val toastCourseCompleteFormat: String,
    val toastAutoAdvancedFormat: String,
    val toastNextUpFormat: String,
)

data class AccountStrings(
    val title: String,
    val signInCardTitle: String,
    val signInDescriptionSignedIn: String,
    val signInDescriptionSignedOut: String,
    val signInButtonSignedIn: String,
    val signInButtonSignedOut: String,
    val courseCardTitle: String,
    val courseDescriptionSignedOut: String,
    val courseDescriptionUnlocked: String,
    val courseDescriptionLocked: String,
    val courseButtonSignedOut: String,
    val courseButtonUnlocked: String,
    val courseButtonLocked: String,
    val supportTitle: String,
    val supportDescription: String,
    val supportFeedbackButton: String,
    val clearDataTitle: String,
    val clearDataDescription: String,
    val clearDataButton: String,
    val clearDataDialogTitle: String,
    val clearDataDialogBody: String,
    val consentSignInTitle: String,
    val consentSignInBullets: List<String>,
    val consentSignInConfirm: String,
    val consentUnlockTitle: String,
    val consentUnlockBullets: List<String>,
    val consentUnlockConfirm: String,
    val consentCheckboxLabel: String,
    val cancelLabel: String,
    val closeLabel: String,
    val proCardTitle: String,
    val proStatusPro: String,
    val proStatusFree: String,
    val proStatusPlayRequired: String,
    val proStatusPending: String,
    val proStatusConnecting: String,
    val proStatusNotAvailable: String,
    val proDescriptionPro: String,
    val proDescriptionNoPlayStore: String,
    val proDescriptionFree: String,
    val proBillingStatusCodeFormat: String,
    val proRestorePurchasesButton: String,
    val proBuyButton: String,
    val proBuyButtonWithPriceFormat: String,
    val appearanceCardTitle: String,
    val appearanceCardDescription: String,
    val themeModeSystemLabel: String,
    val themeModeLightLabel: String,
    val themeModeDarkLabel: String,
    val accentColorLabel: String,
    val brushThicknessLabel: String,
    val requiresProChipLabelFormat: String,
    val accentColorOptionLabels: List<String>,
    val brushWidthOptionLabels: List<String>,
    val audioSafetyTitle: String,
    val audioSafetyDescription: String,
    val audioSafetyVolumeReminderLabel: String,
    val audioSafetyThresholdLabel: String,
    val audioSafetyLowerToLabel: String,
    val guidanceTitle: String,
    val guidanceDescription: String,
    val guidanceOpenButton: String,
    val dailyReminderTitle: String,
    val dailyReminderDescription: String,
    val dailyReminderEnabledLabel: String,
    val dailyReminderTimeLabelFormat: String,
    val dailyReminderChangeTimeButton: String,
    val dailyReminderOnlyWhenIncompleteLabel: String,
    val languageTitle: String,
    val languageDescription: String,
    val languageCurrentFormat: String,
    val languageChangeButton: String,
    val languageSystemOption: String,
    val languageChineseOption: String,
    val languageEnglishOption: String,
    val languageSpanishOption: String,
    val languageJapaneseOption: String,
    val languageKoreanOption: String,
    val languageFrenchOption: String,
    val dataTitle: String,
    val dataDescription: String,
    val clearDictionaryHistoryButton: String,
    val clearDictionaryHistoryDialogTitle: String,
    val clearDictionaryHistoryDialogBody: String,
    val clearDictionaryHistoryConfirmButton: String,
    val shareAppButton: String,
    val shareAppMessage: String,
    val rateAppButton: String,
    val hskWebsiteButton: String,
)

data class SupportStrings(
    val analyticsTitle: String,
    val analyticsDescription: String,
    val crashTitle: String,
    val crashDescription: String,
    val prefetchTitle: String,
    val prefetchDescription: String,
    val privacyCloseLabel: String,
    val feedbackTitle: String,
    val feedbackThanksTitle: String,
    val feedbackSavedMessage: String,
    val feedbackTopicLabel: String,
    val feedbackTopicPlaceholder: String,
    val feedbackMessageLabel: String,
    val feedbackMessagePlaceholder: String,
    val feedbackContactLabel: String,
    val feedbackContactPlaceholder: String,
    val feedbackSendLabel: String,
    val feedbackCloseLabel: String,
    val feedbackShareLogLabel: String,
    val feedbackCancelLabel: String,
    val feedbackLastSentFormat: String,
    val feedbackLogEmpty: String,
    val feedbackLogShareError: String,
    val feedbackLogNoApps: String,
    val feedbackEmailError: String,
    val feedbackEmailNoApp: String,
)

data class NavigationStrings(
    val homeLabel: String,
    val learnLabel: String,
    val libraryLabel: String,
    val accountLabel: String,
)

data class OnboardingPageText(
    val title: String,
    val body: String,
)

data class OnboardingStrings(
    val appTitle: String,
    val skipLabel: String,
    val backLabel: String,
    val nextLabel: String,
    val startLabel: String,
    val pages: List<OnboardingPageText>,
)

data class PracticeBoardStrings(
    val startLabel: String,
    val hintLabel: String,
    val previousLabel: String,
    val nextLabel: String,
    val settingsLabel: String,
    val pronunciationLabel: String,
    val dictionaryLabel: String,
    val hskLabel: String,
    val statusReadyLabel: String,
    val statusStrokeProgressFormat: String,
    val statusStartFromStrokeFormat: String,
    val statusTryAgainFormat: String,
    val statusGreatContinueLabel: String,
    val statusBackwardsAcceptedLabel: String,
    val statusPracticeCompleteLabel: String,
)

data class ProgressStrings(
    val title: String,
    val description: String,
    val jumpBackLabel: String,
    val dailyTitle: String,
    val dailyDescription: String,
    val dailyPracticeLabel: String,
    val dailyCompletedLabel: String,
    val learnedLabel: String,
    val streakLabel: String,
    val lastSessionLabel: String,
    val streakDaySingularFormat: String,
    val streakDayPluralFormat: String,
    val streakStartLabel: String,
    val lastSessionNeverLabel: String,
    val weeklyChartTitle: String,
    val levelsTitle: String,
    val levelsEmpty: String,
    val levelLabelFormat: String,
    val levelMasteredFormat: String,
    val levelJumpDescription: String,
    val levelBrowseCourses: String,
    val historyTitle: String,
    val historyEmpty: String,
    val historyStrokesFormat: String,
    val historyMistakesPerfect: String,
    val historyMistakesFormat: String,
    val historyLoadCharacterFormat: String,
    val relativeJustNow: String,
    val relativeMinutesFormat: String,
    val relativeHoursFormat: String,
    val relativeDaysFormat: String,
)

data class WidgetStrings(
    val dailyTitle: String,
    val streakDaysFormat: String,
    val completedTodayLabel: String,
    val practiceLabel: String,
    val dictionaryLabel: String,
    val tapToPracticeLabel: String,
    val notificationChannelName: String,
    val notificationChannelDescription: String,
)

data class LibraryStrings(
    val title: String,
    val description: String,
    val tabSearchLabel: String,
    val tabFavoritesLabel: String,
    val tabHistoryLabel: String,
    val inputLabel: String,
    val supportingText: String,
    val resultsHeader: String,
    val quickTryLabel: String,
    val recentHeader: String,
    val recentClear: String,
    val recentOverflowLabel: String,
    val recentOverflowDialogTitle: String,
    val recentOverflowClose: String,
    val lookupLabel: String,
    val lookupLoadingLabel: String,
    val clearResultLabel: String,
    val editLabel: String,
    val doneLabel: String,
    val selectAllLabel: String,
    val deselectAllLabel: String,
    val selectedCountFormat: String,
    val deleteLabel: String,
    val favoritesFilterLabel: String,
    val historyFilterLabel: String,
    val filterNoResultsLabel: String,
    val favoritesHeader: String,
    val favoritesEmpty: String,
    val favoritesSaveLabel: String,
    val favoritesSavedLabel: String,
    val favoritesAddLabel: String,
    val favoritesRemoveLabel: String,
    val favoritesClearLabel: String,
    val favoritesClearDialogTitle: String,
    val favoritesClearDialogBody: String,
    val favoritesClearConfirmLabel: String,
    val favoritesClearCancelLabel: String,
    val historyHeader: String,
    val historyPinnedHeader: String,
    val historyEmpty: String,
    val historyPinLabel: String,
    val historyUnpinLabel: String,
    val historyRemoveLabel: String,
    val historyClearLabel: String,
    val historyClearDialogTitle: String,
    val historyClearDialogBody: String,
    val historyClearConfirmLabel: String,
    val historyClearCancelLabel: String,
    val sortRecentLabel: String,
    val sortNameLabel: String,
    val moreActionsLabel: String,
    val helpTitle: String,
    val helpBody: String,
    val pinyinLabelFormat: String,
    val radicalsStrokesFormat: String,
    val traditionalLabelFormat: String,
    val definitionFallback: String,
    val valueNotAvailable: String,
    val valueUnknown: String,
    val practiceButtonLabel: String,
    val practiceCharactersLabel: String,
    val quickSuggestions: List<String>,
    val errorEmpty: String,
    val errorNotFound: String,
    val errorRead: String,
)

data class LocalizedStrings(
    val locale: Locale,
    val appTitle: String,
    val navigation: NavigationStrings,
    val onboarding: OnboardingStrings,
    val searchLabel: String,
    val loadButton: String,
    val clearButton: String,
    val loadingLabel: String,
    val coursesDialogTitle: String,
    val coursePlanHeading: String,
    val courseIntroTitle: String,
    val courseIntroBullets: List<String>,
    val courseEmptyTitle: String,
    val courseEmptyDescription: String,
    val courseNoDataMessage: String,
    val expandCharactersLabel: String,
    val collapseCharactersLabel: String,
    val legendTitle: String,
    val legendGesture: String,
    val legendActiveLabel: String,
    val legendCompletedLabel: String,
    val legendRemainingLabel: String,
    val courseLegendHint: String,
    val courseLevelCompleteLabel: String,
    val courseLevelStartFormat: String,
    val courseLevelNextFormat: String,
    val levelLabelFormat: String,
    val courseLevelProgressFormat: String,
    val loadLevelFormat: String,
    val filterAllLabel: String,
    val filterRemainingLabel: String,
    val filterCompletedLabel: String,
    val helpTitle: String,
    val helpSections: List<HelpSectionText>,
    val helpConfirm: String,
    val privacyTitle: String,
    val privacyIntro: String,
    val dataSafetyHeading: String,
    val privacySummaryRows: List<SummaryRowText>,
    val contactSupportLabel: String,
    val emailSupportButton: String,
    val viewPolicyButton: String,
    val fullPolicyTitle: String,
    val fullPolicySections: List<PolicySectionText>,
    val progress: ProgressStrings,
    val library: LibraryStrings,
    val courses: CoursesStrings,
    val account: AccountStrings,
    val support: SupportStrings,
    val widget: WidgetStrings,
    val boardControls: PracticeBoardStrings,
)

@Composable
fun rememberLocalizedStrings(languageOverride: String?): LocalizedStrings {
    val contextLocale = LocalContext.current.resources.configuration.locales[0]
    val targetLocale = remember(languageOverride, contextLocale) {
        languageOverride?.takeIf { it.isNotBlank() }?.let { Locale.forLanguageTag(it) } ?: contextLocale
    }
    return remember(targetLocale) {
        when (targetLocale.language) {
            "zh" -> localizedStringsZh(targetLocale)
            "es" -> localizedStringsEs(targetLocale)
            "ja" -> localizedStringsJa(targetLocale)
            "ko" -> localizedStringsKo(targetLocale)
            "fr" -> localizedStringsFr(targetLocale)
            else -> localizedStringsEn(targetLocale)
        }
    }
}

/**
 * Non-Composable helper for use in widgets, notifications, and other contexts
 * where Compose LocalContext is unavailable. Reads language override from DataStore.
 */
suspend fun resolveLocalizedStrings(context: Context): LocalizedStrings {
    val contextLocale = context.resources.configuration.locales[0]
    val prefs = UserPreferencesStore(context).data.first()
    val languageOverride = prefs.languageOverride
    val targetLocale = languageOverride?.takeIf { it.isNotBlank() }?.let { Locale.forLanguageTag(it) }
        ?: contextLocale
    return when (targetLocale.language) {
        "zh" -> localizedStringsZh(targetLocale)
        "es" -> localizedStringsEs(targetLocale)
        "ja" -> localizedStringsJa(targetLocale)
        "ko" -> localizedStringsKo(targetLocale)
        "fr" -> localizedStringsFr(targetLocale)
        else -> localizedStringsEn(targetLocale)
    }
}
