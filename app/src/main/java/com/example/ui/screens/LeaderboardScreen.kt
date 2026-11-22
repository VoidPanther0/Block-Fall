package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.cloud.LeaderboardEntry
import com.example.ui.CloudStreamState
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GoogleSignInCard
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentPurple
import com.example.ui.theme.AccentCoral
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
                modifier = Modifier.fillMaxSize()
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
                        CircularProgressIndicator(color = AccentCyan)
                    }
                }

                is CloudStreamState.Error -> {
                    FrostedGlassCard(
                        cornerRadius = 20.dp,
                        borderColor = AccentCoral.copy(alpha = 0.45f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Unable to load leaderboard",
                                style = MaterialTheme.typography.titleMedium,
                                color = AccentCoral
                            )
                            Text(
                                text = leaderboardState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }

                is CloudStreamState.Success -> {
                    val entries = leaderboardState.data
                    if (entries.isEmpty()) {
                        FrostedGlassCard(
                            cornerRadius = 20.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.EmojiEvents,
                                    contentDescription = null,
                                    tint = AccentGold,
                                    modifier = Modifier.size(36.dp)
                                )
                                Text(
                                    text = "No scores posted yet",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Play Classic mode and tap refresh to publish your best score.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                            }
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(entries, key = { _, item -> item.userId }) { index, entry ->
                                val isCurrentUser = entry.userId == currentUserId
                                val rankColor = when (index) {
                                    0 -> AccentGold
                                    1 -> AccentCyan
                                    2 -> AccentPurple
                                    else -> GlassBorder
                                }
                                FrostedGlassCard(
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
