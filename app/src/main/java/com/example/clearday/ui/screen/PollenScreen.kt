package com.example.clearday.ui.screen

import android.Manifest
import android.annotation.SuppressLint
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clearday.location.LocationTracker
import com.example.clearday.network.model.DailyInfo
import com.example.clearday.network.model.PollenTypeInfo
import com.example.clearday.viewmodel.PollenUiState
import com.example.clearday.viewmodel.PollenViewModel
import com.google.android.gms.location.LocationServices

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollenScreen(
    viewModel: PollenViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState
    val currentLocation by viewModel.currentLocation
    
    var locationPermissionGranted by remember { mutableStateOf(false) }
    
    // Location permission launcher
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
                title = { Text("Pollen Forecast") },
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
                is PollenUiState.Idle -> {
                    CenteredMessage("Waiting for location...")
                }
                
                is PollenUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                
                is PollenUiState.Success -> {
                    PollenContent(
                        data = state.data.dailyInfo ?: emptyList(),
                        location = currentLocation
                    )
                }
                
                is PollenUiState.Error -> {
                    ErrorContent(
                        message = state.message,
                        onRetry = { viewModel.retry() }
                    )
                }
            }
        }
    }
}

@SuppressLint("MissingPermission")
private fun requestLocation(context: android.content.Context, viewModel: PollenViewModel) {
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
        location?.let {
            viewModel.updateLocation(it)
        }
    }
}

@Composable
private fun PollenContent(
    data: List<DailyInfo>,
    location: Location?
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Location info
        location?.let {
            item {
                LocationCard(latitude = it.latitude, longitude = it.longitude)
            }
        }
        
        // Daily pollen info
        items(data) { dailyInfo ->
            DailyPollenCard(dailyInfo = dailyInfo)
        }
    }
}

@Composable
private fun LocationCard(latitude: Double, longitude: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Location",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Width: %.4f°".format(latitude),
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Length: %.4f°".format(longitude),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
private fun DailyPollenCard(dailyInfo: DailyInfo) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Date header
            Text(
                text = "${dailyInfo.date.day}.${dailyInfo.date.month}.${dailyInfo.date.year}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Pollen types
            dailyInfo.pollenTypeInfo?.forEach { pollenType ->
                PollenTypeItem(pollenType = pollenType)
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Plant info if available
            dailyInfo.plantInfo?.takeIf { it.isNotEmpty() }?.let { plants ->
                Spacer(modifier = Modifier.height(8.dp))
                Divider()
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Plants:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                plants.forEach { plant ->
                    Text(
                        text = "• ${plant.displayName} (${plant.indexInfo?.value ?: "N/A"})",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
private fun PollenTypeItem(pollenType: PollenTypeInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = getPollenCategoryColor(pollenType.indexInfo?.category),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = pollenType.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = pollenType.indexInfo?.category ?: "N/A",
                style = MaterialTheme.typography.bodyMedium
            )
            if (pollenType.inSeason == true) {
                Text(
                    text = "In season",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        // Index value
        Text(
            text = pollenType.indexInfo?.value?.toString() ?: "N/A",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
    
    // Health recommendations
    pollenType.healthRecommendations?.takeIf { it.isNotEmpty() }?.let { recommendations ->
        Spacer(modifier = Modifier.height(4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
        ) {
            Text(
                text = "Recommendations:",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
            recommendations.forEach { recommendation ->
                Text(
                    text = "• $recommendation",
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun getPollenCategoryColor(category: String?): Color {
    return when (category?.uppercase()) {
        "NONE" -> Color(0xFFE8F5E9)
        "LOW" -> Color(0xFFFFF9C4)
        "MODERATE" -> Color(0xFFFFE082)
        "HIGH" -> Color(0xFFFFAB91)
        "VERY_HIGH" -> Color(0xFFEF5350)
        else -> Color(0xFFEEEEEE)
    }
}

@Composable
private fun CenteredMessage(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Error",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}
