package com.example.clearday.repository

import com.example.clearday.models.*
import com.example.clearday.network.WeatherService
import com.example.clearday.network.WaqiService
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations

class WeatherRepositoryTest {

    @Mock
    private lateinit var weatherApi: WeatherService

    @Mock
    private lateinit var waqiApi: WaqiService

    private lateinit var repository: WeatherRepository

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        repository = WeatherRepository(weatherApi, waqiApi)
    }

    @Test
    fun `getCurrentWeather returns success when API call succeeds`() = runTest {
        // Given
        val mockResponse = CurrentWeatherResponse(
            main = MainStats(temp = 20.5, humidity = 60),
            weather = listOf(WeatherDescription("Clear sky", "01d")),
            name = "Warsaw"
        )
        `when`(weatherApi.getCurrentWeather(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenReturn(mockResponse)

        // When
        val result = repository.getCurrentWeather(52.0, 21.0)

        // Then
        assertTrue(result.isSuccess)
        val temp = result.getOrNull()?.main?.temp ?: 0.0
        assertEquals(20.5, temp, 0.01)
        assertEquals("Warsaw", result.getOrNull()?.name)
    }

    @Test
    fun `getCurrentWeather returns failure when API throws exception`() = runTest {
        // Given
        `when`(weatherApi.getCurrentWeather(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenThrow(RuntimeException("Network error"))

        // When
        val result = repository.getCurrentWeather(52.0, 21.0)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("Network error") == true)
    }

    @Test
    fun `getAirQuality returns success when WAQI API responds with ok status`() = runTest {
        // Given
        val mockResponse = WaqiResponse(
            status = "ok",
            data = WaqiData(
                aqi = 75,
                idx = 12345,
                iaqi = Iaqi(
                    pm25 = PollutantValue(25.0),
                    pm10 = PollutantValue(40.0),
                    no2 = null,
                    o3 = null
                )
            )
        )
        `when`(waqiApi.getAirQuality(anyDouble(), anyDouble(), anyString()))
            .thenReturn(mockResponse)

        // When
        val result = repository.getAirQuality(52.0, 21.0)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(75, result.getOrNull()?.data?.aqi)
        assertEquals("ok", result.getOrNull()?.status)
    }

    @Test
    fun `getAirQuality returns failure when WAQI API status is not ok`() = runTest {
        // Given
        val mockResponse = WaqiResponse(
            status = "error",
            data = WaqiData(
                aqi = -1,
                idx = 0,
                iaqi = null
            )
        )
        `when`(waqiApi.getAirQuality(anyDouble(), anyDouble(), anyString()))
            .thenReturn(mockResponse)

        // When
        val result = repository.getAirQuality(52.0, 21.0)

        // Then
        assertTrue(result.isFailure)
        assertTrue(result.exceptionOrNull()?.message?.contains("API Error") == true)
    }

    @Test
    fun `getForecast returns success when API call succeeds`() = runTest {
        // Given
        val mockForecast = WeatherService.ForecastResponse(
            list = listOf(
                WeatherService.ForecastItem(
                    dt = 1234567890,
                    main = MainStats(temp = 22.0, humidity = 50),
                    dt_txt = "2026-02-01 12:00:00"
                )
            )
        )
        `when`(weatherApi.getForecast(anyDouble(), anyDouble(), anyString(), anyString()))
            .thenReturn(mockForecast)

        // When
        val result = repository.getForecast(52.0, 21.0)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.list?.size)
    }
}
