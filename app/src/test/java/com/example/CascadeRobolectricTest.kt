package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.example.data.GameRepository
import com.example.game.BlockColor
import com.example.game.Board
import com.example.game.GamePiece
import com.example.game.PieceShape
import com.example.game.SavedClassicGame
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CascadeRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `12_save_and_restore_roundtrip_preserves_board_tray_and_score`() {
        val board = Board.fromPattern(
            listOf(
                "........",
                "........",
                "..12....",
                "........",
                "....34..",
                "........",
                "........",
                "000....."
            ),
            startId = 50L
        )
        val snapshot = SavedClassicGame(
            board = board,
            trayPieces = listOf(
                GamePiece(201L, PieceShape.SQUARE_2X2, BlockColor.CYAN),
                null,
                GamePiece(202L, PieceShape.LINE_3_H, BlockColor.GOLD_SAFE())
            ),
            score = 345,
            movesPlayed = 14,
            bestCascadeDepth = 3,
            totalLinesCleared = 8,
            nextIdCounter = 250L
        )

        val json = GameRepository.serializeClassicGame(snapshot)
        val restored = GameRepository.deserializeClassicGame(json)

        assertNotNull(restored)
        assertEquals(snapshot.score, restored!!.score)
        assertEquals(snapshot.movesPlayed, restored.movesPlayed)
        assertEquals(snapshot.bestCascadeDepth, restored.bestCascadeDepth)
        assertEquals(snapshot.totalLinesCleared, restored.totalLinesCleared)
        assertEquals(snapshot.nextIdCounter, restored.nextIdCounter)
        assertEquals(snapshot.board, restored.board)
        assertEquals(snapshot.trayPieces, restored.trayPieces)

        // Also test DataStore persistence roundtrip
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GameRepository(context)
        runBlocking {
            repo.updateClassicBestScore(999)
            repo.saveClassicGame(snapshot)
            val loaded = repo.loadInitialProgress()
            assertEquals(999, loaded.classicBestScore)
            assertEquals(345, loaded.savedClassicGame?.score)
        }
    }

    @Test
    fun `13_ui_renders_board_tray_modes_and_dialogs_interactively`() {
        composeRule.waitForIdle()

        // Main game elements visible
        composeRule.onNodeWithTag("game_board").assertIsDisplayed()
        composeRule.onNodeWithTag("piece_tray").assertIsDisplayed()
        composeRule.onNodeWithTag("tray_slot_0").assertIsDisplayed()
        composeRule.onNodeWithTag("tray_slot_1").assertIsDisplayed()
        composeRule.onNodeWithTag("tray_slot_2").assertIsDisplayed()
        composeRule.onNodeWithTag("score_card").assertIsDisplayed()
        composeRule.onNodeWithTag("best_score_card").assertIsDisplayed()

        // Tap-to-select piece slot 0 then tap board to place
        composeRule.onNodeWithTag("tray_slot_0").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("game_board").performClick()
        composeRule.waitForIdle()

        // Switch to Challenge mode and verify target & moves HUD appear
        composeRule.onNodeWithTag("mode_challenge_tab").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("target_card").assertIsDisplayed()
        composeRule.onNodeWithTag("moves_card").assertIsDisplayed()

        // Open Pause dialog and resume
        composeRule.onNodeWithTag("pause_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("pause_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("resume_button").performClick()
        composeRule.waitForIdle()

        // Open Gravity Rules dialog and close
        composeRule.onNodeWithTag("rules_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("rules_dialog").assertIsDisplayed()
        composeRule.onNodeWithText("Got It").performClick()
        composeRule.waitForIdle()
    }

    private fun BlockColor.Companion.GOLD_SAFE(): BlockColor = BlockColor.AMBER
}
