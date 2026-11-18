package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.RedBtnBottom
import com.example.ui.theme.RedBtnLip
import com.example.ui.theme.RedBtnTop
import com.example.ui.theme.RoyalBlueBottom
import com.example.ui.theme.RoyalBlueMid
import com.example.ui.theme.RoyalBlueTop
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated

enum class ArcadeButtonStyle {
    GOLD,
    BLUE,
    GREEN,
    RED
}

/**
 * Rich royal-cobalt backdrop with subtle floating block silhouettes & stars.
 */
@Composable
fun RoyalSkyBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(RoyalBlueTop, RoyalBlueMid, RoyalBlueBottom)
            )
        )

        // Subtle floating block silhouettes matching the splash/menu background
        val silhouettes = listOf(
            Triple(Offset(size.width * 0.14f, size.height * 0.14f), 42f, -14f),
            Triple(Offset(size.width * 0.85f, size.height * 0.16f), 36f, 18f),
            Triple(Offset(size.width * 0.10f, size.height * 0.62f), 46f, 12f),
            Triple(Offset(size.width * 0.88f, size.height * 0.56f), 44f, -16f),
            Triple(Offset(size.width * 0.24f, size.height * 0.84f), 34f, -10f),
            Triple(Offset(size.width * 0.78f, size.height * 0.82f), 38f, 15f)
        )
        silhouettes.forEach { (center, side, angle) ->
            rotate(degrees = angle, pivot = center) {
                drawRoundRect(
                    color = Color.White.copy(alpha = 0.045f),
                    topLeft = Offset(center.x - side / 2f, center.y - side / 2f),
                    size = Size(side, side),
                    cornerRadius = CornerRadius(8f, 8f)
                )
            }
        }

        // Subtle distant rolling horizon wave at bottom
        val hillPath = Path().apply {
            moveTo(0f, size.height * 0.88f)
            cubicTo(
                size.width * 0.28f, size.height * 0.82f,
                size.width * 0.65f, size.height * 0.92f,
                size.width, size.height * 0.85f
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(hillPath, color = Color(0xFF0A1944).copy(alpha = 0.45f))
    }
}

/**
 * Scenic daytime sky, soft clouds, blue mountains, and lush green hills for Home Screen.
 */
@Composable
fun ScenicHillsBackdrop(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        // Sky gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color(0xFF1A56C4),
                    Color(0xFF2D86EB),
                    Color(0xFF66BEFF),
                    Color(0xFF399F45)
                )
            )
        )

        // Fluffy white clouds
        val cloudColor = Color.White.copy(alpha = 0.22f)
        val cloudPositions = listOf(
            Triple(0.18f, 0.20f, 58f),
            Triple(0.32f, 0.18f, 44f),
            Triple(0.74f, 0.16f, 52f),
            Triple(0.86f, 0.19f, 40f),
            Triple(0.54f, 0.28f, 48f)
        )
        cloudPositions.forEach { (xf, yf, r) ->
            drawCircle(cloudColor, radius = r, center = Offset(size.width * xf, size.height * yf))
        }

        // Distant blue mountain peaks
        val mountainBack = Path().apply {
            moveTo(0f, size.height * 0.62f)
            lineTo(size.width * 0.22f, size.height * 0.46f)
            lineTo(size.width * 0.48f, size.height * 0.58f)
            lineTo(size.width * 0.78f, size.height * 0.44f)
            lineTo(size.width, size.height * 0.56f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(mountainBack, color = Color(0xFF3C8AE0).copy(alpha = 0.55f))

        // Mid-ground rolling green hill
        val hillMid = Path().apply {
            moveTo(0f, size.height * 0.72f)
            cubicTo(
                size.width * 0.30f, size.height * 0.64f,
                size.width * 0.68f, size.height * 0.76f,
                size.width, size.height * 0.68f
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            hillMid,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF56CC46), Color(0xFF288D33)),
                startY = size.height * 0.64f,
                endY = size.height
            )
        )

        // Foreground darker emerald hill for bottom menu contrast
        val hillFront = Path().apply {
            moveTo(0f, size.height * 0.80f)
            cubicTo(
                size.width * 0.40f, size.height * 0.86f,
                size.width * 0.72f, size.height * 0.75f,
                size.width, size.height * 0.81f
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            hillFront,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF288735), Color(0xFF144E26)),
                startY = size.height * 0.75f,
                endY = size.height
            )
        )
    }
}

/**
 * Scenic background for Select Mode Classic card (bright sky + green hills).
 */
@Composable
fun ClassicModeScenicBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF2E9DF7), Color(0xFF72D2FF))
            )
        )
        drawCircle(Color.White.copy(alpha = 0.25f), size.height * 0.28f, Offset(size.width * 0.75f, size.height * 0.24f))
        drawCircle(Color.White.copy(alpha = 0.20f), size.height * 0.22f, Offset(size.width * 0.84f, size.height * 0.28f))

        val hills = Path().apply {
            moveTo(0f, size.height * 0.74f)
            cubicTo(
                size.width * 0.35f, size.height * 0.56f,
                size.width * 0.65f, size.height * 0.84f,
                size.width, size.height * 0.66f
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            hills,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF53CE44), Color(0xFF23852E)),
                startY = size.height * 0.56f,
                endY = size.height
            )
        )
    }
}

/**
 * Scenic background for Select Mode Challenge card (pink-violet twilight sky + mountains).
 */
@Composable
fun ChallengeModeScenicBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE07BD6), Color(0xFF7E5AE0), Color(0xFF424AB5))
            )
        )
        val mountain = Path().apply {
            moveTo(0f, size.height * 0.74f)
            lineTo(size.width * 0.32f, size.height * 0.50f)
            lineTo(size.width * 0.58f, size.height * 0.68f)
            lineTo(size.width * 0.82f, size.height * 0.48f)
            lineTo(size.width, size.height * 0.64f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(mountain, color = Color(0xFF513B9E).copy(alpha = 0.72f))

        val foreground = Path().apply {
            moveTo(0f, size.height * 0.78f)
            cubicTo(
                size.width * 0.35f, size.height * 0.68f,
                size.width * 0.70f, size.height * 0.86f,
                size.width, size.height * 0.72f
            )
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            foreground,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF47B349), Color(0xFF1F772E)),
                startY = size.height * 0.68f,
                endY = size.height
            )
        )

        // Small red challenge flag on right hill matching the mockup
        val poleX = size.width * 0.87f
        val poleBaseY = size.height * 0.77f
        val poleTopY = size.height * 0.56f
        drawLine(
            color = Color(0xFF3E2723),
            start = Offset(poleX, poleBaseY),
            end = Offset(poleX, poleTopY),
            strokeWidth = 4f
        )
        val flagPath = Path().apply {
            moveTo(poleX, poleTopY)
            lineTo(poleX + size.width * 0.06f, poleTopY + size.height * 0.055f)
            lineTo(poleX, poleTopY + size.height * 0.11f)
            close()
        }
        drawPath(flagPath, color = Color(0xFFFF424E))
    }
}

/**
 * 2x2 Tilted glossy jewel block emblem matching the Splash Screen logo.
 */
@Composable
fun ArcadeBlockEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 88.dp
) {
    Canvas(modifier = modifier.size(size)) {
        rotate(degrees = -7f) {
            val cell = this.size.width / 2f
            val gap = cell * 0.10f
            val tile = cell - gap
            drawBevelBlock(gap / 2f, gap / 2f, tile, BlockColor.CORAL)
            drawBevelBlock(cell + gap / 2f, gap / 2f, tile, BlockColor.AMBER)
            drawBevelBlock(gap / 2f, cell + gap / 2f, tile, BlockColor.CYAN)
            drawBevelBlock(cell + gap / 2f, cell + gap / 2f, tile, BlockColor.EMERALD)
        }
    }
}

/**
 * Stacked arcade brand title ("Block" / "Falls") with bold 3D drop shadow.
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
            color = Color(0xFF0A1845),
            offset = Offset(0f, 6f),
            blurRadius = 6f
        )
        Text(
            text = "Block",
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                lineHeight = fontSize * 0.96f,
                letterSpacing = (-0.5).sp,
                shadow = shadowStyle
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Falls",
            style = MaterialTheme.typography.displaySmall.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = fontSize,
                lineHeight = fontSize * 0.96f,
                letterSpacing = (-0.5).sp,
                shadow = shadowStyle
            ),
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Tactile 3D glossy rounded arcade button with top specular sheen & 3D bottom lip.
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
    cornerRadius: Dp = 27.dp,
    fontSize: TextUnit = 17.sp,
    testTag: String? = null
) {
    val (topColor, bottomColor, lipColor, contentColor) = when (style) {
        ArcadeButtonStyle.GOLD -> Quad(GoldBtnTop, GoldBtnBottom, GoldBtnLip, GoldBtnText)
        ArcadeButtonStyle.BLUE -> Quad(BlueBtnTop, BlueBtnBottom, BlueBtnLip, Color.White)
        ArcadeButtonStyle.GREEN -> Quad(GreenBtnTop, GreenBtnBottom, GreenBtnLip, Color.White)
        ArcadeButtonStyle.RED -> Quad(RedBtnTop, RedBtnBottom, RedBtnLip, Color.White)
    }

    val shape = RoundedCornerShape(cornerRadius)
    val outerModifier = modifier
        .height(height)
        .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
        .alpha(if (enabled) 1f else 0.48f)
        .shadow(elevation = 6.dp, shape = shape, clip = false)
        .clip(shape)
        .background(lipColor)
        .clickable(enabled = enabled, onClick = onClick)

    Box(modifier = outerModifier) {
        // Main glossy face slightly raised above bottom 3D lip
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 4.dp)
                .clip(shape)
                .background(Brush.verticalGradient(listOf(topColor, bottomColor)))
                .border(1.dp, Color.White.copy(alpha = 0.42f), shape)
        ) {
            // Subtle top gloss highlight band
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(height * 0.38f)
                    .padding(horizontal = 10.dp, vertical = 2.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.White.copy(alpha = 0.34f), Color.Transparent)
                        )
                    )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 18.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = fontSize
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
 * Translucent dark-navy frosted glass container matching the mockup cards/dialogs.
 */
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 20.dp,
    borderColor: Color = GlassBorder.copy(alpha = 0.55f),
    backgroundBrush: Brush = Brush.verticalGradient(
        colors = listOf(
            SurfaceCardElevated.copy(alpha = 0.88f),
            SurfaceCard.copy(alpha = 0.92f)
        )
    ),
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(cornerRadius)
    Column(
        modifier = modifier
            .shadow(8.dp, shape, clip = false)
            .clip(shape)
            .background(backgroundBrush)
            .border(1.25.dp, borderColor, shape),
        content = content
    )
}

/**
 * Circular floating badge (like the Blue Pause badge or Sad-face Game Over badge).
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
            .shadow(8.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(backgroundBrush)
            .border(2.5.dp, Color.White.copy(alpha = 0.45f), CircleShape),
        content = content
    )
}

private data class Quad<A, B, C, D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D
)
