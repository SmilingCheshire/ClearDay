package com.example.clearday.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clearday.network.model.AirQualityComponents
import com.example.clearday.network.model.AirQualityItem
import com.example.clearday.viewmodel.AirQualityUiState
import com.example.clearday.viewmodel.AirQualityViewModel
import com.google.android.gms.location.LocationServices
import java.text.SimpleDateFormat
import java.util.*

/**
 * Composable screen that displays air quality information based on the user's current location.
 * Handles location permissions, loading states, and detailed pollutant breakdowns.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirQualityScreen(
    viewModel: AirQualityViewModel
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState
    val currentLocation by viewModel.currentLocation

    var locationPermissionGranted by remember { mutableStateOf(false) }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        locationPermissionGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        if (locationPermissionGranted) {
            requestLocation(context, viewModel)
        }
    }

    LaunchedEffect(Unit) {
        locationPermissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Air Quality Index") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is AirQualityUiState.Idle -> CenteredMessage("Waiting for location...")
                is AirQualityUiState.Loading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                is AirQualityUiState.Success -> AirQualityContent(
                    data = state.data.list.firstOrNull(),
                    location = currentLocation
                )

                is AirQualityUiState.Error -> ErrorContent(
                    message = state.message,
                    onRetry = { viewModel.retry() }
                )
            }
        }
    }
}

/**
 * Triggers a location request using FusedLocationProviderClient.
 */
@SuppressLint("MissingPermission")
private fun requestLocation(context: android.content.Context, viewModel: AirQualityViewModel) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        location?.let { viewModel.updateLocation(it) }
    }
}

/**
 * Main scrollable content displayed when air quality data is successfully fetched.
 */
@Composable
private fun AirQualityContent(
    data: AirQualityItem?,
    location: Location?
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        location?.let {
            LocationCard(latitude = it.latitude, longitude = it.longitude)
        }

        data?.let { airQuality ->
            MainAqiCard(aqi = airQuality.main.aqi, timestamp = airQuality.dt)
            airQuality.components?.let { ComponentsCard(components = it) }
            HealthRecommendationsCard(aqi = airQuality.main.aqi)
        }
    }
}

/**
 * Card displaying coordinates of the current data source.
 */
@Composable
private fun LocationCard(latitude: Double, longitude: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Location", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Latitude: %.4f°".format(latitude), style = MaterialTheme.typography.bodyMedium)
            Text("Longitude: %.4f°".format(longitude), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/**
 * High-visibility card showing the AQI numerical value and its qualitative category.
 */
@Composable
private fun MainAqiCard(aqi: Int, timestamp: Long) {
    val aqiInfo = getAqiInfo(aqi)
    val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
    val date = Date(timestamp * 1000)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = aqiInfo.backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Air Quality Index", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = aqi.toString(),
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.Bold,
                fontSize = 72.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(aqiInfo.category, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Updated: ${dateFormat.format(date)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Detailed breakdown of individual chemical pollutants in the atmosphere.
 */
@Composable
private fun ComponentsCard(components: AirQualityComponents) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Air Quality Components", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            components.pm2_5?.let { ComponentRow("PM2.5", it, "μg/m³", "Fine particles") }
            components.pm10?.let { ComponentRow("PM10", it, "μg/m³", "Coarse particles") }
            components.o3?.let { ComponentRow("O₃", it, "μg/m³", "Ozone") }
            components.no2?.let { ComponentRow("NO₂", it, "μg/m³", "Nitrogen dioxide") }
            components.so2?.let { ComponentRow("SO₂", it, "μg/m³", "Sulphur dioxide") }
            components.co?.let { ComponentRow("CO", it, "μg/m³", "Carbon monoxide") }
        }
    }
}

@Composable
private fun ComponentRow(name: String, value: Double, unit: String, description: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text("%.1f %s".format(value, unit), style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
    }
    HorizontalDivider()
}

/**
 * Card providing health advice tailored to the current AQI level.
 */
@Composable
private fun HealthRecommendationsCard(aqi: Int) {
    val recommendations = getHealthRecommendations(aqi)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Health Recommendations", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            recommendations.forEach { recommendation ->
                Row(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text("• ", style = MaterialTheme.typography.bodyMedium)
                    Text(recommendation, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

data class AqiInfo(val category: String, val backgroundColor: Color)

private fun getAqiInfo(aqi: Int): AqiInfo {
    return when (aqi) {
        1 -> AqiInfo("Good", Color(0xFF81C784))
        2 -> AqiInfo("Fair", Color(0xFFFFD54F))
        3 -> AqiInfo("Moderate", Color(0xFFFFB74D))
        4 -> AqiInfo("Poor", Color(0xFFE57373))
        5 -> AqiInfo("Very Poor", Color(0xFFEF5350))
        else -> AqiInfo("Unknown", Color(0xFFBDBDBD))
    }
}

private fun getHealthRecommendations(aqi: Int): List<String> {
    return when (aqi) {
        1 -> listOf("Air quality is satisfactory", "Enjoy your outdoor activities")
        2 -> listOf("Air quality is acceptable", "Sensitive individuals should consider limiting exposure")
        3 -> listOf("Sensitive groups may experience health effects", "Consider reducing prolonged outdoor exertion")
        4 -> listOf("Everyone may begin to experience health effects", "Keep windows closed", "Use air purifiers")
        5 -> listOf("Health alert: avoid outdoor activities", "Wear a mask if you must go outside", "Keep windows and doors closed")
        else -> listOf("Data unavailable")
    }
}

@Composable
private fun CenteredMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(message, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Error", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("Try Again") }
    }
}