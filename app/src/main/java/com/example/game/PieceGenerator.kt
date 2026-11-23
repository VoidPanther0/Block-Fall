package com.example.game

import kotlin.random.Random

/**
 * Generates balanced 3-piece trays with polyomino shapes (1-5 cells).
 * Prevents unfair repeated pentomino floods and avoids unfair lockouts on open boards,
 * while preserving genuine end-game detection when boards fill up.
 */
class PieceGenerator(
    private var random: Random = Random.Default
) {
    /** Deterministic stream for seeded modes (Daily Gravity); null restores true randomness. */
    fun reseed(seed: Long?) {
        random = if (seed == null) Random.Default else Random(seed)
    }

    companion object {
        /**
         * Shapes excluded from newly dealt trays per gameplay tuning:
         * - 3-cube straight bars (LINE_3_H, LINE_3_V)
         * - Oversized 5-cell / bulky rectangles & pentominoes (LINE_5_H, LINE_5_V, BIG_CORNER_BR, BIG_CORNER_BL, PLUS_5, U_5)
         */
        val EXCLUDED_SHAPES: Set<PieceShape> = setOf(
            PieceShape.LINE_3_H,
            PieceShape.LINE_3_V,
            PieceShape.LINE_5_H,
            PieceShape.LINE_5_V,
            PieceShape.BIG_CORNER_BR,
            PieceShape.BIG_CORNER_BL,
            PieceShape.PLUS_5,
            PieceShape.U_5
        )

        val ACTIVE_SHAPES: List<PieceShape> = PieceShape.entries.filterNot { it in EXCLUDED_SHAPES }
    }

    private val allShapes = ACTIVE_SHAPES
    private val totalWeight = allShapes.sumOf { it.weight }
    private val colors = BlockColor.entries

    fun generateTray(
        board: Board,
        startingPieceId: Long
    ): Pair<List<GamePiece>, Long> {
        var nextId = startingPieceId
        val occupied = board.occupiedCount()
        val playablePool = if (occupied >= 28) {
            allShapes.filter { shape ->
                val probe = GamePiece(0L, shape, colors.first())
                PlacementValidator.findAllValidAnchors(board, probe).isNotEmpty()
            }
        } else {
            allShapes
        }
        val selectionPool = if (playablePool.isNotEmpty() && occupied >= 34) playablePool else allShapes
        val selectedShapes = mutableListOf<PieceShape>()

        repeat(3) {
            var candidate = pickWeightedShape(selectionPool)
            var attempts = 0
            while (attempts < 10) {
                val duplicateShape = selectedShapes.contains(candidate)
                val tooManyLarge =
                    candidate.cellCount >= 4 && selectedShapes.count { it.cellCount >= 4 } >= if (occupied >= 32) 1 else 2
                if (!duplicateShape && !tooManyLarge) break
                candidate = pickWeightedShape(selectionPool)
                attempts++
            }
            selectedShapes.add(candidate)
        }

        val distinctColors = colors.shuffled(random).take(3)
        val pieces = selectedShapes.mapIndexed { idx, shape ->
            GamePiece(
                instanceId = nextId++,
                shape = shape,
                color = distinctColors[idx]
            )
        }.toMutableList()

        // Fairness safeguard: whenever the board has room (<= 56 occupied cells) and at least one
        // active shape can fit on the board, guarantee slot 0 is immediately playable.
        if (occupied <= 56 && !PlacementValidator.hasAnyValidMove(board, pieces)) {
            val rescueShapes = allShapes.filter { shape ->
                val probe = GamePiece(0L, shape, pieces[0].color)
                PlacementValidator.findAllValidAnchors(board, probe).isNotEmpty()
            }
            if (rescueShapes.isNotEmpty()) {
                val preferredRescue = if (occupied >= 44) {
                    rescueShapes.sortedBy { it.cellCount }.take(3)
                } else {
                    rescueShapes
                }
                val rescueShape = preferredRescue[random.nextInt(preferredRescue.size)]
                pieces[0] = pieces[0].copy(shape = rescueShape)
            }
        }

        return pieces to nextId
    }

    private fun pickWeightedShape(pool: List<PieceShape> = allShapes): PieceShape {
        val poolWeight = if (pool === allShapes) totalWeight else pool.sumOf { it.weight }.coerceAtLeast(1)
        var roll = random.nextInt(poolWeight)
        for (shape in pool) {
            roll -= shape.weight
            if (roll < 0) return shape
        }
        return pool.first()
    }
}
