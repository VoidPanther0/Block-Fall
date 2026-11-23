package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
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
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumPrimaryButton
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.launch

/** Segment order matches `WHEEL_SEGMENTS` in functions/index.js (server is authoritative). */
private val WheelLabels = listOf(
    "100 Diamonds", "50 Diamonds", "10 Diamonds",
    "1000 Gold", "500 Gold", "100 Gold", "20 Gold",
    "Hammer +1", "Shuffle +1", "Refresh +1"
)
private val WheelColors = listOf(
    Color(0xFF29B6F6), Color(0xFF42A5F5), Color(0xFF5C6BC0),
    Color(0xFFFFB300), Color(0xFFFFA000), Color(0xFFFF8F00), Color(0xFFF57C00),
    Color(0xFF66BB6A), Color(0xFFAB47BC), Color(0xFFEF5350)
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

    LaunchedEffect(Unit) { onRefresh() }

    CascadeScreenContainer(
        maxContentWidth = 520.dp,
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
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp)
                    .aspectRatio(1f)
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().aspectRatio(1f).padding(top = 14.dp)) {
                    val radius = size.minDimension / 2f
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val sweep = 360f / WheelLabels.size
                    rotate(rotation.value, pivot = center) {
                        WheelLabels.forEachIndexed { i, label ->
                            val start = -90f + i * sweep
                            drawArc(
                                color = WheelColors[i],
                                startAngle = start,
                                sweepAngle = sweep,
                                useCenter = true,
                                topLeft = Offset(center.x - radius, center.y - radius),
                                size = Size(radius * 2, radius * 2)
                            )
                            val paint = android.graphics.Paint().apply {
                                color = android.graphics.Color.WHITE
                                textSize = radius * 0.095f
                                isFakeBoldText = true
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            val mid = start + sweep / 2f
                            rotate(mid, pivot = center) {
                                drawContext.canvas.nativeCanvas.drawText(
                                    label, center.x + radius * 0.62f, center.y + paint.textSize / 3f, paint
                                )
                            }
                        }
                    }
                    // hub
                    drawCircle(Color(0xFF1B1464), radius = radius * 0.14f, center = center)
                    drawCircle(Color.White, radius = radius * 0.14f, center = center, style = androidx.compose.ui.graphics.drawscope.Stroke(4f))
                    // pointer
                    val pointer = Path().apply {
                        moveTo(center.x, center.y - radius + 34f)
                        lineTo(center.x - 20f, center.y - radius - 14f)
                        lineTo(center.x + 20f, center.y - radius - 14f)
                        close()
                    }
                    drawPath(pointer, Color.White)
                }
            }

            PremiumCard(cornerRadius = 18.dp, borderColor = AccentGold.copy(alpha = 0.4f), modifier = Modifier.fillMaxWidth()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                ) {
                    Text(
                        text = "Spin points: $spinPoints",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = AccentGold,
                        modifier = Modifier.testTag("wheel_spin_points")
                    )
                    resultText?.let {
                        Text(
                            text = "You won: $it",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                    message?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = TextSecondary, textAlign = TextAlign.Center)
                    }
                    if (!isSignedIn) {
                        Text(
                            "Sign in with Google (Profile) to use the Lucky Wheel. Spins are decided on the server.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                    PremiumPrimaryButton(
                        text = if (isSpinning) "Spinning..." else "SPIN",
                        style = ArcadeButtonStyle.GOLD,
                        enabled = isSignedIn && spinPoints > 0 && !isSpinning,
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
                        height = 52.dp,
                        fontSize = 17.sp,
                        testTag = "wheel_spin_button",
                        modifier = Modifier.fillMaxWidth()
                    )
                    PremiumPrimaryButton(
                        text = if (wheelAdState is AdState.Ready) "Watch Ad · +1 Spin Point" else "Ad Loading...",
                        style = ArcadeButtonStyle.GREEN,
                        enabled = isSignedIn && wheelAdState is AdState.Ready && !isSpinning,
                        onClick = onWatchAd,
                        height = 48.dp,
                        fontSize = 14.sp,
                        testTag = "wheel_watch_ad_button",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}
