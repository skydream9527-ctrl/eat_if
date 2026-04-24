# 领域层功能文档

## 已实现功能

### 领域模型

| 模型 | 文件 | 功能 |
|------|------|------|
| Food | Food.kt | 美食实体 |
| Game | Game.kt | 游戏实体、游戏分类 |
| GameList | Game.kt | 游戏列表定义 |
| GameCategory | Game.kt | 游戏分类枚举 |
| GameDifficulty | GameDifficulty.kt | 游戏难度枚举 |
| GameStats | GameStats.kt | 游戏统计数据 |
| GameLevel | GameLevel.kt | 关卡定义 |
| GameRuleConfig | GameRuleConfig.kt | 游戏规则配置 |
| Recommendation | Recommendation.kt | 推荐结果 |
| RecommendationResult | Recommendation.kt | 推荐详情 |
| Achievement | Achievement.kt | 成就定义 |
| AchievementCategory | Achievement.kt | 成就分类 |
| AchievementProgress | Achievement.kt | 成就进度 |
| Skin | Skin.kt | 皮肤定义 |
| PlayerProfile | PlayerProfile.kt | 用户档案 |

### 仓库接口

| 接口 | 文件 | 功能 |
|------|------|------|
| FoodRepository | FoodRepository.kt | 美食数据接口 |
| HistoryRepository | HistoryRepository.kt | 历史记录接口 |
| GameStatsRepository | GameStatsRepository.kt | 游戏统计接口 |

### UseCase

| UseCase | 功能 |
|---------|------|
| FoodUseCase | 美食相关业务逻辑 |
| GameLevelRegistry | 关卡注册和管理 |
| SkinResolver | 皮肤解析和获取 |
| XPCalculator | XP计算 |

### 游戏分类系统

```
GameCategory:
- PRECISION (精准类)
- JUMP (跳跃类)
- CLIMB (攀爬类)
- LUCK (运气类)
- BATTLE (对战类)
- PUZZLE (益智类)
- CLASSIC (经典类)
- ARCADE (街机类)
```

### 难度系统

```
GameDifficulty:
- EASY (简单) 🟢
- NORMAL (普通) 🟡
- HARD (困难) 🔴
```

### 成就系统

```
AchievementCategory:
- MILESTONE (里程碑)
- SKILL (技能)
- STREAK (连击)
- EXPLORATION (探索)
```

## 规划功能

### 领域模型扩展

- [ ] 用户好友模型
- [ ] 商店物品模型
- [ ] 任务模型
- [ ] 活动模型

### UseCase扩展

- [ ] 排行榜UseCase
- [ ] 社交UseCase
- [ ] 推荐算法优化UseCase

### 业务规则扩展

- [ ] XP等级系统完善
- [ ] 每日任务系统
- [ ] 连续打卡奖励
- [ ] 节日活动规则

### 推荐系统

- [ ] 基于历史推荐
- [ ] 基于天气推荐
- [ ] 基于时间推荐
- [ ] 基于位置推荐