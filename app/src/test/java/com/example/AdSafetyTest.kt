package com.example

import com.example.game.EconomyEngine
import com.example.game.EconomyMutationResult
import com.example.game.PlayerWallet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the ad-reward path against replay and accidental empty credits. The helper below
 * mirrors the ViewModel contract: only a [EconomyMutationResult.Success] replaces the wallet.
 */
class AdSafetyTest {

    private fun applyResult(
        wallet: PlayerWallet,
        result: EconomyMutationResult
    ): PlayerWallet = when (result) {
        is EconomyMutationResult.Success -> result.updatedWallet
        is EconomyMutationResult.Rejected -> wallet
    }

    @Test
    fun `same_ad_transaction_credits_wallet_only_once`() {
        val start = PlayerWallet()
        val transactionId = "ad-tx-001"

        var wallet = applyResult(
            start,
            EconomyEngine.creditReward(
                wallet = start,
                goldDelta = 50,
                diamondDelta = 0,
                transactionId = transactionId,
                reason = "Rewarded ad"
            )
        )
        assertEquals(300, wallet.goldCoins)

        // A duplicate callback with the same transaction id must be ignored.
        val replay = EconomyEngine.creditReward(
            wallet = wallet,
            goldDelta = 50,
            diamondDelta = 0,
            transactionId = transactionId,
            reason = "Rewarded ad"
        )
        assertTrue(replay is EconomyMutationResult.Rejected)

        wallet = applyResult(wallet, replay)
        assertEquals(300, wallet.goldCoins)
        assertEquals(20, wallet.diamonds)
    }

    @Test
    fun `zero_gold_ad_reward_is_rejected_without_changing_wallet`() {
        val start = PlayerWallet()

        val result = EconomyEngine.creditReward(
            wallet = start,
            goldDelta = 0,
            diamondDelta = 0,
            transactionId = "ad-tx-zero",
            reason = "Rewarded ad"
        )

        assertTrue(result is EconomyMutationResult.Rejected)
        assertEquals(start, applyResult(start, result))
    }

    @Test
    fun `rejected_mutation_does_not_replace_wallet`() {
        val start = PlayerWallet(goldCoins = 10)

        val result = EconomyEngine.purchaseShopItem(
            wallet = start,
            itemId = "booster_hammer_1",
            transactionId = "ad-tx-broke"
        )

        assertTrue(result is EconomyMutationResult.Rejected)
        val wallet = applyResult(start, result)
        assertEquals(10, wallet.goldCoins)
        assertEquals(3, wallet.hammerCount)
        assertEquals(start, wallet)
    }

    @Test
    fun `diamond_ad_reward_credits_5_diamonds_once`() {
        val start = PlayerWallet(diamonds = 20)
        val transactionId = "ad-diamond-001"

        val first = EconomyEngine.creditReward(
            wallet = start,
            goldDelta = 0,
            diamondDelta = 5,
            transactionId = transactionId,
            reason = "Rewarded ad"
        )
        assertTrue(first is EconomyMutationResult.Success)
        val updated = applyResult(start, first)
        assertEquals(25, updated.diamonds)
        assertEquals(start.goldCoins, updated.goldCoins)

        val replay = EconomyEngine.creditReward(
            wallet = updated,
            goldDelta = 0,
            diamondDelta = 5,
            transactionId = transactionId,
            reason = "Rewarded ad"
        )
        assertTrue(replay is EconomyMutationResult.Rejected)
        assertEquals(25, applyResult(updated, replay).diamonds)
    }
}
