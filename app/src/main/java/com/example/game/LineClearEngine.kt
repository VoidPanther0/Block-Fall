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
