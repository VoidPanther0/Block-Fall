package com.example.ui

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.navigation.CascadeRoutes
import com.example.ui.theme.LocalBlockStyle
import com.example.ui.theme.LocalBoardPalette
import com.example.ui.screens.ChallengeSelectionScreen
import com.example.ui.screens.GameOverScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.HowToPlayScreen
import com.example.ui.screens.LevelCompleteScreen
import com.example.ui.screens.ModeSelectionScreen
import com.example.ui.screens.PauseScreen
import com.example.ui.screens.RulesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.StatisticsScreen

@Composable
fun CascadeApp(
    viewModel: GameViewModel = viewModel()
) {
    val navController = rememberNavController()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current

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

    @OptIn(ExperimentalComposeUiApi::class)
    CompositionLocalProvider(
        LocalBlockStyle provides state.settings.blockStyle,
        LocalBoardPalette provides state.settings.boardPalette
    ) {
        NavHost(
            navController = navController,
            startDestination = CascadeRoutes.SPLASH,
            modifier = Modifier.semantics { testTagsAsResourceId = true },
            enterTransition = { fadeIn(animationSpec = tween(180)) },
            exitTransition = { fadeOut(animationSpec = tween(180)) },
            popEnterTransition = { fadeIn(animationSpec = tween(180)) },
            popExitTransition = { fadeOut(animationSpec = tween(180)) }
        ) {
        composable(CascadeRoutes.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(CascadeRoutes.HOME) {
                        popUpTo(CascadeRoutes.SPLASH) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(CascadeRoutes.HOME) {
            HomeScreen(
                state = state,
                onPlayNewClassic = {
                    viewModel.playUiClick()
                    viewModel.startNewClassicGame()
                    navController.navigate(CascadeRoutes.GAMEPLAY)
                },
                onContinueClassic = {
                    viewModel.playUiClick()
                    if (viewModel.continueSavedClassicGame()) {
                        navController.navigate(CascadeRoutes.GAMEPLAY)
                    }
                },
                onOpenModes = {
                    viewModel.playUiClick()
                    navController.navigate(CascadeRoutes.MODE_SELECTION)
                },
                onOpenChallenges = {
                    viewModel.playUiClick()
                    navController.navigate(CascadeRoutes.CHALLENGE_SELECTION)
                },
                onOpenSettings = {
                    viewModel.playUiClick()
                    navController.navigate(CascadeRoutes.SETTINGS)
                },
                onOpenStatistics = {
                    viewModel.playUiClick()
                    navController.navigate(CascadeRoutes.STATISTICS)
                }
            )
        }

        composable(CascadeRoutes.MODE_SELECTION) {
            ModeSelectionScreen(
                onBack = { navController.popBackStack() },
                onPlayNewClassic = {
                    viewModel.playUiClick()
                    viewModel.startNewClassicGame()
                    navController.navigate(CascadeRoutes.GAMEPLAY) {
                        popUpTo(CascadeRoutes.HOME)
                    }
                },
                onSelectChallengeLevels = {
                    viewModel.playUiClick()
                    navController.navigate(CascadeRoutes.CHALLENGE_SELECTION)
                }
            )
        }

        composable(CascadeRoutes.GAMEPLAY) {
            CascadeGameScreen(
                viewModel = viewModel,
                onOpenPause = {
                    viewModel.playUiClick()
                    navController.navigate(CascadeRoutes.PAUSE) {
                        launchSingleTop = true
                    }
                },
                onOpenHowToPlay = {
                    viewModel.playUiClick()
                    navController.navigate(CascadeRoutes.HOW_TO_PLAY)
                },
                onLevelComplete = {
                    navController.navigate(CascadeRoutes.LEVEL_COMPLETE) {
                        popUpTo(CascadeRoutes.GAMEPLAY) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onGameOver = {
                    navController.navigate(CascadeRoutes.GAME_OVER) {
                        popUpTo(CascadeRoutes.GAMEPLAY) { inclusive = true }
                        launchSingleTop = true
                    }
                },
                onExitToHome = {
                    navController.popBackStack(CascadeRoutes.HOME, inclusive = false)
                }
            )
        }

        composable(CascadeRoutes.PAUSE) {
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
                    navController.popBackStack(CascadeRoutes.HOME, inclusive = false)
                }
            )
        }

        composable(CascadeRoutes.HOW_TO_PLAY) {
            HowToPlayScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(CascadeRoutes.CHALLENGE_SELECTION) {
            ChallengeSelectionScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onPlayLevel = { levelNumber ->
                    viewModel.playUiClick()
                    viewModel.startChallengeLevel(levelNumber)
                    navController.navigate(CascadeRoutes.GAMEPLAY) {
                        popUpTo(CascadeRoutes.HOME)
                    }
                }
            )
        }

        composable(CascadeRoutes.LEVEL_COMPLETE) {
            LevelCompleteScreen(
                state = state,
                onNextLevel = {
                    viewModel.playUiClick()
                    viewModel.startChallengeLevel(state.currentChallengeLevel.levelNumber + 1)
                    navController.navigate(CascadeRoutes.GAMEPLAY) {
                        popUpTo(CascadeRoutes.HOME)
                    }
                },
                onReplay = {
                    viewModel.playUiClick()
                    viewModel.startChallengeLevel(state.currentChallengeLevel.levelNumber)
                    navController.navigate(CascadeRoutes.GAMEPLAY) {
                        popUpTo(CascadeRoutes.HOME)
                    }
                },
                onHome = {
                    viewModel.playUiClick()
                    navController.popBackStack(CascadeRoutes.HOME, inclusive = false)
                }
            )
        }

        composable(CascadeRoutes.GAME_OVER) {
            GameOverScreen(
                state = state,
                onRetry = {
                    viewModel.playUiClick()
                    viewModel.restartCurrentGame()
                    navController.navigate(CascadeRoutes.GAMEPLAY) {
                        popUpTo(CascadeRoutes.HOME)
                    }
                },
                onHome = {
                    viewModel.playUiClick()
                    navController.popBackStack(CascadeRoutes.HOME, inclusive = false)
                }
            )
        }

        composable(CascadeRoutes.STATISTICS) {
            StatisticsScreen(
                state = state,
                onBack = { navController.popBackStack() },
                onResetProgressConfirmed = {
                    viewModel.playUiClick()
                    viewModel.resetAllProgress()
                }
            )
        }

        composable(CascadeRoutes.SETTINGS) {
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
                }
            )
        }

        composable(CascadeRoutes.RULES) {
            RulesScreen(
                onBack = { navController.popBackStack() }
            )
        }
        }
    }
}
