package com.example.fitjourney.ui.client.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Circle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.domain.model.*
import com.example.fitjourney.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTrackingScreen(
    viewModel: WorkoutTrackingViewModel,
    onNavigateBack: () -> Unit
) {
    val session       by viewModel.workoutSession.collectAsState()
    val totalCalories by viewModel.totalCaloriesBurned.collectAsState()
    val totalDuration by viewModel.totalDurationMinutes.collectAsState()
    val isFinished    by viewModel.isFinished.collectAsState()
    val activePlan     by viewModel.activePlan.collectAsState()
    val restRemaining  by viewModel.restTimeRemaining.collectAsState()
    
    val templates      by viewModel.templates.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var startTab      by remember { mutableIntStateOf(0) }
    var showSaveTemplateDialog by remember { mutableStateOf(false) }
    var showSummary   by remember { mutableStateOf(false) }
    var searchQuery    by remember { mutableStateOf("") }

    LaunchedEffect(isFinished) {
        if (isFinished) showSummary = true
    }

    Scaffold(
        containerColor = FJBackground,
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search exercise...", color = FJTextSecondary, fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = FJSurface,
                                unfocusedContainerColor = FJSurface,
                                focusedBorderColor = FJGold,
                                unfocusedBorderColor = Color.Transparent
                            )
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = FJTextPrimary) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = FJBackground)
                )
                
                // Rest Timer Banner
                if (restRemaining > 0) {
                    Surface(
                        color = FJGold,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(horizontal = 20.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Timer, null, tint = FJOnGold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Rest Timer:", color = FJOnGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(4.dp))
                            Text("${restRemaining}s", color = FJOnGold, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }
        },
        bottomBar = {
            if (session.isNotEmpty()) {
                Surface(color = FJSurface, shadowElevation = 16.dp) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Session Summary", color = FJTextSecondary, fontSize = 11.sp)
                            Text("$totalCalories kcal • $totalDuration min", color = FJTextPrimary, fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { viewModel.finishWorkout() },
                            colors = ButtonDefaults.buttonColors(containerColor = FJGold),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(48.dp)
                        ) { Text("Finish", fontWeight = FontWeight.Bold) }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { 
                    startTab = if (templates.isNotEmpty()) 1 else 0
                    showAddDialog = true 
                }, 
                containerColor = FJGold, 
                contentColor = FJOnGold
            ) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            if (session.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FitnessCenter, null, tint = FJSurfaceHigh, modifier = Modifier.size(64.dp))
                            Spacer(Modifier.height(16.dp))
                            Text("Start your session by adding an exercise", color = FJTextSecondary)
                        }
                    }
                }
            } else {
                items(session) { exercise ->
                    InteractiveExerciseCard(
                        exercise = exercise,
                        onToggleSet = { idx -> viewModel.toggleSetCompletion(exercise.name, idx) },
                        onAddSet = { viewModel.addSetToExercise(exercise.name) },
                        onUpdateSet = { idx, r, w -> viewModel.updateSetValues(exercise.name, idx, r, w) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddExerciseDialog(
            viewModel = viewModel,
            startTab = startTab,
            onDismiss = { showAddDialog = false },
            onAdd = { name, restTime ->
                viewModel.addExercise(name, listOf(WorkoutSet(0, 0f)), 0, 0, restTime)
                showAddDialog = false
            },
            onSaveTemplateRequest = {
                showAddDialog = false
                showSaveTemplateDialog = true
            }
        )
    }

    if (showSaveTemplateDialog) {
        SaveTemplateDialog(
            onDismiss = { showSaveTemplateDialog = false },
            onSave = { name ->
                val exercises = session.map { TemplateExercise(it.name, it.sets.size, it.restTimeSeconds) }
                viewModel.saveTemplate(name, exercises)
                showSaveTemplateDialog = false
            }
        )
    }

    if (showSummary) {
        WorkoutSummaryDialog(
            viewModel = viewModel,
            duration = totalDuration,
            calories = totalCalories,
            exerciseCount = session.size,
            onDismiss = { showSummary = false ; onNavigateBack() }
        )
    }
}

@Composable
fun InteractiveExerciseCard(
    exercise: Exercise,
    onToggleSet: (Int) -> Unit,
    onAddSet: () -> Unit,
    onUpdateSet: (Int, Int, Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FJSurface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(exercise.name, color = FJTextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                IconButton(onClick = onAddSet, modifier = Modifier.size(32.dp).clip(CircleShape).background(FJGold.copy(0.1f))) {
                    Icon(Icons.Default.Add, null, tint = FJGold, modifier = Modifier.size(16.dp))
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                exercise.sets.forEachIndexed { index, set ->
                    SetTrackingRow(
                        index = index + 1,
                        set = set,
                        onToggle = { onToggleSet(index) },
                        onUpdate = { r, w -> onUpdateSet(index, r, w) }
                    )
                }
            }
        }
    }
}

@Composable
fun SetTrackingRow(
    index: Int,
    set: WorkoutSet,
    onToggle: () -> Unit,
    onUpdate: (Int, Float) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth().height(48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            Modifier.size(28.dp).clip(CircleShape).background(if (set.isCompleted) FJGold else FJSurfaceHigh),
            contentAlignment = Alignment.Center
        ) {
            Text("$index", color = if (set.isCompleted) FJOnGold else FJTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        
        Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = if (set.reps == 0) "" else "${set.reps}",
                onValueChange = { it.toIntOrNull()?.let { r -> onUpdate(r, set.weight) } },
                modifier = Modifier.weight(1f).height(44.dp),
                placeholder = { Text("Reps", fontSize = 10.sp) },
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FJGold)
            )
            OutlinedTextField(
                value = if (set.weight == 0f) "" else "${set.weight}",
                onValueChange = { it.toFloatOrNull()?.let { w -> onUpdate(set.reps, w) } },
                modifier = Modifier.weight(1f).height(44.dp),
                placeholder = { Text("kg", fontSize = 10.sp) },
                shape = RoundedCornerShape(8.dp),
                singleLine = true,
                textStyle = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FJGold)
            )
        }

        IconButton(onClick = onToggle) {
            Icon(
                if (set.isCompleted) Icons.Default.CheckCircle else Icons.Default.Circle,
                null,
                tint = if (set.isCompleted) FJGold else FJTextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExerciseDialog(
    initialName: String = "", 
    viewModel: WorkoutTrackingViewModel,
    startTab: Int = 0,
    onDismiss: () -> Unit, 
    onAdd: (String, Int) -> Unit,
    onSaveTemplateRequest: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(startTab) }
    var name     by remember { mutableStateOf(initialName) }
    var restTime by remember { mutableStateOf("60") }
    
    val templates by viewModel.templates.collectAsState()
    val session by viewModel.workoutSession.collectAsState()

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = FJGold, unfocusedBorderColor = FJDivider,
        focusedContainerColor = FJSurfaceHigh, unfocusedContainerColor = FJSurfaceHigh,
        focusedTextColor = FJTextPrimary, unfocusedTextColor = FJTextPrimary
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FJSurface,
        title = {
            Column {
                Text("Log Exercise", color = FJTextPrimary, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(16.dp))
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = Color.Transparent,
                    contentColor = FJGold,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = FJGold
                        )
                    },
                    divider = {}
                ) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }) {
                        Text("Manual", modifier = Modifier.padding(vertical = 8.dp), fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal)
                    }
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }) {
                        Text("Templates", modifier = Modifier.padding(vertical = 8.dp), fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal)
                    }
                }
            }
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                if (selectedTab == 0) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedTextField(
                            value = name, 
                            onValueChange = { name = it },
                            placeholder = { Text("Exercise Name", color = FJTextSecondary) },
                            singleLine = true, 
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), 
                            colors = fieldColors
                        )
                        
                        OutlinedTextField(
                            value = restTime, 
                            onValueChange = { if (it.all { c -> c.isDigit() }) restTime = it },
                            placeholder = { Text("Rest Time (seconds)", color = FJTextSecondary) },
                            singleLine = true, 
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp), 
                            colors = fieldColors,
                            trailingIcon = {
                                Icon(Icons.Default.Timer, null, tint = FJGold, modifier = Modifier.size(20.dp))
                            }
                        )
                    }
                } else {
                    Column(Modifier.fillMaxWidth()) {
                        if (templates.isEmpty()) {
                            Box(Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                                Text("No templates saved yet", color = FJTextSecondary, fontSize = 14.sp)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                                items(templates.size) { index ->
                                    val template = templates[index]
                                    Surface(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                        color = FJSurfaceHigh,
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(Modifier.weight(1f)) {
                                                Text(template.name, color = FJTextPrimary, fontWeight = FontWeight.Bold)
                                                Text("${template.exercises.size} exercises", color = FJTextSecondary, fontSize = 12.sp)
                                            }
                                            Button(
                                                onClick = {
                                                    template.exercises.forEach { ex ->
                                                        val sets = List(ex.defaultSets) { WorkoutSet(0, 0f) }
                                                        viewModel.addExercise(ex.name, sets, 0, 0, ex.restTimeSeconds)
                                                    }
                                                    onDismiss()
                                                },
                                                colors = ButtonDefaults.buttonColors(containerColor = FJGold),
                                                contentPadding = PaddingValues(horizontal = 12.dp),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.height(32.dp)
                                            ) {
                                                Text("Use", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (session.isNotEmpty()) {
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = onSaveTemplateRequest,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FJGold),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = FJGold)
                            ) {
                                Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Save Current as Template", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            if (selectedTab == 0) {
                Button(
                    onClick = { 
                        onAdd(name, restTime.toIntOrNull() ?: 60) 
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold),
                    shape = RoundedCornerShape(50)
                ) { Text("Start Tracking", fontWeight = FontWeight.Bold) }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FJTextSecondary) }
        }
    )
}

@Composable
fun SaveTemplateDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FJSurface,
        title = { Text("Save Workout Template", color = FJTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Template Name (e.g. Leg Day)", color = FJTextSecondary) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FJGold,
                    unfocusedBorderColor = FJDivider,
                    focusedContainerColor = FJSurfaceHigh,
                    unfocusedContainerColor = FJSurfaceHigh
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { if (name.isNotBlank()) onSave(name) },
                enabled = name.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold),
                shape = RoundedCornerShape(50)
            ) { Text("Save", fontWeight = FontWeight.Bold) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FJTextSecondary) }
        }
    )
}

@Composable
fun WorkoutSummaryDialog(
    viewModel: WorkoutTrackingViewModel,
    duration: Int, 
    calories: Int, 
    exerciseCount: Int, 
    onDismiss: () -> Unit
) {
    val isAiLoading by viewModel.isAiLoading.collectAsState()
    val aiError by viewModel.aiError.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FJSurface,
        title = { Text("Workout Complete! 🏆", color = FJTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Default.CheckCircle, null, tint = FJGold, modifier = Modifier.size(64.dp))
                Spacer(Modifier.height(16.dp))
                Text("Great job on your session!", color = FJTextSecondary)
                Spacer(Modifier.height(24.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    SummaryItem("$duration", "Mins")
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            SummaryItem("$calories", "Kcal")
                            Spacer(Modifier.width(4.dp))
                            if (isAiLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = FJGold, strokeWidth = 2.dp)
                            } else {
                                IconButton(onClick = { viewModel.calculateSessionCaloriesAI() }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Default.AutoAwesome, "AI Calculate", tint = FJGold, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                        if (aiError != null) {
                            Text("AI Error", color = Color.Red, fontSize = 8.sp)
                        }
                    }

                    SummaryItem("$exerciseCount", "Exercises")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold),
                shape = RoundedCornerShape(50)
            ) { Text("Awesome!", fontWeight = FontWeight.Bold) }
        }
    )
}

@Composable
private fun SummaryItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = FJTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
        Text(label, color = FJTextSecondary, fontSize = 12.sp)
    }
}
