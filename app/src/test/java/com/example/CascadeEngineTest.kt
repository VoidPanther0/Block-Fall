package com.example

import com.example.game.BlockCell
import com.example.game.BlockColor
import com.example.game.Board
import com.example.game.CascadeResolver
import com.example.game.ChallengeLevels
import com.example.game.GamePiece
import com.example.game.GravityEngine
import com.example.game.GridCoord
import com.example.game.LineClearEngine
import com.example.game.PieceGenerator
import com.example.game.PieceShape
import com.example.game.PlacementValidator
import com.example.game.ScoringEngine
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class CascadeEngineTest {

    @Test
    fun `1_normal_piece_placement_succeeds_and_updates_coordinates`() {
        val board = Board.empty()
        val piece = GamePiece(1L, PieceShape.CORNER_3_BL, BlockColor.CYAN)

        val result = PlacementValidator.placePiece(board, piece, anchorRow = 2, anchorCol = 3, startingBlockId = 10L)

        assertNotNull(result)
        val placed = result!!
        assertEquals(13L, placed.nextBlockId)
        assertEquals(3, placed.board.occupiedCount())
        assertTrue(placed.board.isOccupied(2, 3))
        assertTrue(placed.board.isOccupied(3, 3))
        assertTrue(placed.board.isOccupied(3, 4))
        assertEquals(3, ScoringEngine.scorePlacement(piece.shape.cellCount))
    }

    @Test
    fun `2_invalid_placement_rejects_out_of_bounds_and_overlaps`() {
        val board = Board.fromPattern(
            listOf(
                "........",
                "........",
                "........",
                "...0....",
                "........",
                "........",
                "........",
                "........"
            )
        )
        val square = GamePiece(1L, PieceShape.SQUARE_2X2, BlockColor.AMBER)

        // Out of bounds
        assertFalse(PlacementValidator.canPlace(board, square, -1, 0))
        assertFalse(PlacementValidator.canPlace(board, square, 7, 0))
        assertFalse(PlacementValidator.canPlace(board, square, 0, 7))
        assertNull(PlacementValidator.placePiece(board, square, 7, 7, 50L))

        // Overlapping occupied cell at (3, 3)
        assertFalse(PlacementValidator.canPlace(board, square, 2, 2))
        assertFalse(PlacementValidator.canPlace(board, square, 3, 3))
        assertNull(PlacementValidator.placePiece(board, square, 2, 2, 50L))
    }

    @Test
    fun `3_horizontal_line_clearing_removes_only_full_row`() {
        val board = Board.fromPattern(
            listOf(
                "........",
                "........",
                "........",
                "........",
                "........",
                "........",
                "..1..2..",
                "00000000"
            )
        )

        val fullLines = LineClearEngine.findFullLines(board)
        assertEquals(listOf(7), fullLines.fullRows)
        assertTrue(fullLines.fullCols.isEmpty())

        val cleared = LineClearEngine.clearLines(board, fullLines)
        assertEquals(1, cleared.linesClearedCount)
        assertEquals(8, cleared.clearedCoords.size)
        for (c in 0 until Board.SIZE) {
            assertFalse(cleared.boardAfterClear.isOccupied(7, c))
        }
        assertTrue(cleared.boardAfterClear.isOccupied(6, 2))
        assertTrue(cleared.boardAfterClear.isOccupied(6, 5))
    }

    @Test
    fun `4_vertical_line_clearing_removes_only_full_column`() {
        val board = Board.fromPattern(
            listOf(
                "...1....",
                "...1....",
                "...1....",
                "...1....",
                "...1....",
                "...1....",
                "...1....",
                "22.1..33"
            )
        )

        val fullLines = LineClearEngine.findFullLines(board)
        assertTrue(fullLines.fullRows.isEmpty())
        assertEquals(listOf(3), fullLines.fullCols)

        val cleared = LineClearEngine.clearLines(board, fullLines)
        assertEquals(1, cleared.linesClearedCount)
        for (r in 0 until Board.SIZE) {
            assertFalse(cleared.boardAfterClear.isOccupied(r, 3))
        }
        assertTrue(cleared.boardAfterClear.isOccupied(7, 0))
        assertTrue(cleared.boardAfterClear.isOccupied(7, 7))
    }

    @Test
    fun `5_simultaneous_row_and_column_clear_handles_intersection_cleanly`() {
        val board = Board.fromPattern(
            listOf(
                "...1....",
                "...1....",
                "...1....",
                "...1....",
                "...1....",
                "...1....",
                "...1....",
                "00010000"
            )
        )

        val fullLines = LineClearEngine.findFullLines(board)
        assertEquals(listOf(7), fullLines.fullRows)
        assertEquals(listOf(3), fullLines.fullCols)
        assertEquals(2, fullLines.totalLines)

        val cleared = LineClearEngine.clearLines(board, fullLines)
        assertEquals(2, cleared.linesClearedCount)
        // 8 + 8 - 1 intersection = 15 unique cleared coordinates
        assertEquals(15, cleared.clearedCoords.size)
        assertTrue(cleared.boardAfterClear.isEmpty())
    }

    @Test
    fun `6_gravity_drops_blocks_vertically_without_sideways_movement`() {
        val board = Board.fromPattern(
            listOf(
                "........",
                ".1......",
                "........",
                ".....2..",
                "........",
                "........",
                "........",
                "00000000"
            )
        )

        val resolution = CascadeResolver.resolve(board)
        assertEquals(1, resolution.waves.size)
        val finalBoard = resolution.finalBoard

        // Row 7 cleared, block at (1,1) drops to (7,1), block at (3,5) drops to (7,5)
        assertTrue(finalBoard.isOccupied(7, 1))
        assertTrue(finalBoard.isOccupied(7, 5))
        assertEquals(2, finalBoard.occupiedCount())
        assertFalse(finalBoard.isOccupied(1, 1))
        assertFalse(finalBoard.isOccupied(3, 5))
    }

    @Test
    fun `7_multiple_blocks_falling_in_one_column_preserve_relative_order_and_ids`() {
        val topCell = BlockCell(101L, BlockColor.CORAL)
        val midCell = BlockCell(102L, BlockColor.AMBER)
        val botCell = BlockCell(103L, BlockColor.CYAN)

        val board = Board.empty().withCells(
            mapOf(
                GridCoord(0, 2) to topCell,
                GridCoord(2, 2) to midCell,
                GridCoord(5, 2) to botCell
            )
        )

        val gravity = GravityEngine.applyGravity(board)
        assertEquals(3, gravity.movements.size)
        assertEquals(botCell, gravity.newBoard.get(7, 2))
        assertEquals(midCell, gravity.newBoard.get(6, 2))
        assertEquals(topCell, gravity.newBoard.get(5, 2))
        assertEquals(3, gravity.newBoard.occupiedCount())

        // Idempotent when applied again
        val secondGravity = GravityEngine.applyGravity(gravity.newBoard)
        assertFalse(secondGravity.didBlocksMove)
        assertTrue(secondGravity.movements.isEmpty())
    }

    @Test
    fun `8_cascading_line_clears_trigger_automatically_after_gravity`() {
        // Row 7 is full -> clears.
        // Above empty row 7, columns 0..3 sit at row 5 and columns 4..7 sit at row 4.
        // After gravity settles, columns 0..7 all reach row 7 -> forming a NEW complete row 7!
        val board = Board.fromPattern(
            listOf(
                "........",
                "........",
                "........",
                "........",
                "....1111",
                "2222....",
                "........",
                "00000000"
            )
        )

        val resolution = CascadeResolver.resolve(board)
        assertEquals(2, resolution.waves.size)
        assertEquals(2, resolution.maxCascadeDepth)
        assertEquals(2, resolution.totalLinesCleared)
        assertTrue(resolution.didFullClear)
        assertTrue(resolution.finalBoard.isEmpty())
    }

    @Test
    fun `9_multi_line_gravity_cascade_resolves_and_scores_multipliers`() {
        // Wave 1: Row 7 clears.
        // Gravity compacts columns:
        //   - Row 6 (cols 0..3) and Row 5 (cols 4..7) fall to Row 7 -> full!
        //   - Row 3 (cols 0..3) and Row 2 (cols 4..7) fall to Row 6 -> also full!
        // Wave 2: Both Row 6 and Row 7 clear simultaneously with 2x cascade + multi-line + full-clear bonus!
        val board = Board.fromPattern(
            listOf(
                "........",
                "........",
                "....3333",
                "3333....",
                "........",
                "....2222",
                "1111....",
                "00000000"
            )
        )

        val resolution = CascadeResolver.resolve(board)
        assertEquals(2, resolution.waves.size)
        assertEquals(2, resolution.maxCascadeDepth)
        assertEquals(3, resolution.totalLinesCleared)
        assertEquals(listOf(7), resolution.waves[0].clearedRows)
        assertEquals(listOf(6, 7), resolution.waves[1].clearedRows)
        assertTrue(resolution.didFullClear)
        assertTrue(resolution.finalBoard.isEmpty())

        // Verify escalating wave scores:
        // Wave 1: 1 line -> 10 * 1 = 10
        // Wave 2: 2 lines + cascade x2 + full clear -> ((20 + 15) * 2) + 25 + 150 = 245
        assertEquals(10, resolution.waves[0].scoreBreakdown.totalPoints)
        assertEquals(245, resolution.waves[1].scoreBreakdown.totalPoints)
        assertEquals(255, resolution.totalWavePoints)
    }

    @Test
    fun `10_no_overlap_and_unique_block_ids_invariant_across_heavy_cascade`() {
        val board = Board.fromPattern(
            listOf(
                "1.2.3.4.",
                ".5.6.0.1",
                "22222222",
                "3.4.5.6.",
                ".0.1.2.3",
                "44444444",
                "5.6.0.1.",
                "00000000"
            )
        )

        val resolution = CascadeResolver.resolve(board)
        val seenIds = mutableSetOf<Long>()
        for (r in 0 until Board.SIZE) {
            for (c in 0 until Board.SIZE) {
                val cell = resolution.finalBoard.get(r, c)
                if (cell != null) {
                    assertTrue("Duplicate cell id found: ${cell.id}", seenIds.add(cell.id))
                }
            }
        }
        // Final board must have zero full rows or columns and zero floating blocks above empty cells
        assertTrue(LineClearEngine.findFullLines(resolution.finalBoard).isEmpty)
        assertFalse(GravityEngine.applyGravity(resolution.finalBoard).didBlocksMove)
    }

    @Test
    fun `11_game_over_detection_and_piece_generator_tray_validation`() {
        // Checkerboard-like almost-full board where only 1x1 fits
        val tightBoard = Board.fromPattern(
            listOf(
                "0.0.0.0.",
                ".0.0.0.0",
                "0.0.0.0.",
                ".0.0.0.0",
                "0.0.0.0.",
                ".0.0.0.0",
                "0.0.0.0.",
                ".0.0.0.0"
            )
        )
        val twoByTwo = GamePiece(1L, PieceShape.SQUARE_2X2, BlockColor.CYAN)
        val line3 = GamePiece(2L, PieceShape.LINE_3_H, BlockColor.AMBER)
        val domino = GamePiece(3L, PieceShape.DOMINO_H, BlockColor.CORAL)

        assertFalse(PlacementValidator.hasAnyValidMove(tightBoard, listOf(twoByTwo, line3, domino)))

        val dot = GamePiece(4L, PieceShape.DOT, BlockColor.EMERALD)
        assertTrue(PlacementValidator.hasAnyValidMove(tightBoard, listOf(twoByTwo, dot, domino)))

        // PieceGenerator produces 3 valid pieces (1..5 cells each) with fairness on open board
        val generator = PieceGenerator(Random(42))
        val (tray, nextId) = generator.generateTray(Board.empty(), 1L)
        assertEquals(3, tray.size)
        assertEquals(4L, nextId)
        assertTrue(tray.all { it.shape.cellCount in 1..5 })
        assertTrue(PlacementValidator.hasAnyValidMove(Board.empty(), tray))

        // Challenge levels all construct valid 8x8 boards
        ChallengeLevels.levels.forEach { lvl ->
            val b = lvl.buildBoard()
            assertTrue(b.occupiedCount() > 0)
        }
    }
}
