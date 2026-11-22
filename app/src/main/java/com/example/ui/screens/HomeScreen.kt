package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.game.AchievementId
import com.example.game.BlockColor
import com.example.game.ChallengeLevels
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.ArcadeTitleLogo
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.PlayerProfileHeader
import com.example.ui.components.PremiumBottomNavigation
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.components.PrimaryNavTab
import com.example.ui.components.SectionHeader
import com.example.ui.components.drawBevelBlock
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.CascadeTextStyles
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalBlockStyle
import com.example.ui.theme.LocalBoardPalette
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    state: GameUiState,
    isCloudSignedIn: Boolean = false,
    playerName: String? = null,
    onPlayNewClassic: () -> Unit,
    onContinueClassic: () -> Unit,
    onOpenModes: () -> Unit,
    onOpenChallenges: () -> Unit,
    onOpenLeaderboard: () -> Unit = {},
    onOpenShop: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenStatistics: () -> Unit,
    onNavigateTab: ((PrimaryNavTab) -> Unit)? = null
) {
    val savedSummary = state.savedClassicSummary
    val canContinue = savedSummary != null
    val totalStars = state.challengeProgress.values.sumOf { it.stars }
    val completedChallenges = state.challengeProgress.values.count { it.completed }
    val maxStars = ChallengeLevels.levels.size * 3
    val hasCrown = "cosmetic_prism_crown" in state.wallet.unlockedEntitlements

    CascadeScreenContainer(
        maxContentWidth = 960.dp,
        bottomBar = if (onNavigateTab != null) {
            {
                PremiumBottomNavigation(
                    selectedTab = PrimaryNavTab.HOME,
                    onSelectTab = onNavigateTab
                )
            }
        } else null,
        modifier = Modifier.testTag("home_screen")
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PlayerProfileHeader(
                playerName = playerName,
                rank = state.playerRank,
                goldCoins = state.wallet.goldCoins,
                diamonds = state.wallet.diamonds,
                hasCrownCosmetic = hasCrown,
                isCloudSignedIn = isCloudSignedIn,
                onOpenProfile = onOpenStatistics,
                onOpenWallet = onOpenShop,
                onOpenLeaderboard = onOpenLeaderboard,
                onOpenSettings = onOpenSettings
            )

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
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
                            horizontalArrangement = Arrangement.spacedBy(20.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                HomeHeroSection(
                                    canContinue = canContinue,
                                    savedScore = savedSummary?.score,
                                    savedMoves = savedSummary?.movesPlayed,
                                    onPlayNewClassic = onPlayNewClassic,
                                    onContinueClassic = onContinueClassic
                                )
                            }

                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                HomeActionHub(
                                    totalStars = totalStars,
                                    maxStars = maxStars,
                                    unlockedLevel = state.unlockedChallengeLevel,
                                    goldCoins = state.wallet.goldCoins,
                                    diamonds = state.wallet.diamonds,
                                    onOpenModes = onOpenModes,
                                    onOpenChallenges = onOpenChallenges,
                                    onOpenShop = onOpenShop,
                                    onOpenSettings = onOpenSettings
                                )
                                HomeProgressOverviewCard(
                                    bestScore = state.classicBestScore,
                                    totalGames = state.statistics.totalGamesPlayed,
                                    completedChallenges = completedChallenges,
                                    totalChallenges = ChallengeLevels.levels.size,
                                    unlockedAchievements = state.unlockedAchievements.size,
                                    totalAchievements = AchievementId.entries.size,
                                    onOpenStatistics = onOpenStatistics
                                )
                            }
                        }
                    } else {
                        HomeHeroSection(
                            canContinue = canContinue,
                            savedScore = savedSummary?.score,
                            savedMoves = savedSummary?.movesPlayed,
                            onPlayNewClassic = onPlayNewClassic,
                            onContinueClassic = onContinueClassic
                        )

                        HomeActionHub(
                            totalStars = totalStars,
                            maxStars = maxStars,
                            unlockedLevel = state.unlockedChallengeLevel,
                            goldCoins = state.wallet.goldCoins,
                            diamonds = state.wallet.diamonds,
                            onOpenModes = onOpenModes,
                            onOpenChallenges = onOpenChallenges,
                            onOpenShop = onOpenShop,
                            onOpenSettings = onOpenSettings
                        )

                        HomeProgressOverviewCard(
                            bestScore = state.classicBestScore,
                            totalGames = state.statistics.totalGamesPlayed,
                            completedChallenges = completedChallenges,
                            totalChallenges = ChallengeLevels.levels.size,
                            unlockedAchievements = state.unlockedAchievements.size,
                            totalAchievements = AchievementId.entries.size,
                            onOpenStatistics = onOpenStatistics
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeHeroSection(
    canContinue: Boolean,
    savedScore: Int?,
    savedMoves: Int?,
    onPlayNewClassic: () -> Unit,
    onContinueClassic: () -> Unit
) {
    PremiumCard(
        cornerRadius = 26.dp,
        borderColor = AccentCyan.copy(alpha = 0.36f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                HeroCascadeCluster(
                    modifier = Modifier
                        .size(84.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BoardWellBg.copy(alpha = 0.9f))
                        .border(1.dp, GlassBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(10.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ArcadeTitleLogo(
                        fontSize = 30.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                    Text(
                        text = "Drop shapes, clear lines, trigger chain gravity cascades.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            if (canContinue) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PremiumPrimaryButton(
                        text = "Continue (${savedScore ?: 0} pts · ${savedMoves ?: 0}m)",
                        icon = Icons.Filled.Replay,
                        style = ArcadeButtonStyle.GOLD,
                        onClick = onContinueClassic,
                        height = 58.dp,
                        cornerRadius = 18.dp,
                        fontSize = 17.sp,
                        testTag = "home_continue_button",
                        modifier = Modifier.fillMaxWidth()
                    )
                    PremiumPrimaryButton(
                        text = "New Classic Game",
                        icon = Icons.Filled.PlayArrow,
                        style = ArcadeButtonStyle.BLUE,
                        onClick = onPlayNewClassic,
                        height = 50.dp,
                        cornerRadius = 16.dp,
                        fontSize = 15.sp,
                        testTag = "home_play_button",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                PremiumPrimaryButton(
                    text = stringResource(R.string.nav_play),
                    icon = Icons.Filled.PlayArrow,
                    style = ArcadeButtonStyle.GOLD,
                    onClick = onPlayNewClassic,
                    height = 60.dp,
                    cornerRadius = 18.dp,
                    fontSize = 19.sp,
                    testTag = "home_play_button",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun HeroCascadeCluster(modifier: Modifier = Modifier) {
    val blockStyle = LocalBlockStyle.current
    val boardPalette = LocalBoardPalette.current
    Canvas(modifier = modifier) {
        val cols = 4
        val cell = size.width / cols
        val gap = (cell * 0.14f).coerceAtLeast(1.5f)
        val tile = cell - gap
        val cells = listOf(
            Triple(0, 1, BlockColor.CORAL),
            Triple(1, 1, BlockColor.CORAL),
            Triple(1, 2, BlockColor.AMBER),
            Triple(2, 0, BlockColor.CYAN),
            Triple(2, 1, BlockColor.CYAN),
            Triple(2, 2, BlockColor.EMERALD),
            Triple(3, 0, BlockColor.VIOLET),
            Triple(3, 1, BlockColor.CYAN),
            Triple(3, 2, BlockColor.EMERALD),
            Triple(3, 3, BlockColor.AMBER)
        )
        for ((r, c, color) in cells) {
            drawBevelBlock(
                left = c * cell + gap / 2f,
                top = r * cell + gap / 2f,
                cellSize = tile,
                color = color,
                blockStyle = blockStyle,
                boardPalette = boardPalette
            )
        }
    }
}

@Composable
private fun HomeActionHub(
    totalStars: Int,
    maxStars: Int,
    unlockedLevel: Int,
    goldCoins: Int,
    diamonds: Int,
    onOpenModes: () -> Unit,
    onOpenChallenges: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeHubTile(
                title = "Modes",
                subtitle = "Classic & Campaign",
                icon = Icons.Filled.GridView,
                accent = AccentCyan,
                onClick = onOpenModes,
                testTag = "home_modes_button",
                modifier = Modifier.weight(1f)
            )
            HomeHubTile(
                title = stringResource(R.string.nav_challenges),
                subtitle = "★ $totalStars/$maxStars · Lvl $unlockedLevel",
                icon = Icons.Filled.Star,
                accent = AccentGold,
                onClick = onOpenChallenges,
                testTag = "home_challenges_button",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeHubTile(
                title = "Shop",
                subtitle = "$goldCoins Gold · $diamonds Gems",
                icon = Icons.Filled.ShoppingBag,
                accent = AccentEmerald,
                onClick = onOpenShop,
                testTag = "home_shop_button",
                modifier = Modifier.weight(1f)
            )
            HomeHubTile(
                title = stringResource(R.string.nav_settings),
                subtitle = "Themes & Audio",
                icon = Icons.Filled.Settings,
                accent = AccentPurple,
                onClick = onOpenSettings,
                testTag = "home_settings_button",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HomeProgressOverviewCard(
    bestScore: Int,
    totalGames: Int,
    completedChallenges: Int,
    totalChallenges: Int,
    unlockedAchievements: Int,
    totalAchievements: Int,
    onOpenStatistics: () -> Unit
) {
    PremiumCard(
        cornerRadius = 20.dp,
        borderColor = GlassBorder.copy(alpha = 0.45f),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onOpenStatistics)
            .testTag("home_statistics_button")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            SectionHeader(
                kicker = "Progress Snapshot",
                title = "Your Career Stats",
                accent = AccentEmerald,
                trailing = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(AccentEmerald.copy(alpha = 0.14f))
                            .border(1.dp, AccentEmerald.copy(alpha = 0.35f), CircleShape)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.BarChart,
                            contentDescription = null,
                            tint = AccentEmerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Details",
                            style = MaterialTheme.typography.labelSmall,
                            color = AccentEmerald
                        )
                    }
                }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ProgressMiniMetric(
                    label = "Best",
                    value = "$bestScore",
                    valueColor = AccentGold,
                    modifier = Modifier.weight(1f)
                )
                ProgressMiniMetric(
                    label = "Games",
                    value = "$totalGames",
                    valueColor = TextPrimary,
                    modifier = Modifier.weight(1f)
                )
                ProgressMiniMetric(
                    label = "Levels",
                    value = "$completedChallenges/$totalChallenges",
                    valueColor = AccentCyan,
                    modifier = Modifier.weight(1f)
                )
                ProgressMiniMetric(
                    label = "Badges",
                    value = "$unlockedAchievements/$totalAchievements",
                    valueColor = AccentEmerald,
                    modifier = Modifier.weight(1f)
                )
            }

            val challengeRatio = (completedChallenges.toFloat() / totalChallenges.coerceAtLeast(1).toFloat())
                .coerceIn(0f, 1f)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(BoardWellBg)
            ) {
                if (challengeRatio > 0f) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(challengeRatio.coerceAtLeast(0.04f))
                            .clip(CircleShape)
                            .background(Brush.horizontalGradient(listOf(AccentCyan, AccentEmerald)))
                    )
                }
            }
        }
    }
}

@Composable
private fun ProgressMiniMetric(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BoardWellBg)
            .border(1.dp, GlassBorder.copy(alpha = 0.28f), RoundedCornerShape(12.dp))
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.5.sp),
            color = TextSecondary,
            maxLines = 1
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = value,
            style = CascadeTextStyles.NumericBalance.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontSize = 14.sp
            ),
            color = valueColor,
            maxLines = 1
        )
    }
}

@Composable
private fun HomeHubTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .shadow(6.dp, shape, clip = false)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        SurfaceCardElevated.copy(alpha = 0.92f),
                        SurfaceCard.copy(alpha = 0.96f)
                    )
                )
            )
            .border(1.dp, accent.copy(alpha = 0.36f), shape)
            .clickable(onClick = onClick)
            .padding(14.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(accent.copy(alpha = 0.16f))
                    .border(1.dp, accent.copy(alpha = 0.36f), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
