package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.NotificationsNone
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
import androidx.compose.material3.Surface
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
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.PhotographerProfile
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.JummoLuxuryCameraLogo
import com.example.ui.components.MetricStatCard
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.StatusBadgePill
import com.example.ui.theme.JummoBorderGlow
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoElevatedPurple
import com.example.ui.theme.JummoErrorRed
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoSuccessGreen
import com.example.ui.theme.JummoTextMuted
import com.example.ui.theme.JummoTextPrimary
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import com.example.ui.theme.JummoVioletPrimary
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
    onNavigateToEventsTab: () -> Unit,
    onNavigateToUploadsTab: () -> Unit = {},
    onOpenAdminDashboard: () -> Unit = {}
) {
    val context = LocalContext.current

    // Strictly real metrics derived from live state and local storage
    val totalEvents = events.size
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
    val displayName = profile?.displayName?.ifBlank { "Photographer" } ?: "Photographer"
    val studioSubtitle = profile?.studioName?.ifBlank { "Jummo Pro Studio" } ?: "Jummo Pro Studio"

    val avatarInitials = displayName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifBlank { "JP" }

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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Top Luxury Header + Greeting (Screen 2 in Reference)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            JummoLuxuryCameraLogo(
                                size = 40.dp,
                                showCrown = false
                            )
                            Column {
                                Text(
                                    text = "Jummo Photos AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = JummoGoldBright,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Smart Event Photography Suite",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JummoTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Admin Dashboard Quick Access Button
                            IconButton(
                                onClick = onOpenAdminDashboard,
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(JummoCardPurple)
                                    .border(0.85.dp, JummoGoldBright.copy(alpha = 0.45f), CircleShape)
                                    .testTag("home_admin_dashboard_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Open Admin Dashboard",
                                    tint = JummoGoldBright,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            // Notification / Queue Bell Button with Badge
                            Box {
                                IconButton(
                                    onClick = onNavigateToUploadsTab,
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(JummoCardPurple)
                                        .border(0.85.dp, JummoBorderSubtle, CircleShape)
                                        .testTag("home_notification_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsNone,
                                        contentDescription = "Upload Queue Notifications",
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                                if (pendingPhotos > 0L || failedUploads > 0L) {
                                    Box(
                                        modifier = Modifier
                                            .size(15.dp)
                                            .clip(CircleShape)
                                            .background(if (failedUploads > 0L) JummoErrorRed else JummoErrorRed)
                                            .align(Alignment.TopEnd),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = (pendingPhotos + failedUploads).coerceAtMost(9L).toString(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            // Profile Avatar Badge
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(JummoVioletBright, JummoElevatedPurple)
                                        )
                                    )
                                    .border(1.25.dp, JummoGoldBright, CircleShape)
                                    .clickable { onOpenDriveSettings() }
                                    .testTag("home_profile_avatar"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = avatarInitials,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Greeting Block matching reference
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Welcome, $displayName",
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = JummoTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "👋",
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                            Text(
                                text = studioSubtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = JummoTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        StatusBadgePill(
                            text = if (profile?.autoUploadEnabled != false) "AUTO SYNC" else "MANUAL",
                            color = JummoVioletBright
                        )
                    }
                }
            }

            // 2. Hero Wedding Photography Banner + Side-by-Side Action Buttons Below (Screen 2 in Reference)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                        border = BorderStroke(1.dp, JummoGoldBright.copy(alpha = 0.4f))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(138.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_wedding_cover),
                                contentDescription = "Event Photography Powered by AI Banner",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.horizontalGradient(
                                            colors = listOf(
                                                JummoObsidian.copy(alpha = 0.92f),
                                                JummoCardPurple.copy(alpha = 0.72f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                verticalArrangement = Arrangement.SpaceBetween
                            ) {
                                Surface(
                                    color = JummoObsidian.copy(alpha = 0.72f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.55f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = JummoGoldBright,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "PRO STUDIO EDITION",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = JummoGoldBright,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "Event Photography",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Powered by AI",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = JummoGoldBright,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Side-by-Side Primary Action Buttons: Golden "+ Create Event" & Electric Violet "Camera"
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
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("home_create_event_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Create Event",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Button(
                            onClick = onQuickCameraClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JummoVioletPrimary,
                                contentColor = Color.White
                            ),
                            border = BorderStroke(1.dp, JummoBorderGlow),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .testTag("home_quick_camera_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Camera",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // 3. Compact 3 + 2 Statistics Grid (Events, Photos, Uploaded / Pending, Failed)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricStatCard(
                            title = "Events",
                            value = totalEvents.toString(),
                            icon = Icons.Default.Event,
                            accentColor = JummoVioletBright,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Photos",
                            value = totalPhotos.toString(),
                            icon = Icons.Default.PhotoLibrary,
                            accentColor = JummoVioletBright,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Uploaded",
                            value = uploadedPhotos.toString(),
                            icon = Icons.Default.CheckCircle,
                            accentColor = JummoSuccessGreen,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetricStatCard(
                            title = "Pending",
                            value = pendingPhotos.toString(),
                            icon = Icons.Default.Schedule,
                            accentColor = JummoErrorRed,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Failed",
                            value = failedUploads.toString(),
                            icon = Icons.Default.ErrorOutline,
                            accentColor = JummoWarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 4. Compact Google Drive Status Card & Local Storage Strip
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Google Drive Connected Card with Progress Bar (matching Screen 2 in Reference)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenDriveSettings() }
                            .testTag("drive_connection_card"),
                        shape = RoundedCornerShape(14.dp),
                        color = JummoCardPurple.copy(alpha = 0.94f),
                        border = BorderStroke(0.85.dp, JummoBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(9.dp))
                                        .background(
                                            if (driveConnected) {
                                                JummoSuccessGreen.copy(alpha = 0.16f)
                                            } else {
                                                JummoGoldBright.copy(alpha = 0.16f)
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (driveConnected) Icons.Default.CloudDone else Icons.Default.CloudSync,
                                        contentDescription = "Google Drive Status",
                                        tint = if (driveConnected) JummoSuccessGreen else JummoGoldBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Google Drive",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = if (driveConnected) "Connected" else "Tap to Connect",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (driveConnected) JummoSuccessGreen else JummoTextMuted,
                                        fontWeight = FontWeight.SemiBold,
                                        maxLines = 1
                                    )
                                }
                            }

                            Column(
                                horizontalAlignment = Alignment.End,
                                verticalArrangement = Arrangement.spacedBy(5.dp),
                                modifier = Modifier.width(136.dp)
                            ) {
                                Text(
                                    text = "${storageMetrics.formatBytes(storageMetrics.combinedManagedBytes)} / ${profile?.driveFolderName ?: "Cloud"}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JummoTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                val syncRatio = if (totalPhotos > 0L) {
                                    (uploadedPhotos.toFloat() / totalPhotos.toFloat()).coerceIn(0.08f, 1f)
                                } else if (driveConnected) {
                                    0.38f
                                } else {
                                    0.06f
                                }
                                LinearProgressIndicator(
                                    progress = { syncRatio },
                                    color = if (driveConnected) JummoSuccessGreen else JummoGoldBright,
                                    trackColor = JummoDeepPurple,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(50))
                                )
                            }
                        }
                    }

                    // Compact Local Storage Strip
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("storage_usage_card"),
                        shape = RoundedCornerShape(14.dp),
                        color = JummoCardPurple.copy(alpha = 0.92f),
                        border = BorderStroke(0.75.dp, JummoBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 9.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = "Local Storage",
                                    tint = JummoGoldBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Local Originals (${storageMetrics.localPhotoFileCount} files kept safe)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = JummoTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Text(
                                text = storageMetrics.formatBytes(storageMetrics.combinedManagedBytes),
                                style = MaterialTheme.typography.labelMedium,
                                color = JummoGoldBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 5. Recent Events Section (matching Screen 2 bottom section)
            item {
                SectionHeaderRow(
                    title = "Recent Events",
                    trailingContent = {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onNavigateToEventsTab() }
                                .padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "View All",
                                style = MaterialTheme.typography.labelMedium,
                                color = JummoGoldBright,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                )
            }

            if (events.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.Event,
                        title = "No Events Created Yet",
                        message = "Create your first event to generate a branded QR code and start shooting.",
                        actionButton = {
                            Button(
                                onClick = onCreateNewEventClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoGoldBright,
                                    contentColor = JummoObsidian
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(0.85.dp, JummoBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Square Thumbnail Photograph
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JummoDeepPurple)
                    .border(0.75.dp, JummoGoldBright.copy(alpha = 0.35f), RoundedCornerShape(12.dp))
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
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Event Metadata & Quick Actions
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadgePill(
                        text = if (event.status == "Active") "Active" else event.status,
                        color = if (event.status == "Active") JummoSuccessGreen else JummoTextMuted
                    )
                }

                Text(
                    text = "${event.eventDate} • ${event.category}",
                    style = MaterialTheme.typography.bodySmall,
                    color = JummoTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${event.totalPhotos} Photos",
                        style = MaterialTheme.typography.labelMedium,
                        color = JummoVioletBright,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onOpenQr() },
                            color = JummoElevatedPurple,
                            border = BorderStroke(0.75.dp, JummoBorderSubtle),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.QrCode2,
                                    contentDescription = "Event QR",
                                    tint = JummoVioletBright,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "QR",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onShootCamera() },
                            color = JummoGoldBright.copy(alpha = 0.16f),
                            border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Shoot Camera",
                                    tint = JummoGoldBright,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
