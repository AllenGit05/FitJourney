package com.example.fitjourney.ui.admin.diagnostic

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FirebaseDiagnosticScreen(
    viewModel: FirebaseDiagnosticViewModel,
    onNavigateBack: () -> Unit
) {
    val testResults by viewModel.testResults.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    // Auto-scroll to running test
    LaunchedEffect(testResults) {
        val runningIndex = testResults.indexOfFirst { it.status == TestStatus.RUNNING }
        if (runningIndex != -1) {
            scope.launch {
                listState.animateScrollToItem(runningIndex)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Firebase Diagnostics", color = FJTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FJTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FJBackground)
            )
        },
        bottomBar = {
            SummaryBar(testResults)
        },
        containerColor = FJBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Run button
            Button(
                onClick = { viewModel.runAllTests() },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) FJSurfaceHigh else FJGold,
                    contentColor = if (isRunning) FJTextSecondary else FJOnGold
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = !isRunning
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                        contentDescription = null
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        if (isRunning) "⏹ Running..." else "▶ Run All Tests",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val grouped = testResults.groupBy { it.group }
                grouped.forEach { (group, results) ->
                    item {
                        Text(
                            text = group,
                            color = FJGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                        )
                    }
                    items(results) { result ->
                        TestResultCard(result)
                    }
                }
            }
        }
    }
}

@Composable
fun TestResultCard(result: TestResult) {
    var expanded by remember { mutableStateOf(false) }

    Surface(
        onClick = { if (result.status == TestStatus.FAIL || result.detail.isNotBlank()) expanded = !expanded },
        color = FJSurface,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = result.name,
                    color = FJTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.weight(1f)
                )
                StatusBadge(result.status)
            }

            AnimatedVisibility(visible = expanded || result.status == TestStatus.RUNNING) {
                if (result.detail.isNotBlank()) {
                    Column {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = result.detail,
                            color = if (result.status == TestStatus.FAIL) FJError else FJTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: TestStatus) {
    val color = when (status) {
        TestStatus.PENDING -> Color.Gray
        TestStatus.RUNNING -> FJGold
        TestStatus.PASS -> FJSuccess
        TestStatus.FAIL -> FJError
    }

    val text = when (status) {
        TestStatus.PENDING -> "PENDING"
        TestStatus.RUNNING -> "RUNNING..."
        TestStatus.PASS -> "✓ PASS"
        TestStatus.FAIL -> "✗ FAIL"
    }

    if (status == TestStatus.RUNNING) {
        val infiniteTransition = rememberInfiniteTransition(label = "running")
        val alpha by infiniteTransition.animateFloat(
            initialValue = 0.4f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(800, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )
        BadgePill(text = text, color = color.copy(alpha = alpha))
    } else {
        BadgePill(text = text, color = color)
    }
}

@Composable
fun BadgePill(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(50),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
        )
    }
}

@Composable
fun SummaryBar(results: List<TestResult>) {
    val passed = results.count { it.status == TestStatus.PASS }
    val failed = results.count { it.status == TestStatus.FAIL }
    val pending = results.count { it.status == TestStatus.PENDING || it.status == TestStatus.RUNNING }
    
    val bgColor = if (failed > 0) FJError.copy(alpha = 0.1f) else if (pending == 0) FJSuccess.copy(alpha = 0.1f) else FJSurfaceHigh

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = bgColor,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                SummaryStat(passed, "Passed", FJSuccess)
                Spacer(Modifier.width(12.dp))
                SummaryStat(failed, "Failed", FJError)
                Spacer(Modifier.width(12.dp))
                SummaryStat(pending, "Pending", Color.Gray)
            }
            
            if (failed > 0) {
                Text("NEEDS ATTENTION", color = FJError, fontWeight = FontWeight.Black, fontSize = 10.sp)
            } else if (pending == 0) {
                Text("ALL SYSTEMS GO", color = FJSuccess, fontWeight = FontWeight.Black, fontSize = 10.sp)
            }
        }
    }
}

@Composable
fun SummaryStat(count: Int, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count.toString(), color = color, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(label.uppercase(), color = FJTextSecondary, fontSize = 9.sp, fontWeight = FontWeight.SemiBold)
    }
}
