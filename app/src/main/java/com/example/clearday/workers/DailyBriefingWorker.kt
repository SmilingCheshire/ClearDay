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

class DailyBriefingWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // 1. Setup OpenWeather (For Temp/Forecast)
            val weatherRetrofit = Retrofit.Builder()
                .baseUrl("https://api.openweathermap.org/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val weatherService = weatherRetrofit.create(WeatherService::class.java)

            // 2. Setup WAQI (For Air Quality)
            val waqiRetrofit = Retrofit.Builder()
                .baseUrl("https://api.waqi.info/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val waqiService = waqiRetrofit.create(WaqiService::class.java)

            // 3. Init Repository with BOTH
            val repo = WeatherRepository(weatherService, waqiService)

            // 4. Get Location
            val location = getLastKnownLocation() ?: return Result.retry()

            // 5. Fetch Forecast (For Coldest Temp)
            val forecastRes = repo.getForecast(location.latitude, location.longitude)
            val coldestTemp = if (forecastRes.isSuccess) {
                calculateColdestDayTemp(forecastRes.getOrThrow().list)
            } else {
                // Fallback to current weather if forecast fails
                repo.getCurrentWeather(location.latitude, location.longitude).getOrNull()?.main?.temp ?: 0.0
            }

            // 6. Fetch AQI (Using WAQI Logic)
            val aqiRes = repo.getAirQuality(location.latitude, location.longitude)
            var aqiString = "Unknown"

            if (aqiRes.isSuccess) {
                val data = aqiRes.getOrThrow().data

                // Direct Score from API (No Math needed)
                val score = data.aqi
                val label = AqiUtils.getAqiLabel(score)
                aqiString = "$score ($label)"
            }

            // 7. Send Notification
            sendNotification(coldestTemp, aqiString)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }

    private fun calculateColdestDayTemp(list: List<WeatherService.ForecastItem>): Double {
        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)

        // Filter: Today ONLY, between 07:00 and 20:00
        val dayList = list.filter {
            val date = java.util.Date(it.dt * 1000)
            calendar.time = date
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val day = calendar.get(Calendar.DAY_OF_YEAR)

            day == currentDay && hour in 7..20
        }

        if (dayList.isEmpty()) return 0.0

        // Find minimum temp
        return dayList.minOf { it.main.temp }
    }

    private suspend fun getLastKnownLocation(): Location? {
        val client = LocationServices.getFusedLocationProviderClient(applicationContext)
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return null
        }
        return client.lastLocation.await()
    }

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