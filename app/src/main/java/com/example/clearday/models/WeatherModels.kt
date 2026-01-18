package com.example.clearday.models

import com.google.gson.annotations.SerializedName

// --- Air Quality Models ---
data class AqiResponse(
    val coord: Coord,
    val list: List<AqiData>
)

data class AqiData(
    val main: AqiMain,
    val components: Map<String, Double>, // Holds co, no2, o3, etc.
    val dt: Long
)

data class AqiMain(
    val aqi: Int // 1 = Good, 5 = Very Poor
)

data class Coord(
    val lon: Double,
    val lat: Double
)
// Response wrapper for /weather endpoint
data class CurrentWeatherResponse(
    val main: MainStats,
    val weather: List<WeatherDescription>,
    val name: String // City name
)

data class MainStats(
    val temp: Double,
    val humidity: Int
)

data class WeatherDescription(
    val description: String,
    val icon: String
)

data class ForecastResponse(
    val list: List<ForecastItem>
)

data class ForecastItem(
    val dt: Long,
    val main: MainStats, // Re-using your existing MainStats class
    val dt_txt: String
)