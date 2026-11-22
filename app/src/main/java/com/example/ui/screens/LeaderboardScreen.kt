package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.cloud.LeaderboardEntry
import com.example.ui.CloudStreamState
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.EmptyState
import com.example.ui.components.GoogleSignInCard
import com.example.ui.components.LoadingIndicator
import com.example.ui.components.PremiumCard
import com.example.ui.components.ScreenTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LeaderboardScreen(
    isSignedIn: Boolean,
    currentUserId: String?,
    leaderboardState: CloudStreamState<List<LeaderboardEntry>>,
    onBack: () -> Unit,
    onSyncScore: () -> Unit
) {
    CascadeScreenContainer(
        maxContentWidth = 620.dp,
        modifier = Modifier.testTag("leaderboard_screen")
    ) {
        ScreenTopBar(
            title = "Leaderboard",
            onBack = onBack,
            backTestTag = "leaderboard_back_button",
            actions = {
                if (isSignedIn) {
                    IconButton(
                        onClick = onSyncScore,
                        modifier = Modifier.testTag("leaderboard_sync_button")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Sync my score",
                            tint = AccentCyan
                        )
                    }
                }
            }
        )

        if (!isSignedIn) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                GoogleSignInCard(
                    title = "Global Classic Leaderboard",
                    subtitle = "Sign in with Google to post your verified Classic high score and view global top players."
                )
            }
        } else {
            when (leaderboardState) {
                is CloudStreamState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        LoadingIndicator(message = "Loading global standings...")
                    }
                }

                is CloudStreamState.Error -> {
                    EmptyState(
                        icon = Icons.Filled.CloudOff,
                        title = "Unable to load leaderboard",
                        description = leaderboardState.message,
                        accent = AccentCoral,
                        actionLabel = "Retry Sync",
                        onAction = onSyncScore
                    )
                }

                is CloudStreamState.Success -> {
                    val entries = leaderboardState.data
                    if (entries.isEmpty()) {
                        EmptyState(
                            icon = Icons.Filled.EmojiEvents,
                            title = "No scores posted yet",
                            description = "Play Classic mode and tap refresh to publish your best score.",
                            accent = AccentGold,
                            actionLabel = "Sync Best Score",
                            onAction = onSyncScore
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item(key = "leaderboard_header") {
                                SectionHeader(
                                    kicker = "Verified Standings",
                                    title = "Top Classic Players",
                                    accent = AccentGold
                                )
                            }
                            if (entries.size >= 3) {
                                item(key = "podium_header") {
                                    LeaderboardPodium(
                                        topThree = entries.take(3),
                                        currentUserId = currentUserId
                                    )
                                }
                            }
                            itemsIndexed(entries, key = { _, item -> item.userId }) { index, entry ->
                                val isCurrentUser = entry.userId == currentUserId
                                val rankColor = when (index) {
                                    0 -> AccentGold
                                    1 -> AccentCyan
                                    2 -> AccentPurple
                                    else -> GlassBorder
                                }
                                PremiumCard(
                                    cornerRadius = 16.dp,
                                    borderColor = if (isCurrentUser) AccentGold else rankColor.copy(alpha = 0.35f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(CircleShape)
                                                .background(rankColor.copy(alpha = 0.18f))
                                                .border(1.dp, rankColor.copy(alpha = 0.45f), CircleShape)
                                        ) {
                                            Text(
                                                text = "#${index + 1}",
                                                style = MaterialTheme.typography.labelLarge.copy(
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 12.sp
                                                ),
                                                color = if (index < 3) rankColor else Color.White
                                            )
                                        }
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = if (isCurrentUser) "${entry.displayName} (You)" else entry.displayName,
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontFamily = OutfitFontFamily,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 15.sp
                                                ),
                                                color = if (isCurrentUser) AccentGold else TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "${entry.rankTitle} · Max Chain ${entry.maxCascadeDepth}x",
                                                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 11.sp),
                                                color = TextSecondary,
                                                maxLines = 1
                                            )
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(SurfaceCardElevated.copy(alpha = 0.8f))
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Text(
                                                text = "${entry.classicBestScore}",
                                                style = MaterialTheme.typography.titleMedium.copy(
                                                    fontFamily = JetBrainsMonoFontFamily,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    fontSize = 15.sp
                                                ),
                                                color = AccentGold
                                            )
                                        }
                                    }
                                }
                            }
                            item { Spacer(Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardPodium(
    topThree: List<LeaderboardEntry>,
    currentUserId: String?
) {
    PremiumCard(
        cornerRadius = 20.dp,
        borderColor = AccentGold.copy(alpha = 0.42f),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            PodiumSlot(
                rank = 2,
                entry = topThree[1],
                accent = AccentCyan,
                isCurrentUser = topThree[1].userId == currentUserId,
                elevated = false,
                modifier = Modifier.weight(1f)
            )
            PodiumSlot(
                rank = 1,
                entry = topThree[0],
                accent = AccentGold,
                isCurrentUser = topThree[0].userId == currentUserId,
                elevated = true,
                modifier = Modifier.weight(1.1f)
            )
            PodiumSlot(
                rank = 3,
                entry = topThree[2],
                accent = AccentPurple,
                isCurrentUser = topThree[2].userId == currentUserId,
                elevated = false,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PodiumSlot(
    rank: Int,
    entry: LeaderboardEntry,
    accent: Color,
    isCurrentUser: Boolean,
    elevated: Boolean,
    modifier: Modifier = Modifier
) {
    val initials = entry.displayName
        .trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "P$rank" }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(BoardWellBg)
            .border(
                width = if (isCurrentUser || elevated) 1.5.dp else 1.dp,
                color = accent.copy(alpha = if (elevated) 0.72f else 0.38f),
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = if (elevated) 14.dp else 10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(if (elevated) 44.dp else 36.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.18f))
                .border(1.5.dp, accent, CircleShape)
        ) {
            Text(
                text = initials,
                style = MaterialTheme.typography.labelLarge.copy(fontFamily = OutfitFontFamily),
                color = accent
            )
        }
        Text(
            text = "#$rank",
            style = MaterialTheme.typography.labelSmall.copy(fontFamily = JetBrainsMonoFontFamily),
            color = accent
        )
        Text(
            text = entry.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${entry.classicBestScore}",
            style = MaterialTheme.typography.titleSmall.copy(fontFamily = JetBrainsMonoFontFamily),
            color = AccentGold,
            maxLines = 1
        )
    }
}
