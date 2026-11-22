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
}
