package com.eatif.app.games.slot

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.Gold
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun SlotMachineGame(
    foods: List<Food>,
    onResult: (String) -> Unit
) {
    var isSpinning by remember { mutableStateOf(false) }
    var reel1Index by remember { mutableStateOf(0) }
    var reel2Index by remember { mutableStateOf(1) }
    var reel3Index by remember { mutableStateOf(2) }
    var animationProgress1 by remember { mutableStateOf(0f) }
    var animationProgress2 by remember { mutableStateOf(0f) }
    var animationProgress3 by remember { mutableStateOf(0f) }
    var resultMessage by remember { mutableStateOf("🎰 拉杆子开始!") }

    val animatable1 = remember { Animatable(0f) }
    val animatable2 = remember { Animatable(0f) }
    val animatable3 = remember { Animatable(0f) }

    val reelSymbols = remember {
        if (foods.isNotEmpty()) foods.map { it.name } else listOf("🍕", "🍔", "🍣", "🍜", "🍰", "🍪")
    }

    val displaySymbols = remember(reelSymbols) {
        reelSymbols + reelSymbols + reelSymbols
    }

    LaunchedEffect(isSpinning) {
        if (isSpinning && foods.isNotEmpty()) {
            resultMessage = "🎰 转动中..."

            val stopDelay1 = 1500L
            val stopDelay2 = 2500L
            val stopDelay3 = 3500L

            val finalIndex1 = (0 until reelSymbols.size).random()
            val finalIndex2 = (0 until reelSymbols.size).random()
            val finalIndex3 = (0 until reelSymbols.size).random()

            animatable1.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = stopDelay1.toInt(), easing = FastOutSlowInEasing)
            )
            animationProgress1 = 1f
            reel1Index = finalIndex1

            animatable2.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = (stopDelay2 - stopDelay1).toInt(), easing = FastOutSlowInEasing)
            )
            animationProgress2 = 1f
            reel2Index = finalIndex2

            animatable3.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = (stopDelay3 - stopDelay2).toInt(), easing = FastOutSlowInEasing)
            )
            animationProgress3 = 1f
            reel3Index = finalIndex3

            delay(300)

            val food1 = reelSymbols[finalIndex1]
            val food2 = reelSymbols[finalIndex2]
            val food3 = reelSymbols[finalIndex3]

            val result = when {
                food1 == food2 && food2 == food3 -> {
                    resultMessage = "🎉 超级大奖! $food1"
                    food1
                }
                food1 == food2 || food1 == food3 -> {
                    resultMessage = "✨ 赢了! $food1"
                    food1
                }
                food2 == food3 -> {
                    resultMessage = "✨ 赢了! $food2"
                    food2
                }
                else -> {
                    val randomFood = reelSymbols.random()
                    resultMessage = "🍽️ 选择: $randomFood"
                    randomFood
                }
            }

            delay(500)
            onResult(result)
            isSpinning = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "🎰 老虎机",
            style = MaterialTheme.typography.headlineLarge,
            color = Gold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = resultMessage,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(OrangePrimary.copy(alpha = 0.2f))
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ReelBox(
                symbol = displaySymbols[(reel1Index + 3) % displaySymbols.size],
                isSpinning = animationProgress1 < 1f,
                modifier = Modifier.size(80.dp)
            )

            ReelBox(
                symbol = displaySymbols[(reel2Index + 3) % displaySymbols.size],
                isSpinning = animationProgress2 < 1f,
                modifier = Modifier.size(80.dp)
            )

            ReelBox(
                symbol = displaySymbols[(reel3Index + 3) % displaySymbols.size],
                isSpinning = animationProgress3 < 1f,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = {
                if (!isSpinning && foods.isNotEmpty()) {
                    animationProgress1 = 0f
                    animationProgress2 = 0f
                    animationProgress3 = 0f
                    isSpinning = true
                }
            },
            enabled = !isSpinning && foods.isNotEmpty(),
            modifier = Modifier
                .size(width = 200.dp, height = 56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = OrangePrimary,
                contentColor = White,
                disabledContainerColor = OrangePrimary.copy(alpha = 0.5f),
                disabledContentColor = White.copy(alpha = 0.5f)
            )
        ) {
            Text(
                text = if (isSpinning) "转动中..." else "拉动杆子",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Composable
private fun ReelBox(
    symbol: String,
    isSpinning: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            fontSize = 40.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxSize()
        )
    }
}
