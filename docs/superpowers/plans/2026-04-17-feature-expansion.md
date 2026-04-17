# Eat If 功能扩展实现计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 Eat If App 新增智能推荐、桌面小组件、个性化定制三大功能模块

**Architecture:** 在现有 MVVM + Clean Architecture 基础上扩展。智能推荐新增 domain 层 UseCase + Repository，桌面小组件使用 Jetpack Glance，个性化定制扩展现有数据模型和 ThemeManager。标签功能需先于推荐实现以供推荐算法使用。

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Room, Hilt, Jetpack Glance (AppWidget), SharedPreferences

---

## File Structure

### 新增文件

| 文件 | 职责 |
|------|------|
| `domain/model/FoodTag.kt` | 美食标签枚举 |
| `domain/model/TimeSlot.kt` | 时段枚举 |
| `domain/model/Recommendation.kt` | 推荐结果模型 |
| `domain/model/FoodFrequency.kt` | 美食频次模型 |
| `domain/model/GameRuleConfig.kt` | 游戏规则配置模型 |
| `domain/repository/RecommendRepository.kt` | 推荐仓库接口 |
| `domain/usecase/SmartRecommendUseCase.kt` | 智能推荐 UseCase |
| `data/local/FoodTagTypeConverter.kt` | Room TypeConverter for tags |
| `data/repository/RecommendRepositoryImpl.kt` | 推荐仓库实现 |
| `di/WidgetModule.kt` | Widget Hilt 模块 |
| `ui/widget/FoodWidget.kt` | Glance Widget UI |
| `ui/widget/FoodWidgetReceiver.kt` | Widget 广播接收器 |
| `ui/screens/GameRuleScreen.kt` | 游戏规则编辑页面 |

### 修改文件

| 文件 | 变更 |
|------|------|
| `domain/model/Food.kt` | 新增 tags 字段 |
| `data/local/FoodEntity.kt` | 新增 tags 字段 |
| `data/local/FoodDao.kt` | 新增按标签查询 |
| `data/local/FoodDatabase.kt` | 版本升级 + 注册 TypeConverter |
| `data/local/FoodDataSeeder.kt` | 为预设数据补充标签 |
| `data/local/HistoryDao.kt` | 新增频次统计查询 |
| `data/repository/FoodRepositoryImpl.kt` | tags 字段映射 |
| `data/repository/HistoryRepositoryImpl.kt` | 新增频次查询映射 |
| `domain/repository/FoodRepository.kt` | 新增按标签查询接口 |
| `domain/repository/HistoryRepository.kt` | 新增频次查询接口 |
| `di/DatabaseModule.kt` | DB迁移 + 注册 RecommendRepository |
| `ui/screens/HomeScreen.kt` | 新增"猜你想吃"区域 |
| `ui/screens/HomeViewModel.kt` | 注入推荐 UseCase |
| `ui/screens/FoodLibraryScreen.kt` | 新增标签筛选 + 编辑标签 |
| `ui/screens/FoodLibraryViewModel.kt` | 新增标签相关方法 |
| `ui/theme/ThemeManager.kt` | 新增 seedColor 支持 |
| `ui/theme/Theme.kt` | 动态色板生成 |
| `ui/theme/Color.kt` | 新增预设主题色 |
| `ui/screens/SettingsScreen.kt` | 新增主题色选择器 |
| `ui/settings/GameSettingsManager.kt` | 新增游戏规则存取 |
| `ui/screens/GameSelectScreen.kt` | 新增规则入口 |
| `ui/navigation/Screen.kt` | 新增 GameRule 路由 |
| `ui/navigation/NavHost.kt` | 新增 GameRule 页面 |
| `app/build.gradle.kts` | 新增 Glance 依赖 |
| `AndroidManifest.xml` | 注册 Widget Receiver |

---

## Task 1: 美食标签数据模型与数据库迁移

**Files:**
- Create: `app/src/main/java/com/eatif/app/domain/model/FoodTag.kt`
- Create: `app/src/main/java/com/eatif/app/data/local/FoodTagTypeConverter.kt`
- Modify: `app/src/main/java/com/eatif/app/domain/model/Food.kt`
- Modify: `app/src/main/java/com/eatif/app/data/local/FoodEntity.kt`
- Modify: `app/src/main/java/com/eatif/app/data/local/FoodDao.kt`
- Modify: `app/src/main/java/com/eatif/app/data/local/FoodDatabase.kt`
- Modify: `app/src/main/java/com/eatif/app/data/local/FoodDataSeeder.kt`
- Modify: `app/src/main/java/com/eatif/app/data/repository/FoodRepositoryImpl.kt`
- Modify: `app/src/main/java/com/eatif/app/domain/repository/FoodRepository.kt`
- Modify: `app/src/main/java/com/eatif/app/di/DatabaseModule.kt`

- [ ] **Step 1: 创建 FoodTag 枚举**

```kotlin
// app/src/main/java/com/eatif/app/domain/model/FoodTag.kt
package com.eatif.app.domain.model

enum class FoodTag(val label: String, val emoji: String) {
    SPICY("辣", "🌶️"),
    SWEET("甜", "🍰"),
    FAST_FOOD("快餐", "🍔"),
    HOTPOT("火锅", "🍲"),
    BBQ("烧烤", "🍖"),
    NOODLE("面食", "🍜"),
    RICE("米饭", "🍚"),
    LIGHT("清淡", "🥗"),
    SEAFOOD("海鲜", "🦐"),
    DESSERT("甜点", "🍦")
}
```

- [ ] **Step 2: 创建 Room TypeConverter**

```kotlin
// app/src/main/java/com/eatif/app/data/local/FoodTagTypeConverter.kt
package com.eatif.app.data.local

import androidx.room.TypeConverter
import com.eatif.app.domain.model.FoodTag
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class FoodTagTypeConverter {
    private val gson = Gson()

    @TypeConverter
    fun fromFoodTags(tags: List<FoodTag>): String {
        return gson.toJson(tags.map { it.name })
    }

    @TypeConverter
    fun toFoodTags(data: String): List<FoodTag> {
        val type = object : TypeToken<List<String>>() {}.type
        val names: List<String> = gson.fromJson(data, type)
        return names.mapNotNull { name ->
            FoodTag.entries.find { it.name == name }
        }
    }
}
```

- [ ] **Step 3: 修改 Food 领域模型，新增 tags 字段**

在 `Food.kt` 的 `Food` data class 中新增 `tags` 字段：

```kotlin
data class Food(
    val id: Long = 0,
    val name: String,
    val category: String,
    val imageUrl: String? = null,
    val weight: Int = 1,
    val tags: List<FoodTag> = emptyList()
)
```

- [ ] **Step 4: 修改 FoodEntity，新增 tags 字段**

```kotlin
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
```

- [ ] **Step 5: 修改 FoodDatabase，升级版本 + 注册 TypeConverter**

```kotlin
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
```

在 `DatabaseModule.kt` 的 `provideFoodDatabase` 中添加迁移：

```kotlin
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
```

需要在 `DatabaseModule.kt` 顶部添加 import：
```kotlin
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
```

- [ ] **Step 6: 修改 FoodDao，新增按标签查询**

在 `FoodDao` 接口中新增：

```kotlin
@Query("SELECT * FROM foods WHERE tags LIKE '%' || :tagName || '%' ORDER BY id DESC")
fun getFoodsByTag(tagName: String): Flow<List<FoodEntity>>
```

- [ ] **Step 7: 修改 FoodRepository 接口，新增按标签查询**

在 `FoodRepository` 接口中新增：

```kotlin
fun getFoodsByTag(tag: FoodTag): Flow<List<Food>>
```

- [ ] **Step 8: 修改 FoodRepositoryImpl，映射 tags 字段 + 实现按标签查询**

更新 `toDomain()`:
```kotlin
private fun FoodEntity.toDomain(): Food {
    val converter = FoodTagTypeConverter()
    return Food(
        id = id,
        name = name,
        category = category,
        imageUrl = imageUrl,
        weight = weight,
        tags = converter.toFoodTags(tags)
    )
}
```

更新 `toEntity()`:
```kotlin
private fun Food.toEntity(): FoodEntity {
    val converter = FoodTagTypeConverter()
    return FoodEntity(
        id = id,
        name = name,
        category = category,
        imageUrl = imageUrl,
        weight = weight,
        tags = converter.fromFoodTags(tags)
    )
}
```

新增方法：
```kotlin
override fun getFoodsByTag(tag: FoodTag): Flow<List<Food>> {
    return foodDao.getFoodsByTag(tag.name).map { entities ->
        entities.map { it.toDomain() }
    }
}
```

- [ ] **Step 9: 修改 FoodDataSeeder，为预设数据补充标签**

更新 `defaultFoods` 列表，为每个 FoodEntity 添加 tags 字段。更新 seeder 的 `execSQL` 以包含 tags 列：

```kotlin
db.execSQL(
    """INSERT INTO foods (name, category, imageUrl, weight, tags)
       VALUES (?, ?, ?, ?, ?)""",
    arrayOf(food.name, food.category, food.imageUrl, food.weight, food.tags)
)
```

- [ ] **Step 10: 构建验证**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 11: Commit**

```bash
git add -A
git commit -m "feat: 添加美食标签数据模型与数据库迁移(v1->v2)"
```

---

## Task 2: 智能推荐核心逻辑

**Files:**
- Create: `app/src/main/java/com/eatif/app/domain/model/TimeSlot.kt`
- Create: `app/src/main/java/com/eatif/app/domain/model/Recommendation.kt`
- Create: `app/src/main/java/com/eatif/app/domain/model/FoodFrequency.kt`
- Create: `app/src/main/java/com/eatif/app/domain/repository/RecommendRepository.kt`
- Create: `app/src/main/java/com/eatif/app/data/repository/RecommendRepositoryImpl.kt`
- Create: `app/src/main/java/com/eatif/app/domain/usecase/SmartRecommendUseCase.kt`
- Modify: `app/src/main/java/com/eatif/app/data/local/HistoryDao.kt`
- Modify: `app/src/main/java/com/eatif/app/domain/repository/HistoryRepository.kt`
- Modify: `app/src/main/java/com/eatif/app/data/repository/HistoryRepositoryImpl.kt`
- Modify: `app/src/main/java/com/eatif/app/di/DatabaseModule.kt`

- [ ] **Step 1: 创建 TimeSlot 枚举**

```kotlin
// app/src/main/java/com/eatif/app/domain/model/TimeSlot.kt
package com.eatif.app.domain.model

enum class TimeSlot(val label: String, val emoji: String, val hours: IntRange) {
    MORNING("早餐", "🌅", 6..10),
    LUNCH("午餐", "☀️", 11..14),
    DINNER("晚餐", "🌆", 17..21),
    LATE_NIGHT("夜宵", "🌙", 22..5);

    companion object {
        fun current(): TimeSlot {
            val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
            return entries.find { hour in it.hours } ?: LATE_NIGHT
        }
    }
}
```

- [ ] **Step 2: 创建 Recommendation 和 FoodFrequency 模型**

```kotlin
// app/src/main/java/com/eatif/app/domain/model/Recommendation.kt
package com.eatif.app.domain.model

data class Recommendation(
    val food: Food,
    val reason: String,
    val score: Double
)
```

```kotlin
// app/src/main/java/com/eatif/app/domain/model/FoodFrequency.kt
package com.eatif.app.domain.model

data class FoodFrequency(val foodName: String, val count: Int)
```

- [ ] **Step 3: 修改 HistoryDao，新增频次统计查询**

在 `HistoryDao` 接口中新增内部数据类和查询方法：

```kotlin
data class FoodFrequencyEntity(val foodName: String, val count: Int)

@Query("SELECT foodName, COUNT(*) as count FROM history WHERE timestamp >= :fromTimestamp GROUP BY foodName ORDER BY count DESC")
fun getFoodFrequencySinceRaw(fromTimestamp: Long): Flow<List<FoodFrequencyEntity>>

@Query("SELECT foodName, COUNT(*) as count FROM history WHERE timestamp >= :fromTimestamp AND timestamp <= :toTimestamp GROUP BY foodName ORDER BY count DESC")
fun getFoodFrequencyBetweenRaw(fromTimestamp: Long, toTimestamp: Long): Flow<List<FoodFrequencyEntity>>
```

- [ ] **Step 4: 修改 HistoryRepository 接口，新增频次查询**

```kotlin
fun getFoodFrequencySince(fromTimestamp: Long): Flow<List<FoodFrequency>>
fun getFoodFrequencyBetween(fromTimestamp: Long, toTimestamp: Long): Flow<List<FoodFrequency>>
```

- [ ] **Step 5: 修改 HistoryRepositoryImpl，实现频次查询映射**

```kotlin
override fun getFoodFrequencySince(fromTimestamp: Long): Flow<List<FoodFrequency>> {
    return historyDao.getFoodFrequencySinceRaw(fromTimestamp).map { list ->
        list.map { FoodFrequency(it.foodName, it.count) }
    }
}

override fun getFoodFrequencyBetween(fromTimestamp: Long, toTimestamp: Long): Flow<List<FoodFrequency>> {
    return historyDao.getFoodFrequencyBetweenRaw(fromTimestamp, toTimestamp).map { list ->
        list.map { FoodFrequency(it.foodName, it.count) }
    }
}
```

- [ ] **Step 6: 创建 RecommendRepository 接口**

```kotlin
// app/src/main/java/com/eatif/app/domain/repository/RecommendRepository.kt
package com.eatif.app.domain.repository

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.FoodFrequency
import kotlinx.coroutines.flow.Flow

interface RecommendRepository {
    fun getFoodFrequencySince(fromTimestamp: Long): Flow<List<FoodFrequency>>
    fun getFoodFrequencyBetween(fromTimestamp: Long, toTimestamp: Long): Flow<List<FoodFrequency>>
    fun getAllFoods(): Flow<List<Food>>
}
```

- [ ] **Step 7: 创建 RecommendRepositoryImpl**

```kotlin
// app/src/main/java/com/eatif/app/data/repository/RecommendRepositoryImpl.kt
package com.eatif.app.data.repository

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.FoodFrequency
import com.eatif.app.domain.repository.FoodRepository
import com.eatif.app.domain.repository.HistoryRepository
import com.eatif.app.domain.repository.RecommendRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class RecommendRepositoryImpl @Inject constructor(
    private val historyRepository: HistoryRepository,
    private val foodRepository: FoodRepository
) : RecommendRepository {

    override fun getFoodFrequencySince(fromTimestamp: Long): Flow<List<FoodFrequency>> {
        return historyRepository.getFoodFrequencySince(fromTimestamp)
    }

    override fun getFoodFrequencyBetween(fromTimestamp: Long, toTimestamp: Long): Flow<List<FoodFrequency>> {
        return historyRepository.getFoodFrequencyBetween(fromTimestamp, toTimestamp)
    }

    override fun getAllFoods(): Flow<List<Food>> {
        return foodRepository.getAllFoods()
    }
}
```

- [ ] **Step 8: 创建 SmartRecommendUseCase**

```kotlin
// app/src/main/java/com/eatif/app/domain/usecase/SmartRecommendUseCase.kt
package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.Recommendation
import com.eatif.app.domain.model.TimeSlot
import com.eatif.app.domain.repository.RecommendRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.util.Calendar
import javax.inject.Inject
import kotlin.random.Random

class SmartRecommendUseCase @Inject constructor(
    private val repository: RecommendRepository
) {
    operator fun invoke(count: Int = 5): Flow<Result<List<Recommendation>>> {
        val currentTimeSlot = TimeSlot.current()
        val threeDaysAgo = System.currentTimeMillis() - THREE_DAYS_MILLIS
        val timeRange = getTimeRangeForSlot(currentTimeSlot)

        return combine(
            repository.getFoodFrequencyBetween(timeRange.first, timeRange.second),
            repository.getFoodFrequencySince(threeDaysAgo),
            repository.getAllFoods()
        ) { slotFrequencies, recentFrequencies, foods ->
            if (foods.isEmpty()) {
                Result.failure(IllegalStateException("美食库为空"))
            } else {
                val recentNames = recentFrequencies.map { it.foodName }.toSet()
                val slotFreqMap = slotFrequencies.associate { it.foodName to it.count }
                val scored = foods.map { food ->
                    val slotScore = (slotFreqMap[food.name] ?: 0).toDouble()
                    val tagBonus = calculateTagBonus(food, slotFreqMap, foods)
                    val recentPenalty = if (food.name in recentNames) RECENT_PENALTY else 0.0
                    val weightBonus = food.weight * WEIGHT_MULTIPLIER
                    val randomFactor = Random.nextDouble(RANDOM_MIN, RANDOM_MAX)
                    val totalScore = slotScore + tagBonus + recentPenalty + weightBonus + randomFactor
                    val reason = buildReason(food, currentTimeSlot, slotFreqMap)
                    Recommendation(food = food, reason = reason, score = totalScore)
                }
                Result.success(scored.sortedByDescending { it.score }.take(count))
            }
        }
    }

    private fun calculateTagBonus(food: Food, slotFreqMap: Map<String, Int>, allFoods: List<Food>): Double {
        if (food.tags.isEmpty()) return 0.0
        val sameTagFoods = allFoods.filter { it.tags.any { tag -> tag in food.tags } }
        val sameTagFreq = sameTagFoods.sumOf { slotFreqMap[it.name] ?: 0 }
        return sameTagFreq * TAG_BONUS_MULTIPLIER
    }

    private fun buildReason(food: Food, timeSlot: TimeSlot, slotFreqMap: Map<String, Int>): String {
        val freq = slotFreqMap[food.name] ?: 0
        return when {
            freq >= 3 -> "${timeSlot.emoji} ${timeSlot.label}常选"
            freq >= 1 -> "${timeSlot.emoji} ${timeSlot.label}偶尔吃"
            food.tags.isNotEmpty() -> "${food.tags.first().emoji} ${food.tags.first().label}类"
            else -> "为你推荐"
        }
    }

    private fun getTimeRangeForSlot(slot: TimeSlot): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.set(Calendar.HOUR_OF_DAY, slot.hours.first)
        val start = cal.timeInMillis
        val end = start + (slot.hours.last - slot.hours.first + 1) * 3600_000L
        return Pair(start, end)
    }

    companion object {
        private const val THREE_DAYS_MILLIS = 3L * 24 * 3600_000L
        private const val RECENT_PENALTY = -5.0
        private const val WEIGHT_MULTIPLIER = 0.5
        private const val TAG_BONUS_MULTIPLIER = 0.3
        private const val RANDOM_MIN = 0.0
        private const val RANDOM_MAX = 2.0
    }
}
```

- [ ] **Step 9: 在 DatabaseModule 中注册 RecommendRepository**

```kotlin
@Provides
@Singleton
fun provideRecommendRepository(
    historyRepository: HistoryRepository,
    foodRepository: FoodRepository
): RecommendRepository {
    return RecommendRepositoryImpl(historyRepository, foodRepository)
}
```

- [ ] **Step 10: 构建验证**

Run: `./gradlew assembleDebug`
Expected: BUILD SUCCESSFUL

- [ ] **Step 11: Commit**

```bash
git add -A
git commit -m "feat: 添加智能推荐核心逻辑(时段分析+权重+去重)"
```

---

## Task 3: Home 页面"猜你想吃"推荐区域

**Files:**
- Modify: `app/src/main/java/com/eatif/app/ui/screens/HomeViewModel.kt`
- Modify: `app/src/main/java/com/eatif/app/ui/screens/HomeScreen.kt`

- [ ] **Step 1: 修改 HomeViewModel，注入 SmartRecommendUseCase**

新增 `recommendations` StateFlow 和 `loadRecommendations()` 方法。注入 `SmartRecommendUseCase`，在 init 中调用 `loadRecommendations()`。

- [ ] **Step 2: 修改 HomeScreen，新增"猜你想吃"横向卡片列表**

在模式选择按钮之前插入推荐区域：标题"猜你想吃" + "换一批"按钮 + LazyRow 横向卡片。新增 `RecommendationCard` composable 显示美食名 + 推荐理由。

- [ ] **Step 3: 构建验证 + Commit**

```bash
git add -A && git commit -m "feat: Home页面新增猜你想吃智能推荐区域"
```

---

## Task 4: 美食库标签筛选与编辑

**Files:**
- Modify: `app/src/main/java/com/eatif/app/ui/screens/FoodLibraryViewModel.kt`
- Modify: `app/src/main/java/com/eatif/app/ui/screens/FoodLibraryScreen.kt`

- [ ] **Step 1: 修改 FoodLibraryViewModel**

新增 `selectedTag` StateFlow、`filteredFoods` (combine foods + selectedTag)、`selectTag()`、`updateTags()` 方法。注入 `UpdateFoodUseCase`。

- [ ] **Step 2: 修改 FoodLibraryScreen**

- 顶部新增标签筛选栏（LazyRow + FilterChip）
- FoodItem 显示标签 chips + 新增标签编辑按钮
- 新增 `EditTagsDialog`（Checkbox 多选标签）
- `AddFoodDialog` 支持选择标签
- 将 `foods` 改为 `filteredFoods`

- [ ] **Step 3: 构建验证 + Commit**

```bash
git add -A && git commit -m "feat: 美食库新增标签筛选与编辑功能"
```

---

## Task 5: 桌面小组件（随机推荐卡片）

**Files:**
- Create: `app/src/main/java/com/eatif/app/ui/widget/FoodWidget.kt`
- Create: `app/src/main/java/com/eatif/app/ui/widget/FoodWidgetReceiver.kt`
- Create: `app/src/main/res/xml/food_widget_info.xml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/main/AndroidManifest.xml`

- [ ] **Step 1: 添加 Glance 依赖到 build.gradle.kts**

```kotlin
implementation("androidx.glance:glance-appwidget:1.0.0")
implementation("androidx.glance:glance-material3:1.0.0")
```

- [ ] **Step 2: 创建 Widget 配置 XML**

```xml
<!-- app/src/main/res/xml/food_widget_info.xml -->
<appwidget-provider xmlns:android="http://schemas.android.com/apk/res/android"
    android:minWidth="180dp"
    android:minHeight="110dp"
    android:updatePeriodMillis="1800000"
    android:description="@string/widget_description"
    android:previewLayout="@layout/widget_preview"
    android:widgetCategory="home_screen" />
```

- [ ] **Step 3: 创建 FoodWidget (Glance AppWidget)**

使用 Glance Composable 定义 Widget UI：显示美食名称 + "换一个" ActionButton。通过 `GlanceAppWidget` 和 `provideGlance` 实现。

- [ ] **Step 4: 创建 FoodWidgetReceiver**

处理"换一个"点击广播，刷新 Widget 数据。

- [ ] **Step 5: 在 AndroidManifest.xml 注册 Receiver**

- [ ] **Step 6: 构建验证 + Commit**

```bash
git add -A && git commit -m "feat: 添加桌面随机推荐小组件"
```

---

## Task 6: 主题色自定义

**Files:**
- Modify: `app/src/main/java/com/eatif/app/ui/theme/Color.kt`
- Modify: `app/src/main/java/com/eatif/app/ui/theme/ThemeManager.kt`
- Modify: `app/src/main/java/com/eatif/app/ui/theme/Theme.kt`
- Modify: `app/src/main/java/com/eatif/app/ui/screens/SettingsScreen.kt`
- Modify: `app/src/main/java/com/eatif/app/MainActivity.kt`

- [ ] **Step 1: 在 Color.kt 新增预设主题色**

```kotlin
val ThemeColors = mapOf(
    "orange" to Color(0xFFFF6B35),
    "green" to Color(0xFF34C759),
    "blue" to Color(0xFF007AFF),
    "purple" to Color(0xFFAF52DE),
    "pink" to Color(0xFFFF2D55),
    "red" to Color(0xFFFF3B30),
    "teal" to Color(0xFF5AC8FA),
    "yellow" to Color(0xFFFFCC00)
)
```

- [ ] **Step 2: 扩展 ThemeManager，新增 seedColor 属性**

```kotlin
var seedColor: String
    get() = prefs.getString(KEY_SEED_COLOR, "orange") ?: "orange"
    set(value) {
        prefs.edit().putString(KEY_SEED_COLOR, value).apply()
    }
```

- [ ] **Step 3: 修改 Theme.kt，支持动态 seedColor 生成色板**

`EatIfTheme` 新增 `seedColor` 参数，使用 Material 3 `dynamicColorScheme()` 或手动从 seedColor 生成 light/dark color scheme。

- [ ] **Step 4: 修改 SettingsScreen，新增主题色选择器**

在"外观"Card 中新增圆形色块网格，点击切换主题色。

- [ ] **Step 5: 修改 MainActivity，传递 seedColor 到 Theme**

- [ ] **Step 6: 构建验证 + Commit**

```bash
git add -A && git commit -m "feat: 新增主题色自定义(8种预设色)"
```

---

## Task 7: 自定义游戏规则

**Files:**
- Create: `app/src/main/java/com/eatif/app/domain/model/GameRuleConfig.kt`
- Create: `app/src/main/java/com/eatif/app/ui/screens/GameRuleScreen.kt`
- Modify: `app/src/main/java/com/eatif/app/ui/settings/GameSettingsManager.kt`
- Modify: `app/src/main/java/com/eatif/app/ui/screens/GameSelectScreen.kt`
- Modify: `app/src/main/java/com/eatif/app/ui/navigation/Screen.kt`
- Modify: `app/src/main/java/com/eatif/app/ui/navigation/NavHost.kt`

- [ ] **Step 1: 创建 GameRuleConfig 模型**

```kotlin
// app/src/main/java/com/eatif/app/domain/model/GameRuleConfig.kt
package com.eatif.app.domain.model

data class GameRuleConfig(
    val gameId: String,
    val rounds: Int = 3,
    val timeLimit: Int = 0,
    val customParams: Map<String, String> = emptyMap()
)
```

- [ ] **Step 2: 扩展 GameSettingsManager，新增游戏规则存取**

```kotlin
fun getGameRuleConfig(gameId: String): GameRuleConfig
fun setGameRuleConfig(config: GameRuleConfig)
```

使用 Gson 序列化 GameRuleConfig 到 SharedPreferences。

- [ ] **Step 3: 创建 GameRuleScreen**

编辑页面：轮数 Slider + 时间限制 Slider + 保存按钮。根据 gameId 加载当前配置。

- [ ] **Step 4: 修改 Screen.kt，新增 GameRule 路由**

```kotlin
data object GameRule : Screen("game_rule/{gameId}") {
    fun createRoute(gameId: String) = "game_rule/$gameId"
}
```

- [ ] **Step 5: 修改 NavHost.kt，注册 GameRule 页面**

- [ ] **Step 6: 修改 GameSelectScreen，新增规则入口图标**

每个游戏卡片新增设置图标，点击跳转到 GameRule 页面。

- [ ] **Step 7: 构建验证 + Commit**

```bash
git add -A && git commit -m "feat: 新增自定义游戏规则配置"
```
