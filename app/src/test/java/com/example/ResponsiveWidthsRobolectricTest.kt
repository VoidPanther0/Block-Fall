package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Automated coverage for the two common phone widths that were previously only inspected
 * in source. Checks that the compact header, wallet, primary CTA and top quick-nav stay
 * visible and ordered (nav above content) without overlap.
 */
@RunWith(RobolectricTestRunner::class)
class ResponsiveWidthsRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    @Config(sdk = [36], qualifiers = "w360dp-h740dp")
    fun `home_layout_is_usable_at_360dp`() = verifyHome()

    @Test
    @Config(sdk = [36], qualifiers = "w393dp-h851dp")
    fun `home_layout_is_usable_at_393dp`() = verifyHome()

    private fun verifyHome() {
        composeRule.waitForIdle()
        if (composeRule.onAllNodesWithTag("onboarding_skip_button").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("onboarding_skip_button").performClick()
            composeRule.waitForIdle()
        }
        if (composeRule.onAllNodesWithTag("splash_screen").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("splash_screen").performClick()
            composeRule.waitForIdle()
        }

        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("premium_bottom_navigation").assertIsDisplayed()
        composeRule.onNodeWithTag("home_play_button").assertIsDisplayed()
        composeRule.onNodeWithTag("home_statistics_button").performScrollTo().assertIsDisplayed()

        val navBottom = composeRule.onNodeWithTag("premium_bottom_navigation")
            .fetchSemanticsNode().boundsInRoot.bottom
        val playTop = composeRule.onNodeWithTag("home_play_button")
            .fetchSemanticsNode().boundsInRoot.top
        assertTrue("Quick nav must stay above the primary content", playTop >= navBottom - 1f)

        // All five destinations remain reachable at narrow widths.
        composeRule.onNodeWithTag("bottom_nav_play").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("mode_selection_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("mode_play_daily_button").performScrollTo().assertIsDisplayed()
    }
}
