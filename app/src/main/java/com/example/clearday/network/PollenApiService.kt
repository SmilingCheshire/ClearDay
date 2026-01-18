package com.example.clearday.network

import com.example.clearday.network.model.PollenForecastResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface PollenApiService {

    /**
     * Get pollen forecast data for a specific location.
     * 
     * @param location Location in format "latitude,longitude" (e.g., "52.52,13.405")
     * @param days Number of forecast days (1-5)
     * @param pageSize Number of results per page (max 5 for daily forecast)
     * @param languageCode Language code for response (e.g., "en", "pl")
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
     * Get specific pollen type forecast.
     * Use plantsDescription parameter to get detailed plant information.
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
