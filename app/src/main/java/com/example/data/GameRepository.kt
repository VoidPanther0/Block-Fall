package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.game.BlockCell
import com.example.game.BlockColor
import com.example.game.BlockStyle
import com.example.game.Board
import com.example.game.BoardColorPalette
import com.example.game.ChallengeLevelProgress
import com.example.game.GamePiece
import com.example.game.GameSettings
import com.example.game.GameStatistics
import com.example.game.PieceShape
import com.example.game.PlayerWallet
import com.example.game.ProfileAvatar
import com.example.game.SavedClassicGame
import com.example.security.DecryptionResult
import com.example.security.EncryptedEntitlementCache
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import org.json.JSONArray
import org.json.JSONObject

private val Context.cascadeDataStore: DataStore<Preferences> by preferencesDataStore(name = "cascade_prefs")

data class PersistedProgress(
    val classicBestScore: Int = 0,
    val unlockedChallengeLevel: Int = 1,
    val hapticsEnabled: Boolean = true,
    val savedClassicGame: SavedClassicGame? = null,
    val settings: GameSettings = GameSettings(hapticsEnabled = hapticsEnabled),
    val statistics: GameStatistics = GameStatistics(classicBestScore = classicBestScore),
    val challengeProgress: Map<Int, ChallengeLevelProgress> = emptyMap(),
    val wallet: PlayerWallet = PlayerWallet(),
    val playerDisplayName: String? = null,
    val profileAvatarId: String = ProfileAvatar.CLASSIC_BLOCK.id,
    val localIntegrityTampered: Boolean = false
)

private data class WalletState(
    val wallet: PlayerWallet,
    val tampered: Boolean = false
)

private data class ProtectedProgressState(
    val classicBestScore: Int,
    val unlockedChallengeLevel: Int,
    val statistics: GameStatistics,
    val challengeProgress: Map<Int, ChallengeLevelProgress>,
    val tampered: Boolean = false
)

class GameRepository(
    private val context: Context,
    private val cryptoCache: EncryptedEntitlementCache = EncryptedEntitlementCache(context)
) {

    private object Keys {
        // Legacy / existing keys preserved for migration compatibility
        val CLASSIC_BEST = intPreferencesKey("classic_best_score")
        val UNLOCKED_CHALLENGE_LEVEL = intPreferencesKey("unlocked_challenge_level")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SAVED_CLASSIC_JSON = stringPreferencesKey("saved_classic_json")

        // Extended settings, statistics, and challenge progress keys
        val SOUND_ENABLED = booleanPreferencesKey("sound_enabled")
        val MUSIC_ENABLED = booleanPreferencesKey("music_enabled")
        val VISUAL_EFFECTS_ENABLED = booleanPreferencesKey("visual_effects_enabled")
        val PLACEMENT_PREVIEW_ENABLED = booleanPreferencesKey("placement_preview_enabled")
        val BLOCK_STYLE = stringPreferencesKey("block_style")
        val BOARD_PALETTE = stringPreferencesKey("board_palette")

        val TOTAL_GAMES_PLAYED = intPreferencesKey("total_games_played")
        val TOTAL_LINES_CLEARED = intPreferencesKey("total_lines_cleared")
        val TOTAL_CASCADES = intPreferencesKey("total_cascades")
        val MAX_CASCADE_DEPTH = intPreferencesKey("max_cascade_depth")
        val TOTAL_BLOCKS_PLACED = intPreferencesKey("total_blocks_placed")
        val CHALLENGE_PROGRESS_JSON = stringPreferencesKey("challenge_progress_json")

        // AES-GCM authenticated envelope for protected progression & saved run
        val PROTECTED_PROGRESS_ENVELOPE = stringPreferencesKey("protected_progress_envelope_v1")
        val SAVED_CLASSIC_ENVELOPE = stringPreferencesKey("saved_classic_envelope_v1")
        val WALLET_ENVELOPE = stringPreferencesKey("wallet_envelope_v1")
        val DAILY_PROGRESS = stringPreferencesKey("daily_gravity_progress_v1")
        val PLAYER_DISPLAY_NAME = stringPreferencesKey("player_display_name_v1")
        val PROFILE_AVATAR_ID = stringPreferencesKey("profile_avatar_id_v1")
    }

    val progressFlow: Flow<PersistedProgress> = context.cascadeDataStore.data.map { prefs ->
        val haptics = prefs[Keys.HAPTICS_ENABLED] ?: true
        val settings = GameSettings(
            soundEnabled = prefs[Keys.SOUND_ENABLED] ?: true,
            musicEnabled = prefs[Keys.MUSIC_ENABLED] ?: true,
            hapticsEnabled = haptics,
            visualEffectsEnabled = prefs[Keys.VISUAL_EFFECTS_ENABLED] ?: true,
            placementPreviewEnabled = prefs[Keys.PLACEMENT_PREVIEW_ENABLED] ?: true,
            blockStyle = BlockStyle.fromId(prefs[Keys.BLOCK_STYLE]),
            boardPalette = BoardColorPalette.fromId(prefs[Keys.BOARD_PALETTE])
        )

        val protectedState = readProtectedProgressState(prefs)
        val savedClassic = readSavedClassicGame(prefs)
        val walletState = readWalletState(prefs)

        PersistedProgress(
            classicBestScore = protectedState.classicBestScore,
            unlockedChallengeLevel = protectedState.unlockedChallengeLevel,
            hapticsEnabled = haptics,
            savedClassicGame = savedClassic,
            settings = settings,
            statistics = protectedState.statistics,
            challengeProgress = protectedState.challengeProgress,
            wallet = walletState.wallet,
            playerDisplayName = prefs[Keys.PLAYER_DISPLAY_NAME]?.trim()?.takeIf { it.isNotEmpty() },
            profileAvatarId = ProfileAvatar.fromId(prefs[Keys.PROFILE_AVATAR_ID]).id,
            localIntegrityTampered = protectedState.tampered || walletState.tampered
        )
    }

    suspend fun loadInitialProgress(): PersistedProgress = progressFlow.first()

    suspend fun loadDailyProgress(): com.example.game.DailyProgress =
        com.example.game.DailyProgress.fromJson(context.cascadeDataStore.data.first()[Keys.DAILY_PROGRESS])

    /** Atomic read-modify-write so concurrent updates never lose a better score. */
    suspend fun updateDailyProgress(
        transform: (com.example.game.DailyProgress) -> com.example.game.DailyProgress
    ) {
        context.cascadeDataStore.edit { prefs ->
            val cur = com.example.game.DailyProgress.fromJson(prefs[Keys.DAILY_PROGRESS])
            prefs[Keys.DAILY_PROGRESS] = transform(cur).toJson()
        }
    }

    suspend fun updateClassicBestScore(score: Int) {
        if (score <= 0) return
        context.cascadeDataStore.edit { prefs ->
            val current = readProtectedProgressState(prefs)
            if (score > current.classicBestScore) {
                val updated = current.copy(
                    classicBestScore = score,
                    statistics = current.statistics.copy(classicBestScore = score),
                    tampered = false
                )
                writeProtectedProgressState(prefs, updated)
            }
        }
    }

    suspend fun unlockChallengeLevel(levelNumber: Int) {
        context.cascadeDataStore.edit { prefs ->
            val current = readProtectedProgressState(prefs)
            if (levelNumber > current.unlockedChallengeLevel) {
                val updated = current.copy(
                    unlockedChallengeLevel = levelNumber,
                    tampered = false
                )
                writeProtectedProgressState(prefs, updated)
            }
        }
    }

    suspend fun saveChallengeLevelResult(
        levelNumber: Int,
        score: Int,
        stars: Int,
        completed: Boolean,
        nextUnlockedLevel: Int
    ) {
        context.cascadeDataStore.edit { prefs ->
            val current = readProtectedProgressState(prefs)
            val updatedUnlocked = maxOf(current.unlockedChallengeLevel, nextUnlockedLevel)
            val currentMap = current.challengeProgress.toMutableMap()
            val prev = currentMap[levelNumber]
            currentMap[levelNumber] = ChallengeLevelProgress(
                levelNumber = levelNumber,
                bestScore = maxOf(prev?.bestScore ?: 0, score),
                stars = maxOf(prev?.stars ?: 0, stars),
                completed = (prev?.completed == true) || completed
            )
            writeProtectedProgressState(
                prefs,
                current.copy(
                    unlockedChallengeLevel = updatedUnlocked,
                    challengeProgress = currentMap,
                    tampered = false
                )
            )
        }
    }

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.cascadeDataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
        }
    }

    suspend fun updateSettings(settings: GameSettings) {
        context.cascadeDataStore.edit { prefs ->
            prefs[Keys.SOUND_ENABLED] = settings.soundEnabled
            prefs[Keys.MUSIC_ENABLED] = settings.musicEnabled
            prefs[Keys.HAPTICS_ENABLED] = settings.hapticsEnabled
            prefs[Keys.VISUAL_EFFECTS_ENABLED] = settings.visualEffectsEnabled
            prefs[Keys.PLACEMENT_PREVIEW_ENABLED] = settings.placementPreviewEnabled
            prefs[Keys.BLOCK_STYLE] = settings.blockStyle.id
            prefs[Keys.BOARD_PALETTE] = settings.boardPalette.id
        }
    }

    suspend fun incrementGamesPlayed() {
        context.cascadeDataStore.edit { prefs ->
            val current = readProtectedProgressState(prefs)
            val updated = current.copy(
                statistics = current.statistics.copy(
                    totalGamesPlayed = current.statistics.totalGamesPlayed + 1
                ),
                tampered = false
            )
            writeProtectedProgressState(prefs, updated)
        }
    }

    suspend fun recordTurnStatistics(
        blocksPlaced: Int,
        linesCleared: Int,
        cascadesTriggered: Int,
        maxDepthInTurn: Int,
        fullClearAdded: Int = 0
    ) {
        context.cascadeDataStore.edit { prefs ->
            val current = readProtectedProgressState(prefs)
            val s = current.statistics
            val updated = current.copy(
                statistics = s.copy(
                    totalBlocksPlaced = s.totalBlocksPlaced + blocksPlaced,
                    totalLinesCleared = s.totalLinesCleared + linesCleared,
                    totalCascadesTriggered = s.totalCascadesTriggered + cascadesTriggered,
                    maxCascadeDepth = maxOf(s.maxCascadeDepth, maxDepthInTurn),
                    fullBoardClears = s.fullBoardClears + fullClearAdded
                ),
                tampered = false
            )
            writeProtectedProgressState(prefs, updated)
        }
    }

    suspend fun updatePlayerDisplayName(name: String) {
        val sanitized = name.trim().take(24)
        context.cascadeDataStore.edit { prefs ->
            if (sanitized.isEmpty()) {
                prefs.remove(Keys.PLAYER_DISPLAY_NAME)
            } else {
                prefs[Keys.PLAYER_DISPLAY_NAME] = sanitized
            }
        }
    }

    suspend fun updateProfileAvatar(avatarId: String) {
        val normalized = ProfileAvatar.fromId(avatarId).id
        context.cascadeDataStore.edit { prefs ->
            prefs[Keys.PROFILE_AVATAR_ID] = normalized
        }
    }

    suspend fun mergeCloudProgress(
        cloudBestScore: Int,
        cloudTotalGames: Int,
        cloudTotalLines: Int,
        cloudTotalCascades: Int,
        cloudMaxDepth: Int,
        cloudTotalBlocks: Int,
        cloudFullClears: Int,
        cloudDisplayName: String? = null,
        cloudAvatarId: String? = null
    ): PersistedProgress {
        context.cascadeDataStore.edit { prefs ->
            val current = readProtectedProgressState(prefs)
            val mergedBest = maxOf(current.classicBestScore, cloudBestScore.coerceAtLeast(0))
            val s = current.statistics
            val mergedStats = s.copy(
                totalGamesPlayed = maxOf(s.totalGamesPlayed, cloudTotalGames.coerceAtLeast(0)),
                totalLinesCleared = maxOf(s.totalLinesCleared, cloudTotalLines.coerceAtLeast(0)),
                totalCascadesTriggered = maxOf(s.totalCascadesTriggered, cloudTotalCascades.coerceAtLeast(0)),
                maxCascadeDepth = maxOf(s.maxCascadeDepth, cloudMaxDepth.coerceAtLeast(0)),
                totalBlocksPlaced = maxOf(s.totalBlocksPlaced, cloudTotalBlocks.coerceAtLeast(0)),
                fullBoardClears = maxOf(s.fullBoardClears, cloudFullClears.coerceAtLeast(0)),
                classicBestScore = mergedBest
            )
            writeProtectedProgressState(
                prefs,
                current.copy(
                    classicBestScore = mergedBest,
                    statistics = mergedStats,
                    tampered = false
                )
            )
            if (prefs[Keys.PLAYER_DISPLAY_NAME].isNullOrBlank() &&
                !cloudDisplayName.isNullOrBlank() &&
                cloudDisplayName != "Player"
            ) {
                prefs[Keys.PLAYER_DISPLAY_NAME] = cloudDisplayName.trim().take(24)
            }
            if (prefs[Keys.PROFILE_AVATAR_ID].isNullOrBlank() &&
                !cloudAvatarId.isNullOrBlank() &&
                cloudAvatarId in ProfileAvatar.VALID_IDS
            ) {
                prefs[Keys.PROFILE_AVATAR_ID] = cloudAvatarId
            }
        }
        return loadInitialProgress()
    }

    suspend fun saveClassicGame(snapshot: SavedClassicGame?) {
        context.cascadeDataStore.edit { prefs ->
            prefs.remove(Keys.SAVED_CLASSIC_JSON)
            if (snapshot == null) {
                prefs.remove(Keys.SAVED_CLASSIC_ENVELOPE)
            } else {
                val json = serializeClassicGame(snapshot)
                prefs[Keys.SAVED_CLASSIC_ENVELOPE] =
                    cryptoCache.encrypt(RECORD_SAVED_CLASSIC, json)
            }
        }
    }

    /**
     * Persists the player wallet inside the same AES-GCM authenticated envelope used for
     * protected progression. Values are sanitized before sealing so a corrupted or edited
     * payload can never inflate balances beyond the engine's hard caps.
     */
    suspend fun updateWallet(wallet: PlayerWallet) {
        context.cascadeDataStore.edit { prefs ->
            val sanitized = sanitizeWallet(wallet)
            prefs[Keys.WALLET_ENVELOPE] = cryptoCache.encrypt(
                RECORD_WALLET,
                Json.encodeToString(PlayerWallet.serializer(), sanitized)
            )
        }
    }

    suspend fun resetAllProgress() {
        context.cascadeDataStore.edit { prefs ->
            prefs.remove(Keys.CLASSIC_BEST)
            prefs.remove(Keys.UNLOCKED_CHALLENGE_LEVEL)
            prefs.remove(Keys.SAVED_CLASSIC_JSON)
            prefs.remove(Keys.SAVED_CLASSIC_ENVELOPE)
            prefs.remove(Keys.TOTAL_GAMES_PLAYED)
            prefs.remove(Keys.TOTAL_LINES_CLEARED)
            prefs.remove(Keys.TOTAL_CASCADES)
            prefs.remove(Keys.MAX_CASCADE_DEPTH)
            prefs.remove(Keys.TOTAL_BLOCKS_PLACED)
            prefs.remove(Keys.CHALLENGE_PROGRESS_JSON)
            prefs.remove(Keys.PROTECTED_PROGRESS_ENVELOPE)
            prefs.remove(Keys.WALLET_ENVELOPE)
            prefs.remove(Keys.PLAYER_DISPLAY_NAME)
            prefs.remove(Keys.PROFILE_AVATAR_ID)
        }
    }

    private fun readProtectedProgressState(prefs: Preferences): ProtectedProgressState {
        val envelope = prefs[Keys.PROTECTED_PROGRESS_ENVELOPE]
        if (envelope != null) {
            return when (val result = cryptoCache.decrypt(RECORD_PROTECTED_PROGRESS, envelope)) {
                is DecryptionResult.Verified -> deserializeProtectedProgress(result.plaintext)
                    ?: ProtectedProgressState(
                        classicBestScore = 0,
                        unlockedChallengeLevel = 1,
                        statistics = GameStatistics(),
                        challengeProgress = emptyMap(),
                        tampered = true
                    )

                is DecryptionResult.TamperedOrCorrupted -> ProtectedProgressState(
                    classicBestScore = 0,
                    unlockedChallengeLevel = 1,
                    statistics = GameStatistics(),
                    challengeProgress = emptyMap(),
                    tampered = true
                )

                is DecryptionResult.Missing -> readLegacyUnencryptedProgress(prefs)
            }
        }
        return readLegacyUnencryptedProgress(prefs)
    }

    private fun readLegacyUnencryptedProgress(prefs: Preferences): ProtectedProgressState {
        val classicBest = (prefs[Keys.CLASSIC_BEST] ?: 0).coerceAtLeast(0)
        val unlocked = (prefs[Keys.UNLOCKED_CHALLENGE_LEVEL] ?: 1).coerceIn(1, 50)
        val stats = GameStatistics(
            totalGamesPlayed = (prefs[Keys.TOTAL_GAMES_PLAYED] ?: 0).coerceAtLeast(0),
            totalLinesCleared = (prefs[Keys.TOTAL_LINES_CLEARED] ?: 0).coerceAtLeast(0),
            totalCascadesTriggered = (prefs[Keys.TOTAL_CASCADES] ?: 0).coerceAtLeast(0),
            maxCascadeDepth = (prefs[Keys.MAX_CASCADE_DEPTH] ?: 0).coerceAtLeast(0),
            totalBlocksPlaced = (prefs[Keys.TOTAL_BLOCKS_PLACED] ?: 0).coerceAtLeast(0),
            classicBestScore = classicBest
        )
        val challengeMap = prefs[Keys.CHALLENGE_PROGRESS_JSON]
            ?.let { deserializeChallengeProgress(it) }
            ?: emptyMap()
        return ProtectedProgressState(
            classicBestScore = classicBest,
            unlockedChallengeLevel = unlocked,
            statistics = stats,
            challengeProgress = challengeMap,
            tampered = false
        )
    }

    private fun writeProtectedProgressState(
        prefs: MutablePreferences,
        state: ProtectedProgressState
    ) {
        val json = serializeProtectedProgress(state)
        prefs[Keys.PROTECTED_PROGRESS_ENVELOPE] =
            cryptoCache.encrypt(RECORD_PROTECTED_PROGRESS, json)

        // Remove unencrypted legacy keys once sealed in the authenticated AES-GCM envelope
        prefs.remove(Keys.CLASSIC_BEST)
        prefs.remove(Keys.UNLOCKED_CHALLENGE_LEVEL)
        prefs.remove(Keys.TOTAL_GAMES_PLAYED)
        prefs.remove(Keys.TOTAL_LINES_CLEARED)
        prefs.remove(Keys.TOTAL_CASCADES)
        prefs.remove(Keys.MAX_CASCADE_DEPTH)
        prefs.remove(Keys.TOTAL_BLOCKS_PLACED)
        prefs.remove(Keys.CHALLENGE_PROGRESS_JSON)
    }

    private fun readSavedClassicGame(prefs: Preferences): SavedClassicGame? {
        val envelope = prefs[Keys.SAVED_CLASSIC_ENVELOPE]
        if (envelope != null) {
            return when (val result = cryptoCache.decrypt(RECORD_SAVED_CLASSIC, envelope)) {
                is DecryptionResult.Verified -> deserializeClassicGame(result.plaintext)
                is DecryptionResult.TamperedOrCorrupted -> null
                is DecryptionResult.Missing -> null
            }
        }
        return prefs[Keys.SAVED_CLASSIC_JSON]?.let { deserializeClassicGame(it) }
    }

    private fun readWalletState(prefs: Preferences): WalletState {
        val envelope = prefs[Keys.WALLET_ENVELOPE] ?: return WalletState(PlayerWallet())
        return when (val result = cryptoCache.decrypt(RECORD_WALLET, envelope)) {
            is DecryptionResult.Verified -> {
                val wallet = try {
                    Json.decodeFromString(PlayerWallet.serializer(), result.plaintext)
                } catch (_: Exception) {
                    null
                }
                if (wallet != null) {
                    WalletState(sanitizeWallet(wallet))
                } else {
                    WalletState(PlayerWallet(), tampered = true)
                }
            }

            is DecryptionResult.TamperedOrCorrupted -> WalletState(PlayerWallet(), tampered = true)
            is DecryptionResult.Missing -> WalletState(PlayerWallet())
        }
    }

    private fun sanitizeWallet(wallet: PlayerWallet): PlayerWallet = wallet.copy(
        goldCoins = wallet.goldCoins.coerceIn(0, PlayerWallet.MAX_GOLD),
        diamonds = wallet.diamonds.coerceIn(0, PlayerWallet.MAX_DIAMONDS),
        hammerCount = wallet.hammerCount.coerceIn(0, PlayerWallet.MAX_BOOSTER_STACK),
        refreshCount = wallet.refreshCount.coerceIn(0, PlayerWallet.MAX_BOOSTER_STACK),
        shuffleCount = wallet.shuffleCount.coerceIn(0, PlayerWallet.MAX_BOOSTER_STACK),
        unlockedEntitlements = wallet.unlockedEntitlements.take(64).toSet(),
        processedLedgerIds = wallet.processedLedgerIds.toList().takeLast(64).toSet()
    )

    companion object {
        internal const val RECORD_PROTECTED_PROGRESS = "protected_progress_v1"
        internal const val RECORD_SAVED_CLASSIC = "saved_classic_v1"
        internal const val RECORD_WALLET = "wallet_v1"

        private fun serializeProtectedProgress(state: ProtectedProgressState): String {
            val root = JSONObject()
            root.put("classicBestScore", state.classicBestScore)
            root.put("unlockedChallengeLevel", state.unlockedChallengeLevel)

            val statsObj = JSONObject()
            statsObj.put("totalGamesPlayed", state.statistics.totalGamesPlayed)
            statsObj.put("totalLinesCleared", state.statistics.totalLinesCleared)
            statsObj.put("totalCascadesTriggered", state.statistics.totalCascadesTriggered)
            statsObj.put("maxCascadeDepth", state.statistics.maxCascadeDepth)
            statsObj.put("totalBlocksPlaced", state.statistics.totalBlocksPlaced)
            statsObj.put("classicBestScore", state.classicBestScore)
            statsObj.put("fullBoardClears", state.statistics.fullBoardClears)
            root.put("statistics", statsObj)

            root.put("challengeProgress", JSONArray(serializeChallengeProgress(state.challengeProgress)))
            return root.toString()
        }

        private fun deserializeProtectedProgress(json: String): ProtectedProgressState? {
            return try {
                val root = JSONObject(json)
                val best = root.optInt("classicBestScore", 0).coerceAtLeast(0)
                val unlocked = root.optInt("unlockedChallengeLevel", 1).coerceIn(1, 50)
                val statsObj = root.optJSONObject("statistics")
                val stats = GameStatistics(
                    totalGamesPlayed = (statsObj?.optInt("totalGamesPlayed", 0) ?: 0).coerceAtLeast(0),
                    totalLinesCleared = (statsObj?.optInt("totalLinesCleared", 0) ?: 0).coerceAtLeast(0),
                    totalCascadesTriggered = (statsObj?.optInt("totalCascadesTriggered", 0) ?: 0).coerceAtLeast(0),
                    maxCascadeDepth = (statsObj?.optInt("maxCascadeDepth", 0) ?: 0).coerceAtLeast(0),
                    totalBlocksPlaced = (statsObj?.optInt("totalBlocksPlaced", 0) ?: 0).coerceAtLeast(0),
                    classicBestScore = best,
                    fullBoardClears = (statsObj?.optInt("fullBoardClears", 0) ?: 0).coerceAtLeast(0)
                )
                val challengeArr = root.optJSONArray("challengeProgress")?.toString() ?: "[]"
                ProtectedProgressState(
                    classicBestScore = best,
                    unlockedChallengeLevel = unlocked,
                    statistics = stats,
                    challengeProgress = deserializeChallengeProgress(challengeArr),
                    tampered = false
                )
            } catch (_: Exception) {
                null
            }
        }

        fun serializeChallengeProgress(progress: Map<Int, ChallengeLevelProgress>): String {
            val arr = JSONArray()
            progress.values.sortedBy { it.levelNumber }.forEach { item ->
                val obj = JSONObject()
                obj.put("levelNumber", item.levelNumber)
                obj.put("bestScore", item.bestScore)
                obj.put("stars", item.stars)
                obj.put("completed", item.completed)
                arr.put(obj)
            }
            return arr.toString()
        }

        fun deserializeChallengeProgress(json: String): Map<Int, ChallengeLevelProgress> {
            return try {
                val arr = JSONArray(json)
                val result = mutableMapOf<Int, ChallengeLevelProgress>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val level = obj.getInt("levelNumber")
                    result[level] = ChallengeLevelProgress(
                        levelNumber = level,
                        bestScore = obj.optInt("bestScore", 0),
                        stars = obj.optInt("stars", 0),
                        completed = obj.optBoolean("completed", false)
                    )
                }
                result
            } catch (_: Exception) {
                emptyMap()
            }
        }

        fun serializeClassicGame(snapshot: SavedClassicGame): String {
            val root = JSONObject()
            root.put("score", snapshot.score)
            root.put("movesPlayed", snapshot.movesPlayed)
            root.put("bestCascadeDepth", snapshot.bestCascadeDepth)
            root.put("totalLinesCleared", snapshot.totalLinesCleared)
            root.put("nextIdCounter", snapshot.nextIdCounter)

            val rowsArr = JSONArray()
            for (r in 0 until Board.SIZE) {
                val rowArr = JSONArray()
                for (c in 0 until Board.SIZE) {
                    val cell = snapshot.board.get(r, c)
                    if (cell == null) {
                        rowArr.put(JSONObject.NULL)
                    } else {
                        val cellObj = JSONObject()
                        cellObj.put("id", cell.id)
                        cellObj.put("color", cell.color.index)
                        rowArr.put(cellObj)
                    }
                }
                rowsArr.put(rowArr)
            }
            root.put("board", rowsArr)

            val trayArr = JSONArray()
            for (piece in snapshot.trayPieces) {
                if (piece == null) {
                    trayArr.put(JSONObject.NULL)
                } else {
                    val pObj = JSONObject()
                    pObj.put("instanceId", piece.instanceId)
                    pObj.put("shapeId", piece.shape.id)
                    pObj.put("color", piece.color.index)
                    trayArr.put(pObj)
                }
            }
            root.put("tray", trayArr)
            return root.toString()
        }

        fun deserializeClassicGame(json: String): SavedClassicGame? {
            return try {
                val root = JSONObject(json)
                val score = root.getInt("score")
                val movesPlayed = root.getInt("movesPlayed")
                val bestCascadeDepth = root.optInt("bestCascadeDepth", 0)
                val totalLinesCleared = root.optInt("totalLinesCleared", 0)
                val nextIdCounter = root.getLong("nextIdCounter")

                val rowsArr = root.getJSONArray("board")
                if (rowsArr.length() != Board.SIZE) return null
                val grid = List(Board.SIZE) { r ->
                    val rowArr = rowsArr.getJSONArray(r)
                    if (rowArr.length() != Board.SIZE) return null
                    List(Board.SIZE) { c ->
                        if (rowArr.isNull(c)) {
                            null
                        } else {
                            val cellObj = rowArr.getJSONObject(c)
                            BlockCell(
                                id = cellObj.getLong("id"),
                                color = BlockColor.fromIndex(cellObj.getInt("color"))
                            )
                        }
                    }
                }

                val trayArr = root.getJSONArray("tray")
                val tray = List(3) { idx ->
                    if (idx >= trayArr.length() || trayArr.isNull(idx)) {
                        null
                    } else {
                        val pObj = trayArr.getJSONObject(idx)
                        val shape = PieceShape.fromId(pObj.getString("shapeId")) ?: return null
                        GamePiece(
                            instanceId = pObj.getLong("instanceId"),
                            shape = shape,
                            color = BlockColor.fromIndex(pObj.getInt("color"))
                        )
                    }
                }

                SavedClassicGame(
                    board = Board(grid),
                    trayPieces = tray,
                    score = score,
                    movesPlayed = movesPlayed,
                    bestCascadeDepth = bestCascadeDepth,
                    totalLinesCleared = totalLinesCleared,
                    nextIdCounter = nextIdCounter
                )
            } catch (_: Exception) {
                null
            }
        }
    }
}
