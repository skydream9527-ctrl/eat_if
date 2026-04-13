package com.eatif.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eatif.app.ui.screens.HomeScreen
import com.eatif.app.ui.screens.SetupScreen
import com.eatif.app.ui.screens.GameSelectScreen
import com.eatif.app.ui.screens.PlayScreen
import com.eatif.app.ui.screens.FoodSelectScreen
import com.eatif.app.ui.screens.ResultScreen
import com.eatif.app.ui.screens.SettingsScreen
import com.eatif.app.ui.screens.FoodLibraryScreen

@Composable
fun EatIfNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
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
                }
            )
        }

        // 新增：配置店铺选项界面
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
                    navController.navigate(Screen.Play.createRoute(gameId))
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Play.route,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            PlayScreen(
                gameId = gameId,
                onGameEnd = { foodName, scorePercent ->
                    navController.navigate(
                        Screen.Result.createRoute(foodName, scorePercent)
                    ) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.FoodSelect.route) {
            FoodSelectScreen(
                onFoodSelected = { foodName ->
                    navController.navigate(Screen.Result.createRoute(foodName, -1)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("foodName") { type = NavType.StringType },
                navArgument("scorePercent") {
                    type = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { backStackEntry ->
            val foodName = backStackEntry.arguments?.getString("foodName") ?: ""
            val scorePercent = backStackEntry.arguments?.getInt("scorePercent") ?: -1
            ResultScreen(
                foodName = foodName,
                scorePercent = scorePercent,
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
                }
            )
        }

        composable(Screen.FoodLibrary.route) {
            FoodLibraryScreen(
                onBackClick = {
                    navController.popBackStack()
                }
            )
        }
    }
}
