package com.example.fitjourney.ui.client.insights

import kotlinx.coroutines.launch
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.ui.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyInsightsScreen(
    viewModel: WeeklyInsightsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        containerColor = FJBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Weekly Insights", color = FJTextPrimary, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = FJTextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FJBackground)
            )
        },
        floatingActionButton = {
            if (uiState.currentReport != null) {
                FloatingActionButton(
                    onClick = { 
                        viewModel.saveReport(context, uiState.currentReport!!)
                        scope.launch {
                            snackbarHostState.showSnackbar("Report saved to Downloads!")
                        }
                    },
                    containerColor = FJGold,
                    contentColor = FJOnGold
                ) {
                    Icon(Icons.Default.Download, "Download Report")
                }
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when {
                uiState.isGenerating -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = FJGold)
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "Analyzing your week...",
                                color = FJTextSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                uiState.currentReport != null -> {
                    val report = uiState.currentReport!!
                    val sections = report.aiAnalysis.split("\n\n")

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // 1. Stat Cards Grid
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    InsightStatCard("Workouts", "${report.totalWorkouts}", Icons.Default.FitnessCenter, Modifier.weight(1f))
                                    InsightStatCard("Avg Steps", "${report.averageSteps}", Icons.Default.DirectionsWalk, Modifier.weight(1f))
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    InsightStatCard("Avg Calories", "${report.averageCalories.toInt()}", Icons.Default.LocalFireDepartment, Modifier.weight(1f))
                                    InsightStatCard("Water Intake", "${report.averageWaterMl}ml", Icons.Default.WaterDrop, Modifier.weight(1f))
                                }
                            }
                        }

                        // 2. Weight Change Row
                        item {
                            val weightText = when {
                                report.weightChangeKg > 0 -> "+${String.format("%.1f", report.weightChangeKg)}kg gained"
                                report.weightChangeKg < 0 -> "${String.format("%.1f", -report.weightChangeKg)}kg lost"
                                else -> "No change"
                            }
                            val weightColor = when {
                                report.weightChangeKg > 0 -> FJError
                                report.weightChangeKg < 0 -> FJSuccess
                                else -> FJTextSecondary
                            }
                            
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                color = FJSurface
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Scale, null, tint = FJGold, modifier = Modifier.size(20.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("Weight Trend:", color = FJTextSecondary, fontSize = 14.sp)
                                    Spacer(Modifier.width(8.dp))
                                    Text(weightText, color = weightColor, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }
                        }

                        // 3. Progress Bars
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = FJSurface),
                                shape = RoundedCornerShape(16.dp)
                            ) {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Text("Week at a Glance", color = FJGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    
                                    InsightProgressBar("Steps (Goal: 10k)", report.averageSteps / 10000f, FJGold)
                                    InsightProgressBar("Water (Goal: 2.5L)", report.averageWaterMl / 2500f, Color(0xFF2196F3))
                                    InsightProgressBar("Workouts (Max: 7)", report.totalWorkouts / 7f, FJSuccess)
                                }
                            }
                        }

                        // 4. AI Analysis Sections
                        items(sections) { section ->
                            if (section.isNotBlank()) {
                                val trimmed = section.trim()
                                val accentColor = when {
                                    trimmed.contains("WINS", ignoreCase = true) -> FJSuccess
                                    trimmed.contains("IMPROVE", ignoreCase = true) -> Color(0xFFFF9800)
                                    trimmed.contains("NUTRITION", ignoreCase = true) -> FJCarbs
                                    trimmed.contains("FOCUS", ignoreCase = true) || trimmed.contains("NEXT WEEK", ignoreCase = true) -> FJGold
                                    trimmed.contains("COACH", ignoreCase = true) || trimmed.contains("MESSAGE", ignoreCase = true) -> FJSurfaceHigh
                                    else -> FJGold
                                }

                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = FJSurface),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Box {
                                        // Left border accent
                                        Box(
                                            Modifier
                                                .width(4.dp)
                                                .fillMaxHeight()
                                                .align(Alignment.CenterStart)
                                                .background(accentColor)
                                        )
                                        
                                        Text(
                                            text = trimmed,
                                            modifier = Modifier.padding(16.dp).padding(start = 8.dp),
                                            color = FJTextPrimary,
                                            style = MaterialTheme.typography.bodyMedium,
                                            lineHeight = 22.sp
                                        )
                                    }
                                }
                            }
                        }
                        
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
                else -> {
                    // Show generate button
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                            Text("📊 Weekly AI Report", style = MaterialTheme.typography.headlineSmall, color = FJGold)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Get a personalized analysis of your week including wins, areas to improve, and next week's targets.",
                                style = MaterialTheme.typography.bodyMedium, color = FJTextSecondary, textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = { viewModel.generateWeeklyReport() },
                                colors = ButtonDefaults.buttonColors(containerColor = FJGold),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.height(54.dp).fillMaxWidth(0.8f)
                            ) {
                                Text("Generate Report (3 Credits)", color = FJOnGold, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InsightStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = FJSurface
    ) {
        Column(Modifier.padding(16.dp)) {
            Icon(icon, null, tint = FJGold, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(12.dp))
            Text(value, color = FJTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, color = FJTextSecondary, fontSize = 12.sp)
        }
    }
}

@Composable
fun InsightProgressBar(label: String, progress: Float, color: Color) {
    val clampedProgress = progress.coerceIn(0f, 1f)
    Column {
        Text(label, color = FJTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Canvas(modifier = Modifier.fillMaxWidth().height(12.dp)) {
            // Track
            drawRoundRect(
                color = Color.Gray.copy(0.2f),
                size = size,
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )
            // Progress
            drawRoundRect(
                color = color,
                size = Size(size.width * clampedProgress, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
            )
        }
    }
}

