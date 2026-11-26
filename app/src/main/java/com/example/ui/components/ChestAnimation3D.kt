package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldBright
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/**
 * Interactive 3D Treasure Chest animation driven by the 6 Gemini-rendered 3D chest opening frames:
 * Closed -> Tremble/Glow -> Crack Open -> Coins/Gems Spill -> Burst Rays -> Settled Open.
 * Supports either one-shot claim animation or continuous GIF-like preview looping.
 */
@Composable
fun AnimatedTreasureChest3D(
    modifier: Modifier = Modifier,
    size: Dp = 176.dp,
    autoPlay: Boolean = true,
    loopingPreview: Boolean = false,
    replayTrigger: Int = 0,
    onAnimationComplete: () -> Unit = {},
    onClick: (() -> Unit)? = null
) {
    val frames = GameAssetCatalog.chestOpenFrames
    var frameIndex by remember { mutableIntStateOf(0) }
    val scaleAnim = remember { Animatable(0.88f) }
    val shakeX = remember { Animatable(0f) }
    val burstAlpha = remember { Animatable(0f) }

    val transition = rememberInfiniteTransition(label = "chestAura")
    val auraRotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(11000, easing = LinearEasing)),
        label = "raysRotate"
    )
    val bobOffset by transition.animateFloat(
        initialValue = -3.5f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(tween(1500, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "bob"
    )

    LaunchedEffect(autoPlay, loopingPreview, replayTrigger) {
        if (!autoPlay) {
            frameIndex = 0
            scaleAnim.snapTo(1f)
            burstAlpha.snapTo(0.25f)
            return@LaunchedEffect
        }
        do {
            frameIndex = 0
            burstAlpha.snapTo(0.2f)
            scaleAnim.snapTo(0.88f)
            scaleAnim.animateTo(1.0f, spring(dampingRatio = 0.65f, stiffness = 360f))
            delay(200L)

            // Frame 2: Tremble & golden seam glow
            frameIndex = 1
            burstAlpha.animateTo(0.55f, tween(200))
            repeat(4) { step ->
                shakeX.animateTo(if (step % 2 == 0) 6f else -6f, tween(48))
            }
            shakeX.animateTo(0f, tween(50))

            // Frame 3: Lid pops open 40° with cyan/gold rays
            frameIndex = 2
            launch { scaleAnim.animateTo(1.08f, tween(130)) }
            delay(150L)

            // Frame 4: Lid 75° overflowing with gold coins and diamonds
            frameIndex = 3
            launch { scaleAnim.animateTo(1.13f, spring(dampingRatio = 0.55f, stiffness = 420f)) }
            delay(150L)

            // Frame 5: Peak golden burst & flying particles
            frameIndex = 4
            launch { burstAlpha.animateTo(1f, tween(180)) }
            delay(260L)

            // Frame 6: Settled open glowing treasure chest
            frameIndex = 5
            launch { scaleAnim.animateTo(1.0f, spring(dampingRatio = 0.65f, stiffness = 320f)) }
            onAnimationComplete()

            if (loopingPreview) {
                delay(1100L)
            }
        } while (loopingPreview)
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            )
    ) {
        // Rotating radiant god-rays behind chest
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationZ = auraRotation
                    alpha = burstAlpha.value
                }
        ) {
            val c = Offset(this.size.width / 2f, this.size.height / 2f)
            val maxR = this.size.minDimension * 0.54f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        AccentGoldBright.copy(alpha = 0.42f),
                        AccentCyan.copy(alpha = 0.18f),
                        Color.Transparent
                    ),
                    center = c,
                    radius = maxR
                ),
                radius = maxR,
                center = c
            )
            val rayCount = 10
            for (i in 0 until rayCount) {
                val baseAngle = (i * 2.0 * Math.PI / rayCount).toFloat()
                val spread = 0.14f
                val path = Path().apply {
                    moveTo(c.x, c.y)
                    lineTo(c.x + maxR * cos(baseAngle - spread), c.y + maxR * sin(baseAngle - spread))
                    lineTo(c.x + maxR * cos(baseAngle + spread), c.y + maxR * sin(baseAngle + spread))
                    close()
                }
                drawPath(
                    path = path,
                    color = (if (i % 2 == 0) AccentGoldBright else AccentCyan).copy(alpha = 0.16f)
                )
            }
        }

        // Main 3D Chest keyframe PNG
        Image(
            painter = painterResource(id = frames[frameIndex.coerceIn(0, frames.lastIndex)]),
            contentDescription = "3D Treasure Chest Animation",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .size(size * 0.86f)
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                    translationX = shakeX.value
                    translationY = bobOffset
                }
        )

        // Sparkle & burst overlay on peak opening frames
        if (frameIndex >= 3) {
            GameAssetIcon(
                resId = R.drawable.img_fx_sparkles,
                contentDescription = null,
                size = size * 0.24f,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-6).dp, y = 8.dp)
            )
            GameAssetIcon(
                resId = R.drawable.img_fx_gem_burst,
                contentDescription = null,
                size = size * 0.22f,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(x = 8.dp, y = 12.dp)
            )
        }
    }
}

/**
 * Full-screen 3D Treasure Chest Opening Celebration dialog used for Daily Rewards,
 * Lucky Wheel jackpots, and Event / Chest previews.
 */
@Composable
fun ChestOpenCelebrationDialog(
    title: String,
    subtitle: String,
    goldEarned: Int = 0,
    diamondsEarned: Int = 0,
    bonusLabel: String? = null,
    onDismiss: () -> Unit
) {
    BackHandler(onBack = onDismiss)
    var animDone by remember { mutableStateOf(false) }
    var replayCount by remember { mutableIntStateOf(0) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        PremiumCard(
            cornerRadius = 24.dp,
            borderColor = AccentGoldBright.copy(alpha = 0.7f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp)
                .widthIn(max = 400.dp)
                .testTag("chest_celebration_dialog")
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 22.dp)
            ) {
                GoldMetallicBanner(
                    text = title.uppercase(),
                    isPurple = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                AnimatedTreasureChest3D(
                    size = 188.dp,
                    autoPlay = true,
                    loopingPreview = false,
                    replayTrigger = replayCount,
                    onAnimationComplete = { animDone = true },
                    onClick = {
                        animDone = false
                        replayCount++
                    }
                )

                AnimatedVisibility(
                    visible = animDone,
                    enter = fadeIn(tween(240)) + scaleIn(initialScale = 0.85f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (goldEarned > 0) {
                                ChestRewardPill(
                                    resId = R.drawable.img_coin_pile,
                                    label = "+$goldEarned Gold",
                                    accent = AccentGold
                                )
                            }
                            if (diamondsEarned > 0) {
                                ChestRewardPill(
                                    resId = R.drawable.img_gem_cluster,
                                    label = "+$diamondsEarned Gems",
                                    accent = AccentCyan
                                )
                            }
                            if (!bonusLabel.isNullOrBlank()) {
                                ChestRewardPill(
                                    resId = R.drawable.img_booster_shuffle,
                                    label = bonusLabel,
                                    accent = AccentPurple
                                )
                            }
                        }
                        Text(
                            text = "Tap chest to replay animation",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(Modifier.height(2.dp))

                GlossyArcadeButton(
                    text = "COLLECT REWARD",
                    style = ArcadeButtonStyle.GOLD,
                    onClick = onDismiss,
                    height = 50.dp,
                    cornerRadius = 16.dp,
                    fontSize = 15.sp,
                    testTag = "chest_claim_collect_button",
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun ChestRewardPill(
    resId: Int,
    label: String,
    accent: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .clip(CircleShape)
            .background(BoardWellBg)
            .border(1.2.dp, accent.copy(alpha = 0.65f), CircleShape)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        GameAssetIcon(resId = resId, contentDescription = null, size = 20.dp)
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 12.sp
            ),
            color = TextPrimary
        )
    }
}
