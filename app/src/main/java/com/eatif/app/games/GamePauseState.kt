package com.eatif.app.games

data class GamePauseState(
    val isPaused: Boolean = false,
    val onPauseToggle: ((Boolean) -> Unit)? = null
)