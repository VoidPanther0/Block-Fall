package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.annotation.DrawableRes
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
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
 * Themed canvas backdrop responding to the user's selected [BoardColorPalette] theme:
 * Supports Royal Cobalt, Midnight Nebula, Royal Amethyst, Emerald Aurora, Sunset Horizon, and Obsidian Stealth.
 */
@Composable
fun RoyalSkyBackdrop(
    modifier: Modifier = Modifier,
    palette: com.example.game.BoardColorPalette = LocalBoardPalette.current
) {
    // GPU-only drift: the gradient layer is translated on the compositor, never recomposed.
    val drift by rememberInfiniteTransition(label = "skyDrift").animateFloat(
        initialValue = -16f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            tween(8000, easing = LinearEasing),
            RepeatMode.Reverse
        ),
        label = "skyDriftValue"
    )
    // Deterministic star field generated once — zero allocation in the draw loop.
    val stars = remember {
        var seed = 1337L
        List(56) {
            seed = (seed * 16807L) % 2147483647L
            val sx = seed / 2147483647f
            seed = (seed * 16807L) % 2147483647L
            val sy = seed / 2147483647f
            seed = (seed * 16807L) % 2147483647L
            Triple(sx, sy, 0.6f + (seed / 2147483647f) * 1.6f)
        }
    }

    val (bgGradient, driftColor, gridLineColor, glowPrimary, glowSecondary) = when (palette) {
        com.example.game.BoardColorPalette.ROYAL -> listOf(
            listOf(Color(0xFF04091A), Color(0xFF0A1433), Color(0xFF132559)),
            listOf(Color(0xFF1D4ED8).copy(alpha = 0.30f)),
            listOf(Color(0xFF3B82F6).copy(alpha = 0.18f)),
            listOf(Color(0xFF2563EB)),
            listOf(Color(0xFF06B6D4))
        )
        com.example.game.BoardColorPalette.MIDNIGHT -> listOf(
            listOf(Color(0xFF030510), Color(0xFF080D24), Color(0xFF111B47)),
            listOf(Color(0xFF0EA5E9).copy(alpha = 0.25f)),
            listOf(Color(0xFF38BDF8).copy(alpha = 0.16f)),
            listOf(Color(0xFF00E5FF)),
            listOf(Color(0xFF6366F1))
        )
        com.example.game.BoardColorPalette.AMETHYST -> listOf(
            listOf(Color(0xFF0C051C), Color(0xFF190A38), Color(0xFF2B125E)),
            listOf(Color(0xFF9333EA).copy(alpha = 0.34f)),
            listOf(Color(0xFFC084FC).copy(alpha = 0.18f)),
            listOf(Color(0xFFA855F7)),
            listOf(Color(0xFFEC4899))
        )
        com.example.game.BoardColorPalette.EMERALD -> listOf(
            listOf(Color(0xFF03120B), Color(0xFF072416), Color(0xFF0E3D27)),
            listOf(Color(0xFF10B981).copy(alpha = 0.30f)),
            listOf(Color(0xFF34D399).copy(alpha = 0.18f)),
            listOf(Color(0xFF10B981)),
            listOf(Color(0xFF06B6D4))
        )
        com.example.game.BoardColorPalette.SUNSET -> listOf(
            listOf(Color(0xFF170712), Color(0xFF2A0E20), Color(0xFF42172C)),
            listOf(Color(0xFFF97316).copy(alpha = 0.30f)),
            listOf(Color(0xFFFB923C).copy(alpha = 0.18f)),
            listOf(Color(0xFFF59E0B)),
            listOf(Color(0xFFEC4899))
        )
        com.example.game.BoardColorPalette.OBSIDIAN -> listOf(
            listOf(Color(0xFF050507), Color(0xFF0D0E12), Color(0xFF171921)),
            listOf(Color(0xFF64748B).copy(alpha = 0.18f)),
            listOf(Color(0xFF475569).copy(alpha = 0.20f)),
            listOf(Color(0xFF94A3B8)),
            listOf(Color(0xFFE2E8F0))
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgGradient as List<Color>))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { translationX = drift }
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            (driftColor as List<Color>).first(),
                            Color.Transparent
                        )
                    )
                )
        )
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val lineClr = (gridLineColor as List<Color>).first()
            val glow1 = (glowPrimary as List<Color>).first()
            val glow2 = (glowSecondary as List<Color>).first()

            // Parallax star dust with depth-scaled radius.
            stars.forEach { (sx, sy, radius) ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.10f + radius * 0.14f),
                    radius = radius,
                    center = Offset(sx * w, sy * h * 0.82f)
                )
            }

            // Perspective floor grid: clean arcade stage floor
            for (i in 0..8) {
                val t = i / 8f
                val y = h * (0.72f + 0.28f * t * t)
                drawLine(
                    color = lineClr.copy(alpha = lineClr.alpha * (1f - t)),
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = 1f
                )
            }
            for (i in -4..4) {
                val x = w / 2f + i * w * 0.13f
                drawLine(
                    color = lineClr.copy(alpha = lineClr.alpha * 0.7f),
                    start = Offset(w / 2f + i * w * 0.028f, h * 0.72f),
                    end = Offset(x, h),
                    strokeWidth = 1f
                )
            }

            // Cinematic thematic ambient glow at top-right
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glow1.copy(alpha = 0.24f),
                        Color.Transparent
                    ),
                    center = Offset(w * 0.88f, h * 0.16f),
                    radius = w * 0.72f
                ),
                radius = w * 0.72f,
                center = Offset(w * 0.88f, h * 0.16f)
            )

            // Secondary complementary glow at bottom-left
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        glow2.copy(alpha = 0.08f),
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
 * Editorial BLOCK FALL title logo:
 * Clean white display tier paired with signature PlayStation Blue.
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
                letterSpacing = 1.2.sp,
                shadow = Shadow(color = Color(0xFF00285C), offset = Offset(0f, 4f), blurRadius = 0f)
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
                letterSpacing = 1.6.sp,
                shadow = Shadow(color = Color(0xFF001A3D), offset = Offset(0f, 4f), blurRadius = 0f)
            ),
            color = PsBlue,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Human-crafted Tactile Arcade Button:
 * - Form: Balanced rounded rectangle (16dp radius), avoiding artificial generic stretched capsules.
 * - Tactile depth: Real 4dp-5dp physical extruded bottom lip matching the button's deeper tone.
 * - Press physics: Compresses into its base (translationY = 3.5dp) on press.
 * - Highlight: Subtle, crisp 1.5dp inner top highlight without cheap plastic diagonal streaks.
 * - Typography: Bold Outfit typography with clean contrast and subtle text depth.
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
    cornerRadius: Dp = 16.dp,
    fontSize: TextUnit = 16.sp,
    horizontalPadding: Dp = 16.dp,
    testTag: String? = null
) {
    val (bgBrush, borderColor, contentColor, sideBase) = when (style) {
        ArcadeButtonStyle.BLUE -> Quadruple(
            Brush.verticalGradient(listOf(Color(0xFF2575FC), Color(0xFF1A56DB))),
            Color(0xFF60A5FA).copy(alpha = 0.35f),
            Color.White,
            Color(0xFF0F3B9E)
        )
        ArcadeButtonStyle.COMMERCE -> Quadruple(
            Brush.verticalGradient(listOf(Color(0xFFFF5722), Color(0xFFD84315))),
            Color(0xFFFFAB91).copy(alpha = 0.35f),
            Color.White,
            Color(0xFF8C2607)
        )
        ArcadeButtonStyle.GOLD -> Quadruple(
            Brush.verticalGradient(listOf(Color(0xFFFFC107), Color(0xFFFFA000))),
            Color(0xFFFFE082).copy(alpha = 0.50f),
            Color(0xFF1B1300),
            Color(0xFF9E5C00)
        )
        ArcadeButtonStyle.GOLD_BLUE -> Quadruple(
            Brush.verticalGradient(listOf(Color(0xFF2575FC), Color(0xFF1A56DB))),
            Color(0xFFFFC107),
            Color.White,
            Color(0xFF0F3B9E)
        )
        ArcadeButtonStyle.GOLD_PURPLE -> Quadruple(
            Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))),
            Color(0xFFFFC107),
            Color.White,
            Color(0xFF4C1D95)
        )
        ArcadeButtonStyle.PURPLE -> Quadruple(
            Brush.verticalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))),
            Color(0xFFC4B5FD).copy(alpha = 0.35f),
            Color.White,
            Color(0xFF4C1D95)
        )
        ArcadeButtonStyle.GREEN -> Quadruple(
            Brush.verticalGradient(listOf(Color(0xFF10B981), Color(0xFF059669))),
            Color(0xFFA7F3D0).copy(alpha = 0.35f),
            Color.White,
            Color(0xFF065F46)
        )
        ArcadeButtonStyle.RED -> Quadruple(
            Brush.verticalGradient(listOf(Color(0xFFEF4444), Color(0xFFDC2626))),
            Color(0xFFFECACA).copy(alpha = 0.35f),
            Color.White,
            Color(0xFF991B1B)
        )
        ArcadeButtonStyle.SLATE -> Quadruple(
            Brush.verticalGradient(listOf(Color(0xFF242938), Color(0xFF181C26))),
            Color(0xFF475569).copy(alpha = 0.55f),
            Color(0xFFE2E8F0),
            Color(0xFF0F131C)
        )
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.985f else 1f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 550f),
        label = "ps_btn_press"
    )
    val pressDrop by animateFloatAsState(
        targetValue = if (isPressed && enabled) 3.5f else 0f,
        animationSpec = spring(dampingRatio = 0.70f, stiffness = 520f),
        label = "ps_btn_depth"
    )

    val buttonShape = RoundedCornerShape(cornerRadius)
    val depth = if (style == ArcadeButtonStyle.SLATE) 3.5.dp else 5.dp

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(height)
            .graphicsLayer {
                scaleX = pressScale
                scaleY = pressScale
                translationY = pressDrop
            }
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .alpha(if (enabled) 1f else 0.40f)
            .threeDFrame(shape = buttonShape, depth = depth, base = sideBase)
            .clip(buttonShape)
            .background(if (enabled) bgBrush else Brush.verticalGradient(listOf(Charcoal, SurfaceDarkCard)))
            .then(
                if (borderColor != Color.Transparent) {
                    Modifier.border(1.dp, borderColor, buttonShape)
                } else Modifier
            )
            .clickable(
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = Color.White.copy(alpha = 0.35f)),
                enabled = enabled,
                onClick = onClick
            )
            .padding(horizontal = horizontalPadding)
    ) {
        // Subtle, elegant top edge highlight rim for physical bevel reflection
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.5.dp)
                .align(Alignment.TopCenter)
                .padding(horizontal = 4.dp)
                .background(Color.White.copy(alpha = 0.28f), CircleShape)
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
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
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = fontSize,
                    letterSpacing = 0.5.sp,
                    shadow = if (contentColor == Color.White) {
                        Shadow(color = Color.Black.copy(alpha = 0.45f), offset = Offset(0f, 2f), blurRadius = 2f)
                    } else null
                ),
                color = contentColor,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class Quadruple<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)

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
    cornerRadius: Dp = 18.dp,
    borderColor: Color = HairlineDark,
    backgroundBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF1D2233).copy(alpha = 0.88f),
            Color(0xFF131724).copy(alpha = 0.94f)
        )
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .shadow(6.dp, shape, ambientColor = Color.Black.copy(alpha = 0.35f), spotColor = Color.Black.copy(alpha = 0.35f))
            .clip(shape)
            .background(backgroundBrush)
            .border(1.dp, borderColor, shape),
        content = content
    )
}

/**
 * Clean editorial banner header for seasonal/reward screens.
 */
@Composable
fun GoldMetallicBanner(
    text: String,
    modifier: Modifier = Modifier,
    isPurple: Boolean = false
) {
    val shape = RoundedCornerShape(14.dp)
    val accentBrush = if (isPurple) {
        Brush.horizontalGradient(listOf(Color(0xFF8B5CF6), Color(0xFF38BDF8)))
    } else {
        Brush.horizontalGradient(listOf(PsGoldStart, PsGoldMid, PsGoldEnd))
    }

    Column(
        modifier = modifier
            .clip(shape)
            .background(Color(0xFF151A2A).copy(alpha = 0.92f))
            .border(1.dp, GlassBorder.copy(alpha = 0.55f), shape)
    ) {
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
                .padding(horizontal = 16.dp, vertical = 9.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.2.sp
                ),
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * Glossy 3D Gold Coin powered by pre-bundled Gemini 3D PNG asset.
 */
@Composable
fun CrystalCoinBadge(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    GameAssetIcon(
        resId = R.drawable.img_coin_single,
        contentDescription = null,
        size = size,
        modifier = modifier
    )
}

/**
 * Faceted 3D Cyan Diamond powered by pre-bundled Gemini 3D PNG asset.
 */
@Composable
fun CrystalDiamondBadge(
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    GameAssetIcon(
        resId = R.drawable.img_gem_single,
        contentDescription = null,
        size = size,
        modifier = modifier
    )
}

/**
 * 3D Trophy / Medal badge powered by Gemini-generated PNG assets with locked padlock overlay.
 */
@Composable
fun CrystalMedalBadge(
    tier: Int,
    unlocked: Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 54.dp,
    @DrawableRes customResId: Int? = null
) {
    val resId = customResId ?: when {
        tier >= 3 -> R.drawable.img_medal_trophy_gold
        tier == 2 -> R.drawable.img_medal_lightning
        else -> R.drawable.img_medal_ribbon_star
    }
    val rimColor = when {
        !unlocked -> GlassBorder.copy(alpha = 0.35f)
        tier >= 3 -> AccentGoldBright.copy(alpha = 0.7f)
        tier == 2 -> AccentCyan.copy(alpha = 0.6f)
        else -> Color(0xFFFFB74D).copy(alpha = 0.55f)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF0A1128))
            .border(1.2.dp, rimColor, CircleShape)
    ) {
        GameAssetIcon(
            resId = resId,
            contentDescription = null,
            size = size * 0.86f,
            alpha = if (unlocked) 1f else 0.36f
        )
        if (!unlocked) {
            GameAssetIcon(
                resId = R.drawable.img_status_padlock,
                contentDescription = null,
                size = size * 0.38f,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(2.dp)
            )
        }
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
            .threeDFrame(shape = CircleShape, depth = 5.dp, base = Color(0xFF03060F))
            .shadow(8.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(backgroundBrush)
            .border(2.5.dp, AccentGoldBright, CircleShape),
        content = content
    )
}
