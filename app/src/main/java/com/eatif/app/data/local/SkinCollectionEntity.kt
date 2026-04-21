package com.eatif.app.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(tableName = "skin_collection", indices = [Index(value = ["game_id"])])
data class SkinCollectionEntity(
    val skin_id: String,
    val game_id: String,
    val is_unlocked: Boolean = false,
    val is_active: Boolean = false
)

fun SkinCollectionEntity.toDomain() = com.eatif.app.domain.model.SkinCollection(
    skinId = skin_id, gameId = game_id, isUnlocked = is_unlocked, isActive = is_active
)

fun com.eatif.app.domain.model.SkinCollection.toEntity() = SkinCollectionEntity(
    skin_id = skinId, game_id = gameId, is_unlocked = isUnlocked, is_active = isActive
)
