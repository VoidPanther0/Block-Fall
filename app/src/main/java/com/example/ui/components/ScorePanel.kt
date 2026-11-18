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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
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
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextSecondary

@Composable
fun TopGameBar(
    state: GameUiState,
    onShowHowToPlay: () -> Unit,
    onRestart: () -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("gameplay_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.back_label),
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard.copy(alpha = 0.78f))
                    .border(1.dp, GlassBorder.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = if (state.mode == GameMode.CLASSIC) {
                        stringResource(R.string.mode_classic)
                    } else {
                        "Level ${state.currentChallengeLevel.levelNumber}"
                    },
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    ),
                    color = Color.White,
                    maxLines = 1
                )
            }
        }

        // Utility buttons (Help, Restart, Glossy Blue Pause)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(
                onClick = onShowHowToPlay,
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
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .padding(4.dp)
                    .shadow(4.dp, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(BlueBtnTop, BlueBtnBottom)))
                    .border(1.dp, Color.White.copy(alpha = 0.45f), CircleShape)
                    .clickable(onClick = onPause)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Pause,
                    contentDescription = stringResource(R.string.pause),
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
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
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Primary Score + Best/Target HUD row matching the Gold Star / Gold Trophy mockup
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HudPill(
                icon = Icons.Filled.Star,
                iconTint = AccentGold,
                label = if (state.isNewBest && state.mode == GameMode.CLASSIC) "NEW!" else stringResource(R.string.score_label),
                value = animatedScore.toString(),
                modifier = Modifier
                    .weight(1f)
                    .testTag("score_card")
            )

            if (state.mode == GameMode.CLASSIC) {
                HudPill(
                    icon = Icons.Filled.EmojiEvents,
                    iconTint = AccentGold,
                    label = stringResource(R.string.best_label),
                    value = state.classicBestScore.toString(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("best_score_card")
                )
            } else {
                HudPill(
                    icon = Icons.Filled.Flag,
                    iconTint = AccentEmerald,
                    label = "Target",
                    value = state.currentChallengeLevel.targetScore.toString(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("target_card")
                )
                HudPill(
                    icon = Icons.Filled.EmojiEvents,
                    iconTint = if (state.movesRemaining <= 3) AccentCoral else AccentGold,
                    label = stringResource(R.string.moves_label),
                    value = state.movesRemaining.toString(),
                    valueColor = if (state.movesRemaining <= 3) AccentCoral else Color.White,
                    modifier = Modifier
                        .weight(0.82f)
                        .testTag("moves_card")
                )
            }
        }

        // Challenge progress bar
        if (state.mode == GameMode.CHALLENGE) {
            val progress = (state.score.toFloat() / state.currentChallengeLevel.targetScore.toFloat())
                .coerceIn(0f, 1f)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard.copy(alpha = 0.85f))
                    .border(1.dp, GlassBorder.copy(alpha = 0.45f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${state.currentChallengeLevel.title} — ${state.currentChallengeLevel.subtitle}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 11.sp),
                        color = AccentGold
                    )
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    color = AccentGold,
                    trackColor = SurfaceCardElevated,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )
            }
        }

        // Glowing "Combo x2" arcade callout matching the mockup!
        AnimatedVisibility(visible = state.currentCascadeWave >= 1) {
            val labelText = if (state.currentCascadeWave >= 2) {
                "Combo x${state.currentCascadeWave}"
            } else {
                "Line Clear!"
            }
            Text(
                text = labelText,
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 23.sp,
                    shadow = Shadow(
                        color = Color(0xFF0A1438),
                        offset = Offset(0f, 3f),
                        blurRadius = 4f
                    )
                ),
                color = AccentGold,
                modifier = Modifier
                    .padding(vertical = 1.dp)
                    .testTag("combo_indicator")
            )
        }
    }
}

@Composable
private fun HudPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = Color.White
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.verticalGradient(
                    listOf(SurfaceCardElevated.copy(alpha = 0.85f), SurfaceCard.copy(alpha = 0.90f))
                )
            )
            .border(1.dp, GlassBorder.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(22.dp)
        )
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 10.sp),
                color = TextSecondary,
                maxLines = 1
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp
                ),
                color = valueColor,
                maxLines = 1
            )
        }
    }
}
