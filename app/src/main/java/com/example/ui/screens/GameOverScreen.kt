package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.GameMode
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeBlockEmblem
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBtnText
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GameOverScreen(
    state: GameUiState,
    onRetry: () -> Unit,
    onHome: () -> Unit
) {
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
            PremiumCard(
                cornerRadius = 20.dp,
                borderColor = GlassBorder,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp, vertical = 20.dp)
                ) {
                    // 1. Logo + Game Over title
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(com.example.ui.theme.ObsidianBg)
                                .border(1.5.dp, GlassBorder, RoundedCornerShape(14.dp))
                        ) {
                            ArcadeBlockEmblem(size = 34.dp)
                        }
                        Text(
                            text = stringResource(R.string.game_over_title),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 26.sp
                            ),
                            color = TextPrimary,
                            modifier = Modifier.testTag("game_over_title_text")
                        )
                    }

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
                                .clip(RoundedCornerShape(8.dp))
                                .background(AccentGold)
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    // 2. Score on top, Best Score directly underneath
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(BoardWellBg)
                            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "SCORE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    letterSpacing = 1.sp,
                                    fontSize = 11.sp
                                ),
                                color = TextSecondary
                            )
                            Text(
                                text = "${state.score}",
                                style = MaterialTheme.typography.displaySmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 36.sp
                                ),
                                color = AccentCyan,
                                modifier = Modifier.testTag("game_over_score_value")
                            )
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (state.mode == GameMode.CLASSIC) "BEST SCORE" else "TARGET SCORE",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    letterSpacing = 0.8.sp,
                                    fontSize = 11.sp
                                ),
                                color = TextSecondary
                            )
                            val bestOrTarget = if (state.mode == GameMode.CLASSIC) {
                                state.classicBestScore
                            } else {
                                state.currentChallengeLevel.targetScore
                            }
                            Box(contentAlignment = Alignment.Center) {
                                androidx.compose.foundation.layout.Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.EmojiEvents,
                                        contentDescription = null,
                                        tint = AccentGold,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "$bestOrTarget",
                                        style = MaterialTheme.typography.headlineSmall.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 20.sp
                                        ),
                                        color = AccentGold,
                                        modifier = Modifier.testTag("game_over_best_score_value")
                                    )
                                }
                            }
                        }
                    }

                    // 3. Play Again and Home buttons
                    PremiumPrimaryButton(
                        text = stringResource(R.string.play_again),
                        icon = Icons.Filled.Refresh,
                        style = ArcadeButtonStyle.GREEN,
                        onClick = onRetry,
                        height = 50.dp,
                        cornerRadius = 14.dp,
                        fontSize = 16.sp,
                        testTag = "game_over_retry_button",
                        modifier = Modifier.fillMaxWidth()
                    )

                    PremiumPrimaryButton(
                        text = stringResource(R.string.nav_home),
                        icon = Icons.Filled.Home,
                        style = ArcadeButtonStyle.SLATE,
                        onClick = onHome,
                        height = 48.dp,
                        cornerRadius = 14.dp,
                        fontSize = 15.sp,
                        testTag = "game_over_home_button",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
