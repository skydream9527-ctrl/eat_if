package com.eatif.app.domain.repository

import com.eatif.app.domain.model.History
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun getRecentHistory(): Flow<List<History>>
    fun getHistoryCount(): Flow<Int>
    suspend fun addHistory(history: History): Long
    suspend fun deleteHistory(id: Long)
    suspend fun clearHistory()
}