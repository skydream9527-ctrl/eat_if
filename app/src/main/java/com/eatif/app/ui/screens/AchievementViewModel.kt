package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.Achievement
import com.eatif.app.domain.model.AchievementCategory
import com.eatif.app.domain.model.AchievementCondition
import com.eatif.app.domain.model.AchievementProgress
import com.eatif.app.domain.model.PlayerProfile
import com.eatif.app.domain.usecase.AchievementRegistry
import com.eatif.app.domain.usecase.PlayerProfileUseCase
import com.eatif.app.ui.settings.AchievementSettingsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AchievementWithProgress(
    val achievement: Achievement,
    val progress: AchievementProgress
)

@HiltViewModel
class AchievementViewModel @Inject constructor(
    private val playerProfileUseCase: PlayerProfileUseCase
) : ViewModel() {
    private val _achievements = MutableStateFlow<List<AchievementWithProgress>>(emptyList())
    val achievements: StateFlow<List<AchievementWithProgress>> = _achievements.asStateFlow()

    private val _filter = MutableStateFlow<AchievementFilter>(AchievementFilter.ALL)
    val filter: StateFlow<AchievementFilter> = _filter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<AchievementCategory?>(null)
    val categoryFilter: StateFlow<AchievementCategory?> = _categoryFilter.asStateFlow()

    fun loadAchievements() {
        viewModelScope.launch {
            val profile = playerProfileUseCase.getProfile()
            val unlockedIds = AchievementSettingsManager.getUnlockedIds()

            val achievementsWithProgress = AchievementRegistry.all.map { achievement ->
                val progress = calculateProgress(achievement, profile, unlockedIds)
                AchievementWithProgress(achievement, progress)
            }
            _achievements.value = applyFilters(achievementsWithProgress)
        }
    }

    fun setFilter(newFilter: AchievementFilter) {
        _filter.value = newFilter
        loadAchievements()
    }

    fun setCategoryFilter(category: AchievementCategory?) {
        _categoryFilter.value = category
        loadAchievements()
    }

    private fun calculateProgress(
        achievement: Achievement,
        profile: PlayerProfile,
        unlockedIds: Set<String>
    ): AchievementProgress {
        val isUnlocked = unlockedIds.contains(achievement.id)
        val required = when (val condition = achievement.condition) {
            is AchievementCondition.TotalGames -> condition.count
            is AchievementCondition.GameHighScore -> condition.score
            is AchievementCondition.ConsecutiveDays -> condition.days
            is AchievementCondition.PlayAllGames -> condition.count
            is AchievementCondition.TotalPlayTime -> condition.seconds.toInt()
        }

        val current = when (val condition = achievement.condition) {
            is AchievementCondition.TotalGames -> profile.totalGamesPlayed
            is AchievementCondition.ConsecutiveDays -> profile.currentStreak
            is AchievementCondition.TotalPlayTime -> profile.totalPlayTimeSeconds.toInt()
            is AchievementCondition.GameHighScore -> 0
            is AchievementCondition.PlayAllGames -> 0
        }

        return AchievementProgress(
            achievementId = achievement.id,
            currentProgress = if (isUnlocked) required else current,
            requiredProgress = required,
            isUnlocked = isUnlocked
        )
    }

    private fun applyFilters(list: List<AchievementWithProgress>): List<AchievementWithProgress> {
        var result = list
        if (_filter.value == AchievementFilter.UNLOCKED) {
            result = result.filter { it.progress.isUnlocked }
        } else if (_filter.value == AchievementFilter.LOCKED) {
            result = result.filter { !it.progress.isUnlocked }
        }
        _categoryFilter.value?.let { cat ->
            result = result.filter { it.achievement.category == cat }
        }
        return result
    }
}

enum class AchievementFilter { ALL, UNLOCKED, LOCKED }
