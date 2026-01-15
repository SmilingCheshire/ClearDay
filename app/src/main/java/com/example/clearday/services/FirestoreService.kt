package com.example.clearday.services

import com.example.clearday.models.User
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import android.util.Log

class FirestoreService {
    private val db = FirebaseFirestore.getInstance()

    // 1. Profil użytkownika
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

    // 2. Logi dzienne (Kolekcja "logs", dokument "uid_data")
    fun saveDailyLog(uid: String, date: String, pollenData: Any, onSuccess: () -> Unit) {
        val logId = "${uid}_$date"

        // Konwertujemy obiekt PollenForecastResponse na Mapę, żeby Firestore go przyjął bez błędów
        val gson = com.google.gson.Gson()
        val json = gson.toJson(pollenData)
        val dataMap = gson.fromJson(json, Map::class.java)

        val data = mapOf(
            "userId" to uid,
            "date" to date,
            "pollenData" to dataMap // zapisujemy jako czystą mapę
        )

        db.collection("logs").document(logId)
            .set(data, com.google.firebase.firestore.SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirestoreService", "SUCCESS: Data saved to logs/$logId")
                onSuccess()
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreService", "CRITICAL ERROR: ${e.message}")
            }
    }

    fun getDailyLog(uid: String, date: String, onResult: (Map<String, Any>?) -> Unit) {
        val logId = "${uid}_$date"
        db.collection("logs").document(logId).get()
            .addOnSuccessListener { doc -> onResult(doc.data) }
            .addOnFailureListener { onResult(null) }
    }

    fun saveSymptoms(uid: String, date: String, symptoms: Map<String, Int>, generalSeverity: Int, onSuccess: () -> Unit) {
        val logId = "${uid}_$date"
        val data = mapOf(
            "userId" to uid,
            "date" to date,
            "symptoms" to symptoms,
            "generalSeverity" to generalSeverity,
            "timestamp" to com.google.firebase.Timestamp.now()
        )
        db.collection("logs").document(logId)
            .set(data, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("FirestoreService", "Symptoms saved in logs/$logId")
                onSuccess()
            }
            .addOnFailureListener { e -> Log.e("FirestoreService", "Error", e) }
    }
}