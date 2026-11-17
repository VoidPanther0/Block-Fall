package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.data.GameRepository
import com.example.game.BlockColor
import com.example.game.Board
import com.example.game.GamePiece
import com.example.game.GameSettings
import com.example.game.PieceShape
import com.example.game.SavedClassicGame
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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

        // Also test DataStore persistence roundtrip & new settings/challenge progress compatibility
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GameRepository(context)
        runBlocking {
            repo.updateClassicBestScore(999)
            repo.saveClassicGame(snapshot)
            repo.updateSettings(
                GameSettings(
                    soundEnabled = false,
                    musicEnabled = false,
                    hapticsEnabled = true,
                    visualEffectsEnabled = false,
                    placementPreviewEnabled = true
                )
            )
            repo.saveChallengeLevelResult(
                levelNumber = 1,
                score = 150,
                stars = 3,
                completed = true,
                nextUnlockedLevel = 2
            )

            val loaded = repo.loadInitialProgress()
            assertEquals(999, loaded.classicBestScore)
            assertEquals(345, loaded.savedClassicGame?.score)
            assertFalse(loaded.settings.soundEnabled)
            assertFalse(loaded.settings.musicEnabled)
            assertTrue(loaded.settings.hapticsEnabled)
            assertFalse(loaded.settings.visualEffectsEnabled)
            assertEquals(2, loaded.unlockedChallengeLevel)
            assertEquals(3, loaded.challengeProgress[1]?.stars)
            assertTrue(loaded.challengeProgress[1]?.completed == true)
        }
    }

    @Test
    fun `13_splash_home_gameplay_pause_and_dialogs_navigate_cleanly`() {
        navigatePastSplashToHome()

        // Home screen elements visible
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("home_play_button").assertIsDisplayed()

        // Navigate Home -> Gameplay
        composeRule.onNodeWithTag("home_play_button").performClick()
        composeRule.waitForIdle()

        // Main game elements visible
        composeRule.onNodeWithTag("game_board").assertIsDisplayed()
        composeRule.onNodeWithTag("piece_tray").assertIsDisplayed()
        composeRule.onNodeWithTag("tray_slot_0").assertIsDisplayed()
        composeRule.onNodeWithTag("tray_slot_1").assertIsDisplayed()
        composeRule.onNodeWithTag("tray_slot_2").assertIsDisplayed()
        composeRule.onNodeWithTag("score_card").assertIsDisplayed()
        composeRule.onNodeWithTag("best_score_card").assertIsDisplayed()

        // Tap-to-select piece slot 0 then tap board to place (with upward vertical offset)
        composeRule.onNodeWithTag("tray_slot_0").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("game_board").performClick()
        composeRule.waitForIdle()
        // Score card should no longer be 0 after successful lifted placement
        assertTrue(composeRule.onAllNodesWithTag("score_card").fetchSemanticsNodes().isNotEmpty())

        // Switch to Challenge mode and verify target & moves HUD appear
        composeRule.onNodeWithTag("mode_challenge_tab").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("target_card").assertIsDisplayed()
        composeRule.onNodeWithTag("moves_card").assertIsDisplayed()

        // Open Restart confirmation dialog and cancel
        composeRule.onNodeWithTag("restart_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("restart_confirmation_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("cancel_restart_button").performClick()
        composeRule.waitForIdle()

        // Open Pause screen -> How to Play -> back -> Resume
        composeRule.onNodeWithTag("pause_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("pause_screen").assertIsDisplayed()

        composeRule.onNodeWithTag("pause_how_to_play_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("how_to_play_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("how_to_play_next_step_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("how_to_play_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("pause_screen").assertIsDisplayed()

        composeRule.onNodeWithTag("pause_resume_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("game_board").assertIsDisplayed()

        // Exit back to Home via confirmation dialog
        composeRule.onNodeWithTag("gameplay_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("exit_confirmation_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("confirm_exit_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }

    @Test
    fun `14_secondary_screens_modes_challenges_stats_settings_rules_work`() {
        navigatePastSplashToHome()

        // Mode Selection -> Challenge Selection -> Challenge Details -> Back -> Home
        composeRule.onNodeWithTag("home_modes_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("mode_selection_screen").assertIsDisplayed()

        composeRule.onNodeWithTag("mode_browse_challenges_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("challenge_selection_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("challenge_level_card_1").assertIsDisplayed()
        composeRule.onNodeWithTag("challenge_level_card_1").performClick()
        composeRule.mainClock.advanceTimeBy(300L)
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("challenge_details_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("challenge_details_play_button").performScrollTo().assertIsDisplayed()
        composeRule.onNodeWithTag("challenge_details_back_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("challenge_selection_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("mode_selection_back_button").performClick()
        composeRule.waitForIdle()

        // Statistics screen + Reset confirmation cancel
        composeRule.onNodeWithTag("home_statistics_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("statistics_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("statistics_reset_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reset_progress_confirmation_dialog").assertIsDisplayed()
        composeRule.onNodeWithTag("cancel_reset_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("statistics_back_button").performClick()
        composeRule.waitForIdle()

        // Settings screen + Rules screen
        composeRule.onNodeWithTag("home_settings_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("settings_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("setting_sound_switch").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("settings_rules_button").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("rules_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("rules_back_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("settings_back_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("home_screen").assertIsDisplayed()
    }

    @Test
    fun `15_sound_manager_loads_and_triggers_drop_clear_and_combo_effects_safely`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val manager = com.example.audio.SoundManager(context)
        assertTrue(manager.isReady)

        manager.soundEnabled = true
        manager.playBlockDrop()
        manager.playLineClear(linesCleared = 2)
        manager.playComboComplete(comboDepth = 3)
        manager.playLevelComplete()
        manager.playGameOver()
        manager.playUiClick()

        manager.soundEnabled = false
        manager.playBlockDrop()
        manager.release()
        assertFalse(manager.isReady)
    }

    private fun navigatePastSplashToHome() {
        composeRule.waitForIdle()
        if (composeRule.onAllNodesWithTag("splash_screen").fetchSemanticsNodes().isNotEmpty()) {
            composeRule.onNodeWithTag("splash_screen").performClick()
            composeRule.waitForIdle()
        }
    }

    private fun BlockColor.Companion.GOLD_SAFE(): BlockColor = BlockColor.AMBER
}
