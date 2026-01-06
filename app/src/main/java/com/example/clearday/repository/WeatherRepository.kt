//package com.example.clearday.repository
//
//
//import com.example.clearday.BuildConfig
//import com.example.clearday.network.ApiClient
//import com.example.clearday.network.WeatherApiService
//import com.example.clearday.network.model.AirQualityResponse
//import com.example.clearday.network.model.WeatherResponse
//
//class WeatherRepository {
//
//    private val api: WeatherApiService =
//        ApiClient.retrofit.create(WeatherApiService::class.java)
//
//    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
//        return api.getCurrentWeather(
//            lat = lat,
//            lon = lon,
//            apiKey = BuildConfig.OPENWEATHER_API_KEY
//        )
//    }
//
//    suspend fun getAirQuality(lat: Double, lon: Double): AirQualityResponse {
//        return api.getAirQuality(
//            lat = lat,
//            lon = lon,
//            apiKey = BuildConfig.OPENWEATHER_API_KEY
//        )
//    }
//}