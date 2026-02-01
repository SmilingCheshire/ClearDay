package com.example.clearday

import android.Manifest
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.clearday.models.User
import com.example.clearday.network.WaqiService
import com.example.clearday.network.WeatherService
import com.example.clearday.repository.PollenRepository
import com.example.clearday.repository.WeatherRepository
import com.example.clearday.services.FirestoreService
import com.example.clearday.utils.AqiUtils
import com.example.clearday.workers.DailyBriefingWorker
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

/**
 * Main dashboard activity providing real-time allergy risk and air quality data.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var alertContainer: View
    private lateinit var tvAlertBody: TextView
    private lateinit var tvAllergyRiskLabel: TextView
    private lateinit var tvAllergyScore: TextView
    private lateinit var tvTreeValue: TextView
    private lateinit var pbTree: ProgressBar
    private lateinit var tvTreeTag: TextView
    private lateinit var tvGrassValue: TextView
    private lateinit var pbGrass: ProgressBar
    private lateinit var tvGrassTag: TextView
    private lateinit var tvWeedValue: TextView
    private lateinit var pbWeed: ProgressBar
    private lateinit var tvWeedTag: TextView
    private lateinit var tvAqiValue: TextView
    private lateinit var tvAqiLabel: TextView
    private lateinit var tvAqiAdvice: TextView
    private lateinit var btnOpenPollen: Button
    private lateinit var btnSymptomDiary: Button
    private lateinit var btnAirQuality: Button
    private lateinit var btnSetBriefingTime: Button
    private lateinit var tvBriefingTime: TextView

    private lateinit var gestureDetector: GestureDetector
    private lateinit var swipeRefresh: SwipeRefreshLayout

    private val auth = FirebaseAuth.getInstance()
    private val firestoreService = FirestoreService()
    private val pollenRepository = PollenRepository()

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

    private val scope = CoroutineScope(Dispatchers.Main + Job())

    /**
     * Initializes activity, UI components, and data loading processes.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initViews()

        btnOpenPollen.setOnClickListener { startActivity(Intent(this, PollenActivity::class.java)) }
        btnSymptomDiary.setOnClickListener { startActivity(Intent(this, SymptomDiaryActivity::class.java)) }
        btnAirQuality.setOnClickListener { startActivity(Intent(this, AirQualityActivity::class.java)) }
        btnSetBriefingTime.setOnClickListener { showTimePicker() }

        gestureDetector = GestureDetector(this, SwipeListener())
        swipeRefresh = findViewById(R.id.swipeRefresh)
        setupManualRefresh()

        loadDashboardData()
        checkLocationPermission()
    }

    /**
     * Handles touch events to detect custom gestures.
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev != null && gestureDetector.onTouchEvent(ev)) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    /**
     * Binds UI components to their respective XML layouts.
     */
    private fun initViews() {
        alertContainer = findViewById(R.id.alertContainer)
        tvAlertBody = findViewById(R.id.tvAlertBody)
        tvAllergyRiskLabel = findViewById(R.id.tvAllergyRiskLabel)
        tvAllergyScore = findViewById(R.id.tvAllergyScore)
        tvTreeValue = findViewById(R.id.tvTreeValue)
        pbTree = findViewById(R.id.pbTree)
        tvTreeTag = findViewById(R.id.tvTreeTag)
        tvGrassValue = findViewById(R.id.tvGrassValue)
        pbGrass = findViewById(R.id.pbGrass)
        tvGrassTag = findViewById(R.id.tvGrassTag)
        tvWeedValue = findViewById(R.id.tvWeedValue)
        pbWeed = findViewById(R.id.pbWeed)
        tvWeedTag = findViewById(R.id.tvWeedTag)
        tvAqiValue = findViewById(R.id.tvAqiValue)
        tvAqiLabel = findViewById(R.id.tvAqiLabel)
        tvAqiAdvice = findViewById(R.id.tvAqiAdvice)
        btnOpenPollen = findViewById(R.id.btnOpenPollen)
        btnSymptomDiary = findViewById(R.id.btnSymptomDiary)
        btnAirQuality = findViewById(R.id.btnAirQuality)
        btnSetBriefingTime = findViewById(R.id.btnSetBriefingTime)
        tvBriefingTime = findViewById(R.id.tvBriefingTime)
        alertContainer.visibility = View.GONE
    }

    /**
     * Verifies if location permissions are granted by the user.
     */
    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else { loadDashboardData() }
    }

    /**
     * Retrieves user profile from Firestore and initiates data fetching.
     */
    private fun loadDashboardData() {
        val uid = auth.currentUser?.uid
        if (uid == null) return

        firestoreService.getUserProfile(uid, { user ->
            if (user != null) {
                updateBriefingTimeDisplay(user.morningBriefingHour, user.morningBriefingMinute)
                scheduleDailyNotification(user.morningBriefingHour, user.morningBriefingMinute)
                fetchRealTimeData(user)
            } else {
                val currentUser = auth.currentUser
                val defaultUser = User(
                    uid = uid,
                    name = currentUser?.displayName ?: "User",
                    email = currentUser?.email ?: "",
                    dob = "",
                    trackedAllergens = emptyList(),
                    units = "metric"
                )

                firestoreService.saveUserToFirestore(defaultUser) { success ->
                    if (success) {
                        updateBriefingTimeDisplay(defaultUser.morningBriefingHour, defaultUser.morningBriefingMinute)
                        scheduleDailyNotification(defaultUser.morningBriefingHour, defaultUser.morningBriefingMinute)
                        fetchRealTimeData(defaultUser)
                    }
                }
            }
        }, {
            Log.e("CLEAR_DAY", "Firestore error", it)
        })
    }

    /**
     * Orchestrates the process of fetching fresh environmental data based on current location.
     */
    private fun fetchRealTimeData(user: User) {
        val uid = auth.currentUser?.uid ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fetchEverythingFromApi(user, uid, today, fusedLocationClient, isManualRefresh = false)
    }

    /**
     * Fetches pollen, air quality, and weather data from external APIs.
     */
    private fun fetchEverythingFromApi(
        user: User,
        uid: String,
        date: String,
        client: com.google.android.gms.location.FusedLocationProviderClient,
        isManualRefresh: Boolean
    ) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            swipeRefresh.isRefreshing = false
            return
        }

        client.lastLocation.addOnSuccessListener { location ->
            if (location == null) {
                swipeRefresh.isRefreshing = false
                return@addOnSuccessListener
            }

            scope.launch {
                try {
                    val mockLatitude = 32.0853
                    val mockLongitude = 34.7818

                    val pRes = pollenRepository.getPollenForecastWithPlants(
                        latitude = mockLatitude,
                        longitude = mockLongitude,
                        days = 1
                    )

                    pRes.getOrNull()?.let { pollen ->
                        updatePollenUI(user, pollen)
                    }

                    val aRes = weatherRepository.getAirQuality(location.latitude, location.longitude)
                    aRes.getOrNull()?.let { aqiData ->
                        firestoreService.updateDailyLog(uid, date, "airQuality", aqiData)
                        updateAqiUI(aqiData.data.aqi)
                    }

                    val wRes = weatherRepository.getCurrentWeather(location.latitude, location.longitude)
                    wRes.getOrNull()?.let { weather ->
                        firestoreService.updateDailyLog(uid, date, "weatherData", weather)
                        if(isManualRefresh) {
                            Toast.makeText(this@HomeActivity, "Temp: ${weather.main.temp.toInt()}°C", Toast.LENGTH_SHORT).show()
                        }
                    }

                } catch (e: Exception) {
                    Log.e("Home", "API Error", e)
                } finally {
                    swipeRefresh.isRefreshing = false
                    if (isManualRefresh) {
                        Toast.makeText(this@HomeActivity, "Refreshed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    /**
     * Updates the UI elements related to various pollen types and risk levels.
     */
    private fun updatePollenUI(user: User, pollenData: com.example.clearday.network.model.PollenForecastResponse?) {
        if (pollenData == null) return
        val todayInfo = pollenData.dailyInfo?.firstOrNull() ?: return

        todayInfo.pollenTypeInfo?.forEach { type ->
            val v = type.indexInfo?.value ?: 0
            val cat = type.indexInfo?.category ?: "Low"

            when (type.code) {
                "GRASS" -> {
                    tvGrassValue.text = "Index: $v"
                    pbGrass.progress = v * 20
                    tvGrassTag.text = cat
                }
                "TREE" -> {
                    tvTreeValue.text = "Index: $v"
                    pbTree.progress = v * 20
                    tvTreeTag.text = cat
                }
                "WEED" -> {
                    tvWeedValue.text = "Index: $v"
                    pbWeed.progress = v * 20
                    tvWeedTag.text = cat
                }
            }
        }

        val plants = todayInfo.plantInfo?.filter { it.code in (user.trackedAllergens ?: emptyList()) } ?: emptyList()
        val highRisk = plants.filter { (it.indexInfo?.value ?: 0) >= 3 }

        if (highRisk.isNotEmpty()) {
            alertContainer.visibility = View.VISIBLE
            tvAlertBody.text = "High levels: ${highRisk.joinToString { it.displayName }}"
        }

        if (plants.isNotEmpty()) {
            val avg = plants.map { it.indexInfo?.value ?: 0 }.average()
            tvAllergyScore.text = String.format("%.1f", avg)
            tvAllergyRiskLabel.text = if (avg >= 3) "High" else "Low"
        }
    }

    /**
     * Updates the Air Quality Index UI and provides health recommendations.
     */
    private fun updateAqiUI(aqi: Int) {
        tvAqiValue.text = aqi.toString()
        val label = AqiUtils.getAqiLabel(aqi)
        val color = AqiUtils.getAqiColor(aqi)

        tvAqiLabel.text = label
        tvAqiLabel.setTextColor(color)
        tvAqiValue.setTextColor(color)

        tvAqiAdvice.text = when(aqi) {
            in 0..50 -> "Safe for outdoor activities"
            in 51..100 -> "Good for most, sensitive people should watch out"
            in 101..150 -> "Sensitive groups should reduce outdoor exertion"
            in 151..200 -> "Avoid prolonged outdoor exertion"
            else -> "Stay indoors and close windows"
        }
    }

    /**
     * Sets up the SwipeRefreshLayout listener for manual data updates.
     */
    private fun setupManualRefresh() {
        swipeRefresh.setOnRefreshListener {
            val uid = auth.currentUser?.uid
            if (uid != null) {
                firestoreService.getUserProfile(uid, { user ->
                    if (user != null) {
                        val client = LocationServices.getFusedLocationProviderClient(this)
                        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                        fetchEverythingFromApi(user, uid, today, client, isManualRefresh = true)
                    } else swipeRefresh.isRefreshing = false
                }, { swipeRefresh.isRefreshing = false }
                )
            } else swipeRefresh.isRefreshing = false
        }
    }

    /**
     * Listener class for handling swipe gestures to navigate activities.
     */
    private inner class SwipeListener : GestureDetector.SimpleOnGestureListener() {
        private val SWIPE_THRESHOLD = 100
        private val SWIPE_VELOCITY_THRESHOLD = 100
        override fun onDown(e: MotionEvent): Boolean = false
        override fun onFling(e1: MotionEvent?, e2: MotionEvent, vX: Float, vY: Float): Boolean {
            if (e1 == null) return false
            val diffX = e2.x - e1.x
            val diffY = e2.y - e1.y
            if (abs(diffX) > abs(diffY)) {
                if (abs(diffX) > SWIPE_THRESHOLD && abs(vX) > SWIPE_VELOCITY_THRESHOLD) {
                    openWeatherActivity()
                    return true
                }
            }
            return false
        }
    }

    /**
     * Navigates to the WeatherActivity with current coordinates.
     */
    private fun openWeatherActivity() {
        val client = LocationServices.getFusedLocationProviderClient(this)
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            client.lastLocation.addOnSuccessListener { loc ->
                val intent = Intent(this, WeatherActivity::class.java)
                if (loc != null) {
                    intent.putExtra("LAT", loc.latitude)
                    intent.putExtra("LON", loc.longitude)
                }
                startActivity(intent)
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
            }
        } else {
            startActivity(Intent(this, WeatherActivity::class.java))
        }
    }

    /**
     * Schedules a periodic background worker for daily morning briefings.
     */
    private fun scheduleDailyNotification(targetHour: Int, targetMinute: Int) {
        val currentDate = Calendar.getInstance()
        val dueDate = Calendar.getInstance()

        dueDate.set(Calendar.HOUR_OF_DAY, targetHour)
        dueDate.set(Calendar.MINUTE, targetMinute)
        dueDate.set(Calendar.SECOND, 0)

        if (dueDate.before(currentDate)) {
            dueDate.add(Calendar.HOUR_OF_DAY, 24)
        }

        val timeDiff = dueDate.timeInMillis - currentDate.timeInMillis

        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyBriefingWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(timeDiff, TimeUnit.MILLISECONDS)
            .addTag("daily_briefing")
            .build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork(
            "DailyBriefingWork",
            ExistingPeriodicWorkPolicy.UPDATE,
            dailyWorkRequest
        )
    }

    /**
     * Displays a time picker dialog to set the daily briefing schedule.
     */
    private fun showTimePicker() {
        val uid = auth.currentUser?.uid ?: return

        firestoreService.getUserProfile(uid, { user ->
            val hour = user?.morningBriefingHour ?: 7
            val minute = user?.morningBriefingMinute ?: 0

            TimePickerDialog(this, { _, selectedHour, selectedMinute ->
                val updates = mapOf(
                    "morningBriefingHour" to selectedHour,
                    "morningBriefingMinute" to selectedMinute
                )

                firestoreService.updateUserProfile(uid, updates) { success ->
                    if (success) {
                        updateBriefingTimeDisplay(selectedHour, selectedMinute)
                        scheduleDailyNotification(selectedHour, selectedMinute)
                        Toast.makeText(this, "Morning briefing set", Toast.LENGTH_SHORT).show()
                    }
                }
            }, hour, minute, true).show()
        }, {
            Log.e("CLEAR_DAY", "Profile loading error", it)
        })
    }

    /**
     * Updates the UI display showing the scheduled briefing time.
     */
    private fun updateBriefingTimeDisplay(hour: Int, minute: Int) {
        tvBriefingTime.text = "Morning briefing: ${String.format("%02d:%02d", hour, minute)}"
    }

    /**
     * Cancels background coroutines when the activity is destroyed.
     */
    override fun onDestroy() { super.onDestroy(); scope.cancel() }
}