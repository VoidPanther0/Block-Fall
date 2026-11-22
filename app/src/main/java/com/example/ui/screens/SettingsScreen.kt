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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
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
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.ResetProgressConfirmationDialog
import com.example.ui.components.ScreenTopBar
import com.example.ui.components.drawBevelBlock
import com.example.ui.theme.AccentGold
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnTop
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
    onSelectBlockStyle: (BlockStyle) -> Unit,
    onSelectBoardPalette: (BoardColorPalette) -> Unit,
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

            ThemeSwitcherCard(
                selectedBlockStyle = settings.blockStyle,
                selectedBoardPalette = settings.boardPalette,
                onSelectBlockStyle = onSelectBlockStyle,
                onSelectBoardPalette = onSelectBoardPalette
            )

            Spacer(Modifier.height(4.dp))

            GlossyArcadeButton(
                text = "Reset Progress",
                icon = Icons.Filled.DeleteForever,
                style = ArcadeButtonStyle.RED,
                onClick = { showResetConfirm = true },
                height = 54.dp,
                cornerRadius = 18.dp,
                fontSize = 17.sp,
                testTag = "settings_reset_progress_button",
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(6.dp))
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
private fun ThemeSwitcherCard(
    selectedBlockStyle: BlockStyle,
    selectedBoardPalette: BoardColorPalette,
    onSelectBlockStyle: (BlockStyle) -> Unit,
    onSelectBoardPalette: (BoardColorPalette) -> Unit
) {
    FrostedGlassCard(
        cornerRadius = 20.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("theme_switcher_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentGold.copy(alpha = 0.16f))
                        .border(1.dp, AccentGold.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.Palette,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Board & Block Theme",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = "${selectedBlockStyle.displayName} · ${selectedBoardPalette.displayName}",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextSecondary,
                        modifier = Modifier.testTag("theme_active_summary")
                    )
                }
            }

            ThemeLivePreviewStrip(
                blockStyle = selectedBlockStyle,
                boardPalette = selectedBoardPalette
            )

            Text(
                text = "Block Style",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                color = TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BlockStyle.entries.forEach { style ->
                    val selected = style == selectedBlockStyle
                    val chipShape = RoundedCornerShape(14.dp)
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 48.dp)
                            .clip(chipShape)
                            .background(
                                if (selected) {
                                    Brush.verticalGradient(listOf(BlueBtnTop, BlueBtnBottom))
                                } else {
                                    Brush.verticalGradient(
                                        listOf(
                                            SurfaceCardElevated.copy(alpha = 0.7f),
                                            SurfaceCard.copy(alpha = 0.85f)
                                        )
                                    )
                                }
                            )
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) AccentGold else GlassBorder.copy(alpha = 0.45f),
                                shape = chipShape
                            )
                            .clickable { onSelectBlockStyle(style) }
                            .padding(horizontal = 8.dp, vertical = 10.dp)
                            .testTag("theme_block_style_${style.id}")
                    ) {
                        Text(
                            text = style.displayName,
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                fontSize = 13.sp
                            ),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Text(
                text = "Board Palette",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 12.sp),
                color = TextSecondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BoardColorPalette.entries.forEach { palette ->
                    val selected = palette == selectedBoardPalette
                    val boardColors = palette.toBoardColors()
                    val chipShape = RoundedCornerShape(14.dp)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = 56.dp)
                            .clip(chipShape)
                            .background(
                                if (selected) {
                                    SurfaceCardElevated.copy(alpha = 0.94f)
                                } else {
                                    SurfaceCard.copy(alpha = 0.78f)
                                }
                            )
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) AccentGold else GlassBorder.copy(alpha = 0.45f),
                                shape = chipShape
                            )
                            .clickable { onSelectBoardPalette(palette) }
                            .padding(horizontal = 4.dp, vertical = 8.dp)
                            .testTag("theme_board_palette_${palette.id}")
                    ) {
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(boardColors.swatchPrimary, boardColors.swatchSecondary)
                                    )
                                )
                                .border(1.5.dp, Color.White.copy(alpha = 0.78f), CircleShape)
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = palette.displayName,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontSize = 11.sp
                            ),
                            color = if (selected) AccentGold else Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeLivePreviewStrip(
    blockStyle: BlockStyle,
    boardPalette: BoardColorPalette
) {
    val boardColors = boardPalette.toBoardColors()
    val shape = RoundedCornerShape(if (blockStyle == BlockStyle.RETRO) 8.dp else 14.dp)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(54.dp)
            .clip(shape)
            .background(Brush.verticalGradient(listOf(boardColors.wellTop, boardColors.wellBottom)))
            .border(1.5.dp, boardColors.rim, shape)
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .testTag("theme_live_preview")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cols = 6
            val cellSize = minOf(size.width / cols.toFloat(), size.height)
            val totalW = cellSize * cols
            val startX = (size.width - totalW) / 2f
            val startY = (size.height - cellSize) / 2f
            val gap = (cellSize * 0.10f).coerceAtLeast(2f)
            val tileSize = cellSize - gap
            val slotRadius = if (blockStyle == BlockStyle.RETRO) {
                CornerRadius(2f, 2f)
            } else {
                CornerRadius(tileSize * 0.18f, tileSize * 0.18f)
            }

            val sampleColors = listOf(
                BlockColor.CYAN,
                BlockColor.AMBER,
                BlockColor.CORAL,
                BlockColor.EMERALD,
                BlockColor.VIOLET,
                null
            )

            for (c in 0 until cols) {
                val left = startX + c * cellSize + gap / 2f
                val top = startY + gap / 2f
                drawRoundRect(
                    color = boardColors.slotFill,
                    topLeft = Offset(left, top),
                    size = Size(tileSize, tileSize),
                    cornerRadius = slotRadius
                )
                drawRoundRect(
                    color = boardColors.slotBorder,
                    topLeft = Offset(left, top),
                    size = Size(tileSize, tileSize),
                    cornerRadius = slotRadius,
                    style = Stroke(width = 1f)
                )
                val blockColor = sampleColors[c]
                if (blockColor != null) {
                    drawBevelBlock(
                        left = left,
                        top = top,
                        cellSize = tileSize,
                        color = blockColor,
                        blockStyle = blockStyle,
                        boardPalette = boardPalette
                    )
                }
            }
        }
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
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(22.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = OutfitFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                ),
                color = TextPrimary,
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
