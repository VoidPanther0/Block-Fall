package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.GameUiState
import com.example.ui.components.ArcadeButtonStyle
import com.example.ui.components.CascadeScreenContainer
import com.example.ui.components.ExitConfirmationDialog
import com.example.ui.components.FloatingModalBadge
import com.example.ui.components.FrostedGlassCard
import com.example.ui.components.GlossyArcadeButton
import com.example.ui.components.RestartConfirmationDialog
import com.example.ui.theme.BlueBtnBottom
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.OutfitFontFamily

@Composable
fun PauseScreen(
    state: GameUiState,
    onResume: () -> Unit,
    onRestartConfirmed: () -> Unit,
    onExitToHomeConfirmed: () -> Unit
) {
    var showRestartConfirm by remember { mutableStateOf(false) }
    var showExitConfirm by remember { mutableStateOf(false) }

    BackHandler(onBack = onResume)

    CascadeScreenContainer(
        maxContentWidth = 420.dp,
        modifier = Modifier.testTag("pause_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                contentAlignment = Alignment.TopCenter,
                modifier = Modifier.fillMaxWidth()
            ) {
                FrostedGlassCard(
                    cornerRadius = 28.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 34.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, end = 24.dp, top = 46.dp, bottom = 24.dp)
                    ) {
                        Text(
                            text = "Paused",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontFamily = OutfitFontFamily,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 28.sp
                            ),
                            color = Color.White
                        )

                        Spacer(Modifier.height(2.dp))

                        GlossyArcadeButton(
                            text = stringResource(R.string.resume),
                            style = ArcadeButtonStyle.GREEN,
                            onClick = onResume,
                            height = 52.dp,
                            cornerRadius = 26.dp,
                            fontSize = 18.sp,
                            testTag = "pause_resume_button",
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlossyArcadeButton(
                            text = stringResource(R.string.restart),
                            style = ArcadeButtonStyle.BLUE,
                            onClick = { showRestartConfirm = true },
                            height = 52.dp,
                            cornerRadius = 26.dp,
                            fontSize = 18.sp,
                            testTag = "pause_restart_button",
                            modifier = Modifier.fillMaxWidth()
                        )

                        GlossyArcadeButton(
                            text = stringResource(R.string.nav_home),
                            style = ArcadeButtonStyle.BLUE,
                            onClick = { showExitConfirm = true },
                            height = 52.dp,
                            cornerRadius = 26.dp,
                            fontSize = 18.sp,
                            testTag = "pause_home_button",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                FloatingModalBadge(
                    size = 68.dp,
                    backgroundBrush = Brush.verticalGradient(listOf(BlueBtnTop, BlueBtnBottom)),
                    modifier = Modifier.offset(y = 0.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Pause,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }

    if (showRestartConfirm) {
        RestartConfirmationDialog(
            onConfirm = {
                showRestartConfirm = false
                onRestartConfirmed()
            },
            onDismiss = { showRestartConfirm = false }
        )
    }

    if (showExitConfirm) {
        ExitConfirmationDialog(
            mode = state.mode,
            onConfirm = {
                showExitConfirm = false
                onExitToHomeConfirmed()
            },
            onDismiss = { showExitConfirm = false }
        )
    }
}
