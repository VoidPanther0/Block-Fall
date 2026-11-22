package com.example

import com.example.game.*
import org.junit.Assert.*
import org.junit.Test

class CoreLogicTest {
    private fun rows(vararg r: String) = Board.fromPattern(r.toList())

    @Test fun noClearsGivesNoFeedback() {
        val res = CascadeResolver.resolve(Board.empty())
        assertEquals(ScoreFeedback.NONE, ScoreFeedback.from(res))
    }

    @Test fun fullClearIsPerfect() {
        val b = rows("########", "........", "........", "........", "........", "........", "........", "........")
        val fb = ScoreFeedback.from(CascadeResolver.resolve(b))
        assertEquals(FeedbackTier.PERFECT, fb.tier)
        assertEquals(1, fb.linesCleared)
    }

    @Test fun comboLabels() {
        assertEquals(ComboLabel.NONE, ComboLabel.forDepth(1))
        assertEquals(ComboLabel.COMBO_2, ComboLabel.forDepth(2))
        assertEquals(ComboLabel.COMBO_4, ComboLabel.forDepth(4))
        assertEquals(ComboLabel.COMBO_5_PLUS, ComboLabel.forDepth(9))
    }

    @Test fun cascadeMultiplierAndBonus() {
        val w1 = ScoringEngine.scoreWave(1, 1, false)
        val w2 = ScoringEngine.scoreWave(1, 2, false)
        assertEquals(10, w1.totalPoints)
        assertEquals(2, w2.multiplier)
        assertEquals(25, w2.cascadeBonus)
    }

    @Test fun unavailableBoostersCannotBePurchased() {
        val w = PlayerWallet(goldCoins = 10_000)
        for (id in listOf("booster_hammer_1", "booster_refresh_1", "booster_trio_bundle")) {
            assertTrue(EconomyEngine.purchaseShopItem(w, id, "t-$id") is EconomyMutationResult.Rejected)
        }
        assertTrue(ShopCatalog.items.none { it.available.not() })
    }

    @Test fun purchaseNeverGoesNegativeAndRejectsDuplicates() {
        val w = PlayerWallet(goldCoins = 50)
        assertTrue(EconomyEngine.purchaseShopItem(w, "booster_shuffle_1", "a") is EconomyMutationResult.Rejected)
        val rich = PlayerWallet(goldCoins = 200)
        val ok = EconomyEngine.purchaseShopItem(rich, "booster_shuffle_1", "b") as EconomyMutationResult.Success
        assertEquals(100, ok.updatedWallet.goldCoins)
        assertTrue(EconomyEngine.purchaseShopItem(ok.updatedWallet, "booster_shuffle_1", "b") is EconomyMutationResult.Rejected)
    }

    @Test fun walletFullRejectsInsteadOfWasting() {
        val w = PlayerWallet(goldCoins = PlayerWallet.MAX_GOLD - 10, diamonds = 100)
        assertTrue(EconomyEngine.purchaseShopItem(w, "exchange_gems_to_gold_small", "x") is EconomyMutationResult.Rejected)
    }

    @Test fun comingSoonProductsHaveNoPurchasePath() {
        assertTrue(ShopCatalog.comingSoon.isNotEmpty())
        assertNull(ShopCatalog.findById(ShopCatalog.comingSoon.first().id))
    }
}
