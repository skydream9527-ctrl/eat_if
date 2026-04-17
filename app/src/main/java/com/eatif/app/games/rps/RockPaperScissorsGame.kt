package com.eatif.app.games.rps

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
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.OrangeLight
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White

enum class RPSChoice(val emoji: String) {
    ROCK("\uD83E\uDEA8"),
    PAPER("\uD83D\uDCC4"),
    SCISSORS("\u2702\uFE0F")
}

enum class GameState {
    READY, PLAYER_CHOSEN, AI_CHOSEN, RESULT
}

enum class DoubleState {
    P1_TURN, P2_TURN, REVEAL, RESULT
}

fun determineWinner(player: RPSChoice, ai: RPSChoice): Int {
    return when {
        player == ai -> 0
        (player == RPSChoice.ROCK && ai == RPSChoice.SCISSORS) ||
        (player == RPSChoice.PAPER && ai == RPSChoice.ROCK) ||
        (player == RPSChoice.SCISSORS && ai == RPSChoice.PAPER) -> 1
        else -> -1
    }
}

@Composable
fun RockPaperScissorsGame(
    foods: List<Food>,
    isPaused: Boolean = false,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    if (mode == "double") {
        DoubleRPSMode(foods, isPaused, onResult)
    } else {
        SingleRPSMode(foods, isPaused, onResult)
    }
}

@Composable
private fun SingleRPSMode(
    foods: List<Food>,
    isPaused: Boolean,
    onResult: (String, Int) -> Unit
) {
    var gameState by remember { mutableStateOf(GameState.READY) }
    var playerChoice by remember { mutableStateOf<RPSChoice?>(null) }
    var aiChoice by remember { mutableStateOf<RPSChoice?>(null) }
    var playerScore by remember { mutableStateOf(0) }
    var aiScore by remember { mutableStateOf(0) }
    var resultText by remember { mutableStateOf("") }
    var isFinalResult by remember { mutableStateOf(false) }
    var internalPaused by remember { mutableStateOf(false) }
    val actualPaused = isPaused || internalPaused

    val winTarget = 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\u270A \u77F3\u5934\u526A\u5200\u5E03",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "\u4F60: $playerScore  vs  AI: $aiScore",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\uD83D\uDC64 \u4F60",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = playerChoice?.emoji ?: "?",
                    style = MaterialTheme.typography.displayLarge
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\uD83E\uDD16 AI",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = aiChoice?.emoji ?: "?",
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        when (gameState) {
            GameState.READY -> {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    RPSChoice.entries.forEach { choice ->
                        Button(
                            onClick = {
                                playerChoice = choice
                                gameState = GameState.PLAYER_CHOSEN
                            },
                            modifier = Modifier.size(80.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                contentColor = White
                            )
                        ) {
                            Text(
                                text = choice.emoji,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                }
            }

            GameState.PLAYER_CHOSEN -> {
                Text(
                    text = "\u8BF7\u7B49\u5F85 AI\u9009\u62E9...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            GameState.AI_CHOSEN -> {
                Text(
                    text = "\u5224\u51B3\u4E2D...",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            GameState.RESULT -> {
                if (isFinalResult) {
                    val playerWon = playerScore >= winTarget
                    Text(
                        text = if (playerWon) "\uD83C\uDF89 \u4F60\u8D62\u4E86\u5168\u573A!" else "\uD83D\uDE1E \u4F60\u8F93\u4E86\u5168\u573A!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = if (playerWon) Green else Red
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (foods.isNotEmpty()) {
                        Text(
                            text = if (playerWon) "\u9009\u62E9\u7F8E\u98DF\u5E86\u795D\u5427:" else "\u9009\u62E9\u4E00\u987F\u7F8E\u98DF\u5B89\u6177\u81EA\u5DF1\u5427:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        foods.take(3).forEach { food ->
                            Button(
                                onClick = { onResult(food.name, if (playerWon) 80 else 20) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (playerWon) Green else OrangePrimary,
                                    contentColor = White
                                )
                            ) {
                                Text(text = food.name, style = MaterialTheme.typography.titleMedium)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            playerScore = 0
                            aiScore = 0
                            playerChoice = null
                            aiChoice = null
                            resultText = ""
                            isFinalResult = false
                            gameState = GameState.READY
                        },
                        modifier = Modifier.size(width = 200.dp, height = 56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = White
                        )
                    ) {
                        Text(text = "\u91CD\u65B0\u5F00\u59CB", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    Text(
                        text = resultText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            playerChoice = null
                            aiChoice = null
                            resultText = ""
                            gameState = GameState.READY
                        },
                        modifier = Modifier.size(width = 200.dp, height = 56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = White
                        )
                    ) {
                        Text(text = "\u518D\u6765\u4E00\u5C40", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (foods.isNotEmpty()) {
                                onResult(foods.random().name, 50)
                            }
                        },
                        modifier = Modifier.size(width = 200.dp, height = 56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeLight,
                            contentColor = White
                        )
                    ) {
                        Text(text = "\u7ED3\u675F\u5E76\u9009\u62E9\u7F8E\u98DF", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(
                onClick = { internalPaused = !internalPaused },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (actualPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = if (actualPaused) "\u7EE7\u7EED" else "\u6682\u505C",
                    tint = OrangePrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }

    if (playerChoice != null && gameState == GameState.PLAYER_CHOSEN) {
        LaunchedEffect(playerChoice) {
            kotlinx.coroutines.delay(1000)
            val ai = RPSChoice.entries.random()
            aiChoice = ai
            gameState = GameState.AI_CHOSEN

            kotlinx.coroutines.delay(500)
            val winner = determineWinner(playerChoice!!, ai)
            resultText = when (winner) {
                1 -> "\uD83C\uDF89 \u4F60\u8D62\u4E86!"
                -1 -> "\uD83D\uDE22 \u4F60\u8F93\u4E86!"
                else -> "\uD83D\uDE4B \u5E73\u5C40!"
            }
            if (winner == 1) playerScore++ else if (winner == -1) aiScore++
            if (playerScore >= winTarget || aiScore >= winTarget) {
                isFinalResult = true
            }
            gameState = GameState.RESULT
        }
    }
}

@Composable
private fun DoubleRPSMode(
    foods: List<Food>,
    isPaused: Boolean,
    onResult: (String, Int) -> Unit
) {
    var doubleState by remember { mutableStateOf(DoubleState.P1_TURN) }
    var p1Choice by remember { mutableStateOf<RPSChoice?>(null) }
    var p2Choice by remember { mutableStateOf<RPSChoice?>(null) }
    var p1Score by remember { mutableStateOf(0) }
    var p2Score by remember { mutableStateOf(0) }
    var p1Revealed by remember { mutableStateOf(false) }
    var p2Revealed by remember { mutableStateOf(false) }
    var roundResultText by remember { mutableStateOf("") }
    var isFinalResult by remember { mutableStateOf(false) }
    var internalPaused by remember { mutableStateOf(false) }
    val actualPaused = isPaused || internalPaused

    val winTarget = 3

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "\u270A\u270B\u270C \u53CC\u4EBA\u77F3\u5934\u526A\u5200\u5E03",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "\u73A9\u5BB61: $p1Score  vs  \u73A9\u5BB62: $p2Score",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\uD83D\uDC64 \u73A9\u5BB61",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (p1Revealed) MaterialTheme.colorScheme.surfaceVariant
                            else OrangePrimary.copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (p1Revealed) (p1Choice?.emoji ?: "?") else "\u2753",
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "\uD83D\uDC64 \u73A9\u5BB62",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            if (p2Revealed) MaterialTheme.colorScheme.surfaceVariant
                            else OrangePrimary.copy(alpha = 0.3f),
                            RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (p2Revealed) (p2Choice?.emoji ?: "?") else "\u2753",
                        style = MaterialTheme.typography.displayLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        when (doubleState) {
            DoubleState.P1_TURN -> {
                Text(
                    text = "\uD83D\uDC64 \u73A9\u5BB61 \u8BF7\u51FA\u62F3",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrangePrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    RPSChoice.entries.forEach { choice ->
                        Button(
                            onClick = {
                                p1Choice = choice
                                p1Revealed = false
                                doubleState = DoubleState.P2_TURN
                            },
                            modifier = Modifier.size(72.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                contentColor = White
                            )
                        ) {
                            Text(text = choice.emoji, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }

            DoubleState.P2_TURN -> {
                Text(
                    text = "\uD83D\uDC64 \u73A9\u5BB62 \u8BF7\u51FA\u62F3",
                    style = MaterialTheme.typography.titleMedium,
                    color = OrangePrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    RPSChoice.entries.forEach { choice ->
                        Button(
                            onClick = {
                                p2Choice = choice
                                p2Revealed = false
                                doubleState = DoubleState.REVEAL
                            },
                            modifier = Modifier.size(72.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = OrangePrimary,
                                contentColor = White
                            )
                        ) {
                            Text(text = choice.emoji, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }

            DoubleState.REVEAL -> {
                LaunchedEffect(doubleState) {
                    kotlinx.coroutines.delay(500)
                    p1Revealed = true
                    kotlinx.coroutines.delay(500)
                    p2Revealed = true
                    kotlinx.coroutines.delay(300)

                    val winner = if (p1Choice != null && p2Choice != null) {
                        determineWinner(p1Choice!!, p2Choice!!)
                    } else 0

                    roundResultText = when (winner) {
                        1 -> "\uD83C\uDF89 \u73A9\u5BB61\u8D62\u4E86!"
                        -1 -> "\uD83C\uDF89 \u73A9\u5BB62\u8D62\u4E86!"
                        else -> "\uD83D\uDE4B \u5E73\u5C40!"
                    }
                    if (winner == 1) p1Score++ else if (winner == -1) p2Score++
                    if (p1Score >= winTarget || p2Score >= winTarget) {
                        isFinalResult = true
                    }
                    doubleState = DoubleState.RESULT
                }
                Text(
                    text = "\u63ED\u6653\u4E2D...",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            DoubleState.RESULT -> {
                if (isFinalResult) {
                    val p1Won = p1Score >= winTarget
                    Text(
                        text = if (p1Won) "\uD83C\uDF89 \u73A9\u5BB61\u8D62\u4E86\u5168\u573A!" else "\uD83C\uDF89 \u73A9\u5BB62\u8D62\u4E86\u5168\u573A!",
                        style = MaterialTheme.typography.headlineMedium,
                        color = Green
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    if (foods.isNotEmpty()) {
                        Text(
                            text = "\u8D62\u5BB6\u9009\u62E9\u7F8E\u98DF\u5427:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        foods.take(3).forEach { food ->
                            Button(
                                onClick = { onResult(food.name, if (p1Won) 80 else 20) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
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
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            p1Score = 0
                            p2Score = 0
                            p1Choice = null
                            p2Choice = null
                            p1Revealed = false
                            p2Revealed = false
                            roundResultText = ""
                            isFinalResult = false
                            doubleState = DoubleState.P1_TURN
                        },
                        modifier = Modifier.size(width = 200.dp, height = 56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = White
                        )
                    ) {
                        Text(text = "\u91CD\u65B0\u5F00\u59CB", style = MaterialTheme.typography.titleMedium)
                    }
                } else {
                    Text(
                        text = roundResultText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = {
                            p1Choice = null
                            p2Choice = null
                            p1Revealed = false
                            p2Revealed = false
                            roundResultText = ""
                            doubleState = DoubleState.P1_TURN
                        },
                        modifier = Modifier.size(width = 200.dp, height = 56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = White
                        )
                    ) {
                        Text(text = "\u518D\u6765\u4E00\u5C40", style = MaterialTheme.typography.titleMedium)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (foods.isNotEmpty()) {
                                onResult(foods.random().name, 50)
                            }
                        },
                        modifier = Modifier.size(width = 200.dp, height = 56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeLight,
                            contentColor = White
                        )
                    ) {
                        Text(text = "\u7ED3\u675F\u5E76\u9009\u62E9\u7F8E\u98DF", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            IconButton(
                onClick = { internalPaused = !internalPaused },
                modifier = Modifier.size(56.dp)
            ) {
                Icon(
                    imageVector = if (actualPaused) Icons.Filled.PlayArrow else Icons.Filled.Pause,
                    contentDescription = if (actualPaused) "\u7EE7\u7EED" else "\u6682\u505C",
                    tint = OrangePrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
