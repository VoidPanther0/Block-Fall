package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.GameUiState
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.ResetProgressConfirmationDialog
import com.example.ui.components.ScreenTopBar
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardRim
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.TextSecondary

@Composable
fun SettingsScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleVisualEffects: (Boolean) -> Unit,
    onTogglePlacementPreview: (Boolean) -> Unit,
    onOpenHowToPlay: () -> Unit,
    onOpenRules: () -> Unit,
    onResetProgressConfirmed: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }
    val settings = state.settings

    CascadeScreenContainer(
        maxContentWidth = 600.dp,
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "AUDIO & FEEDBACK",
                style = MaterialTheme.typography.labelMedium,
                color = AccentCyan
            )

            SettingToggleRow(
                title = "Sound Effects",
                subtitle = "Chimes for block drops, line clears, and cascades",
                icon = Icons.AutoMirrored.Outlined.VolumeUp,
                accent = AccentCyan,
                checked = settings.soundEnabled,
                onCheckedChange = onToggleSound,
                switchTag = "setting_sound_switch"
            )

            SettingToggleRow(
                title = "Background Music",
                subtitle = "Soft ambient synth pulse during active play",
                icon = Icons.Filled.MusicNote,
                accent = AccentEmerald,
                checked = settings.musicEnabled,
                onCheckedChange = onToggleMusic,
                switchTag = "setting_music_switch"
            )

            SettingToggleRow(
                title = "Vibration & Haptics",
                subtitle = "Tactile pulse on piece grabs, taps, and chain waves",
                icon = Icons.Filled.Vibration,
                accent = AccentGold,
                checked = settings.hapticsEnabled,
                onCheckedChange = onToggleHaptics,
                switchTag = "setting_haptics_switch"
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "GAMEPLAY & VISUALS",
                style = MaterialTheme.typography.labelMedium,
                color = AccentGold
            )

            SettingToggleRow(
                title = "Visual Effects",
                subtitle = "Shatter sparks, landing dust, and block glow bursts",
                icon = Icons.Filled.AutoAwesome,
                accent = AccentGold,
                checked = settings.visualEffectsEnabled,
                onCheckedChange = onToggleVisualEffects,
                switchTag = "setting_vfx_switch"
            )

            SettingToggleRow(
                title = "Placement Preview",
                subtitle = "Ghost piece outline & gold line-clear prediction",
                icon = Icons.Filled.GridOn,
                accent = AccentCyan,
                checked = settings.placementPreviewEnabled,
                onCheckedChange = onTogglePlacementPreview,
                switchTag = "setting_preview_switch"
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "HELP & DATA",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenHowToPlay,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("settings_how_to_play_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.HelpOutline,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text("How to Play", color = MaterialTheme.colorScheme.onSurface)
                }

                OutlinedButton(
                    onClick = onOpenRules,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("settings_rules_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.MenuBook,
                        contentDescription = null,
                        tint = AccentEmerald,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text("Rules", color = MaterialTheme.colorScheme.onSurface)
                }
            }

            OutlinedButton(
                onClick = { showResetConfirm = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("settings_reset_progress_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.DeleteForever,
                    contentDescription = null,
                    tint = AccentCoral,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.size(8.dp))
                Text(text = "Reset All Progress", color = AccentCoral)
            }
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
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.dp, BoardRim, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(24.dp)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                color = TextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(switchTag)
        )
    }
}
