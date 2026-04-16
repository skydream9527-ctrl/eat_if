package com.eatif.app.ui.tutorial

data class GameTutorial(
    val gameId: String,
    val title: String,
    val description: String,
    val tips: List<String>
)

object GameTutorials {

    fun getTutorial(gameId: String): GameTutorial? {
        return tutorials[gameId]
    }

    private val tutorials = mapOf(
        "2048" to GameTutorial(
            gameId = "2048",
            title = "🧩 2048 游戏教程",
            description = "通过滑动合并数字，目标是得到2048！",
            tips = listOf(
                "📱 滑动屏幕或点击方向按钮移动所有数字",
                "🔢 相同数字碰撞会合并成它们的和",
                "🎯 尽量让大数字保持在角落",
                "⚠️ 不要急于向上滑动，留出空间"
            )
        ),
        "minesweeper" to GameTutorial(
            gameId = "minesweeper",
            title = "🔍 扫雷游戏教程",
            description = "找出所有安全格子避开地雷！",
            tips = listOf(
                "👆 点击格子揭示内容",
                "🔢 数字表示周围8个格子的地雷数量",
                "🚩 长按可以标记地雷",
                "💡 如果一个已揭示格子的数字等于周围未揭示格子数，这些格子都是地雷"
            )
        ),
        "tetris" to GameTutorial(
            gameId = "tetris",
            title = "🧱 俄罗斯方块教程",
            description = "旋转下落方块，填满一行即可消除！",
            tips = listOf(
                "⬅️➡️ 左右移动方块",
                "🔄 点击按钮旋转方块",
                "⬇️ 加速下落",
                "📋 方块堆到顶部就输了"
            )
        ),
        "snake" to GameTutorial(
            gameId = "snake",
            title = "🐍 贪吃蛇教程",
            description = "控制蛇吃食物变长，不要撞到墙壁或自己！",
            tips = listOf(
                "🎮 屏幕上的方向按钮控制方向",
                "🍎 吃到食物蛇身变长",
                "💀 不要撞到墙壁或自己的身体",
                "⚡ 吃食物时不要犹豫太久"
            )
        ),
        "flappy" to GameTutorial(
            gameId = "flappy",
            title = "🐦 Flappy Eat 教程",
            description = "点击屏幕让小鸟飞翔，穿过障碍间隙！",
            tips = listOf(
                "👆 点击屏幕让小鸟向上飞",
                "⚡ 自由下落是常态，要持续点击",
                "🚪 找到间隙穿过",
                "💨 掌握节奏很重要"
            )
        ),
        "onetstroke" to GameTutorial(
            gameId = "onetstroke",
            title = "✏️ 一笔画教程",
            description = "一笔连续连接所有点，不能重复！",
            tips = listOf(
                "👆 从任意点开始拖动",
                "📍 每个点只能经过一次",
                "🔗 必须连接所有点",
                "↩️ 不能走回头路"
            )
        ),
        "boxpusher" to GameTutorial(
            gameId = "boxpusher",
            title = "📦 推箱子教程",
            description = "推动箱子到目标位置！",
            tips = listOf(
                "🎮 使用方向按钮移动角色",
                "📦 推到箱子时箱子会移动",
                "🎯 把两个箱子推到目标位置",
                "🔄 注意不要把箱子推到角落"
            )
        ),
        "shooting" to GameTutorial(
            gameId = "shooting",
            title = "🎯 打靶教程",
            description = "射击靶子，获得高分通过测试！",
            tips = listOf(
                "👆 点击靶子进行射击",
                "🎯 靶心100分，越靠外分数越低",
                "📊 5发子弹，总分250分以上通过",
                "🔄 可以重新开始挑战"
            )
        )
    )
}