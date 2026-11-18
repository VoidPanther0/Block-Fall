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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.ChallengeLevels
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.ResetProgressConfirmationDialog
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBtnBottom
import com.example.ui.theme.GoldBtnTop
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextSecondary

@Composable
fun StatisticsScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onResetProgressConfirmed: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    val stats = state.statistics
    val completedLevels = state.challengeProgress.values.count { it.completed }
    val totalStars = state.challengeProgress.values.sumOf { it.stars }
    val maxStars = ChallengeLevels.levels.size * 3
    val progressRatio = (completedLevels.toFloat() / ChallengeLevels.levels.size.toFloat()).coerceIn(0f, 1f)

    CascadeScreenContainer(
        maxContentWidth = 600.dp,
        modifier = Modifier.testTag("statistics_screen")
    ) {
        ScreenTopBar(
            title = "Statistics",
            onBack = onBack,
            backTestTag = "statistics_back_button"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Primary Statistics Frosted Card matching mockup
            FrostedGlassCard(
                cornerRadius = 22.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)) {
                    StatListRow(
                        icon = Icons.Filled.Star,
                        iconTint = AccentGold,
                        label = "Total Games",
                        value = stats.totalGamesPlayed.toString()
                    )
                    StatDivider()
                    StatListRow(
                        icon = Icons.Filled.EmojiEvents,
                        iconTint = AccentGold,
                        label = "Highest Score",
                        value = state.classicBestScore.toString()
                    )
                    StatDivider()
                    StatListRow(
                        icon = Icons.Filled.Flag,
                        iconTint = AccentCyan,
                        label = "Lines Cleared",
                        value = stats.totalLinesCleared.toString()
                    )
                    StatDivider()
                    StatListRow(
                        icon = Icons.Filled.Layers,
                        iconTint = AccentGold,
                        label = "Cascades",
                        value = stats.totalCascadesTriggered.toString()
                    )
                    StatDivider()
                    StatListRow(
                        icon = Icons.Filled.GridView,
                        iconTint = AccentEmerald,
                        label = "Deepest Chain / Placed",
                        value = "${if (stats.maxCascadeDepth > 0) "x${stats.maxCascadeDepth}" else "—"} · ${stats.totalBlocksPlaced}"
                    )
                }
            }

            // Challenge Progress Card with thick Golden Progress Bar matching mockup
            FrostedGlassCard(
                cornerRadius = 22.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.padding(18.dp)
                ) {
                    Text(
                        text = "Challenge Progress",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.White
                    )

                    Text(
                        text = "$completedLevels / ${ChallengeLevels.levels.size}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp
                        ),
                        color = Color.White
                    )

                    // Thick rounded golden progress bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(18.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF0F1F4E))
                            .border(1.2.dp, GlassBorder.copy(alpha = 0.55f), CircleShape)
                            .padding(2.dp)
                    ) {
                        if (progressRatio > 0f) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progressRatio.coerceAtLeast(0.06f))
                                    .clip(CircleShape)
                                    .background(Brush.horizontalGradient(listOf(GoldBtnTop, GoldBtnBottom)))
                            )
                        }
                    }

                    Text(
                        text = "★ $totalStars / $maxStars Stars · Level ${state.unlockedChallengeLevel} Unlocked",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = AccentGold
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        ChallengeLevels.levels.forEach { level ->
                            val progress = state.challengeProgress[level.levelNumber]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(SurfaceCardElevated.copy(alpha = 0.65f))
                                    .padding(horizontal = 12.dp, vertical = 7.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "L${level.levelNumber} · ${level.title}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                                    color = Color.White
                                )
                                Text(
                                    text = if (progress?.completed == true) {
                                        "${"★".repeat(progress.stars)}  Best ${progress.bestScore}"
                                    } else if (level.levelNumber <= state.unlockedChallengeLevel) {
                                        if ((progress?.bestScore ?: 0) > 0) "Best ${progress?.bestScore}" else "Unlocked"
                                    } else {
                                        "Locked"
                                    },
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (progress?.completed == true) AccentGold else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            GlossyArcadeButton(
                text = "Reset Progress",
                icon = Icons.Filled.DeleteForever,
                style = ArcadeButtonStyle.RED,
                onClick = { showResetConfirm = true },
                height = 52.dp,
                cornerRadius = 26.dp,
                fontSize = 16.sp,
                testTag = "statistics_reset_button",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
        }
    }

    if (showResetConfirm) {
        ResetProgressConfirmationDialog(
            onConfirm = {
                showResetConfirm = false
                onResetProgressConfirmed()
            },
            onDismiss = { showResetConfirm = false }
        )
    }
}

@Composable
private fun StatListRow(
    icon: ImageVector,
    iconTint: Color,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = Color.White
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            ),
            color = Color.White
        )
    }
}

@Composable
private fun StatDivider() {
    HorizontalDivider(
        color = Color.White.copy(alpha = 0.12f),
        thickness = 1.dp
    )
}
