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

    private val allShapes = PieceShape.entries
    private val totalWeight = allShapes.sumOf { it.weight }
    private val colors = BlockColor.entries

    fun generateTray(
        board: Board,
        startingPieceId: Long
    ): Pair<List<GamePiece>, Long> {
        var nextId = startingPieceId
        val selectedShapes = mutableListOf<PieceShape>()

        repeat(3) { slotIdx ->
            var candidate = pickWeightedShape()
            var attempts = 0
            while (attempts < 8) {
                val tooManyPentominoes =
                    candidate.cellCount >= 5 && selectedShapes.count { it.cellCount >= 5 } >= 1
                val duplicateShape = selectedShapes.contains(candidate)
                val tooManyLarge =
                    candidate.cellCount >= 4 && selectedShapes.count { it.cellCount >= 4 } >= 2
                if (!tooManyPentominoes && !duplicateShape && !tooManyLarge) break
                candidate = pickWeightedShape()
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

        // Fairness safeguard: when the board is reasonably open (<= 75% occupied),
        // guarantee at least one generated piece can fit if any catalog shape fits.
        if (board.occupiedCount() <= 48 && !PlacementValidator.hasAnyValidMove(board, pieces)) {
            val playableShapes = allShapes.filter { shape ->
                val probe = GamePiece(0L, shape, pieces[0].color)
                PlacementValidator.findAllValidAnchors(board, probe).isNotEmpty()
            }
            if (playableShapes.isNotEmpty()) {
                val rescueShape = playableShapes[random.nextInt(playableShapes.size)]
                pieces[0] = pieces[0].copy(shape = rescueShape)
            }
        }

        return pieces to nextId
    }

    private fun pickWeightedShape(): PieceShape {
        var roll = random.nextInt(totalWeight)
        for (shape in allShapes) {
            roll -= shape.weight
            if (roll < 0) return shape
        }
        return allShapes.first()
    }
}
