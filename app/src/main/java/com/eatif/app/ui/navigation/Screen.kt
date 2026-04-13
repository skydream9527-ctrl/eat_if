package com.eatif.app.ui.navigation

sealed class Screen(val route: String) {
    data object Home : Screen("home")

    data object Setup : Screen("setup/{mode}") {
        fun createRoute(mode: String) = "setup/$mode"
    }

    data object GameSelect : Screen("game_select/{mode}") {
        fun createRoute(mode: String) = "game_select/$mode"
    }

    data object Play : Screen("play/{gameId}") {
        fun createRoute(gameId: String) = "play/$gameId"
    }

    data object FoodSelect : Screen("food_select")

    /**
     * foodName: 游戏推荐的店铺/菜名
     * scorePercent: 游戏得分百分比（0-100 整数，避免 URL 浮点问题）
     */
    data object Result : Screen("result/{foodName}/{scorePercent}") {
        fun createRoute(foodName: String, scorePercent: Int = -1) =
            "result/$foodName/$scorePercent"
    }

    data object Settings : Screen("settings")

    data object FoodLibrary : Screen("food_library")
}
