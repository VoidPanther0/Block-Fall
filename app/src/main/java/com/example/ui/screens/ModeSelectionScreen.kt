package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun ModeSelectionScreen(
    onBack: () -> Unit,
    onPlayNewClassic: () -> Unit,
    onSelectChallengeLevels: () -> Unit
) {
    CascadeScreenContainer(
        maxContentWidth = 860.dp,
        modifier = Modifier.testTag("mode_selection_screen")
    ) {
        ScreenTopBar(
            title = "Select Mode",
            onBack = onBack,
            backTestTag = "mode_selection_back_button"
        )

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isWide = maxWidth >= 640.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ModeCard(
                            title = "Classic Endless",
                            badge = "ENDLESS SURVIVAL",
                            description = "Place polyominoes freely on the 8×8 grid, clear rows or columns, and chain gravity cascades for high scores.",
                            icon = Icons.Filled.AllInclusive,
                            accent = AccentCyan,
                            buttonText = "Play Classic",
                            buttonStyle = ArcadeButtonStyle.GOLD,
                            buttonTag = "mode_play_classic_button",
                            onAction = onPlayNewClassic,
                            modifier = Modifier.weight(1f)
                        )
                        ModeCard(
                            title = "Challenge Campaign",
                            badge = "8 TACTICAL LEVELS",
                            description = "Conquer curated boards with suspended shelves, move budgets, and 3-star target thresholds.",
                            icon = Icons.Filled.EmojiEvents,
                            accent = AccentGold,
                            buttonText = "Browse Levels",
                            buttonStyle = ArcadeButtonStyle.GREEN,
                            buttonTag = "mode_browse_challenges_button",
                            onAction = onSelectChallengeLevels,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    ModeCard(
                        title = "Classic Endless",
                        badge = "ENDLESS SURVIVAL",
                        description = "Place polyominoes freely on the 8×8 grid, clear rows or columns, and chain gravity cascades for high scores.",
                        icon = Icons.Filled.AllInclusive,
                        accent = AccentCyan,
                        buttonText = "Play Classic",
                        buttonStyle = ArcadeButtonStyle.GOLD,
                        buttonTag = "mode_play_classic_button",
                        onAction = onPlayNewClassic
                    )
                    ModeCard(
                        title = "Challenge Campaign",
                        badge = "8 TACTICAL LEVELS",
                        description = "Conquer curated boards with suspended shelves, move budgets, and 3-star target thresholds.",
                        icon = Icons.Filled.EmojiEvents,
                        accent = AccentGold,
                        buttonText = "Browse Levels",
                        buttonStyle = ArcadeButtonStyle.GREEN,
                        buttonTag = "mode_browse_challenges_button",
                        onAction = onSelectChallengeLevels
                    )
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun ModeCard(
    title: String,
    badge: String,
    description: String,
    icon: ImageVector,
    accent: Color,
    buttonText: String,
    buttonStyle: ArcadeButtonStyle,
    buttonTag: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    FrostedGlassCard(
        cornerRadius = 22.dp,
        borderColor = accent.copy(alpha = 0.42f),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accent.copy(alpha = 0.16f))
                        .border(1.dp, accent.copy(alpha = 0.42f), RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp
                        ),
                        color = accent
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 21.sp
                        ),
                        color = TextPrimary
                    )
                }
            }

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                color = TextSecondary
            )

            GlossyArcadeButton(
                text = buttonText,
                icon = Icons.Filled.PlayArrow,
                style = buttonStyle,
                onClick = onAction,
                height = 50.dp,
                cornerRadius = 15.dp,
                fontSize = 16.sp,
                testTag = buttonTag,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
