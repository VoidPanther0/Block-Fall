import com.google.gms.googleservices.GoogleServicesPlugin.MissingGoogleServicesStrategy

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.kotlin.serialization)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.secrets)
  alias(libs.plugins.google.services)
}

val TEST_ADMOB_APP_ID = "ca-app-pub-3940256099942544~3347511713"
fun envOrDotEnv(name: String): String? {
  System.getenv(name)?.takeIf { it.isNotBlank() }?.let { return it.trim() }
  val envFile = rootProject.file(".env")
  if (!envFile.exists()) return null
  return envFile.readLines().firstOrNull { it.startsWith("$name=") }?.substringAfter("=")?.trim()
}
val admobAppIdPattern = Regex("^ca-app-pub-[0-9]{16}~[0-9]{10}$")
val admobUnitPattern = Regex("^ca-app-pub-[0-9]{16}/[0-9]{10}$")
val realAdmobAppId: String? = envOrDotEnv("ADMOB_APP_ID")
  ?.takeIf { admobAppIdPattern.matches(it) && it != TEST_ADMOB_APP_ID }
val realAdmobRewardedUnit: String? = envOrDotEnv("ADMOB_REWARDED_GOLD_UNIT_ID")
  ?.takeIf { admobUnitPattern.matches(it) && !it.startsWith("ca-app-pub-3940256099942544/") }
// Release builds fall back to the test App ID only so the project still configures; the
// verifyReleaseAdConfig task below blocks packaging unless real IDs are supplied.
val prodAdmobAppId: String = realAdmobAppId ?: TEST_ADMOB_APP_ID

android {
  namespace = "com.example"
  compileSdk { version = release(36) { minorApiLevel = 1 } }

  defaultConfig {
    applicationId = "com.aistudio.cascadegrid.vqmxrt"
    minSdk = 24
    targetSdk = 36
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

  }

  // Do not remove or modify these signingConfigs. They are necessary for building, installing,
  // and updating Android apps in AI Studio.
  signingConfigs {
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH") ?: "${rootDir}/my-upload-key.jks"
      storeFile = file(keystorePath)
      storePassword = System.getenv("STORE_PASSWORD")
      keyAlias = "upload"
      keyPassword = System.getenv("KEY_PASSWORD")
    }
    create("debugConfig") {
      storeFile = file("${rootDir}/debug.keystore")
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
    }
  }

  buildTypes {
    release {
      // Production AdMob App ID comes only from the ADMOB_APP_ID environment variable or the
      // gitignored .env file. If neither supplies a real ID, Google's test ID is used so the
      // build stays valid; no production ID is ever committed.
      manifestPlaceholders["ADMOB_APP_ID"] = prodAdmobAppId
      isCrunchPngs = false
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      // Do not remove or modify this signingConfig assignment. It is necessary for Android apps in
      // AI Studio.
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      // Debug builds always use Google's official test AdMob App ID.
      manifestPlaceholders["ADMOB_APP_ID"] = TEST_ADMOB_APP_ID
      // Do not remove or modify this signingConfig assignment. It is necessary for Android apps in
      // AI Studio.
      // Falls back to the AGP-generated default debug keystore when no repo-root debug.keystore
      // exists (fresh clones, Windows, CI), so debug builds never fail on a missing keystore.
      signingConfig = if (file("${rootDir}/debug.keystore").exists()) {
        signingConfigs.getByName("debugConfig")
      } else {
        signingConfigs.getByName("debug")
      }
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions {
    unitTests {
      isIncludeAndroidResources = true
      // Robolectric URL-encodes spaces in paths under the user's home (e.g. "C:\Users\This PC"),
      // then fails to open its own android-all jar. If a space-free dir holding the jar is
      // supplied (-ProbolectricDepsDir=... or ROBOLECTRIC_DEPS_DIR), run Robolectric offline from it.
      val depsDir = (project.findProperty("robolectricDepsDir") as String?) ?: System.getenv("ROBOLECTRIC_DEPS_DIR")
      if (!depsDir.isNullOrBlank()) {
        all {
          it.systemProperty("robolectric.dependency.dir", depsDir)
          it.systemProperty("robolectric.offline", "true")
        }
      }
    }
  }
  dependenciesInfo {
    includeInApk = false
    includeInBundle = true
  }
}

// Google AI Studio may not carry dotfiles into its workspace; the Secrets plugin fails the whole
// build if the defaults file is missing, so recreate it with the safe test-ID defaults.
val defaultEnvFile = rootProject.file(".env.example")
if (!defaultEnvFile.exists()) {
  defaultEnvFile.writeText(
    listOf(
      "ADMOB_APP_ID=ca-app-pub-3940256099942544~3347511713",
      "ADMOB_REWARDED_GOLD_UNIT_ID=ca-app-pub-3940256099942544/5224354917"
    ).joinToString(separator = System.lineSeparator(), postfix = System.lineSeparator())
  )
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
  ignoreList.add("FIREBASE_APPCHECK_DEBUG_TOKEN")
}

googleServices { missingGoogleServicesStrategy = MissingGoogleServicesStrategy.WARN }

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.profileinstaller)
  // implementation(libs.androidx.room.ktx)
  // implementation(libs.androidx.room.runtime)
  // implementation(libs.coil.compose)
  // implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.firebase.firestore)
  implementation(libs.firebase.auth)
  implementation(libs.androidx.credentials)
  implementation(libs.androidx.credentials.play.services)
  implementation(libs.googleid)
  implementation(libs.firebase.appcheck.playintegrity)
  debugImplementation(libs.firebase.appcheck.debug)
  implementation(libs.play.integrity)
  implementation(libs.play.services.ads)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.kotlinx.coroutines.play.services)
  implementation(libs.kotlinx.serialization.json)
  // implementation(libs.logging.interceptor)
  // implementation(libs.moshi.kotlin)
  // implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  // implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  // "ksp"(libs.androidx.room.compiler)
  // "ksp"(libs.moshi.kotlin.codegen)
}

// Release builds must not ship Google's test ad IDs. Supply real ADMOB_APP_ID and
// ADMOB_REWARDED_GOLD_UNIT_ID via environment or the gitignored .env. For non-shipping CI
// verification only, pass -PallowTestAds=true.
val verifyReleaseAdConfig = tasks.register("verifyReleaseAdConfig") {
  val ok = realAdmobAppId != null && realAdmobRewardedUnit != null
  val allowTest = project.findProperty("allowTestAds") == "true"
  doLast {
    if (!ok && !allowTest) {
      throw GradleException(
        "Release AdMob configuration incomplete: set real ADMOB_APP_ID and " +
          "ADMOB_REWARDED_GOLD_UNIT_ID (env or .env). Use -PallowTestAds=true only for non-shipping builds."
      )
    }
    if (!ok) logger.warn("WARNING: release build is using Google TEST AdMob IDs (allowTestAds=true).")
  }
}
tasks.matching { it.name == "preReleaseBuild" }.configureEach { dependsOn(verifyReleaseAdConfig) }
