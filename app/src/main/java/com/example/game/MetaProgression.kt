package com.example.game

import kotlinx.serialization.Serializable

@Serializable
data class DailyRewardTier(
    val day: Int,
    val title: String,
    val gold: Int = 0,
    val diamonds: Int = 0,
    val shuffleBoosters: Int = 0,
    val isGrandPrize: Boolean = false
)

@Serializable
data class DailyRewardProgress(
    val claimedDaysCount: Int = 0,
    val lastClaimedDateKey: String? = null
) {
    fun canClaimToday(todayKey: String): Boolean = lastClaimedDateKey != todayKey

    fun currentCycleDay(): Int = (claimedDaysCount % 7) + 1

    fun daysClaimedInCycle(todayKey: String): Int {
        if (claimedDaysCount <= 0) return 0
        val mod = claimedDaysCount % 7
        return if (mod == 0 && !canClaimToday(todayKey)) 7 else mod
    }

    fun nextRewardDay(todayKey: String): Int {
        val claimed = daysClaimedInCycle(todayKey)
        return if (claimed >= 7) 1 else claimed + 1
    }
}

object DailyRewardsCatalog {
    val tiers: List<DailyRewardTier> = listOf(
        DailyRewardTier(day = 1, title = "100 Gold", gold = 100),
        DailyRewardTier(day = 2, title = "10 Diamonds", diamonds = 10),
        DailyRewardTier(day = 3, title = "250 Gold", gold = 250),
        DailyRewardTier(day = 4, title = "Shuffle ×1", shuffleBoosters = 1),
        DailyRewardTier(day = 5, title = "25 Diamonds", diamonds = 25),
        DailyRewardTier(day = 6, title = "500 Gold", gold = 500, shuffleBoosters = 1),
        DailyRewardTier(
            day = 7,
            title = "Grand Crystal Chest",
            gold = 1000,
            diamonds = 50,
            shuffleBoosters = 2,
            isGrandPrize = true
        )
    )
}

enum class AchievementCategory(val title: String) {
    ALL("All"),
    SCORE("Score"),
    CASCADES("Cascades"),
    CAMPAIGN("Campaign")
}

data class AchievementDefinition(
    val id: String,
    val title: String,
    val description: String,
    val category: AchievementCategory,
    val target: Int,
    val rewardGold: Int,
    val rewardDiamonds: Int,
    val tier: Int, // 1 = Bronze, 2 = Silver, 3 = Gold
    val progressProvider: (GameStatistics, Map<Int, ChallengeLevelProgress>) -> Int
)

object AchievementsCatalog {
    val items: List<AchievementDefinition> = listOf(
        AchievementDefinition(
            id = "ach_first_spark",
            title = "Crystal Novice",
            description = "Reach 250 points in Classic mode",
            category = AchievementCategory.SCORE,
            target = 250,
            rewardGold = 100,
            rewardDiamonds = 5,
            tier = 1
        ) { stats, _ -> stats.classicBestScore },
        AchievementDefinition(
            id = "ach_score_knight",
            title = "Sapphire Tactician",
            description = "Reach 1,000 points in Classic mode",
            category = AchievementCategory.SCORE,
            target = 1000,
            rewardGold = 250,
            rewardDiamonds = 15,
            tier = 2
        ) { stats, _ -> stats.classicBestScore },
        AchievementDefinition(
            id = "ach_score_monarch",
            title = "Diamond Sovereign",
            description = "Reach 3,000 points in Classic mode",
            category = AchievementCategory.SCORE,
            target = 3000,
            rewardGold = 600,
            rewardDiamonds = 35,
            tier = 3
        ) { stats, _ -> stats.classicBestScore },
        AchievementDefinition(
            id = "ach_line_sweeper",
            title = "Line Sweeper",
            description = "Clear 25 total rows or columns",
            category = AchievementCategory.CASCADES,
            target = 25,
            rewardGold = 120,
            rewardDiamonds = 5,
            tier = 1
        ) { stats, _ -> stats.totalLinesCleared },
        AchievementDefinition(
            id = "ach_gravity_artisan",
            title = "Cascade Artisan",
            description = "Trigger 15 gravity chain cascades",
            category = AchievementCategory.CASCADES,
            target = 15,
            rewardGold = 250,
            rewardDiamonds = 15,
            tier = 2
        ) { stats, _ -> stats.totalCascadesTriggered },
        AchievementDefinition(
            id = "ach_chain_legend",
            title = "Chain Reactor",
            description = "Reach a 3× or deeper cascade wave",
            category = AchievementCategory.CASCADES,
            target = 3,
            rewardGold = 400,
            rewardDiamonds = 25,
            tier = 3
        ) { stats, _ -> stats.maxCascadeDepth },
        AchievementDefinition(
            id = "ach_campaign_trail",
            title = "Trail Explorer",
            description = "Complete 3 Challenge campaign levels",
            category = AchievementCategory.CAMPAIGN,
            target = 3,
            rewardGold = 200,
            rewardDiamonds = 10,
            tier = 1
        ) { _, ch -> ch.values.count { it.completed } },
        AchievementDefinition(
            id = "ach_campaign_master",
            title = "Crown Conqueror",
            description = "Complete all 8 Challenge levels",
            category = AchievementCategory.CAMPAIGN,
            target = 8,
            rewardGold = 800,
            rewardDiamonds = 50,
            tier = 3
        ) { _, ch -> ch.values.count { it.completed } }
    )
}

@Serializable
data class DailyMissionSnapshot(
    val dateKey: String = "",
    val baseGamesPlayed: Int = 0,
    val baseLinesCleared: Int = 0,
    val baseCascadesTriggered: Int = 0,
    val baseDailyCompleted: Int = 0,
    val claimedMissionIds: Set<String> = emptySet()
) {
    fun anchoredTo(todayKey: String, stats: GameStatistics): DailyMissionSnapshot =
        if (dateKey == todayKey) {
            this
        } else {
            DailyMissionSnapshot(
                dateKey = todayKey,
                baseGamesPlayed = stats.totalGamesPlayed,
                baseLinesCleared = stats.totalLinesCleared,
                baseCascadesTriggered = stats.totalCascadesTriggered,
                baseDailyCompleted = stats.dailyCompletedCount,
                claimedMissionIds = emptySet()
            )
        }
}

data class DailyMissionDefinition(
    val id: String,
    val title: String,
    val description: String,
    val target: Int,
    val rewardGold: Int,
    val rewardDiamonds: Int,
    val progressProvider: (GameStatistics, DailyMissionSnapshot) -> Int
)

object DailyMissionsCatalog {
    val missions: List<DailyMissionDefinition> = listOf(
        DailyMissionDefinition(
            id = "mission_play_games",
            title = "Warm-Up Sessions",
            description = "Start 2 puzzle runs today",
            target = 2,
            rewardGold = 75,
            rewardDiamonds = 0
        ) { stats, snap -> (stats.totalGamesPlayed - snap.baseGamesPlayed).coerceAtLeast(0) },
        DailyMissionDefinition(
            id = "mission_clear_lines",
            title = "Prism Line Clear",
            description = "Clear 8 rows or columns today",
            target = 8,
            rewardGold = 120,
            rewardDiamonds = 5
        ) { stats, snap -> (stats.totalLinesCleared - snap.baseLinesCleared).coerceAtLeast(0) },
        DailyMissionDefinition(
            id = "mission_cascades",
            title = "Gravity Chain",
            description = "Trigger 2 gravity cascades today",
            target = 2,
            rewardGold = 150,
            rewardDiamonds = 10
        ) { stats, snap -> (stats.totalCascadesTriggered - snap.baseCascadesTriggered).coerceAtLeast(0) },
        DailyMissionDefinition(
            id = "mission_daily_puzzle",
            title = "Daily Gravity Victor",
            description = "Win today's Daily Gravity puzzle",
            target = 1,
            rewardGold = 200,
            rewardDiamonds = 15
        ) { stats, snap -> (stats.dailyCompletedCount - snap.baseDailyCompleted).coerceAtLeast(0) }
    )
}
