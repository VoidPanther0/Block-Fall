package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameMode
import com.example.game.GameStatus
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FloatingModalBadge
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.theme.AccentGold
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextSecondary

@Composable
fun GameOverScreen(
    state: GameUiState,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
    val isChallengeLoss = state.mode == GameMode.CHALLENGE || state.status == GameStatus.CHALLENGE_LOST
    val title = if (isChallengeLoss) {
        stringResource(R.string.challenge_failed_title)
    } else {
        stringResource(R.string.game_over_title)
    }

    BackHandler(onBack = onHome)

    CascadeScreenContainer(
        maxContentWidth = 420.dp,
        modifier = Modifier.testTag("game_over_screen")
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
                FrostedGlassCard(
                    cornerRadius = 28.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 40.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 52.dp, bottom = 24.dp)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 30.sp
                            ),
                            color = Color.White
                        )

                        if (state.isNewBest && state.mode == GameMode.CLASSIC) {
                            Text(
                                text = "NEW HIGH SCORE!",
                                style = MaterialTheme.typography.labelLarge,
                                color = AccentGold,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentGold.copy(alpha = 0.18f))
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(Modifier.height(4.dp))

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
                                fontSize = 44.sp
                            ),
                            color = AccentGold
                        )

                        Spacer(Modifier.height(2.dp))

                        Text(
                            text = if (state.mode == GameMode.CLASSIC) "Best Score" else "Target Score",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextSecondary
                        )
                        Text(
                            text = if (state.mode == GameMode.CLASSIC) {
                                "${state.classicBestScore}"
                            } else {
                                "${state.currentChallengeLevel.targetScore}"
                            },
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp
                            ),
                            color = Color.White
                        )

                        Spacer(Modifier.height(10.dp))

                        GlossyArcadeButton(
                            text = "Retry",
                            icon = Icons.Filled.Refresh,
                            style = ArcadeButtonStyle.GOLD,
                            onClick = onRetry,
                            height = 54.dp,
                            cornerRadius = 27.dp,
                            fontSize = 18.sp,
                            testTag = "game_over_retry_button",
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlossyArcadeButton(
                            text = stringResource(R.string.nav_home),
                            icon = Icons.Filled.Home,
                            style = ArcadeButtonStyle.BLUE,
                            onClick = onHome,
                            height = 52.dp,
                            cornerRadius = 26.dp,
                            fontSize = 17.sp,
                            testTag = "game_over_home_button",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Protruding circular slate-blue sad face badge matching mockup
                FloatingModalBadge(
                    size = 80.dp,
                    backgroundBrush = Brush.verticalGradient(
                        listOf(Color(0xFFB5C7EA), Color(0xFF7D95C4))
                    )
                ) {
                    Canvas(modifier = Modifier.size(48.dp)) {
                        val eyeRadius = size.width * 0.085f
                        drawCircle(
                            color = Color(0xFF1A2952),
                            radius = eyeRadius,
                            center = Offset(size.width * 0.34f, size.height * 0.40f)
                        )
                        drawCircle(
                            color = Color(0xFF1A2952),
                            radius = eyeRadius,
                            center = Offset(size.width * 0.66f, size.height * 0.40f)
                        )
                        drawArc(
                            color = Color(0xFF1A2952),
                            startAngle = 205f,
                            sweepAngle = 130f,
                            useCenter = false,
                            topLeft = Offset(size.width * 0.28f, size.height * 0.58f),
                            size = Size(size.width * 0.44f, size.height * 0.30f),
                            style = Stroke(width = size.width * 0.09f, cap = StrokeCap.Round)
                        )
                    }
                }
            }
        }
    }
}
