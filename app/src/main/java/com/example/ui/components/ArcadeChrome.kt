package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.BlockColor
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldBright
import com.example.ui.theme.AccentGoldDeep
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnLip
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassTopSheen
import com.example.ui.theme.GoldBtnBottom
import com.example.ui.theme.GoldBtnLip
import com.example.ui.theme.GoldBtnText
import com.example.ui.theme.GoldBtnTop
import com.example.ui.theme.GreenBtnBottom
import com.example.ui.theme.GreenBtnLip
import com.example.ui.theme.GreenBtnTop
import com.example.ui.theme.LocalBlockStyle
import com.example.ui.theme.LocalBoardPalette
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PurpleBtnBottom
import com.example.ui.theme.PurpleBtnLip
import com.example.ui.theme.PurpleBtnTop
import com.example.ui.theme.RedBtnBottom
import com.example.ui.theme.RedBtnLip
import com.example.ui.theme.RedBtnTop
import com.example.ui.theme.SlateBtnBottom
import com.example.ui.theme.SlateBtnLip
import com.example.ui.theme.SlateBtnTop
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.toBoardColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

enum class ArcadeButtonStyle {
    GOLD,
    BLUE,
    PURPLE,
    GOLD_BLUE,
    GOLD_PURPLE,
    GREEN,
    RED,
    SLATE
}

/**
 * Deep midnight crystal cavern backdrop matching the BLOCK FALL reference style sheet:
 * vertical navy gradient, ambient cyan & violet cavern glows, subtle stars, and faceted crystal spires.
 */
@Composable
fun RoyalSkyBackdrop(modifier: Modifier = Modifier) {
    val palette = LocalBoardPalette.current
    val themeColors = palette.toBoardColors()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        themeColors.studioTop,
                        themeColors.studioMid,
                        themeColors.studioBottom
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Atmospheric cavern glows
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        themeColors.studioGlowSecondary.copy(alpha = 0.24f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.16f, h * 0.20f),
                    radius = w * 0.65f
                ),
                radius = w * 0.65f,
                center = Offset(w * 0.16f, h * 0.20f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        themeColors.studioGlowPrimary.copy(alpha = 0.22f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.82f, h * 0.56f),
                    radius = w * 0.72f
                ),
                radius = w * 0.72f,
                center = Offset(w * 0.82f, h * 0.56f)
            )
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        themeColors.studioGlowPrimary.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.5f, h * 0.94f),
                    radius = w * 0.85f
                ),
                radius = w * 0.85f,
                center = Offset(w * 0.5f, h * 0.94f)
            )

            // Subtle starry sparkles
            val starCoords = listOf(
                Triple(0.12f, 0.08f, 2.2f),
                Triple(0.32f, 0.14f, 1.5f),
                Triple(0.54f, 0.06f, 2.0f),
                Triple(0.76f, 0.11f, 2.6f),
                Triple(0.89f, 0.22f, 1.6f),
                Triple(0.20f, 0.34f, 1.8f),
                Triple(0.68f, 0.29f, 1.4f),
                Triple(0.42f, 0.44f, 1.7f),
                Triple(0.85f, 0.42f, 2.1f),
                Triple(0.10f, 0.62f, 1.5f),
                Triple(0.92f, 0.72f, 1.8f)
            )
            for ((sx, sy, r) in starCoords) {
                val cx = w * sx
                val cy = h * sy
                drawCircle(
                    color = Color.White.copy(alpha = 0.55f),
                    radius = r,
                    center = Offset(cx, cy)
                )
            }

            // Bottom-left crystal spire cluster
            drawCrystalCluster(
                baseX = w * 0.02f,
                baseY = h,
                scale = w * 0.16f,
                primary = themeColors.studioGlowPrimary,
                secondary = themeColors.studioGlowSecondary,
                flipX = false
            )
            // Bottom-right crystal spire cluster
            drawCrystalCluster(
                baseX = w * 0.98f,
                baseY = h,
                scale = w * 0.16f,
                primary = themeColors.studioGlowSecondary,
                secondary = themeColors.studioGlowPrimary,
                flipX = true
            )
        }
    }
}

private fun DrawScope.drawCrystalCluster(
    baseX: Float,
    baseY: Float,
    scale: Float,
    primary: Color,
    secondary: Color,
    flipX: Boolean
) {
    val dir = if (flipX) -1f else 1f

    // Tall main shard
    val mainPath = Path().apply {
        moveTo(baseX, baseY)
        lineTo(baseX + dir * scale * 0.12f, baseY - scale * 1.15f)
        lineTo(baseX + dir * scale * 0.42f, baseY - scale * 1.42f)
        lineTo(baseX + dir * scale * 0.66f, baseY - scale * 0.98f)
        lineTo(baseX + dir * scale * 0.72f, baseY)
        close()
    }
    drawPath(
        path = mainPath,
        brush = Brush.verticalGradient(
            colors = listOf(primary.copy(alpha = 0.34f), primary.copy(alpha = 0.08f)),
            startY = baseY - scale * 1.42f,
            endY = baseY
        )
    )
    drawPath(
        path = mainPath,
        color = primary.copy(alpha = 0.28f),
        style = Stroke(width = 1.5f)
    )

    // Side secondary shard
    val sidePath = Path().apply {
        moveTo(baseX + dir * scale * 0.45f, baseY)
        lineTo(baseX + dir * scale * 0.75f, baseY - scale * 0.85f)
        lineTo(baseX + dir * scale * 1.02f, baseY - scale * 0.98f)
        lineTo(baseX + dir * scale * 1.18f, baseY - scale * 0.52f)
        lineTo(baseX + dir * scale * 1.24f, baseY)
        close()
    }
    drawPath(
        path = sidePath,
        brush = Brush.verticalGradient(
            colors = listOf(secondary.copy(alpha = 0.30f), secondary.copy(alpha = 0.06f)),
            startY = baseY - scale * 0.98f,
            endY = baseY
        )
    )
}

/**
 * 2x2 puzzle block emblem rendered in the user's selected BlockStyle & BoardPalette.
 */
@Composable
fun ArcadeBlockEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 88.dp
) {
    val blockStyle = LocalBlockStyle.current
    val boardPalette = LocalBoardPalette.current
    Canvas(modifier = modifier.size(size)) {
        val cell = this.size.width / 2f
        val gap = cell * 0.11f
        val tile = cell - gap
        drawBevelBlock(gap / 2f, gap / 2f, tile, BlockColor.CORAL, blockStyle = blockStyle, boardPalette = boardPalette)
        drawBevelBlock(cell + gap / 2f, gap / 2f, tile, BlockColor.AMBER, blockStyle = blockStyle, boardPalette = boardPalette)
        drawBevelBlock(gap / 2f, cell + gap / 2f, tile, BlockColor.CYAN, blockStyle = blockStyle, boardPalette = boardPalette)
        drawBevelBlock(cell + gap / 2f, cell + gap / 2f, tile, BlockColor.EMERALD, blockStyle = blockStyle, boardPalette = boardPalette)
    }
}

/**
 * Glossy 3D BLOCK FALL logo matching the crystal & metallic gold visual identity.
 */
@Composable
fun ArcadeTitleLogo(
    fontSize: TextUnit = 38.sp,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = "BLOCK",
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                lineHeight = fontSize * 0.94f,
                letterSpacing = 1.6.sp,
                shadow = Shadow(
                    color = Color(0xFF03081C),
                    offset = Offset(0f, 5f),
                    blurRadius = 10f
                )
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = "FALL",
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = fontSize,
                lineHeight = fontSize * 0.94f,
                letterSpacing = 2.6.sp,
                brush = Brush.verticalGradient(
                    colors = listOf(AccentGoldBright, AccentGold, AccentCyan)
                ),
                shadow = Shadow(
                    color = Color(0xFF040A24),
                    offset = Offset(0f, 5f),
                    blurRadius = 12f
                )
            ),
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Tactile 3D glossy arcade button with reflective top highlight, crisp bottom lip,
 * and optional metallic gold rim matching the BLOCK FALL style reference.
 */
@Composable
fun GlossyArcadeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ArcadeButtonStyle = ArcadeButtonStyle.BLUE,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    cornerRadius: Dp = 26.dp,
    fontSize: TextUnit = 16.sp,
    testTag: String? = null
) {
    val hasGoldRim = style == ArcadeButtonStyle.GOLD_BLUE || style == ArcadeButtonStyle.GOLD_PURPLE
    val (topColor, bottomColor, lipColor, contentColor) = when (style) {
        ArcadeButtonStyle.GOLD -> Quad(GoldBtnTop, GoldBtnBottom, GoldBtnLip, GoldBtnText)
        ArcadeButtonStyle.BLUE, ArcadeButtonStyle.GOLD_BLUE -> Quad(BlueBtnTop, BlueBtnBottom, BlueBtnLip, Color.White)
        ArcadeButtonStyle.PURPLE, ArcadeButtonStyle.GOLD_PURPLE -> Quad(PurpleBtnTop, PurpleBtnBottom, PurpleBtnLip, Color.White)
        ArcadeButtonStyle.GREEN -> Quad(GreenBtnTop, GreenBtnBottom, GreenBtnLip, Color.White)
        ArcadeButtonStyle.RED -> Quad(RedBtnTop, RedBtnBottom, RedBtnLip, Color.White)
        ArcadeButtonStyle.SLATE -> Quad(SlateBtnTop, SlateBtnBottom, SlateBtnLip, TextPrimary)
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.76f, stiffness = 560f),
        label = "arcade_btn_press"
    )

    val shape = RoundedCornerShape(cornerRadius)
    val rippleColor = if (style == ArcadeButtonStyle.SLATE) AccentCyan else Color.White

    val outerModifier = modifier
        .height(height)
        .graphicsLayer {
            scaleX = pressScale
            scaleY = pressScale
        }
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        .alpha(if (enabled) 1f else 0.45f)
        .shadow(elevation = 6.dp, shape = shape, clip = false)
        .clip(shape)
        .background(
            if (hasGoldRim) {
                Brush.verticalGradient(listOf(AccentGoldBright, AccentGold, AccentGoldDeep))
            } else {
                Brush.verticalGradient(listOf(lipColor, lipColor))
            }
        )
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(color = rippleColor),
            enabled = enabled,
            onClick = onClick
        )

    Box(modifier = outerModifier) {
        val innerShape = RoundedCornerShape((cornerRadius - 2.dp).coerceAtLeast(8.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    start = if (hasGoldRim) 2.5.dp else 0.dp,
                    end = if (hasGoldRim) 2.5.dp else 0.dp,
                    top = if (hasGoldRim) 2.dp else 0.dp,
                    bottom = if (hasGoldRim) 3.5.dp else 3.5.dp
                )
                .clip(innerShape)
                .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
                .border(
                    width = 1.dp,
                    color = if (style == ArcadeButtonStyle.SLATE) GlassBorder else Color.White.copy(alpha = 0.28f),
                    shape = innerShape
                )
        ) {
            // Glossy top specular sheen lens
            if (style != ArcadeButtonStyle.SLATE) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .height(height * 0.42f)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                        .clip(innerShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.36f),
                                    Color.White.copy(alpha = 0.05f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 14.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = fontSize,
                        letterSpacing = 0.4.sp,
                        shadow = if (style != ArcadeButtonStyle.GOLD && style != ArcadeButtonStyle.SLATE) {
                            Shadow(
                                color = Color.Black.copy(alpha = 0.35f),
                                offset = Offset(0f, 2f),
                                blurRadius = 4f
                            )
                        } else null
                    ),
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Midnight-navy crystal panel with elevated top gradient, inner glass sheen, and crisp glass border.
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    borderColor: Color = GlassBorder,
    backgroundBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            SurfaceCardElevated,
            SurfaceCard
        )
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .shadow(6.dp, shape, clip = false)
            .clip(shape)
            .background(backgroundBrush)
            .border(1.2.dp, borderColor, shape),
        content = content
    )
}

/**
 * Ornate gold-rimmed royal ribbon banner for modal headers & event cards.
 */
@Composable
fun GoldMetallicBanner(
    text: String,
    modifier: Modifier = Modifier,
    isPurple: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)
    val fillColors = if (isPurple) {
        listOf(PurpleBtnTop, PurpleBtnBottom)
    } else {
        listOf(BlueBtnTop, BlueBtnBottom)
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .shadow(6.dp, shape, clip = false)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(AccentGoldBright, AccentGold, AccentGoldDeep)))
            .padding(2.5.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Brush.verticalGradient(fillColors))
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
            .padding(horizontal = 22.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Black,
                fontSize = 16.sp,
                letterSpacing = 1.sp,
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.45f),
                    offset = Offset(0f, 2f),
                    blurRadius = 4f
                )
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Glossy 3D Gold Coin drawn with Compose Canvas (matching the style reference).
 */
@Composable
fun CrystalCoinBadge(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val r = this.size.minDimension / 2f
        val c = Offset(this.size.width / 2f, this.size.height / 2f)
        drawCircle(
            brush = Brush.verticalGradient(listOf(AccentGoldBright, AccentGold, AccentGoldDeep)),
            radius = r,
            center = c
        )
        drawCircle(
            color = AccentGoldDeep,
            radius = r * 0.72f,
            center = c,
            style = Stroke(width = (r * 0.14f).coerceAtLeast(1.2f))
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.55f),
            radius = r * 0.25f,
            center = Offset(c.x - r * 0.28f, c.y - r * 0.28f)
        )
    }
}

/**
 * Faceted cyan Diamond drawn with Compose Canvas (matching the style reference).
 */
@Composable
fun CrystalDiamondBadge(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val path = Path().apply {
            moveTo(w * 0.22f, h * 0.14f)
            lineTo(w * 0.78f, h * 0.14f)
            lineTo(w * 0.96f, h * 0.42f)
            lineTo(w * 0.50f, h * 0.92f)
            lineTo(w * 0.04f, h * 0.42f)
            close()
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                listOf(Color(0xFFA8F3FF), AccentCyan, Color(0xFF0A6BFF))
            )
        )
        val topFacet = Path().apply {
            moveTo(w * 0.22f, h * 0.14f)
            lineTo(w * 0.78f, h * 0.14f)
            lineTo(w * 0.65f, h * 0.42f)
            lineTo(w * 0.35f, h * 0.42f)
            close()
        }
        drawPath(path = topFacet, color = Color.White.copy(alpha = 0.45f))
        drawPath(path = path, color = Color.White.copy(alpha = 0.55f), style = Stroke(width = 1.2f))
    }
}

/**
 * Gold/Silver/Bronze ribbon medal Canvas art matching the style reference.
 */
@Composable
fun CrystalMedalBadge(
    tier: Int,
    unlocked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp
) {
    val (rimLight, rimDark, ribbonColor) = when {
        !unlocked -> Triple(Color(0xFF475B94), Color(0xFF23305C), Color(0xFF2C3E73))
        tier >= 3 -> Triple(AccentGoldBright, AccentGoldDeep, Color(0xFFE52445))
        tier == 2 -> Triple(Color(0xFF9DF0FF), Color(0xFF1A84E8), Color(0xFF1995FF))
        else -> Triple(Color(0xFFFFBC80), Color(0xFF9C4E19), Color(0xFF1CC95B))
    }

    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val cx = w * 0.5f
        val cy = h * 0.40f
        val radius = w * 0.32f

        // Ribbons
        val leftRibbon = Path().apply {
            moveTo(cx - radius * 0.55f, cy + radius * 0.5f)
            lineTo(cx - radius * 0.85f, h * 0.96f)
            lineTo(cx - radius * 0.32f, h * 0.84f)
            lineTo(cx - radius * 0.05f, h * 0.96f)
            lineTo(cx - radius * 0.05f, cy + radius * 0.5f)
            close()
        }
        val rightRibbon = Path().apply {
            moveTo(cx + radius * 0.55f, cy + radius * 0.5f)
            lineTo(cx + radius * 0.85f, h * 0.96f)
            lineTo(cx + radius * 0.32f, h * 0.84f)
            lineTo(cx + radius * 0.05f, h * 0.96f)
            lineTo(cx + radius * 0.05f, cy + radius * 0.5f)
            close()
        }
        drawPath(leftRibbon, ribbonColor)
        drawPath(rightRibbon, ribbonColor)

        // Outer medal ring
        drawCircle(
            brush = Brush.verticalGradient(listOf(rimLight, rimDark)),
            radius = radius,
            center = Offset(cx, cy)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = radius,
            center = Offset(cx, cy),
            style = Stroke(width = 1.5f)
        )

        // Inner medallion center
        drawCircle(
            color = if (unlocked) Color(0xFF0E1D4B) else Color(0xFF141E40),
            radius = radius * 0.70f,
            center = Offset(cx, cy)
        )

        // Center star
        drawStarPolygon(
            center = Offset(cx, cy),
            outerRadius = radius * 0.48f,
            innerRadius = radius * 0.22f,
            color = if (unlocked) rimLight else Color(0xFF5D72AB)
        )
    }
}

private fun DrawScope.drawStarPolygon(
    center: Offset,
    outerRadius: Float,
    innerRadius: Float,
    color: Color
) {
    val path = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outerRadius else innerRadius
        val angle = (-PI / 2.0 + i * PI / 5.0).toFloat()
        val px = center.x + cos(angle) * r
        val py = center.y + sin(angle) * r
        if (i == 0) path.moveTo(px, py) else path.lineTo(px, py)
    }
    path.close()
    drawPath(path, color)
}

/**
 * Glossy circular badge for Pause, Level Complete, and Game Over modals.
 */
@Composable
fun FloatingModalBadge(
    backgroundBrush: Brush,
    modifier: Modifier = Modifier,
    size: Dp = 72.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(8.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(backgroundBrush)
            .border(2.5.dp, AccentGoldBright, CircleShape),
        content = content
    )
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
