package com.example.fitjourneyag.ui.client.ai

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.fitjourneyag.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun AiVoiceCallScreen(
    viewModel: AiCoachViewModel,
    onEndCall: () -> Unit
) {
    val context = LocalContext.current
    val user by viewModel.currentUser.collectAsState()
    val isTyping by viewModel.isTyping.collectAsState()
    val isAiSpeaking by viewModel.isAiSpeaking.collectAsState()
    val voiceTranscript by viewModel.voiceTranscript.collectAsState()
    
    // Voice Manager persistence
    val voiceManager = remember { VoiceManager(context) }
    val isListening by voiceManager.isListening.collectAsState()
    val currentSttText by voiceManager.transcript.collectAsState()

    // Sync transcripts to VM
    LaunchedEffect(currentSttText) {
        viewModel.updateVoiceTranscript(currentSttText)
    }

    // Permission handle
    var hasPermission by remember {
        mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)
    }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        hasPermission = isGranted
    }

    DisposableEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.RECORD_AUDIO)
        onDispose { voiceManager.shutdown() }
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val coachName = when(user?.coachPersona) {
        "Rex" -> "Coach Rex"
        "Zen" -> "Zen Master"
        "Custom" -> "My Coach"
        else -> "Coach Aurora"
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(FJBackground)
    ) {
        // Glowing background effect
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.radialGradient(
                        colors = if (isListening || isAiSpeaking) listOf(FJGold.copy(0.2f), Color.Transparent) else listOf(FJGold.copy(0.05f), Color.Transparent),
                        center = androidx.compose.ui.geometry.Offset(0.5f, 0.4f)
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Info
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(40.dp))
                Surface(
                    color = FJGold.copy(0.2f),
                    shape = RoundedCornerShape(20.dp),
                ) {
                    Row(
                        Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(Modifier.size(8.dp).clip(CircleShape).background(if (isListening || isAiSpeaking) Color(0xFF4CAF50) else Color.Gray))
                        Spacer(Modifier.width(8.dp))
                        Text(if (isListening) "LISTENING" else if (isAiSpeaking) "SPEAKING" else "IDLE", color = FJGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text(coachName, color = FJTextPrimary, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                Text(if (isTyping) "Thinking..." else "Connected", color = FJTextSecondary, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (user?.isPremium == true) {
                        Icon(Icons.Default.Security, null, tint = Color(0xFF4CAF50), modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Premium Member • Unlimited", color = Color(0xFF4CAF50), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    } else {
                        Icon(Icons.Default.CreditCard, null, tint = FJGold, modifier = Modifier.size(12.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${user?.aiCredits ?: 0} Credits remaining", color = FJGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Middle Section: Avatar & Pulse
            Box(contentAlignment = Alignment.Center) {
                // Pulse Circles (Only when active)
                if (isListening || isAiSpeaking) {
                    repeat(3) { index ->
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.5f + (index * 0.2f),
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000 + (index * 500), easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "pulse_ring"
                        )
                        val alpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(2000 + (index * 500), easing = LinearEasing),
                                repeatMode = RepeatMode.Restart
                            ),
                            label = "alpha_pulse"
                        )
                        
                        Box(
                            Modifier
                                .size(160.dp)
                                .graphicsLayer(scaleX = scale, scaleY = scale, alpha = alpha)
                                .border(2.dp, FJGold, CircleShape)
                        )
                    }
                }

                // Avatar
                Surface(
                    modifier = Modifier.size(160.dp),
                    shape = CircleShape,
                    color = FJSurface,
                    border = androidx.compose.foundation.BorderStroke(4.dp, if (isListening) FJGold else FJSurfaceHigh)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isAiSpeaking) Icons.Default.VolumeUp else Icons.Default.AutoAwesome, 
                            null, 
                            tint = if (isListening || isAiSpeaking) FJGold else FJTextSecondary, 
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }
            }

            // Bottom Section: Controls
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Transcript Preview
                Surface(
                    color = FJSurface.copy(0.5f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp)
                ) {
                    Box(Modifier.padding(16.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (voiceTranscript.isBlank()) "Tap the Mic and say something..." else voiceTranscript,
                            color = if (isListening) FJGold else FJTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }

                Spacer(Modifier.height(48.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Mute/Mic Toggle
                    CallControlButton(
                        if (isListening) Icons.Default.Mic else Icons.Default.MicOff, 
                        if (isListening) "Listening" else "Talk", 
                        if (isListening) FJGold else FJSurfaceHigh, 
                        if (isListening) FJOnGold else FJTextPrimary,
                        onClick = {
                            if (isListening) {
                                voiceManager.stopListening()
                            } else {
                                if (hasPermission) {
                                    // STOP AI when we start listening
                                    voiceManager.stopSpeaking()
                                    viewModel.setAiSpeaking(false)
                                    
                                    voiceManager.startListening { result ->
                                        viewModel.onVoiceResult(result) { response ->
                                            viewModel.setAiSpeaking(true)
                                            voiceManager.speakWithPersona(response, user?.coachPersona, user?.coachGender) {
                                                viewModel.setAiSpeaking(false)
                                            }
                                        }
                                    }
                                } else {
                                    launcher.launch(Manifest.permission.RECORD_AUDIO)
                                }
                            }
                        }
                    )
                    
                    CallControlButton(Icons.Default.CallEnd, "End", FJError, Color.White, onClick = onEndCall)
                    
                    var isSpeakerOn by remember { mutableStateOf(false) }
                    CallControlButton(
                        icon = if (isSpeakerOn) Icons.AutoMirrored.Filled.VolumeUp else Icons.Default.VolumeMute, 
                        label = if (isSpeakerOn) "Speaker" else "Normal", 
                        containerColor = if (isSpeakerOn) FJGold else FJSurfaceHigh, 
                        contentColor = if (isSpeakerOn) FJOnGold else FJTextPrimary,
                        onClick = {
                            isSpeakerOn = !isSpeakerOn
                            voiceManager.setSpeakerphone(isSpeakerOn)
                        }
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun CallControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit = {}
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(containerColor)
        ) {
            Icon(icon, label, tint = contentColor, modifier = Modifier.size(28.dp))
        }
        Spacer(Modifier.height(8.dp))
        Text(label, color = FJTextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}
