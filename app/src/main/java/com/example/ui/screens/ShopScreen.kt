package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.BoosterType
import com.example.game.ShopCatalog
import com.example.game.ShopCategory
import com.example.game.ShopItem
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Economy storefront: boosters, gem-to-gold exchange, cosmetics and the rewarded-ad
 * gold faucet. All mutations are delegated to the ViewModel so [com.example.game.EconomyEngine]
 * remains the single source of truth for balance changes and idempotency.
 */
@Composable
fun ShopScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onPurchase: (String) -> Unit,
    onWatchAd: (() -> Unit)? = null
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val categories = ShopCategory.entries
    val safeTab = selectedTab.coerceIn(categories.indices)
    val category = categories[safeTab]
    val visibleItems = ShopCatalog.items.filter { it.category == category }

    CascadeScreenContainer {
        ScreenTopBar(title = "Shop", onBack = onBack)

        FrostedGlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("shop_wallet_bar"),
            cornerRadius = 18.dp,
            borderColor = AccentGold.copy(alpha = 0.32f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                WalletBalance(
                    icon = Icons.Filled.MonetizationOn,
                    tint = AccentGold,
                    label = "Gold",
                    amount = state.wallet.goldCoins,
                    testTag = "shop_gold_balance"
                )
                Box(
                    Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(GlassBorder.copy(alpha = 0.55f))
                )
                WalletBalance(
                    icon = Icons.Filled.Diamond,
                    tint = AccentCyan,
                    label = "Gems",
                    amount = state.wallet.diamonds,
                    testTag = "shop_diamond_balance"
                )
            }
        }

        Spacer(Modifier.height(14.dp))

        TabRow(
            selectedTabIndex = safeTab,
            containerColor = SurfaceCard.copy(alpha = 0.65f),
            contentColor = AccentGold
        ) {
            categories.forEachIndexed { index, shopCategory ->
                Tab(
                    selected = safeTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = shopCategory.title,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = if (safeTab == index) AccentGold else TextSecondary
                        )
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (category == ShopCategory.BOOSTERS) {
                item(key = "booster_inventory") {
                    BoosterInventoryCard(state)
                }
            }

            if (category == ShopCategory.GOLD) {
                item(key = "free_gold") {
                    FreeGoldCard(onWatchAd)
                }
            }

            items(visibleItems, key = { it.id }) { item ->
                ShopItemCard(
                    item = item,
                    state = state,
                    onPurchase = onPurchase
                )
            }
        }
    }
}

@Composable
private fun WalletBalance(
    icon: ImageVector,
    tint: Color,
    label: String,
    amount: Int,
    testTag: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(26.dp)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = label.uppercase(),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                ),
                color = TextSecondary
            )
            Text(
                text = amount.toString(),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp
                ),
                color = TextPrimary,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}

@Composable
private fun BoosterInventoryCard(state: GameUiState) {
    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        borderColor = AccentCyan.copy(alpha = 0.30f)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Your Boosters",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                BoosterType.entries.forEach { type ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = type.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = "×${state.wallet.countFor(type)}",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold
                            ),
                            color = AccentCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FreeGoldCard(onWatchAd: (() -> Unit)?) {
    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        borderColor = AccentEmerald.copy(alpha = 0.32f)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Free Gold",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Text(
                text = if (onWatchAd != null) {
                    "Watch a short ad to earn +50 Gold"
                } else {
                    "Ad is loading — check back in a moment"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Spacer(Modifier.height(10.dp))
            GlossyArcadeButton(
                text = if (onWatchAd != null) "Watch Ad · +50 Gold" else "Ad Not Ready",
                style = ArcadeButtonStyle.GREEN,
                enabled = onWatchAd != null,
                onClick = { onWatchAd?.invoke() },
                height = 46.dp,
                fontSize = 14.sp,
                testTag = "shop_watch_ad_button",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ShopItemCard(
    item: ShopItem,
    state: GameUiState,
    onPurchase: (String) -> Unit
) {
    val alreadyOwned = item.grantEntitlementId
        ?.let { it in state.wallet.unlockedEntitlements } == true
    val canAfford = (item.goldCost <= 0 || state.wallet.goldCoins >= item.goldCost) &&
        (item.diamondCost <= 0 || state.wallet.diamonds >= item.diamondCost)
    val purchasable = !alreadyOwned && canAfford

    val priceLabel = when {
        alreadyOwned -> "Owned"
        item.goldCost > 0 -> "${item.goldCost} Gold"
        item.diamondCost > 0 -> "${item.diamondCost} Gems"
        else -> "Free"
    }

    FrostedGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shop_item_${item.id}"),
        cornerRadius = 18.dp,
        borderColor = if (item.diamondCost > 0) {
            AccentCyan.copy(alpha = 0.30f)
        } else {
            AccentGold.copy(alpha = 0.26f)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                if (item.badge != null) {
                    Text(
                        text = item.badge,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AccentGold
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            GlossyArcadeButton(
                text = priceLabel,
                style = if (item.diamondCost > 0) ArcadeButtonStyle.BLUE else ArcadeButtonStyle.GOLD,
                enabled = purchasable,
                onClick = { onPurchase(item.id) },
                height = 44.dp,
                fontSize = 13.sp,
                testTag = "shop_buy_button_${item.id}"
            )
        }
    }
}
