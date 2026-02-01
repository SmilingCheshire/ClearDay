package com.example.clearday.viewmodel

import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clearday.network.model.AirQualityResponse
import com.example.clearday.repository.AirQualityRepository
import kotlinx.coroutines.launch

/**
 * UI State representing the different phases of fetching air quality data.
 */
sealed class AirQualityUiState {
    object Idle : AirQualityUiState()
    object Loading : AirQualityUiState()
    data class Success(val data: AirQualityResponse) : AirQualityUiState()
    data class Error(val message: String) : AirQualityUiState()
}

class AirQualityViewModel(private val repository: AirQualityRepository) : ViewModel() {

    private val _uiState = mutableStateOf<AirQualityUiState>(AirQualityUiState.Idle)
    val uiState: State<AirQualityUiState> = _uiState

    private val _currentLocation = mutableStateOf<Location?>(null)
    val currentLocation: State<Location?> = _currentLocation

    fun updateLocation(location: Location) {
        _currentLocation.value = location
        fetchAirQuality(location.latitude, location.longitude)
    }

    fun fetchAirQuality(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _uiState.value = AirQualityUiState.Loading

            // MOCK: Using coordinates for testing
            val mockLatitude = 52.2297  // Warsaw
            val mockLongitude = 21.0122

            val result = repository.getAirQuality(
                latitude = mockLatitude,
                longitude = mockLongitude
            )

            _uiState.value = if (result.isSuccess) {
                AirQualityUiState.Success(result.getOrThrow())
            } else {
                AirQualityUiState.Error(
                    result.exceptionOrNull()?.message ?: "Unknown error occurred"
                )
            }
        }
    }

    fun retry() {
        _currentLocation.value?.let { location ->
            fetchAirQuality(location.latitude, location.longitude)
        }
    }
}