package com.example.clearday


import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.clearday.R
import com.example.clearday.auth.AuthService
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etName: TextInputEditText
    private lateinit var etSurname: TextInputEditText
    private lateinit var etAge: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnGoToLogin: TextView

    private val authService = AuthService()
    private val scope = MainScope()  // Coroutine scope for Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etEmail = findViewById(R.id.etEmail)
        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etAge = findViewById(R.id.etAge)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvError = findViewById(R.id.tvError)
        progressBar = findViewById(R.id.progressBar)
        btnGoToLogin = findViewById(R.id.tvGoToLogin)

        btnRegister.setOnClickListener {
            register()
        }

        btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun register() {
        val email = etEmail.text?.toString()?.trim() ?: ""
        val name = etName.text?.toString()?.trim() ?: ""
        val surname = etSurname.text?.toString()?.trim() ?: ""
        val ageStr = etAge.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString()?.trim() ?: ""

        // Simple validation (similar to Flutter validators)
        if (email.isEmpty()) {
            etEmail.error = "Enter email"
            return
        }
        if (!email.contains("@")) {
            etEmail.error = "Enter valid email"
            return
        }
        if (name.isEmpty()) {
            etName.error = "Enter name"
            return
        }
        if (surname.isEmpty()) {
            etSurname.error = "Enter surname"
            return
        }
        if (ageStr.isEmpty()) {
            etAge.error = "Enter age"
            return
        }

        val age = ageStr.toIntOrNull()
        if (age == null || age <= 0) {
            etAge.error = "Please enter a valid age"
            return
        }

        if (password.length < 6) {
            etPassword.error = "Password min 6 chars"
            return
        }

        tvError.text = ""
        progressBar.visibility = View.VISIBLE
        btnRegister.isEnabled = false

        scope.launch {
            try {
                val user = withContext(Dispatchers.IO) {
                    authService.registerWithEmail(
                        email = email,
                        password = password,
                        name = name,
                        surname = surname,
                        age = age
                    )
                }

                if (user != null) {
                    // Go to main page (HomeActivity)
                    val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            } catch (e: Exception) {
                tvError.text = e.message ?: "Registration failed"
            } finally {
                progressBar.visibility = View.GONE
                btnRegister.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}