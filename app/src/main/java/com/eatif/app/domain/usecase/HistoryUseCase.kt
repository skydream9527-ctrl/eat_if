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
    /**
     * @param wasRecommended 用户是否从推荐 Top3 中选择了该美食（用于推荐采纳率统计）
     */
    suspend operator fun invoke(
        foodName: String,
        gameName: String,
        scorePercent: Int,
        wasRecommended: Boolean = false
    ): Long {
        val history = History(
            foodName = foodName,
            gameName = gameName,
            scorePercent = scorePercent,
            timestamp = System.currentTimeMillis(),
            wasRecommended = wasRecommended
        )
        return repository.addHistory(history)
    }
}

class ClearHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke() = repository.clearHistory()
}

/** 推荐采纳率统计 UseCase */
class RecommendationAdoptionUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    data class AdoptionStats(val adopted: Int, val total: Int) {
        val rate: Float get() = if (total == 0) 0f else adopted.toFloat() / total
    }

    operator fun invoke(): Flow<AdoptionStats> = kotlinx.coroutines.flow.combine(
        repository.getAdoptedRecommendationCount(),
        repository.getTotalRecommendationCount()
    ) { adopted, total -> AdoptionStats(adopted, total) }
}
