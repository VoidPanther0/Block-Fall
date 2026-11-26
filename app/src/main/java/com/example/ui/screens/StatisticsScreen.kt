package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.EconomyEngine
import com.example.game.ProfileAvatar
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.BlockFallBottomNav
import com.example.ui.components.BottomNavTab
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.CurrencyWallet
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.PlayerAvatarBadge
import com.example.ui.components.ScreenTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentDiamond
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.CascadeTextStyles
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun StatisticsScreen(
    state: GameUiState,
    onBack: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onResetProgressConfirmed: () -> Unit = {},
    playerName: String? = null,
    googlePhotoUrl: String? = null,
    onSelectAvatar: (String) -> Unit = {},
    onRenameWithDiamonds: (String, (Boolean, String) -> Unit) -> Unit = { _, _ -> },
    cloudAccountContent: @Composable () -> Unit = {},
    onOpenTab: ((BottomNavTab) -> Unit)? = null
) {
    val effectiveName = state.playerDisplayName?.takeIf { it.isNotBlank() }
        ?: playerName?.takeIf { it.isNotBlank() }
        ?: "Player"
    var showRenameDialog by remember { mutableStateOf(false) }

    CascadeScreenContainer(
        maxContentWidth = 760.dp,
        footerBar = if (onOpenTab != null) {
            { BlockFallBottomNav(selectedTab = BottomNavTab.PROFILE, onSelectTab = onOpenTab) }
        } else null,
        modifier = Modifier.testTag("statistics_screen")
    ) {
        ScreenTopBar(
            title = "Profile",
            onBack = onBack,
            backTestTag = "statistics_back_button"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            ProfileIdentityCard(
                state = state,
                displayName = effectiveName,
                googlePhotoUrl = googlePhotoUrl,
                onOpenRename = { showRenameDialog = true }
            )

            AvatarPickerCard(
                selectedAvatarId = state.profileAvatarId,
                googlePhotoUrl = googlePhotoUrl,
                hasCrown = "cosmetic_prism_crown" in state.wallet.unlockedEntitlements,
                onSelectAvatar = onSelectAvatar
            )

            BestScoreCard(
                classicBestScore = state.classicBestScore,
                totalGames = state.statistics.totalGamesPlayed,
                totalLines = state.statistics.totalLinesCleared
            )

            cloudAccountContent()

            Spacer(Modifier.height(8.dp))
        }
    }

    if (showRenameDialog) {
        RenameWithDiamondsDialog(
            currentName = effectiveName,
            availableDiamonds = state.wallet.diamonds,
            onConfirm = { newName, onFeedback ->
                onRenameWithDiamonds(newName) { success, msg ->
                    if (success) {
                        showRenameDialog = false
                    } else {
                        onFeedback(msg)
                    }
                }
            },
            onDismiss = { showRenameDialog = false }
        )
    }
}

@Composable
private fun ProfileIdentityCard(
    state: GameUiState,
    displayName: String,
    googlePhotoUrl: String?,
    onOpenRename: () -> Unit
) {
    val rank = state.playerRank
    val hasCrown = "cosmetic_prism_crown" in state.wallet.unlockedEntitlements

    FrostedGlassCard(
        cornerRadius = 22.dp,
        borderColor = (if (hasCrown) AccentGold else AccentCyan).copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("player_rank_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerAvatarBadge(
                    avatarId = state.profileAvatarId,
                    googlePhotoUrl = googlePhotoUrl,
                    size = 68.dp,
                    hasCrownCosmetic = hasCrown
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("profile_display_name_text")
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (hasCrown) AccentGold.copy(alpha = 0.15f) else AccentCyan.copy(alpha = 0.15f))
                                .border(1.dp, if (hasCrown) AccentGold.copy(alpha = 0.45f) else AccentCyan.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "TIER ${rank.tierNumber} · ${rank.title}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontFamily = JetBrainsMonoFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    letterSpacing = 0.2.sp
                                ),
                                color = if (hasCrown) AccentGold else AccentCyan
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Rank Mastery",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp
                        ),
                        color = TextSecondary
                    )
                    Text(
                        text = "${rank.xp} XP",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        ),
                        color = AccentGold
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(BoardWellBg)
                        .border(1.dp, GlassBorder.copy(alpha = 0.45f), CircleShape)
                        .padding(2.dp)
                ) {
                    if (rank.progressToNext > 0f) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(rank.progressToNext.coerceIn(0.05f, 1f))
                                .clip(CircleShape)
                                .background(Brush.horizontalGradient(listOf(AccentCyan, AccentEmerald)))
                        )
                    }
                }
            }

            GlossyArcadeButton(
                text = "Change Name · ${EconomyEngine.RENAME_DIAMOND_COST} Diamonds",
                icon = Icons.Filled.Edit,
                style = ArcadeButtonStyle.BLUE,
                onClick = onOpenRename,
                height = 48.dp,
                cornerRadius = 14.dp,
                fontSize = 14.sp,
                testTag = "profile_rename_button",
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.35f))
            CurrencyWallet(
                goldCoins = state.wallet.goldCoins,
                diamonds = state.wallet.diamonds,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AvatarPickerCard(
    selectedAvatarId: String,
    googlePhotoUrl: String?,
    hasCrown: Boolean,
    onSelectAvatar: (String) -> Unit
) {
    FrostedGlassCard(
        cornerRadius = 22.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_avatar_picker_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(18.dp)
        ) {
            SectionHeader(
                kicker = "Identity",
                title = "Choose Avatar",
                accent = AccentCyan
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ProfileAvatar.entries.forEach { avatar ->
                    val isSelected = selectedAvatarId == avatar.id
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (isSelected) AccentCyan.copy(alpha = 0.16f) else BoardWellBg)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) AccentCyan else GlassBorder,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onSelectAvatar(avatar.id) }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                            .testTag("avatar_option_${avatar.id}")
                    ) {
                        Box(contentAlignment = Alignment.BottomEnd) {
                            PlayerAvatarBadge(
                                avatarId = avatar.id,
                                googlePhotoUrl = googlePhotoUrl,
                                size = 50.dp,
                                hasCrownCosmetic = isSelected && hasCrown
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Filled.CheckCircle,
                                    contentDescription = "Selected",
                                    tint = AccentEmerald,
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(SurfaceCard)
                                )
                            }
                        }
                        Text(
                            text = if (avatar == ProfileAvatar.GOOGLE_PHOTO && googlePhotoUrl.isNullOrBlank()) {
                                "Google"
                            } else {
                                avatar.label
                            },
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = if (isSelected) AccentCyan else TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BestScoreCard(
    classicBestScore: Int,
    totalGames: Int = 0,
    totalLines: Int = 0
) {
    FrostedGlassCard(
        cornerRadius = 22.dp,
        borderColor = AccentGold.copy(alpha = 0.5f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_best_score_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentGold.copy(alpha = 0.16f))
                        .border(1.2.dp, AccentGold.copy(alpha = 0.55f), RoundedCornerShape(16.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(30.dp)
                    )
                }
                Column {
                    Text(
                        text = "BEST SCORE",
                        style = MaterialTheme.typography.labelMedium.copy(
                            letterSpacing = 1.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = AccentGold
                    )
                    Text(
                        text = "$classicBestScore",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 32.sp
                        ),
                        color = TextPrimary
                    )
                }
            }

            HorizontalDivider(color = GlassBorder.copy(alpha = 0.35f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BoardWellBg)
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GAMES PLAYED",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextSecondary
                    )
                    Text(
                        text = "$totalGames",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(BoardWellBg)
                        .padding(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "LINES CLEARED",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = TextSecondary
                    )
                    Text(
                        text = "$totalLines",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

@Composable
private fun RenameWithDiamondsDialog(
    currentName: String,
    availableDiamonds: Int,
    onConfirm: (String, (String) -> Unit) -> Unit,
    onDismiss: () -> Unit
) {
    var draftName by remember { mutableStateOf(currentName) }
    var feedbackError by remember { mutableStateOf<String?>(null) }
    val hasEnoughDiamonds = availableDiamonds >= EconomyEngine.RENAME_DIAMOND_COST

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("profile_rename_dialog"),
        title = {
            Text(
                text = "Change Player Name",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold
                )
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Cost: ${EconomyEngine.RENAME_DIAMOND_COST} Diamonds (Available: $availableDiamonds)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (hasEnoughDiamonds) AccentDiamond else AccentCoral
                )
                OutlinedTextField(
                    value = draftName,
                    onValueChange = { input ->
                        draftName = input.take(EconomyEngine.MAX_PLAYER_NAME_LENGTH)
                        feedbackError = null
                    },
                    label = { Text("New display name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_rename_input")
                )
                if (feedbackError != null) {
                    Text(
                        text = feedbackError.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = AccentCoral,
                        modifier = Modifier.testTag("profile_rename_feedback")
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = draftName.trim()
                    when {
                        trimmed.isEmpty() -> feedbackError = "Enter a name (1–24 characters)"
                        !hasEnoughDiamonds -> feedbackError = "Need ${EconomyEngine.RENAME_DIAMOND_COST} Diamonds to change name"
                        else -> onConfirm(trimmed) { err -> feedbackError = err }
                    }
                },
                modifier = Modifier.testTag("profile_rename_confirm_button")
            ) {
                Text("Pay ${EconomyEngine.RENAME_DIAMOND_COST} Diamonds")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("profile_rename_cancel_button")
            ) {
                Text("Cancel")
            }
        }
    )
}
