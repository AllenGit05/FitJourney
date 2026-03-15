package com.example.fitjourneyag.ui.client.calculators

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourneyag.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TdeeCalculatorScreen(
    viewModel: CalculatorsViewModel,
    onNavigateBack: () -> Unit
) {
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
                title = { Text("TDEE Calculator", color = FJTextPrimary, fontWeight = FontWeight.ExtraBold) },
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
                Text("Total Daily Energy Expenditure is the number of calories you burn per day.", color = FJTextSecondary, fontSize = 14.sp)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FJSurface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
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

                        Text("Activity Level", color = FJTextSecondary, fontSize = 12.sp)
                        com.example.fitjourneyag.ui.common.SimpleFlowRow(horizontalSpacing = 8.dp, verticalSpacing = 8.dp) {
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

                        Button(
                            onClick = { 
                                viewModel.calculateTDEE(
                                    weightKg = weight.toFloatOrNull() ?: 0f,
                                    heightCm = height.toFloatOrNull() ?: 0f,
                                    age = age.toIntOrNull() ?: 0,
                                    isMale = isMale,
                                    activityMultiplier = activityLevels[selectedActivityIndex].second
                                ) 
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold)
                        ) {
                            Text("Calculate TDEE", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            if (tdeeResult > 0) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FJSurface),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Daily Maintenance Calories", color = FJTextSecondary, fontSize = 14.sp)
                            Text(
                                "${tdeeResult.toInt()} kcal",
                                color = FJGold,
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text("to stay at your current weight", color = FJTextSecondary, fontSize = 14.sp)
                            
                            Spacer(Modifier.height(20.dp))
                            
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Fat Loss", color = FJTextSecondary, fontSize = 12.sp)
                                    Text("${(tdeeResult - 500).toInt()}", color = Color(0xFFFFA000), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                                Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Muscle Gain", color = FJTextSecondary, fontSize = 12.sp)
                                    Text("${(tdeeResult + 300).toInt()}", color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
