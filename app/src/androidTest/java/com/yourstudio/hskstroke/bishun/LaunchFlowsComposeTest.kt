package com.yourstudio.hskstroke.bishun

import android.content.Intent
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yourstudio.hskstroke.bishun.data.daily.DailyPracticeUseCase
import com.yourstudio.hskstroke.bishun.data.settings.UserPreferencesStore
import com.yourstudio.hskstroke.bishun.ui.navigation.AppLaunchRequests
import com.yourstudio.hskstroke.bishun.ui.testing.TestTags
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class LaunchFlowsComposeTest {
    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    private fun skipOnboardingIfNeeded() {
        val hasOnboarding = composeTestRule
            .onAllNodesWithTag(TestTags.ONBOARDING_SKIP)
            .fetchSemanticsNodes()
            .isNotEmpty()

        if (!hasOnboarding) return

        composeTestRule.onNodeWithTag(TestTags.ONBOARDING_SKIP).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(TestTags.NAV_HOME).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun sendLaunchIntent(intent: Intent) {
        composeTestRule.activityRule.scenario.onActivity { activity ->
            activity.dispatchLaunchIntent(intent)
        }
    }

    private fun waitForHomeQuery(expected: String) {
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            runCatching {
                composeTestRule.onNodeWithTag(TestTags.HOME_QUERY_FIELD).assertTextEquals(expected)
                true
            }.getOrDefault(false)
        }
    }

    @Test
    fun widgetPracticeIntent_loadsCharacterInHome() {
        skipOnboardingIfNeeded()

        sendLaunchIntent(AppLaunchRequests.practiceIntent(composeTestRule.activity, "\u597d"))

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag(TestTags.HOME_PRACTICE_CONTENT).fetchSemanticsNodes().isNotEmpty()
        }
        waitForHomeQuery("\u597d")
    }

    @Test
    fun widgetDictionaryIntent_practiceButtonNavigatesToHome() {
        skipOnboardingIfNeeded()

        sendLaunchIntent(AppLaunchRequests.dictionaryIntent(composeTestRule.activity, "\u597d"))

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag(TestTags.SCREEN_LIBRARY).fetchSemanticsNodes().isNotEmpty()
        }
        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag(TestTags.LIBRARY_PRACTICE_BUTTON).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(TestTags.LIBRARY_PRACTICE_BUTTON).performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag(TestTags.SCREEN_HOME).fetchSemanticsNodes().isNotEmpty()
        }
        waitForHomeQuery("\u597d")
    }

    @Test
    fun dailyBadgePracticeButton_switchesToPracticeAndLoadsDailySymbol() {
        skipOnboardingIfNeeded()

        val todayEpochDay = DailyPracticeUseCase.todayEpochDay()
        runBlocking {
            UserPreferencesStore(composeTestRule.activity.applicationContext).setDailyPractice("\u597d", todayEpochDay)
        }

        composeTestRule.onNodeWithTag(TestTags.HOME_TAB_DAILY).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(TestTags.HOME_DAILY_BADGE).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(TestTags.HOME_DAILY_PRACTICE_BUTTON).performClick()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag(TestTags.HOME_PRACTICE_CONTENT).fetchSemanticsNodes().isNotEmpty()
        }
        waitForHomeQuery("\u597d")
    }
}
