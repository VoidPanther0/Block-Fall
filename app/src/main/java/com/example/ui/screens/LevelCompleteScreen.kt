package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.ChallengeLevels
import com.example.ui.GameUiState
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextSecondary

@Composable
fun LevelCompleteScreen(
    state: GameUiState,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onHome: () -> Unit
) {
    val hasNextLevel = state.currentChallengeLevel.levelNumber < ChallengeLevels.levels.size
    val stars = state.lastChallengeStarsEarned.coerceIn(1, 3)

    BackHandler(onBack = onHome)

    CascadeScreenContainer(
        maxContentWidth = 540.dp,
        modifier = Modifier.testTag("level_complete_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(SurfaceCard)
                    .border(1.5.dp, AccentEmerald.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(48.dp)
                )

                Text(
                    text = stringResource(R.string.challenge_complete_title),
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = AccentEmerald
                )

                Text(
                    text = "Level ${state.currentChallengeLevel.levelNumber}: ${state.currentChallengeLevel.title}",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextSecondary
                )

                // Star rating display
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (i in 1..3) {
                        Icon(
                            imageVector = if (i <= stars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (i <= stars) "Earned star $i" else "Empty star $i",
                            tint = if (i <= stars) AccentGold else TextSecondary.copy(alpha = 0.4f),
                            modifier = Modifier.size(38.dp)
                        )
                    }
                }

                // Achievements / Score breakdown
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceCardElevated)
                        .padding(16.dp)
                ) {
                    CompleteStatRow("Final Score", "${state.score}", AccentCyan)
                    CompleteStatRow("Target Score", "${state.currentChallengeLevel.targetScore}", AccentEmerald)
                    CompleteStatRow("Moves Remaining", "${state.movesRemaining}", AccentGold)
                    CompleteStatRow("Lines Cleared", "${state.totalLinesCleared}", MaterialTheme.colorScheme.onSurface)
                    CompleteStatRow(
                        "Best Gravity Chain",
                        if (state.bestCascadeDepth > 0) "x${state.bestCascadeDepth}" else "—",
                        AccentGold
                    )
                }

                if (hasNextLevel) {
                    Button(
                        onClick = onNextLevel,
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("level_complete_next_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.PlayArrow,
                            contentDescription = null,
                            tint = Color(0xFF002414),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            text = stringResource(R.string.next_level),
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF002414)
                        )
                    }
                } else {
                    Text(
                        text = "ALL 8 CHALLENGES MASTERED!",
                        style = MaterialTheme.typography.labelLarge,
                        color = AccentGold
                    )
                }

                OutlinedButton(
                    onClick = onReplay,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("level_complete_replay_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(text = "Replay Level", color = MaterialTheme.colorScheme.onSurface)
                }

                OutlinedButton(
                    onClick = onHome,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("level_complete_home_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(text = stringResource(R.string.nav_home), color = AccentCyan)
                }
            }
        }
    }
}

@Composable
private fun CompleteStatRow(label: String, value: String, valueColor: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
            color = valueColor
        )
    }
}
