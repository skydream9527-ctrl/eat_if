# 新游戏扩展实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-step. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 为 Eat If App 新增 4 款小游戏：消消乐、连连看、记忆翻牌、乒乓球

**Architecture:** 每款游戏遵循现有架构模式 — 独立包目录 + Composable 函数 + GameRegistry 注册 + GameList 定义。核心逻辑与 UI 分离，便于测试和维护。

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, Coroutines + Flow

---

## File Structure Map

| 文件 | 类型 | 职责 |
|------|------|------|
| `games/match3/Match3Game.kt` | 新增 | 消消乐 UI Composable |
| `games/match3/Match3Logic.kt` | 新增 | 消消乐核心逻辑 (消除检测、下落填充) |
| `games/linklink/LinkLinkGame.kt` | 新增 | 连连看 UI Composable |
| `games/linklink/LinkLinkLogic.kt` | 新增 | 连连看路径检测算法 |
| `games/memory/MemoryGame.kt` | 新增 | 记忆翻牌 UI Composable |
| `games/pingpong/PingPongGame.kt` | 新增 | 乒乓球 UI Composable |
| `games/GameRegistryInit.kt` | 修改 | 注册 4 款新游戏 |
| `domain/model/Game.kt` | 修改 | GameList 添加 4 款游戏定义 |
| `domain/usecase/GameLevelRegistry.kt` | 修改 | 新游戏关卡定义 |
| `domain/usecase/AchievementRegistry.kt` | 修改 | 新游戏成就定义 |

---

## Task 1: 消消乐核心逻辑 (Match3Logic)

**Files:**
- Create: `app/src/main/java/com/eatif/app/games/match3/Match3Logic.kt`

- [ ] **Step 1: 创建 Match3Logic.kt 基础结构**

```kotlin
// app/src/main/java/com/eatif/app/games/match3/Match3Logic.kt
package com.eatif.app.games.match3

import kotlin.random.Random

enum class GemColor {
    RED, ORANGE, YELLOW, GREEN, BLUE, PURPLE
}

data class Gem(val color: GemColor, val row: Int, val col: Int)

class Match3Logic(
    val gridSize: Int = 6,
    val colors: List<GemColor> = GemColor.entries.toList()
) {
    private var grid: Array<Array<GemColor?>> = Array(gridSize) { Array(gridSize) { null } }
    
    fun initializeGrid(): Array<Array<GemColor?>> {
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                grid[row][col] = colors[Random.nextInt(colors.size)]
            }
        }
        // Remove initial matches
        while (hasMatches()) {
            removeMatches()
            fillEmptyCells()
        }
        return grid
    }
    
    fun getGrid(): Array<Array<GemColor?>> = grid
    
    fun swapGems(row1: Int, col1: Int, row2: Int, col2: Int): Boolean {
        if (!isAdjacent(row1, col1, row2, col2)) return false
        
        val temp = grid[row1][col1]
        grid[row1][col1] = grid[row2][col2]
        grid[row2][col2] = temp
        
        if (hasMatches()) {
            return true
        } else {
            // Swap back if no match
            grid[row2][col2] = grid[row1][col1]
            grid[row1][col1] = temp
            return false
        }
    }
    
    private fun isAdjacent(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        return (r1 == r2 && kotlin.math.abs(c1 - c2) == 1) ||
               (c1 == c2 && kotlin.math.abs(r1 - r2) == 1)
    }
    
    fun hasMatches(): Boolean {
        return findMatches().isNotEmpty()
    }
    
    fun findMatches(): List<List<Pair<Int, Int>>> {
        val matches = mutableListOf<List<Pair<Int, Int>>>()
        
        // Check horizontal matches
        for (row in 0 until gridSize) {
            var col = 0
            while (col < gridSize - 2) {
                val color = grid[row][col]
                if (color != null && color == grid[row][col + 1] && color == grid[row][col + 2]) {
                    val match = mutableListOf<Pair<Int, Int>>()
                    while (col < gridSize && grid[row][col] == color) {
                        match.add(Pair(row, col))
                        col++
                    }
                    matches.add(match)
                } else {
                    col++
                }
            }
        }
        
        // Check vertical matches
        for (col in 0 until gridSize) {
            var row = 0
            while (row < gridSize - 2) {
                val color = grid[row][col]
                if (color != null && color == grid[row + 1][col] && color == grid[row + 2][col]) {
                    val match = mutableListOf<Pair<Int, Int>>()
                    while (row < gridSize && grid[row][col] == color) {
                        match.add(Pair(row, col))
                        row++
                    }
                    matches.add(match)
                } else {
                    row++
                }
            }
        }
        
        return matches
    }
    
    fun removeMatches(): Int {
        val matches = findMatches()
        var removedCount = 0
        for (match in matches) {
            for ((row, col) in match) {
                grid[row][col] = null
                removedCount++
            }
        }
        return removedCount
    }
    
    fun dropGems(): List<Pair<Int, Int>> {
        val moved = mutableListOf<Pair<Int, Int>>()
        for (col in 0 until gridSize) {
            var writeRow = gridSize - 1
            for (row in gridSize - 1 downTo 0) {
                if (grid[row][col] != null) {
                    if (row != writeRow) {
                        grid[writeRow][col] = grid[row][col]
                        grid[row][col] = null
                        moved.add(Pair(writeRow, col))
                    }
                    writeRow--
                }
            }
        }
        return moved
    }
    
    fun fillEmptyCells(): List<Pair<Int, Int>> {
        val filled = mutableListOf<Pair<Int, Int>>()
        for (col in 0 until gridSize) {
            for (row in gridSize - 1 downTo 0) {
                if (grid[row][col] == null) {
                    grid[row][col] = colors[Random.nextInt(colors.size)]
                    filled.add(Pair(row, col))
                }
            }
        }
        return filled
    }
    
    fun processTurn(): Pair<Int, List<Pair<Int, Int>>> {
        var totalRemoved = 0
        var allMoved = mutableListOf<Pair<Int, Int>>()
        
        while (hasMatches()) {
            totalRemoved += removeMatches()
            allMoved.addAll(dropGems())
            allMoved.addAll(fillEmptyCells())
        }
        
        return Pair(totalRemoved, allMoved)
    }
    
    fun isGameOver(movesLeft: Int): Boolean {
        return movesLeft <= 0 && !hasAnyPossibleMatch()
    }
    
    private fun hasAnyPossibleMatch(): Boolean {
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize - 1) {
                if (canCreateMatch(row, col, row, col + 1)) return true
            }
        }
        for (row in 0 until gridSize - 1) {
            for (col in 0 until gridSize) {
                if (canCreateMatch(row, col, row + 1, col)) return true
            }
        }
        return false
    }
    
    private fun canCreateMatch(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        val temp = grid[r1][c1]
        grid[r1][c1] = grid[r2][c2]
        grid[r2][c2] = temp
        
        val hasMatch = hasMatches()
        
        grid[r2][c2] = grid[r1][c1]
        grid[r1][c1] = temp
        
        return hasMatch
    }
}
```

- [ ] **Step 2: 验证逻辑文件无语法错误**

检查 imports 和语法是否正确。

- [ ] **Step 3: Commit**

```bash
git add games/match3/Match3Logic.kt
git commit -m "feat: add Match3 core logic (消除检测/下落/填充算法)"
```

---

## Task 2: 消消乐 UI (Match3Game)

**Files:**
- Create: `app/src/main/java/com/eatif/app/games/match3/Match3Game.kt`

- [ ] **Step 1: 创建 Match3Game.kt Composable**

```kotlin
// app/src/main/java/com/eatif/app/games/match3/Match3Game.kt
package com.eatif.app.games.match3

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.GrayMedium
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.White
import kotlin.random.Random

@Composable
fun Match3Game(
    foods: List<Food>,
    isPaused: Boolean = false,
    onPauseToggle: ((Boolean) -> Unit)? = null,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    val logic = remember { Match3Logic() }
    val gridSize = 6
    val cellSize = 48.dp
    
    var grid by remember { mutableStateOf(logic.initializeGrid()) }
    var score by remember { mutableIntStateOf(0) }
    var movesLeft by remember { mutableIntStateOf(30) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var gameState by remember { mutableStateOf("playing") }
    var isProcessing by remember { mutableStateOf(false) }
    val targetFood = remember { foods.randomOrNull()?.name ?: "美食" }
    
    val colorMap = mapOf(
        GemColor.RED to Color(0xFFE53935),
        GemColor.ORANGE to Color(0xFFFF9800),
        GemColor.YELLOW to Color(0xFFFDD835),
        GemColor.GREEN to Color(0xFF43A047),
        GemColor.BLUE to Color(0xFF1E88E5),
        GemColor.PURPLE to Color(0xFF8E24AA)
    )
    
    fun handleCellClick(row: Int, col: Int) {
        if (isProcessing || gameState != "playing" || isPaused) return
        
        if (selectedCell == null) {
            selectedCell = Pair(row, col)
        } else {
            val (prevRow, prevCol) = selectedCell!!
            if (prevRow == row && prevCol == col) {
                selectedCell = null
                return
            }
            
            isProcessing = true
            if (logic.swapGems(prevRow, prevCol, row, col)) {
                grid = logic.getGrid().map { it.toList() }.toList().map { it.toTypedArray() }.toTypedArray()
                movesLeft--
                
                val (removed, _) = logic.processTurn()
                score += removed * 10
                grid = logic.getGrid().map { it.toList() }.toList().map { it.toTypedArray() }.toTypedArray()
                
                if (logic.isGameOver(movesLeft)) {
                    gameState = "gameover"
                    val scorePercent = (score / 100).coerceIn(0, 100)
                    onResult(targetFood, scorePercent)
                }
            }
            selectedCell = null
            isProcessing = false
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🧩 消消乐",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "分数: $score | 剩余步数: $movesLeft",
            style = MaterialTheme.typography.titleMedium,
            color = OrangePrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridSize),
            modifier = Modifier.size(cellSize * gridSize),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(grid.flatten().toList().withIndex().toList()) { indexed ->
                val row = indexed.index / gridSize
                val col = indexed.index % gridSize
                val gemColor = indexed.value
                
                Box(
                    modifier = Modifier
                        .size(cellSize)
                        .background(
                            colorMap[gemColor] ?: GrayMedium,
                            RoundedCornerShape(8.dp)
                        )
                        .then(
                            if (selectedCell == Pair(row, col)) {
                                Modifier.border(3.dp, White, RoundedCornerShape(8.dp))
                            } else {
                                Modifier
                            }
                        )
                        .clickable { handleCellClick(row, col) }
                )
            }
        }
        
        if (gameState == "gameover") {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "游戏结束! 获得: $targetFood",
                style = MaterialTheme.typography.titleLarge,
                color = OrangePrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    grid = logic.initializeGrid()
                    score = 0
                    movesLeft = 30
                    gameState = "playing"
                    selectedCell = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("再来一次", color = White)
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add games/match3/Match3Game.kt
git commit -m "feat: add Match3Game UI Composable"
```

---

## Task 3: 连连看路径算法 (LinkLinkLogic)

**Files:**
- Create: `app/src/main/java/com/eatif/app/games/linklink/LinkLinkLogic.kt`

- [ ] **Step 1: 创建 LinkLinkLogic.kt 路径检测算法**

```kotlin
// app/src/main/java/com/eatif/app/games/linklink/LinkLinkLogic.kt
package com.eatif.app.games.linklink

data class LinkCell(
    val row: Int,
    val col: Int,
    val pattern: String?,
    val isRemoved: Boolean = false
)

class LinkLinkLogic(
    val gridSize: Int = 8
) {
    private var grid: Array<Array<LinkCell>> = Array(gridSize) { row ->
        Array(gridSize) { col -> LinkCell(row, col, null, false) }
    }
    
    private val patterns = listOf(
        "🍕", "🍔", "🍜", "🍣", "🍰", "🥗", "🥘", "🍝",
        "🌮", "🥪", "🍱", "🥡", "🍦", "🧁", "🥞", "🍿"
    )
    
    fun initializeGrid(): Array<Array<LinkCell>> {
        val totalCells = gridSize * gridSize
        val patternsNeeded = totalCells / 2
        val patternPairs = mutableListOf<String>()
        
        for (i in 0 until patternsNeeded) {
            val pattern = patterns[i % patterns.size]
            patternPairs.add(pattern)
            patternPairs.add(pattern)
        }
        
        // Shuffle patterns
        patternPairs.shuffle()
        
        // Fill grid
        var idx = 0
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                grid[row][col] = LinkCell(row, col, patternPairs[idx], false)
                idx++
            }
        }
        
        return grid
    }
    
    fun getGrid(): Array<Array<LinkCell>> = grid
    
    fun canConnect(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        if (r1 == r2 && c1 == c2) return false
        
        val cell1 = grid[r1][c1]
        val cell2 = grid[r2][c2]
        
        if (cell1.isRemoved || cell2.isRemoved) return false
        if (cell1.pattern != cell2.pattern) return false
        
        // Direct line (0 turns)
        if (canConnectDirect(r1, c1, r2, c2)) return true
        
        // One turn
        if (canConnectWithOneTurn(r1, c1, r2, c2)) return true
        
        // Two turns
        if (canConnectWithTwoTurns(r1, c1, r2, c2)) return true
        
        return false
    }
    
    private fun canConnectDirect(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        if (r1 != r2 && c1 != c2) return false
        
        if (r1 == r2) {
            val minCol = minOf(c1, c2)
            val maxCol = maxOf(c1, c2)
            for (col in minCol + 1 until maxCol) {
                if (!grid[r1][col].isRemoved) return false
            }
        } else {
            val minRow = minOf(r1, r2)
            val maxRow = maxOf(r1, r2)
            for (row in minRow + 1 until maxRow) {
                if (!grid[row][c1].isRemoved) return false
            }
        }
        
        return true
    }
    
    private fun canConnectWithOneTurn(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        // Try corner at (r1, c2)
        if (isEmptyOrTarget(r1, c2, r1, c1, r2, c2)) {
            if (canConnectDirect(r1, c1, r1, c2) && canConnectDirect(r1, c2, r2, c2)) {
                return true
            }
        }
        
        // Try corner at (r2, c1)
        if (isEmptyOrTarget(r2, c1, r1, c1, r2, c2)) {
            if (canConnectDirect(r1, c1, r2, c1) && canConnectDirect(r2, c1, r2, c2)) {
                return true
            }
        }
        
        return false
    }
    
    private fun canConnectWithTwoTurns(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        // Try horizontal middle line
        for (row in -1..gridSize) {
            if (row != r1 && row != r2) {
                val corner1Empty = row < 0 || row >= gridSize || grid[row][c1].isRemoved
                val corner2Empty = row < 0 || row >= gridSize || grid[row][c2].isRemoved
                
                if (corner1Empty && corner2Empty) {
                    val canReach1 = canReachFromTo(r1, c1, row, c1)
                    val canReach2 = canReachFromTo(row, c1, row, c2)
                    val canReach3 = canReachFromTo(row, c2, r2, c2)
                    
                    if (canReach1 && canReach2 && canReach3) return true
                }
            }
        }
        
        // Try vertical middle line
        for (col in -1..gridSize) {
            if (col != c1 && col != c2) {
                val corner1Empty = col < 0 || col >= gridSize || grid[r1][col].isRemoved
                val corner2Empty = col < 0 || col >= gridSize || grid[r2][col].isRemoved
                
                if (corner1Empty && corner2Empty) {
                    val canReach1 = canReachFromTo(r1, c1, r1, col)
                    val canReach2 = canReachFromTo(r1, col, r2, col)
                    val canReach3 = canReachFromTo(r2, col, r2, c2)
                    
                    if (canReach1 && canReach2 && canReach3) return true
                }
            }
        }
        
        return false
    }
    
    private fun isEmptyOrTarget(checkRow: Int, checkCol: Int, r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        if (checkRow < 0 || checkRow >= gridSize || checkCol < 0 || checkCol >= gridSize) return true
        val cell = grid[checkRow][checkCol]
        return cell.isRemoved || (checkRow == r1 && checkCol == c1) || (checkRow == r2 && checkCol == c2)
    }
    
    private fun canReachFromTo(fromR: Int, fromC: Int, toR: Int, toC: Int): Boolean {
        if (fromR == toR) {
            val minC = minOf(fromC, toC)
            val maxC = maxOf(fromC, toC)
            for (col in minC + 1 until maxC) {
                if (col >= 0 && col < gridSize && !grid[fromR][col].isRemoved) return false
            }
            return true
        }
        if (fromC == toC) {
            val minR = minOf(fromR, toR)
            val maxR = maxOf(fromR, toR)
            for (row in minR + 1 until maxR) {
                if (row >= 0 && row < gridSize && !grid[row][fromC].isRemoved) return false
            }
            return true
        }
        return false
    }
    
    fun removePair(r1: Int, c1: Int, r2: Int, c2: Int): Boolean {
        if (!canConnect(r1, c1, r2, c2)) return false
        
        grid[r1][c1] = grid[r1][c1].copy(isRemoved = true)
        grid[r2][c2] = grid[r2][c2].copy(isRemoved = true)
        
        return true
    }
    
    fun getRemainingPairs(): Int {
        var count = 0
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (!grid[row][col].isRemoved) count++
            }
        }
        return count / 2
    }
    
    fun hasPossibleMatch(): Boolean {
        val remaining = mutableListOf<Pair<Int, Int>>()
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (!grid[row][col].isRemoved) {
                    remaining.add(Pair(row, col))
                }
            }
        }
        
        for (i in remaining.indices) {
            for (j in i + 1 until remaining.size) {
                val (r1, c1) = remaining[i]
                val (r2, c2) = remaining[j]
                if (grid[r1][c1].pattern == grid[r2][c2].pattern) {
                    if (canConnect(r1, c1, r2, c2)) return true
                }
            }
        }
        
        return false
    }
    
    fun shuffle(): Array<Array<LinkCell>> {
        val remaining = mutableListOf<String?>()
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (!grid[row][col].isRemoved) {
                    remaining.add(grid[row][col].pattern)
                } else {
                    remaining.add(null)
                }
            }
        }
        
        // Shuffle non-null patterns
        val nonNullPatterns = remaining.filterNotNull().toMutableList()
        nonNullPatterns.shuffle()
        
        // Put back
        var idx = 0
        for (row in 0 until gridSize) {
            for (col in 0 until gridSize) {
                if (!grid[row][col].isRemoved) {
                    grid[row][col] = LinkCell(row, col, nonNullPatterns[idx], false)
                    idx++
                }
            }
        }
        
        return grid
    }
    
    fun isComplete(): Boolean {
        return getRemainingPairs() == 0
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add games/linklink/LinkLinkLogic.kt
git commit -m "feat: add LinkLink path detection algorithm (BFS/2-turn max)"
```

---

## Task 4: 连连看 UI (LinkLinkGame)

**Files:**
- Create: `app/src/main/java/com/eatif/app/games/linklink/LinkLinkGame.kt`

- [ ] **Step 1: 创建 LinkLinkGame.kt Composable**

```kotlin
// app/src/main/java/com/eatif/app/games/linklink/LinkLinkGame.kt
package com.eatif.app.games.linklink

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.GrayLight
import com.eatif.app.ui.theme.GrayMedium
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.White
import kotlin.random.Random

@Composable
fun LinkLinkGame(
    foods: List<Food>,
    isPaused: Boolean = false,
    onPauseToggle: ((Boolean) -> Unit)? = null,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    val logic = remember { LinkLinkLogic() }
    val gridSize = 8
    val cellSize = 40.dp
    
    var grid by remember { mutableStateOf(logic.initializeGrid().map { it.toList() }.toList()) }
    var pairsMatched by remember { mutableIntStateOf(0) }
    var selectedCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var gameState by remember { mutableStateOf("playing") }
    var totalPairs by remember { mutableIntStateOf(gridSize * gridSize / 2) }
    val targetFood = remember { foods.randomOrNull()?.name ?: "美食" }
    
    fun handleCellClick(row: Int, col: Int) {
        if (gameState != "playing" || isPaused) return
        
        val cell = grid[row][col]
        if (cell.isRemoved) return
        
        if (selectedCell == null) {
            selectedCell = Pair(row, col)
        } else {
            val (prevRow, prevCol) = selectedCell!!
            if (prevRow == row && prevCol == col) {
                selectedCell = null
                return
            }
            
            if (logic.removePair(prevRow, prevCol, row, col)) {
                grid = logic.getGrid().map { it.toList() }.toList()
                pairsMatched++
                
                if (logic.isComplete()) {
                    gameState = "win"
                    val scorePercent = (pairsMatched * 100 / totalPairs).coerceIn(0, 100)
                    onResult(targetFood, scorePercent)
                } else if (!logic.hasPossibleMatch()) {
                    // Auto shuffle when no possible match
                    grid = logic.shuffle().map { it.toList() }.toList()
                }
            }
            selectedCell = null
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🔗 连连看",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "配对: $pairsMatched / $totalPairs",
            style = MaterialTheme.typography.titleMedium,
            color = OrangePrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridSize),
            modifier = Modifier.size(cellSize * gridSize),
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(grid.flatten().toList().withIndex().toList()) { indexed ->
                val row = indexed.index / gridSize
                val col = indexed.index % gridSize
                val cell = indexed.value
                
                Box(
                    modifier = Modifier
                        .size(cellSize)
                        .background(
                            if (cell.isRemoved) GrayLight
                            else if (selectedCell == Pair(row, col)) OrangePrimary
                            else GrayMedium,
                            RoundedCornerShape(4.dp)
                        )
                        .then(
                            if (!cell.isRemoved && selectedCell == Pair(row, col)) {
                                Modifier.border(2.dp, White, RoundedCornerShape(4.dp))
                            } else Modifier
                        )
                        .clickable(enabled = !cell.isRemoved) { handleCellClick(row, col) },
                    contentAlignment = Alignment.Center
                ) {
                    if (!cell.isRemoved && cell.pattern != null) {
                        Text(
                            text = cell.pattern,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        if (gameState != "playing") {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (gameState == "win") "恭喜完成! 获得: $targetFood" else "游戏结束",
                style = MaterialTheme.typography.titleLarge,
                color = OrangePrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    val newGrid = logic.initializeGrid()
                    grid = newGrid.map { it.toList() }.toList()
                    pairsMatched = 0
                    totalPairs = gridSize * gridSize / 2
                    gameState = "playing"
                    selectedCell = null
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("再来一次", color = White)
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add games/linklink/LinkLinkGame.kt
git commit -m "feat: add LinkLinkGame UI Composable"
```

---

## Task 5: 记忆翻牌 (MemoryGame)

**Files:**
- Create: `app/src/main/java/com/eatif/app/games/memory/MemoryGame.kt`

- [ ] **Step 1: 创建 MemoryGame.kt Composable**

```kotlin
// app/src/main/java/com/eatif/app/games/memory/MemoryGame.kt
package com.eatif.app.games.memory

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.GrayMedium
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.White
import kotlinx.coroutines.delay
import kotlin.random.Random

data class MemoryCard(
    val id: Int,
    val pattern: String,
    val isFlipped: Boolean = false,
    val isMatched: Boolean = false
)

@Composable
fun MemoryGame(
    foods: List<Food>,
    isPaused: Boolean = false,
    onPauseToggle: ((Boolean) -> Unit)? = null,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    val gridSize = 4
    val cellSize = 64.dp
    val patterns = listOf("🍕", "🍔", "🍜", "🍣", "🍰", "🥗", "🥘", "🍝")
    
    var cards by remember { mutableStateOf(initCards(patterns)) }
    var flippedCards by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var pairsMatched by remember { mutableIntStateOf(0) }
    var wrongAttempts by remember { mutableIntStateOf(0) }
    var gameState by remember { mutableStateOf("playing") }
    var isProcessing by remember { mutableStateOf(false) }
    val targetFood = remember { foods.randomOrNull()?.name ?: "美食" }
    
    fun handleCardClick(cardId: Int) {
        if (gameState != "playing" || isPaused || isProcessing) return
        
        val cardIndex = cards.indexOfFirst { it.id == cardId }
        if (cardIndex == -1) return
        
        val card = cards[cardIndex]
        if (card.isFlipped || card.isMatched) return
        
        cards = cards.map { if (it.id == cardId) it.copy(isFlipped = true) else it }
        
        if (flippedCards == null) {
            flippedCards = Pair(cardId, -1)
        } else {
            val firstCardId = flippedCards!!.first
            val firstCard = cards.find { it.id == firstCardId }!!
            
            if (firstCard.pattern == card.pattern) {
                // Match!
                cards = cards.map {
                    if (it.id == firstCardId || it.id == cardId) it.copy(isMatched = true)
                    else it
                }
                pairsMatched++
                flippedCards = null
                
                if (pairsMatched == patterns.size) {
                    gameState = "win"
                    val scorePercent = ((pairsMatched * 100 / patterns.size) - (wrongAttempts * 5)).coerceIn(0, 100)
                    onResult(targetFood, scorePercent)
                }
            } else {
                // No match
                wrongAttempts++
                isProcessing = true
                
                LaunchedEffect(Unit) {
                    delay(1000)
                    cards = cards.map {
                        if (it.id == firstCardId || it.id == cardId) it.copy(isFlipped = false)
                        else it
                    }
                    flippedCards = null
                    isProcessing = false
                }
            }
        }
    }
    
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🃏 记忆翻牌",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "配对: $pairsMatched / ${patterns.size} | 翻错: $wrongAttempts",
            style = MaterialTheme.typography.titleMedium,
            color = OrangePrimary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(gridSize),
            modifier = Modifier.size(cellSize * gridSize),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(cards) { card ->
                val rotation by animateFloatAsState(
                    targetValue = if (card.isFlipped || card.isMatched) 0f else 180f,
                    animationSpec = tween(300),
                    label = "card_flip"
                )
                
                Box(
                    modifier = Modifier
                        .size(cellSize)
                        .rotate(rotation)
                        .background(
                            if (card.isMatched) OrangePrimary
                            else if (card.isFlipped) White
                            else GrayMedium,
                            RoundedCornerShape(8.dp)
                        )
                        .clickable(enabled = !card.isFlipped && !card.isMatched) {
                            handleCardClick(card.id)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (card.isFlipped || card.isMatched) {
                        Text(
                            text = card.pattern,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        if (gameState != "playing") {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "完成! 获得: $targetFood",
                style = MaterialTheme.typography.titleLarge,
                color = OrangePrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    cards = initCards(patterns)
                    flippedCards = null
                    pairsMatched = 0
                    wrongAttempts = 0
                    gameState = "playing"
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
            ) {
                Text("再来一次", color = White)
            }
        }
    }
}

private fun initCards(patterns: List<String>): List<MemoryCard> {
    val cardPairs = mutableListOf<MemoryCard>()
    patterns.forEachIndexed { index, pattern ->
        cardPairs.add(MemoryCard(index * 2, pattern))
        cardPairs.add(MemoryCard(index * 2 + 1, pattern))
    }
    cardPairs.shuffle()
    return cardPairs
}
```

- [ ] **Step 2: Commit**

```bash
git add games/memory/MemoryGame.kt
git commit -m "feat: add MemoryGame UI Composable (翻牌配对游戏)"
```

---

## Task 6: 乒乓球 (PingPongGame)

**Files:**
- Create: `app/src/main/java/com/eatif/app/games/pingpong/PingPongGame.kt`

- [ ] **Step 1: 创建 PingPongGame.kt Composable**

```kotlin
// app/src/main/java/com/eatif/app/games/pingpong/PingPongGame.kt
package com.eatif.app.games.pingpong

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.GrayDark
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.White
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun PingPongGame(
    foods: List<Food>,
    isPaused: Boolean = false,
    onPauseToggle: ((Boolean) -> Unit)? = null,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    val targetFood = remember { foods.randomOrNull()?.name ?: "美食" }
    
    var gameState by remember { mutableStateOf("playing") }
    var scoreLeft by remember { mutableIntStateOf(0) }
    var scoreRight by remember { mutableIntStateOf(0) }
    
    // Ball position and velocity
    var ballX by remember { mutableFloatStateOf(0.5f) }
    var ballY by remember { mutableFloatStateOf(0.5f) }
    var ballVX by remember { mutableFloatStateOf(0.02f) }
    var ballVY by remember { mutableFloatStateOf(0.01f) }
    
    // Paddle positions (Y = 0 to 1, representing screen height)
    var paddleLeftY by remember { mutableFloatStateOf(0.5f) }
    var paddleRightY by remember { mutableFloatStateOf(0.5f) }
    
    val paddleHeight = 0.15f
    val paddleWidth = 0.02f
    val ballSize = 0.02f
    
    val isDoubleMode = mode == "double"
    
    LaunchedEffect(gameState, isPaused) {
        while (gameState == "playing" && !isPaused) {
            delay(16) // ~60 FPS
            
            // Move ball
            ballX += ballVX
            ballY += ballVY
            
            // Ball collision with top/bottom
            if (ballY <= 0 || ballY >= 1) {
                ballVY = -ballVY
                ballY = ballY.coerceIn(0f, 1f)
            }
            
            // Ball collision with left paddle
            if (ballX <= paddleWidth && 
                ballY >= paddleLeftY - paddleHeight / 2 &&
                ballY <= paddleLeftY + paddleHeight / 2) {
                ballVX = abs(ballVX) * 1.05f
                ballVY += (ballY - paddleLeftY) * 0.1f
            }
            
            // Ball collision with right paddle
            if (ballX >= 1 - paddleWidth &&
                ballY >= paddleRightY - paddleHeight / 2 &&
                ballY <= paddleRightY + paddleHeight / 2) {
                ballVX = -abs(ballVX) * 1.05f
                ballVY += (ballY - paddleRightY) * 0.1f
            }
            
            // Ball out of bounds
            if (ballX < 0) {
                scoreRight++
                ballX = 0.5f
                ballY = 0.5f
                ballVX = 0.02f
                ballVY = Random.nextFloat() * 0.02f - 0.01f
            }
            
            if (ballX > 1) {
                scoreLeft++
                ballX = 0.5f
                ballY = 0.5f
                ballVX = -0.02f
                ballVY = Random.nextFloat() * 0.02f - 0.01f
            }
            
            // Speed limit
            ballVX = ballVX.coerceIn(-0.1f, 0.1f)
            ballVY = ballVY.coerceIn(-0.1f, 0.1f)
            
            // AI paddle for single mode
            if (!isDoubleMode && gameState == "playing") {
                paddleLeftY += (ballY - paddleLeftY) * 0.05f
                paddleLeftY = paddleLeftY.coerceIn(paddleHeight / 2, 1 - paddleHeight / 2)
            }
            
            // Game over check
            if (scoreLeft >= 11 || scoreRight >= 11) {
                gameState = "gameover"
                val scorePercent = (scoreRight * 100 / 11).coerceIn(0, 100)
                onResult(targetFood, scorePercent)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) {
                // Touch input for paddle control
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        val position = event.changes.first().position
                        val screenWidth = size.width.toFloat()
                        val screenHeight = size.height.toFloat()
                        
                        val touchY = (position.y / screenHeight).coerceIn(0f, 1f)
                        val touchX = position.x / screenWidth
                        
                        if (isDoubleMode) {
                            if (touchX < 0.5f) {
                                paddleLeftY = touchY.coerceIn(paddleHeight / 2, 1 - paddleHeight / 2)
                            } else {
                                paddleRightY = touchY.coerceIn(paddleHeight / 2, 1 - paddleHeight / 2)
                            }
                        } else {
                            paddleRightY = touchY.coerceIn(paddleHeight / 2, 1 - paddleHeight / 2)
                        }
                    }
                }
            }
    ) {
        Text(
            text = "🏓 乒乓球",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                text = "左: $scoreLeft",
                style = MaterialTheme.typography.titleMedium,
                color = OrangePrimary
            )
            Spacer(modifier = Modifier.width(24.dp))
            Text(
                text = "右: $scoreRight",
                style = MaterialTheme.typography.titleMedium,
                color = OrangePrimary
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height
                
                // Background
                drawRect(GrayDark)
                
                // Center line
                drawLine(
                    color = White,
                    start = Offset(canvasWidth / 2, 0),
                    end = Offset(canvasWidth / 2, canvasHeight),
                    strokeWidth = 2f
                )
                
                // Left paddle
                drawRect(
                    color = OrangePrimary,
                    topLeft = Offset(
                        0,
                        (paddleLeftY - paddleHeight / 2) * canvasHeight
                    ),
                    size = Size(
                        paddleWidth * canvasWidth,
                        paddleHeight * canvasHeight
                    )
                )
                
                // Right paddle
                drawRect(
                    color = White,
                    topLeft = Offset(
                        (1 - paddleWidth) * canvasWidth,
                        (paddleRightY - paddleHeight / 2) * canvasHeight
                    ),
                    size = Size(
                        paddleWidth * canvasWidth,
                        paddleHeight * canvasHeight
                    )
                )
                
                // Ball
                drawCircle(
                    color = White,
                    radius = ballSize * canvasWidth / 2,
                    center = Offset(
                        ballX * canvasWidth,
                        ballY * canvasHeight
                    )
                )
            }
        }
        
        if (gameState != "playing") {
            Text(
                text = "游戏结束! 获得: $targetFood",
                style = MaterialTheme.typography.titleLarge,
                color = OrangePrimary,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    gameState = "playing"
                    scoreLeft = 0
                    scoreRight = 0
                    ballX = 0.5f
                    ballY = 0.5f
                    ballVX = 0.02f
                    ballVY = 0.01f
                    paddleLeftY = 0.5f
                    paddleRightY = 0.5f
                },
                colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("再来一次", color = White)
            }
        }
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add games/pingpong/PingPongGame.kt
git commit -m "feat: add PingPongGame UI Composable (双人对战/单人AI)"
```

---

## Task 7: 注册新游戏到 GameRegistry

**Files:**
- Modify: `app/src/main/java/com/eatif/app/games/GameRegistryInit.kt`

- [ ] **Step 1: 添加新游戏 imports 和注册**

读取现有文件后，在 imports 部分添加：

```kotlin
import com.eatif.app.games.match3.Match3Game
import com.eatif.app.games.linklink.LinkLinkGame
import com.eatif.app.games.memory.MemoryGame
import com.eatif.app.games.pingpong.PingPongGame
```

在 `initGameRegistry()` 函数末尾添加：

```kotlin
GameRegistry.register(GameConfig("match3", supportsSelfPause = true) { foods, isPaused, onPauseToggle, onResult, mode ->
    Match3Game(foods = foods, isPaused = isPaused, onPauseToggle = onPauseToggle, onResult = onResult, mode = mode)
})
GameRegistry.register(GameConfig("linklink") { foods, isPaused, _, onResult, mode ->
    LinkLinkGame(foods = foods, isPaused = isPaused, onResult = onResult, mode = mode)
})
GameRegistry.register(GameConfig("memory") { foods, isPaused, _, onResult, mode ->
    MemoryGame(foods = foods, isPaused = isPaused, onResult = onResult, mode = mode)
})
GameRegistry.register(GameConfig("pingpong", supportsSelfPause = true) { foods, isPaused, onPauseToggle, onResult, mode ->
    PingPongGame(foods = foods, isPaused = isPaused, onPauseToggle = onPauseToggle, onResult = onResult, mode = mode)
})
```

- [ ] **Step 2: Commit**

```bash
git add games/GameRegistryInit.kt
git commit -m "feat: register 4 new games (match3/linklink/memory/pingpong)"
```

---

## Task 8: 添加游戏定义到 GameList

**Files:**
- Modify: `app/src/main/java/com/eatif/app/domain/model/Game.kt`

- [ ] **Step 1: 添加新游戏到 GameList.games**

在 `GameList.games = listOf(...)` 中添加 4 个新 Game 定义：

```kotlin
Game("match3", "消消乐", "🧩", "益智类", GameCategory.PUZZLE),
Game("linklink", "连连看", "🔗", "益智类", GameCategory.PUZZLE),
Game("memory", "记忆翻牌", "🃏", "益智类", GameCategory.PUZZLE),
Game("pingpong", "乒乓球", "🏓", "对战类", GameCategory.BATTLE)
```

- [ ] **Step 2: Commit**

```bash
git add domain/model/Game.kt
git commit -m "feat: add 4 new game definitions to GameList"
```

---

## Task 9: 添加关卡定义

**Files:**
- Modify: `app/src/main/java/com/eatif/app/domain/usecase/GameLevelRegistry.kt`

- [ ] **Step 1: 为每款新游戏添加关卡**

在 GameLevelRegistry 中添加关卡定义（每款游戏 10 关）：

```kotlin
// Match3 levels
val match3Levels = listOf(
    GameLevel("match3", 1, GameDifficulty.EASY, 0, mapOf("moves" to "40")),
    GameLevel("match3", 2, GameDifficulty.EASY, 1, mapOf("moves" to "35")),
    // ... 到 level 10
)

// LinkLink levels
val linklinkLevels = listOf(
    GameLevel("linklink", 1, GameDifficulty.EASY, 0, mapOf("gridSize" to "6")),
    // ... 到 level 10
)

// Memory levels
val memoryLevels = listOf(
    GameLevel("memory", 1, GameDifficulty.EASY, 0, mapOf("gridSize" to "4")),
    // ... 到 level 10
)

// PingPong levels
val pingpongLevels = listOf(
    GameLevel("pingpong", 1, GameDifficulty.EASY, 0, mapOf("speed" to "0.02")),
    // ... 到 level 10
)
```

- [ ] **Step 2: Commit**

```bash
git add domain/usecase/GameLevelRegistry.kt
git commit -m "feat: add level definitions for 4 new games"
```

---

## Task 10: 添加成就定义

**Files:**
- Modify: `app/src/main/java/com/eatif/app/domain/usecase/AchievementRegistry.kt`

- [ ] **Step 1: 添加新游戏成就**

在 AchievementRegistry.all 中添加：

```kotlin
Achievement(
    id = "match3_combo_5",
    name = "连击大师",
    description = "消消乐单次消除5组以上",
    icon = "🔥",
    category = AchievementCategory.SKILL,
    condition = AchievementCondition.Custom("match3_combo", 5),
    xpReward = 50
),
Achievement(
    id = "linklink_perfect",
    name = "连连看完美通关",
    description = "连连看零失误完成",
    icon = "✨",
    category = AchievementCategory.SKILL,
    condition = AchievementCondition.Custom("linklink_perfect", 1),
    xpReward = 100
),
Achievement(
    id = "memory_no_error",
    name = "记忆力满分",
    description = "记忆翻牌零翻错完成",
    icon = "🧠",
    category = AchievementCategory.SKILL,
    condition = AchievementCondition.Custom("memory_no_error", 1),
    xpReward = 80
),
Achievement(
    id = "pingpong_win_5",
    name = "乒乓球连胜",
    description = "乒乓球连续获胜5次",
    icon = "🏆",
    category = AchievementCategory.STREAK,
    condition = AchievementCondition.Custom("pingpong_win", 5),
    xpReward = 60
)
```

- [ ] **Step 2: Commit**

```bash
git add domain/usecase/AchievementRegistry.kt
git commit -m "feat: add achievement definitions for 4 new games"
```

---

## Task 11: 最终验证与推送

- [ ] **Step 1: 推送到 GitHub**

```bash
git push origin master
```

- [ ] **Step 2: 检查 GitHub Actions 构建**

等待 CI 构建，确认 APK 生成成功。

- [ ] **Step 3: 完成确认**

全部任务完成后，新游戏已成功添加到 Eat If App。