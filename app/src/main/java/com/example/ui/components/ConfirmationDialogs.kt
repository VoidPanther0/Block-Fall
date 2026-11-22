package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.game.GameMode
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun RestartConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialogShell(
        icon = Icons.Filled.Refresh,
        accent = AccentGold,
        title = stringResource(R.string.confirm_restart_title),
        message = stringResource(R.string.confirm_restart_body),
        confirmLabel = stringResource(R.string.restart),
        confirmStyle = ArcadeButtonStyle.GOLD,
        dialogTag = "restart_confirmation_dialog",
        confirmTag = "confirm_restart_button",
        cancelTag = "cancel_restart_button",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun ExitConfirmationDialog(
    mode: GameMode,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val message = when (mode) {
        GameMode.CLASSIC -> "Your current Classic board is saved automatically so you can continue anytime."
        GameMode.CHALLENGE -> "Leaving now will discard your current Challenge attempt."
    }
    ConfirmationDialogShell(
        icon = Icons.Filled.Home,
        accent = AccentCyan,
        title = stringResource(R.string.confirm_exit_title),
        message = message,
        confirmLabel = "Exit Home",
        confirmStyle = ArcadeButtonStyle.BLUE,
        dialogTag = "exit_confirmation_dialog",
        confirmTag = "confirm_exit_button",
        cancelTag = "cancel_exit_button",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
fun ResetProgressConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialogShell(
        icon = Icons.Filled.DeleteForever,
        accent = AccentCoral,
        title = stringResource(R.string.confirm_reset_title),
        message = stringResource(R.string.confirm_reset_body),
        confirmLabel = "Reset",
        confirmStyle = ArcadeButtonStyle.RED,
        dialogTag = "reset_progress_confirmation_dialog",
        confirmTag = "confirm_reset_button",
        cancelTag = "cancel_reset_button",
        onConfirm = onConfirm,
        onDismiss = onDismiss
    )
}

@Composable
private fun ConfirmationDialogShell(
    icon: ImageVector,
    accent: Color,
    title: String,
    message: String,
    confirmLabel: String,
    confirmStyle: ArcadeButtonStyle,
    dialogTag: String,
    confirmTag: String,
    cancelTag: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        FrostedGlassCard(
            cornerRadius = 24.dp,
            borderColor = accent.copy(alpha = 0.45f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag(dialogTag)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(22.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.16f))
                        .border(1.dp, accent.copy(alpha = 0.45f), CircleShape)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    ),
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlossyArcadeButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        style = ArcadeButtonStyle.SLATE,
                        height = 48.dp,
                        cornerRadius = 14.dp,
                        fontSize = 15.sp,
                        testTag = cancelTag,
                        modifier = Modifier.weight(1f)
                    )
                    GlossyArcadeButton(
                        text = confirmLabel,
                        onClick = onConfirm,
                        style = confirmStyle,
                        height = 48.dp,
                        cornerRadius = 14.dp,
                        fontSize = 15.sp,
                        testTag = confirmTag,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun GravityRulesDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        FrostedGlassCard(
            cornerRadius = 24.dp,
            borderColor = AccentCyan.copy(alpha = 0.45f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("gravity_rules_dialog")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.padding(22.dp)
            ) {
                Text(
                    text = "How Gravity Works",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 21.sp
                    ),
                    color = TextPrimary
                )
                Text(
                    text = "• Place 3 pieces per tray onto the 8×8 grid.\n• Completing any full row or column clears it.\n• Unsupported blocks drop straight down vertically—triggering chain combos for ×2, ×3+ multipliers!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                GlossyArcadeButton(
                    text = "Got it",
                    onClick = onDismiss,
                    style = ArcadeButtonStyle.GOLD,
                    height = 48.dp,
                    cornerRadius = 14.dp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
