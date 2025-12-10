package com.example.clearday.network


import com.example.clearday.network.model.AirQualityResponse
import com.example.clearday.network.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApiService {

    // Current weather
    @GET("weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric" // Celsius
    ): WeatherResponse

    // Air pollution / air quality
    @GET("air_pollution")
    suspend fun getAirQuality(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
    ): AirQualityResponse
}
