# UI/界面模块功能文档

## 已实现功能

### 主要页面

| 页面 | 文件 | 功能 | 状态 |
|------|------|------|------|
| 启动页 | SplashScreen.kt | 应用启动动画 | ✅ |
| 首页 | HomeScreen.kt | 模式选择、推荐展示 | ✅ |
| 游戏选择页 | GameSelectScreen.kt | 游戏列表、分类筛选 | ✅ |
| 游戏页面 | PlayScreen.kt | 游戏渲染、暂停/继续 | ✅ |
| 结果页 | ResultScreen.kt | 游戏结果展示、分享 | ✅ |
| 设置页 | SettingsScreen.kt | 主题、难度、音效设置 | ✅ |
| 美食库页 | FoodLibraryScreen.kt | 美食管理 | ✅ |
| 美食选择页 | FoodSelectScreen.kt | 选择美食 | ✅ |
| 历史页 | HistoryScreen.kt | 吃饭记录历史 | ✅ |
| 统计页 | StatsScreen.kt | 游戏统计数据 | ✅ |
| 成就页 | AchievementScreen.kt | 成就展示 | ✅ |
| 个人页 | ProfileScreen.kt | 用户信息 | ✅ |
| 关卡选择页 | LevelSelectScreen.kt | 关卡选择 | ✅ |
| 皮肤选择页 | SkinSelectorScreen.kt | 游戏皮肤 | ✅ |
| 游戏规则页 | GameRuleScreen.kt | 游戏规则说明 | ✅ |
| 设置页 | SetupScreen.kt | 初始设置流程 | ✅ |

### 导航系统

- Navigation Compose 路由管理
- 屏幕路由定义 (Screen sealed class)
- 参数传递支持 (mode, gameId, levelNumber)

### 底部导航栏

- 首页 (Home)
- 统计 (Stats)
- 成就 (Achievements)
- 我的 (Profile)

### 组件系统

- ModeCard: 模式选择卡片
- GameCard: 游戏展示卡片
- RecommendationCard: 推荐美食卡片
- DifficultySelector: 难度选择器
- XPProgressBar: XP进度条
- EmptyStateWithAction: 空状态提示
- TutorialDialog: 教程弹窗
- AchievementUnlockDialog: 成就解锁弹窗
- AchievementDetailDialog: 成就详情弹窗

### 主题系统

- Material 3 设计
- 深色/浅色模式切换
- 跟随系统主题
- 多种主题色选择 (橙色、蓝色、绿色等)
- 主题管理 (ThemeManager)

### 设置管理

- GameSettingsManager: 游戏设置
  - 难度设置
  - 音效开关
  - 振动开关
  - 收藏游戏列表
  - 教程查看状态

- SkinSettingsManager: 皮肤设置
  - 激活皮肤配置
  - 皮肤解锁状态

- AchievementSettingsManager: 成就设置
  - 成就解锁状态

### 状态持有器

- SkinParamsHolder: 皮肤参数传递
- LevelParamsHolder: 关卡参数传递
- GameEndResultHolder: 游戏结束结果
- LastGameContextHolder: 上局游戏上下文

### 桌面小组件

- FoodWidget: 美食推荐小组件
- FoodWidgetReceiver: 小组件更新接收器
- RefreshAction: 刷新动作

## 规划功能

### 页面扩展

- [ ] 游戏排行榜页面
- [ ] 好友列表页面
- [ ] 商店页面 (皮肤、道具)
- [ ] 每日任务页面
- [ ] 活动页面

### UI增强

- [ ] 更多动画效果
- [ ] 手势导航支持
- [ ] 多语言支持
- [ ] 自定义字体
- [ ] 更丰富的空状态设计

### 交互增强

- [ ] 长按操作支持
- [ ] 拖拽操作支持
- [ ] 手势教程
- [ ] 语音提示

### 小组件扩展

- [ ] 多种尺寸小组件
- [ ] 游戏快捷入口小组件
- [ ] 今日推荐小组件