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
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.game.BlockColor
import com.example.game.BlockStyle
import com.example.game.BoardColorPalette
import com.example.game.PlayerRankInfo
import com.example.game.ProfileAvatar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.CascadeTextStyles
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GoldBtnText
import com.example.ui.theme.HairlineDark
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.MuteDark
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceDarkElevated
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.toBoardColors

@Composable
fun PlayerAvatarBadge(
    avatarId: String,
    googlePhotoUrl: String? = null,
    size: Dp = 48.dp,
    hasCrownCosmetic: Boolean = false,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape((size.value * 0.25f).dp),
    borderColor: Color? = null,
    borderWidth: Dp? = null,
    modifier: Modifier = Modifier
) {
    val avatar = ProfileAvatar.fromId(avatarId)
    val effectiveBorderColor = borderColor ?: if (hasCrownCosmetic) AccentGold else GlassBorder
    val effectiveBorderWidth = borderWidth ?: if (hasCrownCosmetic) 1.5.dp else 1.dp

    val bgColor = when (avatar) {
        ProfileAvatar.CLASSIC_BLOCK -> SurfaceCard
        ProfileAvatar.COBALT_SHIELD -> Color(0xFF102A6B)
        ProfileAvatar.EMERALD_GEM -> Color(0xFF0A332A)
        ProfileAvatar.AMBER_CROWN -> Color(0xFF332106)
        ProfileAvatar.CORAL_SPARK -> Color(0xFF380C1B)
        ProfileAvatar.VIOLET_ORB -> Color(0xFF27114F)
        ProfileAvatar.GOLD_ROBOT -> Color(0xFF2B2006)
        ProfileAvatar.GOOGLE_PHOTO -> Color(0xFF122354)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(bgColor)
            .border(effectiveBorderWidth, effectiveBorderColor, shape)
    ) {
        if (avatar == ProfileAvatar.GOOGLE_PHOTO && !googlePhotoUrl.isNullOrBlank()) {
            AsyncImage(
                model = googlePhotoUrl,
                contentDescription = "Google profile photo",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(shape)
            )
        } else {
            GameAssetIcon(
                resId = GameAssetCatalog.avatarRes(avatar),
                contentDescription = avatar.label,
                size = size * 0.88f
            )
        }
        if (hasCrownCosmetic) {
            GameAssetIcon(
                resId = R.drawable.img_crown_gold,
                contentDescription = null,
                size = (size * 0.34f).coerceAtLeast(14.dp),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(1.dp)
            )
        }
    }
}

@Composable
fun PlayerProfileHeader(
    playerName: String?,
    rank: PlayerRankInfo,
    goldCoins: Int,
    diamonds: Int,
    avatarId: String = ProfileAvatar.CLASSIC_BLOCK.id,
    googlePhotoUrl: String? = null,
    hasCrownCosmetic: Boolean = false,
    isCloudSignedIn: Boolean = false,
    onOpenProfile: () -> Unit,
    onOpenWallet: () -> Unit,
    onOpenLeaderboard: () -> Unit,
    onOpenSettings: () -> Unit,
    trailingWalletAction: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val accent = if (hasCrownCosmetic) AccentGold else AccentCyan
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        // Top Row: Player Avatar, Name, Tier, plus Leaderboard and Settings
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
                PlayerAvatarBadge(
                    avatarId = avatarId,
                    googlePhotoUrl = googlePhotoUrl,
                    size = 46.dp,
                    hasCrownCosmetic = hasCrownCosmetic
                )

                Column(
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = playerName?.takeIf { it.isNotBlank() } ?: "Player",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        GameAssetIcon(
                            resId = GameAssetCatalog.rankCrestRes(rank.tierNumber),
                            contentDescription = null,
                            size = 16.dp
                        )
                        Text(
                            text = "Tier ${rank.tierNumber} · ${rank.title}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = JetBrainsMonoFontFamily,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 0.2.sp
                            ),
                            color = accent,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (trailingWalletAction != null) {
                    trailingWalletAction()
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCardElevated)
                        .border(1.dp, (if (isCloudSignedIn) AccentEmerald else GlassBorder).copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button, onClick = onOpenLeaderboard)
                        .testTag("home_leaderboard_button")
                ) {
                    GameAssetIcon(
                        resId = R.drawable.img_nav_podium,
                        contentDescription = "Leaderboard & Cloud",
                        size = 28.dp
                    )
                }
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCardElevated)
                        .border(1.dp, GlassBorder.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                        .clickable(role = Role.Button, onClick = onOpenSettings)
                        .testTag("home_top_settings_button")
                ) {
                    GameAssetIcon(
                        resId = R.drawable.img_nav_settings,
                        contentDescription = "Settings",
                        size = 28.dp
                    )
                }
            }
        }

        // Bottom Row: Clean Currency Wallet & XP Progress Pill
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenWallet)
            ) {
                CurrencyWallet(
                    goldCoins = goldCoins,
                    diamonds = diamonds,
                    compact = true
                )
            }

            // Compact XP pill that is neatly contained and aligned
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(com.example.ui.theme.SurfaceDarkElevated)
                    .border(1.dp, GlassBorder.copy(alpha = 0.45f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                GameAssetIcon(
                    resId = R.drawable.img_xp_star,
                    contentDescription = null,
                    size = 15.dp
                )
                Text(
                    text = "${rank.xp} XP",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    ),
                    color = TextPrimary
                )
            }
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
        cornerRadius = 16.dp,
        borderColor = HairlineDark,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(accent.copy(alpha = 0.14f))
                        .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
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
                    Text(
                        text = badge.uppercase(),
                        style = CascadeTextStyles.SectionKicker,
                        color = accent,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Medium,
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
                height = 48.dp,
                cornerRadius = 14.dp,
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
    val boardWellColor = boardPalette.toBoardColors().wellTop
    PremiumCard(
        cornerRadius = 16.dp,
        borderColor = if (isEquipped) AccentEmerald else GlassBorder,
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
                    .clip(RoundedCornerShape(12.dp))
                    .background(Brush.verticalGradient(listOf(boardWellColor, Color(0xFF060A16))))
                    .border(1.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
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

enum class BottomNavTab(
    val label: String,
    val icon: ImageVector,
    @DrawableRes val iconRes: Int,
    val tag: String
) {
    HOME("Home", Icons.Filled.Home, R.drawable.img_ui_home, "bottom_nav_home"),
    MODES("Play", Icons.Filled.GridView, R.drawable.img_ui_puzzle_grid, "bottom_nav_modes"),
    EVENTS("Events", Icons.Filled.AutoAwesome, R.drawable.img_fx_sparkles, "bottom_nav_events"),
    SHOP("Shop", Icons.Filled.ShoppingBag, R.drawable.img_nav_shop, "bottom_nav_shop"),
    PROFILE("Profile", Icons.Filled.Person, R.drawable.img_avatar_block, "bottom_nav_profile")
}

@Composable
fun BlockFallBottomNav(
    selectedTab: BottomNavTab,
    onSelectTab: (BottomNavTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(Color(0xFF131724).copy(alpha = 0.94f))
            .border(1.dp, GlassBorder.copy(alpha = 0.45f), shape)
            .padding(horizontal = 6.dp, vertical = 6.dp)
            .testTag("block_fall_bottom_nav")
    ) {
        BottomNavTab.entries.forEach { tab ->
            val selected = tab == selectedTab
            val tint by animateColorAsState(
                targetValue = if (selected) AccentCyan else MuteDark,
                label = "nav_tint"
            )
            val tabShape = RoundedCornerShape(14.dp)
            val bgModifier = if (selected) {
                Modifier
                    .clip(tabShape)
                    .background(AccentCyan.copy(alpha = 0.14f))
                    .border(1.dp, AccentCyan.copy(alpha = 0.35f), tabShape)
            } else {
                Modifier.clip(tabShape)
            }
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .weight(1f)
                    .defaultMinSize(minHeight = 48.dp)
                    .then(bgModifier)
                    .clickable(
                        role = Role.Tab,
                        onClick = { onSelectTab(tab) }
                    )
                    .semantics { this.selected = selected }
                    .padding(vertical = 6.dp, horizontal = 4.dp)
                    .testTag(tab.tag)
            ) {
                GameAssetIcon(
                    resId = tab.iconRes,
                    contentDescription = tab.label,
                    size = 22.dp,
                    alpha = if (selected) 1f else 0.72f,
                    modifier = Modifier.graphicsLayer {
                        val s = if (selected) 1.08f else 0.94f
                        scaleX = s
                        scaleY = s
                    }
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = tab.label,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 10.5.sp,
                        letterSpacing = 0.2.sp
                    ),
                    color = tint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

