package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
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
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.GuestPortalModalScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.UploadsScreen
import com.example.ui.theme.JummoCardPurple
import com.example.ui.theme.JummoDeepPurple
import com.example.ui.theme.JummoGoldBright
import com.example.ui.theme.JummoObsidian
import com.example.ui.theme.JummoTextSecondary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.JummoViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

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
    var currentUser by remember { mutableStateOf(Firebase.auth.currentUser) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose {
            Firebase.auth.removeAuthStateListener(listener)
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            onAuthSuccess = {
                currentUser = Firebase.auth.currentUser
            }
        )
    } else {
        key(user.uid) {
            JummoAuthenticatedShell(
                currentUserId = user.uid,
                userDisplayName = user.displayName ?: "Lead Photographer",
                userEmail = user.email ?: ""
            )
        }
    }
}

private data class NavTabItem(
    val index: Int,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun JummoAuthenticatedShell(
    currentUserId: String,
    userDisplayName: String,
    userEmail: String
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

    var currentTab by rememberSaveable { mutableIntStateOf(0) }
    var openCreateEventModal by remember { mutableStateOf(false) }
    var qrModalEvent by remember { mutableStateOf<PhotoEvent?>(null) }
    var detailsModalEvent by remember { mutableStateOf<PhotoEvent?>(null) }
    var guestPortalEvent by remember { mutableStateOf<PhotoEvent?>(null) }

    BackHandler(enabled = currentTab != 0 && guestPortalEvent == null) {
        currentTab = 0
    }

    val profile = (profileState as? UiState.Success)?.data
    val events = (eventsState as? UiState.Success)?.data.orEmpty()
    val uploads = (uploadsState as? UiState.Success)?.data.orEmpty()
    val guestConsents = (guestConsentsState as? UiState.Success)?.data.orEmpty()

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

    val navItems = listOf(
        NavTabItem(0, "Home", Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
        NavTabItem(1, "Events", Icons.Filled.Event, Icons.Outlined.Event, "nav_tab_events"),
        NavTabItem(2, "Camera", Icons.Filled.CameraAlt, Icons.Outlined.CameraAlt, "nav_tab_camera"),
        NavTabItem(3, "Uploads", Icons.Filled.CloudUpload, Icons.Outlined.CloudUpload, "nav_tab_uploads"),
        NavTabItem(4, "Profile", Icons.Filled.Person, Icons.Outlined.Person, "nav_tab_profile")
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = JummoObsidian,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            NavigationBar(
                containerColor = JummoDeepPurple,
                tonalElevation = 8.dp,
                windowInsets = WindowInsets.navigationBars
            ) {
                navItems.forEach { item ->
                    val selected = currentTab == item.index
                    NavigationBarItem(
                        selected = selected,
                        onClick = { currentTab = item.index },
                        icon = {
                            Icon(
                                imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                contentDescription = item.label
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = JummoObsidian,
                            selectedTextColor = JummoGoldBright,
                            indicatorColor = JummoGoldBright,
                            unselectedIconColor = JummoTextSecondary,
                            unselectedTextColor = JummoTextSecondary
                        ),
                        modifier = Modifier.testTag(item.testTag)
                    )
                }
            }
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
                // Status Notification Banner
                AnimatedVisibility(visible = statusBanner != null) {
                    Surface(
                        color = JummoCardPurple,
                        border = BorderStroke(0.75.dp, JummoGoldBright.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = statusBanner ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = JummoGoldBright,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { viewModel.dismissBanner() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss notification",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // Error Banner if Firestore state failed
                val errorMsg = (eventsState as? UiState.Error)?.message
                    ?: (uploadsState as? UiState.Error)?.message
                if (errorMsg != null) {
                    Surface(
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f),
                        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFCA5A5),
                            modifier = Modifier.padding(12.dp)
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
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Loading events…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White
                            )
                        }
                    }
                } else {
                    when (currentTab) {
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
