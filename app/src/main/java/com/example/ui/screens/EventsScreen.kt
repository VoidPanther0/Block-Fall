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
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.DailyChallengeGenerator
import com.example.ui.GameUiState
import com.example.ui.components.AnimatedTreasureChest3D
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.BlockFallBottomNav
import com.example.ui.components.BottomNavTab
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.ChestOpenCelebrationDialog
import com.example.ui.components.CrystalCoinBadge
import com.example.ui.components.CrystalDiamondBadge
import com.example.ui.components.CrystalMedalBadge
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GameAssetIcon
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.ScreenTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
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
    var showFestivalChestDialog by remember { mutableStateOf(false) }

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
            // Event Festival Hero Banner Artwork
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(142.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.2.dp, AccentPurple.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_banner_festival_events),
                    contentDescription = "Crystal Cascade Festival",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0x33050918), Color(0xCC060B21))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "SEASON 1 · LIVE EVENT",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.2.sp
                        ),
                        color = AccentGold
                    )
                    Text(
                        text = "CRYSTAL CASCADE FESTIVAL",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 19.sp
                        ),
                        color = Color.White
                    )
                }
            }

            // Season & Animated 3D Chest Card
            FrostedGlassCard(
                cornerRadius = 20.dp,
                borderColor = AccentGold.copy(alpha = 0.5f),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showFestivalChestDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(66.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(BoardWellBg)
                            .border(1.2.dp, AccentGold.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                    ) {
                        AnimatedTreasureChest3D(
                            size = 62.dp,
                            autoPlay = true,
                            loopingPreview = true
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SEASON TREASURY · TAP TO INSPECT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.9.sp
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
                                .size(52.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(BoardWellBg)
                                .border(1.dp, AccentPurple.copy(alpha = 0.55f), RoundedCornerShape(14.dp))
                        ) {
                            GameAssetIcon(R.drawable.img_planet_gravity, null, size = 38.dp)
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

    if (showFestivalChestDialog) {
        ChestOpenCelebrationDialog(
            title = "Festival Sovereign Chest",
            subtitle = "Complete 7 Daily Gravity puzzles during Season 1 to unlock the sovereign treasury!",
            goldEarned = 1200,
            diamondsEarned = 60,
            onDismiss = { showFestivalChestDialog = false }
        )
    }
}
