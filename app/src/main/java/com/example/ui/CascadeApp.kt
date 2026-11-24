package com.example.ui

import android.app.Activity
import android.content.Context
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.credentials.CredentialManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.example.ads.AdConfig
import com.example.ads.AdState
import com.example.data.cloud.CloudSyncRepository
import com.example.data.cloud.LeaderboardEntry
import com.example.data.cloud.attemptAutoSignIn
import com.example.data.cloud.authStateFlow
import com.example.security.PlayIntegrityManager
import com.example.ui.components.AuthenticatedCloudAccountCard
import com.example.ui.components.GoogleSignInCard
import com.example.ui.navigation.ChallengeSelectionRoute
import com.example.ui.navigation.GameOverRoute
import com.example.ui.navigation.GameplayRoute
import com.example.ui.navigation.HomeRoute
import com.example.ui.navigation.LeaderboardRoute
import com.example.ui.navigation.LevelCompleteRoute
import com.example.ui.navigation.ModeSelectionRoute
import com.example.ui.navigation.OnboardingRoute
import com.example.ui.navigation.PauseRoute
import com.example.ui.navigation.RulesRoute
import com.example.ui.navigation.SettingsRoute
import com.example.ui.navigation.LuckyWheelRoute
import com.example.ui.navigation.ShopRoute
import com.example.ui.navigation.SplashRoute
import com.example.ui.navigation.StatisticsRoute
import com.example.ui.screens.ChallengeSelectionScreen
import com.example.ui.screens.GameOverScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LeaderboardScreen
import com.example.ui.screens.LuckyWheelScreen
import com.example.ui.screens.LevelCompleteScreen
import com.example.ui.screens.ModeSelectionScreen
import com.example.ui.screens.OnboardingScreen
import com.example.ui.screens.PauseScreen
import com.example.ui.screens.RulesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.ShopScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatisticsScreen
import com.example.ui.theme.LocalBlockStyle
import com.example.ui.theme.LocalBoardPalette
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.emptyFlow

/**
 * Single first-launch onboarding: the [OnboardingRoute] NavHost destination, persisted via
 * [com.example.data.OnboardingPrefs] (the same flag earlier builds used, so existing installs
 * are not shown onboarding again). Google sign-in and guest (Skip) both complete it.
 */

@Composable
fun CascadeApp(
    viewModel: GameViewModel = viewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val auth: FirebaseAuth? = remember {
        try {
            Firebase.auth
        } catch (_: Throwable) {
            null
        }
    }
    val currentUser by remember(auth) {
        auth?.authStateFlow() ?: emptyFlow()
    }.collectAsStateWithLifecycle(initialValue = auth?.currentUser)

    LaunchedEffect(auth) {
        if (auth != null && auth.currentUser == null) {
            val credentialManager = runCatching { CredentialManager.create(context) }.getOrNull()
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    val signedInUser = currentUser
    val cloudViewModel: CloudSyncViewModel? = if (signedInUser != null) {
        key(signedInUser.uid) {
            rememberAuthenticatedCloudViewModel(
                currentUser = signedInUser,
                gameViewModel = viewModel
            )
        }
    } else {
        null
    }

    CascadeNavHost(
        viewModel = viewModel,
        currentUser = signedInUser,
        cloudViewModel = cloudViewModel
    )
}

@Composable
private fun rememberAuthenticatedCloudViewModel(
    currentUser: FirebaseUser,
    gameViewModel: GameViewModel
): CloudSyncViewModel? {
    val uid = currentUser.uid
    val displayName = currentUser.displayName?.takeIf { it.isNotBlank() } ?: "Player"
    val context = LocalContext.current
    val db = remember(uid) {
        runCatching {
            val databaseId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(databaseId)
        }.getOrNull()
    } ?: return null

    val cloudViewModel: CloudSyncViewModel = viewModel(
        key = "cloud_$uid",
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                CloudSyncViewModel(
                    repository = CloudSyncRepository(db),
                    currentUserId = uid,
                    defaultDisplayName = displayName,
                    playIntegrityManager = PlayIntegrityManager(app)
                )
            }
        }
    )

    if (cloudViewModel != null) {
        val gameState by gameViewModel.uiState.collectAsStateWithLifecycle()
        val cloudProfile by cloudViewModel.profileState.collectAsStateWithLifecycle()

        // First cloud snapshot decides ownership of local progress:
        //  - account already has data -> its own data replaces the guest data (guest score is not pushed up)
        //  - brand new account -> guest progress is uploaded to it
        var initialRestoreDone by remember(uid) { mutableStateOf(false) }

        LaunchedEffect(cloudProfile) {
            val success = cloudProfile as? CloudStreamState.Success ?: return@LaunchedEffect
            val profileData = success.data
            if (profileData != null) {
                gameViewModel.applyCloudMerge(profileData, replaceLocal = !initialRestoreDone)
            }
            initialRestoreDone = true
        }

        LaunchedEffect(
            initialRestoreDone,
            uid,
            gameState.classicBestScore,
            gameState.statistics.totalGamesPlayed,
            gameState.playerDisplayName,
            gameState.profileAvatarId
        ) {
            if (initialRestoreDone) {
                cloudViewModel.syncLocalStateToCloud(
                    uiState = gameState,
                    photoUrl = currentUser.photoUrl?.toString()
                )
            }
        }
    }

    return cloudViewModel
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CascadeNavHost(
    viewModel: GameViewModel,
    currentUser: FirebaseUser?,
    cloudViewModel: CloudSyncViewModel?
) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val adState by viewModel.adState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val onboardingPrefs = remember(context) { com.example.data.OnboardingPrefs(context) }
    val startDestination: Any = remember {
        if (onboardingPrefs.signInOnboardingSeen || currentUser != null) SplashRoute else OnboardingRoute
    }

    val safePopBackStack: () -> Unit = {
        val currentEntry = navController.currentBackStackEntry
        val canPop = currentEntry?.lifecycle?.currentState?.isAtLeast(Lifecycle.State.STARTED) == true &&
            navController.previousBackStackEntry != null
        if (canPop) {
            navController.popBackStack()
        }
    }

    val safePopToHome: () -> Unit = {
        runCatching {
            navController.popBackStack<HomeRoute>(inclusive = false)
        }
    }

    var onboardingCompleted by remember { mutableStateOf(false) }
    val completeOnboarding: () -> Unit = {
        if (!onboardingCompleted) {
            onboardingCompleted = true
            onboardingPrefs.signInOnboardingSeen = true
            runCatching {
                navController.navigate(HomeRoute) {
                    popUpTo<OnboardingRoute> { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    var splashCompleted by remember { mutableStateOf(false) }
    val completeSplash: () -> Unit = {
        if (!splashCompleted) {
            splashCompleted = true
            runCatching {
                navController.navigate(HomeRoute) {
                    popUpTo<SplashRoute> { inclusive = true }
                    launchSingleTop = true
                }
            }
        }
    }

    val adsSdkReady by AdConfig.sdkInitialized.collectAsStateWithLifecycle()
    LaunchedEffect(adsSdkReady) {
        if (adsSdkReady) {
            viewModel.onAdsSdkReady()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> viewModel.setAppForeground(true)
                Lifecycle.Event.ON_PAUSE -> viewModel.setAppForeground(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    CompositionLocalProvider(
        LocalBlockStyle provides state.settings.blockStyle,
        LocalBoardPalette provides state.settings.boardPalette
    ) {
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.semantics { testTagsAsResourceId = true },
            enterTransition = { fadeIn(animationSpec = tween(180)) },
            exitTransition = { fadeOut(animationSpec = tween(180)) },
            popEnterTransition = { fadeIn(animationSpec = tween(180)) },
            popExitTransition = { fadeOut(animationSpec = tween(180)) }
        ) {
            composable<OnboardingRoute> {
                OnboardingScreen(onContinue = completeOnboarding)
            }

            composable<SplashRoute> {
                SplashScreen(onFinished = completeSplash)
            }

            composable<HomeRoute> {
                val effectiveName = state.playerDisplayName?.takeIf { it.isNotBlank() }
                    ?: currentUser?.displayName?.takeIf { it.isNotBlank() }
                val homeSpinPoints by viewModel.wheelSpinPoints.collectAsStateWithLifecycle()
                LaunchedEffect(Unit) { viewModel.refreshServerWallet() }
                HomeScreen(
                    state = state,
                    spinPoints = homeSpinPoints,
                    onOpenWheel = {
                        viewModel.playUiClick()
                        navController.navigate(LuckyWheelRoute) { launchSingleTop = true }
                    },
                    isCloudSignedIn = currentUser != null,
                    playerName = effectiveName,
                    googlePhotoUrl = currentUser?.photoUrl?.toString(),
                    onPlayNewClassic = {
                        viewModel.playUiClick()
                        viewModel.startNewClassicGame()
                        navController.navigate(GameplayRoute) { launchSingleTop = true }
                    },
                    onContinueClassic = {
                        viewModel.playUiClick()
                        if (viewModel.continueSavedClassicGame()) {
                            navController.navigate(GameplayRoute) { launchSingleTop = true }
                        }
                    },
                    onPlayDaily = {
                        viewModel.playUiClick()
                        viewModel.startDailyChallenge()
                        navController.navigate(GameplayRoute) { launchSingleTop = true }
                    },
                    onOpenModes = {
                        viewModel.playUiClick()
                        navController.navigate(ModeSelectionRoute) { launchSingleTop = true }
                    },
                    onOpenChallenges = {
                        viewModel.playUiClick()
                        navController.navigate(ChallengeSelectionRoute) { launchSingleTop = true }
                    },
                    onOpenShop = {
                        viewModel.playUiClick()
                        navController.navigate(ShopRoute) { launchSingleTop = true }
                    },
                    onOpenLeaderboard = {
                        viewModel.playUiClick()
                        navController.navigate(LeaderboardRoute) { launchSingleTop = true }
                    },
                    onOpenStatistics = {
                        viewModel.playUiClick()
                        navController.navigate(StatisticsRoute) { launchSingleTop = true }
                    },
                    onOpenSettings = {
                        viewModel.playUiClick()
                        navController.navigate(SettingsRoute) { launchSingleTop = true }
                    }
                )
            }

            composable<ModeSelectionRoute> {
                ModeSelectionScreen(
                    onBack = safePopBackStack,
                    onPlayNewClassic = {
                        viewModel.playUiClick()
                        viewModel.startNewClassicGame()
                        navController.navigate(GameplayRoute) {
                            popUpTo<HomeRoute>()
                            launchSingleTop = true
                        }
                    },
                    onSelectChallengeLevels = {
                        viewModel.playUiClick()
                        navController.navigate(ChallengeSelectionRoute) { launchSingleTop = true }
                    },
                    onPlayDaily = {
                        viewModel.playUiClick()
                        viewModel.startDailyChallenge()
                        navController.navigate(GameplayRoute) {
                            popUpTo<HomeRoute>()
                            launchSingleTop = true
                        }
                    },
                    state = state
                )
            }

            composable<GameplayRoute> {
                CascadeGameScreen(
                    viewModel = viewModel,
                    onOpenPause = {
                        viewModel.playUiClick()
                        navController.navigate(PauseRoute) {
                            launchSingleTop = true
                        }
                    },
                    onLevelComplete = {
                        navController.navigate(LevelCompleteRoute) {
                            popUpTo<GameplayRoute> { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onGameOver = {
                        navController.navigate(GameOverRoute) {
                            popUpTo<GameplayRoute> { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                    onExitToHome = safePopToHome
                )
            }

            composable<PauseRoute> {
                PauseScreen(
                    state = state,
                    onResume = {
                        viewModel.playUiClick()
                        viewModel.resumeGame()
                        safePopBackStack()
                    },
                    onRestartConfirmed = {
                        viewModel.playUiClick()
                        viewModel.restartCurrentGame()
                        safePopBackStack()
                    },
                    onExitToHomeConfirmed = {
                        viewModel.playUiClick()
                        viewModel.exitToHome()
                        safePopToHome()
                    }
                )
            }

            composable<ChallengeSelectionRoute> {
                ChallengeSelectionScreen(
                    state = state,
                    onBack = safePopBackStack,
                    onPlayLevel = { levelNumber ->
                        viewModel.playUiClick()
                        viewModel.startChallengeLevel(levelNumber)
                        navController.navigate(GameplayRoute) {
                            popUpTo<HomeRoute>()
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable<LevelCompleteRoute> {
                LevelCompleteScreen(
                    state = state,
                    onNextLevel = {
                        viewModel.playUiClick()
                        viewModel.startChallengeLevel(state.currentChallengeLevel.levelNumber + 1)
                        navController.navigate(GameplayRoute) {
                            popUpTo<HomeRoute>()
                            launchSingleTop = true
                        }
                    },
                    onReplay = {
                        viewModel.playUiClick()
                        // restartCurrentGame keeps Daily Gravity in daily mode; a raw
                        // startChallengeLevel(0) would fall back to campaign Level 1.
                        viewModel.restartCurrentGame()
                        navController.navigate(GameplayRoute) {
                            popUpTo<HomeRoute>()
                            launchSingleTop = true
                        }
                    },
                    onHome = {
                        viewModel.playUiClick()
                        safePopToHome()
                    }
                )
            }

            composable<GameOverRoute> {
                GameOverScreen(
                    state = state,
                    onRetry = {
                        viewModel.playUiClick()
                        viewModel.restartCurrentGame()
                        navController.navigate(GameplayRoute) {
                            popUpTo<HomeRoute>()
                            launchSingleTop = true
                        }
                    },
                    onHome = {
                        viewModel.playUiClick()
                        safePopToHome()
                    }
                )
            }

            composable<StatisticsRoute> {
                val effectiveName = state.playerDisplayName?.takeIf { it.isNotBlank() }
                    ?: currentUser?.displayName?.takeIf { it.isNotBlank() }
                StatisticsScreen(
                    state = state,
                    onBack = safePopBackStack,
                    onResetProgressConfirmed = {
                        viewModel.playUiClick()
                        viewModel.resetAllProgress()
                    },
                    playerName = effectiveName,
                    googlePhotoUrl = currentUser?.photoUrl?.toString(),
                    onSelectAvatar = viewModel::selectProfileAvatar,
                    onRenameWithDiamonds = viewModel::renamePlayerWithDiamonds,
                    cloudAccountContent = {
                        if (currentUser == null || cloudViewModel == null) {
                            GoogleSignInCard()
                        } else {
                            val sessionStatus by cloudViewModel.sessionStatus.collectAsStateWithLifecycle()
                            AuthenticatedCloudAccountCard(
                                displayName = currentUser.displayName ?: "Player",
                                email = currentUser.email,
                                sessionStatus = sessionStatus,
                                onSyncNow = {
                                    cloudViewModel.syncLocalStateToCloud(
                                        uiState = state,
                                        photoUrl = currentUser.photoUrl?.toString()
                                    )
                                },
                                onVerifyIntegrity = { cloudViewModel.verifyAppIntegrity() },
                                onSignedOut = viewModel::onCloudSignedOut
                            )
                        }
                    }
                )
            }

            composable<SettingsRoute> {
                SettingsScreen(
                    state = state,
                    onBack = safePopBackStack,
                    onToggleSound = viewModel::toggleSound,
                    onToggleMusic = viewModel::toggleMusic,
                    onToggleHaptics = viewModel::toggleHaptics,
                    onToggleVisualEffects = viewModel::toggleVisualEffects,
                    onTogglePlacementPreview = viewModel::togglePlacementPreview,
                    onResetProgressConfirmed = {
                        viewModel.playUiClick()
                        viewModel.resetAllProgress()
                    }
                )
            }

            composable<ShopRoute> {
                val diamondAdState by viewModel.diamondAdState.collectAsStateWithLifecycle()
                val economyMessage by viewModel.economyMessage.collectAsStateWithLifecycle()
                ShopScreen(
                    state = state,
                    onBack = safePopBackStack,
                    diamondAdReady = diamondAdState is AdState.Ready && activity != null,
                    goldAdReady = adState is AdState.Ready && activity != null,
                    message = economyMessage,
                    onWatchDiamondAd = { activity?.let { viewModel.watchDiamondAd(it) } },
                    onWatchGoldAd = { activity?.let { viewModel.watchRewardedAd(it) } },
                    onEnsureAdsLoaded = viewModel::ensureAdsLoaded
                )
            }

            composable<LuckyWheelRoute> {
                val wheelAdState by viewModel.wheelAdState.collectAsStateWithLifecycle()
                val spinPoints by viewModel.wheelSpinPoints.collectAsStateWithLifecycle()
                val spinning by viewModel.isSpinning.collectAsStateWithLifecycle()
                val economyMessage by viewModel.economyMessage.collectAsStateWithLifecycle()
                LuckyWheelScreen(
                    isSignedIn = currentUser != null,
                    spinPoints = spinPoints,
                    isSpinning = spinning,
                    wheelAdState = wheelAdState,
                    message = economyMessage,
                    lastPrize = { viewModel.lastSpinLabel },
                    onBack = safePopBackStack,
                    onSpin = { cb -> viewModel.spinLuckyWheel(cb) },
                    onFinishSpin = viewModel::finishWheelSpin,
                    onWatchAd = { activity?.let { viewModel.watchWheelAd(it) } },
                    onRefresh = {
                        viewModel.refreshServerWallet()
                        viewModel.ensureAdsLoaded()
                    }
                )
            }

            composable<LeaderboardRoute> {
                if (currentUser == null || cloudViewModel == null) {
                    LeaderboardScreen(
                        isSignedIn = false,
                        currentUserId = null,
                        currentUserAvatarId = state.profileAvatarId,
                        currentUserPhotoUrl = null,
                        leaderboardState = CloudStreamState.Success(emptyList<LeaderboardEntry>()),
                        onBack = safePopBackStack
                    )
                } else {
                    val leaderboardState by cloudViewModel.leaderboardState.collectAsStateWithLifecycle()
                    LeaderboardScreen(
                        isSignedIn = true,
                        currentUserId = currentUser.uid,
                        currentUserAvatarId = state.profileAvatarId,
                        currentUserPhotoUrl = currentUser.photoUrl?.toString(),
                        leaderboardState = leaderboardState,
                        onBack = safePopBackStack
                    )
                }
            }

            composable<RulesRoute> {
                RulesScreen(
                    onBack = safePopBackStack
                )
            }
        }
    }
}
