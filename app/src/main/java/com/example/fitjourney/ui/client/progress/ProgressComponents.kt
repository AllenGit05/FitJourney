package com.example.fitjourney.ui.client.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import androidx.core.content.ContextCompat
import android.Manifest
import android.content.pm.PackageManager
import coil.compose.AsyncImage
import com.example.fitjourney.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWeightDialog(onDismiss: () -> Unit, onAdd: (Float) -> Unit) {
    var weight by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FJSurface,
        title = { Text("Log Weight", color = FJTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            OutlinedTextField(
                value = weight, onValueChange = { weight = it },
                placeholder = { Text("Weight (kg)", color = FJTextSecondary) },
                singleLine = true, modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FJGold, focusedContainerColor = FJSurfaceHigh)
            )
        },
        confirmButton = {
            Button(onClick = { weight.toFloatOrNull()?.let { onAdd(it) } }, colors = ButtonDefaults.buttonColors(FJGold)) { Text("Save") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPhotoDialog(onDismiss: () -> Unit, onAdd: (android.net.Uri, Float, String) -> Unit) {
    var imageUri by remember { mutableStateOf<android.net.Uri?>(null) }
    var weight by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    val context = LocalContext.current
    
    val tempFile = remember { 
        val file = File(context.cacheDir, "images")
        if (!file.exists()) file.mkdirs()
        File(file, "temp_camera_${System.currentTimeMillis()}.jpg")
    }
    val cameraUri = remember {
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            tempFile
        )
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) imageUri = uri
    }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) imageUri = cameraUri
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            cameraLauncher.launch(cameraUri)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = FJSurface,
        title = { Text("Add Progress Photo", color = FJTextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    onClick = { galleryLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    color = FJSurfaceHigh,
                    border = androidx.compose.foundation.BorderStroke(1.dp, FJGold.copy(0.3f))
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PhotoCamera, null, tint = FJGold, modifier = Modifier.size(40.dp))
                                Spacer(Modifier.height(8.dp))
                                Text("Select Image", color = FJTextSecondary, fontSize = 12.sp)
                            }
                        }
                    }
                }

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { 
                            val hasPermission = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                            if (hasPermission) {
                                cameraLauncher.launch(cameraUri)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FJSurfaceHigh),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, null, tint = FJGold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Camera", color = FJGold, fontSize = 12.sp)
                    }
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = FJSurfaceHigh),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, null, tint = FJGold, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("Gallery", color = FJGold, fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = weight,
                    onValueChange = { weight = it },
                    label = { Text("Current Weight (kg)", color = FJTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FJGold, focusedContainerColor = FJSurfaceHigh)
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Notes (Optional)", color = FJTextSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = FJGold, focusedContainerColor = FJSurfaceHigh)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    imageUri?.let { uri ->
                        weight.toFloatOrNull()?.let { w ->
                            onAdd(uri, w, note)
                        }
                    }
                },
                enabled = imageUri != null && weight.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(FJGold)
            ) {
                Text("Save Photo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = FJTextSecondary)
            }
        }
    )
}

@Composable
fun PhotoComparisonOverlay(
    photo1: com.example.fitjourney.domain.model.ProgressPhoto, 
    photo2: com.example.fitjourney.domain.model.ProgressPhoto, 
    onDismiss: () -> Unit
) {
    val sorted = listOf(photo1, photo2).sortedBy { it.date }
    val before = sorted[0]
    val after = sorted[1]

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        confirmButton = {
            Button(onClick = onDismiss, colors = ButtonDefaults.buttonColors(containerColor = FJGold), modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Text("Close Comparison", color = FJOnGold)
            }
        },
        text = {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ComparisonItem("Before", before, Modifier.weight(1f))
                    ComparisonItem("After", after, Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
                val weightDiff = after.weight - before.weight
                val diffColor = if (weightDiff <= 0) Color.Green else Color.Red
                Text(
                    "Weight change: ${if (weightDiff > 0) "+" else ""}${String.format("%.1f", weightDiff)} kg",
                    color = diffColor,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        },
        containerColor = FJSurface,
        shape = RoundedCornerShape(24.dp)
    )
}

@Composable
fun ComparisonItem(label: String, photo: com.example.fitjourney.domain.model.ProgressPhoto, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = FJGold, fontWeight = FontWeight.ExtraBold, fontSize = 16.sp)
        Text(
            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(photo.date)),
            color = FJTextSecondary,
            fontSize = 12.sp
        )
        Spacer(Modifier.height(8.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.75f)
                .clip(RoundedCornerShape(12.dp))
                .background(FJSurfaceHigh),
            contentAlignment = Alignment.Center
        ) {
            AsyncImage(
                model = photo.imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                error = androidx.compose.ui.graphics.painter.ColorPainter(FJSurfaceHigh)
            )
        }
        Spacer(Modifier.height(8.dp))
        Text("${photo.weight} kg", color = FJTextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ProgressStatCard(label: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = FJSurface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(48.dp).clip(CircleShape).background(FJGold.copy(0.1f)), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = FJGold)
            }
            Spacer(Modifier.width(16.dp))
            Column {
                Text(label, color = FJTextSecondary, fontSize = 12.sp)
                Text(value, color = FJTextPrimary, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}
