package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import com.example.data.model.BannerPresets
import com.example.data.model.HomeBanner
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun HomeDashboardScreen(
    profile: PhotographerProfile?,
    events: List<PhotoEvent>,
    uploads: List<PhotoUploadRecord>,
    banners: List<HomeBanner> = emptyList(),
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

    val activeBanners = remember(banners) {
        banners.filter { it.isActive }.sortedBy { it.displayOrder }
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
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // 1. Compact iPhone-Style Luxury Studio Header
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(9.dp)
                        ) {
                            JummoLuxuryCameraLogo(
                                size = 36.dp,
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
                            horizontalArrangement = Arrangement.spacedBy(7.dp)
                        ) {
                            // Admin Dashboard Quick Access Button
                            IconButton(
                                onClick = onOpenAdminDashboard,
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(JummoCardPurple)
                                    .border(0.85.dp, JummoGoldBright.copy(alpha = 0.45f), CircleShape)
                                    .testTag("home_admin_dashboard_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = "Open Admin Dashboard",
                                    tint = JummoGoldBright,
                                    modifier = Modifier.size(17.dp)
                                )
                            }

                            // Notification / Queue Bell Button with Badge
                            Box {
                                IconButton(
                                    onClick = onNavigateToUploadsTab,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(JummoCardPurple)
                                        .border(0.85.dp, JummoBorderSubtle, CircleShape)
                                        .testTag("home_notification_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.NotificationsNone,
                                        contentDescription = "Upload Queue Notifications",
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                if (pendingPhotos > 0L || failedUploads > 0L) {
                                    Box(
                                        modifier = Modifier
                                            .size(14.dp)
                                            .clip(CircleShape)
                                            .background(JummoErrorRed)
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
                                    .size(36.dp)
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

                    // Photographer Welcome & Studio Subtitle + Auto Sync Indicator
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    text = "Welcome, $displayName",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = JummoTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "👋",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            }
                            Text(
                                text = studioSubtitle,
                                style = MaterialTheme.typography.labelMedium,
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

            // 2. Premium Image-Only Banner Slider (STRICTLY IMAGE-ONLY — Zero Text/Captions/Logos/Buttons Over Image)
            item {
                HomeImageOnlyBannerCarousel(
                    activeBanners = activeBanners
                )
            }

            // 3. Compact Side-by-Side Quick Actions: Create Event & Open Camera
            item {
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
                        shape = RoundedCornerShape(13.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("home_create_event_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(17.dp)
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
                        shape = RoundedCornerShape(13.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .testTag("home_quick_camera_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Open Camera",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 4. Compact Dashboard Statistics (Total Events, Total Photos, Uploaded, Pending, Failed)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        MetricStatCard(
                            title = "Total Events",
                            value = totalEvents.toString(),
                            icon = Icons.Default.Event,
                            accentColor = JummoVioletBright,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Total Photos",
                            value = totalPhotos.toString(),
                            icon = Icons.Default.PhotoLibrary,
                            accentColor = JummoGoldBright,
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
                        horizontalArrangement = Arrangement.spacedBy(7.dp)
                    ) {
                        MetricStatCard(
                            title = "Pending",
                            value = pendingPhotos.toString(),
                            icon = Icons.Default.Schedule,
                            accentColor = JummoWarningAmber,
                            modifier = Modifier.weight(1f)
                        )
                        MetricStatCard(
                            title = "Failed",
                            value = failedUploads.toString(),
                            icon = Icons.Default.ErrorOutline,
                            accentColor = JummoErrorRed,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 5. Compact Google Drive Real Status Card & Local Storage Strip
            item {
                Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
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
                                        imageVector = if (driveConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                                        contentDescription = "Google Drive Status",
                                        tint = if (driveConnected) JummoSuccessGreen else JummoGoldBright,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Google Drive",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = if (driveConnected) "• Connected" else "• Not Connected",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (driveConnected) JummoSuccessGreen else JummoTextMuted,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                    }
                                    Text(
                                        text = "${storageMetrics.formatBytes(storageMetrics.combinedManagedBytes)} used • /${profile?.driveFolderName ?: "Jummo_Events_Cloud"}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JummoTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    val syncRatio = if (totalPhotos > 0L) {
                                        (uploadedPhotos.toFloat() / totalPhotos.toFloat()).coerceIn(0.05f, 1f)
                                    } else {
                                        0f
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    LinearProgressIndicator(
                                        progress = { syncRatio },
                                        color = if (driveConnected) JummoSuccessGreen else JummoGoldBright,
                                        trackColor = JummoDeepPurple,
                                        modifier = Modifier
                                            .width(136.dp)
                                            .height(5.dp)
                                            .clip(RoundedCornerShape(50))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(9.dp))
                                    .clickable { onOpenDriveSettings() }
                                    .testTag("drive_action_button"),
                                color = if (driveConnected) {
                                    JummoElevatedPurple
                                } else {
                                    JummoGoldBright
                                },
                                border = BorderStroke(
                                    0.75.dp,
                                    if (driveConnected) JummoBorderSubtle else JummoGoldBright
                                ),
                                shape = RoundedCornerShape(9.dp)
                            ) {
                                Text(
                                    text = if (driveConnected) "Manage" else "Connect",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (driveConnected) Color.White else JummoObsidian,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    // Compact Local Storage Strip
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("storage_usage_card"),
                        shape = RoundedCornerShape(13.dp),
                        color = JummoCardPurple.copy(alpha = 0.92f),
                        border = BorderStroke(0.75.dp, JummoBorderSubtle)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
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
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Text(
                                    text = "${storageMetrics.localPhotoFileCount} Local Originals • ${storageMetrics.formatBytes(storageMetrics.combinedManagedBytes)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = JummoTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            StatusBadgePill(
                                text = "Originals Preserved",
                                color = JummoSuccessGreen
                            )
                        }
                    }
                }
            }

            // 6. Recent Events Section
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

/**
 * STRICT IMAGE-ONLY BANNER CAROUSEL:
 * Renders ONLY the uploaded/configured banner photograph inside the rounded luxury frame.
 * Absolutely NO text, NO captions, NO titles, NO logos, NO buttons, NO promotional overlays
 * are drawn on top of the banner image.
 */
@Composable
fun HomeImageOnlyBannerCarousel(
    activeBanners: List<HomeBanner>,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val slideDurationSeconds = remember(activeBanners) {
        (activeBanners.firstOrNull()?.slideDurationSeconds ?: 4L).coerceIn(1L, 30L)
    }
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { activeBanners.size.coerceAtLeast(1) }
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("home_banner_carousel_section"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(7.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(154.dp)
                .testTag("home_banner_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
            border = BorderStroke(1.dp, JummoGoldBright.copy(alpha = 0.42f))
        ) {
            if (activeBanners.isEmpty()) {
                // Neutral studio photography placeholder — strictly image-only with zero text overlays
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("home_banner_empty_placeholder")
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_studio),
                        contentDescription = "Studio Banner Placeholder",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            } else {
                LaunchedEffect(activeBanners.size, slideDurationSeconds) {
                    if (activeBanners.size > 1) {
                        while (true) {
                            delay(slideDurationSeconds * 1000L)
                            val nextPage = (pagerState.currentPage + 1) % activeBanners.size
                            pagerState.animateScrollToPage(nextPage)
                        }
                    }
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("home_banner_pager")
                ) { page ->
                    val banner = activeBanners[page]
                    val slideTag = if (banner.id.isNotBlank()) {
                        "home_banner_slide_${banner.id}"
                    } else {
                        "home_banner_slide_$page"
                    }
                    BannerImageSurface(
                        imageUri = banner.imageUri,
                        contentDescription = "Home Banner ${page + 1}",
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag(slideTag)
                    )
                }
            }
        }

        // Subtle compact pagination dots outside/below the image frame
        if (activeBanners.size > 1) {
            Row(
                modifier = Modifier.testTag("home_banner_indicators"),
                horizontalArrangement = Arrangement.spacedBy(5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                activeBanners.forEachIndexed { index, banner ->
                    val isSelected = pagerState.currentPage == index
                    val dotWidth by animateDpAsState(
                        targetValue = if (isSelected) 16.dp else 6.dp,
                        animationSpec = tween(durationMillis = 200),
                        label = "banner_dot_width"
                    )
                    val dotColor by animateColorAsState(
                        targetValue = if (isSelected) {
                            JummoGoldBright
                        } else {
                            JummoVioletBright.copy(alpha = 0.45f)
                        },
                        animationSpec = tween(durationMillis = 200),
                        label = "banner_dot_color"
                    )
                    Box(
                        modifier = Modifier
                            .height(5.dp)
                            .width(dotWidth)
                            .clip(RoundedCornerShape(50))
                            .background(dotColor)
                            .clickable {
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(index)
                                }
                            }
                            .testTag("home_banner_dot_${banner.id.ifBlank { index.toString() }}")
                    )
                }
            }
        }
    }
}

/**
 * Pure photographic surface for a HomeBanner — renders ONLY the photograph with ContentScale.Crop
 * and zero text or UI overlays.
 */
@Composable
fun BannerImageSurface(
    imageUri: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null
) {
    val fallbackPainter = when (imageUri) {
        BannerPresets.PRESET_FESTIVAL -> painterResource(id = R.drawable.img_festival_cover)
        BannerPresets.PRESET_STUDIO -> painterResource(id = R.drawable.img_hero_studio)
        BannerPresets.PRESET_LOGIN_HERO -> painterResource(id = R.drawable.img_hero_studio)
        else -> painterResource(id = R.drawable.img_wedding_cover)
    }

    if (imageUri.startsWith("preset://") || imageUri.isBlank()) {
        Image(
            painter = fallbackPainter,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    } else {
        AsyncImage(
            model = imageUri,
            contentDescription = contentDescription,
            placeholder = fallbackPainter,
            error = fallbackPainter,
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
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
                    .size(68.dp)
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
                                .clickable { onOpenQr() }
                                .testTag("recent_event_qr_${event.id}"),
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
                                .clickable { onShootCamera() }
                                .testTag("recent_event_camera_${event.id}"),
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
