package com.example.fitjourneyag.ui.client.progress

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.fitjourneyag.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressPhotosScreen(
    viewModel: ProgressViewModel,
    onNavigateBack: () -> Unit
) {
    val photos by viewModel.photos.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var isCompareMode by remember { mutableStateOf(false) }
    val selectedPhotos = remember { mutableStateListOf<com.example.fitjourneyag.domain.model.ProgressPhoto>() }

    Scaffold(
        containerColor = FJBackground,
        topBar = {
            TopAppBar(
                title = { Text("Progress Photos", color = FJTextPrimary, fontWeight = FontWeight.ExtraBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Default.ArrowBack, null, tint = FJTextPrimary) }
                },
                actions = {
                    if (photos.size >= 2) {
                        TextButton(onClick = { 
                            isCompareMode = !isCompareMode
                            if (!isCompareMode) selectedPhotos.clear()
                        }) {
                            Text(if (isCompareMode) "Cancel" else "Compare", color = FJGold, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = FJBackground)
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }, containerColor = FJGold, contentColor = FJOnGold) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (isCompareMode) {
                Surface(
                    color = FJGold.copy(alpha = 0.1f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Select 2 photos to compare (${selectedPhotos.size}/2)",
                        modifier = Modifier.padding(16.dp),
                        color = FJGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }

            if (photos.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PhotoCamera, null, modifier = Modifier.size(64.dp), tint = FJSurfaceHigh)
                        Spacer(Modifier.height(16.dp))
                        Text("No photos uploaded yet", color = FJTextSecondary)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(photos) { photo: com.example.fitjourneyag.domain.model.ProgressPhoto ->
                        val isSelected = selectedPhotos.contains(photo)
                        PhotoGridItem(
                            photo = photo,
                            isSelected = isSelected,
                            isCompareMode = isCompareMode,
                            onDelete = { viewModel.deletePhoto(photo) },
                            onClick = {
                                if (isCompareMode) {
                                    if (isSelected) selectedPhotos.remove(photo)
                                    else if (selectedPhotos.size < 2) selectedPhotos.add(photo)
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddPhotoDialog(
            onDismiss = { showAddDialog = false },
            onAdd = { url, w, n -> viewModel.uploadPhoto(url, w, n); showAddDialog = false }
        )
    }

    if (selectedPhotos.size == 2) {
        PhotoComparisonOverlay(selectedPhotos[0], selectedPhotos[1]) { selectedPhotos.clear(); isCompareMode = false }
    }
}


@Composable
private fun PhotoGridItem(
    photo: com.example.fitjourneyag.domain.model.ProgressPhoto,
    isSelected: Boolean,
    isCompareMode: Boolean,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = FJSurface,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(3.dp, FJGold) else null
    ) {
        Column {
            Box(
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
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
                if (isCompareMode) {
                    Box(
                        Modifier
                            .fillMaxSize()
                            .background(if (isSelected) FJGold.copy(alpha = 0.3f) else Color.Transparent)
                    )
                }
            }
            Column(Modifier.padding(12.dp)) {
                val dateStr = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(photo.date))
                Text(
                    dateStr,
                    color = FJTextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Text("${photo.weight} kg", color = FJGold, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            }
            Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.BottomEnd) {
                IconButton(onClick = onDelete) {
                  Icon(Icons.Default.Delete, null, tint = FJTextSecondary.copy(0.5f), modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}
