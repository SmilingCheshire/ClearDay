package com.example.clearday.network.model


data class WeatherResponse(
    val main: Main,
    val weather: List<WeatherDescription>,
    val name: String? = null
)

data class Main(
    val temp: Double,
    val feels_like: Double
)

data class WeatherDescription(
    val main: String,
    val description: String
)