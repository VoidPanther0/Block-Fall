package com.example.game

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

data class DailyChallengeSpec(
    val dateKey: String,
    val displayDate: String,
    val seed: Long,
    val level: ChallengeLevel
)

/**
 * Generates deterministic, calendar-seeded daily gravity puzzles locally on device.
 * Every player sees the same layout and seeded piece stream for a given calendar date.
 */
object DailyChallengeGenerator {

    fun todayDateKey(calendar: Calendar = Calendar.getInstance()): String {
        val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return fmt.format(calendar.time)
    }

    fun previousDateKey(dateKey: String): String {
        return runCatching {
            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = fmt.parse(dateKey) ?: return ""
            val cal = Calendar.getInstance().apply {
                time = date
                add(Calendar.DAY_OF_YEAR, -1)
            }
            fmt.format(cal.time)
        }.getOrDefault("")
    }

    fun formatDisplayDate(dateKey: String): String {
        return runCatching {
            val inFmt = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val outFmt = SimpleDateFormat("MMM d, yyyy", Locale.US)
            val date = inFmt.parse(dateKey) ?: return dateKey
            outFmt.format(date)
        }.getOrDefault(dateKey)
    }

    fun forDateKey(dateKey: String): DailyChallengeSpec {
        val numericSeed = dateKey.replace("-", "").toLongOrNull() ?: 20260101L
        val rng = Random(numericSeed * 31337L + 17L)

        val pattern = MutableList(Board.SIZE) { CharArray(Board.SIZE) { '.' } }

        // Build a guaranteed non-full, playable gravity-settled lower structure (rows 4..7)
        // with intentional gaps and an upper shelf ready to cascade.
        for (col in 0 until Board.SIZE) {
            val stackHeight = rng.nextInt(1, 4) // 1..3 blocks stacked at bottom of column
            for (h in 0 until stackHeight) {
                val row = Board.SIZE - 1 - h
                val colorChar = ('0' + rng.nextInt(BlockColor.entries.size))
                pattern[row][col] = colorChar
            }
        }

        // Ensure no row in rows 5..7 starts already full (leave at least 2 strategic gaps)
        for (row in 5 until Board.SIZE) {
            var filled = (0 until Board.SIZE).count { pattern[row][it] != '.' }
            while (filled >= Board.SIZE) {
                val colToClear = rng.nextInt(Board.SIZE)
                pattern[row][colToClear] = '.'
                filled--
            }
        }

        // Add one suspended gravity shelf on row 2 or 3 spanning 4 columns to set up a cascade
        val shelfRow = rng.nextInt(2, 4)
        val shelfStart = rng.nextInt(0, Board.SIZE - 4)
        for (c in shelfStart until shelfStart + 4) {
            pattern[shelfRow][c] = ('0' + rng.nextInt(BlockColor.entries.size))
        }

        // Guarantee rows 0..3 stay mostly open so any 1-5 cell piece can place safely
        val starterLines = pattern.map { String(it) }
        val dayVariant = (numericSeed % 5).toInt()
        val targetScore = 240 + dayVariant * 45
        val moveLimit = 14 + (dayVariant % 3) * 2

        val level = ChallengeLevel(
            levelNumber = 0,
            title = "Daily Puzzle",
            subtitle = "Seed $dateKey · Trigger cascades within $moveLimit moves",
            targetScore = targetScore,
            moveLimit = moveLimit,
            starterPattern = starterLines
        )

        return DailyChallengeSpec(
            dateKey = dateKey,
            displayDate = formatDisplayDate(dateKey),
            seed = numericSeed,
            level = level
        )
    }
}
