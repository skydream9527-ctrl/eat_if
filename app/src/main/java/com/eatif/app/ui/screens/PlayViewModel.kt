package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.Achievement
import com.eatif.app.domain.model.DefaultFoods
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.GameList
import com.eatif.app.domain.model.GameStats
import com.eatif.app.domain.model.Recommendation
import com.eatif.app.domain.repository.GameStatsRepository
import com.eatif.app.domain.usecase.AchievementEngine
import com.eatif.app.domain.usecase.AddHistoryUseCase
import com.eatif.app.domain.usecase.DailyTaskUseCase
import com.eatif.app.domain.usecase.GameDrivenRecommendUseCase
import com.eatif.app.domain.usecase.GetAllFoodsUseCase
import com.eatif.app.domain.usecase.PlayerProfileUseCase
import com.eatif.app.domain.usecase.SmartRecommendUseCase
import com.eatif.app.ui.settings.GameSettingsManager
import com.eatif.app.ui.settings.SkinSettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayViewModel @Inject constructor(
    private val getAllFoodsUseCase: GetAllFoodsUseCase,
    private val addHistoryUseCase: AddHistoryUseCase,
    private val achievementEngine: AchievementEngine,
    private val playerProfileUseCase: PlayerProfileUseCase,
    private val gameStatsRepository: GameStatsRepository,
    private val smartRecommendUseCase: SmartRecommendUseCase,
    private val gameDrivenRecommendUseCase: GameDrivenRecommendUseCase,
    private val dailyTaskUseCase: DailyTaskUseCase
) : ViewModel() {

    private val defaultFoods = DefaultFoods.list

    private val _foods = MutableStateFlow<List<Food>>(defaultFoods)
    val foods: StateFlow<List<Food>> = _foods.asStateFlow()

    /**
     * 推荐美食列表（按推荐分排序）。
     * 游戏开始时即生成，供游戏内 `foods.take(3)` 等使用——
     * 让游戏自然地展示推荐候选，实现"游戏 → 决策"闭环。
     */
    private val _recommendedFoods = MutableStateFlow<List<Food>>(emptyList())
    val recommendedFoods: StateFlow<List<Food>> = _recommendedFoods.asStateFlow()

    /** 最近一次推荐候选的美食名集合，用于采纳率跟踪 */
    private var lastRecommendedNames: Set<String> = emptySet()

    /**
     * 游戏结束后的最终推荐（基于真实分数），供 ResultScreen 使用。
     */
    private val _finalRecommendations = MutableStateFlow<List<Recommendation>>(emptyList())
    val finalRecommendations: StateFlow<List<Recommendation>> = _finalRecommendations.asStateFlow()

    init {
        loadFoods()
        loadRecommendations()
    }

    fun loadFoods() {
        viewModelScope.launch {
            getAllFoodsUseCase().collect { repositoryFoods ->
                if (repositoryFoods.isNotEmpty()) {
                    _foods.value = repositoryFoods
                    loadRecommendations()
                }
            }
        }
    }

    /**
     * 加载推荐排序后的美食列表。
     * 游戏开始前用中性分数（50）预排序，让游戏内 take(3) 自动取到推荐候选。
     */
    private fun loadRecommendations() {
        viewModelScope.launch {
            smartRecommendUseCase(count = RECOMMEND_POOL_SIZE).collect { result ->
                result.onSuccess { recommendations ->
                    lastRecommendedNames = recommendations.take(TOP_N).map { it.food.name }.toSet()
                    // 推荐池：Top-N 推荐 + 剩余美食（保证游戏 foods 列表完整）
                    val recommended = recommendations.map { it.food }
                    val remaining = _foods.value.filter { it.name !in recommended.map { f -> f.name } }
                    _recommendedFoods.value = recommended + remaining
                }
            }
        }
    }

    fun getGameName(gameId: String): String {
        return GameList.games.find { it.id == gameId }?.name ?: ""
    }

    fun processGameEnd(
        gameId: String,
        foodName: String,
        scorePercent: Int,
        playTimeSeconds: Long,
        onResult: (GameEndResult) -> Unit
    ) {
        viewModelScope.launch {
            val gameName = getGameName(gameId)
            // 决策闭环：检查用户是否采纳了推荐
            val wasRecommended = foodName in lastRecommendedNames
            addHistoryUseCase(foodName, gameName, scorePercent, wasRecommended)

            // 更新每日任务进度
            dailyTaskUseCase.updateProgressOnGameEnd(gameId, scorePercent, wasRecommended)

            val difficulty = GameSettingsManager.difficulty
            gameStatsRepository.insert(
                GameStats(
                    gameId = gameId, foodName = foodName,
                    score = scorePercent, scorePercent = scorePercent,
                    difficulty = difficulty, playTimeSeconds = playTimeSeconds
                )
            )

            // 生成基于真实分数的最终推荐（供 ResultScreen 展示）
            var finalRecs: List<Recommendation> = emptyList()
            gameDrivenRecommendUseCase(scorePercent, count = TOP_N).collect { result ->
                result.onSuccess { recommendations ->
                    finalRecs = recommendations
                    _finalRecommendations.value = recommendations
                }
            }

            val xpEarned = playerProfileUseCase.calculateXP(scorePercent, difficulty, playTimeSeconds)
            val profile = playerProfileUseCase.recordGameSession(xpEarned, playTimeSeconds)

            val event = AchievementEngine.GameEndEvent(
                gameId = gameId, score = scorePercent, scorePercent = scorePercent,
                playTimeSeconds = playTimeSeconds, difficulty = difficulty.name
            )
            val unlockedAchievements = achievementEngine.checkAndUnlock(event)

            unlockedAchievements.forEach { achievement ->
                achievement.unlockSkinId?.let { skinId ->
                    SkinSettingsManager.unlockSkin(skinId)
                }
            }

            onResult(
                GameEndResult(
                    xpEarned = xpEarned,
                    playerLevel = profile.playerLevel,
                    unlockedAchievements = unlockedAchievements,
                    finalRecommendations = finalRecs
                )
            )
        }
    }

    data class GameEndResult(
        val xpEarned: Int,
        val playerLevel: Int,
        val unlockedAchievements: List<Achievement>,
        val finalRecommendations: List<Recommendation> = emptyList()
    )

    companion object {
        private const val TOP_N = 3
        private const val RECOMMEND_POOL_SIZE = 10
    }
}
