package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnLip
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.GlassBorder
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

enum class ArcadeButtonStyle {
    GOLD,
    BLUE,
    GREEN,
    RED,
    SLATE
}

/**
 * Luminous Prism Atelier backdrop that reacts dynamically to the active [BoardColorPalette].
 */
@Composable
fun RoyalSkyBackdrop(modifier: Modifier = Modifier) {
    val palette = LocalBoardPalette.current
    val themeColors = palette.toBoardColors()

    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    themeColors.studioTop,
                    themeColors.studioMid,
                    themeColors.studioBottom
                )
            )
        )

        // Soft upper-left ambient light bloom
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    themeColors.studioGlowPrimary.copy(alpha = 0.24f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.18f, size.height * 0.12f),
                radius = size.maxDimension * 0.55f
            ),
            radius = size.maxDimension * 0.55f,
            center = Offset(size.width * 0.18f, size.height * 0.12f)
        )

        // Secondary lower-right cyan/accent bloom
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    themeColors.studioGlowSecondary.copy(alpha = 0.14f),
                    Color.Transparent
                ),
                center = Offset(size.width * 0.86f, size.height * 0.82f),
                radius = size.maxDimension * 0.48f
            ),
            radius = size.maxDimension * 0.48f,
            center = Offset(size.width * 0.86f, size.height * 0.82f)
        )

        // Subtle floating prism tile watermarks
        val silhouettes = listOf(
            Triple(Offset(size.width * 0.12f, size.height * 0.18f), 42f, -14f),
            Triple(Offset(size.width * 0.88f, size.height * 0.15f), 34f, 18f),
            Triple(Offset(size.width * 0.08f, size.height * 0.68f), 46f, 12f),
            Triple(Offset(size.width * 0.90f, size.height * 0.60f), 40f, -16f),
            Triple(Offset(size.width * 0.24f, size.height * 0.88f), 32f, -10f),
            Triple(Offset(size.width * 0.78f, size.height * 0.86f), 38f, 15f)
        )
        silhouettes.forEach { (center, side, angle) ->
            rotate(degrees = angle, pivot = center) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.032f),
                    topLeft = Offset(center.x - side / 2f, center.y - side / 2f),
                    size = Size(side, side),
                    cornerRadius = CornerRadius(10f, 10f)
                )
            }
        }
    }
}

/**
 * 2x2 Tilted jewel block emblem rendered in the user's selected BlockStyle & BoardPalette.
 */
@Composable
fun ArcadeBlockEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 88.dp
) {
    val blockStyle = LocalBlockStyle.current
    val boardPalette = LocalBoardPalette.current
    Canvas(modifier = modifier.size(size)) {
        rotate(degrees = -7f) {
            val cell = this.size.width / 2f
            val gap = cell * 0.11f
            val tile = cell - gap
            drawBevelBlock(gap / 2f, gap / 2f, tile, BlockColor.CORAL, blockStyle = blockStyle, boardPalette = boardPalette)
            drawBevelBlock(cell + gap / 2f, gap / 2f, tile, BlockColor.AMBER, blockStyle = blockStyle, boardPalette = boardPalette)
            drawBevelBlock(gap / 2f, cell + gap / 2f, tile, BlockColor.CYAN, blockStyle = blockStyle, boardPalette = boardPalette)
            drawBevelBlock(cell + gap / 2f, cell + gap / 2f, tile, BlockColor.EMERALD, blockStyle = blockStyle, boardPalette = boardPalette)
        }
    }
}

/**
 * Stacked studio brand title ("Block" / "Falls") with crisp crispness & accent glow.
 */
@Composable
fun ArcadeTitleLogo(
    fontSize: TextUnit = 42.sp,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        val shadowStyle = Shadow(
            color = Color(0xFF04091A),
            offset = Offset(0f, 6f),
            blurRadius = 10f
        )
        Text(
            text = "BLOCK",
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                lineHeight = fontSize * 0.96f,
                letterSpacing = 1.5.sp,
                shadow = shadowStyle
            ),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "FALLS",
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                lineHeight = fontSize * 0.96f,
                letterSpacing = 2.5.sp,
                shadow = shadowStyle
            ),
            color = AccentCyan,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Tactile Neo-Glass 3D rounded button with top rim sheen and crisp bottom depth lip.
 */
@Composable
fun GlossyArcadeButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: ArcadeButtonStyle = ArcadeButtonStyle.BLUE,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    height: Dp = 54.dp,
    cornerRadius: Dp = 18.dp,
    fontSize: TextUnit = 16.sp,
    testTag: String? = null
) {
    val (topColor, bottomColor, lipColor, contentColor) = when (style) {
        ArcadeButtonStyle.GOLD -> Quad(GoldBtnTop, GoldBtnBottom, GoldBtnLip, GoldBtnText)
        ArcadeButtonStyle.BLUE -> Quad(BlueBtnTop, BlueBtnBottom, BlueBtnLip, Color.White)
        ArcadeButtonStyle.GREEN -> Quad(GreenBtnTop, GreenBtnBottom, GreenBtnLip, Color.White)
        ArcadeButtonStyle.RED -> Quad(RedBtnTop, RedBtnBottom, RedBtnLip, Color.White)
        ArcadeButtonStyle.SLATE -> Quad(SlateBtnTop, SlateBtnBottom, SlateBtnLip, TextPrimary)
    }

    val shape = RoundedCornerShape(cornerRadius)
    val outerModifier = modifier
        .height(height)
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        .alpha(if (enabled) 1f else 0.44f)
        .shadow(elevation = 6.dp, shape = shape, clip = false)
        .clip(shape)
        .background(lipColor)
        .clickable(enabled = enabled, onClick = onClick)

    Box(modifier = outerModifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 4.dp)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
                .border(1.dp, Color.White.copy(alpha = 0.28f), shape)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(height * 0.34f)
                    .padding(horizontal = 8.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.24f), Color.Transparent)
                        )
                    )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
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
                        letterSpacing = 0.3.sp
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
 * Elevated Neo-Glass container card with crisp top specular hairline and deep shadow.
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = GlassBorder.copy(alpha = 0.48f),
    backgroundBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            SurfaceCardElevated.copy(alpha = 0.92f),
            SurfaceCard.copy(alpha = 0.95f)
        )
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .shadow(10.dp, shape, clip = false)
            .clip(shape)
            .background(backgroundBrush)
            .border(1.dp, borderColor, shape),
        content = content
    )
}

/**
 * Floating circular emblem badge for Pause, Level Complete, and Game Over modals.
 */
@Composable
fun FloatingModalBadge(
    backgroundBrush: Brush,
    modifier: Modifier = Modifier,
    size: Dp = 74.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(10.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(backgroundBrush)
            .border(2.dp, Color.White.copy(alpha = 0.38f), CircleShape),
        content = content
    )
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
