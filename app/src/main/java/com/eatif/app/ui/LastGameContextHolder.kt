package com.eatif.app.ui

data class LastGameContext(
    val gameId: String,
    val mode: String,
    val levelNumber: Int
)

object LastGameContextHolder {
    var lastGame: LastGameContext? = null
}