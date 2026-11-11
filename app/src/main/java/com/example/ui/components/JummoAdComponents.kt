package com.example.ui.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.model.AdConfiguration
import com.example.data.model.AdminAccountRecord
import com.example.data.model.AdminAuditLog
import com.example.data.model.AdminNotification
import com.example.data.model.RemoteAppSettings
import com.example.ui.theme.JummoBorderSubtle
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoElevatedPurple
import com.example.ui.theme.JummoErrorRed
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoSuccessGreen
import com.example.ui.theme.JummoTextPrimary
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import com.example.ui.theme.JummoVioletPrimary

/**
 * Policy-compliant AdMob placement banner controlled remotely by `AdConfiguration`.
 * Never renders on Camera, Uploads, QR, PIN, or Auth screens.
 */
@Composable
fun JummoManagedBannerAd(
    placement: String,
    adConfig: AdConfiguration,
    modifier: Modifier = Modifier
) {
    if (!adConfig.isBannerAllowedForPlacement(placement)) return

    var dismissedInSession by remember(placement) { mutableStateOf(false) }
    if (dismissedInSession) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("jummo_ad_banner_${placement.lowercase()}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
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
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(JummoVioletPrimary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (adConfig.testMode) "AD • TEST" else "SPONSORED",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Jummo Pro Cloud Prints & Album Lab",
                        style = MaterialTheme.typography.labelMedium,
                        color = JummoTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "AdMob ${placement.replaceFirstChar { it.uppercase() }} • ${adConfig.bannerAdUnitId.takeLast(14)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = JummoTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = { dismissedInSession = true },
                modifier = Modifier.size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss Ad",
                    tint = JummoTextSecondary,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

/**
 * Remote announcement banner controlled in real time from Jummo Admin Panel (`appConfig/remoteSettings`).
 */
@Composable
fun RemoteAnnouncementBanner(
    remoteSettings: RemoteAppSettings,
    modifier: Modifier = Modifier
) {
    if (!remoteSettings.announcementEnabled || remoteSettings.announcementBanner.isBlank()) return

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("remote_announcement_banner"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(1.dp, JummoVioletBright)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Campaign,
                contentDescription = "Announcement",
                tint = JummoVioletPrimary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = remoteSettings.announcementBanner,
                style = MaterialTheme.typography.bodySmall,
                color = JummoTextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Complete Ad Management Card for Admin Dashboard & Jummo Admin Panel.
 */
@Composable
fun AdminAdManagementCard(
    adConfig: AdConfiguration,
    onSaveAdConfig: (AdConfiguration) -> Unit,
    modifier: Modifier = Modifier
) {
    var adsEnabled by remember(adConfig) { mutableStateOf(adConfig.adsEnabled) }
    var bannerEnabled by remember(adConfig) { mutableStateOf(adConfig.bannerEnabled) }
    var interstitialEnabled by remember(adConfig) { mutableStateOf(adConfig.interstitialEnabled) }
    var rewardedEnabled by remember(adConfig) { mutableStateOf(adConfig.rewardedEnabled) }
    var appOpenEnabled by remember(adConfig) { mutableStateOf(adConfig.appOpenEnabled) }
    var homeBannerEnabled by remember(adConfig) { mutableStateOf(adConfig.homeBannerEnabled) }
    var eventListBannerEnabled by remember(adConfig) { mutableStateOf(adConfig.eventListBannerEnabled) }
    var galleryBannerEnabled by remember(adConfig) { mutableStateOf(adConfig.galleryBannerEnabled) }
    var interstitialTransitionEnabled by remember(adConfig) { mutableStateOf(adConfig.interstitialTransitionEnabled) }
    var testMode by remember(adConfig) { mutableStateOf(adConfig.testMode) }
    var adFrequency by remember(adConfig) { mutableLongStateOf(adConfig.adFrequency.coerceIn(1L, 20L)) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(0.75.dp, JummoBorderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_ad_management_card")
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = null,
                        tint = JummoVioletPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Column {
                        Text(
                            text = "Ad Management System (AdMob)",
                            style = MaterialTheme.typography.titleSmall,
                            color = JummoTextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (adsEnabled) "Global Ads ON • Freq $adFrequency • Safe Placements Enforced" else "Global Ads OFF",
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoTextSecondary
                        )
                    }
                }

                StatusBadgePill(
                    text = if (testMode) "TEST MODE" else "PRODUCTION",
                    color = if (testMode) JummoVioletBright else JummoSuccessGreen
                )
            }

            HorizontalDivider(color = JummoBorderSubtle)

            CompactToggleRow(
                label = "Global Ads Master Switch",
                checked = adsEnabled,
                tag = "ad_toggle_global",
                onCheckedChange = { adsEnabled = it }
            )
            CompactToggleRow(
                label = "Banner Ads (Home: $homeBannerEnabled • Events: $eventListBannerEnabled)",
                checked = bannerEnabled,
                tag = "ad_toggle_banner",
                onCheckedChange = { bannerEnabled = it }
            )
            CompactToggleRow(
                label = "Home Dashboard Banner Placement",
                checked = homeBannerEnabled,
                tag = "ad_toggle_home_banner",
                onCheckedChange = { homeBannerEnabled = it }
            )
            CompactToggleRow(
                label = "Event List Banner Placement",
                checked = eventListBannerEnabled,
                tag = "ad_toggle_event_banner",
                onCheckedChange = { eventListBannerEnabled = it }
            )
            CompactToggleRow(
                label = "Event Gallery Banner Placement",
                checked = galleryBannerEnabled,
                tag = "ad_toggle_gallery_banner",
                onCheckedChange = { galleryBannerEnabled = it }
            )
            CompactToggleRow(
                label = "Interstitial Transition Ads",
                checked = interstitialEnabled,
                tag = "ad_toggle_interstitial",
                onCheckedChange = {
                    interstitialEnabled = it
                    interstitialTransitionEnabled = it
                }
            )
            CompactToggleRow(
                label = "Rewarded & App Open Formats",
                checked = rewardedEnabled,
                tag = "ad_toggle_rewarded",
                onCheckedChange = {
                    rewardedEnabled = it
                    appOpenEnabled = it
                }
            )
            CompactToggleRow(
                label = "AdMob Safe Test Units Mode",
                checked = testMode,
                tag = "ad_toggle_test_mode",
                onCheckedChange = { testMode = it }
            )

            // Frequency selector chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Transition Cap (Every N actions)",
                    style = MaterialTheme.typography.labelMedium,
                    color = JummoTextPrimary,
                    fontWeight = FontWeight.Medium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(2L, 4L, 6L, 10L).forEach { freq ->
                        val selected = adFrequency == freq
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) JummoVioletPrimary else JummoObsidian)
                                .border(0.75.dp, JummoBorderSubtle, RoundedCornerShape(8.dp))
                                .clickable { adFrequency = freq }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("ad_freq_chip_$freq")
                        ) {
                            Text(
                                text = "${freq}x",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (selected) Color.White else JummoTextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Text(
                text = "Blocked by Policy: Camera, Photo Upload, QR Scanner, PIN Gate & Auth screens never display ads. External AdMob revenue/eCPM metrics require AdMob Reporting API OAuth.",
                style = MaterialTheme.typography.labelSmall,
                color = JummoTextSecondary
            )

            Button(
                onClick = {
                    onSaveAdConfig(
                        adConfig.copy(
                            adsEnabled = adsEnabled,
                            bannerEnabled = bannerEnabled,
                            interstitialEnabled = interstitialEnabled,
                            rewardedEnabled = rewardedEnabled,
                            appOpenEnabled = appOpenEnabled,
                            homeBannerEnabled = homeBannerEnabled,
                            eventListBannerEnabled = eventListBannerEnabled,
                            galleryBannerEnabled = galleryBannerEnabled,
                            interstitialTransitionEnabled = interstitialTransitionEnabled,
                            adFrequency = adFrequency,
                            testMode = testMode
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = JummoVioletPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_ad_config_button")
            ) {
                Text(
                    text = "Save Ad Configuration",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Admin-Controlled Remote App Settings Card (`appConfig/remoteSettings`).
 */
@Composable
fun AdminRemoteSettingsCard(
    remoteSettings: RemoteAppSettings,
    onSaveRemoteSettings: (RemoteAppSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    var maintenanceMode by remember(remoteSettings) { mutableStateOf(remoteSettings.maintenanceMode) }
    var announcementEnabled by remember(remoteSettings) { mutableStateOf(remoteSettings.announcementEnabled) }
    var announcementBanner by remember(remoteSettings) { mutableStateOf(remoteSettings.announcementBanner) }
    var minVersion by remember(remoteSettings) { mutableStateOf(remoteSettings.minimumSupportedVersion) }
    var forceUpdate by remember(remoteSettings) { mutableStateOf(remoteSettings.forceUpdateRequired) }
    var guestDownloadDefault by remember(remoteSettings) { mutableStateOf(remoteSettings.guestDownloadDefault) }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(0.75.dp, JummoBorderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_remote_settings_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SettingsRemote,
                    contentDescription = null,
                    tint = JummoVioletPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Remote App Settings & Version Control",
                        style = MaterialTheme.typography.titleSmall,
                        color = JummoTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Min Version $minVersion • Maintenance: ${if (maintenanceMode) "ON" else "OFF"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = JummoTextSecondary
                    )
                }
            }

            HorizontalDivider(color = JummoBorderSubtle)

            CompactToggleRow(
                label = "Global Maintenance Mode",
                checked = maintenanceMode,
                tag = "remote_toggle_maintenance",
                onCheckedChange = { maintenanceMode = it }
            )
            CompactToggleRow(
                label = "Live Announcement Banner",
                checked = announcementEnabled,
                tag = "remote_toggle_announcement",
                onCheckedChange = { announcementEnabled = it }
            )
            CompactToggleRow(
                label = "Force Minimum App Version ($minVersion)",
                checked = forceUpdate,
                tag = "remote_toggle_force_update",
                onCheckedChange = { forceUpdate = it }
            )
            CompactToggleRow(
                label = "Allow Guest High-Res Downloads by Default",
                checked = guestDownloadDefault,
                tag = "remote_toggle_guest_download",
                onCheckedChange = { guestDownloadDefault = it }
            )

            OutlinedTextField(
                value = announcementBanner,
                onValueChange = { announcementBanner = it },
                label = { Text("Announcement Banner Message") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("remote_announcement_input")
            )

            Button(
                onClick = {
                    onSaveRemoteSettings(
                        remoteSettings.copy(
                            maintenanceMode = maintenanceMode,
                            announcementEnabled = announcementEnabled,
                            announcementBanner = announcementBanner,
                            minimumSupportedVersion = minVersion,
                            forceUpdateRequired = forceUpdate,
                            guestDownloadDefault = guestDownloadDefault
                        )
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = JummoVioletPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("save_remote_settings_button")
            ) {
                Text(
                    text = "Publish Remote Settings",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Notification Management Card (`/admin_notifications/{notificationId}`).
 */
@Composable
fun AdminNotificationManagementCard(
    notifications: List<AdminNotification>,
    onSendNotification: (String, String, String, String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Announcement") }
    var audience by remember { mutableStateOf("AllUsers") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(0.75.dp, JummoBorderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_notification_management_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    tint = JummoVioletPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Notification & Broadcast Center",
                        style = MaterialTheme.typography.titleSmall,
                        color = JummoTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${notifications.size} broadcast(s) in history",
                        style = MaterialTheme.typography.labelSmall,
                        color = JummoTextSecondary
                    )
                }
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Notification Title") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_notif_title_input")
            )

            OutlinedTextField(
                value = message,
                onValueChange = { message = it },
                label = { Text("Broadcast Message") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("admin_notif_message_input")
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Announcement", "Maintenance", "VersionNotice").forEach { cat ->
                    val selected = category == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) JummoVioletPrimary else JummoObsidian)
                            .border(0.75.dp, JummoBorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { category = cat }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = cat,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) Color.White else JummoTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Button(
                onClick = {
                    val safeTitle = title.trim().ifBlank { "Studio Platform Update" }
                    val safeMsg = message.trim().ifBlank { "New Google Drive & QR Face Search improvements are live." }
                    onSendNotification(safeTitle, safeMsg, category, audience, "Immediate")
                    title = ""
                    message = ""
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = JummoVioletPrimary,
                    contentColor = Color.White
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("send_admin_notification_button")
            ) {
                Text(
                    text = "Dispatch Notification",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            notifications.take(3).forEach { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(JummoObsidian)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = JummoTextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${item.category} • ${item.targetAudience} • ${item.message}",
                            style = MaterialTheme.typography.labelSmall,
                            color = JummoTextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    StatusBadgePill(text = item.status, color = JummoSuccessGreen)
                }
            }
        }
    }
}

/**
 * Admin Role-Based Access Control (RBAC) & Immutable Audit Trail Card.
 */
@Composable
fun AdminRbacAndAuditCard(
    adminAccounts: List<AdminAccountRecord>,
    auditLogs: List<AdminAuditLog>,
    onSaveAdminAccount: (AdminAccountRecord) -> Unit,
    modifier: Modifier = Modifier
) {
    var emailInput by remember { mutableStateOf("") }
    var nameInput by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf("Admin") }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = JummoCardPurple),
        border = BorderStroke(0.75.dp, JummoBorderSubtle),
        modifier = modifier
            .fillMaxWidth()
            .testTag("admin_rbac_security_card")
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = JummoVioletPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = "Admin RBAC & Security Audit Logs",
                        style = MaterialTheme.typography.titleSmall,
                        color = JummoTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Owner: Meraj Sir • ${adminAccounts.size + 1} verified admin(s) • ${auditLogs.size} audit log(s)",
                        style = MaterialTheme.typography.labelSmall,
                        color = JummoTextSecondary
                    )
                }
            }

            HorizontalDivider(color = JummoBorderSubtle)

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("SuperAdmin", "Admin", "SupportAdmin", "Analyst").forEach { role ->
                    val selected = selectedRole == role
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) JummoVioletPrimary else JummoObsidian)
                            .border(0.75.dp, JummoBorderSubtle, RoundedCornerShape(8.dp))
                            .clickable { selectedRole = role }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("rbac_role_chip_$role")
                    ) {
                        Text(
                            text = role,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (selected) Color.White else JummoTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = { emailInput = it },
                    label = { Text("Admin Email") },
                    singleLine = true,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("rbac_email_input")
                )
                Button(
                    onClick = {
                        val cleanEmail = emailInput.trim().ifBlank { "operator@jummophotos.ai" }
                        val cleanId = "adm_" + cleanEmail.lowercase().replace(Regex("[^a-z0-9]"), "_")
                        onSaveAdminAccount(
                            AdminAccountRecord(
                                id = cleanId,
                                userId = cleanId,
                                email = cleanEmail,
                                displayName = nameInput.trim().ifBlank { cleanEmail.substringBefore("@") },
                                role = selectedRole,
                                isActive = true
                            )
                        )
                        emailInput = ""
                        nameInput = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = JummoVioletPrimary,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 10.dp),
                    modifier = Modifier.testTag("rbac_grant_role_button")
                ) {
                    Text(
                        text = "Grant Role",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (auditLogs.isNotEmpty()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = JummoVioletPrimary,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = "Recent Immutable Security Audit Logs",
                        style = MaterialTheme.typography.labelMedium,
                        color = JummoTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                auditLogs.take(4).forEach { log ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(JummoObsidian)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${log.actionType} • ${log.actorEmail}",
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = log.summary,
                                style = MaterialTheme.typography.labelSmall,
                                color = JummoTextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompactToggleRow(
    label: String,
    checked: Boolean,
    tag: String,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = JummoTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = JummoVioletPrimary
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}
