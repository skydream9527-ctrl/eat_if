package com.eatif.app.domain.usecase

import com.eatif.app.data.local.DailyTaskDao
import com.eatif.app.data.local.toDomain
import com.eatif.app.data.local.toEntity
import com.eatif.app.domain.model.DailyTask
import com.eatif.app.domain.model.DailyTaskType
import com.eatif.app.domain.model.PlayerProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * 每日任务 UseCase
 *
 * - 每天首次访问时自动生成 3 个随机任务
 * - 游戏结束时更新任务进度
 * - 全部完成后可领取 XP 奖励
 */
@Singleton
class DailyTaskUseCase @Inject constructor(
    private val dailyTaskDao: DailyTaskDao,
    private val playerProfileUseCase: PlayerProfileUseCase
) {
    /** 测试可注入随机源 */
    @Volatile
    internal var random: Random = Random.Default
    /** 获取今日任务列表，如不存在则生成 */
    fun getTodayTasks(): Flow<List<DailyTask>> {
        val today = LocalDate.now().toString()
        return dailyTaskDao.getTasksByDate(today).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    /** 确保今日任务已生成（App 启动或首页加载时调用） */
    suspend fun ensureTodayTasksGenerated() {
        val today = LocalDate.now().toString()
        val existing = dailyTaskDao.getTasksByDateOnce(today)
        if (existing.isEmpty()) {
            // 清理旧任务（保留 7 天以防回看，这里简单清理所有旧任务）
            dailyTaskDao.deleteOlderThan(today)
            val tasks = generateDailyTasks(today)
            dailyTaskDao.insertAll(tasks.map { it.toEntity() })
        }
    }

    /**
     * 游戏结束时更新任务进度
     * @param gameId 本局游戏 ID
     * @param scorePercent 得分百分比
     * @param wasRecommended 是否采纳了推荐
     */
    suspend fun updateProgressOnGameEnd(
        gameId: String,
        scorePercent: Int,
        wasRecommended: Boolean
    ) {
        val today = LocalDate.now().toString()
        val tasks = dailyTaskDao.getTasksByDateOnce(today).map { it.toDomain() }
        var hasUpdate = false

        val updated = tasks.map { task ->
            if (task.isCompleted) return@map task
            val newProgress = when (task.taskType) {
                DailyTaskType.PLAY_GAMES ->
                    task.currentProgress + 1  // 每玩一局 +1
                DailyTaskType.HIGH_SCORE ->
                    if (scorePercent >= 70) task.currentProgress + 1 else task.currentProgress
                DailyTaskType.ADOPT_RECOMMEND ->
                    if (wasRecommended) task.currentProgress + 1 else task.currentProgress
                DailyTaskType.PLAY_DIFFERENT ->
                    // 简化：每玩一局 +1（实际应去重 gameId，但单次调用无法判断）
                    task.currentProgress + 1
                DailyTaskType.TRY_NEW_GAME ->
                    // 需要检查是否是新游戏，简化为每次 +1
                    task.currentProgress + 1
            }

            if (newProgress != task.currentProgress) {
                hasUpdate = true
                task.copy(
                    currentProgress = newProgress,
                    isCompleted = newProgress >= task.targetProgress
                )
            } else {
                task
            }
        }

        if (hasUpdate) {
            updated.forEach { dailyTaskDao.upsert(it.toEntity()) }
        }
    }

    /**
     * 领取任务奖励
     * @return 实际获得的 XP（0 表示不可领取）
     */
    suspend fun claimReward(taskId: Long): Int {
        val today = LocalDate.now().toString()
        val tasks = dailyTaskDao.getTasksByDateOnce(today).map { it.toDomain() }
        val task = tasks.find { it.id == taskId } ?: return 0
        if (!task.canClaimReward) return 0

        // 标记已领取
        dailyTaskDao.upsert(task.copy(isRewardClaimed = true).toEntity())

        // 发放 XP
        if (task.xpReward > 0) {
            playerProfileUseCase.addXP(task.xpReward)
        }
        return task.xpReward
    }

    /** 今日全部任务是否已完成 */
    suspend fun areAllTasksCompleted(): Boolean {
        val today = LocalDate.now().toString()
        val tasks = dailyTaskDao.getTasksByDateOnce(today).map { it.toDomain() }
        return tasks.isNotEmpty() && tasks.all { it.isCompleted }
    }

    /**
     * 生成 3 个每日任务（从池中随机选择，目标值随机化）
     */
    internal fun generateDailyTasks(date: String): List<DailyTask> {
        val pool = DailyTaskType.entries.toList()
        val selected = pool.shuffled(random).take(TASKS_PER_DAY)

        return selected.mapIndexed { index, type ->
            val (desc, target, xp) = when (type) {
                DailyTaskType.PLAY_GAMES ->
                    Triple("玩 ${random.nextInt(2, 5)} 局游戏", random.nextInt(2, 5), 30)
                DailyTaskType.TRY_NEW_GAME ->
                    Triple("尝试 1 款没玩过的游戏", 1, 40)
                DailyTaskType.HIGH_SCORE ->
                    Triple("获得 1 次高分（≥70%）", 1, 50)
                DailyTaskType.ADOPT_RECOMMEND ->
                    Triple("采纳 1 次推荐美食", 1, 35)
                DailyTaskType.PLAY_DIFFERENT ->
                    Triple("玩 ${random.nextInt(2, 4)} 款不同的游戏", random.nextInt(2, 4), 45)
            }
            DailyTask(
                id = 0,
                taskType = type,
                description = desc,
                targetProgress = target,
                xpReward = xp,
                date = date
            )
        }
    }

    companion object {
        const val TASKS_PER_DAY = 3
    }
}
