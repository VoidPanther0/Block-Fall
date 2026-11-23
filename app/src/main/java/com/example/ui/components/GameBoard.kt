package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.game.BlockStyle
import com.example.game.Board
import com.example.game.GridCoord
import com.example.ui.BoardVisualPhase
import com.example.ui.DragState
import com.example.ui.FloatingBanner
import com.example.ui.ParticleBurst
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.InvalidTint
import com.example.ui.theme.LocalBlockStyle
import com.example.ui.theme.LocalBoardPalette
import com.example.ui.theme.toBoardColors
import com.example.ui.theme.toPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameBoard(
    board: Board,
    visualPhase: BoardVisualPhase,
    dragState: DragState?,
    @Suppress("UNUSED_PARAMETER") floatingBanner: FloatingBanner?,
    particleTriggerId: Long,
    latestParticles: List<ParticleBurst>,
    hasSelectedSlot: Boolean,
    onCellTapped: (Int, Int) -> Unit,
    onBoardBoundsChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier,
    visualEffectsEnabled: Boolean = true,
    placementPreviewEnabled: Boolean = true,
    onBoardTouchStart: ((Offset) -> Unit)? = null,
    onBoardTouchMove: ((Offset) -> Unit)? = null,
    onBoardTouchEnd: (() -> Unit)? = null,
    onBoardTouchCancel: (() -> Unit)? = null
) {
    val blockStyle = LocalBlockStyle.current
    val boardPalette = LocalBoardPalette.current
    val boardTheme = remember(boardPalette) { boardPalette.toBoardColors() }
    val boardBrush = remember(boardTheme) {
        Brush.verticalGradient(listOf(boardTheme.wellTop, boardTheme.wellBottom))
    }
    val boardCorner = if (blockStyle == BlockStyle.RETRO) 10.dp else 16.dp
    val boardShape = RoundedCornerShape(boardCorner)

    val particleProgress = remember { Animatable(1f) }
    LaunchedEffect(particleTriggerId, visualEffectsEnabled) {
        if (visualEffectsEnabled && particleTriggerId > 0L && latestParticles.isNotEmpty()) {
            particleProgress.snapTo(0f)
            particleProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 320, easing = FastOutSlowInEasing)
            )
        }
    }

    val isInvalidHover = dragState != null && dragState.hoverAnchor != null && !dragState.isHoverValid
    val rimColor = when {
        isInvalidHover -> InvalidTint.copy(alpha = 0.9f)
        hasSelectedSlot && dragState == null -> AccentCyan.copy(alpha = 0.82f)
        else -> boardTheme.rim
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(4.dp, boardShape, clip = false)
            .clip(boardShape)
            .background(boardBrush)
            .border(2.dp, rimColor, boardShape)
            .padding(8.dp)
            .testTag("game_board")
            .semantics {
                contentDescription = "8 by 8 Block Falls puzzle board"
            }
    ) {
        val currentHasSelectedSlot by rememberUpdatedState(hasSelectedSlot)
        val currentOnCellTapped by rememberUpdatedState(onCellTapped)
        val currentOnBoardTouchStart by rememberUpdatedState(onBoardTouchStart)
        val currentOnBoardTouchMove by rememberUpdatedState(onBoardTouchMove)
        val currentOnBoardTouchEnd by rememberUpdatedState(onBoardTouchEnd)
        val currentOnBoardTouchCancel by rememberUpdatedState(onBoardTouchCancel)

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coords ->
                    onBoardBoundsChanged(coords.boundsInRoot())
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        if (!currentHasSelectedSlot) return@awaitEachGesture
                        down.consume()
                        val touchStart = currentOnBoardTouchStart
                        val touchMove = currentOnBoardTouchMove
                        val touchEnd = currentOnBoardTouchEnd
                        if (touchStart != null && touchMove != null && touchEnd != null) {
                            touchStart(down.position)
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id }
                                if (change == null) {
                                    currentOnBoardTouchCancel?.invoke()
                                    break
                                }
                                if (!change.pressed) {
                                    change.consume()
                                    touchEnd()
                                    break
                                }
                                change.consume()
                                touchMove(change.position)
                            }
                        } else {
                            var lastPos = down.position
                            while (true) {
                                val event = awaitPointerEvent()
                                val change = event.changes.firstOrNull { it.id == down.id } ?: break
                                lastPos = change.position
                                if (!change.pressed) {
                                    val cellSize = size.width / Board.SIZE.toFloat()
                                    if (cellSize > 0f) {
                                        val col = (lastPos.x / cellSize).toInt().coerceIn(0, Board.SIZE - 1)
                                        val row = (lastPos.y / cellSize).toInt().coerceIn(0, Board.SIZE - 1)
                                        currentOnCellTapped(row, col)
                                    }
                                    break
                                }
                            }
                        }
                    }
                }
        ) {
            val cellSize = size.width / Board.SIZE.toFloat()
            val gap = (cellSize * 0.085f).coerceAtLeast(2.5f)
            val tileSize = cellSize - gap
            val slotRadius = if (blockStyle == BlockStyle.RETRO) {
                CornerRadius(2f, 2f)
            } else {
                CornerRadius(tileSize * 0.2f, tileSize * 0.2f)
            }

            // 1. Sculpted 8x8 recessed cell wells with subtle checkerboard contrast
            for (r in 0 until Board.SIZE) {
                for (c in 0 until Board.SIZE) {
                    val left = c * cellSize + gap / 2f
                    val top = r * cellSize + gap / 2f
                    val subtleChecker = if ((r + c) % 2 == 0) {
                        boardTheme.slotFill
                    } else {
                        boardTheme.slotFill.copy(alpha = 0.82f)
                    }
                    drawRoundRect(
                        color = subtleChecker,
                        topLeft = Offset(left, top),
                        size = Size(tileSize, tileSize),
                        cornerRadius = slotRadius
                    )
                    drawRoundRect(
                        color = boardTheme.slotBorder.copy(alpha = 0.68f),
                        topLeft = Offset(left, top),
                        size = Size(tileSize, tileSize),
                        cornerRadius = slotRadius,
                        style = Stroke(width = 1f)
                    )
                }
            }

            // 2. Predictive line-clear highlight while hovering a valid placement
            if (placementPreviewEnabled && dragState != null && dragState.isHoverValid) {
                for (rowIdx in dragState.predictedClearRows) {
                    drawRoundRect(
                        color = AccentGold.copy(alpha = 0.22f),
                        topLeft = Offset(0f, rowIdx * cellSize),
                        size = Size(size.width, cellSize),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = AccentGold.copy(alpha = 0.78f),
                        topLeft = Offset(1f, rowIdx * cellSize + 1f),
                        size = Size(size.width - 2f, cellSize - 2f),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Stroke(width = 2f)
                    )
                }
                for (colIdx in dragState.predictedClearCols) {
                    drawRoundRect(
                        color = AccentGold.copy(alpha = 0.22f),
                        topLeft = Offset(colIdx * cellSize, 0f),
                        size = Size(cellSize, size.height),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                    drawRoundRect(
                        color = AccentGold.copy(alpha = 0.78f),
                        topLeft = Offset(colIdx * cellSize + 1f, 1f),
                        size = Size(cellSize - 2f, size.height - 2f),
                        cornerRadius = CornerRadius(8f, 8f),
                        style = Stroke(width = 2f)
                    )
                }
            }

            // 3. Render board blocks based on current animation phase
            when (visualPhase) {
                is BoardVisualPhase.Idle -> {
                    for (r in 0 until Board.SIZE) {
                        for (c in 0 until Board.SIZE) {
                            val cell = board.get(r, c) ?: continue
                            drawBevelBlock(
                                left = c * cellSize + gap / 2f,
                                top = r * cellSize + gap / 2f,
                                cellSize = tileSize,
                                color = cell.color,
                                blockStyle = blockStyle,
                                boardPalette = boardPalette
                            )
                        }
                    }
                }

                is BoardVisualPhase.ClearingLines -> {
                    val p = visualPhase.progress
                    val b = visualPhase.boardBeforeClear

                    val sweepAlpha = (1f - p) * 0.58f
                    for (r in visualPhase.clearedRows) {
                        drawRoundRect(
                            brush = Brush.horizontalGradient(
                                listOf(Color.Transparent, Color.White.copy(alpha = sweepAlpha), AccentGold.copy(alpha = sweepAlpha), Color.Transparent)
                            ),
                            topLeft = Offset(0f, r * cellSize),
                            size = Size(size.width, cellSize),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }
                    for (c in visualPhase.clearedCols) {
                        drawRoundRect(
                            brush = Brush.verticalGradient(
                                listOf(Color.Transparent, Color.White.copy(alpha = sweepAlpha), AccentGold.copy(alpha = sweepAlpha), Color.Transparent)
                            ),
                            topLeft = Offset(c * cellSize, 0f),
                            size = Size(cellSize, size.height),
                            cornerRadius = CornerRadius(10f, 10f)
                        )
                    }

                    for (r in 0 until Board.SIZE) {
                        for (c in 0 until Board.SIZE) {
                            val cell = b.get(r, c) ?: continue
                            val left = c * cellSize + gap / 2f
                            val top = r * cellSize + gap / 2f
                            if (GridCoord(r, c) in visualPhase.clearedCoords) {
                                val shrink = (1f - p * 0.72f).coerceAtLeast(0.15f)
                                val fade = (1f - p).coerceIn(0f, 1f)
                                drawBevelBlock(
                                    left = left,
                                    top = top,
                                    cellSize = tileSize,
                                    color = cell.color,
                                    alpha = fade,
                                    scaleX = shrink,
                                    scaleY = shrink,
                                    glowBoost = 1.25f,
                                    blockStyle = blockStyle,
                                    boardPalette = boardPalette
                                )
                            } else {
                                drawBevelBlock(
                                    left = left,
                                    top = top,
                                    cellSize = tileSize,
                                    color = cell.color,
                                    blockStyle = blockStyle,
                                    boardPalette = boardPalette
                                )
                            }
                        }
                    }
                }

                is BoardVisualPhase.FallingGravity -> {
                    val b = visualPhase.boardAfterClear

                    for (r in 0 until Board.SIZE) {
                        for (c in 0 until Board.SIZE) {
                            val cell = b.get(r, c) ?: continue
                            if (visualPhase.movements.any { it.cell.id == cell.id }) continue
                            drawBevelBlock(
                                left = c * cellSize + gap / 2f,
                                top = r * cellSize + gap / 2f,
                                cellSize = tileSize,
                                color = cell.color,
                                blockStyle = blockStyle,
                                boardPalette = boardPalette
                            )
                        }
                    }

                    val p = visualPhase.progress
                    for (move in visualPhase.movements) {
                        val interpRow = move.fromRow + (move.toRow - move.fromRow) * p
                        drawBevelBlock(
                            left = move.col * cellSize + gap / 2f,
                            top = interpRow * cellSize + gap / 2f,
                            cellSize = tileSize,
                            color = move.cell.color,
                            glowBoost = 0.28f,
                            blockStyle = blockStyle,
                            boardPalette = boardPalette
                        )
                    }
                }

                is BoardVisualPhase.LandingSettled -> {
                    val bounce = sin(visualPhase.progress * PI.toFloat()).coerceIn(0f, 1f)
                    val sx = 1f + bounce * 0.07f
                    val sy = 1f - bounce * 0.11f
                    for (r in 0 until Board.SIZE) {
                        for (c in 0 until Board.SIZE) {
                            val cell = visualPhase.board.get(r, c) ?: continue
                            val isLanded = GridCoord(r, c) in visualPhase.landedCoords
                            drawBevelBlock(
                                left = c * cellSize + gap / 2f,
                                top = r * cellSize + gap / 2f,
                                cellSize = tileSize,
                                color = cell.color,
                                scaleX = if (isLanded) sx else 1f,
                                scaleY = if (isLanded) sy else 1f,
                                blockStyle = blockStyle,
                                boardPalette = boardPalette
                            )
                        }
                    }
                }
            }

            // 4. Ghost placement preview & invalid placement feedback during drag
            if (placementPreviewEnabled && dragState != null && dragState.hoverAnchor != null) {
                val anchor = dragState.hoverAnchor
                val coords = dragState.piece.cellsAt(anchor.row, anchor.col)
                for (coord in coords) {
                    if (coord.row !in 0 until Board.SIZE || coord.col !in 0 until Board.SIZE) continue
                    val left = coord.col * cellSize + gap / 2f
                    val top = coord.row * cellSize + gap / 2f
                    if (dragState.isHoverValid) {
                        drawBevelBlock(
                            left = left,
                            top = top,
                            cellSize = tileSize,
                            color = dragState.piece.color,
                            alpha = 0.55f,
                            glowBoost = 0f,
                            blockStyle = blockStyle,
                            boardPalette = boardPalette
                        )
                    } else {
                        drawRoundRect(
                            color = InvalidTint.copy(alpha = 0.28f),
                            topLeft = Offset(left, top),
                            size = Size(tileSize, tileSize),
                            cornerRadius = slotRadius
                        )
                        drawRoundRect(
                            color = InvalidTint.copy(alpha = 0.88f),
                            topLeft = Offset(left, top),
                            size = Size(tileSize, tileSize),
                            cornerRadius = slotRadius,
                            style = Stroke(width = 2.2f)
                        )
                    }
                }
            }

            // 5. Lightweight allocation-free particle bursts on clear / landing
            val pProg = particleProgress.value
            if (visualEffectsEnabled && pProg < 0.99f && latestParticles.isNotEmpty()) {
                val alpha = (1f - pProg).coerceIn(0f, 1f)
                val sparkRadius = (cellSize * 0.085f * (1f - pProg * 0.45f)).coerceAtLeast(1.5f)
                for (burst in latestParticles) {
                    val cx = burst.col * cellSize
                    val cy = burst.row * cellSize
                    val palette = burst.color.toPalette(boardPalette)
                    if (burst.isLandingDust) {
                        val spread = cellSize * 0.34f * pProg
                        drawCircle(
                            color = palette.highlight.copy(alpha = alpha * 0.55f),
                            radius = sparkRadius * 0.85f,
                            center = Offset(cx - spread, cy)
                        )
                        drawCircle(
                            color = palette.highlight.copy(alpha = alpha * 0.55f),
                            radius = sparkRadius * 0.85f,
                            center = Offset(cx + spread, cy)
                        )
                    } else {
                        val dist = cellSize * 0.58f * pProg
                        for (k in 0 until 4) {
                            val angle = (k * (PI / 2.0) + (burst.id % 4) * 0.35).toFloat()
                            val px = cx + cos(angle) * dist
                            val py = cy + sin(angle) * dist
                            drawCircle(
                                color = palette.glow.copy(alpha = alpha * 0.85f),
                                radius = sparkRadius,
                                center = Offset(px, py)
                            )
                        }
                    }
                }
            }

            // Inner glass rim — subtle depth without an expensive blur.
            drawRoundRect(
                color = Color.White.copy(alpha = 0.10f),
                topLeft = Offset(1f, 1f),
                size = Size(size.width - 2f, size.height - 2f),
                cornerRadius = slotRadius,
                style = Stroke(width = 1f)
            )
        }
    }
}
