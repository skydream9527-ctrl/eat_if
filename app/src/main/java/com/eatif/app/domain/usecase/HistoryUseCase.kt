package com.eatif.app.domain.usecase

import com.eatif.app.domain.model.History
import com.eatif.app.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    operator fun invoke(): Flow<List<History>> = repository.getRecentHistory()
}

class AddHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke(foodName: String, gameName: String, scorePercent: Int): Long {
        val history = History(
            foodName = foodName,
            gameName = gameName,
            scorePercent = scorePercent,
            timestamp = System.currentTimeMillis()
        )
        return repository.addHistory(history)
    }
}

class ClearHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke() = repository.clearHistory()
}