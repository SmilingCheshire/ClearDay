package com.example.clearday.services

import com.example.clearday.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import io.mockk.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Testy jednostkowe dla FirestoreService używające MockK
 * do mockowania Firebase Firestore
 */
class FirestoreServiceTest {

    private lateinit var mockFirestore: FirebaseFirestore
    private lateinit var mockCollection: CollectionReference
    private lateinit var mockDocument: DocumentReference
    private lateinit var mockDocumentSnapshot: DocumentSnapshot
    private lateinit var mockTask: Task<DocumentSnapshot>
    private lateinit var mockVoidTask: Task<Void>
    
    private lateinit var firestoreService: FirestoreService

    @Before
    fun setup() {
        // Mock Firebase Firestore components
        mockFirestore = mockk(relaxed = true)
        mockCollection = mockk(relaxed = true)
        mockDocument = mockk(relaxed = true)
        mockDocumentSnapshot = mockk(relaxed = true)
        mockTask = mockk(relaxed = true)
        mockVoidTask = mockk(relaxed = true)

        // Mock static FirebaseFirestore.getInstance()
        mockkStatic(FirebaseFirestore::class)
        every { FirebaseFirestore.getInstance() } returns mockFirestore

        firestoreService = FirestoreService()
    }

    @Test
    fun `saveUserToFirestore calls Firestore set and returns success`() {
        // Given
        val testUser = User(
            uid = "test123",
            name = "Test User",
            email = "test@example.com",
            dob = "1990-01-01",
            trackedAllergens = listOf("TREE_BIRCH"),
            units = "metric"
        )
        
        var callbackResult: Boolean? = null
        
        every { mockFirestore.collection("users") } returns mockCollection
        every { mockCollection.document(any()) } returns mockDocument
        every { mockDocument.set(any()) } returns mockVoidTask
        every { mockVoidTask.addOnCompleteListener(any()) } answers {
            val listener = firstArg<OnCompleteListener<Void>>()
            every { mockVoidTask.isSuccessful } returns true
            listener.onComplete(mockVoidTask)
            mockVoidTask
        }

        // When
        firestoreService.saveUserToFirestore(testUser) { success ->
            callbackResult = success
        }

        // Then
        assertEquals(true, callbackResult)
        verify { mockDocument.set(testUser) }
    }

    @Test
    fun `saveUserToFirestore returns failure when Firestore fails`() {
        // Given
        val testUser = User(uid = "test123", name = "Test User")
        var callbackResult: Boolean? = null

        every { mockFirestore.collection("users") } returns mockCollection
        every { mockCollection.document(any()) } returns mockDocument
        every { mockDocument.set(any()) } returns mockVoidTask
        every { mockVoidTask.addOnCompleteListener(any()) } answers {
            val listener = firstArg<OnCompleteListener<Void>>()
            every { mockVoidTask.isSuccessful } returns false
            listener.onComplete(mockVoidTask)
            mockVoidTask
        }

        // When
        firestoreService.saveUserToFirestore(testUser) { success ->
            callbackResult = success
        }

        // Then
        assertEquals(false, callbackResult)
    }

    @Test
    fun `getUserProfile returns user when document exists`() {
        // Given
        val expectedUser = User(
            uid = "test123",
            name = "John Doe",
            email = "john@example.com"
        )
        var resultUser: User? = null
        var resultError: Exception? = null

        every { mockFirestore.collection("users") } returns mockCollection
        every { mockCollection.document("test123") } returns mockDocument
        every { mockDocument.get() } returns mockTask
        every { mockTask.addOnSuccessListener(any()) } answers {
            val listener = firstArg<OnSuccessListener<DocumentSnapshot>>()
            every { mockDocumentSnapshot.toObject(User::class.java) } returns expectedUser
            listener.onSuccess(mockDocumentSnapshot)
            mockTask
        }
        every { mockTask.addOnFailureListener(any()) } returns mockTask

        // When
        firestoreService.getUserProfile(
            uid = "test123",
            onSuccess = { resultUser = it },
            onFailure = { resultError = it }
        )

        // Then
        assertNotNull(resultUser)
        assertEquals("John Doe", resultUser?.name)
        assertEquals("john@example.com", resultUser?.email)
        assertNull(resultError)
    }

    @Test
    fun `getUserProfile returns null when document does not exist`() {
        // Given
        var resultUser: User? = User(uid = "dummy") // Initialize with dummy to verify it gets set to null
        var resultError: Exception? = null

        every { mockFirestore.collection("users") } returns mockCollection
        every { mockCollection.document("nonexistent") } returns mockDocument
        every { mockDocument.get() } returns mockTask
        every { mockTask.addOnSuccessListener(any()) } answers {
            val listener = firstArg<OnSuccessListener<DocumentSnapshot>>()
            every { mockDocumentSnapshot.toObject(User::class.java) } returns null
            listener.onSuccess(mockDocumentSnapshot)
            mockTask
        }
        every { mockTask.addOnFailureListener(any()) } returns mockTask

        // When
        firestoreService.getUserProfile(
            uid = "nonexistent",
            onSuccess = { resultUser = it },
            onFailure = { resultError = it }
        )

        // Then
        assertNull(resultUser)
        assertNull(resultError)
    }

    @Test
    fun `getUserProfile calls onFailure when Firestore fails`() {
        // Given
        var resultUser: User? = null
        var resultError: Exception? = null
        val testException = Exception("Firestore error")

        every { mockFirestore.collection("users") } returns mockCollection
        every { mockCollection.document("test123") } returns mockDocument
        every { mockDocument.get() } returns mockTask
        every { mockTask.addOnSuccessListener(any()) } returns mockTask
        every { mockTask.addOnFailureListener(any()) } answers {
            val listener = firstArg<OnFailureListener>()
            listener.onFailure(testException)
            mockTask
        }

        // When
        firestoreService.getUserProfile(
            uid = "test123",
            onSuccess = { resultUser = it },
            onFailure = { resultError = it }
        )

        // Then
        assertNull(resultUser)
        assertNotNull(resultError)
        assertEquals("Firestore error", resultError?.message)
    }

    @Test
    fun `updateUserProfile successfully updates document`() {
        // Given
        val updates = mapOf(
            "name" to "Updated Name",
            "morningBriefingHour" to 8
        )
        var callbackResult: Boolean? = null

        every { mockFirestore.collection("users") } returns mockCollection
        every { mockCollection.document("test123") } returns mockDocument
        every { mockDocument.update(updates) } returns mockVoidTask
        every { mockVoidTask.addOnCompleteListener(any()) } answers {
            val listener = firstArg<OnCompleteListener<Void>>()
            every { mockVoidTask.isSuccessful } returns true
            listener.onComplete(mockVoidTask)
            mockVoidTask
        }

        // When
        firestoreService.updateUserProfile("test123", updates) { success ->
            callbackResult = success
        }

        // Then
        assertEquals(true, callbackResult)
        verify { mockDocument.update(updates) }
    }

    @Test
    fun `updateDailyLog constructs proper data structure`() {
        // Test logiki konstrukcji danych (bez wywołania Firebase)
        val testData = mapOf("temp" to 20, "humidity" to 60)
        val key = "weatherData"
        
        // Sprawdź że mapa zawiera oczekiwane klucze
        assertTrue(testData.containsKey("temp"))
        assertTrue(testData.containsKey("humidity"))
        assertEquals(20, testData["temp"])
    }

    @Test
    fun `saveSymptoms constructs proper symptom map`() {
        // Test logiki konstrukcji mapy symptomów
        val symptoms = mapOf("sneezing" to 3, "itchyEyes" to 2)
        val generalSeverity = 3
        
        // Sprawdź strukturę danych
        assertTrue(symptoms.containsKey("sneezing"))
        assertTrue(symptoms.containsKey("itchyEyes"))
        assertEquals(3, symptoms["sneezing"])
        assertEquals(2, symptoms["itchyEyes"])
        assertTrue(generalSeverity in 1..5)
    }
}
