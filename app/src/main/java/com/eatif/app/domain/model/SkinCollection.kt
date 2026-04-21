package com.eatif.app.domain.model

data class SkinCollection(
    val skinId: String,
    val gameId: String,
    val isUnlocked: Boolean = false,
    val isActive: Boolean = false
)
