package com.eatif.app.data.local

import androidx.room.TypeConverter
import com.eatif.app.domain.model.AchievementCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ProgressTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringMap(map: Map<Int, Int>): String = gson.toJson(map)

    @TypeConverter
    fun toStringMap(json: String): Map<Int, Int> {
        val type = object : TypeToken<Map<Int, Int>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromAchievementCategory(category: AchievementCategory): String = category.name

    @TypeConverter
    fun toAchievementCategory(name: String): AchievementCategory {
        return try { AchievementCategory.valueOf(name) } catch (e: Exception) { AchievementCategory.MILESTONE }
    }
}
