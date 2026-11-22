package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
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
import com.example.game.SavedClassicGame
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
    val challengeProgress: Map<Int, ChallengeLevelProgress> = emptyMap()
)

class GameRepository(private val context: Context) {

    private object Keys {
        // Legacy / existing keys — preserved for full DataStore compatibility
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
    }

    val progressFlow: Flow<PersistedProgress> = context.cascadeDataStore.data.map { prefs ->
        val classicBest = prefs[Keys.CLASSIC_BEST] ?: 0
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
        val stats = GameStatistics(
            totalGamesPlayed = prefs[Keys.TOTAL_GAMES_PLAYED] ?: 0,
            totalLinesCleared = prefs[Keys.TOTAL_LINES_CLEARED] ?: 0,
            totalCascadesTriggered = prefs[Keys.TOTAL_CASCADES] ?: 0,
            maxCascadeDepth = prefs[Keys.MAX_CASCADE_DEPTH] ?: 0,
            totalBlocksPlaced = prefs[Keys.TOTAL_BLOCKS_PLACED] ?: 0,
            classicBestScore = classicBest
        )
        val challengeProgress = prefs[Keys.CHALLENGE_PROGRESS_JSON]
            ?.let { deserializeChallengeProgress(it) }
            ?: emptyMap()

        PersistedProgress(
            classicBestScore = classicBest,
            unlockedChallengeLevel = prefs[Keys.UNLOCKED_CHALLENGE_LEVEL] ?: 1,
            hapticsEnabled = haptics,
            savedClassicGame = prefs[Keys.SAVED_CLASSIC_JSON]?.let { deserializeClassicGame(it) },
            settings = settings,
            statistics = stats,
            challengeProgress = challengeProgress
        )
    }

    suspend fun loadInitialProgress(): PersistedProgress = progressFlow.first()

    suspend fun updateClassicBestScore(score: Int) {
        context.cascadeDataStore.edit { prefs ->
            val current = prefs[Keys.CLASSIC_BEST] ?: 0
            if (score > current) {
                prefs[Keys.CLASSIC_BEST] = score
            }
        }
    }

    suspend fun unlockChallengeLevel(levelNumber: Int) {
        context.cascadeDataStore.edit { prefs ->
            val current = prefs[Keys.UNLOCKED_CHALLENGE_LEVEL] ?: 1
            if (levelNumber > current) {
                prefs[Keys.UNLOCKED_CHALLENGE_LEVEL] = levelNumber
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
            val currentUnlocked = prefs[Keys.UNLOCKED_CHALLENGE_LEVEL] ?: 1
            if (nextUnlockedLevel > currentUnlocked) {
                prefs[Keys.UNLOCKED_CHALLENGE_LEVEL] = nextUnlockedLevel
            }
            val currentMap = prefs[Keys.CHALLENGE_PROGRESS_JSON]
                ?.let { deserializeChallengeProgress(it) }
                ?.toMutableMap()
                ?: mutableMapOf()
            val prev = currentMap[levelNumber]
            currentMap[levelNumber] = ChallengeLevelProgress(
                levelNumber = levelNumber,
                bestScore = maxOf(prev?.bestScore ?: 0, score),
                stars = maxOf(prev?.stars ?: 0, stars),
                completed = (prev?.completed == true) || completed
            )
            prefs[Keys.CHALLENGE_PROGRESS_JSON] = serializeChallengeProgress(currentMap)
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
            prefs[Keys.TOTAL_GAMES_PLAYED] = (prefs[Keys.TOTAL_GAMES_PLAYED] ?: 0) + 1
        }
    }

    suspend fun recordTurnStatistics(
        blocksPlaced: Int,
        linesCleared: Int,
        cascadesTriggered: Int,
        maxDepthInTurn: Int
    ) {
        context.cascadeDataStore.edit { prefs ->
            prefs[Keys.TOTAL_BLOCKS_PLACED] = (prefs[Keys.TOTAL_BLOCKS_PLACED] ?: 0) + blocksPlaced
            prefs[Keys.TOTAL_LINES_CLEARED] = (prefs[Keys.TOTAL_LINES_CLEARED] ?: 0) + linesCleared
            prefs[Keys.TOTAL_CASCADES] = (prefs[Keys.TOTAL_CASCADES] ?: 0) + cascadesTriggered
            val currentMax = prefs[Keys.MAX_CASCADE_DEPTH] ?: 0
            if (maxDepthInTurn > currentMax) {
                prefs[Keys.MAX_CASCADE_DEPTH] = maxDepthInTurn
            }
        }
    }

    suspend fun saveClassicGame(snapshot: SavedClassicGame?) {
        context.cascadeDataStore.edit { prefs ->
            if (snapshot == null) {
                prefs.remove(Keys.SAVED_CLASSIC_JSON)
            } else {
                prefs[Keys.SAVED_CLASSIC_JSON] = serializeClassicGame(snapshot)
            }
        }
    }

    suspend fun resetAllProgress() {
        context.cascadeDataStore.edit { prefs ->
            prefs.remove(Keys.CLASSIC_BEST)
            prefs[Keys.UNLOCKED_CHALLENGE_LEVEL] = 1
            prefs.remove(Keys.SAVED_CLASSIC_JSON)
            prefs.remove(Keys.TOTAL_GAMES_PLAYED)
            prefs.remove(Keys.TOTAL_LINES_CLEARED)
            prefs.remove(Keys.TOTAL_CASCADES)
            prefs.remove(Keys.MAX_CASCADE_DEPTH)
            prefs.remove(Keys.TOTAL_BLOCKS_PLACED)
            prefs.remove(Keys.CHALLENGE_PROGRESS_JSON)
        }
    }

    companion object {
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
