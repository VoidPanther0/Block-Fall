package com.example

import com.example.game.BoosterType
import com.example.game.EconomyEngine
import com.example.game.EconomyMutationResult
import com.example.game.PlayerWallet
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pure JVM tests for the economy engine. No mocks: these exercise the real wallet model and
 * the real idempotency ledger used by the ViewModel and repository.
 */
class EconomyEngineTest {

    private fun requireSuccess(result: EconomyMutationResult): EconomyMutationResult.Success {
        assertTrue("Expected Success but was $result", result is EconomyMutationResult.Success)
        return result as EconomyMutationResult.Success
    }

    @Test
    fun `purchase_shop_item_deducts_gold_and_grants_booster`() {
        val wallet = PlayerWallet()

        val result = requireSuccess(
            EconomyEngine.purchaseShopItem(
                wallet = wallet,
                itemId = "booster_hammer_1",
                transactionId = "tx-purchase-1"
            )
        )

        assertEquals(150, result.updatedWallet.goldCoins)
        assertEquals(4, result.updatedWallet.hammerCount)
        assertTrue("tx-purchase-1" in result.updatedWallet.processedLedgerIds)
    }

    @Test
    fun `purchase_shop_item_with_same_transaction_id_is_rejected`() {
        val first = requireSuccess(
            EconomyEngine.purchaseShopItem(
                wallet = PlayerWallet(),
                itemId = "booster_hammer_1",
                transactionId = "tx-duplicate"
            )
        )

        val second = EconomyEngine.purchaseShopItem(
            wallet = first.updatedWallet,
            itemId = "booster_hammer_1",
            transactionId = "tx-duplicate"
        )

        assertTrue(second is EconomyMutationResult.Rejected)
        // The first purchase stands; the duplicate never charges again.
        assertEquals(150, first.updatedWallet.goldCoins)
        assertEquals(4, first.updatedWallet.hammerCount)
    }

    @Test
    fun `purchase_shop_item_with_insufficient_gold_is_rejected`() {
        val poorWallet = PlayerWallet(goldCoins = 50)

        val result = EconomyEngine.purchaseShopItem(
            wallet = poorWallet,
            itemId = "booster_hammer_1",
            transactionId = "tx-insufficient"
        )

        assertTrue(result is EconomyMutationResult.Rejected)
    }

    @Test
    fun `consume_hammer_booster_decrements_count_by_one`() {
        val result = requireSuccess(
            EconomyEngine.consumeBooster(
                wallet = PlayerWallet(),
                type = BoosterType.HAMMER,
                transactionId = "tx-consume-hammer"
            )
        )

        assertEquals(2, result.updatedWallet.hammerCount)
        assertEquals(3, result.updatedWallet.refreshCount)
        assertEquals(3, result.updatedWallet.shuffleCount)
    }

    @Test
    fun `consume_booster_with_zero_count_is_rejected`() {
        val emptyWallet = PlayerWallet(hammerCount = 0)

        val result = EconomyEngine.consumeBooster(
            wallet = emptyWallet,
            type = BoosterType.HAMMER,
            transactionId = "tx-consume-empty"
        )

        assertTrue(result is EconomyMutationResult.Rejected)
    }

    @Test
    fun `credit_reward_adds_gold`() {
        val result = requireSuccess(
            EconomyEngine.creditReward(
                wallet = PlayerWallet(),
                goldDelta = 50,
                diamondDelta = 0,
                transactionId = "tx-reward-1",
                reason = "Rewarded ad"
            )
        )

        assertEquals(300, result.updatedWallet.goldCoins)
        assertEquals(20, result.updatedWallet.diamonds)
    }

    @Test
    fun `credit_reward_with_same_transaction_id_is_rejected`() {
        val first = requireSuccess(
            EconomyEngine.creditReward(
                wallet = PlayerWallet(),
                goldDelta = 50,
                diamondDelta = 0,
                transactionId = "tx-reward-duplicate",
                reason = "Rewarded ad"
            )
        )

        val replay = EconomyEngine.creditReward(
            wallet = first.updatedWallet,
            goldDelta = 50,
            diamondDelta = 0,
            transactionId = "tx-reward-duplicate",
            reason = "Rewarded ad"
        )

        assertTrue(replay is EconomyMutationResult.Rejected)
        assertEquals(300, first.updatedWallet.goldCoins)
    }

    @Test
    fun `player_wallet_defaults_are_starter_values`() {
        val wallet = PlayerWallet()

        assertEquals(250, wallet.goldCoins)
        assertEquals(20, wallet.diamonds)
        BoosterType.entries.forEach { type ->
            assertEquals(3, wallet.countFor(type))
        }
    }
}
