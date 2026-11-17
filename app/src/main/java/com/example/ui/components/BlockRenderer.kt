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
 * Draws a crisp, tactile bevelled jewel block at the given pixel rect.
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
    val radius = (minOf(w, h) * 0.20f).coerceAtLeast(2f)
    val bevel = (minOf(w, h) * 0.14f).coerceAtLeast(2f)

    // Subtle outer glow when clearing or highlighting
    if (glowBoost > 0f) {
        drawRoundRect(
            color = palette.glow.copy(alpha = (0.38f * glowBoost * alpha).coerceIn(0f, 1f)),
            topLeft = Offset(x - bevel * 0.6f, y - bevel * 0.6f),
            size = Size(w + bevel * 1.2f, h + bevel * 1.2f),
            cornerRadius = CornerRadius(radius * 1.3f, radius * 1.3f)
        )
    }

    // Main body gradient
    drawRoundRect(
        brush = Brush.linearGradient(
            colors = listOf(
                palette.fill.copy(alpha = alpha),
                palette.shadow.copy(alpha = alpha)
            ),
            start = Offset(x, y),
            end = Offset(x + w, y + h)
        ),
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(radius, radius)
    )

    // Top-left bevel highlight facet
    val highlightPath = Path().apply {
        moveTo(x + radius * 0.6f, y + h - bevel * 0.8f)
        lineTo(x + bevel * 0.2f, y + radius * 0.6f)
        lineTo(x + radius * 0.6f, y + bevel * 0.2f)
        lineTo(x + w - bevel * 0.8f, y + bevel * 0.2f)
        lineTo(x + w - bevel * 1.2f, y + bevel)
        lineTo(x + bevel, y + bevel)
        lineTo(x + bevel, y + h - bevel * 1.2f)
        close()
    }
    drawPath(
        path = highlightPath,
        color = palette.highlight.copy(alpha = 0.52f * alpha)
    )

    // Inner matte glass face
    val inset = bevel * 0.95f
    if (w > inset * 2.5f && h > inset * 2.5f) {
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    palette.highlight.copy(alpha = 0.22f * alpha),
                    palette.fill.copy(alpha = 0.92f * alpha),
                    palette.shadow.copy(alpha = 0.28f * alpha)
                ),
                startY = y + inset,
                endY = y + h - inset
            ),
            topLeft = Offset(x + inset, y + inset),
            size = Size(w - inset * 2f, h - inset * 2f),
            cornerRadius = CornerRadius((radius * 0.65f).coerceAtLeast(1.5f))
        )
    }

    // Crisp rim outline
    drawRoundRect(
        color = palette.highlight.copy(alpha = 0.35f * alpha),
        topLeft = Offset(x + 0.5f, y + 0.5f),
        size = Size((w - 1f).coerceAtLeast(1f), (h - 1f).coerceAtLeast(1f)),
        cornerRadius = CornerRadius(radius, radius),
        style = Stroke(width = 1.2f)
    )
}
