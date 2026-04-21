package com.eatif.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        FoodEntity::class, HistoryEntity::class,
        PlayerProfileEntity::class, GameStatsEntity::class,
        AchievementProgressEntity::class, LevelProgressEntity::class,
        SkinCollectionEntity::class
    ],
    version = 3,
    exportSchema = true
)
@TypeConverters(FoodTagTypeConverter::class, ProgressTypeConverters::class)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun historyDao(): HistoryDao
    abstract fun playerProfileDao(): PlayerProfileDao
    abstract fun gameStatsDao(): GameStatsDao
    abstract fun achievementProgressDao(): AchievementProgressDao
    abstract fun levelProgressDao(): LevelProgressDao
    abstract fun skinCollectionDao(): SkinCollectionDao
}
