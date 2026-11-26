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
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameMode
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FloatingModalBadge
import com.example.ui.components.GoldMetallicBanner
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.components.RestartConfirmationDialog
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SwitchTrackOff
import com.example.ui.theme.SwitchTrackOn
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PauseScreen(
    state: GameUiState,
    onResume: () -> Unit,
    onRestartConfirmed: () -> Unit,
    onExitToHomeConfirmed: () -> Unit,
    onToggleSound: (Boolean) -> Unit = {},
    onToggleMusic: (Boolean) -> Unit = {}
) {
    var showRestartConfirm by remember { mutableStateOf(false) }

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
                    cornerRadius = 20.dp,
                    borderColor = AccentGold.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 18.dp, end = 18.dp, top = 38.dp, bottom = 16.dp)
                    ) {
                        GoldMetallicBanner(text = "PAUSED", isPurple = true)

                        Text(
                            text = when {
                                state.isDailyChallenge -> "Daily Gravity · ${state.currentChallengeLevel.title}"
                                state.mode == GameMode.CLASSIC -> "Classic Endless"
                                else -> "Level ${state.currentChallengeLevel.levelNumber} · ${state.currentChallengeLevel.title}"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.5.sp),
                            color = TextSecondary
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(BoardWellBg)
                                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                                .padding(vertical = 8.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            PauseStatChip("Score", "${state.score}")
                            PauseStatChip("Lines", "${state.totalLinesCleared}")
                            PauseStatChip("Moves", "${state.movesPlayed}")
                        }

                        // Sound and Music quick toggles
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            PauseAudioToggle(
                                label = "Sound",
                                icon = Icons.AutoMirrored.Filled.VolumeUp,
                                checked = state.settings.soundEnabled,
                                accent = AccentCyan,
                                onCheckedChange = onToggleSound,
                                testTag = "pause_sound_switch",
                                modifier = Modifier.weight(1f)
                            )
                            PauseAudioToggle(
                                label = "Music",
                                icon = Icons.Filled.MusicNote,
                                checked = state.settings.musicEnabled,
                                accent = AccentPurple,
                                onCheckedChange = onToggleMusic,
                                testTag = "pause_music_switch",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        PremiumPrimaryButton(
                            text = stringResource(R.string.resume),
                            icon = Icons.Filled.PlayArrow,
                            style = ArcadeButtonStyle.GREEN,
                            onClick = onResume,
                            height = 48.dp,
                            cornerRadius = 14.dp,
                            fontSize = 16.sp,
                            testTag = "pause_resume_button",
                            modifier = Modifier.fillMaxWidth()
                        )

                        PremiumPrimaryButton(
                            text = stringResource(R.string.restart),
                            icon = Icons.Filled.Refresh,
                            style = ArcadeButtonStyle.GOLD,
                            onClick = { showRestartConfirm = true },
                            height = 46.dp,
                            cornerRadius = 14.dp,
                            fontSize = 15.sp,
                            testTag = "pause_restart_button",
                            modifier = Modifier.fillMaxWidth()
                        )

                        PremiumPrimaryButton(
                            text = stringResource(R.string.nav_home),
                            icon = Icons.Filled.Home,
                            style = ArcadeButtonStyle.GOLD_BLUE,
                            onClick = onExitToHomeConfirmed,
                            height = 46.dp,
                            cornerRadius = 14.dp,
                            fontSize = 15.sp,
                            testTag = "pause_home_button",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                FloatingModalBadge(
                    size = 60.dp,
                    backgroundBrush = Brush.verticalGradient(listOf(BlueBtnTop, BlueBtnBottom))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Pause,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
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
}

@Composable
private fun PauseAudioToggle(
    label: String,
    icon: ImageVector,
    checked: Boolean,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(BoardWellBg)
            .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (checked) accent else TextSecondary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.5.sp
                ),
                color = TextPrimary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = SwitchTrackOn,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = SwitchTrackOff
            ),
            modifier = Modifier.testTag(testTag)
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
