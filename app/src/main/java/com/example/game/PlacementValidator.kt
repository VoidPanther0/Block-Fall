package com.example.game

data class PlacementResult(
    val board: Board,
    val placedCoords: List<GridCoord>,
    val nextBlockId: Long
)

/**
 * Authoritative placement validation and piece commitment engine.
 * Visual UI must never determine placement legality independently.
 */
object PlacementValidator {

    fun isWithinBounds(row: Int, col: Int): Boolean =
        row in 0 until Board.SIZE && col in 0 until Board.SIZE

    fun canPlace(
        board: Board,
        piece: GamePiece,
        anchorRow: Int,
        anchorCol: Int
    ): Boolean {
        for (offset in piece.shape.offsets) {
            val targetRow = anchorRow + offset.row
            val targetCol = anchorCol + offset.col
            if (!isWithinBounds(targetRow, targetCol)) return false
            if (board.isOccupied(targetRow, targetCol)) return false
        }
        return true
    }

    fun placePiece(
        board: Board,
        piece: GamePiece,
        anchorRow: Int,
        anchorCol: Int,
        startingBlockId: Long
    ): PlacementResult? {
        if (!canPlace(board, piece, anchorRow, anchorCol)) return null

        var nextId = startingBlockId
        val coords = mutableListOf<GridCoord>()
        val updates = mutableMapOf<GridCoord, BlockCell?>()

        for (offset in piece.shape.offsets) {
            val coord = GridCoord(anchorRow + offset.row, anchorCol + offset.col)
            coords.add(coord)
            updates[coord] = BlockCell(id = nextId++, color = piece.color)
        }

        return PlacementResult(
            board = board.withCells(updates),
            placedCoords = coords,
            nextBlockId = nextId
        )
    }

    fun findAllValidAnchors(board: Board, piece: GamePiece): List<GridCoord> {
        val valid = mutableListOf<GridCoord>()
        val maxRow = Board.SIZE - piece.shape.heightCells
        val maxCol = Board.SIZE - piece.shape.widthCells
        for (r in 0..maxRow) {
            for (c in 0..maxCol) {
                if (canPlace(board, piece, r, c)) {
                    valid.add(GridCoord(r, c))
                }
            }
        }
        return valid
    }

    fun hasAnyValidMove(board: Board, availablePieces: List<GamePiece>): Boolean =
        availablePieces.any { piece -> findAllValidAnchors(board, piece).isNotEmpty() }
}
