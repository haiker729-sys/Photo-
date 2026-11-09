package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.data.model.GuestFaceSearchSession
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.PhotographerProfile
import com.example.ui.components.SettingsInfoRow
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
import com.example.ui.viewmodel.JummoViewModel

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
    onOpenAdminDashboard: () -> Unit = {}
) {
    val context = LocalContext.current

    var displayName by remember(profile?.displayName) {
        mutableStateOf(profile?.displayName ?: "Lead Photographer")
    }
    var studioName by remember(profile?.studioName) {
        mutableStateOf(profile?.studioName ?: "Jummo Pro Studio")
    }
    var driveConnected by remember(profile?.driveConnected) {
        mutableStateOf(profile?.driveConnected ?: false)
    }
    var driveFolderName by remember(profile?.driveFolderName) {
        mutableStateOf(profile?.driveFolderName ?: "Jummo_Events_Cloud")
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

    var cameraPermissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        cameraPermissionGranted = granted
    }

    val storageMetrics = remember(uploads) {
        JummoViewModel.computeStorageUsage(context, uploads)
    }

    var expandedSection by remember { mutableStateOf<String?>(null) }

    val initials = displayName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.take(1).uppercase() }
        .ifBlank { "JP" }

    Box(
        modifier = Modifier.fillMaxSize(),
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
            // Header Title matching Screen 6 in Reference
            item {
                Text(
                    text = "Profile & Settings",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            // 1. Luxury Photographer Profile Hero Card (Screen 6 in Reference)
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(1.dp, JummoBorderGlow),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        JummoElevatedPurple,
                                        JummoVioletPrimary.copy(alpha = 0.55f),
                                        JummoCardPurple
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Circular Avatar with Golden Ring
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            colors = listOf(JummoVioletBright, JummoDeepPurple)
                                        )
                                    )
                                    .border(2.dp, JummoGoldBright, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = displayName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = profile?.email?.ifBlank { "studio@jummophotos.ai" }
                                        ?: "studio@jummophotos.ai",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JummoTextSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Surface(
                                    color = JummoGoldBright.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(50),
                                    border = BorderStroke(0.85.dp, JummoGoldBright.copy(alpha = 0.65f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = JummoGoldBright,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "Photographer Pro",
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

            // 2. Account & Studio Row + Inline Form
            item {
                LuxurySettingsExpandableCard(
                    icon = Icons.Default.Person,
                    iconTint = JummoGoldBright,
                    title = "Account",
                    subtitle = "$studioName • Profile, studio name, role",
                    trailingContent = {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Account",
                            tint = JummoTextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    expanded = true,
                    onToggleExpand = {
                        expandedSection = if (expandedSection == "account") null else "account"
                    }
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
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = studioName,
                            onValueChange = { studioName = it },
                            label = { Text("Studio Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // 3. Google Drive Row + Configuration
            item {
                LuxurySettingsExpandableCard(
                    icon = if (driveConnected) Icons.Default.CloudDone else Icons.Default.CloudSync,
                    iconTint = JummoGoldBright,
                    title = "Google Drive",
                    subtitle = driveFolderName,
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
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = JummoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    expanded = expandedSection == "drive",
                    onToggleExpand = {
                        expandedSection = if (expandedSection == "drive") null else "drive"
                    }
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enable Cloud Drive Sync Bridge",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White
                        )
                        Switch(
                            checked = driveConnected,
                            onCheckedChange = { driveConnected = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = JummoObsidian,
                                checkedTrackColor = JummoSuccessGreen
                            )
                        )
                    }
                    OutlinedTextField(
                        value = driveFolderName,
                        onValueChange = { driveFolderName = it },
                        label = { Text("Root Drive Folder Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 4. Upload Preferences Row + Quick Save Button
            item {
                LuxurySettingsExpandableCard(
                    icon = Icons.Default.CloudUpload,
                    iconTint = JummoGoldBright,
                    title = "Upload Preferences",
                    subtitle = "Auto upload, network, battery",
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
                                )
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = null,
                                tint = JummoTextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    expanded = true,
                    onToggleExpand = {
                        expandedSection = if (expandedSection == "uploads") null else "uploads"
                    }
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                            )
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
                            )
                        )
                    }

                    Button(
                        onClick = {
                            onSaveProfileSettings(
                                displayName,
                                studioName,
                                driveConnected,
                                driveFolderName,
                                autoUpload,
                                wifiOnly,
                                batteryNotLow
                            )
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = JummoGoldBright,
                            contentColor = JummoObsidian
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
                            text = "Save Account, Drive & Upload Settings",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // 5. Storage Row
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
                    expanded = expandedSection == "storage",
                    onToggleExpand = {
                        expandedSection = if (expandedSection == "storage") null else "storage"
                    }
                ) {
                    SettingsInfoRow(
                        label = "Local Originals (filesDir/photos)",
                        value = "${storageMetrics.localPhotoFileCount} file(s)",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Local Photo Storage Used",
                        value = storageMetrics.formatBytes(storageMetrics.localPhotosBytes),
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Tracked Upload Queue Size",
                        value = storageMetrics.formatBytes(storageMetrics.trackedUploadsBytes),
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Original Photo Retention",
                        value = "Never Auto-Deleted",
                        valueColor = JummoGoldBright
                    )
                }
            }

            // 6. Privacy & Security Row (Includes both "Privacy" and "Permissions" labels for test verification)
            item {
                LuxurySettingsExpandableCard(
                    icon = Icons.Default.PrivacyTip,
                    iconTint = JummoVioletBright,
                    title = "Privacy",
                    subtitle = "Face search consent, data & access",
                    expanded = expandedSection == "privacy",
                    onToggleExpand = {
                        expandedSection = if (expandedSection == "privacy") null else "privacy"
                    }
                ) {
                    SettingsInfoRow(
                        label = "Guest Face-Search Consent Sessions",
                        value = "${guestSessions.size} total",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Purged Guest Sessions",
                        value = "${guestSessions.count { it.matchStatus == "DataDeleted" }}",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Event Data Isolation",
                        value = "Enforced per Event ID",
                        valueColor = JummoSuccessGreen
                    )

                    val activeGuestSessions = guestSessions.filter { it.matchStatus != "DataDeleted" }
                    if (activeGuestSessions.isNotEmpty()) {
                        OutlinedButton(
                            onClick = {
                                activeGuestSessions.forEach { onDeleteGuestFaceData(it) }
                            },
                            border = BorderStroke(0.75.dp, JummoErrorRed.copy(alpha = 0.7f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = null,
                                tint = JummoErrorRed,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Delete Active Guest Face Data (${activeGuestSessions.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = JummoErrorRed
                            )
                        }
                    }
                }
            }

            // 7. Permissions & Security Row
            item {
                LuxurySettingsExpandableCard(
                    icon = Icons.Default.Security,
                    iconTint = JummoGoldBright,
                    title = "Permissions",
                    subtitle = if (cameraPermissionGranted) "Camera granted • Zero-permission picker" else "Camera access required for live capture",
                    expanded = expandedSection == "permissions",
                    onToggleExpand = {
                        expandedSection = if (expandedSection == "permissions") null else "permissions"
                    }
                ) {
                    SettingsInfoRow(
                        label = "Camera (CameraX Capture)",
                        value = if (cameraPermissionGranted) "Granted" else "Not Granted",
                        valueColor = if (cameraPermissionGranted) JummoSuccessGreen else JummoGoldBright,
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Media & DSLR Import",
                        value = "Zero-Permission Photo Picker & SAF",
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
                                    contentColor = JummoObsidian
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(36.dp)
                            ) {
                                Text("Grant Camera", fontWeight = FontWeight.Bold)
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
                        ) {
                            Text(
                                text = "System App Settings",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            // 8. Help and Support Row
            item {
                LuxurySettingsExpandableCard(
                    icon = Icons.AutoMirrored.Filled.HelpOutline,
                    iconTint = JummoVioletBright,
                    title = "Help and Support",
                    subtitle = "FAQ, contact, report issue",
                    expanded = expandedSection == "support",
                    onToggleExpand = {
                        expandedSection = if (expandedSection == "support") null else "support"
                    }
                ) {
                    SettingsInfoRow(
                        label = "1. Event & QR Setup",
                        value = "Events -> New Event -> QR Code",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "2. CameraX Capture",
                        value = "Select event -> Capture locally",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "3. DSLR & SD Card Import",
                        value = "Uploads -> SAF / Photo Picker",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "4. Guest Face Search",
                        value = "Requires explicit guest consent"
                    )
                }
            }

            // 9. About Jummo Photos AI Row
            item {
                LuxurySettingsExpandableCard(
                    icon = Icons.Default.Info,
                    iconTint = JummoVioletBright,
                    title = "About Jummo Photos AI",
                    subtitle = "v1.0.0 (Pro) • ${events.size} events • ${uploads.size} photos",
                    expanded = expandedSection == "about",
                    onToggleExpand = {
                        expandedSection = if (expandedSection == "about") null else "about"
                    }
                ) {
                    SettingsInfoRow(
                        label = "Version",
                        value = "v1.0.0 (Pro)",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Architecture",
                        value = "Kotlin • Compose • MVVM",
                        showDivider = true
                    )
                    SettingsInfoRow(
                        label = "Cloud & Auth",
                        value = "Firebase Auth & Firestore"
                    )
                }
            }

            // 10. Admin Dashboard Launch Row (Screen 8 Entry Point)
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenAdminDashboard() }
                        .testTag("open_admin_dashboard_row"),
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
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = JummoGoldBright,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Admin Dashboard",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = JummoGoldBright,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Users, events, QR & guest access, face AI, reports & roles",
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

            // 11. Sign Out Button
            item {
                OutlinedButton(
                    onClick = onSignOut,
                    border = BorderStroke(1.dp, JummoErrorRed.copy(alpha = 0.75f)),
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
                        style = MaterialTheme.typography.labelLarge,
                        color = JummoErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun LuxurySettingsExpandableCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    trailingContent: @Composable (() -> Unit)? = null,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    expandedContent: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(0.85.dp, JummoBorderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onToggleExpand() }
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
                            .clip(CircleShape)
                            .background(iconTint.copy(alpha = 0.14f))
                            .border(0.75.dp, iconTint.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            color = Color.White,
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
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = JummoTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            if (expanded) {
                HorizontalDivider(color = JummoBorderSubtle.copy(alpha = 0.55f))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    expandedContent()
                }
            }
        }
    }
}
