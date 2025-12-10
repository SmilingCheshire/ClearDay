// LoginActivity.kt
package com.example.clearday

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.clearday.R
import com.example.clearday.auth.AuthService
import com.example.clearday.location.LocationTracker
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvGoToRegister: TextView

    private val authService = AuthService()
    private val scope = MainScope()

    // === GEO PERMISSION & TRACKER ===
    companion object {
        private const val REQ_LOCATION_PERMISSION = 1001
    }

    private lateinit var locationTracker: LocationTracker
    private var hasLocationPermission = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvError = findViewById(R.id.tvError)
        progressBar = findViewById(R.id.progressBar)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)

        locationTracker = LocationTracker(this)

        // Ask for location permission when login screen appears
        checkLocationPermission()

        btnLogin.setOnClickListener {
            login()
        }

        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    // === PERMISSION FLOW ===
    private fun checkLocationPermission() {
        val fineGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        hasLocationPermission = fineGranted || coarseGranted

        if (!hasLocationPermission) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                REQ_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == REQ_LOCATION_PERMISSION) {
            hasLocationPermission = grantResults.isNotEmpty() &&
                    grantResults.any { it == PackageManager.PERMISSION_GRANTED }

            if (!hasLocationPermission) {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // === LOGIN + START TRACKING ===
    private fun login() {
        val email = etEmail.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString()?.trim() ?: ""

        if (email.isEmpty()) {
            etEmail.error = "Enter email"
            return
        }
        if (!email.contains("@")) {
            etEmail.error = "Enter valid email"
            return
        }
        if (password.isEmpty()) {
            etPassword.error = "Enter password"
            return
        }

        tvError.text = ""
        progressBar.visibility = View.VISIBLE
        btnLogin.isEnabled = false

        scope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    authService.loginWithEmail(email, password)
                }

                if (user != null) {
                    // Start location tracking after user logged in (if allowed)
                    if (hasLocationPermission) {
                        locationTracker.startTracking { location ->
                            // Here you can send location with user.uid to Firestore, etc.
                            // Example (pseudo):
                            // FirebaseFirestore.getInstance().collection("userLocations")
                            //   .document(user.uid)
                            //   .set(mapOf("lat" to location.latitude, "lng" to location.longitude))
                        }
                    }

                    val intent = Intent(this@LoginActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    tvError.text = "Login failed"
                }
            } catch (e: Exception) {
                tvError.text = e.message ?: "Login failed"
            } finally {
                progressBar.visibility = View.GONE
                btnLogin.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        if (this::locationTracker.isInitialized) {
            locationTracker.stopTracking()
        }
    }
}
