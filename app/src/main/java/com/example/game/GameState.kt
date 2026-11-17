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
