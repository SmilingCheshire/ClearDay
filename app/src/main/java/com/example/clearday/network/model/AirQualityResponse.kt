package com.example.clearday.network.model

/**
 * Model representing the response from an air quality monitoring service.
 */
data class AirQualityResponse(
    val list: List<AirQualityItem>
)

/**
 * Contains specific air quality data points, including pollutant concentrations and timestamp.
 */
data class AirQualityItem(
    val main: AirQualityMain,
    val components: AirQualityComponents?,
    val dt: Long
)

/**
 * Wrapper for the Air Quality Index (AQI) score.
 */
data class AirQualityMain(
    val aqi: Int
)

/**
 * Detailed concentrations of individual pollutants in μg/m³.
 */
data class AirQualityComponents(
    val co: Double?,
    val no: Double?,
    val no2: Double?,
    val o3: Double?,
    val so2: Double?,
    val pm2_5: Double?,
    val pm10: Double?,
    val nh3: Double?
)