package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.Board
import com.example.game.GridCoord
import com.example.ui.BoardVisualPhase
import com.example.ui.DragState
import com.example.ui.FloatingBanner
import com.example.ui.ParticleBurst
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardRim
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.EmptySlotBorder
import com.example.ui.theme.EmptySlotFill
import com.example.ui.theme.InvalidTint
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.toPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private class ActiveParticle(
    var row: Float,
    var col: Float,
    val vRow: Float,
    val vCol: Float,
    val color: Color,
    val radiusFactor: Float,
    var life: Float,
    val decayPerSec: Float
)

@Composable
fun GameBoard(
    board: Board,
    visualPhase: BoardVisualPhase,
    dragState: DragState?,
    floatingBanner: FloatingBanner?,
    particleTriggerId: Long,
    latestParticles: List<ParticleBurst>,
    hasSelectedSlot: Boolean,
    onCellTapped: (Int, Int) -> Unit,
    onBoardBoundsChanged: (Rect) -> Unit,
    modifier: Modifier = Modifier,
    visualEffectsEnabled: Boolean = true,
    placementPreviewEnabled: Boolean = true
) {
    val activeParticles = remember { mutableStateListOf<ActiveParticle>() }

    LaunchedEffect(particleTriggerId, visualEffectsEnabled) {
        if (!visualEffectsEnabled || latestParticles.isEmpty()) return@LaunchedEffect
        val rng = Random(particleTriggerId)
        for (burst in latestParticles) {
            val baseColor = burst.color.toPalette().glow
            val count = if (burst.isLandingDust) 4 else 7
            repeat(count) {
                val angle = if (burst.isLandingDust) {
                    // Spread horizontally along floor impact
                    val side = if (rng.nextBoolean()) 0f else PI.toFloat()
                    side + (rng.nextFloat() - 0.5f) * 0.9f
                } else {
                    rng.nextFloat() * (2f * PI.toFloat())
                }
                val speed = if (burst.isLandingDust) {
                    1.2f + rng.nextFloat() * 1.8f
                } else {
                    1.8f + rng.nextFloat() * 3.8f
                }
                activeParticles.add(
                    ActiveParticle(
                        row = burst.row,
                        col = burst.col,
                        vRow = sin(angle) * speed * (if (burst.isLandingDust) 0.45f else 1f),
                        vCol = cos(angle) * speed,
                        color = if (burst.isLandingDust) Color.White.copy(alpha = 0.7f) else baseColor,
                        radiusFactor = if (burst.isLandingDust) 0.065f else 0.095f,
                        life = 1f,
                        decayPerSec = if (burst.isLandingDust) 3.4f else 2.2f
                    )
                )
            }
        }
    }

    LaunchedEffect(activeParticles.isNotEmpty()) {
        if (activeParticles.isEmpty()) return@LaunchedEffect
        var lastTime = withFrameMillis { it }
        while (activeParticles.isNotEmpty()) {
            val now = withFrameMillis { it }
            val dt = ((now - lastTime).coerceIn(1L, 48L)) / 1000f
            lastTime = now
            val iterator = activeParticles.listIterator()
            while (iterator.hasNext()) {
                val p = iterator.next()
                p.row += p.vRow * dt + 2.4f * dt * dt
                p.col += p.vCol * dt
                p.life -= p.decayPerSec * dt
                if (p.life <= 0f) {
                    iterator.remove()
                }
            }
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(20.dp))
            .background(BoardWellBg)
            .border(1.5.dp, BoardRim, RoundedCornerShape(20.dp))
            .padding(10.dp)
            .testTag("game_board")
            .semantics {
                contentDescription = "8 by 8 Cascade puzzle board"
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coords ->
                    onBoardBoundsChanged(coords.boundsInRoot())
                }
                .pointerInput(hasSelectedSlot) {
                    detectTapGestures { offset ->
                        if (!hasSelectedSlot) return@detectTapGestures
                        val cellSize = size.width / Board.SIZE.toFloat()
                        if (cellSize > 0f) {
                            val col = (offset.x / cellSize).toInt().coerceIn(0, Board.SIZE - 1)
                            val row = (offset.y / cellSize).toInt().coerceIn(0, Board.SIZE - 1)
                            onCellTapped(row, col)
                        }
                    }
                }
        ) {
            val cellSize = size.width / Board.SIZE.toFloat()
            val gap = (cellSize * 0.09f).coerceAtLeast(2.5f)
            val tileSize = cellSize - gap
            val slotRadius = CornerRadius(tileSize * 0.18f, tileSize * 0.18f)

            // 1. Draw recessed 8x8 empty slots
            for (r in 0 until Board.SIZE) {
                for (c in 0 until Board.SIZE) {
                    val left = c * cellSize + gap / 2f
                    val top = r * cellSize + gap / 2f
                    drawRoundRect(
                        color = EmptySlotFill,
                        topLeft = Offset(left, top),
                        size = Size(tileSize, tileSize),
                        cornerRadius = slotRadius
                    )
                    drawRoundRect(
                        color = EmptySlotBorder,
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
                        color = AccentGold.copy(alpha = 0.7f),
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
                        color = AccentGold.copy(alpha = 0.7f),
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
                                color = cell.color
                            )
                        }
                    }
                }

                is BoardVisualPhase.ClearingLines -> {
                    val p = visualPhase.progress
                    val b = visualPhase.boardBeforeClear

                    // Sweep highlight behind clearing rows & columns
                    val sweepAlpha = (1f - p) * 0.55f
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
                                    glowBoost = 1.2f
                                )
                            } else {
                                drawBevelBlock(
                                    left = left,
                                    top = top,
                                    cellSize = tileSize,
                                    color = cell.color
                                )
                            }
                        }
                    }
                }

                is BoardVisualPhase.FallingGravity -> {
                    val movingIds = visualPhase.movements.map { it.cell.id }.toSet()
                    val b = visualPhase.boardAfterClear

                    // Draw stationary blocks first
                    for (r in 0 until Board.SIZE) {
                        for (c in 0 until Board.SIZE) {
                            val cell = b.get(r, c) ?: continue
                            if (cell.id in movingIds) continue
                            drawBevelBlock(
                                left = c * cellSize + gap / 2f,
                                top = r * cellSize + gap / 2f,
                                cellSize = tileSize,
                                color = cell.color
                            )
                        }
                    }

                    // Draw falling blocks smoothly interpolated from oldRow -> newRow
                    val p = visualPhase.progress
                    for (move in visualPhase.movements) {
                        val interpRow = move.fromRow + (move.toRow - move.fromRow) * p
                        drawBevelBlock(
                            left = move.col * cellSize + gap / 2f,
                            top = interpRow * cellSize + gap / 2f,
                            cellSize = tileSize,
                            color = move.cell.color,
                            glowBoost = 0.25f
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
                                scaleY = if (isLanded) sy else 1f
                            )
                        }
                    }
                }
            }

            // 4. Ghost placement preview during drag
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
                            alpha = 0.45f,
                            glowBoost = if (visualEffectsEnabled) 0.5f else 0f
                        )
                    } else {
                        drawRoundRect(
                            color = InvalidTint.copy(alpha = 0.25f),
                            topLeft = Offset(left, top),
                            size = Size(tileSize, tileSize),
                            cornerRadius = slotRadius
                        )
                        drawRoundRect(
                            color = InvalidTint.copy(alpha = 0.8f),
                            topLeft = Offset(left, top),
                            size = Size(tileSize, tileSize),
                            cornerRadius = slotRadius,
                            style = Stroke(width = 2f)
                        )
                    }
                }
            }

            // 5. Render active particles (clear sparks + gravity landing dust)
            if (visualEffectsEnabled) {
                for (particle in activeParticles) {
                    val px = particle.col * cellSize
                    val py = particle.row * cellSize
                    val radius = cellSize * particle.radiusFactor * particle.life.coerceIn(0.2f, 1f)
                    drawCircle(
                        color = particle.color.copy(alpha = particle.life.coerceIn(0f, 1f)),
                        radius = radius,
                        center = Offset(px, py)
                    )
                }
            }
        }

        // 6. Floating Cascade / Chain Reaction Callout Banner
        AnimatedVisibility(
            visible = floatingBanner != null,
            enter = fadeIn() + scaleIn(initialScale = 0.8f),
            exit = fadeOut() + scaleOut(targetScale = 0.9f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 14.dp)
        ) {
            floatingBanner?.let { banner ->
                val accent = if (banner.isChain) AccentGold else AccentCyan
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCardElevated.copy(alpha = 0.94f))
                        .border(1.5.dp, accent.copy(alpha = 0.85f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .testTag("cascade_banner")
                ) {
                    Text(
                        text = "${banner.headline}  ${banner.pointsText}",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 15.sp,
                            letterSpacing = 0.6.sp
                        ),
                        color = accent
                    )
                    if (banner.subline != null) {
                        Text(
                            text = banner.subline,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = Color.White.copy(alpha = 0.88f)
                        )
                    }
                }
            }
        }
    }
}
