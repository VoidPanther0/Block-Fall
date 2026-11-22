package com.example.game

data class ScoringConfig(
    val pointsPerPlacedCell: Int = 1,
    val pointsPerClearedLine: Int = 10,
    val multiLineStepBonus: Int = 15,
    val cascadeChainStepBonus: Int = 25,
    val fullBoardClearBonus: Int = 150
)

data class WaveScoreBreakdown(
    val baseLinePoints: Int,
    val multiLineBonus: Int,
    val cascadeBonus: Int,
    val fullClearBonus: Int,
    val multiplier: Int
) {
    val totalPoints: Int
        get() = (baseLinePoints + multiLineBonus) * multiplier + cascadeBonus + fullClearBonus
}

/**
 * Configurable scoring system rewarding multi-line clears, gravity chain reactions,
 * and full board clears.
 */
object ScoringEngine {

    fun scorePlacement(cellCount: Int, config: ScoringConfig = ScoringConfig()): Int =
        cellCount * config.pointsPerPlacedCell

    fun scoreWave(
        linesCount: Int,
        waveIndex: Int,
        isFullClear: Boolean,
        config: ScoringConfig = ScoringConfig()
    ): WaveScoreBreakdown {
        if (linesCount <= 0) {
            return WaveScoreBreakdown(0, 0, 0, 0, 1)
        }
        val baseLinePoints = linesCount * config.pointsPerClearedLine
        val multiLineBonus = if (linesCount > 1) (linesCount - 1) * config.multiLineStepBonus else 0
        val multiplier = waveIndex.coerceAtLeast(1)
        val cascadeBonus = if (waveIndex > 1) (waveIndex - 1) * config.cascadeChainStepBonus else 0
        val fullClearBonus = if (isFullClear) config.fullBoardClearBonus else 0

        return WaveScoreBreakdown(
            baseLinePoints = baseLinePoints,
            multiLineBonus = multiLineBonus,
            cascadeBonus = cascadeBonus,
            fullClearBonus = fullClearBonus,
            multiplier = multiplier
        )
    }
}
