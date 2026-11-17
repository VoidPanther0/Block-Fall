package com.example.game

/**
 * Immutable 8x8 logical puzzle board.
 * Row 0 is the top of the board; Row 7 is the bottom (gravity floor).
 */
data class Board(
    val cells: List<List<BlockCell?>>
) {
    init {
        require(cells.size == SIZE) { "Board must have exactly $SIZE rows." }
        require(cells.all { it.size == SIZE }) { "Each board row must have exactly $SIZE columns." }
    }

    fun get(row: Int, col: Int): BlockCell? {
        if (row !in 0 until SIZE || col !in 0 until SIZE) return null
        return cells[row][col]
    }

    fun isOccupied(row: Int, col: Int): Boolean = get(row, col) != null

    fun isRowFull(row: Int): Boolean {
        if (row !in 0 until SIZE) return false
        return cells[row].all { it != null }
    }

    fun isColFull(col: Int): Boolean {
        if (col !in 0 until SIZE) return false
        return (0 until SIZE).all { row -> cells[row][col] != null }
    }

    fun isEmpty(): Boolean = cells.all { row -> row.all { it == null } }

    fun occupiedCount(): Int = cells.sumOf { row -> row.count { it != null } }

    fun withCells(updates: Map<GridCoord, BlockCell?>): Board {
        if (updates.isEmpty()) return this
        val nextGrid = cells.mapIndexed { r, rowList ->
            rowList.mapIndexed { c, existing ->
                val coord = GridCoord(r, c)
                if (updates.containsKey(coord)) updates[coord] else existing
            }
        }
        return Board(nextGrid)
    }

    companion object {
        const val SIZE = 8

        fun empty(): Board = Board(List(SIZE) { List(SIZE) { null } })

        /**
         * Builds a board from a compact 8-line string pattern for testing and challenge levels.
         * '.' = empty, '#' or digit '0'..'6' = occupied cell with corresponding color index.
         */
        fun fromPattern(lines: List<String>, startId: Long = 1L): Board {
            require(lines.size == SIZE) { "Pattern must have $SIZE rows." }
            var nextId = startId
            val grid = lines.map { line ->
                require(line.length == SIZE) { "Pattern row '$line' must have length $SIZE." }
                line.map { ch ->
                    when {
                        ch == '.' || ch == ' ' -> null
                        ch in '0'..'6' -> BlockCell(nextId++, BlockColor.fromIndex(ch - '0'))
                        else -> BlockCell(nextId++, BlockColor.CYAN)
                    }
                }
            }
            return Board(grid)
        }
    }
}
