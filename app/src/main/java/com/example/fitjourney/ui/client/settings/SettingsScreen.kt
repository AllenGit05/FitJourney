package com.example.fitjourney.ui.client.settings

import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import com.example.fitjourney.ui.theme.*
import com.example.fitjourney.ui.common.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onLogout: () -> Unit
) {
    val scrollState = rememberScrollState()
    val username      by viewModel.username.collectAsState()
    val gender        by viewModel.gender.collectAsState()
    val height        by viewModel.height.collectAsState()
    val weight        by viewModel.weight.collectAsState()
    val goalWeight    by viewModel.goalWeight.collectAsState()
    val activityLevel by viewModel.activityLevel.collectAsState()
    val foodType      by viewModel.foodType.collectAsState()
    val dob           by viewModel.dob.collectAsState()
    val coachName     by viewModel.coachName.collectAsState()
    val coachGender   by viewModel.coachGender.collectAsState()
    val coachPersona  by viewModel.coachPersona.collectAsState()
    val englishAccent by viewModel.englishAccent.collectAsState()
    val isLoading     by viewModel.isLoading.collectAsState()


    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current



    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val user by viewModel.currentUser.collectAsState()
    val imagePickerLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        uri?.let { viewModel.updateProfileImage(it.toString()) }
    }

    Scaffold(
        containerColor = FJBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = FJTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = FJTextPrimary) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FJBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Section 1: Profile ─────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FJSectionLabel("Profile")
                FJTextField(
                    value = username,
                    onValueChange = viewModel::setUsername,
                    placeholder = "Username",
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = FJTextSecondary) }
                )
                FJOptionLabel("Gender")
                FJOptionRow(
                    options = listOf("Male", "Female", "Other"),
                    selected = gender,
                    onSelect = viewModel::setGender
                )
                FJOptionLabel("Date of Birth")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(FJSurface)
                        .clickable { showDatePicker = true }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.DateRange, null, tint = FJTextSecondary)
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = if (dob.isEmpty()) "Select Date" else dob,
                            color = if (dob.isEmpty()) FJTextSecondary else FJTextPrimary,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            // ── Section 2: Daily Goals ─────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FJSectionLabel("Daily Goals")
                
                FJOptionLabel("Goal")
                FJOptionRow(
                    options = listOf("Lose Weight", "Maintain Weight", "Gain Weight"),
                    selected = user?.fitnessGoal ?: "Maintain Weight",
                    onSelect = viewModel::setFitnessGoal
                )

                FJOptionLabel("Daily Calorie Goal")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FJTextField(
                            value = (user?.calorieGoal ?: 2000).toString(),
                            onValueChange = viewModel::setCalorieGoal,
                            placeholder = "e.g., 2000",
                            leadingIcon = { Icon(Icons.Default.LocalFireDepartment, null, tint = FJTextSecondary) },
                            keyboardType = KeyboardType.Number
                        )
                    }
                    Button(
                        onClick = { viewModel.autoCalculateCalorieGoal() },
                        colors = ButtonDefaults.buttonColors(containerColor = FJSurface, contentColor = FJGold),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.height(54.dp)
                    ) {
                        Text("Auto", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(Modifier.weight(1f)) {
                        FJTextField(
                            value = weight, onValueChange = viewModel::setWeight, placeholder = "Weight (kg)",
                            leadingIcon = { Icon(Icons.Default.Scale, null, tint = FJTextSecondary) },
                            keyboardType = KeyboardType.Number
                        )
                    }
                    Box(Modifier.weight(1f)) {
                        FJTextField(
                            value = goalWeight, onValueChange = viewModel::setGoalWeight, placeholder = "Goal (kg)",
                            leadingIcon = { Icon(Icons.Default.Flag, null, tint = FJTextSecondary) },
                            keyboardType = KeyboardType.Number
                        )
                    }
                }
                FJTextField(
                    value = height, onValueChange = viewModel::setHeight, placeholder = "Height (cm)",
                    leadingIcon = { Icon(Icons.Default.Height, null, tint = FJTextSecondary) },
                    keyboardType = KeyboardType.Number
                )
            }

            // ── Section 3: Your AI Coach ───────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FJSectionLabel("Your AI Coach")
                
                FJOptionLabel("Coach Persona")
                FJOptionRow(
                    options = listOf("Aurora", "Rex", "Zen"),
                    selected = coachPersona,
                    onSelect = viewModel::setCoachPersona
                )


                FJOptionLabel("Voice Accent")
                // 2x2 Grid for Accents
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    val accentList = listOf(
                        "Indian" to "en-in",
                        "British" to "en-gb",
                        "American" to "en-us",
                        "Australian" to "en-au"
                    )
                    repeat(2) { rowIndex ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            repeat(2) { colIndex ->
                                val index = rowIndex * 2 + colIndex
                                val (label, code) = accentList[index]
                                val isSelected = englishAccent == code
                                Surface(
                                    onClick = { viewModel.setEnglishAccent(code) },
                                    modifier = Modifier.weight(1f).height(48.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) FJGold else FJSurface,
                                    border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, FJSurfaceHigh)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Text(label, color = if (isSelected) FJOnGold else FJTextPrimary, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FJSectionLabel("App")

                Button(
                    onClick = { viewModel.saveProfile() },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }


                TextButton(
                    onClick = { viewModel.logout(); onLogout() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Logout", color = FJError, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
