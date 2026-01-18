package com.example.clearday.repository

import com.example.clearday.BuildConfig
import com.example.clearday.models.CurrentWeatherResponse
import com.example.clearday.models.WaqiResponse
import com.example.clearday.network.WeatherService
import com.example.clearday.network.WaqiService

// Update constructor to take BOTH services
class WeatherRepository(
    private val weatherApi: WeatherService,
    private val waqiApi: WaqiService
) {

    // 1. Weather (Still from OpenWeather)
    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<CurrentWeatherResponse> {
        return try {
            val res = weatherApi.getCurrentWeather(lat, lon, com.example.clearday.BuildConfig.OPENWEATHER_API_KEY)
            Result.success(res)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // 2. Air Quality (NOW FROM WAQI)
    suspend fun getAirQuality(lat: Double, lon: Double): Result<WaqiResponse> {
        return try {
            // PASS THE TOKEN HERE using BuildConfig.WAQI_API_KEY
            val response = waqiApi.getAirQuality(
                lat = lat,
                lon = lon,
                token = BuildConfig.WAQI_API_KEY
            )

            if (response.status == "ok") {
                Result.success(response)
            } else {
                Result.failure(Exception("API Error: ${response.status}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getForecast(lat: Double, lon: Double): Result<WeatherService.ForecastResponse> {
        return try {
            val res = weatherApi.getForecast(lat, lon, com.example.clearday.BuildConfig.OPENWEATHER_API_KEY)
            Result.success(res)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}