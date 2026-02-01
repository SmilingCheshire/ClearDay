package com.example.clearday.network.model

/**
 * Model representing current weather conditions retrieved from the weather service.
 */
data class WeatherResponse(
    val main: Main,
    val weather: List<WeatherDescription>,
    val name: String? = null
)

/**
 * Primary physical metrics for weather including temperature and perceived temperature.
 */
data class Main(
    val temp: Double,
    val feels_like: Double
)

/**
 * Brief overview and detailed description of current weather phenomena.
 */
data class WeatherDescription(
    val main: String,
    val description: String
)