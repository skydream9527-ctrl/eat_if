# Eat If 游戏内容深化设计 — 综合深度方案

日期: 2026-04-20

## 概述

在现有 15 款游戏基础上，构建完整的游戏成长体系：成就系统、关卡系统、经验等级、皮肤系统、排行榜统计。通过共享数据层 (PlayerProfile) 统一所有模块，实现成就触发皮肤解锁、关卡联动难度、游戏结束自动结算 XP 的完整闭环。

---

## 模块一：共享数据层

### 目标

建立统一的数据模型和存储方案，供成就、关卡、皮肤、排行榜四大模块共用。

### 核心数据模型

```kotlin
// PlayerProfile.kt
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

// GameStats.kt
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

// Achievement.kt
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
    MILESTONE,   // 里程碑 (首次游戏、全制霸等)
    STREAK,      // 连击/连续
    SKILL,       // 技能型 (高分、完美等)
    EXPLORATION  // 探索型 (玩过所有游戏)
}

sealed class AchievementCondition {
    data class TotalGames(val count: Int) : AchievementCondition()
    data class GameHighScore(val gameId: String, val score: Int) : AchievementCondition()
    data class ConsecutiveDays(val days: Int) : AchievementCondition()
    data class PlayAllGames(val count: Int) : AchievementCondition()
    data class TotalPlayTime(val seconds: Long) : AchievementCondition()
}

// AchievementProgress.kt
data class AchievementProgress(
    val achievementId: String,
    val currentProgress: Int = 0,
    val requiredProgress: Int,
    val isUnlocked: Boolean = false,
    val unlockedAt: Long? = null
)

// GameLevel.kt
data class GameLevel(
    val gameId: String,
    val levelNumber: Int,
    val difficulty: GameDifficulty,
    val requiredStars: Int = 0,
    val params: Map<String, String>
)

// LevelProgress.kt
data class LevelProgress(
    val gameId: String,
    val currentLevel: Int = 1,
    val stars: Map<Int, Int> = emptyMap(),
    val bestScores: Map<Int, Int> = emptyMap()
)

// Skin.kt
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

// SkinCollection.kt
data class SkinCollection(
    val skinId: String,
    val gameId: String,
    val isUnlocked: Boolean = false,
    val isActive: Boolean = false
)
```

### 数据库设计 (Room)

| 实体 | 表名 | 关键字段 |
|------|------|---------|
| PlayerProfileEntity | player_profile | totalGamesPlayed, playerLevel, playerXP, streak |
| GameStatsEntity | game_stats | gameId, score, difficulty, level, playTimeSeconds |
| AchievementProgressEntity | achievement_progress | achievementId, currentProgress, isUnlocked |
| LevelProgressEntity | level_progress | gameId, currentLevel, stars(JSON), bestScores(JSON) |
| SkinCollectionEntity | skin_collection | skinId, gameId, isUnlocked, isActive |

### 存储策略

| 数据类型 | 存储方式 | 原因 |
|---------|---------|------|
| 玩家档案 | Room | 需要查询和更新 |
| 游戏统计 | Room | 需要排序、聚合、排行榜查询 |
| 成就进度 | Room + SharedPreferences | Room 存详细进度，SP 存已解锁 ID 集合 (快速判断) |
| 关卡进度 | Room | 需要按游戏/关卡查询 |
| 皮肤状态 | SharedPreferences | 快速读取，适合游戏内实时判断 |

### 数据库迁移 (v2 → v3)

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
    }
}
```

### 新增/修改文件清单

| 文件 | 操作 | 职责 |
|------|------|------|
| `domain/model/PlayerProfile.kt` | 新增 | 玩家档案模型 |
| `domain/model/GameStats.kt` | 新增 | 游戏统计模型 |
| `domain/model/Achievement.kt` | 新增 | 成就定义 + 条件枚举 |
| `domain/model/AchievementProgress.kt` | 新增 | 成就进度模型 |
| `domain/model/GameLevel.kt` | 新增 | 关卡定义模型 |
| `domain/model/LevelProgress.kt` | 新增 | 关卡进度模型 |
| `domain/model/Skin.kt` | 新增 | 皮肤定义模型 |
| `domain/model/SkinCollection.kt` | 新增 | 皮肤收集状态模型 |
| `data/local/PlayerProfileEntity.kt` | 新增 | Room Entity |
| `data/local/GameStatsEntity.kt` | 新增 | Room Entity |
| `data/local/AchievementProgressEntity.kt` | 新增 | Room Entity |
| `data/local/LevelProgressEntity.kt` | 新增 | Room Entity |
| `data/local/SkinCollectionEntity.kt` | 新增 | Room Entity |
| `data/local/PlayerProfileDao.kt` | 新增 | DAO |
| `data/local/GameStatsDao.kt` | 新增 | DAO |
| `data/local/AchievementProgressDao.kt` | 新增 | DAO |
| `data/local/LevelProgressDao.kt` | 新增 | DAO |
| `data/local/SkinCollectionDao.kt` | 新增 | DAO |
| `data/local/TypeConverters.kt` | 新增 | JSON 转换器 |
| `data/local/FoodDatabase.kt` | 修改 | version=3 + @TypeConverters |
| `di/DatabaseModule.kt` | 修改 | 注册 Migration 2→3 + 所有 DAO Provider |

---

## 模块二：成就引擎

### 目标

定义约 20 个成就，游戏结束时自动检测条件，满足则解锁并发放 XP 奖励。

### 成就定义 (硬编码单例)

```kotlin
object AchievementRegistry {
    val all: List<Achievement> = listOf(
        Achievement(
            id = "first_game",
            name = "初次尝试",
            description = "完成你的第一局游戏",
            icon = "🎮",
            category = MILESTONE,
            condition = TotalGames(1),
            xpReward = 10
        ),
        Achievement(
            id = "first_win",
            name = "初次胜利",
            description = "以超过 70% 的得分完成 10 局游戏",
            icon = "🏆",
            category = SKILL,
            condition = GameHighScore(gameId = "any", score = 70),
            xpReward = 50,
            unlockSkinId = "snake_neon"
        ),
        Achievement(
            id = "streak_7",
            name = "一周打卡",
            description = "连续 7 天游玩",
            icon = "📅",
            category = STREAK,
            condition = ConsecutiveDays(7),
            xpReward = 100
        ),
        Achievement(
            id = "all_games",
            name = "全制霸",
            description = "玩过全部 15 款游戏",
            icon = "👑",
            category = EXPLORATION,
            condition = PlayAllGames(15),
            xpReward = 80,
            unlockSkinId = "spinwheel_gold"
        ),
        Achievement(
            id = "snake_master",
            name = "蛇王",
            description = "贪吃蛇单局得分超过 50",
            icon = "🐍",
            category = SKILL,
            condition = GameHighScore(gameId = "snake", score = 50),
            xpReward = 60
        ),
        Achievement(
            id = "two048_legend",
            name = "2048 传奇",
            description = "2048 达到 1024",
            icon = "🔢",
            category = SKILL,
            condition = GameHighScore(gameId = "2048", score = 1024),
            xpReward = 100,
            unlockSkinId = "game2048_dark"
        ),
        Achievement(
            id = "perfect_shot",
            name = "神枪手",
            description = "打靶命中率超过 90%",
            icon = "🎯",
            category = SKILL,
            condition = GameHighScore(gameId = "shooting", score = 90),
            xpReward = 50
        ),
        Achievement(
            id = "marathon",
            name = "马拉松",
            description = "单局游戏超过 10 分钟",
            icon = "🏃",
            category = MILESTONE,
            condition = TotalPlayTime(600),
            xpReward = 70
        ),
        Achievement(
            id = "ten_games",
            name = "十局达人",
            description = "完成 10 局游戏",
            icon = "🔟",
            category = MILESTONE,
            condition = TotalGames(10),
            xpReward = 20
        ),
        Achievement(
            id = "fifty_games",
            name = "游戏狂热",
            description = "完成 50 局游戏",
            icon = "🔥",
            category = MILESTONE,
            condition = TotalGames(50),
            xpReward = 40
        ),
        Achievement(
            id = "hundred_games",
            name = "百战成神",
            description = "完成 100 局游戏",
            icon = "⚡",
            category = MILESTONE,
            condition = TotalGames(100),
            xpReward = 100
        ),
        Achievement(
            id = "streak_30",
            name = "月度达人",
            description = "连续 30 天游玩",
            icon = "🗓️",
            category = STREAK,
            condition = ConsecutiveDays(30),
            xpReward = 200,
            unlockSkinId = "flappy_golden"
        ),
        Achievement(
            id = "five_different",
            name = "尝鲜玩家",
            description = "玩过 5 款不同的游戏",
            icon = "🌟",
            category = EXPLORATION,
            condition = PlayAllGames(5),
            xpReward = 30
        ),
        Achievement(
            id = "total_hour",
            name = "时光旅者",
            description = "累计游玩时间超过 1 小时",
            icon = "⏰",
            category = MILESTONE,
            condition = TotalPlayTime(3600),
            xpReward = 60
        ),
        Achievement(
            id = "slot_jackpot",
            name = "幸运大奖",
            description = "老虎机命中大奖",
            icon = "🎰",
            category = SKILL,
            condition = GameHighScore(gameId = "slot", score = 100),
            xpReward = 40
        )
    )

    fun getUnlockedIds(): Set<String> = ... // 从 SharedPreferences 读取
    fun markUnlocked(id: String) = ...       // 写入 SharedPreferences
}
```

### 成就检测引擎

```kotlin
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
        val difficulty: GameDifficulty
    )

    suspend fun checkAndUnlock(event: GameEndEvent): List<Achievement> {
        val newlyUnlocked = mutableListOf<Achievement>()
        val profile = profileDao.getOrCreate()
        val allStats = statsDao.getAll()

        for (achievement in AchievementRegistry.all) {
            if (achievement.id in AchievementRegistry.getUnlockedIds()) continue

            val current = calculateProgress(achievement, profile, allStats, event)
            val required = getRequiredProgress(achievement.condition)

            if (current >= required) {
                progressDao.upsert(AchievementProgress(
                    achievementId = achievement.id,
                    currentProgress = current,
                    requiredProgress = required,
                    isUnlocked = true,
                    unlockedAt = System.currentTimeMillis()
                ))
                AchievementRegistry.markUnlocked(achievement.id)
                newlyUnlocked.add(achievement)
            } else {
                progressDao.upsert(AchievementProgress(
                    achievementId = achievement.id,
                    currentProgress = current,
                    requiredProgress = required,
                    isUnlocked = false
                ))
            }
        }
        return newlyUnlocked
    }

    private fun calculateProgress(...): Int {
        // 根据条件类型计算当前进度
    }
}
```

### 触发时机

```
游戏结束 (PlayScreen → onGameEnd)
    ↓
记录 History (原有逻辑)
    ↓
记录 GameStats (新增)
    ↓
更新 PlayerProfile (新增: games++, playTime++, streak)
    ↓
AchievementEngine.checkAndUnlock(event)
    ↓
对每个新解锁成就: 发放 XP → 可能解锁皮肤 → 触发 UI 弹窗
```

---

## 模块三：经验等级系统

### XP 计算公式

```kotlin
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
```

### 等级公式

```kotlin
fun xpForLevel(level: Int): Int {
    // 线性递增: 每级需要 100 * level XP
    // 1级→2级: 100 XP
    // 2级→3级: 200 XP
    // ...
    // 49级→50级: 5000 XP
    // 总 XP 到 50 级: 127,500
    return 100 * level
}
```

### PlayerProfile 更新逻辑

```kotlin
class PlayerProfileUseCase @Inject constructor(
    private val profileDao: PlayerProfileDao
) {
    suspend fun recordGameSession(
        xpEarned: Int,
        playTimeSeconds: Long
    ): PlayerProfile {
        val profile = profileDao.getOrCreate()
        val today = LocalDate.now().toString()

        val newStreak = if (profile.lastPlayedDate == today.minusDays(1)) {
            profile.currentStreak + 1
        } else if (profile.lastPlayedDate != today) {
            1
        } else {
            profile.currentStreak
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
        profileDao.update(updated)
        return updated
    }
}
```

---

## 模块四：关卡系统

### 关卡定义

每款游戏定义 10-20 关，通过 `GameLevelRegistry` 硬编码：

```kotlin
object GameLevelRegistry {
    val levels: List<GameLevel> = listOf(
        // 贪吃蛇 10 关
        GameLevel("snake", 1, EASY, 0, mapOf("speed" to "slow", "obstacles" to "0")),
        GameLevel("snake", 2, EASY, 1, mapOf("speed" to "slow", "obstacles" to "1")),
        GameLevel("snake", 3, EASY, 2, mapOf("speed" to "medium", "obstacles" to "2")),
        ...
        // 2048 10 关
        GameLevel("2048", 1, EASY, 0, mapOf("target" to "256")),
        ...
    )

    fun getLevelsForGame(gameId: String): List<GameLevel> =
        levels.filter { it.gameId == gameId }
}
```

### 关卡解锁与星星

```
解锁条件: 前一关至少获得 requiredStars 颗星
星星评定:
  ⭐   完成关卡 (scorePercent > 0)
  ⭐⭐  scorePercent >= 70
  ⭐⭐⭐ scorePercent >= 90
```

### 关卡参数传递

不修改现有游戏逻辑，通过 `GameRuleConfig.customParams` 传递：

```
关卡选择 → 获取 GameLevel.params → 写入 GameRuleConfig.customParams
    ↓
PlayScreen 读取 GameRuleConfig → 传递给游戏 Composable
    ↓
游戏内读取 customParams (如 "speed", "obstacles") 并应用
```

### 新增文件

| 文件 | 职责 |
|------|------|
| `domain/usecase/LevelManager.kt` | 关卡解锁/进度管理 |
| `ui/screens/LevelSelectScreen.kt` | 关卡选择网格页面 |
| `ui/navigation/Screen.kt` | 新增 LevelSelect 路由 |
| `ui/navigation/NavHost.kt` | 注册 LevelSelect 页面 |

---

## 模块五：皮肤系统

### 皮肤定义

```kotlin
object SkinRegistry {
    val all: List<Skin> = listOf(
        Skin("snake_default", "经典绿", "snake", COMMON, DEFAULT, isDefault = true),
        Skin("snake_neon", "霓虹蛇", "snake", RARE, ACHIEVEMENT, "first_win"),
        Skin("snake_golden", "金蛇", "snake", LEGENDARY, LEVEL, "达到第10关"),
        Skin("game2048_default", "经典", "2048", COMMON, DEFAULT, isDefault = true),
        Skin("game2048_dark", "暗黑", "2048", RARE, ACHIEVEMENT, "two048_legend"),
        Skin("flappy_default", "经典", "flappy", COMMON, DEFAULT, isDefault = true),
        Skin("flappy_golden", "金翼", "flappy", LEGENDARY, ACHIEVEMENT, "streak_30"),
        Skin("spinwheel_default", "经典", "spinwheel", COMMON, DEFAULT, isDefault = true),
        Skin("spinwheel_gold", "金轮", "spinwheel", EPIC, ACHIEVEMENT, "all_games"),
        // ... 每款游戏 2-4 款皮肤
    )

    fun getSkinsForGame(gameId: String): List<Skin> =
        all.filter { it.gameId == gameId }
}
```

### SkinResolver

```kotlin
class SkinResolver @Inject constructor() {
    fun getActiveSkin(gameId: String): Skin {
        val skins = SkinRegistry.getSkinsForGame(gameId)
        val activeSkinId = SkinSettingsManager.getActiveSkinId(gameId)
        return skins.find { it.id == activeSkinId && isUnlocked(it) }
            ?: skins.find { it.isDefault }
            ?: skins.first()
    }

    private fun isUnlocked(skin: Skin): Boolean = ...
}
```

### 皮肤 UI

- 游戏内暂停菜单新增"皮肤"按钮 → SkinSelectorScreen
- 网格显示皮肤缩略图，锁定的显示解锁条件
- 点击解锁的皮肤 → 设为当前活跃 → 返回游戏
- 皮肤参数通过游戏 Composable 的额外参数传递（颜色、背景等）

### 新增文件

| 文件 | 职责 |
|------|------|
| `domain/model/Skin.kt` | 皮肤模型 |
| `ui/settings/SkinSettingsManager.kt` | 皮肤 SharedPreferences 存取 |
| `ui/screens/SkinSelectorScreen.kt` | 皮肤选择器页面 |
| `domain/usecase/SkinResolver.kt` | 活跃皮肤解析 |

---

## 模块六：排行榜/统计

### 数据源

`game_stats` 表，通过 `GameStatsDao` 查询。

### 查询接口

```kotlin
@Dao
interface GameStatsDao {
    @Query("SELECT * FROM game_stats WHERE game_id = :gameId ORDER BY score DESC LIMIT 5")
    fun getTopScoresForGame(gameId: String): Flow<List<GameStatsEntity>>

    @Query("SELECT COUNT(*) FROM game_stats")
    fun getTotalGamesCount(): Flow<Int>

    @Query("SELECT SUM(play_time_seconds) FROM game_stats")
    fun getTotalPlayTime(): Flow<Long>

    @Query("SELECT * FROM game_stats ORDER BY score DESC LIMIT 10")
    fun getGlobalTopScores(): Flow<List<GameStatsEntity>>

    @Query("SELECT * FROM game_stats WHERE game_id = :gameId ORDER BY score DESC")
    fun getBestScoreForGame(gameId: String): Flow<GameStatsEntity?>

    @Query("SELECT DISTINCT game_id FROM game_stats")
    fun getUniqueGamesPlayed(): Flow<List<String>>

    @Query("SELECT * FROM game_stats WHERE timestamp >= :fromTs ORDER BY timestamp DESC")
    fun getRecentGames(fromTs: Long): Flow<List<GameStatsEntity>>
}
```

### 统计页面 UI

```
StatsScreen (底部导航 Tab)
├── 玩家等级卡片
│   ├── 等级数字 + XP 进度条
│   └── 连击天数徽章
├── 时间筛选 (本周 / 本月 / 全部)
├── 统计概览卡片
│   ├── 总游戏数
│   ├── 总游玩时间
│   ├── 最高得分
│   └── 通关游戏数/总游戏数
├── 排行榜卡片
│   ├── 每款游戏最高分卡片
│   └── 点击进入详细统计
└── 成就进度概览
    ├── 已解锁 X/20
    └── 点击进入成就页面
```

### 新增文件

| 文件 | 职责 |
|------|------|
| `ui/screens/StatsScreen.kt` | 统计/排行榜页面 |
| `ui/screens/StatsViewModel.kt` | 聚合统计数据 |
| `domain/usecase/StatsUseCase.kt` | 统计查询 UseCase |

---

## 模块七：UI 整合

### 导航变更

```
Screen.kt 新增:
  data object Profile : Screen("profile")
  data object Achievements : Screen("achievements")
  data object LevelSelect : Screen("levels/{gameId}")
  data object Stats : Screen("stats")
  data object SkinSelector : Screen("skins/{gameId}")
```

### HomeScreen 变更

```
底部导航栏:
  🏠 首页  |  📊 统计  |  🏆 成就  |  ⚙️ 设置

顶部显示:
  玩家头像(emoji) + 等级 Lv.X + XP 进度条
```

### GameSelectScreen 变更

```
游戏卡片新增:
  - 右上角星星数 (⭐⭐⭐)
  - 左下角最高分
  - 设置图标 → GameRuleScreen (已有)
  - 关卡图标 → LevelSelectScreen (新增)
```

### PlayScreen 变更

```
HUD 新增 (游戏区域顶部叠加):
  - 当前难度 badge
  - 当前关卡 badge
  - 实时分数

暂停菜单新增:
  - "皮肤" 按钮 → SkinSelectorScreen

暂停时成就通知:
  - 新解锁成就 Toast/Snackbar
```

### ResultScreen 变更

```
新增内容:
  - XP 获得动画 (+XX XP)
  - 等级升级动画 (如果升级)
  - 星星动画 (关卡模式下)
  - 成就解锁弹窗 (如果有新成就)
  - "再来一局" 按钮
```

### ProfileScreen 新增

```
ProfileScreen:
  - 玩家头像 + 等级 + 总 XP
  - 连击天数 + 最大连击
  - 总游戏数 + 总游玩时间
  - 各游戏游玩次数图表
  - 编辑资料
```

### AchievementScreen 新增

```
AchievementScreen:
  - 筛选 Tabs: 全部 / 已解锁 / 未解锁
  - 按分类筛选: 里程碑 / 连击 / 技能 / 探索
  - 成就卡片网格 (图标 + 名称 + 描述 + 进度条)
  - 点击未解锁成就显示达成条件
```

### LevelSelectScreen 新增

```
LevelSelectScreen:
  - 顶部: 游戏名称 + 返回
  - 网格: 10-20 个关卡卡片
  - 关卡卡片显示: 关卡号 + 星星数 + 锁定/解锁状态
  - 点击已解锁关卡 → PlayScreen (传入关卡参数)
```

---

## 错误处理

| 场景 | 处理 |
|------|------|
| 数据库迁移失败 | fallbackToDestructiveMigration() |
| 成就引擎检测失败 | 静默失败，下次重新检测 |
| XP 计算异常 | 使用默认值，不影响游戏流程 |
| 皮肤加载失败 | 回退到默认皮肤 |
| SharedPreferences 损坏 | 重置为默认设置 |

---

## 测试策略

| 测试类型 | 内容 |
|----------|------|
| 单元测试 | AchievementEngine 条件检测逻辑 |
| 单元测试 | XP 计算 + 等级升级公式 |
| 单元测试 | LevelManager 解锁逻辑 |
| 单元测试 | SkinResolver 皮肤选择逻辑 |
| 集成测试 | Room DAO 查询（排行榜、进度） |
| UI 测试 | 成就解锁弹窗、星星动画、XP 进度条 |

---

## 实现顺序 (7 个 Task)

1. **数据模型 + 数据库迁移 (v2→v3)** — 所有 Entity/DAO/TypeConverter
2. **成就引擎** — AchievementRegistry + AchievementEngine + 自动解锁
3. **经验等级系统** — XP 计算 + PlayerProfileUseCase + 等级升级
4. **关卡系统** — GameLevelRegistry + LevelManager + LevelSelectScreen
5. **皮肤系统** — SkinRegistry + SkinResolver + SkinSelectorScreen
6. **排行榜/统计** — GameStatsDao + StatsScreen + ProfileScreen
7. **UI 整合** — ResultScreen 改造 + GameSelectScreen 改造 + HomeScreen 导航

---

## 依赖关系

```
Task 1 (数据层) → 所有后续任务
Task 2 (成就) → Task 3 (XP), Task 5 (皮肤解锁)
Task 3 (XP/等级) → Task 7 (UI 整合 - ResultScreen)
Task 4 (关卡) → Task 7 (UI 整合 - GameSelectScreen)
Task 5 (皮肤) → Task 7 (UI 整合 - PlayScreen 暂停菜单)
Task 6 (统计) → Task 7 (UI 整合 - 底部导航)
Task 7 (UI 整合) → 最终整合
```
