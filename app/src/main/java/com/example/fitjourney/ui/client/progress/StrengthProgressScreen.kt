package com.example.fitjourney.ui.client.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.ui.theme.FJBackground
import com.example.fitjourney.ui.theme.FJGold
import com.example.fitjourney.ui.theme.FJSurface
import com.example.fitjourney.ui.theme.FJTextPrimary
import com.example.fitjourney.ui.theme.FJTextSecondary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import com.example.fitjourney.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrengthProgressScreen(
    viewModel: ProgressViewModel,
    onNavigateBack: () -> Unit
) {
    val strengthPoints by viewModel.strengthProgress.collectAsState()
    val availableExercises by viewModel.availableExercises.collectAsState()
    val selectedExercise by viewModel.selectedExercise.collectAsState()
    
    var showExercisePicker by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FJBackground,
        topBar = {
            TopAppBar(
                title = { Text("Strength Progress", color = FJTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = FJTextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FJBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Exercise Selector
            item {
                Surface(
                    onClick = { showExercisePicker = true },
                    color = FJSurface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Select Exercise", color = FJTextSecondary, fontSize = 12.sp)
                            Text(selectedExercise ?: "All Exercises", color = FJGold, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = FJGold)
                    }
                }
            }

            if (strengthPoints.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, null, tint = FJSurfaceHigh, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No recorded weight for this exercise yet.", color = FJTextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    }
                }
            } else {
                item {
                    Text("Progress History", color = FJTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                }
                
                items(strengthPoints.reversed()) { point ->
                    StrengthPointCard(point)
                }
            }
        }
    }

    if (showExercisePicker) {
        AlertDialog(
            onDismissRequest = { showExercisePicker = false },
            title = { Text("Select Exercise") },
            text = {
                LazyColumn {
                    item {
                        TextButton(
                            onClick = { viewModel.selectExercise(null); showExercisePicker = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("All Exercises", color = if (selectedExercise == null) FJGold else FJTextPrimary)
                        }
                    }
                    items(availableExercises) { exercise ->
                        TextButton(
                            onClick = { viewModel.selectExercise(exercise); showExercisePicker = false },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(exercise, color = if (selectedExercise == exercise) FJGold else FJTextPrimary)
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = { TextButton(onClick = { showExercisePicker = false }) { Text("Close") } },
            containerColor = FJSurface,
            shape = RoundedCornerShape(24.dp)
        )
    }
}

@Composable
fun StrengthPointCard(point: com.example.fitjourney.domain.model.StrengthPoint) {
    Surface(
        color = FJSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(point.exerciseName, color = FJTextPrimary, fontWeight = FontWeight.Bold)
                Text(
                    java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(point.date)),
                    color = FJTextSecondary,
                    fontSize = 12.sp
                )
            }
            Text("${point.maxWeight} kg", color = FJGold, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        }
    }
}
