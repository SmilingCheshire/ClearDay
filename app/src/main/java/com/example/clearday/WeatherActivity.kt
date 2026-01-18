package com.example.clearday

import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.clearday.network.WaqiService
import com.example.clearday.network.WeatherService
import com.example.clearday.repository.WeatherRepository
import com.example.clearday.services.FirestoreService // Import this
import com.example.clearday.utils.AqiUtils
import com.google.firebase.auth.FirebaseAuth // Import this
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs

class WeatherActivity : AppCompatActivity() {

    // --- Dependencies ---
    private val weatherRetrofit = Retrofit.Builder()
        .baseUrl("https://api.openweathermap.org/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val weatherService = weatherRetrofit.create(WeatherService::class.java)

    private val waqiRetrofit = Retrofit.Builder()
        .baseUrl("https://api.waqi.info/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    private val waqiService = waqiRetrofit.create(WaqiService::class.java)

    private val weatherRepository = WeatherRepository(weatherService, waqiService)

    // --- NEW: Firestore & Auth to save history ---
    private val firestoreService = FirestoreService()
    private val auth = FirebaseAuth.getInstance()

    // --- UI Elements ---
    private lateinit var tvLocation: TextView
    private lateinit var tvTemp: TextView
    private lateinit var tvDesc: TextView
    private lateinit var tvFeelsLike: TextView
    private lateinit var tvAqiStatus: TextView
    private lateinit var tvPm25: TextView
    private lateinit var tvPm10: TextView
    private lateinit var tvNo2: TextView
    private lateinit var tvO3: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvWind: TextView

    private var currentLat: Double = 0.0
    private var currentLon: Double = 0.0

    private lateinit var gestureDetector: GestureDetector

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        gestureDetector = GestureDetector(this, SwipeBackListener())

        // Initialize Views
        tvLocation = findViewById(R.id.tvLocationName)
        tvTemp = findViewById(R.id.tvTemperature)
        tvDesc = findViewById(R.id.tvDescription)
        tvFeelsLike = findViewById(R.id.tvFeelsLike)
        tvAqiStatus = findViewById(R.id.tvAqiStatus)
        tvPm25 = findViewById(R.id.tvPm25)
        tvPm10 = findViewById(R.id.tvPm10)
        tvNo2 = findViewById(R.id.tvNo2)
        tvO3 = findViewById(R.id.tvO3)
        tvHumidity = findViewById(R.id.tvHumidity)
        tvWind = findViewById(R.id.tvWind)

        currentLat = intent.getDoubleExtra("LAT", 51.1079)
        currentLon = intent.getDoubleExtra("LON", 17.0385)

        findViewById<android.view.View>(R.id.locationContainer).setOnClickListener {
            showChangeLocationDialog()
        }

        loadWeatherData(currentLat, currentLon)
    }

    // ... (dispatchTouchEvent and SwipeBackListener remain the same) ...
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null && gestureDetector.onTouchEvent(ev)) return true
        return super.dispatchTouchEvent(ev)
    }

    private inner class SwipeBackListener : GestureDetector.SimpleOnGestureListener() {
        // ... existing swipe code ...
        override fun onDown(e: MotionEvent): Boolean = false
        override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
            if (e1 == null) return false
            if (abs(e2.x - e1.x) > 100 && abs(vX) > 100) {
                finish()
                return true
            }
            return false
        }
    }

    private fun loadWeatherData(lat: Double, lon: Double) {
        // 1. Geocoder Logic (Same as before)
        try {
            val geocoder = Geocoder(this, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(lat, lon, 1)
            if (!addresses.isNullOrEmpty()) {
                tvLocation.text = addresses[0].locality ?: addresses[0].adminArea ?: "Unknown"
            }
        } catch (e: Exception) { tvLocation.text = "Lat: $lat, Lon: $lon" }

        lifecycleScope.launch {
            // 2. Weather UI Update (Same as before)
            val wRes = weatherRepository.getCurrentWeather(lat, lon)
            wRes.getOrNull()?.let { w ->
                tvTemp.text = "${w.main.temp.toInt()}°"
                tvFeelsLike.text = "Feels like ${w.main.temp.toInt()}°"
                tvDesc.text = w.weather.firstOrNull()?.description ?: "-"
                tvHumidity.text = "${w.main.humidity}%"
            }

            // 3. Air Quality UI Update AND SAVE LOGIC
            val aRes = weatherRepository.getAirQuality(lat, lon)
            aRes.getOrNull()?.let { response ->
                val data = response.data
                val score = data.aqi

                // UI Updates
                val label = AqiUtils.getAqiLabel(score)
                val color = AqiUtils.getAqiColor(score)

                tvAqiStatus.text = "$score ($label)"
                tvAqiStatus.setTextColor(color)

                val pm25 = data.iaqi?.pm25?.v ?: 0.0
                val pm10 = data.iaqi?.pm10?.v ?: 0.0
                val no2 = data.iaqi?.no2?.v ?: 0.0
                val o3 = data.iaqi?.o3?.v ?: 0.0

                tvPm25.text = "PM2.5: $pm25"
                tvPm10.text = "PM10: $pm10"
                tvNo2.text = "NO2: $no2"
                tvO3.text = "O3: $o3"

                // --- NEW: SAVE TO FIRESTORE ---
                saveAirQualityToHistory(response)
            }
        }
    }

    private fun saveAirQualityToHistory(aqiResponse: Any) {
        val uid = auth.currentUser?.uid
        if (uid != null) {
            val today = LocalDate.now().toString() // "yyyy-MM-dd"

            // This saves the data to: users/{uid}/daily_logs/{today}
            // Under the key: "airQuality"
            firestoreService.updateDailyLog(uid, today, "airQuality", aqiResponse) {
                Log.d("WeatherActivity", "Air Quality saved to history for $today")
            }
        }
    }

    // ... (showChangeLocationDialog and searchLocation remain the same) ...
    private fun showChangeLocationDialog() {
        val input = EditText(this)
        input.hint = "Enter city name"
        AlertDialog.Builder(this)
            .setTitle("Change Location")
            .setView(input)
            .setPositiveButton("Search") { _, _ -> searchLocation(input.text.toString()) }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun searchLocation(city: String) {
        lifecycleScope.launch {
            try {
                @Suppress("DEPRECATION")
                val geocoder = Geocoder(this@WeatherActivity, Locale.getDefault())
                val addresses = geocoder.getFromLocationName(city, 1)
                if (!addresses.isNullOrEmpty()) {
                    val loc = addresses[0]
                    currentLat = loc.latitude
                    currentLon = loc.longitude
                    loadWeatherData(currentLat, currentLon)
                } else {
                    Toast.makeText(this@WeatherActivity, "City not found", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Toast.makeText(this@WeatherActivity, "Error", Toast.LENGTH_SHORT).show()
            }
        }
    }
}