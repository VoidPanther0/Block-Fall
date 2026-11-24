package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import com.example.ui.FloatingBanner
import com.example.ui.components.CascadeCalloutOverlay
import com.example.ui.components.calloutBadgeFor
import com.example.ui.theme.CascadeTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CascadeCalloutOverlayRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun banner(
        id: Long,
        headline: String,
        depth: Int = 1,
        subline: String? = null,
        points: String = "+10",
        full: Boolean = false,
        total: Int? = null
    ) = FloatingBanner(
        id = id,
        headline = headline,
        subline = subline,
        pointsText = points,
        isChain = depth >= 2 || full,
        depth = depth,
        linesCount = 1,
        isFullClear = full,
        turnTotalPoints = total
    )

    private fun count(tag: String) = composeRule.onAllNodesWithTag(tag).fetchSemanticsNodes().size

    @Test
    fun exitAnimationKeepsContentUntilItFinishes() {
        var current by mutableStateOf<FloatingBanner?>(null)
        composeRule.setContent {
            CascadeTheme {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    CascadeCalloutOverlay(floatingBanner = current)
                }
            }
        }
        composeRule.waitForIdle()
        assertEquals(0, count("cascade_banner"))

        current = banner(1, "LINE CLEAR")
        composeRule.waitForIdle()
        assertEquals(1, count("cascade_banner"))

        composeRule.mainClock.autoAdvance = false
        current = null
        composeRule.mainClock.advanceTimeBy(60)
        // Mid-exit the pill must still be composed (the last banner is remembered for the animation).
        assertEquals(1, count("cascade_banner"))
        composeRule.mainClock.advanceTimeBy(1_000)
        composeRule.mainClock.autoAdvance = true
        composeRule.waitForIdle()
        assertEquals(0, count("cascade_banner"))
    }

    @Test
    fun consecutiveWavesReplaceContentAndShowSublineAndTotal() {
        var current by mutableStateOf<FloatingBanner?>(banner(1, "LINE CLEAR"))
        composeRule.setContent {
            CascadeTheme {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
                    CascadeCalloutOverlay(floatingBanner = current)
                }
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithText("LINE CLEAR").assertExists()

        current = banner(
            id = 2,
            headline = "GRAVITY CHAIN x2!",
            depth = 2,
            subline = "1 line chained by gravity",
            points = "+45",
            total = 55
        )
        composeRule.waitForIdle()
        assertEquals(1, count("cascade_banner"))
        assertEquals(0, composeRule.onAllNodesWithText("LINE CLEAR").fetchSemanticsNodes().size)
        composeRule.onNodeWithText("GRAVITY CHAIN x2!").assertExists()
        composeRule.onNodeWithText("+45").assertExists()
        composeRule.onNodeWithText("Total +55").assertExists()
        // badge and subline share the secondary line and are not duplicated from the headline
        composeRule.onNodeWithText("💫 COMBO ×2 · 1 line chained by gravity").assertExists()
    }

    @Test
    fun badgeUsesEngineDepthAndFullClearWins() {
        assertNull(calloutBadgeFor(banner(1, "LINE CLEAR", depth = 1)).first)
        assertEquals("💫 COMBO ×2", calloutBadgeFor(banner(2, "x", depth = 2)).first)
        assertEquals("✨ SUPER COMBO ×3", calloutBadgeFor(banner(3, "x", depth = 3)).first)
        // A full clear at wave >= 2 must read PERFECT, not COMBO.
        assertNotNull(calloutBadgeFor(banner(4, "x", depth = 3, full = true)).first)
        assertEquals("👑 PERFECT CLEAR!", calloutBadgeFor(banner(4, "x", depth = 3, full = true)).first)
    }
}
