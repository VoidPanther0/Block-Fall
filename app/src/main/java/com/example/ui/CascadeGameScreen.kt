package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.Board
import com.example.game.GameStatus
import com.example.ui.components.CascadeCalloutBar
import com.example.ui.components.ExitConfirmationDialog
import com.example.ui.components.GameBoard
import com.example.ui.components.GravityRulesDialog
import com.example.ui.components.PieceTray
import com.example.ui.components.RestartConfirmationDialog
import com.example.ui.components.RoyalSkyBackdrop
import com.example.ui.components.ScorePanel
import com.example.ui.components.TopGameBar
import com.example.ui.components.drawBevelBlock
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.InvalidTint
import com.example.ui.theme.TextSecondary

@Composable
fun CascadeGameScreen(
    viewModel: GameViewModel = viewModel(),
    onOpenPause: () -> Unit = { viewModel.pauseGame() },
    onOpenHowToPlay: () -> Unit = { viewModel.toggleRulesDialog(true) },
    onLevelComplete: () -> Unit = {},
    onGameOver: () -> Unit = {},
    onExitToHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val visualPhase by viewModel.visualPhase.collectAsStateWithLifecycle()
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    // Clearance between pointer finger and bottom edge of lifted floating piece
    val dragLiftPx = remember(density) { with(density) { 72.dp.toPx() } }

    var rootOrigin by remember { mutableStateOf(Offset.Zero) }
    var boardBoundsInRoot by remember { mutableStateOf(Rect.Zero) }
    var showRestartConfirm by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }

    BackHandler(enabled = state.status == GameStatus.PLAYING) {
        showExitConfirm = true
    }

    LaunchedEffect(state.currentCascadeWave) {
        if (state.settings.hapticsEnabled && state.currentCascadeWave >= 2) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    LaunchedEffect(state.status) {
        when (state.status) {
            GameStatus.CHALLENGE_WON -> onLevelComplete()
            GameStatus.GAME_OVER, GameStatus.CHALLENGE_LOST -> onGameOver()
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { rootOrigin = it.positionInRoot() }
            .testTag("gameplay_screen")
    ) {
        RoyalSkyBackdrop()
        BoxWithConstraints(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            // Short landscape phones cannot fit the vertical stack (HUD + board + tray),
            // so they use the two-pane layout with a height-clamped board instead.
            val isShortLandscape = maxHeight < 480.dp && maxWidth >= 480.dp
            val isWideLayout = (maxWidth >= 660.dp && maxWidth > maxHeight) || isShortLandscape
            val wideBoardMax = (maxHeight - 56.dp).coerceAtLeast(120.dp).coerceAtMost(500.dp)

            if (isWideLayout) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 1060.dp),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1.1f)
                            .fillMaxHeight()
                    ) {
                        CascadeCalloutBar(
                            floatingBanner = state.floatingBanner,
                            cascadeWave = state.currentCascadeWave
                        )
                        Spacer(Modifier.height(4.dp))
                        GameBoard(
                            board = state.board,
                            visualPhase = visualPhase,
                            dragState = state.dragState,
                            floatingBanner = state.floatingBanner,
                            particleTriggerId = state.particleTriggerId,
                            latestParticles = state.latestParticles,
                            hasSelectedSlot = state.selectedSlotIndex != null,
                            visualEffectsEnabled = state.settings.visualEffectsEnabled,
                            placementPreviewEnabled = state.settings.placementPreviewEnabled,
                            onCellTapped = { r, c ->
                                if (state.settings.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.onBoardCellTapped(r, c)
                            },
                            onBoardTouchStart = { localOffset ->
                                if (state.settings.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.startBoardTouch(localOffset)
                            },
                            onBoardTouchMove = viewModel::updateBoardTouch,
                            onBoardTouchEnd = viewModel::endDrag,
                            onBoardTouchCancel = viewModel::cancelDrag,
                            onBoardBoundsChanged = { rect ->
                                boardBoundsInRoot = rect
                                viewModel.updateBoardBounds(rect, dragLiftPx)
                            },
                            modifier = Modifier.widthIn(max = wideBoardMax)
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(0.9f)
                            .widthIn(max = 420.dp),
                        verticalArrangement = Arrangement.spacedBy(if (isShortLandscape) 8.dp else 16.dp)
                    ) {
                        TopGameBar(
                            state = state,
                            onShowHowToPlay = onOpenHowToPlay,
                            onRestart = { showRestartConfirm = true },
                            onPause = {
                                viewModel.pauseGame()
                                onOpenPause()
                            },
                            onBack = { showExitConfirm = true }
                        )
                        ScorePanel(state = state)
                        PieceTray(
                            slots = state.traySlots,
                            board = state.board,
                            selectedSlotIndex = state.selectedSlotIndex,
                            draggingSlotIndex = state.dragState?.slotIndex,
                            status = state.status,
                            onSlotTapped = { idx ->
                                if (state.settings.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.selectTraySlot(idx)
                            },
                            onDragStart = { idx, pos ->
                                if (state.settings.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.startDrag(idx, pos)
                            },
                            onDragMove = viewModel::updateDrag,
                            onDragEnd = viewModel::endDrag,
                            onDragCancel = viewModel::cancelDrag
                        )
                        ShuffleBoosterButton(
                            count = state.wallet.shuffleCount,
                            enabled = state.status == GameStatus.PLAYING &&
                                state.wallet.shuffleCount > 0 &&
                                state.traySlots.any { it.piece != null },
                            onClick = viewModel::useShuffleBooster
                        )
                        HintFooter(hasSelectedPiece = state.selectedSlotIndex != null)
                    }
                }
            } else {
                val boardMaxWidth = (maxHeight * 0.48f).coerceIn(170.dp, 520.dp)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .widthIn(max = 520.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Fixed-height Top HUD (never expands or collapses during clears)
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TopGameBar(
                            state = state,
                            onShowHowToPlay = onOpenHowToPlay,
                            onRestart = { showRestartConfirm = true },
                            onPause = {
                                viewModel.pauseGame()
                                onOpenPause()
                            },
                            onBack = { showExitConfirm = true }
                        )
                        ScorePanel(state = state)
                    }

                    // Centered flexible playfield zone: board position stays 100% locked
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CascadeCalloutBar(
                            floatingBanner = state.floatingBanner,
                            cascadeWave = state.currentCascadeWave
                        )
                            Spacer(Modifier.height(4.dp))
                            GameBoard(
                                board = state.board,
                                visualPhase = visualPhase,
                                dragState = state.dragState,
                                floatingBanner = state.floatingBanner,
                                particleTriggerId = state.particleTriggerId,
                                latestParticles = state.latestParticles,
                                hasSelectedSlot = state.selectedSlotIndex != null,
                                visualEffectsEnabled = state.settings.visualEffectsEnabled,
                                placementPreviewEnabled = state.settings.placementPreviewEnabled,
                                onCellTapped = { r, c ->
                                    if (state.settings.hapticsEnabled) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    viewModel.onBoardCellTapped(r, c)
                                },
                                onBoardTouchStart = { localOffset ->
                                    if (state.settings.hapticsEnabled) {
                                        haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    }
                                    viewModel.startBoardTouch(localOffset)
                                },
                                onBoardTouchMove = viewModel::updateBoardTouch,
                                onBoardTouchEnd = viewModel::endDrag,
                                onBoardTouchCancel = viewModel::cancelDrag,
                                onBoardBoundsChanged = { rect ->
                                    boardBoundsInRoot = rect
                                    viewModel.updateBoardBounds(rect, dragLiftPx)
                                },
                                modifier = Modifier
                                    .widthIn(max = boardMaxWidth)
                                    .fillMaxWidth()
                            )
                        }
                    }

                    // Fixed-height Bottom Dock
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PieceTray(
                            slots = state.traySlots,
                            board = state.board,
                            selectedSlotIndex = state.selectedSlotIndex,
                            draggingSlotIndex = state.dragState?.slotIndex,
                            status = state.status,
                            onSlotTapped = { idx ->
                                if (state.settings.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.selectTraySlot(idx)
                            },
                            onDragStart = { idx, pos ->
                                if (state.settings.hapticsEnabled) {
                                    haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                }
                                viewModel.startDrag(idx, pos)
                            },
                            onDragMove = viewModel::updateDrag,
                            onDragEnd = viewModel::endDrag,
                            onDragCancel = viewModel::cancelDrag
                        )
                        ShuffleBoosterButton(
                            count = state.wallet.shuffleCount,
                            enabled = state.status == GameStatus.PLAYING &&
                                state.wallet.shuffleCount > 0 &&
                                state.traySlots.any { it.piece != null },
                            onClick = viewModel::useShuffleBooster
                        )
                        HintFooter(hasSelectedPiece = state.selectedSlotIndex != null)
                        Spacer(Modifier.height(2.dp))
                    }
                }
            }
        }

        // Full-scale floating piece overlay lifted above the player's finger
        val activeDrag = state.dragState
        if (activeDrag != null && boardBoundsInRoot.width > 1f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cellSize = boardBoundsInRoot.width / Board.SIZE.toFloat()
                val gap = (cellSize * 0.085f).coerceAtLeast(2.5f)
                val tileSize = cellSize - gap

                val pieceW = activeDrag.piece.shape.widthCells * cellSize
                val pieceH = activeDrag.piece.shape.heightCells * cellSize
                val localPointer = activeDrag.pointerRootOffset - rootOrigin
                val startX = localPointer.x - pieceW / 2f
                val startY = localPointer.y - dragLiftPx - pieceH

                for (cell in activeDrag.piece.shape.offsets) {
                    drawBevelBlock(
                        left = startX + cell.col * cellSize + gap / 2f,
                        top = startY + cell.row * cellSize + gap / 2f,
                        cellSize = tileSize,
                        color = activeDrag.piece.color,
                        alpha = 0.96f,
                        glowBoost = if (state.settings.visualEffectsEnabled) 0.72f else 0.12f,
                        blockStyle = state.settings.blockStyle,
                        boardPalette = state.settings.boardPalette
                    )
                }
            }
        }

        if (showRestartConfirm) {
            RestartConfirmationDialog(
                onConfirm = {
                    showRestartConfirm = false
                    viewModel.restartCurrentGame()
                },
                onDismiss = { showRestartConfirm = false }
            )
        }

        if (showExitConfirm) {
            ExitConfirmationDialog(
                mode = state.mode,
                onConfirm = {
                    showExitConfirm = false
                    viewModel.exitToHome()
                    onExitToHome()
                },
                onDismiss = { showExitConfirm = false }
            )
        }

        if (state.showRulesDialog) {
            GravityRulesDialog(onDismiss = { viewModel.toggleRulesDialog(false) })
        }
    }
}

@Composable
private fun ShuffleBoosterButton(count: Int, enabled: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.testTag("shuffle_booster_button")
    ) {
        Icon(Icons.Filled.Shuffle, contentDescription = null)
        Spacer(Modifier.width(6.dp))
        Text("Shuffle ($count)")
    }
}

@Composable
private fun HintFooter(hasSelectedPiece: Boolean) {
    Text(
        text = if (hasSelectedPiece) {
            "Tap any board square to drop the selected piece"
        } else {
            "Drag any piece onto the board · Clear lines to trigger gravity"
        },
        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
        color = TextSecondary.copy(alpha = 0.85f),
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
