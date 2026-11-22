package com.example.game

/**
 * Pure Kotlin deterministic gameplay turn simulator decoupled from Android UI, Sound, and storage.
 * Executes a single validated placement + full gravity cascade chain and determines next turn status.
 * Shared foundation for local solo runs and future server-authoritative / multiplayer match state sync.
 */
data class TurnExecutionResult(
    val placement: PlacementResult,
    val resolution: CascadeResolution,
    val updatedTray: List<TraySlot>,
    val placementPoints: Int,
    val scoreAfterPlacement: Int,
    val finalScore: Int,
    val totalLinesInTurn: Int,
    val fullClearOccurred: Boolean,
    val nextMovesPlayed: Int,
    val nextTraySlots: List<TraySlot>,
    val nextBlockIdCounter: Long,
    val nextStatus: GameStatus,
    val earnedChallengeStars: Int,
    val feedback: ScoreFeedback = ScoreFeedback.NONE,
    val comboStreak: Int = 0
)

class GameSessionEngine(
    private val pieceGenerator: PieceGenerator
) {

    fun executeTurn(
        board: Board,
        traySlots: List<TraySlot>,
        slotIndex: Int,
        anchorRow: Int,
        anchorCol: Int,
        currentScore: Int,
        movesPlayed: Int,
        mode: GameMode,
        challengeLevel: ChallengeLevel,
        startingBlockId: Long,
        comboStreak: Int = 0
    ): TurnExecutionResult? {
        val piece = traySlots.getOrNull(slotIndex)?.piece ?: return null
        val placement = PlacementValidator.placePiece(
            board = board,
            piece = piece,
            anchorRow = anchorRow,
            anchorCol = anchorCol,
            startingBlockId = startingBlockId
        ) ?: return null

        var nextIdCounter = placement.nextBlockId
        val placementPoints = ScoringEngine.scorePlacement(piece.shape.cellCount)
        val updatedTray = traySlots.map { slot ->
            if (slot.index == slotIndex) slot.copy(piece = null) else slot
        }

        val resolution = CascadeResolver.resolve(placement.board)
        val scoreAfterPlacement = currentScore + placementPoints
        val totalLinesInTurn = resolution.waves.sumOf { it.clearedRows.size + it.clearedCols.size }
        val cascadePoints = resolution.waves.sumOf { it.scoreBreakdown.totalPoints }
        val finalScore = scoreAfterPlacement + cascadePoints
        val fullClearOccurred = resolution.waves.any { it.isFullClear }
        val nextMovesPlayed = movesPlayed + 1
        val newStreak = if (resolution.hasClears) comboStreak + 1 else 0

        val remainingPieces = updatedTray.mapNotNull { it.piece }
        val nextTraySlots = if (remainingPieces.isEmpty()) {
            val (refilled, updatedId) = pieceGenerator.generateTray(resolution.finalBoard, nextIdCounter)
            nextIdCounter = updatedId
            refilled.mapIndexed { idx, p -> TraySlot(idx, p) }
        } else {
            updatedTray
        }

        val activePieces = nextTraySlots.mapNotNull { it.piece }
        val hasMove = PlacementValidator.hasAnyValidMove(resolution.finalBoard, activePieces)
        val nextStatus = when (mode) {
            GameMode.CLASSIC -> {
                if (hasMove) GameStatus.PLAYING else GameStatus.GAME_OVER
            }
            GameMode.CHALLENGE -> {
                val targetReached = finalScore >= challengeLevel.targetScore
                val outOfMoves = nextMovesPlayed >= challengeLevel.moveLimit
                when {
                    targetReached -> GameStatus.CHALLENGE_WON
                    outOfMoves || !hasMove -> GameStatus.CHALLENGE_LOST
                    else -> GameStatus.PLAYING
                }
            }
        }

        val stars = if (mode == GameMode.CHALLENGE && nextStatus == GameStatus.CHALLENGE_WON) {
            calculateChallengeStars(
                score = finalScore,
                targetScore = challengeLevel.targetScore,
                movesRemaining = (challengeLevel.moveLimit - nextMovesPlayed).coerceAtLeast(0)
            )
        } else {
            0
        }

        return TurnExecutionResult(
            placement = placement,
            resolution = resolution,
            updatedTray = updatedTray,
            placementPoints = placementPoints,
            scoreAfterPlacement = scoreAfterPlacement,
            finalScore = finalScore,
            totalLinesInTurn = totalLinesInTurn,
            fullClearOccurred = fullClearOccurred,
            nextMovesPlayed = nextMovesPlayed,
            nextTraySlots = nextTraySlots,
            nextBlockIdCounter = nextIdCounter,
            nextStatus = nextStatus,
            earnedChallengeStars = stars,
            feedback = ScoreFeedback.from(resolution, newStreak),
            comboStreak = newStreak
        )
    }

    companion object {
        fun isClassicGameOver(board: Board, tray: List<GamePiece?>): Boolean {
            val available = tray.filterNotNull()
            if (available.isEmpty()) return false
            return !PlacementValidator.hasAnyValidMove(board, available)
        }

        fun calculateChallengeStars(score: Int, targetScore: Int, movesRemaining: Int): Int {
            if (score < targetScore) return 0
            return when {
                score >= (targetScore * 1.5f).toInt() || movesRemaining >= 5 -> 3
                score >= (targetScore * 1.25f).toInt() || movesRemaining >= 3 -> 2
                else -> 1
            }
        }
    }
}
