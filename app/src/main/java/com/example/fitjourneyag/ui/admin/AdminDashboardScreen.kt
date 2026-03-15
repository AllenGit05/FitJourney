package com.example.fitjourneyag.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.fitjourneyag.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel,
    onNavigateToApi: () -> Unit,
    onNavigateToAdmins: () -> Unit,
    onLogout: () -> Unit
) {
    val totalClients  by viewModel.totalClients.collectAsState()
    val totalAdmins   by viewModel.totalAdmins.collectAsState()
    val isLoading     by viewModel.isLoading.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(FJBackground)) {
        Scaffold(
            containerColor = FJBackground,
            topBar = {
                TopAppBar(
                    title = { Text("Admin Dashboard", color = FJTextPrimary, fontWeight = FontWeight.Bold) },
                    actions = {
                        TextButton(onClick = onLogout) {
                            Icon(Icons.Default.Logout, null, tint = FJError)
                            Spacer(Modifier.width(4.dp))
                            Text("Logout", color = FJError, fontSize = 13.sp)
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
                Text("System Overview", color = FJTextSecondary, fontSize = 14.sp)

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
