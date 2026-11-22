package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.BlockStyle
import com.example.game.Board
import com.example.game.BoardColorPalette
import com.example.game.GamePiece
import com.example.game.GameStatus
import com.example.game.PlacementValidator
import com.example.game.TraySlot
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentWarning
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.LocalBlockStyle
import com.example.ui.theme.LocalBoardPalette
import com.example.ui.theme.toBoardColors

@Composable
fun PieceTray(
    slots: List<TraySlot>,
    board: Board,
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
    val blockStyle = LocalBlockStyle.current
    val boardPalette = LocalBoardPalette.current
    val boardTheme = remember(boardPalette) { boardPalette.toBoardColors() }
    val trayCorner = if (blockStyle == BlockStyle.RETRO) 10.dp else 18.dp
    val trayShape = RoundedCornerShape(trayCorner)

    // Legality comes from the authoritative validator — the UI only reflects it.
    val usableSlots = remember(board, slots) {
        slots.map { slot ->
            slot.piece != null && PlacementValidator.findAllValidAnchors(board, slot.piece).isNotEmpty()
        }
    }
    val anyUsable = usableSlots.any { it }

    Column(
        verticalArrangement = Arrangement.spacedBy(7.dp),
        modifier = modifier
            .fillMaxWidth()
            .shadow(8.dp, trayShape, clip = false)
            .clip(trayShape)
            .background(Brush.verticalGradient(listOf(boardTheme.trayTop, boardTheme.trayBottom)))
            .border(1.25.dp, boardTheme.trayBorder, trayShape)
            .padding(horizontal = 10.dp, vertical = 9.dp)
            .testTag("piece_tray")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "YOUR PIECES",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    fontSize = 10.sp
                ),
                color = TextSecondary,
                maxLines = 1
            )
            val statusText = when {
                !anyUsable && status == GameStatus.PLAYING -> "NO VALID PLACEMENT"
                status != GameStatus.PLAYING -> "WAITING"
                else -> "${slots.count { it.piece != null }}/3 READY"
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp,
                    fontSize = 9.5.sp
                ),
                color = if (!anyUsable && status == GameStatus.PLAYING) AccentWarning else AccentCyan,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("piece_tray_status")
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            slots.forEachIndexed { index, slot ->
                val usable = usableSlots.getOrElse(index) { false }
                TraySlotCard(
                    slot = slot,
                    isSelected = selectedSlotIndex == slot.index,
                    isBeingDragged = draggingSlotIndex == slot.index,
                    isUsable = usable,
                    isInteractive = status == GameStatus.PLAYING && slot.piece != null,
                    blockStyle = blockStyle,
                    boardPalette = boardPalette,
                    onTap = { onSlotTapped(slot.index) },
                    onDragStart = { rootPos -> onDragStart(slot.index, rootPos) },
                    onDragMove = onDragMove,
                    onDragEnd = onDragEnd,
                    onDragCancel = onDragCancel,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (!anyUsable && status == GameStatus.PLAYING) {
            Spacer(Modifier.height(1.dp))
            Text(
                text = "No piece fits the board right now.",
                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.5.sp),
                color = AccentWarning,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.testTag("piece_tray_no_valid_hint")
            )
        }
    }
}

@Composable
private fun TraySlotCard(
    slot: TraySlot,
    isSelected: Boolean,
    isBeingDragged: Boolean,
    isUsable: Boolean,
    isInteractive: Boolean,
    blockStyle: BlockStyle,
    boardPalette: BoardColorPalette,
    onTap: () -> Unit,
    onDragStart: (Offset) -> Unit,
    onDragMove: (Offset) -> Unit,
    onDragEnd: () -> Unit,
    onDragCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var slotRootPos by remember { mutableStateOf(Offset.Zero) }
    val boardTheme = remember(boardPalette) { boardPalette.toBoardColors() }
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected -> AccentGold
            slot.piece != null && !isUsable -> AccentWarning.copy(alpha = 0.55f)
            slot.piece != null -> AccentCyan.copy(alpha = 0.42f)
            else -> boardTheme.rim.copy(alpha = 0.2f)
        },
        label = "slot_border"
    )
    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected) 1.04f else 1f,
        animationSpec = spring(dampingRatio = 0.72f, stiffness = 460f),
        label = "slot_scale"
    )

    val bgColor = if (isSelected) boardTheme.selectedSlotBg else boardTheme.slotBg
    val slotCorner = if (blockStyle == BlockStyle.RETRO) 6.dp else 16.dp
    val desc = slot.piece?.let { "Piece ${slot.index + 1}, ${it.shape.cellCount} cells" }
        ?: "Empty piece slot ${slot.index + 1}"

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .defaultMinSize(minHeight = 64.dp)
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = selectionScale
                scaleY = selectionScale
            }
            .clip(RoundedCornerShape(slotCorner))
            .background(bgColor)
            .border(if (isSelected) 2.dp else 1.dp, borderColor, RoundedCornerShape(slotCorner))
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
            .padding(8.dp)
            .testTag("tray_slot_${slot.index}")
            .semantics { contentDescription = desc }
    ) {
        val piece = slot.piece
        if (piece != null && !isBeingDragged) {
            MiniPieceCanvas(
                piece = piece,
                blockStyle = blockStyle,
                boardPalette = boardPalette,
                modifier = Modifier.fillMaxSize()
            )
        }
        if (piece != null && !isUsable) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(slotCorner))
                    .background(androidx.compose.ui.graphics.Color(0x99070B18))
            )
            Text(
                text = "NO FIT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 9.sp
                ),
                color = AccentWarning,
                maxLines = 1
            )
        }
    }
}

@Composable
fun MiniPieceCanvas(
    piece: GamePiece,
    modifier: Modifier = Modifier,
    blockStyle: BlockStyle = LocalBlockStyle.current,
    boardPalette: BoardColorPalette = LocalBoardPalette.current
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
                color = piece.color,
                blockStyle = blockStyle,
                boardPalette = boardPalette
            )
        }
    }
}
