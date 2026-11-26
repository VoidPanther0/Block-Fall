package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.ShopCatalog
import com.example.game.ShopItem
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.BlockFallBottomNav
import com.example.ui.components.BottomNavTab
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.CrystalCoinBadge
import com.example.ui.components.CrystalDiamondBadge
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.PremiumCard
import com.example.ui.components.ScreenTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentDiamond
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.HairlineDark
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PsCommerce
import com.example.ui.theme.SurfaceDarkCard
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private data class StorePack(val title: String, val subtitle: String, val pricePlaceholder: String)

private val DiamondPacks = listOf(
    StorePack("Diamond Pack S", "100 Crystals · Instant Delivery", "$0.99"),
    StorePack("Diamond Pack M", "350 Crystals · 20% Extra Bonus", "$2.99"),
    StorePack("Diamond Pack L", "800 Crystals · 40% Extra Bonus", "$5.99")
)
private val GoldPacks = listOf(
    StorePack("Gold Pack S", "2,500 Coins · Starter Cache", "$0.99"),
    StorePack("Gold Pack M", "8,000 Coins · Popular Choice", "$2.99"),
    StorePack("Gold Pack L", "20,000 Coins · Vault Reserve", "$5.99")
)

@Composable
fun ShopScreen(
    state: GameUiState,
    onBack: () -> Unit,
    diamondAdReady: Boolean,
    goldAdReady: Boolean,
    message: String?,
    onWatchDiamondAd: () -> Unit,
    onWatchGoldAd: () -> Unit,
    onEnsureAdsLoaded: () -> Unit = {},
    onPurchaseItem: (String) -> Unit = {},
    onOpenTab: ((BottomNavTab) -> Unit)? = null
) {
    LaunchedEffect(Unit) { onEnsureAdsLoaded() }

    CascadeScreenContainer(
        maxContentWidth = 820.dp,
        footerBar = if (onOpenTab != null) {
            { BlockFallBottomNav(selectedTab = BottomNavTab.SHOP, onSelectTab = onOpenTab) }
        } else null,
        modifier = Modifier.testTag("shop_screen")
    ) {
        ScreenTopBar(title = "Shop", onBack = onBack, backTestTag = "shop_back_button")

        // Top Wallet Bar
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("shop_wallet_bar")
        ) {
            WalletTile(
                label = "GOLD",
                amount = state.wallet.goldCoins,
                badge = { CrystalCoinBadge(size = 28.dp) },
                accent = AccentGold,
                tag = "shop_gold_balance",
                modifier = Modifier.weight(1f)
            )
            WalletTile(
                label = "DIAMONDS",
                amount = state.wallet.diamonds,
                badge = { CrystalDiamondBadge(size = 28.dp) },
                accent = AccentDiamond,
                tag = "shop_diamond_balance",
                modifier = Modifier.weight(1f)
            )
        }

        if (message != null) {
            Spacer(Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentEmerald.copy(alpha = 0.18f))
                    .border(1.2.dp, AccentEmerald.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
                    .testTag("shop_message")
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextPrimary
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Free Rewards (Ad Watch)
            SectionHeader(kicker = "Free Daily Rewards", title = "Watch Videos", accent = AccentEmerald)
            AdRewardCard(
                badge = { CrystalDiamondBadge(size = 26.dp) },
                accent = AccentDiamond,
                title = "Free Diamonds",
                subtitle = "Watch a short ad to earn +5 Diamonds",
                buttonText = if (diamondAdReady) "Watch Ad · +5 Diamonds" else "Prepare Ad · +5 Diamonds",
                enabled = true,
                onClick = {
                    if (diamondAdReady) onWatchDiamondAd() else {
                        onEnsureAdsLoaded()
                        onWatchDiamondAd()
                    }
                },
                tag = "shop_watch_diamond_ad_button"
            )
            AdRewardCard(
                badge = { CrystalCoinBadge(size = 26.dp) },
                accent = AccentGold,
                title = "Free Gold",
                subtitle = "Watch a short ad to earn +50 Gold",
                buttonText = if (goldAdReady) "Watch Ad · +50 Gold" else "Prepare Ad · +50 Gold",
                enabled = true,
                onClick = {
                    if (goldAdReady) onWatchGoldAd() else {
                        onEnsureAdsLoaded()
                        onWatchGoldAd()
                    }
                },
                tag = "shop_watch_ad_button"
            )

            // In-Game Catalog Items
            SectionHeader(kicker = "In-Game Deals", title = "Boosters & Currency Exchange", accent = AccentCyan)
            ShopCatalog.items.forEach { item ->
                ShopCatalogCard(
                    item = item,
                    walletGold = state.wallet.goldCoins,
                    walletDiamonds = state.wallet.diamonds,
                    unlockedEntitlements = state.wallet.unlockedEntitlements,
                    onBuy = { onPurchaseItem(item.id) }
                )
            }

            // Real-Money Store Packs (Placeholder priced Coming Soon for IAP)
            SectionHeader(kicker = "Premium Bundles", title = "Diamond Packages", accent = AccentDiamond)
            DiamondPacks.forEach { pack ->
                ComingSoonPackCard(badge = { CrystalDiamondBadge(size = 24.dp) }, accent = AccentDiamond, pack = pack)
            }

            SectionHeader(kicker = "Gold Vault", title = "Gold Packages", accent = AccentGold)
            GoldPacks.forEach { pack ->
                ComingSoonPackCard(badge = { CrystalCoinBadge(size = 24.dp) }, accent = AccentGold, pack = pack)
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ShopCatalogCard(
    item: ShopItem,
    walletGold: Int,
    walletDiamonds: Int,
    unlockedEntitlements: Set<String>,
    onBuy: () -> Unit
) {
    val isOwned = item.grantEntitlementId != null && item.grantEntitlementId in unlockedEntitlements
    val canAfford = when {
        isOwned -> false
        item.goldCost > 0 -> walletGold >= item.goldCost
        item.diamondCost > 0 -> walletDiamonds >= item.diamondCost
        else -> true
    }

    val icon: ImageVector = when {
        item.rewardBooster != null -> Icons.Filled.Shuffle
        item.rewardGold > 0 -> Icons.Filled.MonetizationOn
        item.grantEntitlementId == "cosmetic_prism_crown" -> Icons.Filled.WorkspacePremium
        else -> Icons.Filled.AutoAwesome
    }
    val accent = if (item.diamondCost > 0) AccentDiamond else AccentGold

    FrostedGlassCard(
        cornerRadius = 14.dp,
        borderColor = if (isOwned) AccentEmerald.copy(alpha = 0.45f) else HairlineDark,
        modifier = Modifier.fillMaxWidth()
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(accent.copy(alpha = 0.16f))
                    .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
            ) {
                Icon(icon, null, tint = accent, modifier = Modifier.size(24.dp))
            }
            Column(Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        item.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                    if (item.badge != null) {
                        Text(
                            text = item.badge,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Black,
                                fontSize = 9.sp
                            ),
                            color = AccentGold,
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(AccentGold.copy(alpha = 0.16f))
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    item.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            when {
                isOwned -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AccentEmerald.copy(alpha = 0.16f))
                            .border(1.dp, AccentEmerald.copy(alpha = 0.5f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(Icons.Filled.Check, null, tint = AccentEmerald, modifier = Modifier.size(14.dp))
                        Text("Owned", style = MaterialTheme.typography.labelSmall, color = AccentEmerald)
                    }
                }
                else -> {
                    val priceLabel = if (item.goldCost > 0) "${item.goldCost} Gold" else "${item.diamondCost} Gems"
                    GlossyArcadeButton(
                        text = priceLabel,
                        style = ArcadeButtonStyle.COMMERCE,
                        enabled = canAfford,
                        onClick = onBuy,
                        height = 40.dp,
                        cornerRadius = 12.dp,
                        fontSize = 12.sp,
                        modifier = Modifier.width(116.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ComingSoonPackCard(badge: @Composable () -> Unit, accent: Color, pack: StorePack) {
    FrostedGlassCard(
        cornerRadius = 14.dp,
        borderColor = HairlineDark,
        modifier = Modifier.fillMaxWidth()
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
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.14f))
                    .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            ) {
                badge()
            }
            Column(Modifier.weight(1f)) {
                Text(
                    pack.title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Text(
                    "${pack.subtitle} · ${pack.pricePlaceholder}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            GlossyArcadeButton(
                text = "Coming Soon",
                icon = Icons.Filled.Lock,
                style = ArcadeButtonStyle.SLATE,
                enabled = false,
                onClick = {},
                height = 40.dp,
                cornerRadius = 12.dp,
                fontSize = 11.sp,
                modifier = Modifier.width(116.dp)
            )
        }
    }
}

@Composable
private fun AdRewardCard(
    badge: @Composable () -> Unit,
    accent: Color,
    title: String,
    subtitle: String,
    buttonText: String,
    enabled: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    FrostedGlassCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 14.dp,
        borderColor = HairlineDark
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(accent.copy(alpha = 0.16f))
                        .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                ) {
                    badge()
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = TextPrimary
                    )
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            GlossyArcadeButton(
                text = buttonText,
                style = ArcadeButtonStyle.BLUE,
                enabled = enabled,
                onClick = onClick,
                height = 48.dp,
                cornerRadius = 14.dp,
                fontSize = 14.sp,
                testTag = tag,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun WalletTile(
    label: String,
    amount: Int,
    badge: @Composable () -> Unit,
    accent: Color,
    tag: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
            .clip(shape)
            .background(SurfaceDarkCard)
            .border(1.dp, HairlineDark, shape)
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag(tag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.14f))
                .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
        ) {
            badge()
        }
        Column(Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                ),
                color = accent,
                maxLines = 1
            )
            Text(
                text = "%,d".format(amount),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = if (amount >= 100_000) 16.sp else 19.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
