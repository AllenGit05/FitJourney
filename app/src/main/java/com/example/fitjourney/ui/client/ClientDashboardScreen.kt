package com.example.fitjourney.ui.client

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.ui.theme.*
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClientDashboardScreen(
    viewModel: ClientDashboardViewModel,
    onNavigateToDiet: () -> Unit,
    onNavigateToWorkout: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToGeneratePlan: () -> Unit,
    onNavigateToInsights: () -> Unit,
    onNavigateToHabits: () -> Unit
) {
    val user                by viewModel.currentUser.collectAsState()
    val caloriesEaten       by viewModel.caloriesEaten.collectAsState()
    val calorieGoal         by viewModel.dailyCalorieGoal.collectAsState()
    val waterDrank          by viewModel.waterDrank.collectAsState()
    val steps               by viewModel.steps.collectAsState()
    val stepGoal            by viewModel.stepGoal.collectAsState()
    val proteinConsumed     by viewModel.proteinConsumed.collectAsState()
    val proteinTarget       by viewModel.proteinTarget.collectAsState()
    val dailyInsight        by viewModel.dailyInsight.collectAsState()
    val isWorkoutCompleted  by viewModel.isWorkoutCompleted.collectAsState()

    val greeting = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good Morning"
        in 12..16 -> "Good Afternoon"
        else -> "Good Evening"
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.updateProfileImage(it.toString()) }
    }


    Scaffold(
        containerColor = FJBackground,
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "$greeting,",
                                color = FJTextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = user?.username ?: "Enthusiast",
                                color = FJGold,
                                fontSize = 24.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        
                        Surface(
                            onClick = onNavigateToSettings,
                            color = FJSurface,
                            shape = CircleShape,
                            modifier = Modifier.size(44.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, FJGold.copy(0.3f))
                        ) {
                            if (user?.profilePictureUri != null) {
                                coil.compose.AsyncImage(
                                    model = user?.profilePictureUri,
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                )
                            } else {
                                Icon(
                                    Icons.Default.Person,
                                    "Profile",
                                    tint = FJGold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FJBackground)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header moved to TopAppBar
            // AI Motivation Card removed per user request to simplify dashboard

            // ── 2. Gamification Banner & Streak ─────────────────
            item {
                val weeklyStreak by viewModel.weeklyStreak.collectAsState()
                GamificationCard(user, weeklyStreak)
            }
            item {
                DashboardMetricCard(
                    title = "Calories",
                    current = caloriesEaten,
                    goal = calorieGoal,
                    unit = "kcal",
                    icon = Icons.Default.Restaurant,
                    onClick = onNavigateToDiet
                )
            }

            // ── 4. Steps Card ─────────────────────────────────────
            item {
                DashboardMetricCard(
                    title = "Steps",
                    current = steps,
                    goal = stepGoal,
                    unit = "",
                    icon = Icons.Default.DirectionsWalk
                )
            }

            // ── 5. Protein Intake Card ────────────────────────────
            item {
                DashboardMetricCard(
                    title = "Protein Intake",
                    current = proteinConsumed,
                    goal = proteinTarget,
                    unit = "g",
                    icon = Icons.Default.Egg,
                    color = Color(0xFF4CAF50)
                )
            }

            // ── 6. Water Intake Card ──────────────────────────────
            item {
                val waterGoal by viewModel.waterGoal.collectAsState()
                Surface(
                    color = FJSurface,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Water Intake", color = FJTextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            val currentLiters = String.format(Locale.US, "%.2f", waterDrank / 1000f)
                            val goalLiters    = String.format(Locale.US, "%.1f", waterGoal / 1000f)
                            Text("$currentLiters / $goalLiters L", color = FJTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                        }
                        Icon(Icons.Default.WaterDrop, null, tint = Color(0xFF2196F3), modifier = Modifier.size(32.dp))
                    }
                }
            }

            // ── 7. Workout Status Card ────────────────────────────
            item {
                val statusText = if (isWorkoutCompleted) "Workout Completed" else "No Workout Logged"
                val statusIcon = if (isWorkoutCompleted) Icons.Default.CheckCircle else Icons.Default.Timer
                val statusColor = if (isWorkoutCompleted) Color(0xFF4CAF50) else FJTextSecondary
                
                Surface(
                    onClick = onNavigateToWorkout,
                    color = FJSurface,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Workout Status", color = FJTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(statusText, color = FJTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = FJTextSecondary)
                    }
                }
            }

            // ── 8. Weekly AI Report Card ──────────────────────────
            item {
                Surface(
                    onClick = onNavigateToInsights,
                    color = FJGold.copy(0.1f),
                    shape = RoundedCornerShape(24.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, FJGold.copy(0.2f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(48.dp).background(FJGold.copy(0.2f), CircleShape), contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.AutoAwesome, null, tint = FJGold, modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Weekly AI Report", color = FJTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("View your AI fitness analysis", color = FJTextSecondary, fontSize = 12.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = FJGold)
                    }
                }
            }
            
            item { Spacer(Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun DashboardMetricCard(
    title: String,
    current: Int,
    goal: Int,
    unit: String,
    icon: ImageVector,
    color: Color = FJGold,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = onClick ?: {},
        enabled = onClick != null,
        color = FJSurface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(title, color = FJTextSecondary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                Text("$current / $goal $unit", color = FJTextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(16.dp))
            val progress = if (goal > 0) current.toFloat() / goal else 0f
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape),
                color = color,
                trackColor = FJSurfaceHigh,
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun DashboardFabMenu(
    expanded: Boolean,
    onToggle: () -> Unit,
    onAddFood: () -> Unit,
    onLogWorkout: () -> Unit,
    onLogWeight: () -> Unit
) {
    Box(contentAlignment = Alignment.BottomEnd) {
        if (expanded) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 80.dp)
            ) {
                FabMenuItem("Add Food", Icons.Default.Restaurant, onAddFood)
                FabMenuItem("Log Workout", Icons.Default.FitnessCenter, onLogWorkout)
                FabMenuItem("Log Weight", Icons.Default.MonitorWeight, onLogWeight)
            }
        }
        FloatingActionButton(
            onClick = onToggle,
            containerColor = FJGold,
            contentColor = FJOnGold,
            shape = CircleShape,
            modifier = Modifier.size(64.dp)
        ) {
            Icon(if (expanded) Icons.Default.Close else Icons.Default.Add, null, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun FabMenuItem(label: String, icon: ImageVector, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Surface(
            color = FJSurface,
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 4.dp,
            modifier = Modifier.padding(end = 12.dp)
        ) {
            Text(label, color = FJTextPrimary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
        FloatingActionButton(
            onClick = onClick,
            modifier = Modifier.size(52.dp),
            containerColor = FJSurface,
            contentColor = FJGold,
            shape = CircleShape
        ) {
            Icon(icon, contentDescription = label, modifier = Modifier.size(22.dp))
        }
    }
}

@Composable
private fun DashboardCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color = FJSurface,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick ?: {},
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        enabled = onClick != null
    ) {
        Box(Modifier.padding(16.dp)) {
            content()
        }
    }
}

@Composable
private fun QuickNavButton(label: String, icon: ImageVector, modifier: Modifier, onClick: () -> Unit) {
    DashboardCard(modifier = modifier, onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = FJGold, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(4.dp))
            Text(label, color = FJTextPrimary, fontSize = 12.sp)
        }
    }
}

@Composable
fun GamificationCard(user: com.example.fitjourney.domain.model.User?, weeklyStreak: List<Boolean> = emptyList()) {
    val xp = user?.xp ?: 0
    val streak = user?.currentStreak ?: 0
    
    var remainingXp = xp
    var reqXp = 100
    var calcLvl = 1
    while (remainingXp >= reqXp) {
        remainingXp -= reqXp
        calcLvl++
        reqXp = calcLvl * 100
    }
    val progress = remainingXp.toFloat() / reqXp.toFloat()

    Surface(
        color = FJSurface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Level Badge
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(64.dp).background(FJGold.copy(0.1f), CircleShape)
                ) {
                    CircularProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxSize(),
                        color = FJGold,
                        trackColor = FJSurfaceHigh,
                        strokeCap = StrokeCap.Round
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("LVL", color = FJGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text("$calcLvl", color = FJGold, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
                    }
                }
                
                Spacer(Modifier.width(20.dp))
                
                // XP Info
                Column(Modifier.weight(1f)) {
                    Text("Fitness Level", color = FJTextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("$remainingXp / $reqXp XP", color = FJTextSecondary, fontSize = 14.sp)
                }
                
                // Streak
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.LocalFireDepartment, null, tint = if (streak > 0) Color(0xFFFF9800) else FJTextSecondary, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.height(4.dp))
                    Text("$streak Day Streak", color = if (streak > 0) Color(0xFFFF9800) else FJTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            
            if (weeklyStreak.isNotEmpty()) {
                HorizontalDivider(color = FJDivider.copy(0.5f), thickness = 1.dp, modifier = Modifier.padding(horizontal = 20.dp))
                WeeklyStreakView(weeklyStreak)
                Spacer(Modifier.height(10.dp))
            }
        }
    }
}

@Composable
fun WeeklyStreakView(streak: List<Boolean>) {
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    // Note: streak is last 6 days + today. We need to align with day names.
    // For simplicity, let's just show labels for the last 7 days.
    val cal = Calendar.getInstance()
    val labels = mutableListOf<String>()
    
    for (i in 6 downTo 0) {
        val c = Calendar.getInstance()
        c.add(Calendar.DAY_OF_YEAR, -i)
        labels.add(c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.US) ?: "")
    }

    Column(Modifier.padding(vertical = 16.dp, horizontal = 20.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            labels.forEachIndexed { index, label ->
                val isCompleted = streak.getOrElse(index) { false }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        label, 
                        color = if (index == 6) FJTextPrimary else FJTextSecondary, 
                        fontSize = 12.sp, 
                        fontWeight = if (index == 6) FontWeight.Bold else FontWeight.Medium
                    )
                    Spacer(Modifier.height(12.dp))
                    StreakCircle(isCompleted, isToday = (index == 6))
                }
            }
        }
    }
}

@Composable
fun StreakCircle(isCompleted: Boolean, isToday: Boolean) {
    val gradient = androidx.compose.ui.graphics.Brush.linearGradient(
        colors = listOf(Color(0xFFFFA726), Color(0xFFFF7043))
    )
    
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .then(
                if (isCompleted) Modifier.background(gradient)
                else Modifier.background(FJSurfaceHigh).border(1.dp, FJDivider, CircleShape)
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isCompleted) {
            Icon(Icons.Default.Check, null, tint = Color.White, modifier = Modifier.size(20.dp))
        } else if (isToday) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(FJGold.copy(0.5f)))
        }
    }
}
