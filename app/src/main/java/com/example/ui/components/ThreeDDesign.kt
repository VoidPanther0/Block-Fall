package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * God-tier 3D design primitives.
 *
 * Everything here is plain Compose drawing — no 3D engine, no extra dependency, no extra layout
 * nodes. The extrusion is painted in [Modifier.drawBehind] so it stays *outside* the clipped
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
        drawShape(
            shape = shape,
            color = glow.copy(alpha = 0.16f),
            topLeft = Offset(0f, px * 0.7f),
            size = Size(size.width, size.height + px * 0.7f)
        )
    }

    // Extruded side walls: stacked silhouettes stepping down = a beveled 3D edge.
    val steps = 7
    for (i in steps downTo 1) {
        val t = i.toFloat() / steps
        drawShape(
            shape = shape,
            color = base.copy(alpha = (0.34f + 0.58f * t).coerceIn(0f, 1f)),
            topLeft = Offset(0f, px * t),
            size = size
        )
    }

    // Crisp outer edge light where the top face meets the side wall.
    if (px > 0f) {
        drawShape(
            shape = shape,
            color = rimLight,
            style = Stroke(width = 1.4f)
        )
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
