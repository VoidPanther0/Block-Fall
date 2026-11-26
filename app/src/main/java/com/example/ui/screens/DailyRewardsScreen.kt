package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.DailyChallengeGenerator
import com.example.game.DailyRewardTier
import com.example.game.DailyRewardsCatalog
import com.example.ui.GameUiState
import com.example.ui.components.AnimatedTreasureChest3D
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.ChestOpenCelebrationDialog
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GameAssetCatalog
import com.example.ui.components.GameAssetIcon
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.GoldMetallicBanner
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldBright
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun DailyRewardsScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onClaimReward: () -> Unit
) {
    val todayKey = DailyChallengeGenerator.todayDateKey()
    val progress = state.dailyRewardProgress
    val canClaim = progress.canClaimToday(todayKey)
    val daysClaimedInCycle = progress.daysClaimedInCycle(todayKey)
    val nextRewardDay = progress.nextRewardDay(todayKey)
    var claimedDialogTier by remember { mutableStateOf<DailyRewardTier?>(null) }
    var showChestPreview by remember { mutableStateOf(false) }

    CascadeScreenContainer(
        maxContentWidth = 640.dp,
        modifier = Modifier.testTag("daily_rewards_screen")
    ) {
        ScreenTopBar(
            title = "Daily Rewards",
            onBack = onBack,
            backTestTag = "daily_rewards_back_button"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Banner
            GoldMetallicBanner(
                text = "7-DAY REWARD CALENDAR",
                isPurple = true,
                modifier = Modifier.fillMaxWidth()
            )

            Text(
                text = if (canClaim) "Log in daily to claim crystals, gold, and 3D treasure chests!"
                else "Today's reward is claimed! Return tomorrow for Day $nextRewardDay.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 12.dp)
            )

            // Days 1-6 Grid (2 rows of 3)
            val normalTiers = DailyRewardsCatalog.tiers.filter { !it.isGrandPrize }
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    normalTiers.take(3).forEach { tier ->
                        DailyRewardTile(
                            tier = tier,
                            isClaimed = tier.day <= daysClaimedInCycle,
                            isReadyToday = canClaim && tier.day == nextRewardDay,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    normalTiers.drop(3).take(3).forEach { tier ->
                        DailyRewardTile(
                            tier = tier,
                            isClaimed = tier.day <= daysClaimedInCycle,
                            isReadyToday = canClaim && tier.day == nextRewardDay,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Day 7: Grand Crystal Chest Card with live looping 3D chest preview
            val grandTier = DailyRewardsCatalog.tiers.firstOrNull { it.isGrandPrize }
            if (grandTier != null) {
                GrandPrizeCard(
                    tier = grandTier,
                    isClaimed = grandTier.day <= daysClaimedInCycle,
                    isReadyToday = canClaim && grandTier.day == nextRewardDay,
                    onPreviewChest = { showChestPreview = true }
                )
            }

            Spacer(Modifier.height(4.dp))

            // Claim Button — launches 3D Open Chest Celebration Dialog!
            GlossyArcadeButton(
                text = if (canClaim) "CLAIM DAY $nextRewardDay REWARD" else "COME BACK TOMORROW",
                icon = if (canClaim) Icons.Filled.CardGiftcard else Icons.Filled.Check,
                style = if (canClaim) ArcadeButtonStyle.GOLD else ArcadeButtonStyle.SLATE,
                enabled = canClaim,
                onClick = {
                    val targetTier = DailyRewardsCatalog.tiers.firstOrNull { it.day == nextRewardDay }
                    onClaimReward()
                    if (targetTier != null) {
                        claimedDialogTier = targetTier
                    }
                },
                height = 54.dp,
                cornerRadius = 16.dp,
                fontSize = 16.sp,
                testTag = "daily_reward_claim_button",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))
        }
    }

    val activeClaimTier = claimedDialogTier
    if (activeClaimTier != null) {
        ChestOpenCelebrationDialog(
            title = "Day ${activeClaimTier.day} Chest Opened!",
            subtitle = activeClaimTier.title,
            goldEarned = activeClaimTier.gold,
            diamondsEarned = activeClaimTier.diamonds,
            bonusLabel = if (activeClaimTier.shuffleBoosters > 0) "Shuffle ×${activeClaimTier.shuffleBoosters}" else null,
            onDismiss = { claimedDialogTier = null }
        )
    } else if (showChestPreview) {
        val grand = DailyRewardsCatalog.tiers.firstOrNull { it.isGrandPrize }
        ChestOpenCelebrationDialog(
            title = "Day 7 Grand Crystal Chest",
            subtitle = "Complete 7 daily check-ins to unlock all treasures!",
            goldEarned = grand?.gold ?: 1000,
            diamondsEarned = grand?.diamonds ?: 50,
            bonusLabel = if ((grand?.shuffleBoosters ?: 0) > 0) "Shuffle ×${grand?.shuffleBoosters}" else "Shuffle ×2",
            onDismiss = { showChestPreview = false }
        )
    }
}

@Composable
private fun DailyRewardTile(
    tier: DailyRewardTier,
    isClaimed: Boolean,
    isReadyToday: Boolean,
    modifier: Modifier = Modifier
) {
    val isLocked = !isClaimed && !isReadyToday

    val borderColor = when {
        isReadyToday -> AccentGold
        isClaimed -> AccentEmerald.copy(alpha = 0.5f)
        else -> GlassBorder.copy(alpha = 0.35f)
    }

    FrostedGlassCard(
        cornerRadius = 16.dp,
        borderColor = borderColor,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 6.dp)
        ) {
            Text(
                text = "DAY ${tier.day}",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp
                ),
                color = if (isReadyToday) AccentGold else TextSecondary
            )

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(BoardWellBg)
                    .border(1.dp, borderColor, CircleShape)
            ) {
                GameAssetIcon(
                    resId = GameAssetCatalog.dailyRewardRes(tier.day),
                    contentDescription = tier.title,
                    size = 34.dp,
                    alpha = when {
                        isLocked -> 0.42f
                        isClaimed -> 0.72f
                        else -> 1f
                    }
                )
                if (isClaimed) {
                    GameAssetIcon(
                        resId = R.drawable.img_status_check_shield,
                        contentDescription = "Claimed",
                        size = 18.dp,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                } else if (isLocked) {
                    GameAssetIcon(
                        resId = R.drawable.img_status_padlock,
                        contentDescription = "Locked",
                        size = 16.dp,
                        modifier = Modifier.align(Alignment.BottomEnd)
                    )
                }
            }

            Text(
                text = tier.title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = if (isReadyToday) Color.White else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (isReadyToday) {
                Text(
                    text = "READY",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 9.sp
                    ),
                    color = AccentGold
                )
            }
        }
    }
}

@Composable
private fun GrandPrizeCard(
    tier: DailyRewardTier,
    isClaimed: Boolean,
    isReadyToday: Boolean,
    onPreviewChest: () -> Unit
) {
    FrostedGlassCard(
        cornerRadius = 20.dp,
        borderColor = if (isReadyToday) AccentGoldBright else AccentGold.copy(alpha = 0.55f),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPreviewChest)
            .testTag("daily_chest_preview_button")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(BoardWellBg)
                    .border(1.2.dp, AccentGold.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            ) {
                AnimatedTreasureChest3D(
                    size = 68.dp,
                    autoPlay = true,
                    loopingPreview = true
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DAY 7 GRAND CHEST · TAP TO OPEN",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.8.sp
                    ),
                    color = AccentGold
                )
                Text(
                    text = "1,000 Gold · 50 Gems · Shuffle ×2",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = if (isClaimed) "Claimed this cycle" else if (isReadyToday) "Ready to unlock right now!" else "Complete Day 6 to unlock",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isReadyToday) AccentGold else TextSecondary
                )
            }
        }
    }
}
