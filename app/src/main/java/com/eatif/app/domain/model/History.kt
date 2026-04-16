package com.eatif.app.domain.model

data class History(
    val id: Long = 0,
    val foodName: String,
    val gameName: String,
    val scorePercent: Int,
    val timestamp: Long
)