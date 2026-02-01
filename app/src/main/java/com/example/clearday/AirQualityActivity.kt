package com.example.clearday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import com.example.clearday.network.WeatherApiService
import com.example.clearday.repository.AirQualityRepository
import com.example.clearday.ui.screen.AirQualityScreen
import com.example.clearday.viewmodel.AirQualityViewModel
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Entry point for the Air Quality module.
 * Responsibilities include manual dependency injection of the network stack,
 * repositories, and the ViewModel, and hosting the AirQualityScreen.
 */
class AirQualityActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Network layer configuration
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val apiService = retrofit.create(WeatherApiService::class.java)

        // Data and Logic layer initialization
        val repository = AirQualityRepository(apiService)
        val viewModel = AirQualityViewModel(repository)

        setContent {
            MaterialTheme {
                AirQualityScreen(viewModel = viewModel)
            }
        }
    }
}