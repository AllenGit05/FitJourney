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
    val errorMessage  by viewModel.errorMessage.collectAsState()


    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current



    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    val user by viewModel.currentUser.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    var passwordForDelete by remember { mutableStateOf("") }

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
                    options = listOf("Fat Loss", "Recomp", "Muscle Gain", "Maintain"),
                    selected = user?.fitnessGoal ?: "Maintain",
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
                    Text("Logout", color = FJTextSecondary, fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = { showDeleteDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Delete Account", color = FJError, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                passwordForDelete = ""
                viewModel.clearError()
            },
            containerColor = FJSurface,
            title = { Text("Delete Account?", color = FJError, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        "This action is permanent and will delete all your data from our servers (Firestore and Authentication).",
                        color = FJTextSecondary,
                        fontSize = 14.sp
                    )
                    FJTextField(
                        value = passwordForDelete,
                        onValueChange = { passwordForDelete = it },
                        placeholder = "Confirm Password",
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = FJTextSecondary) },
                        isPassword = true
                    )
                    if (errorMessage != null) {
                        Text(errorMessage!!, color = FJError, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAccount(passwordForDelete) {
                            showDeleteDialog = false
                            onLogout()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FJError),
                    enabled = passwordForDelete.isNotBlank() && !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text("Delete Permanently", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    passwordForDelete = ""
                    viewModel.clearError()
                }) {
                    Text("Cancel", color = FJTextSecondary)
                }
            }
        )
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        viewModel.setDob(sdf.format(Date(it)))
                    }
                    showDatePicker = false
                }) { Text("OK", color = FJGold) }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = FJTextSecondary) }
            },
            colors = DatePickerDefaults.colors(containerColor = FJSurface)
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
