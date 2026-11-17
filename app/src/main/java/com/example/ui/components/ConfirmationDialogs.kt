package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.game.GameMode
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardRim
import com.example.ui.theme.SurfaceCard
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
        confirmColor = AccentGold,
        confirmTextColor = Color(0xFF261900),
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
        confirmLabel = "Exit to Home",
        confirmColor = AccentCyan,
        confirmTextColor = Color(0xFF002026),
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
        confirmLabel = "Reset Progress",
        confirmColor = AccentCoral,
        confirmTextColor = Color.White,
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
    confirmColor: Color,
    confirmTextColor: Color,
    dialogTag: String,
    confirmTag: String,
    cancelTag: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(SurfaceCard)
                .border(1.5.dp, BoardRim, RoundedCornerShape(22.dp))
                .padding(20.dp)
                .testTag(dialogTag),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag(cancelTag)
                ) {
                    Text(text = "Cancel", color = MaterialTheme.colorScheme.onSurface)
                }
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = confirmColor),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag(confirmTag)
                ) {
                    Text(
                        text = confirmLabel,
                        fontWeight = FontWeight.Bold,
                        color = confirmTextColor
                    )
                }
            }
        }
    }
}
