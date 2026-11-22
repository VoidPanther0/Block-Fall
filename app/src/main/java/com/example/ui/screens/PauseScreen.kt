package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameMode
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.ExitConfirmationDialog
import com.example.ui.components.FloatingModalBadge
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.components.RestartConfirmationDialog
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PauseScreen(
    state: GameUiState,
    onResume: () -> Unit,
    onRestartConfirmed: () -> Unit,
    onExitToHomeConfirmed: () -> Unit,
    onOpenSettings: (() -> Unit)? = null
) {
    var showRestartConfirm by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }

    BackHandler(onBack = onResume)

    CascadeScreenContainer(
        maxContentWidth = 440.dp,
        modifier = Modifier.testTag("pause_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxWidth()
            ) {
                PremiumCard(
                    cornerRadius = 26.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 34.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 22.dp, end = 22.dp, top = 48.dp, bottom = 22.dp)
                    ) {
                        Text(
                            text = "Game Paused",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp
                            ),
                            color = TextPrimary
                        )

                        Text(
                            text = when (state.mode) {
                                GameMode.CLASSIC -> "Classic Endless"
                                GameMode.CHALLENGE -> "Level ${state.currentChallengeLevel.levelNumber} · ${state.currentChallengeLevel.title}"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(BoardWellBg)
                                .border(1.dp, GlassBorder.copy(alpha = 0.42f), RoundedCornerShape(14.dp))
                                .padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            PauseStatChip("Score", "${state.score}")
                            PauseStatChip("Lines", "${state.totalLinesCleared}")
                            PauseStatChip("Moves", "${state.movesPlayed}")
                        }

                        Spacer(Modifier.height(2.dp))

                        PremiumPrimaryButton(
                            text = stringResource(R.string.resume),
                            icon = Icons.Filled.PlayArrow,
                            style = ArcadeButtonStyle.GREEN,
                            onClick = onResume,
                            height = 54.dp,
                            cornerRadius = 16.dp,
                            fontSize = 17.sp,
                            testTag = "pause_resume_button",
                            modifier = Modifier.fillMaxWidth()
                        )

                        PremiumPrimaryButton(
                            text = stringResource(R.string.restart),
                            icon = Icons.Filled.Refresh,
                            style = ArcadeButtonStyle.GOLD,
                            onClick = { showRestartConfirm = true },
                            height = 50.dp,
                            cornerRadius = 16.dp,
                            fontSize = 16.sp,
                            testTag = "pause_restart_button",
                            modifier = Modifier.fillMaxWidth()
                        )

                        PremiumPrimaryButton(
                            text = stringResource(R.string.nav_home),
                            icon = Icons.Filled.Home,
                            style = ArcadeButtonStyle.BLUE,
                            onClick = { showExitConfirm = true },
                            height = 50.dp,
                            cornerRadius = 16.dp,
                            fontSize = 16.sp,
                            testTag = "pause_home_button",
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (onOpenSettings != null) {
                            SecondaryButton(
                                text = "Audio & Theme Settings",
                                icon = Icons.Filled.Settings,
                                accent = AccentCyan,
                                onClick = onOpenSettings,
                                testTag = "pause_settings_button",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                FloatingModalBadge(
                    size = 68.dp,
                    backgroundBrush = Brush.verticalGradient(listOf(BlueBtnTop, BlueBtnBottom))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Pause,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
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
private fun PauseStatChip(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            ),
            color = AccentGold
        )
    }
}
