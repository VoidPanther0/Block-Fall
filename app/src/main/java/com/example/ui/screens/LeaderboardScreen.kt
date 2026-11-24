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

private val AvatarPalette = listOf(
    Color(0xFF6C4DFF), Color(0xFF1D7AFC), Color(0xFF00A8C6), Color(0xFFE5484D),
    Color(0xFFEC407A), Color(0xFF16A34A), Color(0xFFF59E0B), Color(0xFF7C3AED)
)

private fun avatarColor(name: String): Color =
    AvatarPalette[(name.hashCode() and 0x7fffffff) % AvatarPalette.size]

@Composable
private fun LeaderboardTableHeader() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 6.dp, start = 6.dp, end = 6.dp)
    ) {
        val style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, letterSpacing = 1.sp
        )
        Text("RANK", style = style, color = TextSecondary, modifier = Modifier.width(52.dp))
        Text("PLAYER", style = style, color = TextSecondary, modifier = Modifier.weight(1f))
        Text("BEST SCORE", style = style, color = TextSecondary, textAlign = TextAlign.End)
    }
}

@Composable
private fun LeaderboardRow(rank: Int, entry: LeaderboardEntry, isCurrentUser: Boolean) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isCurrentUser) AccentGold.copy(alpha = 0.16f) else Color.White)
            .border(
                if (isCurrentUser) 2.dp else 1.dp,
                if (isCurrentUser) AccentGold else Color(0xFFDCE4F2),
                shape
            )
            .padding(horizontal = 10.dp, vertical = 8.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(Color(0xFFEEF3FA))
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = OutfitFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp
                ),
                color = TextPrimary
            )
        }
        Spacer(Modifier.width(10.dp))
        LeaderboardAvatar(entry = entry, rank = rank, accent = avatarColor(entry.displayName), size = 42.dp)
        Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(
                text = entry.displayName,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = OutfitFontFamily, fontWeight = FontWeight.Bold
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isCurrentUser) {
                Text(
                    "YOU",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold, fontSize = 10.sp),
                    color = AccentGold
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(Color(0xFF1B1464))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        ) {
            Text(
                text = "${entry.classicBestScore}",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = JetBrainsMonoFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp
                ),
                color = Color.White,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LeaderboardAvatar(entry: LeaderboardEntry, rank: Int, accent: Color, size: Dp, ring: Color = Color.White) {
    val initials = entry.displayName.trim().split(" ").filter { it.isNotEmpty() }
        .take(2).joinToString("") { it.first().uppercase() }.ifEmpty { "P$rank" }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(accent, accent.copy(alpha = 0.65f))))
            .border(2.5.dp, ring, CircleShape)
    ) {
        Text(
            initials,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = OutfitFontFamily, fontWeight = FontWeight.ExtraBold,
                fontSize = (size.value * 0.34f).sp
            ),
            color = Color.White
        )
    }
}

@Composable
private fun LeaderboardPodium(podium: List<LeaderboardEntry>, currentUserId: String?) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(Brush.verticalGradient(listOf(Color(0xFF2A1F8F), Color(0xFF14104F))))
            .padding(start = 8.dp, end = 8.dp, top = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            val second = podium.getOrNull(1)
            val third = podium.getOrNull(2)
            Box(Modifier.weight(1f)) {
                if (second != null) PodiumSlot(2, second, Color(0xFFC0C8D8), second.userId == currentUserId, 58.dp, 54.dp)
            }
            Box(Modifier.weight(1.15f)) {
                PodiumSlot(1, podium[0], AccentGold, podium[0].userId == currentUserId, 76.dp, 84.dp)
            }
            Box(Modifier.weight(1f)) {
                if (third != null) PodiumSlot(3, third, Color(0xFFD08A4E), third.userId == currentUserId, 58.dp, 40.dp)
            }
        }
    }
}

@Composable
private fun PodiumSlot(
    rank: Int,
    entry: LeaderboardEntry,
    medal: Color,
    isCurrentUser: Boolean,
    avatarSize: Dp,
    pedestalHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        LeaderboardAvatar(entry, rank, avatarColor(entry.displayName), avatarSize, ring = medal)
        Spacer(Modifier.height(5.dp))
        Text(
            text = entry.displayName,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = if (isCurrentUser) AccentGold else Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
        Text(
            text = "${entry.classicBestScore}",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = JetBrainsMonoFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = if (rank == 1) 20.sp else 16.sp
            ),
            color = Color.White,
            maxLines = 1
        )
        Spacer(Modifier.height(6.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(Brush.verticalGradient(listOf(medal, medal.copy(alpha = 0.55f))))
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = OutfitFontFamily, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp
                ),
                color = Color.White.copy(alpha = 0.95f)
            )
        }
    }
}
