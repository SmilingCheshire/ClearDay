package com.example.clearday.repository

import com.example.clearday.BuildConfig
import com.example.clearday.network.ApiClient
import com.example.clearday.network.WeatherApiService
import com.example.clearday.network.model.AirQualityResponse
import com.example.clearday.network.model.WeatherResponse
import android.util.Log

class WeatherRepository {
    // Tworzymy instancję serwisu przez ApiClient
    private val api: WeatherApiService = ApiClient.retrofit.create(WeatherApiService::class.java)

    suspend fun getWeather(lat: Double, lon: Double): Result<WeatherResponse> {
        return try {
            val response = api.getCurrentWeather(
                lat = lat,
                lon = lon,
                apiKey = BuildConfig.OPENWEATHER_API_KEY
            )
            Result.success(response)
        } catch (e: Exception) {
            Log.e("WeatherRepo", "Weather error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getAirQuality(lat: Double, lon: Double): Result<AirQualityResponse> {
        return try {
            val response = api.getAirQuality(
                lat = lat,
                lon = lon,
                apiKey = BuildConfig.OPENWEATHER_API_KEY
            )
            Result.success(response)
        } catch (e: Exception) {
            Log.e("WeatherRepo", "AQI error: ${e.message}")
            Result.failure(e)
        }
    }
}