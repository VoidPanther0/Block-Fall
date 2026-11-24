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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ads.AdState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentGold
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
private val PanelDark = Color(0xFFF3EAFF)
private val PanelDeep = Color(0xFFD9C6FF)

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

    LaunchedEffect(Unit) { onRefresh() }

    val hasSpin = spinPoints > 0

    CascadeScreenContainer(
        maxContentWidth = 520.dp,
        backdrop = {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(PanelDark, PanelDeep)))
            )
        },
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF3A1B7A))
                    .border(1.5.dp, AccentGold.copy(alpha = 0.8f), RoundedCornerShape(50))
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "Spins: $spinPoints",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
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

                    drawCircle(
                        Brush.radialGradient(
                            listOf(AccentGold.copy(alpha = 0.45f * glow), Color.Transparent),
                            center = center, radius = outer * 1.15f
                        ),
                        radius = outer * 1.15f, center = center
                    )
                    drawCircle(
                        Brush.linearGradient(listOf(Color(0xFFFFE082), Color(0xFFFFA000), Color(0xFFFFE082))),
                        radius = outer, center = center
                    )
                    drawCircle(Color(0xFF3A1B7A), radius = radius + 3f, center = center)

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
                                    listOf(Color.Transparent, Color.Black.copy(alpha = 0.22f)),
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
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = radius * 0.098f
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                                setShadowLayer(4f, 0f, 2f, android.graphics.Color.argb(160, 0, 0, 0))
                            }
                            val mid = start + sweep / 2f
                            rotate(mid, pivot = center) {
                                drawContext.canvas.nativeCanvas.drawText(
                                    label, center.x + radius * 0.60f, center.y + paint.textSize / 3f, paint
                                )
                            }
                        }
                    }

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

                    drawCircle(
                        Brush.radialGradient(
                            listOf(Color(0xFFFFF59D), Color(0xFFFFA000)),
                            center = center, radius = radius * 0.2f
                        ),
                        radius = radius * 0.17f, center = center
                    )
                    drawCircle(Color(0xFF3A1B7A), radius = radius * 0.1f, center = center)

                    val pointer = Path().apply {
                        moveTo(center.x, center.y - radius + 26f)
                        lineTo(center.x - 24f, center.y - outer - 12f)
                        lineTo(center.x + 24f, center.y - outer - 12f)
                        close()
                    }
                    drawPath(pointer, Color(0xFFFF3D57))
                    drawPath(pointer, Color.White, style = Stroke(width = 4f))
                }
            }

            resultText?.let {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Brush.horizontalGradient(listOf(Color(0xFFFFB300), Color(0xFFFF6D00))))
                        .padding(vertical = 10.dp)
                ) {
                    Text("YOU WON", color = Color.White.copy(alpha = 0.9f), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(it, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center)
                }
            }

            message?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF3A1B7A),
                    textAlign = TextAlign.Center
                )
            }

            val adReady = wheelAdState is AdState.Ready
            if (hasSpin || isSpinning) {
                // Has spin points -> SPIN comes first.
                PremiumPrimaryButton(
                    text = if (isSpinning) "Spinning..." else "SPIN",
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
                    fontSize = 20.sp,
                    testTag = "wheel_spin_button",
                    modifier = Modifier.fillMaxWidth()
                )
                PremiumPrimaryButton(
                    text = if (adReady) "Watch Ad · +1 Spin" else "Tap for Ad · +1 Spin",
                    style = ArcadeButtonStyle.GREEN,
                    enabled = !isSpinning,
                    onClick = {
                        if (!adReady) onRefresh()
                        onWatchAd()
                    },
                    height = 44.dp,
                    fontSize = 13.sp,
                    testTag = "wheel_watch_ad_button",
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                // No spin points -> Watch Ad is the main action.
                Text(
                    "No spins left. Watch a short ad to get 1 free spin!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF3A1B7A),
                    textAlign = TextAlign.Center
                )
                PremiumPrimaryButton(
                    text = if (adReady) "Watch Ad · +1 Spin" else "Tap for Ad · +1 Spin",
                    style = ArcadeButtonStyle.GREEN,
                    enabled = true,
                    onClick = {
                        if (!adReady) onRefresh()
                        onWatchAd()
                    },
                    height = 58.dp,
                    fontSize = 18.sp,
                    testTag = "wheel_watch_ad_button",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
