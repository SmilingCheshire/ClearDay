package com.example.clearday

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.clearday.models.User
import com.example.clearday.repository.PollenRepository
import com.example.clearday.repository.WeatherRepository
import com.example.clearday.services.FirestoreService
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.gson.Gson
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*

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

    private val auth = FirebaseAuth.getInstance()
    private val firestoreService = FirestoreService()
    private val pollenRepository = PollenRepository()
    private val weatherRepository = WeatherRepository()
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        initViews()

        btnOpenPollen.setOnClickListener { startActivity(Intent(this, PollenActivity::class.java)) }
        btnSymptomDiary.setOnClickListener { startActivity(Intent(this, SymptomDiaryActivity::class.java)) }

        checkLocationPermission()
    }

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
        alertContainer.visibility = View.GONE
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
        } else { loadDashboardData() }
    }

    private fun loadDashboardData() {
        val uid = auth.currentUser?.uid ?: return
        firestoreService.getUserProfile(uid, { user ->
            if (user != null) fetchRealTimeData(user)
        }, { Log.e("CLEAR_DAY", "Firestore error", it) })
    }

    private fun fetchRealTimeData(user: User) {
        val uid = auth.currentUser?.uid ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Sprawdzamy w kolekcji "logs" przez FirestoreService
        firestoreService.getDailyLog(uid, today) { data ->
            if (data != null && data.containsKey("pollenData")) {
                Log.d("CLEAR_DAY", "Loading from logs cache")
                val gson = Gson()
                val json = gson.toJson(data["pollenData"])
                val cached = gson.fromJson(json, com.example.clearday.network.model.PollenForecastResponse::class.java)
                updatePollenUI(user, cached)
                fetchAqiOnly(fusedLocationClient)
            } else {
                Log.d("CLEAR_DAY", "No log for today, calling Pollen API")
                fetchEverythingFromApi(user, uid, today, fusedLocationClient)
            }
        }
    }

    private fun fetchAqiOnly(client: com.google.android.gms.location.FusedLocationProviderClient) {
        try {
            client.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    scope.launch {
                        val result = weatherRepository.getAirQuality(it.latitude, it.longitude)
                        result.getOrNull()?.let { resp -> updateAqiUI(resp.list.firstOrNull()?.main?.aqi ?: 1) }
                    }
                }
            }
        } catch (e: SecurityException) {}
    }

    private fun fetchEverythingFromApi(user: User, uid: String, date: String, client: com.google.android.gms.location.FusedLocationProviderClient) {
        try {
            client.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    scope.launch {
                        val pRes = pollenRepository.getPollenForecast(it.latitude, it.longitude, 1)
                        pRes.getOrNull()?.let { pollen ->
                            firestoreService.saveDailyLog(uid, date, pollen) {}
                            updatePollenUI(user, pollen)
                        }
                        val aRes = weatherRepository.getAirQuality(it.latitude, it.longitude)
                        aRes.getOrNull()?.let { updateAqiUI(it.list.firstOrNull()?.main?.aqi ?: 1) }
                    }
                }
            }
        } catch (e: SecurityException) {}
    }

    private fun updatePollenUI(user: User, pollenData: com.example.clearday.network.model.PollenForecastResponse?) {
        if (pollenData == null) return
        val todayInfo = pollenData.dailyInfo?.firstOrNull() ?: return
        todayInfo.pollenTypeInfo?.forEach { type ->
            val v = type.indexInfo?.value ?: 0
            val cat = type.indexInfo?.category ?: "Low"
            when (type.code) {
                "GRASS" -> { tvGrassValue.text = "Index: $v"; pbGrass.progress = v * 20; tvGrassTag.text = cat }
                "TREE" -> { tvTreeValue.text = "Index: $v"; pbTree.progress = v * 20; tvTreeTag.text = cat }
                "WEED" -> { tvWeedValue.text = "Index: $v"; pbWeed.progress = v * 20; tvWeedTag.text = cat }
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

    private fun updateAqiUI(aqi: Int) {
        tvAqiValue.text = aqi.toString()
        val (label, colorStr, advice) = when (aqi) {
            1 -> Triple("Good", "#2E7D32", "Safe for outdoor activities")
            2 -> Triple("Fair", "#4CAF50", "Generally acceptable")
            3 -> Triple("Moderate", "#FBC02D", "Sensitive groups limit outdoor time")
            4 -> Triple("Poor", "#F44336", "Avoid prolonged exposure")
            5 -> Triple("Very Poor", "#B71C1C", "Stay indoors")
            else -> Triple("Unknown", "#777777", "No data")
        }
        tvAqiLabel.text = label
        val color = Color.parseColor(colorStr)
        tvAqiLabel.setTextColor(color)
        tvAqiValue.setTextColor(color)
        tvAqiAdvice.text = advice
    }

    override fun onDestroy() { super.onDestroy(); scope.cancel() }
}