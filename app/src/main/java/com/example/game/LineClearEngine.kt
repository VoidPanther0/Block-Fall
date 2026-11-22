package com.example.game

data class FullLinesResult(
    val fullRows: List<Int>,
    val fullCols: List<Int>
) {
    val isEmpty: Boolean get() = fullRows.isEmpty() && fullCols.isEmpty()
    val totalLines: Int get() = fullRows.size + fullCols.size
}

data class LineClearResult(
    val boardAfterClear: Board,
    val fullRows: List<Int>,
    val fullCols: List<Int>,
    val clearedCoords: Set<GridCoord>,
    val linesClearedCount: Int
)

/**
 * Detects and removes completed horizontal rows and vertical columns simultaneously,
 * handling row/column intersections cleanly without double-removal errors.
 */
object LineClearEngine {

    fun findFullLines(board: Board): FullLinesResult {
        val rows = (0 until Board.SIZE).filter { board.isRowFull(it) }
        val cols = (0 until Board.SIZE).filter { board.isColFull(it) }
        return FullLinesResult(fullRows = rows, fullCols = cols)
    }

    /**
     * Predicts which rows and columns will clear if [piece] is placed at ([anchorRow], [anchorCol]),
     * without allocating a temporary copy of the [Board].
     */
    fun findPredictedFullLines(
        board: Board,
        piece: GamePiece,
        anchorRow: Int,
        anchorCol: Int
    ): FullLinesResult {
        val offsets = piece.shape.offsets
        val fullRows = mutableListOf<Int>()
        val fullCols = mutableListOf<Int>()

        for (r in 0 until Board.SIZE) {
            var complete = true
            for (c in 0 until Board.SIZE) {
                if (board.get(r, c) == null && !pieceCovers(offsets, anchorRow, anchorCol, r, c)) {
                    complete = false
                    break
                }
            }
            if (complete) fullRows.add(r)
        }

        for (c in 0 until Board.SIZE) {
            var complete = true
            for (r in 0 until Board.SIZE) {
                if (board.get(r, c) == null && !pieceCovers(offsets, anchorRow, anchorCol, r, c)) {
                    complete = false
                    break
                }
            }
            if (complete) fullCols.add(c)
        }

        return FullLinesResult(fullRows = fullRows, fullCols = fullCols)
    }

    private fun pieceCovers(
        offsets: List<GridCoord>,
        anchorRow: Int,
        anchorCol: Int,
        row: Int,
        col: Int
    ): Boolean {
        for (i in 0 until offsets.size) {
            val o = offsets[i]
            if (anchorRow + o.row == row && anchorCol + o.col == col) return true
        }
        return false
    }

    fun clearLines(board: Board, fullLines: FullLinesResult = findFullLines(board)): LineClearResult {
        if (fullLines.isEmpty) {
            return LineClearResult(
                boardAfterClear = board,
                fullRows = emptyList(),
                fullCols = emptyList(),
                clearedCoords = emptySet(),
                linesClearedCount = 0
            )
        }

        val clearedCoords = LinkedHashSet<GridCoord>()
        for (r in fullLines.fullRows) {
            for (c in 0 until Board.SIZE) {
                clearedCoords.add(GridCoord(r, c))
            }
        }
        for (c in fullLines.fullCols) {
            for (r in 0 until Board.SIZE) {
                clearedCoords.add(GridCoord(r, c))
            }
        }

        val updates = clearedCoords.associateWith { null }
        return LineClearResult(
            boardAfterClear = board.withCells(updates),
            fullRows = fullLines.fullRows,
            fullCols = fullLines.fullCols,
            clearedCoords = clearedCoords,
            linesClearedCount = fullLines.totalLines
        )
    }
}
