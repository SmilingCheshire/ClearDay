package com.example.clearday.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore

/**
 * Service class handling Firebase Authentication and initial user data synchronization with Firestore.
 */
class AuthService {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    /**
     * Registers a new user with email and password, then creates a corresponding user document in Firestore.
     * * @return The registered [FirebaseUser] or null if the process fails.
     */
    suspend fun registerWithEmail(
        email: String,
        password: String,
        name: String,
        surname: String,
        age: Int
    ): FirebaseUser? {
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

    /**
     * Authenticates a user using email and password.
     * * @return The authenticated [FirebaseUser] or null if login fails.
     */
    suspend fun loginWithEmail(
        email: String,
        password: String
    ): FirebaseUser? {
        val result = com.google.android.gms.tasks.Tasks.await(
            auth.signInWithEmailAndPassword(email, password)
        )
        return result.user
    }

    /**
     * Signs out the currently authenticated user.
     */
    fun signOut() {
        auth.signOut()
    }

    /**
     * Provides an interface to listen for changes in the user's authentication state.
     */
    fun authStateChanges() = auth.addAuthStateListener { }
}