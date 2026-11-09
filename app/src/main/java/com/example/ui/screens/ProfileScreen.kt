package com.example.ui.screens

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteSweep
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.model.GuestFaceSearchSession
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.PhotographerProfile
import com.example.ui.components.SectionHeaderRow
import com.example.ui.components.SettingsInfoRow
import com.example.ui.components.SettingsSectionHeader
import com.example.ui.components.StatusBadgePill
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoErrorRed
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoSuccessGreen
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
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
    onSignOut: () -> Unit
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

    var showSupportDetails by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 840.dp)
                .testTag("profile_screen_list"),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                SectionHeaderRow(
                    title = "Settings & Profile",
                    subtitle = "Account, Google Drive, upload preferences, storage, privacy, and support"
                )
            }

            // 1. Account Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsSectionHeader(
                            icon = Icons.Default.Person,
                            title = "Account",
                            accentColor = JummoGoldBright,
                            trailingBadge = {
                                StatusBadgePill(
                                    text = (profile?.role ?: "photographer").uppercase(),
                                    color = JummoGoldBright
                                )
                            }
                        )

                        SettingsInfoRow(
                            label = "Signed-in Email",
                            value = profile?.email?.ifBlank { "Google Account" } ?: "Google Account"
                        )

                        OutlinedTextField(
                            value = displayName,
                            onValueChange = { displayName = it },
                            label = { Text("Display Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = studioName,
                            onValueChange = { studioName = it },
                            label = { Text("Studio Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 2. Google Drive Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsSectionHeader(
                            icon = if (driveConnected) Icons.Default.CloudDone else Icons.Default.CloudSync,
                            title = "Google Drive",
                            accentColor = if (driveConnected) JummoSuccessGreen else JummoGoldBright,
                            trailingBadge = {
                                Switch(
                                    checked = driveConnected,
                                    onCheckedChange = { driveConnected = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = JummoObsidian,
                                        checkedTrackColor = JummoSuccessGreen
                                    )
                                )
                            }
                        )

                        Text(
                            text = "Google Drive sync operates through a server-side OAuth backend so client credentials are never stored in the APK.",
                            style = MaterialTheme.typography.bodySmall,
                            color = JummoTextSecondary
                        )

                        OutlinedTextField(
                            value = driveFolderName,
                            onValueChange = { driveFolderName = it },
                            label = { Text("Root Drive Folder Name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 3. Upload Preferences Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SettingsSectionHeader(
                            icon = Icons.Default.CloudUpload,
                            title = "Upload Preferences",
                            accentColor = JummoVioletBright,
                            trailingBadge = {
                                Switch(
                                    checked = autoUpload,
                                    onCheckedChange = { autoUpload = it },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = JummoObsidian,
                                        checkedTrackColor = JummoGoldBright
                                    )
                                )
                            }
                        )

                        Text(
                            text = "Configure WorkManager background upload constraints for newly captured and imported photos.",
                            style = MaterialTheme.typography.bodySmall,
                            color = JummoTextSecondary
                        )

                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            FilterChip(
                                selected = wifiOnly,
                                onClick = { wifiOnly = !wifiOnly },
                                label = { Text("Wi-Fi Only") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = JummoVioletBright,
                                    selectedLabelColor = Color.White
                                )
                            )
                            FilterChip(
                                selected = batteryNotLow,
                                onClick = { batteryNotLow = !batteryNotLow },
                                label = { Text("Battery Not Low") },
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
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("save_profile_button")
                        ) {
                            Icon(imageVector = Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Account, Drive & Upload Settings", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 4. Storage Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingsSectionHeader(
                            icon = Icons.Default.Storage,
                            title = "Storage",
                            accentColor = JummoGoldBright
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SettingsInfoRow(
                            label = "Local Original Files (filesDir/photos)",
                            value = "${storageMetrics.localPhotoFileCount} file(s)"
                        )
                        SettingsInfoRow(
                            label = "Local Photo Storage Used",
                            value = storageMetrics.formatBytes(storageMetrics.localPhotosBytes)
                        )
                        SettingsInfoRow(
                            label = "Tracked Upload Queue Size",
                            value = storageMetrics.formatBytes(storageMetrics.trackedUploadsBytes)
                        )
                        if (storageMetrics.availableDeviceBytes > 0L) {
                            SettingsInfoRow(
                                label = "Available Device Storage",
                                value = storageMetrics.formatBytes(storageMetrics.availableDeviceBytes),
                                valueColor = JummoSuccessGreen
                            )
                        }
                        SettingsInfoRow(
                            label = "Original Photo Retention",
                            value = "Never Auto-Deleted",
                            valueColor = JummoGoldBright
                        )
                    }
                }
            }

            // 5. Privacy Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingsSectionHeader(
                            icon = Icons.Default.PrivacyTip,
                            title = "Privacy",
                            accentColor = JummoVioletBright
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SettingsInfoRow(
                            label = "Guest Face-Search Consent Sessions",
                            value = "${guestSessions.size} total"
                        )
                        SettingsInfoRow(
                            label = "Purged Guest Sessions",
                            value = "${guestSessions.count { it.matchStatus == "DataDeleted" }}"
                        )
                        SettingsInfoRow(
                            label = "Event Data Isolation",
                            value = "Enforced per Event ID",
                            valueColor = JummoSuccessGreen
                        )
                        Text(
                            text = "Face search requires explicit guest consent and is isolated per event. No fake client-side biometric matching is performed.",
                            style = MaterialTheme.typography.bodySmall,
                            color = JummoTextSecondary
                        )

                        val activeGuestSessions = guestSessions.filter { it.matchStatus != "DataDeleted" }
                        if (activeGuestSessions.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedButton(
                                onClick = {
                                    activeGuestSessions.forEach { onDeleteGuestFaceData(it) }
                                },
                                border = BorderStroke(0.75.dp, JummoErrorRed.copy(alpha = 0.7f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = JummoErrorRed
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Delete Active Guest Face-Search Data (${activeGuestSessions.size})",
                                    color = JummoErrorRed
                                )
                            }
                        }
                    }
                }
            }

            // 6. Permissions Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SettingsSectionHeader(
                            icon = Icons.Default.Security,
                            title = "Permissions",
                            accentColor = JummoGoldBright
                        )
                        SettingsInfoRow(
                            label = "Camera (CameraX Capture)",
                            value = if (cameraPermissionGranted) "Granted" else "Not Granted",
                            valueColor = if (cameraPermissionGranted) JummoSuccessGreen else JummoGoldBright
                        )
                        SettingsInfoRow(
                            label = "Media & DSLR Import",
                            value = "Zero-Permission Photo Picker & SAF",
                            valueColor = JummoSuccessGreen
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
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
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("System App Settings", color = Color.White)
                            }
                        }
                    }
                }
            }

            // 7. Help and Support Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        SettingsSectionHeader(
                            icon = Icons.AutoMirrored.Filled.HelpOutline,
                            title = "Help and Support",
                            accentColor = JummoVioletBright
                        )
                        Text(
                            text = "Workflow documentation for event creation, DSLR RAW/JPG imports, QR portals, and guest consent management.",
                            style = MaterialTheme.typography.bodySmall,
                            color = JummoTextSecondary
                        )
                        if (showSupportDetails) {
                            HorizontalDivider(color = JummoBorderSubtle)
                            SettingsInfoRow(
                                label = "1. Event & QR Setup",
                                value = "Events tab -> New Event -> QR Code"
                            )
                            SettingsInfoRow(
                                label = "2. CameraX Capture",
                                value = "Select event -> Capture to local storage"
                            )
                            SettingsInfoRow(
                                label = "3. DSLR & SD Card Import",
                                value = "Uploads tab -> SAF / Photo Picker"
                            )
                            SettingsInfoRow(
                                label = "4. Guest Face Search",
                                value = "Requires explicit guest consent"
                            )
                        }
                        OutlinedButton(
                            onClick = { showSupportDetails = !showSupportDetails },
                            border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.6f)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = if (showSupportDetails) "Hide Workflow Guide" else "View Workflow Guide",
                                color = JummoGoldBright
                            )
                        }
                    }
                }
            }

            // 8. About Jummo Photos AI Section
            item {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
                    border = BorderStroke(0.75.dp, JummoBorderSubtle),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.img_app_icon_foreground),
                                contentDescription = "Jummo Photos AI",
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "About Jummo Photos AI",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Professional Event Photography Suite",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = JummoTextSecondary
                                )
                            }
                        }
                        SettingsInfoRow(label = "Version", value = "1.0.0 (Production)")
                        SettingsInfoRow(
                            label = "Architecture",
                            value = "Kotlin • Jetpack Compose • MVVM"
                        )
                        SettingsInfoRow(
                            label = "Cloud & Auth",
                            value = "Firebase Auth & Cloud Firestore"
                        )
                        SettingsInfoRow(
                            label = "Active Events / Uploads",
                            value = "${events.size} events • ${uploads.size} photos"
                        )
                    }
                }
            }

            // 9. Sign Out Section
            item {
                OutlinedButton(
                    onClick = onSignOut,
                    border = BorderStroke(1.dp, JummoErrorRed.copy(alpha = 0.75f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = null,
                        tint = JummoErrorRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign Out",
                        color = JummoErrorRed,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
