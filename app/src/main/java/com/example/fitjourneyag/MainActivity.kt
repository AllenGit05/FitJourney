package com.example.fitjourneyag

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.fitjourneyag.ui.navigation.FitJourneyNavGraph
import com.example.fitjourneyag.ui.theme.FitJourneyAGTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitJourneyAGTheme {
                val navController = rememberNavController()
                FitJourneyNavGraph(navController = navController)
            }
        }
    }
}
