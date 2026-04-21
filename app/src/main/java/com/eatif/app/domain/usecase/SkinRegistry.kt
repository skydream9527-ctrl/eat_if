package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Skin
import com.eatif.app.domain.model.SkinRarity
import com.eatif.app.domain.model.SkinUnlockMethod

object SkinRegistry {
    val all: List<Skin> = listOf(
        Skin("snake_default", "经典绿", "snake", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("snake_neon", "霓虹蛇", "snake", SkinRarity.RARE, SkinUnlockMethod.ACHIEVEMENT, "初次胜利"),
        Skin("snake_golden", "金蛇", "snake", SkinRarity.LEGENDARY, SkinUnlockMethod.LEVEL, "达到第10关"),
        Skin("game2048_default", "经典", "game2048", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("game2048_dark", "暗黑", "game2048", SkinRarity.RARE, SkinUnlockMethod.ACHIEVEMENT, "2048 传奇"),
        Skin("flappy_default", "经典", "flappy", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("flappy_golden", "金翼", "flappy", SkinRarity.LEGENDARY, SkinUnlockMethod.ACHIEVEMENT, "月度达人"),
        Skin("spinwheel_default", "经典", "spinwheel", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("spinwheel_gold", "金轮", "spinwheel", SkinRarity.EPIC, SkinUnlockMethod.ACHIEVEMENT, "全制霸"),
        Skin("slot_default", "经典", "slot", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("shooting_default", "经典", "shooting", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("minesweeper_default", "经典", "minesweeper", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("tetris_default", "经典", "tetris", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("rps_default", "经典", "rps", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("jump_default", "经典", "jump", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("runner_default", "经典", "runner", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("climb100_default", "经典", "climb100", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("needle_default", "经典", "needle", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("onestroke_default", "经典", "onestroke", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("boxpusher_default", "经典", "boxpusher", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true)
    )

    fun getSkinsForGame(gameId: String): List<Skin> = all.filter { it.gameId == gameId }
    fun getById(id: String): Skin? = all.find { it.id == id }
    fun getDefaultSkin(gameId: String): Skin = getSkinsForGame(gameId).find { it.isDefault } ?: getSkinsForGame(gameId).first()
}
