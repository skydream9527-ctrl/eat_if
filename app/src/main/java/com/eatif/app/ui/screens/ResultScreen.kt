package com.eatif.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.eatif.app.data.session.SessionManager
import com.eatif.app.ui.theme.OrangePrimary
import kotlinx.coroutines.delay

@Composable
fun ResultScreen(
    foodName: String,
    scorePercent: Int = -1,
    onPlayAgain: () -> Unit
) {
    var showContent by remember { mutableStateOf(false) }
    var showRecommendation by remember { mutableStateOf(false) }

    // 尝试从 SessionManager 获取推荐（需要用户已配置店铺 + 有合法得分）
    val recommendation = remember {
        if (SessionManager.isConfigured && scorePercent >= 0) {
            SessionManager.getRecommendation(scorePercent / 100f)
        } else if (SessionManager.isConfigured) {
            // 运气类游戏（scorePercent = -1）：随机选一家
            SessionManager.getRecommendation(0.5f)?.copy(
                reason = "随机为你挑选",
                emoji = "🎲"
            )
        } else {
            null
        }
    }

    LaunchedEffect(Unit) {
        delay(100)
        showContent = true
        delay(400)
        showRecommendation = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        AnimatedVisibility(
            visible = showContent,
            enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn()
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // ── 顶部 emoji ──
                Text(
                    text = if (recommendation != null) recommendation.emoji else "🎉",
                    style = MaterialTheme.typography.displayLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (recommendation != null) {
                    // ══ 有推荐：展示店铺推荐卡片 ══

                    // 得分进度条（只在有实际得分时显示）
                    if (scorePercent >= 0) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "本局得分",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            LinearProgressIndicator(
                                progress = { scorePercent / 100f },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = OrangePrimary,
                                trackColor = OrangePrimary.copy(alpha = 0.15f)
                            )
                            Text(
                                text = "$scorePercent%",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = OrangePrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(
                        text = recommendation.reason,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    AnimatedVisibility(
                        visible = showRecommendation,
                        enter = slideInVertically(
                            spring(Spring.DampingRatioLowBouncy)
                        ) { it / 2 } + fadeIn()
                    ) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = OrangePrimary
                            )
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 28.dp, horizontal = 20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = recommendation.shopName,
                                    style = MaterialTheme.typography.displaySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // 候选选项提示：其他店铺
                    val otherShops = SessionManager.shopOptions.filter {
                        it != recommendation.shopName
                    }.take(3)
                    if (otherShops.isNotEmpty()) {
                        Text(
                            text = "其他备选",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            otherShops.forEach { shop ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = shop,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                } else {
                    // ══ 无推荐：旧逻辑，只显示菜名 ══
                    Text(
                        text = "今天的晚餐是",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = foodName,
                            style = MaterialTheme.typography.headlineLarge,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 32.dp, horizontal = 16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = onPlayAgain,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(28.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = OrangePrimary,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "再来一次",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

