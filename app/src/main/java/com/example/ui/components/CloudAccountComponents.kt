package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.data.cloud.onGoogleSignInClicked
import com.example.data.cloud.signOut
import com.example.security.IntegrityStatusCode
import com.example.ui.CloudSessionStatus
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.AccentGold
import com.example.ui.theme.AccentCoral
import com.example.ui.theme.BlueBtnTop
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.OutfitFontFamily
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun GoogleSignInCard(
    title: String = "Cloud Save & Leaderboard",
    subtitle: String = "Sign in with Google to back up your scores, stars, and rank across devices.",
    onAuthSuccess: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    FrostedGlassCard(
        cornerRadius = 20.dp,
        borderColor = AccentCyan.copy(alpha = 0.36f),
        modifier = modifier
            .fillMaxWidth()
            .testTag("google_sign_in_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentCyan.copy(alpha = 0.16f))
                        .border(1.dp, AccentCyan.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudQueue,
                        contentDescription = null,
                        tint = AccentCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = TextPrimary
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextSecondary
                    )
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = AccentCoral,
                    modifier = Modifier.testTag("google_sign_in_error")
                )
            }

            Button(
                onClick = {
                    errorMessage = null
                    isLoading = true
                    onGoogleSignInClicked(
                        context = context,
                        credentialManager = credentialManager,
                        onAuthSuccess = {
                            isLoading = false
                            onAuthSuccess()
                        },
                        onAuthError = { msg ->
                            isLoading = false
                            errorMessage = msg
                        },
                        scope = scope,
                        onAuthCancelled = {
                            isLoading = false
                        }
                    )
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlueBtnTop,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .testTag("google_sign_in_button")
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "Sign in with Google",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun AuthenticatedCloudAccountCard(
    displayName: String,
    email: String?,
    sessionStatus: CloudSessionStatus,
    onSyncNow: () -> Unit,
    onVerifyIntegrity: () -> Unit,
    onSignedOut: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val integrityColor = when (sessionStatus.integrityStatus) {
        IntegrityStatusCode.VERIFIED_SERVER -> AccentEmerald
        IntegrityStatusCode.PENDING_SERVER_VERIFICATION -> AccentGold
        IntegrityStatusCode.REJECTED_SERVER -> AccentCoral
        else -> AccentCyan
    }

    FrostedGlassCard(
        cornerRadius = 20.dp,
        borderColor = AccentEmerald.copy(alpha = 0.35f),
        modifier = modifier
            .fillMaxWidth()
            .testTag("cloud_account_card")
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(AccentEmerald.copy(alpha = 0.16f))
                        .border(1.dp, AccentEmerald.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Filled.CloudDone,
                        contentDescription = null,
                        tint = AccentEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = displayName,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = OutfitFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = email ?: "Signed in with Google",
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Integrity status row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(integrityColor.copy(alpha = 0.12f))
                    .border(1.dp, integrityColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Icon(
                    imageVector = if (sessionStatus.integrityStatus == IntegrityStatusCode.VERIFIED_SERVER) {
                        Icons.Filled.VerifiedUser
                    } else {
                        Icons.Filled.Shield
                    },
                    contentDescription = null,
                    tint = integrityColor,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = sessionStatus.integritySummary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = TextPrimary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("integrity_status_text")
                )
            }

            if (sessionStatus.lastSyncMessage != null) {
                Text(
                    text = sessionStatus.lastSyncMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = AccentEmerald
                )
            }
            if (sessionStatus.lastErrorMessage != null) {
                Text(
                    text = sessionStatus.lastErrorMessage,
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                    color = AccentCoral
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSyncNow,
                    enabled = !sessionStatus.isSyncing,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BlueBtnTop),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("cloud_sync_now_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = if (sessionStatus.isSyncing) "Syncing..." else "Sync Cloud",
                        maxLines = 1,
                        fontSize = 13.sp
                    )
                }

                OutlinedButton(
                    onClick = onVerifyIntegrity,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 48.dp)
                        .testTag("verify_integrity_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Shield,
                        contentDescription = null,
                        tint = AccentGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.size(6.dp))
                    Text(
                        text = "Integrity Check",
                        color = TextPrimary,
                        maxLines = 1,
                        fontSize = 13.sp
                    )
                }
            }

            OutlinedButton(
                onClick = {
                    signOut(
                        credentialManager = credentialManager,
                        scope = scope,
                        onSignOutComplete = onSignedOut
                    )
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("google_sign_out_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    tint = GlassBorder,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.size(6.dp))
                Text(
                    text = "Sign out",
                    color = TextPrimary,
                    fontSize = 13.sp
                )
            }
        }
    }
}
