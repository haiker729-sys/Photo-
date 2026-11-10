package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.data.model.BannerPresets
import com.example.data.model.BannerSliderConfig
import com.example.data.model.BannerVisibilityModes
import com.example.data.model.HomeBanner
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
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import com.example.ui.theme.JummoVioletPrimary
import com.example.ui.theme.JummoWarningAmber
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

private val DATE_FORMAT = SimpleDateFormat("MMM d, yyyy", Locale.US)

/**
 * Advanced Banner Slider Management Section inside the Super Admin Dashboard.
 *
 * Core Capabilities:
 * A. Banner Overview: Metrics for Total, Active, Scheduled, Expired, and Draft Banners + Filter Tabs.
 * B. Global Slider Settings: Auto-slide toggle, 2s/3s/4s/5s/7s slide duration, Infinite loop toggle.
 * C. Banner Actions: Add New Banner modal, Upload Multiple Images from Gallery, Quick Preset Add.
 * D. Banner List: 16:9 thumbnail, admin-only title, status badge, priority badge, schedule, visibility,
 *    Move Up / Down, Edit, Preview, Replace, and Delete with confirmation.
 * E. Add/Edit Banner Modal: Title, 16:9 preview & crop/fit, visibility modes, scheduling start/end dates.
 * F. Dedicated Preview Modal: True 16:9 simulation matching Photographer Home Screen (STRICTLY IMAGE-ONLY).
 */
@Composable
fun AdminBannerManagementSection(
    banners: List<HomeBanner>,
    sliderConfig: BannerSliderConfig = BannerSliderConfig(),
    onSaveSliderConfig: (BannerSliderConfig) -> Unit = {},
    onUploadMultipleBanners: (List<Uri>) -> Unit,
    onAddPresetBanner: (String) -> Unit,
    onSaveAdvancedBanner: (
        bannerId: String?,
        title: String,
        imageUri: String,
        visibility: String,
        targetPhotographerIds: List<String>,
        targetPlanIds: List<String>,
        startAt: Timestamp?,
        endAt: Timestamp?,
        isActive: Boolean,
        slideDurationSeconds: Long
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
    onToggleBannerActive: (HomeBanner) -> Unit,
    onReplaceBannerFromUri: (HomeBanner, Uri) -> Unit,
    onReplaceBannerWithPreset: (HomeBanner, String) -> Unit,
    onReorderBanners: (List<HomeBanner>) -> Unit,
    onUpdateSlideDuration: (Long) -> Unit,
    onDeleteBanner: (HomeBanner) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedBanners = remember(banners) {
        banners.sortedBy { it.displayOrder }
    }
    val now = remember { Timestamp.now() }

    // Status breakdown metrics
    val totalCount = sortedBanners.size
    val activeCount = remember(sortedBanners) {
        sortedBanners.count { it.computedStatus(now) == "Active" }
    }
    val scheduledCount = remember(sortedBanners) {
        sortedBanners.count { it.computedStatus(now) == "Scheduled" }
    }
    val expiredCount = remember(sortedBanners) {
        sortedBanners.count { it.computedStatus(now) == "Expired" }
    }
    val draftCount = remember(sortedBanners) {
        sortedBanners.count { it.computedStatus(now) == "Draft" }
    }

    var selectedFilterTab by remember { mutableStateOf("ALL") }
    val filteredBanners = remember(sortedBanners, selectedFilterTab) {
        when (selectedFilterTab) {
            "ACTIVE" -> sortedBanners.filter { it.computedStatus(now) == "Active" }
            "SCHEDULED" -> sortedBanners.filter { it.computedStatus(now) == "Scheduled" }
            "EXPIRED" -> sortedBanners.filter { it.computedStatus(now) == "Expired" }
            "DRAFT" -> sortedBanners.filter { it.computedStatus(now) == "Draft" }
            else -> sortedBanners
        }
    }

    // Modal states
    var bannerPendingReplace by remember { mutableStateOf<HomeBanner?>(null) }
    var bannerToEdit by remember { mutableStateOf<HomeBanner?>(null) }
    var isCreatingNewBanner by remember { mutableStateOf(false) }
    var bannerToPreview by remember { mutableStateOf<HomeBanner?>(null) }
    var bannerToDelete by remember { mutableStateOf<HomeBanner?>(null) }

    // Multiple banner upload picker from mobile gallery
    val multipleBannersLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        if (uris.isNotEmpty()) {
            onUploadMultipleBanners(uris)
        }
    }

    // Single banner image replacement picker
    val replaceBannerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        val target = bannerPendingReplace
        if (uri != null && target != null) {
            onReplaceBannerFromUri(target, uri)
        }
        bannerPendingReplace = null
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_banner_management_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(1.dp, JummoGoldBright.copy(alpha = 0.45f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title & Auto-Sync Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JummoGoldBright.copy(alpha = 0.16f))
                            .border(0.75.dp, JummoGoldBright.copy(alpha = 0.5f), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.ViewCarousel,
                            contentDescription = "Banner Management",
                            tint = JummoGoldBright,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Banner Slider Manager",
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Admin control • Zero text on Home Screen photographs",
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoTextSecondary
                        )
                    }
                }

                StatusBadgePill(
                    text = "$activeCount/$totalCount ACTIVE",
                    color = if (activeCount > 0) JummoSuccessGreen else JummoGoldBright
                )
            }

            // A. Banner Overview Metrics Grid (Total, Active, Scheduled, Expired, Draft)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "BANNER OVERVIEW",
                    style = MaterialTheme.typography.labelSmall,
                    color = JummoGoldBright,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_banner_overview_metrics"),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BannerMetricTile(
                        label = "Total",
                        count = totalCount,
                        accentColor = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    BannerMetricTile(
                        label = "Active",
                        count = activeCount,
                        accentColor = JummoSuccessGreen,
                        modifier = Modifier.weight(1f)
                    )
                    BannerMetricTile(
                        label = "Scheduled",
                        count = scheduledCount,
                        accentColor = JummoGoldBright,
                        modifier = Modifier.weight(1f)
                    )
                    BannerMetricTile(
                        label = "Expired",
                        count = expiredCount,
                        accentColor = JummoErrorRed,
                        modifier = Modifier.weight(1f)
                    )
                    BannerMetricTile(
                        label = "Draft",
                        count = draftCount,
                        accentColor = JummoVioletBright,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Filter Tabs (All, Active, Scheduled, Expired, Draft)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_banner_filter_tabs"),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    listOf("ALL", "ACTIVE", "SCHEDULED", "EXPIRED", "DRAFT").forEach { filterKey ->
                        val isSelected = selectedFilterTab == filterKey
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedFilterTab = filterKey }
                                .testTag("admin_banner_filter_$filterKey"),
                            color = if (isSelected) JummoGoldBright else JummoDeepPurple,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(
                                0.75.dp,
                                if (isSelected) JummoGoldBright else JummoBorderSubtle
                            )
                        ) {
                            Text(
                                text = filterKey,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) JummoObsidian else JummoTextSecondary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 5.dp)
                            )
                        }
                    }
                }
            }

            // B. Global Slider Settings Card
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = JummoDeepPurple.copy(alpha = 0.85f),
                border = BorderStroke(0.75.dp, JummoBorderSubtle),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_banner_slider_config_card")
            ) {
                Column(
                    modifier = Modifier.padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
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
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Auto Slide",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Switch(
                            checked = sliderConfig.autoSlideEnabled,
                            onCheckedChange = { enabled ->
                                onSaveSliderConfig(sliderConfig.copy(autoSlideEnabled = enabled))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JummoObsidian,
                                checkedTrackColor = JummoGoldBright,
                                uncheckedThumbColor = JummoTextSecondary,
                                uncheckedTrackColor = JummoCardPurple
                            ),
                            modifier = Modifier.testTag("admin_banner_auto_slide_toggle")
                        )
                    }

                    HorizontalDivider(color = JummoBorderSubtle.copy(alpha = 0.4f))

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
                                imageVector = Icons.Default.Timer,
                                contentDescription = null,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Slide Duration",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf(2L, 3L, 4L, 5L, 7L).forEach { sec ->
                                val selected = sliderConfig.slideDurationSeconds == sec
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable {
                                            onSaveSliderConfig(sliderConfig.copy(slideDurationSeconds = sec))
                                            onUpdateSlideDuration(sec)
                                        }
                                        .testTag("admin_banner_duration_${sec}s"),
                                    color = if (selected) JummoGoldBright else JummoCardPurple,
                                    border = BorderStroke(
                                        0.75.dp,
                                        if (selected) JummoGoldBright else JummoBorderSubtle
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "${sec}s",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (selected) JummoObsidian else JummoTextSecondary,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = JummoBorderSubtle.copy(alpha = 0.4f))

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
                                imageVector = Icons.Default.Loop,
                                contentDescription = null,
                                tint = JummoVioletBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Infinite Looping & Smooth Swipe",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Switch(
                            checked = sliderConfig.infiniteLoop,
                            onCheckedChange = { loop ->
                                onSaveSliderConfig(sliderConfig.copy(infiniteLoop = loop))
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JummoObsidian,
                                checkedTrackColor = JummoGoldBright,
                                uncheckedThumbColor = JummoTextSecondary,
                                uncheckedTrackColor = JummoCardPurple
                            ),
                            modifier = Modifier.testTag("admin_banner_infinite_loop_toggle")
                        )
                    }
                }
            }

            // Live Image-Only Carousel Preview
            Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                Text(
                    text = "LIVE HOME SLIDER SIMULATION (IMAGE-ONLY)",
                    style = MaterialTheme.typography.labelSmall,
                    color = JummoGoldBright,
                    fontWeight = FontWeight.Bold
                )
                val visibleBannersForHome = remember(sortedBanners) {
                    sortedBanners.filter { it.computedStatus(now) == "Active" }
                }
                HomeImageOnlyBannerCarousel(
                    activeBanners = visibleBannersForHome,
                    modifier = Modifier.testTag("admin_banner_live_preview")
                )
            }

            // C. Banner Actions Row (Add New Banner, Upload Banners, Quick Preset)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Primary Golden Button: Add New Banner (opens full configuration modal)
                Button(
                    onClick = { isCreatingNewBanner = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoGoldBright,
                        contentColor = JummoObsidian
                    ),
                    shape = RoundedCornerShape(11.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1.1f)
                        .height(38.dp)
                        .testTag("admin_add_new_banner_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Add Banner",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Upload Multiple Banners from Gallery
                Button(
                    onClick = {
                        multipleBannersLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoVioletPrimary,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(0.85.dp, JummoBorderGlow),
                    shape = RoundedCornerShape(11.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("admin_upload_banners_gallery_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AddPhotoAlternate,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Upload",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Add Studio Preset
                Button(
                    onClick = {
                        val nextPreset = BannerPresets.ALL[sortedBanners.size % BannerPresets.ALL.size]
                        onAddPresetBanner(nextPreset)
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoElevatedPurple,
                        contentColor = JummoGoldBright
                    ),
                    border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.45f)),
                    shape = RoundedCornerShape(11.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(38.dp)
                        .testTag("admin_add_studio_banner_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Collections,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Preset",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            HorizontalDivider(color = JummoBorderSubtle.copy(alpha = 0.6f))

            // D. Banner Cards List
            if (filteredBanners.isEmpty()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = JummoDeepPurple.copy(alpha = 0.6f),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_banners_empty_state")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = if (sortedBanners.isEmpty()) "No Home Banners Configured" else "No $selectedFilterTab Banners",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Add banners using the buttons above or select a different filter tab.",
                            style = MaterialTheme.typography.bodySmall,
                            color = JummoTextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.testTag("admin_banner_items_column")
                ) {
                    filteredBanners.forEachIndexed { index, banner ->
                        val globalIndex = sortedBanners.indexOfFirst { it.id == banner.id }.coerceAtLeast(index)
                        AdminBannerRowItem(
                            banner = banner,
                            index = globalIndex,
                            totalCount = sortedBanners.size,
                            currentStatus = banner.computedStatus(now),
                            onToggleActive = { onToggleBannerActive(banner) },
                            onMoveUp = {
                                if (globalIndex > 0) {
                                    val mutable = sortedBanners.toMutableList()
                                    val item = mutable.removeAt(globalIndex)
                                    mutable.add(globalIndex - 1, item)
                                    onReorderBanners(mutable)
                                }
                            },
                            onMoveDown = {
                                if (globalIndex < sortedBanners.lastIndex) {
                                    val mutable = sortedBanners.toMutableList()
                                    val item = mutable.removeAt(globalIndex)
                                    mutable.add(globalIndex + 1, item)
                                    onReorderBanners(mutable)
                                }
                            },
                            onEdit = { bannerToEdit = banner },
                            onPreview = { bannerToPreview = banner },
                            onReplaceGallery = {
                                bannerPendingReplace = banner
                                replaceBannerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            onReplaceStudioPreset = {
                                val currentIdx = BannerPresets.ALL.indexOf(banner.imageUri)
                                val nextPreset = BannerPresets.ALL[(currentIdx + 1).coerceAtLeast(0) % BannerPresets.ALL.size]
                                onReplaceBannerWithPreset(banner, nextPreset)
                            },
                            onDelete = { bannerToDelete = banner }
                        )
                    }
                }
            }
        }
    }

    // Modal E1: Add New Banner Dialog
    if (isCreatingNewBanner) {
        AddOrEditBannerDialog(
            banner = null,
            nextOrder = sortedBanners.size.toLong(),
            onDismiss = { isCreatingNewBanner = false },
            onSave = { title, imageUri, visibility, startAt, endAt, isActive, duration ->
                onSaveAdvancedBanner(
                    null,
                    title,
                    imageUri,
                    visibility,
                    emptyList(),
                    emptyList(),
                    startAt,
                    endAt,
                    isActive,
                    duration
                )
                isCreatingNewBanner = false
            }
        )
    }

    // Modal E2: Edit Existing Banner Dialog
    if (bannerToEdit != null) {
        val banner = bannerToEdit!!
        AddOrEditBannerDialog(
            banner = banner,
            nextOrder = banner.displayOrder,
            onDismiss = { bannerToEdit = null },
            onSave = { title, imageUri, visibility, startAt, endAt, isActive, duration ->
                onSaveAdvancedBanner(
                    banner.id,
                    title,
                    imageUri,
                    visibility,
                    banner.targetPhotographerIds,
                    banner.targetPlanIds,
                    startAt,
                    endAt,
                    isActive,
                    duration
                )
                bannerToEdit = null
            }
        )
    }

    // Modal F: Dedicated Banner Preview Dialog (16:9 Image-Only)
    if (bannerToPreview != null) {
        val banner = bannerToPreview!!
        DedicatedBannerPreviewDialog(
            banner = banner,
            onDismiss = { bannerToPreview = null }
        )
    }

    // Modal G: Delete Confirmation Dialog
    if (bannerToDelete != null) {
        val banner = bannerToDelete!!
        AlertDialog(
            onDismissRequest = { bannerToDelete = null },
            containerColor = JummoCardPurple,
            title = {
                Text(
                    text = "Delete Banner?",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Are you sure you want to delete \"${banner.title.ifBlank { "Banner #${banner.displayOrder + 1}" }}\"?",
                        color = JummoTextSecondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(0.75.dp, JummoBorderSubtle, RoundedCornerShape(10.dp))
                    ) {
                        BannerImageSurface(
                            imageUri = banner.imageUri,
                            contentDescription = "Banner to delete",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text(
                        text = "This will remove the banner from the photographer Home Slider immediately.",
                        color = JummoErrorRed,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteBanner(banner)
                        bannerToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoErrorRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("admin_confirm_delete_banner_button")
                ) {
                    Text("Delete Permanently", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(
                    onClick = { bannerToDelete = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoElevatedPurple,
                        contentColor = Color.White
                    )
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun BannerMetricTile(
    label: String,
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clip(RoundedCornerShape(10.dp)),
        color = JummoDeepPurple.copy(alpha = 0.8f),
        border = BorderStroke(0.65.dp, JummoBorderSubtle),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleSmall,
                color = accentColor,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = JummoTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdminBannerRowItem(
    banner: HomeBanner,
    index: Int,
    totalCount: Int,
    currentStatus: String,
    onToggleActive: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onEdit: () -> Unit,
    onPreview: () -> Unit,
    onReplaceGallery: () -> Unit,
    onReplaceStudioPreset: () -> Unit,
    onDelete: () -> Unit
) {
    var dragOffsetY by remember { mutableFloatStateOf(0f) }

    val statusColor = when (currentStatus) {
        "Active" -> JummoSuccessGreen
        "Scheduled" -> JummoGoldBright
        "Expired" -> JummoErrorRed
        else -> JummoTextMuted
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, dragOffsetY.roundToInt()) }
            .pointerInput(banner.id, index, totalCount) {
                detectDragGesturesAfterLongPress(
                    onDragEnd = {
                        if (dragOffsetY < -48f && index > 0) {
                            onMoveUp()
                        } else if (dragOffsetY > 48f && index < totalCount - 1) {
                            onMoveDown()
                        }
                        dragOffsetY = 0f
                    },
                    onDragCancel = {
                        dragOffsetY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        dragOffsetY = (dragOffsetY + dragAmount.y).coerceIn(-120f, 120f)
                    }
                )
            }
            .testTag("admin_banner_row_${banner.id}"),
        shape = RoundedCornerShape(14.dp),
        color = JummoDeepPurple.copy(alpha = 0.92f),
        border = BorderStroke(
            0.85.dp,
            if (currentStatus == "Active") JummoGoldBright.copy(alpha = 0.45f) else JummoBorderSubtle
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Drag Handle + 16:9 Image Thumbnail + Banner Admin Title & Meta
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.DragIndicator,
                        contentDescription = "Drag to reorder",
                        tint = JummoTextMuted,
                        modifier = Modifier.size(18.dp)
                    )

                    // Strictly Image-Only Banner Thumbnail (16:9 ratio)
                    Box(
                        modifier = Modifier
                            .width(96.dp)
                            .height(54.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .border(
                                0.75.dp,
                                if (currentStatus == "Active") JummoGoldBright else JummoBorderSubtle,
                                RoundedCornerShape(9.dp)
                            )
                            .clickable { onPreview() }
                    ) {
                        BannerImageSurface(
                            imageUri = banner.imageUri,
                            contentDescription = "Banner Preview ${index + 1}",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Surface(
                                color = JummoGoldBright.copy(alpha = 0.18f),
                                shape = CircleShape
                            ) {
                                Text(
                                    text = "#${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JummoGoldBright,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }

                            Text(
                                text = banner.title.ifBlank { "Banner #${index + 1}" },
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusBadgePill(
                                text = currentStatus.uppercase(),
                                color = statusColor
                            )

                            Text(
                                text = "• ${banner.visibility}",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextSecondary
                            )

                            Text(
                                text = "• ${banner.slideDurationSeconds}s",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextSecondary
                            )
                        }

                        // Schedule details
                        val scheduleText = when {
                            banner.startAt != null && banner.endAt != null ->
                                "${DATE_FORMAT.format(banner.startAt!!.toDate())} - ${DATE_FORMAT.format(banner.endAt!!.toDate())}"
                            banner.startAt != null ->
                                "Starts ${DATE_FORMAT.format(banner.startAt!!.toDate())}"
                            banner.endAt != null ->
                                "Expires ${DATE_FORMAT.format(banner.endAt!!.toDate())}"
                            else -> "Immediate / No Expiration"
                        }
                        Text(
                            text = scheduleText,
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoTextMuted,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Active / Inactive Switch
                Switch(
                    checked = banner.isActive,
                    onCheckedChange = { onToggleActive() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = JummoObsidian,
                        checkedTrackColor = JummoGoldBright,
                        uncheckedThumbColor = JummoTextSecondary,
                        uncheckedTrackColor = JummoCardPurple
                    ),
                    modifier = Modifier.testTag("admin_banner_toggle_${banner.id}")
                )
            }

            // Compact Action Bar: Move Up, Move Down, Edit, Preview, Replace, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Reorder controls
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(JummoCardPurple)
                            .border(0.75.dp, JummoBorderSubtle, RoundedCornerShape(7.dp))
                            .testTag("admin_banner_move_up_${banner.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Move Up",
                            tint = if (index > 0) Color.White else JummoTextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                    }

                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(JummoCardPurple)
                            .border(0.75.dp, JummoBorderSubtle, RoundedCornerShape(7.dp))
                            .testTag("admin_banner_move_down_${banner.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Move Down",
                            tint = if (index < totalCount - 1) Color.White else JummoTextMuted.copy(alpha = 0.4f),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Edit, Preview, Replace, Delete
                Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                    // Preview Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .clickable { onPreview() }
                            .testTag("admin_banner_preview_${banner.id}"),
                        color = JummoElevatedPurple,
                        border = BorderStroke(0.75.dp, JummoBorderSubtle),
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Preview Banner",
                                tint = JummoGoldBright,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Preview",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Edit Button
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .clickable { onEdit() }
                            .testTag("admin_banner_edit_${banner.id}"),
                        color = JummoCardPurple,
                        border = BorderStroke(0.75.dp, JummoBorderSubtle),
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Banner",
                                tint = JummoVioletBright,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Edit",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Swap Shot preset
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .clickable { onReplaceStudioPreset() }
                            .testTag("admin_banner_replace_${banner.id}"),
                        color = JummoElevatedPurple,
                        border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.45f)),
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        Text(
                            text = "Swap",
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoGoldBright,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp)
                        )
                    }

                    // Replace Gallery
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(7.dp))
                            .clickable { onReplaceGallery() }
                            .testTag("admin_banner_replace_gallery_${banner.id}"),
                        color = JummoCardPurple,
                        border = BorderStroke(0.75.dp, JummoBorderSubtle),
                        shape = RoundedCornerShape(7.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FindReplace,
                                contentDescription = "Replace Image",
                                tint = JummoVioletBright,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Replace",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Delete Button
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(JummoErrorRed.copy(alpha = 0.16f))
                            .border(0.75.dp, JummoErrorRed.copy(alpha = 0.45f), RoundedCornerShape(7.dp))
                            .testTag("admin_banner_delete_${banner.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Banner",
                            tint = JummoErrorRed,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Add or Edit Banner Modal Dialog:
 * Allows configuring admin title, selecting 16:9 image, setting visibility mode,
 * schedule start/end timestamps, active status, and custom slide duration.
 */
@Composable
private fun AddOrEditBannerDialog(
    banner: HomeBanner?,
    nextOrder: Long,
    onDismiss: () -> Unit,
    onSave: (
        title: String,
        imageUri: String,
        visibility: String,
        startAt: Timestamp?,
        endAt: Timestamp?,
        isActive: Boolean,
        duration: Long
    ) -> Unit
) {
    var title by remember { mutableStateOf(banner?.title ?: "Studio Showcase #${nextOrder + 1}") }
    var imageUri by remember { mutableStateOf(banner?.imageUri ?: BannerPresets.PRESET_STUDIO) }
    var visibility by remember { mutableStateOf(banner?.visibility ?: BannerVisibilityModes.ALL) }
    var isActive by remember { mutableStateOf(banner?.isActive ?: true) }
    var slideDuration by remember { mutableLongStateOf(banner?.slideDurationSeconds ?: 5L) }

    // Schedule options
    var isScheduled by remember {
        mutableStateOf(banner?.startAt != null || banner?.endAt != null)
    }
    var scheduleOption by remember { mutableIntStateOf(if (banner?.startAt != null) 1 else 0) } // 0: Immediate, 1: Tomorrow (+24h)
    var expiryOption by remember { mutableIntStateOf(if (banner?.endAt != null) 1 else 0) } // 0: Never, 1: 7 Days, 2: 30 Days

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { pickedUri ->
        if (pickedUri != null) {
            imageUri = pickedUri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JummoCardPurple,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = if (banner == null) Icons.Default.Add else Icons.Default.Edit,
                    contentDescription = null,
                    tint = JummoGoldBright,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = if (banner == null) "Add New Home Banner" else "Edit Banner #${banner.displayOrder + 1}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Admin Title Input
                Text(
                    text = "BANNER NAME (ADMIN ONLY)",
                    style = MaterialTheme.typography.labelSmall,
                    color = JummoGoldBright,
                    fontWeight = FontWeight.Bold
                )
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("e.g. Royal Wedding Highlights", color = JummoTextMuted) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = JummoGoldBright,
                        unfocusedBorderColor = JummoBorderSubtle,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_banner_title_input")
                )
                Text(
                    text = "Note: Banner name is for admin records only. The photographer Home Screen renders STRICTLY the image with zero text overlay.",
                    style = MaterialTheme.typography.labelSmall,
                    color = JummoTextSecondary
                )

                HorizontalDivider(color = JummoBorderSubtle.copy(alpha = 0.5f))

                // 16:9 Image Preview & Selection
                Text(
                    text = "PHOTOGRAPH (TARGET 16:9)",
                    style = MaterialTheme.typography.labelSmall,
                    color = JummoGoldBright,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, JummoGoldBright.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .testTag("admin_banner_edit_image_preview")
                ) {
                    BannerImageSurface(
                        imageUri = imageUri,
                        contentDescription = "Banner Image Preview",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            imagePickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JummoGoldBright,
                            contentColor = JummoObsidian
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f).testTag("admin_banner_pick_image_button")
                    ) {
                        Text("Pick from Gallery", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            val cur = BannerPresets.ALL.indexOf(imageUri)
                            imageUri = BannerPresets.ALL[(cur + 1).coerceAtLeast(0) % BannerPresets.ALL.size]
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JummoElevatedPurple,
                            contentColor = Color.White
                        ),
                        border = BorderStroke(0.75.dp, JummoBorderSubtle),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.weight(1f).testTag("admin_banner_cycle_preset_button")
                    ) {
                        Text("Cycle Studio Preset", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = JummoBorderSubtle.copy(alpha = 0.5f))

                // Visibility Selector
                Text(
                    text = "VISIBILITY AUDIENCE",
                    style = MaterialTheme.typography.labelSmall,
                    color = JummoGoldBright,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    listOf(
                        BannerVisibilityModes.ALL to "All",
                        BannerVisibilityModes.PLAN to "Pro Plan",
                        BannerVisibilityModes.DRAFT to "Draft"
                    ).forEach { (mode, label) ->
                        val selected = visibility == mode
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { visibility = mode }
                                .testTag("admin_visibility_mode_$mode"),
                            color = if (selected) JummoGoldBright else JummoDeepPurple,
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(0.75.dp, if (selected) JummoGoldBright else JummoBorderSubtle)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) JummoObsidian else JummoTextSecondary,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = JummoBorderSubtle.copy(alpha = 0.5f))

                // Scheduling Options
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
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = JummoGoldBright,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Schedule Banner",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Switch(
                        checked = isScheduled,
                        onCheckedChange = { isScheduled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JummoObsidian,
                            checkedTrackColor = JummoGoldBright,
                            uncheckedThumbColor = JummoTextSecondary,
                            uncheckedTrackColor = JummoCardPurple
                        ),
                        modifier = Modifier.testTag("admin_banner_schedule_toggle")
                    )
                }

                if (isScheduled) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "START TIMING",
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoTextSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Immediate" to 0, "Tomorrow (+24h)" to 1).forEach { (lbl, opt) ->
                                val sel = scheduleOption == opt
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { scheduleOption = opt },
                                    color = if (sel) JummoVioletPrimary else JummoDeepPurple,
                                    border = BorderStroke(0.75.dp, if (sel) JummoGoldBright else JummoBorderSubtle),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = lbl,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "EXPIRATION",
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoTextSecondary
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("No Expiry" to 0, "7 Days" to 1, "30 Days" to 2).forEach { (lbl, opt) ->
                                val sel = expiryOption == opt
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { expiryOption = opt },
                                    color = if (sel) JummoVioletPrimary else JummoDeepPurple,
                                    border = BorderStroke(0.75.dp, if (sel) JummoGoldBright else JummoBorderSubtle),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = lbl,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                HorizontalDivider(color = JummoBorderSubtle.copy(alpha = 0.5f))

                // Active Switch
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Status",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = isActive,
                        onCheckedChange = { isActive = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = JummoObsidian,
                            checkedTrackColor = JummoGoldBright,
                            uncheckedThumbColor = JummoTextSecondary,
                            uncheckedTrackColor = JummoCardPurple
                        ),
                        modifier = Modifier.testTag("admin_banner_edit_active_toggle")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val nowMillis = System.currentTimeMillis()
                    val calculatedStartAt = if (isScheduled && scheduleOption == 1) {
                        Timestamp(Date(nowMillis + 24 * 3600 * 1000L))
                    } else if (isScheduled) {
                        Timestamp(Date(nowMillis))
                    } else {
                        null
                    }

                    val calculatedEndAt = if (isScheduled && expiryOption == 1) {
                        Timestamp(Date(nowMillis + 7 * 24 * 3600 * 1000L))
                    } else if (isScheduled && expiryOption == 2) {
                        Timestamp(Date(nowMillis + 30 * 24 * 3600 * 1000L))
                    } else {
                        null
                    }

                    onSave(
                        title.trim(),
                        imageUri.trim(),
                        visibility,
                        calculatedStartAt,
                        calculatedEndAt,
                        isActive,
                        slideDuration
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = JummoGoldBright,
                    contentColor = JummoObsidian
                ),
                modifier = Modifier.testTag("admin_save_banner_button")
            ) {
                Text("Save Banner", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JummoElevatedPurple,
                    contentColor = Color.White
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

/**
 * Dedicated Banner Preview Dialog:
 * Renders the banner inside a 16:9 aspect-ratio frame EXACTLY as seen by photographers on Home Screen.
 * Strictly zero text, zero captions, zero watermark overlays on the photo surface.
 * Shows admin metadata audit beneath the card.
 */
@Composable
private fun DedicatedBannerPreviewDialog(
    banner: HomeBanner,
    onDismiss: () -> Unit
) {
    val now = remember { Timestamp.now() }
    val status = banner.computedStatus(now)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = JummoObsidian,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Home Slider Simulation",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "16:9 • Strictly Image-Only Surface",
                        color = JummoGoldBright,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Simulated 16:9 Home Screen Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(154.dp)
                        .testTag("dedicated_banner_preview_surface"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(1.dp, JummoGoldBright.copy(alpha = 0.5f))
                ) {
                    BannerImageSurface(
                        imageUri = banner.imageUri,
                        contentDescription = "Simulated Home Banner",
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // Admin Metadata Audit Footer (shown only inside admin dialog)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = JummoDeepPurple,
                    border = BorderStroke(0.65.dp, JummoBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "ADMIN METADATA AUDIT",
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoGoldBright,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Name:", style = MaterialTheme.typography.labelSmall, color = JummoTextSecondary)
                            Text(text = banner.title.ifBlank { "Home Banner" }, style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Status:", style = MaterialTheme.typography.labelSmall, color = JummoTextSecondary)
                            Text(text = status, style = MaterialTheme.typography.labelSmall, color = if (status == "Active") JummoSuccessGreen else JummoGoldBright, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Audience:", style = MaterialTheme.typography.labelSmall, color = JummoTextSecondary)
                            Text(text = banner.visibility, style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Display Order:", style = MaterialTheme.typography.labelSmall, color = JummoTextSecondary)
                            Text(text = "#${banner.displayOrder + 1}", style = MaterialTheme.typography.labelSmall, color = JummoGoldBright, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Slide Duration:", style = MaterialTheme.typography.labelSmall, color = JummoTextSecondary)
                            Text(text = "${banner.slideDurationSeconds} seconds", style = MaterialTheme.typography.labelSmall, color = Color.White)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = JummoGoldBright,
                    contentColor = JummoObsidian
                )
            ) {
                Text("Done", fontWeight = FontWeight.Bold)
            }
        }
    )
}
