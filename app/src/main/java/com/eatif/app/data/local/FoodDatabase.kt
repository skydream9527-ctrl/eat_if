package com.eatif.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FoodEntity::class, HistoryEntity::class],
    version = 2,
    exportSchema = true
)
@TypeConverters(FoodTagTypeConverter::class)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun historyDao(): HistoryDao
}
