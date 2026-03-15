package com.example.fitjourneyag.ui.client.calculators

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MonitorWeight
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
fun BmiCalculatorScreen(
    viewModel: CalculatorsViewModel,
    onNavigateBack: () -> Unit
) {
    val bmiResult by viewModel.bmiResult.collectAsState()
    var weight by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }

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
                title = { Text("BMI Calculator", color = FJTextPrimary, fontWeight = FontWeight.ExtraBold) },
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
                Text("Enter your details to calculate your Body Mass Index.", color = FJTextSecondary, fontSize = 14.sp)
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = FJSurface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        OutlinedTextField(
                            value = weight, onValueChange = { weight = it },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = fieldColors, singleLine = true
                        )
                        OutlinedTextField(
                            value = height, onValueChange = { height = it },
                            label = { Text("Height (cm)") },
                            modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp),
                            colors = fieldColors, singleLine = true
                        )
                        
                        Button(
                            onClick = { 
                                viewModel.calculateBMI(weight.toFloatOrNull() ?: 0f, height.toFloatOrNull() ?: 0f) 
                            },
                            modifier = Modifier.fillMaxWidth().height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold)
                        ) {
                            Text("Calculate BMI", fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }
            }

            if (bmiResult > 0) {
                item {
                    val cat = when {
                        bmiResult < 18.5 -> "Underweight"
                        bmiResult < 25.0 -> "Normal"
                        bmiResult < 30.0 -> "Overweight"
                        else             -> "Obese"
                    }
                    Card(
                        colors = CardDefaults.cardColors(containerColor = FJSurface),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            Modifier.padding(24.dp).fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Your Body Mass Index", color = FJTextSecondary, fontSize = 14.sp)
                            Text(
                                String.format("%.1f", bmiResult),
                                color = if (cat == "Normal") Color(0xFF4CAF50) else Color(0xFFFFA000),
                                fontSize = 48.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(cat, color = FJTextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            
                            Spacer(Modifier.height(16.dp))
                            
                            // Simple scale visualization
                            Box(Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(FJDivider)) {
                                val progress = (bmiResult / 40f).coerceIn(0f, 1f)
                                Box(Modifier.fillMaxWidth(progress).fillMaxHeight().background(FJGold))
                            }
                        }
                    }
                }
            }
        }
    }
}
