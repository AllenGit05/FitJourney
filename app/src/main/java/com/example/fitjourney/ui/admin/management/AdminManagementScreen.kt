package com.example.fitjourney.ui.admin.management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminManagementScreen(
    viewModel: AdminManagementViewModel,
    onNavigateBack: () -> Unit
) {
    val admins        by viewModel.admins.collectAsState()
    val newAdminEmail by viewModel.newAdminEmail.collectAsState()
    val isLoading     by viewModel.isLoading.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(FJBackground)) {
        Scaffold(
            containerColor = FJBackground,
            topBar = {
                TopAppBar(
                    title = { Text("Admin Management", color = FJTextPrimary, fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = FJTextPrimary) }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = FJBackground)
                )
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp, top = 8.dp)
            ) {
                // Add Admin card
                item {
                    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp)).background(FJSurface).padding(20.dp)) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("Add New Admin", color = FJGold, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            OutlinedTextField(
                                value = newAdminEmail, onValueChange = viewModel::setNewAdminEmail,
                                placeholder = { Text("Admin Email", color = FJTextSecondary) },
                                leadingIcon = { Icon(Icons.Default.Email, null, tint = FJTextSecondary) },
                                singleLine = true, modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = FJGold, unfocusedBorderColor = FJDivider,
                                    focusedContainerColor = FJSurfaceHigh, unfocusedContainerColor = FJSurfaceHigh,
                                    focusedTextColor = FJTextPrimary, unfocusedTextColor = FJTextPrimary
                                )
                            )
                            Button(
                                onClick = { viewModel.addAdmin() },
                                enabled = !isLoading && newAdminEmail.isNotBlank(),
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(50),
                                colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold)
                            ) { Text("Add Admin", fontWeight = FontWeight.Bold) }
                        }
                    }
                }

                item { Text("Current Admins", color = FJTextSecondary, fontSize = 13.sp) }

                if (isLoading && admins.isEmpty()) {
                    item {
                        Box(Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = FJGold)
                        }
                    }
                } else {
                    items(admins) { admin ->
                        Box(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                                .background(FJSurface).padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(admin.email, color = FJTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                    if (admin.isMasterAdmin) {
                                        Text("Master Admin", color = FJGold, fontSize = 12.sp)
                                    }
                                }
                                if (!admin.isMasterAdmin) {
                                    IconButton(onClick = { viewModel.removeAdmin(admin.id) }) {
                                        Icon(Icons.Default.Delete, "Remove", tint = FJError)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
