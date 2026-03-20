package com.example.fitjourney.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.ui.theme.*
import com.example.fitjourney.ui.common.FJTextField

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onNavigateToSignUp: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onLoginSuccess: (com.example.fitjourney.domain.model.UserRole) -> Unit
) {
    val email by viewModel.email.collectAsState()
    val password by viewModel.password.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FJBackground)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // ── Logo ──────────────────────────────────────────────
            Image(
                painter = androidx.compose.ui.res.painterResource(id = com.example.fitjourney.R.drawable.app_logo),
                contentDescription = "App Logo",
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(20.dp))
            )

            Spacer(Modifier.height(20.dp))

            Text(
                text = "Hey there,",
                color = FJTextSecondary,
                fontSize = 16.sp
            )
            Text(
                text = "Sign In",
                color = FJTextPrimary,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(36.dp))

            // ── Email field ───────────────────────────────────────
            FJTextField(
                value = email,
                onValueChange = viewModel::onEmailChange,
                placeholder = "Email address",
                leadingIcon = { Icon(Icons.Default.Email, null, tint = FJTextSecondary) },
                keyboardType = KeyboardType.Email
            )

            Spacer(Modifier.height(16.dp))

            // ── Password field ────────────────────────────────────
            FJTextField(
                value = password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = "Password",
                leadingIcon = { Icon(Icons.Default.Lock, null, tint = FJTextSecondary) },
                isPassword = true
            )

            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text("Forgot Password?", color = FJGold, fontSize = 13.sp)
                }
            }

            // ── Error message ─────────────────────────────────────
            if (errorMessage != null) {
                Spacer(Modifier.height(6.dp))
                Text(
                    text = errorMessage!!,
                    color = FJError,
                    fontSize = 13.sp,
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage?.contains("profile not found") == true) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "💡 Tip: If you previously had an account, your profile may have been " +
                        "reset. Please sign up again with the same email or contact support.",
                        color = FJTextSecondary,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            // ── Login button ──────────────────────────────────────
            Button(
                onClick = {
                    viewModel.login(
                        onSuccess = { role -> onLoginSuccess(role) },
                        onError = {}
                    )
                },
                enabled = !isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = FJGold,
                    contentColor   = FJOnGold
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(22.dp), color = FJOnGold, strokeWidth = 2.dp)
                } else {
                    Text("Login", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }

            Spacer(Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Divider(modifier = Modifier.weight(1f), color = FJDivider)
                Text("  Or  ", color = FJTextSecondary, fontSize = 13.sp)
                Divider(modifier = Modifier.weight(1f), color = FJDivider)
            }

            Spacer(Modifier.height(24.dp))

            // ── Sign up link ──────────────────────────────────────
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Don't have an account? ", color = FJTextSecondary, fontSize = 14.sp)
                TextButton(onClick = onNavigateToSignUp, contentPadding = PaddingValues(0.dp)) {
                    Text("Sign Up", color = FJGold, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

