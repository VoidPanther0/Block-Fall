package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.ResetProgressConfirmationDialog
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SwitchTrackOff
import com.example.ui.theme.SwitchTrackOn

@Composable
fun SettingsScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleVisualEffects: (Boolean) -> Unit,
    onTogglePlacementPreview: (Boolean) -> Unit,
    onResetProgressConfirmed: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }
    val settings = state.settings

    CascadeScreenContainer(
        maxContentWidth = 580.dp,
        modifier = Modifier.testTag("settings_screen")
    ) {
        ScreenTopBar(
            title = stringResource(R.string.nav_settings),
            onBack = onBack,
            backTestTag = "settings_back_button"
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SettingToggleRow(
                title = "Music",
                icon = Icons.Filled.MusicNote,
                checked = settings.musicEnabled,
                onCheckedChange = onToggleMusic,
                switchTag = "setting_music_switch"
            )

            SettingToggleRow(
                title = "Sound Effects",
                icon = Icons.AutoMirrored.Outlined.VolumeUp,
                checked = settings.soundEnabled,
                onCheckedChange = onToggleSound,
                switchTag = "setting_sound_switch"
            )

            SettingToggleRow(
                title = "Vibration",
                icon = Icons.Filled.Vibration,
                checked = settings.hapticsEnabled,
                onCheckedChange = onToggleHaptics,
                switchTag = "setting_haptics_switch"
            )

            SettingToggleRow(
                title = "Visual Effects",
                icon = Icons.Filled.AutoAwesome,
                checked = settings.visualEffectsEnabled,
                onCheckedChange = onToggleVisualEffects,
                switchTag = "setting_vfx_switch"
            )

            SettingToggleRow(
                title = "Placement Preview",
                icon = Icons.Filled.GridOn,
                checked = settings.placementPreviewEnabled,
                onCheckedChange = onTogglePlacementPreview,
                switchTag = "setting_preview_switch"
            )

            Spacer(Modifier.height(6.dp))

            GlossyArcadeButton(
                text = "Reset Progress",
                icon = Icons.Filled.DeleteForever,
                style = ArcadeButtonStyle.RED,
                onClick = { showResetConfirm = true },
                height = 54.dp,
                cornerRadius = 27.dp,
                fontSize = 17.sp,
                testTag = "settings_reset_progress_button",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(4.dp))
        }
    }

    if (showResetConfirm) {
        ResetProgressConfirmationDialog(
            onConfirm = {
                showResetConfirm = false
                onResetProgressConfirmed()
            },
            onDismiss = { showResetConfirm = false }
        )
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchTag: String
) {
    FrostedGlassCard(
        cornerRadius = 18.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                ),
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = SwitchTrackOn,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.85f),
                    uncheckedTrackColor = SwitchTrackOff,
                    uncheckedBorderColor = Color.Transparent
                ),
                modifier = Modifier.testTag(switchTag)
            )
        }
    }
}
