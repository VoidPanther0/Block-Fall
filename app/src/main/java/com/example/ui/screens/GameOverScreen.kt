package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridOff
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.components.FloatingModalBadge
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.components.SecondaryButton
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBtnText
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.RedBtnBottom
import com.example.ui.theme.RedBtnTop
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GameOverScreen(
    state: GameUiState,
    onRetry: () -> Unit,
    onHome: () -> Unit,
    onWatchAd: (() -> Unit)? = null
) {
    val subtitle = when (state.mode) {
        GameMode.CLASSIC -> "No valid moves remaining"
        GameMode.CHALLENGE -> "Level ${state.currentChallengeLevel.levelNumber} · Target ${state.currentChallengeLevel.targetScore} not reached"
    }
    val goalScore = when (state.mode) {
        GameMode.CLASSIC -> state.classicBestScore.coerceAtLeast(1)
        GameMode.CHALLENGE -> state.currentChallengeLevel.targetScore.coerceAtLeast(1)
    }
    val progressRatio = (state.score.toFloat() / goalScore.toFloat()).coerceIn(0f, 1f)

    BackHandler(onBack = onHome)

    CascadeScreenContainer(
        maxContentWidth = 440.dp,
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
                PremiumCard(
                    cornerRadius = 26.dp,
                    borderColor = AccentCoral.copy(alpha = 0.45f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 38.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 22.dp, end = 22.dp, top = 50.dp, bottom = 22.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.game_over_title),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            ),
                            color = TextPrimary
                        )

                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = TextSecondary
                        )

                        if (state.isNewBest && state.mode == GameMode.CLASSIC) {
                            Text(
                                text = "NEW HIGH SCORE!",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 12.sp
                                ),
                                color = GoldBtnText,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(AccentGold)
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(BoardWellBg)
                                .border(1.dp, GlassBorder.copy(alpha = 0.42f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "FINAL SCORE",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextSecondary
                            )
                            Text(
                                text = "${state.score}",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 42.sp
                                ),
                                color = AccentCyan
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(7.dp)
                                    .clip(CircleShape)
                                    .background(TextSecondary.copy(alpha = 0.16f))
                            ) {
                                if (progressRatio > 0f) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxHeight()
                                            .fillMaxWidth(progressRatio.coerceAtLeast(0.05f))
                                            .clip(CircleShape)
                                            .background(Brush.horizontalGradient(listOf(AccentCyan, AccentGold)))
                                    )
                                }
                            }
                            Text(
                                text = when (state.mode) {
                                    GameMode.CLASSIC -> "Best Score: ${state.classicBestScore}"
                                    GameMode.CHALLENGE -> "Target Score: ${state.currentChallengeLevel.targetScore}"
                                },
                                style = MaterialTheme.typography.titleMedium.copy(fontSize = 13.sp),
                                color = TextSecondary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GameOverMiniStat("Moves", "${state.movesPlayed}", Modifier.weight(1f))
                            GameOverMiniStat("Lines", "${state.totalLinesCleared}", Modifier.weight(1f))
                            GameOverMiniStat(
                                "Max Chain",
                                if (state.bestCascadeDepth > 0) "x${state.bestCascadeDepth}" else "—",
                                Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(2.dp))

                        PremiumPrimaryButton(
                            text = "Retry",
                            icon = Icons.Filled.Refresh,
                            style = ArcadeButtonStyle.GOLD,
                            onClick = onRetry,
                            height = 54.dp,
                            cornerRadius = 16.dp,
                            fontSize = 18.sp,
                            testTag = "game_over_retry_button",
                            modifier = Modifier.fillMaxWidth()
                        )

                        PremiumPrimaryButton(
                            text = stringResource(R.string.nav_home),
                            icon = Icons.Filled.Home,
                            style = ArcadeButtonStyle.BLUE,
                            onClick = onHome,
                            height = 50.dp,
                            cornerRadius = 16.dp,
                            fontSize = 17.sp,
                            testTag = "game_over_home_button",
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (onWatchAd != null) {
                            SecondaryButton(
                                text = "Watch Ad · +50 Gold",
                                icon = Icons.Filled.PlayCircle,
                                accent = AccentEmerald,
                                onClick = onWatchAd,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                FloatingModalBadge(
                    size = 76.dp,
                    backgroundBrush = Brush.verticalGradient(listOf(RedBtnTop, RedBtnBottom))
                ) {
                    Icon(
                        imageVector = Icons.Filled.GridOff,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GameOverMiniStat(
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
            .padding(vertical = 8.dp)
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
            color = if (label == "Max Chain" && value != "—") AccentGold else TextPrimary
        )
    }
}
