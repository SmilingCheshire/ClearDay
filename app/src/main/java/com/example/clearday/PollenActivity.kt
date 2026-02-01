package com.example.clearday

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.clearday.ui.screen.PollenScreen

/**
 * Activity providing air allergen forecasts.
 * Serves as a container for the PollenScreen.
 */
class PollenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface {
                    PollenScreen()
                }
            }
        }
    }
}