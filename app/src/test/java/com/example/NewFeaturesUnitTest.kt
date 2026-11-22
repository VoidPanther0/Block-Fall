package com.example

import com.example.game.AchievementEvaluator
import com.example.game.AchievementId
import com.example.game.Board
import com.example.game.ChallengeLevelProgress
import com.example.game.DailyChallengeGenerator
import com.example.game.GameStatistics
import com.example.game.LineClearEngine
import com.example.game.PlayerRankCalculator
import com.example.ui.theme.BlockTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
        // Lower rows must never start already full
        assertTrue(LineClearEngine.findFullLines(board).isEmpty)

        // Calendar date rollback across month and year boundaries
        assertEquals("2026-02-28", DailyChallengeGenerator.previousDateKey("2026-03-01"))
        assertEquals("2025-12-31", DailyChallengeGenerator.previousDateKey("2026-01-01"))
    }

    @Test
    fun `achievement_evaluator_unlocks_milestones_from_real_stats`() {
        val emptyUnlocked = AchievementEvaluator.evaluateUnlocked(
            currentlyUnlocked = emptySet(),
            statistics = GameStatistics(),
            challengeProgress = emptyMap()
        )
        assertTrue(emptyUnlocked.isEmpty())

        val stats = GameStatistics(
            totalGamesPlayed = 12,
            totalLinesCleared = 54,
            totalCascadesTriggered = 8,
            maxCascadeDepth = 3,
            totalBlocksPlaced = 420,
            classicBestScore = 1080,
            fullBoardClears = 1,
            dailyCompletedCount = 1,
            dailyStreak = 1
        )
        val challenges = (1..8).associateWith { lvl ->
            ChallengeLevelProgress(levelNumber = lvl, bestScore = 500, stars = 3, completed = true)
        }

        val allUnlocked = AchievementEvaluator.evaluateUnlocked(emptySet(), stats, challenges)
        assertEquals(AchievementId.entries.size, allUnlocked.size)
        AchievementId.entries.forEach { ach ->
            assertTrue("Expected achievement ${ach.id} to be unlocked", ach.id in allUnlocked)
        }
    }

    @Test
    fun `player_rank_calculator_scales_across_tiers`() {
        val recruit = PlayerRankCalculator.compute(
            stats = GameStatistics(),
            challengeProgress = emptyMap(),
            unlockedAchievements = emptySet()
        )
        assertEquals(1, recruit.tierNumber)
        assertEquals("Recruit", recruit.title)

        val grand = PlayerRankCalculator.compute(
            stats = GameStatistics(
                totalLinesCleared = 250,
                totalCascadesTriggered = 60,
                classicBestScore = 4000,
                dailyCompletedCount = 10
            ),
            challengeProgress = (1..8).associateWith {
                ChallengeLevelProgress(it, bestScore = 900, stars = 3, completed = true)
            },
            unlockedAchievements = AchievementId.entries.map { it.id }.toSet()
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
