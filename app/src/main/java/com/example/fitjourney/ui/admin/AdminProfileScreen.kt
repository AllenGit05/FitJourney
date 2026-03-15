package com.example.fitjourney.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import com.example.fitjourney.ui.theme.*
import com.example.fitjourney.ui.theme.FJBackground
import com.example.fitjourney.ui.theme.FJTextPrimary
import com.example.fitjourney.ui.theme.FJGold
import com.example.fitjourney.ui.theme.FJTextSecondary
import com.example.fitjourney.ui.theme.FJSurface
import com.example.fitjourney.ui.theme.FJSurfaceVariant
import com.example.fitjourney.ui.theme.FJError
import com.example.fitjourney.ui.theme.FJSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminProfileScreen(
    viewModel: AdminProfileViewModel,
    onNavIconClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show success/error snackbars
    LaunchedEffect(uiState.emailSuccess) {
        uiState.emailSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }
    LaunchedEffect(uiState.passwordSuccess) {
        uiState.passwordSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Admin Profile",
                            color = FJGold,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Manage your admin credentials",
                            color = FJTextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavIconClick) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = FJGold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = FJBackground
                )
            )
        },
        containerColor = FJBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // Current email display
            Card(
                colors = CardDefaults.cardColors(containerColor = FJSurface),
                border = BorderStroke(1.dp, FJGold.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.AdminPanelSettings,
                        contentDescription = null,
                        tint = FJGold,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "Logged in as Admin",
                            color = FJGold,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            uiState.currentEmailDisplay,
                            color = FJTextPrimary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // ── CHANGE EMAIL SECTION ──────────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = FJSurface),
                border = BorderStroke(1.dp, FJSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Email,
                            contentDescription = null,
                            tint = FJGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Change Admin Email",
                            color = FJTextPrimary,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "This changes the email used to access the admin panel",
                        color = FJTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(16.dp))

                    // New email field
                    OutlinedTextField(
                        value = uiState.newEmail,
                        onValueChange = viewModel::onNewEmailChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("New Admin Email") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, null, tint = FJGold)
                        },
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(Modifier.height(12.dp))

                    // Current password for verification
                    OutlinedTextField(
                        value = uiState.emailCurrentPassword,
                        onValueChange = viewModel::onEmailCurrentPasswordChanged,
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("Current Password (to verify)") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, null, tint = FJGold)
                        },
                        trailingIcon = {
                            IconButton(onClick = viewModel::toggleEmailPasswordVisibility) {
                                Icon(
                                    if (uiState.showEmailCurrentPassword)
                                        Icons.Default.VisibilityOff
                                    else Icons.Default.Visibility,
                                    null,
                                    tint = FJTextSecondary
                                )
                            }
                        },
                        visualTransformation = if (uiState.showEmailCurrentPassword)
                            VisualTransformation.None else PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        singleLine = true,
                        colors = outlinedFieldColors(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    // Error message
                    AnimatedVisibility(visible = uiState.emailError != null) {
                        Text(
                            text = uiState.emailError ?: "",
                            color = FJError,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Update email button
                    Button(
                        onClick = viewModel::changeEmail,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isChangingEmail,
                        colors = ButtonDefaults.buttonColors(containerColor = FJGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (uiState.isChangingEmail) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = FJBackground,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Updating...", color = FJBackground, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                Icons.Default.Save, null,
                                tint = FJBackground,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Update Email", color = FJBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // ── CHANGE PASSWORD SECTION ───────────────────────────
            Card(
                colors = CardDefaults.cardColors(containerColor = FJSurface),
                border = BorderStroke(1.dp, FJSurfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = null,
                            tint = FJGold,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Change Password",
                            color = FJTextPrimary,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Minimum 6 characters required",
                        color = FJTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )

                    Spacer(Modifier.height(16.dp))

                    // Current password
                    PasswordField(
                        value = uiState.currentPassword,
                        onValueChange = viewModel::onCurrentPasswordChanged,
                        label = "Current Password",
                        isVisible = uiState.showCurrentPassword,
                        onToggleVisibility = viewModel::toggleCurrentPasswordVisibility,
                        imeAction = ImeAction.Next
                    )

                    Spacer(Modifier.height(12.dp))

                    // New password
                    PasswordField(
                        value = uiState.newPassword,
                        onValueChange = viewModel::onNewPasswordChanged,
                        label = "New Password",
                        isVisible = uiState.showNewPassword,
                        onToggleVisibility = viewModel::toggleNewPasswordVisibility,
                        imeAction = ImeAction.Next
                    )

                    Spacer(Modifier.height(12.dp))

                    // Confirm new password
                    PasswordField(
                        value = uiState.confirmNewPassword,
                        onValueChange = viewModel::onConfirmNewPasswordChanged,
                        label = "Confirm New Password",
                        isVisible = uiState.showConfirmPassword,
                        onToggleVisibility = viewModel::toggleConfirmPasswordVisibility,
                        imeAction = ImeAction.Done,
                        // Show match indicator
                        trailingMatchIcon = uiState.newPassword.isNotBlank() &&
                                uiState.confirmNewPassword.isNotBlank()
                    )

                    // Password match indicator
                    AnimatedVisibility(
                        visible = uiState.newPassword.isNotBlank() &&
                                  uiState.confirmNewPassword.isNotBlank()
                    ) {
                        Row(
                            modifier = Modifier.padding(top = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val matches = uiState.newPassword == uiState.confirmNewPassword
                            Icon(
                                if (matches) Icons.Default.CheckCircle
                                else Icons.Default.Cancel,
                                null,
                                tint = if (matches) FJSuccess else FJError,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                if (matches) "Passwords match" else "Passwords do not match",
                                color = if (matches) FJSuccess else FJError,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }

                    // Error
                    AnimatedVisibility(visible = uiState.passwordError != null) {
                        Text(
                            text = uiState.passwordError ?: "",
                            color = FJError,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Change password button
                    Button(
                        onClick = viewModel::changePassword,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isChangingPassword,
                        colors = ButtonDefaults.buttonColors(containerColor = FJGold),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (uiState.isChangingPassword) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = FJBackground,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Updating...", color = FJBackground, fontWeight = FontWeight.Bold)
                        } else {
                            Icon(
                                Icons.Default.LockReset, null,
                                tint = FJBackground,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Change Password", color = FJBackground, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// Reusable password field
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isVisible: Boolean,
    onToggleVisibility: () -> Unit,
    imeAction: ImeAction = ImeAction.Next,
    trailingMatchIcon: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        leadingIcon = { Icon(Icons.Default.Lock, null, tint = FJGold) },
        trailingIcon = {
            IconButton(onClick = onToggleVisibility) {
                Icon(
                    if (isVisible) Icons.Default.VisibilityOff
                    else Icons.Default.Visibility,
                    null,
                    tint = FJTextSecondary
                )
            }
        },
        visualTransformation = if (isVisible) VisualTransformation.None
                               else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = imeAction
        ),
        singleLine = true,
        colors = outlinedFieldColors(),
        shape = RoundedCornerShape(10.dp)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun outlinedFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = FJGold,
    unfocusedBorderColor = FJSurfaceVariant,
    focusedTextColor = FJTextPrimary,
    unfocusedTextColor = FJTextPrimary,
    cursorColor = FJGold,
    focusedLabelColor = FJGold,
    unfocusedLabelColor = FJTextSecondary
)
