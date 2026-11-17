package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.game.ChallengeLevels
import com.example.ui.GameUiState
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardRim
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextSecondary

@Composable
fun ModeSelectionScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onPlayNewClassic: () -> Unit,
    onContinueClassic: () -> Unit,
    onSelectChallengeLevels: () -> Unit,
    onQuickPlayChallenge: () -> Unit,
    onOpenRules: () -> Unit
) {
    val totalStars = state.challengeProgress.values.sumOf { it.stars }
    val maxStars = ChallengeLevels.levels.size * 3

    CascadeScreenContainer(
        maxContentWidth = 640.dp,
        modifier = Modifier.testTag("mode_selection_screen")
    ) {
        ScreenTopBar(
            title = "Game Modes",
            onBack = onBack,
            backTestTag = "mode_selection_back_button"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Classic Mode Card
            ModeDetailCard(
                title = "Classic Endless Mode",
                tag = "HIGH SCORE",
                description = "Start with an empty 8×8 grid and place polyominoes from a 3-piece tray. Clear full rows and columns to trigger downward gravity cascades and build massive chain multipliers until no moves remain.",
                accent = AccentCyan,
                icon = Icons.Filled.AllInclusive,
                statsLine = "Best Score: ${state.classicBestScore}" +
                    (state.savedClassicSummary?.let { "  ·  Saved: ${it.score} pts" } ?: ""),
                primaryLabel = "New Classic Game",
                primaryTag = "mode_play_classic_button",
                onPrimaryClick = onPlayNewClassic,
                secondaryLabel = if (state.hasSavedClassicGame) "Continue Saved Game" else null,
                secondaryTag = "mode_continue_classic_button",
                onSecondaryClick = if (state.hasSavedClassicGame) onContinueClassic else null
            )

            // Challenge Mode Card
            ModeDetailCard(
                title = "Challenge Mode",
                tag = "${state.unlockedChallengeLevel}/${ChallengeLevels.levels.size} UNLOCKED",
                description = "Conquer 8 handcrafted gravity puzzles with pre-built block formations, strict move limits, and target scores. Earn up to 3 stars per level by clearing efficiently with cascading chains.",
                accent = AccentGold,
                icon = Icons.Filled.EmojiEvents,
                statsLine = "Stars Earned: $totalStars / $maxStars  ·  Next: Level ${state.unlockedChallengeLevel}",
                primaryLabel = "Browse 8 Levels",
                primaryTag = "mode_browse_challenges_button",
                onPrimaryClick = onSelectChallengeLevels,
                secondaryLabel = "Play Level ${state.unlockedChallengeLevel}",
                secondaryTag = "mode_quick_challenge_button",
                onSecondaryClick = onQuickPlayChallenge
            )

            OutlinedButton(
                onClick = onOpenRules,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("mode_rules_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                    contentDescription = null,
                    tint = AccentEmerald,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "View Complete Rules & Scoring Formula",
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun ModeDetailCard(
    title: String,
    tag: String,
    description: String,
    accent: Color,
    icon: ImageVector,
    statsLine: String,
    primaryLabel: String,
    primaryTag: String,
    onPrimaryClick: () -> Unit,
    secondaryLabel: String?,
    secondaryTag: String,
    onSecondaryClick: (() -> Unit)?
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(SurfaceCard)
            .border(1.5.dp, accent.copy(alpha = 0.45f), RoundedCornerShape(20.dp))
            .padding(18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            Text(
                text = tag,
                style = MaterialTheme.typography.labelMedium,
                color = accent,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent.copy(alpha = 0.14f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Text(
            text = statsLine,
            style = MaterialTheme.typography.labelLarge.copy(fontSize = 13.sp),
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(SurfaceCardElevated)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        )

        Button(
            onClick = onPrimaryClick,
            colors = ButtonDefaults.buttonColors(containerColor = accent),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag(primaryTag)
        ) {
            Text(
                text = primaryLabel,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0A121C)
            )
        }

        if (secondaryLabel != null && onSecondaryClick != null) {
            OutlinedButton(
                onClick = onSecondaryClick,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag(secondaryTag)
            ) {
                Text(
                    text = secondaryLabel,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
