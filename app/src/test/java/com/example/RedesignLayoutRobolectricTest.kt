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
@Config(sdk = [36], qualifiers = "w320dp-h568dp")
class RedesignLayoutRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `18_narrow_screen_bottom_nav_shop_categories_and_profile_render_cleanly`() {
        composeRule.waitForIdle()
        if (composeRule.onAllNodesWithTag("onboarding_skip_button").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("onboarding_skip_button").performClick()
            composeRule.waitForIdle()
        }
        if (composeRule.onAllNodesWithTag("splash_screen").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("splash_screen").performClick()
            composeRule.waitForIdle()
        }

        // Home renders hero play CTA and bottom navigation on narrow 320dp width
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("home_play_button").assertIsDisplayed()
        composeRule.onNodeWithTag("premium_bottom_navigation").assertIsDisplayed()

        // Navigate via bottom nav -> Play (Mode Selection)
        composeRule.onNodeWithTag("bottom_nav_play").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("mode_selection_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("mode_play_classic_button").assertIsDisplayed()

        // Navigate via bottom nav -> Challenges
        composeRule.onNodeWithTag("bottom_nav_challenges").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("challenge_selection_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("challenge_level_card_1").assertIsDisplayed()

        // Navigate via bottom nav -> Shop and inspect all 5 sections
        composeRule.onNodeWithTag("bottom_nav_shop").performClick()
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

        // Navigate via bottom nav -> Profile (Statistics & Achievements)
        composeRule.onNodeWithTag("bottom_nav_profile").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("statistics_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("player_rank_card").assertIsDisplayed()

        // Return to Home via bottom nav
        composeRule.onNodeWithTag("bottom_nav_home").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }
}
