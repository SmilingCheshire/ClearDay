package com.example.clearday.utils

import org.junit.Assert.*
import org.junit.Test

class AqiUtilsTest {

    @Test
    fun `calculateAQI returns Good for low pollutant values`() {
        val aqi = AqiUtils.calculateAQI(pm25 = 5.0, pm10 = 20.0)
        val label = AqiUtils.getAqiLabel(aqi)
        assertEquals("Good", label)
    }

    @Test
    fun `calculateAQI returns Moderate when pm10 is moderate`() {
        val aqi = AqiUtils.calculateAQI(pm25 = 5.0, pm10 = 100.0)
        val label = AqiUtils.getAqiLabel(aqi)
        assertEquals("Moderate", label)
    }

    @Test
    fun `calculateAQI returns Unhealthy but ok when pm25 around 40`() {
        val aqi = AqiUtils.calculateAQI(pm25 = 40.0, pm10 = 20.0)
        val label = AqiUtils.getAqiLabel(aqi)
        assertEquals("Unhealthy but ok", label)
    }

    @Test
    fun `getAqiColor returns consistent color for ranges`() {
        assertEquals(0xFF4CAF50.toInt(), AqiUtils.getAqiColor(25))  // Good
        assertEquals(0xFFFFEB3B.toInt(), AqiUtils.getAqiColor(75))  // Moderate
        assertEquals(0xFFFF9800.toInt(), AqiUtils.getAqiColor(125)) // Unhealthy but ok
        assertEquals(0xFFF44336.toInt(), AqiUtils.getAqiColor(175)) // Unhealthy
        assertEquals(0xFF9C27B0.toInt(), AqiUtils.getAqiColor(250)) // Very Unhealthy
        assertEquals(0xFF880E4F.toInt(), AqiUtils.getAqiColor(400)) // Dangerous
    }
}
