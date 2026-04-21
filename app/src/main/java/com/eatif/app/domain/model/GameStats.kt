package com.eatif.app.domain.model

import com.eatif.app.domain.model.GameDifficulty

data class GameStats(
    val id: Long = 0,
    val gameId: String,
    val foodName: String,
    val score: Int,
    val scorePercent: Int,
    val difficulty: GameDifficulty = GameDifficulty.NORMAL,
    val level: Int = 1,
    val playTimeSeconds: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)
