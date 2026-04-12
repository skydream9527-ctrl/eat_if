package com.eatif.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun EatIfNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen()
        }
        
        composable(
            route = Screen.GameSelect.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: ""
            GameSelectScreen(mode = mode)
        }
        
        composable(
            route = Screen.Play.route,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            PlayScreen(gameId = gameId)
        }
        
        composable(
            route = Screen.Result.route,
            arguments = listOf(navArgument("foodName") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodName = backStackEntry.arguments?.getString("foodName") ?: ""
            ResultScreen(foodName = foodName)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen()
        }
        
        composable(Screen.FoodLibrary.route) {
            FoodLibraryScreen()
        }
    }
}

@Composable
private fun HomeScreen() {}

@Composable
private fun GameSelectScreen(mode: String) {}

@Composable
private fun PlayScreen(gameId: String) {}

@Composable
private fun ResultScreen(foodName: String) {}

@Composable
private fun SettingsScreen() {}

@Composable
private fun FoodLibraryScreen() {}
