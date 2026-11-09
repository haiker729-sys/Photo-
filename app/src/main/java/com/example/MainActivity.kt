package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SettingsSuggest
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
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
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.model.PhotoEvent
import com.example.data.model.UiState
import com.example.data.repository.JummoRepository
import com.example.ui.auth.AuthScreen
import com.example.ui.auth.signOut
import com.example.ui.components.JummoLuxuryBottomBar
import com.example.ui.components.JummoLuxuryCameraLogo
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.GuestPortalModalScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.OfficialWebsiteScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.UploadsScreen
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.JummoVioletBright
import com.example.ui.theme.JummoVioletPrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JummoViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                JummoRootApp()
            }
        }
    }
}

@Composable
fun JummoRootApp() {
    var showSplash by rememberSaveable { mutableStateOf(true) }
    var showOfficialWebsite by rememberSaveable { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    LaunchedEffect(Unit) {
        delay(1200L)
        showSplash = false
    }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    if (showSplash) {
        JummoLuxurySplashScreen(onSkipOrFinish = { showSplash = false })
        return
    }

    if (showOfficialWebsite) {
        OfficialWebsiteScreen(
            onClose = { showOfficialWebsite = false }
        )
        return
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            },
            onOpenOfficialWebsite = {
                showOfficialWebsite = true
            }
        )
    } else {
        key(user.uid) {
            JummoAuthenticatedShell(
                currentUserId = user.uid,
                userDisplayName = user.displayName ?: "Meraj Sir",
                userEmail = user.email ?: "",
                onOpenOfficialWebsite = {
                    showOfficialWebsite = true
                }
            )
        }
    }
}

/**
 * Screen 1 from the visual reference:
 * Luxury Cinematic Splash Screen with Animated Crown Camera Lens Logo,
 * "Jummo Photos AI", "Pro Studio" pill badge, "Smart Event Photography Suite",
 * Capture • Manage • Share with AI, and purple-to-gold progress bar.
 */
@Composable
fun JummoLuxurySplashScreen(
    onSkipOrFinish: () -> Unit
) {
    var animStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animStarted = true }

    val logoScale by animateFloatAsState(
        targetValue = if (animStarted) 1f else 0.86f,
        animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing),
        label = "splash_logo_scale"
    )
    val progressValue by animateFloatAsState(
        targetValue = if (animStarted) 1f else 0.15f,
        animationSpec = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
        label = "splash_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(JummoObsidian)
            .clickable { onSkipOrFinish() }
            .testTag("jummo_splash_screen")
    ) {
        // Luxury Wedding / Studio Bokeh Backdrop
        Image(
            painter = painterResource(id = R.drawable.img_wedding_cover),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Deep Midnight Black & Royal Purple Cinematic Vignette Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            JummoObsidian.copy(alpha = 0.92f),
                            JummoDeepPurple.copy(alpha = 0.82f),
                            JummoObsidian.copy(alpha = 0.72f),
                            JummoObsidian.copy(alpha = 0.96f)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Center Brand Emblem: Crown Camera Lens + Jummo Photos AI + Pro Studio Pill
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                JummoLuxuryCameraLogo(
                    size = 116.dp,
                    showCrown = true,
                    modifier = Modifier.scale(logoScale)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Jummo",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Photos AI",
                    style = MaterialTheme.typography.headlineMedium,
                    color = JummoGoldBright,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(10.dp))

                Surface(
                    color = JummoCardPurple.copy(alpha = 0.88f),
                    border = BorderStroke(1.dp, JummoGoldBright.copy(alpha = 0.65f)),
                    shape = RoundedCornerShape(50)
                ) {
                    Text(
                        text = "✦  Pro Studio  ✦",
                        style = MaterialTheme.typography.labelLarge,
                        color = JummoGoldBright,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 5.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "Smart Event Photography Suite",
                    style = MaterialTheme.typography.bodyMedium,
                    color = JummoTextSecondary
                )
            }

            // Bottom Feature Trio: Capture • Manage • Share with AI + Progress Bar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SplashFeatureIcon(icon = Icons.Default.CameraAlt, label = "Capture")
                    SplashFeatureIcon(icon = Icons.Default.SettingsSuggest, label = "Manage")
                    SplashFeatureIcon(icon = Icons.Default.Share, label = "Share")
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "with ",
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                    Text(
                        text = "AI",
                        style = MaterialTheme.typography.titleLarge,
                        color = JummoGoldBright,
                        fontWeight = FontWeight.Bold
                    )
                }

                LinearProgressIndicator(
                    progress = { progressValue },
                    color = JummoGoldBright,
                    trackColor = JummoVioletPrimary.copy(alpha = 0.35f),
                    modifier = Modifier
                        .width(180.dp)
                        .height(5.dp)
                        .clip(RoundedCornerShape(50))
                )
            }
        }
    }
}

@Composable
private fun SplashFeatureIcon(
    icon: ImageVector,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(JummoCardPurple.copy(alpha = 0.85f))
                .border(0.75.dp, JummoGoldBright.copy(alpha = 0.55f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = JummoGoldBright,
                modifier = Modifier.size(17.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = JummoTextSecondary
        )
    }
}

@Composable
fun JummoAuthenticatedShell(
    currentUserId: String,
    userDisplayName: String,
    userEmail: String,
    onOpenOfficialWebsite: () -> Unit = {}
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    val viewModel: JummoViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                JummoViewModel(
                    repository = JummoRepository(db),
                    currentUserId = currentUserId,
                    defaultDisplayName = userDisplayName,
                    defaultEmail = userEmail
                )
            }
        }
    )

    val profileState by viewModel.profileState.collectAsStateWithLifecycle()
    val eventsState by viewModel.eventsState.collectAsStateWithLifecycle()
    val uploadsState by viewModel.uploadsState.collectAsStateWithLifecycle()
    val guestConsentsState by viewModel.guestConsentsState.collectAsStateWithLifecycle()
    val selectedEventId by viewModel.selectedEventId.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()

    LaunchedEffect(statusBanner) {
        if (statusBanner != null) {
            delay(3500L)
            viewModel.dismissBanner()
        }
    }

    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var openCreateEventModal by remember { mutableStateOf(false) }
    var qrModalEvent by remember { mutableStateOf<PhotoEvent?>(null) }
    var detailsModalEvent by remember { mutableStateOf<PhotoEvent?>(null) }
    var guestPortalEvent by remember { mutableStateOf<PhotoEvent?>(null) }
    var showAdminDashboard by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = currentTab != 0 && guestPortalEvent == null && !showAdminDashboard) {
        currentTab = 0
    }

    val profile = (profileState as? UiState.Success)?.data
    val events = (eventsState as? UiState.Success)?.data.orEmpty()
    val uploads = (uploadsState as? UiState.Success)?.data.orEmpty()
    val guestConsents = (guestConsentsState as? UiState.Success)?.data.orEmpty()

    if (showAdminDashboard) {
        AdminDashboardScreen(
            profile = profile,
            events = events,
            uploads = uploads,
            guestSessions = guestConsents,
            onNavigateToTab = { tab ->
                showAdminDashboard = false
                currentTab = tab
            },
            onClose = {
                showAdminDashboard = false
            }
        )
        return
    }

    if (guestPortalEvent != null) {
        val liveEvent = events.find { it.id == guestPortalEvent?.id } ?: guestPortalEvent!!
        val eventUploads = uploads.filter { it.eventId == liveEvent.id }
        GuestPortalModalScreen(
            event = liveEvent,
            eventUploads = eventUploads,
            guestSessions = guestConsents,
            onRegisterConsentSession = { guestLabel, selfieUri, matchedCount ->
                viewModel.registerGuestFaceSearchConsent(
                    event = liveEvent,
                    guestLabel = guestLabel,
                    selfieUri = selfieUri,
                    matchedCount = matchedCount
                )
            },
            onDeleteFaceData = { session ->
                viewModel.deleteGuestFaceData(session)
            },
            onReportMismatch = { session, notes ->
                viewModel.reportIncorrectMatch(session, notes)
            },
            onNotifyDownload = { msg ->
                viewModel.showBanner(msg)
            },
            onClose = {
                guestPortalEvent = null
            }
        )
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = JummoObsidian,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            JummoLuxuryBottomBar(
                currentTab = currentTab,
                onSelectTab = { currentTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(JummoDeepPurple, JummoObsidian)
                    )
                )
                .padding(innerPadding)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                AnimatedVisibility(
                    visible = statusBanner != null,
                    enter = fadeIn(tween(180)),
                    exit = fadeOut(tween(180))
                ) {
                    Surface(
                        color = JummoCardPurple.copy(alpha = 0.96f),
                        border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.55f)),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = statusBanner ?: "",
                                style = MaterialTheme.typography.labelMedium,
                                color = JummoGoldBright,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.dismissBanner() },
                                modifier = Modifier.size(22.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss notification",
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                val errorMsg = (eventsState as? UiState.Error)?.message
                    ?: (uploadsState as? UiState.Error)?.message
                if (errorMsg != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.18f),
                        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                if (eventsState is UiState.Loading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(
                                color = JummoGoldBright,
                                strokeWidth = 2.5.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Loading studio workspace…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    Crossfade(
                        targetState = currentTab,
                        animationSpec = tween(durationMillis = 200),
                        label = "jummo_tab_transition"
                    ) { tab ->
                        when (tab) {
                            0 -> HomeDashboardScreen(
                                profile = profile,
                                events = events,
                                uploads = uploads,
                                onCreateNewEventClick = {
                                    openCreateEventModal = true
                                    currentTab = 1
                                },
                                onQuickCameraClick = {
                                    currentTab = 2
                                },
                                onOpenEventDetails = { evt ->
                                    detailsModalEvent = evt
                                    currentTab = 1
                                },
                                onOpenEventQr = { evt ->
                                    qrModalEvent = evt
                                    currentTab = 1
                                },
                                onOpenGuestPortal = { evt ->
                                    guestPortalEvent = evt
                                },
                                onSelectEventAndOpenCamera = { evt ->
                                    viewModel.selectEvent(evt.id)
                                    currentTab = 2
                                },
                                onOpenDriveSettings = {
                                    currentTab = 4
                                },
                                onNavigateToEventsTab = {
                                    currentTab = 1
                                },
                                onNavigateToUploadsTab = {
                                    currentTab = 3
                                },
                                onOpenAdminDashboard = {
                                    showAdminDashboard = true
                                }
                            )

                            1 -> EventsScreen(
                                events = events,
                                uploads = uploads,
                                showCreateDialogCommand = openCreateEventModal,
                                initialQrEvent = qrModalEvent,
                                initialDetailsEvent = detailsModalEvent,
                                onConsumeCreateDialogCommand = { openCreateEventModal = false },
                                onConsumeQrEvent = { qrModalEvent = null },
                                onConsumeDetailsEvent = { detailsModalEvent = null },
                                onCreateEvent = { name, cat, date, loc, covType, covUri, guestMode, allowDl, qrExp ->
                                    viewModel.createEvent(
                                        name,
                                        cat,
                                        date,
                                        loc,
                                        covType,
                                        covUri,
                                        guestMode,
                                        allowDl,
                                        qrExp
                                    )
                                },
                                onUpdateEvent = { updated, msg ->
                                    viewModel.updateEvent(updated, msg)
                                },
                                onToggleArchive = { evt ->
                                    viewModel.toggleArchiveEvent(evt)
                                },
                                onDeleteEvent = { evt ->
                                    viewModel.deleteEvent(evt)
                                },
                                onOpenGuestPortal = { evt ->
                                    guestPortalEvent = evt
                                },
                                onSelectEventForCamera = { evt ->
                                    viewModel.selectEvent(evt.id)
                                    currentTab = 2
                                },
                                onImportPhotosForEvent = { evt, uris ->
                                    val autoMode = if (profile?.autoUploadEnabled != false) "Automatic" else "Manual"
                                    viewModel.importPhotosFromUris(
                                        context = context,
                                        event = evt,
                                        uris = uris,
                                        sourceType = "EventPhotoImport",
                                        uploadMode = autoMode,
                                        uploadImmediately = profile?.autoUploadEnabled != false
                                    )
                                },
                                onRetryUpload = { record ->
                                    viewModel.retrySingleUpload(record)
                                },
                                onRemoveUploadFromQueue = { record ->
                                    viewModel.removeUploadRecordFromQueue(record)
                                },
                                onShowMessage = { msg ->
                                    viewModel.showBanner(msg)
                                }
                            )

                            2 -> CameraScreen(
                                events = events,
                                selectedEventId = selectedEventId,
                                uploads = uploads,
                                onSelectEvent = { id -> viewModel.selectEvent(id) },
                                onCreateEventClick = {
                                    openCreateEventModal = true
                                    currentTab = 1
                                },
                                onPhotoCapturedToLocalStorage = { evt, file ->
                                    viewModel.onCameraPhotoCaptured(context, evt, file)
                                },
                                onFallbackStudioCapture = { evt, lens, flash ->
                                    val savedFile = viewModel.captureStudioFrameToLocalStorage(
                                        context = context,
                                        event = evt,
                                        lensLabel = lens,
                                        flashLabel = flash
                                    )
                                    viewModel.onCameraPhotoCaptured(context, evt, savedFile)
                                }
                            )

                            3 -> UploadsScreen(
                                profile = profile,
                                events = events,
                                selectedEventId = selectedEventId,
                                uploads = uploads,
                                onSelectEvent = { id -> viewModel.selectEvent(id) },
                                onCreateEventClick = {
                                    openCreateEventModal = true
                                    currentTab = 1
                                },
                                onImportPhotos = { evt, uris, source, mode, immediate ->
                                    viewModel.importPhotosFromUris(
                                        context = context,
                                        event = evt,
                                        uris = uris,
                                        sourceType = source,
                                        uploadMode = mode,
                                        uploadImmediately = immediate
                                    )
                                },
                                onUploadAllPendingNow = {
                                    viewModel.triggerUploadAllPendingNow(context)
                                },
                                onRetrySingleUpload = { record ->
                                    viewModel.retrySingleUpload(record)
                                },
                                onUpdateAutoUploadSettings = { auto, wifi, battery ->
                                    viewModel.updateProfileAndUploadSettings(
                                        context = context,
                                        displayName = profile?.displayName ?: userDisplayName,
                                        studioName = profile?.studioName ?: "Jummo Pro Studio",
                                        driveConnected = profile?.driveConnected ?: false,
                                        driveFolderName = profile?.driveFolderName ?: "Jummo_Events_Cloud",
                                        autoUploadEnabled = auto,
                                        wifiOnlyUpload = wifi,
                                        batteryNotLowUpload = battery
                                    )
                                }
                            )

                            4 -> ProfileScreen(
                                profile = profile,
                                events = events,
                                uploads = uploads,
                                guestSessions = guestConsents,
                                onSaveProfileSettings = { name, studio, driveConn, driveFolder, auto, wifi, batt ->
                                    viewModel.updateProfileAndUploadSettings(
                                        context = context,
                                        displayName = name,
                                        studioName = studio,
                                        driveConnected = driveConn,
                                        driveFolderName = driveFolder,
                                        autoUploadEnabled = auto,
                                        wifiOnlyUpload = wifi,
                                        batteryNotLowUpload = batt
                                    )
                                },
                                onDeleteGuestFaceData = { session ->
                                    viewModel.deleteGuestFaceData(session)
                                },
                                onOpenAdminDashboard = {
                                    showAdminDashboard = true
                                },
                                onOpenOfficialWebsite = onOpenOfficialWebsite,
                                onSignOut = {
                                    signOut(
                                        credentialManager = credentialManager,
                                        scope = coroutineScope
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
