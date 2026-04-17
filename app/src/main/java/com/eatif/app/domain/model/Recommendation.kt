package com.eatif.app.domain.model

data class Recommendation(
    val food: Food,
    val reason: String,
    val score: Double
)
