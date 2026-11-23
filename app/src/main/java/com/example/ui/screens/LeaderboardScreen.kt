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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
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
    onBack: () -> Unit
) {
    CascadeScreenContainer(
        maxContentWidth = 620.dp,
        modifier = Modifier.testTag("leaderboard_screen")
    ) {
        ScreenTopBar(
            title = "Leaderboard",
            onBack = onBack,
            backTestTag = "leaderboard_back_button"
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
                        accent = AccentCoral
                    )
                }

                is CloudStreamState.Success -> {
                    val entries = leaderboardState.data
                        .distinctBy { it.userId }
                        .sortedByDescending { it.classicBestScore }
                    if (entries.isEmpty()) {
                        EmptyState(
                            icon = Icons.Filled.EmojiEvents,
                            title = "No scores posted yet",
                            description = "Play Classic mode. Your best score is posted here automatically.",
                            accent = AccentGold
                        )
                    } else {
                        val podium = entries.take(3)
                        val rest = entries.drop(3)
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item(key = "podium") {
                                LeaderboardPodium(podium = podium, currentUserId = currentUserId)
                            }
                            if (rest.isNotEmpty()) {
                                item(key = "table_header") { LeaderboardTableHeader() }
                                itemsIndexed(rest, key = { _, item -> item.userId }) { index, entry ->
                                    LeaderboardRow(
                                        rank = index + 4,
                                        entry = entry,
                                        isCurrentUser = entry.userId == currentUserId
                                    )
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
private fun LeaderboardTableHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF1B1464))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        val style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.ExtraBold, fontSize = 11.sp
        )
        Text("RANK", style = style, color = Color.White, modifier = Modifier.width(46.dp))
        Text("NAME", style = style, color = Color.White, modifier = Modifier.weight(1f).padding(start = 46.dp))
        Text("HIGHEST SCORE", style = style, color = Color.White, textAlign = TextAlign.End)
    }
}

@Composable
private fun LeaderboardRow(rank: Int, entry: LeaderboardEntry, isCurrentUser: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isCurrentUser) AccentGold.copy(alpha = 0.22f)
                else if (rank % 2 == 0) Color(0x33FFFFFF) else Color(0x1AFFFFFF)
            )
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = "$rank",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = OutfitFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 24.sp
            ),
            color = TextPrimary,
            modifier = Modifier.width(46.dp)
        )
        LeaderboardAvatar(entry = entry, rank = rank, accent = AccentCyan, size = 40.dp)
        Text(
            text = if (isCurrentUser) "${entry.displayName} (You)" else entry.displayName,
            style = MaterialTheme.typography.titleSmall.copy(
                fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold
            ),
            color = if (isCurrentUser) AccentGold else TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f).padding(horizontal = 10.dp)
        )
        Text(
            text = "${entry.classicBestScore}",
            style = MaterialTheme.typography.titleLarge.copy(
                fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp
            ),
            color = TextPrimary
        )
    }
}

@Composable
private fun LeaderboardAvatar(entry: LeaderboardEntry, rank: Int, accent: Color, size: Dp) {
    val initials = entry.displayName.trim().split(" ").filter { it.isNotEmpty() }
        .take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "P$rank" }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Color(0xFF1B1464))
            .border(2.dp, accent, CircleShape)
    ) {
        Text(initials, style = MaterialTheme.typography.labelLarge.copy(fontFamily = OutfitFontFamily), color = Color.White)
    }
}

@Composable
private fun LeaderboardPodium(podium: List<LeaderboardEntry>, currentUserId: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF2A1F8F), Color(0xFF14104F))))
            .padding(horizontal = 10.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.Top
        ) {
            if (podium.size > 1) {
                PodiumSlot(2, podium[1], AccentCyan, podium[1].userId == currentUserId, false, Modifier.weight(1f).padding(top = 22.dp))
            }
            PodiumSlot(1, podium[0], AccentGold, podium[0].userId == currentUserId, true, Modifier.weight(1.2f))
            if (podium.size > 2) {
                PodiumSlot(3, podium[2], AccentPurple, podium[2].userId == currentUserId, false, Modifier.weight(1f).padding(top = 22.dp))
            }
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
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
        modifier = modifier
    ) {
        Text(
            text = when (rank) { 1 -> "1st"; 2 -> "2nd"; else -> "3rd" },
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.ExtraBold),
            color = accent
        )
        LeaderboardAvatar(entry, rank, accent, if (elevated) 72.dp else 56.dp)
        Text(
            text = if (isCurrentUser) "${entry.displayName} (You)" else entry.displayName,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${entry.classicBestScore}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (elevated) 22.sp else 17.sp
            ),
            color = Color.White,
            maxLines = 1
        )
    }
}
