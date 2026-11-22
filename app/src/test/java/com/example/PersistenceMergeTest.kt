package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.GameRepository
import com.example.game.*
import com.example.security.EncryptedEntitlementCache
import com.example.security.SecretKeyProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.security.SecureRandom
import javax.crypto.SecretKey
import javax.crypto.spec.SecretKeySpec

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class PersistenceMergeTest {
    private val keyProvider = object : SecretKeyProvider {
        private val key: SecretKey by lazy { SecretKeySpec(ByteArray(32).also { SecureRandom().nextBytes(it) }, "AES") }
        override fun getOrCreateKey(): SecretKey = key
    }

    private fun repo(ctx: Context) =
        GameRepository(ctx, EncryptedEntitlementCache("com.example.test", keyProvider))

    private fun merge(r: GameRepository, best: Int, unlocked: Int, ch: Map<Int, ChallengeLevelProgress> = emptyMap()) =
        runBlocking {
            r.mergeCloudProgress(best, unlocked, 0, 0, 0, 0, 0, 0, ch)
        }

    @Test fun guestProgressSurvivesRestartAndCloudMergeNeverLowersIt() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val r1 = repo(ctx)
        r1.resetAllProgress()
        // guest plays offline
        r1.updateClassicBestScore(900)
        r1.saveChallengeLevelResult(3, 400, 2, true, 4)
        r1.updateWallet(PlayerWallet(goldCoins = 777, diamonds = 9, hammerCount = 1, refreshCount = 2, shuffleCount = 5))
        val saved = SavedClassicGame(
            Board.fromPattern(listOf("#.......") + List(7) { "........" }),
            listOf(GamePiece(7, PieceShape.DOT, BlockColor.CYAN), null, null), 55, 4, 1, 2, 321L
        )
        r1.saveClassicGame(saved)

        // "app restart": fresh repository instance reading the same store
        val r2 = repo(ctx)
        val p = r2.loadInitialProgress()
        assertEquals(900, p.classicBestScore)
        assertEquals(4, p.unlockedChallengeLevel)
        assertEquals(2, p.challengeProgress[3]!!.stars)
        assertEquals(777, p.wallet.goldCoins); assertEquals(9, p.wallet.diamonds)
        assertEquals(5, p.wallet.shuffleCount)
        assertEquals(55, p.savedClassicGame!!.score)
        assertEquals(321L, p.savedClassicGame!!.nextIdCounter)
        assertFalse(p.localIntegrityTampered)

        // login: older cloud data must not overwrite newer local progress
        val older = merge(r2, 100, 2, mapOf(3 to ChallengeLevelProgress(3, 50, 1, false)))
        assertEquals(900, older.classicBestScore)
        assertEquals(4, older.unlockedChallengeLevel)
        assertEquals(400, older.challengeProgress[3]!!.bestScore)
        assertEquals(2, older.challengeProgress[3]!!.stars)
        assertTrue(older.challengeProgress[3]!!.completed)
        assertEquals(777, older.wallet.goldCoins) // wallet is local-only, untouched by merge

        // newer cloud data is adopted, local-only levels are kept
        val newer = merge(r2, 5000, 9, mapOf(8 to ChallengeLevelProgress(8, 1000, 3, true)))
        assertEquals(5000, newer.classicBestScore)
        assertEquals(9, newer.unlockedChallengeLevel)
        assertEquals(3, newer.challengeProgress[3]!!.levelNumber)
        assertEquals(3, newer.challengeProgress[8]!!.stars)

        // best score never decreases through the repository API either
        r2.updateClassicBestScore(10)
        assertEquals(5000, r2.loadInitialProgress().classicBestScore)
    }

    @Test fun dailyProgressPersistsAcrossRestartAndKeepsBest() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val r1 = repo(ctx)
        r1.updateDailyProgress { DailyProgress() }
        r1.updateDailyProgress { it.withAttemptStarted("2026-05-05").withResult("2026-05-05", 250, true) }
        r1.updateDailyProgress { it.withResult("2026-05-05", 90, false) }
        val back = repo(ctx).loadDailyProgress()
        assertEquals(250, back.bestScore); assertTrue(back.completed)
        assertEquals(0, back.forDate("2026-05-06").bestScore)
    }

    @Test fun walletSanitizesNegativeValues() = runBlocking {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        val r = repo(ctx)
        r.updateWallet(PlayerWallet(goldCoins = -5, diamonds = -1))
        val w = repo(ctx).loadInitialProgress().wallet
        assertTrue(w.goldCoins >= 0 && w.diamonds >= 0)
    }
}
