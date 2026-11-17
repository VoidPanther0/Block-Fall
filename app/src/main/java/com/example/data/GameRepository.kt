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
import com.example.game.Board
import com.example.game.GamePiece
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
    val savedClassicGame: SavedClassicGame? = null
)

class GameRepository(private val context: Context) {

    private object Keys {
        val CLASSIC_BEST = intPreferencesKey("classic_best_score")
        val UNLOCKED_CHALLENGE_LEVEL = intPreferencesKey("unlocked_challenge_level")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val SAVED_CLASSIC_JSON = stringPreferencesKey("saved_classic_json")
    }

    val progressFlow: Flow<PersistedProgress> = context.cascadeDataStore.data.map { prefs ->
        PersistedProgress(
            classicBestScore = prefs[Keys.CLASSIC_BEST] ?: 0,
            unlockedChallengeLevel = prefs[Keys.UNLOCKED_CHALLENGE_LEVEL] ?: 1,
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            savedClassicGame = prefs[Keys.SAVED_CLASSIC_JSON]?.let { deserializeClassicGame(it) }
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

    suspend fun setHapticsEnabled(enabled: Boolean) {
        context.cascadeDataStore.edit { prefs ->
            prefs[Keys.HAPTICS_ENABLED] = enabled
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

    companion object {
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
