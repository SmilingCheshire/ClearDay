package com.example.clearday.network

import com.example.clearday.network.model.PollenForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface defining endpoints for the Google Pollen API.
 */
interface PollenApiService {

    /**
     * Fetches general pollen forecast data for specific coordinates.
     */
    @GET("v1/forecast:lookup")
    suspend fun getPollenForecast(
        @Query("location.latitude") latitude: Double,
        @Query("location.longitude") longitude: Double,
        @Query("days") days: Int = 5,
        @Query("pageSize") pageSize: Int = 5,
        @Query("languageCode") languageCode: String = "en",
        @Query("key") apiKey: String
    ): PollenForecastResponse

    /**
     * Fetches detailed pollen forecast including specific plant information.
     */
    @GET("v1/forecast:lookup")
    suspend fun getPollenForecastWithPlants(
        @Query("location.latitude") latitude: Double,
        @Query("location.longitude") longitude: Double,
        @Query("days") days: Int = 5,
        @Query("pageSize") pageSize: Int = 5,
        @Query("plantsDescription") plantsDescription: Boolean = true,
        @Query("languageCode") languageCode: String = "en",
        @Query("key") apiKey: String
    ): PollenForecastResponse
}