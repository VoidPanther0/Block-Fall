package com.example

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.example.ui.components.CascadeCalloutBar
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
                        onShowHowToPlay = {},
                        onRestart = {},
                        onPause = {}
                    )
                    ScorePanel(state = uiState)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CascadeCalloutBar(floatingBanner = uiState.floatingBanner)
                            Spacer(Modifier.height(4.dp))
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
        }

        composeRule.waitForIdle()
        val boardTopBefore = composeRule.onNodeWithTag("game_board")
            .fetchSemanticsNode()
            .boundsInRoot
            .top
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

        // Only one callout pill exists and the board vertical origin never moved
        assertEquals(1, composeRule.onAllNodesWithTag("cascade_banner").fetchSemanticsNodes().size)
        composeRule.onNodeWithTag("cascade_banner").assertIsDisplayed()
        val boardTopDuringClear = composeRule.onNodeWithTag("game_board")
            .fetchSemanticsNode()
            .boundsInRoot
            .top
        assertEquals(boardTopBefore, boardTopDuringClear, 0.5f)
    }
}
