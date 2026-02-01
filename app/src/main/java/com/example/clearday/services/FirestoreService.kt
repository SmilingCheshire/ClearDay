package com.example.clearday.services

import com.example.clearday.models.User
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import android.util.Log

/**
 * Service handling all interactions with Cloud Firestore, including user profiles
 * and hierarchical daily logs for health tracking.
 */
class FirestoreService {
    private val db = FirebaseFirestore.getInstance()

    /**
     * Saves or overwrites the main user profile information.
     */
    fun saveUserToFirestore(user: User, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(user.uid).set(user)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    /**
     * Retrieves user profile data for a specific unique identifier.
     */
    fun getUserProfile(uid: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                onSuccess(user)
            }
            .addOnFailureListener { onFailure(it) }
    }

    /**
     * Performs a partial update on the user's profile fields.
     */
    fun updateUserProfile(uid: String, updates: Map<String, Any>, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(uid)
            .update(updates)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    /**
     * Saves pollen data to a specific daily log document using sub-collections.
     */
    fun saveDailyLog(uid: String, date: String, pollenData: Any, onSuccess: () -> Unit) {
        val gson = com.google.gson.Gson()
        val json = gson.toJson(pollenData)
        val dataMap = gson.fromJson(json, Map::class.java)

        val data = mapOf(
            "userId" to uid,
            "date" to date,
            "pollenData" to dataMap
        )

        db.collection("users").document(uid)
            .collection("daily_logs").document(date)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
    }

    /**
     * Retrieves a single daily log document for a given date.
     */
    fun getDailyLog(uid: String, date: String, onResult: (Map<String, Any>?) -> Unit) {
        db.collection("users").document(uid)
            .collection("daily_logs").document(date)
            .get()
            .addOnSuccessListener { doc -> onResult(doc.data) }
            .addOnFailureListener { onResult(null) }
    }

    /**
     * Updates a daily log with user-reported allergy symptoms and overall severity.
     */
    fun saveSymptoms(uid: String, date: String, symptoms: Map<String, Int>, generalSeverity: Int, onSuccess: () -> Unit) {
        val data = mapOf(
            "userId" to uid,
            "date" to date,
            "symptoms" to symptoms,
            "generalSeverity" to generalSeverity,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(uid)
            .collection("daily_logs").document(date)
            .set(data, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
    }

    /**
     * Merges a generic data object into the daily log for a specific key (e.g., weather, airQuality).
     */
    fun updateDailyLog(uid: String, date: String, key: String, dataObj: Any, onSuccess: () -> Unit = {}) {
        val gson = com.google.gson.Gson()
        val json = gson.toJson(dataObj)
        val dataMap = gson.fromJson(json, Map::class.java)

        val updateData = mapOf(
            key to dataMap,
            "lastUpdated" to com.google.firebase.Timestamp.now()
        )

        db.collection("users").document(uid)
            .collection("daily_logs").document(date)
            .set(updateData, SetOptions.merge())
            .addOnSuccessListener { onSuccess() }
    }

    /**
     * Queries all daily logs within a specific month for data visualization or history views.
     */
    fun getMonthLogs(uid: String, yearMonth: String, onResult: (Map<String, Map<String, Any>>?) -> Unit) {
        db.collection("users").document(uid).collection("daily_logs")
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), "$yearMonth-01")
            .whereLessThanOrEqualTo(FieldPath.documentId(), "$yearMonth-31")
            .get()
            .addOnSuccessListener { documents ->
                val logs = documents.associate { it.id to it.data }
                onResult(logs)
            }
            .addOnFailureListener { onResult(null) }
    }
}