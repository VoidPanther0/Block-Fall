package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.game.Board
import com.example.game.GridCoord
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardRim
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.EmptySlotBorder
import com.example.ui.theme.EmptySlotFill

@Composable
fun BoardPreview(
    board: Board,
    modifier: Modifier = Modifier,
    highlightedCoords: Set<GridCoord> = emptySet(),
    highlightRows: List<Int> = emptyList()
) {
    val shape = RoundedCornerShape(18.dp)
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(8.dp, shape, clip = false)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF1A3478), BoardWellBg)
                )
            )
            .border(2.dp, BoardRim, shape)
            .padding(8.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cellSize = size.width / Board.SIZE.toFloat()
            val gap = (cellSize * 0.09f).coerceAtLeast(1.5f)
            val tileSize = cellSize - gap
            val radius = CornerRadius(tileSize * 0.18f, tileSize * 0.18f)

            for (r in 0 until Board.SIZE) {
                for (c in 0 until Board.SIZE) {
                    val left = c * cellSize + gap / 2f
                    val top = r * cellSize + gap / 2f
                    drawRoundRect(
                        color = EmptySlotFill,
                        topLeft = Offset(left, top),
                        size = Size(tileSize, tileSize),
                        cornerRadius = radius
                    )
                    drawRoundRect(
                        color = EmptySlotBorder,
                        topLeft = Offset(left, top),
                        size = Size(tileSize, tileSize),
                        cornerRadius = radius,
                        style = Stroke(width = 1f)
                    )
                }
            }

            for (rowIdx in highlightRows) {
                drawRoundRect(
                    color = AccentGold.copy(alpha = 0.25f),
                    topLeft = Offset(0f, rowIdx * cellSize),
                    size = Size(size.width, cellSize),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }

            for (r in 0 until Board.SIZE) {
                for (c in 0 until Board.SIZE) {
                    val cell = board.get(r, c) ?: continue
                    val isHighlighted = GridCoord(r, c) in highlightedCoords
                    drawBevelBlock(
                        left = c * cellSize + gap / 2f,
                        top = r * cellSize + gap / 2f,
                        cellSize = tileSize,
                        color = cell.color,
                        glowBoost = if (isHighlighted) 0.9f else 0f
                    )
                }
            }

            for (coord in highlightedCoords) {
                if (board.get(coord.row, coord.col) == null) {
                    drawRoundRect(
                        color = AccentGold.copy(alpha = 0.35f),
                        topLeft = Offset(coord.col * cellSize + gap / 2f, coord.row * cellSize + gap / 2f),
                        size = Size(tileSize, tileSize),
                        cornerRadius = radius
                    )
                    drawRoundRect(
                        color = Color.White.copy(alpha = 0.8f),
                        topLeft = Offset(coord.col * cellSize + gap / 2f, coord.row * cellSize + gap / 2f),
                        size = Size(tileSize, tileSize),
                        cornerRadius = radius,
                        style = Stroke(width = 1.5f)
                    )
                }
            }
        }
    }
}
