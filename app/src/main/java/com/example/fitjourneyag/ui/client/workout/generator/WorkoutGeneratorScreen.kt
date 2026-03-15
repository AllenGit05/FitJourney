package com.example.fitjourneyag.ui.client.workout.generator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourneyag.domain.model.*
import com.example.fitjourneyag.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutGeneratorScreen(
    viewModel: WorkoutGeneratorViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        containerColor = FJBackground,
        topBar = {
            TopAppBar(
                title = { Text("AI Workout Generator", color = FJTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = FJTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FJBackground)
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (val state = uiState) {
                is GeneratorUiState.Input -> InputForm(viewModel)
                is GeneratorUiState.Loading -> LoadingState()
                is GeneratorUiState.Success -> PlanDisplay(state.plan, onSave = { viewModel.savePlan(it) })
                is GeneratorUiState.Saved -> SavedState(onNavigateBack)
                is GeneratorUiState.Error -> ErrorState(state.message, onBack = { viewModel.reset() })
            }
        }
    }
}

@Composable
fun InputForm(viewModel: WorkoutGeneratorViewModel) {
    var goal by remember { mutableStateOf(viewModel.goal) }
    var level by remember { mutableStateOf(viewModel.level) }
    var location by remember { mutableStateOf(viewModel.location) }
    var equipment by remember { mutableStateOf(viewModel.equipment) }
    var days by remember { mutableStateOf(viewModel.daysPerWeek.toString()) }
    var duration by remember { mutableStateOf(viewModel.durationMinutes.toString()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 20.dp)
    ) {
        item {
            Text("Tell us about your preferences", color = FJTextSecondary, fontSize = 14.sp)
        }
        item {
            GeneratorTextField(value = goal, label = "Fitness Goal (e.g. Fat Loss)", onValueChange = { goal = it; viewModel.goal = it })
        }
        item {
            GeneratorTextField(value = level, label = "Experience (Beginner, etc.)", onValueChange = { level = it; viewModel.level = it })
        }
        item {
            GeneratorTextField(value = location, label = "Location (Gym, Home)", onValueChange = { location = it; viewModel.location = it })
        }
        item {
            GeneratorTextField(value = equipment, label = "Equipment Available", onValueChange = { equipment = it; viewModel.equipment = it })
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GeneratorTextField(value = days, label = "Days/Week", modifier = Modifier.weight(1f), onValueChange = { days = it; viewModel.daysPerWeek = it.toIntOrNull() ?: 3 })
                GeneratorTextField(value = duration, label = "Duration (min)", modifier = Modifier.weight(1f), onValueChange = { duration = it; viewModel.durationMinutes = it.toIntOrNull() ?: 45 })
            }
        }
        item {
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = { viewModel.generatePlan() },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.AutoAwesome, null)
                Spacer(Modifier.width(8.dp))
                Text("Generate Plan", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun GeneratorTextField(value: String, label: String, modifier: Modifier = Modifier, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = FJTextSecondary) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = FJGold, unfocusedBorderColor = FJDivider,
            focusedContainerColor = FJSurface, unfocusedContainerColor = FJSurface,
            focusedTextColor = FJTextPrimary, unfocusedTextColor = FJTextPrimary
        ),
        singleLine = true
    )
}

@Composable
fun LoadingState() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(color = FJGold)
        Spacer(Modifier.height(20.dp))
        Text("Our AI is crafting your plan...", color = FJGold, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PlanDisplay(plan: WeeklyWorkoutPlan, onSave: (WeeklyWorkoutPlan) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 20.dp)
        ) {
            item {
                Text("Your Weekly Schedule", color = FJTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
            items(plan.weeklySchedule) { day ->
                DayPlanCard(day)
            }
        }
        
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = FJSurface,
            tonalElevation = 8.dp
        ) {
            Button(
                onClick = { onSave(plan) },
                modifier = Modifier.fillMaxWidth().padding(20.dp).height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save and Set as Active Plan", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun DayPlanCard(day: WorkoutDay) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FJSurface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(day.dayName, color = FJGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(Modifier.height(12.dp))
            day.exercises.forEach { exercise ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(exercise.name, color = FJTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("${exercise.sets} sets × ${exercise.reps} reps", color = FJTextSecondary, fontSize = 12.sp)
                    }
                    Text("${exercise.restTimeSeconds}s rest", color = FJTextSecondary, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun SavedState(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.CheckCircle, null, tint = FJGold, modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(20.dp))
        Text("Plan Saved Successfully!", color = FJTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Spacer(Modifier.height(12.dp))
        Text("Your training is now populated.", color = FJTextSecondary)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onNavigateBack,
            colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold),
            shape = RoundedCornerShape(50)
        ) {
            Text("Back to Dashboard", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ErrorState(message: String, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.Error, null, tint = Color(0xFFE57373), modifier = Modifier.size(64.dp))
        Spacer(Modifier.height(24.dp))
        Text("Oops!", color = FJTextPrimary, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.height(8.dp))
        Text(message, color = FJTextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onBack,
            colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("Back to Input", fontWeight = FontWeight.Bold)
        }
    }
}
