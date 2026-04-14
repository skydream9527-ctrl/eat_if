package com.eatif.app.games.jump

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White
import kotlin.math.abs
import kotlin.random.Random

data class Block(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float
)

@Composable
fun JumpGame(
    foods: List<Food>,
    onResult: (String) -> Unit
) {
    var score by remember { mutableIntStateOf(0) }
    var isCharging by remember { mutableStateOf(false) }
    var jumpPower by remember { mutableFloatStateOf(0f) }
    var gameState by remember { mutableStateOf("ready") }
    var currentBlockIndex by remember { mutableIntStateOf(0) }
    var characterY by remember { mutableFloatStateOf(0f) }
    var characterX by remember { mutableFloatStateOf(0f) }
    var isJumping by remember { mutableStateOf(false) }
    var isFalling by remember { mutableStateOf(false) }
    var fallingSpeed by remember { mutableFloatStateOf(0f) }

    val characterSize = 40f
    val groundY = 400f

    val blocks = remember {
        mutableListOf<Block>().apply {
            add(Block(50f, groundY, 120f, 40f))
            var lastX = 170f
            var lastY = groundY
            repeat(5) {
                val jumpDistance = Random.nextFloat() * 150 + 100
                val heightChange = Random.nextFloat() * 100 - 50
                val newY = (lastY + heightChange).coerceIn(groundY - 150, groundY + 50)
                val newX = lastX + jumpDistance
                val width = Random.nextFloat() * 40 + 80
                add(Block(newX, newY, width, 40f))
                lastX = newX + width
                lastY = newY
            }
        }
    }

    val animatableCharY = remember { Animatable(groundY - characterSize) }

    LaunchedEffect(gameState) {
        if (gameState == "jumping") {
            val targetBlock = blocks.getOrNull(currentBlockIndex + 1)
            if (targetBlock != null) {
                val jumpHeight = 200f * jumpPower
                val jumpDuration = (500 + (jumpPower * 500)).toLong()

                animatableCharY.animateTo(
                    targetValue = targetBlock.y - characterSize - jumpHeight,
                    animationSpec = tween(durationMillis = (jumpDuration / 2).toInt())
                )
                animatableCharY.animateTo(
                    targetValue = targetBlock.y - characterSize,
                    animationSpec = tween(durationMillis = (jumpDuration / 2).toInt())
                )

                characterX = targetBlock.x + targetBlock.width / 2 - characterSize / 2
                characterY = targetBlock.y - characterSize
                currentBlockIndex++
                score++
                isJumping = false
                gameState = "ready"

                if (score >= 3) {
                    val randomFood = foods.randomOrNull()
                    if (randomFood != null) {
                        onResult(randomFood.name)
                    }
                }
            } else {
                gameState = "falling"
                isFalling = true
                fallingSpeed = 0f
            }
        }
    }

    LaunchedEffect(gameState) {
        if (gameState == "falling") {
            while (characterY < 600f) {
                fallingSpeed += 15f
                characterY = characterY + fallingSpeed
                kotlinx.coroutines.delay(16)
            }
            gameState = "gameover"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🏃 跳一跳",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "分数: $score",
            style = MaterialTheme.typography.titleLarge,
            color = OrangePrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isCharging) {
            LinearProgressIndicator(
                progress = jumpPower,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = Green,
                trackColor = White,
            )
            Text(
                text = "按住蓄力，松开跳跃！",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .background(GrayLight, RoundedCornerShape(16.dp))
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            if (gameState == "ready" && !isJumping) {
                                isCharging = true
                                jumpPower = 0f
                                while (true) {
                                    jumpPower = (jumpPower + 0.02f).coerceAtMost(1f)
                                    if (jumpPower >= 1f) {
                                        jumpPower = 0f
                                    }
                                    kotlinx.coroutines.delay(30)
                                    try {
                                        awaitRelease()
                                        break
                                    } catch (_: Exception) {
                                        return@detectTapGestures
                                    }
                                }
                                isCharging = false
                                if (gameState == "ready") {
                                    isJumping = true
                                    gameState = "jumping"
                                }
                            }
                        }
                    )
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                blocks.forEachIndexed { index, block ->
                    val blockColor = if (index == currentBlockIndex) OrangePrimary else Green
                    drawRoundRect(
                        color = blockColor,
                        topLeft = Offset(block.x, block.y),
                        size = Size(block.width, block.height),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }

                if (gameState != "gameover") {
                    drawRoundRect(
                        color = Red,
                        topLeft = Offset(characterX, animatableCharY.value),
                        size = Size(characterSize, characterSize),
                        cornerRadius = CornerRadius(8f, 8f)
                    )
                }
            }

            if (gameState == "gameover") {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "游戏结束",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Red
                        )
                        Text(
                            text = "得分: $score",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        if (foods.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "选择一顿美食安慰自己吧:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            foods.take(3).forEach { food ->
                                Button(
                                    onClick = { onResult(food.name) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    shape = RoundedCornerShape(16.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = OrangePrimary,
                                        contentColor = White
                                    )
                                ) {
                                    Text(text = food.name, style = MaterialTheme.typography.titleMedium)
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

            Button(
            onClick = {
                score = 0
                currentBlockIndex = 0
                characterX = blocks[0].x + blocks[0].width / 2 - characterSize / 2
                characterY = blocks[0].y - characterSize
                gameState = "ready"
                isJumping = false
                isFalling = false
                fallingSpeed = 0f
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                contentColor = White
            ),
            enabled = gameState == "gameover" || gameState == "ready"
        ) {
            Text(
                text = if (gameState == "gameover") "重新开始" else "跳跃",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "长按按钮蓄力，松开跳跃",
            style = MaterialTheme.typography.bodySmall,
            color = GrayMedium
        )
    }
}

private val GrayLight = Color(0xFFF5F5F7)
private val GrayMedium = Color(0xFF86868B)
