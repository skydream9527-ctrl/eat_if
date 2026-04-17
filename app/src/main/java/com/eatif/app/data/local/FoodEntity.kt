package com.eatif.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val imageUrl: String?,
    val weight: Int,
    val tags: String = "[]"
)
