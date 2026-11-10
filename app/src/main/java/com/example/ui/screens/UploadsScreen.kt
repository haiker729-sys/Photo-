package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.PhotographerProfile
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.JummoLuxuryCameraLogo
import com.example.ui.components.statusColorForUpload
import com.example.ui.theme.JummoBorderGlow
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoElevatedPurple
import com.example.ui.theme.JummoChampagneGold
import com.example.ui.theme.JummoErrorRed
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoSuccessGreen
import com.example.ui.theme.JummoTextMuted
import com.example.ui.theme.JummoTextPrimary
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import com.example.ui.theme.JummoVioletPrimary
import java.util.Locale

@Composable
fun UploadsScreen(
    profile: PhotographerProfile?,
    events: List<PhotoEvent>,
    selectedEventId: String?,
    uploads: List<PhotoUploadRecord>,
    onSelectEvent: (String) -> Unit,
    onCreateEventClick: () -> Unit,
    onImportPhotos: (
        event: PhotoEvent,
        uris: List<Uri>,
        sourceType: String,
        uploadMode: String,
        uploadImmediately: Boolean
    ) -> Unit,
    onUploadAllPendingNow: () -> Unit,
    onRetrySingleUpload: (PhotoUploadRecord) -> Unit,
    onUpdateAutoUploadSettings: (
        autoUpload: Boolean,
        wifiOnly: Boolean,
        batteryNotLow: Boolean
    ) -> Unit,
    onPauseSingleUpload: (PhotoUploadRecord) -> Unit = {},
    onResumeSingleUpload: (PhotoUploadRecord) -> Unit = {},
    onRemoveUploadFromQueue: (PhotoUploadRecord) -> Unit = {},
    onReassignUploadToEvent: (PhotoUploadRecord, PhotoEvent) -> Unit = { _, _ -> },
    onPauseAllUploads: () -> Unit = {},
    onResumeAllUploads: () -> Unit = {},
    onCancelAllUploads: () -> Unit = {},
    onOpenDriveSettings: () -> Unit = {},
    onOpenProfileTab: () -> Unit = {}
) {
    val activeEvents = remember(events) {
        events.filter { it.status == "Active" }.ifEmpty { events }
    }
    val currentEvent = remember(activeEvents, selectedEventId) {
        activeEvents.find { it.id == selectedEventId } ?: activeEvents.firstOrNull()
    }

    var statusFilter by rememberSaveable { mutableStateOf("Queue") }
    var eventDropdownExpanded by remember { mutableStateOf(false) }
    var showNotificationsSummary by remember { mutableStateOf(false) }

    val autoEnabled = profile?.autoUploadEnabled ?: true
    val wifiOnly = profile?.wifiOnlyUpload ?: true
    val batteryNotLow = profile?.batteryNotLowUpload ?: true

    // 1. Select Photos — Android Photo Picker (Multiple Selection, Original Quality)
    val manualGalleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(50)
    ) { uris ->
        val targetEvent = currentEvent
        if (uris.isNotEmpty() && targetEvent != null) {
            val mode = if (autoEnabled) "Automatic" else "Manual"
            onImportPhotos(targetEvent, uris, "ManualGallery", mode, autoEnabled)
        }
    }

    // 2. DSLR / SD Import — Storage Access Framework (RAW, DNG, CR2, NEF, ARW, JPEG, PNG)
    val dslrSafImporter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val targetEvent = currentEvent
        if (uris.isNotEmpty() && targetEvent != null) {
            val mode = if (autoEnabled) "Automatic" else "Manual"
            onImportPhotos(targetEvent, uris, "DSLRImport", mode, autoEnabled)
        }
    }

    // 3. Google Drive Import — Cloud Document Picker / Drive Provider
    val googleDriveImporter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        val targetEvent = currentEvent
        if (uris.isNotEmpty() && targetEvent != null) {
            val mode = if (autoEnabled) "Automatic" else "Manual"
            onImportPhotos(targetEvent, uris, "GoogleDriveImport", mode, autoEnabled)
        }
    }

    val queueRecords = remember(uploads) {
        uploads.filter {
            it.status == "Pending" || it.status == "Uploading" || it.status == "Paused"
        }
    }
    val uploadedRecords = remember(uploads) {
        uploads.filter { it.status == "Uploaded" }
    }
    val failedRecords = remember(uploads) {
        uploads.filter { it.status == "Failed" }
    }

    val queueCount = queueRecords.size
    val uploadedCount = uploadedRecords.size
    val failedCount = failedRecords.size

    val filteredUploads = remember(uploads, statusFilter, queueRecords, uploadedRecords, failedRecords) {
        when (statusFilter) {
            "Uploaded" -> uploadedRecords
            "Failed" -> failedRecords
            "Queue" -> queueRecords
            else -> queueRecords
        }
    }

    val studioName = profile?.studioName?.ifBlank { "Jummo Pro Studio" } ?: "Jummo Pro Studio"
    val photographerName = profile?.displayName?.ifBlank { "Meraj Sir" } ?: "Meraj Sir"
    val avatarInitial = photographerName.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "J"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        JummoObsidian,
                        JummoDeepPurple,
                        JummoObsidian
                    )
                )
            ),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .testTag("uploads_screen_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. TOP STUDIO HEADER (Logo, Subtitle, Notification Icon, Profile Avatar)
            item {
                UploadsStudioHeader(
                    avatarInitial = avatarInitial,
                    queueCount = queueCount,
                    failedCount = failedCount,
                    onNotificationClick = { showNotificationsSummary = !showNotificationsSummary },
                    onProfileClick = onOpenProfileTab
                )
            }

            // Optional collapsible notification summary bar when notification bell is tapped
            if (showNotificationsSummary) {
                item {
                    Card(
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = JummoElevatedPurple),
                        border = BorderStroke(0.85.dp, JummoGoldBright.copy(alpha = 0.5f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("uploads_notification_summary_card")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Upload Engine Activity • $studioName",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = JummoGoldBright,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$queueCount in queue • $uploadedCount uploaded • $failedCount failed",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JummoTextPrimary
                                )
                            }
                            IconButton(
                                onClick = { showNotificationsSummary = false },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close summary",
                                    tint = JummoTextSecondary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 2. SCREEN TITLE & SUBTITLE ("Uploads" / "Upload, monitor and manage photos")
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Uploads",
                        style = MaterialTheme.typography.headlineSmall,
                        color = JummoTextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Upload, monitor and manage photos",
                        style = MaterialTheme.typography.bodySmall,
                        color = JummoTextSecondary
                    )
                }
            }

            // 3. UPLOAD STATUS TABS: Queue (X) | Uploaded (Y) | Failed (Z)
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("uploads_status_tabs_bar"),
                    color = JummoCardPurple.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.9.dp, JummoBorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tabs = listOf(
                            Triple("Queue", "Queue ($queueCount)", JummoVioletPrimary),
                            Triple("Uploaded", "Uploaded ($uploadedCount)", JummoSuccessGreen),
                            Triple("Failed", "Failed ($failedCount)", JummoErrorRed)
                        )
                        tabs.forEach { (key, label, accentColor) ->
                            val selected = statusFilter == key
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .clickable { statusFilter = key }
                                    .testTag("uploads_tab_$key"),
                                color = if (selected) {
                                    if (key == "Queue") JummoVioletPrimary else accentColor.copy(alpha = 0.25f)
                                } else {
                                    Color.Transparent
                                },
                                border = if (selected) {
                                    BorderStroke(
                                        0.9.dp,
                                        if (key == "Queue") JummoGoldBright.copy(alpha = 0.65f) else accentColor
                                    )
                                } else {
                                    null
                                },
                                shape = RoundedCornerShape(50)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) {
                                            if (key == "Queue") Color.White else JummoTextPrimary
                                        } else {
                                            JummoTextSecondary
                                        },
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. AUTOMATIC UPLOAD CARD (WorkManager Toggle, Wi-Fi Only, Battery Above 20%)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = JummoCardPurple.copy(alpha = 0.92f)
                    ),
                    border = BorderStroke(
                        0.95.dp,
                        if (autoEnabled) JummoGoldBright.copy(alpha = 0.45f) else JummoBorderSubtle
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("automatic_upload_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "Automatic Upload",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = JummoTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Surface(
                                        color = if (autoEnabled) {
                                            JummoSuccessGreen.copy(alpha = 0.16f)
                                        } else {
                                            JummoElevatedPurple
                                        },
                                        border = BorderStroke(
                                            0.75.dp,
                                            if (autoEnabled) JummoSuccessGreen else JummoBorderSubtle
                                        ),
                                        shape = RoundedCornerShape(50)
                                    ) {
                                        Text(
                                            text = if (autoEnabled) "ACTIVE" else "MANUAL",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (autoEnabled) JummoSuccessGreen else JummoTextSecondary,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                            fontSize = 9.sp
                                        )
                                    }
                                }
                                Text(
                                    text = "Background upload via WorkManager",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JummoTextSecondary
                                )
                            }
                            Switch(
                                checked = autoEnabled,
                                onCheckedChange = {
                                    onUpdateAutoUploadSettings(it, wifiOnly, batteryNotLow)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = JummoGoldBright,
                                    uncheckedThumbColor = JummoTextSecondary,
                                    uncheckedTrackColor = JummoBorderSubtle
                                ),
                                modifier = Modifier.testTag("auto_upload_switch")
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ConstraintTogglePill(
                                label = "Wi-Fi Only",
                                icon = Icons.Default.Wifi,
                                checked = wifiOnly,
                                onClick = {
                                    onUpdateAutoUploadSettings(autoEnabled, !wifiOnly, batteryNotLow)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("wifi_only_toggle_pill")
                            )
                            ConstraintTogglePill(
                                label = "Battery Above 20%",
                                icon = Icons.Default.BatteryChargingFull,
                                checked = batteryNotLow,
                                onClick = {
                                    onUpdateAutoUploadSettings(autoEnabled, wifiOnly, !batteryNotLow)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("battery_above_20_toggle_pill")
                            )
                        }
                    }
                }
            }

            // 5. THREE COMPACT IMPORT CARDS: Select Photos | DSLR / SD Import | Google Drive Import
            item {
                val driveConnected = profile?.driveConnected == true
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Card 1: Select Photos (Android Photo Picker)
                        CompactImportSourceCard(
                            title = "Select Photos",
                            subtitle = "Original quality",
                            icon = Icons.Default.Collections,
                            accentColor = JummoGoldBright,
                            borderColor = JummoGoldBright.copy(alpha = 0.55f),
                            enabled = currentEvent != null,
                            onClick = {
                                manualGalleryPicker.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("select_gallery_photos_button")
                        )

                        // Card 2: DSLR / SD Import (Storage Access Framework RAW/JPG)
                        CompactImportSourceCard(
                            title = "DSLR / SD Import",
                            subtitle = "RAW • JPG • SD",
                            icon = Icons.Default.SdCard,
                            accentColor = JummoVioletBright,
                            borderColor = JummoBorderGlow,
                            enabled = currentEvent != null,
                            onClick = {
                                dslrSafImporter.launch(
                                    arrayOf(
                                        "image/jpeg",
                                        "image/png",
                                        "image/x-adobe-dng",
                                        "image/x-canon-cr2",
                                        "image/x-nikon-nef",
                                        "image/x-sony-arw",
                                        "image/*"
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("dslr_saf_import_button")
                        )

                        // Card 3: Google Drive Import
                        CompactImportSourceCard(
                            title = "Google Drive Import",
                            subtitle = if (driveConnected) {
                                profile?.driveFolderName?.ifBlank { "Cloud Connected" } ?: "Cloud Connected"
                            } else {
                                "Cloud Picker"
                            },
                            icon = Icons.Default.CloudQueue,
                            accentColor = if (driveConnected) JummoSuccessGreen else JummoChampagneGold,
                            borderColor = if (driveConnected) {
                                JummoSuccessGreen.copy(alpha = 0.55f)
                            } else {
                                JummoBorderSubtle
                            },
                            enabled = currentEvent != null,
                            onClick = {
                                googleDriveImporter.launch(
                                    arrayOf("image/jpeg", "image/png", "image/*")
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("google_drive_import_button")
                        )
                    }
                }
            }

            // 6. EVENT SELECTION DROPDOWN & GOLDEN GRADIENT "UPLOAD NOW" BUTTON
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = JummoCardPurple.copy(alpha = 0.94f)
                    ),
                    border = BorderStroke(0.9.dp, JummoBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("uploads_event_selection_card")
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Destination Event",
                                style = MaterialTheme.typography.labelMedium,
                                color = JummoChampagneGold,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (currentEvent != null) {
                                    "${currentEvent.category} • ${currentEvent.uploadedPhotos}/${currentEvent.totalPhotos} synced"
                                } else {
                                    "Required before uploading"
                                },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (currentEvent != null) JummoTextSecondary else JummoErrorRed
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Event Selection Dropdown Box
                            Box(modifier = Modifier.weight(1f)) {
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable {
                                            if (activeEvents.isEmpty()) {
                                                onCreateEventClick()
                                            } else {
                                                eventDropdownExpanded = true
                                            }
                                        }
                                        .testTag("uploads_event_dropdown_button"),
                                    color = JummoElevatedPurple,
                                    border = BorderStroke(
                                        0.9.dp,
                                        if (currentEvent != null) {
                                            JummoGoldBright.copy(alpha = 0.45f)
                                        } else {
                                            JummoErrorRed.copy(alpha = 0.65f)
                                        }
                                    ),
                                    shape = RoundedCornerShape(12.dp)
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
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Event,
                                                contentDescription = null,
                                                tint = JummoGoldBright,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Text(
                                                text = currentEvent?.name ?: "Select or Create Event",
                                                style = MaterialTheme.typography.labelLarge,
                                                color = JummoTextPrimary,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowDropDown,
                                            contentDescription = "Select Event",
                                            tint = JummoGoldBright,
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }
                                }

                                DropdownMenu(
                                    expanded = eventDropdownExpanded,
                                    onDismissRequest = { eventDropdownExpanded = false },
                                    modifier = Modifier
                                        .background(JummoElevatedPurple)
                                        .border(0.85.dp, JummoBorderGlow, RoundedCornerShape(8.dp))
                                ) {
                                    activeEvents.forEach { evt ->
                                        val isSelected = currentEvent?.id == evt.id
                                        DropdownMenuItem(
                                            text = {
                                                Column {
                                                    Text(
                                                        text = evt.name,
                                                        style = MaterialTheme.typography.labelLarge,
                                                        color = if (isSelected) JummoGoldBright else JummoTextPrimary,
                                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                    )
                                                    Text(
                                                        text = "${evt.category} • ${evt.eventDate}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = JummoTextSecondary
                                                    )
                                                }
                                            },
                                            onClick = {
                                                onSelectEvent(evt.id)
                                                eventDropdownExpanded = false
                                            },
                                            modifier = Modifier.testTag("dropdown_event_${evt.id}")
                                        )
                                    }
                                    HorizontalDivider(color = JummoBorderSubtle)
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Add,
                                                    contentDescription = null,
                                                    tint = JummoGoldBright,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Text(
                                                    text = "Create New Event",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    color = JummoGoldBright,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        },
                                        onClick = {
                                            eventDropdownExpanded = false
                                            onCreateEventClick()
                                        }
                                    )
                                }
                            }

                            // Primary "Upload Now" Button
                            val canUploadNow = currentEvent != null
                            Button(
                                onClick = onUploadAllPendingNow,
                                enabled = canUploadNow,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoGoldBright,
                                    contentColor = Color.White,
                                    disabledContainerColor = JummoBorderSubtle,
                                    disabledContentColor = JummoTextMuted
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                                modifier = Modifier
                                    .height(42.dp)
                                    .testTag("upload_now_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Upload Now",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        // Quick Event Filter Chips when multiple events exist
                        if (activeEvents.size > 1) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                activeEvents.forEach { evt ->
                                    FilterChip(
                                        selected = currentEvent?.id == evt.id,
                                        onClick = { onSelectEvent(evt.id) },
                                        label = {
                                            Text(
                                                text = evt.name,
                                                style = MaterialTheme.typography.labelSmall,
                                                maxLines = 1
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = JummoGoldBright,
                                            selectedLabelColor = Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 7. UPLOAD MANAGEMENT BAR (Section Header + Pause All / Resume All / Cancel All)
            item {
                val activeUploadingOrPendingCount = queueRecords.count {
                    it.status == "Pending" || it.status == "Uploading"
                }
                val pausedCount = queueRecords.count { it.status == "Paused" }

                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = when (statusFilter) {
                                "Uploaded" -> "Uploaded Photos ($uploadedCount)"
                                "Failed" -> "Failed Uploads ($failedCount)"
                                else -> "Upload Queue ($queueCount)"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = JummoTextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        if (queueCount > 0 || failedCount > 0) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                // Pause All
                                BatchControlChip(
                                    label = "Pause All",
                                    icon = Icons.Default.Pause,
                                    enabled = activeUploadingOrPendingCount > 0,
                                    accentColor = JummoChampagneGold,
                                    onClick = onPauseAllUploads,
                                    modifier = Modifier.testTag("uploads_pause_all_button")
                                )
                                // Resume All
                                BatchControlChip(
                                    label = "Resume All",
                                    icon = Icons.Default.PlayArrow,
                                    enabled = pausedCount > 0 || failedCount > 0 || activeUploadingOrPendingCount > 0,
                                    accentColor = JummoVioletBright,
                                    onClick = onResumeAllUploads,
                                    modifier = Modifier.testTag("uploads_resume_all_button")
                                )
                                // Cancel All
                                BatchControlChip(
                                    label = "Cancel All",
                                    icon = Icons.Default.Close,
                                    enabled = queueCount > 0 || failedCount > 0,
                                    accentColor = JummoErrorRed,
                                    onClick = onCancelAllUploads,
                                    modifier = Modifier.testTag("uploads_cancel_all_button")
                                )
                            }
                        }
                    }
                }
            }

            // 8. UPLOAD RECORDS LIST OR EMPTY STATE
            if (filteredUploads.isEmpty()) {
                item {
                    val emptyTitle = when (statusFilter) {
                        "Uploaded" -> "No Uploaded Photos Yet"
                        "Failed" -> "Zero Failed Uploads"
                        else -> if (uploadedCount > 0) {
                            "All $uploadedCount Photo(s) Uploaded"
                        } else {
                            "Upload Queue is Empty"
                        }
                    }
                    val emptyMessage = when (statusFilter) {
                        "Uploaded" -> "Select photos from Gallery, DSLR / SD card, or Google Drive and tap Upload Now."
                        "Failed" -> "All studio uploads are healthy. Any network or file errors will appear here for instant retry."
                        else -> if (uploadedCount > 0) {
                            "Your active queue is clear. Switch to the Uploaded ($uploadedCount) tab to inspect completed photos, or import more above."
                        } else {
                            "Import photos using Select Photos, DSLR / SD Import, or Google Drive Import above."
                        }
                    }
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        EmptyStateCard(
                            icon = if (statusFilter == "Uploaded" || (statusFilter == "Queue" && uploadedCount > 0)) {
                                Icons.Default.CloudDone
                            } else {
                                Icons.Default.CloudUpload
                            },
                            title = emptyTitle,
                            message = emptyMessage
                        )
                        if (statusFilter == "Queue" && uploadedCount > 0) {
                            OutlinedButton(
                                onClick = { statusFilter = "Uploaded" },
                                border = BorderStroke(1.dp, JummoGoldBright.copy(alpha = 0.6f)),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier.testTag("view_uploaded_photos_shortcut")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = JummoSuccessGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "View Uploaded Photos ($uploadedCount)",
                                    color = JummoGoldBright,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            } else {
                items(filteredUploads, key = { it.id }) { upload ->
                    UploadQueueItemCard(
                        upload = upload,
                        currentSelectedEvent = currentEvent,
                        onRetryOrUploadNow = { onRetrySingleUpload(upload) },
                        onPause = { onPauseSingleUpload(upload) },
                        onResume = { onResumeSingleUpload(upload) },
                        onRemoveFromQueue = { onRemoveUploadFromQueue(upload) },
                        onReassignEvent = { targetEvt ->
                            onReassignUploadToEvent(upload, targetEvt)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun UploadsStudioHeader(
    avatarInitial: String,
    queueCount: Int,
    failedCount: Int,
    onNotificationClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("uploads_studio_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            JummoLuxuryCameraLogo(size = 36.dp)
            Column {
                Text(
                    text = "Jummo Photos AI",
                    style = MaterialTheme.typography.titleMedium,
                    color = JummoTextPrimary,
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
            // Notification Bell Icon with Active Queue / Error Badge
            Box {
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .clickable { onNotificationClick() }
                        .testTag("uploads_notification_button"),
                    color = JummoCardPurple,
                    border = BorderStroke(0.85.dp, JummoBorderSubtle),
                    shape = CircleShape
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.NotificationsNone,
                            contentDescription = "Upload Notifications",
                            tint = JummoChampagneGold,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                if (queueCount > 0 || failedCount > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .size(15.dp)
                            .clip(CircleShape)
                            .background(if (failedCount > 0) JummoErrorRed else JummoVioletPrimary)
                            .border(1.dp, JummoObsidian, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = (if (failedCount > 0) failedCount else queueCount).coerceAtMost(99).toString(),
                            color = Color.White,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // User Profile Avatar
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable { onProfileClick() }
                    .testTag("uploads_profile_avatar"),
                color = JummoElevatedPurple,
                border = BorderStroke(1.2.dp, JummoGoldBright),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = avatarInitial,
                        style = MaterialTheme.typography.labelLarge,
                        color = JummoGoldBright,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }
    }
}

@Composable
private fun ConstraintTogglePill(
    label: String,
    icon: ImageVector,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable { onClick() },
        color = if (checked) JummoElevatedPurple else JummoDeepPurple.copy(alpha = 0.7f),
        border = BorderStroke(
            0.9.dp,
            if (checked) JummoSuccessGreen.copy(alpha = 0.65f) else JummoBorderSubtle
        ),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = if (checked) Icons.Default.CheckCircle else icon,
                contentDescription = null,
                tint = if (checked) JummoSuccessGreen else JummoTextSecondary,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (checked) JummoTextPrimary else JummoTextSecondary,
                fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun CompactImportSourceCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    borderColor: Color,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(enabled = enabled) { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = JummoCardPurple.copy(alpha = if (enabled) 0.94f else 0.55f)
        ),
        border = BorderStroke(0.95.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(accentColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = JummoTextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = JummoTextSecondary,
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun BatchControlChip(
    label: String,
    icon: ImageVector,
    enabled: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(enabled = enabled) { onClick() },
        color = if (enabled) JummoElevatedPurple else JummoCardPurple.copy(alpha = 0.45f),
        border = BorderStroke(
            0.75.dp,
            if (enabled) accentColor.copy(alpha = 0.55f) else JummoBorderSubtle.copy(alpha = 0.4f)
        ),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) accentColor else JummoTextMuted,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (enabled) JummoTextPrimary else JummoTextMuted,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp
            )
        }
    }
}

@Composable
private fun UploadQueueItemCard(
    upload: PhotoUploadRecord,
    currentSelectedEvent: PhotoEvent?,
    onRetryOrUploadNow: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onRemoveFromQueue: () -> Unit,
    onReassignEvent: (PhotoEvent) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val statusColor = when (upload.status) {
        "Uploaded" -> JummoSuccessGreen
        "Failed" -> JummoErrorRed
        "Paused" -> JummoGoldBright
        else -> JummoVioletBright
    }

    val targetProgress = when (upload.status) {
        "Uploaded" -> 1f
        else -> (upload.progress / 100f).coerceIn(0.06f, 1f)
    }
    val animatedItemProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = 250),
        label = "upload_item_progress"
    )

    val formattedSize = remember(upload.fileSizeBytes) {
        val kb = (upload.fileSizeBytes / 1024.0).coerceAtLeast(1.0)
        if (kb >= 1024.0) {
            String.format(Locale.US, "%.1f MB", kb / 1024.0)
        } else {
            String.format(Locale.US, "%.0f KB", kb)
        }
    }

    val sourceBadge = remember(upload.sourceType) {
        when (upload.sourceType) {
            "DSLRImport" -> "DSLR / SD"
            "GoogleDriveImport" -> "Google Drive"
            "CameraCapture" -> "Studio Camera"
            else -> "Gallery"
        }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple.copy(alpha = 0.94f)),
        border = BorderStroke(
            0.85.dp,
            when (upload.status) {
                "Failed" -> JummoErrorRed.copy(alpha = 0.55f)
                "Uploaded" -> JummoSuccessGreen.copy(alpha = 0.35f)
                "Paused" -> JummoGoldBright.copy(alpha = 0.45f)
                else -> JummoBorderSubtle
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("upload_item_card_${upload.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Left Square Photo Thumbnail
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(JummoDeepPurple)
                    .border(0.75.dp, JummoBorderSubtle, RoundedCornerShape(12.dp))
            ) {
                AsyncImage(
                    model = upload.localUri,
                    contentDescription = upload.fileName,
                    placeholder = painterResource(id = R.drawable.img_wedding_cover),
                    error = painterResource(id = R.drawable.img_wedding_cover),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // 2. Center Filename, Metadata, Destination Event, and Progress Bar
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = upload.fileName,
                        style = MaterialTheme.typography.titleSmall,
                        color = JummoTextPrimary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        color = statusColor.copy(alpha = 0.16f),
                        border = BorderStroke(0.65.dp, statusColor.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = upload.status,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }

                Text(
                    text = "$formattedSize • $sourceBadge • ${upload.eventName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = JummoTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LinearProgressIndicator(
                        progress = { animatedItemProgress },
                        color = statusColor,
                        trackColor = JummoBorderSubtle,
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                    )
                    Text(
                        text = "${if (upload.status == "Uploaded") 100L else upload.progress}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = JummoTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // 3. Primary Quick Action Button (Pause / Resume / Retry / Done)
            Surface(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .clickable {
                        when (upload.status) {
                            "Uploaded" -> {}
                            "Failed" -> onRetryOrUploadNow()
                            "Paused" -> onResume()
                            else -> onPause()
                        }
                    }
                    .testTag("upload_item_primary_action_${upload.id}"),
                color = JummoElevatedPurple,
                border = BorderStroke(0.75.dp, statusColor.copy(alpha = 0.55f)),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (upload.status) {
                            "Uploaded" -> Icons.Default.CheckCircle
                            "Failed" -> Icons.Default.Refresh
                            "Paused" -> Icons.Default.PlayArrow
                            else -> Icons.Default.Pause
                        },
                        contentDescription = upload.status,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // 4. Three-Dot Action Menu
            Box {
                IconButton(
                    onClick = { menuExpanded = true },
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("upload_item_menu_${upload.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Upload options for ${upload.fileName}",
                        tint = JummoTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                    modifier = Modifier
                        .background(JummoElevatedPurple)
                        .border(0.85.dp, JummoBorderSubtle, RoundedCornerShape(8.dp))
                ) {
                    if (upload.status != "Uploaded") {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = if (upload.status == "Failed") "Retry Upload Now" else "Upload Immediately",
                                    color = JummoGoldBright,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    tint = JummoGoldBright,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onRetryOrUploadNow()
                            }
                        )

                        if (upload.status == "Paused") {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Resume in Queue",
                                        color = JummoTextPrimary,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = JummoVioletBright,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onResume()
                                }
                            )
                        } else if (upload.status == "Pending" || upload.status == "Uploading") {
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "Pause Upload",
                                        color = JummoTextPrimary,
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Pause,
                                        contentDescription = null,
                                        tint = JummoChampagneGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                onClick = {
                                    menuExpanded = false
                                    onPause()
                                }
                            )
                        }
                    }

                    if (currentSelectedEvent != null && currentSelectedEvent.id != upload.eventId) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Move to ${currentSelectedEvent.name}",
                                    color = JummoTextPrimary,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.SwapHoriz,
                                    contentDescription = null,
                                    tint = JummoChampagneGold,
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            onClick = {
                                menuExpanded = false
                                onReassignEvent(currentSelectedEvent)
                            }
                        )
                    }

                    DropdownMenuItem(
                        text = {
                            Text(
                                text = if (upload.status == "Uploaded") "Remove Record (Keep Local)" else "Cancel Upload",
                                color = JummoErrorRed,
                                style = MaterialTheme.typography.labelMedium
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = null,
                                tint = JummoErrorRed,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        onClick = {
                            menuExpanded = false
                            onRemoveFromQueue()
                        }
                    )
                }
            }
        }
    }
}
