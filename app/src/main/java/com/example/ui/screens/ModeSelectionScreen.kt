package com.example.ui.screens

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
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.ChallengeModeScenicBackground
import com.example.ui.components.ClassicModeScenicBackground
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentGold
import com.example.ui.theme.OutfitFontFamily

@Composable
fun ModeSelectionScreen(
    onBack: () -> Unit,
    onPlayNewClassic: () -> Unit,
    onSelectChallengeLevels: () -> Unit
) {
    CascadeScreenContainer(
        maxContentWidth = 580.dp,
        modifier = Modifier.testTag("mode_selection_screen")
    ) {
        ScreenTopBar(
            title = "Select Mode",
            onBack = onBack,
            backTestTag = "mode_selection_back_button"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            ScenicModeCard(
                title = "Classic",
                subtitle = "Play endlessly\nwith best score",
                icon = Icons.Filled.AllInclusive,
                iconTint = Color.White,
                scenicBackground = { ClassicModeScenicBackground() },
                onClick = onPlayNewClassic,
                testTag = "mode_play_classic_button"
            )

            ScenicModeCard(
                title = "Challenge",
                subtitle = "Complete targets\nin limited moves",
                icon = Icons.Filled.EmojiEvents,
                iconTint = AccentGold,
                scenicBackground = { ChallengeModeScenicBackground() },
                onClick = onSelectChallengeLevels,
                testTag = "mode_browse_challenges_button"
            )
        }
    }
}

@Composable
private fun ScenicModeCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    scenicBackground: @Composable () -> Unit,
    onClick: () -> Unit,
    testTag: String
) {
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .shadow(10.dp, shape, clip = false)
            .clip(shape)
            .border(1.5.dp, Color.White.copy(alpha = 0.55f), shape)
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Box(modifier = Modifier.matchParentSize()) {
            scenicBackground()
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(18.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 18.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(64.dp)
            )

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 26.sp
                    ),
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 15.sp,
                        lineHeight = 20.sp
                    ),
                    color = Color.White.copy(alpha = 0.95f)
                )
            }
        }
    }
}
