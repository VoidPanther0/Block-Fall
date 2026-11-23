package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val CascadeColorScheme = lightColorScheme(
    primary = AccentBlue,
    onPrimary = Color.White,
    primaryContainer = SurfaceElevatedHigh,
    onPrimaryContainer = TextPrimary,
    secondary = AccentGold,
    onSecondary = GoldBtnText,
    secondaryContainer = SurfaceCardElevated,
    onSecondaryContainer = TextPrimary,
    tertiary = AccentEmerald,
    onTertiary = Color.White,
    background = StudioBgMid,
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
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(16.dp),
    extraLarge = RoundedCornerShape(20.dp)
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
