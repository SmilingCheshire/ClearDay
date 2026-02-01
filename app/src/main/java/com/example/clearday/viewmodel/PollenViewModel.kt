package com.example.clearday.viewmodel

import android.location.Location
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clearday.network.model.PollenForecastResponse
import com.example.clearday.repository.PollenRepository
import kotlinx.coroutines.launch

sealed class PollenUiState {
    object Idle : PollenUiState()
    object Loading : PollenUiState()
    data class Success(val data: PollenForecastResponse) : PollenUiState()
    data class Error(val message: String) : PollenUiState()
}

class PollenViewModel : ViewModel() {
    
    private val repository = PollenRepository()
    
    private val _uiState = mutableStateOf<PollenUiState>(PollenUiState.Idle)
    val uiState: State<PollenUiState> = _uiState
    
    private val _currentLocation = mutableStateOf<Location?>(null)
    val currentLocation: State<Location?> = _currentLocation
    
    fun updateLocation(location: Location) {
        _currentLocation.value = location
        fetchPollenData(location.latitude, location.longitude)
    }
    
    fun fetchPollenData(latitude: Double, longitude: Double, days: Int = 5) {
        viewModelScope.launch {
            _uiState.value = PollenUiState.Loading
            
            val result = repository.getPollenForecastWithPlants(
                latitude = latitude,
                longitude = longitude,
                days = days
            )
            
            _uiState.value = if (result.isSuccess) {
                PollenUiState.Success(result.getOrThrow())
            } else {
                PollenUiState.Error(
                    result.exceptionOrNull()?.message ?: "Unknown error occurred"
                )
            }
        }
    }
    
    fun retry() {
        _currentLocation.value?.let { location ->
            fetchPollenData(location.latitude, location.longitude)
        }
    }
}
