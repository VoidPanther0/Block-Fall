package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w840dp-h600dp-land")
class TabletLayoutRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `16_tablet_landscape_renders_adaptive_home_challenges_and_gameplay`() {
        composeRule.waitForIdle()
        if (composeRule.onAllNodesWithTag("onboarding_skip_button").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("onboarding_skip_button").performClick()
            composeRule.waitForIdle()
        }
        if (composeRule.onAllNodesWithTag("splash_screen").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("splash_screen").performClick()
            composeRule.waitForIdle()
        }

        // Wide 2-column Home displays primary CTA + navigation tiles simultaneously
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("home_play_button").assertIsDisplayed()
        composeRule.onNodeWithTag("home_challenges_button").assertIsDisplayed()

        // Challenges shows side-by-side level grid + inspector summary card
        composeRule.onNodeWithTag("home_challenges_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("challenge_selection_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("challenge_level_card_1").assertIsDisplayed()
        composeRule.onNodeWithTag("challenge_target_summary_card").assertIsDisplayed()
        composeRule.onNodeWithTag("challenge_play_button").performScrollTo().performClick()
        composeRule.waitForIdle()

        // Landscape 2-pane Gameplay displays board + HUD + tray simultaneously
        composeRule.onNodeWithTag("gameplay_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("game_board").assertIsDisplayed()
        composeRule.onNodeWithTag("piece_tray").assertIsDisplayed()
        composeRule.onNodeWithTag("score_card").assertIsDisplayed()
        composeRule.onNodeWithTag("pause_button").assertIsDisplayed()
    }
}
