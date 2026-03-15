package com.example.fitjourneyag.ui.auth

import java.text.SimpleDateFormat
import java.util.*

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourneyag.ui.theme.*

import com.example.fitjourneyag.ui.common.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SignUpScreen(
    viewModel: SignUpViewModel,
    onNavigateBack: () -> Unit,
    onSignUpSuccess: () -> Unit
) {
    val scrollState = rememberScrollState()
    val username        by viewModel.username.collectAsState()
    val email           by viewModel.email.collectAsState()
    val dob             by viewModel.dob.collectAsState()
    val gender          by viewModel.gender.collectAsState()
    val height          by viewModel.height.collectAsState()
    val weight          by viewModel.weight.collectAsState()
    val goalWeight      by viewModel.goalWeight.collectAsState()
    val activityLevel   by viewModel.activityLevel.collectAsState()
    val foodType        by viewModel.foodType.collectAsState()
    val password        by viewModel.password.collectAsState()
    val confirmPassword by viewModel.confirmPassword.collectAsState()
    val backupPin       by viewModel.backupPin.collectAsState()
    val isLoading       by viewModel.isLoading.collectAsState()
    val errorMessage    by viewModel.errorMessage.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FJBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // ── Header ──────────────────────────────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FJTextPrimary)
                }
                Spacer(Modifier.width(8.dp))
                Text("Hey there,", color = FJTextSecondary, fontSize = 14.sp)
            }
            Text(
                text = "Create an Account",
                color = FJTextPrimary,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, bottom = 28.dp)
            )

            // ── Section: Basic Info ─────────────────────────────
            FJSectionLabel("Basic Info")
            Spacer(Modifier.height(12.dp))
            FJTextField(value = username, onValueChange = viewModel::setUsername, placeholder = "Username",
                leadingIcon = { Icon(Icons.Default.Person, null, tint = FJTextSecondary) })
            Spacer(Modifier.height(12.dp))
            FJTextField(value = email, onValueChange = viewModel::setEmail, placeholder = "Email address",
                leadingIcon = { Icon(Icons.Default.Email, null, tint = FJTextSecondary) }, keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(12.dp))
            
            // Date of Birth - Clickable Card instead of TextField
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
                        text = if (dob.isEmpty()) "Date of Birth" else dob,
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
                        }) {
                            Text("OK", color = FJGold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDatePicker = false }) {
                            Text("Cancel", color = FJTextSecondary)
                        }
                    },
                    colors = DatePickerDefaults.colors(
                        containerColor = FJSurface
                    )
                ) {
                    DatePicker(
                        state = datePickerState,
                        colors = DatePickerDefaults.colors(
                            titleContentColor = FJTextPrimary,
                            headlineContentColor = FJTextPrimary,
                            weekdayContentColor = FJTextSecondary,
                            subheadContentColor = FJTextSecondary,
                            yearContentColor = FJTextSecondary,
                            currentYearContentColor = FJGold,
                            selectedYearContentColor = FJOnGold,
                            selectedYearContainerColor = FJGold,
                            dayContentColor = FJTextPrimary,
                            selectedDayContentColor = FJOnGold,
                            selectedDayContainerColor = FJGold,
                            todayContentColor = FJGold,
                            todayDateBorderColor = FJGold
                        )
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Section: Body Stats ─────────────────────────────
            FJSectionLabel("Body Stats")
            Spacer(Modifier.height(12.dp))

            // Gender selector
            FJOptionLabel("Gender")
            Spacer(Modifier.height(8.dp))
            FJOptionRow(
                options = listOf("Male", "Female", "Other"),
                selected = gender,
                onSelect = viewModel::setGender
            )
            Spacer(Modifier.height(12.dp))
            FJTextField(value = height, onValueChange = viewModel::setHeight, placeholder = "Height (cm)",
                leadingIcon = { Icon(Icons.Default.Height, null, tint = FJTextSecondary) }, keyboardType = KeyboardType.Number)
            Spacer(Modifier.height(12.dp))
            FJTextField(value = weight, onValueChange = viewModel::setWeight, placeholder = "Current Weight (kg)",
                leadingIcon = { Icon(Icons.Default.FitnessCenter, null, tint = FJTextSecondary) }, keyboardType = KeyboardType.Number)
            Spacer(Modifier.height(12.dp))
            FJTextField(value = goalWeight, onValueChange = viewModel::setGoalWeight, placeholder = "Goal Weight (kg)",
                leadingIcon = { Icon(Icons.Default.Flag, null, tint = FJTextSecondary) }, keyboardType = KeyboardType.Number)
            Spacer(Modifier.height(12.dp))

            // Activity Level selector
            FJOptionLabel("Activity Level")
            Spacer(Modifier.height(8.dp))
            FJOptionRow(
                options = listOf("Sedentary", "Low", "Moderate", "High", "Very High"),
                selected = activityLevel,
                onSelect = viewModel::setActivityLevel
            )
            Spacer(Modifier.height(12.dp))

            // Food Type selector
            FJOptionLabel("Food Type")
            Spacer(Modifier.height(8.dp))
            FJOptionRow(
                options = listOf("Vegetarian", "Non-Veg", "Vegan", "Other"),
                selected = foodType,
                onSelect = viewModel::setFoodType
            )

            Spacer(Modifier.height(24.dp))

            // ── Section: Security ───────────────────────────────
            FJSectionLabel("Security")
            Spacer(Modifier.height(12.dp))
            FJTextField(value = password, onValueChange = viewModel::setPassword, placeholder = "Password",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = FJTextSecondary) }, isPassword = true)
            Spacer(Modifier.height(12.dp))
            FJTextField(value = confirmPassword, onValueChange = viewModel::setConfirmPassword, placeholder = "Confirm Password",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = FJTextSecondary) }, isPassword = true)
            Spacer(Modifier.height(12.dp))
            FJTextField(value = backupPin, onValueChange = viewModel::setBackupPin, placeholder = "Backup PIN (for password reset)",
                leadingIcon = { Icon(Icons.Default.Pin, null, tint = FJTextSecondary) }, isPassword = true)

            // ── Error ───────────────────────────────────────────
            if (errorMessage != null) {
                Spacer(Modifier.height(12.dp))
                Text(errorMessage!!, color = FJError, fontSize = 13.sp, modifier = Modifier.fillMaxWidth())
            }

            Spacer(Modifier.height(32.dp))

            // ── Register button ─────────────────────────────────
            Button(
                onClick = {
                    viewModel.signUp(onSuccess = { onSignUpSuccess() }, onError = {})
                },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = FJOnGold, strokeWidth = 2.dp)
                } else {
                    Text("Register", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Already have an account? ", color = FJTextSecondary, fontSize = 14.sp)
                TextButton(onClick = onNavigateBack, contentPadding = PaddingValues(0.dp)) {
                    Text("Sign In", color = FJGold, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

