package com.example.ui

import com.example.audio.SoundManager
import com.example.game.CascadeResolution
import com.example.game.GridCoord
import kotlinx.coroutines.delay
import kotlin.math.pow

/**
 * Orchestrates multi-wave line-clear, gravity-fall, and block-landing visual phases
 * on the decoupled [BoardVisualPhase] callback without triggering HUD recompositions.
 */
class CascadeAnimationRunner(
    private val soundManager: SoundManager
) {
    private var bannerCounter: Long = 1L
    private var particleCounter: Long = 1L

    suspend fun runCascade(
        resolution: CascadeResolution,
        startingScore: Int,
        visualEffectsEnabled: Boolean,
        onVisualPhase: (BoardVisualPhase) -> Unit,
        onWaveStarted: (waveIndex: Int, banner: FloatingBanner, particleIdDelta: Long?, particles: List<ParticleBurst>) -> Unit,
        onWaveCleared: (boardAfterClear: com.example.game.Board, runningScore: Int, waveLines: Int) -> Unit,
        onGravityLanded: (boardAfterGravity: com.example.game.Board, particleIdDelta: Long?, dustParticles: List<ParticleBurst>) -> Unit
    ) {
        var runningScore = startingScore

        for (wave in resolution.waves) {
            val waveLines = wave.clearedRows.size + wave.clearedCols.size

            if (wave.waveIndex >= 2) {
                soundManager.playComboComplete(comboDepth = wave.waveIndex)
            } else {
                soundManager.playLineClear(linesCleared = waveLines)
            }

            val isLastWave = wave === resolution.waves.last()
            val banner = buildBannerForWave(
                waveIndex = wave.waveIndex,
                linesCount = waveLines,
                points = wave.scoreBreakdown.totalPoints,
                isFullClear = wave.isFullClear,
                turnTotalPoints = if (isLastWave && resolution.waves.size > 1) resolution.totalWavePoints else null
            )

            val clearParticles = if (visualEffectsEnabled) {
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

            onWaveStarted(
                wave.waveIndex,
                banner,
                if (visualEffectsEnabled) particleCounter++ else null,
                clearParticles
            )

            animateStep(durationMs = 210L) { progress ->
                onVisualPhase(
                    BoardVisualPhase.ClearingLines(
                        boardBeforeClear = wave.boardBeforeClear,
                        clearedCoords = wave.clearedCoords,
                        clearedRows = wave.clearedRows,
                        clearedCols = wave.clearedCols,
                        waveIndex = wave.waveIndex,
                        progress = progress
                    )
                )
            }

            runningScore += wave.scoreBreakdown.totalPoints
            onWaveCleared(wave.boardAfterClear, runningScore, waveLines)

            if (wave.movements.isNotEmpty()) {
                val maxDist = wave.movements.maxOf { it.distance }
                val fallDuration = (190L + maxDist * 30L).coerceAtMost(360L)
                animateStep(durationMs = fallDuration) { linear ->
                    val gravityEase = linear.pow(1.45f).coerceIn(0f, 1f)
                    onVisualPhase(
                        BoardVisualPhase.FallingGravity(
                            boardAfterClear = wave.boardAfterClear,
                            movements = wave.movements,
                            waveIndex = wave.waveIndex,
                            progress = gravityEase
                        )
                    )
                }

                val landedCoords = wave.movements.map { GridCoord(it.toRow, it.col) }.toSet()
                val dustParticles = if (visualEffectsEnabled) {
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

                onGravityLanded(
                    wave.boardAfterGravity,
                    if (dustParticles.isNotEmpty()) particleCounter++ else null,
                    dustParticles
                )
                // Softer, lower "landing thud" for gravity; distinct from the placement drop played in the ViewModel.
                soundManager.playBlockDrop(volume = 0.5f, rate = 0.88f)

                animateStep(durationMs = 95L) { progress ->
                    onVisualPhase(
                        BoardVisualPhase.LandingSettled(
                            board = wave.boardAfterGravity,
                            landedCoords = landedCoords,
                            progress = progress
                        )
                    )
                }
            } else {
                onGravityLanded(wave.boardAfterGravity, null, emptyList())
                delay(60L)
            }
        }

        // The combo sound already plays once per wave >= 2 above; no second end-of-cascade cue.
        onVisualPhase(BoardVisualPhase.Idle)
    }

    suspend fun scheduleBannerDismiss(bannerId: Long, onDismiss: (Long) -> Unit) {
        delay(1100L)
        onDismiss(bannerId)
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
        isFullClear: Boolean,
        turnTotalPoints: Int? = null
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
            isChain = waveIndex >= 2 || isFullClear,
            depth = waveIndex,
            linesCount = linesCount,
            isFullClear = isFullClear,
            turnTotalPoints = turnTotalPoints
        )
    }
}
