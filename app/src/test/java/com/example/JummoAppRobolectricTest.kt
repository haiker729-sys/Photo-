package com.example

import android.content.Context
import android.content.Intent
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.GuestAccessModes
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.PhotographerProfile
import com.example.ui.auth.AuthScreen
import com.example.ui.screens.EventQrSystemDialog
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.GuestPortalModalScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.OfficialWebsiteScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.QrCodeGenerator
import java.io.File
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class JummoAppRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun loginScreen_displaysHeroCategoriesFeatureCardGoogleButtonAndSecurityFooter() {
        composeTestRule.setContent {
            MyApplicationTheme {
                AuthScreen(
                    onAuthSuccess = {},
                    enableAutoSignIn = false
                )
            }
        }
        composeTestRule.waitForIdle()

        // 1. Branding & Hero
        composeTestRule.onNodeWithTag("jummo_auth_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jummo Photos AI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Smart Event Photography Suite").assertIsDisplayed()

        // 2. Five Compact Event Categories
        composeTestRule.onNodeWithText("Weddings").assertIsDisplayed()
        composeTestRule.onNodeWithText("Festivals").assertIsDisplayed()
        composeTestRule.onNodeWithText("Corporate").assertIsDisplayed()
        composeTestRule.onNodeWithText("Schools").assertIsDisplayed()
        composeTestRule.onNodeWithText("Live Events").assertIsDisplayed()

        // 3. Four Feature Card Rows
        val scrollContainer = composeTestRule.onNodeWithTag("login_scroll_container")
        scrollContainer.performScrollToNode(hasText("Camera & DSLR Import"))
        composeTestRule.onNodeWithText("Camera & DSLR Import").assertIsDisplayed()
        composeTestRule.onNodeWithText("Capture or import photos from camera, SD card or storage.").assertIsDisplayed()

        scrollContainer.performScrollToNode(hasText("Smart Upload Queue"))
        composeTestRule.onNodeWithText("Smart Upload Queue").assertIsDisplayed()
        composeTestRule.onNodeWithText("Automatic and manual uploads with network and battery protection.").assertIsDisplayed()

        scrollContainer.performScrollToNode(hasText("Event QR & Guest Portal"))
        composeTestRule.onNodeWithText("Event QR & Guest Portal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create event QR codes and share photos securely with guests.").assertIsDisplayed()

        scrollContainer.performScrollToNode(hasText("AI Face Search"))
        composeTestRule.onNodeWithText("AI Face Search").assertIsDisplayed()
        composeTestRule.onNodeWithText("Find guest photos with explicit consent and privacy controls.").assertIsDisplayed()

        // 4. Google Sign-In Button & Security Footer
        scrollContainer.performScrollToNode(hasTestTag("google_sign_in_button"))
        composeTestRule.onNodeWithTag("google_sign_in_button")
            .assertIsDisplayed()
            .assertIsEnabled()
        composeTestRule.onNodeWithText("Sign in with Google").assertIsDisplayed()

        scrollContainer.performScrollToNode(hasTestTag("login_security_footer"))
        composeTestRule.onNodeWithText("Secured by Firebase Authentication").assertIsDisplayed()
    }

    @Test
    fun qrCodeGenerator_generatesDeterministicMatrixAndBrandedBitmap() {
        val url = "https://jummophotos.ai/guest/evt_royal_wedding_101"
        val matrix1 = QrCodeGenerator.generateMatrix(url, 25)
        val matrix2 = QrCodeGenerator.generateMatrix(url, 25)

        assertEquals(25, matrix1.size)
        assertEquals(25, matrix1[0].size)
        for (r in 0 until 25) {
            for (c in 0 until 25) {
                assertEquals(matrix1[r][c], matrix2[r][c])
            }
        }

        val bitmap = QrCodeGenerator.createBrandedEventQrBitmap(
            eventName = "Aarav & Meera Royal Wedding",
            eventCategory = "Wedding",
            eventDate = "2026-12-18",
            qrPayload = url,
            sizePx = 480
        )
        assertNotNull(bitmap)
        assertEquals(480, bitmap.width)
        assertTrue(bitmap.height > 480)
    }

    @Test
    fun homeDashboardScreen_displaysLogoProfileStorageAndQuickCamera() {
        val sampleProfile = PhotographerProfile(
            id = "u1",
            userId = "u1",
            displayName = "Meraj Alam",
            email = "meraj@jummo.studio",
            studioName = "Jummo Pro Studio",
            driveConnected = true
        )
        val sampleEvents = listOf(
            PhotoEvent(
                id = "evt_1",
                userId = "u1",
                name = "Royal Palace Wedding",
                category = "Wedding",
                eventDate = "2026-12-18",
                location = "Udaipur",
                totalPhotos = 12L,
                uploadedPhotos = 10L,
                pendingPhotos = 2L
            )
        )

        var quickCameraClicked = false
        composeTestRule.setContent {
            MyApplicationTheme {
                HomeDashboardScreen(
                    profile = sampleProfile,
                    events = sampleEvents,
                    uploads = emptyList(),
                    onCreateNewEventClick = {},
                    onQuickCameraClick = { quickCameraClicked = true },
                    onOpenEventDetails = {},
                    onOpenEventQr = {},
                    onOpenGuestPortal = {},
                    onSelectEventAndOpenCamera = {},
                    onOpenDriveSettings = {},
                    onNavigateToEventsTab = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("home_dashboard_list").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jummo Photos AI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Welcome, Meraj Alam").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_create_event_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_quick_camera_button").assertIsDisplayed()

        composeTestRule.onNodeWithTag("home_quick_camera_button").performClick()
        assertTrue(quickCameraClicked)

        composeTestRule.onNodeWithTag("home_dashboard_list")
            .performScrollToNode(hasTestTag("storage_usage_card"))
        composeTestRule.onNodeWithTag("storage_usage_card").assertIsDisplayed()
    }

    @Test
    fun eventsScreen_opensCreateEventAndEventDetailsWithPhotoManagement() {
        var createdEventName: String? = null
        val sampleEvents = listOf(
            PhotoEvent(
                id = "evt_detail_1",
                userId = "u1",
                name = "Corporate Gala 2026",
                category = "Corporate",
                eventDate = "2026-12-20",
                location = "Bengaluru"
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                EventsScreen(
                    events = sampleEvents,
                    uploads = emptyList(),
                    showCreateDialogCommand = false,
                    initialQrEvent = null,
                    onConsumeCreateDialogCommand = {},
                    onConsumeQrEvent = {},
                    onCreateEvent = { name, _, _, _, _, _, _, _, _ ->
                        createdEventName = name
                    },
                    onUpdateEvent = { _, _ -> },
                    onToggleArchive = {},
                    onDeleteEvent = {},
                    onOpenGuestPortal = {},
                    onSelectEventForCamera = {},
                    onShowMessage = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Test Create Event dialog
        composeTestRule.onNodeWithTag("events_add_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("save_event_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("save_event_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals("Wedding Shoot", createdEventName)

        // Test opening Event Details & Photo Management view
        composeTestRule.onNodeWithTag("details_button_evt_detail_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("event_details_list").assertIsDisplayed()
        composeTestRule.onNodeWithText("Event Details").assertIsDisplayed()
    }

    @Test
    fun profileScreen_displaysAllProfessionalSettingsSections() {
        val sampleProfile = PhotographerProfile(
            id = "u1",
            userId = "u1",
            displayName = "Meraj Alam",
            email = "meraj@jummo.studio",
            studioName = "Jummo Pro Studio"
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileScreen(
                    profile = sampleProfile,
                    events = emptyList(),
                    uploads = emptyList(),
                    guestSessions = emptyList(),
                    onSaveProfileSettings = { _, _, _, _, _, _, _ -> },
                    onDeleteGuestFaceData = {},
                    onSignOut = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        val listNode = composeTestRule.onNodeWithTag("profile_screen_list")
        listNode.assertIsDisplayed()
        composeTestRule.onNodeWithText("Account").assertIsDisplayed()

        listNode.performScrollToNode(hasText("Google Drive"))
        composeTestRule.onNodeWithText("Google Drive").assertIsDisplayed()

        listNode.performScrollToNode(hasText("Upload Preferences"))
        composeTestRule.onNodeWithText("Upload Preferences").assertIsDisplayed()

        listNode.performScrollToNode(hasText("Storage"))
        composeTestRule.onNodeWithText("Storage").assertIsDisplayed()

        listNode.performScrollToNode(hasText("Privacy"))
        composeTestRule.onNodeWithText("Privacy").assertIsDisplayed()

        listNode.performScrollToNode(hasText("Permissions"))
        composeTestRule.onNodeWithText("Permissions").assertIsDisplayed()

        listNode.performScrollToNode(hasText("Help and Support"))
        composeTestRule.onNodeWithText("Help and Support").assertIsDisplayed()

        listNode.performScrollToNode(hasText("About Jummo Photos AI"))
        composeTestRule.onNodeWithText("About Jummo Photos AI").assertIsDisplayed()

        listNode.performScrollToNode(hasTestTag("sign_out_button"))
        composeTestRule.onNodeWithTag("sign_out_button").assertIsDisplayed()
    }

    @Test
    fun guestPortalModalScreen_requiresExplicitConsentBeforeFaceSearch() {
        val sampleEvent = PhotoEvent(
            id = "evt_guest_1",
            userId = "u1",
            name = "Starlight Gala",
            category = "Festival",
            eventDate = "2026-12-24",
            location = "Mumbai",
            qrCodeValue = "https://jummophotos.ai/guest/evt_guest_1"
        )
        val sampleUploads = listOf(
            PhotoUploadRecord(
                id = "upl_1",
                userId = "u1",
                eventId = "evt_guest_1",
                eventName = "Starlight Gala",
                fileName = "DSC_1001.JPG",
                status = "Uploaded",
                progress = 100L
            )
        )

        var consentSubmitted = false
        composeTestRule.setContent {
            MyApplicationTheme {
                GuestPortalModalScreen(
                    event = sampleEvent,
                    eventUploads = sampleUploads,
                    guestSessions = emptyList(),
                    onRegisterConsentSession = { _, _, _ -> consentSubmitted = true },
                    onDeleteFaceData = {},
                    onReportMismatch = { _, _ -> },
                    onNotifyDownload = {},
                    onClose = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Initially consent button must be disabled until guest checks explicit consent
        composeTestRule.onNodeWithTag("guest_portal_list")
            .performScrollToNode(hasTestTag("submit_face_consent_button"))
        composeTestRule.onNodeWithTag("submit_face_consent_button")
            .assertIsNotEnabled()

        // Toggle explicit guest consent checkbox
        composeTestRule.onNodeWithTag("guest_portal_list")
            .performScrollToNode(hasTestTag("guest_consent_checkbox"))
        composeTestRule.onNodeWithTag("guest_consent_checkbox")
            .performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("guest_portal_list")
            .performScrollToNode(hasTestTag("submit_face_consent_button"))
        composeTestRule.onNodeWithTag("submit_face_consent_button")
            .assertIsEnabled()
            .performClick()
        composeTestRule.waitForIdle()

        assertTrue(consentSubmitted)
    }

    @Test
    fun officialWebsiteScreen_displaysHeroDownloadButtonFeaturesPreviewFaqAndFooter() {
        composeTestRule.setContent {
            MyApplicationTheme {
                OfficialWebsiteScreen(
                    onClose = {},
                    enableLiveNetworkCheck = false
                )
            }
        }
        composeTestRule.waitForIdle()

        val websiteList = composeTestRule.onNodeWithTag("official_website_list")
        websiteList.assertIsDisplayed()

        // 1. Hero & Download APK button
        websiteList.performScrollToNode(hasTestTag("hero_download_apk_button"))
        composeTestRule.onNodeWithTag("hero_download_apk_button").assertIsDisplayed()

        // 2. APK Download System Card, "DOWNLOAD JUMMO PHOTOS AI" CTA & Verified Unavailable Notice
        websiteList.performScrollToNode(hasTestTag("download_jummo_photos_ai_button"))
        composeTestRule.onNodeWithTag("download_jummo_photos_ai_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("DOWNLOAD JUMMO PHOTOS AI").assertIsDisplayed()

        websiteList.performScrollToNode(hasTestTag("apk_unavailable_notice"))
        composeTestRule.onNodeWithTag("apk_unavailable_notice").assertIsDisplayed()

        // 3. 8 Implemented Features Section
        websiteList.performScrollToNode(hasText("Implemented Studio Features"))
        composeTestRule.onNodeWithText("Implemented Studio Features").assertIsDisplayed()
        websiteList.performScrollToNode(hasText("Professional Camera"))
        composeTestRule.onNodeWithText("Professional Camera").assertIsDisplayed()
        composeTestRule.onNodeWithText("DSLR / RAW Import").assertIsDisplayed()

        // 4. 8-Screen App Preview Gallery
        websiteList.performScrollToNode(hasText("App Preview Gallery (8 Screens)"))
        composeTestRule.onNodeWithText("App Preview Gallery (8 Screens)").assertIsDisplayed()

        // 5. How It Works (6 Steps)
        websiteList.performScrollToNode(hasText("How It Works"))
        composeTestRule.onNodeWithText("How It Works").assertIsDisplayed()

        // 6. Configurable Studio Plans (Pricing)
        websiteList.performScrollToNode(hasText("Configurable Studio Plans"))
        composeTestRule.onNodeWithText("Configurable Studio Plans").assertIsDisplayed()

        // 7. FAQ Section
        websiteList.performScrollToNode(hasText("How to install the APK?"))
        composeTestRule.onNodeWithText("How to install the APK?").assertIsDisplayed()

        // 8. Official Website Footer
        websiteList.performScrollToNode(hasTestTag("website_footer_section"))
        composeTestRule.onNodeWithTag("website_footer_section").assertIsDisplayed()
        composeTestRule.onNodeWithText("Official GitHub Repo").assertIsDisplayed()
    }

    @Test
    fun qrCodeGenerator_encodesAndDecodesRealScannableIsoQrPerEvent() {
        val event1 = PhotoEvent(
            id = "evt_rahul_pooja_1512",
            name = "Rahul weds Pooja",
            category = "Wedding",
            eventDate = "15 Dec 2026"
        )
        val event2 = PhotoEvent(
            id = "evt_ankit_bday_1012",
            name = "Ankit Birthday",
            category = "Birthday",
            eventDate = "10 Dec 2026"
        )

        val url1 = QrCodeGenerator.buildGuestPortalUrl(event1)
        val url2 = QrCodeGenerator.buildGuestPortalUrl(event2)

        assertEquals("https://jummophotos.ai/guest/evt_rahul_pooja_1512", url1)
        assertEquals("https://jummophotos.ai/guest/evt_ankit_bday_1012", url2)
        assertNotEquals(url1, url2)

        // Generate real ISO/IEC 18004 QR matrices dynamically
        val isoMatrix1 = QrCodeGenerator.generateIsoQrMatrix(url1)
        val isoMatrix2 = QrCodeGenerator.generateIsoQrMatrix(url2)

        // Verify the two matrices are distinct (never static or sample)
        var diffModules = 0
        for (r in isoMatrix1.indices) {
            for (c in isoMatrix1[r].indices) {
                if (isoMatrix1[r][c] != isoMatrix2[r][c]) diffModules++
            }
        }
        assertTrue("Different events must produce different QR matrices", diffModules > 20)

        // Scan/decode the generated ISO QR matrices back to the exact Guest Portal URLs
        val decodedUrl1 = QrCodeGenerator.decodeQrPayloadFromMatrix(isoMatrix1)
        val decodedUrl2 = QrCodeGenerator.decodeQrPayloadFromMatrix(isoMatrix2)

        assertEquals(url1, decodedUrl1)
        assertTrue(decodedUrl1.contains(event1.id))
        assertEquals(url2, decodedUrl2)
        assertTrue(decodedUrl2.contains(event2.id))
    }

    @Test
    fun qrCodeGenerator_downloadsHighResPngAndSharesViaIntent() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val eventId = "evt_rahul_pooja_qr_export"
        val portalUrl = QrCodeGenerator.buildGuestPortalUrl(eventId)

        // 1. High-resolution PNG download
        val downloadResult = QrCodeGenerator.saveQrBitmapToGallery(
            context = context,
            eventName = "Rahul weds Pooja",
            eventCategory = "Wedding",
            eventDate = "15 Dec 2026",
            qrPayload = portalUrl
        )
        assertTrue("QR download must succeed", downloadResult.isSuccess)
        val localQrDir = File(context.filesDir, "qr_exports")
        val exportedFiles = localQrDir.listFiles()?.filter { it.extension == "png" }.orEmpty()
        assertTrue("High-res PNG file must be written", exportedFiles.isNotEmpty())
        assertTrue("PNG file must have non-zero size", exportedFiles.first().length() > 1000L)

        // 2. Native Android Share Sheet intent
        val shareResult = QrCodeGenerator.shareEventQrCode(
            context = context,
            eventName = "Rahul weds Pooja",
            eventCategory = "Wedding",
            eventDate = "15 Dec 2026",
            qrPayload = portalUrl
        )
        assertTrue("QR native share must succeed", shareResult.isSuccess)
        val shareIntent = shareResult.getOrThrow()
        assertEquals(Intent.ACTION_SEND, shareIntent.action)
        assertEquals("image/png", shareIntent.type)
        val shareText = shareIntent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        assertTrue("Shared text must include the Guest Portal URL with event ID", shareText.contains(portalUrl))
    }

    @Test
    fun qrManagementModal_rendersReferenceDesignAndPersistsAllSettings() {
        val initialEvent = PhotoEvent(
            id = "evt_rahul_weds_pooja_01",
            userId = "u1",
            name = "Rahul weds Pooja",
            category = "Wedding",
            eventDate = "15 Dec 2026",
            location = "Taj Palace",
            qrCodeValue = "https://jummophotos.ai/guest/evt_rahul_weds_pooja_01",
            qrEnabled = true,
            qrExpirationDate = "01 Mar 2027",
            guestAccessMode = GuestAccessModes.FACE_SEARCH_ONLY
        )

        var savedEvent: PhotoEvent? = null
        var openedPortalEvent: PhotoEvent? = null

        composeTestRule.setContent {
            MyApplicationTheme {
                EventQrSystemDialog(
                    event = initialEvent,
                    onUpdateEvent = { updated, _ -> savedEvent = updated },
                    onOpenGuestPortal = { evt -> openedPortalEvent = evt },
                    onShowMessage = {},
                    onDismiss = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // Verify header & hero branding from reference image
        composeTestRule.onNodeWithTag("qr_management_modal").assertIsDisplayed()
        composeTestRule.onNodeWithText("QR Management").assertIsDisplayed()
        composeTestRule.onNodeWithText("Create & manage guest access for your event").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jummo Photos AI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Guest Portal").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_hero_event_name").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_code_preview_image").assertIsDisplayed()

        // Verify 3 hero action buttons
        composeTestRule.onNodeWithTag("qr_download_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_share_button").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_open_guest_portal_button").performScrollTo().assertIsDisplayed()

        // Verify Enable toggle, Expiration selector, and 3 Guest Access Modes
        composeTestRule.onNodeWithTag("qr_enable_switch").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("Enable Event QR Portal").assertIsDisplayed()

        composeTestRule.onNodeWithTag("qr_expiration_selector").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithText("QR Expiration Date").assertIsDisplayed()
        composeTestRule.onNodeWithText("01 Mar 2027").assertIsDisplayed()

        composeTestRule.onNodeWithTag("qr_mode_organizer_approved").performScrollTo().assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_mode_face_search_only").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_mode_view_all").assertIsDisplayed()
        composeTestRule.onNodeWithTag("qr_mode_organizer_approved").assertIsDisplayed()

        // Select "View All + Face Search" mode
        composeTestRule.onNodeWithTag("qr_mode_view_all").performClick()
        composeTestRule.waitForIdle()

        // Change expiration date via picker dialog
        composeTestRule.onNodeWithTag("qr_expiration_selector").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("qr_exp_preset_15_Jun_2027").performClick()
        composeTestRule.waitForIdle()

        // Click "Save QR Settings" and verify settings are saved against the exact event ID
        composeTestRule.onNodeWithTag("qr_save_settings_button").performClick()
        composeTestRule.waitForIdle()

        assertNotNull(savedEvent)
        assertEquals("evt_rahul_weds_pooja_01", savedEvent?.id)
        assertEquals(GuestAccessModes.VIEW_ALL_PERMITTED, savedEvent?.guestAccessMode)
        assertEquals("15 Jun 2027", savedEvent?.qrExpirationDate)
        assertEquals("https://jummophotos.ai/guest/evt_rahul_weds_pooja_01", savedEvent?.qrCodeValue)

        // Click "Open Guest Portal" and verify it passes the updated event
        composeTestRule.onNodeWithTag("qr_open_guest_portal_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals("evt_rahul_weds_pooja_01", openedPortalEvent?.id)
    }

    @Test
    fun guestPortal_validatesEventStatusQrStatusExpirationAndEnforcesThreeAccessModes() {
        val baseEvent = PhotoEvent(
            id = "evt_portal_check_99",
            userId = "u1",
            name = "Rahul weds Pooja",
            category = "Wedding",
            eventDate = "15 Dec 2026",
            location = "New Delhi",
            status = "Active",
            qrCodeValue = "https://jummophotos.ai/guest/evt_portal_check_99",
            qrEnabled = true,
            qrExpirationDate = "01 Mar 2027",
            guestAccessMode = GuestAccessModes.FACE_SEARCH_ONLY
        )

        // 1. Validate Disabled QR blocks guest access
        val disabledResult = QrCodeGenerator.validateGuestPortalAccess(baseEvent.copy(qrEnabled = false))
        assertFalse(disabledResult.isAllowed)
        assertEquals("QR_DISABLED", disabledResult.statusCode)

        // 2. Validate Archived Event blocks guest access
        val archivedResult = QrCodeGenerator.validateGuestPortalAccess(baseEvent.copy(status = "Archived"))
        assertFalse(archivedResult.isAllowed)
        assertEquals("EVENT_ARCHIVED", archivedResult.statusCode)

        // 3. Validate Expired QR date blocks guest access
        val expiredResult = QrCodeGenerator.validateGuestPortalAccess(baseEvent.copy(qrExpirationDate = "01 Jan 2020"))
        assertFalse(expiredResult.isAllowed)
        assertEquals("QR_EXPIRED", expiredResult.statusCode)

        // 4. Validate all three access modes when Active & Non-Expired
        val mode1 = QrCodeGenerator.validateGuestPortalAccess(
            baseEvent.copy(guestAccessMode = GuestAccessModes.FACE_SEARCH_ONLY)
        )
        assertTrue(mode1.isAllowed)
        assertTrue(mode1.requiresFaceSearchConsent)
        assertFalse(mode1.allowsImmediateGallery)

        val mode2 = QrCodeGenerator.validateGuestPortalAccess(
            baseEvent.copy(guestAccessMode = GuestAccessModes.VIEW_ALL_PERMITTED)
        )
        assertTrue(mode2.isAllowed)
        assertTrue(mode2.allowsImmediateGallery)

        val mode3 = QrCodeGenerator.validateGuestPortalAccess(
            baseEvent.copy(guestAccessMode = GuestAccessModes.PIN_PROTECTED)
        )
        assertTrue(mode3.isAllowed)
        assertTrue(mode3.requiresOrganizerApproval)

        // 5. Verify UI enforcement in GuestPortalModalScreen for Organizer Approved Access
        val currentEventState = androidx.compose.runtime.mutableStateOf(
            baseEvent.copy(qrEnabled = false)
        )
        composeTestRule.setContent {
            MyApplicationTheme {
                GuestPortalModalScreen(
                    event = currentEventState.value,
                    eventUploads = emptyList(),
                    guestSessions = emptyList(),
                    onRegisterConsentSession = { _, _, _ -> },
                    onDeleteFaceData = {},
                    onReportMismatch = { _, _ -> },
                    onNotifyDownload = {},
                    onClose = {}
                )
            }
        }
        composeTestRule.waitForIdle()

        // When QR is disabled, blocked card is displayed
        composeTestRule.onNodeWithTag("guest_portal_access_blocked_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Event QR Portal Paused").assertIsDisplayed()

        // When QR is expired, blocked card shows expired notice
        currentEventState.value = baseEvent.copy(qrEnabled = true, qrExpirationDate = "01 Jan 2020")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("guest_portal_access_blocked_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Event QR Code Expired").assertIsDisplayed()

        // When QR is active with Organizer Approved Access, approval gate is shown and can be approved
        currentEventState.value = baseEvent.copy(
            qrEnabled = true,
            qrExpirationDate = "01 Mar 2027",
            guestAccessMode = GuestAccessModes.PIN_PROTECTED
        )
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("guest_portal_list")
            .performScrollToNode(hasTestTag("organizer_approval_gate_card"))
        composeTestRule.onNodeWithTag("organizer_approval_gate_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("guest_portal_list")
            .performScrollToNode(hasTestTag("organizer_approve_access_button"))
        composeTestRule.onNodeWithTag("organizer_approve_access_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("guest_portal_list")
            .performScrollToNode(hasTestTag("guest_portal_tab_face_search"))
        composeTestRule.onNodeWithTag("guest_portal_tab_face_search").assertIsDisplayed()
    }
}
