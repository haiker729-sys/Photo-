package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdCard
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.statusColorForUpload
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright

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

    var statusFilter by remember { mutableStateOf("All") }

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

    val filteredUploads = uploads.filter { record ->
        when (statusFilter) {
            "Uploaded" -> record.status == "Uploaded"
            "Pending" -> record.status == "Pending" || record.status == "Uploading"
            "Failed" -> record.status == "Failed"
            "DSLR" -> record.sourceType == "DSLRImport"
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
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeaderRow(
                    title = "Uploads",
                    subtitle = "Automatic and manual upload queue • Original local files are preserved"
                )
            }

            // Target Event Selector
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Target Event",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (activeEvents.isEmpty()) {
                            Button(
                                onClick = onCreateEventClick,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoGoldBright,
                                    contentColor = JummoObsidian
                                )
                            ) {
                                Text("Create Event", fontWeight = FontWeight.Bold)
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
                                        label = { Text(evt.name) },
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
            }

            // Automatic Upload Card (WorkManager)
            item {
                val autoEnabled = profile?.autoUploadEnabled ?: true
                val wifiOnly = profile?.wifiOnlyUpload ?: true
                val batteryNotLow = profile?.batteryNotLowUpload ?: true

                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
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
                                    text = "Background WorkManager queue respecting network and battery conditions",
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

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = wifiOnly,
                                onClick = {
                                    onUpdateAutoUploadSettings(autoEnabled, !wifiOnly, batteryNotLow)
                                },
                                label = { Text("Wi-Fi Only") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JummoVioletBright,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = batteryNotLow,
                                onClick = {
                                    onUpdateAutoUploadSettings(autoEnabled, wifiOnly, !batteryNotLow)
                                },
                                label = { Text("Battery Not Low") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JummoVioletBright,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // Manual Upload & DSLR Import Card
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Manual Upload & DSLR Import",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Select photos from device gallery or import from SD card / camera storage (JPG, PNG, RAW).",
                            style = MaterialTheme.typography.bodySmall,
                            color = JummoTextSecondary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    manualGalleryPicker.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                enabled = currentEvent != null,
                                border = BorderStroke(0.75.dp, JummoGoldBright),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("select_gallery_photos_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Collections,
                                    contentDescription = null,
                                    tint = JummoGoldBright,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Select Photos", color = JummoGoldBright)
                            }

                            Button(
                                onClick = onUploadAllPendingNow,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoGoldBright,
                                    contentColor = JummoObsidian
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("upload_now_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudUpload,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Upload Now", fontWeight = FontWeight.Bold)
                            }
                        }

                        OutlinedButton(
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
                            enabled = currentEvent != null,
                            border = BorderStroke(0.75.dp, JummoVioletBright),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("dslr_saf_import_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.SdCard,
                                contentDescription = null,
                                tint = JummoVioletBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Import from SD Card / Camera Storage (RAW/JPG)", color = Color.White)
                        }
                    }
                }
            }

            // Queue Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Pending", "Uploaded", "Failed", "DSLR").forEach { tab ->
                        FilterChip(
                            selected = statusFilter == tab,
                            onClick = { statusFilter = tab },
                            label = { Text(tab) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JummoGoldBright,
                                selectedLabelColor = JummoObsidian
                            )
                        )
                    }
                }
            }

            if (filteredUploads.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.CloudUpload,
                        title = "No Uploads in Queue",
                        message = "Capture photos with Camera or select photos above to queue uploads."
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
private fun UploadQueueItemCard(
    upload: PhotoUploadRecord,
    onRetry: () -> Unit
) {
    val badgeColor = statusColorForUpload(upload.status)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(0.75.dp, JummoBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(JummoDeepPurple)
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
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = upload.fileName,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        StatusBadgePill(text = upload.status, color = badgeColor)
                    }
                    Text(
                        text = "${upload.eventName} • ${upload.sourceType} (${upload.uploadMode})",
                        style = MaterialTheme.typography.bodySmall,
                        color = JummoTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${upload.mimeType} • ${(upload.fileSizeBytes / 1024).coerceAtLeast(1)} KB",
                        style = MaterialTheme.typography.labelSmall,
                        color = JummoGoldBright
                    )
                }

                if (upload.status != "Uploaded") {
                    IconButton(onClick = onRetry) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Retry Upload",
                            tint = JummoGoldBright
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { (upload.progress / 100f).coerceIn(0f, 1f) },
                color = badgeColor,
                trackColor = JummoDeepPurple,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
            )
        }
    }
}
