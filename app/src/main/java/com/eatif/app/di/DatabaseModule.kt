package com.eatif.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eatif.app.data.local.FoodDao
import com.eatif.app.data.local.FoodDatabase
import com.eatif.app.data.local.FoodDataSeeder
import com.eatif.app.data.local.HistoryDao
import com.eatif.app.data.repository.FoodRepositoryImpl
import com.eatif.app.data.repository.HistoryRepositoryImpl
import com.eatif.app.domain.repository.FoodRepository
import com.eatif.app.domain.repository.HistoryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFoodDatabase(
        @ApplicationContext context: Context
    ): FoodDatabase {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE foods ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'")
            }
        }

        return Room.databaseBuilder(
            context,
            FoodDatabase::class.java,
            "food_database"
        )
            .addCallback(FoodDataSeeder.getCallback())
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    @Singleton
    fun provideFoodDao(database: FoodDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    @Singleton
    fun provideHistoryDao(database: FoodDatabase): HistoryDao {
        return database.historyDao()
    }

    @Provides
    @Singleton
    fun provideFoodRepository(foodDao: FoodDao): FoodRepository {
        return FoodRepositoryImpl(foodDao)
    }

    @Provides
    @Singleton
    fun provideHistoryRepository(historyDao: HistoryDao): HistoryRepository {
        return HistoryRepositoryImpl(historyDao)
    }
}
