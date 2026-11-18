package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val CascadeColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = Color.White,
    secondary = AccentGold,
    onSecondary = GoldBtnText,
    tertiary = AccentEmerald,
    background = RoyalBlueBottom,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = BoardRim,
    error = InvalidTint,
    onError = Color.White
)

@Composable
fun CascadeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CascadeColorScheme,
        typography = CascadeTypography,
        content = content
    )
}
