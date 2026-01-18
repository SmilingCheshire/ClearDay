package com.example.clearday.models

import com.google.gson.annotations.SerializedName

data class WaqiResponse(
    val status: String, // "ok" or "error"
    val data: WaqiData
)

data class WaqiData(
    val aqi: Int, // This is the number you want! (e.g. 158)
    val idx: Int,
    val iaqi: Iaqi? // Individual pollutants (pm25, pm10, etc.)
)

data class Iaqi(
    val pm25: PollutantValue?,
    val pm10: PollutantValue?,
    val no2: PollutantValue?,
    val o3: PollutantValue?
)

data class PollutantValue(
    val v: Double // The raw concentration value
)