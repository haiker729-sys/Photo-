package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.GuestAccessModes
import com.example.data.model.GuestFaceSearchSession
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusBadgePill
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoElevatedPurple
import com.example.ui.theme.JummoErrorRed
import com.example.ui.theme.JummoGold
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoSuccessGreen
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright

/**
 * Interactive preview of the Guest Web Portal & Consent-Based Face Search Experience
 * that guests open via the Event QR Code without needing to install an application.
 */
@Composable
fun GuestPortalModalScreen(
    event: PhotoEvent,
    eventUploads: List<PhotoUploadRecord>,
    guestSessions: List<GuestFaceSearchSession>,
    onRegisterConsentSession: (guestLabel: String, selfieUri: String, matchedCount: Long) -> Unit,
    onDeleteFaceData: (GuestFaceSearchSession) -> Unit,
    onReportMismatch: (GuestFaceSearchSession, String) -> Unit,
    onNotifyDownload: (String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    var selectedTab by remember { mutableStateOf(0) } // 0 = Find My Photos (Face Search), 1 = Event Gallery
    var guestName by remember { mutableStateOf("") }
    var explicitConsentChecked by remember { mutableStateOf(false) }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    val selectedPhotoIds = remember { mutableStateListOf<String>() }
    var sessionToReport by remember { mutableStateOf<GuestFaceSearchSession?>(null) }
    var reportNoteInput by remember { mutableStateOf("") }

    val selfiePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            selfieUri = uri
        }
    }

    val eventSessions = guestSessions.filter { it.eventId == event.id }
    val activeConsentSession = eventSessions.firstOrNull { it.matchStatus != "DataDeleted" }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("guest_portal_screen"),
        color = JummoObsidian
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Top Header Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(JummoDeepPurple)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onClose) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "Guest QR Web Portal Preview",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = event.qrCodeValue,
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoGoldBright,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                StatusBadgePill(
                    text = if (event.qrEnabled) "PORTAL LIVE" else "QR PAUSED",
                    color = if (event.qrEnabled) JummoSuccessGreen else JummoErrorRed
                )
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("guest_portal_list"),
                contentPadding = PaddingValues(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Event Information Banner
                item {
                    Card(
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                        border = BorderStroke(1.dp, JummoBorderSubtle)
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadgePill(text = event.category, color = JummoGoldBright)
                                Text(
                                    text = "Expires: ${event.qrExpirationDate}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JummoTextSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = event.name,
                                style = MaterialTheme.typography.headlineMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${event.eventDate} • ${event.location}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = JummoTextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Guests open this portal directly in their mobile browser by scanning the Event QR code — no app installation required.",
                                style = MaterialTheme.typography.bodySmall,
                                color = JummoVioletBright
                            )
                        }
                    }
                }

                // Mode Switch Tabs
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FilterChip(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Find My Photos (Face Search)") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Face,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JummoGoldBright,
                                selectedLabelColor = JummoObsidian,
                                selectedLeadingIconColor = JummoObsidian
                            )
                        )
                        FilterChip(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text("Standard Gallery") },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JummoGoldBright,
                                selectedLabelColor = JummoObsidian,
                                selectedLeadingIconColor = JummoObsidian
                            )
                        )
                    }
                }

                if (selectedTab == 0) {
                    // Privacy & Architecture Disclosure Banner
                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = JummoElevatedPurple.copy(alpha = 0.7f)),
                            border = BorderStroke(1.dp, JummoGold.copy(alpha = 0.5f))
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = JummoGoldBright,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Privacy-First Face Search Architecture",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "• Explicit guest consent is mandatory before any selfie submission.\n" +
                                            "• Event-Specific Isolation: Reference data is scoped strictly to ${event.name} (${event.id}).\n" +
                                            "• No fake client-side biometric matching: This interface records verified guest consent and prepares the payload for a separately secured face-matching backend.\n" +
                                            "• Guests can delete their face-search data or report incorrect matches at any time.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = JummoTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    // Consent & Selfie Capture Form
                    item {
                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                            border = BorderStroke(1.dp, JummoBorderSubtle)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Step 1: Guest Identification & Selfie",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedTextField(
                                    value = guestName,
                                    onValueChange = { guestName = it },
                                    label = { Text("Guest Name or Label") },
                                    placeholder = { Text("e.g., Priya Sharma") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            selfiePicker.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        },
                                        border = BorderStroke(1.dp, JummoGoldBright),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Face,
                                            contentDescription = null,
                                            tint = JummoGoldBright
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = if (selfieUri != null) "Selfie Selected" else "Select / Capture Selfie",
                                            color = JummoGoldBright
                                        )
                                    }
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(JummoDeepPurple)
                                        .clickable { explicitConsentChecked = !explicitConsentChecked }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = explicitConsentChecked,
                                        onCheckedChange = { explicitConsentChecked = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = JummoGoldBright,
                                            checkmarkColor = JummoObsidian
                                        ),
                                        modifier = Modifier.testTag("guest_consent_checkbox")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "I explicitly consent to temporary, event-isolated face matching for \"${event.name}\" and understand I can delete my face-search data at any time.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White
                                    )
                                }

                                Button(
                                    onClick = {
                                        onRegisterConsentSession(
                                            guestName.ifBlank { "Verified Guest" },
                                            selfieUri?.toString() ?: "local://guest_selfie_reference",
                                            eventUploads.size.toLong()
                                        )
                                        guestName = ""
                                    },
                                    enabled = explicitConsentChecked,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = JummoGoldBright,
                                        contentColor = JummoObsidian
                                    ),
                                    shape = RoundedCornerShape(14.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("submit_face_consent_button")
                                ) {
                                    Icon(imageVector = Icons.Default.VerifiedUser, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Authorize Consent & Queue Backend Face Search",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    // Active Consent Sessions & Privacy Controls
                    if (eventSessions.isNotEmpty()) {
                        item {
                            Text(
                                text = "Guest Consent & Face-Data Audit (${eventSessions.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(eventSessions, key = { it.id }) { session ->
                            Card(
                                shape = RoundedCornerShape(18.dp),
                                colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                                border = BorderStroke(1.dp, JummoBorderSubtle)
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = session.guestLabel,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                        StatusBadgePill(
                                            text = session.matchStatus,
                                            color = when (session.matchStatus) {
                                                "DataDeleted" -> JummoErrorRed
                                                "ReportedMismatch" -> JummoGoldBright
                                                else -> JummoSuccessGreen
                                            }
                                        )
                                    }
                                    Text(
                                        text = if (session.matchStatus == "DataDeleted") {
                                            "Selfie reference & face vectors permanently purged from event scope."
                                        } else {
                                            "Consent verified • Event-isolated (${event.name}) • Awaiting secured face-matching backend index (${eventUploads.size} event photos)."
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = JummoTextSecondary
                                    )
                                    if (session.reportNotes.isNotBlank()) {
                                        Text(
                                            text = "Mismatch Report: ${session.reportNotes}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = JummoGoldBright
                                        )
                                    }
                                    if (session.matchStatus != "DataDeleted") {
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            OutlinedButton(
                                                onClick = { onDeleteFaceData(session) },
                                                border = BorderStroke(1.dp, JummoErrorRed.copy(alpha = 0.7f)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.DeleteForever,
                                                    contentDescription = null,
                                                    tint = JummoErrorRed,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Delete Face Data", color = JummoErrorRed)
                                            }
                                            OutlinedButton(
                                                onClick = {
                                                    sessionToReport = session
                                                    reportNoteInput = ""
                                                },
                                                border = BorderStroke(1.dp, JummoGoldBright.copy(alpha = 0.7f)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Flag,
                                                    contentDescription = null,
                                                    tint = JummoGoldBright,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Report Match", color = JummoGoldBright)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Gallery & Download Section (works both after consent and in Standard Gallery mode if permitted)
                item {
                    val canViewAll = event.guestAccessMode == GuestAccessModes.VIEW_ALL_PERMITTED ||
                        activeConsentSession != null
                    if (!canViewAll && selectedTab == 1) {
                        EmptyStateCard(
                            icon = Icons.Default.Lock,
                            title = "Organizer Restricted Full Gallery",
                            message = "This event is configured for '${GuestAccessModes.displayLabel(event.guestAccessMode)}'. Complete the Consent Face Search or enable 'View All + Face Search' in Event settings."
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Event Photo Gallery (${eventUploads.size})",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (event.guestAllowDownload) {
                                            "Select photos for batch or individual high-res download"
                                        } else {
                                            "Downloads disabled by event organizer (View-Only)"
                                        },
                                        style = MaterialTheme.typography.bodySmall,
                                        color = JummoTextSecondary
                                    )
                                }
                                if (event.guestAllowDownload && selectedPhotoIds.isNotEmpty()) {
                                    Button(
                                        onClick = {
                                            onNotifyDownload(
                                                "Prepared ${selectedPhotoIds.size} selected photo(s) for guest download."
                                            )
                                            selectedPhotoIds.clear()
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = JummoGoldBright,
                                            contentColor = JummoObsidian
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Download,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Download (${selectedPhotoIds.size})",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }

                            if (eventUploads.isEmpty()) {
                                EmptyStateCard(
                                    icon = Icons.Default.PhotoLibrary,
                                    title = "No Photos Uploaded to Event Yet",
                                    message = "Capture photos with CameraX or import DSLR photos into ${event.name} to preview them in the guest gallery."
                                )
                            } else {
                                eventUploads.forEach { photo ->
                                    val isSelected = selectedPhotoIds.contains(photo.id)
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isSelected) JummoGoldBright else JummoBorderSubtle
                                        ),
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
                                                    .size(64.dp)
                                                    .clip(RoundedCornerShape(12.dp))
                                                    .background(JummoDeepPurple)
                                            ) {
                                                AsyncImage(
                                                    model = photo.localUri,
                                                    contentDescription = photo.fileName,
                                                    placeholder = painterResource(id = R.drawable.img_wedding_cover),
                                                    error = painterResource(id = R.drawable.img_wedding_cover),
                                                    modifier = Modifier.fillMaxSize(),
                                                    contentScale = ContentScale.Crop
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = photo.fileName,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = Color.White,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${photo.sourceType} • ${(photo.fileSizeBytes / 1024).coerceAtLeast(1)} KB",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = JummoTextSecondary
                                                )
                                            }
                                            if (event.guestAllowDownload) {
                                                IconButton(
                                                    onClick = {
                                                        if (isSelected) {
                                                            selectedPhotoIds.remove(photo.id)
                                                        } else {
                                                            selectedPhotoIds.add(photo.id)
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = if (isSelected) {
                                                            Icons.Default.CheckBox
                                                        } else {
                                                            Icons.Default.CheckBoxOutlineBlank
                                                        },
                                                        contentDescription = "Select Photo",
                                                        tint = JummoGoldBright
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        onNotifyDownload("Downloaded original ${photo.fileName}")
                                                    }
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Download,
                                                        contentDescription = "Download Photo",
                                                        tint = Color.White
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
        }
    }

    if (sessionToReport != null) {
        val target = sessionToReport!!
        AlertDialog(
            onDismissRequest = { sessionToReport = null },
            containerColor = JummoCardPurple,
            title = {
                Text("Report Incorrect Face Match", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Flag an inaccurate match for ${target.guestLabel} so the event organizer and face-matching service can exclude the photo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = JummoTextSecondary
                    )
                    OutlinedTextField(
                        value = reportNoteInput,
                        onValueChange = { reportNoteInput = it },
                        label = { Text("Reason / Photo Reference") },
                        placeholder = { Text("e.g., DSC_0412.JPG belongs to another guest") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onReportMismatch(target, reportNoteInput)
                        sessionToReport = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoGoldBright,
                        contentColor = JummoObsidian
                    )
                ) {
                    Text("Submit Report", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToReport = null }) {
                    Text("Cancel", color = JummoTextSecondary)
                }
            }
        )
    }
}
