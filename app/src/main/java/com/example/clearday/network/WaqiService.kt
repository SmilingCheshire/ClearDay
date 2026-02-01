package com.example.clearday.network

import com.example.clearday.models.WaqiResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit interface defining endpoints for the World Air Quality Index (WAQI) API.
 */
interface WaqiService {

    /**
     * Fetches real-time air quality data for a geographical point.
     */
    @GET("feed/geo:{lat};{lon}/")
    suspend fun getAirQuality(
        @Path("lat") lat: Double,
        @Path("lon") lon: Double,
        @Query("token") token: String
    ): WaqiResponse
}