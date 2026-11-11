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
import com.example.ui.components.JummoManagedBannerAd
import com.example.ui.components.RemoteAnnouncementBanner
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.CameraScreen
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.GuestPortalModalScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.OfficialWebsiteScreen
import com.example.ui.screens.PhotographerWebsiteCustomizerScreen
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

@Composable
fun JummoLuxurySplashScreen(
    onSkipOrFinish: () -> Unit
) {
    var animStarted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { animStarted = true }

    val logoScale by animateFloatAsState(
        targetValue = if (animStarted) 1f else 0.88f,
        animationSpec = tween(durationMillis = 650, easing = FastOutSlowInEasing),
        label = "splash_logo_scale"
    )
    val progressValue by animateFloatAsState(
        targetValue = if (animStarted) 1f else 0.15f,
        animationSpec = tween(durationMillis = 1050, easing = FastOutSlowInEasing),
        label = "splash_progress"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFAF9FF),
                        Color(0xFFEDE9FE),
                        Color(0xFFFAF9FF)
                    )
                )
            )
            .clickable { onSkipOrFinish() }
            .testTag("jummo_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            JummoLuxuryCameraLogo(
                size = 96.dp,
                showCrown = false,
                modifier = Modifier.scale(logoScale)
            )

            Text(
                text = "Jummo Photos AI",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF24133F),
                fontWeight = FontWeight.ExtraBold
            )

            Text(
                text = "Smart Event Photography Suite",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF6B5B83)
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progressValue },
                color = Color(0xFF5B21B6),
                trackColor = Color(0xFFC7B3FF),
                modifier = Modifier
                    .width(160.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(50))
            )
        }
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
    val bannersState by viewModel.bannersState.collectAsStateWithLifecycle()
    val sliderConfig by viewModel.sliderConfigState.collectAsStateWithLifecycle()
    val websiteCustomizationsState by viewModel.websiteCustomizationsState.collectAsStateWithLifecycle()
    val websiteVersionsState by viewModel.websiteVersionsState.collectAsStateWithLifecycle()
    val adConfig by viewModel.adConfigState.collectAsStateWithLifecycle()
    val remoteSettings by viewModel.remoteSettingsState.collectAsStateWithLifecycle()
    val adminAccounts by viewModel.adminAccountsState.collectAsStateWithLifecycle()
    val adminNotifications by viewModel.adminNotificationsState.collectAsStateWithLifecycle()
    val auditLogs by viewModel.auditLogsState.collectAsStateWithLifecycle()
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
    var showWebsiteCustomizer by rememberSaveable { mutableStateOf(false) }
    var websiteCustomizerEvent by remember { mutableStateOf<PhotoEvent?>(null) }

    BackHandler(
        enabled = currentTab != 0 &&
            guestPortalEvent == null &&
            !showAdminDashboard &&
            !showWebsiteCustomizer
    ) {
        currentTab = 0
    }

    val profile = (profileState as? UiState.Success)?.data
    val events = (eventsState as? UiState.Success)?.data.orEmpty()
    val uploads = (uploadsState as? UiState.Success)?.data.orEmpty()
    val guestConsents = (guestConsentsState as? UiState.Success)?.data.orEmpty()
    val banners = (bannersState as? UiState.Success)?.data.orEmpty()
    val websiteCustomizations = (websiteCustomizationsState as? UiState.Success)?.data.orEmpty()
    val websiteVersions = (websiteVersionsState as? UiState.Success)?.data.orEmpty()

    if (showWebsiteCustomizer) {
        PhotographerWebsiteCustomizerScreen(
            profile = profile,
            events = events,
            initialEvent = websiteCustomizerEvent,
            uploads = uploads,
            guestSessions = guestConsents,
            customizations = websiteCustomizations,
            versions = websiteVersions,
            onCopyUriToWebsiteStorage = { uri, prefix ->
                viewModel.copyAssetUriToLocalWebsiteStorage(context, uri, prefix)
            },
            onSaveDraft = { draft ->
                viewModel.saveWebsiteDraft(draft)
            },
            onPublishWebsite = { draft, associatedEvent ->
                viewModel.publishWebsiteCustomization(draft, associatedEvent)
            },
            onRollbackVersion = { currentConfig, targetVersion, associatedEvent ->
                viewModel.rollbackWebsiteVersion(currentConfig, targetVersion, associatedEvent)
            },
            onOpenPublicGuestPortal = { targetEvent, _ ->
                showWebsiteCustomizer = false
                guestPortalEvent = targetEvent
            },
            onNotifyMessage = { msg ->
                viewModel.showBanner(msg)
            },
            onClose = {
                showWebsiteCustomizer = false
                websiteCustomizerEvent = null
            }
        )
        return
    }

    if (showAdminDashboard) {
        AdminDashboardScreen(
            profile = profile,
            events = events,
            uploads = uploads,
            guestSessions = guestConsents,
            banners = banners,
            sliderConfig = sliderConfig,
            websiteCustomizations = websiteCustomizations,
            adConfig = adConfig,
            remoteSettings = remoteSettings,
            adminAccounts = adminAccounts,
            adminNotifications = adminNotifications,
            auditLogs = auditLogs,
            onSaveAdConfig = { updatedAdConfig ->
                viewModel.saveAdConfiguration(updatedAdConfig)
            },
            onSaveRemoteSettings = { updatedSettings ->
                viewModel.saveRemoteAppSettings(updatedSettings)
            },
            onSaveAdminAccount = { record ->
                viewModel.saveAdminAccountRecord(record)
            },
            onSendAdminNotification = { title, message, category, targetAudience, scheduledAt ->
                viewModel.sendAdminNotification(title, message, category, targetAudience, scheduledAt)
            },
            onSaveSliderConfig = { config ->
                viewModel.saveSliderConfig(config)
            },
            onUploadMultipleBanners = { uris ->
                viewModel.uploadHomeBannersFromUris(context, uris)
            },
            onAddPresetBanner = { presetUri ->
                viewModel.addPresetHomeBanner(presetUri)
            },
            onSaveAdvancedBanner = { bannerId, title, imageUri, visibility, targetPhotographerIds, targetPlanIds, startAt, endAt, isActive, duration ->
                viewModel.saveAdvancedHomeBanner(
                    bannerId = bannerId,
                    title = title,
                    imageUri = imageUri,
                    visibility = visibility,
                    targetPhotographerIds = targetPhotographerIds,
                    targetPlanIds = targetPlanIds,
                    startAt = startAt,
                    endAt = endAt,
                    isActive = isActive,
                    slideDurationSeconds = duration
                )
            },
            onToggleBannerActive = { banner ->
                viewModel.toggleHomeBannerActive(banner)
            },
            onReplaceBannerFromUri = { banner, newUri ->
                viewModel.replaceHomeBannerImage(context, banner, newUri)
            },
            onReplaceBannerWithPreset = { banner, presetUri ->
                viewModel.replaceHomeBannerWithPreset(banner, presetUri)
            },
            onReorderBanners = { reordered ->
                viewModel.reorderHomeBanners(reordered)
            },
            onUpdateSlideDuration = { seconds ->
                viewModel.updateBannerSlideDuration(seconds)
            },
            onDeleteBanner = { banner ->
                viewModel.deleteHomeBanner(banner)
            },
            onToggleWebsiteSuspended = { webConfig, suspended ->
                viewModel.setWebsiteSuspensionStatus(webConfig, suspended)
            },
            onOpenWebsiteCustomizer = { targetEvent ->
                showAdminDashboard = false
                websiteCustomizerEvent = targetEvent
                showWebsiteCustomizer = true
            },
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
        val eventWebConfig = websiteCustomizations.find { it.eventId == liveEvent.id }
            ?: websiteCustomizations.find { it.id == "web_${liveEvent.id}" }
        GuestPortalModalScreen(
            event = liveEvent,
            eventUploads = eventUploads,
            guestSessions = guestConsents,
            websiteCustomization = eventWebConfig,
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
                                    tint = Color(0xFF24133F),
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
                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.14f),
                        border = BorderStroke(0.75.dp, MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = errorMsg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                RemoteAnnouncementBanner(
                    remoteSettings = remoteSettings,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

                val activePlacement = when (currentTab) {
                    0 -> "home"
                    1 -> "events"
                    else -> "blocked"
                }
                JummoManagedBannerAd(
                    placement = activePlacement,
                    adConfig = adConfig,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )

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
                                text = "Loading…",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF24133F)
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
                                banners = banners,
                                sliderConfig = sliderConfig,
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
                                        name = name,
                                        category = cat,
                                        eventDate = date,
                                        location = loc,
                                        coverType = covType,
                                        coverUri = covUri,
                                        guestAccessMode = guestMode,
                                        guestAllowDownload = allowDl,
                                        qrExpirationDate = qrExp,
                                        context = context
                                    )
                                },
                                onCreateEventWithConfig = { name, cat, date, loc, covType, covUri, guestMode, allowDl, qrExp, desc, storageDest, pin ->
                                    viewModel.createEvent(
                                        name = name,
                                        category = cat,
                                        eventDate = date,
                                        location = loc,
                                        coverType = covType,
                                        coverUri = covUri,
                                        guestAccessMode = guestMode,
                                        guestAllowDownload = allowDl,
                                        qrExpirationDate = qrExp,
                                        description = desc,
                                        storageDestination = storageDest,
                                        guestPin = pin,
                                        context = context
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
                                onOpenWebsiteCustomizer = { evt ->
                                    websiteCustomizerEvent = evt
                                    showWebsiteCustomizer = true
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
                                    val activeEvts = events.filter { it.status == "Active" }.ifEmpty { events }
                                    val selectedEvt = activeEvts.find { it.id == selectedEventId } ?: activeEvts.firstOrNull()
                                    viewModel.triggerUploadAllPendingNow(context, selectedEvt)
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
                                },
                                onPauseSingleUpload = { record ->
                                    viewModel.pauseSingleUpload(record)
                                },
                                onResumeSingleUpload = { record ->
                                    viewModel.resumeSingleUpload(record, context)
                                },
                                onRemoveUploadFromQueue = { record ->
                                    viewModel.removeUploadRecordFromQueue(record)
                                },
                                onReassignUploadToEvent = { record, targetEvt ->
                                    viewModel.reassignUploadToEvent(record, targetEvt)
                                },
                                onPauseAllUploads = {
                                    viewModel.pauseAllUploads(context)
                                },
                                onResumeAllUploads = {
                                    viewModel.resumeAllUploads(context)
                                },
                                onCancelAllUploads = {
                                    viewModel.cancelAllQueuedUploads(context)
                                },
                                onOpenDriveSettings = {
                                    currentTab = 4
                                },
                                onOpenProfileTab = {
                                    currentTab = 4
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
                                onOpenWebsiteCustomizer = {
                                    websiteCustomizerEvent = events.firstOrNull { it.status == "Active" } ?: events.firstOrNull()
                                    showWebsiteCustomizer = true
                                },
                                onRetryFailedUploads = {
                                    viewModel.resumeAllUploads(context)
                                },
                                onNotifyMessage = { msg ->
                                    viewModel.showBanner(msg)
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
}
