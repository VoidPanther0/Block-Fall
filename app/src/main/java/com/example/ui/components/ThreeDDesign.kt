package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * God-tier 3D design primitives.
 *
 * Everything here is plain Compose drawing — no 3D engine, no extra dependency, no extra layout
 * nodes. The extrusion is painted in [Modifier.drawBehind] so it stays outside the clipped
 * surface and therefore reads as physical thickness instead of a flat shadow.
 *
 * Perf note: the draw lambdas allocate nothing (no paths/brushes created per frame), so they are
 * cheap enough for the gameplay board and for buttons that animate on press.
 */

/** Paints a solid, layered slab underneath a surface to give it real thickness. */
fun Modifier.threeDFrame(
    shape: Shape,
    depth: Dp = 6.dp,
    base: Color = Color(0xFF03040A),
    glow: Color = Color.Transparent,
    rimLight: Color = Color(0x2EFFFFFF)
): Modifier = this.drawBehind {
    val px = depth.toPx().coerceAtLeast(0f)

    // Ambient bloom beneath the object (optional).
    if (glow != Color.Transparent && px > 0f) {
        drawSurfaceShape(
            shape = shape,
            color = glow.copy(alpha = 0.16f),
            topLeft = Offset(0f, px * 0.7f)
        )
    }

    // Extruded side walls: stacked silhouettes stepping down = a beveled 3D edge.
    val steps = 7
    for (i in steps downTo 1) {
        val t = i.toFloat() / steps
        drawSurfaceShape(
            shape = shape,
            color = base.copy(alpha = (0.34f + 0.58f * t).coerceIn(0f, 1f)),
            topLeft = Offset(0f, px * t)
        )
    }

    // Crisp outer edge light where the top face meets the side wall.
    if (px > 0f) {
        drawSurfaceShape(shape = shape, color = rimLight, style = Stroke(width = 1.4f))
    }
}

/** Draws the two shape kinds used by the design system without allocating a Path per frame. */
private fun DrawScope.drawSurfaceShape(
    shape: Shape,
    color: Color,
    topLeft: Offset = Offset.Zero,
    style: DrawStyle = Fill,
    alpha: Float = 1f
) {
    when (shape) {
        CircleShape -> drawCircle(
            color = color,
            radius = minOf(size.width, size.height) / 2f,
            center = Offset(topLeft.x + size.width / 2f, topLeft.y + size.height / 2f),
            alpha = alpha,
            style = style
        )
        is RoundedCornerShape -> {
            val radius = shape.topStart.toPx(Size(size.width, size.height), this)
            drawRoundRect(
                color = color,
                topLeft = topLeft,
                size = size,
                cornerRadius = CornerRadius(radius, radius),
                alpha = alpha,
                style = style
            )
        }
        else -> drawRect(color = color, topLeft = topLeft, size = size, alpha = alpha, style = style)
    }
}

/** A 3D-look surface used by cards, packages, dialogs and stat panels. */
@Composable
fun ThreeDSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(16.dp),
    depth: Dp = 8.dp,
    glow: Color = Color.Transparent,
    borderColor: Color = Color(0x33E5E5E5),
    backgroundBrush: Brush = Brush.verticalGradient(
        listOf(Color(0xFF1B1D22), Color(0xFF121314))
    ),
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .threeDFrame(shape = shape, depth = depth, glow = glow)
            .clip(shape)
            .background(backgroundBrush)
            .border(1.dp, borderColor, shape),
        content = content
    )
}
