package com.example.ui.components

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.BlockColor
import com.example.ui.theme.toPalette

/**
 * Draws a juicy, glossy 3D bevelled arcade block at the given pixel rect.
 * Supports scaling (for landing squash/bounce or clear shrink) and alpha.
 */
fun DrawScope.drawBevelBlock(
    left: Float,
    top: Float,
    cellSize: Float,
    color: BlockColor,
    alpha: Float = 1f,
    scaleX: Float = 1f,
    scaleY: Float = 1f,
    glowBoost: Float = 0f
) {
    if (cellSize <= 1f || alpha <= 0.01f) return
    val palette = color.toPalette()

    val w = cellSize * scaleX
    val h = cellSize * scaleY
    val x = left + (cellSize - w) / 2f
    val y = top + (cellSize - h) / 2f
    val radius = (minOf(w, h) * 0.22f).coerceAtLeast(2.5f)
    val bevel = (minOf(w, h) * 0.14f).coerceAtLeast(2f)

    // Subtle outer glow when clearing or highlighting
    if (glowBoost > 0f) {
        drawRoundRect(
            color = palette.glow.copy(alpha = (0.42f * glowBoost * alpha).coerceIn(0f, 1f)),
            topLeft = Offset(x - bevel * 0.65f, y - bevel * 0.65f),
            size = Size(w + bevel * 1.3f, h + bevel * 1.3f),
            cornerRadius = CornerRadius(radius * 1.3f, radius * 1.3f)
        )
    }

    // Soft drop shadow underneath
    drawRoundRect(
        color = Color(0xFF071130).copy(alpha = 0.42f * alpha),
        topLeft = Offset(x, y + bevel * 0.28f),
        size = Size(w, h),
        cornerRadius = CornerRadius(radius, radius)
    )

    // Main 3D base body (rich shadow bottom edge)
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                palette.fill.copy(alpha = alpha),
                palette.shadow.copy(alpha = alpha)
            ),
            startY = y,
            endY = y + h
        ),
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(radius, radius)
    )

    // Bright top-left bevel highlight edge
    val highlightPath = Path().apply {
        moveTo(x + radius * 0.55f, y + h - bevel * 0.85f)
        lineTo(x + bevel * 0.25f, y + radius * 0.55f)
        lineTo(x + radius * 0.55f, y + bevel * 0.25f)
        lineTo(x + w - bevel * 0.85f, y + bevel * 0.25f)
        lineTo(x + w - bevel * 1.15f, y + bevel)
        lineTo(x + bevel, y + bevel)
        lineTo(x + bevel, y + h - bevel * 1.15f)
        close()
    }
    drawPath(
        path = highlightPath,
        color = palette.highlight.copy(alpha = 0.65f * alpha)
    )

    // Inner glossy pillow face
    val inset = bevel * 0.92f
    if (w > inset * 2.5f && h > inset * 2.5f) {
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    palette.highlight.copy(alpha = 0.34f * alpha),
                    palette.fill.copy(alpha = 0.96f * alpha),
                    palette.shadow.copy(alpha = 0.24f * alpha)
                ),
                startY = y + inset,
                endY = y + h - inset
            ),
            topLeft = Offset(x + inset, y + inset),
            size = Size(w - inset * 2f, h - inset * 2f),
            cornerRadius = CornerRadius((radius * 0.65f).coerceAtLeast(1.5f))
        )
    }

    // Top specular shine pill
    if (w > bevel * 4f && h > bevel * 4f) {
        drawRoundRect(
            color = Color.White.copy(alpha = 0.36f * alpha),
            topLeft = Offset(x + bevel * 1.2f, y + bevel * 0.85f),
            size = Size((w - bevel * 2.4f).coerceAtLeast(2f), (h * 0.14f).coerceAtLeast(1.5f)),
            cornerRadius = CornerRadius(radius, radius)
        )
    }

    // Crisp rim outline
    drawRoundRect(
        color = palette.highlight.copy(alpha = 0.45f * alpha),
        topLeft = Offset(x + 0.5f, y + 0.5f),
        size = Size((w - 1f).coerceAtLeast(1f), (h - 1f).coerceAtLeast(1f)),
        cornerRadius = CornerRadius(radius, radius),
        style = Stroke(width = 1.25f)
    )
}
