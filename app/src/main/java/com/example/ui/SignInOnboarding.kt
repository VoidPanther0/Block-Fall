package com.example.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.data.cloud.onGoogleSignInClicked

/**
 * One-time onboarding: Sign in with Google or Skip (play offline as a guest).
 * [onFinished] is called for either outcome so the caller can persist "seen".
 */
@Composable
fun SignInOnboarding(onSignedIn: () -> Unit, onSkip: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var error by remember { mutableStateOf<String?>(null) }
    var busy by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Welcome to Block Fall", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))
        Text(
            "Sign in to sync challenge progress and appear on the leaderboard, or skip and play offline.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(24.dp))
        Button(
            enabled = !busy,
            onClick = {
                busy = true
                error = null
                onGoogleSignInClicked(
                    context = context,
                    credentialManager = CredentialManager.create(context),
                    onAuthSuccess = onSignedIn,
                    onAuthError = { busy = false; error = it },
                    scope = scope,
                    onAuthCancelled = { busy = false }
                )
            }
        ) { Text("Sign in with Google") }
        TextButton(onClick = onSkip) { Text("Skip") }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}
