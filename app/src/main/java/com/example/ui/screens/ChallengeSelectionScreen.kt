package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.ChallengeLevel
import com.example.game.ChallengeLevelProgress
import com.example.game.ChallengeLevels
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.BoardPreview
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBtnBottom
import com.example.ui.theme.GoldBtnText
import com.example.ui.theme.GoldBtnTop
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
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
    val selectedProgress = state.challengeProgress[selectedLevelNumber]
    val totalStars = state.challengeProgress.values.sumOf { it.stars }
    val completedCount = state.challengeProgress.values.count { it.completed }
    val maxStars = ChallengeLevels.levels.size * 3

    CascadeScreenContainer(
        maxContentWidth = 940.dp,
        modifier = Modifier.testTag("challenge_selection_screen")
    ) {
        ScreenTopBar(
            title = "Challenges",
            onBack = onBack,
            backTestTag = "challenge_selection_back_button"
        ) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceCardElevated)
                    .border(1.dp, AccentGold.copy(alpha = 0.48f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "★ $totalStars/$maxStars",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp
                    ),
                    color = AccentGold
                )
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth >= 660.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        ChallengeLevelGrid(
                            unlockedChallengeLevel = state.unlockedChallengeLevel,
                            selectedLevelNumber = selectedLevelNumber,
                            challengeProgress = state.challengeProgress,
                            completedCount = completedCount,
                            onSelectLevel = { selectedLevelNumber = it },
                            modifier = Modifier.weight(1f)
                        )
                        ChallengeInspectorCard(
                            level = selectedLevel,
                            progress = selectedProgress,
                            onPlay = { onPlayLevel(selectedLevel.levelNumber) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    ChallengeLevelGrid(
                        unlockedChallengeLevel = state.unlockedChallengeLevel,
                        selectedLevelNumber = selectedLevelNumber,
                        challengeProgress = state.challengeProgress,
                        completedCount = completedCount,
                        onSelectLevel = { selectedLevelNumber = it }
                    )
                    ChallengeInspectorCard(
                        level = selectedLevel,
                        progress = selectedProgress,
                        onPlay = { onPlayLevel(selectedLevel.levelNumber) }
                    )
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun ChallengeLevelGrid(
    unlockedChallengeLevel: Int,
    selectedLevelNumber: Int,
    challengeProgress: Map<Int, ChallengeLevelProgress>,
    completedCount: Int,
    onSelectLevel: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val rows = ChallengeLevels.levels.chunked(3)
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier
    ) {
        rows.forEachIndexed { rowIndex, rowLevels ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowLevels.forEach { level ->
                    val isUnlocked = level.levelNumber <= unlockedChallengeLevel
                    val isSelected = level.levelNumber == selectedLevelNumber
                    ChallengeGridTile(
                        level = level,
                        isUnlocked = isUnlocked,
                        isSelected = isSelected,
                        progress = challengeProgress[level.levelNumber],
                        onClick = { if (isUnlocked) onSelectLevel(level.levelNumber) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowIndex == rows.lastIndex && rowLevels.size == 2) {
                    val masteryShape = RoundedCornerShape(18.dp)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .clip(masteryShape)
                            .background(SurfaceCard.copy(alpha = 0.85f))
                            .border(1.dp, GlassBorder.copy(alpha = 0.45f), masteryShape)
                            .padding(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = "Challenge completion count",
                            tint = if (completedCount == ChallengeLevels.levels.size) AccentGold else TextSecondary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "$completedCount/${ChallengeLevels.levels.size}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 14.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Cleared",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ChallengeInspectorCard(
    level: ChallengeLevel,
    progress: ChallengeLevelProgress?,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val previewBoard = remember(level.levelNumber) { level.buildBoard() }

    FrostedGlassCard(
        cornerRadius = 22.dp,
        borderColor = AccentGold.copy(alpha = 0.42f),
        modifier = modifier
            .fillMaxWidth()
            .testTag("challenge_target_summary_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BoardPreview(
                    board = previewBoard,
                    modifier = Modifier.widthIn(max = 104.dp).fillMaxWidth(0.32f)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "LEVEL ${level.levelNumber}",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        ),
                        color = AccentGold
                    )
                    Text(
                        text = level.title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = level.subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextSecondary
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InspectorStatChip("Target", "${level.targetScore}", Modifier.weight(1f))
                InspectorStatChip("Moves", "${level.moveLimit}", Modifier.weight(1f))
                InspectorStatChip("Best", "${progress?.bestScore ?: 0}", Modifier.weight(1f))
            }

            GlossyArcadeButton(
                text = "Play Level ${level.levelNumber}",
                icon = Icons.Filled.PlayArrow,
                style = ArcadeButtonStyle.GOLD,
                onClick = onPlay,
                height = 52.dp,
                cornerRadius = 16.dp,
                fontSize = 17.sp,
                testTag = "challenge_play_button",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun InspectorStatChip(
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
            .padding(vertical = 8.dp, horizontal = 6.dp)
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
                fontSize = 17.sp
            ),
            color = TextPrimary
        )
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
    val shape = RoundedCornerShape(18.dp)
    val bgBrush = when {
        isSelected -> Brush.verticalGradient(listOf(GoldBtnTop, GoldBtnBottom))
        isUnlocked -> Brush.verticalGradient(listOf(BlueBtnTop, BlueBtnBottom))
        else -> Brush.verticalGradient(listOf(SurfaceCardElevated.copy(alpha = 0.6f), SurfaceCard.copy(alpha = 0.8f)))
    }
    val borderColor = when {
        isSelected -> TextPrimary
        isUnlocked -> AccentGold.copy(alpha = 0.4f)
        else -> GlassBorder.copy(alpha = 0.32f)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .aspectRatio(1f)
            .shadow(if (isUnlocked) 6.dp else 1.dp, shape, clip = false)
            .clip(shape)
            .background(bgBrush)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, shape)
            .clickable(enabled = isUnlocked, onClick = onClick)
            .padding(6.dp)
            .testTag("challenge_level_card_${level.levelNumber}")
    ) {
        if (!isUnlocked) {
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = "Locked level ${level.levelNumber}",
                tint = TextSecondary.copy(alpha = 0.7f),
                modifier = Modifier.size(26.dp)
            )
        } else {
            Text(
                text = "${level.levelNumber}",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 28.sp
                ),
                color = if (isSelected) GoldBtnText else TextPrimary
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
                            isSelected -> GoldBtnText.copy(alpha = 0.4f)
                            else -> TextPrimary.copy(alpha = 0.4f)
                        },
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}
