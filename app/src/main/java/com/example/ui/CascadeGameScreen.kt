package com.example.ui

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ads.AdState
import com.example.game.Board
import com.example.game.GameStatus
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.BannerAdBar
import com.example.ui.components.GameBoard
import com.example.ui.components.PieceTray
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.components.RestartConfirmationDialog
import com.example.ui.components.RoyalSkyBackdrop
import com.example.ui.components.ScorePanel
import com.example.ui.components.SecondaryButton
import com.example.ui.components.TopGameBar
import com.example.ui.components.drawBevelBlock
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.InvalidTint
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun CascadeGameScreen(
    viewModel: GameViewModel = viewModel(),
    onOpenPause: () -> Unit = { viewModel.pauseGame() },
    onLevelComplete: () -> Unit = {},
    onGameOver: () -> Unit = {},
    onExitToHome: () -> Unit = {}
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val visualPhase by viewModel.visualPhase.collectAsStateWithLifecycle()
    val continueAdState by viewModel.continueAdState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) {
        var cur: android.content.Context? = context
        while (cur is android.content.ContextWrapper && cur !is Activity) {
            cur = cur.baseContext
        }
        cur as? Activity
    }
    val density = LocalDensity.current
    val haptics = LocalHapticFeedback.current
    // Clearance between pointer finger and bottom edge of lifted floating piece
    val dragLiftPx = remember(density) { with(density) { 72.dp.toPx() } }

    var rootOrigin by remember { mutableStateOf(Offset.Zero) }
    var boardBoundsInRoot by remember { mutableStateOf(Rect.Zero) }

    BackHandler(enabled = state.showContinueOffer) {
        viewModel.declineContinueOffer()
    }

    BackHandler(
        enabled = (state.status == GameStatus.PLAYING || state.status == GameStatus.RESOLVING_CASCADE) &&
            !state.showContinueOffer
    ) {
        viewModel.pauseGame()
        onOpenPause()
    }

    // Combo/clear haptics are issued once per turn by GameViewModel via HapticsManager (no second system here).

    LaunchedEffect(state.status, state.showContinueOffer) {
        if (state.showContinueOffer) return@LaunchedEffect
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
            val wideBoardMax = maxHeight.coerceAtLeast(120.dp).coerceAtMost(500.dp)

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
                        GameBoard(
                            board = state.board,
                            visualPhase = visualPhase,
                            dragState = state.dragState,
                            floatingBanner = state.floatingBanner,
                            particleTriggerId = state.particleTriggerId,
                            latestParticles = state.latestParticles,
                            hasSelectedSlot = state.selectedSlotIndex != null,
                            isHammerMode = state.isHammerMode,
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
                            onPause = {
                                viewModel.pauseGame()
                                onOpenPause()
                            },
                            onBack = {
                                viewModel.pauseGame()
                                onOpenPause()
                            }
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
                        PowerUpToolbar(
                            shuffleCount = state.wallet.shuffleCount,
                            shuffleEnabled = state.status == GameStatus.PLAYING &&
                                state.dragState == null &&
                                !state.showContinueOffer &&
                                state.wallet.shuffleCount > 0 &&
                                state.traySlots.any { it.piece != null },
                            onShuffleClick = viewModel::useShuffleBooster,
                            hammerCount = state.wallet.hammerCount,
                            hammerActive = state.isHammerMode,
                            hammerEnabled = state.status == GameStatus.PLAYING &&
                                state.dragState == null &&
                                !state.showContinueOffer &&
                                (state.isHammerMode || (state.wallet.hammerCount > 0 && !state.board.isEmpty())),
                            onHammerClick = viewModel::toggleHammerMode,
                            refreshCount = state.wallet.refreshCount,
                            refreshEnabled = state.status == GameStatus.PLAYING &&
                                state.dragState == null &&
                                !state.showContinueOffer &&
                                state.wallet.refreshCount > 0,
                            onRefreshClick = viewModel::useRefreshBooster
                        )
                        BannerAdBar()
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
                            onPause = {
                                viewModel.pauseGame()
                                onOpenPause()
                            },
                            onBack = {
                                viewModel.pauseGame()
                                onOpenPause()
                            }
                        )
                        ScorePanel(state = state)
                    }

                    // Centered flexible playfield zone: board position stays 100% locked
                    BoxWithConstraints(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // The board is a square: size it from the space actually left between the
                        // HUD and the dock (extra HUD rows such as the Daily progress bar would
                        // otherwise push the board underneath the piece tray). The callout is now
                        // an overlay inside the board, so no vertical space is reserved for it.
                        val fitSide = minOf(maxWidth, maxHeight, boardMaxWidth).coerceAtLeast(120.dp)
                        GameBoard(
                            board = state.board,
                            visualPhase = visualPhase,
                            dragState = state.dragState,
                            floatingBanner = state.floatingBanner,
                            particleTriggerId = state.particleTriggerId,
                            latestParticles = state.latestParticles,
                            hasSelectedSlot = state.selectedSlotIndex != null,
                            isHammerMode = state.isHammerMode,
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
                                .widthIn(max = fitSide)
                                .fillMaxWidth()
                        )
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
                        PowerUpToolbar(
                            shuffleCount = state.wallet.shuffleCount,
                            shuffleEnabled = state.status == GameStatus.PLAYING &&
                                state.dragState == null &&
                                !state.showContinueOffer &&
                                state.wallet.shuffleCount > 0 &&
                                state.traySlots.any { it.piece != null },
                            onShuffleClick = viewModel::useShuffleBooster,
                            hammerCount = state.wallet.hammerCount,
                            hammerActive = state.isHammerMode,
                            hammerEnabled = state.status == GameStatus.PLAYING &&
                                state.dragState == null &&
                                !state.showContinueOffer &&
                                (state.isHammerMode || (state.wallet.hammerCount > 0 && !state.board.isEmpty())),
                            onHammerClick = viewModel::toggleHammerMode,
                            refreshCount = state.wallet.refreshCount,
                            refreshEnabled = state.status == GameStatus.PLAYING &&
                                state.dragState == null &&
                                !state.showContinueOffer &&
                                state.wallet.refreshCount > 0,
                            onRefreshClick = viewModel::useRefreshBooster
                        )
                        BannerAdBar()
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



        if (state.showContinueOffer) {
            ContinueCountdownOverlay(
                secondsRemaining = state.continueCountdownSeconds,
                score = state.score,
                adReady = continueAdState is AdState.Ready && activity != null,
                onWatchAd = {
                    activity?.let { viewModel.watchContinueRewardedAd(it) }
                },
                onSkip = viewModel::declineContinueOffer
            )
        }
    }
}

@Composable
private fun ContinueCountdownOverlay(
    secondsRemaining: Int,
    score: Int,
    adReady: Boolean,
    onWatchAd: () -> Unit,
    onSkip: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xB80F172A))
            .padding(24.dp)
            .testTag("continue_offer_overlay")
    ) {
        PremiumCard(
            cornerRadius = 20.dp,
            borderColor = AccentGold.copy(alpha = 0.65f),
            modifier = Modifier.widthIn(max = 380.dp).fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(AccentGold.copy(alpha = 0.16f))
                        .border(2.5.dp, AccentGold, CircleShape)
                ) {
                    Text(
                        text = "${secondsRemaining}s",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 26.sp
                        ),
                        color = AccentGold,
                        modifier = Modifier.testTag("continue_countdown_text")
                    )
                }

                Text(
                    text = "Continue Playing?",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    ),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Watch a short rewarded ad to clear center blocks and rescue your $score pt run!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                PremiumPrimaryButton(
                    text = if (adReady) "Watch Ad to Continue" else "Watch Ad to Continue",
                    icon = Icons.Filled.PlayCircle,
                    style = ArcadeButtonStyle.GREEN,
                    enabled = true,
                    onClick = onWatchAd,
                    height = 52.dp,
                    cornerRadius = 14.dp,
                    fontSize = 15.sp,
                    testTag = "continue_watch_ad_button",
                    modifier = Modifier.fillMaxWidth()
                )

                SecondaryButton(
                    text = "No Thanks (${secondsRemaining}s)",
                    accent = TextSecondary,
                    onClick = onSkip,
                    height = 48.dp,
                    testTag = "continue_skip_button"
                )
            }
        }
    }
}

@Composable
private fun PowerUpToolbar(
    shuffleCount: Int,
    shuffleEnabled: Boolean,
    onShuffleClick: () -> Unit,
    hammerCount: Int,
    hammerActive: Boolean,
    hammerEnabled: Boolean,
    onHammerClick: () -> Unit,
    refreshCount: Int,
    refreshEnabled: Boolean,
    onRefreshClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PowerUpActionButton(
            label = "Shuffle",
            count = shuffleCount,
            icon = Icons.Filled.Shuffle,
            accent = Color(0xFFA855F7),
            enabled = shuffleEnabled,
            active = false,
            onClick = onShuffleClick,
            testTag = "shuffle_booster_button",
            modifier = Modifier.weight(1f)
        )
        PowerUpActionButton(
            label = if (hammerActive) "Cancel" else "Hammer",
            count = hammerCount,
            icon = Icons.Filled.Build,
            accent = AccentGold,
            enabled = hammerEnabled,
            active = hammerActive,
            onClick = onHammerClick,
            testTag = "hammer_booster_button",
            modifier = Modifier.weight(1f)
        )
        PowerUpActionButton(
            label = "Refresh",
            count = refreshCount,
            icon = Icons.Filled.Refresh,
            accent = AccentCyan,
            enabled = refreshEnabled,
            active = false,
            onClick = onRefreshClick,
            testTag = "refresh_booster_button",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PowerUpActionButton(
    label: String,
    count: Int,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: Color,
    enabled: Boolean,
    active: Boolean,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(14.dp)
    val interactionSource = remember { MutableInteractionSource() }
    val bgColor = when {
        active -> accent.copy(alpha = 0.28f)
        enabled -> accent.copy(alpha = 0.16f)
        else -> com.example.ui.theme.SurfaceCard.copy(alpha = 0.55f)
    }
    val borderColor = when {
        active -> accent
        enabled -> accent.copy(alpha = 0.65f)
        else -> GlassBorder.copy(alpha = 0.45f)
    }
    val contentTint = when {
        active -> accent
        enabled -> TextPrimary
        else -> TextSecondary.copy(alpha = 0.55f)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(bgColor)
            .border(if (active) 2.dp else 1.2.dp, borderColor, shape)
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = androidx.compose.material3.ripple(color = accent),
                onClick = onClick
            )
            .padding(horizontal = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "$label ($count)",
            tint = if (enabled || active) accent else TextSecondary.copy(alpha = 0.5f),
            modifier = Modifier.size(15.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 11.5.sp
            ),
            color = contentTint,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.width(4.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (enabled || active) accent.copy(alpha = 0.28f) else GlassBorder.copy(alpha = 0.25f))
                .padding(horizontal = 5.dp, vertical = 1.dp)
        ) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                ),
                color = if (enabled || active) accent else TextSecondary.copy(alpha = 0.65f),
                maxLines = 1
            )
        }
    }
}

