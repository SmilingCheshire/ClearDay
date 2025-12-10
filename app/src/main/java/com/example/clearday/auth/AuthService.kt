package com.example.clearday.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
class AuthService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    // Register with email & password + extra fields
    suspend fun registerWithEmail(
        email: String,
        password: String,
        name: String,
        surname: String,
        age: Int
    ): FirebaseUser? {
        // Use Tasks.await or Kotlin coroutines + suspendCancellableCoroutine
        val result = com.google.android.gms.tasks.Tasks.await(
            auth.createUserWithEmailAndPassword(email, password)
        )

        val user = result.user
        if (user != null) {
            val userMap = hashMapOf(
                "email" to email,
                "name" to name,
                "surname" to surname,
                "age" to age,
                "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
            )

            com.google.android.gms.tasks.Tasks.await(
                db.collection("users").document(user.uid).set(userMap)
            )
        }
        return user
    }

    // Login with email & password
    suspend fun loginWithEmail(
        email: String,
        password: String
    ): FirebaseUser? {
        val result = com.google.android.gms.tasks.Tasks.await(
            auth.signInWithEmailAndPassword(email, password)
        )
        return result.user
    }

    fun signOut() {
        auth.signOut()
    }

    fun authStateChanges() = auth.addAuthStateListener { /* optional */ }
}