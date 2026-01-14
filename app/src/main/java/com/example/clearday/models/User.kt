package com.example.clearday.models

data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val dob: String = "",
    val trackedAllergens: List<String> = emptyList(), // Lista kluczy z Pollen API
    val units: String = "metric"
)