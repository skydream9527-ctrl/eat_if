package com.eatif.app.domain.model

data class GameStats(
    val id: Long = 0,
    val gameId: String,
    val foodName: String,
    val score: Int,
    val scorePercent: Int,
    val difficulty: String = "NORMAL",
    val level: Int = 1,
    val playTimeSeconds: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)
