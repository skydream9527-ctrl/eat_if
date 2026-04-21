# Eat If Phase 2 完整规划文档

> **整合版本：** 2026-04-20  
> **前置文档：** Phase 1 实施计划 (`2026-04-12-food-roulette-plan.md`)  
> **源文档：** `2026-04-17-feature-expansion.md` + `2026-04-20-game-depth.md` + `2026-04-20-phase2-game-depth-completion.md`  
> **For agentic workers:** REQUIRED SUB-SKILL: Use `subagent-driven-development` or `executing-plans` 来执行本计划。

---

## 1. 概述

### 1.1 Phase 1 状态 (已完成)

| 模块 | 状态 |
|------|------|
| 项目骨架 + Gradle 配置 | ✅ |
| MVVM + Clean Architecture | ✅ |
| Navigation Compose (8 个页面) | ✅ |
| 15 款小游戏 | ✅ |
| 美食库 CRUD | ✅ |
| 历史记录追踪 | ✅ |
| 难度选择器 | ✅ |
| 教程/引导系统 | ✅ |
| 美团 API 集成框架 | ✅ |
| Material 3 主题 (橙色品牌) | ✅ |
| 单人/双人模式 | ✅ |

### 1.2 Phase 2 目标

Phase 2 整合两大功能模块 + 缺陷修复，将应用从"能玩"提升为"好玩且个性化"：

1. **Part A: 功能扩展** — 智能推荐、标签系统、桌面小组件、主题色自定义、游戏规则配置
2. **Part B: 游戏内容深化** — 成就系统、关卡系统、经验等级、皮肤系统、统计排行
3. **Part C: 缺陷修复与补全** — 修复已知的 9 个集成 Bug，打通数据流

### 1.3 技术栈

| 层级 | 技术 |
|------|------|
| 语言 | Kotlin 1.9.20 |
| UI | Jetpack Compose + Material 3 |
| 架构 | MVVM + Clean Architecture |
| DI | Hilt 2.48.1 |
| 数据库 | Room 2.6.1 (v3, 7 表) |
| 网络 | Retrofit 2.9.0 + OkHttp |
| 导航 | Navigation Compose 2.7.6 |
| 小组件 | Jetpack Glance 1.0.0 |
| 异步 | Kotlin Coroutines + Flow |
| 持久化 | SharedPreferences |

### 1.4 里程碑

| 里程碑 | 包含内容 | 验收标准 |
|--------|---------|---------|
| M1: 标签+推荐 | A1-A3 | Home 页展示"猜你想吃"，推荐基于时段和标签 |
| M2: 个性化 | A4-A7 | 标签筛选可用、小组件显示美食、8色主题可选、游戏规则可配置 |
| M3: 数据层 | B1 | Room v3 迁移成功，7 表可用，无数据丢失 |
| M4: 成长系统 | B2-B6 | 成就可解锁、XP 累计升级、关卡可解锁、皮肤可切换、统计准确 |
| M5: UI 集成 | B7 + C | 所有页面接入真实数据，Play 时长准确，"再来一局"正确跳转 |

---

## 2. 架构总览

### 2.1 数据流

```
┌─────────────────────────────────────────────────────┐
│                    UI Layer (Compose)                │
│  HomeScreen / PlayScreen / ResultScreen / GameSelect │
│  / FoodLibraryScreen / SettingsScreen / StatsScreen  │
│  / AchievementScreen / ProfileScreen / LevelSelect   │
│  / SkinSelectorScreen / GameRuleScreen               │
│  / FoodWidget (Glance)                               │
└──────────────────────┬──────────────────────────────┘
                       │ StateFlow / Compose State
┌──────────────────────▼──────────────────────────────┐
│                 ViewModel Layer                       │
│  HomeViewModel / PlayViewModel / FoodLibraryViewModel│
│  / StatsViewModel / AchievementViewModel             │
│  / ProfileViewModel / LevelSelectViewModel           │
└──────────────────────┬──────────────────────────────┘
                       │ UseCase 调用
┌──────────────────────▼──────────────────────────────┐
│                 Domain Layer                          │
│  UseCases: SmartRecommend, PlayerProfile,            │
│  AchievementEngine, LevelManager, SkinResolver,      │
│  StatsUseCase, AchievementRegistry, GameLevelRegistry│
│                                                      │
│  Models: Food, FoodTag, Recommendation, TimeSlot,    │
│  PlayerProfile, GameStats, Achievement, GameLevel,   │
│  Skin, LevelProgress, SkinCollection, GameRuleConfig  │
│                                                      │
│  Repositories: FoodRepository, HistoryRepository,     │
│  RecommendRepository, GameStatsRepository            │
└──────────────────────┬──────────────────────────────┘
                       │ DAO 调用
┌──────────────────────▼──────────────────────────────┐
│                 Data Layer                            │
│  Room Database (v3) — 7 Tables:                      │
│    foods, history, player_profile, game_stats,       │
│    achievement_progress, level_progress, skin_collection│
│                                                      │
│  SharedPreferences: GameSettingsManager,             │
│  ThemeManager, SkinSettingsManager                   │
│                                                      │
│  Remote: MeituanApiClient                            │
└─────────────────────────────────────────────────────┘
```

### 2.2 数据库表结构 (v3)

| 表名 | 字段 | 用途 |
|------|------|------|
| `foods` | id, name, category, imageUrl, weight, tags | 用户美食库 (含标签) |
| `history` | id, foodName, gameId, score, timestamp | 游戏结果历史 |
| `player_profile` | id, totalGamesPlayed, totalPlayTimeSeconds, currentStreak, maxStreak, playerLevel, playerXP, lastPlayedDate | 玩家档案 |
| `game_stats` | id, gameId, foodName, score, scorePercent, difficulty, level, playTimeSeconds, timestamp | 每局统计 |
| `achievement_progress` | id, achievementId, currentProgress, requiredProgress, isUnlocked, unlockedAt | 成就进度 |
| `level_progress` | id, gameId, currentLevel, stars, bestScores | 关卡进度 |
| `skin_collection` | id, skinId, gameId, isUnlocked, isActive | 皮肤收集 |

### 2.3 模块依赖关系

```
A1. 美食标签 ──────► A2. 智能推荐 ──────► A3. Home 推荐区域
                          │
                          ▼
                    A5. 桌面小组件

A1. 美食标签 ──────► A4. 美食库标签筛选

A6. 主题色 (独立)     A7. 游戏规则 (独立)

────────────────────────────────────────
Part B 依赖 Part A 的标签数据和推荐逻辑
────────────────────────────────────────

B1. 数据层 ───► B2. 成就系统 ───┐
             B3. XP/等级系统 ───┤
             B4. 关卡系统 ──────┼──► B7. UI 集成 ◄── A7. 游戏规则
             B5. 皮肤系统 ──────┘        │
             B6. 统计系统 ───────────────┘
                                         ▼
                                   Part C. 缺陷修复
```

---

## 3. Part A: 功能扩展

### A1. 美食标签数据模型与数据库迁移

**状态：** 已完成 (DB v2 迁移)  
**涉及文件：** 10 个

| 操作 | 文件 | 职责 |
|------|------|------|
| Create | `domain/model/FoodTag.kt` | 10 种标签枚举 (SPICY, SWEET, FAST_FOOD, HOTPOT, BBQ, NOODLE, RICE, LIGHT, SEAFOOD, DESSERT) |
| Create | `data/local/FoodTagTypeConverter.kt` | Room TypeConverter，List<FoodTag> ↔ JSON |
| Modify | `domain/model/Food.kt` | 新增 tags: List<FoodTag> |
| Modify | `data/local/FoodEntity.kt` | 新增 tags: String = "[]" |
| Modify | `data/local/FoodDao.kt` | 新增 getFoodsByTag() |
| Modify | `data/local/FoodDatabase.kt` | v1→v2 迁移 |
| Modify | `data/local/FoodDataSeeder.kt` | 预设数据补充标签 |
| Modify | `data/repository/FoodRepositoryImpl.kt` | tags 字段映射 |
| Modify | `domain/repository/FoodRepository.kt` | 新增 getFoodsByTag() |
| Modify | `di/DatabaseModule.kt` | 注册迁移 + TypeConverter |

**验证：** `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL

---

### A2. 智能推荐核心逻辑

**状态：** 已完成  
**涉及文件：** 9 个

| 操作 | 文件 | 职责 |
|------|------|------|
| Create | `domain/model/TimeSlot.kt` | 时段枚举 (MORNING 6-10, LUNCH 11-14, DINNER 17-21, LATE_NIGHT 22-5) |
| Create | `domain/model/Recommendation.kt` | 推荐结果 (food, reason, score) |
| Create | `domain/model/FoodFrequency.kt` | 频次统计 (foodName, count) |
| Create | `domain/repository/RecommendRepository.kt` | 推荐仓库接口 |
| Create | `data/repository/RecommendRepositoryImpl.kt` | 推荐仓库实现 (聚合 HistoryRepository + FoodRepository) |
| Create | `domain/usecase/SmartRecommendUseCase.kt` | 推荐算法入口 |
| Modify | `data/local/HistoryDao.kt` | 新增频次统计查询 |
| Modify | `domain/repository/HistoryRepository.kt` | 新增频次查询接口 |
| Modify | `data/repository/HistoryRepositoryImpl.kt` | 映射频次查询 |
| Modify | `di/DatabaseModule.kt` | 注册 RecommendRepository |

**推荐算法：**

1. **时段分析** — 统计当前时段的历史美食频次
2. **标签亲和** — 同标签美食获得额外加分
3. **去重** — 排除最近 3 天已选过的美食 (-5 分惩罚)
4. **权重** — 美食 weight × 0.5
5. **随机扰动** — 0.0~2.0 随机因子，避免推荐固化
6. **兜底** — 无历史数据时回退到全局随机

**评分公式：**
```
score = slotFrequency + tagBonus + recentPenalty + weightBonus + randomFactor
```

---

### A3. Home 页面"猜你想吃"推荐区域

**状态：** 已完成  
**涉及文件：** 2 个

| 操作 | 文件 | 变更 |
|------|------|------|
| Modify | `ui/screens/HomeViewModel.kt` | 注入 SmartRecommendUseCase，新增 recommendations StateFlow |
| Modify | `ui/screens/HomeScreen.kt` | 新增横向卡片列表 + "换一批"按钮 |

**UI 组件：** `RecommendationCard` — 显示美食名 + 推荐理由

---

### A4. 美食库标签筛选与编辑

**状态：** 已完成  
**涉及文件：** 2 个

| 操作 | 文件 | 变更 |
|------|------|------|
| Modify | `ui/screens/FoodLibraryViewModel.kt` | 新增 selectedTag, filteredFoods, updateTags() |
| Modify | `ui/screens/FoodLibraryScreen.kt` | 标签筛选栏 + 标签编辑 Dialog |

**UI 变更：**
- 顶部 LazyRow FilterChip 标签筛选
- FoodItem 显示标签 chips
- `EditTagsDialog` — Checkbox 多选
- `AddFoodDialog` 支持选择标签

---

### A5. 桌面小组件

**状态：** 已完成 (框架搭建)  
**涉及文件：** 5 个

| 操作 | 文件 | 职责 |
|------|------|------|
| Modify | `app/build.gradle.kts` | 新增 glance-appwidget + glance-material3 依赖 |
| Create | `ui/widget/FoodWidget.kt` | Glance AppWidget UI |
| Create | `ui/widget/FoodWidgetReceiver.kt` | 广播接收器 |
| Create | `res/xml/food_widget_info.xml` | Widget 配置 (180dp × 110dp) |
| Modify | `AndroidManifest.xml` | 注册 Receiver |

**功能：** 美食名称 + 分类图标 + "换一个"按钮，点击卡片打开 App

---

### A6. 主题色自定义

**状态：** 已完成  
**涉及文件：** 5 个

| 操作 | 文件 | 变更 |
|------|------|------|
| Modify | `ui/theme/Color.kt` | 新增 8 种预设色 |
| Modify | `ui/theme/ThemeManager.kt` | 新增 seedColor 属性 |
| Modify | `ui/theme/Theme.kt` | 动态色板生成 |
| Modify | `ui/screens/SettingsScreen.kt` | 圆形色块选择器 |
| Modify | `MainActivity.kt` | 传递 seedColor 到 Theme |

**预设色：** 橙/绿/蓝/紫/粉/红/青/黄

---

### A7. 自定义游戏规则

**状态：** 已完成 (基础配置)  
**涉及文件：** 6 个

| 操作 | 文件 | 职责 |
|------|------|------|
| Create | `domain/model/GameRuleConfig.kt` | 规则模型 (gameId, rounds, timeLimit, customParams) |
| Modify | `ui/settings/GameSettingsManager.kt` | 新增规则存取 (Gson 序列化到 SharedPreferences) |
| Create | `ui/screens/GameRuleScreen.kt` | 规则编辑页面 |
| Modify | `ui/navigation/Screen.kt` | 新增 GameRule 路由 |
| Modify | `ui/navigation/NavHost.kt` | 注册 GameRule 页面 |
| Modify | `ui/screens/GameSelectScreen.kt` | 游戏卡片新增设置图标入口 |

---

## 4. Part B: 游戏内容深化

### B1. 数据层 + 数据库迁移 (v2→v3)

**状态：** 已完成  
**涉及文件：** 15 个

**新增领域模型 (8 个)：**

| 文件 | 职责 |
|------|------|
| `domain/model/PlayerProfile.kt` | 玩家档案 (总游戏数、时长、连胜、等级、XP) |
| `domain/model/GameStats.kt` | 单局统计 (游戏ID、分数、难度、关卡、时长) |
| `domain/model/Achievement.kt` | 成就定义 (条件、奖励、关联皮肤) |
| `domain/model/AchievementProgress.kt` | 成就进度追踪 |
| `domain/model/GameLevel.kt` | 关卡定义 (难度、所需星数、参数) |
| `domain/model/LevelProgress.kt` | 关卡进度 (当前关卡、星数、高分) |
| `domain/model/Skin.kt` | 皮肤定义 (稀有度、解锁方式) |
| `domain/model/SkinCollection.kt` | 皮肤收集状态 (已解锁/已激活) |

**新增 Room 实体 (5 个)：**

| 文件 | 对应表 |
|------|--------|
| `data/local/PlayerProfileEntity.kt` | player_profile |
| `data/local/GameStatsEntity.kt` | game_stats |
| `data/local/AchievementProgressEntity.kt` | achievement_progress |
| `data/local/LevelProgressEntity.kt` | level_progress |
| `data/local/SkinCollectionEntity.kt` | skin_collection |

**新增 DAO (5 个) + TypeConverters + Migration 2→3**

---

### B2. 成就系统

**状态：** 已完成  
**涉及文件：** 4 个

| 文件 | 职责 |
|------|------|
| `domain/usecase/AchievementRegistry.kt` | 定义 15 个成就 |
| `domain/usecase/AchievementEngine.kt` | 成就检测引擎 (游戏结束时触发) |
| `ui/components/AchievementUnlockDialog.kt` | 成就解锁弹窗 |
| `ui/screens/AchievementScreen.kt` + ViewModel | 成就列表页面 |

**成就条件类型：**

| 条件 | 说明 | 示例 |
|------|------|------|
| `TotalGames(count)` | 累计游戏次数 | 玩 10 次、50 次、100 次 |
| `GameHighScore(gameId, score)` | 单游戏高分 | 贪吃蛇达到 80% |
| `ConsecutiveDays(days)` | 连续游玩天数 | 连续 3 天、7 天 |
| `PlayAllGames(count)` | 玩过所有游戏 | 所有 15 款各玩一次 |
| `TotalPlayTime(seconds)` | 累计游玩时长 | 玩满 1 小时、5 小时 |

**触发流程：**
```
游戏结束 → PlayViewModel.processGameEnd()
  → AchievementEngine.check(event)
    → 遍历 AchievementRegistry.all
      → 匹配条件 → 标记解锁 → 记录进度 → 返回解锁列表
        → PlayViewModel 传递到 ResultScreen
          → 显示 AchievementUnlockDialog
```

---

### B3. XP & 等级系统

**状态：** 已完成  
**涉及文件：** 2 个

| 文件 | 职责 |
|------|------|
| `domain/usecase/PlayerProfileUseCase.kt` | XP 计算 + 等级管理 |
| `ui/components/ProgressBar.kt` | XP 进度条组件 |

**XP 计算公式：**
```
XP = score × difficultyMultiplier + timeBonus
difficultyMultiplier: EASY=1.0, NORMAL=1.5, HARD=2.0
timeBonus: min(playTimeSeconds, 300) × 0.1
```

**等级公式：**
```
XP needed for level N = 100 × N
```

---

### B4. 关卡系统

**状态：** 已完成  
**涉及文件：** 4 个

| 文件 | 职责 |
|------|------|
| `domain/model/GameLevel.kt` | 关卡定义 (难度、所需星数、参数) |
| `domain/usecase/GameLevelRegistry.kt` | 定义各游戏关卡 (Snake/2048/Tetris 等各 10 关) |
| `domain/usecase/LevelManager.kt` | 关卡解锁/进度管理 |
| `ui/screens/LevelSelectScreen.kt` + ViewModel | 关卡选择页面 |

**关卡解锁规则：** 达到上一关所需星数即可解锁下一关

**星数计算：** 根据 scorePercent 评定
- ⭐⭐⭐: ≥ 80%
- ⭐⭐: ≥ 50%
- ⭐: ≥ 20%

---

### B5. 皮肤系统

**状态：** 已完成  
**涉及文件：** 5 个

| 文件 | 职责 |
|------|------|
| `domain/model/Skin.kt` | 皮肤定义 |
| `domain/usecase/SkinRegistry.kt` | 定义各游戏皮肤 |
| `domain/usecase/SkinResolver.kt` | 根据 gameId 解析当前激活皮肤 |
| `ui/settings/SkinSettingsManager.kt` | SharedPreferences 存储皮肤选择 |
| `ui/screens/SkinSelectorScreen.kt` | 皮肤选择页面 |

**皮肤稀有度：** COMMON, RARE, EPIC, LEGENDARY  
**解锁方式：** 成就解锁、等级解锁、默认皮肤

---

### B6. 统计系统

**状态：** 已完成  
**涉及文件：** 2 个

| 文件 | 职责 |
|------|------|
| `domain/usecase/StatsUseCase.kt` | 统计聚合 (总游戏数、时长、最高分、成就数) |
| `ui/screens/StatsScreen.kt` + ViewModel | 统计/排行页面 |

---

### B7. UI 集成

**状态：** 已完成  
**涉及文件：** 多个

| 集成点 | 文件 | 变更 |
|--------|------|------|
| 游戏结束处理 | `ui/screens/PlayViewModel.kt` | processGameEnd() 整合统计记录、XP 计算、成就检测 |
| 结果页 | `ui/screens/ResultScreen.kt` | 显示 XP 获得、等级提升、成就弹窗 |
| 首页 | `ui/screens/HomeScreen.kt` | 底部导航 + 玩家等级徽章 |
| 游戏选择 | `ui/screens/GameSelectScreen.kt` | 星级展示、高分显示、关卡入口 |
| 导航 | `ui/navigation/NavHost.kt` | 注册 Stats/Achievement/Profile/LevelSelect/SkinSelector 路由 |
| 导航 | `ui/navigation/Screen.kt` | 新增 6 个路由 |

---

## 5. Part C: 缺陷修复与补全

### C1. 核心逻辑 Bug 修复

| # | 问题 | 文件 | 严重度 | 修复方案 |
|---|------|------|--------|---------|
| 1 | AchievementEngine "any" 高分检查只查当前游戏 | `AchievementEngine.kt:46` | 🔴 | 改用 getGlobalTopScores() 查询全部游戏 |
| 2 | StatsUseCase achievementsUnlocked 硬编码为 0 | `StatsUseCase.kt:34` | 🔴 | 注入 AchievementProgressDao，查询已解锁数量 |
| 3 | LevelManager 星数索引差一错误 | `LevelManager.kt:58` | 🟡 | 修正 stars[lvl-1] 为正确的层级索引 |

### C2. 游戏卡片星级/高分展示

| 问题 | 文件 | 修复方案 |
|------|------|---------|
| GameSelectScreen 不显示星级和高分 | `GameSelectScreen.kt`, `GameCard.kt` | 新增 GameSelectViewModel 加载 LevelProgress + GameStats，GameCard 增加 stars/highScore 参数 |

### C3. PlayScreen 时长追踪

| 问题 | 文件 | 修复方案 |
|------|------|---------|
| playTimeSeconds 硬编码为 0 | `PlayScreen.kt:59` | 使用 DisposableEffect 记录开始时间，退出时计算实际时长 |

### C4. 皮肤/关卡参数传递

| 问题 | 文件 | 修复方案 |
|------|------|---------|
| 皮肤未应用到游戏 | 无集成层 | 创建 SkinParamsHolder，游戏中读取 activeSkin 应用颜色 |
| 关卡参数未传递到游戏 | 无集成层 | 创建 LevelParamsHolder，从 GameLevelRegistry 读取 params |

**集成模式：** 使用共享 Holder object (不破坏现有游戏签名)

### C5. ResultScreen "再来一局"导航修复

| 问题 | 文件 | 修复方案 |
|------|------|---------|
| "再来一局"返回首页而非重玩 | `ResultScreen.kt` | 创建 LastGameContextHolder，onPlayAgain 导航回 PlayScreen |
| Play 路由不支持关卡号 | `Screen.kt` | 路由改为 `play/{gameId}/{mode}/{levelNumber?}` |
| LevelSelect → Play 无跳转 | `NavHost.kt` | onLevelSelected 导航到带 levelNumber 的 Play 路由 |

---

## 6. 实施顺序

Phase 2 各模块存在依赖关系，推荐按以下顺序执行：

```
Part A (功能扩展):
  A1 美食标签 ──► A2 智能推荐 ──► A3 Home 推荐区域
                       │
                       ▼
                   A5 桌面小组件

  A1 美食标签 ──► A4 美食库标签筛选

  A6 主题色 (独立)    A7 游戏规则 (独立)

Part B (游戏深化):
  B1 数据层 ───► B2 成就系统 ───┐
              B3 XP/等级系统 ───┤
              B4 关卡系统 ──────┼──► B7 UI 集成
              B5 皮肤系统 ──────┘
              B6 统计系统 ──────┘

Part C (缺陷修复):
  C1 核心 Bug ──► C2 卡片展示 ──► C3 时长追踪 ──► C4 皮肤/关卡集成 ──► C5 导航修复
```

**注意：** 根据当前 git 历史，Part A 和 Part B 的核心功能代码已大部分完成，主要剩余工作集中在 Part C 的缺陷修复和数据流打通。

---

## 7. 路由总览

| 路由 | 页面 | 所属阶段 | 参数 |
|------|------|---------|------|
| `home` | HomeScreen | Phase 1 | - |
| `setup/{mode}` | SetupScreen | Phase 1 | mode: single/double |
| `game_select/{mode}` | GameSelectScreen | Phase 1 | mode |
| `play/{gameId}/{mode}/{levelNumber?}` | PlayScreen | Phase 1+2 | gameId, mode, levelNumber? |
| `result/{foodName}/{score}/{xp}/{level}` | ResultScreen | Phase 1+2 | foodName, score, xp, level |
| `settings` | SettingsScreen | Phase 1 | - |
| `food_library` | FoodLibraryScreen | Phase 1 | - |
| `history` | HistoryScreen | Phase 1 | - |
| `stats` | StatsScreen | Phase 2 | - |
| `achievements` | AchievementScreen | Phase 2 | - |
| `profile` | ProfileScreen | Phase 2 | - |
| `level_select/{gameId}` | LevelSelectScreen | Phase 2 | gameId |
| `skin_selector/{gameId}` | SkinSelectorScreen | Phase 2 | gameId |
| `game_rule/{gameId}` | GameRuleScreen | Phase 2 | gameId |

---

## 8. 风险与注意事项

### 8.1 数据库迁移

- v2→v3 迁移涉及 5 张新表，需确保 `fallbackToDestructiveMigration()` 在开发环境可用
- 生产环境需验证 `MIGRATION_2_3` SQL 正确性
- 现有玩家数据 (foods, history) 不应受影响

### 8.2 性能

- SmartRecommendUseCase 使用 combine 三流，需监控 Flow 发射频率
- Glance Widget 刷新间隔设置为 30 分钟，避免频繁查询数据库
- LevelProgressDao 和 GameStatsDao 查询需考虑索引优化

### 8.3 兼容性

- Min SDK 24 (Android 7.0)，确保所有新 API 兼容
- Glance 1.0.0 需 API 21+，兼容
- SharedPreferences 数据格式变更需考虑旧数据兼容

### 8.4 已知限制

- 皮肤集成使用 Holder object 模式，非依赖注入，不适合长期维护
- GameRuleConfig 的 customParams 为 Map<String, String>，复杂参数需序列化
- 美团 API 仅搭建框架，未接入真实数据

### 8.5 测试建议

- SmartRecommendUseCase 可独立单元测试 (无需 Android 环境)
- AchievementEngine 条件检测需覆盖 5 种条件类型
- 数据库迁移需编写 MigrationTest
- UI 测试建议覆盖: Home 推荐展示、Play 流程、Result 导航

---

## 9. 文件统计

| 类别 | 新增文件 | 修改文件 | 总计 |
|------|---------|---------|------|
| Part A: 功能扩展 | 7 | 15 | 22 |
| Part B: 游戏深化 | 25 | 8 | 33 |
| Part C: 缺陷修复 | 4 | 6 | 10 |
| **合计** | **36** | **29** | **65** |

> 实际文件数可能因代码拆分略有差异。

---

## 10. 后续 Phase 3 方向建议

Phase 2 完成后，可考虑以下方向：

1. **在线多人对战** — WebSocket 实时对战，排行榜云端同步
2. **美食地图** — 基于地理位置的美食推荐，美团 API 深度集成
3. **社交分享** — 游戏结果生成分享图，微信/微博分享
4. **每日挑战** — 每日一局特殊规则，全球排名
5. **数据可视化** — 饮食习惯分析，周/月/年统计报告
6. **AR 美食识别** — 拍照识别菜品，自动添加到美食库
