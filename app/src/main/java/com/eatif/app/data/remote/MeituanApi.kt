package com.eatif.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface MeituanApi {

    @GET("searchFood")
    suspend fun searchFood(
        @Query("city") city: String,
        @Query("keyword") keyword: String
    ): MeituanResponse<MeituanFood>
}
