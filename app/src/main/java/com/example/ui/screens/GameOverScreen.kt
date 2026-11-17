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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
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
import com.example.game.GameMode
import com.example.game.GameStatus
import com.example.ui.GameUiState
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextSecondary

@Composable
fun GameOverScreen(
    state: GameUiState,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    val isChallengeLoss = state.mode == GameMode.CHALLENGE || state.status == GameStatus.CHALLENGE_LOST
    val accent = if (isChallengeLoss) AccentCoral else AccentGold
    val title = if (isChallengeLoss) {
        stringResource(R.string.challenge_failed_title)
    } else {
        stringResource(R.string.game_over_title)
    }

    BackHandler(onBack = onHome)

    CascadeScreenContainer(
        maxContentWidth = 540.dp,
        modifier = Modifier.testTag("game_over_screen")
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
                    .border(1.5.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
                    .padding(24.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                    color = accent
                )

                Text(
                    text = if (state.mode == GameMode.CLASSIC) {
                        "Classic Endless Mode"
                    } else {
                        "Challenge Level ${state.currentChallengeLevel.levelNumber}: ${state.currentChallengeLevel.title}"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )

                if (state.isNewBest && state.mode == GameMode.CLASSIC) {
                    Text(
                        text = "NEW HIGH SCORE!",
                        style = MaterialTheme.typography.labelLarge,
                        color = AccentGold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentGold.copy(alpha = 0.16f))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(SurfaceCardElevated)
                        .padding(16.dp)
                ) {
                    GameOverStatRow("Final Score", "${state.score}", AccentCyan)
                    if (state.mode == GameMode.CLASSIC) {
                        GameOverStatRow("Best Score", "${state.classicBestScore}", AccentGold)
                    } else {
                        GameOverStatRow("Target Score", "${state.currentChallengeLevel.targetScore}", AccentGold)
                    }
                    GameOverStatRow("Moves Played", "${state.movesPlayed}", MaterialTheme.colorScheme.onSurface)
                    GameOverStatRow("Lines Cleared", "${state.totalLinesCleared}", MaterialTheme.colorScheme.onSurface)
                    GameOverStatRow(
                        "Best Gravity Chain",
                        if (state.bestCascadeDepth > 0) "x${state.bestCascadeDepth}" else "—",
                        AccentGold
                    )
                }

                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("game_over_retry_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = Color(0xFF002026),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = if (isChallengeLoss) stringResource(R.string.retry_level) else "Retry",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF002026)
                    )
                }

                OutlinedButton(
                    onClick = onHome,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("game_over_home_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Home,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(text = stringResource(R.string.nav_home), color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
    }
}

@Composable
private fun GameOverStatRow(label: String, value: String, valueColor: Color) {
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
