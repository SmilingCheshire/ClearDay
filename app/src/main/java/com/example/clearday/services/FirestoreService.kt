package com.example.clearday.services

import com.example.clearday.models.User
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import android.util.Log

class FirestoreService {
    private val db = FirebaseFirestore.getInstance()

    // 1. User Profile (users/{uid})
    fun saveUserToFirestore(user: User, onComplete: (Boolean) -> Unit) {
        db.collection("users").document(user.uid).set(user)
            .addOnCompleteListener { onComplete(it.isSuccessful) }
    }

    fun getUserProfile(uid: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java)
                onSuccess(user)
            }
            .addOnFailureListener { onFailure(it) }
    }

    // --- DAILY LOGS SECTION ---
    // Path: users/{uid}/daily_logs/{date}
    // This structure groups all logs under the specific user, making security and querying easier.

    fun saveDailyLog(uid: String, date: String, pollenData: Any, onSuccess: () -> Unit) {
        // Convert object to Map
        val gson = com.google.gson.Gson()
        val json = gson.toJson(pollenData)
        val dataMap = gson.fromJson(json, Map::class.java)

        val data = mapOf(
            "userId" to uid, // Redundant but harmless
            "date" to date,
            "pollenData" to dataMap
        )

        // CHANGED: Path is now users -> uid -> daily_logs -> date
        db.collection("users").document(uid)
            .collection("daily_logs").document(date)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirestoreService", "SUCCESS: Data saved to users/$uid/daily_logs/$date")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreService", "CRITICAL ERROR: ${e.message}")
            }
    }

    fun getDailyLog(uid: String, date: String, onResult: (Map<String, Any>?) -> Unit) {
        // CHANGED: Path matches saveDailyLog
        db.collection("users").document(uid)
            .collection("daily_logs").document(date)
            .get()
            .addOnSuccessListener { doc -> onResult(doc.data) }
            .addOnFailureListener { onResult(null) }
    }

    fun saveSymptoms(uid: String, date: String, symptoms: Map<String, Int>, generalSeverity: Int, onSuccess: () -> Unit) {
        val data = mapOf(
            "userId" to uid,
            "date" to date,
            "symptoms" to symptoms,
            "generalSeverity" to generalSeverity,
            "timestamp" to com.google.firebase.Timestamp.now()
        )

        // CHANGED: Path matches saveDailyLog
        db.collection("users").document(uid)
            .collection("daily_logs").document(date)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirestoreService", "Symptoms saved for $date")
                onSuccess()
            }
            .addOnFailureListener { e -> Log.e("FirestoreService", "Error saving symptoms", e) }
    }

    fun updateDailyLog(uid: String, date: String, key: String, dataObj: Any, onSuccess: () -> Unit = {}) {
        val gson = com.google.gson.Gson()
        val json = gson.toJson(dataObj)
        val dataMap = gson.fromJson(json, Map::class.java)

        val updateData = mapOf(
            key to dataMap,
            "lastUpdated" to com.google.firebase.Timestamp.now()
        )

        // CHANGED: Path matches saveDailyLog
        db.collection("users").document(uid)
            .collection("daily_logs").document(date)
            .set(updateData, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirestoreService", "Updated $key for $date")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreService", "Error updating $key", e)
            }
    }

    fun getMonthLogs(uid: String, yearMonth: String, onResult: (Map<String, Map<String, Any>>?) -> Unit) {
        // yearMonth format: "yyyy-MM"
        // This query works perfectly with the new structure because the Document ID IS the date (e.g., "2024-05-12")

        db.collection("users").document(uid).collection("daily_logs")
            .whereGreaterThanOrEqualTo(FieldPath.documentId(), "$yearMonth-01")
            .whereLessThanOrEqualTo(FieldPath.documentId(), "$yearMonth-31")
            .get()
            .addOnSuccessListener { documents ->
                val logs = documents.associate { it.id to it.data }
                onResult(logs)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}