package com.example.fitjourney.ui.client.habits

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.domain.model.Habit
import com.example.fitjourney.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitScreen(
    viewModel: HabitViewModel,
    onNavigateBack: () -> Unit
) {
    val habits by viewModel.habits.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = FJBackground,
        topBar = {
            TopAppBar(
                title = { Text("Daily Habits", color = FJTextPrimary, fontWeight = FontWeight.ExtraBold) },
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
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            // Weekly Streak Summary (Simplified)
            HabitOverview(habits)

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(habits) { habit ->
                    HabitItem(
                        habit = habit,
                        onToggle = { viewModel.toggleHabit(habit.id) },
                        onDelete = { viewModel.deleteHabit(habit.id) }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddHabitDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { name, icon -> 
                    viewModel.addHabit(name, icon)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun HabitOverview(habits: List<Habit>) {
    val totalDone = habits.count { it.isCompletedToday }
    val totalHabits = habits.size
    val progress = if (totalHabits > 0) totalDone.toFloat() / totalHabits else 0f

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
        colors = CardDefaults.cardColors(containerColor = FJSurface),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(56.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxSize(),
                    color = FJGold,
                    trackColor = FJSurfaceHigh,
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Text("${(progress * 100).toInt()}%", color = FJGold, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
            Spacer(Modifier.width(20.dp))
            Column {
                Text("Today's Progress", color = FJTextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("$totalDone of $totalHabits habits completed", color = FJTextSecondary, fontSize = 13.sp)
            }
        }
    }
}

@Composable
private fun HabitItem(habit: Habit, onToggle: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        onClick = onToggle,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = if (habit.isCompletedToday) FJGold.copy(alpha = 0.1f) else FJSurface,
        border = if (habit.isCompletedToday) androidx.compose.foundation.BorderStroke(1.dp, FJGold.copy(0.3f)) else null
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(44.dp).clip(CircleShape).background(if (habit.isCompletedToday) FJGold else FJSurfaceHigh),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    getIconForName(habit.icon), 
                    null, 
                    tint = if (habit.isCompletedToday) FJOnGold else FJTextSecondary,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    habit.name, 
                    color = if (habit.isCompletedToday) FJGold else FJTextPrimary, 
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalFireDepartment, null, tint = if(habit.currentStreak > 0) Color(0xFFFF9800) else FJTextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${habit.currentStreak} day streak", color = FJTextSecondary, fontSize = 12.sp)
                }
            }
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, null, tint = FJTextSecondary) }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { onDelete(); showMenu = false }, leadingIcon = { Icon(Icons.Default.Delete, null) })
                }
            }
            Checkbox(
                checked = habit.isCompletedToday,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(checkedColor = FJGold, uncheckedColor = FJTextSecondary)
            )
        }
    }
}

private fun getIconForName(name: String): ImageVector {
    return when(name) {
        "WaterDrop" -> Icons.Default.WaterDrop
        "Bedtime" -> Icons.Default.Bedtime
        "DirectionsWalk" -> Icons.Default.DirectionsWalk
        "SelfImprovement" -> Icons.Default.SelfImprovement
        "EmojiEvents" -> Icons.Default.EmojiEvents
        "FitnessCenter" -> Icons.Default.FitnessCenter
        else -> Icons.Default.Star
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddHabitDialog(onDismiss: () -> Unit, onAdd: (String, String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("Star") }
    val icons = listOf("WaterDrop", "Bedtime", "DirectionsWalk", "SelfImprovement", "EmojiEvents", "FitnessCenter")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FJSurface,
        title = { Text("Add New Habit", color = FJTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    placeholder = { Text("Habit name...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Select Icon", color = FJTextSecondary, fontSize = 12.sp)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    icons.chunked(3).forEach { chunk ->
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            chunk.forEach { iconName ->
                                IconButton(
                                    onClick = { selectedIcon = iconName },
                                    modifier = Modifier.size(40.dp).clip(CircleShape).background(if (selectedIcon == iconName) FJGold.copy(0.2f) else Color.Transparent)
                                ) {
                                    Icon(getIconForName(iconName), null, tint = if (selectedIcon == iconName) FJGold else FJTextSecondary)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(onClick = { if(name.isNotBlank()) onAdd(name, selectedIcon) }, colors = ButtonDefaults.buttonColors(FJGold)) { Text("Add") }
        }
    )
}
