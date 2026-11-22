package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CascadeColorScheme = darkColorScheme(
    primary = AccentCyan,
    onPrimary = GoldBtnText,
    primaryContainer = SurfaceCardElevated,
    onPrimaryContainer = TextPrimary,
    secondary = AccentGold,
    onSecondary = GoldBtnText,
    secondaryContainer = SurfaceElevatedHigh,
    onSecondaryContainer = AccentGold,
    tertiary = AccentEmerald,
    onTertiary = GoldBtnText,
    background = StudioBgBottom,
    onBackground = TextPrimary,
    surface = SurfaceCard,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCardElevated,
    onSurfaceVariant = TextSecondary,
    outline = GlassBorder,
    outlineVariant = DividerSubtle,
    error = InvalidTint,
    onError = Color.White
)

val CascadeShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(26.dp)
)

@Composable
fun CascadeTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = CascadeColorScheme,
        typography = CascadeTypography,
        shapes = CascadeShapes,
        content = content
    )
}
