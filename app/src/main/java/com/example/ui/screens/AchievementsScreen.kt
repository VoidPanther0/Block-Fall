package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.AchievementCategory
import com.example.game.AchievementDefinition
import com.example.game.AchievementsCatalog
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.CrystalCoinBadge
import com.example.ui.components.CrystalDiamondBadge
import com.example.ui.components.CrystalMedalBadge
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GameAssetCatalog
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AchievementsScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onClaimAchievement: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(AchievementCategory.ALL) }

    val filteredList = remember(selectedCategory) {
        if (selectedCategory == AchievementCategory.ALL) {
            AchievementsCatalog.items
        } else {
            AchievementsCatalog.items.filter { it.category == selectedCategory }
        }
    }

    CascadeScreenContainer(
        maxContentWidth = 720.dp,
        modifier = Modifier.testTag("achievements_screen")
    ) {
        ScreenTopBar(
            title = "Achievements",
            onBack = onBack,
            backTestTag = "achievements_back_button"
        )

        // Categories Filter Row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            items(AchievementCategory.entries.toTypedArray()) { cat ->
                val isSelected = cat == selectedCategory
                val bgModifier = if (isSelected) {
                    Modifier.background(
                        Brush.verticalGradient(
                            listOf(
                                AccentCyan.copy(alpha = 0.25f),
                                AccentCyan.copy(alpha = 0.12f)
                            )
                        )
                    )
                } else {
                    Modifier.background(BoardWellBg)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .then(bgModifier)
                        .border(
                            1.2.dp,
                            if (isSelected) AccentCyan else GlassBorder,
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat.title,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                        ),
                        color = if (isSelected) Color.White else TextSecondary
                    )
                }
            }
        }

        // Achievements List
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredList, key = { it.id }) { ach ->
                val isClaimed = ach.id in state.claimedAchievementIds
                val rawProgress = ach.progressProvider(state.statistics, state.challengeProgress)
                val isCompleted = rawProgress >= ach.target
                val canClaim = isCompleted && !isClaimed

                AchievementCard(
                    achievement = ach,
                    currentProgress = rawProgress,
                    isCompleted = isCompleted,
                    isClaimed = isClaimed,
                    canClaim = canClaim,
                    onClaim = { onClaimAchievement(ach.id) }
                )
            }
            item { Spacer(Modifier.height(10.dp)) }
        }
    }
}

@Composable
private fun AchievementCard(
    achievement: AchievementDefinition,
    currentProgress: Int,
    isCompleted: Boolean,
    isClaimed: Boolean,
    canClaim: Boolean,
    onClaim: () -> Unit
) {
    val progressFraction = (currentProgress.toFloat() / achievement.target.toFloat()).coerceIn(0f, 1f)

    FrostedGlassCard(
        cornerRadius = 18.dp,
        borderColor = when {
            canClaim -> AccentGold
            isClaimed -> AccentEmerald.copy(alpha = 0.45f)
            else -> GlassBorder.copy(alpha = 0.45f)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CrystalMedalBadge(
                tier = achievement.tier,
                unlocked = isCompleted || isClaimed,
                size = 54.dp,
                customResId = GameAssetCatalog.achievementMedalRes(achievement.category, achievement.tier)
            )

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = achievement.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Progress Bar
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(top = 2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape)
                            .background(BoardWellBg)
                            .border(0.8.dp, GlassBorder, CircleShape)
                    ) {
                        if (progressFraction > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressFraction)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(AccentCyan, AccentEmerald)
                                        )
                                    )
                            )
                        }
                    }
                    Text(
                        text = "${minOf(currentProgress, achievement.target)}/${achievement.target}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (isCompleted) AccentEmerald else TextSecondary
                    )
                }

                // Reward badges
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        CrystalCoinBadge(size = 14.dp)
                        Text(
                            text = "+${achievement.rewardGold}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = AccentGold
                        )
                    }
                    if (achievement.rewardDiamonds > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CrystalDiamondBadge(size = 14.dp)
                            Text(
                                text = "+${achievement.rewardDiamonds}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                ),
                                color = AccentCyan
                            )
                        }
                    }
                }
            }

            when {
                isClaimed -> {
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
                        Text(
                            text = "Claimed",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = AccentEmerald
                        )
                    }
                }
                canClaim -> {
                    GlossyArcadeButton(
                        text = "CLAIM",
                        style = ArcadeButtonStyle.GOLD,
                        onClick = onClaim,
                        height = 40.dp,
                        cornerRadius = 12.dp,
                        fontSize = 12.sp,
                        modifier = Modifier.width(88.dp)
                    )
                }
                else -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(BoardWellBg)
                            .border(1.dp, GlassBorder.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "${(progressFraction * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold
                            ),
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
