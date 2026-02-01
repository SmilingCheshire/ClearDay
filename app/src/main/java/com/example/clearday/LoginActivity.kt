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
import com.example.clearday.auth.AuthService
import com.example.clearday.location.LocationTracker
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*

/**
 * Handles user authentication via Firebase and initializes location tracking.
 * Manages runtime permissions for location services necessary for air quality alerts.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: Button
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvGoToRegister: TextView

    private val authService = AuthService()
    private val scope = MainScope()

    private lateinit var locationTracker: LocationTracker
    private var hasLocationPermission = false

    companion object {
        private const val REQ_LOCATION_PERMISSION = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        initializeViews()
        locationTracker = LocationTracker(this)

        checkLocationPermission()

        btnLogin.setOnClickListener { login() }
        tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    private fun initializeViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        tvError = findViewById(R.id.tvError)
        progressBar = findViewById(R.id.progressBar)
        tvGoToRegister = findViewById(R.id.tvGoToRegister)
    }

    /**
     * Verifies if the user has granted access to fine or coarse location.
     * Triggers permission request if neither is granted.
     */
    private fun checkLocationPermission() {
        val fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED

        hasLocationPermission = fineGranted || coarseGranted

        if (!hasLocationPermission) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                REQ_LOCATION_PERMISSION
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQ_LOCATION_PERMISSION) {
            hasLocationPermission = grantResults.isNotEmpty() && grantResults.any { it == PackageManager.PERMISSION_GRANTED }
            if (!hasLocationPermission) {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Executes login logic. Upon successful authentication, starts background
     * location tracking if permissions were granted.
     */
    private fun login() {
        val email = etEmail.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString()?.trim() ?: ""

        if (!validateInput(email, password)) return

        toggleLoading(true)

        scope.launch {
            try {
                val user = withContext(Dispatchers.IO) { authService.loginWithEmail(email, password) }

                if (user != null) {
                    if (hasLocationPermission) {
                        locationTracker.startTracking { location ->
                            // TODO: Sync location with Firestore using user.uid
                        }
                    }
                    startActivity(Intent(this@LoginActivity, HomeActivity::class.java))
                    finish()
                } else {
                    tvError.text = "Login failed"
                }
            } catch (e: Exception) {
                tvError.text = e.message ?: "Login failed"
            } finally {
                toggleLoading(false)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) { etEmail.error = "Enter email"; return false }
        if (!email.contains("@")) { etEmail.error = "Enter valid email"; return false }
        if (password.isEmpty()) { etPassword.error = "Enter password"; return false }
        return true
    }

    private fun toggleLoading(isLoading: Boolean) {
        tvError.text = ""
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnLogin.isEnabled = !isLoading
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
        if (this::locationTracker.isInitialized) {
            locationTracker.stopTracking()
        }
    }
}