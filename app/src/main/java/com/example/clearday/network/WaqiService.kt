package com.example.clearday.network

import com.example.clearday.models.WaqiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface WaqiService {
    // URL format: https://api.waqi.info/feed/geo:10.3;20.4/?token=...
    @GET("feed/geo:{lat};{lon}/")
    suspend fun getAirQuality(
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
        @Query("token") token: String
    ): WaqiResponse
}