package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.ChallengeLevels
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.RoyalSkyBackdrop
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun LevelCompleteScreen(
    state: GameUiState,
    onNextLevel: () -> Unit,
    onReplay: () -> Unit,
    onHome: () -> Unit
) {
    val hasNextLevel = state.currentChallengeLevel.levelNumber < ChallengeLevels.levels.size
    val stars = state.lastChallengeStarsEarned.coerceIn(1, 3)

    val starScales = remember { List(3) { Animatable(0.4f) } }
    LaunchedEffect(stars) {
        starScales.forEachIndexed { index, anim ->
            delay(index * 90L)
            anim.animateTo(1f, spring(dampingRatio = 0.55f, stiffness = 420f))
        }
    }

    BackHandler(onBack = onHome)

    CascadeScreenContainer(
        maxContentWidth = 440.dp,
        backdrop = {
            RoyalSkyBackdrop()
            ConfettiCelebrationOverlay()
        },
        modifier = Modifier.testTag("level_complete_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    val earned = i <= stars
                    val starSize = if (i == 2) 70.dp else 52.dp
                    val yShift = if (i == 2) (-10).dp else 0.dp
                    Icon(
                        imageVector = if (earned) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (earned) "Earned star $i" else "Empty star $i",
                        tint = if (earned) AccentGold else TextSecondary.copy(alpha = 0.4f),
                        modifier = Modifier
                            .size(starSize)
                            .offset(y = yShift)
                            .graphicsLayer {
                                val s = starScales[i - 1].value
                                scaleX = s
                                scaleY = s
                            }
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.challenge_complete_title),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 30.sp
                ),
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Level ${state.currentChallengeLevel.levelNumber} · ${state.currentChallengeLevel.title}",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = TextSecondary
            )

            Spacer(Modifier.height(16.dp))

            FrostedGlassCard(
                cornerRadius = 22.dp,
                borderColor = AccentGold.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp)
                ) {
                    Text(
                        text = "SCORE EARNED",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextSecondary
                    )
                    Text(
                        text = "${state.score}",
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 44.sp
                        ),
                        color = AccentGold
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CompleteMetricChip("Target", "${state.currentChallengeLevel.targetScore}", Modifier.weight(1f))
                        CompleteMetricChip("Moves Left", "${state.movesRemaining}", Modifier.weight(1f))
                        CompleteMetricChip(
                            "Max Chain",
                            if (state.bestCascadeDepth > 0) "x${state.bestCascadeDepth}" else "—",
                            Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (hasNextLevel) {
                GlossyArcadeButton(
                    text = stringResource(R.string.next_level),
                    icon = Icons.Filled.PlayArrow,
                    style = ArcadeButtonStyle.GREEN,
                    onClick = onNextLevel,
                    height = 54.dp,
                    cornerRadius = 16.dp,
                    fontSize = 18.sp,
                    testTag = "level_complete_next_button",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "ALL 8 CHALLENGES MASTERED!",
                    style = MaterialTheme.typography.labelLarge.copy(fontFamily = OutfitFontFamily),
                    color = AccentGold
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlossyArcadeButton(
                    text = "Replay",
                    icon = Icons.Filled.Refresh,
                    style = ArcadeButtonStyle.GOLD,
                    onClick = onReplay,
                    height = 50.dp,
                    cornerRadius = 16.dp,
                    fontSize = 16.sp,
                    testTag = "level_complete_replay_button",
                    modifier = Modifier.weight(1f)
                )
                GlossyArcadeButton(
                    text = stringResource(R.string.nav_home),
                    icon = Icons.Filled.Home,
                    style = ArcadeButtonStyle.BLUE,
                    onClick = onHome,
                    height = 50.dp,
                    cornerRadius = 16.dp,
                    fontSize = 16.sp,
                    testTag = "level_complete_home_button",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun CompleteMetricChip(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BoardWellBg)
            .border(1.dp, GlassBorder.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
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
                fontSize = 16.sp
            ),
            color = TextPrimary
        )
    }
}

@Composable
private fun ConfettiCelebrationOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val confettiColors = listOf(AccentGold, AccentCoral, AccentCyan, AccentEmerald, TextPrimary)
        val pieces = listOf(
            Triple(0.14f, 0.12f, 25f),
            Triple(0.28f, 0.08f, -18f),
            Triple(0.46f, 0.06f, 40f),
            Triple(0.66f, 0.09f, -32f),
            Triple(0.84f, 0.14f, 20f),
            Triple(0.20f, 0.24f, -45f),
            Triple(0.78f, 0.23f, 35f),
            Triple(0.11f, 0.33f, 15f),
            Triple(0.89f, 0.30f, -22f)
        )
        pieces.forEachIndexed { idx, (xf, yf, angle) ->
            val center = Offset(size.width * xf, size.height * yf)
            val c = confettiColors[idx % confettiColors.size]
            rotate(degrees = angle, pivot = center) {
                drawRect(
                    color = c.copy(alpha = 0.78f),
                    topLeft = Offset(center.x - 7f, center.y - 3.5f),
                    size = Size(14f, 7f)
                )
            }
        }
    }
}
