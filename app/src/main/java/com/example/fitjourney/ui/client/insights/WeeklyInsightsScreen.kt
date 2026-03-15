package com.example.fitjourney.ui.client.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.fitjourney.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeeklyInsightsScreen(
    viewModel: WeeklyInsightsViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    
    val snackbarHostState = remember { SnackbarHostState() }

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
                uiState.error != null -> {
                    // Show error card
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2A1A1A))
                    ) {
                        Text(
                            text = "⚠️ ${uiState.error}",
                            color = FJError,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                uiState.currentReport != null -> {
                    // Parse and display the 5 sections from aiAnalysis
                    val sections = uiState.currentReport!!.aiAnalysis.split("\n\n")
                    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        items(sections) { section ->
                            if (section.isNotBlank()) {
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    colors = CardDefaults.cardColors(containerColor = FJSurface)
                                ) {
                                    Text(
                                        text = section.trim(),
                                        modifier = Modifier.padding(16.dp),
                                        color = FJTextPrimary,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }
                else -> {
                    // Show generate button
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(32.dp)
                        ) {
                            Text(
                                "📊 Weekly AI Report",
                                style = MaterialTheme.typography.headlineSmall,
                                color = FJGold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Get a personalized analysis of your week including wins, areas to improve, and next week's targets.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = FJTextSecondary,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(Modifier.height(24.dp))
                            // Use your existing FJGoldButton or Button component
                            Button(
                                onClick = { viewModel.generateWeeklyReport() },
                                colors = ButtonDefaults.buttonColors(containerColor = FJGold)
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
