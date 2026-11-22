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

enum class BlockStyle(val id: String, val displayName: String) {
    MODERN("modern", "Modern"),
    RETRO("retro", "Retro"),
    NEON("neon", "Neon");

    companion object {
        fun fromId(id: String?): BlockStyle =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: MODERN
    }
}

enum class BoardColorPalette(val id: String, val displayName: String) {
    ROYAL("royal", "Royal"),
    MIDNIGHT("midnight", "Midnight"),
    EMERALD("emerald", "Forest"),
    SUNSET("sunset", "Sunset");

    companion object {
        fun fromId(id: String?): BoardColorPalette =
            entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: ROYAL
    }
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
    val placementPreviewEnabled: Boolean = true,
    val blockStyle: BlockStyle = BlockStyle.MODERN,
    val boardPalette: BoardColorPalette = BoardColorPalette.ROYAL
)

data class GameStatistics(
    val totalGamesPlayed: Int = 0,
    val totalLinesCleared: Int = 0,
    val totalCascadesTriggered: Int = 0,
    val maxCascadeDepth: Int = 0,
    val totalBlocksPlaced: Int = 0,
    val classicBestScore: Int = 0,
    val fullBoardClears: Int = 0,
    val dailyCompletedCount: Int = 0,
    val dailyStreak: Int = 0
)

data class ChallengeLevelProgress(
    val levelNumber: Int,
    val bestScore: Int = 0,
    val stars: Int = 0,
    val completed: Boolean = false
)
