package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.ChallengeLevels
import com.example.ui.GameUiState
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.ResetProgressConfirmationDialog
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardRim
import com.example.ui.theme.SurfaceCard
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

    CascadeScreenContainer(
        maxContentWidth = 640.dp,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    label = "HIGHEST SCORE",
                    value = state.classicBestScore.toString(),
                    accent = AccentGold,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "TOTAL GAMES",
                    value = stats.totalGamesPlayed.toString(),
                    accent = AccentCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    label = "LINES CLEARED",
                    value = stats.totalLinesCleared.toString(),
                    accent = AccentEmerald,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "CASCADES",
                    value = stats.totalCascadesTriggered.toString(),
                    accent = AccentCoral,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatTile(
                    label = "DEEPEST CHAIN",
                    value = if (stats.maxCascadeDepth > 0) "x${stats.maxCascadeDepth}" else "—",
                    accent = AccentGold,
                    modifier = Modifier.weight(1f)
                )
                StatTile(
                    label = "BLOCKS PLACED",
                    value = stats.totalBlocksPlaced.toString(),
                    accent = AccentCyan,
                    modifier = Modifier.weight(1f)
                )
            }

            // Challenge Progress Breakdown
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BoardRim, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "CHALLENGE PROGRESS",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentEmerald
                    )
                    Text(
                        text = "★ $totalStars / $maxStars",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                        color = AccentGold
                    )
                }

                Text(
                    text = "$completedLevels of ${ChallengeLevels.levels.size} levels completed · Level ${state.unlockedChallengeLevel} unlocked",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                LinearProgressIndicator(
                    progress = { completedLevels.toFloat() / ChallengeLevels.levels.size.toFloat() },
                    color = AccentEmerald,
                    trackColor = SurfaceCardElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )

                ChallengeLevels.levels.forEach { level ->
                    val progress = state.challengeProgress[level.levelNumber]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(SurfaceCardElevated)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "L${level.levelNumber} · ${level.title}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = MaterialTheme.colorScheme.onSurface
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

            OutlinedButton(
                onClick = { showResetConfirm = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("statistics_reset_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = null,
                    tint = AccentCoral,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(text = "Reset All Progress", color = AccentCoral)
            }
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
private fun StatTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, BoardRim, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
            color = accent
        )
    }
}
