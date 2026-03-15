package com.example.fitjourneyag.ui.client.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourneyag.ui.theme.*

@Composable
fun ProgressScreen(
    viewModel: ProgressViewModel,
    onNavigateToWeight: () -> Unit,
    onNavigateToPhotos: () -> Unit,
    onNavigateToStrength: () -> Unit,
    onNavigateToMeasurements: () -> Unit
) {
    val weightHistory by viewModel.weightHistory.collectAsState()
    val frequency by viewModel.workoutFrequency.collectAsState()
    val photos by viewModel.photos.collectAsState()

    Scaffold(
        containerColor = FJBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            item {
                Text("Your Journey", color = FJTextPrimary, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Text("Track your physical transformation", color = FJTextSecondary, fontSize = 14.sp)
            }

            // ── Weight Progress Card ──────────────────────────────
            item {
                val currentWeight = weightHistory.firstOrNull()?.weight ?: 0f
                ProgressHubCard(
                    title = "Weight Progress",
                    subtitle = "Current: $currentWeight kg",
                    icon = Icons.Default.MonitorWeight,
                    onClick = onNavigateToWeight,
                    summaryContent = {
                        if (weightHistory.size >= 2) {
                            val diff = weightHistory.first().weight - weightHistory.last().weight
                            val color = if (diff <= 0) FJGold else FJTextSecondary
                            Text("${if(diff > 0) "+" else ""}$diff kg total change", color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // ── Strength Progress Card ────────────────────────────
            item {
                ProgressHubCard(
                    title = "Strength Progress",
                    subtitle = "$frequency sessions this week",
                    icon = Icons.Default.FitnessCenter,
                    onClick = onNavigateToStrength
                )
            }

            // ── Body Measurements Card ────────────────────────────
            item {
                ProgressHubCard(
                    title = "Body Measurements",
                    subtitle = "Track waist, chest, and more",
                    icon = Icons.Default.Straighten,
                    onClick = onNavigateToMeasurements
                )
            }

            // ── Progress Photos Card ──────────────────────────────
            item {
                ProgressHubCard(
                    title = "Progress Photos",
                    subtitle = "${photos.size} photos uploaded",
                    icon = Icons.Default.PhotoLibrary,
                    onClick = onNavigateToPhotos
                )
            }
        }
    }
}

@Composable
private fun ProgressHubCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit,
    summaryContent: @Composable (ColumnScope.() -> Unit)? = null
) {
    Surface(
        onClick = onClick,
        color = FJSurface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(FJGold.copy(0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = FJGold, modifier = Modifier.size(28.dp))
            }
            Spacer(Modifier.width(20.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = FJTextPrimary, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                Text(subtitle, color = FJTextSecondary, fontSize = 13.sp)
                if (summaryContent != null) {
                    Spacer(Modifier.height(4.dp))
                    summaryContent()
                }
            }
            Icon(Icons.Default.ChevronRight, null, tint = FJTextSecondary)
        }
    }
}
