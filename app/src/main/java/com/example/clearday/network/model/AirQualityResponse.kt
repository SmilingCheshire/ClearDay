package com.example.clearday.network.model


data class AirQualityResponse(
    val list: List<AirQualityItem>
)

data class AirQualityItem(
    val main: AirQualityMain,
    val components: AirQualityComponents?,
    val dt: Long
)

data class AirQualityMain(
    // 1 = Good, 2 = Fair, 3 = Moderate, 4 = Poor, 5 = Very Poor
    val aqi: Int
)

data class AirQualityComponents(
    val co: Double?,      // Carbon monoxide μg/m³
    val no: Double?,      // Nitrogen monoxide μg/m³
    val no2: Double?,     // Nitrogen dioxide μg/m³
    val o3: Double?,      // Ozone μg/m³
    val so2: Double?,     // Sulphur dioxide μg/m³
    val pm2_5: Double?,   // Fine particles matter μg/m³
    val pm10: Double?,    // Coarse particulate matter μg/m³
    val nh3: Double?      // Ammonia μg/m³
)