package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.BlockColor
import com.example.ui.components.drawBevelBlock
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.ObsidianGlow
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onFinished: () -> Unit
) {
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1050, easing = FastOutSlowInEasing)
        )
        delay(150L)
        onFinished()
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(ObsidianGlow, ObsidianBg),
                    radius = 1200f
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onFinished
            )
            .testTag("splash_screen")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            // Original Cascade Block-Fall emblem
            Canvas(
                modifier = Modifier
                    .size(116.dp)
                    .alpha(0.35f + 0.65f * progress.value)
            ) {
                val cell = size.width / 3f
                val gap = cell * 0.12f
                val tile = cell - gap
                val fallOffset = (1f - progress.value) * cell * 0.55f

                // Bottom row blocks
                drawBevelBlock(0f * cell + gap / 2f, 2f * cell + gap / 2f, tile, BlockColor.CYAN)
                drawBevelBlock(1f * cell + gap / 2f, 2f * cell + gap / 2f, tile, BlockColor.EMERALD)
                drawBevelBlock(2f * cell + gap / 2f, 2f * cell + gap / 2f, tile, BlockColor.COBALT)

                // Mid falling blocks
                drawBevelBlock(0f * cell + gap / 2f, 1f * cell + gap / 2f - fallOffset * 0.6f, tile, BlockColor.AMBER)
                drawBevelBlock(1f * cell + gap / 2f, 1f * cell + gap / 2f - fallOffset * 0.3f, tile, BlockColor.CORAL)

                // Top falling crown block
                drawBevelBlock(1f * cell + gap / 2f, 0f * cell + gap / 2f - fallOffset, tile, BlockColor.VIOLET, glowBoost = 0.6f)
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = stringResource(R.string.app_name).uppercase(),
                style = MaterialTheme.typography.displaySmall.copy(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 4.sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(6.dp))

            Text(
                text = "GRAVITY BLOCK PUZZLE",
                style = MaterialTheme.typography.labelMedium.copy(
                    letterSpacing = 2.2.sp
                ),
                color = AccentCyan
            )

            Spacer(Modifier.height(32.dp))

            LinearProgressIndicator(
                progress = { progress.value },
                color = AccentCyan,
                trackColor = SurfaceCardElevated,
                modifier = Modifier
                    .width(180.dp)
                    .height(6.dp)
                    .clip(CircleShape)
            )

            Spacer(Modifier.height(10.dp))

            Text(
                text = "Loading grid & gravity engine...",
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = TextSecondary
            )
        }
    }
}
