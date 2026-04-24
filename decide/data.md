# 数据层功能文档

## 已实现功能

### 数据库 (Room)

#### 实体类

| 实体 | 文件 | 功能 |
|------|------|------|
| FoodEntity | FoodEntity.kt | 美食数据 |
| HistoryEntity | HistoryEntity.kt | 历史记录 |
| GameStatsEntity | GameStatsEntity.kt | 游戏统计 |
| LevelProgressEntity | LevelProgressEntity.kt | 关卡进度 |
| PlayerProfileEntity | PlayerProfileEntity.kt | 用户档案 |
| SkinCollectionEntity | SkinCollectionEntity.kt | 皮肤收藏 |
| AchievementProgressEntity | AchievementProgressEntity.kt | 成就进度 |

#### DAO 接口

| DAO | 文件 | 功能 |
|-----|------|------|
| FoodDao | FoodDao.kt | 美食 CRUD |
| HistoryDao | HistoryDao.kt | 历史记录 CRUD |
| GameStatsDao | GameStatsDao.kt | 游戏统计 CRUD |
| LevelProgressDao | LevelProgressDao.kt | 关卡进度 CRUD |
| PlayerProfileDao | PlayerProfileDao.kt | 用户档案 CRUD |
| SkinCollectionDao | SkinCollectionDao.kt | 皮肤收藏 CRUD |
| AchievementProgressDao | AchievementProgressDao.kt | 成就进度 CRUD |

#### 数据库配置

- FoodDatabase: 数据库定义
- FoodDataSeeder: 预置美食数据
- FoodTagTypeConverter: 标签类型转换器
- ProgressTypeConverters: 进度类型转换器

### 仓库实现

| 仓库 | 文件 | 功能 |
|------|------|------|
| FoodRepositoryImpl | FoodRepositoryImpl.kt | 美食数据仓库 |
| HistoryRepositoryImpl | HistoryRepositoryImpl.kt | 历史记录仓库 |
| GameStatsRepositoryImpl | GameStatsRepositoryImpl.kt | 游戏统计仓库 |

### 远程数据

- 美团 API 集成 (可选)
- Retrofit + OkHttp 网络请求

### Session 管理

- SessionManager: 会话配置管理
  - 商店选项配置
  - 推荐结果生成
  - 会话状态追踪

### 依赖注入模块

- DatabaseModule: 数据库相关依赖
- NetworkModule: 网络相关依赖

## 规划功能

### 数据同步

- [ ] 云端数据同步
- [ ] 多设备数据同步
- [ ] 数据备份恢复

### 数据分析

- [ ] 用户行为分析
- [ ] 游戏偏好分析
- [ ] 吃饭习惯分析

### 远程数据扩展

- [ ] 更多美食平台 API
- [ ] 用户评价数据
- [ ] 热门推荐数据

### 缓存优化

- [ ] 离线数据缓存
- [ ] 图片缓存
- [ ] 数据预加载

### 数据安全

- [ ] 数据加密存储
- [ ] 用户隐私保护
- [ ] 数据导出功能