package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.GameStats
import com.eatif.app.domain.model.PlayerProfile
import com.eatif.app.domain.usecase.PlayerProfileUseCase
import com.eatif.app.domain.usecase.StatsOverview
import com.eatif.app.domain.usecase.StatsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsUseCase: StatsUseCase,
    val playerProfileUseCase: PlayerProfileUseCase
) : ViewModel() {
    private val _statsOverview = MutableStateFlow<StatsOverview?>(null)
    val statsOverview: StateFlow<StatsOverview?> = _statsOverview.asStateFlow()

    private val _topScores = MutableStateFlow<List<GameStats>>(emptyList())
    val topScores: StateFlow<List<GameStats>> = _topScores.asStateFlow()

    private val _profile = MutableStateFlow<PlayerProfile?>(null)
    val profile: StateFlow<PlayerProfile?> = _profile.asStateFlow()

    init { loadStats() }

    fun loadStats() {
        viewModelScope.launch {
            statsUseCase.getStatsOverview().collect { _statsOverview.value = it }
        }
        viewModelScope.launch {
            statsUseCase.getGlobalTopScores().collect { _topScores.value = it }
        }
        viewModelScope.launch {
            _profile.value = playerProfileUseCase.getProfile()
        }
    }
}
