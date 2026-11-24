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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.BlockColor
import com.example.game.DailyChallengeGenerator
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
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.LocalBlockStyle
import com.example.ui.theme.LocalBoardPalette
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
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
    onOpenStatistics: () -> Unit
) {
    val savedSummary = state.savedClassicSummary
    val canContinue = savedSummary != null
    val hasCrown = "cosmetic_prism_crown" in state.wallet.unlockedEntitlements
    val dailySpec = remember {
        DailyChallengeGenerator.forDateKey(DailyChallengeGenerator.todayDateKey())
    }

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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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
                                    onOpenModes = onOpenModes,
                                    onOpenChallenges = onOpenChallenges,
                                    onOpenShop = onOpenShop,
                                    onOpenSettings = onOpenSettings,
                                    onOpenStatistics = onOpenStatistics
                                )
                            }
                        }
                    } else {
                        HomeHeroSection(
                            bestScore = state.classicBestScore,
                            canContinue = canContinue,
                            savedScore = savedSummary?.score,
                            savedMoves = savedSummary?.movesPlayed,
                            onPlayNewClassic = onPlayNewClassic,
                            onContinueClassic = onContinueClassic
                        )
                        DailyRewardStrip(
                            dailyTitle = dailySpec.displayDate,
                            targetScore = dailySpec.level.targetScore,
                            onClick = onPlayDaily
                        )
                        HomeActionHub(
                            onOpenModes = onOpenModes,
                            onOpenChallenges = onOpenChallenges,
                            onOpenShop = onOpenShop,
                            onOpenSettings = onOpenSettings,
                            onOpenStatistics = onOpenStatistics
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                }
            }
        }
    }
}

private val MiniWheelColors = listOf(
    Color(0xFF7C4DFF), Color(0xFF00B8D4), Color(0xFFFFB300), Color(0xFF26C281),
    Color(0xFFFF5252), Color(0xFFEC407A), Color(0xFF3D8BFF), Color(0xFFFF8F00)
)

/** Lucky Wheel header shortcut: custom-drawn colorful spinning prize wheel with spin badge. */
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

            // Outer golden bezel
            drawCircle(
                brush = Brush.linearGradient(listOf(Color(0xFFFFE57F), Color(0xFFFFA000))),
                radius = outer,
                center = center
            )
            drawCircle(color = Color(0xFF3A1B7A), radius = wheelRadius + 1.5f, center = center)

            // Rotating rainbow slices
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

            // Center hub
            drawCircle(Color(0xFFFFD54F), radius = wheelRadius * 0.30f, center = center)
            drawCircle(Color(0xFF3A1B7A), radius = wheelRadius * 0.16f, center = center)

            // Top pointer
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
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (spinPoints > 0) Color(0xFFFF3D57) else Color(0xFF64748B))
                .border(1.5.dp, Color.White, androidx.compose.foundation.shape.CircleShape)
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
    onPlayNewClassic: () -> Unit,
    onContinueClassic: () -> Unit,
    modifier: Modifier = Modifier
) {
    val boardWellColor = LocalBoardPalette.current.toBoardColors().wellTop

    PremiumCard(
        cornerRadius = 18.dp,
        borderColor = GlassBorder,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
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
                        .size(72.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(boardWellColor)
                        .padding(8.dp)
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    ArcadeTitleLogo(fontSize = 28.sp)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(BoardWellBg)
                            .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            tint = AccentGold,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "Best: $bestScore",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.5.sp
                            ),
                            color = TextPrimary,
                            maxLines = 1
                        )
                    }
                }
            }

            // Primary focus: unmistakable classic Play button.
            PremiumPrimaryButton(
                text = stringResource(R.string.nav_play),
                icon = Icons.Filled.PlayArrow,
                style = ArcadeButtonStyle.GREEN,
                onClick = onPlayNewClassic,
                height = 58.dp,
                cornerRadius = 14.dp,
                fontSize = 19.sp,
                testTag = "home_play_button",
                modifier = Modifier.fillMaxWidth()
            )

            if (canContinue) {
                PremiumPrimaryButton(
                    text = "Continue · ${savedScore ?: 0} pts (${savedMoves ?: 0} moves)",
                    icon = Icons.Filled.Replay,
                    style = ArcadeButtonStyle.SLATE,
                    onClick = onContinueClassic,
                    height = 48.dp,
                    cornerRadius = 12.dp,
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
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(SurfaceCard)
            .border(1.dp, GlassBorder, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
            .testTag("home_daily_button")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(AccentPurple.copy(alpha = 0.12f))
        ) {
            Icon(
                imageVector = Icons.Filled.CalendarToday,
                contentDescription = null,
                tint = AccentPurple,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Daily Puzzle & Rewards",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$dailyTitle · Target $targetScore · Earn Gold & Gems",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Play Daily Puzzle",
            tint = AccentPurple,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun HomeActionHub(
    onOpenModes: () -> Unit,
    onOpenChallenges: () -> Unit,
    onOpenShop: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStatistics: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HomeHubTile(
                title = "Game Modes",
                subtitle = "Classic · Challenge · Daily",
                icon = Icons.Filled.GridView,
                accent = AccentCyan,
                onClick = onOpenModes,
                testTag = "home_modes_button",
                modifier = Modifier.weight(1f)
            )
            HomeHubTile(
                title = stringResource(R.string.nav_challenges),
                subtitle = "8 tactical puzzle boards",
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
                subtitle = "Boosters & coins",
                icon = Icons.Filled.ShoppingBag,
                accent = AccentEmerald,
                onClick = onOpenShop,
                testTag = "home_shop_button",
                modifier = Modifier.weight(1f)
            )
            HomeHubTile(
                title = stringResource(R.string.nav_settings),
                subtitle = "Sound & board theme",
                icon = Icons.Filled.Settings,
                accent = AccentPurple,
                onClick = onOpenSettings,
                testTag = "home_settings_button",
                modifier = Modifier.weight(1f)
            )
        }

        HomeHubTile(
            title = "Profile",
            subtitle = "Avatar · rank · best score",
            icon = Icons.Filled.Leaderboard,
            accent = AccentCyan,
            onClick = onOpenStatistics,
            testTag = "home_statistics_button",
            modifier = Modifier.fillMaxWidth()
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
    val shape = RoundedCornerShape(14.dp)
    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier
            .shadow(2.dp, shape, clip = false)
            .clip(shape)
            .background(SurfaceCard)
            .border(1.dp, GlassBorder, shape)
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
                    .size(34.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(accent.copy(alpha = 0.14f))
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(19.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
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
