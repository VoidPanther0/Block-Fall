package com.example.ui

import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.SoundManager
import com.example.data.GameRepository
import com.example.game.BlockColor
import com.example.game.BlockStyle
import com.example.game.Board
import com.example.game.BoardColorPalette
import com.example.game.CascadeResolver
import com.example.game.ChallengeLevel
import com.example.game.ChallengeLevelProgress
import com.example.game.ChallengeLevels
import com.example.game.FallingBlockMove
import com.example.game.GameMode
import com.example.game.GamePiece
import com.example.game.GameSettings
import com.example.game.GameStatistics
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
    val settings: GameSettings = GameSettings(),
    val statistics: GameStatistics = GameStatistics(),
    val challengeProgress: Map<Int, ChallengeLevelProgress> = emptyMap(),
    val lastChallengeStarsEarned: Int = 0,
    val savedClassicSummary: SavedClassicGame? = null,
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

    val hasSavedClassicGame: Boolean
        get() = savedClassicSummary != null
}

class GameViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = GameRepository(application.applicationContext)
    private val pieceGenerator = PieceGenerator(Random.Default)
    val soundManager = SoundManager(application.applicationContext)

    private var nextIdCounter: Long = 100L
    private var bannerCounter: Long = 1L
    private var particleCounter: Long = 1L

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var boardBoundsInRoot: Rect = Rect.Zero
    private var dragLiftPx: Float = 176f

    init {
        viewModelScope.launch {
            val saved = repository.loadInitialProgress()
            soundManager.soundEnabled = saved.settings.soundEnabled
            soundManager.musicEnabled = saved.settings.musicEnabled

            val restored = saved.savedClassicGame
            val validSaved = if (restored != null && !isClassicGameOver(restored.board, restored.trayPieces)) {
                restored
            } else {
                null
            }

            if (validSaved != null) {
                nextIdCounter = validSaved.nextIdCounter.coerceAtLeast(100L)
                _uiState.update {
                    it.copy(
                        mode = GameMode.CLASSIC,
                        status = GameStatus.PLAYING,
                        board = validSaved.board,
                        traySlots = validSaved.trayPieces.mapIndexed { i, p -> TraySlot(i, p) },
                        score = validSaved.score,
                        classicBestScore = maxOf(saved.classicBestScore, validSaved.score),
                        movesPlayed = validSaved.movesPlayed,
                        bestCascadeDepth = validSaved.bestCascadeDepth,
                        totalLinesCleared = validSaved.totalLinesCleared,
                        unlockedChallengeLevel = saved.unlockedChallengeLevel,
                        hapticsEnabled = saved.settings.hapticsEnabled,
                        settings = saved.settings,
                        statistics = saved.statistics.copy(classicBestScore = maxOf(saved.classicBestScore, validSaved.score)),
                        challengeProgress = saved.challengeProgress,
                        savedClassicSummary = validSaved
                    )
                }
            } else {
                val emptyBoard = Board.empty()
                val (trayPieces, updatedId) = pieceGenerator.generateTray(emptyBoard, nextIdCounter)
                nextIdCounter = updatedId
                _uiState.update {
                    it.copy(
                        mode = GameMode.CLASSIC,
                        status = GameStatus.PLAYING,
                        board = emptyBoard,
                        traySlots = trayPieces.mapIndexed { i, p -> TraySlot(i, p) },
                        classicBestScore = saved.classicBestScore,
                        unlockedChallengeLevel = saved.unlockedChallengeLevel,
                        hapticsEnabled = saved.settings.hapticsEnabled,
                        settings = saved.settings,
                        statistics = saved.statistics,
                        challengeProgress = saved.challengeProgress,
                        savedClassicSummary = null
                    )
                }
            }
        }
    }

    fun setAppForeground(inForeground: Boolean) {
        soundManager.setAppForeground(inForeground)
    }

    fun playUiClick() {
        soundManager.playUiClick()
    }

    fun updateBoardBounds(rect: Rect, liftPx: Float) {
        boardBoundsInRoot = rect
        dragLiftPx = liftPx
    }

    fun startNewClassicGame(countAsNewGame: Boolean = true) {
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
                lastChallengeStarsEarned = 0,
                visualPhase = BoardVisualPhase.Idle,
                dragState = null,
                floatingBanner = null,
                latestParticles = emptyList(),
                statistics = if (countAsNewGame) {
                    it.statistics.copy(totalGamesPlayed = it.statistics.totalGamesPlayed + 1)
                } else {
                    it.statistics
                }
            )
        }
        if (countAsNewGame) {
            viewModelScope.launch { repository.incrementGamesPlayed() }
        }
        persistClassicSnapshot()
    }

    fun continueSavedClassicGame(): Boolean {
        val snapshot = _uiState.value.savedClassicSummary
        if (snapshot != null && !isClassicGameOver(snapshot.board, snapshot.trayPieces)) {
            nextIdCounter = snapshot.nextIdCounter.coerceAtLeast(nextIdCounter)
            _uiState.update {
                it.copy(
                    mode = GameMode.CLASSIC,
                    status = GameStatus.PLAYING,
                    board = snapshot.board,
                    traySlots = snapshot.trayPieces.mapIndexed { i, p -> TraySlot(i, p) },
                    selectedSlotIndex = null,
                    tapHoverAnchor = null,
                    score = snapshot.score,
                    isNewBest = false,
                    movesPlayed = snapshot.movesPlayed,
                    bestCascadeDepth = snapshot.bestCascadeDepth,
                    totalLinesCleared = snapshot.totalLinesCleared,
                    visualPhase = BoardVisualPhase.Idle,
                    dragState = null,
                    floatingBanner = null,
                    latestParticles = emptyList()
                )
            }
            return true
        }
        return false
    }

    fun startChallengeLevel(levelNumber: Int, countAsNewGame: Boolean = true) {
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
                lastChallengeStarsEarned = 0,
                currentChallengeLevel = level,
                visualPhase = BoardVisualPhase.Idle,
                dragState = null,
                floatingBanner = null,
                latestParticles = emptyList(),
                statistics = if (countAsNewGame) {
                    it.statistics.copy(totalGamesPlayed = it.statistics.totalGamesPlayed + 1)
                } else {
                    it.statistics
                }
            )
        }
        if (countAsNewGame) {
            viewModelScope.launch { repository.incrementGamesPlayed() }
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
                                savedClassicSummary = saved,
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

    fun exitToHome() {
        val state = _uiState.value
        if (state.mode == GameMode.CLASSIC && state.status != GameStatus.GAME_OVER) {
            persistClassicSnapshot()
        }
        _uiState.update {
            it.copy(
                dragState = null,
                selectedSlotIndex = null,
                status = if (it.status == GameStatus.PAUSED) GameStatus.PLAYING else it.status
            )
        }
    }

    fun toggleRulesDialog(show: Boolean) {
        _uiState.update { it.copy(showRulesDialog = show) }
    }

    fun toggleHaptics(enabled: Boolean) {
        updateSettings { it.copy(hapticsEnabled = enabled) }
    }

    fun toggleSound(enabled: Boolean) {
        soundManager.soundEnabled = enabled
        updateSettings { it.copy(soundEnabled = enabled) }
    }

    fun toggleMusic(enabled: Boolean) {
        soundManager.musicEnabled = enabled
        updateSettings { it.copy(musicEnabled = enabled) }
    }

    fun toggleVisualEffects(enabled: Boolean) {
        updateSettings { it.copy(visualEffectsEnabled = enabled) }
    }

    fun togglePlacementPreview(enabled: Boolean) {
        updateSettings { it.copy(placementPreviewEnabled = enabled) }
    }

    fun setBlockStyle(blockStyle: BlockStyle) {
        updateSettings { it.copy(blockStyle = blockStyle) }
    }

    fun setBoardPalette(boardPalette: BoardColorPalette) {
        updateSettings { it.copy(boardPalette = boardPalette) }
    }

    private fun updateSettings(transform: (GameSettings) -> GameSettings) {
        val updated = transform(_uiState.value.settings)
        _uiState.update {
            it.copy(
                hapticsEnabled = updated.hapticsEnabled,
                settings = updated
            )
        }
        viewModelScope.launch { repository.updateSettings(updated) }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
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
                    classicBestScore = 0,
                    isNewBest = false,
                    movesPlayed = 0,
                    totalLinesCleared = 0,
                    currentCascadeWave = 0,
                    bestCascadeDepth = 0,
                    currentChallengeLevel = ChallengeLevels.getLevel(1),
                    unlockedChallengeLevel = 1,
                    statistics = GameStatistics(),
                    challengeProgress = emptyMap(),
                    lastChallengeStarsEarned = 0,
                    savedClassicSummary = null,
                    visualPhase = BoardVisualPhase.Idle,
                    dragState = null,
                    floatingBanner = null,
                    latestParticles = emptyList()
                )
            }
        }
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

        // Lift placement above the touched row so the user's finger does not obscure the piece
        val liftRows = 2
        val maxAnchorRow = (Board.SIZE - piece.shape.heightCells).coerceAtLeast(0)
        val maxAnchorCol = (Board.SIZE - piece.shape.widthCells).coerceAtLeast(0)
        val liftedBottomRow = (row - liftRows).coerceAtLeast(piece.shape.heightCells - 1)
        val anchorRow = (liftedBottomRow - (piece.shape.heightCells - 1)).coerceIn(0, maxAnchorRow)
        val anchorCol = (col - piece.shape.widthCells / 2).coerceIn(0, maxAnchorCol)

        if (PlacementValidator.canPlace(state.board, piece, anchorRow, anchorCol)) {
            placePiece(slotIdx, anchorRow, anchorCol)
        } else if (PlacementValidator.canPlace(state.board, piece, row, col)) {
            placePiece(slotIdx, row, col)
        }
    }

    fun startBoardTouch(localOffset: Offset) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return
        val slotIdx = state.selectedSlotIndex ?: return
        val piece = state.traySlots.getOrNull(slotIdx)?.piece ?: return
        val pointerRootOffset = boardBoundsInRoot.topLeft + localOffset
        _uiState.update {
            it.copy(
                dragState = computeDragState(
                    board = state.board,
                    slotIndex = slotIdx,
                    piece = piece,
                    pointerRootOffset = pointerRootOffset,
                    clampToBoard = true
                )
            )
        }
    }

    fun updateBoardTouch(localOffset: Offset) {
        val state = _uiState.value
        val activeDrag = state.dragState ?: return
        if (state.status != GameStatus.PLAYING) return
        val pointerRootOffset = boardBoundsInRoot.topLeft + localOffset
        _uiState.update {
            it.copy(
                dragState = computeDragState(
                    board = state.board,
                    slotIndex = activeDrag.slotIndex,
                    piece = activeDrag.piece,
                    pointerRootOffset = pointerRootOffset,
                    clampToBoard = true
                )
            )
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
        pointerRootOffset: Offset,
        clampToBoard: Boolean = false
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

        // Keep the bottom edge of the dragged/touched piece lifted comfortably above the finger
        val topLeftX = pointerRootOffset.x - pieceW / 2f
        val topLeftY = pointerRootOffset.y - dragLiftPx - pieceH

        val rawCol = ((topLeftX - boardBoundsInRoot.left) / cellSize).roundToInt()
        val rawRow = ((topLeftY - boardBoundsInRoot.top) / cellSize).roundToInt()

        if (!clampToBoard) {
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
        }

        val anchor = if (clampToBoard) {
            val maxRow = (Board.SIZE - piece.shape.heightCells).coerceAtLeast(0)
            val maxCol = (Board.SIZE - piece.shape.widthCells).coerceAtLeast(0)
            GridCoord(rawRow.coerceIn(0, maxRow), rawCol.coerceIn(0, maxCol))
        } else {
            GridCoord(rawRow, rawCol)
        }
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

        soundManager.playBlockDrop()

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
                blocksPlaced = piece.shape.cellCount,
                linesAdded = 0,
                maxDepthInTurn = 0
            )
        } else {
            viewModelScope.launch {
                runCascadeAnimation(
                    resolution = resolution,
                    startingScore = initialScore,
                    trayAfterPlacement = updatedTray,
                    movesPlayed = nextMovesPlayed,
                    blocksPlaced = piece.shape.cellCount
                )
            }
        }
    }

    private suspend fun runCascadeAnimation(
        resolution: com.example.game.CascadeResolution,
        startingScore: Int,
        trayAfterPlacement: List<TraySlot>,
        movesPlayed: Int,
        blocksPlaced: Int
    ) {
        var runningScore = startingScore
        var totalLinesInTurn = 0

        for (wave in resolution.waves) {
            val waveLines = wave.clearedRows.size + wave.clearedCols.size
            totalLinesInTurn += waveLines

            if (wave.waveIndex >= 2) {
                soundManager.playComboComplete(comboDepth = wave.waveIndex)
            } else {
                soundManager.playLineClear(linesCleared = waveLines)
            }

            val banner = buildBannerForWave(
                waveIndex = wave.waveIndex,
                linesCount = waveLines,
                points = wave.scoreBreakdown.totalPoints,
                isFullClear = wave.isFullClear
            )

            val showParticles = _uiState.value.settings.visualEffectsEnabled
            val clearParticles = if (showParticles) {
                wave.clearedCoords.mapNotNull { coord ->
                    val color = wave.boardBeforeClear.get(coord.row, coord.col)?.color ?: return@mapNotNull null
                    ParticleBurst(
                        id = particleCounter++,
                        row = coord.row + 0.5f,
                        col = coord.col + 0.5f,
                        color = color,
                        isLandingDust = false
                    )
                }
            } else {
                emptyList()
            }

            _uiState.update {
                it.copy(
                    currentCascadeWave = wave.waveIndex,
                    bestCascadeDepth = maxOf(it.bestCascadeDepth, wave.waveIndex),
                    floatingBanner = banner,
                    particleTriggerId = if (showParticles) particleCounter++ else it.particleTriggerId,
                    latestParticles = clearParticles
                )
            }

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
                val dustParticles = if (_uiState.value.settings.visualEffectsEnabled) {
                    wave.movements.map { move ->
                        ParticleBurst(
                            id = particleCounter++,
                            row = move.toRow + 0.92f,
                            col = move.col + 0.5f,
                            color = move.cell.color,
                            isLandingDust = true
                        )
                    }
                } else {
                    emptyList()
                }

                _uiState.update {
                    it.copy(
                        board = wave.boardAfterGravity,
                        particleTriggerId = if (dustParticles.isNotEmpty()) particleCounter++ else it.particleTriggerId,
                        latestParticles = dustParticles
                    )
                }
                soundManager.playBlockDrop(volume = 0.5f, rate = 0.88f)

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

        if (resolution.maxCascadeDepth >= 2) {
            soundManager.playComboComplete(comboDepth = resolution.maxCascadeDepth)
        }

        val lastBannerId = resolution.waves.lastOrNull()?.let { _uiState.value.floatingBanner?.id }

        finalizeTurn(
            finalBoard = resolution.finalBoard,
            trayAfterPlacement = trayAfterPlacement,
            scoreAfterTurn = runningScore,
            movesPlayed = movesPlayed,
            blocksPlaced = blocksPlaced,
            linesAdded = totalLinesInTurn,
            maxDepthInTurn = resolution.maxCascadeDepth
        )

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
        blocksPlaced: Int,
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

        if (nextStatus == GameStatus.CHALLENGE_WON) {
            soundManager.playLevelComplete()
        } else if (nextStatus == GameStatus.GAME_OVER || nextStatus == GameStatus.CHALLENGE_LOST) {
            soundManager.playGameOver()
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

        val earnedStars = if (state.mode == GameMode.CHALLENGE && nextStatus == GameStatus.CHALLENGE_WON) {
            calculateChallengeStars(
                score = scoreAfterTurn,
                targetScore = state.currentChallengeLevel.targetScore,
                movesRemaining = (state.currentChallengeLevel.moveLimit - movesPlayed).coerceAtLeast(0)
            )
        } else {
            0
        }

        val cascadesInTurn = if (maxDepthInTurn >= 2) 1 else 0
        val updatedChallengeMap = if (state.mode == GameMode.CHALLENGE && nextStatus == GameStatus.CHALLENGE_WON) {
            val lvlNum = state.currentChallengeLevel.levelNumber
            val prev = state.challengeProgress[lvlNum]
            state.challengeProgress + (
                lvlNum to ChallengeLevelProgress(
                    levelNumber = lvlNum,
                    bestScore = maxOf(prev?.bestScore ?: 0, scoreAfterTurn),
                    stars = maxOf(prev?.stars ?: 0, earnedStars),
                    completed = true
                )
            )
        } else if (state.mode == GameMode.CHALLENGE && nextStatus == GameStatus.CHALLENGE_LOST) {
            val lvlNum = state.currentChallengeLevel.levelNumber
            val prev = state.challengeProgress[lvlNum]
            if (scoreAfterTurn > (prev?.bestScore ?: 0)) {
                state.challengeProgress + (
                    lvlNum to ChallengeLevelProgress(
                        levelNumber = lvlNum,
                        bestScore = scoreAfterTurn,
                        stars = prev?.stars ?: 0,
                        completed = prev?.completed == true
                    )
                )
            } else {
                state.challengeProgress
            }
        } else {
            state.challengeProgress
        }

        val updatedStats = state.statistics.copy(
            totalBlocksPlaced = state.statistics.totalBlocksPlaced + blocksPlaced,
            totalLinesCleared = state.statistics.totalLinesCleared + linesAdded,
            totalCascadesTriggered = state.statistics.totalCascadesTriggered + cascadesInTurn,
            maxCascadeDepth = maxOf(state.statistics.maxCascadeDepth, maxDepthInTurn),
            classicBestScore = maxOf(
                state.statistics.classicBestScore,
                if (state.mode == GameMode.CLASSIC) scoreAfterTurn else 0
            )
        )

        _uiState.update {
            it.copy(
                board = finalBoard,
                traySlots = nextTraySlots,
                status = nextStatus,
                unlockedChallengeLevel = nextUnlockedLevel,
                bestCascadeDepth = maxOf(it.bestCascadeDepth, maxDepthInTurn),
                lastChallengeStarsEarned = earnedStars,
                challengeProgress = updatedChallengeMap,
                statistics = updatedStats
            )
        }

        viewModelScope.launch {
            repository.recordTurnStatistics(
                blocksPlaced = blocksPlaced,
                linesCleared = linesAdded,
                cascadesTriggered = cascadesInTurn,
                maxDepthInTurn = maxDepthInTurn
            )

            if (state.mode == GameMode.CLASSIC) {
                repository.updateClassicBestScore(scoreAfterTurn)
                if (nextStatus == GameStatus.GAME_OVER) {
                    _uiState.update { it.copy(savedClassicSummary = null) }
                    repository.saveClassicGame(null)
                } else {
                    persistClassicSnapshot()
                }
            } else if (nextStatus == GameStatus.CHALLENGE_WON) {
                repository.saveChallengeLevelResult(
                    levelNumber = state.currentChallengeLevel.levelNumber,
                    score = scoreAfterTurn,
                    stars = earnedStars,
                    completed = true,
                    nextUnlockedLevel = nextUnlockedLevel
                )
            } else if (nextStatus == GameStatus.CHALLENGE_LOST) {
                repository.saveChallengeLevelResult(
                    levelNumber = state.currentChallengeLevel.levelNumber,
                    score = scoreAfterTurn,
                    stars = 0,
                    completed = false,
                    nextUnlockedLevel = nextUnlockedLevel
                )
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
        _uiState.update { it.copy(savedClassicSummary = snapshot) }
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

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }

    companion object {
        fun calculateChallengeStars(score: Int, targetScore: Int, movesRemaining: Int): Int {
            if (score < targetScore) return 0
            return when {
                score >= (targetScore * 1.5f).toInt() || movesRemaining >= 5 -> 3
                score >= (targetScore * 1.25f).toInt() || movesRemaining >= 3 -> 2
                else -> 1
            }
        }
    }
}
