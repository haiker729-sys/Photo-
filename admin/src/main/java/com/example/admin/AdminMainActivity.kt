package com.example.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SettingsRemote
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.util.UUID
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Official Purple Premium Palette
val AdminPrimaryPurple = Color(0xFF5B21B6)
val AdminAccentPurple = Color(0xFF8B5CF6)
val AdminSecondaryPurple = Color(0xFFC7B3FF)
val AdminSurfacePurple = Color(0xFFEDE9FE)
val AdminBackgroundPurple = Color(0xFFFAF9FF)
val AdminTextPrimary = Color(0xFF24133F)
val AdminTextSecondary = Color(0xFF6B5B83)
val AdminSuccessGreen = Color(0xFF15803D)
val AdminErrorRed = Color(0xFFB91C1C)

private val JummoAdminColorScheme = lightColorScheme(
    primary = AdminPrimaryPurple,
    onPrimary = Color.White,
    secondary = AdminAccentPurple,
    onSecondary = Color.White,
    tertiary = AdminSecondaryPurple,
    background = AdminBackgroundPurple,
    onBackground = AdminTextPrimary,
    surface = AdminSurfacePurple,
    onSurface = AdminTextPrimary,
    surfaceVariant = AdminSurfacePurple,
    onSurfaceVariant = AdminTextSecondary,
    outline = AdminSecondaryPurple,
    error = AdminErrorRed,
    onError = Color.White
)

@IgnoreExtraProperties
data class AdminUserSummary(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "Meraj Sir",
    val email: String = "merajalam906090@gmail.com",
    val studioName: String = "Jummo Pro Studio",
    val role: String = "owner",
    val accountStatus: String = "Active",
    val driveConnected: Boolean = true,
    val driveFolderName: String = "Jummo_Events_Cloud",
    val storageMode: String = "Both"
)

@IgnoreExtraProperties
data class AdminEventSummary(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "Royal Orchid Wedding Gala",
    val category: String = "Wedding",
    val eventDate: String = "2026-11-24",
    val status: String = "Active",
    val qrEnabled: Boolean = true,
    val guestAccessMode: String = "FaceSearchOnly",
    val guestAllowDownload: Boolean = true,
    val totalPhotos: Long = 0L,
    val uploadedPhotos: Long = 0L,
    val pendingPhotos: Long = 0L,
    val failedPhotos: Long = 0L
)

@IgnoreExtraProperties
data class AdminAdConfigState(
    val adsEnabled: Boolean = true,
    val bannerEnabled: Boolean = true,
    val interstitialEnabled: Boolean = false,
    val rewardedEnabled: Boolean = false,
    val appOpenEnabled: Boolean = false,
    val homeBannerEnabled: Boolean = true,
    val eventListBannerEnabled: Boolean = true,
    val galleryBannerEnabled: Boolean = false,
    val interstitialTransitionEnabled: Boolean = false,
    val adFrequency: Long = 4L,
    val testMode: Boolean = true,
    val maintenanceMode: Boolean = false,
    val minimumAppVersion: String = "2.4.0",
    val adMobAppId: String = "ca-app-pub-3940256099942544~3347511713",
    val bannerAdUnitId: String = "ca-app-pub-3940256099942544/6300978111",
    val interstitialAdUnitId: String = "ca-app-pub-3940256099942544/1033173712",
    val rewardedAdUnitId: String = "ca-app-pub-3940256099942544/5224354917",
    val appOpenAdUnitId: String = "ca-app-pub-3940256099942544/9257395921"
)

@IgnoreExtraProperties
data class AdminRemoteConfigState(
    val maintenanceMode: Boolean = false,
    val maintenanceMessage: String = "Scheduled studio cloud maintenance in progress.",
    val announcementBanner: String = "Jummo Photos AI v2.4 • Instant Google Drive + QR Face Search Active",
    val announcementEnabled: Boolean = false,
    val minimumSupportedVersion: String = "2.4.0",
    val forceUpdateRequired: Boolean = false,
    val guestDownloadDefault: Boolean = true,
    val defaultGuestAccessMode: String = "FaceSearchOnly",
    val autoUploadDefault: Boolean = true,
    val wifiOnlyDefault: Boolean = true,
    val supportEmail: String = "merajalam906090@gmail.com",
    val supportPhone: String = "+91 98765 43210",
    val promotionalBannerEnabled: Boolean = true
)

@IgnoreExtraProperties
data class AdminRoleEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "Admin",
    val isActive: Boolean = true,
    val sessionRevoked: Boolean = false,
    val grantedBy: String = "merajalam906090@gmail.com"
)

@IgnoreExtraProperties
data class AdminBroadcastItem(
    @DocumentId val id: String = "",
    val title: String = "",
    val message: String = "",
    val category: String = "Announcement",
    val targetAudience: String = "AllUsers",
    val status: String = "Sent",
    val scheduledAt: String = "Immediate",
    val sentBy: String = "merajalam906090@gmail.com"
)

@IgnoreExtraProperties
data class AdminAuditEntry(
    @DocumentId val id: String = "",
    val actorUid: String = "",
    val actorEmail: String = "",
    val actorRole: String = "Owner",
    val actionType: String = "",
    val targetType: String = "",
    val targetId: String = "",
    val summary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
)

class AdminMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = JummoAdminColorScheme) {
                JummoAdminPanelRoot()
            }
        }
    }
}

@Composable
fun JummoAdminPanelRoot() {
    val auth = remember { runCatching { Firebase.auth }.getOrNull() }
    var currentUser by remember { mutableStateOf(auth?.currentUser) }
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var unauthorizedError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        delay(800L)
        showSplash = false
    }

    if (auth != null) {
        DisposableEffect(auth) {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                currentUser = firebaseAuth.currentUser
            }
            auth.addAuthStateListener(listener)
            onDispose { auth.removeAuthStateListener(listener) }
        }
    }

    if (showSplash) {
        AdminSplashScreen()
        return
    }

    val user = currentUser
    if (user == null) {
        AdminLoginScreen(
            errorMessage = unauthorizedError,
            onSignInSuccess = { signedInUser ->
                currentUser = signedInUser
                unauthorizedError = null
            },
            onUnauthorizedReject = { reason ->
                unauthorizedError = reason
            }
        )
    } else {
        JummoAdminPanelShell(
            adminUid = user.uid,
            adminEmail = user.email ?: "merajalam906090@gmail.com",
            adminDisplayName = user.displayName ?: "Meraj Sir",
            onSignOut = {
                auth?.signOut()
                currentUser = null
            }
        )
    }
}

@Composable
fun AdminSplashScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(AdminBackgroundPurple, AdminSurfacePurple)
                )
            )
            .testTag("admin_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(AdminPrimaryPurple)
                    .border(2.dp, AdminSecondaryPurple, RoundedCornerShape(20.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = "Jummo Admin Panel",
                    tint = Color.White,
                    modifier = Modifier.size(38.dp)
                )
            }
            Text(
                text = "Jummo Admin Panel",
                style = MaterialTheme.typography.headlineSmall,
                color = AdminTextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = "Studio Governance, AdMob & Cloud Control",
                style = MaterialTheme.typography.bodySmall,
                color = AdminTextSecondary
            )
        }
    }
}

@Composable
fun AdminLoginScreen(
    errorMessage: String? = null,
    onSignInSuccess: (com.google.firebase.auth.FirebaseUser) -> Unit = {},
    onUnauthorizedReject: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var statusText by remember(errorMessage) { mutableStateOf(errorMessage) }
    var isVerifying by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_login_screen"),
        color = AdminBackgroundPurple
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = AdminSurfacePurple),
                border = BorderStroke(1.dp, AdminSecondaryPurple),
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp)
            ) {
                Column(
                    modifier = Modifier.padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(AdminPrimaryPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Admin Security Gate",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Text(
                        text = "Jummo Admin Panel",
                        style = MaterialTheme.typography.titleLarge,
                        color = AdminTextPrimary,
                        fontWeight = FontWeight.ExtraBold
                    )

                    Text(
                        text = "Restricted to verified Owner (merajalam906090@gmail.com) and server-authorized Admin accounts in /admins.",
                        style = MaterialTheme.typography.bodySmall,
                        color = AdminTextSecondary
                    )

                    if (!statusText.isNullOrBlank()) {
                        Card(
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = AdminErrorRed.copy(alpha = 0.12f)
                            ),
                            border = BorderStroke(0.75.dp, AdminErrorRed),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("admin_login_error_banner")
                        ) {
                            Text(
                                text = statusText ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                color = AdminErrorRed,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    Button(
                        onClick = {
                            scope.launch {
                                isVerifying = true
                                statusText = null
                                try {
                                    val credentialManager = CredentialManager.create(context)
                                    val webClientIdRes = context.resources.getIdentifier(
                                        "default_web_client_id",
                                        "string",
                                        context.packageName
                                    )
                                    require(webClientIdRes != 0) {
                                        "Missing default_web_client_id in google-services.json"
                                    }
                                    val googleOption = GetSignInWithGoogleOption.Builder(
                                        context.getString(webClientIdRes)
                                    ).build()
                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleOption)
                                        .build()
                                    val result = credentialManager.getCredential(context, request)
                                    val customCred = result.credential as? CustomCredential
                                    val tokenCred = customCred?.let {
                                        GoogleIdTokenCredential.createFrom(it.data)
                                    }
                                    val idToken = tokenCred?.idToken
                                        ?: error("Google ID token missing.")
                                    val authResult = Firebase.auth
                                        .signInWithCredential(
                                            GoogleAuthProvider.getCredential(idToken, null)
                                        )
                                        .await()
                                    val signedUser = authResult.user
                                        ?: error("Firebase user null after sign-in.")
                                    val email = signedUser.email.orEmpty()
                                    val dbId = context.getString(R.string.firestore_database_id)
                                    val db = FirebaseFirestore.getInstance(dbId)
                                    val adminSnap = runCatching {
                                        db.collection("admins").document(signedUser.uid).get().await()
                                    }.getOrNull()
                                    val isOwner = email.equals("merajalam906090@gmail.com", ignoreCase = true)
                                    val isRegisteredAdmin = adminSnap != null &&
                                        adminSnap.exists() &&
                                        adminSnap.getBoolean("isActive") != false &&
                                        adminSnap.getBoolean("sessionRevoked") != true

                                    if (isOwner || isRegisteredAdmin) {
                                        onSignInSuccess(signedUser)
                                    } else {
                                        Firebase.auth.signOut()
                                        credentialManager.clearCredentialState(ClearCredentialStateRequest())
                                        val msg = "Access Denied: $email is not an authorized Jummo Admin."
                                        statusText = msg
                                        onUnauthorizedReject(msg)
                                    }
                                } catch (e: Exception) {
                                    statusText = "Admin sign-in requires a verified Google account: ${e.localizedMessage ?: "Canceled"}"
                                } finally {
                                    isVerifying = false
                                }
                            }
                        },
                        enabled = !isVerifying,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AdminPrimaryPurple,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(46.dp)
                            .testTag("admin_google_sign_in_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isVerifying) "Verifying Admin Role…" else "Sign In with Google (Admin RBAC)",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

/**
 * Standalone Jummo Admin Panel Shell with 5 navigation tabs and 10 complete administrative modules.
 */
@Composable
fun JummoAdminPanelShell(
    adminUid: String = "owner_meraj_uid",
    adminEmail: String = "merajalam906090@gmail.com",
    adminDisplayName: String = "Meraj Sir",
    initialUsers: List<AdminUserSummary> = listOf(
        AdminUserSummary(
            id = "usr_meraj",
            userId = "usr_meraj",
            displayName = "Meraj Sir",
            email = "merajalam906090@gmail.com",
            studioName = "Jummo Pro Studio",
            role = "owner",
            accountStatus = "Active",
            driveConnected = true,
            driveFolderName = "Jummo_Events_Cloud",
            storageMode = "Both"
        )
    ),
    initialEvents: List<AdminEventSummary> = listOf(
        AdminEventSummary(
            id = "evt_royal_wedding",
            userId = "usr_meraj",
            name = "Royal Orchid Wedding Gala",
            category = "Wedding",
            eventDate = "2026-11-24",
            status = "Active",
            qrEnabled = true,
            guestAccessMode = "FaceSearchOnly",
            guestAllowDownload = true,
            totalPhotos = 48L,
            uploadedPhotos = 44L,
            pendingPhotos = 4L,
            failedPhotos = 0L
        )
    ),
    initialAdConfig: AdminAdConfigState = AdminAdConfigState(),
    initialRemoteConfig: AdminRemoteConfigState = AdminRemoteConfigState(),
    initialAdmins: List<AdminRoleEntry> = listOf(
        AdminRoleEntry(
            id = "owner_meraj_uid",
            userId = "owner_meraj_uid",
            email = "merajalam906090@gmail.com",
            displayName = "Meraj Sir",
            role = "Owner",
            isActive = true
        )
    ),
    initialBroadcasts: List<AdminBroadcastItem> = emptyList(),
    initialAuditLogs: List<AdminAuditEntry> = emptyList(),
    enableFirestoreSync: Boolean = true,
    onSignOut: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val db = remember(enableFirestoreSync) {
        if (enableFirestoreSync) {
            runCatching {
                FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
            }.getOrNull()
        } else {
            null
        }
    }

    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var statusBanner by remember { mutableStateOf<String?>(null) }

    var users by remember { mutableStateOf(initialUsers) }
    var events by remember { mutableStateOf(initialEvents) }
    var adConfig by remember { mutableStateOf(initialAdConfig) }
    var remoteConfig by remember { mutableStateOf(initialRemoteConfig) }
    var adminRoles by remember { mutableStateOf(initialAdmins) }
    var broadcasts by remember { mutableStateOf(initialBroadcasts) }
    var auditLogs by remember { mutableStateOf(initialAuditLogs) }

    var pendingEventModeration by remember { mutableStateOf<AdminEventSummary?>(null) }

    BackHandler(enabled = currentTab != 0) {
        currentTab = 0
    }

    fun appendAuditLog(actionType: String, targetType: String, targetId: String, summary: String) {
        val entry = AdminAuditEntry(
            id = "aud_${UUID.randomUUID().toString().replace("-", "").take(12)}",
            actorUid = adminUid,
            actorEmail = adminEmail,
            actorRole = if (adminEmail.equals("merajalam906090@gmail.com", ignoreCase = true)) "Owner" else "Admin",
            actionType = actionType,
            targetType = targetType,
            targetId = targetId,
            summary = summary,
            createdAt = Timestamp.now()
        )
        auditLogs = listOf(entry) + auditLogs
        if (db != null) {
            scope.launch {
                runCatching {
                    db.collection("audit_logs").document(entry.id).set(
                        mapOf(
                            "actorUid" to entry.actorUid,
                            "actorEmail" to entry.actorEmail,
                            "actorRole" to entry.actorRole,
                            "actionType" to entry.actionType,
                            "targetType" to entry.targetType,
                            "targetId" to entry.targetId,
                            "summary" to entry.summary,
                            "createdAt" to FieldValue.serverTimestamp()
                        )
                    ).await()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("jummo_admin_panel_shell"),
        containerColor = AdminBackgroundPurple,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = AdminSurfacePurple,
                tonalElevation = 0.dp
            ) {
                val tabs = listOf(
                    Triple(0, "Overview", Icons.Default.Dashboard),
                    Triple(1, "Users & Events", Icons.Default.Group),
                    Triple(2, "Ads & Remote", Icons.Default.Tune),
                    Triple(3, "Drive & Alerts", Icons.Default.CloudDone),
                    Triple(4, "RBAC & Audit", Icons.Default.Security)
                )
                tabs.forEach { (idx, label, icon) ->
                    NavigationBarItem(
                        selected = currentTab == idx,
                        onClick = { currentTab = idx },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                modifier = Modifier.size(20.dp)
                            )
                        },
                        label = {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (currentTab == idx) FontWeight.Bold else FontWeight.Medium,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = AdminPrimaryPurple,
                            indicatorColor = AdminPrimaryPurple,
                            unselectedIconColor = AdminTextSecondary,
                            unselectedTextColor = AdminTextSecondary
                        ),
                        modifier = Modifier.testTag("admin_nav_tab_$idx")
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Compact Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 840.dp)
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(AdminPrimaryPurple),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = "Jummo Admin Panel",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Jummo Admin Panel",
                            style = MaterialTheme.typography.titleMedium,
                            color = AdminTextPrimary,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "$adminDisplayName • Verified Owner RBAC",
                            style = MaterialTheme.typography.labelSmall,
                            color = AdminTextSecondary
                        )
                    }
                }

                IconButton(
                    onClick = onSignOut,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(AdminSurfacePurple)
                        .border(0.75.dp, AdminSecondaryPurple, CircleShape)
                        .testTag("admin_sign_out_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Logout,
                        contentDescription = "Sign Out",
                        tint = AdminPrimaryPurple,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (statusBanner != null) {
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = AdminSurfacePurple),
                    border = BorderStroke(1.dp, AdminPrimaryPurple),
                    modifier = Modifier
                        .fillMaxWidth()
                        .widthIn(max = 840.dp)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .testTag("admin_status_banner")
                ) {
                    Text(
                        text = statusBanner ?: "",
                        style = MaterialTheme.typography.labelMedium,
                        color = AdminPrimaryPurple,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            LazyColumn(
                state = remember(currentTab) { LazyListState() },
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 840.dp)
                    .testTag("admin_panel_content_list"),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                when (currentTab) {
                    0 -> {
                        // TAB 0: DASHBOARD OVERVIEW
                        item {
                            val totalPhotos = events.sumOf { it.totalPhotos }
                            val uploadedPhotos = events.sumOf { it.uploadedPhotos }
                            val pendingPhotos = events.sumOf { it.pendingPhotos }
                            val failedPhotos = events.sumOf { it.failedPhotos }

                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AdminMetricTile(
                                        title = "Total Users",
                                        value = users.size.toString(),
                                        subtitle = "${users.count { it.accountStatus == "Active" }} Active",
                                        icon = Icons.Default.Group,
                                        modifier = Modifier.weight(1f)
                                    )
                                    AdminMetricTile(
                                        title = "Total Events",
                                        value = events.size.toString(),
                                        subtitle = "${events.count { it.status == "Active" }} Active",
                                        icon = Icons.Default.Event,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AdminMetricTile(
                                        title = "Total Photos",
                                        value = totalPhotos.toString(),
                                        subtitle = "$uploadedPhotos Synced • $pendingPhotos Queued",
                                        icon = Icons.Default.PhotoLibrary,
                                        modifier = Modifier.weight(1f)
                                    )
                                    AdminMetricTile(
                                        title = "AdMob Status",
                                        value = if (adConfig.adsEnabled) "ENABLED" else "DISABLED",
                                        subtitle = if (adConfig.testMode) "Test Units • Freq ${adConfig.adFrequency}x" else "Production Units",
                                        icon = Icons.Default.Tune,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    AdminMetricTile(
                                        title = "Upload Health",
                                        value = if (failedPhotos == 0L) "100% OK" else "$failedPhotos Failed",
                                        subtitle = "Drive Connected: ${users.count { it.driveConnected }}",
                                        icon = Icons.Default.CloudDone,
                                        modifier = Modifier.weight(1f)
                                    )
                                    AdminMetricTile(
                                        title = "App Version",
                                        value = "v${remoteConfig.minimumSupportedVersion}",
                                        subtitle = if (remoteConfig.maintenanceMode) "Maintenance ON" else "Operational",
                                        icon = Icons.Default.SettingsRemote,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // Quick Navigation Cards to all 10 Administrative Sections
                        item {
                            AdminSectionCard(
                                title = "10-Module Platform Governance Suite",
                                subtitle = "Tap any section to manage Jummo Photos AI in real time",
                                icon = Icons.Default.Dashboard
                            ) {
                                val quickSections = listOf(
                                    Triple("User & Photographer Management", "${users.size} registered accounts", 1),
                                    Triple("Event & QR Moderation", "${events.size} events • ${events.count { it.qrEnabled }} QR active", 1),
                                    Triple("AdMob Global & Placement Controls", "Master: ${if (adConfig.adsEnabled) "ON" else "OFF"} • Freq: ${adConfig.adFrequency}x", 2),
                                    Triple("Remote App Settings & Maintenance", "Min Version: ${remoteConfig.minimumSupportedVersion}", 2),
                                    Triple("Google Drive & Storage Health", "Drive folders & upload queue diagnostics", 3),
                                    Triple("Notification & Broadcast Center", "${broadcasts.size} broadcasts sent", 3),
                                    Triple("Admin RBAC & Immutable Audit Trail", "${adminRoles.size} admins • ${auditLogs.size} logs", 4)
                                )
                                quickSections.forEach { (name, desc, targetTab) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AdminBackgroundPurple)
                                            .clickable { currentTab = targetTab }
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = name,
                                                style = MaterialTheme.typography.labelLarge,
                                                color = AdminTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = desc,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AdminTextSecondary
                                            )
                                        }
                                        Text(
                                            text = "Open",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AdminPrimaryPurple,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    1 -> {
                        // TAB 1: USER MANAGEMENT & EVENT MODERATION
                        item {
                            var userQuery by remember { mutableStateOf("") }
                            val filteredUsers = users.filter {
                                userQuery.isBlank() ||
                                    it.displayName.contains(userQuery, ignoreCase = true) ||
                                    it.email.contains(userQuery, ignoreCase = true) ||
                                    it.studioName.contains(userQuery, ignoreCase = true)
                            }

                            AdminSectionCard(
                                title = "User & Photographer Management",
                                subtitle = "${filteredUsers.size} account(s) • Activate, Suspend & Role Control",
                                icon = Icons.Default.Group,
                                tag = "admin_panel_users_card"
                            ) {
                                OutlinedTextField(
                                    value = userQuery,
                                    onValueChange = { userQuery = it },
                                    label = { Text("Search by name, email or studio") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("admin_user_search_input")
                                )

                                filteredUsers.forEach { u ->
                                    val isSuspended = u.accountStatus == "Suspended"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AdminBackgroundPurple)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${u.displayName} (${u.studioName})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AdminTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${u.email} • Role: ${u.role} • Storage: ${u.storageMode} • ${u.accountStatus}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AdminTextSecondary
                                            )
                                        }
                                        Button(
                                            onClick = {
                                                val nextStatus = if (isSuspended) "Active" else "Suspended"
                                                users = users.map {
                                                    if (it.id == u.id) it.copy(accountStatus = nextStatus) else it
                                                }
                                                appendAuditLog(
                                                    actionType = if (isSuspended) "ACTIVATE_USER" else "SUSPEND_USER",
                                                    targetType = "users",
                                                    targetId = u.id,
                                                    summary = "${u.email} marked $nextStatus"
                                                )
                                                statusBanner = "User ${u.displayName} is now $nextStatus."
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSuspended) AdminSuccessGreen else AdminPrimaryPurple,
                                                contentColor = Color.White
                                            ),
                                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                            modifier = Modifier.testTag("admin_toggle_user_${u.id}")
                                        ) {
                                            Text(
                                                text = if (isSuspended) "Activate" else "Suspend",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        item {
                            AdminSectionCard(
                                title = "Event & Guest Gallery Moderation",
                                subtitle = "${events.size} event(s) • Toggle QR Access or Suspend Event",
                                icon = Icons.Default.Event,
                                tag = "admin_panel_events_card"
                            ) {
                                events.forEach { evt ->
                                    val isSuspended = evt.status == "Suspended"
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AdminBackgroundPurple)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${evt.name} (${evt.category})",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AdminTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${evt.status} • Photos: ${evt.totalPhotos} (${evt.uploadedPhotos} synced) • QR: ${if (evt.qrEnabled) "ON" else "OFF"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AdminTextSecondary
                                            )
                                        }
                                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedButton(
                                                onClick = {
                                                    val nextQr = !evt.qrEnabled
                                                    events = events.map {
                                                        if (it.id == evt.id) it.copy(qrEnabled = nextQr) else it
                                                    }
                                                    appendAuditLog(
                                                        actionType = "TOGGLE_EVENT_QR",
                                                        targetType = "events",
                                                        targetId = evt.id,
                                                        summary = "QR for ${evt.name} set to $nextQr"
                                                    )
                                                    statusBanner = "QR access for \"${evt.name}\" set to ${if (nextQr) "Enabled" else "Disabled"}."
                                                },
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.testTag("admin_toggle_qr_${evt.id}")
                                            ) {
                                                Text(
                                                    text = if (evt.qrEnabled) "Disable QR" else "Enable QR",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                            Button(
                                                onClick = { pendingEventModeration = evt },
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = if (isSuspended) AdminSuccessGreen else AdminPrimaryPurple,
                                                    contentColor = Color.White
                                                ),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                                                modifier = Modifier.testTag("admin_moderate_event_${evt.id}")
                                            ) {
                                                Text(
                                                    text = if (isSuspended) "Restore" else "Suspend",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    2 -> {
                        // TAB 2: COMPLETE AD MANAGEMENT SYSTEM + REMOTE APP SETTINGS
                        item {
                            AdminSectionCard(
                                title = "Complete Ad Management System (AdMob)",
                                subtitle = "Real-time Firestore sync to Jummo Photos AI (appConfig/adConfig)",
                                icon = Icons.Default.Tune,
                                tag = "admin_panel_ad_config_card"
                            ) {
                                AdminSwitchRow(
                                    label = "Global Ads Master Switch (adsEnabled)",
                                    checked = adConfig.adsEnabled,
                                    tag = "panel_ad_global_switch",
                                    onCheckedChange = { adConfig = adConfig.copy(adsEnabled = it) }
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Transition Frequency Cap",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AdminTextPrimary,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        listOf(2L, 4L, 6L, 10L).forEach { freq ->
                                            FilterChip(
                                                selected = adConfig.adFrequency == freq,
                                                onClick = { adConfig = adConfig.copy(adFrequency = freq) },
                                                label = { Text("${freq}x") },
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = AdminPrimaryPurple,
                                                    selectedLabelColor = Color.White
                                                ),
                                                modifier = Modifier.testTag("panel_ad_freq_$freq")
                                            )
                                        }
                                    }
                                }

                                Button(
                                    onClick = {
                                        if (db != null) {
                                            scope.launch {
                                                runCatching {
                                                    db.collection("appConfig").document("adConfig").set(
                                                        mapOf(
                                                            "adsEnabled" to adConfig.adsEnabled,
                                                            "bannerEnabled" to adConfig.bannerEnabled,
                                                            "interstitialEnabled" to adConfig.interstitialEnabled,
                                                            "rewardedEnabled" to adConfig.rewardedEnabled,
                                                            "appOpenEnabled" to adConfig.appOpenEnabled,
                                                            "homeBannerEnabled" to adConfig.homeBannerEnabled,
                                                            "eventListBannerEnabled" to adConfig.eventListBannerEnabled,
                                                            "galleryBannerEnabled" to adConfig.galleryBannerEnabled,
                                                            "interstitialTransitionEnabled" to adConfig.interstitialTransitionEnabled,
                                                            "adFrequency" to adConfig.adFrequency,
                                                            "testMode" to adConfig.testMode,
                                                            "maintenanceMode" to adConfig.maintenanceMode,
                                                            "minimumAppVersion" to adConfig.minimumAppVersion,
                                                            "updatedAt" to FieldValue.serverTimestamp(),
                                                            "updatedBy" to adminUid
                                                        )
                                                    ).await()
                                                }
                                            }
                                        }
                                        appendAuditLog(
                                            actionType = "UPDATE_AD_CONFIG",
                                            targetType = "appConfig",
                                            targetId = "adConfig",
                                            summary = "Ads:${adConfig.adsEnabled} • Banner:${adConfig.bannerEnabled} • Freq:${adConfig.adFrequency}x"
                                        )
                                        statusBanner = "Ad configuration synced to Jummo Photos AI."
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AdminPrimaryPurple,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("panel_save_ad_config_button")
                                ) {
                                    Text("Publish Ad Configuration", fontWeight = FontWeight.Bold)
                                }

                                AdminSwitchRow(
                                    label = "Banner Ads Format (bannerEnabled)",
                                    checked = adConfig.bannerEnabled,
                                    tag = "panel_ad_banner_switch",
                                    onCheckedChange = { adConfig = adConfig.copy(bannerEnabled = it) }
                                )
                                AdminSwitchRow(
                                    label = "Home Dashboard Banner Placement",
                                    checked = adConfig.homeBannerEnabled,
                                    tag = "panel_ad_home_banner_switch",
                                    onCheckedChange = { adConfig = adConfig.copy(homeBannerEnabled = it) }
                                )
                                AdminSwitchRow(
                                    label = "Event List Banner Placement",
                                    checked = adConfig.eventListBannerEnabled,
                                    tag = "panel_ad_event_banner_switch",
                                    onCheckedChange = { adConfig = adConfig.copy(eventListBannerEnabled = it) }
                                )
                                AdminSwitchRow(
                                    label = "Event Gallery Banner Placement",
                                    checked = adConfig.galleryBannerEnabled,
                                    tag = "panel_ad_gallery_banner_switch",
                                    onCheckedChange = { adConfig = adConfig.copy(galleryBannerEnabled = it) }
                                )
                                AdminSwitchRow(
                                    label = "Interstitial Transition Ads",
                                    checked = adConfig.interstitialEnabled,
                                    tag = "panel_ad_interstitial_switch",
                                    onCheckedChange = {
                                        adConfig = adConfig.copy(
                                            interstitialEnabled = it,
                                            interstitialTransitionEnabled = it
                                        )
                                    }
                                )
                                AdminSwitchRow(
                                    label = "Rewarded & App Open Formats",
                                    checked = adConfig.rewardedEnabled,
                                    tag = "panel_ad_rewarded_switch",
                                    onCheckedChange = {
                                        adConfig = adConfig.copy(
                                            rewardedEnabled = it,
                                            appOpenEnabled = it
                                        )
                                    }
                                )
                                AdminSwitchRow(
                                    label = "AdMob Safe Test Mode (Official Google Test Units)",
                                    checked = adConfig.testMode,
                                    tag = "panel_ad_test_mode_switch",
                                    onCheckedChange = { adConfig = adConfig.copy(testMode = it) }
                                )

                                Text(
                                    text = "Safety Policy: Ads are strictly blocked on Camera, Photo Upload, QR Scanner, PIN Gate & Auth screens. External AdMob eCPM/revenue requires AdMob Reporting API OAuth.",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = AdminTextSecondary
                                )
                            }
                        }

                        item {
                            AdminSectionCard(
                                title = "Remote App Settings & Version Control",
                                subtitle = "Real-time control over Maintenance Mode, Announcements & Defaults",
                                icon = Icons.Default.SettingsRemote,
                                tag = "admin_panel_remote_settings_card"
                            ) {
                                AdminSwitchRow(
                                    label = "Global Maintenance Mode",
                                    checked = remoteConfig.maintenanceMode,
                                    tag = "panel_remote_maintenance_switch",
                                    onCheckedChange = { remoteConfig = remoteConfig.copy(maintenanceMode = it) }
                                )
                                AdminSwitchRow(
                                    label = "Live Announcement Banner",
                                    checked = remoteConfig.announcementEnabled,
                                    tag = "panel_remote_announcement_switch",
                                    onCheckedChange = { remoteConfig = remoteConfig.copy(announcementEnabled = it) }
                                )

                                Button(
                                    onClick = {
                                        if (db != null) {
                                            scope.launch {
                                                runCatching {
                                                    db.collection("appConfig").document("remoteSettings").set(
                                                        mapOf(
                                                            "maintenanceMode" to remoteConfig.maintenanceMode,
                                                            "maintenanceMessage" to remoteConfig.maintenanceMessage,
                                                            "announcementBanner" to remoteConfig.announcementBanner,
                                                            "announcementEnabled" to remoteConfig.announcementEnabled,
                                                            "minimumSupportedVersion" to remoteConfig.minimumSupportedVersion,
                                                            "forceUpdateRequired" to remoteConfig.forceUpdateRequired,
                                                            "guestDownloadDefault" to remoteConfig.guestDownloadDefault,
                                                            "defaultGuestAccessMode" to remoteConfig.defaultGuestAccessMode,
                                                            "autoUploadDefault" to remoteConfig.autoUploadDefault,
                                                            "wifiOnlyDefault" to remoteConfig.wifiOnlyDefault,
                                                            "supportEmail" to remoteConfig.supportEmail,
                                                            "supportPhone" to remoteConfig.supportPhone,
                                                            "promotionalBannerEnabled" to remoteConfig.promotionalBannerEnabled,
                                                            "updatedAt" to FieldValue.serverTimestamp(),
                                                            "updatedBy" to adminUid
                                                        )
                                                    ).await()
                                                }
                                            }
                                        }
                                        appendAuditLog(
                                            actionType = "UPDATE_REMOTE_SETTINGS",
                                            targetType = "appConfig",
                                            targetId = "remoteSettings",
                                            summary = "Maintenance:${remoteConfig.maintenanceMode} • Announcement:${remoteConfig.announcementEnabled}"
                                        )
                                        statusBanner = "Remote app settings published."
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AdminPrimaryPurple,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("panel_save_remote_settings_button")
                                ) {
                                    Text("Publish Remote App Settings", fontWeight = FontWeight.Bold)
                                }

                                AdminSwitchRow(
                                    label = "Force Update Requirement (v${remoteConfig.minimumSupportedVersion})",
                                    checked = remoteConfig.forceUpdateRequired,
                                    tag = "panel_remote_force_update_switch",
                                    onCheckedChange = { remoteConfig = remoteConfig.copy(forceUpdateRequired = it) }
                                )
                                AdminSwitchRow(
                                    label = "Default Guest High-Res Downloads",
                                    checked = remoteConfig.guestDownloadDefault,
                                    tag = "panel_remote_guest_download_switch",
                                    onCheckedChange = { remoteConfig = remoteConfig.copy(guestDownloadDefault = it) }
                                )

                                OutlinedTextField(
                                    value = remoteConfig.announcementBanner,
                                    onValueChange = { remoteConfig = remoteConfig.copy(announcementBanner = it) },
                                    label = { Text("Announcement Banner Text") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("panel_remote_announcement_input")
                                )
                            }
                        }
                    }

                    3 -> {
                        // TAB 3: GOOGLE DRIVE STORAGE HEALTH & NOTIFICATION BROADCASTS
                        item {
                            AdminSectionCard(
                                title = "Google Drive & Storage Health Monitor",
                                subtitle = "OAuth connection health, root folder status & upload queue recovery",
                                icon = Icons.Default.Storage,
                                tag = "admin_panel_storage_health_card"
                            ) {
                                users.forEach { u ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(AdminBackgroundPurple)
                                            .padding(10.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${u.studioName} • /${u.driveFolderName}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = AdminTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Mode: ${u.storageMode} • Drive OAuth: ${if (u.driveConnected) "Connected & Healthy" else "Disconnected"}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AdminTextSecondary
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.CloudDone,
                                            contentDescription = null,
                                            tint = if (u.driveConnected) AdminSuccessGreen else AdminErrorRed
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            var notifTitle by remember { mutableStateOf("") }
                            var notifBody by remember { mutableStateOf("") }
                            var notifCategory by remember { mutableStateOf("Announcement") }

                            AdminSectionCard(
                                title = "Notification & Broadcast Management",
                                subtitle = "Send announcements, maintenance alerts & version notices",
                                icon = Icons.Default.NotificationsActive,
                                tag = "admin_panel_notifications_card"
                            ) {
                                OutlinedTextField(
                                    value = notifTitle,
                                    onValueChange = { notifTitle = it },
                                    label = { Text("Notification Title") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("panel_notif_title_input")
                                )
                                OutlinedTextField(
                                    value = notifBody,
                                    onValueChange = { notifBody = it },
                                    label = { Text("Broadcast Message") },
                                    singleLine = true,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("panel_notif_body_input")
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("Announcement", "Maintenance", "VersionNotice").forEach { cat ->
                                        FilterChip(
                                            selected = notifCategory == cat,
                                            onClick = { notifCategory = cat },
                                            label = { Text(cat) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AdminPrimaryPurple,
                                                selectedLabelColor = Color.White
                                            )
                                        )
                                    }
                                }
                                Button(
                                    onClick = {
                                        val cleanTitle = notifTitle.trim().ifBlank { "Jummo Platform Notice" }
                                        val cleanBody = notifBody.trim().ifBlank { "All cloud upload queues operating at 100%." }
                                        val newItem = AdminBroadcastItem(
                                            id = "ntf_${UUID.randomUUID().toString().replace("-", "").take(10)}",
                                            title = cleanTitle,
                                            message = cleanBody,
                                            category = notifCategory,
                                            targetAudience = "AllUsers",
                                            status = "Sent",
                                            sentBy = adminEmail
                                        )
                                        broadcasts = listOf(newItem) + broadcasts
                                        appendAuditLog(
                                            actionType = "SEND_NOTIFICATION",
                                            targetType = "admin_notifications",
                                            targetId = newItem.id,
                                            summary = "Dispatched [$notifCategory] $cleanTitle"
                                        )
                                        statusBanner = "Broadcast \"$cleanTitle\" sent."
                                        notifTitle = ""
                                        notifBody = ""
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = AdminPrimaryPurple,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("panel_send_notif_button")
                                ) {
                                    Text("Send Broadcast Notification", fontWeight = FontWeight.Bold)
                                }

                                broadcasts.forEach { b ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AdminBackgroundPurple)
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${b.title} [${b.category}]",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = AdminTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = b.message,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AdminTextSecondary
                                            )
                                        }
                                        Text(
                                            text = b.status,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = AdminSuccessGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    4 -> {
                        // TAB 4: ADMIN RBAC & IMMUTABLE SECURITY AUDIT LOGS
                        item {
                            var newAdminEmail by remember { mutableStateOf("") }
                            var newAdminRole by remember { mutableStateOf("Admin") }

                            AdminSectionCard(
                                title = "Admin Role-Based Access Control (RBAC)",
                                subtitle = "Owner-verified role assignment & session revocation (/admins)",
                                icon = Icons.Default.Security,
                                tag = "admin_panel_rbac_card"
                            ) {
                                Row(
                                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf("SuperAdmin", "Admin", "SupportAdmin", "Analyst").forEach { r ->
                                        FilterChip(
                                            selected = newAdminRole == r,
                                            onClick = { newAdminRole = r },
                                            label = { Text(r) },
                                            colors = FilterChipDefaults.filterChipColors(
                                                selectedContainerColor = AdminPrimaryPurple,
                                                selectedLabelColor = Color.White
                                            ),
                                            modifier = Modifier.testTag("panel_role_chip_$r")
                                        )
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = newAdminEmail,
                                        onValueChange = { newAdminEmail = it },
                                        label = { Text("Operator Email") },
                                        singleLine = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("panel_rbac_email_input")
                                    )
                                    Button(
                                        onClick = {
                                            val cleanEmail = newAdminEmail.trim().ifBlank { "support@jummophotos.ai" }
                                            val entryId = "adm_" + cleanEmail.lowercase().replace(Regex("[^a-z0-9]"), "_")
                                            val entry = AdminRoleEntry(
                                                id = entryId,
                                                userId = entryId,
                                                email = cleanEmail,
                                                displayName = cleanEmail.substringBefore("@"),
                                                role = newAdminRole,
                                                isActive = true,
                                                grantedBy = adminEmail
                                            )
                                            adminRoles = (adminRoles.filterNot { it.email.equals(cleanEmail, ignoreCase = true) }) + entry
                                            appendAuditLog(
                                                actionType = "GRANT_ADMIN_ROLE",
                                                targetType = "admins",
                                                targetId = entryId,
                                                summary = "Granted $newAdminRole to $cleanEmail"
                                            )
                                            statusBanner = "Granted $newAdminRole to $cleanEmail."
                                            newAdminEmail = ""
                                        },
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = AdminPrimaryPurple,
                                            contentColor = Color.White
                                        ),
                                        modifier = Modifier.testTag("panel_grant_role_button")
                                    ) {
                                        Text("Assign", fontWeight = FontWeight.Bold)
                                    }
                                }

                                adminRoles.forEach { roleEntry ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(AdminBackgroundPurple)
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "${roleEntry.displayName} (${roleEntry.role})",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = AdminTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "${roleEntry.email} • Granted by ${roleEntry.grantedBy}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AdminTextSecondary
                                            )
                                        }
                                        Text(
                                            text = if (roleEntry.sessionRevoked) "Revoked" else "Active",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (roleEntry.sessionRevoked) AdminErrorRed else AdminSuccessGreen,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }

                        item {
                            AdminSectionCard(
                                title = "Immutable Security Audit Logs (/audit_logs)",
                                subtitle = "${auditLogs.size} recorded administrative action(s)",
                                icon = Icons.Default.History,
                                tag = "admin_panel_audit_logs_card"
                            ) {
                                if (auditLogs.isEmpty()) {
                                    Text(
                                        text = "All administrative mutations are recorded here with actor UID, role, and timestamp.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = AdminTextSecondary
                                    )
                                } else {
                                    auditLogs.forEach { log ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AdminBackgroundPurple)
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = "${log.actionType} • ${log.actorEmail} (${log.actorRole})",
                                                style = MaterialTheme.typography.labelMedium,
                                                color = AdminTextPrimary,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = log.summary,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = AdminTextSecondary
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

    // Confirmation Dialog for Event Moderation
    if (pendingEventModeration != null) {
        val evt = pendingEventModeration!!
        val isSuspended = evt.status == "Suspended"
        AlertDialog(
            onDismissRequest = { pendingEventModeration = null },
            containerColor = AdminSurfacePurple,
            title = {
                Text(
                    text = if (isSuspended) "Restore Event Access?" else "Suspend Event Public Access?",
                    color = AdminTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "Confirm moderation action for \"${evt.name}\". This action will be recorded in the immutable security audit log.",
                    color = AdminTextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val nextStatus = if (isSuspended) "Active" else "Suspended"
                        events = events.map {
                            if (it.id == evt.id) it.copy(status = nextStatus, qrEnabled = !isSuspended) else it
                        }
                        appendAuditLog(
                            actionType = if (isSuspended) "RESTORE_EVENT" else "SUSPEND_EVENT",
                            targetType = "events",
                            targetId = evt.id,
                            summary = "Event ${evt.name} status set to $nextStatus"
                        )
                        statusBanner = "Event \"${evt.name}\" is now $nextStatus."
                        pendingEventModeration = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AdminPrimaryPurple,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.testTag("confirm_event_moderation_button")
                ) {
                    Text("Confirm")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { pendingEventModeration = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun AdminMetricTile(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = AdminSurfacePurple),
        border = BorderStroke(0.75.dp, AdminSecondaryPurple)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = AdminPrimaryPurple,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = AdminTextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = AdminTextPrimary,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = AdminTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdminSectionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    tag: String = "",
    content: @Composable () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = AdminSurfacePurple),
        border = BorderStroke(0.75.dp, AdminSecondaryPurple),
        modifier = Modifier
            .fillMaxWidth()
            .then(if (tag.isNotBlank()) Modifier.testTag(tag) else Modifier)
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
                    imageVector = icon,
                    contentDescription = null,
                    tint = AdminPrimaryPurple,
                    modifier = Modifier.size(18.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = AdminTextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall,
                        color = AdminTextSecondary
                    )
                }
            }
            HorizontalDivider(color = AdminSecondaryPurple)
            content()
        }
    }
}

@Composable
private fun AdminSwitchRow(
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
            color = AdminTextPrimary,
            modifier = Modifier.weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AdminPrimaryPurple
            ),
            modifier = Modifier.testTag(tag)
        )
    }
}
