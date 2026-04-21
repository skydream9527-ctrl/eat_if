package com.eatif.app.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.eatif.app.data.local.AchievementProgressDao
import com.eatif.app.data.local.FoodDao
import com.eatif.app.data.local.FoodDatabase
import com.eatif.app.data.local.FoodDataSeeder
import com.eatif.app.data.local.GameStatsDao
import com.eatif.app.data.local.HistoryDao
import com.eatif.app.data.local.LevelProgressDao
import com.eatif.app.data.local.PlayerProfileDao
import com.eatif.app.data.local.SkinCollectionDao
import com.eatif.app.data.repository.FoodRepositoryImpl
import com.eatif.app.data.repository.HistoryRepositoryImpl
import com.eatif.app.data.repository.RecommendRepositoryImpl
import com.eatif.app.domain.repository.FoodRepository
import com.eatif.app.domain.repository.HistoryRepository
import com.eatif.app.domain.repository.RecommendRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    private val MIGRATION_1_2 = object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE foods ADD COLUMN tags TEXT NOT NULL DEFAULT '[]'")
        }
    }

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                "CREATE TABLE player_profile (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "total_games_played INTEGER NOT NULL DEFAULT 0, " +
                        "total_play_time_seconds INTEGER NOT NULL DEFAULT 0, " +
                        "current_streak INTEGER NOT NULL DEFAULT 0, " +
                        "max_streak INTEGER NOT NULL DEFAULT 0, " +
                        "player_level INTEGER NOT NULL DEFAULT 1, " +
                        "player_xp INTEGER NOT NULL DEFAULT 0, " +
                        "last_played_date TEXT NOT NULL DEFAULT '')"
            )

            db.execSQL(
                "CREATE TABLE game_stats (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "game_id TEXT NOT NULL, " +
                        "food_name TEXT NOT NULL, " +
                        "score INTEGER NOT NULL, " +
                        "score_percent INTEGER NOT NULL, " +
                        "difficulty TEXT NOT NULL DEFAULT 'NORMAL', " +
                        "level INTEGER NOT NULL DEFAULT 1, " +
                        "play_time_seconds INTEGER NOT NULL DEFAULT 0, " +
                        "timestamp INTEGER NOT NULL)"
            )
            db.execSQL("CREATE INDEX idx_game_stats_game_id ON game_stats(game_id)")
            db.execSQL("CREATE INDEX idx_game_stats_timestamp ON game_stats(timestamp)")

            db.execSQL(
                "CREATE TABLE achievement_progress (" +
                        "achievement_id TEXT PRIMARY KEY, " +
                        "current_progress INTEGER NOT NULL DEFAULT 0, " +
                        "required_progress INTEGER NOT NULL, " +
                        "is_unlocked INTEGER NOT NULL DEFAULT 0, " +
                        "unlocked_at INTEGER)"
            )

            db.execSQL(
                "CREATE TABLE level_progress (" +
                        "game_id TEXT NOT NULL, " +
                        "current_level INTEGER NOT NULL DEFAULT 1, " +
                        "stars_json TEXT NOT NULL DEFAULT '{}', " +
                        "best_scores_json TEXT NOT NULL DEFAULT '{}', " +
                        "PRIMARY KEY (game_id))"
            )

            db.execSQL(
                "CREATE TABLE skin_collection (" +
                        "skin_id TEXT NOT NULL, " +
                        "game_id TEXT NOT NULL, " +
                        "is_unlocked INTEGER NOT NULL DEFAULT 0, " +
                        "is_active INTEGER NOT NULL DEFAULT 0, " +
                        "PRIMARY KEY (skin_id))"
            )
            db.execSQL("CREATE INDEX idx_skin_collection_game_id ON skin_collection(game_id)")
        }
    }

    @Provides
    @Singleton
    fun provideFoodDatabase(
        @ApplicationContext context: Context
    ): FoodDatabase {
        return Room.databaseBuilder(
            context,
            FoodDatabase::class.java,
            "food_database"
        )
            .addCallback(FoodDataSeeder.getCallback())
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
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
    fun providePlayerProfileDao(database: FoodDatabase): PlayerProfileDao = database.playerProfileDao()

    @Provides
    @Singleton
    fun provideGameStatsDao(database: FoodDatabase): GameStatsDao = database.gameStatsDao()

    @Provides
    @Singleton
    fun provideAchievementProgressDao(database: FoodDatabase): AchievementProgressDao = database.achievementProgressDao()

    @Provides
    @Singleton
    fun provideLevelProgressDao(database: FoodDatabase): LevelProgressDao = database.levelProgressDao()

    @Provides
    @Singleton
    fun provideSkinCollectionDao(database: FoodDatabase): SkinCollectionDao = database.skinCollectionDao()

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

    @Provides
    @Singleton
    fun provideRecommendRepository(
        historyRepository: HistoryRepository,
        foodRepository: FoodRepository
    ): RecommendRepository {
        return RecommendRepositoryImpl(historyRepository, foodRepository)
    }
}
