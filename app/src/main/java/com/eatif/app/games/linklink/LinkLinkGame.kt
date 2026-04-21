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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.unit.sp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.GrayLight
import com.eatif.app.ui.theme.GrayMedium
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White
import kotlinx.coroutines.delay

private enum class GameState {
    IDLE, PLAYING, WON, GAME_OVER
}

@Composable
fun LinkLinkGame(
    foods: List<Food>,
    isPaused: Boolean = false,
    onPauseToggle: ((Boolean) -> Unit)? = null,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    val gridSize = 8
    val totalPairs = gridSize * gridSize / 2
    
    var gameState by remember { mutableStateOf(GameState.IDLE) }
    var pairsMatched by remember { mutableIntStateOf(0) }
    var selectedRow by remember { mutableIntStateOf(-1) }
    var selectedCol by remember { mutableIntStateOf(-1) }
    var wrongAttempts by remember { mutableIntStateOf(0) }
    var internalPaused by remember { mutableStateOf(false) }
    val actualPaused = isPaused || internalPaused
    
    val logic = remember { LinkLinkLogic(gridSize) }
    var gridVersion by remember { mutableIntStateOf(0) }
    
    fun initGame() {
        logic.initializeGrid()
        pairsMatched = 0
        wrongAttempts = 0
        selectedRow = -1
        selectedCol = -1
        gameState = GameState.PLAYING
        gridVersion++
    }
    
    fun handleCellClick(row: Int, col: Int) {
        if (gameState != GameState.PLAYING || actualPaused) return
        if (logic.isRemoved(row, col)) return
        
        if (selectedRow == -1) {
            selectedRow = row
            selectedCol = col
        } else {
            if (selectedRow == row && selectedCol == col) {
                selectedRow = -1
                selectedCol = -1
                return
            }
            
            val pattern1 = logic.getPattern(selectedRow, selectedCol)
            val pattern2 = logic.getPattern(row, col)
            
            if (pattern1 == pattern2 && logic.canConnect(selectedRow, selectedCol, row, col)) {
                logic.removePair(selectedRow, selectedCol, row, col)
                pairsMatched++
                gridVersion++
                
                if (logic.isComplete()) {
                    gameState = GameState.WON
                } else if (!logic.hasPossibleMatch()) {
                    logic.shuffle()
                    gridVersion++
                }
            } else {
                wrongAttempts++
            }
            
            selectedRow = -1
            selectedCol = -1
        }
    }
    
    LaunchedEffect(gameState, actualPaused) {
        if (gameState == GameState.PLAYING && !actualPaused) {
            while (gameState == GameState.PLAYING && !actualPaused) {
                delay(500)
                if (!logic.isComplete() && !logic.hasPossibleMatch()) {
                    logic.shuffle()
                    gridVersion++
                }
            }
        }
    }
    
    val score = pairsMatched * 10 - wrongAttempts * 2
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayLight.copy(alpha = 0.1f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🔗 连连看",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "配对: $pairsMatched / $totalPairs",
                style = MaterialTheme.typography.titleLarge,
                color = OrangePrimary
            )
            Text(
                text = "分数: $score",
                style = MaterialTheme.typography.titleLarge,
                color = if (score >= 0) Green else Red
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .size(320.dp)
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
                    
                    val isRemoved = logic.isRemoved(row, col)
                    val pattern = logic.getPattern(row, col)
                    val isSelected = selectedRow == row && selectedCol == col
                    
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                when {
                                    isRemoved -> GrayLight.copy(alpha = 0.3f)
                                    isSelected -> OrangePrimary.copy(alpha = 0.3f)
                                    else -> White
                                }
                            )
                            .then(
                                if (isSelected && !isRemoved) {
                                    Modifier.border(2.dp, OrangePrimary, RoundedCornerShape(4.dp))
                                } else Modifier
                            )
                            .clickable(enabled = !isRemoved) { handleCellClick(row, col) },
                        contentAlignment = Alignment.Center
                    ) {
                        if (!isRemoved && pattern != null) {
                            Text(
                                text = logic.getPatternEmoji(pattern),
                                fontSize = 24.sp
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
                                    onClick = { onResult(food.name, score.coerceIn(0, 100)) },
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
                                    onClick = { onResult(food.name, score.coerceIn(0, 100)) },
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
        
        if (gameState == GameState.PLAYING) {
            Text(
                text = "点击两个相同图案进行配对",
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = {
                    logic.shuffle()
                    gridVersion++
                },
                modifier = Modifier.size(width = 160.dp, height = 48.dp),
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = OrangePrimary,
                    contentColor = White
                )
            ) {
                Text("洗牌")
            }
        }
    }
}