package com.example.game

data class FallingBlockMove(
    val cell: BlockCell,
    val col: Int,
    val fromRow: Int,
    val toRow: Int
) {
    val distance: Int get() = toRow - fromRow
}

data class GravityResult(
    val newBoard: Board,
    val movements: List<FallingBlockMove>
) {
    val didBlocksMove: Boolean get() = movements.isNotEmpty()
}

/**
 * Deterministic vertical gravity engine.
 * Blocks fall strictly downward within their own columns until resting on the bottom
 * or another block, preserving relative vertical order and never moving sideways.
 */
object GravityEngine {

    fun applyGravity(board: Board): GravityResult {
        val nextGrid = Array(Board.SIZE) { arrayOfNulls<BlockCell>(Board.SIZE) }
        val movements = mutableListOf<FallingBlockMove>()

        for (col in 0 until Board.SIZE) {
            var writeRow = Board.SIZE - 1
            for (readRow in (Board.SIZE - 1) downTo 0) {
                val cell = board.get(readRow, col) ?: continue
                nextGrid[writeRow][col] = cell
                if (writeRow != readRow) {
                    movements.add(
                        FallingBlockMove(
                            cell = cell,
                            col = col,
                            fromRow = readRow,
                            toRow = writeRow
                        )
                    )
                }
                writeRow--
            }
        }

        val settledBoard = Board(nextGrid.map { it.toList() })
        return GravityResult(
            newBoard = settledBoard,
            movements = movements
        )
    }
}
