# Eat If 新游戏扩展设计

日期: 2026-04-21

## 概述

为"今天吃什么？"App 新增第一批 4 款小游戏：消消乐、连连看、记忆翻牌、乒乓球。遵循现有游戏架构模式，实现统一的注册、导航、分数计算和食物集成。

---

## 现有游戏架构分析

### 游戏注册模式

```kotlin
// GameRegistryInit.kt
GameRegistry.register(GameConfig("gameId", supportsSelfPause = true/false) {
    foods, isPaused, onPauseToggle, onResult, mode ->
    GameComposable(...)
})
```

### 游戏 Composable 签名

```kotlin
@Composable
fun XXXGame(
    foods: List<Food>,
    isPaused: Boolean = false,
    onPauseToggle: ((Boolean) -> Unit)? = null,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
)
```

### 游戏定义

```kotlin
// GameList.kt
Game(
    id = "gameId",
    name = "游戏名称",
    emoji = "🎮",
    description = "简短描述",
    category = GameCategory.XXX
)
```

---

## 新游戏设计

### 1. 消消乐 (Match3)

**游戏 ID:** `match3`

**玩法:** 6x6 网格，随机颜色方块。玩家滑动交换相邻方块，消除 3 个以上相同颜色的方块。消除后上方方块下落，新方块从顶部补充。

**核心机制:**
- 网格大小: 6x6 (36 格)
- 方块颜色: 6 种 (红/橙/黄/绿/蓝/紫)
- 消除条件: 横向或纵向连续 3+ 个相同颜色
- 连击奖励: 同时消除多组或连续消除获得额外分数
- 游戏结束: 限时模式 (60秒) 或限步模式 (30步)

**分数计算:**
```
scorePercent = min(100, (消除方块数 / 50) * 100)
```

**双人模式:** 轮流操作，各自计分，高分者获胜。

**UI 结构:**
```
Column {
    Text("🧩 消消乐")
    Text("分数: X | 时间/步数: Y")
    Grid(6x6) {
        Cell(可点击/可拖动)
    }
    if (gameOver) -> ResultDialog
}
```

---

### 2. 连连看 (LinkLink)

**游戏 ID:** `linklink`

**玩法:** 8x8 网格，16 种图案每种出现 4 次。玩家点击两个相同图案，如果连线不超过 2 个拐点则消除。目标消除所有方块。

**核心机制:**
- 网格大小: 8x8 (64 格)
- 图案种类: 16 种 (食物 emoji: 🍕🍔🍜🍣🍰等)
- 路径规则: 连线最多 2 个拐点 (最多 3 段)
- 路径检测算法: BFS 寻找合法路径
- 提示功能: 无可用配对时自动重排或提示

**分数计算:**
```
scorePercent = (消除配对数 / 32) * 100 + 时间奖励
```

**双人模式:** 轮流点击，各自消除计数，消除多者获胜。

**UI 结构:**
```
Column {
    Text("🔗 连连看")
    Text("配对: X/32 | 时间: Y")
    Grid(8x8) {
        Cell(可点击)
    }
    if (noMatch) -> ShuffleButton
    if (gameOver) -> ResultDialog
}
```

---

### 3. 记忆翻牌 (MemoryCards)

**游戏 ID:** `memory`

**玩法:** 4x4 网格，8 种图案每种出现 2 次。玩家翻开两张牌，相同则消除，不同则翻回。目标消除所有牌。

**核心机制:**
- 网格大小: 4x4 (16 格)
- 图案种类: 8 种 (食物 emoji)
- 翻牌规则: 每次最多翻开 2 张
- 消除条件: 两张图案相同
- 时间限制: 120 秒
- 完美配对: 连续配对不翻错获得额外奖励

**分数计算:**
```
scorePercent = min(100, (配对数 / 8) * 100 + (1 - 翻错次数/16) * 20)
```

**双人模式:** 轮流翻牌，配对成功继续翻，配对失败换人。配对数多者获胜。

**UI 结构:**
```
Column {
    Text("🃏 记忆翻牌")
    Text("配对: X/8 | 翻错: Y | 时间: Z")
    Grid(4x4) {
        Card(点击翻开, 显示正面/背面)
    }
    if (gameOver) -> ResultDialog
}
```

---

### 4. 乒乓球 (PingPong)

**游戏 ID:** `pingpong`

**玩法:** 屏幕左右各一个挡板，中间球来回反弹。玩家控制挡板接球，球碰到边界则对方得分。

**核心机制:**
- 球速度: 初始中等，每次接球略微加速
- 挡板尺寸: 120x20 dp
- 挡板控制: 
  - 单人模式: 只控制右侧挡板，左侧 AI 挡板
  - 双人模式: 左侧玩家点击左半屏，右侧玩家点击右半屏
- 得分规则: 球越过挡板则对方得 1 分
- 游戏结束: 某方达到 11 分或限时结束

**分数计算:**
```
scorePercent = min(100, (己方得分 / 11) * 100)
```

**双人模式:** 真正的双人对战，各自控制挡板。

**UI 结构:**
```
Box {
    Canvas {
        Ball(圆形, 动画移动)
        LeftPaddle(左侧挡板)
        RightPaddle(右侧挡板)
    }
    Column {
        Text("🏓 乒乓球")
        Row { Text("左: X") Text("右: Y") }
    }
    if (gameOver) -> ResultDialog
}
```

---

## 文件结构

### 新增文件

| 文件 | 职责 |
|------|------|
| `games/match3/Match3Game.kt` | 消消乐游戏实现 |
| `games/match3/Match3Logic.kt` | 消消乐核心逻辑 (消除检测、下落填充) |
| `games/linklink/LinkLinkGame.kt` | 连连看游戏实现 |
| `games/linklink/LinkLinkLogic.kt` | 连连看路径检测算法 |
| `games/memory/MemoryGame.kt` | 记忆翻牌游戏实现 |
| `games/pingpong/PingPongGame.kt` | 乒乓球游戏实现 |

### 修改文件

| 文件 | 变更 |
|------|------|
| `games/GameRegistryInit.kt` | 注册 4 款新游戏 |
| `domain/model/GameList.kt` | 添加 4 款游戏定义 |
| `domain/model/GameCategory.kt` | 可能新增分类 (如 PUZZLE, ACTION) |
| `domain/usecase/GameLevelRegistry.kt` | 新游戏的关卡定义 |
| `domain/usecase/SkinRegistry.kt` | 新游戏的皮肤定义 |
| `domain/usecase/AchievementRegistry.kt` | 新游戏相关成就 |

---

## 游戏分类

现有分类:
- `LUCK` - 运气类 (转盘、老虎机)
- `VERSUS` - 对战类 (石头剪刀布)
- `PRECISION` - 精准类 (见缝插针)
- `JUMP` - 跳跃类 (跳一跳、攀爬)
- `PUZZLE` - 益智类 (2048、一笔画、推箱子、扫雷)
- `CLASSIC` - 经典类 (贪吃蛇、俄罗斯方块)
- `FLY` - 飞行类 (Flappy)
- `RUN` - 跑酷类 (无限跑酷)
- `SHOOT` - 射击类 (打靶)

新游戏分类:
- 消消乐 → `PUZZLE`
- 连连看 → `PUZZLE`
- 记忆翻牌 → `PUZZLE`
- 乒乓球 → `VERSUS`

---

## 实现顺序

1. **消消乐** - 最复杂，先实现核心消除逻辑
2. **连连看** - 路径检测算法是关键
3. **记忆翻牌** - 相对简单，状态管理清晰
4. **乒乓球** - 物理动画，双人交互

---

## 关卡系统扩展

每款新游戏添加 10 个关卡定义：

| 游戏 | 关卡参数 |
|------|---------|
| 消消乐 | 时间限制递减 (90s → 30s) 或步数递减 |
| 连连看 | 网格增大 (6x6 → 10x10)，图案增多 |
| 记忆翻牌 | 网格增大 (4x4 → 6x6)，时间递减 |
| 乒乓球 | 球速递增，挡板变小 |

---

## 成就扩展

新增成就定义：

| 成就 ID | 条件 | 奖励 XP |
|---------|------|--------|
| `match3_combo_5` | 消消乐连续消除 5 组 | 50 |
| `linklink_perfect` | 连连看零失误完成 | 100 |
| `memory_no_error` | 记忆翻牌零翻错完成 | 80 |
| `pingpong_win_5` | 乒乓球连胜 5 次 | 60 |

---

## 风险与注意事项

### 消消乐复杂度

- 消除检测需要遍历整个网格
- 连击逻辑需要递归处理下落和新方块
- 建议使用 `StateFlow` 管理网格状态

### 连连看路径算法

- BFS 搜索合法路径可能耗时
- 建议：限制搜索范围，预先计算可连接配对缓存

### 乒乓球双人交互

- 单屏双人需要区分点击区域
- 建议：左侧玩家点击左半屏，右侧点击右半屏

### 性能考虑

- Canvas 动画使用 `remember` 缓存状态
- 避免 Composable 内频繁创建新对象
- 使用 `LaunchedEffect` 处理游戏循环

---

## 后续扩展

第一批完成后，第二批可考虑：
- 休闲益智：数字华容道、弹珠消消
- 动作竞技：打砖块、弹弓射击
- 创意特色：切水果、合成大西瓜、打地鼠
- 经典复古：吃豆人、太空射击、赛车竞速