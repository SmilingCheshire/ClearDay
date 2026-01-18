package com.example.clearday


import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.clearday.R
import com.example.clearday.network.model.PollenForecastResponse
import com.example.clearday.repository.PollenRepository
import com.example.clearday.viewmodel.PollenViewModel
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity() {

    // Alert
    private lateinit var alertContainer: View
    private lateinit var tvAlertTitle: TextView
    private lateinit var tvAlertBody: TextView

    // Allergy risk
    private lateinit var tvAllergyRiskLabel: TextView
    private lateinit var tvAllergyRiskDesc: TextView
    private lateinit var tvAllergyScore: TextView

    // Pollen rows
    private lateinit var tvTreeValue: TextView
    private lateinit var pbTree: ProgressBar
    private lateinit var tvTreeTag: TextView

    private lateinit var tvGrassValue: TextView
    private lateinit var pbGrass: ProgressBar
    private lateinit var tvGrassTag: TextView

    private lateinit var tvWeedValue: TextView
    private lateinit var pbWeed: ProgressBar
    private lateinit var tvWeedTag: TextView

    // AQI
    private lateinit var tvAqiValue: TextView
    private lateinit var tvAqiLabel: TextView
    private lateinit var tvAqiAdvice: TextView

    // Button
    private lateinit var btnOpenPollen: Button

    private val pollenRepository= PollenRepository()

    private val viewModel by viewModels<PollenViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)




        // --- bind views ---
        alertContainer = findViewById(R.id.alertContainer)
        tvAlertTitle = findViewById(R.id.tvAlertTitle)
        tvAlertBody = findViewById(R.id.tvAlertBody)

        tvAllergyRiskLabel = findViewById(R.id.tvAllergyRiskLabel)
        tvAllergyRiskDesc = findViewById(R.id.tvAllergyRiskDesc)
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

        // Set up button click listener to open PollenActivity
        btnOpenPollen.setOnClickListener {
            val intent = Intent(this, PollenActivity::class.java)
            startActivity(intent)
        }

        
        lifecycleScope.launch {
            // Wait for location to be available
            var location = viewModel.currentLocation.value
            while (location == null) {
                kotlinx.coroutines.delay(100)
                location = viewModel.currentLocation.value
            }
            
            // Fetch pollen data once location is available
            val result = pollenRepository.getPollenForecast(
                latitude = location.latitude,
                longitude = location.longitude
            )
            result?.onSuccess { response ->
                loadMockData(response)
            }?.onFailure { e ->
                Toast.makeText(
                    this@HomeActivity,
                    "API error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun loadMockData(response: PollenForecastResponse) {
        val today=response.dailyInfo?.firstOrNull()?: return
        today.pollenTypeInfo?.forEach { pollen ->
            when(pollen.code){
                "TREE" -> {
                    tvTreeValue.text = "${pollen.indexInfo?.value ?: 0} grains/m³"
                    pbTree.progress = ((pollen.indexInfo?.value ?: 0) * 20)
                    tvTreeTag.text = pollen.indexInfo?.category ?: "N/A"
                }
                "GRASS" -> {
                    tvGrassValue.text = "${pollen.indexInfo?.value ?: 0} grains/m³"
                    pbGrass.progress = ((pollen.indexInfo?.value ?: 0) * 20)
                    tvGrassTag.text = pollen.indexInfo?.category ?: "N/A"
                }
                "WEED" -> {
                    tvWeedValue.text = "${pollen.indexInfo?.value ?: 0} grains/m³"
                    pbWeed.progress = ((pollen.indexInfo?.value ?: 0) * 20)
                    tvWeedTag.text = pollen.indexInfo?.category ?: "N/A"
                }
            }
        }


        // High pollen alert
        alertContainer.visibility = View.VISIBLE
        tvAlertTitle.text = "High Pollen Alert"
        tvAlertBody.text =
            "Tree pollen levels are high today. Consider staying indoors during peak hours (10am–4pm)."

        // Allergy risk
        tvAllergyRiskLabel.text = "High"
        tvAllergyRiskDesc.text = "Based on your allergen profile"
        tvAllergyScore.text = "7.5"

        // AQI – you can bind this from your OpenWeather air quality call
        tvAqiValue.text = "42"
        tvAqiLabel.text = "Good"
        tvAqiAdvice.text = "Safe for outdoor activities"
    }
}