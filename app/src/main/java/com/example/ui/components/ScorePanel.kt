package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.material.icons.filled.Bolt
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameMode
import com.example.ui.FloatingBanner
import com.example.ui.GameUiState
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
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
    val modeBadgeText = when {
        state.isDailyChallenge -> "DAILY GRAVITY"
        state.mode == GameMode.CLASSIC -> stringResource(R.string.mode_classic)
        else -> "Level ${state.currentChallengeLevel.levelNumber}"
    }
    val modeAccent = when {
        state.isDailyChallenge -> AccentPurple
        state.mode == GameMode.CLASSIC -> AccentCyan
        else -> AccentGold
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f, fill = false)
        ) {
            if (onBack != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard)
                        .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                        .clickable(onClick = onBack)
                        .testTag("gameplay_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.back_label),
                        tint = TextPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCard)
                    .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .clip(CircleShape)
                        .background(modeAccent)
                )
                Text(
                    text = modeBadgeText.uppercase(),
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.5.sp,
                        letterSpacing = 0.5.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(SurfaceCard)
                .border(1.dp, GlassBorder, RoundedCornerShape(12.dp))
                .padding(horizontal = 4.dp)
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
                    tint = TextSecondary,
                    modifier = Modifier.size(21.dp)
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
                    tint = TextSecondary,
                    modifier = Modifier.size(21.dp)
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .padding(5.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.verticalGradient(listOf(BlueBtnTop, BlueBtnBottom)))
                    .clickable(onClick = onPause)
                    .testTag("pause_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Pause,
                    contentDescription = stringResource(R.string.pause),
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
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
        animationSpec = tween(durationMillis = 200),
        label = "score_tick"
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StudioHudCard(
                icon = Icons.Filled.Star,
                accent = AccentGold,
                label = if (state.isNewBest && state.mode == GameMode.CLASSIC) "NEW BEST!" else stringResource(R.string.score_label),
                value = animatedScore.toString(),
                highlight = state.isNewBest && state.mode == GameMode.CLASSIC,
                badgeText = if (state.currentCascadeWave >= 2) "×${state.currentCascadeWave}" else null,
                modifier = Modifier
                    .weight(1f)
                    .testTag("score_card")
            )

            if (state.mode == GameMode.CLASSIC) {
                StudioHudCard(
                    icon = Icons.Filled.EmojiEvents,
                    accent = AccentCyan,
                    label = stringResource(R.string.best_label),
                    value = state.classicBestScore.toString(),
                    badgeText = if (state.bestCascadeDepth >= 2) "MAX ×${state.bestCascadeDepth}" else null,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("best_score_card")
                )
            } else {
                val lvl = state.currentChallengeLevel
                StudioHudCard(
                    icon = Icons.Filled.Flag,
                    accent = AccentEmerald,
                    label = stringResource(R.string.target_label),
                    value = lvl.targetScore.toString(),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("target_card")
                )
                StudioHudCard(
                    icon = Icons.Filled.Bolt,
                    accent = if (state.movesRemaining <= 3) AccentCoral else AccentGold,
                    label = stringResource(R.string.moves_label),
                    value = state.movesRemaining.toString(),
                    valueColor = if (state.movesRemaining <= 3) AccentCoral else TextPrimary,
                    modifier = Modifier
                        .weight(0.84f)
                        .testTag("moves_card")
                )
            }
        }

        if (state.mode == GameMode.CHALLENGE) {
            val lvl = state.currentChallengeLevel
            val progress = (state.score.toFloat() / lvl.targetScore.toFloat().coerceAtLeast(1f))
                .coerceIn(0f, 1f)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(SurfaceCard.copy(alpha = 0.88f))
                    .border(1.dp, GlassBorder.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 12.dp, vertical = 7.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${lvl.title} — ${lvl.subtitle}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 11.sp
                        ),
                        color = AccentGold
                    )
                }
                Spacer(Modifier.height(5.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    color = AccentGold,
                    trackColor = BoardWellBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(CircleShape)
                )
            }
        }
    }
}

/**
 * Fixed-height callout slot placed above the game board so line-clear / combo callouts
 * never push or shift the 8x8 board vertically and never overlap board cells.
 */
/**
 * UI-only feedback labels. The score and the core headline still come from the game engine;
 * this only maps the current cascade wave / clear count to a readable tier badge.
 */
private fun feedbackTierFor(headline: String, wave: Int): Pair<String, Color> {
    // The engine resets currentCascadeWave when the turn finalizes while the banner lingers.
    // The banner headline ("GRAVITY CHAIN x2!", "MEGA CASCADE x3!") keeps the authoritative
    // depth, so derive the combo tier from both sources instead of the transient wave alone.
    val comboFromHeadline = Regex("x(\\d+)", RegexOption.IGNORE_CASE)
        .find(headline)
        ?.groupValues
        ?.getOrNull(1)
        ?.toIntOrNull()
        ?: 0
    val depth = maxOf(wave, comboFromHeadline)
    return when {
        depth >= 5 -> "COMBO 5+" to AccentGold
        depth == 4 -> "COMBO 4" to AccentGold
        depth == 3 -> "COMBO 3" to AccentCoral
        depth == 2 -> "COMBO 2" to AccentPurple
        headline.contains("BOARD", ignoreCase = true) || headline.contains("TRIPLE", ignoreCase = true) ->
            "PERFECT" to AccentGold
        headline.contains("DOUBLE", ignoreCase = true) -> "GREAT" to AccentCyan
        else -> "LINE CLEAR" to AccentCyan
    }
}

@Composable
fun CascadeCalloutBar(
    floatingBanner: FloatingBanner?,
    modifier: Modifier = Modifier,
    cascadeWave: Int = 0
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
    ) {
        AnimatedVisibility(
            visible = floatingBanner != null,
            enter = fadeIn(tween(140)) + scaleIn(initialScale = 0.86f, animationSpec = tween(160)),
            exit = fadeOut(tween(180)) + scaleOut(targetScale = 0.92f, animationSpec = tween(180))
        ) {
            floatingBanner?.let { banner ->
                val (tierLabel, tierColor) = feedbackTierFor(banner.headline, cascadeWave)
                val accent = if (banner.isChain) AccentGold else tierColor
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier
                        .shadow(2.dp, CircleShape, clip = false)
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.25.dp, accent.copy(alpha = 0.8f), CircleShape)
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                        .testTag("cascade_banner")
                ) {
                    Text(
                        text = tierLabel,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 11.sp,
                            letterSpacing = 0.5.sp
                        ),
                        color = tierColor,
                        maxLines = 1
                    )
                    Box(
                        modifier = Modifier
                            .size(3.dp)
                            .clip(CircleShape)
                            .background(GlassBorder)
                    )
                    Text(
                        text = banner.headline,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 0.3.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = banner.pointsText,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        ),
                        color = accent,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioHudCard(
    icon: ImageVector,
    accent: Color,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    highlight: Boolean = false,
    badgeText: String? = null,
    valueColor: Color = TextPrimary
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(9.dp),
        modifier = modifier
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(SurfaceCard)
            .border(
                width = if (highlight) 1.5.dp else 1.dp,
                color = if (highlight) AccentGold else GlassBorder,
                shape = shape
            )
            .padding(horizontal = 11.dp, vertical = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(accent.copy(alpha = 0.14f))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = 9.5.sp,
                        letterSpacing = 0.8.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (highlight) AccentGold else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (badgeText != null) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 9.sp
                        ),
                        color = AccentGold,
                        maxLines = 1
                    )
                }
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 19.sp
                ),
                color = valueColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
