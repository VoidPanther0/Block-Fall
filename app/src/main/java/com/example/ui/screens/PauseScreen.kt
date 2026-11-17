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
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameMode
import com.example.ui.GameUiState
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.ExitConfirmationDialog
import com.example.ui.components.RestartConfirmationDialog
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardRim
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextSecondary

@Composable
fun PauseScreen(
    state: GameUiState,
    onResume: () -> Unit,
    onRestartConfirmed: () -> Unit,
    onOpenHowToPlay: () -> Unit,
    onOpenSettings: () -> Unit,
    onExitToHomeConfirmed: () -> Unit
) {
    var showRestartConfirm by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }

    BackHandler(onBack = onResume)

    CascadeScreenContainer(
        maxContentWidth = 520.dp,
        modifier = Modifier.testTag("pause_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(SurfaceCard)
                    .border(1.5.dp, BoardRim, RoundedCornerShape(22.dp))
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "GAME PAUSED",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 1.sp
                    ),
                    color = AccentCyan
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

                // Current Game Snapshot
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCardElevated)
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PauseStat("SCORE", state.score.toString())
                    PauseStat("LINES", state.totalLinesCleared.toString())
                    PauseStat(
                        if (state.mode == GameMode.CLASSIC) "MOVES" else "LEFT",
                        if (state.mode == GameMode.CLASSIC) state.movesPlayed.toString() else state.movesRemaining.toString()
                    )
                }

                Spacer(Modifier.height(4.dp))

                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("pause_resume_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.PlayArrow,
                        contentDescription = null,
                        tint = Color(0xFF002026),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(
                        text = stringResource(R.string.resume),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF002026)
                    )
                }

                OutlinedButton(
                    onClick = { showRestartConfirm = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pause_restart_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(text = stringResource(R.string.restart), color = MaterialTheme.colorScheme.onSurface)
                }

                OutlinedButton(
                    onClick = onOpenHowToPlay,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pause_how_to_play_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(text = stringResource(R.string.how_to_play), color = MaterialTheme.colorScheme.onSurface)
                }

                OutlinedButton(
                    onClick = onOpenSettings,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pause_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(8.dp))
                    Text(text = stringResource(R.string.nav_settings), color = MaterialTheme.colorScheme.onSurface)
                }

                OutlinedButton(
                    onClick = { showExitConfirm = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("pause_home_button")
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

    if (showRestartConfirm) {
        RestartConfirmationDialog(
            onConfirm = {
                showRestartConfirm = false
                onRestartConfirmed()
            },
            onDismiss = { showRestartConfirm = false }
        )
    }

    if (showExitConfirm) {
        ExitConfirmationDialog(
            mode = state.mode,
            onConfirm = {
                showExitConfirm = false
                onExitToHomeConfirmed()
            },
            onDismiss = { showExitConfirm = false }
        )
    }
}

@Composable
private fun PauseStat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 18.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
