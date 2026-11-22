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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
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
import com.example.ui.components.ArcadeBlockEmblem
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.ArcadeTitleLogo
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    state: GameUiState,
    isCloudSignedIn: Boolean = false,
    onPlayNewClassic: () -> Unit,
    onContinueClassic: () -> Unit,
    onOpenModes: () -> Unit,
    onOpenChallenges: () -> Unit,
    onOpenLeaderboard: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenStatistics: () -> Unit
) {
    val savedSummary = state.savedClassicSummary
    val canContinue = savedSummary != null
    val totalStars = state.challengeProgress.values.sumOf { it.stars }
    val maxStars = ChallengeLevels.levels.size * 3

    CascadeScreenContainer(
        maxContentWidth = 920.dp,
        modifier = Modifier.testTag("home_screen")
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth >= 660.dp

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Pinned Top Studio Strip: Best Score badge, Leaderboard & Settings shortcuts
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(SurfaceCardElevated.copy(alpha = 0.9f))
                            .border(1.dp, AccentGold.copy(alpha = 0.45f), CircleShape)
                            .clickable(onClick = onOpenStatistics)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "BEST ${state.classicBestScore}",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            ),
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.verticalGradient(listOf(SurfaceCardElevated, SurfaceCard)))
                                .border(
                                    1.dp,
                                    if (isCloudSignedIn) AccentEmerald.copy(alpha = 0.65f) else AccentCyan.copy(alpha = 0.45f),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable(onClick = onOpenLeaderboard)
                                .testTag("home_leaderboard_button")
                        ) {
                            Icon(
                                imageVector = if (isCloudSignedIn) Icons.Filled.CloudDone else Icons.Filled.Leaderboard,
                                contentDescription = "Leaderboard & Cloud",
                                tint = if (isCloudSignedIn) AccentEmerald else AccentCyan,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(Brush.verticalGradient(listOf(SurfaceCardElevated, SurfaceCard)))
                                .border(1.dp, GlassBorder.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                                .clickable(onClick = onOpenSettings)
                                .testTag("home_top_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Settings,
                                contentDescription = stringResource(R.string.nav_settings),
                                tint = TextPrimary,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(18.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            HomeHeroHeader()
                            HomePrimaryPlaySection(
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
                            HomeStudioGrid(
                                totalStars = totalStars,
                                maxStars = maxStars,
                                unlockedLevel = state.unlockedChallengeLevel,
                                totalLines = state.statistics.totalLinesCleared,
                                onOpenModes = onOpenModes,
                                onOpenChallenges = onOpenChallenges,
                                onOpenStatistics = onOpenStatistics,
                                onOpenSettings = onOpenSettings
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.height(4.dp))
                    HomeHeroHeader()
                    Spacer(Modifier.height(4.dp))
                    HomePrimaryPlaySection(
                        canContinue = canContinue,
                        savedScore = savedSummary?.score,
                        savedMoves = savedSummary?.movesPlayed,
                        onPlayNewClassic = onPlayNewClassic,
                        onContinueClassic = onContinueClassic
                    )
                    HomeStudioGrid(
                        totalStars = totalStars,
                        maxStars = maxStars,
                        unlockedLevel = state.unlockedChallengeLevel,
                        totalLines = state.statistics.totalLinesCleared,
                        onOpenModes = onOpenModes,
                        onOpenChallenges = onOpenChallenges,
                        onOpenStatistics = onOpenStatistics,
                        onOpenSettings = onOpenSettings
                    )
                }

                Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun HomeHeroHeader() {
    FrostedGlassCard(
        cornerRadius = 26.dp,
        borderColor = AccentCyan.copy(alpha = 0.32f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard.copy(alpha = 0.9f))
                    .border(1.dp, GlassBorder.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
                    .padding(12.dp)
            ) {
                ArcadeBlockEmblem(size = 64.dp)
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                ArcadeTitleLogo(fontSize = 32.sp)
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AccentCyan.copy(alpha = 0.14f))
                        .border(1.dp, AccentCyan.copy(alpha = 0.35f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "8×8 GRAVITY PUZZLE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        ),
                        color = AccentCyan,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun HomePrimaryPlaySection(
    canContinue: Boolean,
    savedScore: Int?,
    savedMoves: Int?,
    onPlayNewClassic: () -> Unit,
    onContinueClassic: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        if (canContinue) {
            GlossyArcadeButton(
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
            GlossyArcadeButton(
                text = stringResource(R.string.nav_play),
                icon = Icons.Filled.PlayArrow,
                style = ArcadeButtonStyle.BLUE,
                onClick = onPlayNewClassic,
                height = 52.dp,
                cornerRadius = 16.dp,
                fontSize = 16.sp,
                testTag = "home_play_button",
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            GlossyArcadeButton(
                text = stringResource(R.string.nav_play),
                icon = Icons.Filled.PlayArrow,
                style = ArcadeButtonStyle.GOLD,
                onClick = onPlayNewClassic,
                height = 58.dp,
                cornerRadius = 18.dp,
                fontSize = 19.sp,
                testTag = "home_play_button",
                modifier = Modifier.fillMaxWidth()
            )
            GlossyArcadeButton(
                text = stringResource(R.string.nav_continue),
                icon = Icons.Filled.Replay,
                style = ArcadeButtonStyle.SLATE,
                enabled = false,
                onClick = onContinueClassic,
                height = 50.dp,
                cornerRadius = 16.dp,
                fontSize = 15.sp,
                testTag = "home_continue_button",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HomeStudioGrid(
    totalStars: Int,
    maxStars: Int,
    unlockedLevel: Int,
    totalLines: Int,
    onOpenModes: () -> Unit,
    onOpenChallenges: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StudioMenuTile(
                title = "Modes",
                subtitle = "Classic & Campaign",
                icon = Icons.Filled.GridView,
                accent = AccentCyan,
                onClick = onOpenModes,
                testTag = "home_modes_button",
                modifier = Modifier.weight(1f)
            )
            StudioMenuTile(
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
            StudioMenuTile(
                title = stringResource(R.string.nav_statistics),
                subtitle = "$totalLines lines cleared",
                icon = Icons.Filled.BarChart,
                accent = AccentEmerald,
                onClick = onOpenStatistics,
                testTag = "home_statistics_button",
                modifier = Modifier.weight(1f)
            )
            StudioMenuTile(
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
private fun StudioMenuTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .shadow(8.dp, shape, clip = false)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        SurfaceCardElevated.copy(alpha = 0.92f),
                        SurfaceCard.copy(alpha = 0.95f)
                    )
                )
            )
            .border(1.dp, accent.copy(alpha = 0.36f), shape)
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag(testTag)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.copy(alpha = 0.16f))
                .border(1.dp, accent.copy(alpha = 0.38f), RoundedCornerShape(12.dp))
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 17.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
