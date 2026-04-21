package com.eatif.app.games.match3

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.GrayLight
import com.eatif.app.ui.theme.GrayMedium
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White
import kotlinx.coroutines.delay

private enum class GameState {
    IDLE, PLAYING, PROCESSING, WON, GAME_OVER
}

@Composable
fun Match3Game(
    foods: List<Food>,
    isPaused: Boolean = false,
    onPauseToggle: ((Boolean) -> Unit)? = null,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    val gridSize = 6
    val maxMoves = 30
    val targetScore = 500
    
    var gameState by remember { mutableStateOf(GameState.IDLE) }
    var score by remember { mutableIntStateOf(0) }
    var movesLeft by remember { mutableIntStateOf(maxMoves) }
    var selectedRow by remember { mutableIntStateOf(-1) }
    var selectedCol by remember { mutableIntStateOf(-1) }
    var internalPaused by remember { mutableStateOf(false) }
    val actualPaused = isPaused || internalPaused
    
    val logic = remember { Match3Logic(gridSize) }
    var gridVersion by remember { mutableIntStateOf(0) }
    
    fun initGame() {
        logic.initializeGrid()
        score = 0
        movesLeft = maxMoves
        selectedRow = -1
        selectedCol = -1
        gameState = GameState.PLAYING
        gridVersion++
    }
    
    fun handleCellClick(row: Int, col: Int) {
        if (gameState != GameState.PLAYING || actualPaused) return
        
        if (selectedRow == -1) {
            selectedRow = row
            selectedCol = col
        } else {
            if (selectedRow == row && selectedCol == col) {
                selectedRow = -1
                selectedCol = -1
            } else {
                val swapped = logic.swapGems(selectedRow, selectedCol, row, col)
                if (swapped) {
                    movesLeft--
                    gameState = GameState.PROCESSING
                    gridVersion++
                }
                selectedRow = -1
                selectedCol = -1
            }
        }
    }
    
    LaunchedEffect(gameState, actualPaused) {
        if (gameState == GameState.PROCESSING && !actualPaused) {
            delay(200)
            val removed = logic.processTurn()
            score += removed * 10
            gridVersion++
            
            if (score >= targetScore) {
                gameState = GameState.WON
            } else if (logic.isGameOver(movesLeft)) {
                gameState = GameState.GAME_OVER
            } else {
                gameState = GameState.PLAYING
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayLight.copy(alpha = 0.1f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "💎 消消乐",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "分数: $score / $targetScore",
                style = MaterialTheme.typography.titleLarge,
                color = OrangePrimary
            )
            Text(
                text = "剩余: $movesLeft 步",
                style = MaterialTheme.typography.titleLarge,
                color = if (movesLeft <= 5) Red else GrayMedium
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .size(280.dp)
                .background(GrayMedium.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridSize),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(gridSize * gridSize) { index ->
                    val row = index / gridSize
                    val col = index % gridSize
                    
                    @Suppress("UNUSED_EXPRESSION")
                    gridVersion
                    
                    val gem = logic.getGem(row, col)
                    val isSelected = selectedRow == row && selectedCol == col
                    
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(
                                when (gem) {
                                    GemColor.RED -> Red
                                    GemColor.ORANGE -> OrangePrimary
                                    GemColor.YELLOW -> Color.Yellow
                                    GemColor.GREEN -> Green
                                    GemColor.BLUE -> Color.Blue
                                    GemColor.PURPLE -> Color(0xFFAF52DE)
                                    null -> GrayLight
                                }
                            )
                            .then(
                                if (isSelected) {
                                    Modifier.background(Color.White.copy(alpha = 0.5f))
                                } else Modifier
                            )
                            .clickable { handleCellClick(row, col) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (gem != null) {
                            Text(
                                text = when (gem) {
                                    GemColor.RED -> "🔴"
                                    GemColor.ORANGE -> "🟠"
                                    GemColor.YELLOW -> "🟡"
                                    GemColor.GREEN -> "🟢"
                                    GemColor.BLUE -> "🔵"
                                    GemColor.PURPLE -> "🟣"
                                },
                                fontSize = 20.dp.value.toInt().sp
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (gameState == GameState.IDLE || gameState == GameState.GAME_OVER || gameState == GameState.WON) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (gameState) {
                    GameState.WON -> {
                        Text(
                            text = "🎉 恭喜通关!",
                            style = MaterialTheme.typography.titleLarge,
                            color = Green
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (foods.isNotEmpty()) {
                            Text(
                                text = "选择美食奖励自己:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            foods.take(3).forEach { food ->
                                Button(
                                    onClick = { onResult(food.name, 100) },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Green,
                                        contentColor = White
                                    )
                                ) {
                                    Text(food.name)
                                }
                            }
                        }
                    }
                    GameState.GAME_OVER -> {
                        Text(
                            text = "😵 游戏结束!",
                            style = MaterialTheme.typography.titleLarge,
                            color = Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        if (foods.isNotEmpty()) {
                            Text(
                                text = "选择美食安慰自己:",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            foods.take(3).forEach { food ->
                                Button(
                                    onClick = { onResult(food.name, (score * 100 / targetScore).coerceIn(0, 100)) },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OrangePrimary,
                                        contentColor = White
                                    )
                                ) {
                                    Text(food.name)
                                }
                            }
                        }
                    }
                    else -> {}
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Button(
                    onClick = { initGame() },
                    modifier = Modifier.size(width = 200.dp, height = 56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = White
                    )
                ) {
                    Text(
                        text = if (gameState == GameState.IDLE) "开始游戏" else "重新开始",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
        
        if (gameState == GameState.PLAYING || gameState == GameState.PROCESSING) {
            Text(
                text = "点击选择宝石，再点击相邻宝石交换",
                style = MaterialTheme.typography.bodyMedium,
                color = GrayMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(
                    onClick = {
                        internalPaused = !internalPaused
                        onPauseToggle?.invoke(internalPaused)
                    },
                    modifier = Modifier.size(width = 120.dp, height = 48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GrayMedium,
                        contentColor = White
                    )
                ) {
                    Text(if (internalPaused) "继续" else "暂停")
                }
                
                Button(
                    onClick = { initGame() },
                    modifier = Modifier.size(width = 120.dp, height = 48.dp),
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GrayMedium,
                        contentColor = White
                    )
                ) {
                    Text("重新开始")
                }
            }
        }
    }
}