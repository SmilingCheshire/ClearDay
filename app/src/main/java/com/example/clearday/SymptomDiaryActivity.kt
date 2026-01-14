package com.example.clearday

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clearday.services.FirestoreService
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*

class SymptomDiaryActivity : ComponentActivity() {
    private val firestoreService = FirestoreService()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Surface(color = MaterialTheme.colorScheme.background) {
                SymptomDiaryScreen(onSave = { symptoms, score -> saveToFirebase(symptoms, score) })
            }
        }
    }

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

    Scaffold(topBar = { TopAppBar(title = { Text("Daily Symptom Diary") }) }) { padding ->
        LazyColumn(modifier = Modifier.padding(padding).padding(16.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            item {
                Card(elevation = CardDefaults.cardElevation(2.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("General Severity", fontWeight = FontWeight.Bold)
                        Slider(value = generalSev, onValueChange = { generalSev = it }, valueRange = 1f..5f, steps = 3)
                        Text("Level: ${generalSev.toInt()}/5", modifier = Modifier.align(Alignment.End))
                    }
                }
            }
            items(options) { s ->
                SymptomRow(name = s, current = selected[s] ?: 0, onRating = { selected[s] = it })
            }
            item {
                Button(onClick = { onSave(selected.toMap(), generalSev.toInt()) }, modifier = Modifier.fillMaxWidth().height(56.dp)) {
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
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            (0..5).forEach { i ->
                FilterChip(selected = current == i, onClick = { onRating(i) }, label = { Text(if(i==0) "None" else i.toString()) })
            }
        }
        Divider(modifier = Modifier.padding(top = 8.dp))
    }
}