package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.BlockColor
import com.example.ui.components.ArcadeTitleLogo
import com.example.ui.components.RoyalSkyBackdrop
import com.example.ui.components.drawBevelBlock
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldBright
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.LocalBlockStyle
import com.example.ui.theme.LocalBoardPalette
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val emblemScale = remember { Animatable(0.76f) }
    val emblemAlpha = remember { Animatable(0f) }
    val progress = remember { Animatable(0f) }
    var hasFinished by remember { mutableStateOf(false) }
    val finishOnce: () -> Unit = {
        if (!hasFinished) {
            hasFinished = true
            onFinished()
        }
    }
    val infiniteTransition = rememberInfiniteTransition(label = "splashAnim")
    val haloPulse by infiniteTransition.animateFloat(
        initialValue = 0.90f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(tween(1100, easing = LinearEasing), RepeatMode.Reverse),
        label = "halo"
    )
    val fallOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(3400, easing = LinearEasing), RepeatMode.Restart),
        label = "fallingBlocks"
    )
    val blockStyle = LocalBlockStyle.current
    val boardPalette = LocalBoardPalette.current

    LaunchedEffect(Unit) {
        launch { emblemAlpha.animateTo(1f, tween(280)) }
        launch { emblemScale.animateTo(1f, spring(dampingRatio = 0.65f, stiffness = 360f)) }
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing)
        )
        delay(100L)
        finishOnce()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = finishOnce
            )
            .testTag("splash_screen")
    ) {
        RoyalSkyBackdrop()

        // Glowing falling crystal blocks in background
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val blockSize = (w * 0.085f).coerceIn(24f, 44f)
            val lanes = listOf(
                Triple(0.11f, 0.15f, BlockColor.CYAN),
                Triple(0.24f, 0.52f, BlockColor.VIOLET),
                Triple(0.77f, 0.25f, BlockColor.AMBER),
                Triple(0.88f, 0.65f, BlockColor.EMERALD),
                Triple(0.16f, 0.80f, BlockColor.CORAL),
                Triple(0.82f, 0.88f, BlockColor.COBALT)
            )
            lanes.forEachIndexed { idx, (xFrac, phase, color) ->
                val p = (fallOffset + phase + idx * 0.13f) % 1f
                val bx = w * xFrac - blockSize / 2f
                val by = -blockSize + p * (h + blockSize * 2f)
                val alpha = kotlin.math.sin(p * kotlin.math.PI).toFloat().coerceIn(0.10f, 0.55f)
                drawBevelBlock(
                    left = bx,
                    top = by,
                    cellSize = blockSize,
                    color = color,
                    alpha = alpha,
                    glowBoost = 0.45f,
                    blockStyle = blockStyle,
                    boardPalette = boardPalette
                )
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 34.dp)
        ) {
            Spacer(Modifier.height(12.dp))

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.graphicsLayer {
                    scaleX = emblemScale.value
                    scaleY = emblemScale.value
                    alpha = emblemAlpha.value
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(190.dp)
                            .graphicsLayer {
                                scaleX = haloPulse
                                scaleY = haloPulse
                            }
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        AccentGold.copy(alpha = 0.36f),
                                        AccentCyan.copy(alpha = 0.20f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(148.dp)
                            .shadow(16.dp, RoundedCornerShape(34.dp), clip = false)
                            .clip(RoundedCornerShape(34.dp))
                            .background(Color(0xFF101C47))
                            .border(
                                width = 3.dp,
                                brush = Brush.linearGradient(
                                    listOf(AccentGoldBright, AccentGold, AccentCyan)
                                ),
                                shape = RoundedCornerShape(34.dp)
                            )
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.img_splash_emblem_1795510317785),
                            contentDescription = "Block Fall Logo",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(32.dp))
                        )
                    }
                }

                Spacer(Modifier.height(22.dp))
                ArcadeTitleLogo(fontSize = 44.sp)
                Spacer(Modifier.height(10.dp))

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(SurfaceCard)
                        .border(1.dp, AccentGold.copy(alpha = 0.55f), CircleShape)
                        .padding(horizontal = 16.dp, vertical = 7.dp)
                ) {
                    Text(
                        text = stringResource(R.string.game_subtitle).uppercase(),
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.6.sp,
                            fontSize = 11.sp
                        ),
                        color = TextSecondary
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .width(236.dp)
                        .height(12.dp)
                        .clip(CircleShape)
                        .background(BoardWellBg)
                        .border(1.2.dp, GlassBorder, CircleShape)
                        .padding(2.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(progress.value.coerceIn(0.08f, 1f))
                            .clip(CircleShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(AccentCyan, AccentGoldBright, AccentGold)
                                )
                            )
                    )
                }

                Text(
                    text = "Tap anywhere to continue",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = TextSecondary
                )
            }
        }
    }
}
