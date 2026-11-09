package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusBadgePill
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoElevatedPurple
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoSuccessGreen
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CameraScreen(
    events: List<PhotoEvent>,
    selectedEventId: String?,
    uploads: List<PhotoUploadRecord>,
    onSelectEvent: (String) -> Unit,
    onCreateEventClick: () -> Unit,
    onPhotoCapturedToLocalStorage: (PhotoEvent, File) -> Unit,
    onFallbackStudioCapture: (PhotoEvent, String, String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    val activeEvents = events.filter { it.status == "Active" }.ifEmpty { events }
    val currentEvent = activeEvents.find { it.id == selectedEventId } ?: activeEvents.firstOrNull()

    var lensFacing by remember { mutableIntStateOf(CameraSelector.LENS_FACING_BACK) }
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_AUTO) }
    var cameraBindFailed by remember { mutableStateOf(false) }

    val imageCapture = remember(flashMode) {
        ImageCapture.Builder()
            .setFlashMode(flashMode)
            .build()
    }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(hasCameraPermission, lensFacing, flashMode) {
        if (hasCameraPermission) {
            try {
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    try {
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val selector = CameraSelector.Builder()
                            .requireLensFacing(lensFacing)
                            .build()
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            selector,
                            preview,
                            imageCapture
                        )
                        cameraBindFailed = false
                    } catch (e: Exception) {
                        cameraBindFailed = true
                    }
                }, ContextCompat.getMainExecutor(context))
            } catch (e: Exception) {
                cameraBindFailed = true
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .padding(horizontal = 20.dp, vertical = 14.dp)
                .testTag("camera_screen"),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Event Selection Bar
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                border = BorderStroke(0.75.dp, JummoBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Event",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        StatusBadgePill(
                            text = "LOCAL STORAGE",
                            color = JummoSuccessGreen
                        )
                    }

                    if (activeEvents.isEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Select or create an event before capturing photos.",
                                style = MaterialTheme.typography.bodySmall,
                                color = JummoTextSecondary,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = onCreateEventClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoGoldBright,
                                    contentColor = JummoObsidian
                                )
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Create Event", fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            activeEvents.forEach { evt ->
                                FilterChip(
                                    selected = currentEvent?.id == evt.id,
                                    onClick = { onSelectEvent(evt.id) },
                                    label = { Text("${evt.name} (${evt.category})") },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = JummoGoldBright,
                                        selectedLabelColor = JummoObsidian
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (currentEvent == null) {
                EmptyStateCard(
                    icon = Icons.Default.FolderSpecial,
                    title = "Select an Event Before Capturing",
                    message = "Create or select an event so captured photos are stored locally and linked to the event upload queue.",
                    actionButton = {
                        Button(
                            onClick = onCreateEventClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JummoGoldBright,
                                contentColor = JummoObsidian
                            )
                        ) {
                            Text("Create Event", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                return@Column
            }

            // CameraX Viewfinder + Controls
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(JummoDeepPurple)
                    .border(0.75.dp, JummoBorderSubtle, RoundedCornerShape(24.dp))
            ) {
                if (!hasCameraPermission) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(28.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = JummoGoldBright,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Camera Access",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Allow Camera permission to use the live viewfinder, flash, and front/rear lens switching.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = JummoTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JummoGoldBright,
                                contentColor = JummoObsidian
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("grant_camera_permission_button")
                        ) {
                            Text("Allow Camera Permission", fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    AndroidView(
                        factory = { previewView },
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Top Overlay: Flash & Camera Switching
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Flash Control Button
                    Surface(
                        color = JummoObsidian.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    flashMode = when (flashMode) {
                                        ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                                        ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                                        else -> ImageCapture.FLASH_MODE_AUTO
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val flashIcon = when (flashMode) {
                                ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                                ImageCapture.FLASH_MODE_OFF -> Icons.Default.FlashOff
                                else -> Icons.Default.FlashAuto
                            }
                            val flashLabel = when (flashMode) {
                                ImageCapture.FLASH_MODE_ON -> "Flash On"
                                ImageCapture.FLASH_MODE_OFF -> "Flash Off"
                                else -> "Flash Auto"
                            }
                            Icon(
                                imageVector = flashIcon,
                                contentDescription = flashLabel,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = flashLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Front / Rear Camera Switch
                    Surface(
                        color = JummoObsidian.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.75.dp, JummoVioletBright.copy(alpha = 0.6f))
                    ) {
                        Row(
                            modifier = Modifier
                                .clickable {
                                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                        CameraSelector.LENS_FACING_FRONT
                                    } else {
                                        CameraSelector.LENS_FACING_BACK
                                    }
                                }
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Switch Front/Rear Camera",
                                tint = JummoVioletBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (lensFacing == CameraSelector.LENS_FACING_BACK) "Rear Camera" else "Front Camera",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Bottom Shutter Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(JummoObsidian.copy(alpha = 0.80f))
                        .padding(vertical = 14.dp, horizontal = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val lensName = if (lensFacing == CameraSelector.LENS_FACING_BACK) "Rear" else "Front"
                    val flashName = when (flashMode) {
                        ImageCapture.FLASH_MODE_ON -> "ON"
                        ImageCapture.FLASH_MODE_OFF -> "OFF"
                        else -> "AUTO"
                    }

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(3.dp, JummoGoldBright, CircleShape)
                            .padding(6.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .clickable {
                                if (hasCameraPermission && !cameraBindFailed) {
                                    val photosDir = File(context.filesDir, "photos").apply { mkdirs() }
                                    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
                                    val photoFile = File(photosDir, "JUMMO_${timestamp}.JPG")
                                    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
                                    imageCapture.takePicture(
                                        outputOptions,
                                        ContextCompat.getMainExecutor(context),
                                        object : ImageCapture.OnImageSavedCallback {
                                            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                                                onPhotoCapturedToLocalStorage(currentEvent, photoFile)
                                            }

                                            override fun onError(exception: ImageCaptureException) {
                                                onFallbackStudioCapture(currentEvent, lensName, flashName)
                                            }
                                        }
                                    )
                                } else {
                                    onFallbackStudioCapture(currentEvent, lensName, flashName)
                                }
                            }
                            .testTag("camera_capture_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Capture Photo",
                            tint = JummoObsidian,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = currentEvent.name,
                        style = MaterialTheme.typography.labelSmall,
                        color = JummoGoldBright,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Recent Captured Photos Preview Strip for Selected Event
            val eventCaptures = uploads.filter { it.eventId == currentEvent.id }
            if (eventCaptures.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "Photo Preview (${eventCaptures.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(eventCaptures.take(10), key = { it.id }) { item ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = JummoElevatedPurple),
                                border = BorderStroke(0.75.dp, JummoBorderSubtle),
                                modifier = Modifier.width(116.dp)
                            ) {
                                Column {
                                    AsyncImage(
                                        model = item.localUri,
                                        contentDescription = item.fileName,
                                        placeholder = painterResource(id = R.drawable.img_wedding_cover),
                                        error = painterResource(id = R.drawable.img_wedding_cover),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(62.dp),
                                        contentScale = ContentScale.Crop
                                    )
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = item.fileName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = item.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (item.status == "Uploaded") JummoSuccessGreen else JummoGoldBright
                                        )
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
