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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.VolumeUp
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.SupportAgent
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.game.BlockColor
import com.example.game.BlockStyle
import com.example.game.BoardColorPalette
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.ResetProgressConfirmationDialog
import com.example.ui.components.ScreenTopBar
import com.example.ui.components.SectionHeader
import com.example.ui.components.drawBevelBlock
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BoardWellBg
import com.example.ui.theme.DividerSubtle
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SwitchTrackOff
import com.example.ui.theme.SwitchTrackOn
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.toBoardColors

private val SupportedLanguages = listOf(
    "English", "Español", "Français", "Deutsch", "Português", "Italiano", "日本語", "한국어"
)

@Composable
fun SettingsScreen(
    state: GameUiState,
    onBack: () -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleMusic: (Boolean) -> Unit,
    onToggleHaptics: (Boolean) -> Unit,
    onToggleVisualEffects: (Boolean) -> Unit,
    onTogglePlacementPreview: (Boolean) -> Unit,
    onResetProgressConfirmed: () -> Unit,
    isCloudSignedIn: Boolean = false,
    onSelectBlockStyle: (BlockStyle) -> Unit = {},
    onSelectBoardPalette: (BoardColorPalette) -> Unit = {},
    onSelectMusicTrack: (com.example.game.MusicTrack) -> Unit = {},
    onSelectLanguage: (String) -> Unit = {}
) {
    val settings = state.settings
    var showResetDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var policyDialogTitle by remember { mutableStateOf<String?>(null) }
    var policyDialogBody by remember { mutableStateOf<String?>(null) }
    var resetConfirmationMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(resetConfirmationMessage) {
        if (resetConfirmationMessage != null) {
            kotlinx.coroutines.delay(4000L)
            resetConfirmationMessage = null
        }
    }

    CascadeScreenContainer(
        maxContentWidth = 640.dp,
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
            // Audio Section
            SectionHeader(kicker = "Audio", title = "Sound & Music", accent = AccentCyan)
            FrostedGlassCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingToggleRow(
                        title = "Music",
                        subtitle = "Ambient soundtrack during sessions",
                        icon = Icons.Filled.MusicNote,
                        checked = settings.musicEnabled,
                        onCheckedChange = onToggleMusic,
                        switchTag = "setting_music_switch"
                    )
                    if (settings.musicEnabled) {
                        HorizontalDivider(color = DividerSubtle.copy(alpha = 0.5f))
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "SOUNDTRACK VARIETY",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                                color = AccentCyan
                            )
                            com.example.game.MusicTrack.entries.forEach { track ->
                                val isSelected = track == settings.musicTrack
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) AccentCyan.copy(alpha = 0.16f) else BoardWellBg)
                                        .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) AccentCyan else GlassBorder, RoundedCornerShape(12.dp))
                                        .clickable { onSelectMusicTrack(track) }
                                        .padding(horizontal = 14.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) AccentCyan else BoardWellBg)
                                            .border(1.dp, if (isSelected) AccentCyan else GlassBorder, CircleShape)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.MusicNote,
                                            contentDescription = null,
                                            tint = if (isSelected) Color(0xFF001A3D) else TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = track.displayName,
                                            style = MaterialTheme.typography.titleSmall.copy(
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp
                                            ),
                                            color = if (isSelected) AccentCyan else TextPrimary
                                        )
                                        Text(
                                            text = track.subtitle,
                                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                            color = TextSecondary
                                        )
                                    }
                                }
                            }
                        }
                    }
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

            // Touch Section
            SectionHeader(kicker = "Touch", title = "Haptic Feedback", accent = AccentEmerald)
            FrostedGlassCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                SettingToggleRow(
                    title = "Vibration & Haptics",
                    subtitle = "Tactile feedback on placement and cascades",
                    icon = Icons.Filled.Vibration,
                    checked = settings.hapticsEnabled,
                    onCheckedChange = onToggleHaptics,
                    switchTag = "setting_haptics_switch"
                )
            }

            // Visuals Section
            SectionHeader(kicker = "Graphics", title = "Visuals & Performance", accent = AccentGold)
            FrostedGlassCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
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

            // Customization Themes
            SectionHeader(kicker = "Themes & Atmosphere", title = "Background & Board Design", accent = AccentCyan)
            FrostedGlassCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "BACKGROUND & ATMOSPHERE THEME",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = AccentGold
                    )
                    Text(
                        text = "Choose your favorite background colors, atmospheric glow, and board theme.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    // Grid of 6 themes (2 columns of 3)
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        BoardColorPalette.entries.chunked(2).forEach { rowPalettes ->
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                rowPalettes.forEach { pal ->
                                    val isSelected = pal == settings.boardPalette
                                    val boardColors = pal.toBoardColors()
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(if (isSelected) AccentGold.copy(alpha = 0.18f) else BoardWellBg)
                                            .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) AccentGold else GlassBorder, RoundedCornerShape(12.dp))
                                            .clickable { onSelectBoardPalette(pal) }
                                            .padding(vertical = 10.dp, horizontal = 10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .clip(CircleShape)
                                                .background(boardColors.rim)
                                                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                                        )
                                        Text(
                                            text = pal.themeTitle,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontFamily = OutfitFontFamily,
                                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                                fontSize = 12.sp
                                            ),
                                            color = if (isSelected) AccentGold else TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                if (rowPalettes.size == 1) {
                                    Spacer(Modifier.weight(1f))
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = DividerSubtle.copy(alpha = 0.6f))

                    Text(
                        text = "BLOCK SHADING",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, letterSpacing = 1.sp),
                        color = AccentCyan
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        BlockStyle.entries.forEach { style ->
                            val isSelected = style == settings.blockStyle
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) AccentCyan.copy(alpha = 0.18f) else BoardWellBg)
                                    .border(if (isSelected) 1.5.dp else 1.dp, if (isSelected) AccentCyan else GlassBorder, RoundedCornerShape(12.dp))
                                    .clickable { onSelectBlockStyle(style) }
                                    .padding(vertical = 10.dp, horizontal = 4.dp)
                            ) {
                                Canvas(modifier = Modifier.size(24.dp)) {
                                    drawBevelBlock(
                                        left = 0f,
                                        top = 0f,
                                        cellSize = size.minDimension,
                                        color = BlockColor.CYAN,
                                        blockStyle = style,
                                        boardPalette = settings.boardPalette
                                    )
                                }
                                Text(
                                    text = style.displayName,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium
                                    ),
                                    color = if (isSelected) AccentCyan else TextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // Language & Legal
            SectionHeader(kicker = "App & Community", title = "Language & Information", accent = AccentEmerald)
            FrostedGlassCard(cornerRadius = 20.dp, modifier = Modifier.fillMaxWidth()) {
                Column {
                    SettingActionRow(
                        title = "Language",
                        subtitle = state.selectedLanguage,
                        icon = Icons.Filled.Language,
                        onClick = { showLanguageDialog = true }
                    )
                    HorizontalDivider(color = DividerSubtle.copy(alpha = 0.6f))
                    SettingActionRow(
                        title = "Privacy Policy",
                        subtitle = "Data handling & player telemetry guidelines",
                        icon = Icons.Filled.PrivacyTip,
                        onClick = {
                            policyDialogTitle = "Privacy Policy"
                            policyDialogBody = "BLOCK FALL respects your privacy. All user-authored records, puzzle state, and scores are stored locally on your device or securely synced via Google Sign-In and Firebase Firestore. No personal data is sold or shared with third parties."
                        }
                    )
                    HorizontalDivider(color = DividerSubtle.copy(alpha = 0.6f))
                    SettingActionRow(
                        title = "Terms of Service",
                        subtitle = "Fair play and game rules",
                        icon = Icons.Filled.Info,
                        onClick = {
                            policyDialogTitle = "Terms of Service"
                            policyDialogBody = "BLOCK FALL is intended for personal, casual entertainment. Fair play and integrity checks protect global leaderboards and game economy. In-game items and currencies are virtual items with no real-world monetary value."
                        }
                    )
                    HorizontalDivider(color = DividerSubtle.copy(alpha = 0.6f))
                    SettingActionRow(
                        title = "Support & Feedback",
                        subtitle = "Report issues or send feature requests",
                        icon = Icons.Filled.SupportAgent,
                        onClick = {
                            policyDialogTitle = "Support & Feedback"
                            policyDialogBody = "Need help or have suggestions for new puzzles? Reach our community support team at support@blockfall.game. We reply within 24 hours!"
                        }
                    )
                }
            }

            // Danger Zone: Reset Progress
            SectionHeader(kicker = "Danger Zone", title = "Account Reset", accent = AccentCoral)
            FrostedGlassCard(
                cornerRadius = 20.dp,
                borderColor = AccentCoral.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentCoral.copy(alpha = 0.16f))
                                .border(1.dp, AccentCoral.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        ) {
                            Icon(Icons.Filled.DeleteForever, null, tint = AccentCoral, modifier = Modifier.size(24.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Reset All Progress",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontFamily = OutfitFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                ),
                                color = TextPrimary
                            )
                            Text(
                                text = if (isCloudSignedIn) {
                                    "Signs out of Cloud Sync and clears local scores, wallet, and levels"
                                } else {
                                    "Clears local scores, wallet, levels, rewards, and starts fresh"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                    if (resetConfirmationMessage != null) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AccentEmerald.copy(alpha = 0.15f))
                                .border(1.dp, AccentEmerald.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                                .testTag("settings_reset_confirmation_banner")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = null,
                                tint = AccentEmerald,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = resetConfirmationMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                color = TextPrimary
                            )
                        }
                    }
                    GlossyArcadeButton(
                        text = "Reset All Progress",
                        icon = Icons.Filled.DeleteForever,
                        style = ArcadeButtonStyle.RED,
                        onClick = { showResetDialog = true },
                        height = 48.dp,
                        cornerRadius = 14.dp,
                        fontSize = 14.sp,
                        horizontalPadding = 16.dp,
                        modifier = Modifier.fillMaxWidth(),
                        testTag = "setting_reset_progress_button"
                    )
                }
            }

            Spacer(Modifier.height(10.dp))
        }
    }

    // Reset Progress Confirmation Dialog
    if (showResetDialog) {
        ResetProgressConfirmationDialog(
            isCloudSignedIn = isCloudSignedIn,
            onConfirm = {
                showResetDialog = false
                resetConfirmationMessage = if (isCloudSignedIn) {
                    "Signed out and all local progress reset."
                } else {
                    "All local progress has been reset."
                }
                onResetProgressConfirmed()
            },
            onDismiss = { showResetDialog = false }
        )
    }

    // Language Selector Dialog
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = {
                Text(
                    text = "Select Language",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    SupportedLanguages.forEach { lang ->
                        val isSelected = lang == state.selectedLanguage
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) AccentCyan.copy(alpha = 0.16f) else Color.Transparent)
                                .clickable {
                                    onSelectLanguage(lang)
                                    showLanguageDialog = false
                                }
                                .padding(horizontal = 12.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = lang,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                ),
                                color = if (isSelected) AccentCyan else TextPrimary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Policy / Terms / Support Dialog
    if (policyDialogTitle != null && policyDialogBody != null) {
        AlertDialog(
            onDismissRequest = {
                policyDialogTitle = null
                policyDialogBody = null
            },
            title = {
                Text(
                    text = policyDialogTitle.orEmpty(),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = OutfitFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                )
            },
            text = {
                Text(
                    text = policyDialogBody.orEmpty(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        policyDialogTitle = null
                        policyDialogBody = null
                    }
                ) {
                    Text("OK")
                }
            }
        )
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

@Composable
private fun SettingActionRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Icon(icon, null, tint = AccentCyan, modifier = Modifier.size(22.dp))
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
    }
}
