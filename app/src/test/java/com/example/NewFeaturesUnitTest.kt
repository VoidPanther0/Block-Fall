package com.example

import com.example.game.AchievementsCatalog
import com.example.game.Board
import com.example.game.DailyChallengeGenerator
import com.example.game.DailyMissionSnapshot
import com.example.game.DailyMissionsCatalog
import com.example.game.DailyRewardProgress
import com.example.game.DailyRewardsCatalog
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

    @Test
    fun `daily_rewards_catalog_progresses_through_7_day_cycle`() {
        assertEquals(7, DailyRewardsCatalog.tiers.size)
        val day7 = DailyRewardsCatalog.tiers.last()
        assertTrue(day7.isGrandPrize)
        assertEquals(1000, day7.gold)
        assertEquals(50, day7.diamonds)

        var prog = DailyRewardProgress()
        assertTrue(prog.canClaimToday("2026-11-24"))
        assertEquals(1, prog.currentCycleDay())
        assertEquals(0, prog.daysClaimedInCycle("2026-11-24"))
        assertEquals(1, prog.nextRewardDay("2026-11-24"))

        // Claiming Day 1 marks ONLY Day 1 as claimed and sets tomorrow to Day 2 (never Day 3)
        prog = prog.copy(claimedDaysCount = 1, lastClaimedDateKey = "2026-11-24")
        assertFalse(prog.canClaimToday("2026-11-24"))
        assertEquals(1, prog.daysClaimedInCycle("2026-11-24"))
        assertEquals(2, prog.nextRewardDay("2026-11-24"))
        assertTrue(prog.canClaimToday("2026-11-25"))
        assertEquals(2, prog.currentCycleDay())
        assertEquals(1, prog.daysClaimedInCycle("2026-11-25"))
        assertEquals(2, prog.nextRewardDay("2026-11-25"))

        // Claiming Day 7 shows all 7 days claimed today and resets to Day 1 tomorrow
        prog = prog.copy(claimedDaysCount = 7, lastClaimedDateKey = "2026-11-30")
        assertEquals(1, prog.currentCycleDay())
        assertEquals(7, prog.daysClaimedInCycle("2026-11-30"))
        assertEquals(1, prog.nextRewardDay("2026-11-30"))
        assertEquals(0, prog.daysClaimedInCycle("2026-12-01"))
        assertEquals(1, prog.nextRewardDay("2026-12-01"))
    }

    @Test
    fun `music_tracks_and_board_themes_expose_variety_and_lookup`() {
        assertTrue(com.example.game.MusicTrack.entries.size >= 4)
        assertEquals(
            com.example.game.MusicTrack.SYNTHWAVE_NEON,
            com.example.game.MusicTrack.fromId("synthwave")
        )
        assertEquals(6, com.example.game.BoardColorPalette.entries.size)
        assertEquals(
            com.example.game.BoardColorPalette.AMETHYST,
            com.example.game.BoardColorPalette.fromId("amethyst")
        )
        assertEquals(
            com.example.game.BoardColorPalette.OBSIDIAN,
            com.example.game.BoardColorPalette.fromId("obsidian")
        )
    }

    @Test
    fun `daily_missions_catalog_evaluates_anchored_progress`() {
        assertEquals(4, DailyMissionsCatalog.missions.size)
        val initialStats = GameStatistics(totalGamesPlayed = 10, totalLinesCleared = 50)
        val snap = DailyMissionSnapshot().anchoredTo("2026-11-24", initialStats)

        val updatedStats = initialStats.copy(totalGamesPlayed = 12, totalLinesCleared = 58)
        val playMission = DailyMissionsCatalog.missions.first { it.id == "mission_play_games" }
        val clearMission = DailyMissionsCatalog.missions.first { it.id == "mission_clear_lines" }

        assertEquals(2, playMission.progressProvider(updatedStats, snap))
        assertEquals(8, clearMission.progressProvider(updatedStats, snap))
        assertTrue(playMission.progressProvider(updatedStats, snap) >= playMission.target)
        assertTrue(clearMission.progressProvider(updatedStats, snap) >= clearMission.target)
    }

    @Test
    fun `achievements_catalog_tracks_all_categories_properly`() {
        assertEquals(8, AchievementsCatalog.items.size)
        val stats = GameStatistics(classicBestScore = 1500, totalLinesCleared = 30)
        val firstSpark = AchievementsCatalog.items.first { it.id == "ach_first_spark" }
        val scoreKnight = AchievementsCatalog.items.first { it.id == "ach_score_knight" }
        val lineSweeper = AchievementsCatalog.items.first { it.id == "ach_line_sweeper" }

        assertTrue(firstSpark.progressProvider(stats, emptyMap()) >= firstSpark.target)
        assertTrue(scoreKnight.progressProvider(stats, emptyMap()) >= scoreKnight.target)
        assertTrue(lineSweeper.progressProvider(stats, emptyMap()) >= lineSweeper.target)
    }
}
