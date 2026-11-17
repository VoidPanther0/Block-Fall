package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.game.GamePiece
import com.example.game.GameStatus
import com.example.game.TraySlot
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.BoardRim
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated

@Composable
fun PieceTray(
    slots: List<TraySlot>,
    selectedSlotIndex: Int?,
    draggingSlotIndex: Int?,
    status: GameStatus,
    onSlotTapped: (Int) -> Unit,
    onDragStart: (Int, Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("piece_tray"),
        horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (slot in slots) {
            TraySlotCard(
                slot = slot,
                isSelected = selectedSlotIndex == slot.index,
                isBeingDragged = draggingSlotIndex == slot.index,
                isInteractive = status == GameStatus.PLAYING && slot.piece != null,
                onTap = { onSlotTapped(slot.index) },
                onDragStart = { rootPos -> onDragStart(slot.index, rootPos) },
                onDragMove = onDragMove,
                onDragEnd = onDragEnd,
                onDragCancel = onDragCancel,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TraySlotCard(
    slot: TraySlot,
    isSelected: Boolean,
    isBeingDragged: Boolean,
    isInteractive: Boolean,
    onTap: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var slotRootPos by remember { mutableStateOf(Offset.Zero) }
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> AccentCyan
            slot.piece != null -> BoardRim
            else -> BoardRim.copy(alpha = 0.35f)
        },
        label = "slot_border"
    )

    val bgColor = if (isSelected) SurfaceCardElevated else SurfaceCard
    val desc = slot.piece?.let { "Piece ${slot.index + 1}, ${it.shape.cellCount} cells" }
        ?: "Empty piece slot ${slot.index + 1}"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 92.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(16.dp))
            .onGloballyPositioned { coords ->
                slotRootPos = coords.positionInRoot()
            }
            .clickable(enabled = isInteractive, onClick = onTap)
            .pointerInput(slot.index, slot.piece?.instanceId, isInteractive) {
                if (!isInteractive) return@pointerInput
                var currentPointerInRoot = Offset.Zero
                detectDragGestures(
                    onDragStart = { localOffset ->
                        currentPointerInRoot = slotRootPos + localOffset
                        onDragStart(currentPointerInRoot)
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        currentPointerInRoot += dragAmount
                        onDragMove(currentPointerInRoot)
                    },
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragCancel() }
                )
            }
            .padding(10.dp)
            .testTag("tray_slot_${slot.index}")
            .semantics { contentDescription = desc }
    ) {
        val piece = slot.piece
        if (piece != null && !isBeingDragged) {
            MiniPieceCanvas(piece = piece, modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
fun MiniPieceCanvas(
    piece: GamePiece,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val gridDim = 5f
        val maxCellSize = minOf(size.width, size.height) / gridDim
        val gap = (maxCellSize * 0.12f).coerceAtLeast(1.5f)
        val tile = maxCellSize - gap

        val pieceW = piece.shape.widthCells * maxCellSize
        val pieceH = piece.shape.heightCells * maxCellSize
        val offsetX = (size.width - pieceW) / 2f
        val offsetY = (size.height - pieceH) / 2f

        for (cell in piece.shape.offsets) {
            drawBevelBlock(
                left = offsetX + cell.col * maxCellSize + gap / 2f,
                top = offsetY + cell.row * maxCellSize + gap / 2f,
                cellSize = tile,
                color = piece.color
            )
        }
    }
}
