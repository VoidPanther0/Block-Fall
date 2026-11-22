package com.example

import com.example.game.*
import org.junit.Assert.*
import org.junit.Test
import kotlin.random.Random

class CoreAuditTest {
    private fun piece(shape: PieceShape, id: Long = 1) = GamePiece(id, shape, BlockColor.CYAN)
    private fun tray(vararg p: GamePiece?) = p.mapIndexed { i, x -> TraySlot(i, x) }
    private val lvl = ChallengeLevels.getLevel(1)

    // ---- Game over ----
    @Test fun emptyBoardIsNeverGameOver() {
        assertFalse(GameSessionEngine.isClassicGameOver(Board.empty(), listOf(piece(PieceShape.DOT))))
    }

    @Test fun fullBoardIsGameOverForAnyPiece() {
        val full = Board.fromPattern(List(8) { "########" })
        assertTrue(GameSessionEngine.isClassicGameOver(full, PieceShape.entries.map { piece(it) }))
    }

    @Test fun oneHoleAcceptsDotButNotLargerPieces() {
        val rows = MutableList(8) { "########" }
        rows[3] = "###.####"
        val b = Board.fromPattern(rows)
        assertFalse(GameSessionEngine.isClassicGameOver(b, listOf(piece(PieceShape.DOT))))
        val bigOnly = PieceShape.entries.filter { it.cellCount > 1 }.map { piece(it) }
        assertTrue(GameSessionEngine.isClassicGameOver(b, bigOnly))
        assertFalse(GameSessionEngine.isClassicGameOver(b, bigOnly.take(2) + piece(PieceShape.DOT)))
    }

    @Test fun usedSlotsAreIgnoredAndEmptyTrayIsNotGameOver() {
        val full = Board.fromPattern(List(8) { "########" })
        assertFalse(GameSessionEngine.isClassicGameOver(full, listOf(null, null, null)))
    }

    @Test fun clearingEverythingRefillsTrayAndContinues() {
        val rows = MutableList(8) { "........" }
        rows[7] = "#######."
        val engine = GameSessionEngine(PieceGenerator(Random(1)))
        val turn = engine.executeTurn(
            Board.fromPattern(rows), tray(piece(PieceShape.DOT), null, null), 0, 7, 7, 0, 0,
            GameMode.CLASSIC, lvl, 100L
        )!!
        assertTrue(turn.resolution.finalBoard.isEmpty())
        assertEquals(GameStatus.PLAYING, turn.nextStatus)
        assertEquals(3, turn.nextTraySlots.count { it.piece != null })
        assertEquals(FeedbackTier.PERFECT, turn.feedback.tier)
    }

    @Test fun gameEndsWhenRemainingPiecesCannotFit() {
        // every row/column has two adjacent-ish holes; after one DOT no line completes
        val rows = List(8) { i -> CharArray(8) { '#' }.also { it[i] = '.'; it[(i + 1) % 8] = '.' }.concatToString() }
        val engine = GameSessionEngine(PieceGenerator(Random(2)))
        val turn = engine.executeTurn(
            Board.fromPattern(rows), tray(piece(PieceShape.DOT), piece(PieceShape.LINE_3_H, 2), null),
            0, 0, 0, 0, 0, GameMode.CLASSIC, lvl, 100L
        )!!
        assertFalse(turn.resolution.hasClears)
        assertEquals(GameStatus.GAME_OVER, turn.nextStatus)
    }

    @Test fun gameContinuesWhenAnyRemainingPieceStillFits() {
        val rows = List(8) { i -> CharArray(8) { '#' }.also { it[i] = '.'; it[(i + 1) % 8] = '.' }.concatToString() }
        val engine = GameSessionEngine(PieceGenerator(Random(2)))
        val turn = engine.executeTurn(
            Board.fromPattern(rows), tray(piece(PieceShape.DOT), piece(PieceShape.LINE_3_H, 2), piece(PieceShape.DOT, 3)),
            0, 0, 0, 0, 0, GameMode.CLASSIC, lvl, 100L
        )!!
        assertEquals(GameStatus.PLAYING, turn.nextStatus)
    }

    @Test fun invalidPlacementIsRejected() {
        val engine = GameSessionEngine(PieceGenerator(Random(3)))
        assertNull(engine.executeTurn(Board.empty(), tray(piece(PieceShape.LINE_3_H), null, null), 0, 0, 7, 0, 0,
            GameMode.CLASSIC, lvl, 1L))
    }

    // ---- Cascades / gravity ----
    @Test fun cascadeResolutionAlwaysEndsWithNoFullLinesAndDepthMatchesWaves() {
        val rnd = Random(42)
        repeat(300) {
            val rows = List(8) { CharArray(8) { if (rnd.nextInt(100) < 80) '#' else '.' }.concatToString() }
            val res = CascadeResolver.resolve(Board.fromPattern(rows))
            assertEquals(res.waves.size, res.maxCascadeDepth)
            assertEquals(res.waves.sumOf { it.scoreBreakdown.totalPoints }, res.totalWavePoints)
            assertTrue(LineClearEngine.findFullLines(res.finalBoard).isEmpty)
        }
    }

    @Test fun gravitySettlesColumnsDownward() {
        val rows = MutableList(8) { "........" }
        rows[0] = "#......."; rows[4] = "#......."
        val g = GravityEngine.applyGravity(Board.fromPattern(rows)).newBoard
        assertNotNull(g.get(7, 0)); assertNotNull(g.get(6, 0)); assertNull(g.get(0, 0))
    }

    // ---- Scoring / feedback ----
    @Test fun greatBoundaryIsExactlyThreeLines() {
        fun fb(lines: Int): ScoreFeedback {
            val rows = MutableList(8) { "........" }
            for (i in 0 until lines) rows[7 - i] = "########"
            rows[0] = "#......."
            return ScoreFeedback.from(CascadeResolver.resolve(Board.fromPattern(rows)))
        }
        assertEquals(FeedbackTier.NONE, fb(1).tier)
        assertEquals(FeedbackTier.NONE, fb(2).tier)
        assertEquals(FeedbackTier.GREAT, fb(3).tier)
        assertEquals(FeedbackTier.GREAT, fb(5).tier)
    }

    @Test fun perfectOutranksGreat() {
        val fb = ScoreFeedback.from(CascadeResolver.resolve(Board.fromPattern(List(8) { "########" })))
        assertEquals(FeedbackTier.PERFECT, fb.tier)
    }

    @Test fun noClearMeansNoFeedbackEvenWithStreak() {
        assertFalse(ScoreFeedback.from(CascadeResolver.resolve(Board.empty()), comboStreak = 6).hasFeedback)
    }

    @Test fun comboStreakBuildsAndResets() {
        val rows = MutableList(8) { "........" }
        rows[7] = "#######."; rows[0] = "#......."
        val engine = GameSessionEngine(PieceGenerator(Random(5)))
        val t = tray(piece(PieceShape.DOT), piece(PieceShape.DOT, 2), piece(PieceShape.DOT, 3))
        val first = engine.executeTurn(Board.fromPattern(rows), t, 0, 7, 7, 0, 0, GameMode.CLASSIC, lvl, 100L, comboStreak = 1)!!
        assertEquals(2, first.comboStreak)
        assertEquals(ComboLabel.COMBO_2, first.feedback.combo)
        val miss = engine.executeTurn(Board.empty(), t, 0, 3, 3, 0, 0, GameMode.CLASSIC, lvl, 100L, comboStreak = 4)!!
        assertEquals(0, miss.comboStreak)
        assertEquals(ComboLabel.NONE, miss.feedback.combo)
        assertEquals(ComboLabel.COMBO_5_PLUS, ScoreFeedback.from(first.resolution, comboStreak = 7).combo)
        assertEquals(ComboLabel.COMBO_3, ScoreFeedback.from(first.resolution, comboStreak = 3).combo)
        assertEquals(ComboLabel.COMBO_4, ScoreFeedback.from(first.resolution, comboStreak = 4).combo)
    }

    @Test fun multiplierAndCascadeBonusScaleWithWave() {
        assertEquals(1, ScoringEngine.scoreWave(2, 1, false).multiplier)
        val w3 = ScoringEngine.scoreWave(1, 3, false)
        assertEquals(3, w3.multiplier); assertEquals(50, w3.cascadeBonus)
        assertEquals(10 * 3 + 50, w3.totalPoints)
        assertEquals(0, ScoringEngine.scoreWave(0, 5, true).totalPoints)
        assertEquals(20 + 15 + 150, ScoringEngine.scoreWave(2, 1, true).totalPoints)
    }

    // ---- Daily Gravity ----
    @Test fun dailyPuzzleIsDeterministicAndPlayable() {
        for (day in 1..28) {
            val key = "2026-02-%02d".format(day)
            val a = DailyChallengeGenerator.forDateKey(key)
            val b = DailyChallengeGenerator.forDateKey(key)
            assertEquals(a.level.starterPattern, b.level.starterPattern)
            val board = a.level.buildBoard(1L)
            assertTrue(LineClearEngine.findFullLines(board).isEmpty)
            assertTrue(PlacementValidator.hasAnyValidMove(board, listOf(piece(PieceShape.DOT))))
        }
    }

    @Test fun seededPieceStreamIsReproducible() {
        val g1 = PieceGenerator(); val g2 = PieceGenerator()
        g1.reseed(99L); g2.reseed(99L)
        val b = Board.empty()
        assertEquals(
            g1.generateTray(b, 1).first.map { it.shape to it.color },
            g2.generateTray(b, 1).first.map { it.shape to it.color }
        )
    }

    @Test fun dailyProgressResetsOnNewDayAndNeverRegresses() {
        var p = DailyProgress()
        p = p.withAttemptStarted("2026-03-01").withResult("2026-03-01", 300, won = true)
        p = p.withResult("2026-03-01", 100, won = false)
        assertEquals(300, p.bestScore); assertTrue(p.completed); assertEquals(1, p.attempts)
        val next = p.forDate("2026-03-02")
        assertEquals(0, next.bestScore); assertFalse(next.completed); assertEquals(0, next.attempts)
        assertEquals(DailyProgress("2026-03-02", 50, false, 0), p.withResult("2026-03-02", 50, false))
        assertEquals(p, DailyProgress.fromJson(p.toJson()))
        assertEquals(DailyProgress(), DailyProgress.fromJson("garbage"))
    }
}
