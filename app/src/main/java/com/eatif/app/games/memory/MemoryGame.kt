package com.eatif.app.games.memory

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
import androidx.compose.runtime.mutableStateListOf
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
import kotlin.random.Random

private enum class GameState {
    IDLE, PLAYING, FLIPPING_BACK, WON, GAME_OVER
}

data class MemoryCard(
    val pattern: Int,
    val flipped: Boolean = false,
    val matched: Boolean = false
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
    val totalPairs = gridSize * gridSize / 2
    
    val patternEmojis = listOf(
        "🍎", "🍊", "🍋", "🍇", "🍓", "🍑", "🥝", "🍒"
    )
    
    var gameState by remember { mutableStateOf(GameState.IDLE) }
    var pairsMatched by remember { mutableIntStateOf(0) }
    var wrongAttempts by remember { mutableIntStateOf(0) }
    var firstFlipped by remember { mutableStateOf(-1) }
    var secondFlipped by remember { mutableStateOf(-1) }
    var internalPaused by remember { mutableStateOf(false) }
    val actualPaused = isPaused || internalPaused
    
    val cards = remember { mutableStateListOf<MemoryCard>() }
    
    fun initGame() {
        val patterns = mutableListOf<Int>()
        for (i in 0 until totalPairs) {
            patterns.add(i)
            patterns.add(i)
        }
        patterns.shuffle()
        
        cards.clear()
        for (pattern in patterns) {
            cards.add(MemoryCard(pattern))
        }
        
        pairsMatched = 0
        wrongAttempts = 0
        firstFlipped = -1
        secondFlipped = -1
        gameState = GameState.PLAYING
    }
    
    fun handleCardClick(index: Int) {
        if (gameState != GameState.PLAYING || actualPaused) return
        if (cards[index].matched || cards[index].flipped) return
        
        if (firstFlipped == -1) {
            cards[index] = cards[index].copy(flipped = true)
            firstFlipped = index
        } else if (secondFlipped == -1) {
            cards[index] = cards[index].copy(flipped = true)
            secondFlipped = index
            
            if (cards[firstFlipped].pattern == cards[secondFlipped].pattern) {
                cards[firstFlipped] = cards[firstFlipped].copy(matched = true)
                cards[secondFlipped] = cards[secondFlipped].copy(matched = true)
                pairsMatched++
                firstFlipped = -1
                secondFlipped = -1
                
                if (pairsMatched == totalPairs) {
                    gameState = GameState.WON
                }
            } else {
                wrongAttempts++
                gameState = GameState.FLIPPING_BACK
            }
        }
    }
    
    LaunchedEffect(gameState, actualPaused) {
        if (gameState == GameState.FLIPPING_BACK && !actualPaused) {
            delay(1000)
            cards[firstFlipped] = cards[firstFlipped].copy(flipped = false)
            cards[secondFlipped] = cards[secondFlipped].copy(flipped = false)
            firstFlipped = -1
            secondFlipped = -1
            gameState = GameState.PLAYING
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
            text = "🧠 记忆翻牌",
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
                .size(280.dp)
                .background(GrayMedium.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                .padding(8.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(gridSize),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(gridSize * gridSize) { index ->
                    val card = cards.getOrElse(index) { MemoryCard(-1) }
                    
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    card.matched -> Green.copy(alpha = 0.3f)
                                    card.flipped -> White
                                    else -> OrangePrimary
                                }
                            )
                            .then(
                                if (card.flipped && !card.matched) {
                                    Modifier.border(2.dp, OrangePrimary, RoundedCornerShape(8.dp))
                                } else Modifier
                            )
                            .clickable(enabled = gameState == GameState.PLAYING && !card.matched && !card.flipped) {
                                handleCardClick(index)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (card.matched || card.flipped) {
                            if (card.pattern >= 0 && card.pattern < patternEmojis.size) {
                                Text(
                                    text = patternEmojis[card.pattern],
                                    fontSize = 32.sp
                                )
                            }
                        } else {
                            Text(
                                text = "?",
                                fontSize = 28.sp,
                                color = White
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (gameState == GameState.IDLE || gameState == GameState.WON) {
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
        
        if (gameState == GameState.PLAYING || gameState == GameState.FLIPPING_BACK) {
            Text(
                text = "点击翻开卡片，找到相同的图案",
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