package com.example.clearday.models

/**
 * Data model representing a user profile within the application.
 * This class is compatible with Firebase Firestore serialization.
 */
data class User(
    val uid: String = "",
    val name: String = "",
    val email: String = "",
    val dob: String = "",
    val trackedAllergens: List<String> = emptyList(),
    val units: String = "metric",
    val morningBriefingHour: Int = 7,
    val morningBriefingMinute: Int = 0
)