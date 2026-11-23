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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.drawscope.rotate
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
 * Clean, bright daylight canvas backdrop that adapts subtly to the active [BoardColorPalette].
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
 * Crisp classic puzzle title ("BLOCK" / "FALL").
 */
@Composable
fun ArcadeTitleLogo(
    fontSize: TextUnit = 40.sp,
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
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                lineHeight = fontSize * 0.96f,
                letterSpacing = 1.2.sp
            ),
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "FALL",
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                lineHeight = fontSize * 0.96f,
                letterSpacing = 2.0.sp
            ),
            color = AccentCyan,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Tactile classic puzzle button with crisp bottom lip and subtle elevation.
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
    cornerRadius: Dp = 12.dp,
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

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressScale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.975f else 1f,
        animationSpec = spring(dampingRatio = 0.78f, stiffness = 580f),
        label = "arcade_btn_press"
    )

    val shape = RoundedCornerShape(cornerRadius)
    val rippleColor = if (style == ArcadeButtonStyle.SLATE) TextPrimary else Color.White
    val borderStrokeColor = if (style == ArcadeButtonStyle.SLATE) GlassBorder else Color.White.copy(alpha = 0.16f)

    val outerModifier = modifier
        .height(height)
        .graphicsLayer {
            scaleX = pressScale
            scaleY = pressScale
        }
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        .alpha(if (enabled) 1f else 0.45f)
        .shadow(elevation = 2.dp, shape = shape, clip = false)
        .clip(shape)
        .background(lipColor)
        .clickable(
            interactionSource = interactionSource,
            indication = androidx.compose.material3.ripple(color = rippleColor),
            enabled = enabled,
            onClick = onClick
        )

    Box(modifier = outerModifier) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 3.dp)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
                .border(1.dp, borderStrokeColor, shape)
        ) {
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
                        fontWeight = FontWeight.Bold,
                        fontSize = fontSize,
                        letterSpacing = 0.2.sp
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
 * Clean white container card with crisp 1dp border and subtle shadow.
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    borderColor: Color = GlassBorder,
    backgroundBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            SurfaceCard,
            SurfaceCardElevated
        )
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .shadow(3.dp, shape, clip = false)
            .clip(shape)
            .background(backgroundBrush)
            .border(1.dp, borderColor, shape),
        content = content
    )
}

/**
 * Clean circular badge for Pause, Level Complete, and Game Over modals.
 */
@Composable
fun FloatingModalBadge(
    backgroundBrush: Brush,
    modifier: Modifier = Modifier,
    size: Dp = 68.dp,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .shadow(4.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(backgroundBrush)
            .border(2.dp, Color.White, CircleShape),
        content = content
    )
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
