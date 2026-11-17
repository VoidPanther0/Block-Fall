package com.example.ui

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.GameRepository
import com.example.game.BlockColor
import com.example.game.Board
import com.example.game.CascadeResolver
import com.example.game.ChallengeLevel
import com.example.game.ChallengeLevels
import com.example.game.FallingBlockMove
import com.example.game.GameMode
import com.example.game.GamePiece
import com.example.game.GameStatus
import com.example.game.GridCoord
import com.example.game.LineClearEngine
import com.example.game.PieceGenerator
import com.example.game.PlacementValidator
import com.example.game.SavedClassicGame
import com.example.game.ScoringEngine
import com.example.game.TraySlot
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random

sealed interface BoardVisualPhase {
    data object Idle : BoardVisualPhase

    data class ClearingLines(
        val boardBeforeClear: Board,
        val clearedCoords: Set<GridCoord>,
        val clearedRows: List<Int>,
        val clearedCols: List<Int>,
        val waveIndex: Int,
        val progress: Float
    ) : BoardVisualPhase

    data class FallingGravity(
        val boardAfterClear: Board,
        val movements: List<FallingBlockMove>,
        val waveIndex: Int,
        val progress: Float
    ) : BoardVisualPhase

    data class LandingSettled(
        val board: Board,
        val landedCoords: Set<GridCoord>,
        val progress: Float
    ) : BoardVisualPhase
}

data class ParticleBurst(
    val id: Long,
    val row: Float,
    val col: Float,
    val color: BlockColor,
    val isLandingDust: Boolean
)

data class FloatingBanner(
    val id: Long,
    val headline: String,
    val subline: String?,
    val pointsText: String,
    val isChain: Boolean
)

data class DragState(
    val slotIndex: Int,
    val piece: GamePiece,
    val pointerRootOffset: Offset,
    val hoverAnchor: GridCoord?,
    val isHoverValid: Boolean,
    val predictedClearRows: List<Int>,
    val predictedClearCols: List<Int>
)

data class GameUiState(
    val mode: GameMode = GameMode.CLASSIC,
    val status: GameStatus = GameStatus.PLAYING,
    val board: Board = Board.empty(),
    val traySlots: List<TraySlot> = List(3) { TraySlot(it, null) },
    val selectedSlotIndex: Int? = null,
    val tapHoverAnchor: GridCoord? = null,
    val score: Int = 0,
    val classicBestScore: Int = 0,
    val isNewBest: Boolean = false,
    val movesPlayed: Int = 0,
    val totalLinesCleared: Int = 0,
    val currentCascadeWave: Int = 0,
    val bestCascadeDepth: Int = 0,
    val currentChallengeLevel: ChallengeLevel = ChallengeLevels.getLevel(1),
    val unlockedChallengeLevel: Int = 1,
    val hapticsEnabled: Boolean = true,
    val visualPhase: BoardVisualPhase = BoardVisualPhase.Idle,
    val dragState: DragState? = null,
    val floatingBanner: FloatingBanner? = null,
    val particleTriggerId: Long = 0L,
    val latestParticles: List<ParticleBurst> = emptyList(),
    val showRulesDialog: Boolean = false
) {
    val movesRemaining: Int
        get() = (currentChallengeLevel.moveLimit - movesPlayed).coerceAtLeast(0)

    val selectedPiece: GamePiece?
        get() = selectedSlotIndex?.let { idx -> traySlots.getOrNull(idx)?.piece }
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application.applicationContext)
    private val pieceGenerator = PieceGenerator(Random.Default)
    private var nextIdCounter: Long = 100L
    private var bannerCounter: Long = 1L
    private var particleCounter: Long = 1L

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var boardBoundsInRoot: Rect = Rect.Zero
    private var dragLiftPx: Float = 140f

    init {
        viewModelScope.launch {
            val saved = repository.loadInitialProgress()
            val restored = saved.savedClassicGame
            if (restored != null && !isClassicGameOver(restored.board, restored.trayPieces)) {
                nextIdCounter = restored.nextIdCounter.coerceAtLeast(100L)
                _uiState.update {
                    it.copy(
                        mode = GameMode.CLASSIC,
                        status = GameStatus.PLAYING,
                        board = restored.board,
                        traySlots = restored.trayPieces.mapIndexed { i, p -> TraySlot(i, p) },
                        score = restored.score,
                        classicBestScore = maxOf(saved.classicBestScore, restored.score),
                        movesPlayed = restored.movesPlayed,
                        bestCascadeDepth = restored.bestCascadeDepth,
                        totalLinesCleared = restored.totalLinesCleared,
                        unlockedChallengeLevel = saved.unlockedChallengeLevel,
                        hapticsEnabled = saved.hapticsEnabled
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        classicBestScore = saved.classicBestScore,
                        unlockedChallengeLevel = saved.unlockedChallengeLevel,
                        hapticsEnabled = saved.hapticsEnabled
                    )
                }
                startNewClassicGame()
            }
        }
    }

    fun updateBoardBounds(rect: Rect, liftPx: Float) {
        boardBoundsInRoot = rect
        dragLiftPx = liftPx
    }

    fun startNewClassicGame() {
        val emptyBoard = Board.empty()
        val (trayPieces, updatedId) = pieceGenerator.generateTray(emptyBoard, nextIdCounter)
        nextIdCounter = updatedId

        _uiState.update {
            it.copy(
                mode = GameMode.CLASSIC,
                status = GameStatus.PLAYING,
                board = emptyBoard,
                traySlots = trayPieces.mapIndexed { i, p -> TraySlot(i, p) },
                selectedSlotIndex = null,
                tapHoverAnchor = null,
                score = 0,
                isNewBest = false,
                movesPlayed = 0,
                totalLinesCleared = 0,
                currentCascadeWave = 0,
                bestCascadeDepth = 0,
                visualPhase = BoardVisualPhase.Idle,
                dragState = null,
                floatingBanner = null,
                latestParticles = emptyList()
            )
        }
        persistClassicSnapshot()
    }

    fun startChallengeLevel(levelNumber: Int) {
        val level = ChallengeLevels.getLevel(levelNumber)
        val board = level.buildBoard(startId = nextIdCounter)
        nextIdCounter += 100L
        val (trayPieces, updatedId) = pieceGenerator.generateTray(board, nextIdCounter)
        nextIdCounter = updatedId

        _uiState.update {
            it.copy(
                mode = GameMode.CHALLENGE,
                status = GameStatus.PLAYING,
                board = board,
                traySlots = trayPieces.mapIndexed { i, p -> TraySlot(i, p) },
                selectedSlotIndex = null,
                tapHoverAnchor = null,
                score = 0,
                isNewBest = false,
                movesPlayed = 0,
                totalLinesCleared = 0,
                currentCascadeWave = 0,
                bestCascadeDepth = 0,
                currentChallengeLevel = level,
                visualPhase = BoardVisualPhase.Idle,
                dragState = null,
                floatingBanner = null,
                latestParticles = emptyList()
            )
        }
    }

    fun switchMode(mode: GameMode) {
        if (_uiState.value.status == GameStatus.RESOLVING_CASCADE) return
        when (mode) {
            GameMode.CLASSIC -> {
                viewModelScope.launch {
                    val saved = repository.loadInitialProgress().savedClassicGame
                    if (saved != null && !isClassicGameOver(saved.board, saved.trayPieces)) {
                        nextIdCounter = saved.nextIdCounter.coerceAtLeast(nextIdCounter)
                        _uiState.update {
                            it.copy(
                                mode = GameMode.CLASSIC,
                                status = GameStatus.PLAYING,
                                board = saved.board,
                                traySlots = saved.trayPieces.mapIndexed { i, p -> TraySlot(i, p) },
                                selectedSlotIndex = null,
                                score = saved.score,
                                movesPlayed = saved.movesPlayed,
                                bestCascadeDepth = saved.bestCascadeDepth,
                                totalLinesCleared = saved.totalLinesCleared,
                                visualPhase = BoardVisualPhase.Idle,
                                dragState = null,
                                floatingBanner = null
                            )
                        }
                    } else {
                        startNewClassicGame()
                    }
                }
            }
            GameMode.CHALLENGE -> {
                startChallengeLevel(_uiState.value.unlockedChallengeLevel)
            }
        }
    }

    fun restartCurrentGame() {
        val state = _uiState.value
        if (state.status == GameStatus.RESOLVING_CASCADE) return
        if (state.mode == GameMode.CLASSIC) {
            startNewClassicGame()
        } else {
            startChallengeLevel(state.currentChallengeLevel.levelNumber)
        }
    }

    fun pauseGame() {
        _uiState.update {
            if (it.status == GameStatus.PLAYING) it.copy(status = GameStatus.PAUSED, dragState = null)
            else it
        }
    }

    fun resumeGame() {
        _uiState.update {
            if (it.status == GameStatus.PAUSED) it.copy(status = GameStatus.PLAYING)
            else it
        }
    }

    fun toggleRulesDialog(show: Boolean) {
        _uiState.update { it.copy(showRulesDialog = show) }
    }

    fun toggleHaptics(enabled: Boolean) {
        _uiState.update { it.copy(hapticsEnabled = enabled) }
        viewModelScope.launch { repository.setHapticsEnabled(enabled) }
    }

    fun selectTraySlot(slotIndex: Int) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return
        val slot = state.traySlots.getOrNull(slotIndex) ?: return
        if (slot.piece == null) return
        _uiState.update {
            val nextSelection = if (it.selectedSlotIndex == slotIndex) null else slotIndex
            it.copy(selectedSlotIndex = nextSelection, tapHoverAnchor = null)
        }
    }

    fun onBoardCellTapped(row: Int, col: Int) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return
        val slotIdx = state.selectedSlotIndex ?: return
        val piece = state.traySlots.getOrNull(slotIdx)?.piece ?: return

        // Center the piece around tapped cell for comfortable tap-to-place ergonomics
        val anchorRow = (row - piece.shape.heightCells / 2).coerceIn(0, Board.SIZE - 1)
        val anchorCol = (col - piece.shape.widthCells / 2).coerceIn(0, Board.SIZE - 1)

        if (PlacementValidator.canPlace(state.board, piece, anchorRow, anchorCol)) {
            placePiece(slotIdx, anchorRow, anchorCol)
        } else if (PlacementValidator.canPlace(state.board, piece, row, col)) {
            placePiece(slotIdx, row, col)
        }
    }

    fun startDrag(slotIndex: Int, pointerRootOffset: Offset) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return
        val piece = state.traySlots.getOrNull(slotIndex)?.piece ?: return
        _uiState.update {
            it.copy(
                selectedSlotIndex = slotIndex,
                dragState = computeDragState(state.board, slotIndex, piece, pointerRootOffset)
            )
        }
    }

    fun updateDrag(pointerRootOffset: Offset) {
        val state = _uiState.value
        val activeDrag = state.dragState ?: return
        if (state.status != GameStatus.PLAYING) return
        _uiState.update {
            it.copy(
                dragState = computeDragState(
                    board = state.board,
                    slotIndex = activeDrag.slotIndex,
                    piece = activeDrag.piece,
                    pointerRootOffset = pointerRootOffset
                )
            )
        }
    }

    fun endDrag() {
        val state = _uiState.value
        val activeDrag = state.dragState ?: return
        val anchor = activeDrag.hoverAnchor
        _uiState.update { it.copy(dragState = null) }

        if (state.status == GameStatus.PLAYING && anchor != null && activeDrag.isHoverValid) {
            placePiece(activeDrag.slotIndex, anchor.row, anchor.col)
        }
    }

    fun cancelDrag() {
        _uiState.update { it.copy(dragState = null) }
    }

    private fun computeDragState(
        board: Board,
        slotIndex: Int,
        piece: GamePiece,
        pointerRootOffset: Offset
    ): DragState {
        if (boardBoundsInRoot.width <= 1f || boardBoundsInRoot.height <= 1f) {
            return DragState(
                slotIndex = slotIndex,
                piece = piece,
                pointerRootOffset = pointerRootOffset,
                hoverAnchor = null,
                isHoverValid = false,
                predictedClearRows = emptyList(),
                predictedClearCols = emptyList()
            )
        }

        val cellSize = boardBoundsInRoot.width / Board.SIZE.toFloat()
        val pieceW = piece.shape.widthCells * cellSize
        val pieceH = piece.shape.heightCells * cellSize

        val topLeftX = pointerRootOffset.x - pieceW / 2f
        val topLeftY = pointerRootOffset.y - dragLiftPx - pieceH / 2f

        val rawCol = ((topLeftX - boardBoundsInRoot.left) / cellSize).roundToInt()
        val rawRow = ((topLeftY - boardBoundsInRoot.top) / cellSize).roundToInt()

        val nearBoard = topLeftX >= boardBoundsInRoot.left - cellSize * 1.1f &&
            topLeftX <= boardBoundsInRoot.right + cellSize * 0.4f &&
            topLeftY >= boardBoundsInRoot.top - cellSize * 1.1f &&
            topLeftY <= boardBoundsInRoot.bottom + cellSize * 0.4f

        if (!nearBoard) {
            return DragState(
                slotIndex = slotIndex,
                piece = piece,
                pointerRootOffset = pointerRootOffset,
                hoverAnchor = null,
                isHoverValid = false,
                predictedClearRows = emptyList(),
                predictedClearCols = emptyList()
            )
        }

        val anchor = GridCoord(rawRow, rawCol)
        val valid = PlacementValidator.canPlace(board, piece, anchor.row, anchor.col)
        var predRows = emptyList<Int>()
        var predCols = emptyList<Int>()
        if (valid) {
            val previewPlacement = PlacementValidator.placePiece(board, piece, anchor.row, anchor.col, 0L)
            if (previewPlacement != null) {
                val fullLines = LineClearEngine.findFullLines(previewPlacement.board)
                predRows = fullLines.fullRows
                predCols = fullLines.fullCols
            }
        }

        return DragState(
            slotIndex = slotIndex,
            piece = piece,
            pointerRootOffset = pointerRootOffset,
            hoverAnchor = anchor,
            isHoverValid = valid,
            predictedClearRows = predRows,
            predictedClearCols = predCols
        )
    }

    fun placePiece(slotIndex: Int, anchorRow: Int, anchorCol: Int) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return
        val piece = state.traySlots.getOrNull(slotIndex)?.piece ?: return

        val placement = PlacementValidator.placePiece(
            board = state.board,
            piece = piece,
            anchorRow = anchorRow,
            anchorCol = anchorCol,
            startingBlockId = nextIdCounter
        ) ?: return
        nextIdCounter = placement.nextBlockId

        val placementPoints = ScoringEngine.scorePlacement(piece.shape.cellCount)
        val updatedTray = state.traySlots.map { slot ->
            if (slot.index == slotIndex) slot.copy(piece = null) else slot
        }

        val resolution = CascadeResolver.resolve(placement.board)
        val nextMovesPlayed = state.movesPlayed + 1
        val initialScore = state.score + placementPoints

        val newClassicBest = if (state.mode == GameMode.CLASSIC && initialScore > state.classicBestScore) {
            initialScore
        } else {
            state.classicBestScore
        }
        val becameNewBest = state.mode == GameMode.CLASSIC &&
            (state.isNewBest || initialScore > state.classicBestScore)

        _uiState.update {
            it.copy(
                board = placement.board,
                traySlots = updatedTray,
                selectedSlotIndex = null,
                tapHoverAnchor = null,
                dragState = null,
                score = initialScore,
                classicBestScore = newClassicBest,
                isNewBest = becameNewBest,
                movesPlayed = nextMovesPlayed,
                currentCascadeWave = 0,
                status = if (resolution.hasClears) GameStatus.RESOLVING_CASCADE else it.status
            )
        }

        if (!resolution.hasClears) {
            finalizeTurn(
                finalBoard = placement.board,
                trayAfterPlacement = updatedTray,
                scoreAfterTurn = initialScore,
                movesPlayed = nextMovesPlayed,
                linesAdded = 0,
                maxDepthInTurn = 0
            )
        } else {
            viewModelScope.launch {
                runCascadeAnimation(
                    resolution = resolution,
                    startingScore = initialScore,
                    trayAfterPlacement = updatedTray,
                    movesPlayed = nextMovesPlayed
                )
            }
        }
    }

    private suspend fun runCascadeAnimation(
        resolution: com.example.game.CascadeResolution,
        startingScore: Int,
        trayAfterPlacement: List<TraySlot>,
        movesPlayed: Int
    ) {
        var runningScore = startingScore
        var totalLinesInTurn = 0

        for (wave in resolution.waves) {
            val waveLines = wave.clearedRows.size + wave.clearedCols.size
            totalLinesInTurn += waveLines

            val banner = buildBannerForWave(
                waveIndex = wave.waveIndex,
                linesCount = waveLines,
                points = wave.scoreBreakdown.totalPoints,
                isFullClear = wave.isFullClear
            )

            // Spawn shatter particles on cleared cells
            val clearParticles = wave.clearedCoords.mapNotNull { coord ->
                val color = wave.boardBeforeClear.get(coord.row, coord.col)?.color ?: return@mapNotNull null
                ParticleBurst(
                    id = particleCounter++,
                    row = coord.row + 0.5f,
                    col = coord.col + 0.5f,
                    color = color,
                    isLandingDust = false
                )
            }

            _uiState.update {
                it.copy(
                    currentCascadeWave = wave.waveIndex,
                    bestCascadeDepth = maxOf(it.bestCascadeDepth, wave.waveIndex),
                    floatingBanner = banner,
                    particleTriggerId = particleCounter++,
                    latestParticles = clearParticles
                )
            }

            // Phase 1: Line clear flash & shrink
            animateStep(durationMs = 210L) { progress ->
                _uiState.update {
                    it.copy(
                        visualPhase = BoardVisualPhase.ClearingLines(
                            boardBeforeClear = wave.boardBeforeClear,
                            clearedCoords = wave.clearedCoords,
                            clearedRows = wave.clearedRows,
                            clearedCols = wave.clearedCols,
                            waveIndex = wave.waveIndex,
                            progress = progress
                        )
                    )
                }
            }

            // Update score right as wave clears
            runningScore += wave.scoreBreakdown.totalPoints
            _uiState.update { state ->
                val updatedBest = if (state.mode == GameMode.CLASSIC && runningScore > state.classicBestScore) {
                    runningScore
                } else {
                    state.classicBestScore
                }
                val newBestFlag = state.mode == GameMode.CLASSIC &&
                    (state.isNewBest || runningScore > state.classicBestScore)
                state.copy(
                    board = wave.boardAfterClear,
                    score = runningScore,
                    classicBestScore = updatedBest,
                    isNewBest = newBestFlag,
                    totalLinesCleared = state.totalLinesCleared + waveLines
                )
            }

            // Phase 2: Vertical gravity fall (only if blocks actually moved)
            if (wave.movements.isNotEmpty()) {
                val maxDist = wave.movements.maxOf { it.distance }
                val fallDuration = (190L + maxDist * 30L).coerceAtMost(360L)
                animateStep(durationMs = fallDuration) { linear ->
                    val gravityEase = linear.pow(1.45f).coerceIn(0f, 1f)
                    _uiState.update {
                        it.copy(
                            visualPhase = BoardVisualPhase.FallingGravity(
                                boardAfterClear = wave.boardAfterClear,
                                movements = wave.movements,
                                waveIndex = wave.waveIndex,
                                progress = gravityEase
                            )
                        )
                    }
                }

                val landedCoords = wave.movements.map { GridCoord(it.toRow, it.col) }.toSet()
                val dustParticles = wave.movements.map { move ->
                    ParticleBurst(
                        id = particleCounter++,
                        row = move.toRow + 0.92f,
                        col = move.col + 0.5f,
                        color = move.cell.color,
                        isLandingDust = true
                    )
                }

                _uiState.update {
                    it.copy(
                        board = wave.boardAfterGravity,
                        particleTriggerId = particleCounter++,
                        latestParticles = dustParticles
                    )
                }

                // Phase 3: Subtle landing squash feedback
                animateStep(durationMs = 95L) { progress ->
                    _uiState.update {
                        it.copy(
                            visualPhase = BoardVisualPhase.LandingSettled(
                                board = wave.boardAfterGravity,
                                landedCoords = landedCoords,
                                progress = progress
                            )
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(board = wave.boardAfterGravity) }
                delay(60L)
            }
        }

        _uiState.update {
            it.copy(
                board = resolution.finalBoard,
                visualPhase = BoardVisualPhase.Idle,
                currentCascadeWave = 0
            )
        }

        val lastBannerId = resolution.waves.lastOrNull()?.let { _uiState.value.floatingBanner?.id }

        finalizeTurn(
            finalBoard = resolution.finalBoard,
            trayAfterPlacement = trayAfterPlacement,
            scoreAfterTurn = runningScore,
            movesPlayed = movesPlayed,
            linesAdded = totalLinesInTurn,
            maxDepthInTurn = resolution.maxCascadeDepth
        )

        // Clear floating banner gently after a beat
        if (lastBannerId != null) {
            delay(1100L)
            _uiState.update { current ->
                if (current.floatingBanner?.id == lastBannerId) {
                    current.copy(floatingBanner = null)
                } else {
                    current
                }
            }
        }
    }

    private suspend fun animateStep(durationMs: Long, onFrame: (Float) -> Unit) {
        val frameMs = 16L
        val steps = (durationMs / frameMs).coerceAtLeast(1L).toInt()
        for (step in 0..steps) {
            val progress = (step.toFloat() / steps.toFloat()).coerceIn(0f, 1f)
            onFrame(progress)
            if (step < steps) delay(frameMs)
        }
    }

    private fun buildBannerForWave(
        waveIndex: Int,
        linesCount: Int,
        points: Int,
        isFullClear: Boolean
    ): FloatingBanner {
        val headline = when {
            isFullClear -> "BOARD CLEARED!"
            waveIndex >= 3 -> "MEGA CASCADE x$waveIndex!"
            waveIndex == 2 -> "GRAVITY CHAIN x2!"
            linesCount >= 3 -> "TRIPLE CLEAR!"
            linesCount == 2 -> "DOUBLE CLEAR!"
            else -> "LINE CLEAR"
        }
        val subline = when {
            waveIndex >= 2 -> "$linesCount ${if (linesCount == 1) "line" else "lines"} chained by gravity"
            linesCount >= 2 -> "$linesCount lines cleared simultaneously"
            else -> null
        }
        return FloatingBanner(
            id = bannerCounter++,
            headline = headline,
            subline = subline,
            pointsText = "+$points",
            isChain = waveIndex >= 2 || isFullClear
        )
    }

    private fun finalizeTurn(
        finalBoard: Board,
        trayAfterPlacement: List<TraySlot>,
        scoreAfterTurn: Int,
        movesPlayed: Int,
        linesAdded: Int,
        maxDepthInTurn: Int
    ) {
        val remainingPieces = trayAfterPlacement.mapNotNull { it.piece }
        val nextTraySlots = if (remainingPieces.isEmpty()) {
            val (refilled, updatedId) = pieceGenerator.generateTray(finalBoard, nextIdCounter)
            nextIdCounter = updatedId
            refilled.mapIndexed { idx, p -> TraySlot(idx, p) }
        } else {
            trayAfterPlacement
        }

        val activePieces = nextTraySlots.mapNotNull { it.piece }
        val hasMove = PlacementValidator.hasAnyValidMove(finalBoard, activePieces)
        val state = _uiState.value

        val nextStatus = when (state.mode) {
            GameMode.CLASSIC -> {
                if (hasMove) GameStatus.PLAYING else GameStatus.GAME_OVER
            }
            GameMode.CHALLENGE -> {
                val targetReached = scoreAfterTurn >= state.currentChallengeLevel.targetScore
                val outOfMoves = movesPlayed >= state.currentChallengeLevel.moveLimit
                when {
                    targetReached -> GameStatus.CHALLENGE_WON
                    outOfMoves || !hasMove -> GameStatus.CHALLENGE_LOST
                    else -> GameStatus.PLAYING
                }
            }
        }

        val nextUnlockedLevel = if (
            state.mode == GameMode.CHALLENGE &&
            nextStatus == GameStatus.CHALLENGE_WON
        ) {
            maxOf(
                state.unlockedChallengeLevel,
                (state.currentChallengeLevel.levelNumber + 1).coerceAtMost(ChallengeLevels.levels.size)
            )
        } else {
            state.unlockedChallengeLevel
        }

        _uiState.update {
            it.copy(
                board = finalBoard,
                traySlots = nextTraySlots,
                status = nextStatus,
                unlockedChallengeLevel = nextUnlockedLevel,
                bestCascadeDepth = maxOf(it.bestCascadeDepth, maxDepthInTurn)
            )
        }

        viewModelScope.launch {
            if (state.mode == GameMode.CLASSIC) {
                repository.updateClassicBestScore(scoreAfterTurn)
                if (nextStatus == GameStatus.GAME_OVER) {
                    repository.saveClassicGame(null)
                } else {
                    persistClassicSnapshot()
                }
            } else if (nextStatus == GameStatus.CHALLENGE_WON) {
                repository.unlockChallengeLevel(nextUnlockedLevel)
            }
        }
    }

    private fun persistClassicSnapshot() {
        val s = _uiState.value
        if (s.mode != GameMode.CLASSIC) return
        val snapshot = SavedClassicGame(
            board = s.board,
            trayPieces = s.traySlots.map { it.piece },
            score = s.score,
            movesPlayed = s.movesPlayed,
            bestCascadeDepth = s.bestCascadeDepth,
            totalLinesCleared = s.totalLinesCleared,
            nextIdCounter = nextIdCounter
        )
        viewModelScope.launch {
            repository.saveClassicGame(snapshot)
            repository.updateClassicBestScore(s.score)
        }
    }

    private fun isClassicGameOver(board: Board, tray: List<GamePiece?>): Boolean {
        val available = tray.filterNotNull()
        if (available.isEmpty()) return false
        return !PlacementValidator.hasAnyValidMove(board, available)
    }
}
