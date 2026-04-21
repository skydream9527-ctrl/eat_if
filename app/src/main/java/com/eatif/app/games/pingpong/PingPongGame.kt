package com.eatif.app.games.pingpong

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.GrayLight
import com.eatif.app.ui.theme.GrayMedium
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White
import kotlinx.coroutines.delay
import kotlin.math.abs
import kotlin.math.sign

private enum class GameState {
    IDLE, PLAYING, WON, GAME_OVER
}

@Composable
fun PingPongGame(
    foods: List<Food>,
    isPaused: Boolean = false,
    onPauseToggle: ((Boolean) -> Unit)? = null,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    val canvasWidth = 300f
    val canvasHeight = 400f
    val paddleWidth = 80f
    val paddleHeight = 15f
    val ballSize = 15f
    val winningScore = 11
    
    var gameState by remember { mutableStateOf(GameState.IDLE) }
    var player1Score by remember { mutableIntStateOf(0) }
    var player2Score by remember { mutableIntStateOf(0) }
    var internalPaused by remember { mutableStateOf(false) }
    val actualPaused = isPaused || internalPaused
    
    var ballX by remember { mutableFloatStateOf(canvasWidth / 2) }
    var ballY by remember { mutableFloatStateOf(canvasHeight / 2) }
    var ballVX by remember { mutableFloatStateOf(3f) }
    var ballVY by remember { mutableFloatStateOf(3f) }
    
    var paddle1Y by remember { mutableFloatStateOf(canvasHeight / 2 - paddleWidth / 2) }
    var paddle2Y by remember { mutableFloatStateOf(canvasHeight / 2 - paddleWidth / 2) }
    
    fun resetBall() {
        ballX = canvasWidth / 2
        ballY = canvasHeight / 2
        ballVX = 3f * (if (player1Score > player2Score) -1 else 1)
        ballVY = 3f * sign(ballVY)
    }
    
    fun initGame() {
        player1Score = 0
        player2Score = 0
        paddle1Y = canvasHeight / 2 - paddleWidth / 2
        paddle2Y = canvasHeight / 2 - paddleWidth / 2
        resetBall()
        gameState = GameState.PLAYING
    }
    
    fun updateGame() {
        ballX += ballVX
        ballY += ballVY
        
        if (ballY <= paddleHeight || ballY >= canvasHeight - paddleHeight - ballSize) {
            ballVY = -ballVY
            ballY = ballY.coerceIn(paddleHeight, canvasHeight - paddleHeight - ballSize)
        }
        
        val paddle1X = 0f
        val paddle2X = canvasWidth - paddleHeight
        
        if (ballX <= paddle1X + paddleHeight + ballSize) {
            if (ballY >= paddle1Y && ballY <= paddle1Y + paddleWidth) {
                ballVX = abs(ballVX) + 0.5f
                ballVY += (ballY - paddle1Y - paddleWidth / 2) * 0.05f
                ballX = paddle1X + paddleHeight + ballSize
            } else if (ballX <= paddle1X) {
                player2Score++
                if (player2Score >= winningScore) {
                    gameState = GameState.GAME_OVER
                } else {
                    resetBall()
                }
            }
        }
        
        if (ballX >= paddle2X - ballSize) {
            if (ballY >= paddle2Y && ballY <= paddle2Y + paddleWidth) {
                ballVX = -abs(ballVX) - 0.5f
                ballVY += (ballY - paddle2Y - paddleWidth / 2) * 0.05f
                ballX = paddle2X - ballSize
            } else if (ballX >= paddle2X + paddleHeight) {
                player1Score++
                if (player1Score >= winningScore) {
                    gameState = GameState.WON
                } else {
                    resetBall()
                }
            }
        }
        
        ballVX = ballVX.coerceIn(-8f, 8f)
        ballVY = ballVY.coerceIn(-6f, 6f)
        
        if (mode == "single") {
            val targetY = ballY - paddleWidth / 2
            val diff = targetY - paddle1Y
            paddle1Y += sign(diff) * minOf(abs(diff), 2f)
            paddle1Y = paddle1Y.coerceIn(0f, canvasHeight - paddleWidth)
        }
    }
    
    LaunchedEffect(gameState, actualPaused) {
        if (gameState == GameState.PLAYING) {
            while (gameState == GameState.PLAYING) {
                if (actualPaused) {
                    delay(100)
                    continue
                }
                delay(16)
                updateGame()
            }
        }
    }
    
    val winner = if (player1Score >= winningScore) "玩家" else "AI"
    val isDoubleMode = mode == "double"
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(GrayLight.copy(alpha = 0.1f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🏓 乒乓球",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Row(
            horizontalArrangement = Arrangement.spacedBy(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isDoubleMode) "玩家1" else "AI",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayMedium
                )
                Text(
                    text = "$player1Score",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (player1Score > player2Score) Green else Red
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isDoubleMode) "玩家2" else "玩家",
                    style = MaterialTheme.typography.bodyMedium,
                    color = GrayMedium
                )
                Text(
                    text = "$player2Score",
                    style = MaterialTheme.typography.titleLarge,
                    color = if (player2Score > player1Score) Green else Red
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Box(
            modifier = Modifier
                .size(width = 300.dp, height = 400.dp)
                .background(White, RoundedCornerShape(8.dp))
                .pointerInput(gameState) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        if (gameState != GameState.PLAYING) return@detectDragGestures
                        
                        val (_, dy) = dragAmount
                        if (isDoubleMode) {
                            if (change.position.x < size.width / 2) {
                                paddle1Y = (paddle1Y + dy).coerceIn(0f, canvasHeight - paddleWidth)
                            } else {
                                paddle2Y = (paddle2Y + dy).coerceIn(0f, canvasHeight - paddleWidth)
                            }
                        } else {
                            paddle2Y = (paddle2Y + dy).coerceIn(0f, canvasHeight - paddleWidth)
                        }
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = GrayMedium.copy(alpha = 0.1f),
                    topLeft = Offset(size.width / 2 - 1f, 0f),
                    size = Size(2f, size.height)
                )
                
                drawRect(
                    color = OrangePrimary,
                    topLeft = Offset(0f, paddle1Y),
                    size = Size(paddleHeight, paddleWidth)
                )
                
                drawRect(
                    color = OrangePrimary,
                    topLeft = Offset(canvasWidth - paddleHeight, paddle2Y),
                    size = Size(paddleHeight, paddleWidth)
                )
                
                drawCircle(
                    color = Red,
                    radius = ballSize / 2,
                    center = Offset(ballX, ballY)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (gameState == GameState.IDLE || gameState == GameState.WON || gameState == GameState.GAME_OVER) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                when (gameState) {
                    GameState.WON -> {
                        Text(
                            text = "🎉 $winner 获胜!",
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
                            text = "😵 $winner 失败!",
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
                                    onClick = { onResult(food.name, (maxOf(player1Score, player2Score) * 100 / winningScore).coerceIn(0, 100)) },
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
                text = if (isDoubleMode) "左侧控制玩家1，右侧控制玩家2" else "拖动控制右侧球拍",
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