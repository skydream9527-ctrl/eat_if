package com.eatif.app.domain.model

/**
 * 每日任务类型
 */
enum class DailyTaskType(val id: String) {
    /** 玩 N 局游戏 */
    PLAY_GAMES("play_games"),
    /** 尝试一款没玩过的游戏 */
    TRY_NEW_GAME("try_new_game"),
    /** 获得一次高分（>=70%） */
    HIGH_SCORE("high_score"),
    /** 采纳一次推荐 */
    ADOPT_RECOMMEND("adopt_recommend"),
    /** 玩 N 款不同的游戏 */
    PLAY_DIFFERENT("play_different");

    companion object {
        fun fromId(id: String): DailyTaskType = entries.find { it.id == id } ?: PLAY_GAMES
    }
}

/**
 * 每日任务
 */
data class DailyTask(
    val id: Long = 0,
    val taskType: DailyTaskType,
    val description: String,
    val targetProgress: Int,
    val currentProgress: Int = 0,
    val isCompleted: Boolean = false,
    val isRewardClaimed: Boolean = false,
    val xpReward: Int,
    val date: String  // yyyy-MM-dd
) {
    val progressPercent: Float
        get() = if (targetProgress == 0) 0f else (currentProgress.toFloat() / targetProgress).coerceIn(0f, 1f)

    val canClaimReward: Boolean
        get() = isCompleted && !isRewardClaimed
}
