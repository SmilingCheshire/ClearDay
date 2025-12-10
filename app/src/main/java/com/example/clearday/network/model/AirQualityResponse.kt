package com.example.clearday.network.model


data class AirQualityResponse(
    val list: List<AirQualityItem>
)

data class AirQualityItem(
    val main: AirQualityMain
)

data class AirQualityMain(
    // 1 = Good, 2 = Fair, 3 = Moderate, 4 = Poor, 5 = Very Poor
    val aqi: Int
)