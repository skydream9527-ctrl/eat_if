package com.eatif.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val foodName: String,
    val gameName: String,
    val scorePercent: Int,
    val timestamp: Long = System.currentTimeMillis()
)