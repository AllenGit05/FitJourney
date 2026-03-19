package com.example.fitjourney.ui.client.diet

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.clickable
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.example.fitjourney.ui.theme.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.drawscope.Stroke

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DietTrackingScreen(
    viewModel: DietTrackingViewModel,
    onNavigateBack: () -> Unit
) {
    val totalCalories by viewModel.totalCalories.collectAsState()
    val totalProtein  by viewModel.totalProtein.collectAsState()
    val breakfast     by viewModel.breakfastLogs.collectAsState(initial = emptyList())
    val lunch         by viewModel.lunchLogs.collectAsState(initial = emptyList())
    val dinner        by viewModel.dinnerLogs.collectAsState(initial = emptyList())
    val snacks        by viewModel.snackLogs.collectAsState(initial = emptyList())
    val recentFoods   by viewModel.recentFoods.collectAsState(initial = emptyList())
    val totalProteinLocal by viewModel.totalProtein.collectAsState()
    val totalCarbs by viewModel.totalCarbs.collectAsState()
    val totalFats by viewModel.totalFats.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedMealType by remember { mutableStateOf("Breakfast") }
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        containerColor = FJBackground,
        topBar = {
            Surface(shadowElevation = 4.dp, color = FJBackground) {
                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search food...", color = FJTextSecondary, fontSize = 14.sp) },
                            modifier = Modifier.fillMaxWidth().padding(end = 12.dp).height(48.dp),
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
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // ── Daily Summary ─────────────────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FJSurface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Daily Summary", color = FJTextPrimary, fontWeight = FontWeight.Bold)
                            Text("Today", color = FJTextSecondary, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            SummaryMetric("Calories", "$totalCalories", "kcal", Modifier.weight(1f))
                            SummaryMetric("Protein", "$totalProtein", "g", Modifier.weight(1f))
                        }
                    }
                }
            }

            // ── Macro Breakdown Donut ──────────────────────────
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FJSurface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(
                        Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Macro Distribution", 
                            color = FJTextPrimary, 
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.Start)
                        )
                        Spacer(Modifier.height(24.dp))
                        
                        MacroDonutChart(
                            protein = totalProteinLocal,
                            carbs = totalCarbs,
                            fats = totalFats,
                            totalCalories = totalCalories,
                            modifier = Modifier.size(200.dp)
                        )
                        
                        Spacer(Modifier.height(24.dp))
                        
                        MacroLegend(
                            protein = totalProteinLocal,
                            carbs = totalCarbs,
                            fats = totalFats
                        )
                    }
                }
            }

            // ── Recent Foods ──────────────────────────────────────
            if (recentFoods.isNotEmpty()) {
                item {
                    Column {
                        Text("Recently Logged", color = FJTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Spacer(Modifier.height(12.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(recentFoods) { food ->
                                RecentFoodChip(
                                    food = food, 
                                    onClick = { 
                                        viewModel.addFood(food.name, food.calories, food.protein, food.carbs, food.fats, food.mealType) 
                                    },
                                    onRemove = {
                                        viewModel.removeRecentFood(food.name)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // ── Meal Sections ─────────────────────────────────────
            mealSection("Breakfast", breakfast, onAdd = { selectedMealType = "Breakfast"; showDialog = true }, onRemove = { viewModel.removeFood(it) })
            mealSection("Lunch", lunch, onAdd = { selectedMealType = "Lunch"; showDialog = true }, onRemove = { viewModel.removeFood(it) })
            mealSection("Dinner", dinner, onAdd = { selectedMealType = "Dinner"; showDialog = true }, onRemove = { viewModel.removeFood(it) })
            mealSection("Snacks", snacks, onAdd = { selectedMealType = "Snack"; showDialog = true }, onRemove = { viewModel.removeFood(it) })
        }
    }

    if (showDialog) {
        AddFoodDialog(
            initialMealType = selectedMealType,
            viewModel = viewModel,
            onDismiss = { showDialog = false },
            onAdd = { n, c, p, cb, f, m -> viewModel.addFood(n, c, p, cb, f, m); showDialog = false }
        )
    }
}

private fun LazyListScope.mealSection(title: String, logs: List<com.example.fitjourney.domain.repository.FoodLogEntry>, onAdd: () -> Unit, onRemove: (com.example.fitjourney.domain.repository.FoodLogEntry) -> Unit) {
    item {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, color = FJTextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
            TextButton(onClick = onAdd, contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.Default.Add, null, tint = FJGold, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Add", color = FJGold, fontWeight = FontWeight.Bold)
            }
        }
        Spacer(Modifier.height(8.dp))
        if (logs.isEmpty()) {
            Surface(
                color = FJSurface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("No $title logged", color = FJTextSecondary, modifier = Modifier.padding(16.dp), fontSize = 13.sp)
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                logs.forEach { food -> FoodRow(food, onRemove = { onRemove(food) }) }
            }
        }
    }
}

@Composable
private fun SummaryMetric(label: String, value: String, unit: String, modifier: Modifier) {
    Column(modifier) {
        Text(label, color = FJTextSecondary, fontSize = 12.sp)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = FJGold, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.width(4.dp))
            Text(unit, color = FJTextSecondary, fontSize = 12.sp, modifier = Modifier.padding(bottom = 4.dp))
        }
    }
}

@Composable
private fun RecentFoodChip(
    food: com.example.fitjourney.domain.repository.FoodLogEntry, 
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Surface(
        color = FJSurface,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, FJGold.copy(0.2f))
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                Modifier
                    .clickable { onClick() }
                    .padding(start = 16.dp, end = 4.dp, top = 10.dp, bottom = 10.dp)
            ) {
                Text(food.name, color = FJTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text("${food.calories} kcal", color = FJTextSecondary, fontSize = 11.sp)
            }
            IconButton(
                onClick = onRemove, 
                modifier = Modifier.size(32.dp).padding(end = 4.dp)
            ) {
                Icon(
                    Icons.Default.Delete, 
                    contentDescription = "Remove from recent", 
                    tint = FJTextSecondary.copy(alpha = 0.6f), 
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun FoodRow(food: com.example.fitjourney.domain.repository.FoodLogEntry, onRemove: () -> Unit) {
    Surface(
        color = FJSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(start = 16.dp, end = 8.dp, top = 16.dp, bottom = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(food.name, color = FJTextPrimary, fontWeight = FontWeight.Bold)
                Text("P: ${food.protein}g • C: ${food.carbs}g • F: ${food.fats}g", color = FJTextSecondary, fontSize = 12.sp)
            }
            Text("${food.calories}", color = FJTextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
            Text(" kcal", color = FJTextSecondary, fontSize = 12.sp)
            Spacer(Modifier.width(8.dp))
            IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, null, tint = FJTextSecondary, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun MacroBadge(label: String, value: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(FJSurfaceHigh)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text("$label: $value", color = FJTextSecondary, fontSize = 11.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFoodDialog(
    initialMealType: String = "Breakfast",
    viewModel: DietTrackingViewModel,
    onDismiss: () -> Unit, 
    onAdd: (String, Int, Int, Int, Int, String) -> Unit
) {
    var name     by remember { mutableStateOf("") }
    var cal      by remember { mutableStateOf("") }
    var protein  by remember { mutableStateOf("") }
    var carbs    by remember { mutableStateOf("") }
    var fats     by remember { mutableStateOf("") }
    var mealType by remember { mutableStateOf(initialMealType) }
    
    val isAILoading by viewModel.isAILoading.collectAsState()
    val aiError by viewModel.aiError.collectAsState()

    var showScanner by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            showScanner = true
        } else {
            // Handle permission denied (optional: show snackbar)
        }
    }

    if (showScanner) {
        val hasPermission = remember {
            androidx.core.content.ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }

        if (hasPermission) {
            BarcodeScannerView(
                onBarcodeScanned = { barcode ->
                    showScanner = false
                    viewModel.lookupBarcode(barcode) { n, c, p, cb, f ->
                        name = n; cal = c.toString(); protein = p.toString(); carbs = cb.toString(); fats = f.toString()
                    }
                },
                onClose = { showScanner = false }
            )
        } else {
            LaunchedEffect(Unit) {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
            // Screen remains background or shows loading while asking
        }
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FJSurface,
        title = { 
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Log Food", color = FJTextPrimary, fontWeight = FontWeight.Bold)
                IconButton(onClick = { showScanner = true }) {
                    Icon(Icons.Default.QrCodeScanner, null, tint = FJGold)
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // AI Error Message
                aiError?.let {
                    Text(it, color = Color.Red, fontSize = 11.sp, modifier = Modifier.padding(bottom = 4.dp))
                }

                // Meal Type Selection
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Breakfast", "Lunch", "Dinner", "Snack").forEach { type ->
                        val isSelected = mealType == type
                        Surface(
                            onClick = { mealType = type },
                            color = if (isSelected) FJGold else FJSurfaceHigh,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                type.take(1),
                                modifier = Modifier.padding(vertical = 8.dp),
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = if (isSelected) FJOnGold else FJTextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            placeholder = { Text("Food Name or Description", color = FJTextSecondary, fontSize = 14.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            trailingIcon = {
                                if (isAILoading) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = FJGold)
                                } else {
                                    IconButton(onClick = {
                                        viewModel.parseFoodWithAI(name) { n, c, p, cb, f ->
                                            name = n; cal = c.toString(); protein = p.toString(); carbs = cb.toString(); fats = f.toString()
                                        }
                                    }) {
                                        Icon(Icons.Default.AutoAwesome, "AI Fetch", tint = FJGold, modifier = Modifier.size(20.dp))
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FJGold, focusedContainerColor = FJSurfaceHigh)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = cal, onValueChange = { cal = it },
                            placeholder = { Text("Kcal") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FJGold)
                        )
                        OutlinedTextField(
                            value = protein, onValueChange = { protein = it },
                            placeholder = { Text("Prot") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FJGold)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = carbs, onValueChange = { carbs = it },
                            placeholder = { Text("Carb") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FJGold)
                        )
                        OutlinedTextField(
                            value = fats, onValueChange = { fats = it },
                            placeholder = { Text("Fat") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FJGold)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onAdd(
                        name, 
                        (cal.toIntOrNull() ?: 0).coerceAtLeast(0), 
                        (protein.toIntOrNull() ?: 0).coerceAtLeast(0), 
                        (carbs.toIntOrNull() ?: 0).coerceAtLeast(0), 
                        (fats.toIntOrNull() ?: 0).coerceAtLeast(0), 
                        mealType
                    ) 
                },
                colors = ButtonDefaults.buttonColors(containerColor = FJGold),
                shape = RoundedCornerShape(50),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            ) { Text("Save Entry", fontWeight = FontWeight.Bold) }
        }
    )
}

@Composable
private fun MacroDonutChart(
    protein: Int,
    carbs: Int,
    fats: Int,
    totalCalories: Int,
    modifier: Modifier = Modifier
) {
    val totalGrams = (protein + carbs + fats).toFloat().coerceAtLeast(1f)
    
    val proteinProportion = protein / totalGrams
    val carbsProportion = carbs / totalGrams
    val fatsProportion = fats / totalGrams

    var animationTriggered by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animationTriggered = true }

    val proteinSweep by animateFloatAsState(
        targetValue = if (animationTriggered) proteinProportion * 360f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumLow, stiffness = Spring.StiffnessLow)
    )
    val carbsSweep by animateFloatAsState(
        targetValue = if (animationTriggered) carbsProportion * 360f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumLow, stiffness = Spring.StiffnessLow)
    )
    val fatsSweep by animateFloatAsState(
        targetValue = if (animationTriggered) fatsProportion * 360f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumLow, stiffness = Spring.StiffnessLow)
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 18.dp.toPx()
            val innerRadius = (size.minDimension - strokeWidth) / 2
            
            // Draw Fats
            drawArc(
                color = FJFats,
                startAngle = -90f,
                sweepAngle = fatsSweep,
                useCenter = false,
                style = Stroke(strokeWidth)
            )
            
            // Draw Carbs
            drawArc(
                color = FJCarbs,
                startAngle = -90f + fatsSweep,
                sweepAngle = carbsSweep,
                useCenter = false,
                style = Stroke(strokeWidth)
            )
            
            // Draw Protein
            drawArc(
                color = FJGold,
                startAngle = -90f + fatsSweep + carbsSweep,
                sweepAngle = proteinSweep,
                useCenter = false,
                style = Stroke(strokeWidth)
            )
        }
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$totalCalories",
                color = FJTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "kcal",
                color = FJTextSecondary,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun MacroLegend(protein: Int, carbs: Int, fats: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        LegendItem("Protein", "${protein}g", FJGold)
        LegendItem("Carbs", "${carbs}g", FJCarbs)
        LegendItem("Fats", "${fats}g", FJFats)
    }
}

@Composable
private fun LegendItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(androidx.compose.foundation.shape.CircleShape)
                    .background(color)
            )
            Spacer(Modifier.width(6.dp))
            Text(label, color = FJTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
        Text(value, color = FJTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
