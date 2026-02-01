package com.example.clearday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clearday.services.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import java.time.YearMonth
import java.time.format.DateTimeFormatter

/**
 * Activity hosting the history calendar where users can visualize the relationship
 * between air quality (borders) and symptom severity (background colors).
 */
class CalendarActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    CalendarScreen(onBack = { finish() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(onBack: () -> Unit) {
    val firestoreService = remember { FirestoreService() }
    val auth = remember { FirebaseAuth.getInstance() }

    var currentYearMonth by remember { mutableStateOf(YearMonth.now()) }
    var monthlyData by remember { mutableStateOf<Map<String, Map<String, Any>>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(false) }

    var showDialog by remember { mutableStateOf(false) }
    var selectedDateStr by remember { mutableStateOf("") }
    var selectedDayData by remember { mutableStateOf<Map<String, Any>?>(null) }

    LaunchedEffect(currentYearMonth) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            isLoading = true
            val dateStr = currentYearMonth.format(DateTimeFormatter.ofPattern("yyyy-MM"))
            firestoreService.getMonthLogs(uid, dateStr) { data ->
                monthlyData = data ?: emptyMap()
                isLoading = false
            }
        }
    }

    if (showDialog) {
        DayDetailsDialog(
            date = selectedDateStr,
            data = selectedDayData,
            onDismiss = { showDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Symptom & Pollen History") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentYearMonth = currentYearMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ArrowBack, "Prev")
                }
                Text(
                    text = currentYearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { currentYearMonth = currentYearMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ArrowForward, "Next")
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                    Text(it, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                CalendarGrid(
                    yearMonth = currentYearMonth,
                    data = monthlyData,
                    onDayClick = { date, data ->
                        selectedDateStr = date
                        selectedDayData = data
                        showDialog = true
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Legend()
        }
    }
}

/**
 * Renders the grid of days for a specific month, handling offsets for the first day of the week.
 */
@Composable
fun CalendarGrid(
    yearMonth: YearMonth,
    data: Map<String, Map<String, Any>>,
    onDayClick: (String, Map<String, Any>?) -> Unit
) {
    val daysInMonth = yearMonth.lengthOfMonth()
    val firstDayOfMonth = yearMonth.atDay(1).dayOfWeek.value
    val emptyCells = firstDayOfMonth - 1

    LazyVerticalGrid(
        columns = GridCells.Fixed(7),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        items(emptyCells) { Box(modifier = Modifier.aspectRatio(1f)) }
        items(daysInMonth) { dayIndex ->
            val day = dayIndex + 1
            val date = yearMonth.atDay(day)
            val dateKey = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
            val logData = data[dateKey]
            DayCell(day, logData, onClick = { onDayClick(dateKey, logData) })
        }
    }
}

/**
 * Single day cell. Background color represents symptom severity,
 * while the border represents the Air Quality Index (AQI).
 */
@Composable
fun DayCell(day: Int, logData: Map<String, Any>?, onClick: () -> Unit) {
    val backgroundColor = if (logData == null || !logData.containsKey("symptoms")) {
        Color.LightGray.copy(alpha = 0.3f)
    } else {
        calculateSymptomColor(logData)
    }

    val borderColor = if (logData != null && logData.containsKey("airQuality")) {
        val aqi = getAqiValue(logData["airQuality"])
        getAqiColor(aqi)
    } else {
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .background(backgroundColor, shape = RectangleShape)
            .border(BorderStroke(3.dp, borderColor), shape = RectangleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = day.toString(), fontWeight = FontWeight.Bold)
    }
}

/**
 * Detailed view of a selected day, showing breakdown of symptoms and exact AQI metrics.
 */
@Composable
fun DayDetailsDialog(
    date: String,
    data: Map<String, Any>?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } },
        title = { Text(text = "Details for $date") },
        text = {
            if (data == null) {
                Text("No data recorded for this day.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (data.containsKey("airQuality")) {
                        val aqi = getAqiValue(data["airQuality"])
                        Text("Air Quality", fontWeight = FontWeight.Bold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = getAqiColor(aqi), shape = RoundedCornerShape(4.dp), modifier = Modifier.size(24.dp)) {}
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("AQI: $aqi (${getAqiDescription(aqi)})")
                        }
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                    }

                    if (data.containsKey("symptoms") || data.containsKey("generalSeverity")) {
                        Text("How you felt", fontWeight = FontWeight.Bold)
                        val general = (data["generalSeverity"] as? Number)?.toInt() ?: 0
                        Text("General Severity: $general/5")

                        val symptoms = data["symptoms"] as? Map<String, Number>
                        if (!symptoms.isNullOrEmpty()) {
                            Text("Specific Symptoms:", fontSize = 14.sp, modifier = Modifier.padding(top=4.dp))
                            symptoms.forEach { (name, level) ->
                                if (level.toInt() > 0) Text(" • $name: ${level.toInt()}/5", fontSize = 14.sp)
                            }
                        }
                    }
                }
            }
        }
    )
}

// --- Logic Helpers ---

fun getAqiValue(aqiDataObj: Any?): Int {
    return try {
        val aqiMap = aqiDataObj as? Map<String, Any>
        val dataMap = aqiMap?.get("data") as? Map<String, Any>
        (dataMap?.get("aqi") as? Number)?.toInt()
            ?: (aqiMap?.get("aqi") as? Number)?.toInt()
            ?: 0
    } catch (e: Exception) { 0 }
}

fun getAqiColor(aqi: Int): Color = when (aqi) {
    in 0..50 -> Color(0xFF009966)
    in 51..100 -> Color(0xFFFFDE33)
    in 101..150 -> Color(0xFFFF9933)
    in 151..200 -> Color(0xFFCC0033)
    in 201..300 -> Color(0xFF660099)
    else -> Color(0xFF7E0023)
}

fun getAqiDescription(aqi: Int): String = when (aqi) {
    in 0..50 -> "Good"
    in 51..100 -> "Moderate"
    in 101..150 -> "Unhealthy for Sensitive"
    in 151..200 -> "Unhealthy"
    in 201..300 -> "Very Unhealthy"
    else -> "Hazardous"
}

fun calculateSymptomColor(data: Map<String, Any>): Color {
    return try {
        val generalScore = (data["generalSeverity"] as? Number)?.toDouble() ?: 0.0
        val symptoms = data["symptoms"] as? Map<String, Number> ?: emptyMap()
        val specificAvg = if (symptoms.isNotEmpty()) symptoms.values.map { it.toDouble() }.sum() / symptoms.size else 0.0
        val finalScore = if (symptoms.isNotEmpty()) (generalScore + specificAvg) / 2.0 else generalScore

        when {
            finalScore < 1.0 -> Color(0xFF4CAF50)
            finalScore < 2.0 -> Color(0xFF8BC34A)
            finalScore < 3.0 -> Color(0xFFFFEB3B)
            finalScore < 4.0 -> Color(0xFFFF9800)
            else -> Color(0xFFF44336)
        }
    } catch (e: Exception) { Color.Gray }
}

@Composable
fun Legend() {
    Column {
        Text("Legend:", fontWeight = FontWeight.Bold)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(16.dp).background(Color(0xFF4CAF50)))
            Text(" Feels Good", fontSize = 12.sp)
            Spacer(Modifier.width(16.dp))
            Box(Modifier.size(16.dp).background(Color(0xFFF44336)))
            Text(" Severe", fontSize = 12.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
            Box(Modifier.size(16.dp).border(2.dp, Color.Gray))
            Text(" Border color = Air Quality", fontSize = 12.sp)
        }
    }
}