package com.eatif.app.domain.model

enum class GameDifficulty {
    EASY,
    NORMAL,
    HARD
}

fun GameDifficulty.getDisplayName(): String {
    return when (this) {
        GameDifficulty.EASY -> "简单"
        GameDifficulty.NORMAL -> "普通"
        GameDifficulty.HARD -> "困难"
    }
}

fun GameDifficulty.getEmoji(): String {
    return when (this) {
        GameDifficulty.EASY -> "😊"
        GameDifficulty.NORMAL -> "🤔"
        GameDifficulty.HARD -> "😈"
    }
}