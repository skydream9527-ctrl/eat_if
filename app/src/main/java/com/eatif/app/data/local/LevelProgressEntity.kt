package com.eatif.app.data.local

import androidx.room.Entity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "level_progress", primaryKeys = ["game_id"])
data class LevelProgressEntity(
    val game_id: String,
    val current_level: Int = 1,
    val stars_json: String = "{}",
    val best_scores_json: String = "{}"
)

fun LevelProgressEntity.toDomain() = com.eatif.app.domain.model.LevelProgress(
    gameId = game_id, currentLevel = current_level,
    stars = parseMap(stars_json), bestScores = parseMap(best_scores_json)
)

fun com.eatif.app.domain.model.LevelProgress.toEntity() = LevelProgressEntity(
    game_id = gameId, current_level = currentLevel,
    stars_json = mapToString(stars), best_scores_json = mapToString(bestScores)
)

private fun parseMap(json: String): Map<Int, Int> {
    return try {
        val gson = Gson()
        val type = object : TypeToken<Map<Int, Int>>() {}.type
        gson.fromJson(json, type) ?: emptyMap()
    } catch (e: Exception) { emptyMap() }
}

private fun mapToString(map: Map<Int, Int>): String = Gson().toJson(map)
