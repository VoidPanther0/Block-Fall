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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private data class StorePack(val title: String, val subtitle: String)

private val DiamondPacks = listOf(
    StorePack("Diamond Pack S", "Small pack of Diamonds"),
    StorePack("Diamond Pack M", "Medium pack of Diamonds"),
    StorePack("Diamond Pack L", "Large pack of Diamonds")
)
private val GoldPacks = listOf(
    StorePack("Gold Pack S", "Small pack of Gold"),
    StorePack("Gold Pack M", "Medium pack of Gold"),
    StorePack("Gold Pack L", "Large pack of Gold")
)

/**
 * All-in-one shop on a single page:
 * 1. Diamond packs (Google Play Billing, coming soon)
 * 2. Gold packs (Google Play Billing, coming soon)
 * 3. Watch-ad rewards: +5 Diamonds and +50 Gold (the Lucky Wheel lives on the Home screen).
 * Ad rewards are credited by the server for signed-in players.
 */
@Composable
fun ShopScreen(
    state: GameUiState,
    onBack: () -> Unit,
    diamondAdReady: Boolean,
    goldAdReady: Boolean,
    message: String?,
    onWatchDiamondAd: () -> Unit,
    onWatchGoldAd: () -> Unit
) {
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
            Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
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

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            SectionHeader(kicker = "Premium Currency", title = "Diamond Packs", accent = AccentDiamond)
            DiamondPacks.forEach { pack ->
                ComingSoonPackCard(Icons.Filled.Diamond, AccentDiamond, pack)
            }

            SectionHeader(kicker = "Coins", title = "Gold Packs", accent = AccentGold)
            GoldPacks.forEach { pack ->
                ComingSoonPackCard(Icons.Filled.MonetizationOn, AccentGold, pack)
            }

            SectionHeader(kicker = "Free Rewards", title = "Watch Ads", accent = AccentEmerald)
            AdRewardCard(
                icon = Icons.Filled.Diamond,
                accent = AccentDiamond,
                title = "Free Diamonds",
                subtitle = "Watch a short ad to earn +5 Diamonds",
                buttonText = if (diamondAdReady) "Watch Ad · +5 Diamonds" else "Ad Loading...",
                enabled = diamondAdReady,
                onClick = onWatchDiamondAd,
                tag = "shop_watch_diamond_ad_button"
            )
            AdRewardCard(
                icon = Icons.Filled.PlayCircle,
                accent = AccentGold,
                title = "Free Gold",
                subtitle = "Watch a short ad to earn +50 Gold",
                buttonText = if (goldAdReady) "Watch Ad · +50 Gold" else "Ad Loading...",
                enabled = goldAdReady,
                onClick = onWatchGoldAd,
                tag = "shop_watch_ad_button"
            )

            if (message != null) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.testTag("shop_message")
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ComingSoonPackCard(icon: ImageVector, accent: Color, pack: StorePack) {
    PremiumCard(
        cornerRadius = 18.dp,
        borderColor = accent.copy(alpha = 0.34f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon, accent)
            Column(Modifier.weight(1f)) {
                Text(pack.title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                Text(pack.subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
            PremiumPrimaryButton(
                text = "Coming Soon",
                icon = Icons.Filled.Lock,
                style = ArcadeButtonStyle.SLATE,
                enabled = false,
                onClick = {},
                height = 44.dp,
                cornerRadius = 12.dp,
                fontSize = 11.sp
            )
        }
    }
}

@Composable
private fun AdRewardCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    subtitle: String,
    buttonText: String,
    enabled: Boolean,
    onClick: () -> Unit,
    tag: String
) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        borderColor = accent.copy(alpha = 0.38f)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                IconBadge(icon, accent)
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold), color = TextPrimary)
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                }
            }
            PremiumPrimaryButton(
                text = buttonText,
                style = ArcadeButtonStyle.GREEN,
                enabled = enabled,
                onClick = onClick,
                height = 48.dp,
                fontSize = 14.sp,
                testTag = tag,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector, accent: Color) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.16f))
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
    ) {
        Icon(icon, null, tint = accent, modifier = Modifier.size(22.dp))
    }
}
