package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.ArcadeTitleLogo
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.ScenicHillsBackdrop
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.GreenTileBottom
import com.example.ui.theme.GreenTileTop
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.PurpleTileBottom
import com.example.ui.theme.PurpleTileTop
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated

@Composable
fun HomeScreen(
    state: GameUiState,
    onPlayNewClassic: () -> Unit,
    onContinueClassic: () -> Unit,
    onOpenModes: () -> Unit,
    onOpenChallenges: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStatistics: () -> Unit
) {
    val savedSummary = state.savedClassicSummary
    val canContinue = savedSummary != null

    CascadeScreenContainer(
        maxContentWidth = 560.dp,
        backdrop = { ScenicHillsBackdrop() },
        modifier = Modifier.testTag("home_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top-right Settings gear icon matching mockup
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .size(48.dp)
                        .testTag("home_top_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Settings,
                        contentDescription = stringResource(R.string.nav_settings),
                        tint = Color.White,
                        modifier = Modifier.size(30.dp)
                    )
                }
            }

            Spacer(Modifier.height(6.dp))

            // Hero Block Falls Title
            ArcadeTitleLogo(fontSize = 50.sp)

            Spacer(Modifier.height(10.dp))

            // Glossy Yellow Play CTA
            GlossyArcadeButton(
                text = "Play",
                icon = Icons.Filled.PlayArrow,
                style = ArcadeButtonStyle.GOLD,
                onClick = onPlayNewClassic,
                height = 58.dp,
                cornerRadius = 29.dp,
                fontSize = 21.sp,
                testTag = "home_play_button",
                modifier = Modifier.fillMaxWidth()
            )

            // Glossy Blue Continue CTA
            GlossyArcadeButton(
                text = stringResource(R.string.nav_continue),
                icon = Icons.Filled.Replay,
                style = ArcadeButtonStyle.BLUE,
                enabled = canContinue,
                onClick = onContinueClassic,
                height = 56.dp,
                cornerRadius = 28.dp,
                fontSize = 19.sp,
                testTag = "home_continue_button",
                modifier = Modifier.fillMaxWidth()
            )

            // Purple "Modes" & Green "Challenges" feature tiles
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeGradientSquareTile(
                    title = "Modes",
                    icon = Icons.Filled.EmojiEvents,
                    iconTint = AccentGold,
                    gradient = Brush.verticalGradient(listOf(PurpleTileTop, PurpleTileBottom)),
                    onClick = onOpenModes,
                    testTag = "home_modes_button",
                    modifier = Modifier.weight(1f)
                )
                HomeGradientSquareTile(
                    title = stringResource(R.string.nav_challenges),
                    icon = Icons.Filled.Star,
                    iconTint = Color.White,
                    gradient = Brush.verticalGradient(listOf(GreenTileTop, GreenTileBottom)),
                    onClick = onOpenChallenges,
                    testTag = "home_challenges_button",
                    modifier = Modifier.weight(1f)
                )
            }

            // Frosted Glass Menu Bars: Statistics & Settings ONLY
            HomeGlassMenuBar(
                title = stringResource(R.string.nav_statistics),
                icon = Icons.Filled.BarChart,
                onClick = onOpenStatistics,
                testTag = "home_statistics_button"
            )

            HomeGlassMenuBar(
                title = stringResource(R.string.nav_settings),
                icon = Icons.Filled.Settings,
                onClick = onOpenSettings,
                testTag = "home_settings_button"
            )

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HomeGradientSquareTile(
    title: String,
    icon: ImageVector,
    iconTint: Color,
    gradient: Brush,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(20.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .height(92.dp)
            .shadow(6.dp, shape, clip = false)
            .clip(shape)
            .background(gradient)
            .border(1.5.dp, Color.White.copy(alpha = 0.35f), shape)
            .clickable(onClick = onClick)
            .padding(12.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(34.dp)
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            ),
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun HomeGlassMenuBar(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(SurfaceCardElevated.copy(alpha = 0.82f), SurfaceCard.copy(alpha = 0.90f))
                )
            )
            .border(1.2.dp, GlassBorder.copy(alpha = 0.6f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp)
        )
        Spacer(Modifier.size(10.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            ),
            color = Color.White
        )
    }
}
