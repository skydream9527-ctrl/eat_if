package com.eatif.app.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")

    data object Setup : Screen("setup/{mode}") {
        fun createRoute(mode: String) = "setup/$mode"
    }

    data object GameSelect : Screen("game_select/{mode}") {
        fun createRoute(mode: String) = "game_select/$mode"
    }

    data object Play : Screen("play/{gameId}/{mode}") {
        fun createRoute(gameId: String, mode: String = "single") = "play/$gameId/$mode"
    }

    data object FoodSelect : Screen("food_select")

    data object Result : Screen("result/{foodName}/{scorePercent}") {
        fun createRoute(foodName: String, scorePercent: Int = -1) =
            "result/$foodName/$scorePercent"
    }

    data object Settings : Screen("settings")

    data object FoodLibrary : Screen("food_library")

    data object History : Screen("history")
}
