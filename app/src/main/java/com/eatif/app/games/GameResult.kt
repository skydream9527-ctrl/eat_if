package com.eatif.app.games

sealed class GameResult {
    data class FoodSelected(val foodName: String) : GameResult()
    data object Continue : GameResult()
}
