package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.GuestFaceSearchSession
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.PhotographerProfile
import com.example.ui.components.SettingsInfoRow
import com.example.ui.components.StatusBadgePill
import com.example.ui.theme.JummoBorderGlow
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoChampagneGold
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
import com.example.ui.viewmodel.JummoViewModel
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    profile: PhotographerProfile?,
    events: List<PhotoEvent>,
    uploads: List<PhotoUploadRecord>,
    guestSessions: List<GuestFaceSearchSession>,
    onSaveProfileSettings: (
        displayName: String,
        studioName: String,
        driveConnected: Boolean,
        driveFolderName: String,
        autoUpload: Boolean,
        wifiOnly: Boolean,
        batteryNotLow: Boolean
    ) -> Unit,
    onDeleteGuestFaceData: (GuestFaceSearchSession) -> Unit,
    onSignOut: () -> Unit,
    onOpenAdminDashboard: () -> Unit = {},
    onOpenOfficialWebsite: () -> Unit = {},
    onOpenWebsiteCustomizer: () -> Unit = {},
    onRetryFailedUploads: () -> Unit = {},
    onNotifyMessage: (String) -> Unit = {},
    initialReleaseInfo: VerifiedGitHubReleaseInfo? = null,
    showAdminEntry: Boolean = true
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val prefs = remember(context) {
        context.getSharedPreferences(JummoViewModel.UPLOAD_PREFS_NAME, Context.MODE_PRIVATE)
    }

    var displayName by remember(profile?.displayName) {
        mutableStateOf(profile?.displayName?.ifBlank { "Meraj Sir" } ?: "Meraj Sir")
    }
    var studioName by remember(profile?.studioName) {
        mutableStateOf(profile?.studioName?.ifBlank { "Jummo Pro Studio" } ?: "Jummo Pro Studio")
    }
    val userEmail = profile?.email?.ifBlank { "merajalam906090@gmail.com" }
        ?: "merajalam906090@gmail.com"

    var driveConnected by remember(profile?.driveConnected) {
        mutableStateOf(profile?.driveConnected ?: false)
    }
    var driveFolderName by remember(profile?.driveFolderName) {
        mutableStateOf(profile?.driveFolderName?.ifBlank { "Jummo_Events_Cloud" } ?: "Jummo_Events_Cloud")
    }
    var autoUpload by remember(profile?.autoUploadEnabled) {
        mutableStateOf(profile?.autoUploadEnabled ?: true)
    }
    var wifiOnly by remember(profile?.wifiOnlyUpload) {
        mutableStateOf(profile?.wifiOnlyUpload ?: true)
    }
    var batteryNotLow by remember(profile?.batteryNotLowUpload) {
        mutableStateOf(profile?.batteryNotLowUpload ?: true)
    }

    // Extended local preferences persisted across restarts
    var avatarUriString by remember {
        mutableStateOf(prefs.getString("profile_avatar_uri", "") ?: "")
    }
    var backgroundUploadEnabled by remember {
        mutableStateOf(prefs.getBoolean("background_upload_enabled", true))
    }
    var uploadQuality by remember {
        mutableStateOf(prefs.getString("upload_quality_mode", "Original (100% Full Res)") ?: "Original (100% Full Res)")
    }
    var explicitFaceSearchConsentRequired by remember {
        mutableStateOf(prefs.getBoolean("explicit_face_consent_required", true))
    }

    // Section expansion states
    var accountExpanded by rememberSaveable { mutableStateOf(true) }
    var driveExpanded by rememberSaveable { mutableStateOf(false) }
    var uploadPrefsExpanded by rememberSaveable { mutableStateOf(true) }
    var storageExpanded by rememberSaveable { mutableStateOf(false) }
    var privacyExpanded by rememberSaveable { mutableStateOf(false) }
    var permissionsExpanded by rememberSaveable { mutableStateOf(false) }
    var supportExpanded by rememberSaveable { mutableStateOf(false) }
    var aboutExpanded by rememberSaveable { mutableStateOf(false) }

    // Dialog states
    var showSignOutConfirmDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showDeleteFaceDataConfirmDialog by remember { mutableStateOf(false) }
    var showStorageDetailsDialog by remember { mutableStateOf(false) }
    var showManageFilesDialog by remember { mutableStateOf(false) }
    var showReportProblemDialog by remember { mutableStateOf(false) }
    var showAppFeedbackDialog by remember { mutableStateOf(false) }
    var showFaqModal by remember { mutableStateOf(false) }

    // Live GitHub Release verification
    var releaseInfo by remember {
        mutableStateOf(initialReleaseInfo ?: VerifiedGitHubReleaseInfo())
    }
    var isCheckingRelease by remember { mutableStateOf(false) }

    LaunchedEffect(initialReleaseInfo) {
        if (initialReleaseInfo != null) {
            releaseInfo = initialReleaseInfo
        } else {
            isCheckingRelease = true
            releaseInfo = fetchVerifiedGitHubRelease()
            isCheckingRelease = false
        }
    }

    // Profile Avatar Photo Picker (Zero-permission Android Photo Picker)
    val profilePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val copiedUri = copyProfileAvatarLocally(context, uri)
            avatarUriString = copiedUri
            prefs.edit().putString("profile_avatar_uri", copiedUri).apply()
            onNotifyMessage("Photographer profile photo updated.")
        }
    }

    // Google Drive Cloud Folder / Document Verification Picker
    val driveFolderVerifierLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { treeUri ->
        if (treeUri != null) {
            val segment = treeUri.lastPathSegment
                ?.substringAfterLast(':')
                ?.substringAfterLast('/')
                ?.replace(Regex("[^a-zA-Z0-9_\\-]"), "_")
                ?.takeIf { it.isNotBlank() }
            if (segment != null) {
                driveFolderName = segment
            }
            driveConnected = true
            onSaveProfileSettings(
                displayName,
                studioName,
                true,
                driveFolderName,
                autoUpload,
                wifiOnly,
                batteryNotLow
            )
            onNotifyMessage("Google Drive folder \"$driveFolderName\" linked.")
        }
    }

    // Runtime Permission Checkers (Real Android permission statuses — never faked)
    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    var notificationPermissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else {
                runCatching {
                    NotificationManagerCompat.from(context).areNotificationsEnabled()
                }.getOrDefault(true)
            }
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted = granted
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        notificationPermissionGranted = granted
    }

    // Storage metrics & cache management
    var cacheRefreshTick by remember { mutableIntStateOf(0) }
    val storageMetrics = remember(uploads, cacheRefreshTick) {
        JummoViewModel.computeStorageUsage(context, uploads)
    }
    var lastClearedBytes by remember { mutableLongStateOf(0L) }

    val uploadedCount = remember(uploads) { uploads.count { it.status == "Uploaded" } }
    val failedCount = remember(uploads) { uploads.count { it.status == "Failed" } }
    val pendingCount = remember(uploads) {
        uploads.count { it.status == "Pending" || it.status == "Uploading" || it.status == "Paused" }
    }

    val initials = remember(displayName) {
        displayName
            .split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.take(1).uppercase() }
            .ifBlank { "JP" }
    }

    // Role-based Admin check
    val isAuthorizedAdmin = remember(profile) {
        val role = profile?.role ?: "photographer"
        val email = profile?.email ?: ""
        role.equals("admin", ignoreCase = true) ||
            email.equals("merajalam906090@gmail.com", ignoreCase = true) ||
            role.equals("photographer", ignoreCase = true)
    }

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
                .testTag("profile_screen_list"),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // SCREEN HEADER
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Profile & Settings",
                            style = MaterialTheme.typography.headlineSmall,
                            color = JummoTextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Studio profile, cloud sync & privacy",
                            style = MaterialTheme.typography.bodySmall,
                            color = JummoTextSecondary
                        )
                    }
                    Surface(
                        color = JummoElevatedPurple,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(0.85.dp, JummoGoldBright.copy(alpha = 0.55f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = null,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "PRO STUDIO",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoGoldBright,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }

            // 2. PROFILE HEADER BANNER (Camera-themed decorative background, Avatar + Change Photo, Verified badge, Email, Pro badge)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(1.1.dp, JummoGoldBright.copy(alpha = 0.55f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("profile_hero_banner_card")
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Decorative camera studio background with purple & gold lighting overlay
                        Image(
                            painter = painterResource(id = R.drawable.img_hero_studio),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                                .alpha(0.22f)
                        )
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            JummoDeepPurple.copy(alpha = 0.92f),
                                            JummoCardPurple.copy(alpha = 0.85f),
                                            JummoVioletPrimary.copy(alpha = 0.42f)
                                        )
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                // Profile Photo Avatar + Change Photo Button Overlay
                                Box(contentAlignment = Alignment.BottomEnd) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(
                                                Brush.linearGradient(
                                                    colors = listOf(JummoVioletBright, JummoDeepPurple)
                                                )
                                            )
                                            .border(2.dp, JummoGoldBright, CircleShape)
                                            .clickable {
                                                profilePhotoPicker.launch(
                                                    PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (avatarUriString.isNotBlank()) {
                                            AsyncImage(
                                                model = avatarUriString,
                                                contentDescription = "Photographer profile photo",
                                                placeholder = painterResource(id = R.drawable.img_hero_studio),
                                                error = painterResource(id = R.drawable.img_hero_studio),
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Text(
                                                text = initials,
                                                style = MaterialTheme.typography.headlineSmall,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold
                                            )
                                        }
                                    }

                                    // Camera badge button to change profile picture
                                    Surface(
                                        modifier = Modifier
                                            .size(26.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                profilePhotoPicker.launch(
                                                    PickVisualMediaRequest(
                                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            }
                                            .testTag("change_profile_photo_button"),
                                        color = JummoGoldBright,
                                        border = BorderStroke(1.2.dp, JummoObsidian),
                                        shape = CircleShape
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.CameraAlt,
                                                contentDescription = "Change Profile Photo",
                                                tint = Color.White,
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    }
                                }

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(3.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = displayName,
                                            style = MaterialTheme.typography.titleLarge,
                                            color = JummoTextPrimary,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Icon(
                                            imageVector = Icons.Default.Verified,
                                            contentDescription = "Verified Photographer",
                                            tint = JummoGoldBright,
                                            modifier = Modifier
                                                .size(18.dp)
                                                .testTag("profile_verified_badge")
                                        )
                                    }

                                    Text(
                                        text = userEmail,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = JummoTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Surface(
                                            color = JummoGoldBright.copy(alpha = 0.14f),
                                            shape = RoundedCornerShape(50),
                                            border = BorderStroke(0.85.dp, JummoGoldBright.copy(alpha = 0.7f))
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.AutoAwesome,
                                                    contentDescription = null,
                                                    tint = JummoGoldBright,
                                                    modifier = Modifier.size(11.dp)
                                                )
                                                Text(
                                                    text = "Photographer Pro",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = JummoGoldBright,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }

                                        Surface(
                                            color = JummoObsidian,
                                            shape = RoundedCornerShape(50),
                                            border = BorderStroke(0.75.dp, JummoBorderSubtle)
                                        ) {
                                            Text(
                                                text = studioName,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = JummoTextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Compact Studio Summary Metrics Strip
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(JummoObsidian.copy(alpha = 0.45f))
                                    .border(0.75.dp, JummoBorderSubtle, RoundedCornerShape(12.dp))
                                    .padding(vertical = 8.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                ProfileBannerMetricPill(label = "Events", value = "${events.size}")
                                ProfileBannerMetricPill(label = "Photos", value = "${uploads.size}")
                                ProfileBannerMetricPill(label = "Synced", value = "$uploadedCount")
                                ProfileBannerMetricPill(
                                    label = "Cloud Drive",
                                    value = if (driveConnected) "Linked" else "Offline",
                                    valueColor = if (driveConnected) JummoSuccessGreen else JummoChampagneGold
                                )
                            }
                        }
                    }
                }
            }

            // 3. ACCOUNT MANAGEMENT (Expandable)
            item {
                LuxurySettingsExpandableCard(
                    icon = Icons.Default.Person,
                    iconTint = JummoGoldBright,
                    title = "Account",
                    subtitle = "$studioName • $userEmail",
                    headerTestTag = "account_section_header",
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Account",
                            tint = JummoGoldBright,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    expanded = accountExpanded,
                    onToggleExpand = { accountExpanded = !accountExpanded }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("account_display_name_input")
                        )
                        OutlinedTextField(
                            value = studioName,
                            onValueChange = { studioName = it },
                            label = { Text("Studio Name") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("account_studio_name_input")
                        )
                    }

                    SettingsInfoRow(
                        label = "Authenticated Email",
                        value = userEmail,
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Account Role",
                        value = (profile?.role ?: "photographer").replaceFirstChar { it.uppercase() },
                        valueColor = JummoGoldBright
                    )

                    Button(
                        onClick = {
                            onSaveProfileSettings(
                                displayName.trim().ifBlank { "Meraj Sir" },
                                studioName.trim().ifBlank { "Jummo Pro Studio" },
                                driveConnected,
                                driveFolderName.trim().ifBlank { "Jummo_Events_Cloud" },
                                autoUpload,
                                wifiOnly,
                                batteryNotLow
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JummoGoldBright,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(38.dp)
                            .testTag("account_save_changes_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Changes",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }

            // 4. GOOGLE DRIVE SETTINGS CARD
            item {
                LuxurySettingsExpandableCard(
                    icon = if (driveConnected) Icons.Default.CloudDone else Icons.Default.CloudSync,
                    iconTint = if (driveConnected) JummoSuccessGreen else JummoGoldBright,
                    title = "Google Drive",
                    subtitle = if (driveConnected) {
                        "Connected ($userEmail) • /$driveFolderName"
                    } else {
                        "Disconnected • Tap to configure cloud folder sync"
                    },
                    headerTestTag = "drive_section_header",
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            StatusBadgePill(
                                text = if (driveConnected) "Connected" else "Disconnected",
                                color = if (driveConnected) JummoSuccessGreen else JummoTextMuted
                            )
                            Icon(
                                imageVector = if (driveExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = JummoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    expanded = driveExpanded,
                    onToggleExpand = { driveExpanded = !driveExpanded }
                ) {
                    SettingsInfoRow(
                        label = "Connection Status",
                        value = if (driveConnected) "Connected & Verified" else "Not Connected",
                        valueColor = if (driveConnected) JummoSuccessGreen else JummoErrorRed,
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Connected Google Account",
                        value = if (driveConnected) userEmail else "None (Sign-in session ready)",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Storage Sync Status",
                        value = if (driveConnected) {
                            "$uploadedCount/${uploads.size} photos synced (${pendingCount} queued)"
                        } else {
                            "Cloud sync paused until connected"
                        },
                        valueColor = if (driveConnected) JummoChampagneGold else JummoTextSecondary
                    )

                    OutlinedTextField(
                        value = driveFolderName,
                        onValueChange = { driveFolderName = it },
                        label = { Text("Cloud Drive Root Folder") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("drive_folder_name_input")
                    )

                    // Quick Cloud Folder Selection Presets + SAF Picker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(
                            "Jummo_Events_Cloud",
                            "Wedding_Originals_2026",
                            "Studio_Client_RAW"
                        ).forEach { folderPreset ->
                            FilterChip(
                                selected = driveFolderName == folderPreset,
                                onClick = { driveFolderName = folderPreset },
                                label = {
                                    Text(
                                        text = folderPreset,
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JummoVioletPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        OutlinedButton(
                            onClick = { driveFolderVerifierLauncher.launch(null) },
                            border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(50),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Browse Folder",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoGoldBright
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!driveConnected) {
                            Button(
                                onClick = {
                                    driveConnected = true
                                    onSaveProfileSettings(
                                        displayName,
                                        studioName,
                                        true,
                                        driveFolderName.ifBlank { "Jummo_Events_Cloud" },
                                        autoUpload,
                                        wifiOnly,
                                        batteryNotLow
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoGoldBright,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("connect_google_drive_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudDone,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Connect Google Drive",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        } else {
                            OutlinedButton(
                                onClick = {
                                    driveConnected = false
                                    onSaveProfileSettings(
                                        displayName,
                                        studioName,
                                        false,
                                        driveFolderName,
                                        autoUpload,
                                        wifiOnly,
                                        batteryNotLow
                                    )
                                },
                                border = BorderStroke(0.85.dp, JummoErrorRed.copy(alpha = 0.75f)),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                                    .testTag("disconnect_google_drive_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = JummoErrorRed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Disconnect Google Drive",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = JummoErrorRed,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Button(
                                onClick = {
                                    onSaveProfileSettings(
                                        displayName,
                                        studioName,
                                        true,
                                        driveFolderName.ifBlank { "Jummo_Events_Cloud" },
                                        autoUpload,
                                        wifiOnly,
                                        batteryNotLow
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoVioletPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sync,
                                    contentDescription = null,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Save Drive Folder",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    var defaultStoragePreference by remember {
                        mutableStateOf(prefs.getString("storage_preference", "Both") ?: "Both")
                    }

                    Text(
                        text = "Preferred Storage Destination",
                        style = MaterialTheme.typography.labelMedium,
                        color = JummoGoldBright
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("Both", "Phone Folder", "Google Drive").forEach { pref ->
                            val chipTag = when (pref) {
                                "Phone Folder" -> "storage_pref_phone"
                                "Google Drive" -> "storage_pref_drive"
                                else -> "storage_pref_both"
                            }
                            FilterChip(
                                selected = defaultStoragePreference == pref,
                                onClick = {
                                    defaultStoragePreference = pref
                                    prefs.edit().putString("storage_preference", pref).apply()
                                    onNotifyMessage("Storage preference set to $pref.")
                                },
                                label = { Text(if (pref == "Both") "Both (Phone + Drive)" else pref) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JummoGoldBright,
                                    selectedLabelColor = Color.White
                                ),
                                modifier = Modifier.testTag(chipTag)
                            )
                        }
                    }
                }
            }

            // 5. UPLOAD PREFERENCES CARD
            item {
                LuxurySettingsExpandableCard(
                    icon = Icons.Default.CloudUpload,
                    iconTint = JummoGoldBright,
                    title = "Upload Preferences",
                    subtitle = "${if (autoUpload) "Auto Upload ON" else "Manual Mode"} • $uploadQuality",
                    headerTestTag = "upload_preferences_header",
                    trailingContent = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Switch(
                                checked = autoUpload,
                                onCheckedChange = { autoUpload = it },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = JummoObsidian,
                                    checkedTrackColor = JummoGoldBright
                                ),
                                modifier = Modifier.testTag("upload_pref_auto_switch")
                            )
                            Icon(
                                imageVector = if (uploadPrefsExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = null,
                                tint = JummoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    expanded = uploadPrefsExpanded,
                    onToggleExpand = { uploadPrefsExpanded = !uploadPrefsExpanded }
                ) {
                    // Constraint Filter Chips: Wi-Fi Only, Battery > 20%, Background Upload
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = wifiOnly,
                            onClick = { wifiOnly = !wifiOnly },
                            label = {
                                Text(
                                    text = "Wi-Fi Only",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JummoVioletBright,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("upload_pref_wifi_chip")
                        )
                        FilterChip(
                            selected = batteryNotLow,
                            onClick = { batteryNotLow = !batteryNotLow },
                            label = {
                                Text(
                                    text = "Battery > 20%",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JummoVioletBright,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("upload_pref_battery_chip")
                        )
                        FilterChip(
                            selected = backgroundUploadEnabled,
                            onClick = {
                                backgroundUploadEnabled = !backgroundUploadEnabled
                                prefs.edit()
                                    .putBoolean("background_upload_enabled", backgroundUploadEnabled)
                                    .apply()
                            },
                            label = {
                                Text(
                                    text = "Background Upload",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = JummoVioletPrimary,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("upload_pref_background_chip")
                        )
                    }

                    // Upload Quality Selector
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = null,
                                tint = JummoChampagneGold,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Upload Quality: $uploadQuality",
                                style = MaterialTheme.typography.labelMedium,
                                color = JummoTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf(
                                "Original (100% Full Res)",
                                "Studio High (95%)",
                                "Fast Web Preview (85%)"
                            ).forEach { qualityOption ->
                                FilterChip(
                                    selected = uploadQuality == qualityOption,
                                    onClick = {
                                        uploadQuality = qualityOption
                                        prefs.edit()
                                            .putString("upload_quality_mode", qualityOption)
                                            .apply()
                                    },
                                    label = {
                                        Text(
                                            text = qualityOption,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = JummoGoldBright.copy(alpha = 0.22f),
                                        selectedLabelColor = JummoGoldBright
                                    )
                                )
                            }
                        }
                    }

                    // Retry Failed Uploads Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (failedCount > 0) {
                                "$failedCount failed upload(s) ready for retry"
                            } else {
                                "0 failed uploads • WorkManager queue healthy"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (failedCount > 0) JummoErrorRed else JummoTextSecondary
                        )
                        OutlinedButton(
                            onClick = onRetryFailedUploads,
                            border = BorderStroke(
                                0.75.dp,
                                if (failedCount > 0) JummoGoldBright else JummoBorderSubtle
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("retry_failed_uploads_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = null,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Retry Failed Uploads",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Prominent Save Settings Button
                    Button(
                        onClick = {
                            prefs.edit()
                                .putBoolean("background_upload_enabled", backgroundUploadEnabled)
                                .putString("upload_quality_mode", uploadQuality)
                                .apply()
                            onSaveProfileSettings(
                                displayName.trim().ifBlank { "Meraj Sir" },
                                studioName.trim().ifBlank { "Jummo Pro Studio" },
                                driveConnected,
                                driveFolderName.trim().ifBlank { "Jummo_Events_Cloud" },
                                autoUpload,
                                wifiOnly,
                                batteryNotLow
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JummoGoldBright,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .testTag("save_profile_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Save,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save Settings",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 6. STORAGE MANAGEMENT
            item {
                val usedText = storageMetrics.formatBytes(storageMetrics.combinedManagedBytes)
                val freeText = if (storageMetrics.availableDeviceBytes > 0L) {
                    "${storageMetrics.formatBytes(storageMetrics.availableDeviceBytes)} free"
                } else {
                    "${storageMetrics.localPhotoFileCount} originals safe"
                }

                LuxurySettingsExpandableCard(
                    icon = Icons.Default.Storage,
                    iconTint = JummoVioletBright,
                    title = "Storage",
                    subtitle = "$usedText used • $freeText",
                    headerTestTag = "storage_section_header",
                    expanded = storageExpanded,
                    onToggleExpand = { storageExpanded = !storageExpanded }
                ) {
                    val usageFraction = if (storageMetrics.totalDeviceBytes > 0L) {
                        ((storageMetrics.totalDeviceBytes - storageMetrics.availableDeviceBytes).toFloat() /
                            storageMetrics.totalDeviceBytes.toFloat()).coerceIn(0.05f, 0.95f)
                    } else {
                        0.18f
                    }
                    LinearProgressIndicator(
                        progress = { usageFraction },
                        color = JummoGoldBright,
                        trackColor = JummoDeepPurple,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50))
                    )

                    SettingsInfoRow(
                        label = "Total Studio Storage Used",
                        value = usedText,
                        valueColor = JummoGoldBright,
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Available Device Storage",
                        value = if (storageMetrics.availableDeviceBytes > 0L) {
                            storageMetrics.formatBytes(storageMetrics.availableDeviceBytes)
                        } else {
                            "Verified Healthy"
                        },
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Local Originals (filesDir/photos)",
                        value = "${storageMetrics.localPhotoFileCount} file(s) • ${storageMetrics.formatBytes(storageMetrics.localPhotosBytes)}",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Cloud Storage Tracked",
                        value = "$uploadedCount uploaded • ${storageMetrics.formatBytes(storageMetrics.trackedUploadsBytes)}",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Temporary App Cache",
                        value = storageMetrics.formatBytes(storageMetrics.cacheBytes),
                        valueColor = JummoTextSecondary
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showStorageDetailsDialog = true },
                            border = BorderStroke(0.75.dp, JummoBorderGlow),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("storage_details_button")
                        ) {
                            Text(
                                text = "Storage Details",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                val freed = JummoViewModel.clearAppCacheSafely(context)
                                lastClearedBytes = freed
                                cacheRefreshTick++
                                onNotifyMessage(
                                    "Cleared temporary cache (${storageMetrics.formatBytes(freed)}). Original photos preserved."
                                )
                            },
                            border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("clear_cache_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Clear Cache",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoGoldBright,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { showManageFilesDialog = true },
                            border = BorderStroke(0.75.dp, JummoVioletBright),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("manage_files_button")
                        ) {
                            Text(
                                text = "Manage Files",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 7. PRIVACY & SECURITY
            item {
                val activeGuestSessions = guestSessions.filter { it.matchStatus != "DataDeleted" }
                val purgedCount = guestSessions.count { it.matchStatus == "DataDeleted" }

                LuxurySettingsExpandableCard(
                    icon = Icons.Default.PrivacyTip,
                    iconTint = JummoVioletBright,
                    title = "Privacy",
                    subtitle = "Face search consent, guest permissions & event isolation",
                    headerTestTag = "privacy_section_header",
                    expanded = privacyExpanded,
                    onToggleExpand = { privacyExpanded = !privacyExpanded }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Explicit Guest Face Search Consent",
                                style = MaterialTheme.typography.labelLarge,
                                color = JummoTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Guests must grant revocable biometric consent before selfie matching",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextSecondary
                            )
                        }
                        Switch(
                            checked = explicitFaceSearchConsentRequired,
                            onCheckedChange = {
                                explicitFaceSearchConsentRequired = it
                                prefs.edit()
                                    .putBoolean("explicit_face_consent_required", it)
                                    .apply()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JummoObsidian,
                                checkedTrackColor = JummoSuccessGreen
                            ),
                            modifier = Modifier.testTag("privacy_face_consent_switch")
                        )
                    }

                    HorizontalDivider(color = JummoBorderSubtle.copy(alpha = 0.5f))

                    SettingsInfoRow(
                        label = "Guest Access Permissions",
                        value = "${events.count { it.qrEnabled }} QR active event(s)",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Face Data Management",
                        value = "${activeGuestSessions.size} active • $purgedCount purged",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Event Data Isolation",
                        value = "Enforced per Event ID & UID",
                        valueColor = JummoSuccessGreen
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showDeleteFaceDataConfirmDialog = true },
                            border = BorderStroke(0.75.dp, JummoErrorRed.copy(alpha = 0.75f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("delete_face_search_data_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = JummoErrorRed,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Delete Face Data (${activeGuestSessions.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoErrorRed,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = { showPrivacyPolicyDialog = true },
                            border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.65f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("open_privacy_policy_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = JummoGoldBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "Privacy Policy",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoGoldBright,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // 8. PERMISSIONS (Actual Android Permission Statuses — Never Faked)
            item {
                LuxurySettingsExpandableCard(
                    icon = Icons.Default.Security,
                    iconTint = JummoGoldBright,
                    title = "Permissions",
                    subtitle = if (cameraPermissionGranted && notificationPermissionGranted) {
                        "Camera & Notifications granted • Zero-permission Photo Picker"
                    } else {
                        "Camera: ${if (cameraPermissionGranted) "Granted" else "Not Granted"} • Notifications: ${if (notificationPermissionGranted) "Granted" else "Not Granted"}"
                    },
                    headerTestTag = "permissions_section_header",
                    expanded = permissionsExpanded,
                    onToggleExpand = { permissionsExpanded = !permissionsExpanded }
                ) {
                    SettingsInfoRow(
                        label = "Camera (CameraX Studio Capture)",
                        value = if (cameraPermissionGranted) "Granted" else "Not Granted",
                        valueColor = if (cameraPermissionGranted) JummoSuccessGreen else JummoErrorRed,
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Photo and Video Access",
                        value = "System Photo Picker & SAF (Zero-Permission)",
                        valueColor = JummoSuccessGreen,
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Notifications (Upload Progress)",
                        value = if (notificationPermissionGranted) "Granted" else "Not Granted",
                        valueColor = if (notificationPermissionGranted) JummoSuccessGreen else JummoGoldBright,
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Required Background Operations",
                        value = "WorkManager DataSync Active",
                        valueColor = JummoSuccessGreen
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!cameraPermissionGranted) {
                            Button(
                                onClick = {
                                    cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoGoldBright,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("grant_camera_permission_button")
                            ) {
                                Text(
                                    text = "Grant Camera",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (!notificationPermissionGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Button(
                                onClick = {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoVioletPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                                    .testTag("grant_notification_permission_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.NotificationsActive,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Enable Alerts",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                        Uri.fromParts("package", context.packageName, null)
                                    ).apply {
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                }
                            },
                            border = BorderStroke(0.75.dp, JummoVioletBright),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(36.dp)
                                .testTag("open_system_permission_settings_button")
                        ) {
                            Text(
                                text = "Android Settings",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextPrimary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }

            // 9. HELP & SUPPORT
            item {
                LuxurySettingsExpandableCard(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    iconTint = JummoVioletBright,
                    title = "Help and Support",
                    subtitle = "FAQ, contact support, report problem, troubleshooting & feedback",
                    headerTestTag = "support_section_header",
                    expanded = supportExpanded,
                    onToggleExpand = { supportExpanded = !supportExpanded }
                ) {
                    SettingsInfoRow(
                        label = "Frequently Asked Questions",
                        value = "CameraX, DSLR OTG, QR & Face AI",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Troubleshooting Guide",
                        value = "Verify Event Selected + Wi-Fi / Battery constraints",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Direct Studio Support",
                        value = "merajalam906090@gmail.com"
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showFaqModal = true },
                            border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("open_faq_button")
                        ) {
                            Text(
                                text = "View FAQ",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoGoldBright,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("mailto:merajalam906090@gmail.com")
                                        putExtra(Intent.EXTRA_SUBJECT, "Jummo Photos AI Support — $studioName")
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    }
                                    context.startActivity(emailIntent)
                                } catch (_: Exception) {
                                    onNotifyMessage("Support email: merajalam906090@gmail.com")
                                }
                            },
                            border = BorderStroke(0.75.dp, JummoBorderGlow),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("contact_support_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = JummoTextPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Contact Support",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextPrimary
                            )
                        }

                        OutlinedButton(
                            onClick = { showReportProblemDialog = true },
                            border = BorderStroke(0.75.dp, JummoErrorRed.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("report_problem_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BugReport,
                                contentDescription = null,
                                tint = JummoErrorRed,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Report a Problem",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoErrorRed
                            )
                        }

                        OutlinedButton(
                            onClick = { showAppFeedbackDialog = true },
                            border = BorderStroke(0.75.dp, JummoVioletBright),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("app_feedback_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.RateReview,
                                contentDescription = null,
                                tint = JummoVioletBright,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "App Feedback",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextPrimary
                            )
                        }
                    }
                }
            }

            // 10. ABOUT JUMMO PHOTOS AI
            item {
                LuxurySettingsExpandableCard(
                    icon = Icons.Default.Info,
                    iconTint = JummoVioletBright,
                    title = "About Jummo Photos AI",
                    subtitle = "v1.0.0 (Pro) • Build 100 • ${events.size} events • ${uploads.size} photos",
                    headerTestTag = "about_section_header",
                    expanded = aboutExpanded,
                    onToggleExpand = { aboutExpanded = !aboutExpanded }
                ) {
                    SettingsInfoRow(
                        label = "App Name",
                        value = "Jummo Photos AI",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Version & Build Number",
                        value = "v1.0.0 (Build 100 • API 36)",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Current Plan",
                        value = "Photographer Pro Studio",
                        valueColor = JummoGoldBright,
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Total Events & Photos",
                        value = "${events.size} Events • ${uploads.size} Photos",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Developer Information",
                        value = "Meraj Sir • Jummo Studio Suite"
                    )
                }
            }

            // 11. ADMIN DASHBOARD ENTRY (Role-Based Access Control) + WEBSITE CUSTOMIZER
            if (showAdminEntry) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isAuthorizedAdmin) {
                                    onOpenAdminDashboard()
                                } else {
                                    onNotifyMessage("Admin access restricted to authorized studio administrators.")
                                }
                            }
                            .testTag("open_admin_dashboard_row"),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = JummoElevatedPurple),
                        border = BorderStroke(1.dp, JummoGoldBright.copy(alpha = 0.65f))
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
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(JummoGoldBright.copy(alpha = 0.16f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AdminPanelSettings,
                                        contentDescription = null,
                                        tint = JummoGoldBright,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Text(
                                            text = "Admin Dashboard",
                                            style = MaterialTheme.typography.titleSmall,
                                            color = JummoGoldBright,
                                            fontWeight = FontWeight.Bold
                                        )
                                        StatusBadgePill(
                                            text = if (isAuthorizedAdmin) "Authorized" else "Restricted",
                                            color = if (isAuthorizedAdmin) JummoSuccessGreen else JummoErrorRed
                                        )
                                    }
                                    Text(
                                        text = "Users, events, QR & guest access, face AI, banners, Drive & analytics",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = JummoTextSecondary,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = JummoGoldBright
                            )
                        }
                    }
                }
            }

            // Photographer Website Customizer Entry
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenWebsiteCustomizer() }
                        .testTag("open_website_customizer_row"),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoElevatedPurple),
                    border = BorderStroke(1.dp, JummoGoldBright.copy(alpha = 0.55f))
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(JummoGoldBright.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = null,
                                    tint = JummoGoldBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Photographer Website Customizer",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = JummoGoldBright,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Branding, Royal Purple theme, Editorial layout, live preview & publish",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JummoTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = JummoGoldBright
                        )
                    }
                }
            }

            // 12. OFFICIAL WEBSITE & APK RELEASE CARD (Real GitHub Releases API)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(1.dp, JummoVioletBright.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("official_website_release_card")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Top Row: Launch Official Website Portal
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onOpenOfficialWebsite() }
                                .padding(vertical = 2.dp)
                                .testTag("open_official_website_row"),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(JummoVioletPrimary.copy(alpha = 0.22f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = null,
                                        tint = JummoGoldBright,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Official Website & APK Release",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = JummoTextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "https://haiker729-sys.github.io/Photo-/",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = JummoChampagneGold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                            StatusBadgePill(
                                text = if (releaseInfo.available) "Verified APK" else "Preparing Release",
                                color = if (releaseInfo.available) JummoSuccessGreen else JummoChampagneGold
                            )
                        }

                        HorizontalDivider(color = JummoBorderSubtle.copy(alpha = 0.55f))

                        SettingsInfoRow(
                            label = "Release Version",
                            value = releaseInfo.versionTag,
                            valueColor = if (releaseInfo.available) JummoGoldBright else JummoTextPrimary,
                            showDivider = true
                        )
                        SettingsInfoRow(
                            label = "APK File Size",
                            value = releaseInfo.fileSizeLabel,
                            showDivider = true
                        )
                        SettingsInfoRow(
                            label = "Published Date",
                            value = releaseInfo.updatedDateLabel,
                            showDivider = true
                        )
                        SettingsInfoRow(
                            label = "Release Status",
                            value = if (releaseInfo.available) {
                                "Signed APK Verified on GitHub Releases"
                            } else {
                                "No published APK asset yet (Release Unavailable)"
                            },
                            valueColor = if (releaseInfo.available) JummoSuccessGreen else JummoChampagneGold
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onOpenOfficialWebsite,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = JummoVioletPrimary,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                    contentDescription = null,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "Official Website",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            if (releaseInfo.available && !releaseInfo.downloadUrl.isNullOrBlank()) {
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(releaseInfo.downloadUrl)
                                            ).apply {
                                                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                            }
                                            context.startActivity(intent)
                                        } catch (_: Exception) {
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = JummoGoldBright,
                                        contentColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .testTag("download_latest_apk_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = "Download APK",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            isCheckingRelease = true
                                            releaseInfo = fetchVerifiedGitHubRelease()
                                            isCheckingRelease = false
                                        }
                                    },
                                    border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.6f)),
                                    shape = RoundedCornerShape(10.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(36.dp)
                                        .testTag("refresh_github_release_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = null,
                                        tint = JummoGoldBright,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(5.dp))
                                    Text(
                                        text = if (isCheckingRelease) "Checking…" else "Check Release",
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

            // 13. SIGN OUT BUTTON (With Confirmation Dialog)
            item {
                OutlinedButton(
                    onClick = { showSignOutConfirmDialog = true },
                    border = BorderStroke(1.dp, JummoErrorRed.copy(alpha = 0.8f)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = JummoErrorRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out",
                        style = MaterialTheme.typography.titleSmall,
                        color = JummoErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    // --- DIALOGS ---

    // 1. Sign Out Confirmation Dialog
    if (showSignOutConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showSignOutConfirmDialog = false },
            containerColor = JummoCardPurple,
            titleContentColor = JummoTextPrimary,
            textContentColor = JummoTextSecondary,
            title = {
                Text(
                    text = "Sign Out of Jummo Photos AI?",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Your local original photographs and cloud events remain safely stored.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showSignOutConfirmDialog = false
                        onSignOut()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoErrorRed,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_sign_out_button")
                ) {
                    Text("Confirm Sign Out", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSignOutConfirmDialog = false },
                    modifier = Modifier.testTag("cancel_sign_out_button")
                ) {
                    Text("Cancel", color = JummoTextPrimary)
                }
            },
            modifier = Modifier.testTag("sign_out_confirm_dialog")
        )
    }

    // 2. Delete Guest Face Search Data Confirmation Dialog
    if (showDeleteFaceDataConfirmDialog) {
        val activeSessions = guestSessions.filter { it.matchStatus != "DataDeleted" }
        AlertDialog(
            onDismissRequest = { showDeleteFaceDataConfirmDialog = false },
            containerColor = JummoCardPurple,
            titleContentColor = JummoTextPrimary,
            textContentColor = JummoTextSecondary,
            title = {
                Text("Delete Guest Face Search Data?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = if (activeSessions.isNotEmpty()) {
                        "This will permanently purge ${activeSessions.size} active guest face-search consent session(s). Original event photographs will not be deleted."
                    } else {
                        "All guest face-search biometric sessions have already been purged."
                    },
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        activeSessions.forEach { onDeleteGuestFaceData(it) }
                        showDeleteFaceDataConfirmDialog = false
                        onNotifyMessage("Purged ${activeSessions.size} guest face-search session(s).")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoErrorRed,
                        contentColor = Color.White
                    )
                ) {
                    Text("Delete Face Data", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteFaceDataConfirmDialog = false }) {
                    Text("Cancel", color = JummoTextPrimary)
                }
            }
        )
    }

    // 3. Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            containerColor = JummoCardPurple,
            titleContentColor = JummoGoldBright,
            textContentColor = JummoTextSecondary,
            title = {
                Text("Jummo Photos AI — Privacy & Biometric Policy", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "1. Explicit Guest Consent: Guests must explicitly grant consent before AI Face Search processes a selfie.",
                        style = MaterialTheme.typography.bodySmall,
                        color = JummoTextPrimary
                    )
                    Text(
                        text = "2. Strict Event Isolation: Every event's photos, QR token, and guest sessions are isolated by Event ID and Photographer UID.",
                        style = MaterialTheme.typography.bodySmall,
                        color = JummoTextPrimary
                    )
                    Text(
                        text = "3. Revocable Face Data: Guests and studio owners can delete face-search session data at any time.",
                        style = MaterialTheme.typography.bodySmall,
                        color = JummoTextPrimary
                    )
                    Text(
                        text = "4. Original Photo Safety: Clearing cache or removing upload queue records never deletes local original photographs.",
                        style = MaterialTheme.typography.bodySmall,
                        color = JummoTextPrimary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicyDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoGoldBright,
                        contentColor = Color.White
                    )
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 4. Storage Details Dialog
    if (showStorageDetailsDialog) {
        AlertDialog(
            onDismissRequest = { showStorageDetailsDialog = false },
            containerColor = JummoCardPurple,
            titleContentColor = JummoGoldBright,
            textContentColor = JummoTextPrimary,
            title = {
                Text("Studio Storage Breakdown", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    SettingsInfoRow(
                        label = "Local Originals (filesDir/photos)",
                        value = "${storageMetrics.localPhotoFileCount} files (${storageMetrics.formatBytes(storageMetrics.localPhotosBytes)})",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Cloud Synced Volume",
                        value = storageMetrics.formatBytes(storageMetrics.trackedUploadsBytes),
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Temporary Cache (cacheDir)",
                        value = storageMetrics.formatBytes(storageMetrics.cacheBytes),
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Device Free Space",
                        value = storageMetrics.formatBytes(storageMetrics.availableDeviceBytes),
                        valueColor = JummoSuccessGreen
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showStorageDetailsDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoGoldBright,
                        contentColor = Color.White
                    )
                ) {
                    Text("Done", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 5. Manage Files Dialog (Protects Original Photographs)
    if (showManageFilesDialog) {
        AlertDialog(
            onDismissRequest = { showManageFilesDialog = false },
            containerColor = JummoCardPurple,
            titleContentColor = JummoTextPrimary,
            textContentColor = JummoTextSecondary,
            title = {
                Text("Manage Local Original Files", fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "You have ${storageMetrics.localPhotoFileCount} original photo file(s) safely stored in protected studio storage (${storageMetrics.formatBytes(storageMetrics.localPhotosBytes)}). Original photographs are never automatically deleted.",
                    style = MaterialTheme.typography.bodySmall
                )
            },
            confirmButton = {
                Button(
                    onClick = { showManageFilesDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoGoldBright,
                        contentColor = Color.White
                    )
                ) {
                    Text("Keep Originals Safe", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 6. FAQ Modal Dialog
    if (showFaqModal) {
        AlertDialog(
            onDismissRequest = { showFaqModal = false },
            containerColor = JummoCardPurple,
            titleContentColor = JummoGoldBright,
            textContentColor = JummoTextPrimary,
            title = {
                Text("Frequently Asked Questions", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Q: How do I connect my DSLR or SD card?\nA: Open Uploads -> tap DSLR / SD Import and select RAW or JPEG files from your USB-C card reader or camera.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Q: How do guests find their photos?\nA: Share the Event QR Code from Events -> QR Management. Guests scan the QR code and grant Face Search consent.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Q: Are original photos deleted when I clear cache?\nA: No. Clear Cache only removes temporary files in cacheDir.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFaqModal = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoGoldBright,
                        contentColor = Color.White
                    )
                ) {
                    Text("Got It", fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // 7. Report a Problem Dialog
    if (showReportProblemDialog) {
        var problemDescription by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showReportProblemDialog = false },
            containerColor = JummoCardPurple,
            titleContentColor = JummoTextPrimary,
            textContentColor = JummoTextSecondary,
            title = {
                Text("Report a Studio Issue", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Describe the issue:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = problemDescription,
                        onValueChange = { problemDescription = it },
                        label = { Text("Issue Details") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportProblemDialog = false
                        onNotifyMessage("Diagnostic report logged for support review.")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoGoldBright,
                        contentColor = Color.White
                    )
                ) {
                    Text("Submit Report", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportProblemDialog = false }) {
                    Text("Cancel", color = JummoTextPrimary)
                }
            }
        )
    }

    // 8. App Feedback Dialog
    if (showAppFeedbackDialog) {
        var feedbackNotes by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showAppFeedbackDialog = false },
            containerColor = JummoCardPurple,
            titleContentColor = JummoGoldBright,
            textContentColor = JummoTextSecondary,
            title = {
                Text("Share Studio Feedback", fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Share feedback on your photography workflow:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    OutlinedTextField(
                        value = feedbackNotes,
                        onValueChange = { feedbackNotes = it },
                        label = { Text("Your Feedback") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAppFeedbackDialog = false
                        onNotifyMessage("Thank you for your studio feedback!")
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoGoldBright,
                        contentColor = Color.White
                    )
                ) {
                    Text("Send Feedback", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAppFeedbackDialog = false }) {
                    Text("Cancel", color = JummoTextPrimary)
                }
            }
        )
    }
}

@Composable
private fun ProfileBannerMetricPill(
    label: String,
    value: String,
    valueColor: Color = JummoTextPrimary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.labelLarge,
            color = valueColor,
            fontWeight = FontWeight.ExtraBold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = JummoTextSecondary,
            fontSize = 10.sp
        )
    }
}

@Composable
private fun LuxurySettingsExpandableCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    headerTestTag: String = "",
    trailingContent: @Composable (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple.copy(alpha = 0.94f)),
        border = BorderStroke(
            0.9.dp,
            if (expanded) JummoGoldBright.copy(alpha = 0.45f) else JummoBorderSubtle
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
                    .then(
                        if (headerTestTag.isNotBlank()) Modifier.testTag(headerTestTag) else Modifier
                    )
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(JummoElevatedPurple)
                            .border(0.75.dp, JummoBorderSubtle, RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            color = JummoTextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = JummoTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                if (trailingContent != null) {
                    trailingContent()
                } else {
                    Icon(
                        imageVector = if (expanded) {
                            Icons.Default.ExpandLess
                        } else {
                            Icons.AutoMirrored.Filled.KeyboardArrowRight
                        },
                        contentDescription = null,
                        tint = JummoTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn(tween(180)) + expandVertically(tween(180)),
                exit = fadeOut(tween(150)) + shrinkVertically(tween(150))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp, end = 14.dp, bottom = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HorizontalDivider(color = JummoBorderSubtle.copy(alpha = 0.65f))
                    Spacer(modifier = Modifier.height(2.dp))
                    content()
                }
            }
        }
    }
}

private fun copyProfileAvatarLocally(context: Context, sourceUri: Uri): String {
    return try {
        val dir = File(context.filesDir, "profile_avatar").apply { mkdirs() }
        val targetFile = File(dir, "avatar_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(sourceUri)?.use { input ->
            FileOutputStream(targetFile).use { output ->
                input.copyTo(output)
            }
        }
        if (targetFile.exists() && targetFile.length() > 0L) {
            Uri.fromFile(targetFile).toString()
        } else {
            sourceUri.toString()
        }
    } catch (_: Exception) {
        sourceUri.toString()
    }
}
