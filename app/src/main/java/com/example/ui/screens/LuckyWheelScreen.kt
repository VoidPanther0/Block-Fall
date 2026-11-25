package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.GoldMetallicBanner
import com.example.ui.components.RoyalSkyBackdrop
import com.example.ui.components.threeDFrame
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldBright
import com.example.ui.theme.AccentGoldDeep
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.sin

/** Segment order matches `WHEEL_SEGMENTS` in functions/index.js (server is authoritative). */
private val WheelLabels = listOf(
    "100 Gems", "50 Gems", "10 Gems",
    "1000 Gold", "500 Gold", "100 Gold", "20 Gold",
    "Hammer", "Shuffle", "Refresh"
)
private val WheelColors = listOf(
    Color(0xFF7C4DFF), Color(0xFF3D8BFF), Color(0xFF00B8D4),
    Color(0xFFFFB300), Color(0xFFFF8F00), Color(0xFFFF6D00), Color(0xFFFFA726),
    Color(0xFF26C281), Color(0xFFEC407A), Color(0xFFFF5252)
)

@Composable
fun LuckyWheelScreen(
    isSignedIn: Boolean,
    spinPoints: Int,
    isSpinning: Boolean,
    wheelAdState: AdState,
    message: String?,
    lastPrize: () -> String,
    onBack: () -> Unit,
    onSpin: ((Int?) -> Unit) -> Unit,
    onFinishSpin: () -> Unit,
    onWatchAd: () -> Unit,
    onRefresh: () -> Unit
) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var resultText by remember { mutableStateOf<String?>(null) }
    val glow by rememberInfiniteTransition(label = "wheelGlow").animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing), RepeatMode.Reverse),
        label = "glow"
    )

    val labelPaint = remember {
        android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            isFakeBoldText = true
            textAlign = android.graphics.Paint.Align.CENTER
            isAntiAlias = true
            setShadowLayer(4f, 0f, 2f, android.graphics.Color.argb(180, 0, 0, 0))
        }
    }
    val currentSpinning by rememberUpdatedState(isSpinning)
    val currentOnFinishSpin by rememberUpdatedState(onFinishSpin)

    LaunchedEffect(Unit) { onRefresh() }
    DisposableEffect(Unit) {
        onDispose {
            if (currentSpinning) {
                currentOnFinishSpin()
            }
        }
    }

    val hasSpin = spinPoints > 0

    CascadeScreenContainer(
        maxContentWidth = 520.dp,
        backdrop = { RoyalSkyBackdrop() },
        modifier = Modifier.testTag("lucky_wheel_screen")
    ) {
        ScreenTopBar(title = "Lucky Wheel", onBack = onBack, backTestTag = "wheel_back_button")

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            // Spin Points Pill
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .threeDFrame(shape = CircleShape, depth = 5.dp, base = Color(0xFF3A2400))
                    .shadow(6.dp, CircleShape, clip = false)
                    .clip(CircleShape)
                    .background(Brush.verticalGradient(listOf(Color(0xFF2A2410), BoardWellBg)))
                    .border(1.5.dp, AccentGold.copy(alpha = 0.85f), CircleShape)
                    .padding(horizontal = 20.dp, vertical = 9.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.padding(end = 2.dp)
                )
                Text(
                    text = "Spins: $spinPoints",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 17.sp
                    ),
                    color = AccentGold,
                    modifier = Modifier.testTag("wheel_spin_points")
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp)
                    .aspectRatio(1f)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(top = 10.dp)) {
                    val outer = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f + 6f)
                    val rim = outer * 0.075f
                    val radius = outer - rim - 4f
                    val sweep = 360f / WheelLabels.size

                    // Outer ambient radial glow
                    drawCircle(
                        Brush.radialGradient(
                            listOf(AccentGold.copy(alpha = 0.45f * glow), Color.Transparent),
                            center = center, radius = outer * 1.15f
                        ),
                        radius = outer * 1.15f, center = center
                    )

                    // Extruded wheel body: stacked silhouettes give the disc real thickness.
                    val thickness = outer * 0.085f
                    for (i in 7 downTo 1) {
                        val t = i / 7f
                        drawCircle(
                            color = Color(0xFF120A18).copy(alpha = 0.48f + 0.42f * t),
                            radius = outer,
                            center = Offset(center.x, center.y + thickness * t)
                        )
                    }

                    // Metallic gold outer rim with a specular sweep
                    drawCircle(
                        Brush.linearGradient(
                            listOf(
                                AccentGoldDeep,
                                AccentGoldBright,
                                Color.White.copy(alpha = 0.92f),
                                AccentGold,
                                AccentGoldDeep,
                                AccentGoldBright
                            )
                        ),
                        radius = outer, center = center
                    )
                    drawCircle(Color(0xFF0C173B), radius = radius + 3f, center = center)
                    drawCircle(
                        color = AccentGoldBright.copy(alpha = 0.35f),
                        radius = radius + 1f,
                        center = center,
                        style = Stroke(width = 2f)
                    )

                    // Rotating segmented wheel
                    rotate(rotation.value, pivot = center) {
                        WheelLabels.forEachIndexed { i, label ->
                            val start = -90f + i * sweep
                            val topLeft = Offset(center.x - radius, center.y - radius)
                            val arcSize = Size(radius * 2, radius * 2)
                            drawArc(
                                color = WheelColors[i],
                                startAngle = start,
                                sweepAngle = sweep,
                                useCenter = true,
                                topLeft = topLeft,
                                size = arcSize
                            )
                            drawArc(
                                brush = Brush.radialGradient(
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.28f)),
                                    center = center, radius = radius
                                ),
                                startAngle = start, sweepAngle = sweep, useCenter = true,
                                topLeft = topLeft, size = arcSize
                            )
                            drawArc(
                                color = Color.White.copy(alpha = 0.55f),
                                startAngle = start,
                                sweepAngle = sweep,
                                useCenter = true,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = 2.5f)
                            )
                            labelPaint.textSize = radius * 0.098f
                            val mid = start + sweep / 2f
                            rotate(mid, pivot = center) {
                                drawContext.canvas.nativeCanvas.drawText(
                                    label, center.x + radius * 0.60f, center.y + labelPaint.textSize / 3f, labelPaint
                                )
                            }
                        }
                    }

                    // Rim neon bulbs
                    val bulbs = 20
                    for (b in 0 until bulbs) {
                        val ang = Math.toRadians((b * 360.0 / bulbs) - 90.0)
                        val bx = center.x + (outer - rim / 2f) * cos(ang).toFloat()
                        val by = center.y + (outer - rim / 2f) * sin(ang).toFloat()
                        val on = (b % 2 == 0) == (glow > 0.65f)
                        drawCircle(
                            if (on) Color.White else Color(0xFFFFF3C4),
                            radius = rim * 0.32f,
                            center = Offset(bx, by)
                        )
                    }

                    // Center wheel hub with a raised 3D boss
                    for (i in 4 downTo 1) {
                        drawCircle(
                            color = Color(0xFF3A2400).copy(alpha = 0.70f),
                            radius = radius * 0.18f,
                            center = Offset(center.x, center.y + i * 2.2f)
                        )
                    }
                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color.White.copy(alpha = 0.85f), AccentGoldBright, AccentGold, AccentGoldDeep),
                            center = Offset(center.x - radius * 0.05f, center.y - radius * 0.05f),
                            radius = radius * 0.2f
                        ),
                        radius = radius * 0.18f, center = center
                    )
                    drawCircle(Color(0xFF060B21), radius = radius * 0.11f, center = center)
                    drawCircle(
                        color = Color.White.copy(alpha = 0.28f),
                        radius = radius * 0.11f,
                        center = center,
                        style = Stroke(width = 1.5f)
                    )

                    // Top indicator arrow/pointer
                    val pointer = Path().apply {
                        moveTo(center.x, center.y - radius + 26f)
                        lineTo(center.x - 24f, center.y - outer - 12f)
                        lineTo(center.x + 24f, center.y - outer - 12f)
                        close()
                    }
                    translate(0f, 6f) { drawPath(pointer, Color(0xFF2A0008)) }
                    drawPath(pointer, Color(0xFFFF3D57))
                    drawPath(pointer, Color.White, style = Stroke(width = 4f))
                }
            }

            resultText?.let {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .threeDFrame(shape = RoundedCornerShape(18.dp), depth = 6.dp, base = Color(0xFF3A2400))
                        .shadow(8.dp, RoundedCornerShape(18.dp), clip = false)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(AccentGoldDeep, AccentGold, AccentGoldDeep)
                            )
                        )
                        .border(1.2.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .padding(vertical = 12.dp, horizontal = 16.dp)
                ) {
                    Text(
                        "PRIZE WON!",
                        color = Color(0xFF241300),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Text(
                        it,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }
            }

            message?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = AccentCyan,
                    textAlign = TextAlign.Center
                )
            }

            val adReady = wheelAdState is AdState.Ready
            if (hasSpin || isSpinning) {
                // Has spin points -> SPIN is the primary glossy button
                GlossyArcadeButton(
                    text = if (isSpinning) "Spinning..." else "SPIN NOW",
                    style = ArcadeButtonStyle.GOLD,
                    enabled = hasSpin && !isSpinning,
                    onClick = {
                        resultText = null
                        onSpin { index ->
                            if (index != null) {
                                scope.launch {
                                    val sweep = 360f / WheelLabels.size
                                    val base = (rotation.value / 360f).toInt() * 360f
                                    val target = base + 360f * 6 + (360f - (index * sweep + sweep / 2f))
                                    rotation.animateTo(
                                        target,
                                        tween(4500, easing = CubicBezierEasing(0.15f, 0.7f, 0.2f, 1f))
                                    )
                                    resultText = lastPrize()
                                    onFinishSpin()
                                }
                            }
                        }
                    },
                    height = 58.dp,
                    cornerRadius = 16.dp,
                    fontSize = 20.sp,
                    testTag = "wheel_spin_button",
                    modifier = Modifier.fillMaxWidth()
                )
                GlossyArcadeButton(
                    text = if (adReady) "Watch Ad · +1 Free Spin" else "Prepare Ad · +1 Spin",
                    style = ArcadeButtonStyle.GREEN,
                    enabled = !isSpinning,
                    onClick = {
                        if (!adReady) onRefresh()
                        onWatchAd()
                    },
                    height = 46.dp,
                    cornerRadius = 14.dp,
                    fontSize = 14.sp,
                    testTag = "wheel_watch_ad_button",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // No spin points left -> Watch Ad is the primary action
                Text(
                    "No spins left. Watch a short ad to get 1 free spin!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                GlossyArcadeButton(
                    text = if (adReady) "Watch Ad · +1 Free Spin" else "Prepare Ad · +1 Spin",
                    style = ArcadeButtonStyle.GREEN,
                    enabled = true,
                    onClick = {
                        if (!adReady) onRefresh()
                        onWatchAd()
                    },
                    height = 58.dp,
                    cornerRadius = 16.dp,
                    fontSize = 17.sp,
                    testTag = "wheel_watch_ad_button",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
