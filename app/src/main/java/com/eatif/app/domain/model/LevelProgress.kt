package com.eatif.app.domain.model

data class LevelProgress(
    val gameId: String,
    val currentLevel: Int = 1,
    val stars: Map<Int, Int> = emptyMap(),
    val bestScores: Map<Int, Int> = emptyMap()
)
