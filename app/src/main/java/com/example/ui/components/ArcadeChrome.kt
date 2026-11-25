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
import com.example.ui.theme.CanvasDark
import com.example.ui.theme.Charcoal
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GlassTopSheen
import com.example.ui.theme.GoldBtnBottom
import com.example.ui.theme.GoldBtnLip
import com.example.ui.theme.GoldBtnText
import com.example.ui.theme.GoldBtnTop
import com.example.ui.theme.GreenBtnBottom
import com.example.ui.theme.GreenBtnLip
import com.example.ui.theme.GreenBtnTop
import com.example.ui.theme.HairlineDark
import com.example.ui.theme.LocalBlockStyle
import com.example.ui.theme.LocalBoardPalette
import com.example.ui.theme.MuteDark
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PsBlue
import com.example.ui.theme.PsBluePressed
import com.example.ui.theme.PsCommerce
import com.example.ui.theme.PsCommercePressed
import com.example.ui.theme.PsGoldEnd
import com.example.ui.theme.PsGoldMid
import com.example.ui.theme.PsGoldStart
import com.example.ui.theme.PurpleBtnBottom
import com.example.ui.theme.PurpleBtnLip
import com.example.ui.theme.PurpleBtnTop
import com.example.ui.theme.RedBtnBottom
import com.example.ui.theme.RedBtnLip
import com.example.ui.theme.RedBtnTop
import com.example.ui.theme.SlateBtnBottom
import com.example.ui.theme.SlateBtnLip
import com.example.ui.theme.SlateBtnTop
import com.example.ui.theme.SurfaceDarkCard
import com.example.ui.theme.SurfaceDarkElevated
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
    COMMERCE,
    PURPLE,
    GOLD_BLUE,
    GOLD_PURPLE,
    GREEN,
    RED,
    SLATE
}

/**
 * PlayStation editorial canvas backdrop (per DESIGN.md):
 * Alternating chapter contrast starting with Canvas Dark (#000000)
 * and deep graphite elevation (#121314) with subtle PlayStation Blue ambient accent.
 */
@Composable
fun RoyalSkyBackdrop(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CanvasDark,
                        Color(0xFF08090C),
                        SurfaceDarkElevated
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // Quiet, cinematic PlayStation Blue atmospheric ambient glow at top-right
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PsBlue.copy(alpha = 0.16f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.88f, h * 0.18f),
                    radius = w * 0.70f
                ),
                radius = w * 0.70f,
                center = Offset(w * 0.88f, h * 0.18f)
            )

            // Subtle lower-left dim glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        PsGoldStart.copy(alpha = 0.05f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.12f, h * 0.75f),
                    radius = w * 0.55f
                ),
                radius = w * 0.55f,
                center = Offset(w * 0.12f, h * 0.75f)
            )
        }
    }
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
 * Editorial PlayStation SST style BLOCK FALL title logo (per DESIGN.md):
 * Clean white display tier at light/airy weight paired with signature PlayStation Blue.
 */
@Composable
fun ArcadeTitleLogo(
    fontSize: TextUnit = 36.sp,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Text(
            text = "BLOCK",
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Light,
                fontSize = fontSize,
                letterSpacing = 1.2.sp
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = "FALL",
            style = MaterialTheme.typography.displayMedium.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = fontSize,
                letterSpacing = 1.6.sp
            ),
            color = PsBlue,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * PlayStation CTA Button (per DESIGN.md button-primary / button-commerce / button-secondary-dark):
 * - Primary: PlayStation Blue (#0070d1), fully-rounded pill (rounded.full), height ~48-52dp.
 * - Commerce: Orange (#d53b00) reserved for store/buy actions.
 * - Secondary: Sleek outline on dark canvas with hairline border.
 * - Type: Bold 700 with +0.45sp tracking.
 */
@Composable
fun GlossyArcadeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ArcadeButtonStyle = ArcadeButtonStyle.BLUE,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 50.dp,
    cornerRadius: Dp = 9999.dp, // Fully rounded pill per DESIGN.md rounded.full
    fontSize: TextUnit = 16.sp,
    testTag: String? = null
) {
    val (bgBrush, borderColor, contentColor) = when (style) {
        ArcadeButtonStyle.BLUE -> Triple(
            Brush.verticalGradient(listOf(PsBlue, PsBluePressed)),
            Color.Transparent,
            Color.White
        )
        ArcadeButtonStyle.COMMERCE -> Triple(
            Brush.verticalGradient(listOf(PsCommerce, PsCommercePressed)),
            Color.Transparent,
            Color.White
        )
        ArcadeButtonStyle.GOLD -> Triple(
            Brush.horizontalGradient(listOf(PsGoldStart, PsGoldMid, PsGoldEnd)),
            Color.Transparent,
            Color(0xFF121314)
        )
        ArcadeButtonStyle.GOLD_BLUE -> Triple(
            Brush.verticalGradient(listOf(PsBlue, PsBluePressed)),
            PsGoldStart.copy(alpha = 0.85f),
            Color.White
        )
        ArcadeButtonStyle.GOLD_PURPLE -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF7040E6), Color(0xFF5020B0))),
            PsGoldStart.copy(alpha = 0.85f),
            Color.White
        )
        ArcadeButtonStyle.PURPLE -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF7040E6), Color(0xFF5020B0))),
            Color.Transparent,
            Color.White
        )
        ArcadeButtonStyle.GREEN -> Triple(
            Brush.verticalGradient(listOf(Color(0xFF0CA34A), Color(0xFF087A36))),
            Color.Transparent,
            Color.White
        )
        ArcadeButtonStyle.RED -> Triple(
            Brush.verticalGradient(listOf(Color(0xFFD9163B), Color(0xFFA60C2A))),
            Color.Transparent,
            Color.White
        )
        ArcadeButtonStyle.SLATE -> Triple(
            Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent)),
            HairlineDark,
            Color.White
        )
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 550f),
        label = "ps_btn_press"
    )

    val pillShape = RoundedCornerShape(9999.dp)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
            }
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .alpha(if (enabled) 1f else 0.40f)
            .clip(pillShape)
            .background(if (enabled) bgBrush else Brush.verticalGradient(listOf(Charcoal, SurfaceDarkCard)))
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(1.2.dp, borderColor, pillShape)
                } else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = Color.White.copy(alpha = 0.35f)),
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = 24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize,
                    letterSpacing = 0.45.sp
                ),
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * PlayStation Dark Card (per DESIGN.md product-card-dark / game-tile):
 * - Container: Surface Dark Card (#181818)
 * - Border: 1px hairline dark divider (rgba(229,229,229,0.2))
 * - Rounded: 8px (rounded.md)
 * - Depth: Flat resting canvas (no drop shadow; depth built from surface contrast)
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 8.dp, // rounded.md per DESIGN.md
    borderColor: Color = HairlineDark,
    backgroundBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            SurfaceDarkCard,
            SurfaceDarkCard
        )
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .clip(shape)
            .background(backgroundBrush)
            .border(1.dp, borderColor, shape),
        content = content
    )
}

/**
 * PlayStation Plus Tier Callout Banner (per DESIGN.md ps-plus-banner):
 * Background #121314 with a horizontal gold gradient accent bar (#ffce21 -> #ee8e00) across the top,
 * and 8px border radius (rounded.md).
 */
@Composable
fun GoldMetallicBanner(
    text: String,
    modifier: Modifier = Modifier,
    isPurple: Boolean = false
) {
    val shape = RoundedCornerShape(8.dp)
    val accentBrush = if (isPurple) {
        Brush.horizontalGradient(listOf(Color(0xFF9E4BFF), PsBlue))
    } else {
        Brush.horizontalGradient(listOf(PsGoldStart, PsGoldMid, PsGoldEnd))
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(SurfaceDarkElevated)
            .border(1.dp, HairlineDark, shape)
    ) {
        // Horizontal accent bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .background(accentBrush)
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    letterSpacing = 0.5.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
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
