package com.example.fitjourney.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
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
import com.example.fitjourney.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onNavigateToApi: () -> Unit,
    onNavigateToAdmins: () -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToDiagnostics: () -> Unit,
    onLogout: () -> Unit
) {
    val totalClients  by viewModel.totalClients.collectAsState()
    val totalAdmins   by viewModel.totalAdmins.collectAsState()
    val isLoading     by viewModel.isLoading.collectAsState()
    val isSyncing     by viewModel.isSyncing.collectAsState()
    val syncMessage   by viewModel.syncMessage.collectAsState()
    val lastSyncTime  by viewModel.lastSyncTime.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(FJBackground)) {
        Scaffold(
            containerColor = FJBackground,
            topBar = {
                TopAppBar(
                    title = { Text("Admin Dashboard", color = FJTextPrimary, fontWeight = FontWeight.Bold) },
                    actions = {
                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier.padding(end = 8.dp).width(90.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Logout,
                                    contentDescription = "Logout",
                                    tint = FJError,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text("Logout", color = FJError, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = FJBackground)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("System Overview", color = FJTextSecondary, fontSize = 14.sp)
                    IconButton(
                        onClick = { viewModel.refreshStats() },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = FJGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                if (isLoading) {
                    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = FJGold)
                    }
                } else {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        AdminStatCard(Modifier.weight(1f), Icons.Default.People,   "Clients",   totalClients.toString())
                        AdminStatCard(Modifier.weight(1f), Icons.Default.AdminPanelSettings,"Admins",   totalAdmins.toString())
                    }
                }

                Text("Management", color = FJTextSecondary, fontSize = 14.sp)

                AdminNavCard(icon = Icons.Default.Key, label = "Manage APIs",
                    sub = "Configure AI Coach provider keys", onClick = onNavigateToApi)
                AdminNavCard(icon = Icons.Default.AdminPanelSettings, label = "Manage Admins",
                    sub = "Add or remove admin accounts", onClick = onNavigateToAdmins)
                AdminNavCard(icon = Icons.Default.LocalFireDepartment, label = "Firebase Diagnostics",
                    sub = "Run system-wide integration tests", onClick = onNavigateToDiagnostics)

                Text("Cloud Sync", color = FJTextSecondary, fontSize = 14.sp)
                
                Box(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
                        .background(FJSurface).padding(18.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudSync, null, tint = FJGold)
                            Spacer(Modifier.width(12.dp))
                            Text("Database Sync Status", color = FJTextPrimary, fontWeight = FontWeight.SemiBold)
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = if (isSyncing) "Syncing..." else if (lastSyncTime == null) "Not synced yet" 
                                   else "Last synced: ${java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(lastSyncTime!!))}",
                            color = FJTextSecondary,
                            fontSize = 13.sp
                        )
                        if (syncMessage != null) {
                            Text(syncMessage!!, color = if (syncMessage!!.contains("failed")) FJError else FJGold, fontSize = 12.sp)
                        }
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { viewModel.triggerSync() },
                                enabled = !isSyncing,
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = FJGold, contentColor = FJOnGold)
                            ) { Text("Force Sync", fontSize = 13.sp) }
                            
                            OutlinedButton(
                                onClick = { viewModel.resetFirebase() },
                                modifier = Modifier.weight(1f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, FJError),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = FJError)
                            ) { Text("Reset Firebase", fontSize = 13.sp) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(modifier: Modifier, icon: ImageVector, label: String, value: String) {
    Box(
        modifier = modifier.clip(RoundedCornerShape(14.dp)).background(FJSurface).padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = FJGold, modifier = Modifier.size(24.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, color = FJGold, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
            Text(label, color = FJTextSecondary, fontSize = 11.sp)
        }
    }
}

@Composable
private fun AdminNavCard(icon: ImageVector, label: String, sub: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(14.dp))
            .background(FJSurface).clickable(onClick = onClick).padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(FJSurfaceHigh),
                contentAlignment = Alignment.Center
            ) { Icon(icon, null, tint = FJGold, modifier = Modifier.size(22.dp)) }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(label, color = FJTextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                Text(sub, color = FJTextSecondary, fontSize = 12.sp)
            }
            Icon(Icons.Default.ChevronRight, null, tint = FJTextSecondary)
        }
    }
}
