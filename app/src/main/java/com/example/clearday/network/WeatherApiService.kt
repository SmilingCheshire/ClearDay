package com.example.clearday.network

import com.example.clearday.network.model.AirQualityResponse
import com.example.clearday.network.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit interface for standard OpenWeatherMap 2.5 API endpoints.
 */
interface WeatherApiService {

    /**
     * Retrieves current weather conditions for a specific location.
     */
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse

    /**
     * Retrieves air pollution and chemical concentration data.
     */
    @GET("air_pollution")
    suspend fun getAirQuality(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
    ): AirQualityResponse
}