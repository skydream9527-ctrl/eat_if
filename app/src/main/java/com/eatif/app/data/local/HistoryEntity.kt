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
    val timestamp: Long = System.currentTimeMillis(),
    /** 是否采纳了推荐（用户从推荐 Top3 中选择 vs 自主另选） */
    val wasRecommended: Boolean = false
)
