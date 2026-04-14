package com.eatif.app.games.rps

import androidx.compose.foundation.layout.Arrangement
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
    onResult: (String) -> Unit
) {
    var gameState by remember { mutableStateOf(GameState.READY) }
    var playerChoice by remember { mutableStateOf<RPSChoice?>(null) }
    var aiChoice by remember { mutableStateOf<RPSChoice?>(null) }
    var playerScore by remember { mutableStateOf(0) }
    var aiScore by remember { mutableStateOf(0) }
    var resultText by remember { mutableStateOf("") }
    var isFinalResult by remember { mutableStateOf(false) }
    
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
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                                onClick = { onResult(food.name) },
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
                        modifier = Modifier
                            .size(width = 200.dp, height = 56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = White
                        )
                    ) {
                        Text(
                            text = "\u91CD\u65B0\u5F00\u59CB",
                            style = MaterialTheme.typography.titleMedium
                        )
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
                        modifier = Modifier
                            .size(width = 200.dp, height = 56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangePrimary,
                            contentColor = White
                        )
                    ) {
                        Text(
                            text = "\u518D\u6765\u4E00\u5C40",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (foods.isNotEmpty()) {
                                val randomFood = foods.random().name
                                onResult(randomFood)
                            }
                        },
                        modifier = Modifier
                            .size(width = 200.dp, height = 56.dp),
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = OrangeLight,
                            contentColor = White
                        )
                    ) {
                        Text(
                            text = "\u7ED3\u675F\u5E76\u9009\u62E9\u7F8E\u98DF",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }
    }

    if (playerChoice != null && gameState == GameState.PLAYER_CHOSEN) {
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            val ai = RPSChoice.entries.random()
            aiChoice = ai
            gameState = GameState.AI_CHOSEN

            android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
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
            }, 500)
        }, 1000)
    }
}
