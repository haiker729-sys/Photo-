package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.statusColorForUpload
import com.example.ui.theme.JummoBorderGlow
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoElevatedPurple
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoSuccessGreen
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
    ) -> Unit
) {
    val activeEvents = events.filter { it.status == "Active" }.ifEmpty { events }
    val currentEvent = activeEvents.find { it.id == selectedEventId } ?: activeEvents.firstOrNull()

    var statusFilter by remember { mutableStateOf("Queue") }

    // Manual Gallery Multiple Photo Picker
    val manualGalleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(20)
    ) { uris ->
        if (uris.isNotEmpty() && currentEvent != null) {
            onImportPhotos(currentEvent, uris, "ManualGallery", "Manual", false)
        }
    }

    // Storage Access Framework (SAF) Multiple Document Importer for SD Card / Camera Storage (JPG, JPEG, PNG, RAW)
    val dslrSafImporter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty() && currentEvent != null) {
            val autoMode = if (profile?.autoUploadEnabled != false) "Automatic" else "Manual"
            onImportPhotos(
                currentEvent,
                uris,
                "DSLRImport",
                autoMode,
                profile?.autoUploadEnabled != false
            )
        }
    }

    val uploadedCount = uploads.count { it.status == "Uploaded" }
    val pendingCount = uploads.count { it.status == "Pending" || it.status == "Uploading" }
    val failedCount = uploads.count { it.status == "Failed" }

    val filteredUploads = uploads.filter { record ->
        when (statusFilter) {
            "Uploaded" -> record.status == "Uploaded"
            "Failed" -> record.status == "Failed"
            "Queue" -> true
            else -> true
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
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
            // 1. Header: "Uploads" + "Upload, monitor and manage photos" (Screen 5 in Reference)
            item {
                SectionHeaderRow(
                    title = "Uploads",
                    subtitle = "Upload, monitor and manage photos"
                )
            }

            // 2. Segmented Filter Pills Bar: Queue (X) | Uploaded | Failed (Screen 5 in Reference)
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = JummoCardPurple,
                    shape = RoundedCornerShape(50),
                    border = BorderStroke(0.85.dp, JummoBorderSubtle)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val tabs = listOf(
                            "Queue" to "Queue (${uploads.size})",
                            "Uploaded" to "Uploaded ($uploadedCount)",
                            "Failed" to "Failed ($failedCount)"
                        )
                        tabs.forEach { (key, label) ->
                            val selected = statusFilter == key
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(50))
                                    .clickable { statusFilter = key },
                                color = if (selected) JummoVioletPrimary else Color.Transparent,
                                shape = RoundedCornerShape(50)
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) Color.White else JummoTextSecondary,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Automatic Upload Card with Golden Switch & Green Check Pills (Screen 5 in Reference)
            item {
                val autoEnabled = profile?.autoUploadEnabled ?: true
                val wifiOnly = profile?.wifiOnlyUpload ?: true
                val batteryNotLow = profile?.batteryNotLowUpload ?: true

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(0.9.dp, JummoBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Automatic Upload",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
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
                                    checkedThumbColor = JummoObsidian,
                                    checkedTrackColor = JummoGoldBright
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ConstraintTogglePill(
                                label = "Wi-Fi Only",
                                checked = wifiOnly,
                                onClick = {
                                    onUpdateAutoUploadSettings(autoEnabled, !wifiOnly, batteryNotLow)
                                },
                                modifier = Modifier.weight(1f)
                            )
                            ConstraintTogglePill(
                                label = "Battery > 20%",
                                checked = batteryNotLow,
                                onClick = {
                                    onUpdateAutoUploadSettings(autoEnabled, wifiOnly, !batteryNotLow)
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // 4. Two Large Side-by-Side Action Cards: "Select Photos" & "DSLR / SD Import" + Target Event Selector (Screen 5 in Reference)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Left Action Card: Select Photos
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = currentEvent != null) {
                                    manualGalleryPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                                .testTag("select_gallery_photos_button"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                            border = BorderStroke(1.dp, JummoGoldBright.copy(alpha = 0.55f))
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(JummoGoldBright.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Collections,
                                        contentDescription = "Select Photos",
                                        tint = JummoGoldBright,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    text = "Select Photos",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }

                        // Right Action Card: DSLR / SD Import
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable(enabled = currentEvent != null) {
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
                                }
                                .testTag("dslr_saf_import_button"),
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                            border = BorderStroke(1.dp, JummoBorderGlow)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 16.dp, horizontal = 12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(42.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(JummoVioletBright.copy(alpha = 0.18f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SdCard,
                                        contentDescription = "DSLR / SD Import",
                                        tint = JummoVioletBright,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Text(
                                    text = "DSLR / SD Import",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    // Target Event Selector + Upload Now Action Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (activeEvents.isEmpty()) {
                            Button(
                                onClick = onCreateEventClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoGoldBright,
                                    contentColor = JummoObsidian
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Create Event First", fontWeight = FontWeight.Bold)
                            }
                        } else {
                            Row(
                                modifier = Modifier
                                    .weight(1f)
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
                                                style = MaterialTheme.typography.labelMedium,
                                                maxLines = 1
                                            )
                                        },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = JummoGoldBright,
                                            selectedLabelColor = JummoObsidian
                                        )
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = onUploadAllPendingNow,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = JummoGoldBright,
                                contentColor = JummoObsidian
                            ),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("upload_now_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudUpload,
                                contentDescription = null,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Upload Now",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 5. Uploading / Queue List Header
            item {
                Text(
                    text = if (pendingCount > 0) {
                        "Uploading ($pendingCount)"
                    } else {
                        "Uploading (${filteredUploads.size})"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            if (filteredUploads.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.CloudUpload,
                        title = "No Uploads in This View",
                        message = "Capture photos with Camera or tap Select Photos / DSLR Import above."
                    )
                }
            } else {
                items(filteredUploads, key = { it.id }) { upload ->
                    UploadQueueItemCard(
                        upload = upload,
                        onRetry = { onRetrySingleUpload(upload) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ConstraintTogglePill(
    label: String,
    checked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable { onClick() },
        color = JummoElevatedPurple,
        border = BorderStroke(
            0.85.dp,
            if (checked) JummoSuccessGreen.copy(alpha = 0.55f) else JummoBorderSubtle
        ),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = if (checked) JummoSuccessGreen else JummoTextSecondary,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun UploadQueueItemCard(
    upload: PhotoUploadRecord,
    onRetry: () -> Unit
) {
    val badgeColor = statusColorForUpload(upload.status)
    val animatedItemProgress by animateFloatAsState(
        targetValue = (upload.progress / 100f).coerceIn(0.08f, 1f),
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

    // Upload Queue Item Card matching Screen 5 in Reference
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(0.85.dp, JummoBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Square Photo Thumbnail
            Box(
                modifier = Modifier
                    .size(54.dp)
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

            // Middle Filename, Size/Status, and Progress Bar with Percentage
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = upload.fileName,
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "$formattedSize • ${if (upload.status == "Uploaded") "Uploaded (${upload.eventName})" else "${upload.status}... (${upload.eventName})"}",
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
                        color = if (upload.status == "Uploaded") JummoSuccessGreen else JummoVioletBright,
                        trackColor = JummoDeepPurple,
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                    )
                    Text(
                        text = "${upload.progress}%",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right Circular Action Button (Pause / Retry / Done)
            Surface(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .clickable {
                        if (upload.status != "Uploaded") {
                            onRetry()
                        }
                    },
                color = JummoElevatedPurple,
                border = BorderStroke(0.75.dp, JummoBorderSubtle),
                shape = CircleShape
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (upload.status) {
                            "Uploaded" -> Icons.Default.CheckCircle
                            "Failed" -> Icons.Default.Refresh
                            else -> Icons.Default.Pause
                        },
                        contentDescription = upload.status,
                        tint = if (upload.status == "Uploaded") JummoSuccessGreen else Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
