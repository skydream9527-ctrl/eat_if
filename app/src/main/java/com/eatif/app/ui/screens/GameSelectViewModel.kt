package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.data.local.LevelProgressDao
import com.eatif.app.data.local.GameStatsDao
import com.eatif.app.data.local.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameCardInfo(
    val gameId: String,
    val stars: Int,
    val highScore: Int
)

@HiltViewModel
class GameSelectViewModel @Inject constructor(
    private val levelProgressDao: LevelProgressDao,
    private val gameStatsDao: GameStatsDao
) : ViewModel() {
    private val _gameInfoMap = MutableStateFlow<Map<String, GameCardInfo>>(emptyMap())
    val gameInfoMap: StateFlow<Map<String, GameCardInfo>> = _gameInfoMap.asStateFlow()

    fun loadGameInfo(gameIds: List<String>) {
        viewModelScope.launch {
            val infoMap = gameIds.associateWith { gameId ->
                val progress = levelProgressDao.getProgressOnce(gameId)
                val totalStars = progress?.toDomain()?.stars?.values?.sum() ?: 0
                val bestScore = gameStatsDao.getBestScoreForGameOnce(gameId)?.score_percent ?: 0
                GameCardInfo(gameId = gameId, stars = totalStars, highScore = bestScore)
            }
            _gameInfoMap.value = infoMap
        }
    }
}