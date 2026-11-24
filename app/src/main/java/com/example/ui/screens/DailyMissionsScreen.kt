package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.game.DailyChallengeGenerator
import com.example.game.DailyMissionDefinition
import com.example.game.DailyMissionsCatalog
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.CrystalCoinBadge
import com.example.ui.components.CrystalDiamondBadge
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.GoldMetallicBanner
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
fun DailyMissionsScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onClaimMission: (String) -> Unit
) {
    val todayKey = DailyChallengeGenerator.todayDateKey()
    val snapshot = state.dailyMissionSnapshot.anchoredTo(todayKey, state.statistics)

    CascadeScreenContainer(
        maxContentWidth = 720.dp,
        modifier = Modifier.testTag("daily_missions_screen")
    ) {
        ScreenTopBar(
            title = "Daily Missions",
            onBack = onBack,
            backTestTag = "daily_missions_back_button"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header reset timer card
            FrostedGlassCard(cornerRadius = 18.dp, modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(14.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(AccentCyan.copy(alpha = 0.16f))
                            .border(1.dp, AccentCyan.copy(alpha = 0.45f), CircleShape)
                    ) {
                        Icon(Icons.Filled.Schedule, null, tint = AccentCyan, modifier = Modifier.size(20.dp))
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MISSIONS RESET DAILY",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = AccentCyan
                        )
                        Text(
                            text = "Complete all 4 objectives each day for Gold and Gem bonuses!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Missions list
            DailyMissionsCatalog.missions.forEach { mission ->
                val isClaimed = mission.id in snapshot.claimedMissionIds
                val currentProgress = mission.progressProvider(state.statistics, snapshot)
                val isCompleted = currentProgress >= mission.target
                val canClaim = isCompleted && !isClaimed

                MissionItemCard(
                    mission = mission,
                    currentProgress = currentProgress,
                    isCompleted = isCompleted,
                    isClaimed = isClaimed,
                    canClaim = canClaim,
                    onClaim = { onClaimMission(mission.id) }
                )
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun MissionItemCard(
    mission: DailyMissionDefinition,
    currentProgress: Int,
    isCompleted: Boolean,
    isClaimed: Boolean,
    canClaim: Boolean,
    onClaim: () -> Unit
) {
    val progressFraction = (currentProgress.toFloat() / mission.target.toFloat()).coerceIn(0f, 1f)

    FrostedGlassCard(
        cornerRadius = 18.dp,
        borderColor = when {
            canClaim -> AccentGold
            isClaimed -> AccentEmerald.copy(alpha = 0.45f)
            else -> GlassBorder.copy(alpha = 0.4f)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (canClaim) AccentGold.copy(alpha = 0.20f)
                        else if (isClaimed) AccentEmerald.copy(alpha = 0.16f)
                        else AccentCyan.copy(alpha = 0.14f)
                    )
                    .border(
                        1.dp,
                        if (canClaim) AccentGold else if (isClaimed) AccentEmerald else GlassBorder,
                        RoundedCornerShape(14.dp)
                    )
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = if (canClaim) AccentGold else if (isClaimed) AccentEmerald else AccentCyan,
                    modifier = Modifier.size(22.dp)
                )
            }

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = mission.title,
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
                    text = mission.description,
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
                        text = "${minOf(currentProgress, mission.target)}/${mission.target}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = if (isCompleted) AccentEmerald else TextSecondary
                    )
                }

                // Reward labels
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
                            text = "+${mission.rewardGold}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            ),
                            color = AccentGold
                        )
                    }
                    if (mission.rewardDiamonds > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CrystalDiamondBadge(size = 14.dp)
                            Text(
                                text = "+${mission.rewardDiamonds}",
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
