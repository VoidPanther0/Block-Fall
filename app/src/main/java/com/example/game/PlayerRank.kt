package com.example.game

data class PlayerRankInfo(
    val title: String,
    val tierNumber: Int,
    val xp: Int,
    val nextTierXp: Int,
    val progressToNext: Float
)

/**
 * Computes player progression rank deterministically from genuine local stats.
 */
object PlayerRankCalculator {

    private data class Tier(val title: String, val minXp: Int)

    private val tiers = listOf(
        Tier("Recruit", 0),
        Tier("Apprentice", 150),
        Tier("Tactician", 450),
        Tier("Cascade Pro", 1000),
        Tier("Gravity Ace", 2200),
        Tier("Grand Architect", 4500)
    )

    fun compute(stats: GameStatistics): PlayerRankInfo {
        val xp = (stats.totalLinesCleared * 10) +
            (stats.totalCascadesTriggered * 35) +
            (stats.dailyCompletedCount * 80) +
            (stats.classicBestScore / 8)

        var currentIdx = 0
        for (i in tiers.indices) {
            if (xp >= tiers[i].minXp) {
                currentIdx = i
            }
        }
        val current = tiers[currentIdx]
        val next = tiers.getOrNull(currentIdx + 1)
        val progress = if (next == null) {
            1f
        } else {
            val span = (next.minXp - current.minXp).coerceAtLeast(1)
            ((xp - current.minXp).toFloat() / span.toFloat()).coerceIn(0f, 1f)
        }

        return PlayerRankInfo(
            title = current.title,
            tierNumber = currentIdx + 1,
            xp = xp,
            nextTierXp = next?.minXp ?: current.minXp,
            progressToNext = progress
        )
    }
}
