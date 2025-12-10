package com.example.clearday


import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.clearday.R

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

        // TODO: replace this with real API calls (pollen + air quality)
        loadMockData()
    }

    private fun loadMockData() {
        // High pollen alert
        alertContainer.visibility = View.VISIBLE
        tvAlertTitle.text = "High Pollen Alert"
        tvAlertBody.text =
            "Tree pollen levels are high today. Consider staying indoors during peak hours (10am–4pm)."

        // Allergy risk
        tvAllergyRiskLabel.text = "High"
        tvAllergyRiskDesc.text = "Based on your allergen profile"
        tvAllergyScore.text = "7.5"

        // Pollen levels (scale 0–100 mapped to progress 0–100)
        tvTreeValue.text = "8.2 grains/m³"
        pbTree.progress = 90
        tvTreeTag.text = "High"

        tvGrassValue.text = "5.4 grains/m³"
        pbGrass.progress = 65
        tvGrassTag.text = "Moderate"

        tvWeedValue.text = "2.1 grains/m³"
        pbWeed.progress = 25
        tvWeedTag.text = "Low"

        // AQI – you can bind this from your OpenWeather air quality call
        tvAqiValue.text = "42"
        tvAqiLabel.text = "Good"
        tvAqiAdvice.text = "Safe for outdoor activities"
    }
}