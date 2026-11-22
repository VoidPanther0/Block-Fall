package com.example.game

import kotlinx.serialization.Serializable

enum class BoosterType(
    val id: String,
    val title: String,
    val description: String,
    val goldPrice: Int,
    val starterCount: Int
) {
    HAMMER(
        id = "hammer",
        title = "Hammer",
        description = "Smash any single block on the board",
        goldPrice = 100,
        starterCount = 3
    ),
    REFRESH(
        id = "refresh",
        title = "Refresh",
        description = "Deal 3 brand new pieces",
        goldPrice = 100,
        starterCount = 3
    ),
    SHUFFLE(
        id = "shuffle",
        title = "Shuffle",
        description = "Reroll remaining tray pieces",
        goldPrice = 100,
        starterCount = 3
    );

    companion object {
        fun fromId(id: String?): BoosterType? = entries.firstOrNull { it.id == id }
    }
}

enum class ShopCategory(val title: String) {
    BOOSTERS("Boosters"),
    DIAMONDS("Gems"),
    GOLD("Coins")
}

data class ShopItem(
    val id: String,
    val category: ShopCategory,
    val title: String,
    val subtitle: String,
    val goldCost: Int = 0,
    val diamondCost: Int = 0,
    val rewardGold: Int = 0,
    val rewardDiamonds: Int = 0,
    val rewardBooster: BoosterType? = null,
    val rewardBoosterCount: Int = 0,
    val grantEntitlementId: String? = null,
    val badge: String? = null
)

object ShopCatalog {
    val items: List<ShopItem> = listOf(
        ShopItem(
            id = "booster_hammer_1",
            category = ShopCategory.BOOSTERS,
            title = "Hammer",
            subtitle = "Remove any 1 block",
            goldCost = 100,
            rewardBooster = BoosterType.HAMMER,
            rewardBoosterCount = 1
        ),
        ShopItem(
            id = "booster_refresh_1",
            category = ShopCategory.BOOSTERS,
            title = "Refresh",
            subtitle = "Deal 3 new pieces",
            goldCost = 100,
            rewardBooster = BoosterType.REFRESH,
            rewardBoosterCount = 1
        ),
        ShopItem(
            id = "booster_shuffle_1",
            category = ShopCategory.BOOSTERS,
            title = "Shuffle",
            subtitle = "Reroll active pieces",
            goldCost = 100,
            rewardBooster = BoosterType.SHUFFLE,
            rewardBoosterCount = 1
        ),
        ShopItem(
            id = "booster_trio_bundle",
            category = ShopCategory.BOOSTERS,
            title = "Tactician Trio",
            subtitle = "+2 Hammer, +2 Refresh, +2 Shuffle",
            goldCost = 500,
            rewardBoosterCount = 2,
            badge = "SAVE 17%"
        ),
        ShopItem(
            id = "exchange_gems_to_gold_small",
            category = ShopCategory.GOLD,
            title = "500 Gold Pouch",
            subtitle = "Convert 15 Diamonds into Gold",
            diamondCost = 15,
            rewardGold = 500
        ),
        ShopItem(
            id = "exchange_gems_to_gold_large",
            category = ShopCategory.GOLD,
            title = "1,500 Gold Chest",
            subtitle = "Convert 40 Diamonds into Gold",
            diamondCost = 40,
            rewardGold = 1500,
            badge = "BEST VALUE"
        ),
        ShopItem(
            id = "entitlement_prism_crown",
            category = ShopCategory.DIAMONDS,
            title = "Prism Crown Frame",
            subtitle = "Exclusive profile & rank crest",
            diamondCost = 30,
            grantEntitlementId = "cosmetic_prism_crown"
        ),
        ShopItem(
            id = "entitlement_cascade_aura",
            category = ShopCategory.DIAMONDS,
            title = "Cascade Aura FX",
            subtitle = "Enhanced gold cascade trail badge",
            diamondCost = 45,
            grantEntitlementId = "cosmetic_cascade_aura"
        )
    )

    fun findById(id: String): ShopItem? = items.firstOrNull { it.id == id }
}

@Serializable
data class PlayerWallet(
    val goldCoins: Int = DEFAULT_STARTER_GOLD,
    val diamonds: Int = DEFAULT_STARTER_DIAMONDS,
    val hammerCount: Int = BoosterType.HAMMER.starterCount,
    val refreshCount: Int = BoosterType.REFRESH.starterCount,
    val shuffleCount: Int = BoosterType.SHUFFLE.starterCount,
    val vipActive: Boolean = false,
    val vipPlanId: String = "none",
    val vipExpiresAtMillis: Long = 0L,
    val unlockedEntitlements: Set<String> = emptySet(),
    val processedLedgerIds: Set<String> = emptySet()
) {
    fun countFor(type: BoosterType): Int = when (type) {
        BoosterType.HAMMER -> hammerCount
        BoosterType.REFRESH -> refreshCount
        BoosterType.SHUFFLE -> shuffleCount
    }

    companion object {
        const val DEFAULT_STARTER_GOLD = 250
        const val DEFAULT_STARTER_DIAMONDS = 20
        const val MAX_GOLD = 10_000_000
        const val MAX_DIAMONDS = 1_000_000
        const val MAX_BOOSTER_STACK = 999
    }
}

sealed interface EconomyMutationResult {
    data class Success(
        val updatedWallet: PlayerWallet,
        val ledgerId: String,
        val message: String
    ) : EconomyMutationResult

    data class Rejected(val reason: String) : EconomyMutationResult
}

object EconomyEngine {

    fun calculateTurnRewardGold(
        linesCleared: Int,
        maxCascadeDepth: Int,
        fullBoardClear: Boolean
    ): Int {
        if (linesCleared <= 0 && !fullBoardClear) return 0
        val lineGold = linesCleared * 8
        val cascadeBonus = if (maxCascadeDepth >= 2) (maxCascadeDepth - 1) * 12 else 0
        val fullClearBonus = if (fullBoardClear) 40 else 0
        return (lineGold + cascadeBonus + fullClearBonus).coerceIn(0, 500)
    }

    fun calculateChallengeWinReward(stars: Int): Pair<Int, Int> {
        val clampedStars = stars.coerceIn(1, 3)
        val gold = 40 + clampedStars * 25
        val diamonds = clampedStars * 2
        return gold to diamonds
    }

    fun purchaseShopItem(
        wallet: PlayerWallet,
        itemId: String,
        transactionId: String
    ): EconomyMutationResult {
        if (transactionId.isBlank()) {
            return EconomyMutationResult.Rejected("Missing transaction ID")
        }
        if (transactionId in wallet.processedLedgerIds) {
            return EconomyMutationResult.Rejected("Duplicate transaction ignored")
        }
        val item = ShopCatalog.findById(itemId)
            ?: return EconomyMutationResult.Rejected("Unknown catalog item")

        if (item.grantEntitlementId != null && item.grantEntitlementId in wallet.unlockedEntitlements) {
            return EconomyMutationResult.Rejected("Item already owned")
        }
        if (wallet.goldCoins < item.goldCost) {
            return EconomyMutationResult.Rejected("Not enough Gold coins")
        }
        if (wallet.diamonds < item.diamondCost) {
            return EconomyMutationResult.Rejected("Not enough Diamonds")
        }

        val nextGold = (wallet.goldCoins - item.goldCost + item.rewardGold)
            .coerceIn(0, PlayerWallet.MAX_GOLD)
        val nextDiamonds = (wallet.diamonds - item.diamondCost + item.rewardDiamonds)
            .coerceIn(0, PlayerWallet.MAX_DIAMONDS)

        var nextHammer = wallet.hammerCount
        var nextRefresh = wallet.refreshCount
        var nextShuffle = wallet.shuffleCount

        when {
            item.id == "booster_trio_bundle" -> {
                nextHammer = (nextHammer + item.rewardBoosterCount).coerceAtMost(PlayerWallet.MAX_BOOSTER_STACK)
                nextRefresh = (nextRefresh + item.rewardBoosterCount).coerceAtMost(PlayerWallet.MAX_BOOSTER_STACK)
                nextShuffle = (nextShuffle + item.rewardBoosterCount).coerceAtMost(PlayerWallet.MAX_BOOSTER_STACK)
            }
            item.rewardBooster == BoosterType.HAMMER -> {
                nextHammer = (nextHammer + item.rewardBoosterCount).coerceAtMost(PlayerWallet.MAX_BOOSTER_STACK)
            }
            item.rewardBooster == BoosterType.REFRESH -> {
                nextRefresh = (nextRefresh + item.rewardBoosterCount).coerceAtMost(PlayerWallet.MAX_BOOSTER_STACK)
            }
            item.rewardBooster == BoosterType.SHUFFLE -> {
                nextShuffle = (nextShuffle + item.rewardBoosterCount).coerceAtMost(PlayerWallet.MAX_BOOSTER_STACK)
            }
        }

        val nextEntitlements = if (item.grantEntitlementId != null) {
            wallet.unlockedEntitlements + item.grantEntitlementId
        } else {
            wallet.unlockedEntitlements
        }

        val updated = wallet.copy(
            goldCoins = nextGold,
            diamonds = nextDiamonds,
            hammerCount = nextHammer,
            refreshCount = nextRefresh,
            shuffleCount = nextShuffle,
            unlockedEntitlements = nextEntitlements,
            processedLedgerIds = (wallet.processedLedgerIds + transactionId).takeLastSet(64)
        )
        return EconomyMutationResult.Success(
            updatedWallet = updated,
            ledgerId = transactionId,
            message = "Purchased ${item.title}"
        )
    }

    fun consumeBooster(
        wallet: PlayerWallet,
        type: BoosterType,
        transactionId: String
    ): EconomyMutationResult {
        if (transactionId.isBlank()) {
            return EconomyMutationResult.Rejected("Missing transaction ID")
        }
        if (transactionId in wallet.processedLedgerIds) {
            return EconomyMutationResult.Rejected("Duplicate booster consumption ignored")
        }
        if (wallet.countFor(type) <= 0) {
            return EconomyMutationResult.Rejected("No ${type.title} boosters left")
        }

        val updated = when (type) {
            BoosterType.HAMMER -> wallet.copy(
                hammerCount = (wallet.hammerCount - 1).coerceAtLeast(0),
                processedLedgerIds = (wallet.processedLedgerIds + transactionId).takeLastSet(64)
            )
            BoosterType.REFRESH -> wallet.copy(
                refreshCount = (wallet.refreshCount - 1).coerceAtLeast(0),
                processedLedgerIds = (wallet.processedLedgerIds + transactionId).takeLastSet(64)
            )
            BoosterType.SHUFFLE -> wallet.copy(
                shuffleCount = (wallet.shuffleCount - 1).coerceAtLeast(0),
                processedLedgerIds = (wallet.processedLedgerIds + transactionId).takeLastSet(64)
            )
        }
        return EconomyMutationResult.Success(
            updatedWallet = updated,
            ledgerId = transactionId,
            message = "Used ${type.title}"
        )
    }

    fun creditReward(
        wallet: PlayerWallet,
        goldDelta: Int,
        diamondDelta: Int,
        transactionId: String,
        reason: String
    ): EconomyMutationResult {
        if (transactionId.isBlank()) {
            return EconomyMutationResult.Rejected("Missing transaction ID")
        }
        if (transactionId in wallet.processedLedgerIds) {
            return EconomyMutationResult.Rejected("Reward already claimed")
        }
        val safeGold = goldDelta.coerceIn(0, 50_000)
        val safeDiamonds = diamondDelta.coerceIn(0, 5_000)
        if (safeGold == 0 && safeDiamonds == 0) {
            return EconomyMutationResult.Rejected("Empty reward")
        }
        val updated = wallet.copy(
            goldCoins = (wallet.goldCoins + safeGold).coerceAtMost(PlayerWallet.MAX_GOLD),
            diamonds = (wallet.diamonds + safeDiamonds).coerceAtMost(PlayerWallet.MAX_DIAMONDS),
            processedLedgerIds = (wallet.processedLedgerIds + transactionId).takeLastSet(64)
        )
        return EconomyMutationResult.Success(
            updatedWallet = updated,
            ledgerId = transactionId,
            message = reason
        )
    }

    private fun Set<String>.takeLastSet(maxSize: Int): Set<String> {
        if (size <= maxSize) return this
        return toList().takeLast(maxSize).toSet()
    }
}
