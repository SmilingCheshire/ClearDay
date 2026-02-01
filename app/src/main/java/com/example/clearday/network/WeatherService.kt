package com.example.clearday.network

import com.example.clearday.models.AqiResponse
import com.example.clearday.models.CurrentWeatherResponse
import com.example.clearday.models.MainStats
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Comprehensive service for weather and forecast data using OpenWeatherMap data structures.
 */
interface WeatherService {

    /**
     * Fetches current Air Quality Index and pollutant components.
     */
    @GET("data/2.5/air_pollution")
    suspend fun getAirQuality(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): AqiResponse

    /**
     * Fetches real-time weather stats for the given coordinates.
     */
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): CurrentWeatherResponse

    /**
     * Fetches a 5-day weather forecast with 3-hour step intervals.
     */
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): ForecastResponse

    /**
     * Data wrapper for weather forecast response.
     */
    data class ForecastResponse(
        val list: List<ForecastItem>
    )

    /**
     * Represents a single time-point in the weather forecast.
     */
    data class ForecastItem(
        val dt: Long,
        val main: MainStats,
        val dt_txt: String
    )
}