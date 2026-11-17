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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
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
import com.example.ui.components.BoardPreview
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardRim
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextSecondary

@Composable
fun ChallengeDetailsScreen(
    levelNumber: Int,
    state: GameUiState,
    onBack: () -> Unit,
    onPlayLevel: (Int) -> Unit
) {
    val level = remember(levelNumber) { ChallengeLevels.getLevel(levelNumber) }
    val previewBoard = remember(levelNumber) { level.buildBoard() }
    val progress = state.challengeProgress[level.levelNumber]
    val isUnlocked = level.levelNumber <= state.unlockedChallengeLevel

    CascadeScreenContainer(
        maxContentWidth = 600.dp,
        modifier = Modifier.testTag("challenge_details_screen")
    ) {
        ScreenTopBar(
            title = "Level ${level.levelNumber}: ${level.title}",
            onBack = onBack,
            backTestTag = "challenge_details_back_button"
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Board Preview
            BoardPreview(
                board = previewBoard,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 200.dp)
            )

            // Objective & Stats Card
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BoardRim, RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Text(
                    text = "LEVEL OBJECTIVE",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentCyan
                )
                Text(
                    text = level.subtitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    DetailMetric("TARGET SCORE", "${level.targetScore}", AccentEmerald, Modifier.weight(1f))
                    DetailMetric("MOVE LIMIT", "${level.moveLimit}", AccentGold, Modifier.weight(1f))
                    DetailMetric("YOUR BEST", "${progress?.bestScore ?: 0}", AccentCyan, Modifier.weight(1f))
                }

                // Star Rating Breakdown
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCardElevated)
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "STAR GOALS",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            val earned = progress?.stars ?: 0
                            for (i in 1..3) {
                                Icon(
                                    imageVector = if (i <= earned) Icons.Filled.Star else Icons.Outlined.StarBorder,
                                    contentDescription = null,
                                    tint = if (i <= earned) AccentGold else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                    Text(
                        text = "★ Reach ${level.targetScore} pts   ★★ ${(level.targetScore * 1.25f).toInt()} pts or 3+ moves left   ★★★ ${(level.targetScore * 1.5f).toInt()} pts or 5+ moves left",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextSecondary
                    )
                }
            }

            Button(
                onClick = { onPlayLevel(level.levelNumber) },
                enabled = isUnlocked,
                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("challenge_details_play_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF002414),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = if (isUnlocked) "Play Level ${level.levelNumber}" else "Level Locked",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color(0xFF002414)
                )
            }
        }
    }
}

@Composable
private fun DetailMetric(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardElevated)
            .padding(vertical = 10.dp, horizontal = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp),
            color = accent
        )
    }
}
