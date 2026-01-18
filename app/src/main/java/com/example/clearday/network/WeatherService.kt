package com.example.clearday.network

import com.example.clearday.models.AqiResponse
import com.example.clearday.models.CurrentWeatherResponse
import com.example.clearday.models.MainStats
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    // Existing AQI call...
    @GET("data/2.5/air_pollution")
    suspend fun getAirQuality(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String
    ): AqiResponse

    // ADD THIS: Current Weather call
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric" // Celsius
    ): CurrentWeatherResponse

    data class ForecastResponse(
        val list: List<ForecastItem>
    )

    data class ForecastItem(
        val dt: Long, // Unix timestamp
        val main: MainStats,
        val dt_txt: String // "2023-10-15 09:00:00"
    )

    // Add this to interface
    @GET("data/2.5/forecast")
    suspend fun getForecast(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): ForecastResponse
}