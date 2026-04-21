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

    data object Play : Screen("play/{gameId}/{mode}/{levelNumber?}") {
        fun createRoute(gameId: String, mode: String = "single", levelNumber: Int = 0) =
            if (levelNumber > 0) "play/$gameId/$mode/$levelNumber" else "play/$gameId/$mode"
    }

    data object FoodSelect : Screen("food_select")

    data object Result : Screen("result/{foodName}/{scorePercent}") {
        fun createRoute(foodName: String, scorePercent: Int = -1) =
            "result/$foodName/$scorePercent"
    }

    data object GameRule : Screen("game_rule/{gameId}") {
        fun createRoute(gameId: String) = "game_rule/$gameId"
    }

    data object Settings : Screen("settings")

    data object FoodLibrary : Screen("food_library")

    data object History : Screen("history")
    data object Profile : Screen("profile")
    data object Achievements : Screen("achievements")
    data object LevelSelect : Screen("level_select/{gameId}") {
        fun createRoute(gameId: String) = "level_select/$gameId"
    }
    data object Stats : Screen("stats")
    data object SkinSelector : Screen("skin_selector/{gameId}") {
        fun createRoute(gameId: String) = "skin_selector/$gameId"
    }
}
