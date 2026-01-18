package com.example.clearday.ui.screen
import androidx.compose.foundation.clickable

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
// Pamiętaj o imporcie swojego obiektu z listą alergenów
import com.example.clearday.utils.AllergenData
@Composable
fun AllergenSelectionScreen(
    onSelectionComplete: (List<String>) -> Unit
) {
    val selectedAllergens = remember { mutableStateListOf<String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Text(
            text = "Select your allergens",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "We will use this to personalize your alerts.",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(AllergenData.allergens) { allergen ->
                val isSelected = selectedAllergens.contains(allergen)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isSelected) selectedAllergens.remove(allergen)
                            else selectedAllergens.add(allergen)
                        }
                        .padding(vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = null // Handled by Row clickable
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(text = allergen.replace("_", " ").lowercase().capitalize())
                }
            }
        }

        Button(
            onClick = { onSelectionComplete(selectedAllergens.toList()) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Complete Registration")
        }
    }
}