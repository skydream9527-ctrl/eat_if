package com.eatif.app.games

import androidx.compose.runtime.Composable
import com.eatif.app.domain.model.Food

interface GameEngine {
    val gameId: String
    val gameName: String

    @Composable
    fun Render(foods: List<Food>, onResult: (GameResult) -> Unit)
}
