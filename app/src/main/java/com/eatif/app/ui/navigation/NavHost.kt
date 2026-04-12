package com.eatif.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eatif.app.ui.screens.HomeScreen
import com.eatif.app.ui.screens.GameSelectScreen
import com.eatif.app.ui.screens.PlayScreen
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
                    navController.navigate(Screen.GameSelect.createRoute("single"))
                },
                onTwoPlayerClick = {
                    navController.navigate(Screen.GameSelect.createRoute("double"))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
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
                onGameEnd = { foodName ->
                    navController.navigate(Screen.Result.createRoute(foodName)) {
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
            arguments = listOf(navArgument("foodName") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodName = backStackEntry.arguments?.getString("foodName") ?: ""
            ResultScreen(
                foodName = foodName,
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
