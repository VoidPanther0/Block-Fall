package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.AchievementId
import com.example.game.ChallengeLevels
import com.example.game.GameStatistics
import com.example.ui.GameUiState
import com.example.ui.components.AchievementCard
import com.example.ui.components.ArcadeBlockEmblem
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.CurrencyWallet
import com.example.ui.components.PremiumQuickNav
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.components.PrimaryNavTab
import com.example.ui.components.ResetProgressConfirmationDialog
import com.example.ui.components.ScreenTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.CascadeTextStyles
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBtnBottom
import com.example.ui.theme.GoldBtnTop
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StatisticsScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onResetProgressConfirmed: () -> Unit,
    playerName: String? = null,
    onNavigateTab: ((PrimaryNavTab) -> Unit)? = null
) {
    val stats = state.statistics
    val completedLevels = state.challengeProgress.values.count { it.completed }
    val totalStars = state.challengeProgress.values.sumOf { it.stars }
    val maxStars = ChallengeLevels.levels.size * 3
    val progressRatio = (completedLevels.toFloat() / ChallengeLevels.levels.size.toFloat()).coerceIn(0f, 1f)

    CascadeScreenContainer(
        maxContentWidth = 940.dp,
        bottomBar = if (onNavigateTab != null) {
            {
                PremiumQuickNav(
                    selectedTab = PrimaryNavTab.PROFILE,
                    onSelectTab = onNavigateTab
                )
            }
        } else null,
        modifier = Modifier.testTag("statistics_screen")
    ) {
        ScreenTopBar(
            title = "Profile & Stats",
            onBack = onBack,
            backTestTag = "statistics_back_button"
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth >= 660.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            PlayerRankAndMilestonesCard(state = state, playerName = playerName)
                            ClassicBestSummaryCard(state)
                            LifetimeMetricsCard(state, stats)
                        }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(14.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            AchievementsListCard(state)
                            ChallengeMasteryCard(state, completedLevels, totalStars, maxStars, progressRatio)
                        }
                    }
                } else {
                    PlayerRankAndMilestonesCard(state = state, playerName = playerName)
                    ClassicBestSummaryCard(state)
                    LifetimeMetricsCard(state, stats)
                    AchievementsListCard(state)
                    ChallengeMasteryCard(state, completedLevels, totalStars, maxStars, progressRatio)
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }

}

@Composable
private fun ClassicBestSummaryCard(state: GameUiState) {
    PremiumCard(
        cornerRadius = 20.dp,
        borderColor = AccentGold.copy(alpha = 0.42f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(AccentGold.copy(alpha = 0.16f))
                        .border(1.dp, AccentGold.copy(alpha = 0.42f), RoundedCornerShape(14.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Column {
                    Text(
                        text = "CLASSIC HIGH SCORE",
                        style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.8.sp),
                        color = AccentGold
                    )
                    Text(
                        text = "${state.classicBestScore}",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 28.sp
                        ),
                        color = TextPrimary
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "MAX CHAIN",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary
                )
                Text(
                    text = if (state.statistics.maxCascadeDepth > 0) "x${state.statistics.maxCascadeDepth}" else "—",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AccentCyan
                )
            }
        }
    }
}

@Composable
private fun PlayerRankAndMilestonesCard(
    state: GameUiState,
    playerName: String?
) {
    val rank = state.playerRank
    val hasCrown = "cosmetic_prism_crown" in state.wallet.unlockedEntitlements
    PremiumCard(
        cornerRadius = 20.dp,
        borderColor = (if (hasCrown) AccentGold else AccentCyan).copy(alpha = 0.42f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_rank_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Brush.verticalGradient(listOf(SurfaceCardElevated, SurfaceCard)))
                        .border(
                            width = 1.5.dp,
                            color = if (hasCrown) AccentGold else AccentCyan.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    ArcadeBlockEmblem(size = 30.dp)
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playerName?.takeIf { it.isNotBlank() } ?: "Block Tactician",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "TIER ${rank.tierNumber} · ${rank.title}",
                        style = CascadeTextStyles.SectionKicker,
                        color = if (hasCrown) AccentGold else AccentCyan
                    )
                }
                Text(
                    text = "${rank.xp} XP",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold
                    ),
                    color = AccentGold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(BoardWellBg)
                    .border(1.dp, GlassBorder.copy(alpha = 0.45f), CircleShape)
                    .padding(2.dp)
            ) {
                if (rank.progressToNext > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(rank.progressToNext.coerceIn(0.05f, 1f))
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(AccentCyan, AccentEmerald)))
                    )
                }
            }

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.3f))
            CurrencyWallet(
                goldCoins = state.wallet.goldCoins,
                diamonds = state.wallet.diamonds,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun AchievementsListCard(state: GameUiState) {
    val unlockedSet = state.unlockedAchievements
    PremiumCard(
        cornerRadius = 20.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(18.dp)
        ) {
            SectionHeader(
                kicker = "Career Milestones",
                title = "Achievements (${unlockedSet.size}/${AchievementId.entries.size})",
                accent = AccentGold
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AchievementId.entries.forEach { achievement ->
                    AchievementCard(
                        achievement = achievement,
                        unlocked = achievement.id in unlockedSet
                    )
                }
            }
        }
    }
}

@Composable
private fun LifetimeMetricsCard(state: GameUiState, stats: GameStatistics) {
    PremiumCard(
        cornerRadius = 20.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(18.dp)
        ) {
            SectionHeader(
                kicker = "Career Telemetry",
                title = "Lifetime Performance",
                accent = AccentCyan
            )

            StatMetricRow("Total Games", stats.totalGamesPlayed.toString())
            StatDivider()
            StatMetricRow("Classic Best Score", state.classicBestScore.toString(), AccentGold)
            StatDivider()
            StatMetricRow("Lines Cleared", stats.totalLinesCleared.toString())
            StatDivider()
            StatMetricRow("Cascades Triggered", stats.totalCascadesTriggered.toString())
            StatDivider()
            StatMetricRow("Blocks Placed", stats.totalBlocksPlaced.toString())

            Spacer(Modifier.height(4.dp))
            StatisticsBreakdownChart(stats = stats)
        }
    }
}

@Composable
private fun StatisticsBreakdownChart(stats: GameStatistics) {
    val metrics = listOf(
        Triple("Games", stats.totalGamesPlayed.toFloat(), AccentCyan),
        Triple("Lines", stats.totalLinesCleared.toFloat(), AccentEmerald),
        Triple("Cascades", stats.totalCascadesTriggered.toFloat(), AccentGold),
        Triple("Depth", stats.maxCascadeDepth.toFloat(), AccentCoral)
    )
    val maxVal = metrics.maxOf { it.second }.coerceAtLeast(1f)

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(BoardWellBg)
            .border(1.dp, GlassBorder.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Activity Breakdown",
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary
        )
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            val barWidth = size.width / (metrics.size * 2f)
            val step = size.width / metrics.size
            metrics.forEachIndexed { idx, (_, value, color) ->
                val ratio = (value / maxVal).coerceIn(0.06f, 1f)
                val barHeight = size.height * ratio
                val x = idx * step + (step - barWidth) / 2f
                drawRoundRect(
                    color = color,
                    topLeft = Offset(x, size.height - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            metrics.forEach { (label, value, color) ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "${value.toInt()}",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 12.sp
                        ),
                        color = color
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 10.sp),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun ChallengeMasteryCard(
    state: GameUiState,
    completedLevels: Int,
    totalStars: Int,
    maxStars: Int,
    progressRatio: Float
) {
    PremiumCard(
        cornerRadius = 20.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Challenge Mastery",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "$completedLevels/${ChallengeLevels.levels.size} · ★ $totalStars/$maxStars",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontSize = 13.sp
                    ),
                    color = AccentGold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(12.dp)
                    .clip(CircleShape)
                    .background(BoardWellBg)
                    .border(1.dp, GlassBorder.copy(alpha = 0.45f), CircleShape)
                    .padding(2.dp)
            ) {
                if (progressRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progressRatio.coerceAtLeast(0.06f))
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(GoldBtnTop, GoldBtnBottom)))
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ChallengeLevels.levels.forEach { level ->
                    val progress = state.challengeProgress[level.levelNumber]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(BoardWellBg)
                            .padding(horizontal = 12.dp, vertical = 7.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "L${level.levelNumber} · ${level.title}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            color = TextPrimary
                        )
                        Text(
                            text = if (progress?.completed == true) {
                                "${"★".repeat(progress.stars)}  ${progress.bestScore}"
                            } else if (level.levelNumber <= state.unlockedChallengeLevel) {
                                if ((progress?.bestScore ?: 0) > 0) "Best ${progress?.bestScore}" else "Unlocked"
                            } else {
                                "Locked"
                            },
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                letterSpacing = 0.sp
                            ),
                            color = if (progress?.completed == true) AccentGold else TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatMetricRow(
    label: String,
    value: String,
    valueColor: Color = TextPrimary
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            ),
            color = valueColor
        )
    }
}

@Composable
private fun StatDivider() {
    HorizontalDivider(
        color = GlassBorder.copy(alpha = 0.3f),
        thickness = 1.dp
    )
}
