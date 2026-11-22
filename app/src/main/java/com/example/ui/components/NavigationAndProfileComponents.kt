package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.AchievementId
import com.example.game.BlockColor
import com.example.game.BlockStyle
import com.example.game.BoardColorPalette
import com.example.game.PlayerRankInfo
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.CascadeTextStyles
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBtnText
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

enum class PrimaryNavTab(
    val label: String,
    val compactLabel: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    HOME("Home", "Home", Icons.Filled.Home, Icons.Outlined.Home, "bottom_nav_home"),
    PLAY("Play", "Play", Icons.Filled.GridView, Icons.Outlined.GridView, "bottom_nav_play"),
    CHALLENGES("Challenges", "Levels", Icons.Filled.EmojiEvents, Icons.Outlined.EmojiEvents, "bottom_nav_challenges"),
    SHOP("Shop", "Shop", Icons.Filled.ShoppingBag, Icons.Outlined.ShoppingBag, "bottom_nav_shop"),
    PROFILE("Profile", "Profile", Icons.Filled.Person, Icons.Outlined.Person, "bottom_nav_profile")
}

/**
 * Compact top quick-navigation strip.
 *
 * Replaces the previous five-item bottom navigation bar. It keeps the existing navigation
 * callbacks, screen routes and test tags intact while freeing the bottom of the screen for
 * the game board, piece tray and content. Rendered at the top of [CascadeScreenContainer].
 */
@Composable
fun PremiumQuickNav(
    selectedTab: PrimaryNavTab,
    onSelectTab: (PrimaryNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Surface(
        color = SurfaceCard.copy(alpha = 0.92f),
        shape = shape,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("premium_bottom_navigation")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(SurfaceCardElevated.copy(alpha = 0.55f), SurfaceCard.copy(alpha = 0.85f))
                    )
                )
                .border(1.dp, GlassBorder.copy(alpha = 0.4f), shape)
                .padding(horizontal = 4.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PrimaryNavTab.entries.forEach { tab ->
                val isSelected = tab == selectedTab
                val tint by animateColorAsState(
                    targetValue = if (isSelected) AccentCyan else TextMuted,
                    label = "nav_tab_tint"
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) AccentCyan.copy(alpha = 0.12f) else Color.Transparent)
                        .clickable(role = Role.Tab, onClick = { onSelectTab(tab) })
                        .semantics { selected = isSelected }
                        .padding(vertical = 3.dp)
                        .testTag(tab.testTag)
                ) {
                    Icon(
                        imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                        contentDescription = tab.label,
                        tint = tint,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(Modifier.height(1.dp))
                    Text(
                        text = tab.compactLabel,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 9.5.sp,
                            letterSpacing = 0.1.sp
                        ),
                        color = tint,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * @deprecated The bottom bar has been removed. Use [PremiumQuickNav]; this alias keeps
 * existing call sites and test tags source-compatible.
 */
@Deprecated("Use PremiumQuickNav — bottom navigation has been replaced by a top strip.")
@Composable
fun PremiumBottomNavigation(
    selectedTab: PrimaryNavTab,
    onSelectTab: (PrimaryNavTab) -> Unit,
    modifier: Modifier = Modifier
) = PremiumQuickNav(selectedTab = selectedTab, onSelectTab = onSelectTab, modifier = modifier)

@Composable
fun PlayerProfileHeader(
    playerName: String?,
    rank: PlayerRankInfo,
    goldCoins: Int,
    diamonds: Int,
    hasCrownCosmetic: Boolean = false,
    isCloudSignedIn: Boolean = false,
    onOpenProfile: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accent = if (hasCrownCosmetic) AccentGold else AccentCyan
    Column(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // Identity row — avatar + name + rank on the left, quick actions on the right.
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable(onClick = onOpenProfile)
                    .padding(vertical = 2.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Brush.verticalGradient(listOf(SurfaceCardElevated, SurfaceCard)))
                        .border(
                            width = if (hasCrownCosmetic) 1.5.dp else 1.dp,
                            color = accent.copy(alpha = if (hasCrownCosmetic) 0.9f else 0.5f),
                            shape = RoundedCornerShape(14.dp)
                        )
                ) {
                    ArcadeBlockEmblem(size = 27.dp)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = playerName?.takeIf { it.isNotBlank() } ?: "Block Tactician",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "TIER ${rank.tierNumber} · ${rank.title}",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontSize = 10.sp,
                            letterSpacing = 0.2.sp
                        ),
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconActionButton(
                    icon = Icons.Filled.Leaderboard,
                    contentDescription = "Leaderboard & Cloud",
                    size = 48.dp,
                    tint = if (isCloudSignedIn) AccentEmerald else AccentCyan,
                    borderColor = if (isCloudSignedIn) AccentEmerald.copy(alpha = 0.6f) else AccentCyan.copy(alpha = 0.45f),
                    onClick = onOpenLeaderboard,
                    testTag = "home_leaderboard_button"
                )
                IconActionButton(
                    icon = Icons.Filled.Settings,
                    contentDescription = "Settings",
                    size = 48.dp,
                    onClick = onOpenSettings,
                    testTag = "home_top_settings_button"
                )
            }
        }

        // Wallet row — dedicated line so gold and diamond values never overlap the name.
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .clickable(onClick = onOpenWallet),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CurrencyWallet(
                goldCoins = goldCoins,
                diamonds = diamonds,
                compact = true,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${rank.xp} XP",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 10.sp
                ),
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
fun GameModeCard(
    title: String,
    badge: String,
    description: String,
    icon: ImageVector,
    accent: Color,
    buttonText: String,
    buttonStyle: ArcadeButtonStyle,
    buttonTag: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    artwork: (@Composable () -> Unit)? = null,
    secondaryStat: String? = null,
    enabled: Boolean = true
) {
    PremiumCard(
        cornerRadius = 22.dp,
        borderColor = accent.copy(alpha = if (enabled) 0.45f else 0.25f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accent.copy(alpha = 0.14f))
                        .border(1.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(16.dp))
                ) {
                    if (artwork != null) {
                        artwork()
                    } else {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = accent,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = badge.uppercase(),
                            style = CascadeTextStyles.SectionKicker,
                            color = accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (secondaryStat != null) {
                        Text(
                            text = secondaryStat,
                            style = CascadeTextStyles.NumericBalance.copy(fontSize = 12.sp),
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            PremiumPrimaryButton(
                text = buttonText,
                icon = if (enabled) Icons.Filled.PlayArrow else Icons.Filled.Lock,
                style = buttonStyle,
                enabled = enabled,
                onClick = onAction,
                height = 50.dp,
                cornerRadius = 15.dp,
                fontSize = 15.sp,
                testTag = buttonTag,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun SkinCard(
    title: String,
    subtitle: String,
    blockStyle: BlockStyle,
    boardPalette: BoardColorPalette,
    isUnlocked: Boolean,
    isEquipped: Boolean,
    unlockRequirement: String,
    onEquip: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    val accent = when {
        isEquipped -> AccentEmerald
        isUnlocked -> AccentCyan
        else -> TextMuted
    }
    PremiumCard(
        cornerRadius = 18.dp,
        borderColor = accent.copy(alpha = if (isEquipped) 0.72f else 0.34f),
        modifier = modifier
            .fillMaxWidth()
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(BoardWellBg)
                    .border(1.dp, accent.copy(alpha = 0.38f), RoundedCornerShape(14.dp))
                    .padding(6.dp)
            ) {
                Canvas(modifier = Modifier.size(42.dp)) {
                    val cell = size.width / 2f
                    val gap = cell * 0.12f
                    val tile = cell - gap
                    drawBevelBlock(gap / 2f, gap / 2f, tile, BlockColor.CYAN, blockStyle = blockStyle, boardPalette = boardPalette)
                    drawBevelBlock(cell + gap / 2f, gap / 2f, tile, BlockColor.AMBER, blockStyle = blockStyle, boardPalette = boardPalette)
                    drawBevelBlock(gap / 2f, cell + gap / 2f, tile, BlockColor.CORAL, blockStyle = blockStyle, boardPalette = boardPalette)
                    drawBevelBlock(cell + gap / 2f, cell + gap / 2f, tile, BlockColor.EMERALD, blockStyle = blockStyle, boardPalette = boardPalette)
                }
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontSize = 15.sp),
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isUnlocked) "Unlocked" else unlockRequirement,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontSize = 10.sp,
                        letterSpacing = 0.3.sp
                    ),
                    color = if (isUnlocked) AccentEmerald else AccentGold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (isEquipped) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(AccentEmerald.copy(alpha = 0.16f))
                        .border(1.dp, AccentEmerald.copy(alpha = 0.55f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = AccentEmerald,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = "Equipped",
                        style = MaterialTheme.typography.labelSmall,
                        color = AccentEmerald
                    )
                }
            } else if (isUnlocked) {
                PremiumPrimaryButton(
                    text = "Equip",
                    style = ArcadeButtonStyle.BLUE,
                    onClick = onEquip,
                    height = 40.dp,
                    cornerRadius = 12.dp,
                    fontSize = 13.sp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(BoardWellBg)
                        .border(1.dp, GlassBorder.copy(alpha = 0.4f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = null,
                        tint = TextMuted,
                        modifier = Modifier.size(13.dp)
                    )
                    Text(
                        text = "Locked",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementCard(
    achievement: AchievementId,
    unlocked: Boolean,
    modifier: Modifier = Modifier
) {
    val icon = when (achievement.iconKey) {
        "star" -> Icons.Filled.Star
        "layers" -> Icons.Filled.Layers
        "bolt" -> Icons.Filled.Bolt
        "auto_awesome" -> Icons.Filled.AutoAwesome
        "trending" -> Icons.AutoMirrored.Filled.TrendingUp
        "trophy" -> Icons.Filled.EmojiEvents
        "flag" -> Icons.Filled.Flag
        "check" -> Icons.Filled.CheckCircle
        "crown" -> Icons.Filled.EmojiEvents
        "calendar" -> Icons.Filled.CalendarToday
        else -> Icons.Filled.Star
    }
    val accent = if (unlocked) AccentGold else TextMuted
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (unlocked) SurfaceCardElevated.copy(alpha = 0.78f) else BoardWellBg)
            .border(
                width = 1.dp,
                color = if (unlocked) AccentGold.copy(alpha = 0.42f) else GlassBorder.copy(alpha = 0.25f),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.14f))
                .border(1.dp, accent.copy(alpha = 0.36f), RoundedCornerShape(10.dp))
        ) {
            Icon(
                imageVector = if (unlocked) icon else Icons.Filled.Lock,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(19.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = achievement.title,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontSize = 14.sp,
                    fontWeight = if (unlocked) FontWeight.Bold else FontWeight.SemiBold
                ),
                color = if (unlocked) TextPrimary else TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = achievement.description,
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.5.sp),
                color = TextMuted,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .clip(CircleShape)
                .background(if (unlocked) AccentEmerald.copy(alpha = 0.16f) else SurfaceCard)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Icon(
                imageVector = if (unlocked) Icons.Filled.CheckCircle else Icons.Filled.Lock,
                contentDescription = null,
                tint = if (unlocked) AccentEmerald else TextMuted,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = if (unlocked) "UNLOCKED" else "LOCKED",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontSize = 9.5.sp
                ),
                color = if (unlocked) AccentEmerald else TextMuted
            )
        }
    }
}
