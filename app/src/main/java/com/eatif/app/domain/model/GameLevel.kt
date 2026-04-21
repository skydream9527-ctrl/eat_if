package com.eatif.app.domain.model

data class GameLevel(
    val gameId: String,
    val levelNumber: Int,
    val difficulty: GameDifficulty,
    val requiredStars: Int = 0,
    val params: Map<String, String> = emptyMap()
)
