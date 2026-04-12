package com.eatif.app.data.remote

import com.google.gson.annotations.SerializedName

data class MeituanResponse<T>(
    @SerializedName("data")
    val data: List<T>
)

data class MeituanFood(
    @SerializedName("name")
    val name: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("imageUrl")
    val imageUrl: String?
)
