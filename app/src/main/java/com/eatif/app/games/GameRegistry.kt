package com.eatif.app.games

import androidx.compose.runtime.Composable
import com.eatif.app.domain.model.Food

data class GameConfig(
    val gameId: String,
    val supportsSelfPause: Boolean = false,
    val content: @Composable (
        foods: List<Food>,
        isPaused: Boolean,
        onPauseToggle: ((Boolean) -> Unit)?,
        onResult: (String, Int) -> Unit,
        mode: String
    ) -> Unit
)

object GameRegistry {
    private val registry = mutableMapOf<String, GameConfig>()

    fun register(config: GameConfig) {
        registry[config.gameId] = config
    }

    fun get(gameId: String): GameConfig? = registry[gameId]

    fun allIds(): Set<String> = registry.keys
}
