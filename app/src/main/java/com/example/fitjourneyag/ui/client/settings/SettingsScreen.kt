package com.example.fitjourneyag.ui.client.settings

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
import com.example.fitjourneyag.ui.theme.*
import com.example.fitjourneyag.ui.common.*

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
    val customCoachPersona by viewModel.customCoachPersona.collectAsState()
    val isLoading     by viewModel.isLoading.collectAsState()

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

            // ── Section: Profile Picture ────────────────────────
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {
                    Surface(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        color = FJSurface,
                        shape = CircleShape,
                        modifier = Modifier.size(100.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, FJGold.copy(0.5f))
                    ) {
                        if (user?.profilePictureUri != null) {
                            coil.compose.AsyncImage(
                                model = user?.profilePictureUri,
                                contentDescription = "Profile",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(Icons.Default.Person, "Profile", tint = FJGold, modifier = Modifier.padding(24.dp).size(48.dp))
                        }
                    }
                    IconButton(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.size(32.dp).background(FJGold, CircleShape)
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, tint = FJOnGold, modifier = Modifier.size(16.dp))
                    }
                }
                
                if (user?.profilePictureUri != null) {
                    TextButton(onClick = { viewModel.removeProfileImage() }) {
                        Text("Remove Photo", color = Color.Red, fontSize = 14.sp)
                    }
                }
            }

            // ── Section: Account Info ───────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FJSectionLabel("Account Info")
                FJTextField(
                    value = username,
                    onValueChange = viewModel::setUsername,
                    placeholder = "Username",
                    leadingIcon = { Icon(Icons.Default.Person, null, tint = FJTextSecondary) }
                )
            }

            // ── Section: Personal Details ────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FJSectionLabel("Personal Details")
                
                // DoB
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

                if (showDatePicker) {
                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val date = Date(millis)
                                    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                                    viewModel.setDob(format.format(date))
                                }
                                showDatePicker = false
                            }) { Text("OK", color = FJGold) }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) { Text("Cancel", color = FJTextSecondary) }
                        }
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }

                // Gender
                FJOptionLabel("Gender")
                FJOptionRow(
                    options = listOf("Male", "Female", "Other"),
                    selected = gender,
                    onSelect = viewModel::setGender
                )
            }

            // ── Section: AI Coach ──────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FJSectionLabel("AI Coach Customization")
                
                FJOptionLabel("Coach Persona")
                FJOptionRow(
                    options = listOf("Aurora", "Rex", "Zen", "Custom"),
                    selected = coachPersona,
                    onSelect = viewModel::setCoachPersona
                )

                Spacer(Modifier.height(8.dp))
                
                if (coachPersona == "Custom") {
                    Text("Customize your AI Persona:", color = FJGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    
                    FJOptionLabel("1. Coach Name")
                    FJTextField(
                        value = coachName,
                        onValueChange = viewModel::setCoachName,
                        placeholder = "e.g., Master Chief",
                        leadingIcon = { Icon(Icons.Default.Badge, null, tint = FJTextSecondary) }
                    )

                    FJOptionLabel("2. Coach Personality")
                    FJTextField(
                        value = customCoachPersona,
                        onValueChange = viewModel::setCustomCoachPersona,
                        placeholder = "e.g., A tough-loving veteran coach",
                        leadingIcon = { Icon(Icons.Default.Psychology, null, tint = FJTextSecondary) }
                    )

                    FJOptionLabel("3. Coach Gender")
                    FJOptionRow(
                        options = listOf("Male", "Female"),
                        selected = coachGender,
                        onSelect = viewModel::setCoachGender
                    )
                } else {
                    // Predefined persona defaults (modifiable if desired, but auto-set)
                    FJOptionLabel("Coach Name")
                    FJTextField(
                        value = coachName,
                        onValueChange = viewModel::setCoachName,
                        placeholder = "Name your coach",
                        leadingIcon = { Icon(Icons.Default.Badge, null, tint = FJTextSecondary) }
                    )

                    FJOptionLabel("Coach Gender")
                    FJOptionRow(
                        options = listOf("Male", "Female"),
                        selected = coachGender,
                        onSelect = viewModel::setCoachGender
                    )
                }
            }

            // ── Section: Body Metrics ───────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FJSectionLabel("Body Metrics")
                FJTextField(
                    value = height, onValueChange = viewModel::setHeight, placeholder = "Height (cm)",
                    leadingIcon = { Icon(Icons.Default.Height, null, tint = FJTextSecondary) },
                    keyboardType = KeyboardType.Number
                )
                FJTextField(
                    value = weight, onValueChange = viewModel::setWeight, placeholder = "Weight (kg)",
                    leadingIcon = { Icon(Icons.Default.Scale, null, tint = FJTextSecondary) },
                    keyboardType = KeyboardType.Number
                )
                FJTextField(
                    value = goalWeight, onValueChange = viewModel::setGoalWeight, placeholder = "Goal Weight (kg)",
                    leadingIcon = { Icon(Icons.Default.Flag, null, tint = FJTextSecondary) },
                    keyboardType = KeyboardType.Number
                )
            }

            // ── Section: Goals & Targets ───────────────────────
            val calorieGoal by viewModel.calorieGoal.collectAsState()
            val fitnessGoal by viewModel.fitnessGoal.collectAsState()

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FJSectionLabel("Goals & Calorie Targets")
                
                FJOptionLabel("Fitness Goal")
                FJOptionRow(
                    options = listOf("Lose Weight", "Maintain Weight", "Gain Weight"),
                    selected = fitnessGoal,
                    onSelect = viewModel::setFitnessGoal
                )

                Spacer(Modifier.height(4.dp))

                FJOptionLabel("Daily Calorie Goal")
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Box(modifier = Modifier.weight(1f)) {
                        FJTextField(
                            value = calorieGoal.toString(),
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
                        modifier = Modifier.height(54.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(Icons.Default.Calculate, null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Auto", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Text(
                    "Auto-calculate uses the Mifflin-St Jeor formula based on your metrics.",
                    color = FJTextSecondary,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            // ── Section: Lifestyle ──────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FJSectionLabel("Lifestyle & Diet")
                
                FJOptionLabel("Activity Level")
                FJOptionRow(
                    options = listOf("Sedentary", "Low", "Moderate", "High", "Very High"),
                    selected = activityLevel,
                    onSelect = viewModel::setActivityLevel
                )

                FJOptionLabel("Food Type")
                FJOptionRow(
                    options = listOf("Vegetarian", "Non-Veg", "Vegan", "Other"),
                    selected = foodType,
                    onSelect = viewModel::setFoodType
                )
            }

            // ── Section: Subscription & Credits ──────────────────
            val user by viewModel.currentUser.collectAsState()
            val showStore by viewModel.showCreditStore.collectAsState()
            var selectedPackageForPayment by remember { mutableStateOf<String?>(null) }

            if (showStore) {
                CreditStoreDialog(
                    onDismiss = { viewModel.dismissCreditStore() },
                    onPurchase = { option -> selectedPackageForPayment = option }
                )
            }

            if (selectedPackageForPayment != null) {
                PaymentDialog(
                    packageName = selectedPackageForPayment!!,
                    onDismiss = { selectedPackageForPayment = null },
                    onPaymentSuccess = { 
                        viewModel.purchaseOption(selectedPackageForPayment!!)
                        selectedPackageForPayment = null 
                    }
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                FJSectionLabel("Subscription & Credits")
                
                if (user?.isPremium == true) {
                    Surface(
                        color = FJGold.copy(0.1f),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FJGold),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Star, null, tint = FJGold)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Elite Member", color = FJTextPrimary, fontWeight = FontWeight.Bold)
                                Text("Unlimited AI & Features", color = FJTextSecondary, fontSize = 12.sp)
                            }
                            TextButton(onClick = { viewModel.cancelSubscription() }) {
                                Text("Cancel", color = FJError, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                } else {
                    Surface(
                        onClick = { viewModel.showStore() },
                        color = FJSurface,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AutoAwesome, null, tint = FJGold)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Free Plan", color = FJTextPrimary, fontWeight = FontWeight.Bold)
                                Text("${user?.aiCredits ?: 0} AI Credits available", color = FJTextSecondary, fontSize = 12.sp)
                            }
                            Text("Upgrade", color = FJGold, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // ── Action Buttons ──────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { viewModel.saveProfile() },
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(Modifier.size(24.dp), color = FJOnGold)
                    } else {
                        Text("Save Profile", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }

                Button(
                    onClick = {
                        viewModel.logout()
                        onLogout()
                    },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A1A1A), contentColor = FJError)
                ) {
                    Icon(Icons.Default.Logout, null, tint = FJError)
                    Spacer(Modifier.width(8.dp))
                    Text("Log Out", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}
