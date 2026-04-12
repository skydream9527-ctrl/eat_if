package com.eatif.app.domain.model

data class Food(
    val id: Long = 0,
    val name: String,
    val category: String,
    val imageUrl: String? = null,
    val weight: Int = 1
)
