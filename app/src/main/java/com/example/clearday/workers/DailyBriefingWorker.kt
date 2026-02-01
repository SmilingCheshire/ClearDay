package com.example.clearday.workers

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.clearday.network.WaqiService
import com.example.clearday.network.WeatherService
import com.example.clearday.repository.WeatherRepository
import com.example.clearday.utils.AqiUtils
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Calendar

/**
 * Background worker responsible for generating and sending a morning briefing notification.
 * It consolidates weather forecast (min temperature during day hours) and air quality data.
 */
class DailyBriefingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Manual Retrofit setup for background execution context
            val weatherRetrofit = Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val weatherService = weatherRetrofit.create(WeatherService::class.java)

            val waqiRetrofit = Retrofit.Builder()
                .baseUrl("https://api.waqi.info/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val waqiService = waqiRetrofit.create(WaqiService::class.java)

            val repo = WeatherRepository(weatherService, waqiService)

            val location = getLastKnownLocation() ?: return Result.retry()

            val forecastRes = repo.getForecast(location.latitude, location.longitude)
            val coldestTemp = if (forecastRes.isSuccess) {
                calculateColdestDayTemp(forecastRes.getOrThrow().list)
            } else {
                repo.getCurrentWeather(location.latitude, location.longitude).getOrNull()?.main?.temp ?: 0.0
            }

            val aqiRes = repo.getAirQuality(location.latitude, location.longitude)
            var aqiString = "Unknown"

            if (aqiRes.isSuccess) {
                val data = aqiRes.getOrThrow().data
                val score = data.aqi
                val label = AqiUtils.getAqiLabel(score)
                aqiString = "$score ($label)"
            }

            sendNotification(coldestTemp, aqiString)
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    /**
     * Filters the 5-day forecast to find the minimum temperature for today
     * between 07:00 and 20:00.
     */
    private fun calculateColdestDayTemp(list: List<WeatherService.ForecastItem>): Double {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)

        val dayList = list.filter {
            val date = java.util.Date(it.dt * 1000)
            calendar.time = date
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val day = calendar.get(Calendar.DAY_OF_YEAR)

            day == currentDay && hour in 7..20
        }

        if (dayList.isEmpty()) return 0.0
        return dayList.minOf { it.main.temp }
    }

    /**
     * Retrieves the last known location using FusedLocationProviderClient.
     * Requires ACCESS_FINE_LOCATION permission.
     */
    private suspend fun getLastKnownLocation(): Location? {
        val client = LocationServices.getFusedLocationProviderClient(applicationContext)
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        return client.lastLocation.await()
    }

    /**
     * Builds and displays the system notification with the briefing summary.
     */
    private fun sendNotification(temp: Double, aqi: String) {
        val channelId = "daily_briefing_channel"
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Daily Briefing", NotificationManager.IMPORTANCE_HIGH)
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_day)
            .setContentTitle("Morning Briefing \uD83C\uDF1E")
            .setStyle(NotificationCompat.BigTextStyle().bigText(
                "Coldest today: ${temp.toInt()}°C\n" +
                        "Air Quality: $aqi\n" +
                        "Pollen: Check app for details"
            ))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        manager.notify(1001, notification)
    }
}