# Game Depth Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete game growth system (achievements, levels, XP, skins, stats) on top of the existing 15-game Eat If app.

**Architecture:** Layered architecture following existing patterns — domain models → Room entities/DAOs → UseCases → ViewModels → Compose screens. All new modules share a unified PlayerProfile data layer via Room database migration v2→v3.

**Tech Stack:** Kotlin, Jetpack Compose, Room, Hilt, Coroutines/Flow, SharedPreferences

---

## File Structure Map

| File | Type | Responsibility |
|------|------|----------------|
| `domain/model/PlayerProfile.kt` | New | Player profile domain model |
| `domain/model/GameStats.kt` | New | Game stats domain model |
| `domain/model/Achievement.kt` | New | Achievement + condition definitions |
| `domain/model/AchievementProgress.kt` | New | Achievement progress model |
| `domain/model/GameLevel.kt` | New | Level definition model |
| `domain/model/LevelProgress.kt` | New | Level progress model |
| `domain/model/Skin.kt` | New | Skin definition model |
| `domain/model/SkinCollection.kt` | New | Skin collection state model |
| `data/local/PlayerProfileEntity.kt` | New | Room entity for player profile |
| `data/local/GameStatsEntity.kt` | New | Room entity for game stats |
| `data/local/AchievementProgressEntity.kt` | New | Room entity for achievement progress |
| `data/local/LevelProgressEntity.kt` | New | Room entity for level progress |
| `data/local/SkinCollectionEntity.kt` | New | Room entity for skin collection |
| `data/local/ProgressTypeConverters.kt` | New | JSON TypeConverters for Maps/Lists |
| `data/local/PlayerProfileDao.kt` | New | DAO for player profile |
| `data/local/GameStatsDao.kt` | New | DAO for game stats + leaderboard queries |
| `data/local/AchievementProgressDao.kt` | New | DAO for achievement progress |
| `data/local/LevelProgressDao.kt` | New | DAO for level progress |
| `data/local/SkinCollectionDao.kt` | New | DAO for skin collection |
| `data/local/FoodDatabase.kt` | Modify | v2→v3, add entities + TypeConverters |
| `di/DatabaseModule.kt` | Modify | Migration 2→3, new DAO providers |
| `ui/settings/SkinSettingsManager.kt` | New | SharedPreferences for skin selection |
| `ui/settings/AchievementSettingsManager.kt` | New | SharedPreferences for quick achievement lookup |
| `domain/usecase/AchievementRegistry.kt` | New | Hard-coded achievement definitions |
| `domain/usecase/AchievementEngine.kt` | New | Achievement detection engine |
| `domain/usecase/PlayerProfileUseCase.kt` | New | XP calculation + profile updates |
| `domain/usecase/GameLevelRegistry.kt` | New | Hard-coded level definitions |
| `domain/usecase/LevelManager.kt` | New | Level unlock/progress management |
| `domain/usecase/SkinResolver.kt` | New | Resolve active skin for a game |
| `domain/usecase/StatsUseCase.kt` | New | Stats aggregation queries |
| `ui/screens/PlayViewModel.kt` | Modify | Add game end processing logic |
| `ui/screens/ResultScreen.kt` | Modify | Add XP animation, achievement popup |
| `ui/screens/ResultViewModel.kt` | New | Handle XP, level-up, achievements display |
| `ui/screens/HomeScreen.kt` | Modify | Add bottom nav + player level badge |
| `ui/screens/GameSelectScreen.kt` | Modify | Add stars, high scores, level icon |
| `ui/screens/ProfileScreen.kt` | New | Player profile page |
| `ui/screens/ProfileViewModel.kt` | New | Profile data aggregation |
| `ui/screens/AchievementScreen.kt` | New | Achievement list/grid page |
| `ui/screens/AchievementViewModel.kt` | New | Achievement data + filtering |
| `ui/screens/StatsScreen.kt` | New | Stats/leaderboard page |
| `ui/screens/StatsViewModel.kt` | New | Stats aggregation |
| `ui/screens/LevelSelectScreen.kt` | New | Level selection grid |
| `ui/screens/LevelSelectViewModel.kt` | New | Level data management |
| `ui/screens/SkinSelectorScreen.kt` | New | Skin selection page |
| `ui/navigation/Screen.kt` | Modify | Add new routes |
| `ui/navigation/NavHost.kt` | Modify | Register new screens |
| `ui/components/AchievementUnlockDialog.kt` | New | Achievement unlock popup component |
| `ui/components/ProgressBar.kt` | New | Reusable XP progress bar |

---

### Task 1: Data Models + Database Migration (v2→v3)

**Goal:** Create all domain models, Room entities, DAOs, TypeConverters, and migrate database from v2 to v3.

**Files:**
- Create: `domain/model/PlayerProfile.kt`
- Create: `domain/model/GameStats.kt`
- Create: `domain/model/Achievement.kt`
- Create: `domain/model/AchievementProgress.kt`
- Create: `domain/model/GameLevel.kt`
- Create: `domain/model/LevelProgress.kt`
- Create: `domain/model/Skin.kt`
- Create: `domain/model/SkinCollection.kt`
- Create: `data/local/PlayerProfileEntity.kt`
- Create: `data/local/GameStatsEntity.kt`
- Create: `data/local/AchievementProgressEntity.kt`
- Create: `data/local/LevelProgressEntity.kt`
- Create: `data/local/SkinCollectionEntity.kt`
- Create: `data/local/ProgressTypeConverters.kt`
- Create: `data/local/PlayerProfileDao.kt`
- Create: `data/local/GameStatsDao.kt`
- Create: `data/local/AchievementProgressDao.kt`
- Create: `data/local/LevelProgressDao.kt`
- Create: `data/local/SkinCollectionDao.kt`
- Modify: `data/local/FoodDatabase.kt`
- Modify: `di/DatabaseModule.kt`

- [ ] **Step 1: Create domain models**

Create all 8 domain model files in `domain/model/`:

`domain/model/PlayerProfile.kt`:
```kotlin
package com.eatif.app.domain.model

data class PlayerProfile(
    val id: Long = 0,
    val totalGamesPlayed: Int = 0,
    val totalPlayTimeSeconds: Long = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val playerLevel: Int = 1,
    val playerXP: Int = 0,
    val lastPlayedDate: String = ""
)
```

`domain/model/GameStats.kt`:
```kotlin
package com.eatif.app.domain.model

data class GameStats(
    val id: Long = 0,
    val gameId: String,
    val foodName: String,
    val score: Int,
    val scorePercent: Int,
    val difficulty: String = "NORMAL",
    val level: Int = 1,
    val playTimeSeconds: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)
```

`domain/model/Achievement.kt`:
```kotlin
package com.eatif.app.domain.model

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val category: AchievementCategory,
    val condition: AchievementCondition,
    val xpReward: Int,
    val unlockSkinId: String? = null
)

enum class AchievementCategory {
    MILESTONE,
    STREAK,
    SKILL,
    EXPLORATION
}

sealed class AchievementCondition {
    data class TotalGames(val count: Int) : AchievementCondition()
    data class GameHighScore(val gameId: String, val score: Int) : AchievementCondition()
    data class ConsecutiveDays(val days: Int) : AchievementCondition()
    data class PlayAllGames(val count: Int) : AchievementCondition()
    data class TotalPlayTime(val seconds: Long) : AchievementCondition()
}
```

`domain/model/AchievementProgress.kt`:
```kotlin
package com.eatif.app.domain.model

data class AchievementProgress(
    val achievementId: String,
    val currentProgress: Int = 0,
    val requiredProgress: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)
```

`domain/model/GameLevel.kt`:
```kotlin
package com.eatif.app.domain.model

import com.eatif.app.domain.model.GameDifficulty

data class GameLevel(
    val gameId: String,
    val levelNumber: Int,
    val difficulty: GameDifficulty,
    val requiredStars: Int = 0,
    val params: Map<String, String> = emptyMap()
)
```

`domain/model/LevelProgress.kt`:
```kotlin
package com.eatif.app.domain.model

data class LevelProgress(
    val gameId: String,
    val currentLevel: Int = 1,
    val stars: Map<Int, Int> = emptyMap(),
    val bestScores: Map<Int, Int> = emptyMap()
)
```

`domain/model/Skin.kt`:
```kotlin
package com.eatif.app.domain.model

data class Skin(
    val id: String,
    val name: String,
    val gameId: String,
    val rarity: SkinRarity,
    val unlockMethod: SkinUnlockMethod,
    val unlockRequirement: String = "",
    val isDefault: Boolean = false
)

enum class SkinRarity {
    COMMON, RARE, EPIC, LEGENDARY
}

enum class SkinUnlockMethod {
    ACHIEVEMENT, LEVEL, DEFAULT
}
```

`domain/model/SkinCollection.kt`:
```kotlin
package com.eatif.app.domain.model

data class SkinCollection(
    val skinId: String,
    val gameId: String,
    val isUnlocked: Boolean = false,
    val isActive: Boolean = false
)
```

- [ ] **Step 2: Create Room Entities**

Create all 5 entity files in `data/local/`:

`data/local/PlayerProfileEntity.kt`:
```kotlin
package com.eatif.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_profile")
data class PlayerProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val totalGamesPlayed: Int = 0,
    val totalPlayTimeSeconds: Long = 0,
    val currentStreak: Int = 0,
    val maxStreak: Int = 0,
    val playerLevel: Int = 1,
    val playerXP: Int = 0,
    val lastPlayedDate: String = ""
)

fun PlayerProfileEntity.toDomain() = com.eatif.app.domain.model.PlayerProfile(
    id = id,
    totalGamesPlayed = totalGamesPlayed,
    totalPlayTimeSeconds = totalPlayTimeSeconds,
    currentStreak = currentStreak,
    maxStreak = maxStreak,
    playerLevel = playerLevel,
    playerXP = playerXP,
    lastPlayedDate = lastPlayedDate
)

fun com.eatif.app.domain.model.PlayerProfile.toEntity() = PlayerProfileEntity(
    id = id,
    totalGamesPlayed = totalGamesPlayed,
    totalPlayTimeSeconds = totalPlayTimeSeconds,
    currentStreak = currentStreak,
    maxStreak = maxStreak,
    playerLevel = playerLevel,
    playerXP = playerXP,
    lastPlayedDate = lastPlayedDate
)
```

`data/local/GameStatsEntity.kt`:
```kotlin
package com.eatif.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_stats",
    indices = [
        Index(value = ["game_id"]),
        Index(value = ["timestamp"])
    ]
)
data class GameStatsEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val game_id: String,
    val food_name: String,
    val score: Int,
    val score_percent: Int,
    val difficulty: String = "NORMAL",
    val level: Int = 1,
    val play_time_seconds: Long = 0,
    val timestamp: Long = System.currentTimeMillis()
)

fun GameStatsEntity.toDomain() = com.eatif.app.domain.model.GameStats(
    id = id,
    gameId = game_id,
    foodName = food_name,
    score = score,
    scorePercent = score_percent,
    difficulty = difficulty,
    level = level,
    playTimeSeconds = play_time_seconds,
    timestamp = timestamp
)

fun com.eatif.app.domain.model.GameStats.toEntity() = GameStatsEntity(
    id = id,
    game_id = gameId,
    food_name = foodName,
    score = score,
    score_percent = scorePercent,
    difficulty = difficulty,
    level = level,
    play_time_seconds = playTimeSeconds,
    timestamp = timestamp
)
```

`data/local/AchievementProgressEntity.kt`:
```kotlin
package com.eatif.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "achievement_progress")
data class AchievementProgressEntity(
    @PrimaryKey val achievement_id: String,
    val current_progress: Int = 0,
    val required_progress: Int,
    val is_unlocked: Boolean = false,
    val unlocked_at: Long? = null
)

fun AchievementProgressEntity.toDomain() = com.eatif.app.domain.model.AchievementProgress(
    achievementId = achievement_id,
    currentProgress = current_progress,
    requiredProgress = required_progress,
    isUnlocked = is_unlocked,
    unlockedAt = unlocked_at
)

fun com.eatif.app.domain.model.AchievementProgress.toEntity() = AchievementProgressEntity(
    achievement_id = achievementId,
    current_progress = currentProgress,
    required_progress = requiredProgress,
    is_unlocked = isUnlocked,
    unlocked_at = unlockedAt
)
```

`data/local/LevelProgressEntity.kt`:
```kotlin
package com.eatif.app.data.local

import androidx.room.Entity

@Entity(tableName = "level_progress", primaryKeys = ["game_id"])
data class LevelProgressEntity(
    val game_id: String,
    val current_level: Int = 1,
    val stars_json: String = "{}",
    val best_scores_json: String = "{}"
)

fun LevelProgressEntity.toDomain() = com.eatif.app.domain.model.LevelProgress(
    gameId = game_id,
    currentLevel = current_level,
    stars = parseMap(stars_json),
    bestScores = parseMap(best_scores_json)
)

fun com.eatif.app.domain.model.LevelProgress.toEntity() = LevelProgressEntity(
    game_id = gameId,
    current_level = currentLevel,
    stars_json = mapToString(stars),
    best_scores_json = mapToString(bestScores)
)

private fun parseMap(json: String): Map<Int, Int> {
    return try {
        val gson = com.google.gson.Gson()
        val type = object : com.google.gson.reflect.TypeToken<Map<Int, Int>>() {}.type
        gson.fromJson(json, type) ?: emptyMap()
    } catch (e: Exception) {
        emptyMap()
    }
}

private fun mapToString(map: Map<Int, Int>): String {
    return com.google.gson.Gson().toJson(map)
}
```

`data/local/SkinCollectionEntity.kt`:
```kotlin
package com.eatif.app.data.local

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "skin_collection",
    indices = [Index(value = ["game_id"])]
)
data class SkinCollectionEntity(
    val skin_id: String,
    val game_id: String,
    val is_unlocked: Boolean = false,
    val is_active: Boolean = false
)

fun SkinCollectionEntity.toDomain() = com.eatif.app.domain.model.SkinCollection(
    skinId = skin_id,
    gameId = game_id,
    isUnlocked = is_unlocked,
    isActive = is_active
)

fun com.eatif.app.domain.model.SkinCollection.toEntity() = SkinCollectionEntity(
    skin_id = skinId,
    game_id = gameId,
    is_unlocked = isUnlocked,
    is_active = isActive
)
```

- [ ] **Step 3: Create TypeConverters**

Create `data/local/ProgressTypeConverters.kt`:
```kotlin
package com.eatif.app.data.local

import androidx.room.TypeConverter
import com.eatif.app.domain.model.AchievementCategory
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ProgressTypeConverters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringMap(map: Map<Int, Int>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toStringMap(json: String): Map<Int, Int> {
        val type = object : TypeToken<Map<Int, Int>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromStringStringMap(map: Map<String, String>): String {
        return gson.toJson(map)
    }

    @TypeConverter
    fun toStringStringMap(json: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return gson.fromJson(json, type) ?: emptyMap()
    }

    @TypeConverter
    fun fromAchievementCategory(category: AchievementCategory): String {
        return category.name
    }

    @TypeConverter
    fun toAchievementCategory(name: String): AchievementCategory {
        return try {
            AchievementCategory.valueOf(name)
        } catch (e: Exception) {
            AchievementCategory.MILESTONE
        }
    }
}
```

- [ ] **Step 4: Create DAOs**

Create all 5 DAO files in `data/local/`:

`data/local/PlayerProfileDao.kt`:
```kotlin
package com.eatif.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProfileDao {
    @Query("SELECT * FROM player_profile LIMIT 1")
    fun getProfile(): Flow<PlayerProfileEntity?>

    @Query("SELECT * FROM player_profile LIMIT 1")
    suspend fun getProfileOnce(): PlayerProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: PlayerProfileEntity)

    @Update
    suspend fun update(profile: PlayerProfileEntity)

    suspend fun getOrCreate(): PlayerProfileEntity {
        val existing = getProfileOnce()
        return existing ?: PlayerProfileEntity().also { insert(it) }
    }
}
```

`data/local/GameStatsDao.kt`:
```kotlin
package com.eatif.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GameStatsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(stats: GameStatsEntity)

    @Query("SELECT * FROM game_stats WHERE game_id = :gameId ORDER BY score DESC LIMIT 5")
    fun getTopScoresForGame(gameId: String): Flow<List<GameStatsEntity>>

    @Query("SELECT COUNT(*) FROM game_stats")
    fun getTotalGamesCount(): Flow<Int>

    @Query("SELECT SUM(play_time_seconds) FROM game_stats")
    fun getTotalPlayTime(): Flow<Long?>

    @Query("SELECT * FROM game_stats ORDER BY score_percent DESC LIMIT 10")
    fun getGlobalTopScores(): Flow<List<GameStatsEntity>>

    @Query("SELECT * FROM game_stats WHERE game_id = :gameId ORDER BY score_percent DESC LIMIT 1")
    fun getBestScoreForGame(gameId: String): Flow<GameStatsEntity?>

    @Query("SELECT DISTINCT game_id FROM game_stats")
    fun getUniqueGamesPlayed(): Flow<List<String>>

    @Query("SELECT * FROM game_stats WHERE timestamp >= :fromTs ORDER BY timestamp DESC")
    fun getRecentGames(fromTs: Long): Flow<List<GameStatsEntity>>

    @Query("SELECT * FROM game_stats WHERE game_id = :gameId ORDER BY timestamp DESC LIMIT 50")
    fun getHistoryForGame(gameId: String): Flow<List<GameStatsEntity>>

    @Query("SELECT COUNT(*) FROM game_stats")
    suspend fun getCount(): Int
}
```

`data/local/AchievementProgressDao.kt`:
```kotlin
package com.eatif.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementProgressDao {
    @Query("SELECT * FROM achievement_progress")
    fun getAllProgress(): Flow<List<AchievementProgressEntity>>

    @Query("SELECT * FROM achievement_progress WHERE achievement_id = :id")
    fun getProgress(id: String): Flow<AchievementProgressEntity?>

    @Query("SELECT achievement_id FROM achievement_progress WHERE is_unlocked = 1")
    fun getUnlockedIds(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM achievement_progress WHERE is_unlocked = 1")
    fun getUnlockedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: AchievementProgressEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(progress: List<AchievementProgressEntity>)
}
```

`data/local/LevelProgressDao.kt`:
```kotlin
package com.eatif.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress WHERE game_id = :gameId")
    fun getProgress(gameId: String): Flow<LevelProgressEntity?>

    @Query("SELECT * FROM level_progress WHERE game_id = :gameId")
    suspend fun getProgressOnce(gameId: String): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: LevelProgressEntity)

    @Query("SELECT * FROM level_progress")
    fun getAllProgress(): Flow<List<LevelProgressEntity>>
}
```

`data/local/SkinCollectionDao.kt`:
```kotlin
package com.eatif.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface SkinCollectionDao {
    @Query("SELECT * FROM skin_collection WHERE skin_id = :skinId")
    fun getSkin(skinId: String): Flow<SkinCollectionEntity?>

    @Query("SELECT * FROM skin_collection WHERE game_id = :gameId")
    fun getSkinsForGame(gameId: String): Flow<List<SkinCollectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(skin: SkinCollectionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(skins: List<SkinCollectionEntity>)

    @Query("UPDATE skin_collection SET is_active = 0 WHERE game_id = :gameId")
    suspend fun deactivateAllForGame(gameId: String)

    @Query("UPDATE skin_collection SET is_active = 1 WHERE skin_id = :skinId")
    suspend fun activateSkin(skinId: String)
}
```

- [ ] **Step 5: Update FoodDatabase to v3**

Modify `data/local/FoodDatabase.kt`:
```kotlin
package com.eatif.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        FoodEntity::class,
        HistoryEntity::class,
        PlayerProfileEntity::class,
        GameStatsEntity::class,
        AchievementProgressEntity::class,
        LevelProgressEntity::class,
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
```

- [ ] **Step 6: Update DatabaseModule with migration and new DAO providers**

Modify `di/DatabaseModule.kt` — add the Migration 2→3 and new DAO/Repository providers.

Add the migration inside `provideFoodDatabase`:
```kotlin
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE player_profile (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                total_games INTEGER NOT NULL DEFAULT 0,
                total_play_time INTEGER NOT NULL DEFAULT 0,
                current_streak INTEGER NOT NULL DEFAULT 0,
                max_streak INTEGER NOT NULL DEFAULT 0,
                player_level INTEGER NOT NULL DEFAULT 1,
                player_xp INTEGER NOT NULL DEFAULT 0,
                last_played_date TEXT NOT NULL DEFAULT ''
            )
        """)
        db.execSQL("""
            CREATE TABLE game_stats (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                game_id TEXT NOT NULL,
                food_name TEXT NOT NULL,
                score INTEGER NOT NULL,
                score_percent INTEGER NOT NULL,
                difficulty TEXT NOT NULL DEFAULT 'NORMAL',
                level INTEGER NOT NULL DEFAULT 1,
                play_time_seconds INTEGER NOT NULL DEFAULT 0,
                timestamp INTEGER NOT NULL
            )
        """)
        db.execSQL("CREATE INDEX idx_game_stats_game_id ON game_stats(game_id)")
        db.execSQL("CREATE INDEX idx_game_stats_timestamp ON game_stats(timestamp)")
        db.execSQL("""
            CREATE TABLE achievement_progress (
                achievement_id TEXT PRIMARY KEY,
                current_progress INTEGER NOT NULL DEFAULT 0,
                required_progress INTEGER NOT NULL,
                is_unlocked INTEGER NOT NULL DEFAULT 0,
                unlocked_at INTEGER
            )
        """)
        db.execSQL("""
            CREATE TABLE level_progress (
                game_id TEXT NOT NULL,
                current_level INTEGER NOT NULL DEFAULT 1,
                stars_json TEXT NOT NULL DEFAULT '{}',
                best_scores_json TEXT NOT NULL DEFAULT '{}',
                PRIMARY KEY (game_id)
            )
        """)
        db.execSQL("""
            CREATE TABLE skin_collection (
                skin_id TEXT NOT NULL,
                game_id TEXT NOT NULL,
                is_unlocked INTEGER NOT NULL DEFAULT 0,
                is_active INTEGER NOT NULL DEFAULT 0,
                PRIMARY KEY (skin_id)
            )
        """)
        db.execSQL("CREATE INDEX idx_skin_collection_game_id ON skin_collection(game_id)")
    }
}
```

Add to `.addMigrations()`: `.addMigrations(MIGRATION_1_2, MIGRATION_2_3)`

Add new DAO providers after `provideHistoryDao`:
```kotlin
@Provides
@Singleton
fun providePlayerProfileDao(database: FoodDatabase): PlayerProfileDao = database.playerProfileDao()

@Provides
@Singleton
fun provideGameStatsDao(database: FoodDatabase): GameStatsDao = database.gameStatsDao()

@Provides
@Singleton
fun provideAchievementProgressDao(database: FoodDatabase): AchievementProgressDao =
    database.achievementProgressDao()

@Provides
@Singleton
fun provideLevelProgressDao(database: FoodDatabase): LevelProgressDao =
    database.levelProgressDao()

@Provides
@Singleton
fun provideSkinCollectionDao(database: FoodDatabase): SkinCollectionDao =
    database.skinCollectionDao()
```

- [ ] **Step 7: Build to verify compilation**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 8: Commit**

```bash
git add domain/model/PlayerProfile.kt domain/model/GameStats.kt domain/model/Achievement.kt domain/model/AchievementProgress.kt domain/model/GameLevel.kt domain/model/LevelProgress.kt domain/model/Skin.kt domain/model/SkinCollection.kt
git add data/local/PlayerProfileEntity.kt data/local/GameStatsEntity.kt data/local/AchievementProgressEntity.kt data/local/LevelProgressEntity.kt data/local/SkinCollectionEntity.kt data/local/ProgressTypeConverters.kt
git add data/local/PlayerProfileDao.kt data/local/GameStatsDao.kt data/local/AchievementProgressDao.kt data/local/LevelProgressDao.kt data/local/SkinCollectionDao.kt
git add data/local/FoodDatabase.kt di/DatabaseModule.kt
git commit -m "feat: add game depth data layer with Room migration v2→v3"
```

---

### Task 2: Achievement Engine

**Goal:** Create achievement registry (~15 achievements) and engine that detects and unlocks achievements on game end.

**Files:**
- Create: `ui/settings/AchievementSettingsManager.kt`
- Create: `domain/usecase/AchievementRegistry.kt`
- Create: `domain/usecase/AchievementEngine.kt`

- [ ] **Step 1: Create AchievementSettingsManager**

Create `ui/settings/AchievementSettingsManager.kt`:
```kotlin
package com.eatif.app.ui.settings

import android.content.Context
import android.content.SharedPreferences

object AchievementSettingsManager {
    private const val PREFS_NAME = "eat_if_achievements"
    private const val KEY_UNLOCKED = "unlocked_ids"
    private const val KEY_LAST_PLAYED = "last_played_date"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getUnlockedIds(): Set<String> {
        return prefs.getStringSet(KEY_UNLOCKED, emptySet()) ?: emptySet()
    }

    fun markUnlocked(id: String) {
        val current = getUnlockedIds().toMutableSet()
        current.add(id)
        prefs.edit().putStringSet(KEY_UNLOCKED, current).apply()
    }

    fun isUnlocked(id: String): Boolean = getUnlockedIds().contains(id)

    fun setLastPlayedDate(date: String) {
        prefs.edit().putString(KEY_LAST_PLAYED, date).apply()
    }

    fun getLastPlayedDate(): String {
        return prefs.getString(KEY_LAST_PLAYED, "") ?: ""
    }
}
```

- [ ] **Step 2: Create AchievementRegistry**

Create `domain/usecase/AchievementRegistry.kt`:
```kotlin
package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Achievement
import com.eatif.app.domain.model.AchievementCategory
import com.eatif.app.domain.model.AchievementCondition

object AchievementRegistry {
    val all: List<Achievement> = listOf(
        Achievement(
            id = "first_game",
            name = "初次尝试",
            description = "完成你的第一局游戏",
            icon = "🎮",
            category = AchievementCategory.MILESTONE,
            condition = AchievementCondition.TotalGames(1),
            xpReward = 10
        ),
        Achievement(
            id = "ten_games",
            name = "十局达人",
            description = "完成 10 局游戏",
            icon = "🔟",
            category = AchievementCategory.MILESTONE,
            condition = AchievementCondition.TotalGames(10),
            xpReward = 20
        ),
        Achievement(
            id = "fifty_games",
            name = "游戏狂热",
            description = "完成 50 局游戏",
            icon = "🔥",
            category = AchievementCategory.MILESTONE,
            condition = AchievementCondition.TotalGames(50),
            xpReward = 40
        ),
        Achievement(
            id = "hundred_games",
            name = "百战成神",
            description = "完成 100 局游戏",
            icon = "⚡",
            category = AchievementCategory.MILESTONE,
            condition = AchievementCondition.TotalGames(100),
            xpReward = 100
        ),
        Achievement(
            id = "streak_7",
            name = "一周打卡",
            description = "连续 7 天游玩",
            icon = "📅",
            category = AchievementCategory.STREAK,
            condition = AchievementCondition.ConsecutiveDays(7),
            xpReward = 100
        ),
        Achievement(
            id = "streak_30",
            name = "月度达人",
            description = "连续 30 天游玩",
            icon = "🗓️",
            category = AchievementCategory.STREAK,
            condition = AchievementCondition.ConsecutiveDays(30),
            xpReward = 200,
            unlockSkinId = "flappy_golden"
        ),
        Achievement(
            id = "first_win",
            name = "初次胜利",
            description = "以超过 70% 的得分完成游戏",
            icon = "🏆",
            category = AchievementCategory.SKILL,
            condition = AchievementCondition.GameHighScore(gameId = "any", score = 70),
            xpReward = 50,
            unlockSkinId = "snake_neon"
        ),
        Achievement(
            id = "snake_master",
            name = "蛇王",
            description = "贪吃蛇单局得分超过 50",
            icon = "🐍",
            category = AchievementCategory.SKILL,
            condition = AchievementCondition.GameHighScore(gameId = "snake", score = 50),
            xpReward = 60
        ),
        Achievement(
            id = "two048_legend",
            name = "2048 传奇",
            description = "2048 达到 1024",
            icon = "🔢",
            category = AchievementCategory.SKILL,
            condition = AchievementCondition.GameHighScore(gameId = "game2048", score = 1024),
            xpReward = 100,
            unlockSkinId = "game2048_dark"
        ),
        Achievement(
            id = "perfect_shot",
            name = "神枪手",
            description = "打靶命中率超过 90%",
            icon = "🎯",
            category = AchievementCategory.SKILL,
            condition = AchievementCondition.GameHighScore(gameId = "shooting", score = 90),
            xpReward = 50
        ),
        Achievement(
            id = "slot_jackpot",
            name = "幸运大奖",
            description = "老虎机命中大奖",
            icon = "🎰",
            category = AchievementCategory.SKILL,
            condition = AchievementCondition.GameHighScore(gameId = "slot", score = 100),
            xpReward = 40
        ),
        Achievement(
            id = "marathon",
            name = "马拉松",
            description = "单局游戏超过 10 分钟",
            icon = "🏃",
            category = AchievementCategory.MILESTONE,
            condition = AchievementCondition.TotalPlayTime(600),
            xpReward = 70
        ),
        Achievement(
            id = "total_hour",
            name = "时光旅者",
            description = "累计游玩时间超过 1 小时",
            icon = "⏰",
            category = AchievementCategory.MILESTONE,
            condition = AchievementCondition.TotalPlayTime(3600),
            xpReward = 60
        ),
        Achievement(
            id = "five_different",
            name = "尝鲜玩家",
            description = "玩过 5 款不同的游戏",
            icon = "🌟",
            category = AchievementCategory.EXPLORATION,
            condition = AchievementCondition.PlayAllGames(5),
            xpReward = 30
        ),
        Achievement(
            id = "all_games",
            name = "全制霸",
            description = "玩过全部 15 款游戏",
            icon = "👑",
            category = AchievementCategory.EXPLORATION,
            condition = AchievementCondition.PlayAllGames(15),
            xpReward = 80,
            unlockSkinId = "spinwheel_gold"
        )
    )

    fun getById(id: String): Achievement? = all.find { it.id == id }
}
```

- [ ] **Step 3: Create AchievementEngine**

Create `domain/usecase/AchievementEngine.kt`:
```kotlin
package com.eatif.app.domain.usecase

import com.eatif.app.data.local.AchievementProgressDao
import com.eatif.app.data.local.GameStatsDao
import com.eatif.app.data.local.PlayerProfileDao
import com.eatif.app.data.local.toDomain
import com.eatif.app.domain.model.Achievement
import com.eatif.app.domain.model.AchievementCondition
import com.eatif.app.domain.model.AchievementProgress
import com.eatif.app.ui.settings.AchievementSettingsManager
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementEngine @Inject constructor(
    private val profileDao: PlayerProfileDao,
    private val statsDao: GameStatsDao,
    private val progressDao: AchievementProgressDao
) {
    data class GameEndEvent(
        val gameId: String,
        val score: Int,
        val scorePercent: Int,
        val playTimeSeconds: Long,
        val difficulty: String
    )

    suspend fun checkAndUnlock(event: GameEndEvent): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()
        val profile = profileDao.getOrCreate().toDomain()
        val unlockedIds = AchievementSettingsManager.getUnlockedIds()

        val totalGames = profile.totalGamesPlayed
        val totalPlayTime = profile.totalPlayTimeSeconds
        val currentStreak = profile.currentStreak
        val uniqueGames = statsDao.getUniqueGamesPlayed().first().size

        for (achievement in AchievementRegistry.all) {
            if (achievement.id in unlockedIds) continue

            val current = when (val condition = achievement.condition) {
                is AchievementCondition.TotalGames -> totalGames
                is AchievementCondition.GameHighScore -> {
                    if (condition.gameId == "any") {
                        val bestScore = statsDao.getBestScoreForGame(event.gameId).first()
                        if (bestScore != null && bestScore.score_percent >= condition.score) condition.score
                        else 0
                    } else {
                        val best = statsDao.getBestScoreForGame(condition.gameId).first()
                        if (best != null) best.score_percent else 0
                    }
                }
                is AchievementCondition.ConsecutiveDays -> currentStreak
                is AchievementCondition.PlayAllGames -> uniqueGames
                is AchievementCondition.TotalPlayTime -> totalPlayTime.toInt()
            }

            val required = getRequiredProgress(achievement.condition)

            val progress = AchievementProgress(
                achievementId = achievement.id,
                currentProgress = current,
                requiredProgress = required,
                isUnlocked = current >= required,
                unlockedAt = if (current >= required) System.currentTimeMillis() else null
            )
            progressDao.upsert(progress)

            if (current >= required && achievement.id !in unlockedIds) {
                AchievementSettingsManager.markUnlocked(achievement.id)
                newlyUnlocked.add(achievement)
            }
        }

        return newlyUnlocked
    }

    private fun getRequiredProgress(condition: AchievementCondition): Int = when (condition) {
        is AchievementCondition.TotalGames -> condition.count
        is AchievementCondition.GameHighScore -> condition.score
        is AchievementCondition.ConsecutiveDays -> condition.days
        is AchievementCondition.PlayAllGames -> condition.count
        is AchievementCondition.TotalPlayTime -> condition.seconds.toInt()
    }
}
```

- [ ] **Step 4: Create GameStatsRepository**

Create `domain/repository/GameStatsRepository.kt`:
```kotlin
package com.eatif.app.domain.repository

import com.eatif.app.domain.model.GameStats
import kotlinx.coroutines.flow.Flow

interface GameStatsRepository {
    suspend fun insert(stats: GameStats)
    fun getTopScoresForGame(gameId: String): Flow<List<GameStats>>
    fun getTotalGamesCount(): Flow<Int>
    fun getTotalPlayTime(): Flow<Long?>
    fun getGlobalTopScores(): Flow<List<GameStats>>
    fun getBestScoreForGame(gameId: String): Flow<GameStats?>
    fun getUniqueGamesPlayed(): Flow<List<String>>
    fun getRecentGames(fromTs: Long = 0): Flow<List<GameStats>>
    fun getHistoryForGame(gameId: String): Flow<List<GameStats>>
}
```

Create `data/repository/GameStatsRepositoryImpl.kt`:
```kotlin
package com.eatif.app.data.repository

import com.eatif.app.data.local.GameStatsDao
import com.eatif.app.data.local.toDomain
import com.eatif.app.data.local.toEntity
import com.eatif.app.domain.model.GameStats
import com.eatif.app.domain.repository.GameStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GameStatsRepositoryImpl @Inject constructor(
    private val dao: GameStatsDao
) : GameStatsRepository {
    override suspend fun insert(stats: GameStats) {
        dao.insert(stats.toEntity())
    }

    override fun getTopScoresForGame(gameId: String): Flow<List<GameStats>> {
        return dao.getTopScoresForGame(gameId).map { it.map { e -> e.toDomain() } }
    }

    override fun getTotalGamesCount(): Flow<Int> = dao.getTotalGamesCount()

    override fun getTotalPlayTime(): Flow<Long?> = dao.getTotalPlayTime()

    override fun getGlobalTopScores(): Flow<List<GameStats>> {
        return dao.getGlobalTopScores().map { it.map { e -> e.toDomain() } }
    }

    override fun getBestScoreForGame(gameId: String): Flow<GameStats?> {
        return dao.getBestScoreForGame(gameId).map { it?.toDomain() }
    }

    override fun getUniqueGamesPlayed(): Flow<List<String>> = dao.getUniqueGamesPlayed()

    override fun getRecentGames(fromTs: Long): Flow<List<GameStats>> {
        return dao.getRecentGames(fromTs).map { it.map { e -> e.toDomain() } }
    }

    override fun getHistoryForGame(gameId: String): Flow<List<GameStats>> {
        return dao.getHistoryForGame(gameId).map { it.map { e -> e.toDomain() } }
    }
}
```

Add provider in `di/DatabaseModule.kt`:
```kotlin
import com.eatif.app.data.repository.GameStatsRepositoryImpl
import com.eatif.app.domain.repository.GameStatsRepository

@Provides
@Singleton
fun provideGameStatsRepository(dao: GameStatsDao): GameStatsRepository =
    GameStatsRepositoryImpl(dao)
```

- [ ] **Step 5: Update PlayViewModel to handle game end with XP/achievements**

Replace the entire `ui/screens/PlayViewModel.kt` with:
```kotlin
package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.Achievement
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.DefaultFoods
import com.eatif.app.domain.model.GameList
import com.eatif.app.domain.model.GameStats
import com.eatif.app.domain.repository.GameStatsRepository
import com.eatif.app.domain.usecase.AchievementEngine
import com.eatif.app.domain.usecase.PlayerProfileUseCase
import com.eatif.app.domain.usecase.GetAllFoodsUseCase
import com.eatif.app.domain.usecase.AddHistoryUseCase
import com.eatif.app.ui.settings.GameSettingsManager
import com.eatif.app.ui.settings.SkinSettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayViewModel @Inject constructor(
    private val getAllFoodsUseCase: GetAllFoodsUseCase,
    private val addHistoryUseCase: AddHistoryUseCase,
    private val achievementEngine: AchievementEngine,
    private val playerProfileUseCase: PlayerProfileUseCase,
    private val gameStatsRepository: GameStatsRepository
) : ViewModel() {

    private val defaultFoods = DefaultFoods.list

    private val _foods = MutableStateFlow<List<Food>>(defaultFoods)
    val foods: StateFlow<List<Food>> = _foods.asStateFlow()

    init {
        loadFoods()
    }

    fun loadFoods() {
        viewModelScope.launch {
            getAllFoodsUseCase().collect { repositoryFoods ->
                if (repositoryFoods.isNotEmpty()) {
                    _foods.value = repositoryFoods
                }
            }
        }
    }

    fun getGameName(gameId: String): String {
        return GameList.games.find { it.id == gameId }?.name ?: ""
    }

    fun processGameEnd(
        gameId: String,
        foodName: String,
        scorePercent: Int,
        playTimeSeconds: Long,
        onResult: (GameEndResult) -> Unit
    ) {
        viewModelScope.launch {
            // Record history (existing)
            val gameName = getGameName(gameId)
            addHistoryUseCase(foodName, gameName, scorePercent)

            // Record game stats
            val difficulty = GameSettingsManager.difficulty
            gameStatsRepository.insert(
                GameStats(
                    gameId = gameId,
                    foodName = foodName,
                    score = scorePercent,
                    scorePercent = scorePercent,
                    difficulty = difficulty.name,
                    playTimeSeconds = playTimeSeconds
                )
            )

            // Calculate XP
            val xpEarned = playerProfileUseCase.calculateXP(scorePercent, difficulty, playTimeSeconds)

            // Update player profile
            val profile = playerProfileUseCase.recordGameSession(xpEarned, playTimeSeconds)

            // Check achievements
            val event = AchievementEngine.GameEndEvent(
                gameId = gameId,
                score = scorePercent,
                scorePercent = scorePercent,
                playTimeSeconds = playTimeSeconds,
                difficulty = difficulty.name
            )
            val unlockedAchievements = achievementEngine.checkAndUnlock(event)

            // Unlock skins from achievement rewards
            unlockedAchievements.forEach { achievement ->
                achievement.unlockSkinId?.let { skinId ->
                    SkinSettingsManager.unlockSkin(skinId)
                }
            }

            onResult(
                GameEndResult(
                    xpEarned = xpEarned,
                    playerLevel = profile.playerLevel,
                    unlockedAchievements = unlockedAchievements
                )
            )
        }
    }

    data class GameEndResult(
        val xpEarned: Int,
        val playerLevel: Int,
        val unlockedAchievements: List<Achievement>
    )
}
```

- [ ] **Step 6: Update PlayScreen to use processGameEnd and add skin button**

Modify `ui/screens/PlayScreen.kt`:

1. Update the signature:
```kotlin
@Composable
fun PlayScreen(
    gameId: String,
    mode: String = "single",
    onGameEnd: (String, Int, PlayViewModel.GameEndResult) -> Unit,
    onBackClick: () -> Unit,
    onSkinsClick: (() -> Unit)? = null,
    viewModel: PlayViewModel = hiltViewModel()
) {
```

2. Replace the handleGameEnd lambda:
```kotlin
    val handleGameEnd: (String, Int) -> Unit = { foodName, scorePercent ->
        viewModel.processGameEnd(
            gameId = gameId,
            foodName = foodName,
            scorePercent = scorePercent,
            playTimeSeconds = 0,
            onResult = { result ->
                onGameEnd(foodName, scorePercent, result)
            }
        )
    }
```

3. Add skin button to pause overlay. Inside the `if (isPaused)` Column, add before the closing brace:
```kotlin
                    if (onSkinsClick != null) {
                        androidx.compose.material3.TextButton(onClick = { onSkinsClick() }) {
                            androidx.compose.material3.Text("🎨 皮肤", color = White)
                        }
                    }
```

- [ ] **Step 4: Build and verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add ui/settings/AchievementSettingsManager.kt
git add domain/usecase/AchievementRegistry.kt domain/usecase/AchievementEngine.kt
git add domain/repository/GameStatsRepository.kt data/repository/GameStatsRepositoryImpl.kt
git add di/DatabaseModule.kt
git commit -m "feat: add achievement engine with registry and game stats repository"
```

---

### Task 3: XP/Level System

**Goal:** Implement XP calculation, level formula, and PlayerProfileUseCase for recording game sessions.

**Files:**
- Create: `domain/usecase/PlayerProfileUseCase.kt`

- [ ] **Step 1: Create PlayerProfileUseCase**

Create `domain/usecase/PlayerProfileUseCase.kt`:
```kotlin
package com.eatif.app.domain.usecase

import com.eatif.app.data.local.GameStatsDao
import com.eatif.app.data.local.PlayerProfileDao
import com.eatif.app.data.local.toDomain
import com.eatif.app.data.local.toEntity
import com.eatif.app.domain.model.GameDifficulty
import com.eatif.app.domain.model.PlayerProfile
import com.eatif.app.ui.settings.AchievementSettingsManager
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlayerProfileUseCase @Inject constructor(
    private val profileDao: PlayerProfileDao
) {
    fun xpForLevel(level: Int): Int = 100 * level

    fun calculateXP(scorePercent: Int, difficulty: GameDifficulty, playTimeSeconds: Long): Int {
        val baseXP = (scorePercent * 0.5).toInt().coerceIn(0, 50)
        val difficultyMultiplier = when (difficulty) {
            GameDifficulty.EASY -> 1.0f
            GameDifficulty.NORMAL -> 1.5f
            GameDifficulty.HARD -> 2.0f
        }
        val timeBonus = (playTimeSeconds / 60).toInt().coerceAtMost(20)
        return (baseXP * difficultyMultiplier + timeBonus).toInt().coerceAtLeast(1)
    }

    suspend fun recordGameSession(
        xpEarned: Int,
        playTimeSeconds: Long
    ): PlayerProfile {
        val profile = profileDao.getOrCreate().toDomain()
        val today = LocalDate.now().toString()

        val newStreak = if (profile.lastPlayedDate == today) {
            profile.currentStreak
        } else if (profile.lastPlayedDate == LocalDate.now().minusDays(1).toString()) {
            profile.currentStreak + 1
        } else {
            1
        }

        val newXP = profile.playerXP + xpEarned
        var newLevel = profile.playerLevel
        var remainingXP = newXP
        var levelUpXP = xpForLevel(newLevel)

        while (remainingXP >= levelUpXP && newLevel < 50) {
            remainingXP -= levelUpXP
            newLevel++
            levelUpXP = xpForLevel(newLevel)
        }

        val updated = profile.copy(
            totalGamesPlayed = profile.totalGamesPlayed + 1,
            totalPlayTimeSeconds = profile.totalPlayTimeSeconds + playTimeSeconds,
            currentStreak = newStreak,
            maxStreak = maxOf(newStreak, profile.maxStreak),
            playerLevel = newLevel,
            playerXP = remainingXP,
            lastPlayedDate = today
        )
        profileDao.update(updated.toEntity())
        AchievementSettingsManager.setLastPlayedDate(today)
        return updated
    }

    suspend fun getProfile(): PlayerProfile {
        return profileDao.getOrCreate().toDomain()
    }

    fun xpNeededForNextLevel(profile: PlayerProfile): Int {
        return xpForLevel(profile.playerLevel) - profile.playerXP
    }

    fun xpProgressPercent(profile: PlayerProfile): Float {
        val needed = xpForLevel(profile.playerLevel)
        return if (needed > 0) (profile.playerXP.toFloat() / needed) * 100f else 0f
    }
}
```

- [ ] **Step 2: Build and verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add domain/usecase/PlayerProfileUseCase.kt
git commit -m "feat: add XP calculation and player profile use case"
```

---

### Task 4: Level System

**Goal:** Create game level registry, level manager, level select screen, and navigation.

**Files:**
- Create: `domain/usecase/GameLevelRegistry.kt`
- Create: `domain/usecase/LevelManager.kt`
- Create: `ui/screens/LevelSelectScreen.kt`
- Create: `ui/screens/LevelSelectViewModel.kt`

- [ ] **Step 1: Create GameLevelRegistry**

Create `domain/usecase/GameLevelRegistry.kt`:
```kotlin
package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.GameDifficulty
import com.eatif.app.domain.model.GameLevel

object GameLevelRegistry {
    private val levels: List<GameLevel> = listOf(
        // Snake - 10 levels
        GameLevel("snake", 1, GameDifficulty.EASY, 0, mapOf("speed" to "slow", "obstacles" to "0")),
        GameLevel("snake", 2, GameDifficulty.EASY, 1, mapOf("speed" to "slow", "obstacles" to "1")),
        GameLevel("snake", 3, GameDifficulty.EASY, 2, mapOf("speed" to "medium", "obstacles" to "2")),
        GameLevel("snake", 4, GameDifficulty.NORMAL, 3, mapOf("speed" to "medium", "obstacles" to "3")),
        GameLevel("snake", 5, GameDifficulty.NORMAL, 4, mapOf("speed" to "medium", "obstacles" to "4")),
        GameLevel("snake", 6, GameDifficulty.NORMAL, 5, mapOf("speed" to "fast", "obstacles" to "3")),
        GameLevel("snake", 7, GameDifficulty.HARD, 6, mapOf("speed" to "fast", "obstacles" to "5")),
        GameLevel("snake", 8, GameDifficulty.HARD, 7, mapOf("speed" to "fast", "obstacles" to "6")),
        GameLevel("snake", 9, GameDifficulty.HARD, 8, mapOf("speed" to "very_fast", "obstacles" to "5")),
        GameLevel("snake", 10, GameDifficulty.HARD, 9, mapOf("speed" to "very_fast", "obstacles" to "7")),

        // 2048 - 10 levels
        GameLevel("game2048", 1, GameDifficulty.EASY, 0, mapOf("target" to "256")),
        GameLevel("game2048", 2, GameDifficulty.EASY, 1, mapOf("target" to "512")),
        GameLevel("game2048", 3, GameDifficulty.NORMAL, 2, mapOf("target" to "1024")),
        GameLevel("game2048", 4, GameDifficulty.NORMAL, 3, mapOf("target" to "2048")),
        GameLevel("game2048", 5, GameDifficulty.NORMAL, 4, mapOf("target" to "2048")),
        GameLevel("game2048", 6, GameDifficulty.HARD, 5, mapOf("target" to "4096")),
        GameLevel("game2048", 7, GameDifficulty.HARD, 6, mapOf("target" to "4096")),
        GameLevel("game2048", 8, GameDifficulty.HARD, 7, mapOf("target" to "8192")),
        GameLevel("game2048", 9, GameDifficulty.HARD, 8, mapOf("target" to "8192")),
        GameLevel("game2048", 10, GameDifficulty.HARD, 9, mapOf("target" to "16384")),

        // Flappy - 10 levels
        GameLevel("flappy", 1, GameDifficulty.EASY, 0, mapOf("gap" to "wide", "speed" to "slow")),
        GameLevel("flappy", 2, GameDifficulty.EASY, 1, mapOf("gap" to "wide", "speed" to "slow")),
        GameLevel("flappy", 3, GameDifficulty.EASY, 2, mapOf("gap" to "medium", "speed" to "medium")),
        GameLevel("flappy", 4, GameDifficulty.NORMAL, 3, mapOf("gap" to "medium", "speed" to "medium")),
        GameLevel("flappy", 5, GameDifficulty.NORMAL, 4, mapOf("gap" to "medium", "speed" to "fast")),
        GameLevel("flappy", 6, GameDifficulty.NORMAL, 5, mapOf("gap" to "narrow", "speed" to "fast")),
        GameLevel("flappy", 7, GameDifficulty.HARD, 6, mapOf("gap" to "narrow", "speed" to "very_fast")),
        GameLevel("flappy", 8, GameDifficulty.HARD, 7, mapOf("gap" to "narrow", "speed" to "very_fast")),
        GameLevel("flappy", 9, GameDifficulty.HARD, 8, mapOf("gap" to "very_narrow", "speed" to "very_fast")),
        GameLevel("flappy", 10, GameDifficulty.HARD, 9, mapOf("gap" to "very_narrow", "speed" to "insane")),

        // Default levels for all other games (5 levels each)
        GameLevel("spinwheel", 1, GameDifficulty.EASY, 0, mapOf()),
        GameLevel("spinwheel", 2, GameDifficulty.EASY, 1, mapOf()),
        GameLevel("spinwheel", 3, GameDifficulty.NORMAL, 2, mapOf()),
        GameLevel("spinwheel", 4, GameDifficulty.HARD, 3, mapOf()),
        GameLevel("spinwheel", 5, GameDifficulty.HARD, 4, mapOf()),

        GameLevel("slot", 1, GameDifficulty.EASY, 0, mapOf()),
        GameLevel("slot", 2, GameDifficulty.EASY, 1, mapOf()),
        GameLevel("slot", 3, GameDifficulty.NORMAL, 2, mapOf()),
        GameLevel("slot", 4, GameDifficulty.HARD, 3, mapOf()),
        GameLevel("slot", 5, GameDifficulty.HARD, 4, mapOf()),

        GameLevel("shooting", 1, GameDifficulty.EASY, 0, mapOf()),
        GameLevel("shooting", 2, GameDifficulty.EASY, 1, mapOf()),
        GameLevel("shooting", 3, GameDifficulty.NORMAL, 2, mapOf()),
        GameLevel("shooting", 4, GameDifficulty.HARD, 3, mapOf()),
        GameLevel("shooting", 5, GameDifficulty.HARD, 4, mapOf()),

        GameLevel("minesweeper", 1, GameDifficulty.EASY, 0, mapOf()),
        GameLevel("minesweeper", 2, GameDifficulty.EASY, 1, mapOf()),
        GameLevel("minesweeper", 3, GameDifficulty.NORMAL, 2, mapOf()),
        GameLevel("minesweeper", 4, GameDifficulty.HARD, 3, mapOf()),
        GameLevel("minesweeper", 5, GameDifficulty.HARD, 4, mapOf()),

        GameLevel("tetris", 1, GameDifficulty.EASY, 0, mapOf()),
        GameLevel("tetris", 2, GameDifficulty.EASY, 1, mapOf()),
        GameLevel("tetris", 3, GameDifficulty.NORMAL, 2, mapOf()),
        GameLevel("tetris", 4, GameDifficulty.HARD, 3, mapOf()),
        GameLevel("tetris", 5, GameDifficulty.HARD, 4, mapOf()),

        GameLevel("rps", 1, GameDifficulty.EASY, 0, mapOf()),
        GameLevel("rps", 2, GameDifficulty.EASY, 1, mapOf()),
        GameLevel("rps", 3, GameDifficulty.NORMAL, 2, mapOf()),
        GameLevel("rps", 4, GameDifficulty.HARD, 3, mapOf()),
        GameLevel("rps", 5, GameDifficulty.HARD, 4, mapOf()),

        GameLevel("jump", 1, GameDifficulty.EASY, 0, mapOf()),
        GameLevel("jump", 2, GameDifficulty.EASY, 1, mapOf()),
        GameLevel("jump", 3, GameDifficulty.NORMAL, 2, mapOf()),
        GameLevel("jump", 4, GameDifficulty.HARD, 3, mapOf()),
        GameLevel("jump", 5, GameDifficulty.HARD, 4, mapOf()),

        GameLevel("runner", 1, GameDifficulty.EASY, 0, mapOf()),
        GameLevel("runner", 2, GameDifficulty.EASY, 1, mapOf()),
        GameLevel("runner", 3, GameDifficulty.NORMAL, 2, mapOf()),
        GameLevel("runner", 4, GameDifficulty.HARD, 3, mapOf()),
        GameLevel("runner", 5, GameDifficulty.HARD, 4, mapOf()),

        GameLevel("climb100", 1, GameDifficulty.EASY, 0, mapOf()),
        GameLevel("climb100", 2, GameDifficulty.EASY, 1, mapOf()),
        GameLevel("climb100", 3, GameDifficulty.NORMAL, 2, mapOf()),
        GameLevel("climb100", 4, GameDifficulty.HARD, 3, mapOf()),
        GameLevel("climb100", 5, GameDifficulty.HARD, 4, mapOf()),

        GameLevel("needle", 1, GameDifficulty.EASY, 0, mapOf()),
        GameLevel("needle", 2, GameDifficulty.EASY, 1, mapOf()),
        GameLevel("needle", 3, GameDifficulty.NORMAL, 2, mapOf()),
        GameLevel("needle", 4, GameDifficulty.HARD, 3, mapOf()),
        GameLevel("needle", 5, GameDifficulty.HARD, 4, mapOf()),

        GameLevel("onestroke", 1, GameDifficulty.EASY, 0, mapOf()),
        GameLevel("onestroke", 2, GameDifficulty.EASY, 1, mapOf()),
        GameLevel("onestroke", 3, GameDifficulty.NORMAL, 2, mapOf()),
        GameLevel("onestroke", 4, GameDifficulty.HARD, 3, mapOf()),
        GameLevel("onestroke", 5, GameDifficulty.HARD, 4, mapOf()),

        GameLevel("boxpusher", 1, GameDifficulty.EASY, 0, mapOf()),
        GameLevel("boxpusher", 2, GameDifficulty.EASY, 1, mapOf()),
        GameLevel("boxpusher", 3, GameDifficulty.NORMAL, 2, mapOf()),
        GameLevel("boxpusher", 4, GameDifficulty.HARD, 3, mapOf()),
        GameLevel("boxpusher", 5, GameDifficulty.HARD, 4, mapOf())
    )

    fun getLevelsForGame(gameId: String): List<GameLevel> =
        levels.filter { it.gameId == gameId }

    fun getLevel(gameId: String, levelNumber: Int): GameLevel? =
        levels.find { it.gameId == gameId && it.levelNumber == levelNumber }

    fun getMaxLevelForGame(gameId: String): Int =
        levels.filter { it.gameId == gameId }.maxOfOrNull { it.levelNumber } ?: 1
}
```

- [ ] **Step 2: Create LevelManager**

Create `domain/usecase/LevelManager.kt`:
```kotlin
package com.eatif.app.domain.usecase

import com.eatif.app.data.local.LevelProgressDao
import com.eatif.app.data.local.toDomain
import com.eatif.app.data.local.toEntity
import com.eatif.app.domain.model.LevelProgress
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LevelManager @Inject constructor(
    private val levelProgressDao: LevelProgressDao
) {
    suspend fun getProgress(gameId: String): LevelProgress {
        val existing = levelProgressDao.getProgressOnce(gameId)
        return existing?.toDomain() ?: LevelProgress(gameId = gameId)
    }

    suspend fun isLevelUnlocked(gameId: String, levelNumber: Int): Boolean {
        if (levelNumber <= 1) return true
        val progress = getProgress(gameId)
        val prevLevel = levelNumber - 1
        val prevStars = progress.stars[prevLevel] ?: 0
        val requiredLevel = GameLevelRegistry.getLevel(gameId, levelNumber)
        return prevStars >= (requiredLevel?.requiredStars ?: 0)
    }

    suspend fun recordLevelResult(
        gameId: String,
        levelNumber: Int,
        scorePercent: Int
    ): LevelProgress {
        val progress = getProgress(gameId)
        val stars = when {
            scorePercent <= 0 -> 0
            scorePercent >= 90 -> 3
            scorePercent >= 70 -> 2
            else -> 1
        }

        val currentStars = progress.stars.toMutableMap()
        val prevStars = currentStars[levelNumber] ?: 0
        if (stars > prevStars) {
            currentStars[levelNumber] = stars
        }

        val bestScores = progress.bestScores.toMutableMap()
        val prevBest = bestScores[levelNumber] ?: 0
        if (scorePercent > prevBest) {
            bestScores[levelNumber] = scorePercent
        }

        // Unlock next level if stars are enough
        val maxLevel = GameLevelRegistry.getMaxLevelForGame(gameId)
        var newCurrentLevel = progress.currentLevel
        for (lvl in 1..maxLevel) {
            val level = GameLevelRegistry.getLevel(gameId, lvl) ?: continue
            val reqStars = level.requiredStars
            val earnedStars = currentStars[lvl - 1] ?: 0
            if (lvl > 1 && earnedStars >= reqStars && lvl > newCurrentLevel) {
                newCurrentLevel = lvl
            }
        }

        val updated = progress.copy(
            currentLevel = newCurrentLevel,
            stars = currentStars,
            bestScores = bestScores
        )
        levelProgressDao.upsert(updated.toEntity())
        return updated
    }

    fun calculateStars(scorePercent: Int): Int = when {
        scorePercent <= 0 -> 0
        scorePercent >= 90 -> 3
        scorePercent >= 70 -> 2
        else -> 1
    }
}
```

- [ ] **Step 3: Create LevelSelectViewModel**

Create `ui/screens/LevelSelectViewModel.kt`:
```kotlin
package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.GameLevel
import com.eatif.app.domain.model.LevelProgress
import com.eatif.app.domain.usecase.GameLevelRegistry
import com.eatif.app.domain.usecase.LevelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LevelSelectViewModel @Inject constructor(
    private val levelManager: LevelManager
) : ViewModel() {
    private val _levels = MutableStateFlow<List<GameLevel>>(emptyList())
    val levels: StateFlow<List<GameLevel>> = _levels.asStateFlow()

    private val _progress = MutableStateFlow<LevelProgress?>(null)
    val progress: StateFlow<LevelProgress?> = _progress.asStateFlow()

    private val _unlockedMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val unlockedMap: StateFlow<Map<Int, Boolean>> = _unlockedMap.asStateFlow()

    fun loadLevels(gameId: String) {
        viewModelScope.launch {
            val gameLevels = GameLevelRegistry.getLevelsForGame(gameId)
            _levels.value = gameLevels

            val prog = levelManager.getProgress(gameId)
            _progress.value = prog

            val unlocked = gameLevels.associate { level ->
                level.levelNumber to levelManager.isLevelUnlocked(gameId, level.levelNumber)
            }
            _unlockedMap.value = unlocked
        }
    }
}
```

- [ ] **Step 4: Create LevelSelectScreen**

Create `ui/screens/LevelSelectScreen.kt`:
```kotlin
package com.eatif.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectScreen(
    gameId: String,
    gameName: String,
    onLevelSelected: (levelNumber: Int) -> Unit,
    onBackClick: () -> Unit,
    viewModel: LevelSelectViewModel = hiltViewModel()
) {
    val levels by viewModel.levels.collectAsState()
    val unlockedMap by viewModel.unlockedMap.collectAsState()

    LaunchedEffect(gameId) {
        viewModel.loadLevels(gameId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$gameName - 关卡") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(levels) { level ->
                val isUnlocked = unlockedMap[level.levelNumber] ?: false
                LevelCard(
                    levelNumber = level.levelNumber,
                    difficulty = level.difficulty.name,
                    stars = viewModel.progress.value?.stars?.get(level.levelNumber) ?: 0,
                    isUnlocked = isUnlocked,
                    onClick = { if (isUnlocked) onLevelSelected(level.levelNumber) }
                )
            }
        }
    }
}

import androidx.compose.foundation.layout.Spacer

@Composable
private fun LevelCard(
    levelNumber: Int,
    difficulty: String,
    stars: Int,
    isUnlocked: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isUnlocked) 4.dp else 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isUnlocked) {
                Text(
                    text = "关卡 $levelNumber",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = difficulty,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.padding(top = 4.dp))
                Text(
                    text = "⭐".repeat(stars) + "☆".repeat(3 - stars),
                    style = MaterialTheme.typography.bodyLarge
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "锁定",
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$levelNumber",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

- [ ] **Step 5: Build and verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add domain/usecase/GameLevelRegistry.kt domain/usecase/LevelManager.kt
git add ui/screens/LevelSelectViewModel.kt ui/screens/LevelSelectScreen.kt
git commit -m "feat: add level system with registry, manager, and select screen"
```

---

### Task 5: Skin System

**Goal:** Create skin registry, skin resolver, skin settings manager, and skin selector screen.

**Files:**
- Create: `ui/settings/SkinSettingsManager.kt`
- Create: `domain/usecase/SkinRegistry.kt`
- Create: `domain/usecase/SkinResolver.kt`
- Create: `ui/screens/SkinSelectorScreen.kt`

- [ ] **Step 1: Create SkinSettingsManager**

Create `ui/settings/SkinSettingsManager.kt`:
```kotlin
package com.eatif.app.ui.settings

import android.content.Context
import android.content.SharedPreferences

object SkinSettingsManager {
    private const val PREFS_NAME = "eat_if_skins"
    private const val KEY_ACTIVE_SKIN_PREFIX = "active_skin_"
    private const val KEY_UNLOCKED_PREFIX = "unlocked_skin_"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getActiveSkinId(gameId: String): String? {
        return prefs.getString(KEY_ACTIVE_SKIN_PREFIX + gameId, null)
    }

    fun setActiveSkinId(gameId: String, skinId: String) {
        prefs.edit().putString(KEY_ACTIVE_SKIN_PREFIX + gameId, skinId).apply()
    }

    fun isSkinUnlocked(skinId: String): Boolean {
        return prefs.getBoolean(KEY_UNLOCKED_PREFIX + skinId, false)
    }

    fun unlockSkin(skinId: String) {
        prefs.edit().putBoolean(KEY_UNLOCKED_PREFIX + skinId, true).apply()
    }

    fun unlockSkins(skinIds: List<String>) {
        val editor = prefs.edit()
        skinIds.forEach { id ->
            editor.putBoolean(KEY_UNLOCKED_PREFIX + id, true)
        }
        editor.apply()
    }
}
```

- [ ] **Step 2: Create SkinRegistry**

Create `domain/usecase/SkinRegistry.kt`:
```kotlin
package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Skin
import com.eatif.app.domain.model.SkinRarity
import com.eatif.app.domain.model.SkinUnlockMethod

object SkinRegistry {
    val all: List<Skin> = listOf(
        // Snake skins
        Skin("snake_default", "经典绿", "snake", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("snake_neon", "霓虹蛇", "snake", SkinRarity.RARE, SkinUnlockMethod.ACHIEVEMENT, "初次胜利"),
        Skin("snake_golden", "金蛇", "snake", SkinRarity.LEGENDARY, SkinUnlockMethod.LEVEL, "达到第10关"),

        // 2048 skins
        Skin("game2048_default", "经典", "game2048", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("game2048_dark", "暗黑", "game2048", SkinRarity.RARE, SkinUnlockMethod.ACHIEVEMENT, "2048 传奇"),

        // Flappy skins
        Skin("flappy_default", "经典", "flappy", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("flappy_golden", "金翼", "flappy", SkinRarity.LEGENDARY, SkinUnlockMethod.ACHIEVEMENT, "月度达人"),

        // SpinWheel skins
        Skin("spinwheel_default", "经典", "spinwheel", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),
        Skin("spinwheel_gold", "金轮", "spinwheel", SkinRarity.EPIC, SkinUnlockMethod.ACHIEVEMENT, "全制霸"),

        // Slot skins
        Skin("slot_default", "经典", "slot", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),

        // Shooting skins
        Skin("shooting_default", "经典", "shooting", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),

        // Minesweeper skins
        Skin("minesweeper_default", "经典", "minesweeper", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),

        // Tetris skins
        Skin("tetris_default", "经典", "tetris", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),

        // RPS skins
        Skin("rps_default", "经典", "rps", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),

        // Jump skins
        Skin("jump_default", "经典", "jump", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),

        // Runner skins
        Skin("runner_default", "经典", "runner", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),

        // Climb100 skins
        Skin("climb100_default", "经典", "climb100", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),

        // Needle skins
        Skin("needle_default", "经典", "needle", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),

        // OneStroke skins
        Skin("onestroke_default", "经典", "onestroke", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true),

        // BoxPusher skins
        Skin("boxpusher_default", "经典", "boxpusher", SkinRarity.COMMON, SkinUnlockMethod.DEFAULT, isDefault = true)
    )

    fun getSkinsForGame(gameId: String): List<Skin> =
        all.filter { it.gameId == gameId }

    fun getById(id: String): Skin? = all.find { it.id == id }

    fun getDefaultSkin(gameId: String): Skin =
        getSkinsForGame(gameId).find { it.isDefault } ?: getSkinsForGame(gameId).first()
}
```

- [ ] **Step 3: Create SkinResolver**

Create `domain/usecase/SkinResolver.kt`:
```kotlin
package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Skin
import com.eatif.app.domain.model.SkinCollection
import com.eatif.app.ui.settings.SkinSettingsManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkinResolver @Inject constructor() {
    fun getActiveSkin(gameId: String): Skin {
        val skins = SkinRegistry.getSkinsForGame(gameId)
        val activeSkinId = SkinSettingsManager.getActiveSkinId(gameId)

        if (activeSkinId != null) {
            val skin = skins.find { it.id == activeSkinId }
            if (skin != null && isUnlocked(skin.id)) {
                return skin
            }
        }

        return skins.find { it.isDefault } ?: skins.firstOrNull() ?: SkinRegistry.all.first()
    }

    fun getSkinCollection(gameId: String): List<SkinCollection> {
        return SkinRegistry.getSkinsForGame(gameId).map { skin ->
            SkinCollection(
                skinId = skin.id,
                gameId = skin.gameId,
                isUnlocked = isUnlocked(skin.id),
                isActive = SkinSettingsManager.getActiveSkinId(gameId) == skin.id && isUnlocked(skin.id)
            )
        }
    }

    fun setActiveSkin(gameId: String, skinId: String): Boolean {
        val skin = SkinRegistry.getById(skinId) ?: return false
        if (!isUnlocked(skinId)) return false
        SkinSettingsManager.setActiveSkinId(gameId, skinId)
        return true
    }

    fun unlockSkin(skinId: String) {
        SkinSettingsManager.unlockSkin(skinId)
    }

    private fun isUnlocked(skinId: String): Boolean {
        return SkinSettingsManager.isSkinUnlocked(skinId)
    }

    fun initializeDefaults() {
        SkinRegistry.all.filter { it.isDefault }.forEach { skin ->
            if (!isUnlocked(skin.id)) {
                unlockSkin(skin.id)
            }
        }
    }
}
```

- [ ] **Step 4: Create SkinSelectorScreen**

Create `ui/screens/SkinSelectorScreen.kt`:
```kotlin
package com.eatif.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Skin
import com.eatif.app.domain.model.SkinRarity
import com.eatif.app.domain.usecase.SkinRegistry
import com.eatif.app.domain.usecase.SkinResolver
import com.eatif.app.ui.settings.SkinSettingsManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkinSelectorScreen(
    gameId: String,
    gameName: String,
    onBackClick: () -> Unit,
    skinResolver: SkinResolver = remember { SkinResolver() }
) {
    val skins = remember(gameId) { SkinRegistry.getSkinsForGame(gameId) }
    val activeSkinId = SkinSettingsManager.getActiveSkinId(gameId)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$gameName - 皮肤") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(skins) { skin ->
                val isUnlocked = SkinSettingsManager.isSkinUnlocked(skin.id)
                val isActive = activeSkinId == skin.id && isUnlocked
                SkinCard(
                    skin = skin,
                    isUnlocked = isUnlocked,
                    isActive = isActive,
                    onClick = {
                        if (isUnlocked && !isActive) {
                            skinResolver.setActiveSkin(gameId, skin.id)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun SkinCard(
    skin: Skin,
    isUnlocked: Boolean,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val rarityColor = when (skin.rarity) {
        SkinRarity.COMMON -> MaterialTheme.colorScheme.primary
        SkinRarity.RARE -> Color(0xFF2196F3)
        SkinRarity.EPIC -> Color(0xFF9C27B0)
        SkinRarity.LEGENDARY -> Color(0xFFFFC107)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked, onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive)
                MaterialTheme.colorScheme.primaryContainer
            else if (isUnlocked)
                MaterialTheme.colorScheme.surfaceVariant
            else
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Rarity indicator
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(rarityColor.copy(alpha = if (isUnlocked) 0.3f else 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(
                        text = skin.name.first().toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        color = rarityColor
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "锁定",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                text = skin.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 8.dp)
            )

            Text(
                text = skin.rarity.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                color = rarityColor
            )

            if (!isUnlocked) {
                Text(
                    text = "解锁条件: ${skin.unlockRequirement}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            if (isActive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "已激活",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "使用中",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 5: Build and verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 6: Commit**

```bash
git add ui/settings/SkinSettingsManager.kt domain/usecase/SkinRegistry.kt domain/usecase/SkinResolver.kt
git add ui/screens/SkinSelectorScreen.kt
git commit -m "feat: add skin system with registry, resolver, and selector screen"
```

---

### Task 6: Stats/Profile UseCases

**Goal:** Create StatsUseCase for aggregation queries, ProfileScreen, AchievementScreen, StatsScreen, and their ViewModels.

**Files:**
- Create: `domain/usecase/StatsUseCase.kt`
- Create: `ui/screens/ProfileScreen.kt`
- Create: `ui/screens/ProfileViewModel.kt`
- Create: `ui/screens/AchievementScreen.kt`
- Create: `ui/screens/AchievementViewModel.kt`
- Create: `ui/screens/StatsScreen.kt`
- Create: `ui/screens/StatsViewModel.kt`
- Create: `ui/components/AchievementUnlockDialog.kt`
- Create: `ui/components/ProgressBar.kt`

- [ ] **Step 1: Create StatsUseCase**

Create `domain/usecase/StatsUseCase.kt`:
```kotlin
package com.eatif.app.domain.usecase

import com.eatif.app.data.local.GameStatsDao
import com.eatif.app.data.local.PlayerProfileDao
import com.eatif.app.data.local.AchievementProgressDao
import com.eatif.app.data.local.toDomain
import com.eatif.app.domain.model.GameStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class StatsOverview(
    val totalGames: Int,
    val totalPlayTimeSeconds: Long,
    val bestScore: Int,
    val gamesPlayed: Int,
    val achievementsUnlocked: Int,
    val totalAchievements: Int
)

data class GameStatsDetail(
    val gameId: String,
    val playCount: Int,
    val bestScore: Int,
    val bestScorePercent: Int,
    val avgScorePercent: Int
)

@Singleton
class StatsUseCase @Inject constructor(
    private val gameStatsDao: GameStatsDao,
    private val profileDao: PlayerProfileDao,
    private val achievementProgressDao: AchievementProgressDao
) {
    fun getStatsOverview(): Flow<StatsOverview> {
        return kotlinx.coroutines.flow.combine(
            gameStatsDao.getTotalGamesCount(),
            gameStatsDao.getTotalPlayTime(),
            gameStatsDao.getGlobalTopScores(),
            achievementProgressDao.getUnlockedCount()
        ) { totalGames, totalPlayTime, topScores, unlockedCount ->
            StatsOverview(
                totalGames = totalGames,
                totalPlayTimeSeconds = totalPlayTime ?: 0L,
                bestScore = topScores.firstOrNull()?.score_percent ?: 0,
                gamesPlayed = totalGames,
                achievementsUnlocked = unlockedCount,
                totalAchievements = AchievementRegistry.all.size
            )
        }
    }

    fun getTopScoresForGame(gameId: String): Flow<List<GameStats>> {
        return gameStatsDao.getTopScoresForGame(gameId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getGlobalTopScores(): Flow<List<GameStats>> {
        return gameStatsDao.getGlobalTopScores().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getGamesPlayed(): Flow<List<String>> {
        return gameStatsDao.getUniqueGamesPlayed()
    }

    fun getRecentGames(fromTs: Long = 0): Flow<List<GameStats>> {
        return gameStatsDao.getRecentGames(fromTs).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    fun getGameStatsDetail(gameId: String): Flow<GameStatsDetail?> {
        return kotlinx.coroutines.flow.combine(
            gameStatsDao.getHistoryForGame(gameId),
            gameStatsDao.getBestScoreForGame(gameId)
        ) { history, best ->
            if (history.isEmpty()) return@combine null
            val scores = history.map { it.score_percent }
            GameStatsDetail(
                gameId = gameId,
                playCount = history.size,
                bestScore = best?.score_percent ?: 0,
                bestScorePercent = scores.maxOrNull() ?: 0,
                avgScorePercent = if (scores.isNotEmpty()) scores.average().toInt() else 0
            )
        }
    }
}
```

- [ ] **Step 2: Create ProgressBar component**

Create `ui/components/ProgressBar.kt`:
```kotlin
package com.eatif.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun XPProgressBar(
    progressPercent: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    animated: Boolean = true
) {
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(progressPercent) {
        if (animated) {
            animatedProgress.animateTo(
                targetValue = progressPercent / 100f,
                animationSpec = tween(durationMillis = 500)
            )
        } else {
            animatedProgress.snapTo(progressPercent / 100f)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(color.copy(alpha = 0.15f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedProgress.value)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color)
        )
    }
}
```

- [ ] **Step 3: Create AchievementUnlockDialog**

Create `ui/components/AchievementUnlockDialog.kt`:
```kotlin
package com.eatif.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.eatif.app.domain.model.Achievement

@Composable
fun AchievementUnlockDialog(
    achievement: Achievement,
    onDismiss: () -> Unit
) {
    val scale = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(durationMillis = 400))
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .scale(scale.value),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "🏆 成就解锁!",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = achievement.icon,
                    style = MaterialTheme.typography.displayLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = achievement.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "+${achievement.xpReward}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFC107)
                        )
                        Text(
                            text = "XP",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (achievement.unlockSkinId != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "🎨",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = "新皮肤解锁",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("太棒了!", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
```

- [ ] **Step 4: Create ProfileViewModel**

Create `ui/screens/ProfileViewModel.kt`:
```kotlin
package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.PlayerProfile
import com.eatif.app.domain.usecase.PlayerProfileUseCase
import com.eatif.app.domain.usecase.StatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    val playerProfileUseCase: PlayerProfileUseCase,
    private val statsUseCase: StatsUseCase
) : ViewModel() {
    private val _profile = MutableStateFlow<PlayerProfile?>(null)
    val profile: StateFlow<PlayerProfile?> = _profile.asStateFlow()

    fun loadProfile() {
        viewModelScope.launch {
            _profile.value = playerProfileUseCase.getProfile()
        }
    }
}
```

- [ ] **Step 5: Create ProfileScreen**

Create `ui/screens/ProfileScreen.kt`:
```kotlin
package com.eatif.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eatif.app.ui.components.XPProgressBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    onBackClick: () -> Unit,
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val profile by viewModel.profile.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("个人资料") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            profile?.let { p ->
                // Player avatar + level
                Text(
                    text = "🎮",
                    style = MaterialTheme.typography.displayLarge
                )
                Text(
                    text = "Lv.${p.playerLevel}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "XP: ${p.playerXP} / ${viewModel.playerProfileUseCase.xpForLevel(p.playerLevel)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                XPProgressBar(
                    progressPercent = viewModel.playerProfileUseCase.xpProgressPercent(p),
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Stats cards
                StatsRow(
                    label1 = "总游戏数",
                    value1 = "${p.totalGamesPlayed}",
                    label2 = "总游玩时间",
                    value2 = formatTime(p.totalPlayTimeSeconds)
                )

                Spacer(modifier = Modifier.height(12.dp))

                StatsRow(
                    label1 = "当前连击",
                    value1 = "${p.currentStreak} 天",
                    label2 = "最高连击",
                    value2 = "${p.maxStreak} 天"
                )
            }
        }
    }
}

@Composable
private fun StatsRow(
    label1: String,
    value1: String,
    label2: String,
    value2: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatCard(
            label = label1,
            value = value1,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            label = label2,
            value = value2,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}小时${minutes}分钟" else "${minutes}分钟"
}
```

- [ ] **Step 6: Create AchievementViewModel**

Create `ui/screens/AchievementViewModel.kt`:
```kotlin
package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.Achievement
import com.eatif.app.domain.model.AchievementCategory
import com.eatif.app.domain.model.AchievementProgress
import com.eatif.app.domain.usecase.AchievementRegistry
import com.eatif.app.domain.usecase.PlayerProfileUseCase
import com.eatif.app.ui.settings.AchievementSettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementWithProgress(
    val achievement: Achievement,
    val progress: AchievementProgress
)

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val playerProfileUseCase: PlayerProfileUseCase
) : ViewModel() {
    private val _achievements = MutableStateFlow<List<AchievementWithProgress>>(emptyList())
    val achievements: StateFlow<List<AchievementWithProgress>> = _achievements.asStateFlow()

    private val _filter = MutableStateFlow<AchievementFilter>(AchievementFilter.ALL)
    val filter: StateFlow<AchievementFilter> = _filter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<AchievementCategory?>(null)
    val categoryFilter: StateFlow<AchievementCategory?> = _categoryFilter.asStateFlow()

    fun loadAchievements() {
        viewModelScope.launch {
            val profile = playerProfileUseCase.getProfile()
            val unlockedIds = AchievementSettingsManager.getUnlockedIds()

            val achievementsWithProgress = AchievementRegistry.all.map { achievement ->
                val progress = calculateProgress(achievement, profile, unlockedIds)
                AchievementWithProgress(achievement, progress)
            }

            _achievements.value = applyFilters(achievementsWithProgress)
        }
    }

    fun setFilter(newFilter: AchievementFilter) {
        _filter.value = newFilter
        loadAchievements()
    }

    fun setCategoryFilter(category: AchievementCategory?) {
        _categoryFilter.value = category
        loadAchievements()
    }

    private fun calculateProgress(
        achievement: Achievement,
        profile: com.eatif.app.domain.model.PlayerProfile,
        unlockedIds: Set<String>
    ): AchievementProgress {
        val isUnlocked = unlockedIds.contains(achievement.id)
        val required = when (val condition = achievement.condition) {
            is com.eatif.app.domain.model.AchievementCondition.TotalGames -> condition.count
            is com.eatif.app.domain.model.AchievementCondition.GameHighScore -> condition.score
            is com.eatif.app.domain.model.AchievementCondition.ConsecutiveDays -> condition.days
            is com.eatif.app.domain.model.AchievementCondition.PlayAllGames -> condition.count
            is com.eatif.app.domain.model.AchievementCondition.TotalPlayTime -> condition.seconds.toInt()
        }

        val current = when (val condition = achievement.condition) {
            is com.eatif.app.domain.model.AchievementCondition.TotalGames -> profile.totalGamesPlayed
            is com.eatif.app.domain.model.AchievementCondition.ConsecutiveDays -> profile.currentStreak
            is com.eatif.app.domain.model.AchievementCondition.TotalPlayTime -> profile.totalPlayTimeSeconds.toInt()
            is com.eatif.app.domain.model.AchievementCondition.GameHighScore -> 0 // Needs stats lookup
            is com.eatif.app.domain.model.AchievementCondition.PlayAllGames -> 0 // Needs stats lookup
        }

        return AchievementProgress(
            achievementId = achievement.id,
            currentProgress = if (isUnlocked) required else current,
            requiredProgress = required,
            isUnlocked = isUnlocked
        )
    }

    private fun applyFilters(list: List<AchievementWithProgress>): List<AchievementWithProgress> {
        var result = list
        if (_filter.value == AchievementFilter.UNLOCKED) {
            result = result.filter { it.progress.isUnlocked }
        } else if (_filter.value == AchievementFilter.LOCKED) {
            result = result.filter { !it.progress.isUnlocked }
        }
        _categoryFilter.value?.let { cat ->
            result = result.filter { it.achievement.category == cat }
        }
        return result
    }
}

enum class AchievementFilter {
    ALL, UNLOCKED, LOCKED
}
```

- [ ] **Step 7: Create AchievementScreen**

Create `ui/screens/AchievementScreen.kt`:
```kotlin
package com.eatif.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eatif.app.domain.model.AchievementCategory
import com.eatif.app.ui.components.XPProgressBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementScreen(
    onBackClick: () -> Unit,
    viewModel: AchievementViewModel = hiltViewModel()
) {
    val achievements by viewModel.achievements.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val categoryFilter by viewModel.categoryFilter.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadAchievements()
    }

    var selectedAchievement by remember { mutableStateOf<AchievementWithProgress?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("成就") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Filter chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == AchievementFilter.ALL,
                    onClick = { viewModel.setFilter(AchievementFilter.ALL) },
                    label = { Text("全部") }
                )
                FilterChip(
                    selected = filter == AchievementFilter.UNLOCKED,
                    onClick = { viewModel.setFilter(AchievementFilter.UNLOCKED) },
                    label = { Text("已解锁") }
                )
                FilterChip(
                    selected = filter == AchievementFilter.LOCKED,
                    onClick = { viewModel.setFilter(AchievementFilter.LOCKED) },
                    label = { Text("未解锁") }
                )
            }

            // Category filter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryChip("全部", categoryFilter == null) {
                    viewModel.setCategoryFilter(null)
                }
                CategoryChip("里程碑", categoryFilter == AchievementCategory.MILESTONE) {
                    viewModel.setCategoryFilter(AchievementCategory.MILESTONE)
                }
                CategoryChip("技能", categoryFilter == AchievementCategory.SKILL) {
                    viewModel.setCategoryFilter(AchievementCategory.SKILL)
                }
                CategoryChip("连击", categoryFilter == AchievementCategory.STREAK) {
                    viewModel.setCategoryFilter(AchievementCategory.STREAK)
                }
                CategoryChip("探索", categoryFilter == AchievementCategory.EXPLORATION) {
                    viewModel.setCategoryFilter(AchievementCategory.EXPLORATION)
                }
            }

            // Achievement grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(achievements) { item ->
                    AchievementCard(
                        item = item,
                        onClick = { selectedAchievement = item }
                    )
                }
            }
        }

        // Detail dialog for locked achievements
        selectedAchievement?.let { item ->
            if (!item.progress.isUnlocked) {
                AchievementDetailDialog(
                    item = item,
                    onDismiss = { selectedAchievement = null }
                )
            }
        }
    }
}

@Composable
private fun CategoryChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) }
    )
}

@Composable
private fun AchievementCard(
    item: AchievementWithProgress,
    onClick: () -> Unit
) {
    val alpha = if (item.progress.isUnlocked) 1f else 0.5f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (item.progress.isUnlocked)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
                .alpha(alpha),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = item.achievement.icon,
                style = MaterialTheme.typography.headlineMedium
            )
            Text(
                text = item.achievement.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                text = item.achievement.description,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (!item.progress.isUnlocked) {
                XPProgressBar(
                    progressPercent = if (item.progress.requiredProgress > 0)
                        (item.progress.currentProgress.toFloat() / item.progress.requiredProgress) * 100f
                    else 0f,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                Text(
                    text = "${item.progress.currentProgress}/${item.progress.requiredProgress}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "✅",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun AchievementDetailDialog(
    item: AchievementWithProgress,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(item.achievement.name) },
        text = {
            Column {
                Text(
                    text = item.achievement.icon,
                    style = MaterialTheme.typography.displayMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = item.achievement.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "进度: ${item.progress.currentProgress}/${item.progress.requiredProgress}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "奖励: +${item.achievement.xpReward} XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFFFFC107)
                )
                if (item.achievement.unlockSkinId != null) {
                    Text(
                        text = "解锁皮肤",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("关闭")
            }
        }
    )
}
```

- [ ] **Step 8: Create StatsViewModel**

Create `ui/screens/StatsViewModel.kt`:
```kotlin
package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.GameStats
import com.eatif.app.domain.model.PlayerProfile
import com.eatif.app.domain.usecase.PlayerProfileUseCase
import com.eatif.app.domain.usecase.StatsOverview
import com.eatif.app.domain.usecase.StatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsUseCase: StatsUseCase,
    val playerProfileUseCase: PlayerProfileUseCase
) : ViewModel() {
    private val _statsOverview = MutableStateFlow<StatsOverview?>(null)
    val statsOverview: StateFlow<StatsOverview?> = _statsOverview.asStateFlow()

    private val _topScores = MutableStateFlow<List<GameStats>>(emptyList())
    val topScores: StateFlow<List<GameStats>> = _topScores.asStateFlow()

    private val _profile = MutableStateFlow<PlayerProfile?>(null)
    val profile: StateFlow<PlayerProfile?> = _profile.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch {
            statsUseCase.getStatsOverview().collect { overview ->
                _statsOverview.value = overview
            }
        }
        viewModelScope.launch {
            statsUseCase.getGlobalTopScores().collect { scores ->
                _topScores.value = scores
            }
        }
        viewModelScope.launch {
            _profile.value = playerProfileUseCase.getProfile()
        }
    }
}
```

- [ ] **Step 9: Create StatsScreen**

Create `ui/screens/StatsScreen.kt`:
```kotlin
package com.eatif.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eatif.app.ui.components.XPProgressBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    onBackClick: () -> Unit,
    viewModel: StatsViewModel = hiltViewModel()
) {
    val statsOverview by viewModel.statsOverview.collectAsState()
    val topScores by viewModel.topScores.collectAsState()
    val profile by viewModel.profile.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("统计") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            profile?.let { p ->
                item {
                    // Player level card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Lv.${p.playerLevel}",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${p.playerXP}/${viewModel.playerProfileUseCase.xpForLevel(p.playerLevel)} XP",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            XPProgressBar(
                                progressPercent = viewModel.playerProfileUseCase.xpProgressPercent(p)
                            )
                        }
                    }
                }
            }

            statsOverview?.let { stats ->
                item {
                    // Stats overview
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard("总游戏", "${stats.totalGames}", Modifier.weight(1f))
                        StatCard("总时间", formatTime(stats.totalPlayTimeSeconds), Modifier.weight(1f))
                    }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        StatCard("最高分", "${stats.bestScore}%", Modifier.weight(1f))
                        StatCard("成就", "${stats.achievementsUnlocked}/${stats.totalAchievements}", Modifier.weight(1f))
                    }
                }
            }

            item {
                Text(
                    text = "🏆 排行榜",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            items(topScores) { score ->
                ScoreCard(score)
            }
        }
    }
}

@Composable
private fun StatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ScoreCard(score: com.eatif.app.domain.model.GameStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = score.foodName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = score.gameId,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${score.scorePercent}%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

private fun formatTime(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}h${minutes}m" else "${minutes}m"
}
```

- [ ] **Step 10: Build and verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 11: Commit**

```bash
git add domain/usecase/StatsUseCase.kt
git add ui/components/ProgressBar.kt ui/components/AchievementUnlockDialog.kt
git add ui/screens/ProfileViewModel.kt ui/screens/ProfileScreen.kt
git add ui/screens/AchievementViewModel.kt ui/screens/AchievementScreen.kt
git add ui/screens/StatsViewModel.kt ui/screens/StatsScreen.kt
git commit -m "feat: add stats, profile, and achievement screens with ViewModels"
```

---

### Task 7: UI Integration

**Goal:** Wire everything together — modify existing screens, add navigation, integrate game-end flow with achievements/XP/skins.

**Files:**
- Modify: `ui/navigation/Screen.kt`
- Modify: `ui/navigation/NavHost.kt`
- Modify: `ui/screens/HomeScreen.kt`
- Modify: `ui/screens/GameSelectScreen.kt`
- Modify: `ui/screens/PlayScreen.kt`
- Modify: `ui/screens/ResultScreen.kt`
- Modify: `EatIfApplication.kt`
- Create: `ui/GameEndResultHolder.kt`

- [ ] **Step 1: Update Screen.kt with new routes**

Modify `ui/navigation/Screen.kt` — add new screen routes:

After `data object History : Screen("history")`, add:
```kotlin
    data object Profile : Screen("profile")
    data object Achievements : Screen("achievements")
    data object LevelSelect : Screen("level_select/{gameId}") {
        fun createRoute(gameId: String) = "level_select/$gameId"
    }
    data object Stats : Screen("stats")
    data object SkinSelector : Screen("skin_selector/{gameId}") {
        fun createRoute(gameId: String) = "skin_selector/$gameId"
    }
```

- [ ] **Step 2: Update NavHost.kt**

Modify `ui/navigation/NavHost.kt`:

1. Add imports after existing screen imports:
```kotlin
import com.eatif.app.ui.screens.ProfileScreen
import com.eatif.app.ui.screens.AchievementScreen
import com.eatif.app.ui.screens.LevelSelectScreen
import com.eatif.app.ui.screens.StatsScreen
import com.eatif.app.ui.screens.SkinSelectorScreen
import com.eatif.app.domain.model.GameList
```

2. Update the Play composable to handle the new `onGameEnd` signature and add `onSkinsClick`:
```kotlin
        composable(
            route = Screen.Play.route,
            arguments = listOf(
                navArgument("gameId") { type = NavType.StringType },
                navArgument("mode") { type = NavType.StringType; defaultValue = "single" }
            )
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            val mode = backStackEntry.arguments?.getString("mode") ?: "single"
            PlayScreen(
                gameId = gameId,
                mode = mode,
                onGameEnd = { foodName, scorePercent, result ->
                    navController.navigate(
                        "result/$foodName/$scorePercent/${result.xpEarned}/${result.playerLevel}"
                    ) {
                        popUpTo(Screen.Home.route)
                    }
                    // Store achievements in a shared holder for ResultScreen to access
                    com.eatif.app.ui.GameEndResultHolder.unlockedAchievements = result.unlockedAchievements
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onSkinsClick = {
                    navController.navigate(Screen.SkinSelector.createRoute(gameId))
                }
            )
        }
```

3. Add import for the holder:
```kotlin
import com.eatif.app.ui.GameEndResultHolder
```

4. Add new composable routes before the closing `}` of NavHost:
```kotlin
        composable(Screen.Profile.route) {
            ProfileScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Achievements.route) {
            AchievementScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.LevelSelect.route,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            val gameName = GameList.games.find { it.id == gameId }?.name ?: "游戏"
            LevelSelectScreen(
                gameId = gameId,
                gameName = gameName,
                onLevelSelected = { _ ->
                    navController.navigate(Screen.Play.createRoute(gameId, "single"))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(Screen.Stats.route) {
            StatsScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.SkinSelector.route,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            val gameName = GameList.games.find { it.id == gameId }?.name ?: "游戏"
            SkinSelectorScreen(
                gameId = gameId,
                gameName = gameName,
                onBackClick = { navController.popBackStack() }
            )
        }
```

- [ ] **Step 3: Create GameEndResultHolder**

Create `ui/GameEndResultHolder.kt`:
```kotlin
package com.eatif.app.ui

import com.eatif.app.domain.model.Achievement

object GameEndResultHolder {
    var unlockedAchievements: List<Achievement> = emptyList()
}
```

- [ ] **Step 4: Update PlayScreen to add skin button**

Modify `ui/screens/PlayScreen.kt`:

1. Update the signature:
```kotlin
@Composable
fun PlayScreen(
    gameId: String,
    mode: String = "single",
    onGameEnd: (String, Int, PlayViewModel.GameEndResult) -> Unit,
    onBackClick: () -> Unit,
    onSkinsClick: (() -> Unit)? = null,
    viewModel: PlayViewModel = hiltViewModel()
) {
```

2. Replace the handleGameEnd lambda:
```kotlin
    val handleGameEnd: (String, Int) -> Unit = { foodName, scorePercent ->
        viewModel.processGameEnd(
            gameId = gameId,
            foodName = foodName,
            scorePercent = scorePercent,
            playTimeSeconds = 0,
            onResult = { result ->
                onGameEnd(foodName, scorePercent, result)
            }
        )
    }
```

3. Add skin button to pause overlay. Inside the `if (isPaused)` Column, after the existing "点击播放按钮继续" Text:
```kotlin
                    if (onSkinsClick != null) {
                        androidx.compose.material3.TextButton(onClick = { onSkinsClick() }) {
                            androidx.compose.material3.Text("🎨 皮肤", color = White)
                        }
                    }
```

- [ ] **Step 5: Update ResultScreen with XP display and achievement popup**

Modify `ui/screens/ResultScreen.kt`:

1. Add imports:
```kotlin
import com.eatif.app.domain.model.Achievement
import com.eatif.app.ui.components.AchievementUnlockDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
```

2. Update the signature:
```kotlin
@Composable
fun ResultScreen(
    foodName: String,
    scorePercent: Int = -1,
    xpEarned: Int = 0,
    playerLevel: Int = 1,
    onPlayAgain: () -> Unit
) {
```

3. After the existing score display section (after the LinearProgressIndicator block or the fallback "今天的晚餐是" block), add XP display:
```kotlin
    if (xpEarned > 0) {
        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFC107).copy(alpha = 0.15f)
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "+$xpEarned",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFFC107)
                    )
                    Text(
                        text = "XP 获得",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Lv.$playerLevel",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "当前等级",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
```

4. Add achievement unlock dialog at the end of the Composable function (after the last Button but before the closing Column):
```kotlin
    var currentAchievementIndex by remember { mutableStateOf(0) }
    val unlockedAchievements = remember { com.eatif.app.ui.GameEndResultHolder.unlockedAchievements }

    if (unlockedAchievements.isNotEmpty() && currentAchievementIndex < unlockedAchievements.size) {
        AchievementUnlockDialog(
            achievement = unlockedAchievements[currentAchievementIndex],
            onDismiss = { currentAchievementIndex++ }
        )
    }
```

- [ ] **Step 6: Update Result route and NavHost result composable**

1. Update the Result Screen route in `Screen.kt`:
```kotlin
    data object Result : Screen("result/{foodName}/{scorePercent}/{xpEarned}/{playerLevel}") {
        fun createRoute(foodName: String, scorePercent: Int = -1, xpEarned: Int = 0, playerLevel: Int = 1) =
            "result/$foodName/$scorePercent/$xpEarned/$playerLevel"
    }
```

2. Update the Result composable in `NavHost.kt`:
```kotlin
        composable(
            route = Screen.Result.route,
            arguments = listOf(
                navArgument("foodName") { type = NavType.StringType },
                navArgument("scorePercent") { type = NavType.IntType; defaultValue = -1 },
                navArgument("xpEarned") { type = NavType.IntType; defaultValue = 0 },
                navArgument("playerLevel") { type = NavType.IntType; defaultValue = 1 }
            )
        ) { backStackEntry ->
            val foodName = backStackEntry.arguments?.getString("foodName") ?: ""
            val scorePercent = backStackEntry.arguments?.getInt("scorePercent") ?: -1
            val xpEarned = backStackEntry.arguments?.getInt("xpEarned") ?: 0
            val playerLevel = backStackEntry.arguments?.getInt("playerLevel") ?: 1
            ResultScreen(
                foodName = foodName,
                scorePercent = scorePercent,
                xpEarned = xpEarned,
                playerLevel = playerLevel,
                onPlayAgain = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
```

- [ ] **Step 7: Update HomeScreen with bottom navigation**

Modify `ui/screens/HomeScreen.kt`:

1. Add imports:
```kotlin
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
```

2. Update signature:
```kotlin
@Composable
fun HomeScreen(
    onSinglePlayerClick: () -> Unit,
    onTwoPlayerClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatsClick: () -> Unit = {},
    onAchievementsClick: () -> Unit = {},
    onProfileClick: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
```

3. Add bottomBar to Scaffold:
```kotlin
    Scaffold(
        topBar = { /* existing TopAppBar */ },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "首页") },
                    label = { Text("首页") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Star, contentDescription = "统计") },
                    label = { Text("统计") },
                    selected = false,
                    onClick = onStatsClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = "成就") },
                    label = { Text("成就") },
                    selected = false,
                    onClick = onAchievementsClick
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "我的") },
                    label = { Text("我的") },
                    selected = false,
                    onClick = onProfileClick
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
```

- [ ] **Step 8: Update GameSelectScreen with level select button**

Modify `ui/screens/GameSelectScreen.kt`:

1. Add import:
```kotlin
import androidx.compose.material.icons.filled.Star
```

2. Update signature:
```kotlin
@Composable
fun GameSelectScreen(
    mode: String,
    onGameSelected: (String) -> Unit,
    onBackClick: () -> Unit,
    onGameRuleClick: (String) -> Unit = {},
    onLevelSelectClick: (String) -> Unit = {}
) {
```

3. Add level button to TopAppBar actions (after the existing IconButton for favorites):
```kotlin
                    IconButton(onClick = { onLevelSelectClick("") }) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "关卡"
                        )
                    }
```

Note: The level select per-game is better accessed from the game card. Add a new parameter to GameCard for `onLevelClick`:

Modify `ui/components/GameCard.kt` to add:
```kotlin
    onLevelClick: (() -> Unit)? = null
```

And in the Box, add (before the settings IconButton):
```kotlin
            if (onLevelClick != null) {
                IconButton(
                    onClick = onLevelClick,
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(4.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "关卡",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
```

Then in GameSelectScreen's LazyVerticalGrid items:
```kotlin
                        GameCard(
                            title = game.name,
                            emoji = game.emoji,
                            description = game.description,
                            isFavorite = GameSettingsManager.isFavorite(game.id),
                            onFavoriteClick = {
                                GameSettingsManager.toggleFavorite(game.id)
                            },
                            onClick = { onGameSelected(game.id) },
                            onSettingsClick = { onGameRuleClick(game.id) },
                            onLevelClick = { onLevelSelectClick(game.id) }
                        )
```

4. Update the NavHost call to GameSelectScreen:
```kotlin
            GameSelectScreen(
                mode = mode,
                onGameSelected = { gameId ->
                    navController.navigate(Screen.Play.createRoute(gameId, mode))
                },
                onBackClick = {
                    navController.popBackStack()
                },
                onGameRuleClick = { gameId ->
                    navController.navigate(Screen.GameRule.createRoute(gameId))
                },
                onLevelSelectClick = { gameId ->
                    navController.navigate(Screen.LevelSelect.createRoute(gameId))
                }
            )
```

- [ ] **Step 9: Initialize new settings managers in EatIfApplication**

Modify `EatIfApplication.kt`:

Add imports:
```kotlin
import com.eatif.app.ui.settings.SkinSettingsManager
import com.eatif.app.ui.settings.AchievementSettingsManager
```

Add init calls in `onCreate()` (after `SessionManager.init(this)`):
```kotlin
        SkinSettingsManager.init(this)
        AchievementSettingsManager.init(this)
```

- [ ] **Step 10: Build and verify**

Run: `./gradlew :app:assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 11: Commit**

```bash
git add ui/GameEndResultHolder.kt
git add ui/navigation/Screen.kt ui/navigation/NavHost.kt
git add ui/screens/HomeScreen.kt ui/screens/GameSelectScreen.kt
git add ui/screens/PlayScreen.kt ui/screens/ResultScreen.kt
git add ui/components/GameCard.kt
git add EatIfApplication.kt
git commit -m "feat: integrate all game depth features with navigation and UI"
```

---

## Post-Implementation Checklist

- [ ] Verify database migration works (install v2 app, update, check no crash)
- [ ] Test achievement unlocking after playing games
- [ ] Verify XP calculation and level progression
- [ ] Test level select screen and unlocking
- [ ] Verify skin selection works
- [ ] Test all new screens display correctly
- [ ] Run: `./gradlew :app:assembleDebug` — final build
