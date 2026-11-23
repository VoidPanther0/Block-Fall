package com.example

import com.example.game.Board
import com.example.game.DailyChallengeGenerator
import com.example.game.EconomyEngine
import com.example.game.EconomyMutationResult
import com.example.game.GameStatistics
import com.example.game.LineClearEngine
import com.example.game.PieceGenerator
import com.example.game.PieceShape
import com.example.game.PlacementValidator
import com.example.game.PlayerRankCalculator
import com.example.game.PlayerWallet
import com.example.ui.GameViewModel
import com.example.ui.theme.BlockTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class NewFeaturesUnitTest {

    @Test
    fun `daily_challenge_generator_is_deterministic_and_playable`() {
        val specA = DailyChallengeGenerator.forDateKey("2026-06-15")
        val specB = DailyChallengeGenerator.forDateKey("2026-06-15")
        val specOther = DailyChallengeGenerator.forDateKey("2026-06-16")

        assertEquals(specA.seed, specB.seed)
        assertEquals(specA.level.starterPattern, specB.level.starterPattern)
        assertEquals(specA.level.targetScore, specB.level.targetScore)
        assertNotEquals(specA.level.starterPattern, specOther.level.starterPattern)

        val board = specA.level.buildBoard()
        assertTrue(board.occupiedCount() in 8..32)
        assertTrue(LineClearEngine.findFullLines(board).isEmpty)

        assertEquals("2026-02-28", DailyChallengeGenerator.previousDateKey("2026-03-01"))
        assertEquals("2025-12-31", DailyChallengeGenerator.previousDateKey("2026-01-01"))
    }

    @Test
    fun `piece_generator_excludes_3_cube_lines_and_oversized_5_cell_shapes`() {
        val generator = PieceGenerator(Random(2026))
        val boards = listOf(
            Board.empty(),
            Board.fromPattern(
                listOf(
                    "##..##..",
                    ".##..##.",
                    "..##..##",
                    "........",
                    "##....##",
                    ".##..##.",
                    "....##..",
                    "#..#..#."
                )
            )
        )
        var nextId = 1L
        boards.forEach { board ->
            repeat(150) {
                val (tray, updatedId) = generator.generateTray(board, nextId)
                nextId = updatedId
                assertEquals(3, tray.size)
                tray.forEach { piece ->
                    assertFalse(
                        "Excluded shape ${piece.shape} was dealt",
                        piece.shape in PieceGenerator.EXCLUDED_SHAPES
                    )
                }
            }
        }
        assertTrue(PieceShape.LINE_3_H in PieceGenerator.EXCLUDED_SHAPES)
        assertTrue(PieceShape.LINE_3_V in PieceGenerator.EXCLUDED_SHAPES)
        assertTrue(PieceShape.LINE_5_H in PieceGenerator.EXCLUDED_SHAPES)
        assertTrue(PieceShape.LINE_5_V in PieceGenerator.EXCLUDED_SHAPES)
        assertTrue(PieceShape.BIG_CORNER_BR in PieceGenerator.EXCLUDED_SHAPES)
        assertTrue(PieceShape.BIG_CORNER_BL in PieceGenerator.EXCLUDED_SHAPES)
        assertTrue(PieceShape.PLUS_5 in PieceGenerator.EXCLUDED_SHAPES)
        assertTrue(PieceShape.U_5 in PieceGenerator.EXCLUDED_SHAPES)
    }

    @Test
    fun `continue_offer_respects_10000_score_cap_and_once_per_run_and_rescues_board`() {
        assertTrue(GameViewModel.shouldOfferContinue(score = 0, alreadyUsedThisRun = false))
        assertTrue(GameViewModel.shouldOfferContinue(score = 10_000, alreadyUsedThisRun = false))
        assertFalse(GameViewModel.shouldOfferContinue(score = 10_001, alreadyUsedThisRun = false))
        assertFalse(GameViewModel.shouldOfferContinue(score = 500, alreadyUsedThisRun = true))

        val crowded = Board.fromPattern(List(8) { "########" })
        val rescued = GameViewModel.applyContinueBoardRescue(crowded)
        assertTrue(rescued.occupiedCount() < crowded.occupiedCount())
        val (tray, _) = PieceGenerator(Random(7)).generateTray(rescued, 10L)
        assertTrue(PlacementValidator.hasAnyValidMove(rescued, tray))
    }

    @Test
    fun `rename_profile_with_diamonds_deducts_100_diamonds_and_validates`() {
        val broke = PlayerWallet(diamonds = 99)
        assertTrue(
            EconomyEngine.renameProfileWithDiamonds(broke, "CascadeAce", "tx-broke") is EconomyMutationResult.Rejected
        )

        val rich = PlayerWallet(diamonds = 150)
        assertTrue(
            EconomyEngine.renameProfileWithDiamonds(rich, "   ", "tx-blank") is EconomyMutationResult.Rejected
        )

        val ok = EconomyEngine.renameProfileWithDiamonds(rich, "  BlockMaster  ", "tx-rename-1")
        assertTrue(ok is EconomyMutationResult.Success)
        val success = ok as EconomyMutationResult.Success
        assertEquals(50, success.updatedWallet.diamonds)
        assertEquals("BlockMaster", success.message)

        val replay = EconomyEngine.renameProfileWithDiamonds(success.updatedWallet, "OtherName", "tx-rename-1")
        assertTrue(replay is EconomyMutationResult.Rejected)
    }

    @Test
    fun `player_rank_calculator_scales_across_tiers`() {
        val recruit = PlayerRankCalculator.compute(stats = GameStatistics())
        assertEquals(1, recruit.tierNumber)
        assertEquals("Recruit", recruit.title)

        val grand = PlayerRankCalculator.compute(
            stats = GameStatistics(
                totalLinesCleared = 250,
                totalCascadesTriggered = 60,
                classicBestScore = 4000,
                dailyCompletedCount = 10
            )
        )
        assertEquals(6, grand.tierNumber)
        assertEquals("Grand Architect", grand.title)
        assertEquals(1f, grand.progressToNext, 0.001f)
    }

    @Test
    fun `block_themes_unlock_progressively_with_real_milestones`() {
        val freshStats = GameStatistics()
        assertTrue(BlockTheme.CLASSIC_COBALT.isUnlocked(freshStats, emptyMap()))
        assertFalse(BlockTheme.IVORY_BRASS.isUnlocked(freshStats, emptyMap()))
        assertFalse(BlockTheme.SEAFOAM_ARCADE.isUnlocked(freshStats, emptyMap()))
        assertFalse(BlockTheme.ROYAL_VELVET.isUnlocked(freshStats, emptyMap()))

        assertTrue(BlockTheme.IVORY_BRASS.isUnlocked(freshStats.copy(totalLinesCleared = 15), emptyMap()))
        assertTrue(BlockTheme.SEAFOAM_ARCADE.isUnlocked(freshStats.copy(classicBestScore = 250), emptyMap()))
        assertTrue(BlockTheme.ROYAL_VELVET.isUnlocked(freshStats.copy(maxCascadeDepth = 3), emptyMap()))
    }
}
