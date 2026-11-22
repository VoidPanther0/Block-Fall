package com.example.data.cloud

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@Keep
@IgnoreExtraProperties
data class UserCloudProfile(
    @DocumentId val documentId: String = "",
    val userId: String = "",
    val displayName: String = "Player",
    val classicBestScore: Long = 0L,
    val unlockedChallengeLevel: Long = 1L,
    val totalGamesPlayed: Long = 0L,
    val totalLinesCleared: Long = 0L,
    val totalCascadesTriggered: Long = 0L,
    val maxCascadeDepth: Long = 0L,
    val totalBlocksPlaced: Long = 0L,
    val fullBoardClears: Long = 0L,
    val blockStyleId: String = "modern",
    val boardPaletteId: String = "deep_ocean",
    val soundEnabled: Boolean = true,
    val musicEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val visualEffectsEnabled: Boolean = true,
    val placementPreviewEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "userId" to userId,
        "displayName" to sanitizeDisplayName(displayName),
        "classicBestScore" to classicBestScore.coerceIn(0L, MAX_SCORE),
        "unlockedChallengeLevel" to unlockedChallengeLevel.coerceIn(1L, MAX_LEVEL),
        "totalGamesPlayed" to totalGamesPlayed.coerceIn(0L, MAX_STAT),
        "totalLinesCleared" to totalLinesCleared.coerceIn(0L, MAX_STAT),
        "totalCascadesTriggered" to totalCascadesTriggered.coerceIn(0L, MAX_STAT),
        "maxCascadeDepth" to maxCascadeDepth.coerceIn(0L, MAX_DEPTH),
        "totalBlocksPlaced" to totalBlocksPlaced.coerceIn(0L, MAX_BLOCKS),
        "fullBoardClears" to fullBoardClears.coerceIn(0L, MAX_STAT),
        "blockStyleId" to sanitizeBlockStyle(blockStyleId),
        "boardPaletteId" to sanitizeBoardPalette(boardPaletteId),
        "soundEnabled" to soundEnabled,
        "musicEnabled" to musicEnabled,
        "hapticsEnabled" to hapticsEnabled,
        "visualEffectsEnabled" to visualEffectsEnabled,
        "placementPreviewEnabled" to placementPreviewEnabled,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toUpdateMap(): Map<String, Any> = mapOf(
        "displayName" to sanitizeDisplayName(displayName),
        "classicBestScore" to classicBestScore.coerceIn(0L, MAX_SCORE),
        "unlockedChallengeLevel" to unlockedChallengeLevel.coerceIn(1L, MAX_LEVEL),
        "totalGamesPlayed" to totalGamesPlayed.coerceIn(0L, MAX_STAT),
        "totalLinesCleared" to totalLinesCleared.coerceIn(0L, MAX_STAT),
        "totalCascadesTriggered" to totalCascadesTriggered.coerceIn(0L, MAX_STAT),
        "maxCascadeDepth" to maxCascadeDepth.coerceIn(0L, MAX_DEPTH),
        "totalBlocksPlaced" to totalBlocksPlaced.coerceIn(0L, MAX_BLOCKS),
        "fullBoardClears" to fullBoardClears.coerceIn(0L, MAX_STAT),
        "blockStyleId" to sanitizeBlockStyle(blockStyleId),
        "boardPaletteId" to sanitizeBoardPalette(boardPaletteId),
        "soundEnabled" to soundEnabled,
        "musicEnabled" to musicEnabled,
        "hapticsEnabled" to hapticsEnabled,
        "visualEffectsEnabled" to visualEffectsEnabled,
        "placementPreviewEnabled" to placementPreviewEnabled,
        "updatedAt" to FieldValue.serverTimestamp()
    )

    companion object {
        const val MAX_SCORE = 10_000_000L
        const val MAX_LEVEL = 50L
        const val MAX_STAT = 10_000_000L
        const val MAX_DEPTH = 100L
        const val MAX_BLOCKS = 100_000_000L

        private val VALID_BLOCK_STYLES = setOf("modern", "retro", "neon")
        private val VALID_BOARD_PALETTES = setOf("deep_ocean", "midnight", "emerald_grove", "sunset")

        fun sanitizeDisplayName(raw: String?): String =
            raw?.trim()?.take(40)?.takeIf { it.isNotEmpty() } ?: "Player"

        fun sanitizeBlockStyle(raw: String): String =
            if (raw in VALID_BLOCK_STYLES) raw else "modern"

        fun sanitizeBoardPalette(raw: String): String =
            if (raw in VALID_BOARD_PALETTES) raw else "deep_ocean"
    }
}

@Keep
@IgnoreExtraProperties
data class ChallengeProgressDoc(
    @DocumentId val documentId: String = "",
    val levelNumber: Long = 1L,
    val bestScore: Long = 0L,
    val stars: Long = 0L,
    val completed: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun toCreateMap(): Map<String, Any> = mapOf(
        "levelNumber" to levelNumber.coerceIn(1L, 50L),
        "bestScore" to bestScore.coerceIn(0L, UserCloudProfile.MAX_SCORE),
        "stars" to stars.coerceIn(0L, 3L),
        "completed" to completed,
        "createdAt" to FieldValue.serverTimestamp(),
        "updatedAt" to FieldValue.serverTimestamp()
    )

    fun toUpdateMap(): Map<String, Any> = mapOf(
        "bestScore" to bestScore.coerceIn(0L, UserCloudProfile.MAX_SCORE),
        "stars" to stars.coerceIn(0L, 3L),
        "completed" to completed,
        "updatedAt" to FieldValue.serverTimestamp()
    )
}

@Keep
@IgnoreExtraProperties
data class IntegrityAttestationDoc(
    @DocumentId val documentId: String = "",
    val userId: String = "",
    val nonce: String = "",
    val tokenLength: Long = 1L,
    val tokenDigest: String = "",
    val appVersionCode: Long = 1L,
    val verificationStatus: String = "LOCAL_UNAVAILABLE",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class LeaderboardEntry(
    @DocumentId val documentId: String = "",
    val userId: String = "",
    val displayName: String = "Player",
    val visibility: String = "public",
    val classicBestScore: Long = 0L,
    val maxCascadeDepth: Long = 0L,
    val rankTitle: String = "Block Initiate",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
