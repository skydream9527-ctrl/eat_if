package com.eatif.app.ui.screens.friendpk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eatif.app.data.local.FriendScore
import com.eatif.app.data.local.FriendScoreStore
import com.eatif.app.domain.model.GameList
import com.eatif.app.domain.model.GameStats
import com.eatif.app.domain.repository.GameStatsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

/**
 * 单个游戏的 PK 排行项：合并「我的最佳」与「好友成绩」
 */
data class PKRankItem(
    val name: String,
    val scorePercent: Int,
    val isMe: Boolean,
    val timestamp: Long,
    /** 仅好友项有值，用于删除 */
    val friendId: String? = null
)

data class GamePKBoard(
    val gameId: String,
    val gameName: String,
    val emoji: String,
    val ranks: List<PKRankItem>
)

@HiltViewModel
class FriendPKViewModel @Inject constructor(
    private val gameStatsRepository: GameStatsRepository
) : ViewModel() {

    private val _friendScores = MutableStateFlow<List<FriendScore>>(emptyList())
    val friendScores: StateFlow<List<FriendScore>> = _friendScores.asStateFlow()

    /** 用户每个游戏的最佳成绩（按 scorePercent 取最高） */
    private val myBestPerGame: StateFlow<Map<String, GameStats>> =
        gameStatsRepository.getGlobalTopScores()
            .map { list ->
                list.groupBy { it.gameId }
                    .mapValues { (_, stats) -> stats.maxByOrNull { it.scorePercent }!! }
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    /** 合并后的 PK 排行榜（按游戏分组） */
    val boards: StateFlow<List<GamePKBoard>> =
        combine(myBestPerGame, _friendScores) { myMap, friends ->
            val gameIds = (myMap.keys + friends.map { it.gameId }).distinct()
            gameIds.mapNotNull { gameId ->
                val game = GameList.games.find { it.id == gameId }
                val ranks = mutableListOf<PKRankItem>()
                myMap[gameId]?.let { stats ->
                    ranks.add(
                        PKRankItem(
                            name = "我",
                            scorePercent = stats.scorePercent,
                            isMe = true,
                            timestamp = stats.timestamp
                        )
                    )
                }
                friends.filter { it.gameId == gameId }
                    .sortedByDescending { it.scorePercent }
                    .forEach { fs ->
                        ranks.add(
                            PKRankItem(
                                name = fs.name,
                                scorePercent = fs.scorePercent,
                                isMe = false,
                                timestamp = fs.timestamp,
                                friendId = fs.id
                            )
                        )
                    }
                if (ranks.isEmpty()) return@mapNotNull null
                GamePKBoard(
                    gameId = gameId,
                    gameName = game?.name ?: gameId,
                    emoji = game?.emoji ?: "🎮",
                    ranks = ranks.sortedByDescending { it.scorePercent }
                )
            }.sortedByDescending { board -> board.racesMaxScore() }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        reloadFriends()
    }

    fun reloadFriends() {
        _friendScores.value = FriendScoreStore.getAll()
    }

    fun addFriendScore(name: String, gameId: String, scorePercent: Int) {
        if (name.isBlank() || scorePercent < 0) return
        val game = GameList.games.find { it.id == gameId }
        val fs = FriendScore(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            gameId = gameId,
            gameName = game?.name ?: gameId,
            scorePercent = scorePercent.coerceIn(0, 100)
        )
        viewModelScope.launch {
            FriendScoreStore.add(fs)
            reloadFriends()
        }
    }

    fun removeFriendScore(id: String) {
        viewModelScope.launch {
            FriendScoreStore.remove(id)
            reloadFriends()
        }
    }

    private fun GamePKBoard.racesMaxScore(): Int =
        ranks.maxOfOrNull { it.scorePercent } ?: 0
}
