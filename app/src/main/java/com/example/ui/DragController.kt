package com.example.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import com.example.game.Board
import com.example.game.GamePiece
import com.example.game.GridCoord
import com.example.game.LineClearEngine
import com.example.game.PlacementValidator
import kotlin.math.roundToInt

/**
 * Coordinates drag geometry, lift clearance above finger, and cached zero-copy line-clear
 * prediction so pointer move events stay allocation-light.
 */
class DragController {

    var boardBoundsInRoot: Rect = Rect.Zero
        private set
    private var dragLiftPx: Float = 176f

    private var cachedKey: Triple<Long, Board, GridCoord>? = null
    private var cachedValid: Boolean = false
    private var cachedPredRows: List<Int> = emptyList()
    private var cachedPredCols: List<Int> = emptyList()

    fun updateBoardBounds(rect: Rect, liftPx: Float) {
        boardBoundsInRoot = rect
        dragLiftPx = liftPx
    }

    fun rootOffsetForLocalBoardTouch(localOffset: Offset): Offset =
        boardBoundsInRoot.topLeft + localOffset

    fun computeTapPlacementAnchor(
        board: Board,
        piece: GamePiece,
        tappedRow: Int,
        tappedCol: Int
    ): GridCoord? {
        // Lift placement above the touched row so the user's finger does not obscure the piece
        val liftRows = 2
        val maxAnchorRow = (Board.SIZE - piece.shape.heightCells).coerceAtLeast(0)
        val maxAnchorCol = (Board.SIZE - piece.shape.widthCells).coerceAtLeast(0)
        val liftedBottomRow = (tappedRow - liftRows).coerceAtLeast(piece.shape.heightCells - 1)
        val anchorRow = (liftedBottomRow - (piece.shape.heightCells - 1)).coerceIn(0, maxAnchorRow)
        val anchorCol = (tappedCol - piece.shape.widthCells / 2).coerceIn(0, maxAnchorCol)

        return when {
            PlacementValidator.canPlace(board, piece, anchorRow, anchorCol) -> GridCoord(anchorRow, anchorCol)
            PlacementValidator.canPlace(board, piece, tappedRow, tappedCol) -> GridCoord(tappedRow, tappedCol)
            else -> null
        }
    }

    fun computeDragState(
        board: Board,
        slotIndex: Int,
        piece: GamePiece,
        pointerRootOffset: Offset,
        clampToBoard: Boolean = false
    ): DragState {
        if (boardBoundsInRoot.width <= 1f || boardBoundsInRoot.height <= 1f) {
            return DragState(
                slotIndex = slotIndex,
                piece = piece,
                pointerRootOffset = pointerRootOffset,
                hoverAnchor = null,
                isHoverValid = false,
                predictedClearRows = emptyList(),
                predictedClearCols = emptyList()
            )
        }

        val cellSize = boardBoundsInRoot.width / Board.SIZE.toFloat()
        val pieceW = piece.shape.widthCells * cellSize
        val pieceH = piece.shape.heightCells * cellSize

        // Keep the bottom edge of the dragged/touched piece lifted comfortably above the finger
        val topLeftX = pointerRootOffset.x - pieceW / 2f
        val topLeftY = pointerRootOffset.y - dragLiftPx - pieceH

        val rawCol = ((topLeftX - boardBoundsInRoot.left) / cellSize).roundToInt()
        val rawRow = ((topLeftY - boardBoundsInRoot.top) / cellSize).roundToInt()

        if (!clampToBoard) {
            val nearBoard = topLeftX >= boardBoundsInRoot.left - cellSize * 1.1f &&
                topLeftX <= boardBoundsInRoot.right + cellSize * 0.4f &&
                topLeftY >= boardBoundsInRoot.top - cellSize * 1.1f &&
                topLeftY <= boardBoundsInRoot.bottom + cellSize * 0.4f

            if (!nearBoard) {
                return DragState(
                    slotIndex = slotIndex,
                    piece = piece,
                    pointerRootOffset = pointerRootOffset,
                    hoverAnchor = null,
                    isHoverValid = false,
                    predictedClearRows = emptyList(),
                    predictedClearCols = emptyList()
                )
            }
        }

        val anchor = if (clampToBoard) {
            val maxRow = (Board.SIZE - piece.shape.heightCells).coerceAtLeast(0)
            val maxCol = (Board.SIZE - piece.shape.widthCells).coerceAtLeast(0)
            GridCoord(rawRow.coerceIn(0, maxRow), rawCol.coerceIn(0, maxCol))
        } else {
            GridCoord(rawRow, rawCol)
        }

        val key = Triple(piece.instanceId, board, anchor)
        val valid: Boolean
        val predRows: List<Int>
        val predCols: List<Int>
        if (cachedKey == key) {
            valid = cachedValid
            predRows = cachedPredRows
            predCols = cachedPredCols
        } else {
            valid = PlacementValidator.canPlace(board, piece, anchor.row, anchor.col)
            if (valid) {
                val fullLines = LineClearEngine.findPredictedFullLines(board, piece, anchor.row, anchor.col)
                predRows = fullLines.fullRows
                predCols = fullLines.fullCols
            } else {
                predRows = emptyList()
                predCols = emptyList()
            }
            cachedKey = key
            cachedValid = valid
            cachedPredRows = predRows
            cachedPredCols = predCols
        }

        return DragState(
            slotIndex = slotIndex,
            piece = piece,
            pointerRootOffset = pointerRootOffset,
            hoverAnchor = anchor,
            isHoverValid = valid,
            predictedClearRows = predRows,
            predictedClearCols = predCols
        )
    }
}
