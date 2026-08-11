package com.eatif.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.eatif.app.domain.model.DailyTask
import com.eatif.app.ui.theme.Green
import com.eatif.app.ui.theme.OrangePrimary

/**
 * 每日任务卡片 - 展示单个任务的进度和领取按钮
 */
@Composable
fun DailyTaskCard(
    task: DailyTask,
    onClaimReward: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (task.isCompleted)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "+${task.xpReward} XP",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (task.isRewardClaimed)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        OrangePrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LinearProgressIndicator(
                    progress = { task.progressPercent },
                    modifier = Modifier.weight(1f),
                    color = if (task.isCompleted) Green else OrangePrimary,
                    trackColor = MaterialTheme.colorScheme.surface
                )
                Text(
                    text = "  ${task.currentProgress}/${task.targetProgress}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AnimatedVisibility(visible = task.canClaimReward) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onClaimReward(task.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green,
                        contentColor = Color.White
                    )
                ) {
                    Text("领取奖励", style = MaterialTheme.typography.labelMedium)
                }
            }
            if (task.isRewardClaimed) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "✓ 已领取",
                    style = MaterialTheme.typography.labelSmall,
                    color = Green,
                    modifier = Modifier.align(Alignment.End)
                )
            }
        }
    }
}
