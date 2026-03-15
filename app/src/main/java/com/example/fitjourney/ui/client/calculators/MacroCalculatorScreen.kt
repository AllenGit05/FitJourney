package com.example.fitjourney.ui.client.calculators

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MacroCalculatorScreen(
    viewModel: CalculatorsViewModel,
    onNavigateBack: () -> Unit
) {
    val macrosResult by viewModel.macrosResult.collectAsState()
    val tdeeResult by viewModel.tdeeResult.collectAsState()
    
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var age    by remember { mutableStateOf("") }
    var isMale by remember { mutableStateOf(true) }
    
    val activityLevels = listOf(
        "Sedentary" to 1.2f,
        "Lightly Active" to 1.375f,
        "Moderately Active" to 1.55f,
        "Very Active" to 1.725f,
        "Extra Active" to 1.9f
    )
    var selectedActivityIndex by remember { mutableIntStateOf(0) }
    
    val goals = listOf("Fat Loss", "Maintenance", "Muscle Gain")
    var selectedGoalIndex by remember { mutableIntStateOf(1) }
    
    var showManualMode by remember { mutableStateOf(false) }
    var manualCalories by remember { mutableStateOf("") }

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedBorderColor = FJGold, unfocusedBorderColor = FJDivider,
        focusedContainerColor = FJSurface, unfocusedContainerColor = FJSurface,
        focusedTextColor = FJTextPrimary, unfocusedTextColor = FJTextPrimary,
        cursorColor = FJGold
    )

    Scaffold(
        containerColor = FJBackground,
        topBar = {
            TopAppBar(
                title = { Text("Macro Calculator", color = FJTextPrimary, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = FJTextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FJBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp)
        ) {
            item {
                Text(
                    "Enter your details to get a personalized macro breakdown based on your biology and activity level.", 
                    color = FJTextSecondary, 
                    fontSize = 14.sp
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FJSurface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        // Section 1: Basic Stats
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            OutlinedTextField(
                                value = weight, onValueChange = { weight = it },
                                label = { Text("Weight (kg)") },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                                colors = fieldColors, singleLine = true
                            )
                            OutlinedTextField(
                                value = height, onValueChange = { height = it },
                                label = { Text("Height (cm)") },
                                modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp),
                                colors = fieldColors, singleLine = true
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = age, onValueChange = { age = it },
                                label = { Text("Age") },
                                modifier = Modifier.weight(0.5f), shape = RoundedCornerShape(12.dp),
                                colors = fieldColors, singleLine = true
                            )
                            Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                val maleColor = if (isMale) FJGold else FJSurfaceHigh
                                val femaleColor = if (!isMale) FJGold else FJSurfaceHigh
                                
                                Button(
                                    onClick = { isMale = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = maleColor),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) { Text("Male", fontSize = 12.sp, color = if (isMale) FJOnGold else FJTextSecondary) }
                                
                                Button(
                                    onClick = { isMale = false },
                                    colors = ButtonDefaults.buttonColors(containerColor = femaleColor),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) { Text("Female", fontSize = 12.sp, color = if (!isMale) FJOnGold else FJTextSecondary) }
                            }
                        }

                        // Section 2: Activity Level
                        Text("Activity Level", color = FJTextSecondary, fontSize = 12.sp)
                        com.example.fitjourney.ui.common.SimpleFlowRow(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
                            activityLevels.forEachIndexed { index, pair ->
                                FilterChip(
                                    selected = selectedActivityIndex == index,
                                    onClick = { selectedActivityIndex = index },
                                    label = { Text(pair.first, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = FJGold,
                                        selectedLabelColor = FJOnGold,
                                        containerColor = FJSurfaceHigh,
                                        labelColor = FJTextSecondary
                                    ),
                                    border = null
                                )
                            }
                        }

                        // Section 3: Goal
                        Text("Your Fitness Goal", color = FJTextSecondary, fontSize = 12.sp)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            goals.forEachIndexed { index, g ->
                                val isSelected = selectedGoalIndex == index
                                Button(
                                    onClick = { selectedGoalIndex = index },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isSelected) FJGold else FJSurfaceHigh
                                    ),
                                    modifier = Modifier.weight(1f).height(40.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Text(g, fontSize = 10.sp, color = if (isSelected) FJOnGold else FJTextSecondary, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        // Manual Mode Toggle
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 8.dp)) {
                            Checkbox(
                                checked = showManualMode,
                                onCheckedChange = { showManualMode = it },
                                colors = CheckboxDefaults.colors(checkedColor = FJGold)
                            )
                            Text("I want to set calories manually", color = FJTextSecondary, fontSize = 12.sp)
                        }

                        AnimatedVisibility(visible = showManualMode) {
                            OutlinedTextField(
                                value = manualCalories, onValueChange = { manualCalories = it },
                                label = { Text("Daily Calorie Target") },
                                placeholder = { Text("e.g. 2000") },
                                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                                colors = fieldColors, singleLine = true
                            )
                        }

                        Button(
                            onClick = { 
                                val weightKg = weight.toFloatOrNull() ?: 0f
                                if (showManualMode) {
                                    val cals = manualCalories.toIntOrNull() ?: 0
                                    viewModel.calculateMacros(goals[selectedGoalIndex], cals, weightKg)
                                } else {
                                    viewModel.calculateFullMacros(
                                        goal = goals[selectedGoalIndex],
                                        weightKg = weightKg,
                                        heightCm = height.toFloatOrNull() ?: 0f,
                                        age = age.toIntOrNull() ?: 0,
                                        isMale = isMale,
                                        activityMultiplier = activityLevels[selectedActivityIndex].second
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold)
                        ) {
                            Text("Calculate Macros", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            if (macrosResult.calories > 0) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FJSurface),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(Modifier.padding(24.dp)) {
                            Text("Your Personalized Daily Targets", color = FJTextSecondary, fontSize = 14.sp, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                            Spacer(Modifier.height(24.dp))
                            
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                MacroBox("Protein", "${macrosResult.protein}g", FJGold, Modifier.weight(1f))
                                MacroBox("Carbs", "${macrosResult.carbs}g", Color(0xFF4CAF50), Modifier.weight(1f))
                                MacroBox("Fats", "${macrosResult.fats}g", Color(0xFF2196F3), Modifier.weight(1f))
                            }
                            
                            Spacer(Modifier.height(24.dp))
                            
                            Box(
                                Modifier.fillMaxWidth()
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(FJGold.copy(0.1f))
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        "Target: ${macrosResult.calories} kcal/day",
                                        color = FJGold,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 18.sp
                                    )
                                    if (!showManualMode && tdeeResult > 0) {
                                        Text(
                                            "Estimated Maintenance: ${tdeeResult.toInt()} kcal",
                                            color = FJGold.copy(0.7f),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroBox(label: String, value: String, color: Color, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(Modifier.height(8.dp))
        Text(value, color = FJTextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp)
        Text(label, color = FJTextSecondary, fontSize = 12.sp)
    }
}
