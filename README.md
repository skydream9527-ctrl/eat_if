# 今天吃什么？ (Eat If)

🍽️ 一款通过小游戏随机决定下一顿饭吃什么的 Android 应用

## 功能特点

- **15 款精选小游戏**: 大转盘、石头剪刀布、老虎机、见缝插针、跳一跳、勇闯100层、2048、贪吃蛇、俄罗斯方块、扫雷、一笔画、Flappy Eat、推箱子、无限跑酷、打靶
- **双模式**: 单人闯关 / 双人竞技
- **美食库管理**: 支持手动添加个人美食
- **美团 API 集成**: 可接入美团数据（需申请 API Key）
- **Material 3 设计**: 简约现代风格，橙色品牌色

## 技术栈

- Kotlin 1.9.20
- Jetpack Compose + Material 3
- MVVM + Clean Architecture
- Hilt 依赖注入
- Room 数据库
- Retrofit + OkHttp
- Navigation Compose

## 环境要求

- JDK 17+
- Android SDK (API 34)
- Gradle 8.2

## 快速开始

### 1. 克隆项目

```bash
git clone https://github.com/YOUR_USERNAME/eat-if.git
cd eat-if
```

### 2. 配置 Android SDK

确保 `local.properties` 中的 SDK 路径正确：

```properties
sdk.dir=/path/to/your/Android/sdk
```

### 3. 构建项目

```bash
# 使用 Gradle Wrapper
./gradlew assembleDebug

# 或直接使用系统 Gradle
gradle assembleDebug
```

### 4. 安装 APK

```bash
# 通过 ADB 安装
adb install app/build/outputs/apk/debug/app-debug.apk
```

## 项目结构

```
app/src/main/java/com/eatif/app/
├── core/                    # 核心 (Application, MainActivity)
├── data/                    # 数据层
│   ├── local/              # Room 数据库
│   ├── remote/             # 美团 API
│   └── repository/         # 仓库实现
├── domain/                  # 领域层
│   ├── model/              # 领域模型
│   └── repository/         # 仓库接口
├── di/                      # Hilt 依赖注入模块
├── games/                   # 15 款小游戏
│   ├── spinwheel/         # 大转盘
│   ├── rps/               # 石头剪刀布
│   ├── slot/              # 老虎机
│   └── ...
└── ui/                      # 界面层
    ├── screens/            # 页面
    ├── components/        # 组件
    ├── theme/             # 主题
    └── navigation/        # 导航
```

## 游戏列表

| ID | 游戏 | 类型 |
|----|------|------|
| spinwheel | 大转盘 | 运气 |
| rps | 石头剪刀布 | 对战 |
| slot | 老虎机 | 运气 |
| needle | 见缝插针 | 精准 |
| jump | 跳一跳 | 跳跃 |
| climb100 | 勇闯100层 | 攀爬 |
| 2048 | 2048 | 益智 |
| snake | 贪吃蛇 | 经典 |
| tetris | 俄罗斯方块 | 消消 |
| minesweeper | 扫雷 | 探索 |
| onetstroke | 一笔画 | 绘制 |
| flappy | Flappy Eat | 飞行 |
| boxpusher | 推箱子 | 推箱 |
| runner | 无限跑酷 | 跑酷 |
| shooting | 打靶 | 射击 |

## 配置美团 API（可选）

1. 前往 [美团开放平台](https://open.meituan.com/) 申请 API Key
2. 在 `NetworkModule.kt` 中配置您的 API Key
3. 在设置页面启用美团数据源

## License

MIT License
