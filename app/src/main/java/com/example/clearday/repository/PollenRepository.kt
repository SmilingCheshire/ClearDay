package com.example.clearday.repository

import android.util.Log
import com.example.clearday.network.PollenApiClient
import com.example.clearday.network.model.PollenForecastResponse

/**
 * Repository for fetching pollen levels and plant-specific forecasts using Google Pollen API.
 */
class PollenRepository {

    private val pollenApiService = PollenApiClient.pollenApiService

    /**
     * Retrieves general pollen forecast for a given location.
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
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves pollen forecast including detailed botanical information about plants.
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
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}