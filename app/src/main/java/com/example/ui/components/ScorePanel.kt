package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameMode
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
fun TopGameBar(
    mode: GameMode,
    onSwitchMode: (GameMode) -> Unit,
    onShowRules: () -> Unit,
    onRestart: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Brand mark + Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(
                        Brush.linearGradient(listOf(AccentCyan, AccentGold))
                    )
            )
            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.6.sp
                ),
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Mode switcher pill
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard)
                .border(1.dp, BoardRim, RoundedCornerShape(12.dp))
                .padding(3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            ModePill(
                label = stringResource(R.string.mode_classic),
                selected = mode == GameMode.CLASSIC,
                onClick = { onSwitchMode(GameMode.CLASSIC) },
                testTag = "mode_classic_tab"
            )
            ModePill(
                label = stringResource(R.string.mode_challenge),
                selected = mode == GameMode.CHALLENGE,
                onClick = { onSwitchMode(GameMode.CHALLENGE) },
                testTag = "mode_challenge_tab"
            )
        }

        // Utility buttons (Help, Restart, Pause)
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onShowRules,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("rules_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                    contentDescription = stringResource(R.string.how_to_play),
                    tint = TextSecondary
                )
            }
            IconButton(
                onClick = onRestart,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("restart_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Refresh,
                    contentDescription = stringResource(R.string.restart),
                    tint = TextSecondary
                )
            }
            IconButton(
                onClick = onPause,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Pause,
                    contentDescription = stringResource(R.string.pause),
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

@Composable
private fun ModePill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    val bg = if (selected) SurfaceCardElevated else Color.Transparent
    val textColor = if (selected) AccentCyan else TextSecondary
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                letterSpacing = 0.4.sp
            ),
            color = textColor,
            maxLines = 1
        )
    }
}

@Composable
fun ScorePanel(
    state: GameUiState,
    modifier: Modifier = Modifier
) {
    val animatedScore by animateIntAsState(
        targetValue = state.score,
        animationSpec = tween(durationMillis = 220),
        label = "score_tick"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Current Score Card
            HudStatCard(
                label = stringResource(R.string.score_label),
                value = animatedScore.toString(),
                accent = AccentCyan,
                badge = if (state.isNewBest && state.mode == GameMode.CLASSIC) {
                    stringResource(R.string.new_best_badge)
                } else null,
                modifier = Modifier
                    .weight(1f)
                    .testTag("score_card")
            )

            if (state.mode == GameMode.CLASSIC) {
                HudStatCard(
                    label = stringResource(R.string.best_label),
                    value = state.classicBestScore.toString(),
                    accent = AccentGold,
                    badge = if (state.bestCascadeDepth >= 2) "MAX x${state.bestCascadeDepth}" else null,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("best_score_card")
                )
            } else {
                HudStatCard(
                    label = stringResource(R.string.target_label),
                    value = state.currentChallengeLevel.targetScore.toString(),
                    accent = AccentEmerald,
                    badge = "LVL ${state.currentChallengeLevel.levelNumber}",
                    modifier = Modifier
                        .weight(0.95f)
                        .testTag("target_card")
                )
                HudStatCard(
                    label = stringResource(R.string.moves_label),
                    value = state.movesRemaining.toString(),
                    accent = if (state.movesRemaining <= 3) AccentCoral else AccentGold,
                    badge = null,
                    modifier = Modifier
                        .weight(0.75f)
                        .testTag("moves_card")
                )
            }
        }

        // Challenge progress bar + subtitle OR active cascade indicator
        if (state.mode == GameMode.CHALLENGE) {
            val progress = (state.score.toFloat() / state.currentChallengeLevel.targetScore.toFloat())
                .coerceIn(0f, 1f)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .border(1.dp, BoardRim, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.currentChallengeLevel.title} — ${state.currentChallengeLevel.subtitle}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                        color = AccentEmerald
                    )
                }
                Spacer(Modifier.height(5.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    color = AccentEmerald,
                    trackColor = SurfaceCardElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(CircleShape)
                )
            }
        }

        AnimatedVisibility(visible = state.currentCascadeWave >= 1) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(AccentGold.copy(alpha = 0.14f))
                    .border(1.dp, AccentGold.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .padding(vertical = 4.dp)
                    .testTag("combo_indicator")
            ) {
                Text(
                    text = if (state.currentCascadeWave >= 2) {
                        "GRAVITY CASCADE x${state.currentCascadeWave} ACTIVE!"
                    } else {
                        "LINE CLEAR · GRAVITY DROPPING..."
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.9.sp
                    ),
                    color = AccentGold
                )
            }
        }
    }
}

@Composable
private fun HudStatCard(
    label: String,
    value: String,
    accent: Color,
    badge: String?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard)
            .border(1.dp, BoardRim, RoundedCornerShape(14.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
            if (badge != null) {
                Text(
                    text = badge,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = accent,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(accent.copy(alpha = 0.16f))
                        .padding(horizontal = 5.dp, vertical = 2.dp)
                )
            }
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge.copy(
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
    }
}
