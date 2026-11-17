package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
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
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.game.ChallengeLevels
import com.example.game.GameMode
import com.example.game.GameStatus
import com.example.ui.GameUiState
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardRim
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextSecondary

@Composable
fun PauseDialog(
    state: GameUiState,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onSelectChallengeLevel: (Int) -> Unit,
    onSwitchMode: (GameMode) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onOpenRules: () -> Unit
) {
    Dialog(onDismissRequest = onResume) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(SurfaceCard)
                .border(1.5.dp, BoardRim, RoundedCornerShape(22.dp))
                .padding(20.dp)
                .testTag("pause_dialog"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "GAME PAUSED",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCardElevated)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.haptics_label),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Switch(
                    checked = state.hapticsEnabled,
                    onCheckedChange = onToggleHaptics
                )
            }

            if (state.mode == GameMode.CHALLENGE) {
                Text(
                    text = "CHALLENGE LEVELS",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChallengeLevels.levels.take(4).forEach { lvl ->
                        val unlocked = lvl.levelNumber <= state.unlockedChallengeLevel
                        LevelChip(
                            number = lvl.levelNumber,
                            active = lvl.levelNumber == state.currentChallengeLevel.levelNumber,
                            unlocked = unlocked,
                            onClick = { if (unlocked) onSelectChallengeLevel(lvl.levelNumber) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ChallengeLevels.levels.drop(4).forEach { lvl ->
                        val unlocked = lvl.levelNumber <= state.unlockedChallengeLevel
                        LevelChip(
                            number = lvl.levelNumber,
                            active = lvl.levelNumber == state.currentChallengeLevel.levelNumber,
                            unlocked = unlocked,
                            onClick = { if (unlocked) onSelectChallengeLevel(lvl.levelNumber) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("resume_button")
            ) {
                Text(
                    text = stringResource(R.string.resume),
                    fontWeight = FontWeight.Bold
                )
            }

            OutlinedButton(
                onClick = {
                    onResume()
                    onRestart()
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pause_restart_button")
            ) {
                Text(text = stringResource(R.string.restart), color = MaterialTheme.colorScheme.onSurface)
            }

            OutlinedButton(
                onClick = onOpenRules,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(text = stringResource(R.string.how_to_play), color = TextSecondary)
            }

            OutlinedButton(
                onClick = {
                    val target = if (state.mode == GameMode.CLASSIC) GameMode.CHALLENGE else GameMode.CLASSIC
                    onResume()
                    onSwitchMode(target)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                val label = if (state.mode == GameMode.CLASSIC) "Switch to Challenge Mode" else "Switch to Classic Mode"
                Text(text = label, color = AccentGold)
            }
        }
    }
}

@Composable
private fun LevelChip(
    number: Int,
    active: Boolean,
    unlocked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when {
        active -> AccentCyan.copy(alpha = 0.25f)
        unlocked -> SurfaceCardElevated
        else -> SurfaceCardElevated.copy(alpha = 0.4f)
    }
    val border = if (active) AccentCyan else BoardRim
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable(enabled = unlocked, onClick = onClick)
    ) {
        Text(
            text = if (unlocked) "$number" else "🔒",
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 14.sp),
            color = if (active) AccentCyan else if (unlocked) Color.White else TextSecondary
        )
    }
}

@Composable
fun EndGameDialog(
    state: GameUiState,
    onPlayAgain: () -> Unit,
    onNextLevel: () -> Unit,
    onSwitchMode: (GameMode) -> Unit
) {
    val isChallengeWin = state.status == GameStatus.CHALLENGE_WON
    val isChallengeLoss = state.status == GameStatus.CHALLENGE_LOST
    val hasNextLevel = state.currentChallengeLevel.levelNumber < ChallengeLevels.levels.size

    val title = when {
        isChallengeWin -> stringResource(R.string.challenge_complete_title)
        isChallengeLoss -> stringResource(R.string.challenge_failed_title)
        else -> stringResource(R.string.game_over_title)
    }
    val accent = when {
        isChallengeWin -> AccentEmerald
        isChallengeLoss -> AccentCoral
        else -> AccentGold
    }

    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(SurfaceCard)
                .border(1.5.dp, accent.copy(alpha = 0.7f), RoundedCornerShape(22.dp))
                .padding(22.dp)
                .testTag("end_game_dialog"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = accent
            )

            if (state.isNewBest && state.mode == GameMode.CLASSIC) {
                Text(
                    text = "NEW HIGH SCORE!",
                    style = MaterialTheme.typography.labelMedium,
                    color = AccentGold
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCardElevated)
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatLine("Score", state.score.toString())
                if (state.mode == GameMode.CLASSIC) {
                    StatLine("Best Score", state.classicBestScore.toString())
                } else {
                    StatLine("Target Score", state.currentChallengeLevel.targetScore.toString())
                }
                StatLine("Lines Cleared", state.totalLinesCleared.toString())
                StatLine("Best Gravity Chain", if (state.bestCascadeDepth > 0) "x${state.bestCascadeDepth}" else "—")
            }

            if (isChallengeWin && hasNextLevel) {
                Button(
                    onClick = onNextLevel,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("next_level_button")
                ) {
                    Text(
                        text = stringResource(R.string.next_level),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF002414)
                    )
                }
            }

            Button(
                onClick = onPlayAgain,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isChallengeWin && hasNextLevel) SurfaceCardElevated else AccentCyan
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("play_again_button")
            ) {
                Text(
                    text = if (state.mode == GameMode.CHALLENGE) {
                        stringResource(R.string.retry_level)
                    } else {
                        stringResource(R.string.play_again)
                    },
                    fontWeight = FontWeight.Bold,
                    color = if (isChallengeWin && hasNextLevel) Color.White else Color(0xFF002026)
                )
            }

            OutlinedButton(
                onClick = {
                    val target = if (state.mode == GameMode.CLASSIC) GameMode.CHALLENGE else GameMode.CLASSIC
                    onSwitchMode(target)
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = if (state.mode == GameMode.CLASSIC) "Try Challenge Mode" else "Play Classic Mode",
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun StatLine(label: String, value: String) {
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
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun GravityRulesDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(SurfaceCard)
                .border(1.5.dp, AccentCyan.copy(alpha = 0.6f), RoundedCornerShape(22.dp))
                .padding(20.dp)
                .testTag("rules_dialog"),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = "How Cascade Gravity Works",
                style = MaterialTheme.typography.titleLarge,
                color = AccentCyan
            )
            RuleStep("1. Place Anywhere", "Drag pieces from the tray (or tap a piece then tap the board). Pieces stay where you place them until a line clears.")
            RuleStep("2. Clear Lines", "Fill any complete horizontal row or vertical column to shatter those blocks.")
            RuleStep("3. Gravity Falls", "Every block above empty space drops straight DOWN within its column. New lines formed by falling blocks clear automatically for huge chain multipliers!")
            Spacer(Modifier.height(4.dp))
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Got It", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun RuleStep(title: String, body: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCardElevated)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = AccentGold
        )
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            color = TextSecondary
        )
    }
}
