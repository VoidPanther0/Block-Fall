package com.example.game

/**
 * Original jewel-toned block palette for Cascade.
 * Color index is persisted cleanly across saves without tying engine to Compose Color.
 */
enum class BlockColor(val index: Int) {
    CYAN(0),
    AMBER(1),
    CORAL(2),
    EMERALD(3),
    VIOLET(4),
    TANGERINE(5),
    COBALT(6);

    companion object {
        fun fromIndex(index: Int): BlockColor =
            entries.firstOrNull { it.index == index } ?: CYAN
    }
}

data class GridCoord(
    val row: Int,
    val col: Int
)

data class BlockCell(
    val id: Long,
    val color: BlockColor
)
