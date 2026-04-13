package com.eatif.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    
    data object GameSelect : Screen("game_select/{mode}") {
        fun createRoute(mode: String) = "game_select/$mode"
    }
    
    data object Play : Screen("play/{gameId}") {
        fun createRoute(gameId: String) = "play/$gameId"
    }
    
    data object FoodSelect : Screen("food_select")
    
    data object Result : Screen("result/{foodName}") {
        fun createRoute(foodName: String) = "result/$foodName"
    }
    
    data object Settings : Screen("settings")
    
    data object FoodLibrary : Screen("food_library")
}
