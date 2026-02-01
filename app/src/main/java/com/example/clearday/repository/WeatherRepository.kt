package com.example.clearday.repository

import com.example.clearday.BuildConfig
import com.example.clearday.models.CurrentWeatherResponse
import com.example.clearday.models.WaqiResponse
import com.example.clearday.network.WeatherService
import com.example.clearday.network.WaqiService

/**
 * Consolidated repository for weather conditions (OpenWeather) and air quality data (WAQI).
 */
class WeatherRepository(
    private val weatherApi: WeatherService,
    private val waqiApi: WaqiService
) {

    /**
     * Fetches current atmospheric weather conditions.
     */
    suspend fun getCurrentWeather(lat: Double, lon: Double): Result<CurrentWeatherResponse> {
        return try {
            val res = weatherApi.getCurrentWeather(
                lat,
                lon,
                BuildConfig.OPENWEATHER_API_KEY
            )
            Result.success(res)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Retrieves detailed air quality data from the WAQI API service.
     */
    suspend fun getAirQuality(lat: Double, lon: Double): Result<WaqiResponse> {
        return try {
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

    /**
     * Fetches long-term weather forecast data.
     */
    suspend fun getForecast(lat: Double, lon: Double): Result<WeatherService.ForecastResponse> {
        return try {
            val res = weatherApi.getForecast(
                lat,
                lon,
                BuildConfig.OPENWEATHER_API_KEY
            )
            Result.success(res)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}