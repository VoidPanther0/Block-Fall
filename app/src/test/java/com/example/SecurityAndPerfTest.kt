package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GameRepository
import com.example.game.BlockColor
import com.example.game.Board
import com.example.game.GamePiece
import com.example.game.LineClearEngine
import com.example.game.PieceShape
import com.example.game.PlacementValidator
import com.example.security.DecryptionResult
import com.example.security.EncryptedEntitlementCache
import com.example.security.SecretKeyProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.SecureRandom
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class SecurityAndPerfTest {

    private class FixedTestKeyProvider : SecretKeyProvider {
        private val key: SecretKey by lazy {
            val bytes = ByteArray(32)
            SecureRandom().nextBytes(bytes)
            SecretKeySpec(bytes, "AES")
        }

        override fun getOrCreateKey(): SecretKey = key
    }

    @Test
    fun `18_encrypted_cache_roundtrip_and_tamper_detection_rejects_modified_payloads`() {
        val keyProvider = FixedTestKeyProvider()
        val cache = EncryptedEntitlementCache(
            packageName = "com.aistudio.cascadegrid.vqmxrt",
            keyProvider = keyProvider
        )
        val payload = """{"classicBestScore":4200,"unlockedChallengeLevel":5}"""

        val envelope = cache.encrypt("protected_progress_v1", payload)
        assertTrue(envelope.startsWith("v1:"))
        assertNotEquals(payload, envelope)

        val decrypted = cache.decrypt("protected_progress_v1", envelope)
        assertTrue(decrypted is DecryptionResult.Verified)
        assertEquals(payload, (decrypted as DecryptionResult.Verified).plaintext)

        // Ciphertext tampering must fail GCM authentication
        val parts = envelope.split(":").toMutableList()
        val cipherChars = parts[2].toCharArray()
        cipherChars[cipherChars.lastIndex / 2] = if (cipherChars[cipherChars.lastIndex / 2] == 'A') 'B' else 'A'
        val tamperedEnvelope = "${parts[0]}:${parts[1]}:${String(cipherChars)}"
        assertTrue(cache.decrypt("protected_progress_v1", tamperedEnvelope) is DecryptionResult.TamperedOrCorrupted)

        // Swapping record key violates AAD binding
        assertTrue(cache.decrypt("saved_classic_v1", envelope) is DecryptionResult.TamperedOrCorrupted)

        // Package name mismatch violates AAD binding
        val otherAppCache = EncryptedEntitlementCache(
            packageName = "com.attacker.repackaged",
            keyProvider = keyProvider
        )
        assertTrue(otherAppCache.decrypt("protected_progress_v1", envelope) is DecryptionResult.TamperedOrCorrupted)

        // Null/blank returns Missing; plain JSON without envelope returns TamperedOrCorrupted
        assertTrue(cache.decrypt("protected_progress_v1", null) is DecryptionResult.Missing)
        assertTrue(cache.decrypt("protected_progress_v1", payload) is DecryptionResult.TamperedOrCorrupted)
    }

    @Test
    fun `19_zero_copy_predicted_full_lines_matches_board_copy_line_clear_engine`() {
        val board = Board.fromPattern(
            listOf(
                ".0000000",
                ".1111111",
                ".22.....",
                ".33.....",
                ".44.....",
                ".55.....",
                ".66.....",
                "........"
            )
        )

        val vertBar = GamePiece(2L, PieceShape.DOMINO_V, BlockColor.AMBER)
        val anchorRow = 0
        val anchorCol = 0

        val zeroCopyPrediction = LineClearEngine.findPredictedFullLines(board, vertBar, anchorRow, anchorCol)
        val copiedBoardPrediction = LineClearEngine.findFullLines(
            PlacementValidator.placePiece(board, vertBar, anchorRow, anchorCol, 100L)!!.board
        )

        assertEquals(copiedBoardPrediction.fullRows, zeroCopyPrediction.fullRows)
        assertEquals(copiedBoardPrediction.fullCols, zeroCopyPrediction.fullCols)
        assertEquals(listOf(0, 1), zeroCopyPrediction.fullRows)
        assertTrue(zeroCopyPrediction.fullCols.isEmpty())

        // Combined row + column prediction on bottom-left corner
        val singleDot = GamePiece(3L, PieceShape.DOT, BlockColor.EMERALD)
        val dotBoard = Board.fromPattern(
            listOf(
                "0.......",
                "0.......",
                "0.......",
                "0.......",
                "0.......",
                "0.......",
                "0.......",
                ".1111111"
            )
        )
        val dotZeroCopy = LineClearEngine.findPredictedFullLines(dotBoard, singleDot, 7, 0)
        val dotCopied = LineClearEngine.findFullLines(
            PlacementValidator.placePiece(dotBoard, singleDot, 7, 0, 200L)!!.board
        )
        assertEquals(dotCopied, dotZeroCopy)
        assertEquals(listOf(7), dotZeroCopy.fullRows)
        assertEquals(listOf(0), dotZeroCopy.fullCols)
    }

    @Test
    fun `20_repository_seals_protected_progress_in_encrypted_datastore_and_resets_cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GameRepository(context)
        runBlocking {
            repo.resetAllProgress()
            repo.updateClassicBestScore(1850)
            repo.saveChallengeLevelResult(
                levelNumber = 2,
                score = 310,
                stars = 3,
                completed = true,
                nextUnlockedLevel = 3
            )
            val loaded = repo.loadInitialProgress()
            assertFalse(loaded.localIntegrityTampered)
            assertEquals(1850, loaded.classicBestScore)
            assertEquals(3, loaded.unlockedChallengeLevel)
            assertEquals(3, loaded.challengeProgress[2]?.stars)

            repo.resetAllProgress()
            val afterReset = repo.loadInitialProgress()
            assertEquals(0, afterReset.classicBestScore)
            assertEquals(1, afterReset.unlockedChallengeLevel)
            assertTrue(afterReset.challengeProgress.isEmpty())
        }
    }

    @Test
    fun `21_game_session_engine_executes_turn_deterministically_and_awards_challenge_stars`() {
        val engine = com.example.game.GameSessionEngine(
            com.example.game.PieceGenerator(kotlin.random.Random(42))
        )
        val board = Board.fromPattern(
            listOf(
                "........",
                "........",
                "........",
                "........",
                "........",
                "........",
                "........",
                ".1111111"
            )
        )
        val tray = listOf(
            com.example.game.TraySlot(0, GamePiece(10L, PieceShape.DOT, BlockColor.CYAN)),
            com.example.game.TraySlot(1, GamePiece(11L, PieceShape.DOMINO_H, BlockColor.AMBER)),
            com.example.game.TraySlot(2, null)
        )
        val level = com.example.game.ChallengeLevels.getLevel(1)

        val result = engine.executeTurn(
            board = board,
            traySlots = tray,
            slotIndex = 0,
            anchorRow = 7,
            anchorCol = 0,
            currentScore = 95,
            movesPlayed = 0,
            mode = com.example.game.GameMode.CHALLENGE,
            challengeLevel = level,
            startingBlockId = 100L
        )

        assertTrue(result != null)
        val turn = result!!
        assertEquals(1, turn.totalLinesInTurn)
        assertTrue(turn.fullClearOccurred)
        assertTrue(turn.finalScore >= level.targetScore)
        assertEquals(com.example.game.GameStatus.CHALLENGE_WON, turn.nextStatus)
        assertEquals(3, turn.earnedChallengeStars)
    }

    @Test
    fun `22_cloud_models_and_play_integrity_helpers_enforce_rule_parity_and_monotonic_merge`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = GameRepository(context)

        val nonce1 = com.example.security.PlayIntegrityManager.generateNonce()
        val nonce2 = com.example.security.PlayIntegrityManager.generateNonce()
        assertTrue(nonce1.length in 16..128)
        assertNotEquals(nonce1, nonce2)
        val digest = com.example.security.PlayIntegrityManager.sha256Hex("sample_token")
        assertEquals(64, digest.length)

        val profile = com.example.data.cloud.UserCloudProfile(
            userId = "uid_test_1",
            displayName = "   Cascade Pro   ",
            classicBestScore = 2500L,
            unlockedChallengeLevel = 4L,
            blockStyleId = "invalid_style",
            boardPaletteId = "midnight"
        )
        val createMap = profile.toCreateMap()
        assertFalse(createMap.containsKey("id"))
        assertFalse(createMap.containsKey("documentId"))
        assertEquals("Cascade Pro", createMap["displayName"])
        assertEquals("modern", createMap["blockStyleId"])
        assertEquals("midnight", createMap["boardPaletteId"])
        assertTrue(createMap["createdAt"] is com.google.firebase.firestore.FieldValue)
        assertTrue(createMap["updatedAt"] is com.google.firebase.firestore.FieldValue)

        runBlocking {
            repo.resetAllProgress()
            repo.updateClassicBestScore(2500)
            // Lower cloud score must NOT overwrite higher local score
            val mergedLower = repo.mergeCloudProgress(
                cloudBestScore = 1100,
                cloudUnlockedLevel = 2,
                cloudTotalGames = 5,
                cloudTotalLines = 12,
                cloudTotalCascades = 2,
                cloudMaxDepth = 2,
                cloudTotalBlocks = 40,
                cloudFullClears = 0,
                cloudChallenges = mapOf(
                    1 to com.example.game.ChallengeLevelProgress(1, 140, 2, true)
                )
            )
            assertEquals(2500, mergedLower.classicBestScore)
            assertEquals(2, mergedLower.unlockedChallengeLevel)
            assertEquals(2, mergedLower.challengeProgress[1]?.stars)

            // Higher cloud score upgrades local score
            val mergedHigher = repo.mergeCloudProgress(
                cloudBestScore = 4100,
                cloudUnlockedLevel = 5,
                cloudTotalGames = 15,
                cloudTotalLines = 80,
                cloudTotalCascades = 14,
                cloudMaxDepth = 5,
                cloudTotalBlocks = 300,
                cloudFullClears = 2,
                cloudChallenges = mapOf(
                    1 to com.example.game.ChallengeLevelProgress(1, 210, 3, true)
                )
            )
            assertEquals(4100, mergedHigher.classicBestScore)
            assertEquals(5, mergedHigher.unlockedChallengeLevel)
            assertEquals(3, mergedHigher.challengeProgress[1]?.stars)
            assertEquals(2, mergedHigher.statistics.fullBoardClears)
        }
    }
}
