package com.example.game

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/**
 * Persisted per-day result of the Daily Gravity puzzle. Only the current date's record is
 * meaningful: when the calendar date key changes the puzzle resets (see [forDate]).
 */
@Serializable
data class DailyProgress(
    val dateKey: String = "",
    val bestScore: Int = 0,
    val completed: Boolean = false,
    val attempts: Int = 0
) {
    /** Progress that applies to [todayKey]; a stale record from another day resets to empty. */
    fun forDate(todayKey: String): DailyProgress =
        if (dateKey == todayKey) this else DailyProgress(dateKey = todayKey)

    fun withAttemptStarted(todayKey: String): DailyProgress {
        val cur = forDate(todayKey)
        return cur.copy(attempts = cur.attempts + 1)
    }

    /** Scores never decrease and `completed` never reverts within a day. */
    fun withResult(todayKey: String, score: Int, won: Boolean): DailyProgress {
        val cur = forDate(todayKey)
        return cur.copy(
            bestScore = maxOf(cur.bestScore, score.coerceAtLeast(0)),
            completed = cur.completed || won
        )
    }

    fun toJson(): String = Json.encodeToString(serializer(), this)

    companion object {
        fun fromJson(json: String?): DailyProgress = runCatching {
            val p = Json.decodeFromString(serializer(), json ?: return DailyProgress())
            p.copy(bestScore = p.bestScore.coerceAtLeast(0), attempts = p.attempts.coerceAtLeast(0))
        }.getOrDefault(DailyProgress())
    }
}
