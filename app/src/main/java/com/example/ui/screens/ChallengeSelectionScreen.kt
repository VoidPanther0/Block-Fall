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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
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
import com.example.game.EconomyEngine
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.BoardPreview
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.PremiumQuickNav
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.components.PrimaryNavTab
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.CascadeTextStyles
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
import kotlin.math.ceil

@Composable
fun ChallengeSelectionScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onPlayLevel: (Int) -> Unit,
    onNavigateTab: ((PrimaryNavTab) -> Unit)? = null
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
        bottomBar = if (onNavigateTab != null) {
            {
                PremiumQuickNav(
                    selectedTab = PrimaryNavTab.CHALLENGES,
                    onSelectTab = onNavigateTab
                )
            }
        } else null,
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
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
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
                    ChallengeProgressBanner(
                        completedCount = completedCount,
                        totalLevels = ChallengeLevels.levels.size,
                        totalStars = totalStars,
                        maxStars = maxStars
                    )
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
private fun ChallengeProgressBanner(
    completedCount: Int,
    totalLevels: Int,
    totalStars: Int,
    maxStars: Int
) {
    PremiumCard(
        cornerRadius = 16.dp,
        borderColor = AccentGold.copy(alpha = 0.34f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "CAMPAIGN PROGRESS",
                    style = CascadeTextStyles.SectionKicker,
                    color = AccentGold
                )
                Text(
                    text = "$completedCount of $totalLevels Levels Cleared",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
            }
            Text(
                text = "★ $totalStars/$maxStars",
                style = CascadeTextStyles.NumericBalance.copy(fontSize = 15.sp),
                color = AccentGold
            )
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
    val (tierLabel, tierColor) = when (level.levelNumber) {
        in 1..2 -> "Intro" to AccentEmerald
        in 3..4 -> "Tactical" to AccentCyan
        in 5..6 -> "Advanced" to AccentGold
        else -> "Master" to AccentPurple
    }
    val twoStarTarget = ceil(level.targetScore * 1.25).toInt()
    val threeStarTarget = ceil(level.targetScore * 1.50).toInt()

    PremiumCard(
        cornerRadius = 22.dp,
        borderColor = AccentGold.copy(alpha = 0.45f),
        modifier = modifier
            .fillMaxWidth()
            .testTag("challenge_target_summary_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BoardPreview(
                    board = previewBoard,
                    modifier = Modifier
                        .widthIn(max = 96.dp)
                        .fillMaxWidth(0.30f)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "LEVEL ${level.levelNumber}",
                            style = CascadeTextStyles.SectionKicker,
                            color = AccentGold
                        )
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(tierColor.copy(alpha = 0.16f))
                                .border(1.dp, tierColor.copy(alpha = 0.45f), CircleShape)
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = tierLabel.uppercase(),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                color = tierColor
                            )
                        }
                    }
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
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
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

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BoardWellBg)
                    .border(1.dp, GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 7.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "1★ ${level.targetScore}",
                    style = CascadeTextStyles.NumericBalance.copy(fontSize = 11.sp),
                    color = TextSecondary
                )
                Text(
                    text = "2★ $twoStarTarget / 3m",
                    style = CascadeTextStyles.NumericBalance.copy(fontSize = 10.sp),
                    color = AccentCyan,
                    maxLines = 1
                )
                Text(
                    text = "3★ $threeStarTarget / 5m",
                    style = CascadeTextStyles.NumericBalance.copy(fontSize = 10.sp),
                    color = AccentGold,
                    maxLines = 1
                )
            }
            Text(
                text = "Star thresholds: target score OR moves left (3m / 5m).",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            val (rewardGold, rewardDiamonds) = EconomyEngine.calculateChallengeWinReward(3)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(BoardWellBg)
                    .border(1.dp, GlassBorder.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "OBJECTIVES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            fontSize = 9.5.sp
                        ),
                        color = AccentCyan
                    )
                    Text(
                        text = level.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "3★ REWARD",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.6.sp,
                            fontSize = 9.5.sp
                        ),
                        color = AccentGold
                    )
                    Text(
                        text = "$rewardGold Gold · $rewardDiamonds Gems",
                        style = CascadeTextStyles.NumericBalance.copy(fontSize = 11.sp),
                        color = AccentGold,
                        maxLines = 1
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val completionText = when {
                    progress?.completed == true -> "COMPLETED · ${"★".repeat(progress.stars.coerceIn(0, 3))}"
                    (progress?.bestScore ?: 0) > 0 -> "IN PROGRESS · BEST ${progress?.bestScore}"
                    else -> "NOT STARTED"
                }
                Text(
                    text = completionText,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 10.sp
                    ),
                    color = if (progress?.completed == true) AccentEmerald else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            PremiumPrimaryButton(
                text = "Play Level ${level.levelNumber}",
                icon = Icons.Filled.PlayArrow,
                style = ArcadeButtonStyle.GOLD,
                onClick = onPlay,
                height = 50.dp,
                cornerRadius = 15.dp,
                fontSize = 16.sp,
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
            .padding(vertical = 7.dp, horizontal = 6.dp)
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
private fun ChallengeGridTile(
    level: ChallengeLevel,
    isUnlocked: Boolean,
    isSelected: Boolean,
    progress: ChallengeLevelProgress?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stars = progress?.stars ?: 0
    val completed = progress?.completed == true
    val shape = RoundedCornerShape(18.dp)
    val bgBrush = when {
        isSelected -> Brush.verticalGradient(listOf(GoldBtnTop, GoldBtnBottom))
        isUnlocked -> Brush.verticalGradient(listOf(BlueBtnTop, BlueBtnBottom))
        else -> Brush.verticalGradient(listOf(SurfaceCardElevated.copy(alpha = 0.6f), SurfaceCard.copy(alpha = 0.8f)))
    }
    val borderColor = when {
        isSelected -> TextPrimary
        completed -> AccentEmerald.copy(alpha = 0.68f)
        isUnlocked -> AccentGold.copy(alpha = 0.4f)
        else -> GlassBorder.copy(alpha = 0.32f)
    }

    Box(
        contentAlignment = Alignment.Center,
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
        if (completed) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = "Completed",
                tint = if (isSelected) GoldBtnText else AccentEmerald,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(16.dp)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
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
                        fontSize = 27.sp
                    ),
                    color = if (isSelected) GoldBtnText else TextPrimary
                )
                Spacer(Modifier.height(3.dp))
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
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}
