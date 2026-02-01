package com.example.clearday

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.clearday.services.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

/**
 * Activity for recording daily health logs.
 * Users can rate specific symptoms and general well-being,
 * which are then stored in Firestore for historical analysis.
 */
class SymptomDiaryActivity : ComponentActivity() {
    private val firestoreService = FirestoreService()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(color = MaterialTheme.colorScheme.background) {
                    SymptomDiaryScreen(onSave = { symptoms, score -> saveToFirebase(symptoms, score) })
                }
            }
        }
    }

    /**
     * Persists symptom data to Firestore indexed by the current date.
     */
    private fun saveToFirebase(symptoms: Map<String, Int>, generalScore: Int) {
        val uid = auth.currentUser?.uid ?: return
        val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        firestoreService.saveSymptoms(uid, today, symptoms, generalScore) {
            Toast.makeText(this, "Symptoms saved to logs!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomDiaryScreen(onSave: (Map<String, Int>, Int) -> Unit) {
    val options = listOf("Sneezing", "Runny Nose", "Coughing", "Itchy Eyes", "Shortness of Breath")
    var generalSev by remember { mutableFloatStateOf(1f) }
    val selected = remember { mutableStateMapOf<String, Int>() }
    val context = LocalContext.current

    Scaffold(topBar = { TopAppBar(title = { Text("Daily Symptom Diary") }) }) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                OutlinedButton(
                    onClick = { context.startActivity(Intent(context, CalendarActivity::class.java)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("View History Calendar \uD83D\uDCC5")
                }
            }

            item {
                Card(elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("General Severity", fontWeight = FontWeight.Bold)
                        Slider(
                            value = generalSev,
                            onValueChange = { generalSev = it },
                            valueRange = 1f..5f,
                            steps = 3
                        )
                        Text(
                            text = "Level: ${generalSev.toInt()}/5",
                            modifier = Modifier.align(Alignment.End)
                        )
                    }
                }
            }

            items(options) { symptom ->
                SymptomRow(
                    name = symptom,
                    current = selected[symptom] ?: 0,
                    onRating = { selected[symptom] = it }
                )
            }

            item {
                Button(
                    onClick = { onSave(selected.toMap(), generalSev.toInt()) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Save to Daily Log")
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SymptomRow(name: String, current: Int, onRating: (Int) -> Unit) {
    Column {
        Text(name, fontWeight = FontWeight.Medium)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            (0..5).forEach { i ->
                FilterChip(
                    selected = current == i,
                    onClick = { onRating(i) },
                    label = { Text(if (i == 0) "None" else i.toString()) }
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(top = 8.dp))
    }
}