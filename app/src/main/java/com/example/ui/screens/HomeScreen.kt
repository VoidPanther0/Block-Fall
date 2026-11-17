package com.example.ui.screens

import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.BlockColor
import com.example.game.ChallengeLevels
import com.example.ui.GameUiState
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.drawBevelBlock
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardRim
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    state: GameUiState,
    onPlayNewClassic: () -> Unit,
    onContinueClassic: () -> Unit,
    onOpenModes: () -> Unit,
    onOpenChallenges: () -> Unit,
    onOpenHowToPlay: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenStatistics: () -> Unit,
    onOpenRules: () -> Unit
) {
    val completedCount = state.challengeProgress.values.count { it.completed }
    val savedSummary = state.savedClassicSummary

    CascadeScreenContainer(
        maxContentWidth = 600.dp,
        modifier = Modifier.testTag("home_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // Brand Header Card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(SurfaceCard)
                    .border(1.5.dp, BoardRim, RoundedCornerShape(20.dp))
                    .padding(18.dp)
            ) {
                Canvas(modifier = Modifier.size(56.dp)) {
                    val cell = size.width / 2f
                    val gap = cell * 0.14f
                    val tile = cell - gap
                    drawBevelBlock(gap / 2f, gap / 2f, tile, BlockColor.CYAN)
                    drawBevelBlock(cell + gap / 2f, gap / 2f, tile, BlockColor.AMBER)
                    drawBevelBlock(gap / 2f, cell + gap / 2f, tile, BlockColor.CORAL)
                    drawBevelBlock(cell + gap / 2f, cell + gap / 2f, tile, BlockColor.EMERALD)
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.app_name).uppercase(),
                        style = MaterialTheme.typography.displaySmall.copy(
                            fontSize = 28.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 2.sp
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Gravity-Driven 8×8 Block Puzzle",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                        color = TextSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "BEST",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentGold
                    )
                    Text(
                        text = state.classicBestScore.toString(),
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 20.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Primary Play CTA
            Button(
                onClick = onPlayNewClassic,
                colors = ButtonDefaults.buttonColors(containerColor = AccentCyan),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .testTag("home_play_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = Color(0xFF002026),
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(
                    text = "Play Classic",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color(0xFF002026)
                )
            }

            // Continue Saved Classic Game Card
            val canContinue = savedSummary != null
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (canContinue) SurfaceCardElevated else SurfaceCard.copy(alpha = 0.65f))
                    .border(
                        1.dp,
                        if (canContinue) AccentEmerald.copy(alpha = 0.65f) else BoardRim.copy(alpha = 0.5f),
                        RoundedCornerShape(16.dp)
                    )
                    .clickable(enabled = canContinue, onClick = onContinueClassic)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .testTag("home_continue_button")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Replay,
                        contentDescription = null,
                        tint = if (canContinue) AccentEmerald else TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = stringResource(R.string.nav_continue),
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (canContinue) MaterialTheme.colorScheme.onSurface else TextSecondary.copy(alpha = 0.6f)
                        )
                        Text(
                            text = if (savedSummary != null) {
                                "Saved Classic · Score ${savedSummary.score} · ${savedSummary.movesPlayed} moves"
                            } else {
                                "No active saved Classic game"
                            },
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = TextSecondary.copy(alpha = if (canContinue) 1f else 0.6f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (canContinue) {
                    Text(
                        text = "RESUME",
                        style = MaterialTheme.typography.labelMedium,
                        color = AccentEmerald
                    )
                }
            }

            // Mode & Challenge Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeMenuTile(
                    title = stringResource(R.string.nav_game_modes),
                    subtitle = "Classic & Challenge",
                    icon = Icons.Filled.GridView,
                    accent = AccentGold,
                    onClick = onOpenModes,
                    testTag = "home_modes_button",
                    modifier = Modifier.weight(1f)
                )
                HomeMenuTile(
                    title = stringResource(R.string.nav_challenges),
                    subtitle = "$completedCount/${ChallengeLevels.levels.size} Cleared",
                    icon = Icons.Filled.EmojiEvents,
                    accent = AccentCoral,
                    onClick = onOpenChallenges,
                    testTag = "home_challenges_button",
                    modifier = Modifier.weight(1f)
                )
            }

            // Learning & Reference Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeMenuTile(
                    title = stringResource(R.string.how_to_play),
                    subtitle = "Interactive Demo",
                    icon = Icons.AutoMirrored.Outlined.HelpOutline,
                    accent = AccentCyan,
                    onClick = onOpenHowToPlay,
                    testTag = "home_how_to_play_button",
                    modifier = Modifier.weight(1f)
                )
                HomeMenuTile(
                    title = stringResource(R.string.nav_rules),
                    subtitle = "Scoring & Gravity",
                    icon = Icons.AutoMirrored.Rounded.MenuBook,
                    accent = AccentEmerald,
                    onClick = onOpenRules,
                    testTag = "home_rules_button",
                    modifier = Modifier.weight(1f)
                )
            }

            // Stats & Settings Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HomeMenuTile(
                    title = stringResource(R.string.nav_statistics),
                    subtitle = "${state.statistics.totalGamesPlayed} Games",
                    icon = Icons.Filled.BarChart,
                    accent = AccentGold,
                    onClick = onOpenStatistics,
                    testTag = "home_statistics_button",
                    modifier = Modifier.weight(1f)
                )
                HomeMenuTile(
                    title = stringResource(R.string.nav_settings),
                    subtitle = "Audio & Visuals",
                    icon = Icons.Filled.Settings,
                    accent = TextSecondary,
                    onClick = onOpenSettings,
                    testTag = "home_settings_button",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun HomeMenuTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    onClick: () -> Unit,
    testTag: String,
    modifier: Modifier = Modifier
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, BoardRim, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(26.dp)
        )
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
