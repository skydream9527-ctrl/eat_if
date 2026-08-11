package com.eatif.app.ui

import com.eatif.app.domain.model.Achievement
import com.eatif.app.domain.model.Recommendation

object GameEndResultHolder {
    var unlockedAchievements: List<Achievement> = emptyList()
    /** 游戏结束后基于真实分数生成的最终推荐，供 ResultScreen 展示 */
    var finalRecommendations: List<Recommendation> = emptyList()
}
