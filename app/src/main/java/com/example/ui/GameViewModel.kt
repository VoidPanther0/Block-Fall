package com.example.ui

import android.app.Activity
import android.app.Application
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ads.AdConfig
import com.example.ads.AdState
import com.example.ads.RewardedAdManager
import com.example.audio.SoundManager
import com.example.data.GameRepository
import com.example.game.AchievementsCatalog
import com.example.game.BlockColor
import com.example.game.BlockStyle
import com.example.game.Board
import com.example.game.BoardColorPalette
import com.example.game.BoosterType
import com.example.game.CascadeResolver
import com.example.game.ChallengeLevel
import com.example.game.ChallengeLevelProgress
import com.example.game.ChallengeLevels
import com.example.game.DailyChallengeGenerator
import com.example.game.DailyMissionSnapshot
import com.example.game.DailyMissionsCatalog
import com.example.game.DailyProgress
import com.example.game.DailyRewardProgress
import com.example.game.DailyRewardsCatalog
import com.example.game.EconomyEngine
import com.example.game.EconomyMutationResult
import com.example.game.FallingBlockMove
import com.example.game.GameMode
import com.example.game.GamePiece
import com.example.game.GameSessionEngine
import com.example.game.GameSettings
import com.example.game.GameStatistics
import com.example.game.GameStatus
import com.example.game.GravityEngine
import com.example.game.GridCoord
import com.example.game.MusicTrack
import com.example.game.PieceGenerator
import com.example.game.PlayerRankCalculator
import com.example.game.PlayerRankInfo
import com.example.game.PlayerWallet
import com.example.game.ProfileAvatar
import com.example.game.SavedClassicGame
import com.example.game.TraySlot
import com.example.game.TurnExecutionResult
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val isChain: Boolean,
    /** Cascade wave (1 = initial clear, 2+ = gravity chain). Single source of truth for combo tier. */
    val depth: Int = 1,
    val linesCount: Int = 1,
    val isFullClear: Boolean = false,
    /** Whole-turn points, set on the final wave only. */
    val turnTotalPoints: Int? = null
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
    val isHammerMode: Boolean = false,
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
    val playerRank: PlayerRankInfo = PlayerRankCalculator.compute(GameStatistics()),
    val lastChallengeStarsEarned: Int = 0,
    val savedClassicSummary: SavedClassicGame? = null,
    val visualPhase: BoardVisualPhase = BoardVisualPhase.Idle,
    val dragState: DragState? = null,
    val floatingBanner: FloatingBanner? = null,
    val particleTriggerId: Long = 0L,
    val latestParticles: List<ParticleBurst> = emptyList(),
    val wallet: PlayerWallet = PlayerWallet(),
    val playerDisplayName: String? = null,
    val profileAvatarId: String = ProfileAvatar.CLASSIC_BLOCK.id,
    val showContinueOffer: Boolean = false,
    val continueCountdownSeconds: Int = GameViewModel.CONTINUE_COUNTDOWN_SECONDS,
    val continueOfferUsedThisRun: Boolean = false,
    val showRulesDialog: Boolean = false,
    /** True while the seeded Daily Gravity puzzle is being played. UI-only routing flag. */
    val isDailyChallenge: Boolean = false,
    val dailyRewardProgress: DailyRewardProgress = DailyRewardProgress(),
    val dailyMissionSnapshot: DailyMissionSnapshot = DailyMissionSnapshot(),
    val claimedAchievementIds: Set<String> = emptySet(),
    val selectedLanguage: String = "English",
    val dailyProgress: DailyProgress = DailyProgress()
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
    val haptics = com.example.audio.HapticsManager(application.applicationContext)
    private val animationRunner = CascadeAnimationRunner(soundManager)
    private val adManager by lazy {
        RewardedAdManager(
            context = getApplication<Application>(),
            adUnitId = AdConfig.rewardedGoldUnitId,
            debugFallbackAdUnitId = null
        )
    }
    private val continueAdManager by lazy {
        RewardedAdManager(
            context = getApplication<Application>(),
            adUnitId = AdConfig.rewardedContinueUnitId,
            debugFallbackAdUnitId = null
        )
    }

    private val diamondAdManager by lazy {
        RewardedAdManager(
            context = getApplication<Application>(),
            adUnitId = AdConfig.rewardedDiamondUnitId,
            debugFallbackAdUnitId = null
        )
    }
    private val wheelAdManager by lazy {
        RewardedAdManager(
            context = getApplication<Application>(),
            adUnitId = AdConfig.rewardedWheelUnitId,
            debugFallbackAdUnitId = null
        )
    }
    private val serverEconomy by lazy { com.example.data.cloud.ServerEconomyClient() }

    val adState: StateFlow<AdState> = adManager.adState
    val diamondAdState: StateFlow<AdState> = diamondAdManager.adState
    val wheelAdState: StateFlow<AdState> = wheelAdManager.adState

    private val _wheelSpinPoints = MutableStateFlow(0)
    val wheelSpinPoints: StateFlow<Int> = _wheelSpinPoints.asStateFlow()
    private val _economyMessage = MutableStateFlow<String?>(null)
    val economyMessage: StateFlow<String?> = _economyMessage.asStateFlow()
    private val _isSpinning = MutableStateFlow(false)
    val isSpinning: StateFlow<Boolean> = _isSpinning.asStateFlow()

    private fun isSignedIn(): Boolean =
        runCatching { com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null }.getOrDefault(false)
    val continueAdState: StateFlow<AdState> = continueAdManager.adState

    private var nextIdCounter: Long = 100L
    private var activeDailyKey: String? = null
    private var comboStreak: Int = 0
    private var continueCountdownJob: Job? = null
    private var cascadeJob: Job? = null
    private var bannerDismissJob: Job? = null
    private var inFlightTurn: TurnExecutionResult? = null
    private var inFlightBlocksPlaced: Int = 0
    private var pendingPause: Boolean = false
    private var shuffleInFlight: Boolean = false
    private var refreshInFlight: Boolean = false
    private var hammerInFlight: Boolean = false
    private var isContinueAdShowing: Boolean = false
    private var adsPreloadJob: Job? = null

    private val wheelPrefs: android.content.SharedPreferences? by lazy {
        runCatching {
            application.getSharedPreferences("lucky_wheel_local", android.content.Context.MODE_PRIVATE)
        }.getOrNull()
    }
    private var serverSpinPoints = 0
    private var pendingServerSpinPoints: Int? = null
    var lastSpinLabel: String = ""
        private set

    /** Same 10 segments / weights as `WHEEL_SEGMENTS` in functions/index.js. */
    private data class LocalPrize(
        val label: String,
        val gold: Int = 0,
        val diamonds: Int = 0,
        val hammer: Int = 0,
        val shuffle: Int = 0,
        val refresh: Int = 0,
        val weight: Int
    )

    private val localPrizes = listOf(
        LocalPrize("100 Diamonds", diamonds = 100, weight = 1),
        LocalPrize("50 Diamonds", diamonds = 50, weight = 3),
        LocalPrize("10 Diamonds", diamonds = 10, weight = 10),
        LocalPrize("1,000 Gold", gold = 1000, weight = 2),
        LocalPrize("500 Gold", gold = 500, weight = 5),
        LocalPrize("100 Gold", gold = 100, weight = 20),
        LocalPrize("20 Gold", gold = 20, weight = 30),
        LocalPrize("Hammer +1", hammer = 1, weight = 8),
        LocalPrize("Shuffle +1", shuffle = 1, weight = 8),
        LocalPrize("Refresh +1", refresh = 1, weight = 8)
    )
    private var pendingLocalPrize: LocalPrize? = null

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private val _visualPhase = MutableStateFlow<BoardVisualPhase>(BoardVisualPhase.Idle)
    val visualPhase: StateFlow<BoardVisualPhase> = _visualPhase.asStateFlow()

    init {
        publishSpinPoints()
        if (AdConfig.sdkInitialized.value) {
            onAdsSdkReady()
        }
        viewModelScope.launch {
            val saved = runCatching { repository.loadInitialProgress() }
                .getOrElse { com.example.data.PersistedProgress() }
            soundManager.soundEnabled = saved.settings.soundEnabled
            soundManager.musicEnabled = saved.settings.musicEnabled
            soundManager.setMusicTrack(saved.settings.musicTrack)
            haptics.enabled = saved.settings.hapticsEnabled

            val restored = saved.savedClassicGame
            val validSaved = if (restored != null && !GameSessionEngine.isClassicGameOver(restored.board, restored.trayPieces)) {
                restored
            } else {
                null
            }

            val todayKey = DailyChallengeGenerator.todayDateKey()
            val anchoredMissions = saved.dailyMissionSnapshot.anchoredTo(todayKey, saved.statistics)
            if (anchoredMissions != saved.dailyMissionSnapshot) {
                runCatching { repository.updateDailyMissionSnapshot(anchoredMissions) }
            }

            if (validSaved != null) {
                nextIdCounter = validSaved.nextIdCounter.coerceAtLeast(100L)
                val mergedStats = saved.statistics.copy(
                    classicBestScore = maxOf(saved.classicBestScore, validSaved.score)
                )
                val rank = computeProgression(mergedStats)
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
                        playerRank = rank,
                        wallet = saved.wallet,
                        playerDisplayName = saved.playerDisplayName,
                        profileAvatarId = saved.profileAvatarId,
                        savedClassicSummary = validSaved,
                        dailyRewardProgress = saved.dailyRewardProgress,
                        dailyMissionSnapshot = anchoredMissions,
                        claimedAchievementIds = saved.claimedAchievementIds,
                        selectedLanguage = saved.selectedLanguage,
                        dailyProgress = saved.dailyProgress.forDate(todayKey)
                    )
                }
            } else {
                val emptyBoard = Board.empty()
                val (trayPieces, updatedId) = pieceGenerator.generateTray(emptyBoard, nextIdCounter)
                nextIdCounter = updatedId
                val rank = computeProgression(saved.statistics)
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
                        playerRank = rank,
                        wallet = saved.wallet,
                        playerDisplayName = saved.playerDisplayName,
                        profileAvatarId = saved.profileAvatarId,
                        savedClassicSummary = null,
                        dailyRewardProgress = saved.dailyRewardProgress,
                        dailyMissionSnapshot = anchoredMissions,
                        claimedAchievementIds = saved.claimedAchievementIds,
                        selectedLanguage = saved.selectedLanguage,
                        dailyProgress = saved.dailyProgress.forDate(todayKey)
                    )
                }
            }
        }
    }

    fun setAppForeground(inForeground: Boolean) {
        soundManager.setAppForeground(inForeground)
        adManager.setAppForeground(inForeground)
        continueAdManager.setAppForeground(inForeground)
        diamondAdManager.setAppForeground(inForeground)
        wheelAdManager.setAppForeground(inForeground)
    }

    fun playUiClick() {
        soundManager.playUiClick()
        haptics.perform(com.example.audio.HapticEvent.TAP)
    }

    fun updateBoardBounds(rect: Rect, liftPx: Float) {
        dragController.updateBoardBounds(rect, liftPx)
    }

    fun startNewClassicGame(countAsNewGame: Boolean = true) {
        cancelContinueCountdown()
        cancelCascade()
        pieceGenerator.reseed(null)
        activeDailyKey = null
        comboStreak = 0
        val emptyBoard = Board.empty()
        val (trayPieces, updatedId) = pieceGenerator.generateTray(emptyBoard, nextIdCounter)
        nextIdCounter = updatedId

        _visualPhase.value = BoardVisualPhase.Idle
        _uiState.update {
            it.copy(
                mode = GameMode.CLASSIC,
                isDailyChallenge = false,
                status = GameStatus.PLAYING,
                board = emptyBoard,
                traySlots = trayPieces.mapIndexed { i, p -> TraySlot(i, p) },
                selectedSlotIndex = null,
                isHammerMode = false,
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
                showContinueOffer = false,
                continueCountdownSeconds = CONTINUE_COUNTDOWN_SECONDS,
                continueOfferUsedThisRun = false,
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
        cancelContinueCountdown()
        cancelCascade()
        val snapshot = _uiState.value.savedClassicSummary
        if (snapshot != null && !GameSessionEngine.isClassicGameOver(snapshot.board, snapshot.trayPieces)) {
            nextIdCounter = snapshot.nextIdCounter.coerceAtLeast(nextIdCounter)
            _visualPhase.value = BoardVisualPhase.Idle
            _uiState.update {
                it.copy(
                    mode = GameMode.CLASSIC,
                    isDailyChallenge = false,
                    status = GameStatus.PLAYING,
                    board = snapshot.board,
                    traySlots = snapshot.trayPieces.mapIndexed { i, p -> TraySlot(i, p) },
                    selectedSlotIndex = null,
                    isHammerMode = false,
                    tapHoverAnchor = null,
                    score = snapshot.score,
                    isNewBest = false,
                    movesPlayed = snapshot.movesPlayed,
                    bestCascadeDepth = snapshot.bestCascadeDepth,
                    totalLinesCleared = snapshot.totalLinesCleared,
                    visualPhase = BoardVisualPhase.Idle,
                    dragState = null,
                    floatingBanner = null,
                    latestParticles = emptyList(),
                    showContinueOffer = false,
                    continueCountdownSeconds = CONTINUE_COUNTDOWN_SECONDS,
                    continueOfferUsedThisRun = false
                )
            }
            return true
        }
        return false
    }

    /** Entry point used by the Home screen; delegates to the seeded, persisted Daily Gravity flow. */
    fun startDailyChallenge(@Suppress("UNUSED_PARAMETER") countAsNewGame: Boolean = true) = startDailyGravity()

    /** Daily Gravity: today's seeded puzzle (same board and piece stream for every player). */
    fun startDailyGravity() {
        val todayKey = DailyChallengeGenerator.todayDateKey()
        val spec = DailyChallengeGenerator.forDateKey(todayKey)
        pieceGenerator.reseed(spec.seed)
        activeDailyKey = todayKey
        comboStreak = 0
        _uiState.update { it.copy(dailyProgress = it.dailyProgress.withAttemptStarted(todayKey)) }
        viewModelScope.launch {
            repository.updateDailyProgress { it.withAttemptStarted(todayKey) }
        }
        startChallengeLevel(0, countAsNewGame = true, levelOverride = spec.level, keepSeed = true)
    }

    fun startChallengeLevel(
        levelNumber: Int,
        countAsNewGame: Boolean = true,
        levelOverride: ChallengeLevel? = null,
        keepSeed: Boolean = false
    ) {
        cancelContinueCountdown()
        cancelCascade()
        if (!keepSeed) {
            pieceGenerator.reseed(null)
            activeDailyKey = null
            comboStreak = 0
        }
        val level = levelOverride ?: ChallengeLevels.getLevel(levelNumber)
        val isDaily = levelOverride != null
        val board = level.buildBoard(startId = nextIdCounter)
        nextIdCounter += 100L
        val (trayPieces, updatedId) = pieceGenerator.generateTray(board, nextIdCounter)
        nextIdCounter = updatedId

        _visualPhase.value = BoardVisualPhase.Idle
        _uiState.update {
            it.copy(
                mode = GameMode.CHALLENGE,
                isDailyChallenge = isDaily,
                status = GameStatus.PLAYING,
                board = board,
                traySlots = trayPieces.mapIndexed { i, p -> TraySlot(i, p) },
                selectedSlotIndex = null,
                isHammerMode = false,
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
                showContinueOffer = false,
                continueCountdownSeconds = CONTINUE_COUNTDOWN_SECONDS,
                continueOfferUsedThisRun = false,
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
        cancelContinueCountdown()
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
                                isDailyChallenge = false,
                                status = GameStatus.PLAYING,
                                board = saved.board,
                                traySlots = saved.trayPieces.mapIndexed { i, p -> TraySlot(i, p) },
                                selectedSlotIndex = null,
                                isHammerMode = false,
                                score = saved.score,
                                movesPlayed = saved.movesPlayed,
                                bestCascadeDepth = saved.bestCascadeDepth,
                                totalLinesCleared = saved.totalLinesCleared,
                                savedClassicSummary = saved,
                                visualPhase = BoardVisualPhase.Idle,
                                dragState = null,
                                floatingBanner = null,
                                showContinueOffer = false,
                                continueCountdownSeconds = CONTINUE_COUNTDOWN_SECONDS,
                                continueOfferUsedThisRun = false
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
        when {
            state.isDailyChallenge -> startDailyGravity()
            state.mode == GameMode.CLASSIC -> startNewClassicGame()
            else -> startChallengeLevel(state.currentChallengeLevel.levelNumber)
        }
    }

    fun pauseGame() {
        if (_uiState.value.status == GameStatus.RESOLVING_CASCADE) {
            // Queue the pause: it is applied as soon as the in-flight cascade finalizes.
            pendingPause = true
            return
        }
        _uiState.update {
            if (it.status == GameStatus.PLAYING && !it.showContinueOffer) {
                it.copy(status = GameStatus.PAUSED, dragState = null, isHammerMode = false)
            } else it
        }
    }

    fun resumeGame() {
        _uiState.update {
            if (it.status == GameStatus.PAUSED) it.copy(status = GameStatus.PLAYING)
            else it
        }
    }

    fun exitToHome() {
        // Never snapshot a half-resolved board: finish the in-flight turn deterministically first.
        val turn = inFlightTurn
        if (_uiState.value.status == GameStatus.RESOLVING_CASCADE && turn != null) {
            val blocks = inFlightBlocksPlaced
            cancelCascade()
            applyTurnFinalization(turn, blocks)
        } else {
            cancelCascade()
        }
        cancelContinueCountdown()
        val state = _uiState.value
        if (state.mode == GameMode.CLASSIC && state.status != GameStatus.GAME_OVER && !state.showContinueOffer) {
            persistClassicSnapshot()
        }
        _uiState.update {
            it.copy(
                dragState = null,
                selectedSlotIndex = null,
                isHammerMode = false,
                showContinueOffer = false,
                status = if (it.status == GameStatus.PAUSED) GameStatus.PLAYING else it.status
            )
        }
    }

    fun toggleRulesDialog(show: Boolean) {
        _uiState.update { it.copy(showRulesDialog = show) }
    }

    fun toggleHaptics(enabled: Boolean) {
        haptics.enabled = enabled
        if (enabled) haptics.perform(com.example.audio.HapticEvent.TAP)
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

    fun selectMusicTrack(track: MusicTrack) {
        soundManager.setMusicTrack(track)
        updateSettings { it.copy(musicTrack = track) }
    }

    private fun updateSettings(transform: (GameSettings) -> GameSettings) {
        val updated = transform(_uiState.value.settings)
        _uiState.update { it.copy(hapticsEnabled = updated.hapticsEnabled, settings = updated) }
        viewModelScope.launch { repository.updateSettings(updated) }
    }

    fun resetAllProgress() {
        cancelContinueCountdown()
        cancelCascade()
        pieceGenerator.reseed(null)
        activeDailyKey = null
        comboStreak = 0
        inFlightTurn = null
        inFlightBlocksPlaced = 0
        pendingPause = false
        shuffleInFlight = false
        refreshInFlight = false
        hammerInFlight = false
        serverSpinPoints = 0
        pendingServerSpinPoints = null
        pendingLocalPrize = null
        lastSpinLabel = ""
        _isSpinning.value = false
        _economyMessage.value = null
        setLocalSpinPoints(0)
        viewModelScope.launch {
            repository.resetAllProgress()
            val emptyBoard = Board.empty()
            val (trayPieces, updatedId) = pieceGenerator.generateTray(emptyBoard, nextIdCounter)
            nextIdCounter = updatedId
            _visualPhase.value = BoardVisualPhase.Idle
            val emptyStats = GameStatistics()
            val rank = computeProgression(emptyStats)
            _uiState.update {
                it.copy(
                    mode = GameMode.CLASSIC,
                    isDailyChallenge = false,
                    status = GameStatus.PLAYING,
                    board = emptyBoard,
                    traySlots = trayPieces.mapIndexed { i, p -> TraySlot(i, p) },
                    selectedSlotIndex = null,
                    isHammerMode = false,
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
                    playerRank = rank,
                    lastChallengeStarsEarned = 0,
                    savedClassicSummary = null,
                    wallet = PlayerWallet(),
                    playerDisplayName = null,
                    profileAvatarId = ProfileAvatar.CLASSIC_BLOCK.id,
                    showContinueOffer = false,
                    continueCountdownSeconds = CONTINUE_COUNTDOWN_SECONDS,
                    continueOfferUsedThisRun = false,
                    visualPhase = BoardVisualPhase.Idle,
                    dragState = null,
                    floatingBanner = null,
                    latestParticles = emptyList(),
                    dailyRewardProgress = DailyRewardProgress(),
                    dailyMissionSnapshot = DailyMissionSnapshot(dateKey = DailyChallengeGenerator.todayDateKey()),
                    claimedAchievementIds = emptySet(),
                    dailyProgress = DailyProgress()
                )
            }
        }
    }

    fun setLanguage(language: String) {
        _uiState.update { it.copy(selectedLanguage = language) }
        viewModelScope.launch { repository.updateSelectedLanguage(language) }
    }

    private val dailyRewardClaimLock = Any()

    fun claimDailyReward() {
        synchronized(dailyRewardClaimLock) {
            val state = _uiState.value
            val todayKey = DailyChallengeGenerator.todayDateKey()
            val progress = state.dailyRewardProgress
            if (!progress.canClaimToday(todayKey)) return

            val dayIndex = progress.currentCycleDay()
            val tier = DailyRewardsCatalog.tiers.firstOrNull { it.day == dayIndex } ?: return
            val txId = "daily_reward_${todayKey}_d${tier.day}_${UUID.randomUUID()}"
            val baseWallet = if (tier.gold > 0 || tier.diamonds > 0) {
                val res = EconomyEngine.creditReward(
                    wallet = state.wallet,
                    goldDelta = tier.gold,
                    diamondDelta = tier.diamonds,
                    transactionId = txId,
                    reason = "Daily Reward Day ${tier.day}"
                )
                if (res is EconomyMutationResult.Success) res.updatedWallet else state.wallet
            } else {
                state.wallet
            }
            val updatedWallet = baseWallet.copy(
                shuffleCount = (baseWallet.shuffleCount + tier.shuffleBoosters).coerceAtMost(PlayerWallet.MAX_BOOSTER_STACK)
            )
            val updatedProgress = progress.copy(
                claimedDaysCount = progress.claimedDaysCount + 1,
                lastClaimedDateKey = todayKey
            )
            soundManager.playLevelComplete()
            haptics.perform(com.example.audio.HapticEvent.CLEAR)
            _economyMessage.value = "Claimed Day ${tier.day}: ${tier.title}!"
            _uiState.update {
                it.copy(
                    wallet = updatedWallet,
                    dailyRewardProgress = updatedProgress
                )
            }
            viewModelScope.launch {
                repository.updateWallet(updatedWallet)
                repository.updateDailyRewardProgress(updatedProgress)
            }
        }
    }

    fun claimDailyMission(missionId: String) {
        val state = _uiState.value
        val todayKey = DailyChallengeGenerator.todayDateKey()
        val snapshot = state.dailyMissionSnapshot.anchoredTo(todayKey, state.statistics)
        if (missionId in snapshot.claimedMissionIds) return
        val mission = DailyMissionsCatalog.missions.firstOrNull { it.id == missionId } ?: return
        val progress = mission.progressProvider(state.statistics, snapshot)
        if (progress < mission.target) return

        val txId = "daily_mission_${todayKey}_${mission.id}_${UUID.randomUUID()}"
        val res = EconomyEngine.creditReward(
            wallet = state.wallet,
            goldDelta = mission.rewardGold,
            diamondDelta = mission.rewardDiamonds,
            transactionId = txId,
            reason = "Daily Mission: ${mission.title}"
        )
        val updatedWallet = if (res is EconomyMutationResult.Success) res.updatedWallet else state.wallet
        val updatedSnapshot = snapshot.copy(claimedMissionIds = snapshot.claimedMissionIds + mission.id)
        soundManager.playGreat()
        haptics.perform(com.example.audio.HapticEvent.CLEAR)
        _economyMessage.value = "Mission complete: ${mission.title}!"
        _uiState.update {
            it.copy(
                wallet = updatedWallet,
                dailyMissionSnapshot = updatedSnapshot
            )
        }
        viewModelScope.launch {
            repository.updateWallet(updatedWallet)
            repository.updateDailyMissionSnapshot(updatedSnapshot)
        }
    }

    fun claimAchievement(achievementId: String) {
        val state = _uiState.value
        if (achievementId in state.claimedAchievementIds) return
        val ach = AchievementsCatalog.items.firstOrNull { it.id == achievementId } ?: return
        val progress = ach.progressProvider(state.statistics, state.challengeProgress)
        if (progress < ach.target) return

        val txId = "achievement_${ach.id}_${UUID.randomUUID()}"
        val res = EconomyEngine.creditReward(
            wallet = state.wallet,
            goldDelta = ach.rewardGold,
            diamondDelta = ach.rewardDiamonds,
            transactionId = txId,
            reason = "Achievement: ${ach.title}"
        )
        val updatedWallet = if (res is EconomyMutationResult.Success) res.updatedWallet else state.wallet
        val updatedClaimed = state.claimedAchievementIds + ach.id
        soundManager.playLevelComplete()
        haptics.perform(com.example.audio.HapticEvent.PERFECT)
        _economyMessage.value = "Unlocked ${ach.title}!"
        _uiState.update {
            it.copy(
                wallet = updatedWallet,
                claimedAchievementIds = updatedClaimed
            )
        }
        viewModelScope.launch {
            repository.updateWallet(updatedWallet)
            repository.updateClaimedAchievements(updatedClaimed)
        }
    }

    fun onCloudSignedOut() {
        serverSpinPoints = 0
        pendingServerSpinPoints = null
        pendingLocalPrize = null
        _isSpinning.value = false
        _economyMessage.value = null
        publishSpinPoints()
        resetAllProgress()
    }

    fun ensureAdsLoaded() {
        if (!AdConfig.sdkInitialized.value) return
        adManager.reloadIfNeeded()
        diamondAdManager.reloadIfNeeded()
        wheelAdManager.reloadIfNeeded()
        continueAdManager.reloadIfNeeded()
    }

    fun onAdsSdkReady() {
        if (!AdConfig.sdkInitialized.value) return
        if (adsPreloadJob?.isActive == true) return
        adsPreloadJob = viewModelScope.launch {
            wheelAdManager.reloadAfterSdkInit()
            adManager.reloadAfterSdkInit()
            delay(250L)
            diamondAdManager.reloadAfterSdkInit()
            delay(250L)
            continueAdManager.reloadAfterSdkInit()
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

    /** Shuffle booster: spends one charge and rerolls the remaining tray pieces. */
    fun useShuffleBooster() {
        val state = _uiState.value
        if (!canShuffle(state) || shuffleInFlight) return
        val remaining = state.traySlots.count { it.piece != null }
        if (remaining == 0) return
        shuffleInFlight = true
        viewModelScope.launch {
            try {
                // Re-validate after any suspension point: a turn may have started meanwhile.
                if (!canShuffle(_uiState.value)) return@launch
                val result = EconomyEngine.consumeBooster(
                    wallet = _uiState.value.wallet,
                    type = BoosterType.SHUFFLE,
                    transactionId = UUID.randomUUID().toString()
                )
                if (result !is EconomyMutationResult.Success) return@launch
                if (!canShuffle(_uiState.value)) return@launch
                val (fresh, updatedId) = pieceGenerator.generateTray(_uiState.value.board, nextIdCounter)
                nextIdCounter = updatedId
                var i = 0
                _uiState.update { st ->
                    st.copy(
                        selectedSlotIndex = null,
                        isHammerMode = false,
                        traySlots = st.traySlots.map { slot ->
                            if (slot.piece != null) slot.copy(piece = fresh[i++ % fresh.size]) else slot
                        }
                    )
                }
                soundManager.playUiClick()
                haptics.perform(com.example.audio.HapticEvent.TAP)
                applyWalletMutation(result.updatedWallet)
                persistClassicSnapshot()
            } finally {
                shuffleInFlight = false
            }
        }
    }

    /** Refresh booster: spends one charge and deals a brand-new set of 3 pieces into all tray slots. */
    fun useRefreshBooster() {
        val state = _uiState.value
        if (!canRefresh(state) || refreshInFlight) return
        refreshInFlight = true
        viewModelScope.launch {
            try {
                if (!canRefresh(_uiState.value)) return@launch
                val result = EconomyEngine.consumeBooster(
                    wallet = _uiState.value.wallet,
                    type = BoosterType.REFRESH,
                    transactionId = UUID.randomUUID().toString()
                )
                if (result !is EconomyMutationResult.Success) return@launch
                if (!canRefresh(_uiState.value)) return@launch
                val (fresh, updatedId) = pieceGenerator.generateTray(_uiState.value.board, nextIdCounter)
                nextIdCounter = updatedId
                _uiState.update { st ->
                    st.copy(
                        selectedSlotIndex = null,
                        tapHoverAnchor = null,
                        isHammerMode = false,
                        traySlots = List(3) { idx -> TraySlot(idx, fresh[idx]) }
                    )
                }
                soundManager.playGreat()
                haptics.perform(com.example.audio.HapticEvent.PLACE)
                applyWalletMutation(result.updatedWallet)
                persistClassicSnapshot()
            } finally {
                refreshInFlight = false
            }
        }
    }

    /** Toggles Hammer targeting mode so the player can tap any occupied block on the board to smash it. */
    fun toggleHammerMode() {
        val state = _uiState.value
        if (hammerInFlight) return
        if (state.isHammerMode) {
            _uiState.update { it.copy(isHammerMode = false) }
            soundManager.playUiClick()
            return
        }
        if (!canEnterHammerMode(state)) return
        _uiState.update {
            it.copy(
                isHammerMode = true,
                selectedSlotIndex = null,
                tapHoverAnchor = null
            )
        }
        soundManager.playUiClick()
        haptics.perform(com.example.audio.HapticEvent.TAP)
    }

    fun cancelHammerMode() {
        if (_uiState.value.isHammerMode) {
            _uiState.update { it.copy(isHammerMode = false) }
        }
    }

    private fun useHammerAt(row: Int, col: Int) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING || !state.isHammerMode || state.dragState != null || state.showContinueOffer) return
        if (state.wallet.hammerCount <= 0 || hammerInFlight) return
        val targetCell = state.board.get(row, col) ?: return

        hammerInFlight = true
        viewModelScope.launch {
            try {
                val cur = _uiState.value
                if (cur.status != GameStatus.PLAYING || !cur.isHammerMode || cur.wallet.hammerCount <= 0) return@launch
                val cellToSmash = cur.board.get(row, col) ?: return@launch
                val result = EconomyEngine.consumeBooster(
                    wallet = cur.wallet,
                    type = BoosterType.HAMMER,
                    transactionId = UUID.randomUUID().toString()
                )
                if (result !is EconomyMutationResult.Success) return@launch

                applyWalletMutation(result.updatedWallet)
                soundManager.playLineClear(linesCleared = 1)
                haptics.perform(com.example.audio.HapticEvent.CLEAR)

                val boardAfterSmash = cur.board.withCells(mapOf(GridCoord(row, col) to null))
                val gravityAfterSmash = GravityEngine.applyGravity(boardAfterSmash)
                val settledBoard = gravityAfterSmash.newBoard
                val cascadeRes = CascadeResolver.resolve(settledBoard)

                val smashParticle = if (cur.settings.visualEffectsEnabled) {
                    listOf(
                        ParticleBurst(
                            id = System.nanoTime(),
                            row = row + 0.5f,
                            col = col + 0.5f,
                            color = cellToSmash.color,
                            isLandingDust = false
                        )
                    )
                } else {
                    emptyList()
                }

                if (!gravityAfterSmash.didBlocksMove && !cascadeRes.hasClears) {
                    _uiState.update { st ->
                        st.copy(
                            board = settledBoard,
                            isHammerMode = false,
                            selectedSlotIndex = null,
                            particleTriggerId = if (smashParticle.isNotEmpty()) st.particleTriggerId + 1 else st.particleTriggerId,
                            latestParticles = smashParticle
                        )
                    }
                    persistClassicSnapshot()
                    return@launch
                }

                _uiState.update { st ->
                    st.copy(
                        board = boardAfterSmash,
                        isHammerMode = false,
                        selectedSlotIndex = null,
                        status = GameStatus.RESOLVING_CASCADE,
                        particleTriggerId = if (smashParticle.isNotEmpty()) st.particleTriggerId + 1 else st.particleTriggerId,
                        latestParticles = smashParticle
                    )
                }

                val startingScore = _uiState.value.score
                cascadeJob?.cancel()
                cascadeJob = viewModelScope.launch {
                    try {
                        if (gravityAfterSmash.didBlocksMove) {
                            val maxDist = gravityAfterSmash.movements.maxOf { it.distance }
                            val fallDuration = (160L + maxDist * 28L).coerceAtMost(320L)
                            val steps = (fallDuration / 16L).coerceAtLeast(1L).toInt()
                            for (step in 0..steps) {
                                val progress = (step.toFloat() / steps.toFloat()).coerceIn(0f, 1f)
                                _visualPhase.value = BoardVisualPhase.FallingGravity(
                                    boardAfterClear = boardAfterSmash,
                                    movements = gravityAfterSmash.movements,
                                    waveIndex = 0,
                                    progress = progress
                                )
                                if (step < steps) delay(16L)
                            }
                            soundManager.playBlockDrop(volume = 0.5f, rate = 0.88f)
                            _uiState.update { it.copy(board = settledBoard) }
                        }

                        if (cascadeRes.hasClears) {
                            animationRunner.runCascade(
                                resolution = cascadeRes,
                                startingScore = startingScore,
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
                        }

                        finalizeHammerResolution(cascadeRes, settledBoard)
                    } finally {
                        hammerInFlight = false
                    }
                }
                cascadeJob?.join()
            } finally {
                hammerInFlight = false
            }
        }
    }

    private fun finalizeHammerResolution(cascadeRes: com.example.game.CascadeResolution, settledBoard: Board) {
        val state = _uiState.value
        val finalBoard = if (cascadeRes.hasClears) cascadeRes.finalBoard else settledBoard
        val maxDepthInTurn = cascadeRes.maxCascadeDepth
        val cascadesInTurn = if (maxDepthInTurn >= 2) 1 else 0
        val fullClearAdded = if (cascadeRes.didFullClear) 1 else 0
        val finalScore = state.score
        val isChallengeWin = state.mode == GameMode.CHALLENGE &&
            finalScore >= state.currentChallengeLevel.targetScore
        val nextStatus = if (isChallengeWin) GameStatus.CHALLENGE_WON else GameStatus.PLAYING

        val updatedStats = if (cascadeRes.hasClears) {
            state.statistics.copy(
                totalLinesCleared = state.statistics.totalLinesCleared + cascadeRes.totalLinesCleared,
                totalCascadesTriggered = state.statistics.totalCascadesTriggered + cascadesInTurn,
                maxCascadeDepth = maxOf(state.statistics.maxCascadeDepth, maxDepthInTurn),
                fullBoardClears = state.statistics.fullBoardClears + fullClearAdded,
                classicBestScore = maxOf(
                    state.statistics.classicBestScore,
                    if (state.mode == GameMode.CLASSIC) finalScore else 0
                )
            )
        } else {
            state.statistics
        }

        val lastBannerId = state.floatingBanner?.id
        _visualPhase.value = BoardVisualPhase.Idle
        _uiState.update {
            it.copy(
                board = finalBoard,
                status = nextStatus,
                visualPhase = BoardVisualPhase.Idle,
                currentCascadeWave = 0,
                isHammerMode = false,
                statistics = updatedStats,
                playerRank = computeProgression(updatedStats)
            )
        }

        if (isChallengeWin) {
            soundManager.playLevelComplete()
        }
        persistClassicSnapshot()

        if (cascadeRes.hasClears) {
            viewModelScope.launch {
                repository.recordTurnStatistics(
                    blocksPlaced = 0,
                    linesCleared = cascadeRes.totalLinesCleared,
                    cascadesTriggered = cascadesInTurn,
                    maxDepthInTurn = maxDepthInTurn,
                    fullClearAdded = fullClearAdded,
                    dailyWonAdded = 0
                )
                if (state.mode == GameMode.CLASSIC) {
                    repository.updateClassicBestScore(finalScore)
                }
            }
        }

        if (pendingPause) {
            pendingPause = false
            pauseGame()
        }

        if (lastBannerId != null) {
            bannerDismissJob = viewModelScope.launch {
                animationRunner.scheduleBannerDismiss(lastBannerId) { id ->
                    _uiState.update { cur ->
                        if (cur.floatingBanner?.id == id) cur.copy(floatingBanner = null) else cur
                    }
                }
            }
        }
    }

    private fun canShuffle(s: GameUiState): Boolean =
        s.status == GameStatus.PLAYING && s.dragState == null && !s.showContinueOffer && s.wallet.shuffleCount > 0

    private fun canRefresh(s: GameUiState): Boolean =
        s.status == GameStatus.PLAYING && s.dragState == null && !s.showContinueOffer && s.wallet.refreshCount > 0

    private fun canEnterHammerMode(s: GameUiState): Boolean =
        s.status == GameStatus.PLAYING && s.dragState == null && !s.showContinueOffer && s.wallet.hammerCount > 0 && !s.board.isEmpty()

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
        _economyMessage.value = null
        if (!adManager.isReady()) {
            adManager.reloadIfNeeded()
            _economyMessage.value = "Preparing reward ad, please tap again..."
            return
        }
        adManager.showAd(
            activity = activity,
            onRewarded = { claimAdRewardOnServer(com.example.data.cloud.AdRewardType.GOLD_50) },
            onDismissed = { },
            onFailed = {
                adManager.reloadIfNeeded()
                _economyMessage.value = "Ad couldn't open, reloading now..."
            }
        )
    }

    fun watchDiamondAd(activity: Activity) {
        _economyMessage.value = null
        if (!diamondAdManager.isReady()) {
            diamondAdManager.reloadIfNeeded()
            _economyMessage.value = "Preparing reward ad, please tap again..."
            return
        }
        diamondAdManager.showAd(
            activity = activity,
            onRewarded = { claimAdRewardOnServer(com.example.data.cloud.AdRewardType.DIAMOND_5) },
            onDismissed = { },
            onFailed = {
                diamondAdManager.reloadIfNeeded()
                _economyMessage.value = "Ad couldn't open, reloading now..."
            }
        )
    }

    fun watchWheelAd(activity: Activity) {
        _economyMessage.value = null
        if (!wheelAdManager.isReady()) {
            wheelAdManager.reloadIfNeeded()
            _economyMessage.value = "Preparing spin ad, please tap again..."
            return
        }
        wheelAdManager.showAd(
            activity = activity,
            onRewarded = { claimAdRewardOnServer(com.example.data.cloud.AdRewardType.WHEEL_POINT) },
            onDismissed = { },
            onFailed = {
                wheelAdManager.reloadIfNeeded()
                _economyMessage.value = "Ad couldn't open, reloading now..."
            }
        )
    }

    fun clearEconomyMessage() {
        _economyMessage.value = null
    }

    /**
     * Always grants the earned ad reward locally immediately upon reward verification so players
     * never lose Gold, Diamonds, or Spin Points due to backend latency or App Check delays.
     * Signed-in sessions reconcile with the server ledger in the background.
     */
    private fun claimAdRewardOnServer(type: com.example.data.cloud.AdRewardType) {
        soundManager.playGreat()
        haptics.perform(com.example.audio.HapticEvent.CLEAR)
        when (type) {
            com.example.data.cloud.AdRewardType.GOLD_50 -> {
                claimAdReward(AD_REWARD_GOLD, UUID.randomUUID().toString())
                _economyMessage.value = "+$AD_REWARD_GOLD Gold earned!"
                if (isSignedIn()) {
                    viewModelScope.launch { serverEconomy.claimAdReward(type) }
                }
            }
            com.example.data.cloud.AdRewardType.DIAMOND_5 -> {
                viewModelScope.launch {
                    val result = EconomyEngine.creditReward(
                        wallet = _uiState.value.wallet,
                        goldDelta = 0,
                        diamondDelta = AD_REWARD_DIAMONDS,
                        transactionId = UUID.randomUUID().toString(),
                        reason = "Rewarded ad"
                    )
                    if (result is EconomyMutationResult.Success) {
                        applyWalletMutation(result.updatedWallet)
                    }
                    _economyMessage.value = "+$AD_REWARD_DIAMONDS Diamonds earned!"
                }
                if (isSignedIn()) {
                    viewModelScope.launch { serverEconomy.claimAdReward(type) }
                }
            }
            com.example.data.cloud.AdRewardType.WHEEL_POINT -> {
                setLocalSpinPoints(localSpinPoints() + 1)
                _economyMessage.value = "+1 Spin Point earned!"
                if (isSignedIn()) {
                    viewModelScope.launch {
                        serverEconomy.claimAdReward(type)
                            .onSuccess { wallet ->
                                setLocalSpinPoints((localSpinPoints() - 1).coerceAtLeast(0))
                                serverSpinPoints = wallet.wheelSpinPoints
                                publishSpinPoints()
                            }
                    }
                }
            }
        }
    }

    /** Spins the server-side wheel; [onResult] receives the winning segment index. */
    fun spinLuckyWheel(onResult: (Int?) -> Unit) {
        if (_isSpinning.value) return
        // On-device points (guests, or ads whose server claim fell back to device) are spent first.
        if (!isSignedIn() || localSpinPoints() > 0) {
            spinLocalWheel(onResult)
            return
        }
        if (serverSpinPoints <= 0) {
            _economyMessage.value = "No spin points. Watch an ad to get 1 spin."
            onResult(null)
            return
        }
        _isSpinning.value = true
        viewModelScope.launch {
            serverEconomy.spinLuckyWheel()
                .onSuccess { spin ->
                    val matchedPrize = localPrizes.getOrNull(spin.segmentIndex)
                    pendingLocalPrize = matchedPrize
                    pendingServerSpinPoints = spin.wallet.wheelSpinPoints
                    _economyMessage.value = null
                    lastSpinLabel = spin.prizeLabel.ifBlank { matchedPrize?.label.orEmpty() }
                    onResult(spin.segmentIndex)
                }
                .onFailure {
                    // Server unreachable: spend one server-held point gracefully via local wheel roll.
                    serverSpinPoints = (serverSpinPoints - 1).coerceAtLeast(0)
                    publishSpinPoints()
                    val index = rollLocalPrizeIndex()
                    pendingLocalPrize = localPrizes[index]
                    lastSpinLabel = localPrizes[index].label
                    _economyMessage.value = null
                    onResult(index)
                }
        }
    }

    /** Called by the wheel UI once the spin animation stops, so the balance updates after the reveal. */
    fun finishWheelSpin() {
        pendingLocalPrize?.let { prize ->
            val w = _uiState.value.wallet
            viewModelScope.launch {
                val txId = UUID.randomUUID().toString()
                val base = if (prize.gold > 0 || prize.diamonds > 0) {
                    val currencyResult = EconomyEngine.creditReward(
                        wallet = w,
                        goldDelta = prize.gold,
                        diamondDelta = prize.diamonds,
                        transactionId = txId,
                        reason = "Lucky Wheel"
                    )
                    if (currencyResult is EconomyMutationResult.Success) currencyResult.updatedWallet else w
                } else {
                    w
                }
                applyWalletMutation(
                    base.copy(
                        hammerCount = (base.hammerCount + prize.hammer).coerceAtMost(PlayerWallet.MAX_BOOSTER_STACK),
                        shuffleCount = (base.shuffleCount + prize.shuffle).coerceAtMost(PlayerWallet.MAX_BOOSTER_STACK),
                        refreshCount = (base.refreshCount + prize.refresh).coerceAtMost(PlayerWallet.MAX_BOOSTER_STACK)
                    )
                )
            }
        }
        pendingLocalPrize = null
        pendingServerSpinPoints?.let { pts ->
            serverSpinPoints = pts
            publishSpinPoints()
        }
        pendingServerSpinPoints = null
        _isSpinning.value = false
    }

    // ---- Guest (not signed in) wheel: points + prizes are handled on-device. ----
    private fun localSpinPoints(): Int =
        runCatching { wheelPrefs?.getInt("points", 0) ?: 0 }.getOrDefault(0)

    private fun setLocalSpinPoints(value: Int) {
        val v = value.coerceIn(0, 99)
        runCatching { wheelPrefs?.edit()?.putInt("points", v)?.apply() }
        publishSpinPoints()
    }

    private fun publishSpinPoints() {
        _wheelSpinPoints.value = serverSpinPoints + localSpinPoints()
    }

    private fun rollLocalPrizeIndex(): Int {
        var roll = kotlin.random.Random.nextInt(localPrizes.sumOf { it.weight })
        for ((i, p) in localPrizes.withIndex()) {
            if (roll < p.weight) return i
            roll -= p.weight
        }
        return localPrizes.lastIndex
    }

    private fun spinLocalWheel(onResult: (Int?) -> Unit) {
        val points = localSpinPoints()
        if (points <= 0) {
            _economyMessage.value = "No spin points. Watch an ad to get 1 spin."
            onResult(null)
            return
        }
        setLocalSpinPoints(points - 1)
        val index = rollLocalPrizeIndex()
        pendingLocalPrize = localPrizes[index]
        lastSpinLabel = localPrizes[index].label
        _economyMessage.value = null
        _isSpinning.value = true
        onResult(index)
    }

    fun refreshServerWallet() {
        if (!isSignedIn()) {
            serverSpinPoints = 0
            publishSpinPoints()
            return
        }
        viewModelScope.launch {
            serverEconomy.fetchWallet().onSuccess { serverSpinPoints = it.wheelSpinPoints; publishSpinPoints() }
        }
    }

    fun watchContinueRewardedAd(activity: Activity) {
        val state = _uiState.value
        if (!state.showContinueOffer) return
        if (!continueAdManager.isReady()) {
            continueAdManager.reloadIfNeeded()
            return
        }
        var rewardDelivered = false
        isContinueAdShowing = true
        continueAdManager.showAd(
            activity = activity,
            onRewarded = {
                rewardDelivered = true
                isContinueAdShowing = false
                grantContinueRescue()
            },
            onDismissed = {
                isContinueAdShowing = false
                if (!rewardDelivered && _uiState.value.showContinueOffer) {
                    declineContinueOffer()
                }
            },
            onFailed = {
                isContinueAdShowing = false
                if (!rewardDelivered && _uiState.value.showContinueOffer) {
                    declineContinueOffer()
                }
            }
        )
    }

    fun grantContinueRescue() {
        cancelContinueCountdown()
        val state = _uiState.value
        val rescuedBoard = applyContinueBoardRescue(state.board)
        val (freshPieces, updatedId) = pieceGenerator.generateTray(rescuedBoard, nextIdCounter)
        nextIdCounter = updatedId
        soundManager.playGreat()
        haptics.perform(com.example.audio.HapticEvent.CLEAR)
        _visualPhase.value = BoardVisualPhase.Idle
        _uiState.update {
            it.copy(
                board = rescuedBoard,
                traySlots = freshPieces.mapIndexed { idx, piece -> TraySlot(idx, piece) },
                selectedSlotIndex = null,
                dragState = null,
                status = GameStatus.PLAYING,
                showContinueOffer = false,
                continueCountdownSeconds = CONTINUE_COUNTDOWN_SECONDS,
                continueOfferUsedThisRun = true
            )
        }
        persistClassicSnapshot()
    }

    fun declineContinueOffer() {
        cancelContinueCountdown()
        isContinueAdShowing = false
        val current = _uiState.value
        if (current.status == GameStatus.GAME_OVER && !current.showContinueOffer) return
        soundManager.playGameOver()
        haptics.perform(com.example.audio.HapticEvent.GAME_OVER)
        _uiState.update {
            it.copy(
                status = GameStatus.GAME_OVER,
                showContinueOffer = false,
                savedClassicSummary = null,
                dragState = null,
                selectedSlotIndex = null
            )
        }
        viewModelScope.launch {
            repository.saveClassicGame(null)
        }
    }

    fun selectProfileAvatar(avatarId: String) {
        val normalized = ProfileAvatar.fromId(avatarId).id
        _uiState.update { it.copy(profileAvatarId = normalized) }
        viewModelScope.launch {
            repository.updateProfileAvatar(normalized)
        }
    }

    fun renamePlayerWithDiamonds(
        newName: String,
        onResult: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            val result = EconomyEngine.renameProfileWithDiamonds(
                wallet = _uiState.value.wallet,
                newName = newName,
                transactionId = UUID.randomUUID().toString()
            )
            when (result) {
                is EconomyMutationResult.Success -> {
                    val sanitizedName = newName.trim().take(EconomyEngine.MAX_PLAYER_NAME_LENGTH)
                    _uiState.update {
                        it.copy(
                            wallet = result.updatedWallet,
                            playerDisplayName = sanitizedName
                        )
                    }
                    repository.updateWallet(result.updatedWallet)
                    repository.updatePlayerDisplayName(sanitizedName)
                    onResult(true, "Name changed to $sanitizedName")
                }
                is EconomyMutationResult.Rejected -> {
                    onResult(false, result.reason)
                }
            }
        }
    }

    private suspend fun applyWalletMutation(wallet: PlayerWallet) {
        _uiState.update { it.copy(wallet = wallet) }
        repository.updateWallet(wallet)
    }

    private fun startContinueCountdown() {
        continueCountdownJob?.cancel()
        continueCountdownJob = viewModelScope.launch {
            var remaining = CONTINUE_COUNTDOWN_SECONDS
            while (remaining > 0) {
                _uiState.update {
                    if (!it.showContinueOffer) return@launch
                    it.copy(continueCountdownSeconds = remaining)
                }
                delay(1000L)
                while (isContinueAdShowing) {
                    delay(200L)
                }
                if (!_uiState.value.showContinueOffer) return@launch
                remaining--
            }
            if (_uiState.value.showContinueOffer && !isContinueAdShowing) {
                declineContinueOffer()
            }
        }
    }

    /** Cancels any in-flight cascade/banner coroutines so they cannot leak into another game. */
    private fun cancelCascade() {
        cascadeJob?.cancel()
        cascadeJob = null
        bannerDismissJob?.cancel()
        bannerDismissJob = null
        inFlightTurn = null
        pendingPause = false
        hammerInFlight = false
        refreshInFlight = false
        shuffleInFlight = false
        _visualPhase.value = BoardVisualPhase.Idle
        _uiState.update {
            if (it.status == GameStatus.RESOLVING_CASCADE || it.floatingBanner != null || it.currentCascadeWave != 0 || it.isHammerMode) {
                it.copy(
                    floatingBanner = null,
                    currentCascadeWave = 0,
                    latestParticles = emptyList(),
                    isHammerMode = false,
                    visualPhase = BoardVisualPhase.Idle
                )
            } else it
        }
    }

    private fun cancelContinueCountdown() {
        continueCountdownJob?.cancel()
        continueCountdownJob = null
    }

    fun selectTraySlot(slotIndex: Int) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return
        if (state.traySlots.getOrNull(slotIndex)?.piece == null) return
        _uiState.update {
            val nextSelection = if (it.selectedSlotIndex == slotIndex) null else slotIndex
            it.copy(selectedSlotIndex = nextSelection, tapHoverAnchor = null, isHammerMode = false)
        }
    }

    fun onBoardCellTapped(row: Int, col: Int) {
        val state = _uiState.value
        if (state.status != GameStatus.PLAYING) return
        if (state.isHammerMode) {
            useHammerAt(row, col)
            return
        }
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
                isHammerMode = false,
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
        val willPlace = state.status == GameStatus.PLAYING && anchor != null && activeDrag.isHoverValid
        // A failed drop must not leave a stale highlighted slot behind.
        _uiState.update { it.copy(dragState = null, selectedSlotIndex = if (willPlace) it.selectedSlotIndex else null) }

        if (willPlace && anchor != null) {
            placePiece(activeDrag.slotIndex, anchor.row, anchor.col)
        }
    }

    fun cancelDrag() {
        _uiState.update { it.copy(dragState = null, selectedSlotIndex = null) }
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
            startingBlockId = nextIdCounter,
            comboStreak = comboStreak
        ) ?: return
        nextIdCounter = turn.nextBlockIdCounter
        comboStreak = turn.comboStreak

        soundManager.playBlockDrop()
        haptics.perform(com.example.audio.HapticEvent.PLACE)
        val fb = turn.feedback
        if (fb.hasFeedback) {
            when (fb.tier) {
                com.example.game.FeedbackTier.PERFECT -> { soundManager.playPerfect(); haptics.perform(com.example.audio.HapticEvent.PERFECT) }
                com.example.game.FeedbackTier.GREAT -> { soundManager.playGreat(); haptics.perform(com.example.audio.HapticEvent.CLEAR) }
                else -> haptics.perform(if (fb.combo != com.example.game.ComboLabel.NONE) com.example.audio.HapticEvent.COMBO else com.example.audio.HapticEvent.CLEAR)
            }
        } else if (turn.resolution.hasClears) {
            haptics.perform(com.example.audio.HapticEvent.CLEAR)
        }

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
            inFlightTurn = turn
            inFlightBlocksPlaced = blocksPlaced
            cascadeJob?.cancel()
            cascadeJob = viewModelScope.launch {
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
                inFlightTurn = null
                applyTurnFinalization(turn, blocksPlaced)
                if (pendingPause) {
                    pendingPause = false
                    pauseGame()
                }

                if (lastBannerId != null) {
                    bannerDismissJob = launch {
                        animationRunner.scheduleBannerDismiss(lastBannerId) { id ->
                            _uiState.update { cur ->
                                if (cur.floatingBanner?.id == id) cur.copy(floatingBanner = null) else cur
                            }
                        }
                    }
                }
            }
        }
    }

    private fun applyTurnFinalization(turn: TurnExecutionResult, blocksPlaced: Int) {
        val state = _uiState.value
        val nextStatus = turn.nextStatus
        val triggerContinueOffer = nextStatus == GameStatus.GAME_OVER &&
            state.mode == GameMode.CLASSIC &&
            shouldOfferContinue(turn.finalScore, state.continueOfferUsedThisRun)

        val resolvedStatus = if (triggerContinueOffer) GameStatus.PLAYING else nextStatus

        if (!triggerContinueOffer) {
            if (nextStatus == GameStatus.CHALLENGE_WON) {
                soundManager.playLevelComplete()
            } else if (nextStatus == GameStatus.GAME_OVER || nextStatus == GameStatus.CHALLENGE_LOST) {
                soundManager.playGameOver()
                haptics.perform(com.example.audio.HapticEvent.GAME_OVER)
            }
        }

        val isDaily = state.isDailyChallenge && activeDailyKey != null
        val isCampaignLevel = state.mode == GameMode.CHALLENGE && !state.isDailyChallenge

        val nextUnlockedLevel = if (
            isCampaignLevel && nextStatus == GameStatus.CHALLENGE_WON
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

        val updatedChallengeMap = if (isCampaignLevel && nextStatus == GameStatus.CHALLENGE_WON) {
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
        } else if (isCampaignLevel && nextStatus == GameStatus.CHALLENGE_LOST) {
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

        val dailyWonAdded = if (state.isDailyChallenge && nextStatus == GameStatus.CHALLENGE_WON) 1 else 0
        val updatedStats = state.statistics.copy(
            totalBlocksPlaced = state.statistics.totalBlocksPlaced + blocksPlaced,
            totalLinesCleared = state.statistics.totalLinesCleared + turn.totalLinesInTurn,
            totalCascadesTriggered = state.statistics.totalCascadesTriggered + cascadesInTurn,
            maxCascadeDepth = maxOf(state.statistics.maxCascadeDepth, maxDepthInTurn),
            fullBoardClears = state.statistics.fullBoardClears + fullClearAdded,
            dailyCompletedCount = state.statistics.dailyCompletedCount + dailyWonAdded,
            classicBestScore = maxOf(
                state.statistics.classicBestScore,
                if (state.mode == GameMode.CLASSIC) turn.finalScore else 0
            )
        )
        val rank = computeProgression(updatedStats)
        val todayKey = DailyChallengeGenerator.todayDateKey()
        val anchoredMissions = state.dailyMissionSnapshot.anchoredTo(todayKey, state.statistics)
        val updatedDailyProgress = if (isDaily && (nextStatus == GameStatus.CHALLENGE_WON || nextStatus == GameStatus.CHALLENGE_LOST)) {
            state.dailyProgress.withResult(activeDailyKey!!, turn.finalScore, nextStatus == GameStatus.CHALLENGE_WON)
        } else {
            state.dailyProgress
        }

        _visualPhase.value = BoardVisualPhase.Idle
        _uiState.update {
            it.copy(
                board = turn.resolution.finalBoard,
                traySlots = turn.nextTraySlots,
                score = turn.finalScore,
                status = resolvedStatus,
                visualPhase = BoardVisualPhase.Idle,
                currentCascadeWave = 0,
                unlockedChallengeLevel = nextUnlockedLevel,
                bestCascadeDepth = maxOf(it.bestCascadeDepth, maxDepthInTurn),
                lastChallengeStarsEarned = turn.earnedChallengeStars,
                challengeProgress = updatedChallengeMap,
                statistics = updatedStats,
                playerRank = rank,
                dailyMissionSnapshot = anchoredMissions,
                dailyProgress = updatedDailyProgress,
                showContinueOffer = triggerContinueOffer,
                continueCountdownSeconds = if (triggerContinueOffer) CONTINUE_COUNTDOWN_SECONDS else it.continueCountdownSeconds,
                continueOfferUsedThisRun = it.continueOfferUsedThisRun || triggerContinueOffer
            )
        }

        if (triggerContinueOffer) {
            continueAdManager.reloadIfNeeded()
            startContinueCountdown()
        }

        if (isDaily && (nextStatus == GameStatus.CHALLENGE_WON || nextStatus == GameStatus.CHALLENGE_LOST)) {
            val key = activeDailyKey!!
            viewModelScope.launch {
                repository.updateDailyProgress {
                    it.withResult(key, turn.finalScore, nextStatus == GameStatus.CHALLENGE_WON)
                }
            }
        }

        viewModelScope.launch {
            if (anchoredMissions != state.dailyMissionSnapshot) {
                repository.updateDailyMissionSnapshot(anchoredMissions)
            }
            repository.recordTurnStatistics(
                blocksPlaced = blocksPlaced,
                linesCleared = turn.totalLinesInTurn,
                cascadesTriggered = cascadesInTurn,
                maxDepthInTurn = maxDepthInTurn,
                fullClearAdded = fullClearAdded,
                dailyWonAdded = dailyWonAdded
            )
            if (state.mode == GameMode.CLASSIC) {
                repository.updateClassicBestScore(turn.finalScore)
                if (resolvedStatus == GameStatus.GAME_OVER) {
                    _uiState.update { it.copy(savedClassicSummary = null) }
                    repository.saveClassicGame(null)
                } else {
                    persistClassicSnapshot()
                }
            } else if (isCampaignLevel && (nextStatus == GameStatus.CHALLENGE_WON || nextStatus == GameStatus.CHALLENGE_LOST)) {
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

    private fun computeProgression(stats: GameStatistics): PlayerRankInfo {
        return PlayerRankCalculator.compute(stats)
    }

    fun applyCloudMerge(
        cloudProfile: com.example.data.cloud.UserCloudProfile?,
        replaceLocal: Boolean = false
    ) {
        if (cloudProfile == null) return
        viewModelScope.launch {
            val merged = repository.mergeCloudProgress(
                cloudBestScore = cloudProfile.classicBestScore.toInt(),
                cloudTotalGames = cloudProfile.totalGamesPlayed.toInt(),
                cloudTotalLines = cloudProfile.totalLinesCleared.toInt(),
                cloudTotalCascades = cloudProfile.totalCascadesTriggered.toInt(),
                cloudMaxDepth = cloudProfile.maxCascadeDepth.toInt(),
                cloudTotalBlocks = cloudProfile.totalBlocksPlaced.toInt(),
                cloudFullClears = cloudProfile.fullBoardClears.toInt(),
                cloudDisplayName = cloudProfile.displayName,
                cloudAvatarId = cloudProfile.avatarId,
                replaceLocal = replaceLocal
            )
            val rank = computeProgression(merged.statistics)
            _uiState.update { cur ->
                cur.copy(
                    classicBestScore = if (replaceLocal) merged.classicBestScore else maxOf(cur.classicBestScore, merged.classicBestScore),
                    statistics = merged.statistics,
                    playerRank = rank,
                    playerDisplayName = if (replaceLocal) merged.playerDisplayName else cur.playerDisplayName ?: merged.playerDisplayName,
                    profileAvatarId = if (replaceLocal) merged.profileAvatarId else if (cur.profileAvatarId == ProfileAvatar.CLASSIC_BLOCK.id && merged.profileAvatarId != ProfileAvatar.CLASSIC_BLOCK.id) {
                        merged.profileAvatarId
                    } else {
                        cur.profileAvatarId
                    }
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
        adsPreloadJob?.cancel()
        cancelContinueCountdown()
        adManager.release()
        continueAdManager.release()
        diamondAdManager.release()
        wheelAdManager.release()
        soundManager.release()
        haptics.cancel()
    }

    companion object {
        const val AD_REWARD_GOLD = 50
        const val AD_REWARD_DIAMONDS = 5
        const val CONTINUE_MAX_SCORE = 10_000
        const val CONTINUE_COUNTDOWN_SECONDS = 5

        fun shouldOfferContinue(score: Int, alreadyUsedThisRun: Boolean): Boolean =
            !alreadyUsedThisRun && score <= CONTINUE_MAX_SCORE

        fun applyContinueBoardRescue(board: Board): Board {
            val rowUpdates = buildMap<GridCoord, com.example.game.BlockCell?> {
                for (r in setOf(2, 3, 4, 5)) {
                    for (c in 0 until Board.SIZE) {
                        put(GridCoord(r, c), null)
                    }
                }
            }
            val rowsCleared = board.withCells(rowUpdates)
            if (rowsCleared.occupiedCount() <= 40) return rowsCleared

            val colUpdates = buildMap<GridCoord, com.example.game.BlockCell?> {
                for (r in 0 until Board.SIZE) {
                    for (c in setOf(3, 4)) {
                        put(GridCoord(r, c), null)
                    }
                }
            }
            return rowsCleared.withCells(colUpdates)
        }

        fun calculateChallengeStars(score: Int, targetScore: Int, movesRemaining: Int): Int =
            GameSessionEngine.calculateChallengeStars(score, targetScore, movesRemaining)
    }
}
