package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.DailyChallengeGenerator
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.BlockFallBottomNav
import com.example.ui.components.BottomNavTab
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.CrystalCoinBadge
import com.example.ui.components.CrystalDiamondBadge
import com.example.ui.components.CrystalMedalBadge
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.GoldMetallicBanner
import com.example.ui.components.ScreenTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldBright
import com.example.ui.theme.AccentGoldDeep
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

private data class EventMilestone(
    val tier: Int,
    val title: String,
    val targetWins: Int,
    val rewardGold: Int,
    val rewardDiamonds: Int
)

private val SeasonMilestones = listOf(
    EventMilestone(1, "Bronze Star", 1, 200, 10),
    EventMilestone(2, "Silver Crown", 3, 500, 25),
    EventMilestone(3, "Crystal Sovereign", 7, 1200, 60)
)

@Composable
fun EventsScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onPlayEventChallenge: () -> Unit,
    onOpenTab: ((BottomNavTab) -> Unit)? = null
) {
    val dailyCompleted = state.statistics.dailyCompletedCount
    val streak = state.statistics.dailyStreak

    CascadeScreenContainer(
        maxContentWidth = 720.dp,
        footerBar = if (onOpenTab != null) {
            { BlockFallBottomNav(selectedTab = BottomNavTab.EVENTS, onSelectTab = onOpenTab) }
        } else null,
        modifier = Modifier.testTag("events_screen")
    ) {
        ScreenTopBar(
            title = "Events",
            onBack = onBack,
            backTestTag = "events_back_button"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Event Hero Banner
            GoldMetallicBanner(
                text = "CRYSTAL CASCADE FESTIVAL",
                isPurple = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Season & Timer Card
            FrostedGlassCard(
                cornerRadius = 20.dp,
                borderColor = AccentGold.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(54.dp)
                            .shadow(6.dp, RoundedCornerShape(16.dp), clip = false)
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    listOf(AccentGoldBright, AccentGold, AccentGoldDeep)
                                )
                            )
                            .border(1.5.dp, Color.White, RoundedCornerShape(16.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.WorkspacePremium,
                            contentDescription = null,
                            tint = Color(0xFF241300),
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SEASON 1 · LIVE EVENT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            ),
                            color = AccentGold
                        )
                        Text(
                            text = "Gravity Cascade Rally",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            ),
                            color = TextPrimary
                        )
                        Text(
                            text = "Ends in 4d 18h · Win Daily Gravity puzzles to climb milestone tiers!",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Featured Event Challenge Card
            SectionHeader(kicker = "Active Event Stage", title = "Today's Puzzle Board", accent = AccentCyan)
            val todaySpec = DailyChallengeGenerator.forDateKey(DailyChallengeGenerator.todayDateKey())
            FrostedGlassCard(
                cornerRadius = 20.dp,
                borderColor = AccentCyan.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(AccentPurple.copy(alpha = 0.18f))
                                .border(1.dp, AccentPurple.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                        ) {
                            Icon(Icons.Filled.CalendarToday, null, tint = AccentCyan, modifier = Modifier.size(24.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DAILY GRAVITY CHALLENGE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = AccentCyan
                            )
                            Text(
                                text = todaySpec.level.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Target: ${todaySpec.level.targetScore} pts · Moves: ${todaySpec.level.moveLimit}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    GlossyArcadeButton(
                        text = "PLAY EVENT CHALLENGE",
                        icon = Icons.Filled.PlayArrow,
                        style = ArcadeButtonStyle.GOLD_PURPLE,
                        onClick = onPlayEventChallenge,
                        height = 52.dp,
                        cornerRadius = 14.dp,
                        fontSize = 16.sp,
                        testTag = "event_play_challenge_button",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Milestone Track
            SectionHeader(kicker = "Season Track", title = "Event Milestones", accent = AccentGold)
            SeasonMilestones.forEach { milestone ->
                val isUnlocked = dailyCompleted >= milestone.targetWins
                FrostedGlassCard(
                    cornerRadius = 18.dp,
                    borderColor = if (isUnlocked) AccentGold else GlassBorder.copy(alpha = 0.4f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        CrystalMedalBadge(tier = milestone.tier, unlocked = isUnlocked, size = 52.dp)
                        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text(
                                text = milestone.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = "Requires ${milestone.targetWins} Daily puzzle victories (${minOf(dailyCompleted, milestone.targetWins)}/${milestone.targetWins})",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    CrystalCoinBadge(size = 14.dp)
                                    Text(
                                        text = "+${milestone.rewardGold}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = AccentGold
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    CrystalDiamondBadge(size = 14.dp)
                                    Text(
                                        text = "+${milestone.rewardDiamonds}",
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontFamily = JetBrainsMonoFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 11.sp
                                        ),
                                        color = AccentCyan
                                    )
                                }
                            }
                        }

                        if (isUnlocked) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(AccentEmerald.copy(alpha = 0.20f))
                                    .border(1.2.dp, AccentEmerald, CircleShape)
                            ) {
                                Icon(Icons.Filled.Check, null, tint = AccentEmerald, modifier = Modifier.size(18.dp))
                            }
                        } else {
                            Text(
                                text = "${minOf(dailyCompleted, milestone.targetWins)}/${milestone.targetWins}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }
}
