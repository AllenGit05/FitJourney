package com.example.fitjourney.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.ui.theme.*
import com.example.fitjourney.ui.common.FJTextField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    viewModel: ForgotPasswordViewModel,
    onNavigateBack: () -> Unit,
    onResetSuccess: () -> Unit
) {
    val email              by viewModel.email.collectAsState()
    val isLoading          by viewModel.isLoading.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FJBackground)
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = FJTextPrimary)
            }

            Spacer(Modifier.height(16.dp))

            Text("Reset Password", color = FJTextPrimary, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Text("Enter your email address to receive a password reset link.", color = FJTextSecondary, fontSize = 14.sp)

            Spacer(Modifier.height(32.dp))

            FJTextField(
                value = email,
                onValueChange = viewModel::setEmail,
                placeholder = "Email address",
                leadingIcon = { Icon(Icons.Default.Email, null, tint = FJTextSecondary) }
            )

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = { viewModel.resetPassword { onResetSuccess() } },
                enabled = !isLoading,
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(Modifier.size(22.dp), color = FJOnGold, strokeWidth = 2.dp)
                } else {
                    Text("Reset Password", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}
