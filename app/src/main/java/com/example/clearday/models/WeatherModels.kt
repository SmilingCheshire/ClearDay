package com.example.clearday.models

import com.google.gson.annotations.SerializedName

/**
 * Response model for the OpenWeatherMap Air Pollution API.
 */
data class AqiResponse(
    val coord: Coord,
    val list: List<AqiData>
)

/**
 * Contains individual air quality measurements and timestamps.
 */
data class AqiData(
    val main: AqiMain,
    val components: Map<String, Double>,
    val dt: Long
)

/**
 * Represents the Air Quality Index (AQI) level.
 * Scale: 1 (Good) to 5 (Very Poor).
 */
data class AqiMain(
    val aqi: Int
)

/**
 * Geographic coordinates for the requested location.
 */
data class Coord(
    val lon: Double,
    val lat: Double
)

/**
 * Root response for the current weather data endpoint.
 */
data class CurrentWeatherResponse(
    val main: MainStats,
    val weather: List<WeatherDescription>,
    val name: String
)

/**
 * Basic atmospheric measurements including temperature and humidity.
 */
data class MainStats(
    val temp: Double,
    val humidity: Int
)

/**
 * Human-readable description and icon code for weather conditions.
 */
data class WeatherDescription(
    val description: String,
    val icon: String
)

/**
 * Response model for the 5-day weather forecast endpoint.
 */
data class ForecastResponse(
    val list: List<ForecastItem>
)

/**
 * Represents a specific forecast point in time.
 */
data class ForecastItem(
    val dt: Long,
    val main: MainStats,
    val dt_txt: String
)