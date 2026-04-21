package com.eatif.app.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.domain.model.GameLevel
import com.eatif.app.domain.model.LevelProgress
import com.eatif.app.domain.usecase.GameLevelRegistry
import com.eatif.app.domain.usecase.LevelManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LevelSelectViewModel @Inject constructor(
    private val levelManager: LevelManager
) : ViewModel() {
    private val _levels = MutableStateFlow<List<GameLevel>>(emptyList())
    val levels: StateFlow<List<GameLevel>> = _levels.asStateFlow()

    private val _progress = MutableStateFlow<LevelProgress?>(null)
    val progress: StateFlow<LevelProgress?> = _progress.asStateFlow()

    private val _unlockedMap = MutableStateFlow<Map<Int, Boolean>>(emptyMap())
    val unlockedMap: StateFlow<Map<Int, Boolean>> = _unlockedMap.asStateFlow()

    fun loadLevels(gameId: String) {
        viewModelScope.launch {
            val gameLevels = GameLevelRegistry.getLevelsForGame(gameId)
            _levels.value = gameLevels

            val prog = levelManager.getProgress(gameId)
            _progress.value = prog

            val unlocked = gameLevels.associate { level ->
                level.levelNumber to levelManager.isLevelUnlocked(gameId, level.levelNumber)
            }
            _unlockedMap.value = unlocked
        }
    }
}
