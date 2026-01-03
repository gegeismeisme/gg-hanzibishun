package com.yourstudio.hskstroke.bishun

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.yourstudio.hskstroke.bishun.ui.testing.TestTags
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MainNavigationComposeTest {
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

    @Test
    fun bottomNavigation_hasFourTabs() {
        skipOnboardingIfNeeded()

        composeTestRule.onNodeWithTag(TestTags.NAV_HOME).assertExists()
        composeTestRule.onNodeWithTag(TestTags.NAV_LEARN).assertExists()
        composeTestRule.onNodeWithTag(TestTags.NAV_LIBRARY).assertExists()
        composeTestRule.onNodeWithTag(TestTags.NAV_ACCOUNT).assertExists()
    }

    @Test
    fun homeSecondaryTabs_showsDailyBadgeWithoutHidingPractice() {
        skipOnboardingIfNeeded()

        composeTestRule.waitUntil(timeoutMillis = 10_000) {
            composeTestRule.onAllNodesWithTag(TestTags.HOME_PRACTICE_CONTENT).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(TestTags.HOME_TAB_DAILY).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(TestTags.HOME_DAILY_BADGE).fetchSemanticsNodes().isNotEmpty()
        }

        composeTestRule.onNodeWithTag(TestTags.HOME_TAB_PRACTICE).performClick()
        composeTestRule.waitUntil(timeoutMillis = 5_000) {
            composeTestRule.onAllNodesWithTag(TestTags.HOME_PRACTICE_CONTENT).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
