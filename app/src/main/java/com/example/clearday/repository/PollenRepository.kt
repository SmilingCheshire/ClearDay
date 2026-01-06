package com.example.clearday.repository

import android.util.Log
import com.example.clearday.network.PollenApiClient
import com.example.clearday.network.model.PollenForecastResponse

class PollenRepository {

    private val pollenApiService = PollenApiClient.pollenApiService

    /**
     * Get pollen forecast for a specific location.
     */
    suspend fun getPollenForecast(
        latitude: Double,
        longitude: Double,
        days: Int = 5,
        languageCode: String = "en"
    ): Result<PollenForecastResponse> {
        return try {

            val response = pollenApiService.getPollenForecast(
                latitude = latitude,
                longitude = longitude,
                days = days,
                pageSize = days,
                languageCode = languageCode,
                apiKey = PollenApiClient.getApiKey()
            )
            Log.d("POLLEN_API", "Response: $response")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("POLLEN_API", "Error", e)
            Result.failure(e)
        }
    }

    /**
     * Get pollen forecast with detailed plant information.
     */
    suspend fun getPollenForecastWithPlants(
        latitude: Double,
        longitude: Double,
        days: Int = 5,
        languageCode: String = "en"
    ): Result<PollenForecastResponse> {
        return try {
            val response = pollenApiService.getPollenForecastWithPlants(
                latitude = latitude,
                longitude = longitude,
                days = days,
                pageSize = days,
                plantsDescription = true,
                languageCode = languageCode,
                apiKey = PollenApiClient.getApiKey()
            )
            Log.d("POLLEN_API", "Response: $response")
            Result.success(response)
        } catch (e: Exception) {
            Log.e("POLLEN_API", "Error", e)
            Result.failure(e)
        }
    }
}
