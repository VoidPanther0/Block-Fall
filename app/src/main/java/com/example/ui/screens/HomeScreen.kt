package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.AchievementsCatalog
import com.example.game.BlockColor
import com.example.game.BlockStyle
import com.example.game.BoardColorPalette
import com.example.game.DailyChallengeGenerator
import com.example.game.DailyMissionsCatalog
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.ArcadeTitleLogo
import com.example.ui.components.BlockFallBottomNav
import com.example.ui.components.BottomNavTab
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.PlayerProfileHeader
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumModalSheet
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.components.drawBevelBlock
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalBlockStyle
import com.example.ui.theme.LocalBoardPalette
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.toBoardColors

@Composable
fun HomeScreen(
    state: GameUiState,
    spinPoints: Int = 0,
    onOpenWheel: () -> Unit = {},
    isCloudSignedIn: Boolean = false,
    playerName: String? = null,
    googlePhotoUrl: String? = null,
    onPlayNewClassic: () -> Unit,
    onContinueClassic: () -> Unit,
    onPlayDaily: () -> Unit = {},
    onOpenModes: () -> Unit,
    onOpenChallenges: () -> Unit,
    onOpenLeaderboard: () -> Unit = {},
    onOpenShop: () -> Unit = {},
    onOpenSettings: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenDailyRewards: () -> Unit = {},
    onOpenAchievements: () -> Unit = {},
    onOpenDailyMissions: () -> Unit = {},
    onOpenEvents: () -> Unit = {},
    onSelectBoardPalette: (BoardColorPalette) -> Unit = {},
    onSelectBlockStyle: (BlockStyle) -> Unit = {}
) {
    val savedSummary = state.savedClassicSummary
    val canContinue = savedSummary != null
    val hasCrown = "cosmetic_prism_crown" in state.wallet.unlockedEntitlements
    val todayKey = remember { DailyChallengeGenerator.todayDateKey() }
    val dailySpec = remember(todayKey) { DailyChallengeGenerator.forDateKey(todayKey) }
    val canClaimDailyReward = state.dailyRewardProgress.canClaimToday(todayKey)
    val claimableMissions = remember(state.statistics, state.dailyMissionSnapshot, todayKey) {
        val snap = state.dailyMissionSnapshot.anchoredTo(todayKey, state.statistics)
        DailyMissionsCatalog.missions.count { m ->
            m.id !in snap.claimedMissionIds && m.progressProvider(state.statistics, snap) >= m.target
        }
    }
    val claimableAchievements = remember(state.statistics, state.challengeProgress, state.claimedAchievementIds) {
        AchievementsCatalog.items.count { ach ->
            ach.id !in state.claimedAchievementIds && ach.progressProvider(state.statistics, state.challengeProgress) >= ach.target
        }
    }
    var showThemeSheet by remember { mutableStateOf(false) }

    CascadeScreenContainer(
        maxContentWidth = 960.dp,
        modifier = Modifier.testTag("home_screen"),
        footerBar = {
            BlockFallBottomNav(
                selectedTab = BottomNavTab.HOME,
                onSelectTab = { tab ->
                    when (tab) {
                        BottomNavTab.HOME -> Unit
                        BottomNavTab.MODES -> onOpenModes()
                        BottomNavTab.EVENTS -> onOpenEvents()
                        BottomNavTab.SHOP -> onOpenShop()
                        BottomNavTab.PROFILE -> onOpenStatistics()
                    }
                }
            )
        }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PlayerProfileHeader(
                playerName = playerName,
                rank = state.playerRank,
                goldCoins = state.wallet.goldCoins,
                diamonds = state.wallet.diamonds,
                avatarId = state.profileAvatarId,
                googlePhotoUrl = googlePhotoUrl,
                hasCrownCosmetic = hasCrown,
                isCloudSignedIn = isCloudSignedIn,
                onOpenProfile = onOpenStatistics,
                onOpenWallet = onOpenShop,
                onOpenLeaderboard = onOpenLeaderboard,
                onOpenSettings = onOpenSettings,
                trailingWalletAction = {
                    LuckyWheelHeaderButton(
                        spinPoints = spinPoints,
                        onClick = onOpenWheel
                    )
                }
            )

            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth()) {
                val isWide = maxWidth >= 660.dp

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(18.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            HomeHeroSection(
                                bestScore = state.classicBestScore,
                                canContinue = canContinue,
                                savedScore = savedSummary?.score,
                                savedMoves = savedSummary?.movesPlayed,
                                currentPalette = state.settings.boardPalette,
                                onOpenThemes = { showThemeSheet = true },
                                onPlayNewClassic = onPlayNewClassic,
                                onContinueClassic = onContinueClassic,
                                modifier = Modifier.weight(1f)
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                DailyRewardStrip(
                                    dailyTitle = dailySpec.displayDate,
                                    targetScore = dailySpec.level.targetScore,
                                    onClick = onPlayDaily
                                )
                                HomeActionHub(
                                    canClaimDailyReward = canClaimDailyReward,
                                    claimableMissions = claimableMissions,
                                    claimableAchievements = claimableAchievements,
                                    onOpenModes = onOpenModes,
                                    onOpenChallenges = onOpenChallenges,
                                    onOpenShop = onOpenShop,
                                    onOpenSettings = onOpenSettings,
                                    onOpenStatistics = onOpenStatistics,
                                    onOpenDailyRewards = onOpenDailyRewards,
                                    onOpenAchievements = onOpenAchievements,
                                    onOpenDailyMissions = onOpenDailyMissions
                                )
                            }
                        }
                    } else {
                        HomeHeroSection(
                            bestScore = state.classicBestScore,
                            canContinue = canContinue,
                            savedScore = savedSummary?.score,
                            savedMoves = savedSummary?.movesPlayed,
                            currentPalette = state.settings.boardPalette,
                            onOpenThemes = { showThemeSheet = true },
                            onPlayNewClassic = onPlayNewClassic,
                            onContinueClassic = onContinueClassic
                        )
                        DailyRewardStrip(
                            dailyTitle = dailySpec.displayDate,
                            targetScore = dailySpec.level.targetScore,
                            onClick = onPlayDaily
                        )
                        HomeActionHub(
                            canClaimDailyReward = canClaimDailyReward,
                            claimableMissions = claimableMissions,
                            claimableAchievements = claimableAchievements,
                            onOpenModes = onOpenModes,
                            onOpenChallenges = onOpenChallenges,
                            onOpenShop = onOpenShop,
                            onOpenSettings = onOpenSettings,
                            onOpenStatistics = onOpenStatistics,
                            onOpenDailyRewards = onOpenDailyRewards,
                            onOpenAchievements = onOpenAchievements,
                            onOpenDailyMissions = onOpenDailyMissions
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }

    if (showThemeSheet) {
        HomeThemePickerSheet(
            selectedPalette = state.settings.boardPalette,
            selectedStyle = state.settings.blockStyle,
            onSelectPalette = onSelectBoardPalette,
            onSelectStyle = onSelectBlockStyle,
            onDismiss = { showThemeSheet = false }
        )
    }
}

private val MiniWheelColors = listOf(
    Color(0xFF7C4DFF), Color(0xFF00B8D4), Color(0xFFFFB300), Color(0xFF26C281),
    Color(0xFFFF5252), Color(0xFFEC407A), Color(0xFF3D8BFF), Color(0xFFFF8F00)
)

@Composable
private fun LuckyWheelHeaderButton(
    spinPoints: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "luckyWheelHeader")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = if (spinPoints > 0) 1.09f else 1.02f,
        animationSpec = infiniteRepeatable(tween(850), RepeatMode.Reverse),
        label = "pulse"
    )
    val spinDeg by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(9000, easing = LinearEasing)),
        label = "rotate"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(50.dp)
            .semantics {
                contentDescription = "Lucky Wheel ($spinPoints spins)"
                role = Role.Button
            }
            .testTag("home_wheel_button")
            .clickable(onClick = onClick)
    ) {
        Canvas(
            modifier = Modifier
                .size(44.dp)
                .graphicsLayer { scaleX = pulse; scaleY = pulse }
        ) {
            val outer = size.minDimension / 2f
            val center = Offset(size.width / 2f, size.height / 2f + 1.5f)
            val rim = outer * 0.14f
            val wheelRadius = outer - rim

            drawCircle(
                brush = Brush.linearGradient(listOf(Color(0xFFFFE57F), Color(0xFFFFA000))),
                radius = outer,
                center = center
            )
            drawCircle(color = Color(0xFF3A1B7A), radius = wheelRadius + 1.5f, center = center)

            val sweep = 360f / MiniWheelColors.size
            rotate(spinDeg, pivot = center) {
                MiniWheelColors.forEachIndexed { idx, sliceColor ->
                    drawArc(
                        color = sliceColor,
                        startAngle = -90f + idx * sweep,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset(center.x - wheelRadius, center.y - wheelRadius),
                        size = Size(wheelRadius * 2f, wheelRadius * 2f)
                    )
                    drawArc(
                        color = Color.White.copy(alpha = 0.7f),
                        startAngle = -90f + idx * sweep,
                        sweepAngle = sweep,
                        useCenter = true,
                        topLeft = Offset(center.x - wheelRadius, center.y - wheelRadius),
                        size = Size(wheelRadius * 2f, wheelRadius * 2f),
                        style = Stroke(width = 1.4f)
                    )
                }
            }

            drawCircle(Color(0xFFFFD54F), radius = wheelRadius * 0.30f, center = center)
            drawCircle(Color(0xFF3A1B7A), radius = wheelRadius * 0.16f, center = center)

            val pointer = Path().apply {
                moveTo(center.x, center.y - wheelRadius + 3f)
                lineTo(center.x - 5.5f, center.y - outer - 2f)
                lineTo(center.x + 5.5f, center.y - outer - 2f)
                close()
            }
            drawPath(pointer, Color(0xFFFF3D57))
            drawPath(pointer, Color.White, style = Stroke(width = 1.6f))
        }

        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(19.dp)
                .clip(CircleShape)
                .background(if (spinPoints > 0) Color(0xFFFF3D57) else Color(0xFF475569))
                .border(1.5.dp, Color.White, CircleShape)
        ) {
            Text(
                text = "$spinPoints",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun HomeHeroSection(
    bestScore: Int,
    canContinue: Boolean,
    savedScore: Int?,
    savedMoves: Int?,
    currentPalette: BoardColorPalette,
    onOpenThemes: () -> Unit,
    onPlayNewClassic: () -> Unit,
    onContinueClassic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paletteColors = currentPalette.toBoardColors()
    val boardWellColor = paletteColors.wellTop

    PremiumCard(
        cornerRadius = 18.dp,
        borderColor = GlassBorder.copy(alpha = 0.55f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                HeroCascadeCluster(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(boardWellColor)
                        .border(1.dp, GlassBorder.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        .padding(6.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ArcadeTitleLogo(fontSize = 24.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(com.example.ui.theme.SurfaceDarkElevated)
                                .border(1.dp, com.example.ui.theme.HairlineDark, RoundedCornerShape(10.dp))
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.EmojiEvents,
                                contentDescription = null,
                                tint = AccentGold,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Best: $bestScore",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.5.sp
                                ),
                                color = TextPrimary,
                                maxLines = 1
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(paletteColors.rim.copy(alpha = 0.16f))
                                .border(1.dp, paletteColors.rim.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                                .clickable(onClick = onOpenThemes)
                                .padding(horizontal = 9.dp, vertical = 4.dp)
                                .testTag("home_theme_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(paletteColors.rim)
                            )
                            Text(
                                text = currentPalette.displayName,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.5.sp
                                ),
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Primary Play CTA button with tactile 16dp rounded rectangle
            PremiumPrimaryButton(
                text = stringResource(R.string.nav_play),
                icon = Icons.Filled.PlayArrow,
                style = ArcadeButtonStyle.BLUE,
                onClick = onPlayNewClassic,
                height = 52.dp,
                cornerRadius = 16.dp,
                fontSize = 16.5.sp,
                testTag = "home_play_button",
                modifier = Modifier.fillMaxWidth()
            )

            if (canContinue) {
                // Secondary Continue button
                PremiumPrimaryButton(
                    text = "Continue · ${savedScore ?: 0} pts (${savedMoves ?: 0} moves)",
                    icon = Icons.Filled.Replay,
                    style = ArcadeButtonStyle.SLATE,
                    onClick = onContinueClassic,
                    height = 48.dp,
                    cornerRadius = 16.dp,
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
private fun DailyRewardStrip(
    dailyTitle: String,
    targetScore: Int,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(com.example.ui.theme.SurfaceDarkElevated)
            .border(1.dp, com.example.ui.theme.HairlineDark, shape)
            .clickable(onClick = onClick)
            .testTag("home_daily_button")
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(Brush.horizontalGradient(listOf(com.example.ui.theme.PsGoldStart, com.example.ui.theme.PsGoldMid, com.example.ui.theme.PsGoldEnd)))
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(com.example.ui.theme.PsGoldStart.copy(alpha = 0.14f))
                    .border(1.dp, com.example.ui.theme.PsGoldStart.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.CalendarToday,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Daily Gravity Puzzle",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "$dailyTitle · Target $targetScore · Win Gold & Gems",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Filled.PlayArrow,
                contentDescription = "Play Daily Puzzle",
                tint = AccentGold,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun HomeActionHub(
    canClaimDailyReward: Boolean,
    claimableMissions: Int,
    claimableAchievements: Int,
    onOpenModes: () -> Unit,
    onOpenChallenges: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenDailyRewards: () -> Unit,
    onOpenAchievements: () -> Unit,
    onOpenDailyMissions: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Streamlined 2x2 Primary Hub: spacious, balanced, zero text truncation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeHubTile(
                title = "Daily Rewards",
                subtitle = if (canClaimDailyReward) "Reward ready!" else "7-day calendar",
                icon = Icons.Filled.WorkspacePremium,
                accent = AccentGold,
                badgeCount = if (canClaimDailyReward) 1 else 0,
                onClick = onOpenDailyRewards,
                testTag = "home_daily_rewards_button",
                modifier = Modifier.weight(1f)
            )
            HomeHubTile(
                title = "Challenges",
                subtitle = "8 tactical boards",
                icon = Icons.Filled.Star,
                accent = AccentCyan,
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
                title = "Missions",
                subtitle = "Daily goals & gems",
                icon = Icons.Filled.Flag,
                accent = AccentEmerald,
                badgeCount = claimableMissions,
                onClick = onOpenDailyMissions,
                testTag = "home_missions_button",
                modifier = Modifier.weight(1f)
            )
            HomeHubTile(
                title = "Achievements",
                subtitle = "Trophies & medals",
                icon = Icons.Filled.EmojiEvents,
                accent = AccentPurple,
                badgeCount = claimableAchievements,
                onClick = onOpenAchievements,
                testTag = "home_achievements_button",
                modifier = Modifier.weight(1f)
            )
        }

        // Compact single-row quick utility strip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HomeQuickUtilityChip(
                label = "Modes",
                icon = Icons.Filled.GridView,
                accent = AccentCyan,
                onClick = onOpenModes,
                testTag = "home_modes_button",
                modifier = Modifier.weight(1f)
            )
            HomeQuickUtilityChip(
                label = "Shop",
                icon = Icons.Filled.ShoppingBag,
                accent = AccentEmerald,
                onClick = onOpenShop,
                testTag = "home_shop_button",
                modifier = Modifier.weight(1f)
            )
            HomeQuickUtilityChip(
                label = "Profile",
                icon = Icons.Filled.Person,
                accent = AccentGold,
                onClick = onOpenStatistics,
                testTag = "home_statistics_button",
                modifier = Modifier.weight(1f)
            )
            HomeQuickUtilityChip(
                label = "Settings",
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
private fun HomeHubTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier,
    badgeCount: Int = 0
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clip(shape)
            .background(com.example.ui.theme.SurfaceDarkCard)
            .border(1.dp, com.example.ui.theme.HairlineDark, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 13.dp, vertical = 12.dp)
            .testTag(testTag)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(accent.copy(alpha = 0.14f))
                    .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(9.dp))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (badgeCount > 0) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(com.example.ui.theme.WarningRed)
                ) {
                    Text(
                        text = "$badgeCount",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun HomeQuickUtilityChip(
    label: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 48.dp)
            .clip(shape)
            .background(com.example.ui.theme.SurfaceDarkCard)
            .border(1.dp, com.example.ui.theme.HairlineDark, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(5.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp
            ),
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeThemePickerSheet(
    selectedPalette: BoardColorPalette,
    selectedStyle: BlockStyle,
    onSelectPalette: (BoardColorPalette) -> Unit,
    onSelectStyle: (BlockStyle) -> Unit,
    onDismiss: () -> Unit
) {
    PremiumModalSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = null,
                    tint = AccentCyan,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Background & Design Theme",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "Switch background colors, glow & block style live",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                BoardColorPalette.entries.chunked(2).forEach { rowPalettes ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowPalettes.forEach { pal ->
                            val isSelected = pal == selectedPalette
                            val boardColors = pal.toBoardColors()
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) AccentGold.copy(alpha = 0.16f) else BoardWellBg)
                                    .border(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) AccentGold else GlassBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onSelectPalette(pal) }
                                    .padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(22.dp)
                                        .clip(CircleShape)
                                        .background(boardColors.rim)
                                        .border(1.5.dp, Color.White.copy(alpha = 0.65f), CircleShape)
                                )
                                Text(
                                    text = pal.themeTitle,
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = OutfitFontFamily,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.5.sp
                                    ),
                                    color = if (isSelected) AccentGold else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Selected",
                                        tint = AccentGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        if (rowPalettes.size == 1) {
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }

            HorizontalDivider(color = DividerSubtle.copy(alpha = 0.6f))

            Text(
                text = "BLOCK DESIGN",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = AccentCyan
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                BlockStyle.entries.forEach { style ->
                    val isSelected = style == selectedStyle
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) AccentCyan.copy(alpha = 0.16f) else BoardWellBg)
                            .border(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) AccentCyan else GlassBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onSelectStyle(style) }
                            .padding(vertical = 10.dp, horizontal = 6.dp)
                    ) {
                        Canvas(modifier = Modifier.size(24.dp)) {
                            drawBevelBlock(
                                left = 0f,
                                top = 0f,
                                cellSize = size.minDimension,
                                color = BlockColor.CYAN,
                                blockStyle = style,
                                boardPalette = selectedPalette
                            )
                        }
                        Text(
                            text = style.displayName,
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            ),
                            color = if (isSelected) AccentCyan else TextSecondary
                        )
                    }
                }
            }
        }
    }
}
