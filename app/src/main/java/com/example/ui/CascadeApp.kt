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
import com.example.ui.navigation.HowToPlayRoute
import com.example.ui.navigation.LeaderboardRoute
import com.example.ui.navigation.LevelCompleteRoute
import com.example.ui.navigation.ModeSelectionRoute
import com.example.ui.navigation.OnboardingRoute
import com.example.ui.navigation.PauseRoute
import com.example.ui.navigation.RulesRoute
import com.example.ui.navigation.SettingsRoute
import com.example.ui.navigation.ShopRoute
import com.example.ui.navigation.SplashRoute
import com.example.ui.navigation.StatisticsRoute
import com.example.ui.screens.ChallengeSelectionScreen
import com.example.ui.screens.GameOverScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HowToPlayScreen
import com.example.ui.screens.LeaderboardScreen
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
            attemptAutoSignIn(
                context = context,
                credentialManager = CredentialManager.create(context),
                onAuthSuccess = {},
                onUnauthenticated = {},
                scope = scope
            )
        }
    }

    val signedInUser = currentUser
    if (signedInUser == null) {
        CascadeNavHost(
            viewModel = viewModel,
            currentUser = null,
            cloudViewModel = null
        )
    } else {
        key(signedInUser.uid) {
            AuthenticatedCloudSession(
                currentUser = signedInUser,
                gameViewModel = viewModel
            )
        }
    }
}

@Composable
private fun AuthenticatedCloudSession(
    currentUser: FirebaseUser,
    gameViewModel: GameViewModel
) {
    val uid = currentUser.uid
    val displayName = currentUser.displayName?.takeIf { it.isNotBlank() } ?: "Player"
    val cloudViewModel: CloudSyncViewModel = viewModel(
        key = uid,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                CloudSyncViewModel(
                    repository = CloudSyncRepository(db),
                    currentUserId = uid,
                    defaultDisplayName = displayName,
                    playIntegrityManager = PlayIntegrityManager(app)
                )
            }
        }
    )

    val gameState by gameViewModel.uiState.collectAsStateWithLifecycle()
    val cloudProfile by cloudViewModel.profileState.collectAsStateWithLifecycle()
    val cloudChallenges by cloudViewModel.challengeDocsState.collectAsStateWithLifecycle()

    LaunchedEffect(cloudProfile, cloudChallenges) {
        val profileData = (cloudProfile as? CloudStreamState.Success)?.data
        val challengesData = (cloudChallenges as? CloudStreamState.Success)?.data.orEmpty()
        if (profileData != null || challengesData.isNotEmpty()) {
            gameViewModel.applyCloudMerge(profileData, challengesData)
        }
    }

    LaunchedEffect(
        uid,
        gameState.classicBestScore,
        gameState.unlockedChallengeLevel,
        gameState.statistics.totalGamesPlayed
    ) {
        cloudViewModel.syncLocalStateToCloud(gameState)
    }

    CascadeNavHost(
        viewModel = gameViewModel,
        currentUser = currentUser,
        cloudViewModel = cloudViewModel
    )
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
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current
    val onboardingPrefs = remember(context) { com.example.data.OnboardingPrefs(context) }
    val onboardingComplete = remember(currentUser != null) {
        onboardingPrefs.signInOnboardingSeen || currentUser != null
    }
    val startDestination: Any = if (onboardingComplete) SplashRoute else OnboardingRoute

    val completeOnboarding: () -> Unit = {
        onboardingPrefs.signInOnboardingSeen = true
        navController.navigate(HomeRoute) {
            popUpTo<OnboardingRoute> { inclusive = true }
            launchSingleTop = true
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
                SplashScreen(
                    onFinished = {
                        navController.navigate(HomeRoute) {
                            popUpTo<SplashRoute> { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }

            composable<HomeRoute> {
                HomeScreen(
                    state = state,
                    isCloudSignedIn = currentUser != null,
                    playerName = currentUser?.displayName,
                    onPlayNewClassic = {
                        viewModel.playUiClick()
                        viewModel.startNewClassicGame()
                        navController.navigate(GameplayRoute)
                    },
                    onContinueClassic = {
                        viewModel.playUiClick()
                        if (viewModel.continueSavedClassicGame()) {
                            navController.navigate(GameplayRoute)
                        }
                    },
                    onOpenModes = {
                        viewModel.playUiClick()
                        navController.navigate(ModeSelectionRoute)
                    },
                    onOpenChallenges = {
                        viewModel.playUiClick()
                        navController.navigate(ChallengeSelectionRoute)
                    },
                    onOpenShop = {
                        viewModel.playUiClick()
                        navController.navigate(ShopRoute)
                    },
                    onOpenLeaderboard = {
                        viewModel.playUiClick()
                        navController.navigate(LeaderboardRoute)
                    },
                    onOpenSettings = {
                        viewModel.playUiClick()
                        navController.navigate(SettingsRoute)
                    },
                    onOpenStatistics = {
                        viewModel.playUiClick()
                        navController.navigate(StatisticsRoute)
                    }
                )
            }

            composable<ModeSelectionRoute> {
                ModeSelectionScreen(
                    onBack = { navController.popBackStack() },
                    onPlayNewClassic = {
                        viewModel.playUiClick()
                        viewModel.startNewClassicGame()
                        navController.navigate(GameplayRoute) {
                            popUpTo<HomeRoute>()
                        }
                    },
                    onSelectChallengeLevels = {
                        viewModel.playUiClick()
                        navController.navigate(ChallengeSelectionRoute)
                    },
                    onPlayDaily = {
                        viewModel.playUiClick()
                        viewModel.startDailyChallenge()
                        navController.navigate(GameplayRoute) {
                            popUpTo<HomeRoute>()
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
                    onOpenHowToPlay = {
                        viewModel.playUiClick()
                        navController.navigate(HowToPlayRoute)
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
                    onExitToHome = {
                        navController.popBackStack<HomeRoute>(inclusive = false)
                    }
                )
            }

            composable<PauseRoute> {
                PauseScreen(
                    state = state,
                    onResume = {
                        viewModel.playUiClick()
                        viewModel.resumeGame()
                        navController.popBackStack()
                    },
                    onRestartConfirmed = {
                        viewModel.playUiClick()
                        viewModel.restartCurrentGame()
                        navController.popBackStack()
                    },
                    onExitToHomeConfirmed = {
                        viewModel.playUiClick()
                        viewModel.exitToHome()
                        navController.popBackStack<HomeRoute>(inclusive = false)
                    },
                    onOpenSettings = {
                        viewModel.playUiClick()
                        navController.navigate(SettingsRoute)
                    }
                )
            }

            composable<HowToPlayRoute> {
                HowToPlayScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable<ChallengeSelectionRoute> {
                ChallengeSelectionScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onPlayLevel = { levelNumber ->
                        viewModel.playUiClick()
                        viewModel.startChallengeLevel(levelNumber)
                        navController.navigate(GameplayRoute) {
                            popUpTo<HomeRoute>()
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
                        }
                    },
                    onReplay = {
                        viewModel.playUiClick()
                        // restartCurrentGame keeps Daily Gravity in daily mode; a raw
                        // startChallengeLevel(0) would fall back to campaign Level 1.
                        viewModel.restartCurrentGame()
                        navController.navigate(GameplayRoute) {
                            popUpTo<HomeRoute>()
                        }
                    },
                    onHome = {
                        viewModel.playUiClick()
                        navController.popBackStack<HomeRoute>(inclusive = false)
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
                        }
                    },
                    onHome = {
                        viewModel.playUiClick()
                        navController.popBackStack<HomeRoute>(inclusive = false)
                    }
                )
            }

            composable<StatisticsRoute> {
                StatisticsScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onResetProgressConfirmed = {
                        viewModel.playUiClick()
                        viewModel.resetAllProgress()
                    },
                    playerName = currentUser?.displayName
                )
            }

            composable<SettingsRoute> {
                SettingsScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onToggleSound = viewModel::toggleSound,
                    onToggleMusic = viewModel::toggleMusic,
                    onToggleHaptics = viewModel::toggleHaptics,
                    onToggleVisualEffects = viewModel::toggleVisualEffects,
                    onTogglePlacementPreview = viewModel::togglePlacementPreview,
                    onSelectBlockStyle = viewModel::setBlockStyle,
                    onSelectBoardPalette = viewModel::setBoardPalette,
                    onResetProgressConfirmed = {
                        viewModel.playUiClick()
                        viewModel.resetAllProgress()
                    },
                    cloudAccountContent = {
                        if (currentUser == null || cloudViewModel == null) {
                            GoogleSignInCard()
                        } else {
                            val sessionStatus by cloudViewModel.sessionStatus.collectAsStateWithLifecycle()
                            AuthenticatedCloudAccountCard(
                                displayName = currentUser.displayName ?: "Player",
                                email = currentUser.email,
                                sessionStatus = sessionStatus,
                                onSyncNow = { cloudViewModel.syncLocalStateToCloud(state) },
                                onVerifyIntegrity = { cloudViewModel.verifyAppIntegrity() }
                            )
                        }
                    }
                )
            }

            composable<ShopRoute> {
                ShopScreen(
                    state = state,
                    onBack = { navController.popBackStack() },
                    onPurchase = viewModel::purchaseShopItem,
                    onWatchAd = if (adState is AdState.Ready && activity != null) {
                        { viewModel.watchRewardedAd(activity) }
                    } else {
                        null
                    },
                    onSelectBlockStyle = viewModel::setBlockStyle,
                    onSelectBoardPalette = viewModel::setBoardPalette
                )
            }

            composable<LeaderboardRoute> {
                if (currentUser == null || cloudViewModel == null) {
                    LeaderboardScreen(
                        isSignedIn = false,
                        currentUserId = null,
                        leaderboardState = CloudStreamState.Success(emptyList<LeaderboardEntry>()),
                        onBack = { navController.popBackStack() }
                    )
                } else {
                    val leaderboardState by cloudViewModel.leaderboardState.collectAsStateWithLifecycle()
                    LeaderboardScreen(
                        isSignedIn = true,
                        currentUserId = currentUser.uid,
                        leaderboardState = leaderboardState,
                        onBack = { navController.popBackStack() }
                    )
                }
            }

            composable<RulesRoute> {
                RulesScreen(
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
