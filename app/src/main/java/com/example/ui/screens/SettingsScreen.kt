package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
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
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.BlockColor
import com.example.game.BlockStyle
import com.example.game.BoardColorPalette
import com.example.ui.GameUiState
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.PremiumCard
import com.example.ui.components.ScreenTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.components.drawBevelBlock
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.SwitchTrackOff
import com.example.ui.theme.SwitchTrackOn
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.toBoardColors

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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionHeader(kicker = "Audio", title = "Sound & Music", accent = AccentCyan)
            PremiumCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingToggleRow(
                        title = "Music",
                        subtitle = "Ambient soundtrack during sessions",
                        icon = Icons.Filled.MusicNote,
                        checked = settings.musicEnabled,
                        onCheckedChange = onToggleMusic,
                        switchTag = "setting_music_switch"
                    )
                    HorizontalDivider(color = DividerSubtle.copy(alpha = 0.6f))
                    SettingToggleRow(
                        title = "Sound Effects",
                        subtitle = "Drops, line clears & cascade chimes",
                        icon = Icons.AutoMirrored.Outlined.VolumeUp,
                        checked = settings.soundEnabled,
                        onCheckedChange = onToggleSound,
                        switchTag = "setting_sound_switch"
                    )
                }
            }

            SectionHeader(kicker = "Touch", title = "Haptic Feedback", accent = AccentEmerald)
            PremiumCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                SettingToggleRow(
                    title = "Vibration & Haptics",
                    subtitle = "Tactile feedback on placement and cascades",
                    icon = Icons.Filled.Vibration,
                    checked = settings.hapticsEnabled,
                    onCheckedChange = onToggleHaptics,
                    switchTag = "setting_haptics_switch"
                )
            }

            SectionHeader(kicker = "Graphics", title = "Visuals & Performance", accent = AccentGold)
            PremiumCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingToggleRow(
                        title = "Visual Effects",
                        subtitle = "Line sweeps, particles & board glow",
                        icon = Icons.Filled.AutoAwesome,
                        checked = settings.visualEffectsEnabled,
                        onCheckedChange = onToggleVisualEffects,
                        switchTag = "setting_vfx_switch"
                    )
                    HorizontalDivider(color = DividerSubtle.copy(alpha = 0.6f))
                    SettingToggleRow(
                        title = "Placement Preview",
                        subtitle = "Highlight predicted row & column clears",
                        icon = Icons.Filled.GridOn,
                        checked = settings.placementPreviewEnabled,
                        onCheckedChange = onTogglePlacementPreview,
                        switchTag = "setting_preview_switch"
                    )
                }
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    switchTag: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(icon, null, tint = TextPrimary, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
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
