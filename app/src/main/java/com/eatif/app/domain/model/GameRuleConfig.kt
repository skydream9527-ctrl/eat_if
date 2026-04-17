package com.eatif.app.domain.model

data class GameRuleConfig(
    val gameId: String,
    val rounds: Int = 3,
    val timeLimit: Int = 0,
    val customParams: Map<String, String> = emptyMap()
)
