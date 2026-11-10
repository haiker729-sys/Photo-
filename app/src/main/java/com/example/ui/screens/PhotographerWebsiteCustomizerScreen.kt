package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DesktopWindows
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TabletMac
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.ViewQuilt
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.BannerCropModes
import com.example.data.model.BannerHeightModes
import com.example.data.model.BannerPresets
import com.example.data.model.GuestAccessModes
import com.example.data.model.GuestFaceSearchSession
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.PhotographerProfile
import com.example.data.model.WebsiteCustomization
import com.example.data.model.WebsiteFonts
import com.example.data.model.WebsiteLayouts
import com.example.data.model.WebsiteThemes
import com.example.data.model.WebsiteVersionSnapshot
import com.example.data.model.buildDefaultWebsiteCustomizationForEvent
import com.example.ui.components.JummoGoldPrimaryButton
import com.example.ui.components.JummoPurpleSecondaryButton
import com.example.ui.components.StatusBadgePill
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoElevatedPurple
import com.example.ui.theme.JummoErrorRed
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoSuccessGreen
import com.example.ui.theme.JummoTextPrimary
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import com.example.ui.theme.JummoVioletPrimary
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Jummo Photos AI — Complete Production-Ready Photographer Website Customizer.
 * Enables every photographer to independently customize their event's public-facing
 * guest website (branding, clean image-only banners, Royal Purple & luxury themes,
 * Editorial / Classic Grid / Masonry layouts, AI Face Search & Gallery permissions,
 * real-time mobile/tablet/desktop live preview, Save Draft, Publish Website & Version Rollback).
 */
@Composable
fun PhotographerWebsiteCustomizerScreen(
    profile: PhotographerProfile?,
    events: List<PhotoEvent>,
    initialEvent: PhotoEvent? = null,
    uploads: List<PhotoUploadRecord>,
    guestSessions: List<GuestFaceSearchSession>,
    customizations: List<WebsiteCustomization>,
    versions: List<WebsiteVersionSnapshot>,
    onCopyUriToWebsiteStorage: (Uri, String) -> String,
    onSaveDraft: (WebsiteCustomization) -> Unit,
    onPublishWebsite: (WebsiteCustomization, PhotoEvent?) -> Unit,
    onRollbackVersion: (WebsiteCustomization, WebsiteVersionSnapshot, PhotoEvent?) -> Unit,
    onOpenPublicGuestPortal: (PhotoEvent, WebsiteCustomization) -> Unit,
    onNotifyMessage: (String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val defaultEvent = remember(initialEvent, events) {
        initialEvent ?: events.firstOrNull { it.status == "Active" } ?: events.firstOrNull()
    }
    var selectedEventId by rememberSaveable(defaultEvent?.id) {
        mutableStateOf(defaultEvent?.id ?: "studio_default")
    }

    val activeEvent = remember(events, selectedEventId) {
        events.find { it.id == selectedEventId } ?: PhotoEvent(
            id = "studio_default",
            userId = profile?.userId ?: "",
            name = "${profile?.studioName ?: "Jummo Pro Studio"} Showcase",
            category = "Wedding",
            eventDate = "15 Dec 2026",
            location = "Royal Grand Palace",
            status = "Active",
            qrEnabled = true,
            qrExpirationDate = "01 Mar 2027",
            guestAccessMode = GuestAccessModes.FACE_SEARCH_ONLY,
            guestAllowDownload = true
        )
    }

    val persistedCustomization = remember(customizations, activeEvent.id) {
        customizations.find { it.eventId == activeEvent.id }
            ?: customizations.find { it.id == "web_${activeEvent.id}" }
    }

    var draftConfig by remember(activeEvent.id, persistedCustomization?.id, persistedCustomization?.publishedVersion) {
        mutableStateOf(
            persistedCustomization ?: buildDefaultWebsiteCustomizationForEvent(
                event = activeEvent,
                studioName = profile?.studioName ?: "Jummo Pro Studio",
                photographerName = profile?.displayName ?: "Meraj Sir"
            )
        )
    }

    // Keep published status & version synced when Firestore updates arrive
    LaunchedEffect(
        persistedCustomization?.publicationStatus,
        persistedCustomization?.publishedVersion,
        persistedCustomization?.hasUnpublishedChanges
    ) {
        if (persistedCustomization != null) {
            draftConfig = draftConfig.copy(
                id = persistedCustomization.id,
                publicationStatus = persistedCustomization.publicationStatus,
                publishedVersion = persistedCustomization.publishedVersion,
                publishedUrl = persistedCustomization.publishedUrl,
                hasUnpublishedChanges = persistedCustomization.hasUnpublishedChanges
            )
        }
    }

    val eventVersions = remember(versions, activeEvent.id) {
        versions.filter { it.eventId == activeEvent.id }.sortedByDescending { it.versionNumber }
    }

    val eventUploads = remember(uploads, activeEvent.id) {
        val matching = uploads.filter { it.eventId == activeEvent.id }
        matching.ifEmpty { uploads.take(4) }
    }

    // Mobile tab mode ("edit" vs "preview") and preview device viewport ("mobile", "tablet", "desktop")
    var mobileModeTab by rememberSaveable { mutableStateOf("edit") }
    var previewDeviceViewport by rememberSaveable { mutableStateOf("mobile") }

    // Media Pickers (Zero-permission Android Photo Picker)
    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val stored = onCopyUriToWebsiteStorage(uri, "studio_logo")
            draftConfig = draftConfig.copy(logoUri = stored, hasUnpublishedChanges = true)
            onNotifyMessage("Studio logo updated in live preview.")
        }
    }

    val coverPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            val stored = onCopyUriToWebsiteStorage(uri, "website_cover")
            val updatedBanners = (listOf(stored) + draftConfig.bannerUrisList()).distinct()
            draftConfig = draftConfig.copy(
                coverImageUri = stored,
                bannerUrisCsv = updatedBanners.joinToString("|"),
                hasUnpublishedChanges = true
            )
            onNotifyMessage("Website cover image updated.")
        }
    }

    val multipleBannersPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(6)
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            val copied = uris.mapIndexed { idx, u ->
                onCopyUriToWebsiteStorage(u, "hero_banner_$idx")
            }
            val combined = (draftConfig.bannerUrisList() + copied).distinct()
            draftConfig = draftConfig.copy(
                bannerUrisCsv = combined.joinToString("|"),
                hasUnpublishedChanges = true
            )
            onNotifyMessage("Added ${copied.size} clean hero banner(s).")
        }
    }

    val previewMaxWidth = when (previewDeviceViewport) {
        "tablet" -> 600.dp
        "desktop" -> 840.dp
        else -> 380.dp
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("photographer_website_customizer_screen"),
        color = JummoObsidian
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(JummoDeepPurple, JummoObsidian)
                    )
                )
                .windowInsetsPadding(WindowInsets.safeDrawing)
        ) {
            // Top Customizer Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JummoCardPurple)
                            .border(0.75.dp, JummoBorderSubtle, RoundedCornerShape(10.dp))
                            .testTag("website_customizer_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = JummoTextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Website Customizer",
                            style = MaterialTheme.typography.titleMedium,
                            color = JummoTextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${draftConfig.studioName} • ${activeEvent.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoGoldBright,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    StatusBadgePill(
                        text = if (draftConfig.hasUnpublishedChanges) {
                            "UNPUBLISHED DRAFT"
                        } else {
                            "${draftConfig.publicationStatus.uppercase()} v${draftConfig.publishedVersion}"
                        },
                        color = when {
                            draftConfig.publicationStatus == "Suspended" -> JummoErrorRed
                            draftConfig.hasUnpublishedChanges -> JummoGoldBright
                            else -> JummoSuccessGreen
                        }
                    )
                }
            }

            // Event Selector Strip (Per-Event & Studio Default Customization)
            if (events.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                        .testTag("website_customizer_event_selector"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    events.forEach { evt ->
                        val isSelected = evt.id == activeEvent.id
                        Surface(
                            color = if (isSelected) JummoVioletPrimary else JummoCardPurple,
                            border = BorderStroke(
                                width = if (isSelected) 1.2.dp else 0.75.dp,
                                color = if (isSelected) JummoGoldBright else JummoBorderSubtle
                            ),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier
                                .clickable { selectedEventId = evt.id }
                                .testTag("website_event_chip_${evt.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Text(
                                    text = evt.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else JummoTextPrimary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }

            // Responsive Body: Two-panel side-by-side on Tablet/Desktop, Compact Tabbed on Mobile
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                val isTwoPanel = maxWidth >= 700.dp

                if (isTwoPanel) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            WebsiteCustomizerEditorPanel(
                                draftConfig = draftConfig,
                                activeEvent = activeEvent,
                                eventVersions = eventVersions,
                                onUpdateDraft = { updated ->
                                    draftConfig = updated.copy(hasUnpublishedChanges = true)
                                },
                                onPickLogo = {
                                    logoPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onPickCover = {
                                    coverPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onPickBanners = {
                                    multipleBannersPickerLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                onRollbackToVersion = { versionSnap ->
                                    onRollbackVersion(
                                        draftConfig,
                                        versionSnap,
                                        events.find { it.id == activeEvent.id }
                                    )
                                },
                                onOpenLiveGuestPortal = {
                                    onOpenPublicGuestPortal(activeEvent, draftConfig)
                                },
                                onNotifyMessage = onNotifyMessage
                            )
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(18.dp))
                                .border(1.dp, JummoBorderSubtle, RoundedCornerShape(18.dp))
                                .background(JummoCardPurple)
                        ) {
                            WebsitePreviewViewportToolbar(
                                selectedViewport = previewDeviceViewport,
                                onSelectViewport = { previewDeviceViewport = it },
                                onOpenFullScreenPortal = {
                                    onOpenPublicGuestPortal(activeEvent, draftConfig)
                                }
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(8.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .border(0.75.dp, JummoGoldBright.copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.TopCenter
                            ) {
                                GuestPortalModalScreen(
                                    event = activeEvent,
                                    eventUploads = eventUploads,
                                    guestSessions = guestSessions,
                                    websiteCustomization = draftConfig,
                                    isLivePreviewMode = true,
                                    previewMaxWidthDp = previewMaxWidth,
                                    onRegisterConsentSession = { _, _, _ -> },
                                    onDeleteFaceData = {},
                                    onReportMismatch = { _, _ -> },
                                    onNotifyDownload = onNotifyMessage,
                                    onClose = {}
                                )
                            }
                        }
                    }
                } else {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Compact Mobile Mode Switcher: Edit Controls vs Live Preview
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(JummoCardPurple)
                                .border(0.75.dp, JummoBorderSubtle, RoundedCornerShape(12.dp))
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val isEdit = mobileModeTab == "edit"
                            Surface(
                                color = if (isEdit) JummoVioletPrimary else Color.Transparent,
                                shape = RoundedCornerShape(9.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { mobileModeTab = "edit" }
                                    .testTag("website_customizer_tab_edit")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = if (isEdit) Color.White else JummoTextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Customize Website",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isEdit) Color.White else JummoTextPrimary,
                                        fontWeight = if (isEdit) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }

                            val isPreview = mobileModeTab == "preview"
                            Surface(
                                color = if (isPreview) JummoVioletPrimary else Color.Transparent,
                                shape = RoundedCornerShape(9.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { mobileModeTab = "preview" }
                                    .testTag("website_customizer_tab_preview")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = if (isPreview) Color.White else JummoTextSecondary,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Live Website Preview",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isPreview) Color.White else JummoTextPrimary,
                                        fontWeight = if (isPreview) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }

                        if (mobileModeTab == "edit") {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp)
                            ) {
                                WebsiteCustomizerEditorPanel(
                                    draftConfig = draftConfig,
                                    activeEvent = activeEvent,
                                    eventVersions = eventVersions,
                                    onUpdateDraft = { updated ->
                                        draftConfig = updated.copy(hasUnpublishedChanges = true)
                                    },
                                    onPickLogo = {
                                        logoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onPickCover = {
                                        coverPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onPickBanners = {
                                        multipleBannersPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    onRollbackToVersion = { versionSnap ->
                                        onRollbackVersion(
                                            draftConfig,
                                            versionSnap,
                                            events.find { it.id == activeEvent.id }
                                        )
                                    },
                                    onOpenLiveGuestPortal = {
                                        onOpenPublicGuestPortal(activeEvent, draftConfig)
                                    },
                                    onNotifyMessage = onNotifyMessage
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 14.dp, vertical = 4.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(1.dp, JummoBorderSubtle, RoundedCornerShape(16.dp))
                                    .background(JummoCardPurple)
                            ) {
                                WebsitePreviewViewportToolbar(
                                    selectedViewport = previewDeviceViewport,
                                    onSelectViewport = { previewDeviceViewport = it },
                                    onOpenFullScreenPortal = {
                                        onOpenPublicGuestPortal(activeEvent, draftConfig)
                                    }
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(6.dp)
                                        .clip(RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.TopCenter
                                ) {
                                    GuestPortalModalScreen(
                                        event = activeEvent,
                                        eventUploads = eventUploads,
                                        guestSessions = guestSessions,
                                        websiteCustomization = draftConfig,
                                        isLivePreviewMode = true,
                                        previewMaxWidthDp = previewMaxWidth,
                                        onRegisterConsentSession = { _, _, _ -> },
                                        onDeleteFaceData = {},
                                        onReportMismatch = { _, _ -> },
                                        onNotifyDownload = onNotifyMessage,
                                        onClose = {}
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Fixed Bottom Action Bar: Save Draft | Live Preview | Publish Website
            Surface(
                color = JummoCardPurple,
                border = BorderStroke(0.75.dp, JummoBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            onSaveDraft(draftConfig)
                        },
                        border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.65f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("website_save_draft_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            tint = JummoGoldBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Save Draft",
                            style = MaterialTheme.typography.labelMedium,
                            color = JummoTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = {
                            mobileModeTab = if (mobileModeTab == "preview") "edit" else "preview"
                        },
                        border = BorderStroke(0.75.dp, JummoVioletBright.copy(alpha = 0.7f)),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("website_live_preview_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = JummoVioletBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Live Preview",
                            style = MaterialTheme.typography.labelMedium,
                            color = JummoTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Button(
                        onClick = {
                            onPublishWebsite(
                                draftConfig,
                                events.find { it.id == activeEvent.id }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JummoGoldBright,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                        modifier = Modifier
                            .weight(1.25f)
                            .testTag("website_publish_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Publish,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Publish Website",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WebsitePreviewViewportToolbar(
    selectedViewport: String,
    onSelectViewport: (String) -> Unit,
    onOpenFullScreenPortal: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ViewportChip(
                label = "Mobile",
                icon = Icons.Default.PhoneAndroid,
                selected = selectedViewport == "mobile",
                tag = "website_preview_viewport_mobile",
                onClick = { onSelectViewport("mobile") }
            )
            ViewportChip(
                label = "Tablet",
                icon = Icons.Default.TabletMac,
                selected = selectedViewport == "tablet",
                tag = "website_preview_viewport_tablet",
                onClick = { onSelectViewport("tablet") }
            )
            ViewportChip(
                label = "Desktop",
                icon = Icons.Default.DesktopWindows,
                selected = selectedViewport == "desktop",
                tag = "website_preview_viewport_desktop",
                onClick = { onSelectViewport("desktop") }
            )
        }

        Surface(
            color = JummoElevatedPurple,
            border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.5f)),
            shape = RoundedCornerShape(50),
            modifier = Modifier
                .clickable { onOpenFullScreenPortal() }
                .testTag("website_open_guest_portal_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.OpenInNew,
                    contentDescription = null,
                    tint = JummoGoldBright,
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "Open Portal",
                    style = MaterialTheme.typography.labelSmall,
                    color = JummoGoldBright,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun ViewportChip(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        color = if (selected) JummoVioletPrimary else JummoObsidian,
        border = BorderStroke(
            width = 0.75.dp,
            color = if (selected) JummoGoldBright else JummoBorderSubtle
        ),
        shape = RoundedCornerShape(50),
        modifier = Modifier
            .clickable { onClick() }
            .testTag(tag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (selected) Color.White else JummoTextSecondary,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (selected) Color.White else JummoTextPrimary,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}

@Composable
private fun WebsiteCustomizerEditorPanel(
    draftConfig: WebsiteCustomization,
    activeEvent: PhotoEvent,
    eventVersions: List<WebsiteVersionSnapshot>,
    onUpdateDraft: (WebsiteCustomization) -> Unit,
    onPickLogo: () -> Unit,
    onPickCover: () -> Unit,
    onPickBanners: () -> Unit,
    onRollbackToVersion: (WebsiteVersionSnapshot) -> Unit,
    onOpenLiveGuestPortal: () -> Unit,
    onNotifyMessage: (String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val verifiedPagesUrl = remember(activeEvent.id) {
        QrCodeGenerator.buildVerifiedPagesGuestUrl(activeEvent.id)
    }
    val qrBitmap = remember(activeEvent.id, activeEvent.qrCodeValue) {
        QrCodeGenerator.generateQrMatrixBitmap(
            event = activeEvent,
            sizePx = 560
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("website_customizer_editor_list"),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // =========================================================================
        // A. WEBSITE IDENTITY & PHOTOGRAPHER BRANDING
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                border = BorderStroke(0.75.dp, JummoBorderSubtle),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "A. Website Identity & Branding",
                                style = MaterialTheme.typography.titleSmall,
                                color = JummoTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Branding",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoGoldBright
                            )
                            Switch(
                                checked = draftConfig.brandingEnabled,
                                onCheckedChange = {
                                    onUpdateDraft(draftConfig.copy(brandingEnabled = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = JummoGoldBright
                                ),
                                modifier = Modifier.testTag("website_branding_switch")
                            )
                        }
                    }

                    OutlinedTextField(
                        value = draftConfig.studioName,
                        onValueChange = { onUpdateDraft(draftConfig.copy(studioName = it)) },
                        label = { Text("Studio / Business Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("website_studio_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JummoGoldBright,
                            unfocusedBorderColor = JummoBorderSubtle,
                            focusedTextColor = JummoTextPrimary,
                            unfocusedTextColor = JummoTextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = draftConfig.photographerName,
                        onValueChange = { onUpdateDraft(draftConfig.copy(photographerName = it)) },
                        label = { Text("Lead Photographer Name") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("website_photographer_name_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JummoGoldBright,
                            unfocusedBorderColor = JummoBorderSubtle,
                            focusedTextColor = JummoTextPrimary,
                            unfocusedTextColor = JummoTextPrimary
                        )
                    )

                    // Studio Logo & Website Cover Upload Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onPickLogo,
                            border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.65f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("website_upload_logo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = if (draftConfig.logoUri.isBlank()) "Upload Logo" else "Replace Logo",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextPrimary
                            )
                        }

                        if (draftConfig.logoUri.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    onUpdateDraft(draftConfig.copy(logoUri = ""))
                                },
                                border = BorderStroke(0.75.dp, JummoErrorRed.copy(alpha = 0.65f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("website_remove_logo_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Remove Logo",
                                    tint = JummoErrorRed,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onPickCover,
                            border = BorderStroke(0.75.dp, JummoVioletBright.copy(alpha = 0.65f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("website_upload_cover_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = JummoVioletBright,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Cover Image",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextPrimary
                            )
                        }
                    }

                    // Typography Selection
                    Text(
                        text = "Editorial Typography",
                        style = MaterialTheme.typography.labelMedium,
                        color = JummoGoldBright,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WebsiteFonts.ALL.forEach { fontKey ->
                            val selected = draftConfig.fontFamily == fontKey
                            Surface(
                                color = if (selected) JummoVioletPrimary else JummoObsidian,
                                border = BorderStroke(
                                    width = 0.75.dp,
                                    color = if (selected) JummoGoldBright else JummoBorderSubtle
                                ),
                                shape = RoundedCornerShape(50),
                                modifier = Modifier
                                    .clickable {
                                        onUpdateDraft(draftConfig.copy(fontFamily = fontKey))
                                    }
                                    .testTag("website_font_$fontKey")
                            ) {
                                Text(
                                    text = WebsiteFonts.displayLabel(fontKey),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) Color.White else JummoTextPrimary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = draftConfig.welcomeMessage,
                        onValueChange = { onUpdateDraft(draftConfig.copy(welcomeMessage = it)) },
                        label = { Text("Custom Guest Welcome Message") },
                        maxLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("website_welcome_message_input"),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = JummoGoldBright,
                            unfocusedBorderColor = JummoBorderSubtle,
                            focusedTextColor = JummoTextPrimary,
                            unfocusedTextColor = JummoTextPrimary
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = draftConfig.contactEmail,
                            onValueChange = { onUpdateDraft(draftConfig.copy(contactEmail = it)) },
                            label = { Text("Contact Email") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JummoGoldBright,
                                unfocusedBorderColor = JummoBorderSubtle,
                                focusedTextColor = JummoTextPrimary,
                                unfocusedTextColor = JummoTextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = draftConfig.contactPhone,
                            onValueChange = { onUpdateDraft(draftConfig.copy(contactPhone = it)) },
                            label = { Text("Studio Phone") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JummoGoldBright,
                                unfocusedBorderColor = JummoBorderSubtle,
                                focusedTextColor = JummoTextPrimary,
                                unfocusedTextColor = JummoTextPrimary
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = draftConfig.studioLocation,
                            onValueChange = { onUpdateDraft(draftConfig.copy(studioLocation = it)) },
                            label = { Text("Studio Location") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JummoGoldBright,
                                unfocusedBorderColor = JummoBorderSubtle,
                                focusedTextColor = JummoTextPrimary,
                                unfocusedTextColor = JummoTextPrimary
                            )
                        )
                        OutlinedTextField(
                            value = draftConfig.instagramUrl,
                            onValueChange = { onUpdateDraft(draftConfig.copy(instagramUrl = it)) },
                            label = { Text("Instagram / Portfolio URL") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = JummoGoldBright,
                                unfocusedBorderColor = JummoBorderSubtle,
                                focusedTextColor = JummoTextPrimary,
                                unfocusedTextColor = JummoTextPrimary
                            )
                        )
                    }
                }
            }
        }

        // =========================================================================
        // B. BANNER / HERO CUSTOMIZATION (STRICT IMAGE-ONLY RULE)
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                border = BorderStroke(0.75.dp, JummoBorderSubtle),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ViewCarousel,
                                contentDescription = null,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Column {
                                Text(
                                    text = "B. Hero Banner Customization",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = JummoTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Image-only hero slider",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JummoTextSecondary
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "Auto Slide",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoGoldBright
                            )
                            Switch(
                                checked = draftConfig.autoSliderEnabled,
                                onCheckedChange = {
                                    onUpdateDraft(draftConfig.copy(autoSliderEnabled = it))
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = JummoGoldBright
                                ),
                                modifier = Modifier.testTag("website_auto_slider_switch")
                            )
                        }
                    }

                    // Upload & Add Preset Banner Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        JummoGoldPrimaryButton(
                            text = "Upload Banner(s)",
                            icon = Icons.Default.AddPhotoAlternate,
                            onClick = onPickBanners,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("website_upload_banners_button")
                        )

                        JummoPurpleSecondaryButton(
                            text = "Add Studio Artwork",
                            icon = Icons.Default.ViewCarousel,
                            onClick = {
                                val currentList = draftConfig.bannerUrisList()
                                val nextPreset = BannerPresets.ALL.firstOrNull { it !in currentList }
                                    ?: BannerPresets.PRESET_FESTIVAL
                                val updated = (currentList + nextPreset)
                                onUpdateDraft(
                                    draftConfig.copy(bannerUrisCsv = updated.joinToString("|"))
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("website_add_preset_banner_button")
                        )
                    }

                    // Active Banner Thumbnails with Remove Action
                    val currentBanners = draftConfig.bannerUrisList()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        currentBanners.forEachIndexed { idx, uriStr ->
                            Box(
                                modifier = Modifier
                                    .width(124.dp)
                                    .height(74.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(0.75.dp, JummoGoldBright.copy(alpha = 0.55f), RoundedCornerShape(10.dp))
                            ) {
                                GuestWebsiteImageThumbnail(
                                    imageUri = uriStr,
                                    contentDescription = "Hero Banner ${idx + 1}",
                                    modifier = Modifier.fillMaxSize()
                                )
                                if (currentBanners.size > 1) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(22.dp)
                                            .clip(CircleShape)
                                            .background(JummoObsidian.copy(alpha = 0.8f))
                                            .clickable {
                                                val remaining = currentBanners.filterIndexed { i, _ -> i != idx }
                                                onUpdateDraft(
                                                    draftConfig.copy(
                                                        bannerUrisCsv = remaining.joinToString("|")
                                                    )
                                                )
                                            }
                                            .testTag("website_remove_banner_$idx"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remove Banner ${idx + 1}",
                                            tint = JummoErrorRed,
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Slider Interval, Banner Height & Crop Mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Slide Interval:",
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoTextSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf(2L, 4L, 6L, 8L).forEach { sec ->
                                val selected = draftConfig.sliderIntervalSeconds == sec
                                Surface(
                                    color = if (selected) JummoVioletPrimary else JummoObsidian,
                                    border = BorderStroke(
                                        0.75.dp,
                                        if (selected) JummoGoldBright else JummoBorderSubtle
                                    ),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.clickable {
                                        onUpdateDraft(draftConfig.copy(sliderIntervalSeconds = sec))
                                    }
                                ) {
                                    Text(
                                        text = "${sec}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) Color.White else JummoTextPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Banner Height:",
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoTextSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BannerHeightModes.ALL.forEach { heightMode ->
                                val selected = draftConfig.bannerHeightMode == heightMode
                                Surface(
                                    color = if (selected) JummoVioletPrimary else JummoObsidian,
                                    border = BorderStroke(
                                        0.75.dp,
                                        if (selected) JummoGoldBright else JummoBorderSubtle
                                    ),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier
                                        .clickable {
                                            onUpdateDraft(draftConfig.copy(bannerHeightMode = heightMode))
                                        }
                                        .testTag("website_banner_height_$heightMode")
                                ) {
                                    Text(
                                        text = heightMode,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) Color.White else JummoTextPrimary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Image Framing:",
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoTextSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            BannerCropModes.ALL.forEach { cropMode ->
                                val selected = draftConfig.bannerCropMode == cropMode
                                Surface(
                                    color = if (selected) JummoVioletPrimary else JummoObsidian,
                                    border = BorderStroke(
                                        0.75.dp,
                                        if (selected) JummoGoldBright else JummoBorderSubtle
                                    ),
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.clickable {
                                        onUpdateDraft(draftConfig.copy(bannerCropMode = cropMode))
                                    }
                                ) {
                                    Text(
                                        text = BannerCropModes.displayLabel(cropMode),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) Color.White else JummoTextPrimary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // C. WEBSITE THEME SETTINGS (Default: Royal Purple)
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                border = BorderStroke(0.75.dp, JummoBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ColorLens,
                            contentDescription = null,
                            tint = JummoGoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "C. Website Theme Settings",
                                style = MaterialTheme.typography.titleSmall,
                                color = JummoTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Selected: ${WebsiteThemes.displayLabel(draftConfig.theme)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoGoldBright
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WebsiteThemes.ALL.forEach { themeKey ->
                            val selected = draftConfig.theme == themeKey
                            val swatchColors = when (themeKey) {
                                WebsiteThemes.ROYAL_PURPLE -> listOf(Color(0xFFFAF9FF), Color(0xFFEDE9FE), Color(0xFF5B21B6))
                                WebsiteThemes.MIDNIGHT_BLACK -> listOf(Color(0xFF050508), Color(0xFF27273A), Color(0xFFF7C747))
                                WebsiteThemes.CHAMPAGNE_GOLD -> listOf(Color(0xFF1C130C), Color(0xFFF7C747), Color(0xFFFEF08A))
                                WebsiteThemes.ELEGANT_IVORY -> listOf(Color(0xFF231B32), Color(0xFFD8B4FE), Color(0xFFFDE68A))
                                else -> listOf(Color(0xFFFAF9FF), Color(0xFF8B5CF6), Color(0xFF5B21B6))
                            }

                            Surface(
                                color = if (selected) JummoVioletPrimary else JummoObsidian,
                                border = BorderStroke(
                                    width = if (selected) 1.5.dp else 0.75.dp,
                                    color = if (selected) JummoGoldBright else JummoBorderSubtle
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .clickable {
                                        onUpdateDraft(draftConfig.copy(theme = themeKey))
                                    }
                                    .testTag("website_theme_$themeKey")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                                        swatchColors.forEach { c ->
                                            Box(
                                                modifier = Modifier
                                                    .size(10.dp)
                                                    .clip(CircleShape)
                                                    .background(c)
                                            )
                                        }
                                    }
                                    Text(
                                        text = WebsiteThemes.displayLabel(themeKey),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) Color.White else JummoTextPrimary,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        }
                    }

                    if (draftConfig.theme == WebsiteThemes.CUSTOM) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = draftConfig.primaryAccentHex,
                                onValueChange = { onUpdateDraft(draftConfig.copy(primaryAccentHex = it)) },
                                label = { Text("Primary Accent Hex") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = draftConfig.secondaryAccentHex,
                                onValueChange = { onUpdateDraft(draftConfig.copy(secondaryAccentHex = it)) },
                                label = { Text("Secondary Gold Hex") },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }

        // =========================================================================
        // D. WEBSITE LAYOUT SETTINGS (Default: Editorial)
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                border = BorderStroke(0.75.dp, JummoBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewQuilt,
                            contentDescription = null,
                            tint = JummoGoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "D. Gallery Layout Settings",
                                style = MaterialTheme.typography.titleSmall,
                                color = JummoTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Selected: ${WebsiteLayouts.displayLabel(draftConfig.layout)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoGoldBright
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WebsiteLayouts.ALL.forEach { layoutKey ->
                            val selected = draftConfig.layout == layoutKey
                            Surface(
                                color = if (selected) JummoVioletPrimary else JummoObsidian,
                                border = BorderStroke(
                                    width = if (selected) 1.5.dp else 0.75.dp,
                                    color = if (selected) JummoGoldBright else JummoBorderSubtle
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable {
                                        onUpdateDraft(draftConfig.copy(layout = layoutKey))
                                    }
                                    .testTag("website_layout_$layoutKey")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = WebsiteLayouts.displayLabel(layoutKey),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (selected) Color.White else JummoTextPrimary,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = when (layoutKey) {
                                            WebsiteLayouts.EDITORIAL -> "Magazine Story"
                                            WebsiteLayouts.CLASSIC_GRID -> "Symmetrical 2x"
                                            else -> "Multi-Aspect"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) Color.White.copy(alpha = 0.85f) else JummoTextSecondary,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // =========================================================================
        // E. GUEST FEATURES & ACCESS PERMISSIONS
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                border = BorderStroke(0.75.dp, JummoBorderSubtle),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = JummoGoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                        Column {
                            Text(
                                text = "E. Guest Features & Access Controls",
                                style = MaterialTheme.typography.titleSmall,
                                color = JummoTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Configure AI Face Search, Gallery browsing, and download rights",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextSecondary
                            )
                        }
                    }

                    // 4 Feature Toggles
                    WebsiteFeatureToggleRow(
                        title = "AI Face Search",
                        subtitle = "Allow guests to find their photos with explicit biometric consent",
                        checked = draftConfig.faceSearchEnabled,
                        tag = "website_toggle_face_search",
                        onCheckedChange = {
                            onUpdateDraft(draftConfig.copy(faceSearchEnabled = it))
                        }
                    )
                    WebsiteFeatureToggleRow(
                        title = "Photo Gallery",
                        subtitle = "Display the curated event photo gallery section",
                        checked = draftConfig.galleryEnabled,
                        tag = "website_toggle_gallery",
                        onCheckedChange = {
                            onUpdateDraft(draftConfig.copy(galleryEnabled = it))
                        }
                    )
                    WebsiteFeatureToggleRow(
                        title = "Photo Downloads",
                        subtitle = "Allow guests to download original high-resolution photos",
                        checked = draftConfig.downloadsEnabled,
                        tag = "website_toggle_downloads",
                        onCheckedChange = {
                            onUpdateDraft(draftConfig.copy(downloadsEnabled = it))
                        }
                    )
                    WebsiteFeatureToggleRow(
                        title = "Share Event Photos",
                        subtitle = "Allow guests to share event links and photos",
                        checked = draftConfig.shareEnabled,
                        tag = "website_toggle_share",
                        onCheckedChange = {
                            onUpdateDraft(draftConfig.copy(shareEnabled = it))
                        }
                    )

                    HorizontalDivider(color = JummoBorderSubtle)

                    Text(
                        text = "Guest Access Mode",
                        style = MaterialTheme.typography.labelMedium,
                        color = JummoGoldBright,
                        fontWeight = FontWeight.SemiBold
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        GuestAccessModes.ALL.chunked(2).forEach { rowModes ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowModes.forEach { modeKey ->
                                    val selected = draftConfig.guestAccessMode == modeKey
                                    Surface(
                                        color = if (selected) JummoVioletPrimary else JummoObsidian,
                                        border = BorderStroke(
                                            width = if (selected) 1.5.dp else 0.75.dp,
                                            color = if (selected) JummoGoldBright else JummoBorderSubtle
                                        ),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                val faceEnabled = if (modeKey == GuestAccessModes.GALLERY_ONLY) {
                                                    false
                                                } else {
                                                    true
                                                }
                                                onUpdateDraft(
                                                    draftConfig.copy(
                                                        guestAccessMode = modeKey,
                                                        faceSearchEnabled = faceEnabled
                                                    )
                                                )
                                            }
                                            .testTag("website_access_mode_$modeKey")
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = GuestAccessModes.displayLabel(modeKey),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (selected) Color.White else JummoTextPrimary,
                                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            if (selected) {
                                                Icon(
                                                    imageVector = Icons.Default.CheckCircle,
                                                    contentDescription = null,
                                                    tint = Color.White,
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
            }
        }

        // =========================================================================
        // F. QR PORTAL, SHAREABLE GUEST URL, DNS STATUS & VERSION ROLLBACK
        // =========================================================================
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.45f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("website_qr_and_url_card")
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "F. Published Guest URL & Event QR",
                                style = MaterialTheme.typography.titleSmall,
                                color = JummoTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        StatusBadgePill(
                            text = "v${draftConfig.publishedVersion} LIVE",
                            color = JummoSuccessGreen
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.White)
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = qrBitmap.asImageBitmap(),
                                contentDescription = "Scannable Event Website QR Code",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit
                            )
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "Verified Live GitHub Pages Portal URL:",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoGoldBright,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = verifiedPagesUrl,
                                style = MaterialTheme.typography.bodySmall,
                                color = JummoTextPrimary,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            val result = withContext(Dispatchers.IO) {
                                                QrCodeGenerator.saveHighResQrPngToGallery(
                                                    context = context,
                                                    event = activeEvent,
                                                    resolutionPx = 1600
                                                )
                                            }
                                            result.onSuccess {
                                                onNotifyMessage("Saved high-resolution Event QR PNG.")
                                            }
                                        }
                                    },
                                    border = BorderStroke(0.75.dp, JummoGoldBright),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = JummoGoldBright,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "QR PNG",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JummoTextPrimary
                                    )
                                }

                                OutlinedButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "${draftConfig.studioName} — ${activeEvent.name} Guest Website: $verifiedPagesUrl"
                                            )
                                        }
                                        context.startActivity(
                                            Intent.createChooser(shareIntent, "Share Event Website Link")
                                        )
                                    },
                                    border = BorderStroke(0.75.dp, JummoVioletBright),
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = null,
                                        tint = JummoVioletBright,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Share URL",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JummoTextPrimary
                                    )
                                }
                            }
                        }
                    }

                    // Honest Custom Domain DNS Status Disclosure
                    Surface(
                        color = JummoObsidian.copy(alpha = 0.72f),
                        border = BorderStroke(0.75.dp, JummoBorderSubtle),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Hosting & DNS Status: Live portal is served via verified GitHub Pages ($verifiedPagesUrl).",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextSecondary
                            )
                        }
                    }

                    // Version History & Rollback Controls
                    if (eventVersions.isNotEmpty()) {
                        HorizontalDivider(color = JummoBorderSubtle)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = JummoGoldBright,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Published Version History (${eventVersions.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = JummoTextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            val rollbackTarget = eventVersions.firstOrNull {
                                it.versionNumber != draftConfig.publishedVersion
                            } ?: eventVersions.first()

                            OutlinedButton(
                                onClick = { onRollbackToVersion(rollbackTarget) },
                                border = BorderStroke(0.75.dp, JummoGoldBright),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.testTag("website_rollback_button")
                            ) {
                                Text(
                                    text = "Rollback to v${rollbackTarget.versionNumber}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JummoGoldBright,
                                    fontWeight = FontWeight.Bold
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
private fun WebsiteFeatureToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    tag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(JummoObsidian.copy(alpha = 0.65f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = JummoTextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = JummoTextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = JummoGoldBright
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}
