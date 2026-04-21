package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.Achievement
import com.eatif.app.domain.model.DefaultFoods
import com.eatif.app.domain.model.Food
import com.eatif.app.domain.model.GameList
import com.eatif.app.domain.model.GameStats
import com.eatif.app.domain.repository.GameStatsRepository
import com.eatif.app.domain.usecase.AchievementEngine
import com.eatif.app.domain.usecase.AddHistoryUseCase
import com.eatif.app.domain.usecase.GetAllFoodsUseCase
import com.eatif.app.domain.usecase.PlayerProfileUseCase
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
    private val gameStatsRepository: GameStatsRepository
) : ViewModel() {

    private val defaultFoods = DefaultFoods.list

    private val _foods = MutableStateFlow<List<Food>>(defaultFoods)
    val foods: StateFlow<List<Food>> = _foods.asStateFlow()

    init {
        loadFoods()
    }

    fun loadFoods() {
        viewModelScope.launch {
            getAllFoodsUseCase().collect { repositoryFoods ->
                if (repositoryFoods.isNotEmpty()) {
                    _foods.value = repositoryFoods
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
            addHistoryUseCase(foodName, gameName, scorePercent)

            val difficulty = GameSettingsManager.difficulty
            gameStatsRepository.insert(
                GameStats(
                    gameId = gameId, foodName = foodName,
                    score = scorePercent, scorePercent = scorePercent,
                    difficulty = difficulty, playTimeSeconds = playTimeSeconds
                )
            )

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
                    unlockedAchievements = unlockedAchievements
                )
            )
        }
    }

    data class GameEndResult(
        val xpEarned: Int,
        val playerLevel: Int,
        val unlockedAchievements: List<Achievement>
    )
}
