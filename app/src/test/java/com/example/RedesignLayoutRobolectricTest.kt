package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36], qualifiers = "w320dp-h568dp")
class RedesignLayoutRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `18_narrow_screen_home_shop_categories_and_profile_render_cleanly`() {
        composeRule.waitForIdle()
        if (composeRule.onAllNodesWithTag("onboarding_skip_button").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("onboarding_skip_button").performClick()
            composeRule.waitForIdle()
        }
        if (composeRule.onAllNodesWithTag("splash_screen").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("splash_screen").performClick()
            composeRule.waitForIdle()
        }

        // Home renders hero play CTA on narrow 320dp width
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("home_play_button").assertIsDisplayed()

        // Home -> Play (Mode Selection)
        composeRule.onNodeWithTag("home_modes_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("mode_selection_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("mode_play_classic_button").assertIsDisplayed()

        // Back, then Home -> Challenges
        composeRule.onNodeWithTag("mode_selection_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_challenges_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("challenge_selection_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("challenge_level_card_1").assertIsDisplayed()

        // Back, then Home -> Shop and inspect all 5 sections
        composeRule.onNodeWithTag("challenge_selection_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_shop_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("shop_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("shop_wallet_bar").assertIsDisplayed()
        composeRule.onNodeWithTag("shop_gold_balance").assertIsDisplayed()
        composeRule.onNodeWithTag("shop_diamond_balance").assertIsDisplayed()

        composeRule.onNodeWithTag("shop_tab_diamonds").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("shop_item_entitlement_prism_crown").assertIsDisplayed()

        composeRule.onNodeWithTag("shop_tab_gold").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("shop_watch_ad_button").assertIsDisplayed()

        composeRule.onNodeWithTag("shop_tab_abilities").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("shop_wallet_bar").assertIsDisplayed()

        composeRule.onNodeWithTag("shop_tab_vip_teams").performScrollTo().performClick()
        composeRule.waitForIdle()

        // Back, then Home -> Profile (Rank, 100-Diamond Name Change, Avatar Picker, Best Score)
        composeRule.onNodeWithTag("shop_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_statistics_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("statistics_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("player_rank_card").assertIsDisplayed()
        composeRule.onNodeWithTag("profile_rename_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("profile_avatar_picker_card").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("profile_best_score_card").performScrollTo().assertIsDisplayed()
        assertEquals(0, composeRule.onAllNodesWithText("Challenge Mastery", substring = true).fetchSemanticsNodes().size)
        assertEquals(0, composeRule.onAllNodesWithText("Career Telemetry", substring = true).fetchSemanticsNodes().size)
        assertEquals(0, composeRule.onAllNodesWithText("Achievements", substring = true).fetchSemanticsNodes().size)

        // Return to Home -> Play Classic -> Verify Banner Ad footer and no NO FIT/drag hint text
        composeRule.onNodeWithTag("statistics_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("home_play_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("gameplay_screen").assertIsDisplayed()
        val bannerNode = composeRule.onNodeWithTag("gameplay_banner_ad").fetchSemanticsNode()
        org.junit.Assert.assertTrue(
            "Expected gameplay_banner_ad height > 0, bounds=${bannerNode.boundsInRoot}",
            bannerNode.boundsInRoot.height > 0f
        )
        composeRule.onNodeWithTag("gameplay_banner_ad").assertIsDisplayed()
        assertEquals(0, composeRule.onAllNodesWithText("Drag or tap", substring = true).fetchSemanticsNodes().size)
        assertEquals(0, composeRule.onAllNodesWithText("NO FIT", substring = true).fetchSemanticsNodes().size)
    }
}
