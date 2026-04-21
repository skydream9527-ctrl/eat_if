package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Achievement
import com.eatif.app.domain.model.AchievementCategory
import com.eatif.app.domain.model.AchievementCondition

object AchievementRegistry {
    val all: List<Achievement> = listOf(
        Achievement("first_game", "初次尝试", "完成你的第一局游戏", "🎮", AchievementCategory.MILESTONE, AchievementCondition.TotalGames(1), 10),
        Achievement("ten_games", "十局达人", "完成 10 局游戏", "🔟", AchievementCategory.MILESTONE, AchievementCondition.TotalGames(10), 20),
        Achievement("fifty_games", "游戏狂热", "完成 50 局游戏", "🔥", AchievementCategory.MILESTONE, AchievementCondition.TotalGames(50), 40),
        Achievement("hundred_games", "百战成神", "完成 100 局游戏", "⚡", AchievementCategory.MILESTONE, AchievementCondition.TotalGames(100), 100),
        Achievement("streak_7", "一周打卡", "连续 7 天游玩", "📅", AchievementCategory.STREAK, AchievementCondition.ConsecutiveDays(7), 100),
        Achievement("streak_30", "月度达人", "连续 30 天游玩", "🗓️", AchievementCategory.STREAK, AchievementCondition.ConsecutiveDays(30), 200, unlockSkinId = "flappy_golden"),
        Achievement("first_win", "初次胜利", "以超过 70% 的得分完成游戏", "🏆", AchievementCategory.SKILL, AchievementCondition.GameHighScore(gameId = "any", score = 70), 50, unlockSkinId = "snake_neon"),
        Achievement("snake_master", "蛇王", "贪吃蛇单局得分超过 50", "🐍", AchievementCategory.SKILL, AchievementCondition.GameHighScore(gameId = "snake", score = 50), 60),
        Achievement("two048_legend", "2048 传奇", "2048 达到 1024", "🔢", AchievementCategory.SKILL, AchievementCondition.GameHighScore(gameId = "game2048", score = 1024), 100, unlockSkinId = "game2048_dark"),
        Achievement("perfect_shot", "神枪手", "打靶命中率超过 90%", "🎯", AchievementCategory.SKILL, AchievementCondition.GameHighScore(gameId = "shooting", score = 90), 50),
        Achievement("slot_jackpot", "幸运大奖", "老虎机命中大奖", "🎰", AchievementCategory.SKILL, AchievementCondition.GameHighScore(gameId = "slot", score = 100), 40),
        Achievement("marathon", "马拉松", "单局游戏超过 10 分钟", "🏃", AchievementCategory.MILESTONE, AchievementCondition.TotalPlayTime(600), 70),
        Achievement("total_hour", "时光旅者", "累计游玩时间超过 1 小时", "⏰", AchievementCategory.MILESTONE, AchievementCondition.TotalPlayTime(3600), 60),
        Achievement("five_different", "尝鲜玩家", "玩过 5 款不同的游戏", "🌟", AchievementCategory.EXPLORATION, AchievementCondition.PlayAllGames(5), 30),
        Achievement("all_games", "全制霸", "玩过全部 15 款游戏", "👑", AchievementCategory.EXPLORATION, AchievementCondition.PlayAllGames(15), 80, unlockSkinId = "spinwheel_gold"),
        Achievement("match3_combo_5", "连击大师", "消消乐单次消除5组", "🧩", AchievementCategory.SKILL, AchievementCondition.GameHighScore(gameId = "match3", score = 50), 50),
        Achievement("linklink_perfect", "连连看完美通关", "零失误完成连连看", "🔗", AchievementCategory.SKILL, AchievementCondition.GameHighScore(gameId = "linklink", score = 100), 60),
        Achievement("memory_no_error", "记忆力满分", "记忆翻牌零翻错", "🃏", AchievementCategory.SKILL, AchievementCondition.GameHighScore(gameId = "memory", score = 100), 60),
        Achievement("pingpong_win_5", "乒乓球连胜", "乒乓球连胜5次", "🏓", AchievementCategory.SKILL, AchievementCondition.GameHighScore(gameId = "pingpong", score = 5), 50)
    )

    fun getById(id: String): Achievement? = all.find { it.id == id }
}
