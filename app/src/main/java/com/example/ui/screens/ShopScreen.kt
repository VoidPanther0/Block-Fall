package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.BlockStyle
import com.example.game.BoardColorPalette
import com.example.game.BoosterType
import com.example.game.ShopCatalog
import com.example.game.ShopCategory
import com.example.game.ShopItem
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.CurrencyWallet
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.components.ScreenTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentDiamond
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.CascadeTextStyles
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private enum class StoreSection(val label: String) {
    ABILITIES("Abilities"),
    DIAMONDS("Diamonds"),
    GOLD("Gold"),
    VIP_TEAMS("VIP & Teams")
}

/** Offers that grant boosters the current gameplay loop cannot consume yet. */
private val HIDDEN_OFFER_IDS = setOf(
    "booster_hammer_1",
    "booster_refresh_1",
    // This bundle also grants Hammer + Refresh, which the current gameplay loop cannot use.
    "booster_trio_bundle"
)

/**
 * Premium game storefront. Block Skins are intentionally hidden for this release.
 * Prices and purchase behaviour are driven entirely by the existing [ShopCatalog].
 */
@Composable
fun ShopScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onPurchase: (String) -> Unit,
    onWatchAd: (() -> Unit)? = null,
    onSelectBlockStyle: ((BlockStyle) -> Unit)? = null,
    onSelectBoardPalette: ((BoardColorPalette) -> Unit)? = null
) {
    var selectedSection by remember { mutableStateOf(StoreSection.DIAMONDS) }

    val visibleItems = remember {
        ShopCatalog.items.filterNot { it.id in HIDDEN_OFFER_IDS }
    }
    val boosterItems = remember { visibleItems.filter { it.category == ShopCategory.BOOSTERS } }
    val diamondItems = remember { visibleItems.filter { it.category == ShopCategory.DIAMONDS } }
    val goldItems = remember { visibleItems.filter { it.category == ShopCategory.GOLD } }

    CascadeScreenContainer(
        maxContentWidth = 820.dp,
        modifier = Modifier.testTag("shop_screen")
    ) {
        ScreenTopBar(title = "Shop", onBack = onBack, backTestTag = "shop_back_button")

        PremiumCard(
            cornerRadius = 18.dp,
            borderColor = AccentGold.copy(alpha = 0.38f),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("shop_wallet_bar")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "YOUR WALLET",
                        style = CascadeTextStyles.SectionKicker,
                        color = AccentGold,
                        maxLines = 1
                    )
                    Text(
                        text = "Earn more by playing & challenges",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                CurrencyWallet(
                    goldCoins = state.wallet.goldCoins,
                    diamonds = state.wallet.diamonds,
                    goldTag = "shop_gold_balance",
                    diamondTag = "shop_diamond_balance",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StoreSection.entries.forEach { section ->
                val selected = section == selectedSection
                val shape = RoundedCornerShape(12.dp)
                val accent = when (section) {
                    StoreSection.DIAMONDS -> AccentDiamond
                    StoreSection.GOLD -> AccentGold
                    StoreSection.VIP_TEAMS -> AccentPurple
                    else -> AccentCyan
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(shape)
                        .background(if (selected) accent.copy(alpha = 0.12f) else SurfaceCard)
                        .border(
                            width = if (selected) 1.5.dp else 1.dp,
                            color = if (selected) accent else GlassBorder,
                            shape = shape
                        )
                        .clickable { selectedSection = section }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                        .testTag("shop_tab_${section.name.lowercase()}")
                ) {
                    Text(
                        text = section.label,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                        ),
                        color = if (selected) accent else TextSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (selectedSection) {
                StoreSection.ABILITIES -> {
                    item(key = "booster_inventory") { BoosterInventoryCard(state) }
                    items(boosterItems, key = { it.id }) { item ->
                        ShopItemCard(item = item, state = state, onPurchase = onPurchase)
                    }
                }

                StoreSection.DIAMONDS -> {
                    item(key = "diamond_packs_hdr") {
                        SectionHeader(
                            kicker = "Premium Currency",
                            title = "Diamond Packs",
                            accent = AccentDiamond
                        )
                    }
                    item(key = "diamond_packs_preview") {
                        StorePreviewCard(
                            icon = Icons.Filled.Diamond,
                            accent = AccentDiamond,
                            title = "Gem Bundles",
                            subtitle = "Direct packs open with store billing in a future release. Earn Diamonds today through 3-star Challenges.",
                            testTag = "shop_diamond_packs_preview"
                        )
                    }
                    items(diamondItems, key = { it.id }) { item ->
                        ShopItemCard(item = item, state = state, onPurchase = onPurchase)
                    }
                }

                StoreSection.GOLD -> {
                    item(key = "free_gold") { FreeGoldCard(onWatchAd) }
                    item(key = "gold_exchange_hdr") {
                        SectionHeader(
                            kicker = "Currency Exchange",
                            title = "Convert Diamonds to Gold",
                            accent = AccentGold
                        )
                    }
                    items(goldItems, key = { it.id }) { item ->
                        ShopItemCard(item = item, state = state, onPurchase = onPurchase)
                    }
                }

                StoreSection.VIP_TEAMS -> {
                    item(key = "vip_hdr") {
                        SectionHeader(kicker = "Membership", title = "VIP Pass", accent = AccentGold)
                    }
                    item(key = "vip_monthly") {
                        VipMembershipCard(
                            title = "Monthly VIP Pass",
                            benefits = listOf(
                                "Ad-free experience across all modes",
                                "Bonus daily login Gold & Diamonds",
                                "Exclusive golden profile halo"
                            )
                        )
                    }
                    item(key = "vip_yearly") {
                        VipMembershipCard(
                            title = "Annual VIP Pass",
                            benefits = listOf(
                                "All Monthly VIP perks included",
                                "Priority leaderboard badge",
                                "Seasonal booster drop rewards"
                            )
                        )
                    }
                    item(key = "teams_hdr") {
                        SectionHeader(kicker = "Social Clubs", title = "Team Pass", accent = AccentCyan)
                    }
                    item(key = "teams_preview") {
                        StorePreviewCard(
                            icon = Icons.Filled.Groups,
                            accent = AccentCyan,
                            title = "Team Pass",
                            subtitle = "Team leaderboards and cooperative cascade goals arrive in an upcoming cloud season.",
                            testTag = "shop_team_pass_preview"
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BoosterInventoryCard(state: GameUiState) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        borderColor = AccentCyan.copy(alpha = 0.34f)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "Your Ability Stockpile",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = TextPrimary
            )
            Spacer(Modifier.height(10.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val availableTypes = listOf(BoosterType.SHUFFLE)
                availableTypes.forEach { type ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(BoardWellBg)
                            .border(1.dp, GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(14.dp))
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Shuffle,
                            contentDescription = type.title,
                            tint = AccentCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = type.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "×${state.wallet.countFor(type)}",
                            style = CascadeTextStyles.NumericBalance.copy(fontSize = 16.sp),
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
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        borderColor = AccentEmerald.copy(alpha = 0.38f)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(AccentEmerald.copy(alpha = 0.16f))
                ) {
                    Icon(Icons.Filled.PlayCircle, null, tint = AccentEmerald, modifier = Modifier.size(22.dp))
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Free Gold Reward",
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
                }
            }
            Spacer(Modifier.height(12.dp))
            PremiumPrimaryButton(
                text = if (onWatchAd != null) "Watch Ad · +50 Gold" else "Ad Not Ready",
                style = ArcadeButtonStyle.GREEN,
                enabled = onWatchAd != null,
                onClick = { onWatchAd?.invoke() },
                height = 48.dp,
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
    val accent = if (item.diamondCost > 0) AccentDiamond else AccentGold
    val itemIcon = when {
        item.rewardBooster == BoosterType.SHUFFLE -> Icons.Filled.Shuffle
        item.id == "booster_trio_bundle" -> Icons.Filled.Bolt
        item.rewardGold > 0 -> Icons.Filled.MonetizationOn
        else -> Icons.Filled.Diamond
    }

    PremiumCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("shop_item_${item.id}"),
        cornerRadius = 18.dp,
        borderColor = accent.copy(alpha = 0.34f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(Brush.verticalGradient(listOf(accent.copy(alpha = 0.22f), accent.copy(alpha = 0.08f))))
                    .border(1.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(13.dp))
            ) {
                Icon(itemIcon, null, tint = accent, modifier = Modifier.size(23.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.badge != null) {
                        Text(
                            text = item.badge,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                            color = AccentGold,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(AccentGold.copy(alpha = 0.14f))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            PremiumPrimaryButton(
                text = priceLabel,
                style = if (item.diamondCost > 0) ArcadeButtonStyle.BLUE else ArcadeButtonStyle.GOLD,
                enabled = purchasable,
                onClick = { onPurchase(item.id) },
                height = 44.dp,
                cornerRadius = 12.dp,
                fontSize = 12.sp,
                testTag = "shop_buy_button_${item.id}"
            )
        }
    }
}

@Composable
private fun VipMembershipCard(title: String, benefits: List<String>) {
    PremiumCard(
        cornerRadius = 20.dp,
        borderColor = AccentGold.copy(alpha = 0.42f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(Icons.Filled.WorkspacePremium, null, tint = AccentGold, modifier = Modifier.size(24.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, AccentGold.copy(alpha = 0.5f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "COMING SOON",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                        color = AccentGold
                    )
                }
            }
            benefits.forEach { perk ->
                Text(text = "• $perk", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            PremiumPrimaryButton(
                text = "Coming Soon · Google Play Billing",
                icon = Icons.Filled.Lock,
                style = ArcadeButtonStyle.SLATE,
                enabled = false,
                onClick = {},
                height = 44.dp,
                cornerRadius = 12.dp,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StorePreviewCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String,
    testTag: String? = null
) {
    PremiumCard(
        cornerRadius = 18.dp,
        borderColor = accent.copy(alpha = 0.34f),
        modifier = Modifier.fillMaxWidth().then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.14f))
                    .border(1.dp, accent.copy(alpha = 0.36f), RoundedCornerShape(12.dp))
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    maxLines = 3
                )
            }
        }
    }
}
