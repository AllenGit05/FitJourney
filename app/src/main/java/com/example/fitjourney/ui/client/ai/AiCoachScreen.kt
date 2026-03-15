package com.example.fitjourney.ui.client.ai

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Reply
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Delete
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitjourney.ui.theme.*
import com.example.fitjourney.ui.common.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiCoachScreen(
    viewModel: AiCoachViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToVoiceCall: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val calories by viewModel.caloriesEaten.collectAsState()
    val workouts by viewModel.workoutsToday.collectAsState()
    val user     by viewModel.currentUser.collectAsState()
    val showStore by viewModel.showCreditStore.collectAsState()
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var inputText by remember { mutableStateOf("") }
    var selectedPackageForPayment by remember { mutableStateOf<String?>(null) }
    var showPersonaDialog by remember { mutableStateOf(false) }
    var showClearDialog by remember { mutableStateOf(false) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    if (showStore) {
        CreditStoreDialog(
            isOutOfCredits = (user?.aiCredits ?: 0) <= 0 && user?.isPremium != true, 
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

    if (showPersonaDialog) {
        PersonaSelectionDialog(
            currentPersona = user?.coachPersona ?: "Aurora",
            currentCustomBio = user?.customCoachPersona ?: "",
            onDismiss = { showPersonaDialog = false },
            onSave = { persona, bio ->
                viewModel.updatePersona(persona, bio)
                showPersonaDialog = false
            }
        )
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            containerColor = FJSurface,
            title = { Text("Clear Conversation?", color = FJError, fontWeight = FontWeight.Bold) },
            text = { Text("This will permanently delete your current chat history with your coach.", color = FJTextSecondary) },
            confirmButton = {
                TextButton(onClick = { 
                    viewModel.clearChat()
                    showClearDialog = false 
                }) {
                    Text("Clear All", color = FJError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("Cancel", color = FJTextSecondary) }
            }
        )
    }

    Scaffold(
        containerColor = FJBackground,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    val coachName = when(user?.coachPersona) {
                        "Rex" -> "Coach Rex"
                        "Zen" -> "Zen Master"
                        "Custom" -> "My Coach"
                        else -> "Coach Aurora"
                    }
                    Text(coachName, color = FJTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Black)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = FJTextPrimary)
                    }
                },
                actions = {
                    if (user?.isPremium != true) {
                        Surface(
                            onClick = { viewModel.showStore() },
                            shape = CircleShape,
                            color = FJGold.copy(0.1f)
                        ) {
                            Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Add, null, tint = FJGold, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Add", color = FJGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(Modifier.width(16.dp))
                    }
                    IconButton(onClick = { showClearDialog = true }) {
                        Icon(Icons.Default.Delete, "Clear Chat", tint = FJTextPrimary)
                    }
                    IconButton(onClick = { showPersonaDialog = true }) {
                        Icon(Icons.Default.Settings, null, tint = FJTextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FJBackground)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = padding.calculateTopPadding())
        ) {
            // Credit Status Bar - Replaced kcal/workout stats
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FJSurface)
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (user?.isPremium == true) {
                    Icon(Icons.Default.Security, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Premium Membership • Unlimited Access", color = Color(0xFF4CAF50), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                } else {
                    Icon(Icons.Default.CreditCard, null, tint = FJGold, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("${user?.aiCredits ?: 0} AI Credits Remaining", color = FJGold, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                }
            }

            // Chat area
            Column(
                modifier = Modifier.weight(1f).imePadding()
            ) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (messages.isEmpty()) {
                        item {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    Icons.Default.AutoAwesome,
                                    null,
                                    tint = FJGold.copy(0.3f),
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    "How can I help you today?",
                                    color = FJTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                                Text("Ask Aurora for fitness advice", color = FJTextSecondary, fontSize = 14.sp)
                            }
                        }
                    }

                    items(messages) { msg ->
                        ChatBubble(msg, onLongClick = { viewModel.setReplyTo(msg) })
                    }
                    if (isTyping) {
                        item {
                            TypingIndicator()
                        }
                    }
                }

                // Reply Preview
                val replyToMsg by viewModel.replyTo.collectAsState()

                // Suggested Questions
                if (messages.size < 5 && replyToMsg == null) {
                    androidx.compose.foundation.lazy.LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val suggestions = listOf(
                            "Generate a workout plan",
                            "Analyze my progress",
                            "Nutritional advice",
                            "How to lose fat?"
                        )
                        items(suggestions) { suggestion ->
                            SuggestionChip(
                                label = suggestion,
                                onClick = {
                                    if (user?.isPremium == true || (user?.aiCredits ?: 0) > 0) {
                                        viewModel.sendMessage(suggestion)
                                    }
                                }
                            )
                        }
                    }
                }

                // Reply Preview
                if (replyToMsg != null) {
                    Surface(
                        color = FJSurfaceHigh,
                        modifier = Modifier.fillMaxWidth(),
                        border = androidx.compose.foundation.BorderStroke(1.dp, FJGold.copy(0.2f))
                    ) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Reply, null, tint = FJGold, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Replying to ${if (replyToMsg!!.isFromUser) "You" else "Coach"}", color = FJGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(replyToMsg!!.text, color = FJTextSecondary, fontSize = 12.sp, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                            }
                            IconButton(onClick = { viewModel.cancelReply() }, modifier = Modifier.size(24.dp)) {
                                Icon(Icons.Default.Close, null, tint = FJTextSecondary, modifier = Modifier.size(16.dp))
                            }
                        }
                    }
                }

                // Input Bar - Now just above the keyboard thanks to imePadding on weight(1f) parent
                Surface(
                    color = FJSurface,
                    modifier = Modifier.fillMaxWidth(),
                    shadowElevation = 16.dp,
                    tonalElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            placeholder = {
                                Text(
                                    "Ask about your diet or training...",
                                    color = FJTextSecondary,
                                    fontSize = 14.sp
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(28.dp)),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = FJBackground,
                                unfocusedContainerColor = FJBackground,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedTextColor = FJTextPrimary,
                                unfocusedTextColor = FJTextPrimary
                            ),
                            maxLines = 5,
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 15.sp),
                            enabled = user?.isPremium == true || (user?.aiCredits ?: 0) > 0
                        )
                        Spacer(Modifier.width(12.dp))
                        IconButton(
                            onClick = onNavigateToVoiceCall,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(FJSurfaceHigh)
                        ) {
                            Icon(
                                Icons.Default.Mic,
                                null,
                                tint = FJGold,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                if (inputText.isNotBlank()) {
                                    viewModel.sendMessage(inputText)
                                    inputText = ""
                                }
                            },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(if (user?.isPremium == true || (user?.aiCredits ?: 0) > 0) FJGold else FJSurfaceHigh)
                        ) {
                            val iconColor =
                                if (user?.isPremium == true || (user?.aiCredits ?: 0) > 0) FJOnGold else FJTextSecondary
                            Icon(
                                Icons.AutoMirrored.Filled.Send,
                                null,
                                tint = iconColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun ChatBubble(message: ChatMessage, onLongClick: () -> Unit) {
    val alignment = if (message.isFromUser) Alignment.End else Alignment.Start
    val bgColor = if (message.isFromUser) FJSurfaceHigh else FJSurface
    val shape = if (message.isFromUser) {
        RoundedCornerShape(16.dp, 16.dp, 4.dp, 16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 4.dp)
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Column(
            modifier = Modifier
                .widthIn(max = 260.dp)
                .clip(shape)
                .background(bgColor)
                .combinedClickable(
                    onLongClick = onLongClick,
                    onClick = {} // Keep normal click empty for now
                )
                .padding(10.dp)
        ) {
            if (message.repliedToText != null) {
                Surface(
                    color = FJGold.copy(0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp).fillMaxWidth()
                ) {
                    Column(Modifier.padding(8.dp)) {
                        Text("Replying to...", color = FJGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Text(
                            message.repliedToText,
                            color = FJTextSecondary,
                            fontSize = 12.sp,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
            }
            Text(message.text, color = FJTextPrimary, fontSize = 14.sp)
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).background(FJSurface).padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(3) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(FJGold.copy(alpha = 0.6f)))
        }
    }
}



@Composable
fun SuggestionChip(label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = FJSurface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, FJGold.copy(alpha = 0.3f))
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            color = FJGold,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}



@Composable
fun PaymentMethodOption(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) FJGold.copy(alpha = 0.1f) else FJBackground,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, FJGold) else androidx.compose.foundation.BorderStroke(1.dp, FJSurfaceHigh),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, color = FJTextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = "Selected", tint = FJGold)
            }
        }
    }
}
@Composable
fun PersonaSelectionDialog(
    currentPersona: String,
    currentCustomBio: String,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit
) {
    var selected by remember { mutableStateOf(currentPersona) }
    var customBio by remember { mutableStateOf(currentCustomBio) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FJSurface,
        title = { Text("Choose Your Coach", color = FJGold, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                PersonaOption("Aurora", "Supportive & Encouraging", "Supportive", selected == "Aurora") { selected = "Aurora" }
                PersonaOption("Rex", "Strict & High Energy", "No-nonsense", selected == "Rex") { selected = "Rex" }
                PersonaOption("Zen", "Calm & Mindful", "Holistic", selected == "Zen") { selected = "Zen" }
                PersonaOption("Custom", "Your Own Creation", "Define Personality", selected == "Custom") { selected = "Custom" }

                if (selected == "Custom") {
                    OutlinedTextField(
                        value = customBio,
                        onValueChange = { customBio = it },
                        placeholder = { Text("e.g. 'A friendly dog who loves fitness'", color = FJTextSecondary, fontSize = 12.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(color = FJTextPrimary, fontSize = 14.sp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = FJGold,
                            unfocusedBorderColor = FJSurfaceHigh
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(selected, customBio) },
                colors = ButtonDefaults.buttonColors(containerColor = FJGold)
            ) {
                Text("Update Coach", color = FJOnGold, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = FJTextSecondary) }
        }
    )
}

@Composable
fun PersonaOption(
    id: String,
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected) FJGold.copy(alpha = 0.1f) else FJBackground,
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, FJGold) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = FJTextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(subtitle, color = FJTextSecondary, fontSize = 11.sp)
            }
            RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = FJGold))
        }
    }
}
