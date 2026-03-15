package com.example.fitjourney.ui.client.track

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.ui.theme.*

@Composable
fun TrackScreen(
    viewModel: TrackViewModel,
    onNavigateToDiet: () -> Unit,
    onNavigateToWorkout: () -> Unit,
    onNavigateToBmi: () -> Unit,
    onNavigateToTdee: () -> Unit,
    onNavigateToMacros: () -> Unit,
    onNavigateToHabits: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    val steps by viewModel.steps.collectAsState()
    val waterIntake by viewModel.waterDrank.collectAsState()

    var showStepDialog by remember { mutableStateOf(false) }
    var showGoalDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FJBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                Text("Track Your Day", color = FJTextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text("Log your activity to stay on track", color = FJTextSecondary, fontSize = 16.sp)
            }

            // ── 1. Progress Summary ──────────────────────────────
            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    val stepGoal = user?.stepGoal ?: 10000
                    val waterGoal = user?.waterGoal ?: 2500
                    
                    ProgressCircleCard(
                        title = "Steps",
                        current = steps,
                        goal = stepGoal,
                        unit = "",
                        icon = Icons.Default.DirectionsWalk,
                        color = FJGold,
                        modifier = Modifier.weight(1f)
                    )
                    ProgressCircleCard(
                        title = "Water",
                        current = waterIntake,
                        goal = waterGoal,
                        unit = "ml",
                        icon = Icons.Default.WaterDrop,
                        color = Color(0xFF2196F3),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // ── 2. Logging Section ───────────────────────────────
            item {
                Text("Daily Logs", color = FJTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TrackActionButton("Log Steps", "Manual entry for your daily steps", Icons.Default.AddCircleOutline, { showStepDialog = true })
                    TrackActionButton("Daily Habits", "Track your lifestyle routines", Icons.Default.Checklist, onNavigateToHabits)
                    TrackActionButton("Add Food", "Log your meals and calories", Icons.Default.Restaurant, onNavigateToDiet)
                    TrackActionButton("Log Workout", "Record your training session", Icons.Default.FitnessCenter, onNavigateToWorkout)
                }
            }

            // ── 3. Water Intake Section ──────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FJSurface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            WaterQuickButton("-1000", { viewModel.removeSteps(1000) }, isNegative = true)
                            WaterQuickButton("+1000", { viewModel.logSteps(1000) })
                        }
                    }
                }
            }

            // ── 3. Water Intake Section ──────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FJSurface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.WaterDrop, null, tint = Color(0xFF2196F3), modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Water Logs", color = FJTextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            WaterQuickButton("-250ml", { viewModel.removeWater(250) }, isNegative = true)
                            WaterQuickButton("+250ml", { viewModel.addWater(250) })
                            WaterQuickButton("+500ml", { viewModel.addWater(500) })
                        }
                    }
                }
            }

            // ── 4. Goals & Tools ─────────────────────────────────
            item {
                Text("Goals & Tools", color = FJTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Spacer(Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    TrackActionButton("Edit Daily Goals", "Set step and water intake targets", Icons.Default.Settings, { showGoalDialog = true })
                    ToolButton("BMI Calculator", "Calculate your Body Mass Index", Icons.Default.Calculate, onNavigateToBmi)
                    ToolButton("TDEE Calculator", "Find your daily energy expenditure", Icons.Default.LocalFireDepartment, onNavigateToTdee)
                    ToolButton("Macros Calculator", "Optimize your nutrient breakdown", Icons.Default.PieChart, onNavigateToMacros)
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { /* TODO: Health Connect */ },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FJSurfaceHigh),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.HealthAndSafety, null, tint = FJGold)
                    Spacer(Modifier.width(12.dp))
                    Text("Connect Health App", color = FJTextPrimary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showStepDialog) {
        StepEntryDialog(onDismiss = { showStepDialog = false }, onConfirm = { viewModel.logSteps(it) })
    }
    if (showGoalDialog) {
        GoalSettingDialog(
            initialSteps = user?.stepGoal ?: 10000,
            initialWater = user?.waterGoal ?: 2500,
            onDismiss = { showGoalDialog = false },
            onConfirm = { s, w -> viewModel.updateGoals(s, w) }
        )
    }
}

@Composable
fun ProgressCircleCard(title: String, current: Int, goal: Int, unit: String, icon: ImageVector, color: Color, modifier: Modifier) {
    Surface(
        color = FJSurface,
        shape = RoundedCornerShape(24.dp),
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(80.dp)) {
                CircularProgressIndicator(
                    progress = { (current.toFloat() / goal.toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxSize(),
                    color = color,
                    trackColor = FJSurfaceHigh,
                    strokeCap = StrokeCap.Round,
                    strokeWidth = 8.dp
                )
                Icon(icon, null, tint = color.copy(0.6f), modifier = Modifier.size(32.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(title, color = FJTextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("$current $unit", color = FJTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun WaterQuickButton(label: String, onClick: () -> Unit, isNegative: Boolean = false) {
    Button(
        onClick = onClick,
        modifier = Modifier.width(90.dp).height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isNegative) FJSurfaceHigh else Color(0xFF2196F3).copy(0.1f),
            contentColor = if (isNegative) FJTextSecondary else Color(0xFF2196F3)
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
    }
}

@Composable
fun StepEntryDialog(onDismiss: () -> Unit, onConfirm: (Int) -> Unit) {
    var text by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Steps", fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.all { char -> char.isDigit() }) text = it },
                label = { Text("How many steps?") },
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
        },
        confirmButton = {
            Button(
                onClick = { 
                    val stepCount = text.toIntOrNull() ?: 0
                    if (stepCount > 0) {
                        onConfirm(stepCount)
                    }
                    onDismiss() 
                }, 
                colors = ButtonDefaults.buttonColors(containerColor = FJGold)
            ) {
                Text("Log", color = FJOnGold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FJTextSecondary) }
        },
        containerColor = FJSurface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun GoalSettingDialog(initialSteps: Int, initialWater: Int, onDismiss: () -> Unit, onConfirm: (Int, Int) -> Unit) {
    var stepText by remember { mutableStateOf(initialSteps.toString()) }
    var waterText by remember { mutableStateOf(initialWater.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Daily Goals", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = stepText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) stepText = it },
                    label = { Text("Step Goal") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
                OutlinedTextField(
                    value = waterText,
                    onValueChange = { if (it.all { char -> char.isDigit() }) waterText = it },
                    label = { Text("Water Goal (ml)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val steps = stepText.toIntOrNull()?.coerceAtLeast(0) ?: 10000
                    val water = waterText.toIntOrNull()?.coerceAtLeast(0) ?: 2500
                    onConfirm(steps, water)
                    onDismiss() 
                }, 
                colors = ButtonDefaults.buttonColors(containerColor = FJGold)
            ) {
                Text("Save", color = FJOnGold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FJTextSecondary) }
        },
        containerColor = FJSurface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
private fun TrackActionButton(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = FJSurface,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(48.dp).background(FJGold.copy(0.1f), RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = FJGold, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = FJTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(subtitle, color = FJTextSecondary, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = FJTextSecondary)
        }
    }
}

@Composable
private fun ToolButton(title: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = FJSurfaceHigh.copy(0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = FJGold, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = FJTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(subtitle, color = FJTextSecondary, fontSize = 11.sp)
            }
            Icon(Icons.Default.ArrowForward, null, tint = FJTextSecondary, modifier = Modifier.size(16.dp))
        }
    }
}
