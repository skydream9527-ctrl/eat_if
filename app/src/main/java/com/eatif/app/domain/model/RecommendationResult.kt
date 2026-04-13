package com.eatif.app.domain.model

data class RecommendationResult(
    val shopName: String,
    val reason: String,
    val scorePercent: Float,
    val emoji: String
)
