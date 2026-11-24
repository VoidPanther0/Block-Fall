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
import com.example.game.BlockStyle
import com.example.game.BoardColorPalette
import com.example.ui.theme.BlockPalette
import com.example.ui.theme.toPalette

/**
 * Draws an arcade block using the active [BlockStyle] and [BoardColorPalette].
 * Supports scaling (for landing squash/bounce or clear shrink) and alpha.
 */
private val scratchHighlightPath = ThreadLocal.withInitial { Path() }
private val scratchTopLipPath = ThreadLocal.withInitial { Path() }
private val scratchBottomLipPath = ThreadLocal.withInitial { Path() }

fun DrawScope.drawBevelBlock(
    left: Float,
    top: Float,
    cellSize: Float,
    color: BlockColor,
    alpha: Float = 1f,
    scaleX: Float = 1f,
    scaleY: Float = 1f,
    glowBoost: Float = 0f,
    blockStyle: BlockStyle = BlockStyle.MODERN,
    boardPalette: BoardColorPalette = BoardColorPalette.ROYAL
) {
    if (cellSize <= 1f || alpha <= 0.01f) return
    val palette = color.toPalette(boardPalette)

    val w = cellSize * scaleX
    val h = cellSize * scaleY
    val x = left + (cellSize - w) / 2f
    val y = top + (cellSize - h) / 2f

    when (blockStyle) {
        BlockStyle.MODERN -> drawModernBevelBlock(x, y, w, h, palette, alpha, glowBoost)
        BlockStyle.RETRO -> drawRetroPixelBlock(x, y, w, h, palette, alpha, glowBoost)
        BlockStyle.NEON -> drawNeonSynthBlock(x, y, w, h, palette, alpha, glowBoost)
    }
}

private fun DrawScope.drawModernBevelBlock(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    palette: BlockPalette,
    alpha: Float,
    glowBoost: Float
) {
    val radius = (minOf(w, h) * 0.20f).coerceAtLeast(2.5f)
    val bevel = (minOf(w, h) * 0.14f).coerceAtLeast(2f)

    if (glowBoost > 0f) {
        drawRoundRect(
            color = palette.glow.copy(alpha = (0.42f * glowBoost * alpha).coerceIn(0f, 1f)),
            topLeft = Offset(x - bevel * 0.55f, y - bevel * 0.55f),
            size = Size(w + bevel * 1.1f, h + bevel * 1.1f),
            cornerRadius = CornerRadius(radius * 1.25f, radius * 1.25f)
        )
    }

    // Crisp outer shadow lip for 3D jewel depth
    drawRoundRect(
        color = palette.shadow.copy(alpha = 0.98f * alpha),
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(radius, radius)
    )

    val faceHeight = (h - bevel * 0.50f).coerceAtLeast(1f)
    // Main crystal gem body gradient
    drawRoundRect(
        brush = Brush.verticalGradient(
            colors = listOf(
                palette.highlight.copy(alpha = 0.94f * alpha),
                palette.fill.copy(alpha = alpha),
                palette.shadow.copy(alpha = 0.94f * alpha)
            ),
            startY = y,
            endY = y + faceHeight
        ),
        topLeft = Offset(x, y),
        size = Size(w, faceHeight),
        cornerRadius = CornerRadius(radius, radius)
    )

    // Faceted inner crystal prism table
    val inset = (minOf(w, h) * 0.17f).coerceAtLeast(1.8f)
    if (w > inset * 2.5f && faceHeight > inset * 2.5f) {
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    palette.fill.copy(alpha = 0.96f * alpha),
                    palette.highlight.copy(alpha = 0.42f * alpha),
                    palette.fill.copy(alpha = 0.92f * alpha)
                ),
                startY = y + inset,
                endY = y + faceHeight - inset
            ),
            topLeft = Offset(x + inset, y + inset * 0.85f),
            size = Size(w - inset * 2f, (faceHeight - inset * 1.8f).coerceAtLeast(1f)),
            cornerRadius = CornerRadius(radius * 0.58f, radius * 0.58f)
        )
    }

    // Glossy specular lens across top
    val lensMargin = (minOf(w, h) * 0.12f).coerceAtLeast(1.5f)
    if (w > lensMargin * 3f && faceHeight > lensMargin * 3f) {
        drawRoundRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.45f * alpha),
                    Color.White.copy(alpha = 0.08f * alpha),
                    Color.Transparent
                ),
                startY = y + lensMargin * 0.45f,
                endY = y + faceHeight * 0.52f
            ),
            topLeft = Offset(x + lensMargin, y + lensMargin * 0.45f),
            size = Size(w - lensMargin * 2f, faceHeight * 0.45f),
            cornerRadius = CornerRadius(radius * 0.7f, radius * 0.7f)
        )
    }

    // Top-left crystal bevel edge highlight
    val highlightPath = (scratchHighlightPath.get() ?: Path()).apply {
        rewind()
        moveTo(x + radius * 0.55f, y + faceHeight - bevel * 0.65f)
        lineTo(x + bevel * 0.25f, y + radius * 0.55f)
        lineTo(x + radius * 0.55f, y + bevel * 0.25f)
        lineTo(x + w - bevel * 0.75f, y + bevel * 0.25f)
        lineTo(x + w - bevel * 1.05f, y + bevel * 0.9f)
        lineTo(x + bevel * 0.9f, y + bevel * 0.9f)
        lineTo(x + bevel * 0.9f, y + faceHeight - bevel * 0.9f)
        close()
    }
    drawPath(
        path = highlightPath,
        color = Color.White.copy(alpha = 0.50f * alpha)
    )

    // Crisp jewel rim
    drawRoundRect(
        color = Color.White.copy(alpha = 0.44f * alpha),
        topLeft = Offset(x + 0.5f, y + 0.5f),
        size = Size((w - 1f).coerceAtLeast(1f), (faceHeight - 0.5f).coerceAtLeast(1f)),
        cornerRadius = CornerRadius(radius, radius),
        style = Stroke(width = 1.1f)
    )
}

private fun DrawScope.drawRetroPixelBlock(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    palette: BlockPalette,
    alpha: Float,
    glowBoost: Float
) {
    val px = (minOf(w, h) * 0.13f).coerceAtLeast(1.5f)
    val corner = CornerRadius(1.5f, 1.5f)

    if (glowBoost > 0f) {
        drawRect(
            color = palette.glow.copy(alpha = (0.4f * glowBoost * alpha).coerceIn(0f, 1f)),
            topLeft = Offset(x - px * 0.5f, y - px * 0.5f),
            size = Size(w + px, h + px)
        )
    }

    // Crisp dark pixel drop border
    drawRect(
        color = Color(0xFF050814).copy(alpha = 0.72f * alpha),
        topLeft = Offset(x + px * 0.25f, y + px * 0.35f),
        size = Size(w, h)
    )

    // Base flat arcade tile fill
    drawRoundRect(
        color = palette.fill.copy(alpha = alpha),
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = corner
    )

    // Stepped bright top and left 8-bit bevel edges
    val topLip = (scratchTopLipPath.get() ?: Path()).apply {
        rewind()
        moveTo(x, y)
        lineTo(x + w, y)
        lineTo(x + w - px, y + px)
        lineTo(x + px, y + px)
        lineTo(x + px, y + h - px)
        lineTo(x, y + h)
        close()
    }
    drawPath(topLip, color = palette.highlight.copy(alpha = 0.85f * alpha))

    // Stepped dark bottom and right 8-bit shadow edges
    val bottomLip = (scratchBottomLipPath.get() ?: Path()).apply {
        rewind()
        moveTo(x + w, y)
        lineTo(x + w, y + h)
        lineTo(x, y + h)
        lineTo(x + px, y + h - px)
        lineTo(x + w - px, y + h - px)
        lineTo(x + w - px, y + px)
        close()
    }
    drawPath(bottomLip, color = palette.shadow.copy(alpha = 0.92f * alpha))

    // Classic inner pixel border + top-left square glint
    if (w > px * 4f && h > px * 4f) {
        drawRect(
            color = palette.shadow.copy(alpha = 0.38f * alpha),
            topLeft = Offset(x + px * 1.5f, y + px * 1.5f),
            size = Size(w - px * 3f, h - px * 3f),
            style = Stroke(width = (px * 0.55f).coerceAtLeast(1f))
        )
        drawRect(
            color = Color.White.copy(alpha = 0.78f * alpha),
            topLeft = Offset(x + px * 1.35f, y + px * 1.35f),
            size = Size(px * 1.15f, px * 1.15f)
        )
    }
}

private fun DrawScope.drawNeonSynthBlock(
    x: Float,
    y: Float,
    w: Float,
    h: Float,
    palette: BlockPalette,
    alpha: Float,
    glowBoost: Float
) {
    val radius = (minOf(w, h) * 0.22f).coerceAtLeast(2.5f)
    val edge = (minOf(w, h) * 0.12f).coerceAtLeast(2f)
    val haloAlpha = ((0.36f + glowBoost * 0.32f) * alpha).coerceIn(0f, 0.85f)

    // Luminous neon outer aura
    drawRoundRect(
        color = palette.glow.copy(alpha = haloAlpha),
        topLeft = Offset(x - edge * 0.45f, y - edge * 0.45f),
        size = Size(w + edge * 0.9f, h + edge * 0.9f),
        cornerRadius = CornerRadius(radius * 1.25f, radius * 1.25f)
    )

    // Dark synth-glass core with subtle inner tint
    drawRoundRect(
        brush = Brush.radialGradient(
            colors = listOf(
                palette.fill.copy(alpha = 0.38f * alpha),
                Color(0xFF0A1026).copy(alpha = 0.92f * alpha)
            ),
            center = Offset(x + w * 0.5f, y + h * 0.5f),
            radius = maxOf(w, h) * 0.72f
        ),
        topLeft = Offset(x, y),
        size = Size(w, h),
        cornerRadius = CornerRadius(radius, radius)
    )

    // Glowing primary neon rim
    drawRoundRect(
        color = palette.glow.copy(alpha = 0.95f * alpha),
        topLeft = Offset(x + edge * 0.35f, y + edge * 0.35f),
        size = Size((w - edge * 0.7f).coerceAtLeast(1f), (h - edge * 0.7f).coerceAtLeast(1f)),
        cornerRadius = CornerRadius(radius, radius),
        style = Stroke(width = (edge * 0.9f).coerceAtLeast(1.8f))
    )

    // Bright white-hot inner hairline filament
    if (w > edge * 4f && h > edge * 4f) {
        drawRoundRect(
            color = Color.White.copy(alpha = 0.65f * alpha),
            topLeft = Offset(x + edge * 1.25f, y + edge * 1.25f),
            size = Size(w - edge * 2.5f, h - edge * 2.5f),
            cornerRadius = CornerRadius((radius * 0.6f).coerceAtLeast(1.5f)),
            style = Stroke(width = (edge * 0.32f).coerceAtLeast(1f))
        )
    }
}
