package com.example.clearday.models

import com.google.gson.annotations.SerializedName

/**
 * Root response object from the World Air Quality Index (WAQI) API.
 */
data class WaqiResponse(
    val status: String,
    val data: WaqiData
)

/**
 * Main data container for air quality information.
 */
data class WaqiData(
    val aqi: Int,
    val idx: Int,
    val iaqi: Iaqi?
)

/**
 * Individual pollutant levels (pm25, pm10, no2, o3) measured by the station.
 */
data class Iaqi(
    val pm25: PollutantValue?,
    val pm10: PollutantValue?,
    val no2: PollutantValue?,
    val o3: PollutantValue?
)

/**
 * Wrapper for raw concentration values of specific pollutants.
 */
data class PollutantValue(
    val v: Double
)