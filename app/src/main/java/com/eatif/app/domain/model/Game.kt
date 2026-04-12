package com.eatif.app.domain.model

enum class GameCategory {
    PRECISION,
    JUMP,
    CLIMB,
    LUCK,
    BATTLE,
    PUZZLE,
    CLASSIC,
    ARCADE
}

data class Game(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val category: GameCategory
)

object GameList {
    val games = listOf(
        Game("needle", "见缝插针", "🎯", "精准类", GameCategory.PRECISION),
        Game("jump", "跳一跳", "🏃", "跳跃类", GameCategory.JUMP),
        Game("climb100", "勇闯100层", "🧗", "攀爬类", GameCategory.CLIMB),
        Game("slot", "老虎机", "🎰", "运气类", GameCategory.LUCK),
        Game("spinwheel", "大转盘", "🎯", "运气类", GameCategory.LUCK),
        Game("rps", "石头剪刀布", "✊", "对战类", GameCategory.BATTLE),
        Game("2048", "2048", "🧩", "益智类", GameCategory.PUZZLE),
        Game("snake", "贪吃蛇", "🐍", "经典类", GameCategory.CLASSIC),
        Game("tetris", "俄罗斯方块", "🧱", "消消类", GameCategory.ARCADE),
        Game("minesweeper", "扫雷", "🔍", "探索类", GameCategory.PRECISION),
        Game("onetstroke", "一笔画", "✏️", "绘制类", GameCategory.PUZZLE),
        Game("flappy", "Flappy Eat", "🐦", "飞行类", GameCategory.JUMP),
        Game("boxpusher", "推箱子", "📦", "推箱类", GameCategory.PUZZLE),
        Game("runner", "无限跑酷", "🏃", "跑酷类", GameCategory.ARCADE),
        Game("shooting", "打靶", "🎯", "射击类", GameCategory.PRECISION)
    )
}
