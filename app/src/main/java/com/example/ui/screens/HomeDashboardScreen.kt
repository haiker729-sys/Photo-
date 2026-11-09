package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.GuestAccessModes
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.PhotographerProfile
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.MetricStatCard
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.StatusBadgePill
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoElevatedPurple
import com.example.ui.theme.JummoErrorRed
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoSuccessGreen
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import com.example.ui.theme.JummoWarningAmber
import com.example.ui.viewmodel.JummoViewModel

@Composable
fun HomeDashboardScreen(
    profile: PhotographerProfile?,
    events: List<PhotoEvent>,
    uploads: List<PhotoUploadRecord>,
    onCreateNewEventClick: () -> Unit,
    onQuickCameraClick: () -> Unit = {},
    onOpenEventDetails: (PhotoEvent) -> Unit = {},
    onOpenEventQr: (PhotoEvent) -> Unit,
    onOpenGuestPortal: (PhotoEvent) -> Unit,
    onSelectEventAndOpenCamera: (PhotoEvent) -> Unit,
    onOpenDriveSettings: () -> Unit,
    onNavigateToEventsTab: () -> Unit
) {
    val context = LocalContext.current

    // Strictly real metrics derived from live state and local storage
    val totalEvents = events.size
    val activeEventsCount = events.count { it.status == "Active" }
    val totalPhotos = if (uploads.isNotEmpty()) {
        uploads.size.toLong()
    } else {
        events.sumOf { it.totalPhotos }
    }
    val uploadedPhotos = if (uploads.isNotEmpty()) {
        uploads.count { it.status == "Uploaded" }.toLong()
    } else {
        events.sumOf { it.uploadedPhotos }
    }
    val pendingPhotos = if (uploads.isNotEmpty()) {
        uploads.count { it.status == "Pending" || it.status == "Uploading" }.toLong()
    } else {
        events.sumOf { it.pendingPhotos }
    }
    val failedUploads = if (uploads.isNotEmpty()) {
        uploads.count { it.status == "Failed" }.toLong()
    } else {
        events.sumOf { it.failedPhotos }
    }
    val driveConnected = profile?.driveConnected == true

    val storageMetrics = remember(uploads) {
        JummoViewModel.computeStorageUsage(context, uploads)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .testTag("home_dashboard_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Jummo Photos AI Logo, Greeting & User Profile, Create Event & Quick Camera Actions
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        JummoElevatedPurple.copy(alpha = 0.65f),
                                        JummoCardPurple
                                    )
                                )
                            )
                            .padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Brand Logo & Studio Profile Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.img_app_icon_foreground),
                                    contentDescription = "Jummo Photos AI Logo",
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Jummo Photos AI",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = profile?.studioName ?: "Jummo Pro Studio",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = JummoGoldBright
                                    )
                                }
                            }
                            StatusBadgePill(
                                text = if (profile?.autoUploadEnabled != false) "AUTO-UPLOAD" else "MANUAL QUEUE",
                                color = JummoVioletBright
                            )
                        }

                        // Greeting & User Profile Info
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "Welcome, ${profile?.displayName ?: "Photographer"}",
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            if (!profile?.email.isNullOrBlank()) {
                                Text(
                                    text = profile?.email.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JummoTextSecondary
                                )
                            }
                        }

                        // Primary Actions: Create Event & Quick Camera
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                onClick = onCreateNewEventClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoGoldBright,
                                    contentColor = JummoObsidian
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("home_create_event_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddCircle,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Create Event",
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            OutlinedButton(
                                onClick = onQuickCameraClick,
                                border = BorderStroke(1.dp, JummoVioletBright.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp)
                                    .testTag("home_quick_camera_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = JummoGoldBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Quick Camera",
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            // 2. Upload Status & Event Counts
            item {
                SectionHeaderRow(
                    title = "Upload Status",
                    subtitle = "Live event and upload queue counts"
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            title = "Total Events",
                            value = totalEvents.toString(),
                            subtitle = "$activeEventsCount active",
                            icon = Icons.Default.Event,
                            accentColor = JummoVioletBright,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Total Photos",
                            value = totalPhotos.toString(),
                            subtitle = "Originals stored",
                            icon = Icons.Default.PhotoLibrary,
                            accentColor = JummoGoldBright,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        MetricStatCard(
                            title = "Uploaded Photos",
                            value = uploadedPhotos.toString(),
                            subtitle = "Synced to cloud",
                            icon = Icons.Default.CheckCircle,
                            accentColor = JummoSuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Pending Photos",
                            value = pendingPhotos.toString(),
                            subtitle = "In upload queue",
                            icon = Icons.Default.Schedule,
                            accentColor = JummoWarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    MetricStatCard(
                        title = "Failed Uploads",
                        value = failedUploads.toString(),
                        subtitle = if (failedUploads == 0L) {
                            "No failed transfers"
                        } else {
                            "Open Uploads tab to retry"
                        },
                        icon = Icons.Default.ErrorOutline,
                        accentColor = if (failedUploads == 0L) JummoSuccessGreen else JummoErrorRed,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 3. Google Drive Connection Status & Storage Usage Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Google Drive Connection Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drive_connection_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                        border = BorderStroke(0.75.dp, JummoBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (driveConnected) {
                                            JummoSuccessGreen.copy(alpha = 0.16f)
                                        } else {
                                            JummoVioletBright.copy(alpha = 0.16f)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (driveConnected) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                    contentDescription = "Google Drive Status",
                                    tint = if (driveConnected) JummoSuccessGreen else JummoGoldBright,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Google Drive",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadgePill(
                                        text = if (driveConnected) "CONNECTED" else "NOT CONNECTED",
                                        color = if (driveConnected) JummoSuccessGreen else JummoTextSecondary
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (driveConnected) {
                                        "Folder: /${profile?.driveFolderName ?: "Jummo_Events_Cloud"}"
                                    } else {
                                        "Configure folder & server-side Drive bridge in Settings"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JummoTextSecondary
                                )
                            }
                            OutlinedButton(
                                onClick = onOpenDriveSettings,
                                border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "Settings",
                                    color = JummoGoldBright,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                    }

                    // Storage Usage Card (Real local photo files & device storage)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("storage_usage_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                        border = BorderStroke(0.75.dp, JummoBorderSubtle)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(JummoGoldBright.copy(alpha = 0.14f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Storage,
                                            contentDescription = "Storage Usage",
                                            tint = JummoGoldBright,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Local Storage Usage",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${storageMetrics.localPhotoFileCount} local original file(s) • Originals preserved",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = JummoTextSecondary
                                        )
                                    }
                                }
                                Text(
                                    text = storageMetrics.formatBytes(storageMetrics.combinedManagedBytes),
                                    style = MaterialTheme.typography.titleSmall,
                                    color = JummoGoldBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val usageRatio = if (storageMetrics.totalDeviceBytes > 0L) {
                                val usedDevice = (storageMetrics.totalDeviceBytes - storageMetrics.availableDeviceBytes)
                                    .coerceAtLeast(0L)
                                (usedDevice.toFloat() / storageMetrics.totalDeviceBytes.toFloat()).coerceIn(0.02f, 1f)
                            } else {
                                0.05f
                            }
                            LinearProgressIndicator(
                                progress = { usageRatio },
                                color = JummoVioletBright,
                                trackColor = JummoDeepPurple,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(50))
                            )
                            if (storageMetrics.availableDeviceBytes > 0L) {
                                Text(
                                    text = "Available device space: ${storageMetrics.formatBytes(storageMetrics.availableDeviceBytes)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JummoTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 4. Recent Events Section
            item {
                SectionHeaderRow(
                    title = "Recent Events",
                    subtitle = if (events.isEmpty()) null else "Select an event for details, QR code, or camera",
                    trailingContent = {
                        if (events.isNotEmpty()) {
                            Text(
                                text = "All Events (${events.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = JummoGoldBright,
                                modifier = Modifier
                                    .clickable { onNavigateToEventsTab() }
                                    .padding(6.dp)
                            )
                        }
                    }
                )
            }

            if (events.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Event,
                        title = "No Events Yet",
                        message = "Create an event to generate a unique QR code and start capturing or importing photos.",
                        actionButton = {
                            Button(
                                onClick = onCreateNewEventClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoGoldBright,
                                    contentColor = JummoObsidian
                                ),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(imageVector = Icons.Default.AddCircle, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Event", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }
            } else {
                items(events.take(5), key = { it.id }) { event ->
                    RecentEventSummaryCard(
                        event = event,
                        onOpenDetails = { onOpenEventDetails(event) },
                        onOpenQr = { onOpenEventQr(event) },
                        onShootCamera = { onSelectEventAndOpenCamera(event) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentEventSummaryCard(
    event: PhotoEvent,
    onOpenDetails: () -> Unit,
    onOpenQr: () -> Unit,
    onShootCamera: () -> Unit
) {
    val coverPainter = when (event.coverType) {
        "festival", "School", "Corporate" -> painterResource(id = R.drawable.img_festival_cover)
        "studio" -> painterResource(id = R.drawable.img_hero_studio)
        else -> painterResource(id = R.drawable.img_wedding_cover)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() }
            .testTag("recent_event_card_${event.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(0.75.dp, JummoBorderSubtle)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(124.dp)
            ) {
                if (event.coverUri.isNotBlank()) {
                    AsyncImage(
                        model = event.coverUri,
                        contentDescription = event.name,
                        placeholder = coverPainter,
                        error = coverPainter,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = coverPainter,
                        contentDescription = event.name,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    JummoObsidian.copy(alpha = 0.88f)
                                )
                            )
                        )
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StatusBadgePill(text = event.category, color = JummoGoldBright)
                    StatusBadgePill(
                        text = if (event.qrEnabled) "QR ACTIVE" else "QR PAUSED",
                        color = if (event.qrEnabled) JummoSuccessGreen else JummoErrorRed
                    )
                }
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(14.dp)
                ) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${event.eventDate} • ${event.location}",
                        style = MaterialTheme.typography.bodySmall,
                        color = JummoTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${event.totalPhotos} photos • ${event.uploadedPhotos} uploaded • ${event.pendingPhotos} pending",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                    Text(
                        text = GuestAccessModes.displayLabel(event.guestAccessMode),
                        style = MaterialTheme.typography.labelSmall,
                        color = JummoTextSecondary
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpenQr,
                        border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Event QR",
                            tint = JummoGoldBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("QR", color = JummoGoldBright)
                    }

                    IconButton(
                        onClick = onShootCamera,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JummoElevatedPurple)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Open Camera for Event",
                            tint = JummoGoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
