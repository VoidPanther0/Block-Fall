package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.ArcadeBlockEmblem
import com.example.ui.components.ArcadeTitleLogo
import com.example.ui.components.GoogleSignInCard
import com.example.ui.components.RoyalSkyBackdrop
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.SurfaceCard
import com.example.ui.theme.SurfaceCardElevated
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

/**
 * Lightweight first-launch onboarding. Signing in keeps the existing auth callbacks intact;
 * skipping persists a one-time flag in CascadeApp so this screen is never shown again.
 */
@Composable
fun OnboardingScreen(
    onContinue: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().testTag("onboarding_screen")) {
        RoyalSkyBackdrop()

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Spacer(Modifier.height(16.dp))

                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(28.dp))
                        .background(Brush.verticalGradient(listOf(SurfaceCardElevated, SurfaceCard)))
                        .border(1.dp, AccentCyan.copy(alpha = 0.45f), RoundedCornerShape(28.dp))
                ) {
                    ArcadeBlockEmblem(size = 66.dp)
                }

                ArcadeTitleLogo(fontSize = 38.sp)

                Text(
                    text = "A premium gravity puzzle. Plan every drop, clear lines and chain cascades for huge scores.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(Modifier.height(8.dp))

                OnboardingBenefit(
                    icon = Icons.Filled.CloudDone,
                    accent = AccentCyan,
                    title = "Cloud saves",
                    description = "Your progress follows you across devices."
                )
                OnboardingBenefit(
                    icon = Icons.Filled.EmojiEvents,
                    accent = AccentGold,
                    title = "Global leaderboard",
                    description = "Post your verified Classic high score."
                )
                OnboardingBenefit(
                    icon = Icons.Filled.Shield,
                    accent = AccentEmerald,
                    title = "Play your way",
                    description = "Skipping is fine — every mode stays available offline."
                )
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
                    .padding(top = 8.dp, bottom = 6.dp)
            ) {
                GoogleSignInCard(
                    title = "Save your progress",
                    subtitle = "Sign in with Google for cloud save and the leaderboard.",
                    onAuthSuccess = onContinue
                )

                androidx.compose.material3.TextButton(
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("onboarding_skip_button")
                ) {
                    Text(
                        text = "Skip for now",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun OnboardingBenefit(
    icon: ImageVector,
    accent: androidx.compose.ui.graphics.Color,
    title: String,
    description: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SurfaceCard.copy(alpha = 0.8f))
            .border(1.dp, GlassBorder.copy(alpha = 0.38f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(accent.copy(alpha = 0.14f))
                .border(1.dp, accent.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = accent, modifier = Modifier.size(19.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp),
                color = TextPrimary,
                maxLines = 1
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                maxLines = 2
            )
        }
    }
}
