# Eat If 功能扩展设计

日期: 2026-04-17

## 概述

为"今天吃什么？"App 新增三大功能模块：智能推荐、桌面小组件、个性化定制。实现顺序：智能推荐 → 桌面小组件 → 个性化定制。

---

## 模块一：智能推荐（轻量本地统计）

### 目标

基于用户历史决策记录，按时段和偏好权重推荐美食，无需网络。

### 数据来源

- 已有 `HistoryEntity` 历史记录（含时间戳、美食名称）
- 新增 `FoodTag` 标签数据（配合模块三）

### 推荐算法

1. **时段分析**：将历史记录按小时分桶（早6-10 / 午11-14 / 晚17-21 / 夜22-5），统计各时段美食频次
2. **权重计算**：`score = frequency * timeSlotWeight * tagAffinity`
3. **去重**：排除最近 3 天已选过的美食
4. **随机扰动**：对 top-N 加入随机因子，避免推荐过于固定
5. **兜底**：无历史数据时回退到全局随机

### 新增文件

| 文件 | 职责 |
|------|------|
| `domain/usecase/SmartRecommendUseCase.kt` | 推荐入口，组合时段分析+去重+权重 |
| `domain/model/TimeSlot.kt` | 时段枚举（MORNING/LUNCH/DINNER/LATE_NIGHT） |
| `domain/model/Recommendation.kt` | 推荐结果模型（food + reason + score） |
| `data/repository/RecommendRepositoryImpl.kt` | 查询历史统计实现 |
| `domain/repository/RecommendRepository.kt` | 推荐仓库接口 |
| `ui/screens/HomeScreen.kt` | 新增"猜你想吃"卡片区域 |

### UI 变更

- Home 页新增"猜你想吃"横向卡片列表，显示 3-5 个推荐
- 每张卡片显示美食名 + 推荐理由（如"午餐常选"）
- "换一批"按钮刷新推荐

---

## 模块二：桌面小组件（随机推荐卡片）

### 目标

Android 桌面 Widget，一键查看随机美食推荐。

### 技术方案

- 使用 `Glance` (Jetpack Glance AppWidget) 构建小组件
- `AppWidgetProvider` 管理生命周期
- `RemoteViews` 或 Glance Composable 定义布局

### 功能

- 卡片显示：美食名称 + 分类图标 + "换一个"按钮
- 点击卡片 → 打开 App Home 页
- 点击"换一个" → 刷新推荐（通过 PendingIntent 广播）
- 浅色/深色模式自适应

### 新增文件

| 文件 | 职责 |
|------|------|
| `ui/widget/FoodWidget.kt` | Glance AppWidget UI 定义 |
| `ui/widget/FoodWidgetReceiver.kt` | 广播接收器，处理"换一个"点击 |
| `ui/widget/FoodWidgetInfo.kt` | Widget 元信息（尺寸/预览） |
| `di/WidgetModule.kt` | Hilt 注入 Widget 依赖 |

### 配置

- `res/xml/food_widget_info.xml` — Widget 配置（最小尺寸 3x2）
- `AndroidManifest.xml` 注册 receiver

---

## 模块三：个性化定制

### 3a. 美食标签分类

#### 数据模型

```kotlin
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

#### 变更

- `FoodEntity` 新增 `tags: List<FoodTag>` 字段（Room 用 TypeConverter 存 JSON）
- `FoodDao` 新增 `@Query("SELECT * FROM food WHERE tags LIKE :tag")` 按标签查询
- `FoodLibraryScreen` 新增标签筛选栏（横向滚动 Chip 组）
- `AddFoodUseCase` / `UpdateFoodUseCase` 支持标签参数
- `FoodDataSeeder` 为预设数据补充标签

### 3b. 主题色自定义

#### 方案

- 预设 8 种主题色：橙/绿/蓝/紫/粉/红/青/黄
- `ThemeManager` 新增 `seedColor` 属性，存储到 DataStore
- 使用 Material 3 `dynamicColorScheme()` 根据 seedColor 生成完整色板
- 设置页新增主题色选择器（圆形色块网格）

#### 变更

- `ThemeManager.kt` 扩展 `seedColor` + `colorScheme` 动态生成
- `SettingsScreen.kt` 新增"主题色"选择区域
- `ui/theme/Color.kt` 新增预设色定义

### 3c. 自定义游戏规则

#### 数据模型

```kotlin
data class GameRuleConfig(
    val gameId: String,
    val rounds: Int = 3,          // 轮数
    val timeLimit: Int = 0,       // 时间限制(秒)，0=无限
    val winCondition: String = "default", // 胜负条件
    val customParams: Map<String, Any> = emptyMap() // 游戏特有参数
)
```

#### 变更

- `GameSettingsManager` 新增 `getRuleConfig(gameId)` / `setRuleConfig(gameId, config)`
- 规则存储到 DataStore（JSON 序列化）
- `GameSelectScreen` 每个游戏卡片新增"规则"图标入口
- 新增 `GameRuleScreen` 规则编辑页面
- 各游戏读取 `GameRuleConfig` 替代硬编码参数

---

## 实现顺序

1. **智能推荐** — 纯逻辑层，无 UI 框架依赖，可独立测试
2. **桌面小组件** — 依赖推荐逻辑，需要 Android Manifest 配置
3. **个性化定制** — 标签影响推荐算法，主题色和游戏规则相对独立

## 依赖关系

```
智能推荐 ← 美食标签（推荐时按标签偏好过滤）
桌面小组件 ← 智能推荐（复用推荐逻辑）
个性化定制.标签 ← 智能推荐（标签数据供推荐使用）
```

标签功能虽在模块三定义，但实现时需与模块一协同：先在 FoodEntity 加 tags 字段，推荐算法即可利用标签数据。
