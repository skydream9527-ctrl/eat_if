package com.eatif.app.data.local

import androidx.room.TypeConverter
import com.eatif.app.domain.model.AchievementCategory

class ProgressTypeConverters {
    // Reserved for future AchievementEntity with category field
    @TypeConverter
    fun fromAchievementCategory(category: AchievementCategory): String = category.name

    @TypeConverter
    fun toAchievementCategory(name: String): AchievementCategory {
        return try { AchievementCategory.valueOf(name) } catch (e: Exception) { AchievementCategory.MILESTONE }
    }
}
