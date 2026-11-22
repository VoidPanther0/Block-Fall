package com.example.data

import android.content.Context

/** Remembers that the one-time Google sign-in onboarding was shown (signed in or skipped). */
class OnboardingPrefs(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("onboarding", Context.MODE_PRIVATE)

    var signInOnboardingSeen: Boolean
        get() = prefs.getBoolean(KEY_SEEN, false)
        set(value) { prefs.edit().putBoolean(KEY_SEEN, value).apply() }

    private companion object { const val KEY_SEEN = "sign_in_onboarding_seen" }
}
