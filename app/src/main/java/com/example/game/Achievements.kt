package com.example.game

enum class AchievementId(
    val id: String,
    val title: String,
    val description: String,
    val iconKey: String
) {
    FIRST_CLEAR(
        id = "first_clear",
        title = "First Break",
        description = "Clear your very first row or column.",
        iconKey = "star"
    ),
    CASCADE_APPRENTICE(
        id = "cascade_apprentice",
        title = "Gravity Trigger",
        description = "Trigger a 2x gravity cascade chain.",
        iconKey = "layers"
    ),
    CHAIN_MASTER(
        id = "chain_master",
        title = "Chain Architect",
        description = "Reach a 3x or deeper cascade wave.",
        iconKey = "bolt"
    ),
    BOARD_SWEEPER(
        id = "board_sweeper",
        title = "Clean Sweep",
        description = "Clear every single block off the 8x8 board.",
        iconKey = "auto_awesome"
    ),
    CENTURY_CLUB(
        id = "century_club",
        title = "Century Club",
        description = "Score 100+ points in a Classic run.",
        iconKey = "trending"
    ),
    GRANDMASTER_1000(
        id = "grandmaster_1000",
        title = "Cobalt Titan",
        description = "Reach a 1,000+ Classic high score.",
        iconKey = "trophy"
    ),
    LINE_CRUSHER(
        id = "line_crusher",
        title = "Line Crusher",
        description = "Clear 50 total lines across all games.",
        iconKey = "flag"
    ),
    CHALLENGE_VICTOR(
        id = "challenge_victor",
        title = "Puzzle Solver",
        description = "Complete your first Challenge level.",
        iconKey = "check"
    ),
    ALL_CHALLENGES(
        id = "all_challenges",
        title = "Apex Conqueror",
        description = "Complete all 8 Challenge levels.",
        iconKey = "crown"
    ),
    DAILY_CHALLENGER(
        id = "daily_challenger",
        title = "Daily Regular",
        description = "Complete a Daily Challenge puzzle.",
        iconKey = "calendar"
    );

    companion object {
        fun fromId(id: String): AchievementId? = entries.firstOrNull { it.id == id }
    }
}

object AchievementEvaluator {
    fun evaluateUnlocked(
        currentlyUnlocked: Set<String>,
        statistics: GameStatistics,
        challengeProgress: Map<Int, ChallengeLevelProgress>
    ): Set<String> {
        val result = currentlyUnlocked.toMutableSet()
        if (statistics.totalLinesCleared >= 1) result.add(AchievementId.FIRST_CLEAR.id)
        if (statistics.maxCascadeDepth >= 2) result.add(AchievementId.CASCADE_APPRENTICE.id)
        if (statistics.maxCascadeDepth >= 3) result.add(AchievementId.CHAIN_MASTER.id)
        if (statistics.fullBoardClears >= 1) result.add(AchievementId.BOARD_SWEEPER.id)
        if (statistics.classicBestScore >= 100) result.add(AchievementId.CENTURY_CLUB.id)
        if (statistics.classicBestScore >= 1000) result.add(AchievementId.GRANDMASTER_1000.id)
        if (statistics.totalLinesCleared >= 50) result.add(AchievementId.LINE_CRUSHER.id)

        val completedChallenges = challengeProgress.values.count { it.completed }
        if (completedChallenges >= 1) result.add(AchievementId.CHALLENGE_VICTOR.id)
        if (completedChallenges >= ChallengeLevels.levels.size) result.add(AchievementId.ALL_CHALLENGES.id)
        if (statistics.dailyCompletedCount >= 1) result.add(AchievementId.DAILY_CHALLENGER.id)

        return result
    }
}
