package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
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
    ConfirmationDialog(
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
fun ResetProgressConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    ConfirmationDialog(
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
fun GravityRulesDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        PremiumCard(
            cornerRadius = 24.dp,
            borderColor = AccentCyan.copy(alpha = 0.45f),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("gravity_rules_dialog")
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .heightIn(max = 520.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(22.dp)
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
                PremiumPrimaryButton(
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
