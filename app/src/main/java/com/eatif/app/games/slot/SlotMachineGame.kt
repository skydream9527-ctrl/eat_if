package com.eatif.app.games.slot

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White
import kotlinx.coroutines.delay

@Composable
fun SlotMachineGame(
    foods: List<Food>,
    isPaused: Boolean = false,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    var isSpinning by remember { mutableStateOf(false) }
    var reel1Display by remember { mutableStateOf("") }
    var reel2Display by remember { mutableStateOf("") }
    var reel3Display by remember { mutableStateOf("") }
    var reel1Settled by remember { mutableStateOf(false) }
    var reel2Settled by remember { mutableStateOf(false) }
    var reel3Settled by remember { mutableStateOf(false) }
    var resultMessage by remember { mutableStateOf("🎰 拉杆子开始!") }
    var isFailure by remember { mutableStateOf(false) }
    var isWin by remember { mutableStateOf(false) }
    var winFood by remember { mutableStateOf<String?>(null) }
    var winScore by remember { mutableStateOf(0) }
    var internalPaused by remember { mutableStateOf(false) }
    val actualPaused = isPaused || internalPaused

    val reelSymbols = remember {
        if (foods.isNotEmpty()) foods.map { it.name } else listOf("🍕", "🍔", "🍣", "🍜", "🍰", "🍪")
    }

    val emojiSymbols = remember {
        listOf("🍕", "🍔", "🍣", "🍜", "🍰", "🍪", "🍩", "🧁", "🌮", "🍱")
    }

    LaunchedEffect(isSpinning) {
        if (isSpinning && foods.isNotEmpty()) {
            resultMessage = "🎰 转动中..."
            isWin = false
            isFailure = false
            winFood = null
            reel1Settled = false
            reel2Settled = false
            reel3Settled = false

            val finalIndex1 = (0 until reelSymbols.size).random()
            val finalIndex2 = (0 until reelSymbols.size).random()
            val finalIndex3 = (0 until reelSymbols.size).random()

            val totalDuration1 = 1500L
            val totalDuration2 = 2500L
            val totalDuration3 = 3500L
            val interval1 = 50L
            val interval2 = 80L
            val interval3 = 100L

            var elapsed1 = 0L
            while (elapsed1 < totalDuration1) {
                reel1Display = emojiSymbols.random()
                delay(interval1)
                elapsed1 += interval1
            }
            reel1Display = reelSymbols[finalIndex1]
            reel1Settled = true

            var elapsed2 = 0L
            while (elapsed2 < totalDuration2) {
                reel2Display = emojiSymbols.random()
                delay(interval2)
                elapsed2 += interval2
            }
            reel2Display = reelSymbols[finalIndex2]
            reel2Settled = true

            var elapsed3 = 0L
            while (elapsed3 < totalDuration3) {
                reel3Display = emojiSymbols.random()
                delay(interval3)
                elapsed3 += interval3
            }
            reel3Display = reelSymbols[finalIndex3]
            reel3Settled = true

            delay(300)

            val food1 = reelSymbols[finalIndex1]
            val food2 = reelSymbols[finalIndex2]
            val food3 = reelSymbols[finalIndex3]

            when {
                food1 == food2 && food2 == food3 -> {
                    resultMessage = "🎉 超级大奖! $food1"
                    isFailure = false
                    isWin = true
                    winFood = food1
                    winScore = 100
                }
                food1 == food2 || food1 == food3 -> {
                    resultMessage = "✨ 赢了! $food1"
                    isFailure = false
                    isWin = true
                    winFood = food1
                    winScore = 70
                }
                food2 == food3 -> {
                    resultMessage = "✨ 赢了! $food2"
                    isFailure = false
                    isWin = true
                    winFood = food2
                    winScore = 70
                }
                else -> {
                    resultMessage = "😢 没有匹配..."
                    isFailure = true
                    isWin = false
                    winFood = null
                    winScore = 20
                }
            }

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
                symbol = reel1Display,
                isSpinning = !reel1Settled,
                modifier = Modifier.size(80.dp)
            )

            ReelBox(
                symbol = reel2Display,
                isSpinning = !reel2Settled,
                modifier = Modifier.size(80.dp)
            )

            ReelBox(
                symbol = reel3Display,
                isSpinning = !reel3Settled,
                modifier = Modifier.size(80.dp)
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            IconButton(
                onClick = { internalPaused = !internalPaused },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (actualPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = if (actualPaused) "继续" else "暂停",
                    tint = OrangePrimary,
                    modifier = Modifier.size(32.dp)
                )
            }

            Button(
                onClick = {
                    if (!isSpinning && foods.isNotEmpty()) {
                        reel1Settled = false
                        reel2Settled = false
                        reel3Settled = false
                        isWin = false
                        isFailure = false
                        winFood = null
                        isSpinning = true
                    }
                },
                enabled = !isSpinning && foods.isNotEmpty(),
                modifier = Modifier
                    .size(width = 160.dp, height = 56.dp),
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

        if (isWin && winFood != null && foods.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "选择一顿美食奖励自己吧:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            foods.take(3).forEach { food ->
                Button(
                    onClick = { onResult(food.name, winScore) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        contentColor = White
                    )
                ) {
                    Text(text = food.name, style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        if (isFailure && foods.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "选择一顿美食安慰自己吧:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            foods.take(3).forEach { food ->
                Button(
                    onClick = { onResult(food.name, 20) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 4.dp),
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
            fontSize = if (symbol.length > 2) 18.sp else 40.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxSize()
        )
    }
}
