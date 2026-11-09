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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
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
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
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
    var hdrEnabled by remember { mutableStateOf(true) }
    var gridEnabled by remember { mutableStateOf(true) }
    var selectedCameraMode by remember { mutableStateOf("Photo") }
    var showEventSelectorMenu by remember { mutableStateOf(false) }
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
                .padding(horizontal = 12.dp, vertical = 8.dp)
                .testTag("camera_screen"),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Top Camera Bar matching Screen 4 in Reference: X, Golden Flash, HDR, [4K], Settings Gear
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { showEventSelectorMenu = !showEventSelectorMenu },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Switch Event",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // Flash Toggle Icon
                IconButton(
                    onClick = {
                        flashMode = when (flashMode) {
                            ImageCapture.FLASH_MODE_AUTO -> ImageCapture.FLASH_MODE_ON
                            ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_OFF
                            else -> ImageCapture.FLASH_MODE_AUTO
                        }
                    },
                    modifier = Modifier.size(36.dp)
                ) {
                    val flashIcon = when (flashMode) {
                        ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn
                        ImageCapture.FLASH_MODE_OFF -> Icons.Default.FlashOff
                        else -> Icons.Default.FlashAuto
                    }
                    Icon(
                        imageVector = flashIcon,
                        contentDescription = "Flash Mode",
                        tint = JummoGoldBright,
                        modifier = Modifier.size(20.dp)
                    )
                }

                // HDR Toggle Text
                Text(
                    text = "HDR",
                    style = MaterialTheme.typography.titleSmall,
                    color = if (hdrEnabled) Color.White else JummoTextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { hdrEnabled = !hdrEnabled }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                // 4K Framed Badge
                Surface(
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.85f)),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "4K",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp)
                    )
                }

                // Grid / Pro Settings Gear
                IconButton(
                    onClick = { gridEnabled = !gridEnabled },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Toggle Framing Grid",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
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
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Create Event", fontWeight = FontWeight.Bold)
                        }
                    }
                )
                return@Column
            }

            val eventCaptures = uploads.filter { it.eventId == currentEvent.id }

            // 2. Dominant Full-Bleed Viewfinder with Rule-of-Thirds Grid & Floating Event Selector Pill (Screen 4 in Reference)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .background(JummoDeepPurple)
                    .border(0.85.dp, JummoBorderSubtle, RoundedCornerShape(22.dp))
            ) {
                if (!hasCameraPermission) {
                    // Studio Fallback Preview Image behind permission card so the viewfinder looks rich
                    Image(
                        painter = painterResource(id = R.drawable.img_wedding_cover),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(JummoObsidian.copy(alpha = 0.65f))
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = JummoGoldBright,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Camera Access",
                            style = MaterialTheme.typography.headlineMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Allow Camera permission for live viewfinder, or tap the shutter below to capture a studio sample.",
                            style = MaterialTheme.typography.bodySmall,
                            color = JummoTextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JummoGoldBright,
                                contentColor = JummoObsidian
                            ),
                            shape = RoundedCornerShape(12.dp),
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

                // Rule-of-Thirds Composition Grid Overlay
                if (gridEnabled) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val thirdW = size.width / 3f
                        val thirdH = size.height / 3f
                        val lineColor = Color.White.copy(alpha = 0.22f)
                        drawLine(lineColor, Offset(thirdW, 0f), Offset(thirdW, size.height), strokeWidth = 1f)
                        drawLine(lineColor, Offset(thirdW * 2f, 0f), Offset(thirdW * 2f, size.height), strokeWidth = 1f)
                        drawLine(lineColor, Offset(0f, thirdH), Offset(size.width, thirdH), strokeWidth = 1f)
                        drawLine(lineColor, Offset(0f, thirdH * 2f), Offset(size.width, thirdH * 2f), strokeWidth = 1f)
                    }
                }

                // Floating Bottom Event Selector Pill inside Viewfinder: [Folder Icon] Event : <Event Name> v
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 14.dp)
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .clickable { showEventSelectorMenu = true },
                        color = JummoObsidian.copy(alpha = 0.78f),
                        border = BorderStroke(0.9.dp, JummoGoldBright.copy(alpha = 0.55f)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Event : ${currentEvent.name}",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = "Select Event",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    DropdownMenu(
                        expanded = showEventSelectorMenu,
                        onDismissRequest = { showEventSelectorMenu = false },
                        modifier = Modifier.background(JummoCardPurple)
                    ) {
                        activeEvents.forEach { evt ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${evt.name} (${evt.category})",
                                        color = if (evt.id == currentEvent.id) JummoGoldBright else Color.White,
                                        fontWeight = if (evt.id == currentEvent.id) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                onClick = {
                                    onSelectEvent(evt.id)
                                    showEventSelectorMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // 3. Bottom Camera Controls Dock matching Screen 4 in Reference:
            // Row 1: Left Recent Thumbnail | Center White/Gold Shutter | Right Circular Lens Switch
            // Row 2: Mode Selector Strip ("Photo" golden pill | "Video" | "Pro")
            val lensName = if (lensFacing == CameraSelector.LENS_FACING_BACK) "Rear" else "Front"
            val flashName = when (flashMode) {
                ImageCapture.FLASH_MODE_ON -> "ON"
                ImageCapture.FLASH_MODE_OFF -> "OFF"
                else -> "AUTO"
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left Recent Capture Square Thumbnail
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(JummoCardPurple)
                            .border(1.dp, JummoGoldBright.copy(alpha = 0.65f), RoundedCornerShape(12.dp))
                    ) {
                        val latestShot = eventCaptures.firstOrNull()
                        if (latestShot != null) {
                            AsyncImage(
                                model = latestShot.localUri,
                                contentDescription = latestShot.fileName,
                                placeholder = painterResource(id = R.drawable.img_wedding_cover),
                                error = painterResource(id = R.drawable.img_wedding_cover),
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.img_wedding_cover),
                                contentDescription = "Recent Capture Thumbnail",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    // Center Large White Shutter Circle with Golden Outer Ring
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .border(3.dp, JummoGoldBright, CircleShape)
                            .padding(5.dp)
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
                            .testTag("camera_capture_button")
                    )

                    // Right Circular Lens-Switch Button
                    Surface(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .clickable {
                                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                                    CameraSelector.LENS_FACING_FRONT
                                } else {
                                    CameraSelector.LENS_FACING_BACK
                                }
                            },
                        color = JummoCardPurple,
                        border = BorderStroke(1.dp, JummoBorderSubtle),
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Cameraswitch,
                                contentDescription = "Switch Lens",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Mode Selector Strip ("Photo" golden pill | "Video" | "Pro")
                Surface(
                    color = JummoCardPurple.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("Photo", "Video", "Pro").forEach { mode ->
                            val selected = selectedCameraMode == mode
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { selectedCameraMode = mode },
                                color = if (selected) JummoGoldBright else Color.Transparent,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = mode,
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (selected) JummoObsidian else JummoTextSecondary,
                                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
