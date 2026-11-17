package com.example.game

/**
 * Original polyomino shapes from 1 to 5 cells.
 * Coordinates are normalized relative to (0, 0) top-left bounding box.
 */
enum class PieceShape(
    val id: String,
    val offsets: List<GridCoord>,
    val weight: Int
) {
    // 1-cell
    DOT(
        id = "dot",
        offsets = listOf(GridCoord(0, 0)),
        weight = 5
    ),

    // 2-cell dominoes
    DOMINO_H(
        id = "domino_h",
        offsets = listOf(GridCoord(0, 0), GridCoord(0, 1)),
        weight = 8
    ),
    DOMINO_V(
        id = "domino_v",
        offsets = listOf(GridCoord(0, 0), GridCoord(1, 0)),
        weight = 8
    ),

    // 3-cell trominoes
    LINE_3_H(
        id = "line3_h",
        offsets = listOf(GridCoord(0, 0), GridCoord(0, 1), GridCoord(0, 2)),
        weight = 9
    ),
    LINE_3_V(
        id = "line3_v",
        offsets = listOf(GridCoord(0, 0), GridCoord(1, 0), GridCoord(2, 0)),
        weight = 9
    ),
    CORNER_3_TL(
        id = "corner3_tl",
        offsets = listOf(GridCoord(0, 0), GridCoord(0, 1), GridCoord(1, 0)),
        weight = 7
    ),
    CORNER_3_TR(
        id = "corner3_tr",
        offsets = listOf(GridCoord(0, 0), GridCoord(0, 1), GridCoord(1, 1)),
        weight = 7
    ),
    CORNER_3_BL(
        id = "corner3_bl",
        offsets = listOf(GridCoord(0, 0), GridCoord(1, 0), GridCoord(1, 1)),
        weight = 7
    ),
    CORNER_3_BR(
        id = "corner3_br",
        offsets = listOf(GridCoord(0, 1), GridCoord(1, 0), GridCoord(1, 1)),
        weight = 7
    ),

    // 4-cell tetrominoes
    SQUARE_2X2(
        id = "square_2x2",
        offsets = listOf(GridCoord(0, 0), GridCoord(0, 1), GridCoord(1, 0), GridCoord(1, 1)),
        weight = 9
    ),
    LINE_4_H(
        id = "line4_h",
        offsets = listOf(GridCoord(0, 0), GridCoord(0, 1), GridCoord(0, 2), GridCoord(0, 3)),
        weight = 6
    ),
    LINE_4_V(
        id = "line4_v",
        offsets = listOf(GridCoord(0, 0), GridCoord(1, 0), GridCoord(2, 0), GridCoord(3, 0)),
        weight = 6
    ),
    L_4_DOWN_RIGHT(
        id = "l4_dr",
        offsets = listOf(GridCoord(0, 0), GridCoord(1, 0), GridCoord(2, 0), GridCoord(2, 1)),
        weight = 5
    ),
    L_4_DOWN_LEFT(
        id = "l4_dl",
        offsets = listOf(GridCoord(0, 1), GridCoord(1, 1), GridCoord(2, 0), GridCoord(2, 1)),
        weight = 5
    ),
    L_4_UP_RIGHT(
        id = "l4_ur",
        offsets = listOf(GridCoord(0, 0), GridCoord(0, 1), GridCoord(1, 0), GridCoord(2, 0)),
        weight = 4
    ),
    L_4_UP_LEFT(
        id = "l4_ul",
        offsets = listOf(GridCoord(0, 0), GridCoord(0, 1), GridCoord(1, 1), GridCoord(2, 1)),
        weight = 4
    ),
    T_4_DOWN(
        id = "t4_d",
        offsets = listOf(GridCoord(0, 0), GridCoord(0, 1), GridCoord(0, 2), GridCoord(1, 1)),
        weight = 5
    ),
    T_4_UP(
        id = "t4_u",
        offsets = listOf(GridCoord(0, 1), GridCoord(1, 0), GridCoord(1, 1), GridCoord(1, 2)),
        weight = 5
    ),
    Z_4_H(
        id = "z4_h",
        offsets = listOf(GridCoord(0, 0), GridCoord(0, 1), GridCoord(1, 1), GridCoord(1, 2)),
        weight = 4
    ),
    S_4_H(
        id = "s4_h",
        offsets = listOf(GridCoord(0, 1), GridCoord(0, 2), GridCoord(1, 0), GridCoord(1, 1)),
        weight = 4
    ),

    // 5-cell pentominoes
    LINE_5_H(
        id = "line5_h",
        offsets = listOf(
            GridCoord(0, 0), GridCoord(0, 1), GridCoord(0, 2), GridCoord(0, 3), GridCoord(0, 4)
        ),
        weight = 3
    ),
    LINE_5_V(
        id = "line5_v",
        offsets = listOf(
            GridCoord(0, 0), GridCoord(1, 0), GridCoord(2, 0), GridCoord(3, 0), GridCoord(4, 0)
        ),
        weight = 3
    ),
    BIG_CORNER_BR(
        id = "big_corner_br",
        offsets = listOf(
            GridCoord(0, 2), GridCoord(1, 2), GridCoord(2, 0), GridCoord(2, 1), GridCoord(2, 2)
        ),
        weight = 3
    ),
    BIG_CORNER_BL(
        id = "big_corner_bl",
        offsets = listOf(
            GridCoord(0, 0), GridCoord(1, 0), GridCoord(2, 0), GridCoord(2, 1), GridCoord(2, 2)
        ),
        weight = 3
    ),
    PLUS_5(
        id = "plus_5",
        offsets = listOf(
            GridCoord(0, 1), GridCoord(1, 0), GridCoord(1, 1), GridCoord(1, 2), GridCoord(2, 1)
        ),
        weight = 2
    ),
    U_5(
        id = "u_5",
        offsets = listOf(
            GridCoord(0, 0), GridCoord(0, 2), GridCoord(1, 0), GridCoord(1, 1), GridCoord(1, 2)
        ),
        weight = 2
    );

    val cellCount: Int get() = offsets.size
    val widthCells: Int get() = (offsets.maxOfOrNull { it.col } ?: 0) + 1
    val heightCells: Int get() = (offsets.maxOfOrNull { it.row } ?: 0) + 1

    companion object {
        fun fromId(id: String): PieceShape? = entries.firstOrNull { it.id == id }
    }
}

data class GamePiece(
    val instanceId: Long,
    val shape: PieceShape,
    val color: BlockColor
) {
    fun cellsAt(anchorRow: Int, anchorCol: Int): List<GridCoord> =
        shape.offsets.map { GridCoord(anchorRow + it.row, anchorCol + it.col) }
}
