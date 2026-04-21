package com.eatif.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eatif.app.ui.onboarding.OnboardingManager
import com.eatif.app.ui.onboarding.OnboardingScreen
import com.eatif.app.ui.onboarding.WhatsNewDialog
import com.eatif.app.ui.screens.HomeScreen
import com.eatif.app.ui.screens.SetupScreen
import com.eatif.app.ui.screens.GameSelectScreen
import com.eatif.app.ui.screens.PlayScreen
import com.eatif.app.ui.screens.FoodSelectScreen
import com.eatif.app.ui.screens.ResultScreen
import com.eatif.app.ui.screens.SettingsScreen
import com.eatif.app.ui.screens.FoodLibraryScreen
import com.eatif.app.ui.screens.HistoryScreen
import com.eatif.app.ui.screens.SplashScreen
import com.eatif.app.ui.screens.GameRuleScreen
import com.eatif.app.ui.screens.ProfileScreen
import com.eatif.app.ui.screens.AchievementScreen
import com.eatif.app.ui.screens.LevelSelectScreen
import com.eatif.app.ui.screens.StatsScreen
import com.eatif.app.ui.screens.SkinSelectorScreen
import com.eatif.app.domain.model.GameList
import com.eatif.app.ui.GameEndResultHolder
import com.eatif.app.ui.theme.ThemeManager

@Composable
fun EatIfNavHost(
    navController: NavHostController = rememberNavController(),
    onThemeChanged: (darkMode: Boolean, followSystem: Boolean) -> Unit = { _, _ -> },
    onSeedColorChanged: (String) -> Unit = {}
) {
    var isDarkMode by remember { mutableStateOf(ThemeManager.isDarkMode) }
    var followSystem by remember { mutableStateOf(ThemeManager.followSystem) }
    var showOnboarding by remember { mutableStateOf(!OnboardingManager.hasSeenOnboarding) }
    var showWhatsNew by remember { mutableStateOf(OnboardingManager.shouldShowWhatsNew()) }

    if (showOnboarding) {
        OnboardingScreen(
            onComplete = {
                OnboardingManager.setOnboardingComplete()
                showOnboarding = false
                if (OnboardingManager.shouldShowWhatsNew()) {
                    showWhatsNew = true
                }
            }
        )
        return
    }

    if (showWhatsNew) {
        WhatsNewDialog(
            onDismiss = {
                OnboardingManager.setWhatsNewShown()
                OnboardingManager.setShowWhatsNew(false)
                showWhatsNew = false
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onSinglePlayerClick = {
                    navController.navigate(Screen.Setup.createRoute("single"))
                },
                onTwoPlayerClick = {
                    navController.navigate(Screen.Setup.createRoute("double"))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                },
                onStatsClick = { navController.navigate(Screen.Stats.route) },
                onAchievementsClick = { navController.navigate(Screen.Achievements.route) },
                onProfileClick = { navController.navigate(Screen.Profile.route) }
            )
        }

        composable(
            route = Screen.Setup.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "single"
            SetupScreen(
                mode = mode,
                onConfirm = {
                    navController.navigate(Screen.GameSelect.createRoute(mode))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.GameSelect.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "single"
            GameSelectScreen(
                mode = mode,
                onGameSelected = { gameId ->
                    navController.navigate(Screen.Play.createRoute(gameId, mode))
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onGameRuleClick = { gameId ->
                    navController.navigate(Screen.GameRule.createRoute(gameId))
                },
                onLevelSelectClick = { gameId ->
                    navController.navigate(Screen.LevelSelect.createRoute(gameId))
                }
            )
        }

        composable(
            route = Screen.Play.route,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType; defaultValue = "single" }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            val mode = backStackEntry.arguments?.getString("mode") ?: "single"
            PlayScreen(
                gameId = gameId,
                mode = mode,
                onGameEnd = { foodName, scorePercent, result ->
                    navController.navigate(
                        "result/$foodName/$scorePercent/${result.xpEarned}/${result.playerLevel}"
                    ) {
                        popUpTo(Screen.Home.route)
                    }
                    GameEndResultHolder.unlockedAchievements = result.unlockedAchievements
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onSkinsClick = {
                    navController.navigate(Screen.SkinSelector.createRoute(gameId))
                }
            )
        }

        composable(
            route = Screen.GameRule.route,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            GameRuleScreen(
                gameId = gameId,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.FoodSelect.route) {
            FoodSelectScreen(
                onFoodSelected = { foodName ->
                    navController.navigate("result/$foodName/-1/0/1") {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = "result/{foodName}/{scorePercent}/{xpEarned}/{playerLevel}",
            arguments = listOf(
                navArgument("foodName") { type = NavType.StringType },
                navArgument("scorePercent") {
                    type = NavType.IntType
                    defaultValue = -1
                },
                navArgument("xpEarned") {
                    type = NavType.IntType
                    defaultValue = 0
                },
                navArgument("playerLevel") {
                    type = NavType.IntType
                    defaultValue = 1
                }
            )
        ) { backStackEntry ->
            val foodName = backStackEntry.arguments?.getString("foodName") ?: ""
            val scorePercent = backStackEntry.arguments?.getInt("scorePercent") ?: -1
            val xpEarned = backStackEntry.arguments?.getInt("xpEarned") ?: 0
            val playerLevel = backStackEntry.arguments?.getInt("playerLevel") ?: 1
            ResultScreen(
                foodName = foodName,
                scorePercent = scorePercent,
                xpEarned = xpEarned,
                playerLevel = playerLevel,
                onPlayAgain = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onFoodLibraryClick = {
                    navController.navigate(Screen.FoodLibrary.route)
                },
                onHistoryClick = {
                    navController.navigate(Screen.History.route)
                },
                isDarkMode = isDarkMode,
                followSystem = followSystem,
                onThemeChanged = { darkMode, followSystemTheme ->
                    isDarkMode = darkMode
                    followSystem = followSystemTheme
                    onThemeChanged(darkMode, followSystemTheme)
                },
                onSeedColorChanged = onSeedColorChanged
            )
        }

        composable(Screen.FoodLibrary.route) {
            FoodLibraryScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.History.route) {
            HistoryScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(onBackClick = { navController.popBackStack() })
        }

        composable(Screen.Achievements.route) {
            AchievementScreen(onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Screen.LevelSelect.route,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            val gameName = GameList.games.find { it.id == gameId }?.name ?: "游戏"
            LevelSelectScreen(
                gameId = gameId, gameName = gameName,
                onLevelSelected = { _ -> navController.navigate(Screen.Play.createRoute(gameId, "single")) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(onBackClick = { navController.popBackStack() })
        }

        composable(
            route = Screen.SkinSelector.route,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            val gameName = GameList.games.find { it.id == gameId }?.name ?: "游戏"
            SkinSelectorScreen(gameId = gameId, gameName = gameName, onBackClick = { navController.popBackStack() })
        }
    }
}
