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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Leaderboard
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
import com.example.game.ChallengeLevels
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.ArcadeTitleLogo
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.PlayerProfileHeader
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.components.drawBevelBlock
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
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
    onOpenStatistics: () -> Unit
) {
    val savedSummary = state.savedClassicSummary
    val canContinue = savedSummary != null
    val totalStars = state.challengeProgress.values.sumOf { it.stars }
    val maxStars = ChallengeLevels.levels.size * 3
    val hasCrown = "cosmetic_prism_crown" in state.wallet.unlockedEntitlements

    CascadeScreenContainer(
        maxContentWidth = 960.dp,
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

            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
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
                            HomeHeroSection(
                                canContinue = canContinue,
                                savedScore = savedSummary?.score,
                                savedMoves = savedSummary?.movesPlayed,
                                onPlayNewClassic = onPlayNewClassic,
                                onContinueClassic = onContinueClassic,
                                modifier = Modifier.weight(1f)
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                HomeActionHub(
                                    totalStars = totalStars,
                                    maxStars = maxStars,
                                    unlockedLevel = state.unlockedChallengeLevel,
                                    onOpenModes = onOpenModes,
                                    onOpenChallenges = onOpenChallenges,
                                    onOpenShop = onOpenShop,
                                    onOpenSettings = onOpenSettings,
                                    onOpenLeaderboard = onOpenLeaderboard,
                                    onOpenStatistics = onOpenStatistics
                                )
                                HomeLeaderboardCard(
                                    isSignedIn = isCloudSignedIn,
                                    bestScore = state.classicBestScore,
                                    onOpenLeaderboard = onOpenLeaderboard
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
                            onOpenModes = onOpenModes,
                            onOpenChallenges = onOpenChallenges,
                            onOpenShop = onOpenShop,
                            onOpenSettings = onOpenSettings,
                            onOpenLeaderboard = onOpenLeaderboard,
                            onOpenStatistics = onOpenStatistics
                        )
                        HomeLeaderboardCard(
                            isSignedIn = isCloudSignedIn,
                            bestScore = state.classicBestScore,
                            onOpenLeaderboard = onOpenLeaderboard
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
    onContinueClassic: () -> Unit,
    modifier: Modifier = Modifier
) {
    PremiumCard(
        cornerRadius = 26.dp,
        borderColor = AccentCyan.copy(alpha = 0.38f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                HeroCascadeCluster(
                    modifier = Modifier
                        .size(76.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(BoardWellBg.copy(alpha = 0.9f))
                        .border(1.dp, GlassBorder.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                        .padding(9.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ArcadeTitleLogo(fontSize = 27.sp)
                    Text(
                        text = "Drop shapes. Clear lines. Chain gravity cascades.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Primary focus: one large, unmistakable Play action.
            PremiumPrimaryButton(
                text = stringResource(R.string.nav_play),
                icon = Icons.Filled.PlayArrow,
                style = ArcadeButtonStyle.GOLD,
                onClick = onPlayNewClassic,
                height = 64.dp,
                cornerRadius = 20.dp,
                fontSize = 19.sp,
                testTag = "home_play_button",
                modifier = Modifier.fillMaxWidth()
            )

            if (canContinue) {
                PremiumPrimaryButton(
                    text = "Continue · ${savedScore ?: 0} pts · ${savedMoves ?: 0} moves",
                    icon = Icons.Filled.Replay,
                    style = ArcadeButtonStyle.SLATE,
                    onClick = onContinueClassic,
                    height = 48.dp,
                    cornerRadius = 15.dp,
                    fontSize = 14.sp,
                    testTag = "home_continue_button",
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
            Triple(0, 1, com.example.game.BlockColor.CORAL),
            Triple(1, 1, com.example.game.BlockColor.CORAL),
            Triple(1, 2, com.example.game.BlockColor.AMBER),
            Triple(2, 0, com.example.game.BlockColor.CYAN),
            Triple(2, 1, com.example.game.BlockColor.CYAN),
            Triple(2, 2, com.example.game.BlockColor.EMERALD),
            Triple(3, 0, com.example.game.BlockColor.VIOLET),
            Triple(3, 1, com.example.game.BlockColor.CYAN),
            Triple(3, 2, com.example.game.BlockColor.EMERALD),
            Triple(3, 3, com.example.game.BlockColor.AMBER)
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
    onOpenModes: () -> Unit,
    onOpenChallenges: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenStatistics: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeHubTile(
                title = "Modes",
                subtitle = "Classic · Challenge · Daily",
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
                subtitle = "Packs & rewards",
                icon = Icons.Filled.ShoppingBag,
                accent = AccentEmerald,
                onClick = onOpenShop,
                testTag = "home_shop_button",
                modifier = Modifier.weight(1f)
            )
            HomeHubTile(
                title = stringResource(R.string.nav_settings),
                subtitle = "Audio & board theme",
                icon = Icons.Filled.Settings,
                accent = AccentPurple,
                onClick = onOpenSettings,
                testTag = "home_settings_button",
                modifier = Modifier.weight(1f)
            )
        }

        HomeHubTile(
            title = "Profile & Stats",
            subtitle = "Rank · achievements · milestones",
            icon = Icons.Filled.Leaderboard,
            accent = AccentCyan,
            onClick = onOpenStatistics,
            testTag = "home_statistics_button",
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun HomeLeaderboardCard(
    isSignedIn: Boolean,
    bestScore: Int,
    onOpenLeaderboard: () -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(6.dp, shape, clip = false)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(SurfaceCardElevated.copy(alpha = 0.92f), SurfaceCard.copy(alpha = 0.96f))
                )
            )
            .border(1.dp, AccentGold.copy(alpha = 0.34f), shape)
            .clickable(onClick = onOpenLeaderboard)
            .padding(14.dp)
            .testTag("home_leaderboard_card")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(AccentGold.copy(alpha = 0.15f))
                .border(1.dp, AccentGold.copy(alpha = 0.38f), RoundedCornerShape(11.dp))
        ) {
            Icon(
                imageVector = Icons.Filled.Leaderboard,
                contentDescription = null,
                tint = AccentGold,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "GLOBAL LEADERBOARD",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.6.sp,
                    fontSize = 9.5.sp
                ),
                color = AccentGold,
                maxLines = 1
            )
            Text(
                text = if (isSignedIn) {
                    "Your best: $bestScore · View verified standings"
                } else {
                    "Sign in with Google to post your best score"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = null,
            tint = AccentGold,
            modifier = Modifier.size(18.dp)
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
                    listOf(SurfaceCardElevated.copy(alpha = 0.92f), SurfaceCard.copy(alpha = 0.96f))
                )
            )
            .border(1.dp, accent.copy(alpha = 0.34f), shape)
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
