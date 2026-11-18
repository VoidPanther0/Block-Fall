package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.ChallengeLevels
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.RoyalSkyBackdrop
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.OutfitFontFamily
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
        maxContentWidth = 420.dp,
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
            // 3 Golden Stars header matching the mockup
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 1..3) {
                    val earned = i <= stars
                    val starSize = if (i == 2) 72.dp else 54.dp
                    val yShift = if (i == 2) (-10).dp else 0.dp
                    Icon(
                        imageVector = if (earned) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (earned) "Earned star $i" else "Empty star $i",
                        tint = if (earned) AccentGold else TextSecondary.copy(alpha = 0.45f),
                        modifier = Modifier
                            .size(starSize)
                            .offset(y = yShift)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Level Complete!",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                ),
                color = Color.White
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Score",
                style = MaterialTheme.typography.titleMedium,
                color = TextSecondary
            )
            Text(
                text = "${state.score}",
                style = MaterialTheme.typography.displaySmall.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 48.sp
                ),
                color = AccentGold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Target: ${state.currentChallengeLevel.targetScore}",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )

            Spacer(Modifier.height(28.dp))

            if (hasNextLevel) {
                GlossyArcadeButton(
                    text = stringResource(R.string.next_level),
                    icon = Icons.Filled.PlayArrow,
                    style = ArcadeButtonStyle.GREEN,
                    onClick = onNextLevel,
                    height = 56.dp,
                    cornerRadius = 28.dp,
                    fontSize = 18.sp,
                    testTag = "level_complete_next_button",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "ALL 8 CHALLENGES MASTERED!",
                    style = MaterialTheme.typography.labelLarge,
                    color = AccentGold
                )
            }

            Spacer(Modifier.height(12.dp))

            GlossyArcadeButton(
                text = stringResource(R.string.nav_home),
                icon = Icons.Filled.Home,
                style = ArcadeButtonStyle.BLUE,
                onClick = onHome,
                height = 54.dp,
                cornerRadius = 27.dp,
                fontSize = 18.sp,
                testTag = "level_complete_home_button",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            GlossyArcadeButton(
                text = "Replay",
                icon = Icons.Filled.Refresh,
                style = ArcadeButtonStyle.GOLD,
                onClick = onReplay,
                height = 50.dp,
                cornerRadius = 25.dp,
                fontSize = 16.sp,
                testTag = "level_complete_replay_button",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ConfettiCelebrationOverlay() {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val confettiColors = listOf(AccentGold, AccentCoral, AccentCyan, AccentEmerald, Color(0xFF9C4DF4))
        val pieces = listOf(
            Triple(0.14f, 0.14f, 25f),
            Triple(0.28f, 0.09f, -18f),
            Triple(0.46f, 0.07f, 40f),
            Triple(0.66f, 0.10f, -32f),
            Triple(0.84f, 0.15f, 20f),
            Triple(0.20f, 0.25f, -45f),
            Triple(0.78f, 0.24f, 35f),
            Triple(0.12f, 0.34f, 15f),
            Triple(0.88f, 0.32f, -22f)
        )
        pieces.forEachIndexed { idx, (xf, yf, angle) ->
            val center = Offset(size.width * xf, size.height * yf)
            val c = confettiColors[idx % confettiColors.size]
            rotate(degrees = angle, pivot = center) {
                drawRect(
                    color = c.copy(alpha = 0.85f),
                    topLeft = Offset(center.x - 8f, center.y - 4f),
                    size = Size(16f, 8f)
                )
            }
        }
    }
}
