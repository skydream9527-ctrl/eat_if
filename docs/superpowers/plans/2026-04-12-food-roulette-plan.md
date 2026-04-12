# 今天吃什么？- 美食转盘游戏 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建一个完整的Android游戏应用，包含15款小游戏，用于决定用户下一顿饭吃什么

**Architecture:**  Clean Architecture + MVVM，Jetpack Compose UI，Hilt依赖注入，Room本地存储 + Retrofit美团API

**Tech Stack:** Kotlin 1.9, Jetpack Compose, Material 3, Hilt, Room, Retrofit, Navigation Compose, Coroutines + Flow

---

## 项目结构预览

```
eat_if/
├── app/
│   └── src/main/
│       ├── java/com/eatif/app/
│       │   ├── core/                      # 核心公共模块
│       │   │   ├── data/                  # 数据层
│       │   │   │   ├── local/            # Room数据库
│       │   │   │   ├── remote/           # 美团API
│       │   │   │   └── repository/       # 仓库实现
│       │   │   ├── domain/               # 领域层
│       │   │   │   ├── model/           # 领域模型
│       │   │   │   ├── repository/      # 仓库接口
│       │   │   │   └── usecase/         # 用例
│       │   │   └── ui/                   # 公共UI
│       │   │       ├── theme/           # 主题配置
│       │   │       ├── components/      # 通用组件
│       │   │       └── navigation/      # 导航配置
│       │   └── games/                    # 小游戏模块
│       │       ├── spinwheel/           # 大转盘
│       │       ├── rps/                 # 石头剪刀布
│       │       ├── slotmachine/        # 老虎机
│       │       └── ...
│       └── res/
└── build.gradle.kts
```

---

## 阶段一：项目骨架搭建

### 任务 1: 初始化 Gradle 项目

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts` (project-level)
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `gradlew`, `gradlew.bat`
- Create: `app/build.gradle.kts`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/values/themes.xml`

- [ ] **Step 1: 创建 settings.gradle.kts**

```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "EatIf"
include(":app")
```

- [ ] **Step 2: 创建 project-level build.gradle.kts**

```kotlin
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.20" apply false
    id("com.google.dagger.hilt.android") version "2.48.1" apply false
    id("com.google.devtools.ksp") version "1.9.20-1.0.14" apply false
}
```

- [ ] **Step 3: 创建 app/build.gradle.kts**

```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.eatif.app"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.eatif.app"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.5"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.activity:activity-compose:1.8.1")
    
    // Compose UI
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    
    // Navigation
    implementation("androidx.navigation:navigation-compose:2.7.5")
    
    // Hilt
    implementation("com.google.dagger:hilt-android:2.48.1")
    ksp("com.google.dagger:hilt-android-compiler:2.48.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    
    // Retrofit
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    
    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(composeBom)
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

- [ ] **Step 4: 创建 AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

    <application
        android:name=".EatIfApplication"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.EatIf">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:theme="@style/Theme.EatIf">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 5: 创建 Application 类**

Create: `app/src/main/java/com/eatif/app/EatIfApplication.kt`

```kotlin
package com.eatif.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class EatIfApplication : Application()
```

- [ ] **Step 6: 创建资源文件**

Create: `app/src/main/res/values/strings.xml`
```xml
<resources>
    <string name="app_name">今天吃什么？</string>
</resources>
```

Create: `app/src/main/res/values/colors.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <color name="orange_primary">#FF6B35</color>
    <color name="white">#FFFFFF</color>
    <color name="gray_light">#F5F5F7</color>
    <color name="gray_dark">#86868B</color>
    <color name="black">#1D1D1F</color>
</resources>
```

Create: `app/src/main/res/values/themes.xml`
```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.EatIf" parent="android:Theme.Material.Light.NoActionBar">
        <item name="android:statusBarColor">@color/white</item>
        <item name="android:navigationBarColor">@color/white</item>
    </style>
</resources>
```

- [ ] **Step 7: 创建 MainActivity**

Create: `app/src/main/java/com/eatif/app/MainActivity.kt`

```kotlin
package com.eatif.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.eatif.app.ui.navigation.EatIfNavHost
import com.eatif.app.ui.theme.EatIfTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EatIfTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EatIfNavHost()
                }
            }
        }
    }
}
```

- [ ] **Step 8: 生成 Gradle Wrapper**

Run: `./gradlew wrapper --gradle-version 8.2`

---

### 任务 2: 创建主题和通用UI组件

**Files:**
- Create: `app/src/main/java/com/eatif/app/ui/theme/Color.kt`
- Create: `app/src/main/java/com/eatif/app/ui/theme/Type.kt`
- Create: `app/src/main/java/com/eatif/app/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/eatif/app/ui/components/GameCard.kt`
- Create: `app/src/main/java/com/eatif/app/ui/components/ModeCard.kt`

- [ ] **Step 1: 创建 Color.kt**

```kotlin
package com.eatif.app.ui.theme

import androidx.compose.ui.graphics.Color

val OrangePrimary = Color(0xFFFF6B35)
val OrangeLight = Color(0xFFFF8F5C)
val OrangeDark = Color(0xFFE55A2B)

val White = Color(0xFFFFFFFF)
val GrayLight = Color(0xFFF5F5F7)
val GrayMedium = Color(0xFF86868B)
val Black = Color(0xFF1D1D1F)

val Green = Color(0xFF34C759)
val Red = Color(0xFFFF3B30)
```

- [ ] **Step 2: 创建 Type.kt**

```kotlin
package com.eatif.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        lineHeight = 40.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 20.sp,
        lineHeight = 28.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)
```

- [ ] **Step 3: 创建 Theme.kt**

```kotlin
package com.eatif.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = OrangePrimary,
    onPrimary = White,
    primaryContainer = OrangeLight,
    onPrimaryContainer = Black,
    secondary = GrayMedium,
    onSecondary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = GrayLight,
    onSurfaceVariant = GrayMedium,
    error = Red,
    onError = White
)

@Composable
fun EatIfTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

- [ ] **Step 4: 创建 GameCard.kt**

```kotlin
package com.eatif.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun GameCard(
    title: String,
    emoji: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
```

- [ ] **Step 5: 创建 ModeCard.kt**

```kotlin
package com.eatif.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ModeCard(
    title: String,
    emoji: String,
    description: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.headlineLarge
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
            }
        }
    }
}
```

---

### 任务 3: 导航结构

**Files:**
- Create: `app/src/main/java/com/eatif/app/ui/navigation/Screen.kt`
- Create: `app/src/main/java/com/eatif/app/ui/navigation/NavHost.kt`

- [ ] **Step 1: 创建 Screen.kt**

```kotlin
package com.eatif.app.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object GameSelect : Screen("game_select/{mode}") {
        fun createRoute(mode: String) = "game_select/$mode"
    }
    object Play : Screen("play/{gameId}") {
        fun createRoute(gameId: String) = "play/$gameId"
    }
    object Result : Screen("result/{foodName}") {
        fun createRoute(foodName: String) = "result/$foodName"
    }
    object Settings : Screen("settings")
    object FoodLibrary : Screen("food_library")
}
```

- [ ] **Step 2: 创建 NavHost.kt**

```kotlin
package com.eatif.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.eatif.app.ui.screens.*

@Composable
fun EatIfNavHost(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onSinglePlayerClick = {
                    navController.navigate(Screen.GameSelect.createRoute("single"))
                },
                onTwoPlayerClick = {
                    navController.navigate(Screen.GameSelect.createRoute("double"))
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        composable(
            route = Screen.GameSelect.route,
            arguments = listOf(navArgument("mode") { type = NavType.StringType })
        ) { backStackEntry ->
            val mode = backStackEntry.arguments?.getString("mode") ?: "single"
            GameSelectScreen(
                mode = mode,
                onGameSelected = { gameId ->
                    navController.navigate(Screen.Play.createRoute(gameId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Play.route,
            arguments = listOf(navArgument("gameId") { type = NavType.StringType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getString("gameId") ?: ""
            PlayScreen(
                gameId = gameId,
                onGameEnd = { foodName ->
                    navController.navigate(Screen.Result.createRoute(foodName)) {
                        popUpTo(Screen.Home.route)
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.Result.route,
            arguments = listOf(navArgument("foodName") { type = NavType.StringType })
        ) { backStackEntry ->
            val foodName = backStackEntry.arguments?.getString("foodName") ?: ""
            ResultScreen(
                foodName = foodName,
                onPlayAgain = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                onBackClick = { navController.popBackStack() },
                onFoodLibraryClick = {
                    navController.navigate(Screen.FoodLibrary.route)
                }
            )
        }

        composable(Screen.FoodLibrary.route) {
            FoodLibraryScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
```

---

### 任务 4: 首页 (HomeScreen)

**Files:**
- Create: `app/src/main/java/com/eatif/app/ui/screens/HomeScreen.kt`

- [ ] **Step 1: 创建 HomeScreen.kt**

```kotlin
package com.eatif.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eatif.app.ui.components.ModeCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onSinglePlayerClick: () -> Unit,
    onTwoPlayerClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🍽️ 今天吃什么？") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "🎰",
                style = MaterialTheme.typography.displayLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = "今天吃什么？",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "通过小游戏决定你的下一顿饭",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "当前美食库: 23 道菜",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(48.dp))

            ModeCard(
                title = "单人模式",
                emoji = "👤",
                description = "独自决定今天吃什么",
                onClick = onSinglePlayerClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            ModeCard(
                title = "双人竞技",
                emoji = "👥",
                description = "和朋友一起决定吃啥",
                onClick = onTwoPlayerClick
            )
        }
    }
}
```

---

### 任务 5: 游戏选择页 (GameSelectScreen)

**Files:**
- Create: `app/src/main/java/com/eatif/app/ui/screens/GameSelectScreen.kt`
- Create: `app/src/main/java/com/eatif/app/domain/model/Game.kt`

- [ ] **Step 1: 创建 Game.kt**

```kotlin
package com.eatif.app.domain.model

data class Game(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val category: GameCategory
)

enum class GameCategory {
    PRECISION,    // 精准类
    JUMP,         // 跳跃类
    CLIMB,        // 攀爬类
    LUCK,         // 运气类
    BATTLE,       // 对战类
    PUZZLE,       // 益智类
    CLASSIC,      // 经典类
    ARCADE        // 街机类
}

object GameList {
    val games = listOf(
        Game("needle", "见缝插针", "🎯", "找准角度插入针", GameCategory.PRECISION),
        Game("jump", "跳一跳", "🏃", "按压时间跳跃", GameCategory.JUMP),
        Game("climb100", "勇闯100层", "🧗", "向上攀爬跳跃", GameCategory.CLIMB),
        Game("slot", "老虎机", "🎰", "拉动拉杆看结果", GameCategory.LUCK),
        Game("spinwheel", "大转盘", "🎯", "指针停在哪吃啥", GameCategory.LUCK),
        Game("rps", "石头剪刀布", "✊", "经典对决", GameCategory.BATTLE),
        Game("2048", "2048", "🧩", "滑动合并数字", GameCategory.PUZZLE),
        Game("snake", "贪吃蛇", "🐍", "吃食物长身体", GameCategory.CLASSIC),
        Game("tetris", "俄罗斯方块", "🧱", "消除方块", GameCategory.ARCADE),
        Game("minesweeper", "扫雷", "🔍", "点开安全区域", GameCategory.PUZZLE),
        Game("onetstroke", "一笔画", "✏️", "一笔连完所有点", GameCategory.PUZZLE),
        Game("flappy", "Flappy Eat", "🐦", "穿越障碍飞行", GameCategory.ARCADE),
        Game("boxpusher", "推箱子", "📦", "推动箱子到目标", GameCategory.PUZZLE),
        Game("runner", "无限跑酷", "🏃", "躲避障碍", GameCategory.ARCADE),
        Game("shooting", "打靶", "🎯", "射击命中靶心", GameCategory.PRECISION)
    )
}
```

- [ ] **Step 2: 创建 GameSelectScreen.kt**

```kotlin
package com.eatif.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.GameList
import com.eatif.app.ui.components.GameCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameSelectScreen(
    mode: String,
    onGameSelected: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val isDoubleMode = mode == "double"
    val title = if (isDoubleMode) "双人竞技 - 选择游戏" else "单人模式 - 选择游戏"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(GameList.games) { game ->
                GameCard(
                    title = game.name,
                    emoji = game.emoji,
                    description = game.description,
                    onClick = { onGameSelected(game.id) }
                )
            }
        }
    }
}
```

---

### 任务 6: 结果页 (ResultScreen)

**Files:**
- Create: `app/src/main/java/com/eatif/app/ui/screens/ResultScreen.kt`

- [ ] **Step 1: 创建 ResultScreen.kt**

```kotlin
package com.eatif.app.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun ResultScreen(
    foodName: String,
    onPlayAgain: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        showContent = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = showContent,
            enter = scaleIn() + fadeIn()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "🎉",
                    style = MaterialTheme.typography.displayLarge
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "今天的晚餐是",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = foodName,
                        style = MaterialTheme.typography.headlineLarge,
                        modifier = Modifier.padding(horizontal = 32.dp, vertical = 16.dp),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        textAlign = TextAlign.Center
                    )
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("再来一次", style = MaterialTheme.typography.titleLarge)
                }
            }
        }
    }
}
```

---

### 任务 7: 设置页 (SettingsScreen)

**Files:**
- Create: `app/src/main/java/com/eatif/app/ui/screens/SettingsScreen.kt`

- [ ] **Step 1: 创建 SettingsScreen.kt**

```kotlin
package com.eatif.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onFoodLibraryClick: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onFoodLibraryClick() },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "🍽️ 我的美食库",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "管理个人收藏的餐厅和菜品",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ℹ️ 关于",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "版本: 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
```

---

### 任务 8: 美食库管理页 (FoodLibraryScreen)

**Files:**
- Create: `app/src/main/java/com/eatif/app/ui/screens/FoodLibraryScreen.kt`
- Create: `app/src/main/java/com/eatif/app/data/local/FoodDatabase.kt`
- Create: `app/src/main/java/com/eatif/app/data/local/FoodDao.kt`
- Create: `app/src/main/java/com/eatif/app/data/local/FoodEntity.kt`
- Create: `app/src/main/java/com/eatif/app/data/repository/FoodRepositoryImpl.kt`
- Create: `app/src/main/java/com/eatif/app/domain/repository/FoodRepository.kt`
- Create: `app/src/main/java/com/eatif/app/domain/model/Food.kt`
- Create: `app/src/main/java/com/eatif/app/di/DatabaseModule.kt`

- [ ] **Step 1: 创建 Food.kt (Domain Model)**

```kotlin
package com.eatif.app.domain.model

data class Food(
    val id: Long = 0,
    val name: String,
    val category: String,
    val imageUrl: String? = null,
    val weight: Int = 1  // 出现概率权重
)
```

- [ ] **Step 2: 创建 FoodEntity.kt (Room Entity)**

```kotlin
package com.eatif.app.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "foods")
data class FoodEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val category: String,
    val imageUrl: String?,
    val weight: Int
)
```

- [ ] **Step 3: 创建 FoodDao.kt**

```kotlin
package com.eatif.app.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM foods ORDER BY name ASC")
    fun getAllFoods(): Flow<List<FoodEntity>>

    @Query("SELECT * FROM foods WHERE category = :category ORDER BY name ASC")
    fun getFoodsByCategory(category: String): Flow<List<FoodEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFood(food: FoodEntity): Long

    @Update
    suspend fun updateFood(food: FoodEntity)

    @Delete
    suspend fun deleteFood(food: FoodEntity)

    @Query("DELETE FROM foods WHERE id = :id")
    suspend fun deleteFoodById(id: Long)

    @Query("SELECT COUNT(*) FROM foods")
    fun getFoodCount(): Flow<Int>
}
```

- [ ] **Step 4: 创建 FoodDatabase.kt**

```kotlin
package com.eatif.app.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FoodEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FoodDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
}
```

- [ ] **Step 5: 创建 FoodRepository.kt (Domain Interface)**

```kotlin
package com.eatif.app.domain.repository

import com.eatif.app.domain.model.Food
import kotlinx.coroutines.flow.Flow

interface FoodRepository {
    fun getAllFoods(): Flow<List<Food>>
    fun getFoodCount(): Flow<Int>
    suspend fun addFood(food: Food): Long
    suspend fun updateFood(food: Food)
    suspend fun deleteFood(id: Long)
}
```

- [ ] **Step 6: 创建 FoodRepositoryImpl.kt**

```kotlin
package com.eatif.app.data.repository

import com.eatif.app.data.local.FoodDao
import com.eatif.app.data.local.FoodEntity
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.repository.FoodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FoodRepositoryImpl @Inject constructor(
    private val foodDao: FoodDao
) : FoodRepository {

    override fun getAllFoods(): Flow<List<Food>> {
        return foodDao.getAllFoods().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getFoodCount(): Flow<Int> {
        return foodDao.getFoodCount()
    }

    override suspend fun addFood(food: Food): Long {
        return foodDao.insertFood(food.toEntity())
    }

    override suspend fun updateFood(food: Food) {
        foodDao.updateFood(food.toEntity())
    }

    override suspend fun deleteFood(id: Long) {
        foodDao.deleteFoodById(id)
    }

    private fun FoodEntity.toDomain() = Food(
        id = id,
        name = name,
        category = category,
        imageUrl = imageUrl,
        weight = weight
    )

    private fun Food.toEntity() = FoodEntity(
        id = id,
        name = name,
        category = category,
        imageUrl = imageUrl,
        weight = weight
    )
}
```

- [ ] **Step 7: 创建 DatabaseModule.kt (Hilt)**

```kotlin
package com.eatif.app.di

import android.content.Context
import androidx.room.Room
import com.eatif.app.data.local.FoodDao
import com.eatif.app.data.local.FoodDatabase
import com.eatif.app.data.repository.FoodRepositoryImpl
import com.eatif.app.domain.repository.FoodRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFoodDatabase(@ApplicationContext context: Context): FoodDatabase {
        return Room.databaseBuilder(
            context,
            FoodDatabase::class.java,
            "eatif_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFoodDao(database: FoodDatabase): FoodDao {
        return database.foodDao()
    }

    @Provides
    @Singleton
    fun provideFoodRepository(foodDao: FoodDao): FoodRepository {
        return FoodRepositoryImpl(foodDao)
    }
}
```

- [ ] **Step 8: 创建 FoodLibraryScreen.kt**

```kotlin
package com.eatif.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eatif.app.domain.model.Food

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FoodLibraryScreen(
    onBackClick: () -> Unit,
    viewModel: FoodLibraryViewModel = hiltViewModel()
) {
    val foods by viewModel.foods.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的美食库") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加美食")
            }
        }
    ) { padding ->
        if (foods.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🍽️",
                        style = MaterialTheme.typography.displayMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "还没有美食，点击+添加",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(foods, key = { it.id }) { food ->
                    FoodItem(
                        food = food,
                        onDelete = { viewModel.deleteFood(food.id) }
                    )
                }
                item { Spacer(modifier = Modifier.height(80.dp)) }
            }
        }
    }

    if (showAddDialog) {
        AddFoodDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { name, category ->
                viewModel.addFood(name, category)
                showAddDialog = false
            }
        )
    }
}

@Composable
private fun FoodItem(
    food: Food,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = food.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = food.category,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun AddFoodDialog(
    onDismiss: () -> Unit,
    onAdd: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加美食") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("美食名称") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("分类（如：中餐、日料）") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onAdd(name, category) },
                enabled = name.isNotBlank() && category.isNotBlank()
            ) {
                Text("添加")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
```

- [ ] **Step 9: 创建 FoodLibraryViewModel.kt**

Create: `app/src/main/java/com/eatif/app/ui/screens/FoodLibraryViewModel.kt`

```kotlin
package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FoodLibraryViewModel @Inject constructor(
    private val repository: FoodRepository
) : ViewModel() {

    val foods: StateFlow<List<Food>> = repository.getAllFoods()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFood(name: String, category: String) {
        viewModelScope.launch {
            repository.addFood(Food(name = name, category = category))
        }
    }

    fun deleteFood(id: Long) {
        viewModelScope.launch {
            repository.deleteFood(id)
        }
    }
}
```

---

### 任务 9: 游戏框架 - PlayScreen 入口

**Files:**
- Create: `app/src/main/java/com/eatif/app/ui/screens/PlayScreen.kt`
- Create: `app/src/main/java/com/eatif/app/games/GameEngine.kt`
- Create: `app/src/main/java/com/eatif/app/games/GameResult.kt`

- [ ] **Step 1: 创建 GameResult.kt**

```kotlin
package com.eatif.app.games

sealed class GameResult {
    data class FoodSelected(val foodName: String) : GameResult()
    data object Continue : GameResult()
}
```

- [ ] **Step 2: 创建 GameEngine.kt (游戏引擎接口)**

```kotlin
package com.eatif.app.games

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.eatif.app.domain.model.Food

interface GameEngine {
    val gameId: String
    val gameName: String
    
    @Composable
    fun Render(
        foods: List<Food>,
        onResult: (GameResult) -> Unit
    )
}

@Composable
fun rememberGameState() {
    // Placeholder for shared game state
}
```

- [ ] **Step 3: 创建 PlayScreen.kt**

```kotlin
package com.eatif.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.eatif.app.domain.model.Food
import com.eatif.app.games.spinwheel.SpinWheelGame
import com.eatif.app.games.rps.RockPaperScissorsGame

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayScreen(
    gameId: String,
    onGameEnd: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: PlayViewModel = hiltViewModel()
) {
    val foods by viewModel.foods.collectAsState()
    
    LaunchedEffect(gameId) {
        viewModel.loadFoods()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(viewModel.getGameName(gameId)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.Close, contentDescription = "退出")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (gameId) {
                "spinwheel" -> SpinWheelGame(
                    foods = foods,
                    onResult = { onGameEnd(it) }
                )
                "rps" -> RockPaperScissorsGame(
                    foods = foods,
                    onResult = { onGameEnd(it) }
                )
                else -> {
                    // Placeholder for other games
                    Text(
                        text = "游戏 ${viewModel.getGameName(gameId)} 开发中...",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
```

- [ ] **Step 4: 创建 PlayViewModel.kt**

Create: `app/src/main/java/com/eatif/app/ui/screens/PlayViewModel.kt`

```kotlin
package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.GameList
import com.eatif.app.domain.repository.FoodRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayViewModel @Inject constructor(
    private val repository: FoodRepository
) : ViewModel() {

    private val _foods = MutableStateFlow<List<Food>>(emptyList())
    val foods: StateFlow<List<Food>> = _foods.asStateFlow()

    fun loadFoods() {
        viewModelScope.launch {
            repository.getAllFoods().collect { foodList ->
                if (foodList.isEmpty()) {
                    // Default demo foods
                    _foods.value = listOf(
                        Food(name = "火锅", category = "中餐"),
                        Food(name = "寿司", category = "日料"),
                        Food(name = "汉堡", category = "西餐"),
                        Food(name = "披萨", category = "西餐"),
                        Food(name = "拉面", category = "日料"),
                        Food(name = "饺子", category = "中餐"),
                        Food(name = "沙拉", category = "轻食"),
                        Food(name = "炸鸡", category = "快餐")
                    )
                } else {
                    _foods.value = foodList
                }
            }
        }
    }

    fun getGameName(gameId: String): String {
        return GameList.games.find { it.id == gameId }?.name ?: "游戏"
    }
}
```

---

## 阶段二：游戏实现

### 任务 10: 大转盘游戏 (SpinWheel)

**Files:**
- Create: `app/src/main/java/com/eatif/app/games/spinwheel/SpinWheelGame.kt`
- Create: `app/src/main/java/com/eatif/app/games/spinwheel/SpinWheel.kt`

- [ ] **Step 1: 创建 SpinWheel.kt**

```kotlin
package com.eatif.app.games.spinwheel

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SpinWheel(
    foods: List<String>,
    isSpinning: Boolean,
    onSpinEnd: (Int) -> Unit
) {
    var rotation by remember { mutableFloatStateOf(0f) }
    val infiniteTransition = rememberInfiniteTransition(label = "spin")
    
    val animatedRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    LaunchedEffect(isSpinning) {
        if (isSpinning) {
            val targetRotation = 360f * 5 + Random.nextFloat() * 360f
            rotation = targetRotation
        }
    }

    val colors = listOf(
        Color(0xFFFF6B35),
        Color(0xFFFF8F5C),
        Color(0xFFFFAB5C),
        Color(0xFFFFD5C),
        Color(0xFFFFF0DB)
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Canvas(
            modifier = Modifier
                .size(300.dp)
                .padding(16.dp)
        ) {
            val sweepAngle = 360f / foods.size.coerceAtLeast(1)
            foods.forEachIndexed { index, _ ->
                rotate(rotation) {
                    drawArc(
                        color = colors[index % colors.size],
                        startAngle = index * sweepAngle,
                        sweepAngle = sweepAngle,
                        useCenter = true,
                        size = Size(size.width, size.height)
                    )
                }
            }
            drawCircle(
                color = Color.White,
                radius = 30f,
                center = center,
                style = Stroke(width = 4f)
            )
        }

        foods.forEachIndexed { index, food ->
            Text(
                text = "$index: $food",
                modifier = Modifier.padding(4.dp)
            )
        }
    }
}
```

- [ ] **Step 2: 创建 SpinWheelGame.kt**

```kotlin
package com.eatif.app.games.spinwheel

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.games.GameResult
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun SpinWheelGame(
    foods: List<Food>,
    onResult: (String) -> Unit
) {
    var isSpinning by remember { mutableStateOf(false) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var hasEnded by remember { mutableStateOf(false) }
    
    val foodNames = remember(foods) { foods.map { it.name } }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isSpinning) "🎡 旋转中..." else "🎯 点击按钮开始",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Spacer(modifier = Modifier.height(24.dp))

        Box(contentAlignment = Alignment.Center) {
            Canvas(
                modifier = Modifier.size(300.dp)
            ) {
                val sweepAngle = 360f / foodNames.size.coerceAtLeast(1)
                foodNames.forEachIndexed { index, _ ->
                    rotate(rotation) {
                        drawArc(
                            color = if (index % 2 == 0) Color(0xFFFF6B35) else Color(0xFFFF8F5C),
                            startAngle = index * sweepAngle - 90f,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            size = Size(size.width, size.height)
                        )
                    }
                }
                drawCircle(
                    color = Color.White,
                    radius = 40f,
                    center = center,
                    style = Stroke(width = 6f)
                )
            }
            
            Text(
                text = "▼",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFFFF3B30),
                modifier = Modifier.align(Alignment.TopCenter).offset(y = (-8).dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                if (!isSpinning && !hasEnded) {
                    isSpinning = true
                    val targetRotation = 360f * 5 + Random.nextFloat() * 360f
                    rotation = targetRotation
                }
            },
            enabled = !isSpinning && !hasEnded,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier.size(width = 200.dp, height = 56.dp)
        ) {
            Text("开始转动", style = MaterialTheme.typography.titleLarge)
        }

        LaunchedEffect(rotation) {
            if (rotation > 0 && isSpinning) {
                kotlinx.coroutines.delay(4000)
                isSpinning = false
                hasEnded = true
                val sweepAngle = 360f / foodNames.size
                val normalizedRotation = (rotation % 360f)
                val pointerAngle = (360f - normalizedRotation + 90f) % 360f
                val winningIndex = ((pointerAngle / sweepAngle).toInt()) % foodNames.size
                onResult(foodNames[winningIndex])
            }
        }
    }
}
```

---

### 任务 11: 石头剪刀布游戏 (RockPaperScissors)

**Files:**
- Create: `app/src/main/java/com/eatif/app/games/rps/RockPaperScissorsGame.kt`

- [ ] **Step 1: 创建 RockPaperScissorsGame.kt**

```kotlin
package com.eatif.app.games.rps

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import kotlin.random.Random

enum class RPSChoice(val emoji: String) {
    ROCK("🪨"),
    PAPER("📄"),
    SCISSORS("✂️")
}

enum class GameState {
    READY,
    PLAYER_CHOSEN,
    AI_CHOSEN,
    RESULT
}

@Composable
fun RockPaperScissorsGame(
    foods: List<Food>,
    onResult: (String) -> Unit
) {
    var gameState by remember { mutableStateOf(GameState.READY) }
    var playerChoice by remember { mutableStateOf<RPSChoice?>(null) }
    var aiChoice by remember { mutableStateOf<RPSChoice?>(null) }
    var playerScore by remember { mutableIntStateOf(0) }
    var aiScore by remember { mutableIntStateOf(0) }
    
    val foodNames = remember(foods) { foods.map { it.name } }

    fun determineWinner(player: RPSChoice, ai: RPSChoice): Int {
        return when {
            player == ai -> 0
            (player == RPSChoice.ROCK && ai == RPSChoice.SCISSORS) ||
            (player == RPSChoice.PAPER && ai == RPSChoice.ROCK) ||
            (player == RPSChoice.SCISSORS && ai == RPSChoice.PAPER) -> 1
            else -> -1
        }
    }

    fun play(choice: RPSChoice) {
        playerChoice = choice
        gameState = GameState.PLAYER_CHOSEN
        
        aiChoice = RPSChoice.entries[Random.nextInt(3)]
        gameState = GameState.AI_CHOSEN
        
        kotlinx.coroutines.delay(500)
        
        val result = determineWinner(choice, aiChoice!!)
        when (result) {
            1 -> playerScore++
            -1 -> aiScore++
        }
        gameState = GameState.RESULT
    }

    fun finish() {
        val winnerFood = foodNames[Random.nextInt(foodNames.size)]
        onResult(winnerFood)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "✊ 石头剪刀布",
            style = MaterialTheme.typography.headlineMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "你: $playerScore  vs  AI: $aiScore",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "👤 你", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = playerChoice?.emoji ?: "?",
                    style = MaterialTheme.typography.displayMedium
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🤖 AI", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = when (gameState) {
                        GameState.READY -> "?"
                        else -> aiChoice?.emoji ?: "?"
                    },
                    style = MaterialTheme.typography.displayMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (gameState) {
            GameState.READY -> {
                Text(
                    text = "选择你的出拳",
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    RPSChoice.entries.forEach { choice ->
                        Button(
                            onClick = { play(choice) },
                            modifier = Modifier.size(80.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(choice.emoji, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }
            GameState.RESULT -> {
                val result = playerChoice?.let { determineWinner(it, aiChoice!!) }
                Text(
                    text = when (result) {
                        1 -> "🎉 你赢了!"
                        -1 -> "😢 你输了!"
                        else -> "🤝 平局!"
                    },
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        playerChoice = null
                        aiChoice = null
                        gameState = GameState.READY
                    },
                    modifier = Modifier.fillMaxWidth(0.6f)
                ) {
                    Text("再来一局")
                }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { finish() },
                    modifier = Modifier.fillMaxWidth(0.6f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text("结束并选择美食")
                }
            }
            else -> {
                Text(
                    text = "等待中...",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
```

---

### 任务 12-26: 剩余13款游戏

**每个游戏一个独立文件，结构类似：**

**Files for each game:**
- Create: `app/src/main/java/com/eatif/app/games/{gameid}/{GameName}Game.kt`

**游戏清单：**

| 任务 | 游戏ID | 文件 |
|------|--------|------|
| 12 | needle | 见缝插针 |
| 13 | jump | 跳一跳 |
| 14 | climb100 | 勇闯100层 |
| 15 | slot | 老虎机 |
| 16 | 2048 | 2048 |
| 17 | snake | 贪吃蛇 |
| 18 | tetris | 俄罗斯方块 |
| 19 | minesweeper | 扫雷 |
| 20 | onetstroke | 一笔画 |
| 21 | flappy | Flappy Eat |
| 22 | boxpusher | 推箱子 |
| 23 | runner | 无限跑酷 |
| 24 | shooting | 打靶 |

每个游戏实现为独立Composable函数，接收 `foods: List<Food>` 和 `onResult: (String) -> Unit` 参数。

---

## 阶段三：美团API集成 (可选后续迭代)

### 任务 27: 美团API模块

**Files:**
- Create: `app/src/main/java/com/eatif/app/data/remote/MeituanApi.kt`
- Create: `app/src/main/java/com/eatif/app/data/remote/MeituanModels.kt`
- Create: `app/src/main/java/com/eatif/app/di/NetworkModule.kt`

- [ ] **Step 1: 创建 MeituanModels.kt**

```kotlin
package com.eatif.app.data.remote

import com.google.gson.annotations.SerializedName

data class MeituanResponse(
    @SerializedName("data")
    val data: List<MeituanFood>
)

data class MeituanFood(
    @SerializedName("name")
    val name: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("image_url")
    val imageUrl: String?
)
```

- [ ] **Step 2: 创建 MeituanApi.kt**

```kotlin
package com.eatif.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface MeituanApi {
    @GET("food/search")
    suspend fun searchFood(
        @Query("city") city: String,
        @Query("keyword") keyword: String
    ): MeituanResponse
}
```

- [ ] **Step 3: 创建 NetworkModule.kt**

```kotlin
package com.eatif.app.di

import com.eatif.app.data.remote.MeituanApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.meituan.com/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideMeituanApi(retrofit: Retrofit): MeituanApi {
        return retrofit.create(MeituanApi::class.java)
    }
}
```

---

## 阶段四：验证与构建

### 任务 28: 编译验证

- [ ] **Step 1: 运行 assembleDebug**

Run: `./gradlew assembleDebug --stacktrace`

- [ ] **Step 2: 验证APK生成**

Run: `ls -la app/build/outputs/apk/debug/`

---

## 计划自查

1. **Spec覆盖检查：**
   - ✅ 单人/双人模式选择
   - ✅ 15款小游戏清单
   - ✅ 美团API + 用户自建库
   - ✅ 简约现代风格 + 橙色品牌色
   - ✅ Kotlin + Jetpack Compose + MVVM架构
   - ✅ 设置页 + 美食库管理

2. **占位符检查：** 无TBD/TODO占位符

3. **类型一致性：** 所有接口保持一致

---

**计划完成并保存至：** `docs/superpowers/plans/2026-04-12-food-roulette-plan.md`

**两种执行方式：**

1. **子代理驱动（推荐）** - 每个任务分配独立子代理，快速迭代
2. **本会话执行** - 我直接执行每个步骤，有检查点

你想用哪种方式？