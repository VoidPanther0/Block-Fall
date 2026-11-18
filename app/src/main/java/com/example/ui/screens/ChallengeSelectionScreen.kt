package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.ChallengeLevel
import com.example.game.ChallengeLevelProgress
import com.example.game.ChallengeLevels
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBtnBottom
import com.example.ui.theme.GoldBtnText
import com.example.ui.theme.GoldBtnTop
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextSecondary

@Composable
fun ChallengeSelectionScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onPlayLevel: (Int) -> Unit
) {
    var selectedLevelNumber by remember(state.unlockedChallengeLevel) {
        mutableIntStateOf(state.unlockedChallengeLevel.coerceIn(1, ChallengeLevels.levels.size))
    }
    val selectedLevel = ChallengeLevels.getLevel(selectedLevelNumber)

    CascadeScreenContainer(
        maxContentWidth = 540.dp,
        modifier = Modifier.testTag("challenge_selection_screen")
    ) {
        ScreenTopBar(
            title = "Challenges",
            onBack = onBack,
            backTestTag = "challenge_selection_back_button"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 3x3 Grid matching mockup (Levels 1..8 + 9th Lock slot)
            val rows = ChallengeLevels.levels.chunked(3)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                rows.forEachIndexed { rowIndex, rowLevels ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        rowLevels.forEach { level ->
                            val isUnlocked = level.levelNumber <= state.unlockedChallengeLevel
                            val isSelected = level.levelNumber == selectedLevelNumber
                            val progress = state.challengeProgress[level.levelNumber]
                            ChallengeGridTile(
                                level = level,
                                isUnlocked = isUnlocked,
                                isSelected = isSelected,
                                progress = progress,
                                onClick = {
                                    if (isUnlocked) {
                                        selectedLevelNumber = level.levelNumber
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowIndex == rows.lastIndex && rowLevels.size == 2) {
                            // 9th slot: Lock tile matching mockup grid symmetry
                            val lockShape = RoundedCornerShape(20.dp)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(lockShape)
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                SurfaceCardElevated.copy(alpha = 0.65f),
                                                SurfaceCard.copy(alpha = 0.78f)
                                            )
                                        )
                                    )
                                    .border(1.5.dp, GlassBorder.copy(alpha = 0.45f), lockShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Lock,
                                    contentDescription = "More challenges locked",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Target Score & Move Limit card matching mockup bottom card
            FrostedGlassCard(
                cornerRadius = 20.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("challenge_target_summary_card")
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "Target Score",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                        color = TextSecondary
                    )
                    Text(
                        text = "${selectedLevel.targetScore}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        ),
                        color = Color.White
                    )
                    Text(
                        text = "Move Limit: ${selectedLevel.moveLimit}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp
                        ),
                        color = TextSecondary
                    )
                }
            }

            // Yellow Play button directly launches the selected level
            GlossyArcadeButton(
                text = "Play",
                style = ArcadeButtonStyle.GOLD,
                onClick = { onPlayLevel(selectedLevel.levelNumber) },
                height = 54.dp,
                cornerRadius = 27.dp,
                fontSize = 19.sp,
                testTag = "challenge_play_button",
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun ChallengeGridTile(
    level: ChallengeLevel,
    isUnlocked: Boolean,
    isSelected: Boolean,
    progress: ChallengeLevelProgress?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stars = progress?.stars ?: 0
    val shape = RoundedCornerShape(20.dp)
    val bgBrush = when {
        isSelected -> Brush.verticalGradient(listOf(GoldBtnTop, GoldBtnBottom))
        isUnlocked -> Brush.verticalGradient(listOf(Color(0xFF4270D6), Color(0xFF284CA8)))
        else -> Brush.verticalGradient(
            listOf(
                SurfaceCardElevated.copy(alpha = 0.65f),
                SurfaceCard.copy(alpha = 0.78f)
            )
        )
    }
    val borderColor = when {
        isSelected -> Color(0xFFFFF3B0)
        isUnlocked -> GlassBorder.copy(alpha = 0.75f)
        else -> GlassBorder.copy(alpha = 0.35f)
    }
    val numberColor = Color.White

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .aspectRatio(1f)
            .shadow(if (isUnlocked) 6.dp else 2.dp, shape, clip = false)
            .clip(shape)
            .background(bgBrush)
            .border(1.5.dp, borderColor, shape)
            .clickable(enabled = isUnlocked, onClick = onClick)
            .padding(8.dp)
            .testTag("challenge_level_card_${level.levelNumber}")
    ) {
        if (!isUnlocked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Locked level ${level.levelNumber}",
                tint = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.size(30.dp)
            )
        } else {
            Text(
                text = "${level.levelNumber}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                ),
                color = numberColor
            )
            Spacer(Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                for (starIdx in 1..3) {
                    val filled = starIdx <= stars
                    Icon(
                        imageVector = if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                        contentDescription = null,
                        tint = when {
                            filled && isSelected -> GoldBtnText
                            filled -> AccentGold
                            isSelected -> GoldBtnText.copy(alpha = 0.55f)
                            else -> Color.White.copy(alpha = 0.45f)
                        },
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
