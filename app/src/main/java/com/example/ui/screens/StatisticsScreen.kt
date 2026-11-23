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
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.EconomyEngine
import com.example.game.ProfileAvatar
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.CurrencyWallet
import com.example.ui.components.PlayerAvatarBadge
import com.example.ui.components.PremiumCard
import com.example.ui.components.PremiumPrimaryButton
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
    onRenameWithDiamonds: (String, (Boolean, String) -> Unit) -> Unit = { _, _ -> }
) {
    val effectiveName = state.playerDisplayName?.takeIf { it.isNotBlank() }
        ?: playerName?.takeIf { it.isNotBlank() }
        ?: "Player"
    var showRenameDialog by remember { mutableStateOf(false) }

    CascadeScreenContainer(
        maxContentWidth = 760.dp,
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

            BestScoreCard(classicBestScore = state.classicBestScore)

            Spacer(Modifier.height(6.dp))
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

    PremiumCard(
        cornerRadius = 20.dp,
        borderColor = (if (hasCrown) AccentGold else AccentCyan).copy(alpha = 0.45f),
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
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerAvatarBadge(
                    avatarId = state.profileAvatarId,
                    googlePhotoUrl = googlePhotoUrl,
                    size = 64.dp,
                    hasCrownCosmetic = hasCrown
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(3.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 20.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("profile_display_name_text")
                    )
                    Text(
                        text = "TIER ${rank.tierNumber} · ${rank.title}",
                        style = CascadeTextStyles.SectionKicker,
                        color = if (hasCrown) AccentGold else AccentCyan
                    )
                    Text(
                        text = "${rank.xp} XP",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = JetBrainsMonoFontFamily,
                            fontWeight = FontWeight.Bold
                        ),
                        color = AccentGold
                    )
                }
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

            PremiumPrimaryButton(
                text = "Change Name · ${EconomyEngine.RENAME_DIAMOND_COST} Diamonds",
                icon = Icons.Filled.Edit,
                style = ArcadeButtonStyle.BLUE,
                onClick = onOpenRename,
                height = 48.dp,
                cornerRadius = 12.dp,
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
    PremiumCard(
        cornerRadius = 20.dp,
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
                            .background(if (isSelected) AccentCyan.copy(alpha = 0.12f) else BoardWellBg)
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
private fun BestScoreCard(classicBestScore: Int) {
    PremiumCard(
        cornerRadius = 20.dp,
        borderColor = AccentGold.copy(alpha = 0.42f),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("profile_best_score_card")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(AccentGold.copy(alpha = 0.16f))
                    .border(1.dp, AccentGold.copy(alpha = 0.42f), RoundedCornerShape(14.dp))
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = AccentGold,
                    modifier = Modifier.size(26.dp)
                )
            }
            Column {
                Text(
                    text = "BEST SCORE",
                    style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.8.sp),
                    color = AccentGold
                )
                Text(
                    text = "$classicBestScore",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = JetBrainsMonoFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 30.sp
                    ),
                    color = TextPrimary
                )
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
