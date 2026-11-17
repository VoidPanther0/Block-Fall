package com.example.game

data class CascadeWave(
    val waveIndex: Int,
    val boardBeforeClear: Board,
    val clearedRows: List<Int>,
    val clearedCols: List<Int>,
    val clearedCoords: Set<GridCoord>,
    val boardAfterClear: Board,
    val movements: List<FallingBlockMove>,
    val boardAfterGravity: Board,
    val scoreBreakdown: WaveScoreBreakdown,
    val isFullClear: Boolean
)

data class CascadeResolution(
    val initialBoard: Board,
    val finalBoard: Board,
    val waves: List<CascadeWave>,
    val totalWavePoints: Int,
    val totalLinesCleared: Int,
    val maxCascadeDepth: Int,
    val didFullClear: Boolean
) {
    val hasClears: Boolean get() = waves.isNotEmpty()
}

/**
 * Resolves the full chain reaction after a piece placement:
 * 1. Detect completed rows & columns
 * 2. Clear them simultaneously
 * 3. Apply downward vertical gravity
 * 4. Repeat until no completed lines remain
 *
 * Completely deterministic and independent of UI/animations.
 */
object CascadeResolver {

    fun resolve(
        boardAfterPlacement: Board,
        scoringConfig: ScoringConfig = ScoringConfig()
    ): CascadeResolution {
        val waves = mutableListOf<CascadeWave>()
        var currentBoard = boardAfterPlacement
        var waveIndex = 1

        while (true) {
            val fullLines = LineClearEngine.findFullLines(currentBoard)
            if (fullLines.isEmpty) break

            val clearResult = LineClearEngine.clearLines(currentBoard, fullLines)
            val gravityResult = GravityEngine.applyGravity(clearResult.boardAfterClear)
            val isFullClear = gravityResult.newBoard.isEmpty()

            val scoreBreakdown = ScoringEngine.scoreWave(
                linesCount = clearResult.linesClearedCount,
                waveIndex = waveIndex,
                isFullClear = isFullClear,
                config = scoringConfig
            )

            waves.add(
                CascadeWave(
                    waveIndex = waveIndex,
                    boardBeforeClear = currentBoard,
                    clearedRows = clearResult.fullRows,
                    clearedCols = clearResult.fullCols,
                    clearedCoords = clearResult.clearedCoords,
                    boardAfterClear = clearResult.boardAfterClear,
                    movements = gravityResult.movements,
                    boardAfterGravity = gravityResult.newBoard,
                    scoreBreakdown = scoreBreakdown,
                    isFullClear = isFullClear
                )
            )

            currentBoard = gravityResult.newBoard
            waveIndex++
        }

        return CascadeResolution(
            initialBoard = boardAfterPlacement,
            finalBoard = currentBoard,
            waves = waves,
            totalWavePoints = waves.sumOf { it.scoreBreakdown.totalPoints },
            totalLinesCleared = waves.sumOf { it.clearedRows.size + it.clearedCols.size },
            maxCascadeDepth = waves.size,
            didFullClear = waves.any { it.isFullClear }
        )
    }
}
