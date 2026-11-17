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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.ChallengeLevel
import com.example.game.ChallengeLevelProgress
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
fun ChallengeSelectionScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onSelectLevel: (Int) -> Unit
) {
    val completedCount = state.challengeProgress.values.count { it.completed }
    val totalStars = state.challengeProgress.values.sumOf { it.stars }
    val maxStars = ChallengeLevels.levels.size * 3

    CascadeScreenContainer(
        maxContentWidth = 680.dp,
        modifier = Modifier.testTag("challenge_selection_screen")
    ) {
        ScreenTopBar(
            title = "Challenge Levels",
            onBack = onBack,
            backTestTag = "challenge_selection_back_button"
        )

        // Overall Progress Banner
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .border(1.dp, BoardRim, RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "$completedCount / ${ChallengeLevels.levels.size} Levels Cleared",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "★ $totalStars / $maxStars",
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 15.sp),
                    color = AccentGold
                )
            }
            LinearProgressIndicator(
                progress = { completedCount.toFloat() / ChallengeLevels.levels.size.toFloat() },
                color = AccentEmerald,
                trackColor = SurfaceCardElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(ChallengeLevels.levels, key = { it.levelNumber }) { level ->
                val isUnlocked = level.levelNumber <= state.unlockedChallengeLevel
                val progress = state.challengeProgress[level.levelNumber]
                ChallengeLevelRow(
                    level = level,
                    isUnlocked = isUnlocked,
                    progress = progress,
                    onClick = { if (isUnlocked) onSelectLevel(level.levelNumber) }
                )
            }
        }
    }
}

@Composable
private fun ChallengeLevelRow(
    level: ChallengeLevel,
    isUnlocked: Boolean,
    progress: ChallengeLevelProgress?,
    onClick: () -> Unit
) {
    val isCompleted = progress?.completed == true
    val stars = progress?.stars ?: 0
    val borderColor = when {
        isCompleted -> AccentEmerald.copy(alpha = 0.65f)
        isUnlocked -> AccentCyan.copy(alpha = 0.45f)
        else -> BoardRim.copy(alpha = 0.45f)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(if (isUnlocked) SurfaceCard else SurfaceCard.copy(alpha = 0.55f))
            .border(1.2.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable(enabled = isUnlocked, onClick = onClick)
            .padding(14.dp)
            .testTag("challenge_level_card_${level.levelNumber}")
    ) {
        // Level Number Badge
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(
                    when {
                        isCompleted -> AccentEmerald.copy(alpha = 0.18f)
                        isUnlocked -> AccentCyan.copy(alpha = 0.18f)
                        else -> SurfaceCardElevated
                    }
                )
        ) {
            if (!isUnlocked) {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = "Locked level ${level.levelNumber}",
                    tint = TextSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.size(22.dp)
                )
            } else {
                Text(
                    text = "${level.levelNumber}",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = if (isCompleted) AccentEmerald else AccentCyan
                )
            }
        }

        // Title, Subtitle, Target & Moves
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = level.title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (isUnlocked) MaterialTheme.colorScheme.onSurface else TextSecondary
                )
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Completed",
                        tint = AccentEmerald,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = level.subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = TextSecondary
            )

            Text(
                text = "Target: ${level.targetScore} pts  ·  ${level.moveLimit} moves" +
                    if ((progress?.bestScore ?: 0) > 0) "  ·  Best: ${progress?.bestScore}" else "",
                style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                color = if (isUnlocked) AccentGold else TextSecondary.copy(alpha = 0.6f)
            )
        }

        // Stars
        Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            for (starIdx in 1..3) {
                Icon(
                    imageVector = if (starIdx <= stars) Icons.Filled.Star else Icons.Outlined.StarBorder,
                    contentDescription = null,
                    tint = if (starIdx <= stars) AccentGold else Color.Gray.copy(alpha = 0.5f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
