package com.example.fitjourney.ui.admin.api

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApiManagementScreen(
    viewModel: AdminApiManagementViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showLiveTestProvider by remember { mutableStateOf<String?>(null) }

    // Show success snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccessMessage()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("API Management", color = FJTextPrimary, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = FJGold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FJBackground)
            )
        },
        containerColor = FJBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { Spacer(Modifier.height(8.dp)) }

            // Info banner
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0D1A0D)),
                    border = BorderStroke(1.dp, FJSuccess),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("🔐", fontSize = 20.sp)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "API keys are stored securely on this device's DataStore. " +
                            "They are never transmitted except directly to the providers.",
                            color = FJTextSecondary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            // ── GEMINI SECTION ──
            item {
                ApiKeySection(
                    title = "Gemini API",
                    subtitle = "Primary AI engine — gemini-2.0-flash",
                    badgeText = "PRIMARY",
                    badgeColor = FJGold,
                    keyInput = uiState.geminiKeyInput,
                    keyVisible = uiState.geminiKeyVisible,
                    isKeySaved = uiState.isGeminiKeySaved,
                    status = uiState.geminiStatus,
                    statusMessage = uiState.geminiStatusMessage,
                    isTesting = uiState.isTestingGemini,
                    placeholder = "AIzaSy...",
                    getKeyUrl = "aistudio.google.com",
                    onKeyChanged = viewModel::onGeminiKeyChanged,
                    onToggleVisibility = viewModel::toggleGeminiKeyVisibility,
                    onSave = viewModel::saveGeminiKey,
                    onDelete = viewModel::deleteGeminiKey,
                    onTest = viewModel::testGeminiConnection,
                    onLiveTest = { showLiveTestProvider = "Gemini" },
                    onClearStatus = viewModel::clearGeminiStatus
                )
            }

            item {
                if (uiState.geminiStatus == "RATE_LIMITED") {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1200)),
                        border = BorderStroke(1.dp, Color(0xFFFFB300).copy(0.4f)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                            Text("⚡", fontSize = 16.sp)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text(
                                    "Gemini Free Tier Limits",
                                    color = Color(0xFFFFB300),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "• 15 requests/minute (RPM)\n" +
                                    "• 1,500 requests/day (RPD)\n" +
                                    "• Daily quota resets at midnight Pacific Time\n\n" +
                                    "Your Groq fallback (Llama 3.3 70B) is automatically handling requests while Gemini is rate limited. No action needed.",
                                    color = FJTextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            // ── GROQ SECTION ──
            item {
                ApiKeySection(
                    title = "Groq API",
                    subtitle = "Fallback engine — llama-3.3-70b-versatile",
                    badgeText = "FALLBACK",
                    badgeColor = FJCarbs,
                    keyInput = uiState.groqKeyInput,
                    keyVisible = uiState.groqKeyVisible,
                    isKeySaved = uiState.isGroqKeySaved,
                    status = uiState.groqStatus,
                    statusMessage = uiState.groqStatusMessage,
                    isTesting = uiState.isTestingGroq,
                    placeholder = "gsk_...",
                    getKeyUrl = "console.groq.com",
                    onKeyChanged = viewModel::onGroqKeyChanged,
                    onToggleVisibility = viewModel::toggleGroqKeyVisibility,
                    onSave = viewModel::saveGroqKey,
                    onDelete = viewModel::deleteGroqKey,
                    onTest = viewModel::testGroqConnection,
                    onLiveTest = { showLiveTestProvider = "Groq" },
                    onClearStatus = viewModel::clearGroqStatus
                )
            }

            // ── ELEVENLABS SECTION ──
            item {
                ApiKeySection(
                    title = "ElevenLabs API",
                    subtitle = "Voice Synthesis — Multilingual v2",
                    badgeText = "VOICE TTS",
                    badgeColor = Color(0xFFE91E63), // Pinkish for voice
                    keyInput = uiState.elevenLabsKeyInput,
                    keyVisible = uiState.elevenLabsKeyVisible,
                    isKeySaved = uiState.isElevenLabsKeySaved,
                    status = uiState.elevenLabsStatus,
                    statusMessage = uiState.elevenLabsStatusMessage,
                    isTesting = uiState.isTestingElevenLabs,
                    placeholder = "eleven_...",
                    getKeyUrl = "elevenlabs.io",
                    onKeyChanged = viewModel::onElevenLabsKeyChanged,
                    onToggleVisibility = viewModel::toggleElevenLabsKeyVisibility,
                    onSave = viewModel::saveElevenLabsKey,
                    onDelete = viewModel::deleteElevenLabsKey,
                    onTest = viewModel::testElevenLabsConnection,
                    onLiveTest = { /* Not needed for TTS */ },
                    onClearStatus = viewModel::clearElevenLabsStatus
                )
            }

            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1215)),
                    border = BorderStroke(1.dp, Color(0xFFE91E63).copy(0.4f)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.Top) {
                        Text("🎤", fontSize = 16.sp)
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "ElevenLabs Free Tier Info",
                                color = Color(0xFFE91E63),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "• 10,000 characters/month (refills monthly)\n" +
                                "• Standard API access to all voices\n" +
                                "• Attribution required for free tier usage\n\n" +
                                "The app automatically falls back to local Android TTS if quota is exceeded or key is missing.",
                                color = FJTextSecondary,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        if (showLiveTestProvider != null) {
            AdminApiTestDialog(
                provider = showLiveTestProvider!!,
                viewModel = viewModel,
                onDismiss = { showLiveTestProvider = null; viewModel.clearTest() }
            )
        }
    }
}

@Composable
private fun ApiKeySection(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: Color,
    keyInput: String,
    keyVisible: Boolean,
    isKeySaved: Boolean,
    status: String,
    statusMessage: String,
    isTesting: Boolean,
    placeholder: String,
    getKeyUrl: String,
    onKeyChanged: (String) -> Unit,
    onToggleVisibility: () -> Unit,
    onSave: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit,
    onLiveTest: () -> Unit,
    onClearStatus: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FJSurface),
        border = BorderStroke(1.dp, FJSurfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            title,
                            color = FJTextPrimary,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(8.dp))
                        Surface(
                            color = badgeColor.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                badgeText,
                                color = badgeColor,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(
                        subtitle,
                        color = FJTextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                // Status dot
                val dotColor = when (status) {
                    "ONLINE" -> FJSuccess
                    "RATE_LIMITED" -> Color(0xFFFFB300)
                    "ERROR" -> FJError
                    else -> FJTextSecondary
                }
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(dotColor, CircleShape)
                )
            }

            Spacer(Modifier.height(12.dp))

            // Key input field
            OutlinedTextField(
                value = keyInput,
                onValueChange = onKeyChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(placeholder, color = FJTextSecondary, fontSize = 14.sp)
                },
                visualTransformation = if (keyVisible)
                    VisualTransformation.None
                else
                    PasswordVisualTransformation(),
                trailingIcon = {
                    Row {
                        IconButton(onClick = onToggleVisibility) {
                            Icon(
                                imageVector = if (keyVisible)
                                    Icons.Default.VisibilityOff
                                else
                                    Icons.Default.Visibility,
                                contentDescription = "Toggle visibility",
                                tint = FJTextSecondary
                            )
                        }
                        // Delete button (only if key is saved)
                        if (isKeySaved) {
                            IconButton(onClick = onDelete) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete key",
                                    tint = FJError
                                )
                            }
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = badgeColor,
                    unfocusedBorderColor = FJSurfaceVariant,
                    focusedTextColor = FJTextPrimary,
                    unfocusedTextColor = FJTextPrimary,
                    cursorColor = badgeColor
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Where to get key hint
            Text(
                "Get key: $getKeyUrl",
                color = FJTextSecondary,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(Modifier.height(12.dp))

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Save button
                Button(
                    onClick = onSave,
                    enabled = keyInput.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = badgeColor,
                        disabledContainerColor = FJSurfaceVariant
                    ),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        Icons.Default.Save,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = FJBackground
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        if (isKeySaved) "Update" else "Save",
                        color = FJBackground,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                // Test button
                OutlinedButton(
                    onClick = onTest,
                    enabled = !isTesting && keyInput.isNotBlank(),
                    border = BorderStroke(1.dp, if (keyInput.isNotBlank()) badgeColor else FJSurfaceVariant),
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = badgeColor,
                            strokeWidth = 2.dp
                        )
                    } else {
                        val tint = if (keyInput.isNotBlank()) badgeColor else FJTextSecondary
                        Icon(
                            Icons.Default.NetworkCheck,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = tint
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Test",
                            color = tint,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                // Live Test button
                if (isKeySaved) {
                    IconButton(
                        onClick = onLiveTest,
                        modifier = Modifier.background(FJSurfaceVariant.copy(0.3f), CircleShape).size(40.dp)
                    ) {
                        Icon(Icons.Default.Chat, "Live Test", tint = FJGold, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // Status message
            if (statusMessage.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                val msgColor = when (status) {
                    "ONLINE"       -> FJSuccess
                    "RATE_LIMITED" -> Color(0xFFFFB300)
                    "ERROR"        -> FJError
                    else           -> FJTextSecondary
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        if (status == "ONLINE") Icons.Default.CheckCircle else Icons.Default.Warning,
                        null,
                        modifier = Modifier.size(14.dp),
                        tint = msgColor
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        statusMessage,
                        color = msgColor,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        modifier = Modifier.weight(1f)
                    )
                    // Show "Clear" button for stuck error/rate-limited states
                    if (status == "RATE_LIMITED" || status == "ERROR") {
                        TextButton(
                            onClick = onClearStatus,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text("Clear", color = FJTextSecondary, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminApiTestDialog(
    provider: String,
    viewModel: AdminApiManagementViewModel,
    onDismiss: () -> Unit
) {
    var testPrompt by remember { mutableStateOf("Tell me a fitness tip") }
    val uiState by viewModel.uiState.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FJSurface,
        title = { Text("Live API Test: $provider", color = FJTextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = testPrompt,
                    onValueChange = { testPrompt = it },
                    label = { Text("Test Message") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = FJTextPrimary,
                        unfocusedTextColor = FJTextPrimary,
                        focusedBorderColor = FJGold
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 200.dp),
                    colors = CardDefaults.cardColors(containerColor = FJBackground),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    LazyColumn(Modifier.padding(12.dp)) {
                        item {
                            Text(
                                text = uiState.testResponse ?: "Enter a message and click 'Send Test' to verify the API response.",
                                color = if (uiState.testResponse?.startsWith("Error") == true) FJError else FJTextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { viewModel.testAiPrompt(provider, testPrompt) },
                enabled = !uiState.isLiveTesting && testPrompt.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = FJGold)
            ) {
                if (uiState.isLiveTesting) {
                    CircularProgressIndicator(Modifier.size(20.dp), color = FJBackground, strokeWidth = 2.dp)
                } else {
                    Text("Send Test", color = FJBackground, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = FJTextSecondary)
            }
        }
    )
}
