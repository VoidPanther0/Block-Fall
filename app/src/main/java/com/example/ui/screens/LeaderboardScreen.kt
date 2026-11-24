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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.EmojiEvents
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
import com.example.game.ProfileAvatar
import com.example.ui.CloudStreamState
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.EmptyState
import com.example.ui.components.GoogleSignInCard
import com.example.ui.components.LoadingIndicator
import com.example.ui.components.PlayerAvatarBadge
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentGoldBright
import com.example.ui.theme.AccentGoldDeep
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun LeaderboardScreen(
    isSignedIn: Boolean,
    currentUserId: String?,
    leaderboardState: CloudStreamState<List<LeaderboardEntry>>,
    onBack: () -> Unit,
    currentUserAvatarId: String = ProfileAvatar.CLASSIC_BLOCK.id,
    currentUserPhotoUrl: String? = null
) {
    CascadeScreenContainer(
        maxContentWidth = 640.dp,
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
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            item(key = "podium") {
                                LeaderboardPodium(
                                    podium = podium,
                                    currentUserId = currentUserId,
                                    currentUserAvatarId = currentUserAvatarId,
                                    currentUserPhotoUrl = currentUserPhotoUrl
                                )
                            }
                            if (rest.isNotEmpty()) {
                                item(key = "table_header") { LeaderboardTableHeader() }
                                itemsIndexed(rest, key = { _, item -> item.userId }) { index, entry ->
                                    val isCurrent = entry.userId == currentUserId
                                    LeaderboardRow(
                                        rank = index + 4,
                                        entry = entry,
                                        isCurrentUser = isCurrent,
                                        currentUserAvatarId = currentUserAvatarId,
                                        currentUserPhotoUrl = currentUserPhotoUrl
                                    )
                                }
                            }
                            item { Spacer(Modifier.height(10.dp)) }
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
            .padding(top = 10.dp, start = 8.dp, end = 8.dp, bottom = 4.dp)
    ) {
        val style = MaterialTheme.typography.labelMedium.copy(
            fontFamily = OutfitFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 11.sp,
            letterSpacing = 1.sp
        )
        Text("RANK", style = style, color = TextSecondary, modifier = Modifier.width(52.dp))
        Text("PLAYER", style = style, color = TextSecondary, modifier = Modifier.weight(1f))
        Text("BEST SCORE", style = style, color = TextSecondary, textAlign = TextAlign.End)
    }
}

@Composable
private fun LeaderboardRow(
    rank: Int,
    entry: LeaderboardEntry,
    isCurrentUser: Boolean,
    currentUserAvatarId: String,
    currentUserPhotoUrl: String?
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (isCurrentUser) AccentGold.copy(alpha = 0.16f) else SurfaceCard)
            .border(
                if (isCurrentUser) 1.5.dp else 1.dp,
                if (isCurrentUser) AccentGold else GlassBorder.copy(alpha = 0.45f),
                shape
            )
            .padding(horizontal = 12.dp, vertical = 9.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BoardWellBg)
                .border(1.dp, GlassBorder, CircleShape)
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                ),
                color = TextPrimary
            )
        }
        Spacer(Modifier.width(10.dp))
        LeaderboardAvatar(
            entry = entry,
            isCurrentUser = isCurrentUser,
            currentUserAvatarId = currentUserAvatarId,
            currentUserPhotoUrl = currentUserPhotoUrl,
            size = 42.dp,
            ring = if (isCurrentUser) AccentGold else GlassBorder
        )
        Column(modifier = Modifier.weight(1f).padding(horizontal = 10.dp)) {
            Text(
                text = entry.displayName,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isCurrentUser) {
                Text(
                    "YOU",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 10.sp
                    ),
                    color = AccentGold
                )
            }
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(BoardWellBg)
                .border(1.dp, GlassBorder, RoundedCornerShape(50))
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = "${entry.classicBestScore}",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontFamily = JetBrainsMonoFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp
                ),
                color = AccentGold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun LeaderboardAvatar(
    entry: LeaderboardEntry,
    isCurrentUser: Boolean,
    currentUserAvatarId: String,
    currentUserPhotoUrl: String?,
    size: Dp,
    ring: Color = Color.White
) {
    val resolvedAvatarId = if (isCurrentUser) currentUserAvatarId else entry.avatarId
    val resolvedPhotoUrl = when {
        isCurrentUser && !currentUserPhotoUrl.isNullOrBlank() -> currentUserPhotoUrl
        entry.photoUrl.isNotBlank() -> entry.photoUrl
        else -> null
    }
    PlayerAvatarBadge(
        avatarId = resolvedAvatarId,
        googlePhotoUrl = resolvedPhotoUrl,
        size = size,
        hasCrownCosmetic = resolvedAvatarId == ProfileAvatar.AMBER_CROWN.id,
        shape = CircleShape,
        borderColor = ring,
        borderWidth = 2.dp
    )
}

@Composable
private fun LeaderboardPodium(
    podium: List<LeaderboardEntry>,
    currentUserId: String?,
    currentUserAvatarId: String,
    currentUserPhotoUrl: String?
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(
                Brush.verticalGradient(
                    listOf(SurfaceCardElevated, BoardWellBg)
                )
            )
            .border(1.2.dp, GlassBorder, RoundedCornerShape(24.dp))
            .padding(start = 10.dp, end = 10.dp, top = 20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            val second = podium.getOrNull(1)
            val third = podium.getOrNull(2)
            Box(Modifier.weight(1f)) {
                if (second != null) PodiumSlot(2, second, Color(0xFFC4D1EA), second.userId == currentUserId, currentUserAvatarId, currentUserPhotoUrl, 56.dp, 56.dp)
            }
            Box(Modifier.weight(1.15f)) {
                PodiumSlot(1, podium[0], AccentGoldBright, podium[0].userId == currentUserId, currentUserAvatarId, currentUserPhotoUrl, 74.dp, 88.dp)
            }
            Box(Modifier.weight(1f)) {
                if (third != null) PodiumSlot(3, third, Color(0xFFD48B54), third.userId == currentUserId, currentUserAvatarId, currentUserPhotoUrl, 56.dp, 42.dp)
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
    currentUserAvatarId: String,
    currentUserPhotoUrl: String?,
    avatarSize: Dp,
    pedestalHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        LeaderboardAvatar(
            entry = entry,
            isCurrentUser = isCurrentUser,
            currentUserAvatarId = currentUserAvatarId,
            currentUserPhotoUrl = currentUserPhotoUrl,
            size = avatarSize,
            ring = medal
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = entry.displayName,
            style = MaterialTheme.typography.labelLarge.copy(
                fontFamily = OutfitFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            ),
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
            color = AccentGold,
            maxLines = 1
        )
        Spacer(Modifier.height(8.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(pedestalHeight)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(Brush.verticalGradient(listOf(medal, medal.copy(alpha = 0.45f))))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.35f),
                    RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp)
                )
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 32.sp
                ),
                color = Color.White
            )
        }
    }
}
