package com.eatif.app.domain.model

data class Skin(
    val id: String,
    val name: String,
    val gameId: String,
    val rarity: SkinRarity,
    val unlockMethod: SkinUnlockMethod,
    val unlockRequirement: String = "",
    val isDefault: Boolean = false
)

enum class SkinRarity { COMMON, RARE, EPIC, LEGENDARY }
enum class SkinUnlockMethod { ACHIEVEMENT, LEVEL, DEFAULT }
