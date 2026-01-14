package com.example.clearday

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.clearday.auth.AuthService
import com.example.clearday.models.User
import com.example.clearday.services.FirestoreService
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private lateinit var etName: TextInputEditText
    private lateinit var etSurname: TextInputEditText
    private lateinit var etDob: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnRegister: Button
    private lateinit var tvError: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnGoToLogin: TextView

    private lateinit var layoutStep1: LinearLayout
    private lateinit var layoutStep2: LinearLayout

    private lateinit var checkboxes: Map<String, CheckBox>
    private var selectedDob: String = ""

    private val authService = AuthService()
    private val firestoreService = FirestoreService()
    private val scope = MainScope()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        etEmail = findViewById(R.id.etEmail)
        etName = findViewById(R.id.etName)
        etSurname = findViewById(R.id.etSurname)
        etDob = findViewById(R.id.etDob)
        etPassword = findViewById(R.id.etPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvError = findViewById(R.id.tvError)
        progressBar = findViewById(R.id.progressBar)
        btnGoToLogin = findViewById(R.id.tvGoToLogin)
        layoutStep1 = findViewById(R.id.layoutStep1)
        layoutStep2 = findViewById(R.id.layoutStep2)

        checkboxes = mapOf(
            "ALDER" to findViewById(R.id.cbAlder),
            "BIRCH" to findViewById(R.id.cbBirch),
            "GRASS" to findViewById(R.id.cbGrass),
            "MUGWORT" to findViewById(R.id.cbMugwort),
            "OLIVE" to findViewById(R.id.cbOlive),
            "PINE" to findViewById(R.id.cbPine),
            "RAGWEED" to findViewById(R.id.cbRagweed)
        )

        etDob.setOnClickListener { showDatePicker() }

        btnRegister.setOnClickListener { register() }

        btnGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(this, { _, year, month, day ->
            selectedDob = "$year-${month + 1}-$day"
            etDob.setText(selectedDob)
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH))

        datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
        datePickerDialog.show()
    }

    private fun register() {
        val email = etEmail.text?.toString()?.trim() ?: ""
        val name = etName.text?.toString()?.trim() ?: ""
        val surname = etSurname.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString()?.trim() ?: ""

        if (email.isEmpty() || !email.contains("@")) { etEmail.error = "Enter valid email"; return }
        if (name.isEmpty()) { etName.error = "Enter name"; return }
        if (surname.isEmpty()) { etSurname.error = "Enter surname"; return }
        if (selectedDob.isEmpty()) { etDob.error = "Select date of birth"; return }
        if (password.length < 6) { etPassword.error = "Password min 6 chars"; return }

        tvError.text = ""
        progressBar.visibility = View.VISIBLE
        btnRegister.isEnabled = false

        scope.launch {
            try {
                // Wyliczamy wiek z daty, aby pasował do Twojego AuthService (age: Int)
                val age = calculateAge(selectedDob)

                val firebaseUser = withContext(Dispatchers.IO) {
                    authService.registerWithEmail(email, password, name, surname, age)
                }

                if (firebaseUser != null) {
                    progressBar.visibility = View.GONE
                    layoutStep1.visibility = View.GONE
                    layoutStep2.visibility = View.VISIBLE
                    btnRegister.text = "Complete Profile"
                    btnRegister.isEnabled = true

                    btnRegister.setOnClickListener {
                        saveAllergenInfo(firebaseUser.uid, email, name, surname)
                    }
                }
            } catch (e: Exception) {
                tvError.text = e.message ?: "Registration failed"
                progressBar.visibility = View.GONE
                btnRegister.isEnabled = true
            }
        }
    }

    private fun calculateAge(dob: String): Int {
        val parts = dob.split("-")
        val year = parts[0].toInt()
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) - year
    }

    private fun saveAllergenInfo(uid: String, email: String, name: String, surname: String) {
        val selectedAllergens = checkboxes.filter { it.value.isChecked }.keys.toList()

        val newUser = User(
            uid = uid,
            email = email,
            name = "$name $surname",
            dob = selectedDob,
            trackedAllergens = selectedAllergens,
            units = "metric"
        )

        progressBar.visibility = View.VISIBLE
        btnRegister.isEnabled = false

        firestoreService.saveUserToFirestore(newUser) { success ->
            progressBar.visibility = View.GONE
            if (success) {
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                tvError.text = "Failed to save profile to database"
                btnRegister.isEnabled = true
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}