package com.eatif.app.games.spinwheel

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.Food
import com.eatif.app.ui.theme.GrayMedium
import com.eatif.app.ui.theme.OrangeDark
import com.eatif.app.ui.theme.OrangeLight
import com.eatif.app.ui.theme.OrangePrimary
import com.eatif.app.ui.theme.Red
import com.eatif.app.ui.theme.White
import kotlinx.coroutines.delay
import kotlin.random.Random
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun SpinWheelGame(
    foods: List<Food>,
    isPaused: Boolean = false,
    onResult: (String, Int) -> Unit,
    mode: String = "single"
) {
    val isSpinning = remember { mutableStateOf(false) }
    val rotation = remember { mutableStateOf(0f) }
    val animatableRotation = remember { Animatable(0f) }
    var hasSpun by remember { mutableStateOf(false) }
    var winningIndex by remember { mutableStateOf(-1) }
    var internalPaused by remember { mutableStateOf(false) }
    val actualPaused = isPaused || internalPaused

    val segmentColors = listOf(
        OrangePrimary,
        OrangeLight,
        OrangeDark,
        OrangePrimary.copy(alpha = 0.8f),
        OrangeLight.copy(alpha = 0.9f),
        OrangeDark.copy(alpha = 0.7f),
        OrangePrimary.copy(alpha = 0.9f),
        OrangeLight.copy(alpha = 0.7f)
    )

    LaunchedEffect(isSpinning.value) {
        if (isSpinning.value && foods.isNotEmpty()) {
            winningIndex = -1
            val segmentCount = foods.size
            val segmentAngle = 360f / segmentCount
            val randomOffset = (0 until segmentCount).random() * segmentAngle + Random.nextFloat() * segmentAngle
            val spins = 5 * 360f
            val targetRotation = spins + randomOffset

            animatableRotation.animateTo(
                targetValue = rotation.value + targetRotation,
                animationSpec = tween(durationMillis = 4000)
            )
            rotation.value = animatableRotation.value
            isSpinning.value = false
            hasSpun = true

            val normalizedAngle = ((360f - (rotation.value % 360f)) % 360f)
            winningIndex = (normalizedAngle / segmentAngle).toInt() % segmentCount
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
            text = if (isSpinning.value) "🎡 旋转中..." else "🎯 点击按钮开始",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            contentAlignment = Alignment.TopCenter
        ) {
            Canvas(
                modifier = Modifier.size(300.dp)
            ) {
                val centerX = size.width / 2
                val centerY = size.height / 2
                val radius = min(size.width, size.height) / 2 - 10

                rotate(rotation.value, Offset(centerX, centerY)) {
                    val sweepAngle = 360f / foods.size
                    foods.forEachIndexed { index, _ ->
                        val startAngle = index * sweepAngle - 90f
                        val color = segmentColors[index % segmentColors.size]
                        val isWinning = index == winningIndex && hasSpun && !isSpinning.value

                        drawArc(
                            color = if (isWinning) Color.White.copy(alpha = 0.4f) else color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = true,
                            topLeft = Offset(centerX - radius, centerY - radius),
                            size = Size(radius * 2, radius * 2)
                        )

                        if (isWinning) {
                            drawArc(
                                color = Color.White,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = true,
                                topLeft = Offset(centerX - radius, centerY - radius),
                                size = Size(radius * 2, radius * 2),
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 6f)
                            )
                        }

                        val middleAngle = Math.toRadians((startAngle + sweepAngle / 2).toDouble())
                        val textRadius = radius * 0.65f
                        val textX = centerX + textRadius * cos(middleAngle).toFloat()
                        val textY = centerY + textRadius * sin(middleAngle).toFloat()

                        drawContext.canvas.nativeCanvas.apply {
                            save()
                            rotate(
                                (startAngle + sweepAngle / 2 + 180).toFloat(),
                                textX,
                                textY
                            )
                            val paint = android.graphics.Paint().apply {
                                this.color = android.graphics.Color.WHITE
                                textSize = radius * 0.12f
                                textAlign = android.graphics.Paint.Align.CENTER
                                isAntiAlias = true
                            }
                            val foodName = foods[index].name
                            val displayName = if (foodName.length > 6) foodName.take(6) else foodName
                            drawText(displayName, textX, textY + paint.textSize / 3, paint)
                            restore()
                        }
                    }
                }
            }

            Canvas(modifier = Modifier.size(300.dp)) {
                val centerX = size.width / 2
                val trianglePath = Path().apply {
                    moveTo(centerX, 0f)
                    lineTo(centerX - 20f, 35f)
                    lineTo(centerX + 20f, 35f)
                    close()
                }
                drawPath(trianglePath, Red)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        if (hasSpun && foods.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            val winningFood = if (winningIndex >= 0 && winningIndex < foods.size) foods[winningIndex] else null
            if (winningFood != null) {
                Text(
                    text = "🎉 ${winningFood.name}",
                    style = MaterialTheme.typography.headlineMedium,
                    color = OrangePrimary
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "选择美食吧:",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            foods.take(3).forEach { food ->
                Button(
                    onClick = { onResult(food.name, 50) },
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
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    hasSpun = false
                    winningIndex = -1
                    isSpinning.value = true
                },
                modifier = Modifier
                    .size(width = 200.dp, height = 56.dp),
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = GrayMedium,
                    contentColor = White
                )
            ) {
                Text(
                    text = "再转一次",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        } else {
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
                        if (!isSpinning.value && foods.isNotEmpty()) {
                            isSpinning.value = true
                        }
                    },
                    enabled = !isSpinning.value && foods.isNotEmpty(),
                    modifier = Modifier
                        .size(width = 160.dp, height = 56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = White,
                        disabledContainerColor = OrangeLight.copy(alpha = 0.5f),
                        disabledContentColor = White.copy(alpha = 0.5f)
                    )
                ) {
                    Text(
                        text = if (isSpinning.value) "旋转中..." else "开始转动",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}
