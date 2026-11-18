package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.game.GameMode
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextSecondary

@Composable
fun RestartConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialogShell(
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
    val message = if (mode == GameMode.CLASSIC) {
        "Your current Classic game is automatically saved so you can resume anytime via Continue."
    } else {
        "Leaving now will discard your current Challenge attempt progress."
    }
    ConfirmationDialogShell(
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag(dialogTag)
        ) {
            androidx.compose.foundation.layout.Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.padding(22.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    GlossyArcadeButton(
                        text = "Cancel",
                        onClick = onDismiss,
                        style = ArcadeButtonStyle.BLUE,
                        height = 48.dp,
                        cornerRadius = 24.dp,
                        testTag = cancelTag,
                        modifier = Modifier.weight(1f)
                    )
                    GlossyArcadeButton(
                        text = confirmLabel,
                        onClick = onConfirm,
                        style = confirmStyle,
                        height = 48.dp,
                        cornerRadius = 24.dp,
                        testTag = confirmTag,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
