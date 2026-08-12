package com.eatif.app.data.local

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject

/**
 * 好友成绩本地存储 - 基于 SharedPreferences，避免 DB 迁移。
 *
 * 每条记录：好友昵称 + 游戏 + 分数，用于本地 PK 排行。
 */
data class FriendScore(
    val id: String,
    val name: String,
    val gameId: String,
    val gameName: String,
    val scorePercent: Int,
    val timestamp: Long = System.currentTimeMillis()
)

object FriendScoreStore {
    private const val PREFS_NAME = "eatif_friend_scores"
    private const val KEY_SCORES = "scores"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getAll(): List<FriendScore> {
        val raw = prefs.getString(KEY_SCORES, null) ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).map { i ->
                val obj = arr.getJSONObject(i)
                FriendScore(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    gameId = obj.getString("gameId"),
                    gameName = obj.optString("gameName", obj.getString("gameId")),
                    scorePercent = obj.getInt("scorePercent"),
                    timestamp = obj.optLong("timestamp", 0L)
                )
            }
        }.getOrDefault(emptyList())
    }

    fun add(score: FriendScore) {
        val current = getAll().toMutableList()
        current.add(score)
        save(current)
    }

    fun remove(id: String) {
        val current = getAll().filterNot { it.id == id }
        save(current)
    }

    private fun save(list: List<FriendScore>) {
        val arr = JSONArray()
        list.forEach { fs ->
            arr.put(JSONObject().apply {
                put("id", fs.id)
                put("name", fs.name)
                put("gameId", fs.gameId)
                put("gameName", fs.gameName)
                put("scorePercent", fs.scorePercent)
                put("timestamp", fs.timestamp)
            })
        }
        prefs.edit().putString(KEY_SCORES, arr.toString()).apply()
    }
}
