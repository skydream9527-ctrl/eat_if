package com.eatif.app.data.local

import androidx.room.TypeConverter
import com.eatif.app.domain.model.FoodTag
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FoodTagTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromFoodTags(tags: List<FoodTag>): String {
        return gson.toJson(tags.map { it.name })
    }

    @TypeConverter
    fun toFoodTags(data: String): List<FoodTag> {
        val type = object : TypeToken<List<String>>() {}.type
        val names: List<String> = gson.fromJson(data, type)
        return names.mapNotNull { name ->
            FoodTag.entries.find { it.name == name }
        }
    }
}
