package com.example.clearday.repository

import com.example.clearday.BuildConfig
import com.example.clearday.network.WeatherApiService
import com.example.clearday.network.model.AirQualityResponse

/**
 * Repository handling air quality data requests from OpenWeatherMap.
 */
class AirQualityRepository(private val apiService: WeatherApiService) {

    /**
     * Fetches air quality data and wraps the response in a Result object.
     */
    suspend fun getAirQuality(latitude: Double, longitude: Double): Result<AirQualityResponse> {
        return try {
            val response = apiService.getAirQuality(
                lat = latitude,
                lon = longitude,
                apiKey = BuildConfig.OPENWEATHER_API_KEY
            )
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}