package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Timer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.game.BlockColor
import com.example.game.ChallengeLevels
import com.example.game.DailyChallengeGenerator
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.GameModeCard
import com.example.ui.components.PremiumQuickNav
import com.example.ui.components.PrimaryNavTab
import com.example.ui.components.ScreenTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.components.drawBevelBlock
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.LocalBlockStyle
import com.example.ui.theme.LocalBoardPalette
import com.example.ui.theme.TextMuted

@Composable
fun ModeSelectionScreen(
    onBack: () -> Unit,
    onPlayNewClassic: () -> Unit,
    onSelectChallengeLevels: () -> Unit,
    onPlayDaily: () -> Unit = {},
    state: GameUiState = GameUiState(),
    onNavigateTab: ((PrimaryNavTab) -> Unit)? = null
) {
    val totalStars = state.challengeProgress.values.sumOf { it.stars }
    val maxStars = ChallengeLevels.levels.size * 3
    val classicStat = if (state.classicBestScore > 0) "Best Score: ${state.classicBestScore}" else "No limit · Beat your best"
    val challengeStat = "★ $totalStars/$maxStars · Level ${state.unlockedChallengeLevel} unlocked"

    CascadeScreenContainer(
        maxContentWidth = 880.dp,
        bottomBar = if (onNavigateTab != null) {
            {
                PremiumQuickNav(
                    selectedTab = PrimaryNavTab.PLAY,
                    onSelectTab = onNavigateTab
                )
            }
        } else null,
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
                SectionHeader(
                    kicker = "Playable Modes",
                    title = "Choose Your Challenge",
                    accent = AccentCyan
                )

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GameModeCard(
                            title = "Classic Endless",
                            badge = "Endless Survival",
                            description = "Place polyominoes freely on the 8×8 grid, clear rows or columns, and chain gravity cascades for high scores.",
                            icon = Icons.Filled.AllInclusive,
                            accent = AccentCyan,
                            buttonText = "Play Classic",
                            buttonStyle = ArcadeButtonStyle.GOLD,
                            buttonTag = "mode_play_classic_button",
                            onAction = onPlayNewClassic,
                            secondaryStat = classicStat,
                            artwork = { MiniModeBlocksArtwork(isClassic = true) },
                            modifier = Modifier.weight(1f)
                        )
                        GameModeCard(
                            title = "Challenge Campaign",
                            badge = "8 Tactical Levels",
                            description = "Conquer curated boards with suspended shelves, move budgets, and 3-star target thresholds.",
                            icon = Icons.Filled.EmojiEvents,
                            accent = AccentGold,
                            buttonText = "Browse Levels",
                            buttonStyle = ArcadeButtonStyle.GREEN,
                            buttonTag = "mode_browse_challenges_button",
                            onAction = onSelectChallengeLevels,
                            secondaryStat = challengeStat,
                            artwork = { MiniModeBlocksArtwork(isClassic = false) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    GameModeCard(
                        title = "Classic Endless",
                        badge = "Endless Survival",
                        description = "Place polyominoes freely on the 8×8 grid, clear rows or columns, and chain gravity cascades for high scores.",
                        icon = Icons.Filled.AllInclusive,
                        accent = AccentCyan,
                        buttonText = "Play Classic",
                        buttonStyle = ArcadeButtonStyle.GOLD,
                        buttonTag = "mode_play_classic_button",
                        onAction = onPlayNewClassic,
                        secondaryStat = classicStat,
                        artwork = { MiniModeBlocksArtwork(isClassic = true) }
                    )
                    GameModeCard(
                        title = "Challenge Campaign",
                        badge = "8 Tactical Levels",
                        description = "Conquer curated boards with suspended shelves, move budgets, and 3-star target thresholds.",
                        icon = Icons.Filled.EmojiEvents,
                        accent = AccentGold,
                        buttonText = "Browse Levels",
                        buttonStyle = ArcadeButtonStyle.GREEN,
                        buttonTag = "mode_browse_challenges_button",
                        onAction = onSelectChallengeLevels,
                        secondaryStat = challengeStat,
                        artwork = { MiniModeBlocksArtwork(isClassic = false) }
                    )
                }

                SectionHeader(
                    kicker = "Daily & Upcoming",
                    title = "More Ways To Play",
                    accent = AccentPurple
                )

                val dailySpec = remember {
                    DailyChallengeGenerator.forDateKey(DailyChallengeGenerator.todayDateKey())
                }
                val dailyStat = "${dailySpec.displayDate} · Target ${dailySpec.level.targetScore} · ${dailySpec.level.moveLimit} moves"

                if (isWide) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        GameModeCard(
                            title = "Daily Gravity",
                            badge = "New Puzzle Every Day",
                            description = "One seeded 8×8 puzzle shared by every player each day. Chain gravity cascades within your move budget.",
                            icon = Icons.Filled.CalendarToday,
                            accent = AccentPurple,
                            buttonText = "Play Today's Puzzle",
                            buttonStyle = ArcadeButtonStyle.BLUE,
                            buttonTag = "mode_play_daily_button",
                            onAction = onPlayDaily,
                            secondaryStat = dailyStat,
                            modifier = Modifier.weight(1f)
                        )
                        GameModeCard(
                            title = "Blitz 180",
                            badge = "Coming Soon",
                            description = "Race a 3-minute timer where cascade waves extend your clock.",
                            icon = Icons.Filled.Timer,
                            accent = TextMuted,
                            buttonText = "Coming Soon",
                            buttonStyle = ArcadeButtonStyle.SLATE,
                            buttonTag = "mode_blitz_upcoming_button",
                            onAction = {},
                            enabled = false,
                            modifier = Modifier.weight(1f)
                        )
                    }
                } else {
                    GameModeCard(
                        title = "Daily Gravity",
                        badge = "New Puzzle Every Day",
                        description = "One seeded 8×8 puzzle shared by every player each day. Chain gravity cascades within your move budget.",
                        icon = Icons.Filled.CalendarToday,
                        accent = AccentPurple,
                        buttonText = "Play Today's Puzzle",
                        buttonStyle = ArcadeButtonStyle.BLUE,
                        buttonTag = "mode_play_daily_button",
                        onAction = onPlayDaily,
                        secondaryStat = dailyStat
                    )
                    GameModeCard(
                        title = "Blitz 180",
                        badge = "Coming Soon",
                        description = "Race a 3-minute timer where cascade waves extend your clock.",
                        icon = Icons.Filled.Timer,
                        accent = TextMuted,
                        buttonText = "Coming Soon",
                        buttonStyle = ArcadeButtonStyle.SLATE,
                        buttonTag = "mode_blitz_upcoming_button",
                        onAction = {},
                        enabled = false
                    )
                }

                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

@Composable
private fun MiniModeBlocksArtwork(isClassic: Boolean) {
    val blockStyle = LocalBlockStyle.current
    val boardPalette = LocalBoardPalette.current
    Canvas(modifier = Modifier.size(38.dp)) {
        val cell = size.width / 3f
        val gap = cell * 0.14f
        val tile = cell - gap
        val pattern = if (isClassic) {
            listOf(
                Triple(0, 1, BlockColor.CYAN),
                Triple(1, 1, BlockColor.CYAN),
                Triple(2, 0, BlockColor.AMBER),
                Triple(2, 1, BlockColor.CYAN),
                Triple(2, 2, BlockColor.EMERALD)
            )
        } else {
            listOf(
                Triple(0, 0, BlockColor.CORAL),
                Triple(0, 2, BlockColor.VIOLET),
                Triple(1, 0, BlockColor.CORAL),
                Triple(1, 1, BlockColor.AMBER),
                Triple(1, 2, BlockColor.VIOLET),
                Triple(2, 1, BlockColor.AMBER)
            )
        }
        for ((r, c, color) in pattern) {
            drawBevelBlock(
                left = c * cell + gap / 2f,
                top = r * cell + gap / 2f,
                cellSize = tile,
                color = color,
                blockStyle = blockStyle,
                boardPalette = boardPalette
            )
        }
    }
}
