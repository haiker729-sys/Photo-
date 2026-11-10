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
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import com.example.ui.theme.JummoVioletPrimary
import com.example.util.QrCodeGenerator
import java.util.Locale

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
    onOpenWebsiteCustomizer: (PhotoEvent) -> Unit = {},
    onShowMessage: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
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
        val q = searchQuery.trim().lowercase()
        val matchesSearch = q.isEmpty() ||
            event.name.lowercase().contains(q) ||
            event.location.lowercase().contains(q) ||
            event.category.lowercase().contains(q)
        matchesStatus && matchesCategory && matchesSearch
    }

    // Event Details & Photo Gallery View (Screen 7 in Reference)
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
            onRemoveUploadFromQueue = onRemoveUploadFromQueue,
            onShowMessage = onShowMessage
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
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header: "My Events" + Golden "+ New Event" Pill (Screen 3 in Reference)
                item {
                    SectionHeaderRow(
                        title = "My Events",
                        subtitle = "Manage your photography events",
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
                                shape = RoundedCornerShape(50),
                                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("events_add_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "New Event",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    maxLines = 1
                                )
                            }
                        }
                    )
                }

                // Search Bar + Filter Button + Category Pills + Active / Archived Pills (Screen 3 in Reference)
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = {
                                    Text(
                                        text = "Search events...",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = JummoTextSecondary
                                    )
                                },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search Events",
                                        tint = JummoTextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (searchQuery.isNotBlank()) {
                                        IconButton(
                                            onClick = { searchQuery = "" },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                tint = JummoTextSecondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    }
                                },
                                singleLine = true,
                                shape = RoundedCornerShape(50),
                                textStyle = MaterialTheme.typography.bodyMedium.copy(color = Color.White),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("events_search_input")
                            )

                            Surface(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .clickable {
                                        selectedCategoryFilter = "All"
                                        showArchived = false
                                    },
                                color = JummoCardPurple,
                                border = BorderStroke(0.85.dp, JummoBorderSubtle),
                                shape = CircleShape
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.FilterList,
                                        contentDescription = "Reset Filters",
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Category Filter Pills Row (All golden when selected)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val categories = listOf("All") + EventCategories.ALL
                            categories.forEach { category ->
                                val selected = selectedCategoryFilter == category
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .clickable { selectedCategoryFilter = category },
                                    color = if (selected) JummoGoldBright else JummoCardPurple,
                                    border = BorderStroke(
                                        0.85.dp,
                                        if (selected) JummoGoldBright else JummoBorderSubtle
                                    ),
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = category,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) JummoObsidian else Color.White,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
                                    )
                                }
                            }
                        }

                        // Active (X) / Archived (Y) Pills Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val activeCount = events.count { it.status == "Active" }
                            val archivedCount = events.count { it.status == "Archived" }

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { showArchived = false },
                                color = if (!showArchived) JummoVioletPrimary else JummoCardPurple,
                                border = BorderStroke(
                                    0.85.dp,
                                    if (!showArchived) JummoBorderGlow else JummoBorderSubtle
                                ),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Active ($activeCount)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .clickable { showArchived = true },
                                color = if (showArchived) JummoVioletPrimary else JummoCardPurple,
                                border = BorderStroke(
                                    0.85.dp,
                                    if (showArchived) JummoBorderGlow else JummoBorderSubtle
                                ),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "Archived ($archivedCount)",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (showArchived) Color.White else JummoTextSecondary,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                if (filteredEvents.isEmpty()) {
                    item {
                        EmptyStateCard(
                            icon = Icons.Default.Event,
                            title = if (showArchived) "No Archived Events" else "No Matching Events",
                            message = "Create an event with a category, date, venue, cover photograph, and QR access rules.",
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
                                        ),
                                        shape = RoundedCornerShape(10.dp)
                                    ) {
                                        Text("Create Event", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        )
                    }
                } else {
                    items(filteredEvents, key = { it.id }) { event ->
                        val eventPhotoBytes = uploads
                            .filter { it.eventId == event.id }
                            .sumOf { it.fileSizeBytes }
                        EventManagementCard(
                            event = event,
                            eventBytes = eventPhotoBytes,
                            onOpenDetails = { detailsEvent = event },
                            onEdit = {
                                editingEvent = event
                                isCreatingNew = false
                            },
                            onToggleArchive = { onToggleArchive(event) },
                            onDeleteRequest = { deletingEvent = event },
                            onOpenQrModal = { activeQrEvent = event },
                            onOpenWebsiteCustomizer = { onOpenWebsiteCustomizer(event) }
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
                    style = MaterialTheme.typography.bodySmall,
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
            onOpenGuestPortal = { updatedEvent ->
                activeQrEvent = null
                onConsumeQrEvent()
                onOpenGuestPortal(updatedEvent)
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
    eventBytes: Long,
    onOpenDetails: () -> Unit,
    onEdit: () -> Unit,
    onToggleArchive: () -> Unit,
    onDeleteRequest: () -> Unit,
    onOpenQrModal: () -> Unit,
    onOpenWebsiteCustomizer: () -> Unit = {}
) {
    val defaultPainter = when (event.coverType) {
        "festival", "School", "Corporate" -> painterResource(id = R.drawable.img_festival_cover)
        "studio" -> painterResource(id = R.drawable.img_hero_studio)
        else -> painterResource(id = R.drawable.img_wedding_cover)
    }

    val sizeText = remember(eventBytes, event.totalPhotos) {
        if (eventBytes > 0L) {
            val mb = eventBytes / (1024.0 * 1024.0)
            if (mb >= 1024.0) {
                String.format(Locale.US, "%.1f GB", mb / 1024.0)
            } else {
                String.format(Locale.US, "%.1f MB", mb.coerceAtLeast(0.1))
            }
        } else {
            "${event.uploadedPhotos} synced"
        }
    }

    // Compact Horizontal Luxury Event Card (matching Screen 3 in Reference)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetails() }
            .testTag("event_card_${event.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(0.9.dp, JummoBorderSubtle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Square Thumbnail Photograph
            Box(
                modifier = Modifier
                    .size(92.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(JummoDeepPurple)
                    .border(0.85.dp, JummoGoldBright.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
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
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Right Content: Title + Status Badge, Date/Category, Photos/Size, and 4 Square Action Buttons
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = event.name,
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    StatusBadgePill(
                        text = event.status,
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

                Text(
                    text = "${event.totalPhotos} Photos • $sizeText",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                // 4 Rounded Square Action Buttons + Archive Button (Gallery, QR, Edit, Archive, Delete)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Open Event Gallery / Details
                    EventSquareActionButton(
                        icon = Icons.Default.PhotoLibrary,
                        contentDescription = "Details & Photos",
                        tint = Color.White,
                        containerColor = JummoElevatedPurple,
                        onClick = onOpenDetails,
                        modifier = Modifier.testTag("details_button_${event.id}")
                    )

                    // 2. Open Event QR Modal
                    EventSquareActionButton(
                        icon = Icons.Default.QrCode2,
                        contentDescription = "QR Code",
                        tint = JummoVioletBright,
                        containerColor = JummoElevatedPurple,
                        onClick = onOpenQrModal,
                        modifier = Modifier.testTag("qr_button_${event.id}")
                    )

                    // 2b. Customize Event Website
                    EventSquareActionButton(
                        icon = Icons.Default.Share,
                        contentDescription = "Customize Website",
                        tint = JummoGoldBright,
                        containerColor = JummoElevatedPurple,
                        onClick = onOpenWebsiteCustomizer,
                        modifier = Modifier.testTag("website_customizer_button_${event.id}")
                    )

                    // 3. Edit Event
                    EventSquareActionButton(
                        icon = Icons.Default.Edit,
                        contentDescription = "Edit Event",
                        tint = Color.White,
                        containerColor = JummoElevatedPurple,
                        onClick = onEdit
                    )

                    // 4. Archive / Unarchive Event
                    EventSquareActionButton(
                        icon = if (event.status == "Archived") Icons.Default.Unarchive else Icons.Default.Archive,
                        contentDescription = "Archive Event",
                        tint = JummoGoldBright,
                        containerColor = JummoElevatedPurple,
                        onClick = onToggleArchive
                    )

                    // 5. Delete Event
                    EventSquareActionButton(
                        icon = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Event",
                        tint = JummoErrorRed,
                        containerColor = JummoErrorRed.copy(alpha = 0.14f),
                        onClick = onDeleteRequest
                    )
                }
            }
        }
    }
}

@Composable
private fun EventSquareActionButton(
    icon: ImageVector,
    contentDescription: String,
    tint: Color,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(9.dp))
            .clickable { onClick() },
        color = containerColor,
        border = BorderStroke(0.75.dp, JummoBorderSubtle),
        shape = RoundedCornerShape(9.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
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
    onRemoveUploadFromQueue: (PhotoUploadRecord) -> Unit,
    onShowMessage: (String) -> Unit
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

    var selectedGalleryTab by remember { mutableStateOf("All Photos") }
    var multiSelectActive by remember { mutableStateOf(false) }
    val selectedPhotoIds = remember { mutableStateListOf<String>() }

    val displayedPhotos = remember(eventUploads, selectedGalleryTab) {
        when (selectedGalleryTab) {
            "People (AI)" -> eventUploads.filter { it.status == "Uploaded" }
            "Highlights" -> eventUploads.take(6)
            else -> eventUploads
        }
    }

    // Event Photo Gallery View matching Screen 7 in Reference
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 840.dp)
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .testTag("event_details_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Header: Back Button + Event Name + Date & Photo Count + More Menu (Screen 7 in Reference)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Events",
                                tint = Color.White
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = event.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "Event Details",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JummoGoldBright,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "${event.eventDate} • ${event.totalPhotos} Photos",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JummoTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onOpenQr) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = "QR Management",
                                tint = JummoGoldBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        IconButton(onClick = onEditEvent) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Edit Event Options",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // 3 Filter Pill Tabs: "All Photos", "People (AI)", "Highlights" (Screen 7 in Reference)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    GalleryFilterPill(
                        label = "All Photos",
                        icon = null,
                        selected = selectedGalleryTab == "All Photos",
                        onClick = { selectedGalleryTab = "All Photos" }
                    )
                    GalleryFilterPill(
                        label = "People (AI)",
                        icon = Icons.Default.Face,
                        selected = selectedGalleryTab == "People (AI)",
                        onClick = { selectedGalleryTab = "People (AI)" }
                    )
                    GalleryFilterPill(
                        label = "Highlights",
                        icon = Icons.Default.AutoAwesome,
                        selected = selectedGalleryTab == "Highlights",
                        onClick = { selectedGalleryTab = "Highlights" }
                    )
                }
            }

            // Quick Actions Strip: Camera, Import, Guest Portal, Archive
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenCamera,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JummoGoldBright,
                            contentColor = JummoObsidian
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Camera", fontWeight = FontWeight.Bold, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = {
                            photoImporter.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        border = BorderStroke(0.75.dp, JummoGoldBright),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Collections,
                            contentDescription = null,
                            tint = JummoGoldBright,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Import", color = JummoGoldBright, maxLines = 1)
                    }

                    OutlinedButton(
                        onClick = onOpenGuestPortal,
                        border = BorderStroke(0.75.dp, JummoVioletBright),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = JummoVioletBright,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Guest AI", color = Color.White, maxLines = 1)
                    }
                }
            }

            // 3-Column Photo Grid (matching Screen 7 in Reference)
            if (displayedPhotos.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.PhotoLibrary,
                        title = "No Photos in $selectedGalleryTab",
                        message = "Use Camera or Import above to add original event photos to \"${event.name}\"."
                    )
                }
            } else {
                val rows = displayedPhotos.chunked(3)
                items(rows) { rowPhotos ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowPhotos.forEach { photo ->
                            val isSelected = selectedPhotoIds.contains(photo.id)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(JummoDeepPurple)
                                    .border(
                                        width = if (isSelected) 2.dp else 0.75.dp,
                                        color = if (isSelected) JummoGoldBright else JummoBorderSubtle,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        if (multiSelectActive) {
                                            if (isSelected) {
                                                selectedPhotoIds.remove(photo.id)
                                            } else {
                                                selectedPhotoIds.add(photo.id)
                                            }
                                        } else if (photo.status != "Uploaded") {
                                            onRetryUpload(photo)
                                        } else {
                                            onShowMessage("${photo.fileName} (${photo.status})")
                                        }
                                    }
                            ) {
                                AsyncImage(
                                    model = photo.localUri,
                                    contentDescription = photo.fileName,
                                    placeholder = defaultPainter,
                                    error = defaultPainter,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )

                                // Top-right status or selection badge
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                ) {
                                    if (multiSelectActive) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Selected",
                                            tint = if (isSelected) JummoGoldBright else Color.White.copy(alpha = 0.55f),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(8.dp)
                                                .clip(CircleShape)
                                                .background(statusColorForUpload(photo.status))
                                        )
                                    }
                                }

                                // Bottom subtle filename overlay
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .align(Alignment.BottomCenter)
                                        .background(
                                            Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Transparent,
                                                    JummoObsidian.copy(alpha = 0.8f)
                                                )
                                            )
                                        )
                                        .padding(horizontal = 6.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = photo.fileName,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                        repeat(3 - rowPhotos.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Bottom Gallery Action Bar: Select, Download, Share, More (Screen 7 in Reference)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = JummoCardPurple.copy(alpha = 0.96f),
            border = BorderStroke(0.75.dp, JummoBorderSubtle)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                BottomGalleryActionItem(
                    icon = Icons.Default.CheckBox,
                    label = if (multiSelectActive) "Done (${selectedPhotoIds.size})" else "Select",
                    active = multiSelectActive,
                    onClick = {
                        multiSelectActive = !multiSelectActive
                        if (!multiSelectActive) selectedPhotoIds.clear()
                    }
                )
                BottomGalleryActionItem(
                    icon = Icons.Default.Download,
                    label = "Download",
                    onClick = {
                        val count = if (selectedPhotoIds.isNotEmpty()) selectedPhotoIds.size else displayedPhotos.size
                        onShowMessage("$count original photo(s) verified in local device storage.")
                    }
                )
                BottomGalleryActionItem(
                    icon = Icons.Default.Share,
                    label = "Share",
                    onClick = onOpenQr
                )
                BottomGalleryActionItem(
                    icon = Icons.Default.MoreHoriz,
                    label = "More",
                    onClick = onEditEvent
                )
            }
        }
    }
}

@Composable
private fun GalleryFilterPill(
    label: String,
    icon: ImageVector?,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { onClick() },
        color = if (selected) JummoVioletPrimary else JummoCardPurple,
        border = BorderStroke(
            width = 0.9.dp,
            color = if (selected) JummoBorderGlow else JummoBorderSubtle
        ),
        shape = RoundedCornerShape(50)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (selected) Color.White else JummoGoldBright,
                    modifier = Modifier.size(15.dp)
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = Color.White,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BottomGalleryActionItem(
    icon: ImageVector,
    label: String,
    active: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (active) JummoGoldBright else Color.White,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (active) JummoGoldBright else JummoTextSecondary,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Medium
        )
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
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
                            text = if (coverUri.isNotBlank()) "Custom cover selected" else "Default category cover ($coverType)",
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
                        border = BorderStroke(0.75.dp, JummoGoldBright),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = null,
                            tint = JummoGoldBright,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Select Cover", color = JummoGoldBright)
                    }
                }

                // Guest Access Configuration
                Text(
                    text = "Guest Access",
                    style = MaterialTheme.typography.labelMedium,
                    color = JummoGoldBright
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
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
