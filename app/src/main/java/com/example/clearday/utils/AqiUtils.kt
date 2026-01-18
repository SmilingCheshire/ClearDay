package com.example.clearday.utils

import kotlin.math.roundToInt

object AqiUtils {

    // Main function to get the score (0-500)
    fun calculateAQI(pm25: Double, pm10: Double): Int {
        // We calculate both and take the worst (highest) one
        val aqi25 = calculateIndivAQI(pm25, 12.0, 35.4, 55.4, 150.4, 250.4, 350.4, 500.4)
        val aqi10 = calculateIndivAQI(pm10, 54.0, 154.0, 254.0, 354.0, 424.0, 504.0, 604.0)
        return maxOf(aqi25, aqi10)
    }

    // Standard EPA Linear Interpolation Formula
    private fun calculateIndivAQI(sensorVal: Double, c1: Double, c2: Double, c3: Double, c4: Double, c5: Double, c6: Double, c7: Double): Int {
        return when {
            sensorVal <= c1 -> linear(50.0, 0.0, c1, 0.0, sensorVal)
            sensorVal <= c2 -> linear(100.0, 51.0, c2, c1 + 0.1, sensorVal)
            sensorVal <= c3 -> linear(150.0, 101.0, c3, c2 + 0.1, sensorVal)
            sensorVal <= c4 -> linear(200.0, 151.0, c4, c3 + 0.1, sensorVal)
            sensorVal <= c5 -> linear(300.0, 201.0, c5, c4 + 0.1, sensorVal)
            sensorVal <= c6 -> linear(400.0, 301.0, c6, c5 + 0.1, sensorVal)
            else -> 500
        }
    }

    private fun linear(ih: Double, il: Double, ch: Double, cl: Double, c: Double): Int {
        return (((ih - il) / (ch - cl)) * (c - cl) + il).roundToInt()
    }

    // Your Custom Labels
    fun getAqiLabel(aqi: Int): String {
        return when (aqi) {
            in 0..50 -> "Good"
            in 51..100 -> "Moderate"
            in 101..150 -> "Unhealthy but ok" // Custom label
            in 151..200 -> "Unhealthy"
            in 201..300 -> "Very Unhealthy"
            else -> "Dangerous"
        }
    }

    // Helper to get color for the UI
    fun getAqiColor(aqi: Int): Int {
        return when (aqi) {
            in 0..50 -> 0xFF4CAF50.toInt()   // Green
            in 51..100 -> 0xFFFFEB3B.toInt() // Yellow
            in 101..150 -> 0xFFFF9800.toInt() // Orange
            in 151..200 -> 0xFFF44336.toInt() // Red
            in 201..300 -> 0xFF9C27B0.toInt() // Purple
            else -> 0xFF880E4F.toInt()        // Maroon
        }
    }
}