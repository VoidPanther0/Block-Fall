package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import com.example.game.Board
import com.example.ui.BoardVisualPhase
import com.example.ui.FloatingBanner
import com.example.ui.GameUiState
import com.example.ui.components.GameBoard
import com.example.ui.components.ScorePanel
import com.example.ui.components.TopGameBar
import com.example.ui.theme.CascadeTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BoardStabilityRobolectricTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun `17_line_clear_callout_is_unique_and_never_shifts_board_vertically`() {
        var uiState by mutableStateOf(GameUiState(score = 54, classicBestScore = 54))

        composeRule.setContent {
            CascadeTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TopGameBar(
                        state = uiState,
                        onPause = {}
                    )
                    ScorePanel(state = uiState)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        GameBoard(
                            board = Board.empty(),
                            visualPhase = BoardVisualPhase.Idle,
                            dragState = null,
                            floatingBanner = uiState.floatingBanner,
                            particleTriggerId = 0L,
                            latestParticles = emptyList(),
                            hasSelectedSlot = false,
                            onCellTapped = { _, _ -> },
                            onBoardBoundsChanged = { _: Rect -> },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        composeRule.waitForIdle()
        val boardBoundsBefore = composeRule.onNodeWithTag("game_board")
            .fetchSemanticsNode()
            .boundsInRoot
        assertEquals(0, composeRule.onAllNodesWithTag("cascade_banner").fetchSemanticsNodes().size)

        // Trigger active line-clear & cascade wave state
        uiState = uiState.copy(
            score = 64,
            currentCascadeWave = 1,
            floatingBanner = FloatingBanner(
                id = 1L,
                headline = "LINE CLEAR",
                subline = null,
                pointsText = "+10",
                isChain = false
            )
        )
        composeRule.waitForIdle()

        // Exactly one callout pill exists, it is drawn inside the board, and the board's
        // origin and size are unchanged by its presence.
        assertEquals(1, composeRule.onAllNodesWithTag("cascade_banner").fetchSemanticsNodes().size)
        val bannerBounds = composeRule.onNodeWithTag("cascade_banner")
            .assertIsDisplayed()
            .fetchSemanticsNode()
            .boundsInRoot
        val boardBoundsDuringClear = composeRule.onNodeWithTag("game_board")
            .fetchSemanticsNode()
            .boundsInRoot

        org.junit.Assert.assertTrue(
            "Callout pill must be rendered inside the game board bounds",
            bannerBounds.left >= boardBoundsDuringClear.left &&
                bannerBounds.top >= boardBoundsDuringClear.top &&
                bannerBounds.right <= boardBoundsDuringClear.right &&
                bannerBounds.bottom <= boardBoundsDuringClear.bottom
        )
        assertEquals(boardBoundsBefore.top, boardBoundsDuringClear.top, 0.5f)
        assertEquals(boardBoundsBefore.width, boardBoundsDuringClear.width, 0.5f)
        assertEquals(boardBoundsBefore.height, boardBoundsDuringClear.height, 0.5f)
    }

    @Test
    fun `18_game_over_screen_renders_logo_title_score_above_best_and_two_buttons`() {
        composeRule.setContent {
            CascadeTheme {
                com.example.ui.screens.GameOverScreen(
                    state = GameUiState(score = 4250, classicBestScore = 9800),
                    onRetry = {},
                    onHome = {}
                )
            }
        }
        composeRule.waitForIdle()

        val titleBounds = composeRule.onNodeWithTag("game_over_title_text").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val scoreBounds = composeRule.onNodeWithTag("game_over_score_value").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val bestBounds = composeRule.onNodeWithTag("game_over_best_score_value").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val retryBounds = composeRule.onNodeWithTag("game_over_retry_button").assertIsDisplayed().fetchSemanticsNode().boundsInRoot
        val homeBounds = composeRule.onNodeWithTag("game_over_home_button").assertIsDisplayed().fetchSemanticsNode().boundsInRoot

        org.junit.Assert.assertTrue("Title must appear above score", titleBounds.bottom <= scoreBounds.top + 1f)
        org.junit.Assert.assertTrue("Score must appear above best score", scoreBounds.bottom <= bestBounds.top + 1f)
        org.junit.Assert.assertTrue("Best score must appear above Play Again button", bestBounds.bottom <= retryBounds.top + 1f)
        org.junit.Assert.assertTrue("Play Again button must appear above Home button", retryBounds.bottom <= homeBounds.top + 1f)
    }
}
