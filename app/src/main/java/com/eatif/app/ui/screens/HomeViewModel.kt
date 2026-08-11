package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.DailyTask
import com.eatif.app.domain.model.PlayerProfile
import com.eatif.app.domain.model.Recommendation
import com.eatif.app.domain.usecase.DailyTaskUseCase
import com.eatif.app.domain.usecase.GetFoodCountUseCase
import com.eatif.app.domain.usecase.PlayerProfileUseCase
import com.eatif.app.domain.usecase.SmartRecommendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getFoodCountUseCase: GetFoodCountUseCase,
    private val smartRecommendUseCase: SmartRecommendUseCase,
    private val dailyTaskUseCase: DailyTaskUseCase,
    private val playerProfileUseCase: PlayerProfileUseCase
) : ViewModel() {

    private val _foodCount = MutableStateFlow(0)
    val foodCount: StateFlow<Int> = _foodCount.asStateFlow()

    private val _recommendations = MutableStateFlow<List<Recommendation>>(emptyList())
    val recommendations: StateFlow<List<Recommendation>> = _recommendations.asStateFlow()

    private val _dailyTasks = MutableStateFlow<List<DailyTask>>(emptyList())
    val dailyTasks: StateFlow<List<DailyTask>> = _dailyTasks.asStateFlow()

    private val _profile = MutableStateFlow<PlayerProfile?>(null)
    val profile: StateFlow<PlayerProfile?> = _profile.asStateFlow()

    init {
        viewModelScope.launch {
            getFoodCountUseCase().collect { count ->
                _foodCount.value = count
            }
        }
        loadRecommendations()
        loadDailyTasks()
        loadProfile()
    }

    fun loadRecommendations() {
        viewModelScope.launch {
            smartRecommendUseCase(5).collect { result ->
                result.onSuccess { _recommendations.value = it }
            }
        }
    }

    private fun loadDailyTasks() {
        viewModelScope.launch {
            // 确保今日任务已生成
            dailyTaskUseCase.ensureTodayTasksGenerated()
            // 订阅今日任务
            dailyTaskUseCase.getTodayTasks().collect { tasks ->
                _dailyTasks.value = tasks
            }
        }
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _profile.value = playerProfileUseCase.getProfile()
        }
    }

    /** 领取任务奖励 */
    fun claimTaskReward(taskId: Long) {
        viewModelScope.launch {
            val xp = dailyTaskUseCase.claimReward(taskId)
            if (xp > 0) {
                // 刷新 profile 以更新 XP 显示
                _profile.value = playerProfileUseCase.getProfile()
            }
        }
    }

    /** 刷新数据（从其他页面返回时调用） */
    fun refresh() {
        loadProfile()
    }
}
