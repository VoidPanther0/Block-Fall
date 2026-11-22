package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdState
import com.example.ads.RewardedAdManager
import com.example.audio.SoundManager
import com.example.data.GameRepository
import com.example.game.AchievementEvaluator
import com.example.game.BlockColor
import com.example.game.BlockStyle
import com.example.game.Board
import com.example.game.BoardColorPalette
import com.example.game.BoosterType
import com.example.game.ChallengeLevel
import com.example.game.ChallengeLevelProgress
import com.example.game.ChallengeLevels
import com.example.game.EconomyEngine
import com.example.game.EconomyMutationResult
import com.example.game.FallingBlockMove
import com.example.game.GameMode
import com.example.game.GamePiece
import com.example.game.GameSessionEngine
import com.example.game.GameSettings
import com.example.game.GameStatistics
import com.example.game.GameStatus
import com.example.game.GridCoord
import com.example.game.PieceGenerator
import com.example.game.PlayerRankCalculator
import com.example.game.PlayerRankInfo
import com.example.game.PlayerWallet
import com.example.game.SavedClassicGame
import com.example.game.TraySlot
import com.example.game.TurnExecutionResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
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
    val unlockedAchievements: Set<String> = emptySet(),
    val playerRank: PlayerRankInfo = PlayerRankCalculator.compute(
        GameStatistics(),
        emptyMap(),
        emptySet()
    ),
    val lastChallengeStarsEarned: Int = 0,
    val savedClassicSummary: SavedClassicGame? = null,
    val visualPhase: BoardVisualPhase = BoardVisualPhase.Idle,
    val dragState: DragState? = null,
    val floatingBanner: FloatingBanner? = null,
    val particleTriggerId: Long = 0L,
    val latestParticles: List<ParticleBurst> = emptyList(),
    val wallet: PlayerWallet = PlayerWallet(),
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
    private val sessionEngine = GameSessionEngine(pieceGenerator)
    private val dragController = DragController()
    val soundManager = SoundManager(application.applicationContext)
    private val animationRunner = CascadeAnimationRunner(soundManager)
    private val adManager by lazy { RewardedAdManager(getApplication<Application>()) }

    val adState: StateFlow<AdState> = adManager.adState

    private var nextIdCounter: Long = 100L

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _visualPhase = MutableStateFlow<BoardVisualPhase>(BoardVisualPhase.Idle)
    val visualPhase: StateFlow<BoardVisualPhase> = _visualPhase.asStateFlow()

    init {
        adManager.loadAd()
        viewModelScope.launch {
            val saved = repository.loadInitialProgress()
            soundManager.soundEnabled = saved.settings.soundEnabled
            soundManager.musicEnabled = saved.settings.musicEnabled

            val restored = saved.savedClassicGame
            val validSaved = if (restored != null && !GameSessionEngine.isClassicGameOver(restored.board, restored.trayPieces)) {
                restored
            } else {
                null
            }

            if (validSaved != null) {
                nextIdCounter = validSaved.nextIdCounter.coerceAtLeast(100L)
                val mergedStats = saved.statistics.copy(
                    classicBestScore = maxOf(saved.classicBestScore, validSaved.score)
                )
                val (achievements, rank) = computeProgression(mergedStats, saved.challengeProgress)
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
                        statistics = mergedStats,
                        challengeProgress = saved.challengeProgress,
                        unlockedAchievements = achievements,
                        playerRank = rank,
                        wallet = saved.wallet,
                        savedClassicSummary = validSaved
                    )
                }
            } else {
                val emptyBoard = Board.empty()
                val (trayPieces, updatedId) = pieceGenerator.generateTray(emptyBoard, nextIdCounter)
                nextIdCounter = updatedId
                val (achievements, rank) = computeProgression(saved.statistics, saved.challengeProgress)
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
                        unlockedAchievements = achievements,
                        playerRank = rank,
                        wallet = saved.wallet,
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
        dragController.updateBoardBounds(rect, liftPx)
    }

    fun startNewClassicGame(countAsNewGame: Boolean = true) {
        val emptyBoard = Board.empty()
        val (trayPieces, updatedId) = pieceGenerator.generateTray(emptyBoard, nextIdCounter)
        nextIdCounter = updatedId

        _visualPhase.value = BoardVisualPhase.Idle
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
        if (snapshot != null && !GameSessionEngine.isClassicGameOver(snapshot.board, snapshot.trayPieces)) {
            nextIdCounter = snapshot.nextIdCounter.coerceAtLeast(nextIdCounter)
            _visualPhase.value = BoardVisualPhase.Idle
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

        _visualPhase.value = BoardVisualPhase.Idle
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
                    if (saved != null && !GameSessionEngine.isClassicGameOver(saved.board, saved.trayPieces)) {
                        nextIdCounter = saved.nextIdCounter.coerceAtLeast(nextIdCounter)
                        _visualPhase.value = BoardVisualPhase.Idle
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
            GameMode.CHALLENGE -> startChallengeLevel(_uiState.value.unlockedChallengeLevel)
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
        _uiState.update { it.copy(hapticsEnabled = updated.hapticsEnabled, settings = updated) }
        viewModelScope.launch { repository.updateSettings(updated) }
    }

    fun resetAllProgress() {
        viewModelScope.launch {
            repository.resetAllProgress()
            val emptyBoard = Board.empty()
            val (trayPieces, updatedId) = pieceGenerator.generateTray(emptyBoard, nextIdCounter)
            nextIdCounter = updatedId
            _visualPhase.value = BoardVisualPhase.Idle
            val emptyStats = GameStatistics()
            val (achievements, rank) = computeProgression(emptyStats, emptyMap())
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
                    statistics = emptyStats,
                    challengeProgress = emptyMap(),
                    unlockedAchievements = achievements,
                    playerRank = rank,
                    lastChallengeStarsEarned = 0,
                    savedClassicSummary = null,
                    wallet = PlayerWallet(),
                    visualPhase = BoardVisualPhase.Idle,
                    dragState = null,
                    floatingBanner = null,
                    latestParticles = emptyList()
                )
            }
        }
    }

    fun purchaseShopItem(itemId: String) {
        viewModelScope.launch {
            val result = EconomyEngine.purchaseShopItem(
                wallet = _uiState.value.wallet,
                itemId = itemId,
                transactionId = UUID.randomUUID().toString()
            )
            if (result is EconomyMutationResult.Success) {
                applyWalletMutation(result.updatedWallet)
            }
            // Rejected mutations intentionally leave the wallet untouched.
        }
    }

    fun consumeBooster(type: BoosterType) {
        viewModelScope.launch {
            val result = EconomyEngine.consumeBooster(
                wallet = _uiState.value.wallet,
                type = type,
                transactionId = UUID.randomUUID().toString()
            )
            if (result is EconomyMutationResult.Success) {
                applyWalletMutation(result.updatedWallet)
            }
        }
    }

    fun claimAdReward(goldAmount: Int, transactionId: String) {
        viewModelScope.launch {
            val result = EconomyEngine.creditReward(
                wallet = _uiState.value.wallet,
                goldDelta = goldAmount,
                diamondDelta = 0,
                transactionId = transactionId,
                reason = "Rewarded ad"
            )
            if (result is EconomyMutationResult.Success) {
                applyWalletMutation(result.updatedWallet)
            }
        }
    }

    fun watchRewardedAd(activity: Activity) {
        adManager.showAd(
            activity = activity,
            onRewarded = {
                claimAdReward(
                    goldAmount = AD_REWARD_GOLD,
                    transactionId = UUID.randomUUID().toString()
                )
            },
            onDismissed = { /* Ad closed without or after reward */ },
            onFailed = { /* Ad unavailable — button stays disabled until next load */ }
        )
    }

    private suspend fun applyWalletMutation(wallet: PlayerWallet) {
        _uiState.update { it.copy(wallet = wallet) }
        repository.updateWallet(wallet)
    }

    fun selectTraySlot(slotIndex: Int) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return
        if (state.traySlots.getOrNull(slotIndex)?.piece == null) return
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
        val anchor = dragController.computeTapPlacementAnchor(state.board, piece, row, col) ?: return
        placePiece(slotIdx, anchor.row, anchor.col)
    }

    fun startBoardTouch(localOffset: Offset) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return
        val slotIdx = state.selectedSlotIndex ?: return
        val piece = state.traySlots.getOrNull(slotIdx)?.piece ?: return
        val rootOffset = dragController.rootOffsetForLocalBoardTouch(localOffset)
        _uiState.update {
            it.copy(
                dragState = dragController.computeDragState(state.board, slotIdx, piece, rootOffset, clampToBoard = true)
            )
        }
    }

    fun updateBoardTouch(localOffset: Offset) {
        val state = _uiState.value
        val activeDrag = state.dragState ?: return
        if (state.status != GameStatus.PLAYING) return
        val rootOffset = dragController.rootOffsetForLocalBoardTouch(localOffset)
        _uiState.update {
            it.copy(
                dragState = dragController.computeDragState(
                    state.board, activeDrag.slotIndex, activeDrag.piece, rootOffset, clampToBoard = true
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
                dragState = dragController.computeDragState(state.board, slotIndex, piece, pointerRootOffset)
            )
        }
    }

    fun updateDrag(pointerRootOffset: Offset) {
        val state = _uiState.value
        val activeDrag = state.dragState ?: return
        if (state.status != GameStatus.PLAYING) return
        _uiState.update {
            it.copy(
                dragState = dragController.computeDragState(
                    state.board, activeDrag.slotIndex, activeDrag.piece, pointerRootOffset
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

    fun placePiece(slotIndex: Int, anchorRow: Int, anchorCol: Int) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return

        val turn = sessionEngine.executeTurn(
            board = state.board,
            traySlots = state.traySlots,
            slotIndex = slotIndex,
            anchorRow = anchorRow,
            anchorCol = anchorCol,
            currentScore = state.score,
            movesPlayed = state.movesPlayed,
            mode = state.mode,
            challengeLevel = state.currentChallengeLevel,
            startingBlockId = nextIdCounter
        ) ?: return
        nextIdCounter = turn.nextBlockIdCounter

        soundManager.playBlockDrop()

        val initialScore = turn.scoreAfterPlacement
        val newClassicBest = if (state.mode == GameMode.CLASSIC && initialScore > state.classicBestScore) {
            initialScore
        } else {
            state.classicBestScore
        }
        val becameNewBest = state.mode == GameMode.CLASSIC &&
            (state.isNewBest || initialScore > state.classicBestScore)

        _uiState.update {
            it.copy(
                board = turn.placement.board,
                traySlots = turn.updatedTray,
                selectedSlotIndex = null,
                tapHoverAnchor = null,
                dragState = null,
                score = initialScore,
                classicBestScore = newClassicBest,
                isNewBest = becameNewBest,
                movesPlayed = turn.nextMovesPlayed,
                currentCascadeWave = 0,
                status = if (turn.resolution.hasClears) GameStatus.RESOLVING_CASCADE else it.status
            )
        }

        val blocksPlaced = turn.placement.placedCoords.size
        if (!turn.resolution.hasClears) {
            applyTurnFinalization(turn, blocksPlaced)
        } else {
            viewModelScope.launch {
                animationRunner.runCascade(
                    resolution = turn.resolution,
                    startingScore = initialScore,
                    visualEffectsEnabled = _uiState.value.settings.visualEffectsEnabled,
                    onVisualPhase = { phase -> _visualPhase.value = phase },
                    onWaveStarted = { waveIndex, banner, particleTrigger, particles ->
                        _uiState.update {
                            it.copy(
                                currentCascadeWave = waveIndex,
                                bestCascadeDepth = maxOf(it.bestCascadeDepth, waveIndex),
                                floatingBanner = banner,
                                particleTriggerId = particleTrigger ?: it.particleTriggerId,
                                latestParticles = particles
                            )
                        }
                    },
                    onWaveCleared = { boardAfterClear, runningScore, waveLines ->
                        _uiState.update { s ->
                            val updatedBest = if (s.mode == GameMode.CLASSIC && runningScore > s.classicBestScore) {
                                runningScore
                            } else {
                                s.classicBestScore
                            }
                            s.copy(
                                board = boardAfterClear,
                                score = runningScore,
                                classicBestScore = updatedBest,
                                isNewBest = s.mode == GameMode.CLASSIC && (s.isNewBest || runningScore > s.classicBestScore),
                                totalLinesCleared = s.totalLinesCleared + waveLines
                            )
                        }
                    },
                    onGravityLanded = { boardAfterGravity, particleTrigger, dustParticles ->
                        _uiState.update {
                            it.copy(
                                board = boardAfterGravity,
                                particleTriggerId = particleTrigger ?: it.particleTriggerId,
                                latestParticles = dustParticles
                            )
                        }
                    }
                )

                val lastBannerId = _uiState.value.floatingBanner?.id
                applyTurnFinalization(turn, blocksPlaced)

                if (lastBannerId != null) {
                    animationRunner.scheduleBannerDismiss(lastBannerId) { id ->
                        _uiState.update { cur ->
                            if (cur.floatingBanner?.id == id) cur.copy(floatingBanner = null) else cur
                        }
                    }
                }
            }
        }
    }

    private fun applyTurnFinalization(turn: TurnExecutionResult, blocksPlaced: Int) {
        val state = _uiState.value
        val nextStatus = turn.nextStatus

        if (nextStatus == GameStatus.CHALLENGE_WON) {
            soundManager.playLevelComplete()
        } else if (nextStatus == GameStatus.GAME_OVER || nextStatus == GameStatus.CHALLENGE_LOST) {
            soundManager.playGameOver()
        }

        val nextUnlockedLevel = if (
            state.mode == GameMode.CHALLENGE && nextStatus == GameStatus.CHALLENGE_WON
        ) {
            maxOf(
                state.unlockedChallengeLevel,
                (state.currentChallengeLevel.levelNumber + 1).coerceAtMost(ChallengeLevels.levels.size)
            )
        } else {
            state.unlockedChallengeLevel
        }

        val maxDepthInTurn = turn.resolution.maxCascadeDepth
        val cascadesInTurn = if (maxDepthInTurn >= 2) 1 else 0
        val fullClearAdded = if (turn.fullClearOccurred) 1 else 0

        val updatedChallengeMap = if (state.mode == GameMode.CHALLENGE && nextStatus == GameStatus.CHALLENGE_WON) {
            val lvlNum = state.currentChallengeLevel.levelNumber
            val prev = state.challengeProgress[lvlNum]
            state.challengeProgress + (
                lvlNum to ChallengeLevelProgress(
                    levelNumber = lvlNum,
                    bestScore = maxOf(prev?.bestScore ?: 0, turn.finalScore),
                    stars = maxOf(prev?.stars ?: 0, turn.earnedChallengeStars),
                    completed = true
                )
            )
        } else if (state.mode == GameMode.CHALLENGE && nextStatus == GameStatus.CHALLENGE_LOST) {
            val lvlNum = state.currentChallengeLevel.levelNumber
            val prev = state.challengeProgress[lvlNum]
            if (turn.finalScore > (prev?.bestScore ?: 0)) {
                state.challengeProgress + (
                    lvlNum to ChallengeLevelProgress(
                        levelNumber = lvlNum,
                        bestScore = turn.finalScore,
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
            totalLinesCleared = state.statistics.totalLinesCleared + turn.totalLinesInTurn,
            totalCascadesTriggered = state.statistics.totalCascadesTriggered + cascadesInTurn,
            maxCascadeDepth = maxOf(state.statistics.maxCascadeDepth, maxDepthInTurn),
            fullBoardClears = state.statistics.fullBoardClears + fullClearAdded,
            classicBestScore = maxOf(
                state.statistics.classicBestScore,
                if (state.mode == GameMode.CLASSIC) turn.finalScore else 0
            )
        )
        val (achievements, rank) = computeProgression(updatedStats, updatedChallengeMap, state.unlockedAchievements)

        _visualPhase.value = BoardVisualPhase.Idle
        _uiState.update {
            it.copy(
                board = turn.resolution.finalBoard,
                traySlots = turn.nextTraySlots,
                status = nextStatus,
                visualPhase = BoardVisualPhase.Idle,
                currentCascadeWave = 0,
                unlockedChallengeLevel = nextUnlockedLevel,
                bestCascadeDepth = maxOf(it.bestCascadeDepth, maxDepthInTurn),
                lastChallengeStarsEarned = turn.earnedChallengeStars,
                challengeProgress = updatedChallengeMap,
                statistics = updatedStats,
                unlockedAchievements = achievements,
                playerRank = rank
            )
        }

        viewModelScope.launch {
            repository.recordTurnStatistics(
                blocksPlaced = blocksPlaced,
                linesCleared = turn.totalLinesInTurn,
                cascadesTriggered = cascadesInTurn,
                maxDepthInTurn = maxDepthInTurn,
                fullClearAdded = fullClearAdded
            )
            if (state.mode == GameMode.CLASSIC) {
                repository.updateClassicBestScore(turn.finalScore)
                if (nextStatus == GameStatus.GAME_OVER) {
                    _uiState.update { it.copy(savedClassicSummary = null) }
                    repository.saveClassicGame(null)
                } else {
                    persistClassicSnapshot()
                }
            } else if (nextStatus == GameStatus.CHALLENGE_WON || nextStatus == GameStatus.CHALLENGE_LOST) {
                repository.saveChallengeLevelResult(
                    levelNumber = state.currentChallengeLevel.levelNumber,
                    score = turn.finalScore,
                    stars = turn.earnedChallengeStars,
                    completed = nextStatus == GameStatus.CHALLENGE_WON,
                    nextUnlockedLevel = nextUnlockedLevel
                )
            }
        }
    }

    private fun computeProgression(
        stats: GameStatistics,
        challengeProgress: Map<Int, ChallengeLevelProgress>,
        existing: Set<String> = emptySet()
    ): Pair<Set<String>, PlayerRankInfo> {
        val unlocked = AchievementEvaluator.evaluateUnlocked(existing, stats, challengeProgress)
        val rank = PlayerRankCalculator.compute(stats, challengeProgress, unlocked)
        return unlocked to rank
    }

    fun applyCloudMerge(
        cloudProfile: com.example.data.cloud.UserCloudProfile?,
        cloudChallenges: List<com.example.data.cloud.ChallengeProgressDoc>
    ) {
        if (cloudProfile == null && cloudChallenges.isEmpty()) return
        viewModelScope.launch {
            val challengeMap = cloudChallenges.associate { doc ->
                val lvl = doc.levelNumber.toInt().coerceIn(1, 50)
                lvl to ChallengeLevelProgress(
                    levelNumber = lvl,
                    bestScore = doc.bestScore.toInt().coerceAtLeast(0),
                    stars = doc.stars.toInt().coerceIn(0, 3),
                    completed = doc.completed
                )
            }
            val merged = repository.mergeCloudProgress(
                cloudBestScore = cloudProfile?.classicBestScore?.toInt() ?: 0,
                cloudUnlockedLevel = cloudProfile?.unlockedChallengeLevel?.toInt() ?: 1,
                cloudTotalGames = cloudProfile?.totalGamesPlayed?.toInt() ?: 0,
                cloudTotalLines = cloudProfile?.totalLinesCleared?.toInt() ?: 0,
                cloudTotalCascades = cloudProfile?.totalCascadesTriggered?.toInt() ?: 0,
                cloudMaxDepth = cloudProfile?.maxCascadeDepth?.toInt() ?: 0,
                cloudTotalBlocks = cloudProfile?.totalBlocksPlaced?.toInt() ?: 0,
                cloudFullClears = cloudProfile?.fullBoardClears?.toInt() ?: 0,
                cloudChallenges = challengeMap
            )
            val (achievements, rank) = computeProgression(
                merged.statistics,
                merged.challengeProgress,
                _uiState.value.unlockedAchievements
            )
            _uiState.update { cur ->
                cur.copy(
                    classicBestScore = maxOf(cur.classicBestScore, merged.classicBestScore),
                    unlockedChallengeLevel = maxOf(cur.unlockedChallengeLevel, merged.unlockedChallengeLevel),
                    statistics = merged.statistics,
                    challengeProgress = merged.challengeProgress,
                    unlockedAchievements = achievements,
                    playerRank = rank
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

    override fun onCleared() {
        super.onCleared()
        soundManager.release()
    }

    companion object {
        const val AD_REWARD_GOLD = 50

        fun calculateChallengeStars(score: Int, targetScore: Int, movesRemaining: Int): Int =
            GameSessionEngine.calculateChallengeStars(score, targetScore, movesRemaining)
    }
}
