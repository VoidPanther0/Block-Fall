package com.example

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import android.util.Log
import com.example.ads.AdConfig
import com.example.ui.CascadeApp
import com.example.ui.theme.CascadeTheme
import com.google.android.gms.ads.MobileAds
import com.google.firebase.Firebase
import com.google.firebase.appcheck.AppCheckProviderFactory
import com.google.firebase.appcheck.appCheck
import com.google.firebase.appcheck.playintegrity.PlayIntegrityAppCheckProviderFactory
import com.google.firebase.initialize

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeMobileAds()
        configureFirebaseAppCheck()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT)
        )
        setContent {
            CascadeTheme {
                CascadeApp()
            }
        }
    }

    private fun initializeMobileAds() {
        if (!AdConfig.isAppIdValid) {
            Log.w("MainActivity", "AdMob skipped: ADMOB_APP_ID is missing or not a complete ID")
            return
        }
        runCatching {
            MobileAds.initialize(this) {
                AdConfig.markSdkInitialized()
            }
        }.onFailure { Log.w("MainActivity", "AdMob initialization failed: ${it.message}") }
    }

    private fun configureFirebaseAppCheck() {
        try {
            Firebase.initialize(this)
            val factory: AppCheckProviderFactory = if (BuildConfig.DEBUG) {
                runCatching {
                    val clazz = Class.forName(
                        "com.google.firebase.appcheck.debug.DebugAppCheckProviderFactory"
                    )
                    clazz.getMethod("getInstance").invoke(null) as AppCheckProviderFactory
                }.getOrElse {
                    PlayIntegrityAppCheckProviderFactory.getInstance()
                }
            } else {
                PlayIntegrityAppCheckProviderFactory.getInstance()
            }
            Firebase.appCheck.installAppCheckProviderFactory(factory)
        } catch (t: Throwable) {
            Log.w("MainActivity", "AppCheck initialization skipped: ${t.message}")
        }
    }
}
