package com.example.ui.screens

import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.EventCategories
import com.example.data.model.GuestAccessModes
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.StatusBadgePill
import com.example.ui.components.statusColorForUpload
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoErrorRed
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoSuccessGreen
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import com.example.util.QrCodeGenerator

@Composable
fun EventsScreen(
    events: List<PhotoEvent>,
    uploads: List<PhotoUploadRecord> = emptyList(),
    showCreateDialogCommand: Boolean,
    initialQrEvent: PhotoEvent?,
    initialDetailsEvent: PhotoEvent? = null,
    onConsumeCreateDialogCommand: () -> Unit,
    onConsumeQrEvent: () -> Unit,
    onConsumeDetailsEvent: () -> Unit = {},
    onCreateEvent: (
        name: String,
        category: String,
        eventDate: String,
        location: String,
        coverType: String,
        coverUri: String,
        guestAccessMode: String,
        guestAllowDownload: Boolean,
        qrExpirationDate: String
    ) -> Unit,
    onUpdateEvent: (PhotoEvent, String) -> Unit,
    onToggleArchive: (PhotoEvent) -> Unit,
    onDeleteEvent: (PhotoEvent) -> Unit,
    onOpenGuestPortal: (PhotoEvent) -> Unit,
    onSelectEventForCamera: (PhotoEvent) -> Unit,
    onImportPhotosForEvent: (PhotoEvent, List<Uri>) -> Unit = { _, _ -> },
    onRetryUpload: (PhotoUploadRecord) -> Unit = {},
    onRemoveUploadFromQueue: (PhotoUploadRecord) -> Unit = {},
    onShowMessage: (String) -> Unit
) {
    var selectedCategoryFilter by remember { mutableStateOf("All") }
    var showArchived by remember { mutableStateOf(false) }

    var editingEvent by remember { mutableStateOf<PhotoEvent?>(null) }
    var isCreatingNew by remember(showCreateDialogCommand) { mutableStateOf(showCreateDialogCommand) }
    var deletingEvent by remember { mutableStateOf<PhotoEvent?>(null) }
    var activeQrEvent by remember(initialQrEvent) { mutableStateOf(initialQrEvent) }
    var detailsEvent by remember(initialDetailsEvent) { mutableStateOf(initialDetailsEvent) }

    val filteredEvents = events.filter { event ->
        val matchesStatus = if (showArchived) event.status == "Archived" else event.status == "Active"
        val matchesCategory = selectedCategoryFilter == "All" || event.category == selectedCategoryFilter
        matchesStatus && matchesCategory
    }

    // Event Details & Photo Management View
    if (detailsEvent != null) {
        val liveEvent = events.find { it.id == detailsEvent?.id } ?: detailsEvent!!
        val eventUploads = uploads.filter { it.eventId == liveEvent.id }
        EventDetailsAndPhotosView(
            event = liveEvent,
            eventUploads = eventUploads,
            onBack = {
                detailsEvent = null
                onConsumeDetailsEvent()
            },
            onEditEvent = {
                editingEvent = liveEvent
                isCreatingNew = false
            },
            onOpenQr = { activeQrEvent = liveEvent },
            onOpenGuestPortal = { onOpenGuestPortal(liveEvent) },
            onOpenCamera = { onSelectEventForCamera(liveEvent) },
            onToggleArchive = { onToggleArchive(liveEvent) },
            onDeleteRequest = { deletingEvent = liveEvent },
            onImportPhotos = { uris -> onImportPhotosForEvent(liveEvent, uris) },
            onRetryUpload = onRetryUpload,
            onRemoveUploadFromQueue = onRemoveUploadFromQueue
        )
    } else {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .testTag("events_screen_list"),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SectionHeaderRow(
                        title = "Events",
                        subtitle = "Manage event shoots, cover art, QR codes, and photos",
                        trailingContent = {
                            Button(
                                onClick = {
                                    editingEvent = null
                                    isCreatingNew = true
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoGoldBright,
                                    contentColor = JummoObsidian
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.testTag("events_add_button")
                            ) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("New Event", fontWeight = FontWeight.Bold)
                            }
                        }
                    )
                }

                // Category & Archive Filter Row
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val categories = listOf("All") + EventCategories.ALL
                            categories.forEach { category ->
                                FilterChip(
                                    selected = selectedCategoryFilter == category,
                                    onClick = { selectedCategoryFilter = category },
                                    label = { Text(category) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = JummoGoldBright,
                                        selectedLabelColor = JummoObsidian
                                    )
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = !showArchived,
                                onClick = { showArchived = false },
                                label = { Text("Active (${events.count { it.status == "Active" }})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JummoVioletBright,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = showArchived,
                                onClick = { showArchived = true },
                                label = { Text("Archived (${events.count { it.status == "Archived" }})") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JummoVioletBright,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }

                if (filteredEvents.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Default.Event,
                            title = if (showArchived) "No Archived Events" else "No Active Events",
                            message = "Create an event with a category, date, location, cover image, and QR code configuration.",
                            actionButton = {
                                if (!showArchived) {
                                    Button(
                                        onClick = {
                                            editingEvent = null
                                            isCreatingNew = true
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = JummoGoldBright,
                                            contentColor = JummoObsidian
                                        )
                                    ) {
                                        Text("Create Event", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        )
                    }
                } else {
                    items(filteredEvents, key = { it.id }) { event ->
                        EventManagementCard(
                            event = event,
                            onOpenDetails = { detailsEvent = event },
                            onEdit = {
                                editingEvent = event
                                isCreatingNew = false
                            },
                            onToggleArchive = { onToggleArchive(event) },
                            onDeleteRequest = { deletingEvent = event },
                            onOpenQrModal = { activeQrEvent = event },
                            onOpenCamera = { onSelectEventForCamera(event) }
                        )
                    }
                }
            }
        }
    }

    // Create / Edit Event Modal
    if (isCreatingNew || editingEvent != null) {
        EventEditorDialog(
            existingEvent = editingEvent,
            onDismiss = {
                isCreatingNew = false
                editingEvent = null
                onConsumeCreateDialogCommand()
            },
            onSaveNew = { name, cat, date, loc, covType, covUri, guestMode, allowDl, qrExp ->
                onCreateEvent(name, cat, date, loc, covType, covUri, guestMode, allowDl, qrExp)
                isCreatingNew = false
                onConsumeCreateDialogCommand()
            },
            onSaveEdited = { updated ->
                onUpdateEvent(updated, "Event \"${updated.name}\" updated.")
                editingEvent = null
            }
        )
    }

    // Delete Confirmation Dialog
    if (deletingEvent != null) {
        val target = deletingEvent!!
        AlertDialog(
            onDismissRequest = { deletingEvent = null },
            containerColor = JummoCardPurple,
            title = {
                Text(
                    text = "Delete Event?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Delete \"${target.name}\"? Original photos stored locally on your device will remain untouched, while the event record and QR link will be removed.",
                    color = JummoTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (detailsEvent?.id == target.id) {
                            detailsEvent = null
                            onConsumeDetailsEvent()
                        }
                        onDeleteEvent(target)
                        deletingEvent = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoErrorRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_delete_event_button")
                ) {
                    Text("Delete Event", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingEvent = null }) {
                    Text("Cancel", color = JummoTextSecondary)
                }
            }
        )
    }

    // Event QR Management Dialog
    if (activeQrEvent != null) {
        val currentEvent = events.find { it.id == activeQrEvent?.id } ?: activeQrEvent!!
        EventQrSystemDialog(
            event = currentEvent,
            onUpdateEvent = { updated, msg ->
                onUpdateEvent(updated, msg)
                activeQrEvent = updated
            },
            onOpenGuestPortal = {
                val target = currentEvent
                activeQrEvent = null
                onConsumeQrEvent()
                onOpenGuestPortal(target)
            },
            onShowMessage = onShowMessage,
            onDismiss = {
                activeQrEvent = null
                onConsumeQrEvent()
            }
        )
    }
}

@Composable
private fun EventManagementCard(
    event: PhotoEvent,
    onOpenDetails: () -> Unit,
    onEdit: () -> Unit,
    onToggleArchive: () -> Unit,
    onDeleteRequest: () -> Unit,
    onOpenQrModal: () -> Unit,
    onOpenCamera: () -> Unit
) {
    val defaultPainter = when (event.coverType) {
        "festival", "School", "Corporate" -> painterResource(id = R.drawable.img_festival_cover)
        "studio" -> painterResource(id = R.drawable.img_hero_studio)
        else -> painterResource(id = R.drawable.img_wedding_cover)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() }
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(0.75.dp, JummoBorderSubtle)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(148.dp)
            ) {
                if (event.coverUri.isNotBlank()) {
                    AsyncImage(
                        model = event.coverUri,
                        contentDescription = event.name,
                        placeholder = defaultPainter,
                        error = defaultPainter,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = defaultPainter,
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
                                    JummoObsidian.copy(alpha = 0.90f)
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
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatusBadgePill(text = event.category, color = JummoGoldBright)
                        StatusBadgePill(
                            text = event.status,
                            color = if (event.status == "Active") JummoSuccessGreen else JummoTextSecondary
                        )
                    }
                    StatusBadgePill(
                        text = if (event.qrEnabled) "QR ACTIVE" else "QR PAUSED",
                        color = if (event.qrEnabled) JummoSuccessGreen else JummoErrorRed
                    )
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = JummoGoldBright,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${event.eventDate} • ${event.location}",
                            style = MaterialTheme.typography.bodySmall,
                            color = JummoTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${event.totalPhotos} photos • ${event.uploadedPhotos} uploaded • ${event.pendingPhotos} pending",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White
                    )
                    Text(
                        text = if (event.guestAllowDownload) "Downloads On" else "View-Only",
                        style = MaterialTheme.typography.labelSmall,
                        color = JummoGoldBright
                    )
                }

                // Primary Actions Row (Event Details & Photos, QR Management)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenDetails,
                        border = BorderStroke(0.75.dp, JummoVioletBright),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("details_button_${event.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhotoLibrary,
                            contentDescription = null,
                            tint = JummoVioletBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Details & Photos", color = Color.White)
                    }

                    Button(
                        onClick = onOpenQrModal,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JummoGoldBright,
                            contentColor = JummoObsidian
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("qr_button_${event.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("QR Code", fontWeight = FontWeight.Bold)
                    }
                }

                // Secondary Management Row (Camera, Edit, Archive, Delete)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onOpenCamera) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = JummoGoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Open Camera", color = JummoGoldBright)
                    }

                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Event",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onToggleArchive) {
                            Icon(
                                imageVector = if (event.status == "Archived") {
                                    Icons.Default.Unarchive
                                } else {
                                    Icons.Default.Archive
                                },
                                contentDescription = "Archive Event",
                                tint = JummoVioletBright
                            )
                        }
                        IconButton(onClick = onDeleteRequest) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Event",
                                tint = JummoErrorRed
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventDetailsAndPhotosView(
    event: PhotoEvent,
    eventUploads: List<PhotoUploadRecord>,
    onBack: () -> Unit,
    onEditEvent: () -> Unit,
    onOpenQr: () -> Unit,
    onOpenGuestPortal: () -> Unit,
    onOpenCamera: () -> Unit,
    onToggleArchive: () -> Unit,
    onDeleteRequest: () -> Unit,
    onImportPhotos: (List<Uri>) -> Unit,
    onRetryUpload: (PhotoUploadRecord) -> Unit,
    onRemoveUploadFromQueue: (PhotoUploadRecord) -> Unit
) {
    BackHandler { onBack() }

    val defaultPainter = when (event.coverType) {
        "festival", "School", "Corporate" -> painterResource(id = R.drawable.img_festival_cover)
        "studio" -> painterResource(id = R.drawable.img_hero_studio)
        else -> painterResource(id = R.drawable.img_wedding_cover)
    }

    val photoImporter = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(20)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onImportPhotos(uris)
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
                .testTag("event_details_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Events",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = "Event Details",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Row {
                        IconButton(onClick = onEditEvent) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Event",
                                tint = Color.White
                            )
                        }
                        IconButton(onClick = onToggleArchive) {
                            Icon(
                                imageVector = if (event.status == "Archived") {
                                    Icons.Default.Unarchive
                                } else {
                                    Icons.Default.Archive
                                },
                                contentDescription = "Archive Event",
                                tint = JummoVioletBright
                            )
                        }
                        IconButton(onClick = onDeleteRequest) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "Delete Event",
                                tint = JummoErrorRed
                            )
                        }
                    }
                }
            }

            // Event Cover & Metadata Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle)
                ) {
                    Column {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(175.dp)
                        ) {
                            if (event.coverUri.isNotBlank()) {
                                AsyncImage(
                                    model = event.coverUri,
                                    contentDescription = event.name,
                                    placeholder = defaultPainter,
                                    error = defaultPainter,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Image(
                                    painter = defaultPainter,
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
                                                JummoObsidian.copy(alpha = 0.92f)
                                            )
                                        )
                                    )
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
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
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = event.name,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${event.eventDate} • ${event.location}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JummoTextSecondary
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Guest Access: ${GuestAccessModes.displayLabel(event.guestAccessMode)} • QR Expiration: ${event.qrExpirationDate}",
                                style = MaterialTheme.typography.bodySmall,
                                color = JummoTextSecondary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onOpenQr,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = JummoGoldBright,
                                        contentColor = JummoObsidian
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.QrCode2,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("QR Management", fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = onOpenGuestPortal,
                                    border = BorderStroke(0.75.dp, JummoVioletBright),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = JummoVioletBright,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Guest Portal", color = Color.White)
                                }
                            }
                        }
                    }
                }
            }

            // Photo Management Header & Actions
            item {
                SectionHeaderRow(
                    title = "Photo Management (${eventUploads.size})",
                    subtitle = "Capture or import event photos • Original local files are preserved",
                    trailingContent = {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    photoImporter.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                border = BorderStroke(0.75.dp, JummoGoldBright),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Collections,
                                    contentDescription = "Import Photos",
                                    tint = JummoGoldBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Import", color = JummoGoldBright)
                            }
                            Button(
                                onClick = onOpenCamera,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoGoldBright,
                                    contentColor = JummoObsidian
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = "Camera",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Camera", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                )
            }

            if (eventUploads.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.PhotoLibrary,
                        title = "No Photos in This Event Yet",
                        message = "Use the Camera or Import button above to add photos to \"${event.name}\"."
                    )
                }
            } else {
                items(eventUploads, key = { it.id }) { photo ->
                    val statusColor = statusColorForUpload(photo.status)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                        border = BorderStroke(0.75.dp, JummoBorderSubtle),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(58.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(JummoDeepPurple)
                            ) {
                                AsyncImage(
                                    model = photo.localUri,
                                    contentDescription = photo.fileName,
                                    placeholder = defaultPainter,
                                    error = defaultPainter,
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
                                        text = photo.fileName,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    StatusBadgePill(text = photo.status, color = statusColor)
                                }
                                Text(
                                    text = "${photo.sourceType} • ${(photo.fileSizeBytes / 1024).coerceAtLeast(1)} KB • Original Local File Kept",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JummoTextSecondary
                                )
                            }
                            if (photo.status != "Uploaded") {
                                IconButton(onClick = { onRetryUpload(photo) }) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Upload Photo Now",
                                        tint = JummoGoldBright
                                    )
                                }
                            }
                            IconButton(onClick = { onRemoveUploadFromQueue(photo) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Remove from Cloud Queue (Keep Local Original)",
                                    tint = JummoTextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EventEditorDialog(
    existingEvent: PhotoEvent?,
    onDismiss: () -> Unit,
    onSaveNew: (
        name: String,
        category: String,
        eventDate: String,
        location: String,
        coverType: String,
        coverUri: String,
        guestAccessMode: String,
        guestAllowDownload: Boolean,
        qrExpirationDate: String
    ) -> Unit,
    onSaveEdited: (PhotoEvent) -> Unit
) {
    var name by remember { mutableStateOf(existingEvent?.name ?: "") }
    var category by remember { mutableStateOf(existingEvent?.category ?: "Wedding") }
    var eventDate by remember { mutableStateOf(existingEvent?.eventDate ?: "2026-12-15") }
    var location by remember { mutableStateOf(existingEvent?.location ?: "") }
    var coverType by remember { mutableStateOf(existingEvent?.coverType ?: "wedding") }
    var coverUri by remember { mutableStateOf(existingEvent?.coverUri ?: "") }
    var guestAccessMode by remember {
        mutableStateOf(existingEvent?.guestAccessMode ?: GuestAccessModes.FACE_SEARCH_ONLY)
    }
    var guestAllowDownload by remember { mutableStateOf(existingEvent?.guestAllowDownload ?: true) }
    var qrExpirationDate by remember { mutableStateOf(existingEvent?.qrExpirationDate ?: "2027-03-01") }

    val coverPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coverUri = uri.toString()
            coverType = "custom"
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JummoCardPurple,
        title = {
            Text(
                text = if (existingEvent == null) "Create Event" else "Edit Event",
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Event Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_name_input")
                )

                Text(
                    text = "Event Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = JummoGoldBright
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    EventCategories.ALL.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = {
                                category = cat
                                if (coverUri.isBlank()) {
                                    coverType = if (cat == "Festival" || cat == "Corporate" || cat == "School") {
                                        "festival"
                                    } else {
                                        "wedding"
                                    }
                                }
                            },
                            label = { Text(cat) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JummoGoldBright,
                                selectedLabelColor = JummoObsidian
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = eventDate,
                    onValueChange = { eventDate = it },
                    label = { Text("Event Date (YYYY-MM-DD)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Cover Image Upload
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Event Cover",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                        Text(
                            text = if (coverUri.isNotBlank()) "Custom cover image selected" else "Default category cover ($coverType)",
                            style = MaterialTheme.typography.bodySmall,
                            color = JummoTextSecondary
                        )
                    }
                    OutlinedButton(
                        onClick = {
                            coverPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        border = BorderStroke(0.75.dp, JummoGoldBright)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = JummoGoldBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Select Cover", color = JummoGoldBright)
                    }
                }

                // Guest Access Configuration
                Text(
                    text = "Guest Access",
                    style = MaterialTheme.typography.labelMedium,
                    color = JummoGoldBright
                )
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    GuestAccessModes.ALL.forEach { mode ->
                        FilterChip(
                            selected = guestAccessMode == mode,
                            onClick = { guestAccessMode = mode },
                            label = { Text(GuestAccessModes.displayLabel(mode)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JummoVioletBright,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                OutlinedTextField(
                    value = qrExpirationDate,
                    onValueChange = { qrExpirationDate = it },
                    label = { Text("QR Expiration Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Allow Guest Photo Downloads",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White
                    )
                    Switch(
                        checked = guestAllowDownload,
                        onCheckedChange = { guestAllowDownload = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JummoObsidian,
                            checkedTrackColor = JummoGoldBright
                        )
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val finalName = name.trim().ifBlank { "$category Shoot" }
                    val finalLoc = location.trim().ifBlank { "Main Studio Venue" }
                    if (existingEvent == null) {
                        onSaveNew(
                            finalName,
                            category,
                            eventDate,
                            finalLoc,
                            coverType,
                            coverUri,
                            guestAccessMode,
                            guestAllowDownload,
                            qrExpirationDate
                        )
                    } else {
                        onSaveEdited(
                            existingEvent.copy(
                                name = finalName,
                                category = category,
                                eventDate = eventDate,
                                location = finalLoc,
                                coverType = coverType,
                                coverUri = coverUri,
                                guestAccessMode = guestAccessMode,
                                guestAllowDownload = guestAllowDownload,
                                qrExpirationDate = qrExpirationDate
                            )
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = JummoGoldBright,
                    contentColor = JummoObsidian
                ),
                modifier = Modifier.testTag("save_event_button")
            ) {
                Text(
                    text = if (existingEvent == null) "Create Event" else "Save Changes",
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = JummoTextSecondary)
            }
        }
    )
}

@Composable
private fun EventQrSystemDialog(
    event: PhotoEvent,
    onUpdateEvent: (PhotoEvent, String) -> Unit,
    onOpenGuestPortal: () -> Unit,
    onShowMessage: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var qrEnabled by remember(event.qrEnabled) { mutableStateOf(event.qrEnabled) }
    var expirationDate by remember(event.qrExpirationDate) { mutableStateOf(event.qrExpirationDate) }
    var guestAccessMode by remember(event.guestAccessMode) { mutableStateOf(event.guestAccessMode) }

    val qrBitmap: Bitmap = remember(event.id, event.name, event.category, event.eventDate, event.qrCodeValue) {
        QrCodeGenerator.createBrandedEventQrBitmap(
            eventName = event.name,
            eventCategory = event.category,
            eventDate = event.eventDate,
            qrPayload = event.qrCodeValue,
            sizePx = 720
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JummoCardPurple,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "QR Management",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                StatusBadgePill(
                    text = if (qrEnabled) "ACTIVE" else "DISABLED",
                    color = if (qrEnabled) JummoSuccessGreen else JummoErrorRed
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = "Generated Event QR Code",
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                )

                // Download & Share QR Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            QrCodeGenerator.saveQrBitmapToGallery(
                                context = context,
                                eventName = event.name,
                                eventCategory = event.category,
                                eventDate = event.eventDate,
                                qrPayload = event.qrCodeValue
                            ).onSuccess { savedPath ->
                                onShowMessage("QR Code downloaded to $savedPath")
                            }.onFailure { err ->
                                onShowMessage("QR download failed: ${err.localizedMessage}")
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JummoGoldBright,
                            contentColor = JummoObsidian
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Download QR", fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            QrCodeGenerator.shareEventQrCode(
                                context = context,
                                eventName = event.name,
                                eventCategory = event.category,
                                eventDate = event.eventDate,
                                qrPayload = event.qrCodeValue
                            ).onFailure { err ->
                                onShowMessage("Unable to open Share sheet: ${err.localizedMessage}")
                            }
                        },
                        border = BorderStroke(0.75.dp, JummoGoldBright),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = null,
                            tint = JummoGoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Share QR", color = JummoGoldBright)
                    }
                }

                // Enable / Disable QR Toggle
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(JummoDeepPurple)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Event QR Portal",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White
                        )
                        Text(
                            text = if (qrEnabled) "Guests can scan and search photos" else "QR access is paused for guests",
                            style = MaterialTheme.typography.bodySmall,
                            color = JummoTextSecondary
                        )
                    }
                    Switch(
                        checked = qrEnabled,
                        onCheckedChange = { checked ->
                            qrEnabled = checked
                            onUpdateEvent(
                                event.copy(qrEnabled = checked),
                                if (checked) "QR code enabled for ${event.name}." else "QR code disabled for ${event.name}."
                            )
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JummoObsidian,
                            checkedTrackColor = JummoGoldBright
                        )
                    )
                }

                // Expiration Settings
                OutlinedTextField(
                    value = expirationDate,
                    onValueChange = { expirationDate = it },
                    label = { Text("QR Expiration Date") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Guest Access Configuration
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Guest Access Mode",
                        style = MaterialTheme.typography.labelMedium,
                        color = JummoGoldBright
                    )
                    GuestAccessModes.ALL.forEach { mode ->
                        FilterChip(
                            selected = guestAccessMode == mode,
                            onClick = { guestAccessMode = mode },
                            label = { Text(GuestAccessModes.displayLabel(mode)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JummoVioletBright,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                OutlinedButton(
                    onClick = onOpenGuestPortal,
                    border = BorderStroke(0.75.dp, JummoVioletBright),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = null,
                        tint = JummoVioletBright,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Open Guest Portal View", color = Color.White)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onUpdateEvent(
                        event.copy(
                            qrEnabled = qrEnabled,
                            qrExpirationDate = expirationDate,
                            guestAccessMode = guestAccessMode
                        ),
                        "QR settings updated for ${event.name}."
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = JummoGoldBright,
                    contentColor = JummoObsidian
                )
            ) {
                Text("Save QR Settings", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = JummoTextSecondary)
            }
        }
    )
}
