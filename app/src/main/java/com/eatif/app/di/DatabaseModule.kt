package com.eatif.app.di

import android.content.Context
import androidx.room.Room
import com.eatif.app.data.local.FoodDao
import com.eatif.app.data.local.FoodDatabase
import com.eatif.app.data.repository.FoodRepositoryImpl
import com.eatif.app.domain.repository.FoodRepository
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
        return Room.databaseBuilder(
            context,
            FoodDatabase::class.java,
            "food_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFoodDao(database: FoodDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    @Singleton
    fun provideFoodRepository(foodDao: FoodDao): FoodRepository {
        return FoodRepositoryImpl(foodDao)
    }
}
