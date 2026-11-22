package com.example.game

enum class FeedbackTier { NONE, GREAT, PERFECT }

enum class ComboLabel(val minDepth: Int, val display: String) {
    NONE(0, ""),
    COMBO_2(2, "Combo 2"),
    COMBO_3(3, "Combo 3"),
    COMBO_4(4, "Combo 4"),
    COMBO_5_PLUS(5, "Combo 5+");

    companion object {
        fun forDepth(depth: Int): ComboLabel = entries.lastOrNull { depth >= it.minDepth && it != NONE } ?: NONE
    }
}

/**
 * Per-turn feedback derived purely from our own rules:
 * - PERFECT: the turn left the board completely empty.
 * - GREAT: three or more lines were cleared in a single turn.
 * - Combo: the larger of the cascade depth (gravity chains are possible but rare) and the
 *   clear streak (consecutive turns that cleared lines), so combos are reachable in normal play.
 * - multiplier: highest wave multiplier applied this turn (equals cascade depth).
 */
data class ScoreFeedback(
    val tier: FeedbackTier,
    val combo: ComboLabel,
    val multiplier: Int,
    val cascadeBonus: Int,
    val linesCleared: Int
) {
    val hasFeedback: Boolean get() = tier != FeedbackTier.NONE || combo != ComboLabel.NONE

    companion object {
        const val GREAT_MIN_LINES = 3
        val NONE = ScoreFeedback(FeedbackTier.NONE, ComboLabel.NONE, 1, 0, 0)

        fun from(resolution: CascadeResolution, comboStreak: Int = 0): ScoreFeedback {
            if (!resolution.hasClears) return NONE
            val tier = when {
                resolution.didFullClear -> FeedbackTier.PERFECT
                resolution.totalLinesCleared >= GREAT_MIN_LINES -> FeedbackTier.GREAT
                else -> FeedbackTier.NONE
            }
            return ScoreFeedback(
                tier = tier,
                combo = ComboLabel.forDepth(maxOf(resolution.maxCascadeDepth, comboStreak)),
                multiplier = resolution.waves.maxOf { it.scoreBreakdown.multiplier },
                cascadeBonus = resolution.waves.sumOf { it.scoreBreakdown.cascadeBonus },
                linesCleared = resolution.totalLinesCleared
            )
        }
    }
}
