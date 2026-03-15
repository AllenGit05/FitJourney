package com.example.fitjourney

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.example.fitjourney.ui.navigation.FitJourneyNavGraph
import com.example.fitjourney.ui.theme.FitJourneyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FitJourneyTheme {
                val navController = rememberNavController()
                FitJourneyNavGraph(navController = navController)
            }
        }
    }
}
