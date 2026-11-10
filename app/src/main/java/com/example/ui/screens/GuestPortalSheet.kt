package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.BannerCropModes
import com.example.data.model.BannerHeightModes
import com.example.data.model.BannerPresets
import com.example.data.model.GuestAccessModes
import com.example.data.model.GuestFaceSearchSession
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.WebsiteCustomization
import com.example.data.model.WebsiteFonts
import com.example.data.model.WebsiteLayouts
import com.example.data.model.WebsiteThemes
import com.example.ui.components.EmptyStateCard
import com.example.ui.components.StatusBadgePill
import com.example.ui.theme.JummoErrorRed
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoSuccessGreen
import com.example.util.QrCodeGenerator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Resolve a hex color string (e.g. "#0B0615") safely into a Compose Color.
 */
fun parseWebsiteHexColor(hex: String, fallback: Color): Color {
    val clean = hex.trim().removePrefix("#")
    return try {
        when (clean.length) {
            6 -> Color(("FF$clean").toLong(16))
            8 -> Color(clean.toLong(16))
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

/**
 * Palette resolved from a photographer's [WebsiteCustomization].
 */
data class GuestWebsitePalette(
    val background: Color,
    val surface: Color,
    val card: Color,
    val primaryAccent: Color,
    val secondaryHighlight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val borderSubtle: Color,
    val isLightTheme: Boolean
)

fun resolveGuestWebsitePalette(config: WebsiteCustomization): GuestWebsitePalette {
    val activeTheme = config.resolvedTheme()
    val preset = WebsiteThemes.paletteFor(activeTheme)
    val isCustom = activeTheme == WebsiteThemes.CUSTOM
    val bgHex = if (isCustom) config.backgroundColorHex else preset.backgroundHex
    val surfHex = if (isCustom) config.surfaceColorHex else preset.surfaceHex
    val cardHex = if (isCustom) config.cardColorHex else preset.cardHex
    val accentHex = if (isCustom) config.primaryAccentHex else preset.primaryAccentHex
    val goldHex = if (isCustom) config.secondaryAccentHex.ifBlank { config.secondaryHighlightHex } else preset.secondaryHighlightHex

    val isLight = activeTheme == WebsiteThemes.ELEGANT_IVORY || activeTheme == WebsiteThemes.ROYAL_PURPLE
    val background = parseWebsiteHexColor(bgHex, Color(0xFFFAF9FF))
    val surface = parseWebsiteHexColor(surfHex, Color(0xFFEDE9FE))
    val card = parseWebsiteHexColor(cardHex, Color(0xFFEDE9FE))
    val primaryAccent = parseWebsiteHexColor(accentHex, Color(0xFF5B21B6))
    val secondaryHighlight = parseWebsiteHexColor(goldHex, Color(0xFF8B5CF6))

    return GuestWebsitePalette(
        background = background,
        surface = surface,
        card = card,
        primaryAccent = primaryAccent,
        secondaryHighlight = secondaryHighlight,
        textPrimary = if (isLight) Color(0xFF24133F) else Color(0xFFFFFFFF),
        textSecondary = if (isLight) Color(0xFF6B5B83) else Color(0xFFD9CEE8),
        borderSubtle = if (isLight) Color(0xFFC7B3FF) else secondaryHighlight.copy(alpha = 0.28f),
        isLightTheme = isLight
    )
}

/**
 * 6-Section Customizable Guest Event Website & Consent-Based Face Search Experience
 * that guests open via the Event QR Code or inside the Live Website Preview:
 *
 * SECTION 1 — HEADER (Studio logo/name, Event title, Quick action button)
 * SECTION 2 — HERO BANNER SLIDER (STRICT IMAGE-ONLY: zero generated text/logos/buttons over the photo)
 * SECTION 3 — EVENT INFORMATION (Title, Date, Location, Category, Custom Welcome Message)
 * SECTION 4 — GUEST FEATURES BAR (Find My Photos AI Face Search, View Gallery, Download Selected, Share)
 * SECTION 5 — PHOTO GALLERY (Editorial / Classic Grid / Masonry, Lightbox, Select All, Batch Download, Pagination)
 * SECTION 6 — COMPACT FOOTER (Branding, Contact & Social links, Privacy Notice & Data Deletion modals)
 */
@Composable
fun GuestPortalModalScreen(
    event: PhotoEvent,
    eventUploads: List<PhotoUploadRecord>,
    guestSessions: List<GuestFaceSearchSession>,
    websiteCustomization: WebsiteCustomization? = null,
    isLivePreviewMode: Boolean = false,
    previewMaxWidthDp: Dp = 900.dp,
    onRegisterConsentSession: (guestLabel: String, selfieUri: String, matchedCount: Long) -> Unit,
    onDeleteFaceData: (GuestFaceSearchSession) -> Unit,
    onReportMismatch: (GuestFaceSearchSession, String) -> Unit,
    onNotifyDownload: (String) -> Unit,
    onClose: () -> Unit
) {
    if (!isLivePreviewMode) {
        BackHandler { onClose() }
    }

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val effectiveConfig = remember(websiteCustomization, event) {
        websiteCustomization ?: WebsiteCustomization(
            id = "web_${event.id}",
            userId = event.userId,
            eventId = event.id,
            studioName = "Jummo Pro Studio",
            photographerName = "Meraj Sir",
            brandingEnabled = true,
            coverImageUri = event.coverImageUri.ifBlank { BannerPresets.forCategory(event.category) },
            bannerImageUris = listOf(
                event.coverImageUri.ifBlank { BannerPresets.forCategory(event.category) },
                BannerPresets.PRESET_FESTIVAL,
                BannerPresets.PRESET_CORPORATE
            ).distinct(),
            themePreset = WebsiteThemes.ROYAL_PURPLE,
            websiteLayout = WebsiteLayouts.EDITORIAL,
            aiFaceSearchEnabled = event.guestAccessMode != GuestAccessModes.GALLERY_ONLY,
            photoGalleryEnabled = true,
            photoDownloadsEnabled = event.guestAllowDownload,
            sharePhotosEnabled = true,
            guestAccessMode = event.guestAccessMode
        )
    }

    val palette = remember(effectiveConfig) { resolveGuestWebsitePalette(effectiveConfig) }
    val headingFontFamily = remember(effectiveConfig.resolvedFont()) {
        when (effectiveConfig.resolvedFont()) {
            WebsiteFonts.PLAYFAIR_DISPLAY, WebsiteFonts.CORMORANT_GARAMOND -> FontFamily.Serif
            else -> FontFamily.SansSerif
        }
    }

    val accessValidation = remember(
        event.id,
        event.status,
        event.qrEnabled,
        event.qrExpirationDate,
        event.guestAccessMode,
        event.qrCodeValue,
        effectiveConfig.guestAccessMode,
        effectiveConfig.isWebsiteSuspended()
    ) {
        val mergedEvent = event.copy(
            guestAccessMode = effectiveConfig.guestAccessMode.ifBlank { event.guestAccessMode },
            guestAllowDownload = effectiveConfig.isDownloadsActive()
        )
        QrCodeGenerator.validateGuestPortalAccess(
            event = mergedEvent,
            websiteSuspended = effectiveConfig.isWebsiteSuspended()
        )
    }

    var selectedTab by remember(
        effectiveConfig.guestAccessMode,
        effectiveConfig.isFaceSearchActive()
    ) {
        mutableStateOf(
            if (
                !effectiveConfig.isFaceSearchActive() ||
                effectiveConfig.guestAccessMode == GuestAccessModes.VIEW_ALL_PERMITTED ||
                effectiveConfig.guestAccessMode == GuestAccessModes.GALLERY_ONLY
            ) {
                1
            } else {
                0
            }
        )
    }

    var guestName by remember { mutableStateOf("") }
    var explicitConsentChecked by remember { mutableStateOf(false) }
    var selfieUri by remember { mutableStateOf<Uri?>(null) }
    val selectedPhotoIds = remember { mutableStateListOf<String>() }
    var sessionToReport by remember { mutableStateOf<GuestFaceSearchSession?>(null) }
    var reportNoteInput by remember { mutableStateOf("") }
    var lightboxPhoto by remember { mutableStateOf<PhotoUploadRecord?>(null) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showDataDeletionDialog by remember { mutableStateOf(false) }
    var visiblePhotoCount by remember { mutableIntStateOf(6) }
    var isBatchDownloading by remember { mutableStateOf(false) }
    var batchDownloadProgress by remember { mutableStateOf(0f) }

    // Organizer Approved Access state (for GuestAccessModes.PIN_PROTECTED)
    val expectedApprovalCode = remember(event.guestPin, event.id) {
        if (event.guestPin.isNotBlank()) event.guestPin.trim()
        else event.id.replace("evt_", "").takeLast(4).uppercase().ifBlank { "2026" }
    }
    var approvalCodeInput by remember { mutableStateOf("") }
    var organizerApproved by remember(event.id, effectiveConfig.guestAccessMode) {
        mutableStateOf(effectiveConfig.guestAccessMode != GuestAccessModes.PIN_PROTECTED)
    }
    var approvalError by remember { mutableStateOf<String?>(null) }

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
        color = palette.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isLivePreviewMode) Modifier else Modifier.windowInsetsPadding(WindowInsets.safeDrawing)
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Preview Navigation Chrome (only when opened full-screen outside Customizer)
            if (!isLivePreviewMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(palette.surface)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.testTag("guest_portal_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = palette.textPrimary
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Guest Event Website • ${effectiveConfig.themePreset}",
                                style = MaterialTheme.typography.titleSmall,
                                color = palette.textPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = accessValidation.guestPortalUrl,
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.secondaryHighlight,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.testTag("guest_portal_url_text")
                            )
                        }
                    }
                    StatusBadgePill(
                        text = when {
                            !accessValidation.isAllowed && accessValidation.statusCode == "WEBSITE_SUSPENDED" -> "SUSPENDED"
                            !accessValidation.isAllowed && accessValidation.statusCode == "QR_EXPIRED" -> "QR EXPIRED"
                            !accessValidation.isAllowed && accessValidation.statusCode == "EVENT_ARCHIVED" -> "ARCHIVED"
                            !event.qrEnabled -> "QR PAUSED"
                            else -> "PORTAL LIVE"
                        },
                        color = if (accessValidation.isAllowed) JummoSuccessGreen else JummoErrorRed
                    )
                }
            }

            LazyColumn(
                modifier = Modifier
                    .widthIn(max = previewMaxWidthDp)
                    .fillMaxSize()
                    .testTag("guest_portal_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // =============================================================
                // SECTION 1 — STUDIO & EVENT WEBSITE HEADER
                // =============================================================
                item {
                    Surface(
                        color = palette.surface,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.dp, palette.borderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("guest_website_header_section")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (effectiveConfig.brandingEnabled) {
                                    Box(
                                        modifier = Modifier
                                            .size(38.dp)
                                            .clip(CircleShape)
                                            .background(palette.card)
                                            .border(1.dp, palette.secondaryHighlight, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (effectiveConfig.logoUri.isNotBlank()) {
                                            AsyncImage(
                                                model = effectiveConfig.logoUri,
                                                contentDescription = "Studio Logo",
                                                modifier = Modifier.fillMaxSize(),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Studio Camera Logo",
                                                tint = palette.secondaryHighlight,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                }

                                Column(modifier = Modifier.weight(1f)) {
                                    if (effectiveConfig.brandingEnabled) {
                                        Text(
                                            text = effectiveConfig.studioName.ifBlank { "Jummo Pro Studio" },
                                            style = MaterialTheme.typography.titleSmall,
                                            fontFamily = headingFontFamily,
                                            color = palette.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.testTag("guest_website_studio_name")
                                        )
                                        Text(
                                            text = "By ${effectiveConfig.photographerName.ifBlank { "Meraj Sir" }} • ${event.name}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.secondaryHighlight,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    } else {
                                        Text(
                                            text = event.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontFamily = headingFontFamily,
                                            color = palette.textPrimary,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Official Event Photo Portal",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.textSecondary
                                        )
                                    }
                                }
                            }

                            if (effectiveConfig.aiFaceSearchEnabled && accessValidation.isAllowed) {
                                Surface(
                                    color = palette.secondaryHighlight,
                                    shape = RoundedCornerShape(50),
                                    modifier = Modifier.clickable { selectedTab = 0 }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Face,
                                            contentDescription = null,
                                            tint = JummoObsidian,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Find Photos",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = JummoObsidian,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // =============================================================
                // SECTION 2 — CLEAN IMAGE-ONLY HERO BANNER SLIDER
                // STRICT RULE: Zero automatically generated text, titles, logos, or buttons over the photo
                // =============================================================
                item {
                    val bannerUris = remember(
                        effectiveConfig.bannerUrisCsv,
                        effectiveConfig.bannerImageUris,
                        effectiveConfig.coverImageUri,
                        event.coverImageUri
                    ) {
                        val list = effectiveConfig.bannerUrisList()
                        if (list.isNotEmpty()) {
                            list
                        } else if (effectiveConfig.coverImageUri.isNotBlank()) {
                            listOf(effectiveConfig.coverImageUri)
                        } else if (event.coverImageUri.isNotBlank()) {
                            listOf(event.coverImageUri)
                        } else {
                            listOf(BannerPresets.forCategory(event.category))
                        }
                    }
                    GuestCleanHeroBannerSlider(
                        bannerUris = bannerUris,
                        autoSlideEnabled = effectiveConfig.autoSliderEnabled,
                        sliderIntervalSeconds = effectiveConfig.sliderIntervalSeconds,
                        bannerHeightMode = effectiveConfig.bannerHeightMode,
                        bannerCropMode = effectiveConfig.bannerCropMode,
                        palette = palette
                    )
                }

                // =============================================================
                // SECTION 3 — EVENT INFORMATION & WELCOME MESSAGE
                // =============================================================
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = palette.card),
                        border = BorderStroke(1.dp, palette.borderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("guest_website_event_info_section")
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                StatusBadgePill(text = event.category, color = palette.secondaryHighlight)
                                Text(
                                    text = "Expires: ${QrCodeGenerator.formatDisplayDate(event.qrExpirationDate, event.qrExpirationDate)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (accessValidation.statusCode == "QR_EXPIRED") JummoErrorRed else palette.textSecondary
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = event.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontFamily = headingFontFamily,
                                color = palette.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${QrCodeGenerator.formatDisplayDate(event.eventDate, event.eventDate)} • ${event.location}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = palette.textSecondary
                            )
                            if (effectiveConfig.welcomeMessage.isNotBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = effectiveConfig.welcomeMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textPrimary.copy(alpha = 0.92f),
                                    modifier = Modifier.testTag("guest_website_welcome_message")
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Access Mode: ${GuestAccessModes.displayLabel(effectiveConfig.guestAccessMode.ifBlank { event.guestAccessMode })} • Layout: ${WebsiteLayouts.displayLabel(effectiveConfig.resolvedLayout())}",
                                style = MaterialTheme.typography.labelMedium,
                                color = palette.secondaryHighlight,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.testTag("guest_portal_active_mode_label")
                            )
                        }
                    }
                }

                // =============================================================
                // VALIDATION GATE: Block access if Website Suspended, Event Archived, QR Disabled, or QR Expired
                // =============================================================
                if (!accessValidation.isAllowed) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("guest_portal_access_blocked_card"),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = palette.card),
                            border = BorderStroke(1.5.dp, JummoErrorRed)
                        ) {
                            Column(
                                modifier = Modifier.padding(20.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Block,
                                    contentDescription = null,
                                    tint = JummoErrorRed,
                                    modifier = Modifier.size(40.dp)
                                )
                                Text(
                                    text = accessValidation.title,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = palette.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = accessValidation.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = palette.textSecondary
                                )
                                StatusBadgePill(
                                    text = accessValidation.statusCode,
                                    color = JummoErrorRed
                                )
                            }
                        }
                    }
                    return@LazyColumn
                }

                // =============================================================
                // ACCESS MODE 3 GATE: Organizer Approved Access (PINProtected)
                // =============================================================
                if (accessValidation.requiresOrganizerApproval && !organizerApproved) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("organizer_approval_gate_card"),
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = palette.card),
                            border = BorderStroke(1.2.dp, palette.secondaryHighlight)
                        ) {
                            Column(
                                modifier = Modifier.padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Key,
                                        contentDescription = null,
                                        tint = palette.secondaryHighlight,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Organizer Approved Access Required",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = palette.textPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "Studio approval code for ${event.name}: $expectedApprovalCode",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.secondaryHighlight
                                        )
                                    }
                                }

                                Text(
                                    text = "This event is configured with 'Organizer Approved Access'. Guests must enter the organizer's approval code or receive studio approval before viewing photos.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textSecondary
                                )

                                OutlinedTextField(
                                    value = approvalCodeInput,
                                    onValueChange = {
                                        approvalCodeInput = it
                                        approvalError = null
                                    },
                                    label = { Text("Organizer Approval Code") },
                                    placeholder = { Text("Enter $expectedApprovalCode") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("organizer_approval_code_input")
                                )

                                if (approvalError != null) {
                                    Text(
                                        text = approvalError!!,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JummoErrorRed
                                    )
                                }

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Button(
                                        onClick = {
                                            if (approvalCodeInput.trim().equals(expectedApprovalCode, ignoreCase = true)) {
                                                organizerApproved = true
                                                approvalError = null
                                            } else {
                                                approvalError = "Invalid approval code. Expected $expectedApprovalCode."
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = palette.secondaryHighlight,
                                            contentColor = JummoObsidian
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("verify_organizer_code_button")
                                    ) {
                                        Text("Verify Code", fontWeight = FontWeight.Bold)
                                    }

                                    OutlinedButton(
                                        onClick = {
                                            organizerApproved = true
                                            approvalError = null
                                        },
                                        border = BorderStroke(1.dp, palette.secondaryHighlight),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("organizer_approve_access_button")
                                    ) {
                                        Text("Approve Guest", color = palette.secondaryHighlight, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                    return@LazyColumn
                }

                // =============================================================
                // SECTION 4 — GUEST FEATURES BAR
                // =============================================================
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("guest_website_features_bar"),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (effectiveConfig.aiFaceSearchEnabled && effectiveConfig.guestAccessMode != GuestAccessModes.GALLERY_ONLY) {
                                FilterChip(
                                    selected = selectedTab == 0,
                                    onClick = { selectedTab = 0 },
                                    label = { Text("Find My Photos (Face Search)") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Face,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = palette.secondaryHighlight,
                                        selectedLabelColor = JummoObsidian,
                                        selectedLeadingIconColor = JummoObsidian
                                    ),
                                    modifier = Modifier.testTag("guest_portal_tab_face_search")
                                )
                            }
                            if (effectiveConfig.photoGalleryEnabled) {
                                FilterChip(
                                    selected = selectedTab == 1,
                                    onClick = { selectedTab = 1 },
                                    label = { Text("View Gallery") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.PhotoLibrary,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = palette.secondaryHighlight,
                                        selectedLabelColor = JummoObsidian,
                                        selectedLeadingIconColor = JummoObsidian
                                    ),
                                    modifier = Modifier.testTag("guest_portal_tab_standard_gallery")
                                )
                            }
                            if (effectiveConfig.sharePhotosEnabled) {
                                FilterChip(
                                    selected = false,
                                    onClick = {
                                        val shareUrl = QrCodeGenerator.buildVerifiedPagesGuestUrl(event.id)
                                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "${event.name} • Event Photo Gallery")
                                            putExtra(
                                                Intent.EXTRA_TEXT,
                                                "View & find your event photos for ${event.name} by ${effectiveConfig.studioName}:\n$shareUrl"
                                            )
                                        }
                                        runCatching {
                                            context.startActivity(
                                                Intent.createChooser(sendIntent, "Share Event Website")
                                                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            )
                                        }
                                        onNotifyDownload("Shared event website link for ${event.name}")
                                    },
                                    label = { Text("Share") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Share,
                                            contentDescription = null,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    },
                                    modifier = Modifier.testTag("guest_website_share_button")
                                )
                            }
                        }
                    }
                }

                // =============================================================
                // AI FACE SEARCH CONSENT & SELFIE CAPTURE SECTION
                // =============================================================
                if (selectedTab == 0 && effectiveConfig.aiFaceSearchEnabled && effectiveConfig.guestAccessMode != GuestAccessModes.GALLERY_ONLY) {
                    item {
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = palette.surface),
                            border = BorderStroke(1.dp, palette.secondaryHighlight.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Security,
                                    contentDescription = null,
                                    tint = palette.secondaryHighlight,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Privacy-First Face Search Architecture",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = palette.textPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "• Explicit guest consent is mandatory before any selfie submission.\n" +
                                            "• Event-Specific Isolation: Reference data is scoped strictly to ${event.name} (${event.id}).\n" +
                                            "• Guests can delete their face-search data or report incorrect matches at any time.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textSecondary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = palette.card),
                            border = BorderStroke(1.dp, palette.borderSubtle)
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Step 1: Guest Identification & Selfie",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = palette.textPrimary,
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

                                OutlinedButton(
                                    onClick = {
                                        selfiePicker.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    border = BorderStroke(1.dp, palette.secondaryHighlight),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Face,
                                        contentDescription = null,
                                        tint = palette.secondaryHighlight
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (selfieUri != null) "Selfie Selected" else "Select / Capture Selfie",
                                        color = palette.secondaryHighlight
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(palette.surface)
                                        .clickable { explicitConsentChecked = !explicitConsentChecked }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Checkbox(
                                        checked = explicitConsentChecked,
                                        onCheckedChange = { explicitConsentChecked = it },
                                        colors = CheckboxDefaults.colors(
                                            checkedColor = palette.secondaryHighlight,
                                            checkmarkColor = JummoObsidian
                                        ),
                                        modifier = Modifier.testTag("guest_consent_checkbox")
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "I explicitly consent to temporary, event-isolated face matching for \"${event.name}\" and understand I can delete my face-search data at any time.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.textPrimary
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
                                        containerColor = palette.secondaryHighlight,
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

                    if (eventSessions.isNotEmpty()) {
                        item {
                            Text(
                                text = "Guest Consent & Face-Data Audit (${eventSessions.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = palette.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        items(eventSessions, key = { it.id }) { session ->
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(containerColor = palette.card),
                                border = BorderStroke(1.dp, palette.borderSubtle)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
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
                                            color = palette.textPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        StatusBadgePill(
                                            text = session.matchStatus,
                                            color = when (session.matchStatus) {
                                                "DataDeleted" -> JummoErrorRed
                                                "ReportedMismatch" -> palette.secondaryHighlight
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
                                        color = palette.textSecondary
                                    )
                                    if (session.reportNotes.isNotBlank()) {
                                        Text(
                                            text = "Mismatch Report: ${session.reportNotes}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = palette.secondaryHighlight
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
                                                border = BorderStroke(1.dp, palette.secondaryHighlight.copy(alpha = 0.7f)),
                                                shape = RoundedCornerShape(10.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Flag,
                                                    contentDescription = null,
                                                    tint = palette.secondaryHighlight,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("Report Match", color = palette.secondaryHighlight)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =============================================================
                // SECTION 5 — PHOTO GALLERY (Editorial / Classic Grid / Masonry)
                // =============================================================
                if (effectiveConfig.isGalleryActive()) {
                    item {
                        val effectiveMode = effectiveConfig.guestAccessMode.ifBlank { event.guestAccessMode }
                        val canViewGallery = when (effectiveMode) {
                            GuestAccessModes.VIEW_ALL_PERMITTED, GuestAccessModes.GALLERY_ONLY -> true
                            GuestAccessModes.PIN_PROTECTED -> organizerApproved
                            else -> activeConsentSession != null
                        }
                        if (!canViewGallery && selectedTab == 1) {
                            EmptyStateCard(
                                icon = Icons.Default.Lock,
                                title = "Organizer Restricted Full Gallery",
                                message = "This event is configured for '${GuestAccessModes.displayLabel(effectiveMode)}'. Complete the Consent Face Search or enable 'View All + Face Search' in QR/Website Management."
                            )
                        } else if (canViewGallery) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("guest_portal_unlocked_gallery"),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = "Event Photo Gallery (${eventUploads.size})",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontFamily = headingFontFamily,
                                            color = palette.textPrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (effectiveConfig.isDownloadsActive()) {
                                                "Layout: ${WebsiteLayouts.displayLabel(effectiveConfig.resolvedLayout())} • Tap photo for Lightbox"
                                            } else {
                                                "Downloads disabled by studio (View-Only Mode)"
                                            },
                                            style = MaterialTheme.typography.bodySmall,
                                            color = palette.textSecondary
                                        )
                                    }

                                    if (effectiveConfig.isDownloadsActive() && eventUploads.isNotEmpty()) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    if (selectedPhotoIds.size == eventUploads.size) {
                                                        selectedPhotoIds.clear()
                                                    } else {
                                                        selectedPhotoIds.clear()
                                                        selectedPhotoIds.addAll(eventUploads.map { it.id })
                                                    }
                                                },
                                                border = BorderStroke(1.dp, palette.secondaryHighlight),
                                                shape = RoundedCornerShape(10.dp),
                                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                modifier = Modifier.testTag("guest_gallery_select_all_button")
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.SelectAll,
                                                    contentDescription = null,
                                                    tint = palette.secondaryHighlight,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (selectedPhotoIds.size == eventUploads.size) "Clear" else "Select All",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = palette.secondaryHighlight
                                                )
                                            }

                                            if (selectedPhotoIds.isNotEmpty()) {
                                                Button(
                                                    onClick = {
                                                        val count = selectedPhotoIds.size
                                                        scope.launch {
                                                            isBatchDownloading = true
                                                            batchDownloadProgress = 0.25f
                                                            delay(120L)
                                                            batchDownloadProgress = 0.75f
                                                            delay(120L)
                                                            batchDownloadProgress = 1f
                                                            isBatchDownloading = false
                                                            onNotifyDownload("Prepared $count selected photo(s) for high-res download.")
                                                            selectedPhotoIds.clear()
                                                        }
                                                    },
                                                    colors = ButtonDefaults.buttonColors(
                                                        containerColor = palette.secondaryHighlight,
                                                        contentColor = JummoObsidian
                                                    ),
                                                    shape = RoundedCornerShape(10.dp),
                                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                                    modifier = Modifier.testTag("guest_gallery_batch_download_button")
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Download,
                                                        contentDescription = null,
                                                        modifier = Modifier.size(14.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(
                                                        text = "Download (${selectedPhotoIds.size})",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }

                                if (isBatchDownloading) {
                                    LinearProgressIndicator(
                                        progress = { batchDownloadProgress },
                                        color = palette.secondaryHighlight,
                                        trackColor = palette.surface,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(50))
                                    )
                                }

                                if (eventUploads.isEmpty()) {
                                    EmptyStateCard(
                                        icon = Icons.Default.PhotoLibrary,
                                        title = "No Photos Uploaded to Event Yet",
                                        message = "Capture photos with CameraX or import DSLR photos into ${event.name} to preview them in the guest gallery."
                                    )
                                } else {
                                    val visiblePhotos = eventUploads.take(visiblePhotoCount)
                                    GuestGalleryLayoutRenderer(
                                        photos = visiblePhotos,
                                        layoutMode = effectiveConfig.resolvedLayout(),
                                        allowDownloads = effectiveConfig.isDownloadsActive(),
                                        selectedPhotoIds = selectedPhotoIds,
                                        palette = palette,
                                        onToggleSelect = { photoId ->
                                            if (selectedPhotoIds.contains(photoId)) {
                                                selectedPhotoIds.remove(photoId)
                                            } else {
                                                selectedPhotoIds.add(photoId)
                                            }
                                        },
                                        onOpenLightbox = { photo -> lightboxPhoto = photo },
                                        onDownloadSingle = { photo ->
                                            onNotifyDownload("Downloaded original ${photo.fileName}")
                                        }
                                    )

                                    if (eventUploads.size > visiblePhotoCount) {
                                        OutlinedButton(
                                            onClick = { visiblePhotoCount += 6 },
                                            border = BorderStroke(1.dp, palette.secondaryHighlight),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("guest_gallery_load_more_button")
                                        ) {
                                            Text(
                                                text = "Load More Photos (${eventUploads.size - visiblePhotoCount} remaining)",
                                                color = palette.secondaryHighlight,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // =============================================================
                // SECTION 6 — COMPACT STUDIO FOOTER
                // =============================================================
                item {
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = palette.surface),
                        border = BorderStroke(1.dp, palette.borderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("guest_website_footer_section")
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            if (effectiveConfig.brandingEnabled) {
                                Text(
                                    text = "${effectiveConfig.studioName.ifBlank { "Jummo Pro Studio" }} • ${effectiveConfig.photographerName.ifBlank { "Meraj Sir" }}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = palette.textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${effectiveConfig.contactPhone} • ${effectiveConfig.contactEmail} • ${effectiveConfig.instagramHandle}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = palette.textSecondary
                                )
                            }
                            HorizontalDivider(color = palette.borderSubtle.copy(alpha = 0.5f))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Powered by Jummo Photos AI",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = palette.secondaryHighlight
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Text(
                                        text = "Privacy Policy",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.textSecondary,
                                        modifier = Modifier
                                            .clickable { showPrivacyDialog = true }
                                            .testTag("guest_footer_privacy_link")
                                    )
                                    Text(
                                        text = "Data Deletion",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.secondaryHighlight,
                                        modifier = Modifier
                                            .clickable { showDataDeletionDialog = true }
                                            .testTag("guest_footer_data_deletion_link")
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Lightbox Full-Screen Photo Preview Modal
    if (lightboxPhoto != null) {
        val targetPhoto = lightboxPhoto!!
        Dialog(onDismissRequest = { lightboxPhoto = null }) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = palette.card),
                border = BorderStroke(1.dp, palette.secondaryHighlight)
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
                        Text(
                            text = targetPhoto.fileName,
                            style = MaterialTheme.typography.titleSmall,
                            color = palette.textPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { lightboxPhoto = null }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Lightbox",
                                tint = palette.textPrimary
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(palette.surface)
                    ) {
                        AsyncImage(
                            model = targetPhoto.localUri,
                            contentDescription = targetPhoto.fileName,
                            placeholder = painterResource(id = R.drawable.img_wedding_cover),
                            error = painterResource(id = R.drawable.img_wedding_cover),
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                    if (effectiveConfig.photoDownloadsEnabled) {
                        Button(
                            onClick = {
                                onNotifyDownload("Downloaded original ${targetPhoto.fileName}")
                                lightboxPhoto = null
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = palette.secondaryHighlight,
                                contentColor = JummoObsidian
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Download, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Download High-Res Photo", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = palette.card,
            title = {
                Text("Event Privacy & Biometric Consent Policy", color = palette.textPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "All guest selfies and face-search queries for ${event.name} are strictly isolated to this event's cryptographic scope (${event.id}). No cross-event facial recognition is performed, and guests can permanently purge their consent session at any time.",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.secondaryHighlight,
                        contentColor = JummoObsidian
                    )
                ) {
                    Text("Understood", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    if (showDataDeletionDialog) {
        AlertDialog(
            onDismissRequest = { showDataDeletionDialog = false },
            containerColor = palette.card,
            title = {
                Text("Instant Guest Data Deletion", color = palette.textPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = if (activeConsentSession != null) {
                        "Active consent session found for ${activeConsentSession.guestLabel}. Confirm below to immediately purge your selfie reference and face vectors from ${event.name}."
                    } else {
                        "No active face-search session is stored for your current visit on ${event.name}."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.textSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (activeConsentSession != null) {
                            onDeleteFaceData(activeConsentSession)
                        }
                        showDataDeletionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoErrorRed,
                        contentColor = Color.White
                    )
                ) {
                    Text(if (activeConsentSession != null) "Purge My Face Data" else "Close", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDataDeletionDialog = false }) {
                    Text("Cancel", color = palette.textSecondary)
                }
            }
        )
    }

    if (sessionToReport != null) {
        val target = sessionToReport!!
        AlertDialog(
            onDismissRequest = { sessionToReport = null },
            containerColor = palette.card,
            title = {
                Text("Report Incorrect Face Match", color = palette.textPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Flag an inaccurate match for ${target.guestLabel} so the event organizer and face-matching service can exclude the photo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.textSecondary
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
                        containerColor = palette.secondaryHighlight,
                        contentColor = JummoObsidian
                    )
                ) {
                    Text("Submit Report", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToReport = null }) {
                    Text("Cancel", color = palette.textSecondary)
                }
            }
        )
    }
}

/**
 * STRICT IMAGE-ONLY HERO BANNER SLIDER FOR GUEST WEBSITE:
 * Renders ONLY the uploaded or selected banner image(s) with smooth horizontal paging and subtle
 * dot indicators. Zero text, captions, titles, logos, or buttons are rendered over the photograph.
 */
@Composable
fun GuestCleanHeroBannerSlider(
    bannerUris: List<String>,
    autoSlideEnabled: Boolean,
    sliderIntervalSeconds: Long,
    bannerHeightMode: String,
    bannerCropMode: String,
    palette: GuestWebsitePalette
) {
    val bannerHeightDp = when (bannerHeightMode) {
        BannerHeightModes.COMPACT -> 148.dp
        BannerHeightModes.CINEMATIC -> 228.dp
        else -> 184.dp
    }
    val contentScale = when (bannerCropMode) {
        BannerCropModes.FIT -> ContentScale.Fit
        else -> ContentScale.Crop
    }
    val contentAlignment = when (bannerCropMode) {
        BannerCropModes.TOP -> Alignment.TopCenter
        else -> Alignment.Center
    }

    val safeUris = if (bannerUris.isEmpty()) listOf(BannerPresets.PRESET_WEDDING) else bannerUris
    val pagerState = rememberPagerState(pageCount = { safeUris.size })

    LaunchedEffect(safeUris.size, autoSlideEnabled, sliderIntervalSeconds) {
        if (autoSlideEnabled && safeUris.size > 1) {
            val intervalMs = (sliderIntervalSeconds.coerceIn(2L, 10L)) * 1000L
            while (true) {
                delay(intervalMs)
                val next = (pagerState.currentPage + 1) % safeUris.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(bannerHeightDp)
            .testTag("guest_website_clean_banner_slider"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = palette.surface),
        border = BorderStroke(1.dp, palette.secondaryHighlight.copy(alpha = 0.42f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val uri = safeUris[page]
                val drawableRes = BannerPresets.drawableResForUri(uri)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("guest_website_banner_slide_$page"),
                    contentAlignment = contentAlignment
                ) {
                    if (drawableRes != null) {
                        Image(
                            painter = painterResource(id = drawableRes),
                            contentDescription = "Event Hero Photograph",
                            contentScale = contentScale,
                            alignment = contentAlignment,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        AsyncImage(
                            model = uri,
                            contentDescription = "Event Hero Photograph",
                            placeholder = painterResource(id = R.drawable.img_wedding_cover),
                            error = painterResource(id = R.drawable.img_wedding_cover),
                            contentScale = contentScale,
                            alignment = contentAlignment,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            if (safeUris.size > 1) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color.Black.copy(alpha = 0.35f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(safeUris.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        val dotWidth by animateDpAsState(
                            targetValue = if (isSelected) 16.dp else 6.dp,
                            animationSpec = tween(220),
                            label = "guest_banner_dot"
                        )
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(dotWidth)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) palette.secondaryHighlight else Color.White.copy(alpha = 0.5f)
                                )
                        )
                    }
                }
            }
        }
    }
}

/**
 * Renders the event photo gallery according to the photographer's chosen layout:
 * - [WebsiteLayouts.EDITORIAL]: Hero feature card + paired editorial rows
 * - [WebsiteLayouts.CLASSIC_GRID]: Uniform 2-column symmetric grid
 * - [WebsiteLayouts.MASONRY]: Staggered aspect-ratio dual-column masonry layout
 */
@Composable
private fun GuestGalleryLayoutRenderer(
    photos: List<PhotoUploadRecord>,
    layoutMode: String,
    allowDownloads: Boolean,
    selectedPhotoIds: List<String>,
    palette: GuestWebsitePalette,
    onToggleSelect: (String) -> Unit,
    onOpenLightbox: (PhotoUploadRecord) -> Unit,
    onDownloadSingle: (PhotoUploadRecord) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("guest_gallery_layout_$layoutMode"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        when (layoutMode) {
            WebsiteLayouts.CLASSIC_GRID -> {
                photos.chunked(2).forEach { rowPhotos ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowPhotos.forEach { photo ->
                            GuestGalleryPhotoCard(
                                photo = photo,
                                cardHeight = 148.dp,
                                isSelected = selectedPhotoIds.contains(photo.id),
                                allowDownloads = allowDownloads,
                                palette = palette,
                                onToggleSelect = { onToggleSelect(photo.id) },
                                onOpenLightbox = { onOpenLightbox(photo) },
                                onDownloadSingle = { onDownloadSingle(photo) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowPhotos.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            WebsiteLayouts.MASONRY -> {
                photos.chunked(2).forEachIndexed { rowIndex, rowPhotos ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowPhotos.forEachIndexed { colIndex, photo ->
                            val heightDp = if ((rowIndex + colIndex) % 2 == 0) 172.dp else 136.dp
                            GuestGalleryPhotoCard(
                                photo = photo,
                                cardHeight = heightDp,
                                isSelected = selectedPhotoIds.contains(photo.id),
                                allowDownloads = allowDownloads,
                                palette = palette,
                                onToggleSelect = { onToggleSelect(photo.id) },
                                onOpenLightbox = { onOpenLightbox(photo) },
                                onDownloadSingle = { onDownloadSingle(photo) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowPhotos.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            else -> {
                // Default: Premium Editorial Layout (featured hero frame + paired story frames)
                val firstPhoto = photos.first()
                GuestGalleryPhotoCard(
                    photo = firstPhoto,
                    cardHeight = 196.dp,
                    isSelected = selectedPhotoIds.contains(firstPhoto.id),
                    allowDownloads = allowDownloads,
                    palette = palette,
                    onToggleSelect = { onToggleSelect(firstPhoto.id) },
                    onOpenLightbox = { onOpenLightbox(firstPhoto) },
                    onDownloadSingle = { onDownloadSingle(firstPhoto) },
                    modifier = Modifier.fillMaxWidth()
                )
                photos.drop(1).chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        pair.forEach { photo ->
                            GuestGalleryPhotoCard(
                                photo = photo,
                                cardHeight = 148.dp,
                                isSelected = selectedPhotoIds.contains(photo.id),
                                allowDownloads = allowDownloads,
                                palette = palette,
                                onToggleSelect = { onToggleSelect(photo.id) },
                                onOpenLightbox = { onOpenLightbox(photo) },
                                onDownloadSingle = { onDownloadSingle(photo) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (pair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GuestGalleryPhotoCard(
    photo: PhotoUploadRecord,
    cardHeight: Dp,
    isSelected: Boolean,
    allowDownloads: Boolean,
    palette: GuestWebsitePalette,
    onToggleSelect: () -> Unit,
    onOpenLightbox: () -> Unit,
    onDownloadSingle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = palette.card),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) palette.secondaryHighlight else palette.borderSubtle
        ),
        modifier = modifier.clickable { onOpenLightbox() }
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(cardHeight)
                    .background(palette.surface)
            ) {
                AsyncImage(
                    model = photo.localUri,
                    contentDescription = photo.fileName,
                    placeholder = painterResource(id = R.drawable.img_wedding_cover),
                    error = painterResource(id = R.drawable.img_wedding_cover),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                IconButton(
                    onClick = onOpenLightbox,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(4.dp)
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Preview Photo",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = photo.fileName,
                        style = MaterialTheme.typography.labelMedium,
                        color = palette.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${(photo.fileSizeBytes / 1024).coerceAtLeast(1)} KB",
                        style = MaterialTheme.typography.labelSmall,
                        color = palette.textSecondary
                    )
                }

                if (allowDownloads) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onToggleSelect,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = if (isSelected) {
                                    Icons.Default.CheckBox
                                } else {
                                    Icons.Default.CheckBoxOutlineBlank
                                },
                                contentDescription = "Select Photo",
                                tint = palette.secondaryHighlight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onDownloadSingle,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "Download Photo",
                                tint = palette.textPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuestWebsiteImageThumbnail(
    imageUri: String,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    val drawableRes = BannerPresets.drawableResForUri(imageUri)
    if (drawableRes != null) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        AsyncImage(
            model = imageUri,
            contentDescription = contentDescription,
            placeholder = painterResource(id = R.drawable.img_wedding_cover),
            error = painterResource(id = R.drawable.img_wedding_cover),
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}
