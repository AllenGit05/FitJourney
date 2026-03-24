package com.example.fitjourney.ui.client.progress

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeightProgressScreen(
    viewModel: ProgressViewModel,
    onNavigateBack: () -> Unit
) {
    val weightHistory by viewModel.weightHistory.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FJBackground,
        topBar = {
            TopAppBar(
                title = { Text("Weight Progress", color = FJTextPrimary, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = FJTextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FJBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = FJGold, contentColor = FJOnGold) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Summary Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = FJSurface),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(Modifier.padding(20.dp)) {
                        Text("Current Weight", color = FJTextSecondary, fontSize = 14.sp)
                        val lastWeight = weightHistory.firstOrNull()?.weight ?: 0f
                        Text("$lastWeight kg", color = FJTextPrimary, fontSize = 32.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }

            // Chart Section
            item {
                Text("Analytical View", color = FJTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                WeightChartDetailed(weightHistory)
            }

            // History List
            item {
                Text("History", color = FJTextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            if (weightHistory.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Text("No weight records yet", color = FJTextSecondary)
                    }
                }
            } else {
                items(weightHistory) { entry ->
                    Surface(
                        color = FJSurface,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(entry.date)),
                                    color = FJTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date(entry.date)),
                                    color = FJTextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("${entry.weight} kg", color = FJGold, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                                Spacer(Modifier.width(8.dp))
                                IconButton(onClick = { viewModel.deleteWeight(entry) }) {
                                    Icon(Icons.Default.Delete, null, tint = FJError.copy(0.6f), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddWeightDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { weight -> viewModel.logWeight(weight); showAddDialog = false }
        )
    }
}

@Composable
private fun WeightChartDetailed(history: List<com.example.fitjourney.domain.model.WeightEntry>) {
    Card(
        modifier = Modifier.fillMaxWidth().height(240.dp),
        colors = CardDefaults.cardColors(containerColor = FJSurface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Box(Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            if (history.size < 2) {
                Text("Add more logs to see progress trends", color = FJTextSecondary, fontSize = 13.sp)
            } else {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    val maxW = history.maxOf { it.weight } + 2
                    val minW = history.minOf { it.weight } - 2
                    val range = maxW - minW
                    
                    val points = history.take(10).reversed()
                    val spacing = size.width / (points.size - 1)
                    
                    val path = androidx.compose.ui.graphics.Path()
                    points.forEachIndexed { i, entry ->
                        val x = i * spacing
                        val y = size.height - ((entry.weight - minW) / range * size.height)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                        drawCircle(FJGold, radius = 5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(x, y))
                    }
                    drawPath(path, FJGold, style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3.dp.toPx()))
                }
            }
        }
    }
}
