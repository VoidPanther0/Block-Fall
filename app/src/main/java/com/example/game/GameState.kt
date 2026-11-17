package com.example.game

enum class GameMode {
    CLASSIC,
    CHALLENGE
}

enum class GameStatus {
    PLAYING,
    RESOLVING_CASCADE,
    PAUSED,
    GAME_OVER,
    CHALLENGE_WON,
    CHALLENGE_LOST
}

data class TraySlot(
    val index: Int,
    val piece: GamePiece?
)

data class SavedClassicGame(
    val board: Board,
    val trayPieces: List<GamePiece?>,
    val score: Int,
    val movesPlayed: Int,
    val bestCascadeDepth: Int,
    val totalLinesCleared: Int,
    val nextIdCounter: Long
)

data class GameSettings(
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val visualEffectsEnabled: Boolean = true,
    val placementPreviewEnabled: Boolean = true
)

data class GameStatistics(
    val totalGamesPlayed: Int = 0,
    val totalLinesCleared: Int = 0,
    val totalCascadesTriggered: Int = 0,
    val maxCascadeDepth: Int = 0,
    val totalBlocksPlaced: Int = 0,
    val classicBestScore: Int = 0
)

data class ChallengeLevelProgress(
    val levelNumber: Int,
    val bestScore: Int = 0,
    val stars: Int = 0,
    val completed: Boolean = false
)

