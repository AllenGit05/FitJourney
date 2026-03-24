package com.example.fitjourney.ui.client.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Straighten
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

import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import com.example.fitjourney.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BodyMeasurementsScreen(
    viewModel: ProgressViewModel,
    onNavigateBack: () -> Unit
) {
    val measurements by viewModel.bodyMeasurements.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FJBackground,
        topBar = {
            TopAppBar(
                title = { Text("Body Measurements", color = FJTextPrimary, fontWeight = FontWeight.Bold) },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("Track your progress beyond the scale.", color = FJTextSecondary, fontSize = 14.sp)
            }

            if (measurements.isEmpty()) {
                item {
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Straighten, null, tint = FJSurfaceHigh, modifier = Modifier.size(48.dp))
                        Spacer(Modifier.height(16.dp))
                        Text("No measurements logged yet.", color = FJTextSecondary)
                    }
                }
            } else {
                items(measurements) { log ->
                    MeasurementLogCard(log, onDelete = { viewModel.deleteMeasurement(log) })
                }
            }
        }
    }

    if (showAddDialog) {
        AddMeasurementDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { w, c, a, h, l -> 
                viewModel.logMeasurements(w, c, a, h, l)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun MeasurementLogCard(log: com.example.fitjourney.domain.model.BodyMeasurement, onDelete: () -> Unit) {
    Surface(
        color = FJSurface,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(log.date)),
                    modifier = Modifier.weight(1f),
                    color = FJTextPrimary,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = FJError.copy(0.7f), modifier = Modifier.size(20.dp))
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MeasurementBadge("Waist", "${log.waist}cm", Modifier.weight(1f))
                MeasurementBadge("Chest", "${log.chest}cm", Modifier.weight(1f))
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                MeasurementBadge("Arms", "${log.arms}cm", Modifier.weight(1f))
                MeasurementBadge("Hips", "${log.hips}cm", Modifier.weight(1f))
                MeasurementBadge("Legs", "${log.legs}cm", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun MeasurementBadge(label: String, value: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = FJTextSecondary, fontSize = 11.sp)
        Text(value, color = FJGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}

@Composable
fun AddMeasurementDialog(onDismiss: () -> Unit, onConfirm: (Float, Float, Float, Float, Float) -> Unit) {
    var waist by remember { mutableStateOf("") }
    var chest by remember { mutableStateOf("") }
    var arms by remember { mutableStateOf("") }
    var hips by remember { mutableStateOf("") }
    var legs by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Measurements") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                MeasurementInput("Waist (cm)", waist) { waist = it }
                MeasurementInput("Chest (cm)", chest) { chest = it }
                MeasurementInput("Arms (cm)", arms) { arms = it }
                MeasurementInput("Hips (cm)", hips) { hips = it }
                MeasurementInput("Legs (cm)", legs) { legs = it }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onConfirm(
                        waist.toFloatOrNull() ?: 0f,
                        chest.toFloatOrNull() ?: 0f,
                        arms.toFloatOrNull() ?: 0f,
                        hips.toFloatOrNull() ?: 0f,
                        legs.toFloatOrNull() ?: 0f
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = FJGold)
            ) {
                Text("Log", color = FJOnGold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        containerColor = FJSurface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun MeasurementInput(label: String, value: String, onValueChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) onValueChange(it) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth(),
        keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
        shape = RoundedCornerShape(12.dp)
    )
}
