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
import androidx.compose.ui.test.performTextReplacement
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BannerPresets
import com.example.data.model.GuestAccessModes
import com.example.data.model.HomeBanner
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.PhotographerProfile
import com.example.data.model.WebsiteCustomization
import com.example.data.model.WebsiteLayouts
import com.example.data.model.WebsiteThemes
import com.example.data.model.WebsiteVersionSnapshot
import com.example.ui.auth.AuthScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.EventQrSystemDialog
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.GuestPortalModalScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.OfficialWebsiteScreen
import com.example.ui.screens.PhotographerWebsiteCustomizerScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.UploadsScreen
import com.example.ui.screens.VerifiedGitHubReleaseInfo
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
            studioName = "Jummo Pro Studio",
            driveConnected = false,
            driveFolderName = "Jummo_Events_Cloud"
        )

        var savedName = ""
        var savedStudio = ""
        var savedDriveConnected = false
        var signOutConfirmed = false
        var adminOpened = false

        val mockRelease = VerifiedGitHubReleaseInfo(
            checked = true,
            available = false,
            versionTag = "APK release is being prepared.",
            fileSizeLabel = "Pending release asset",
            updatedDateLabel = "Pending release run",
            downloadUrl = null
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                ProfileScreen(
                    profile = sampleProfile,
                    events = listOf(
                        PhotoEvent(id = "e1", userId = "u1", name = "Royal Wedding", qrEnabled = true)
                    ),
                    uploads = listOf(
                        PhotoUploadRecord(id = "u1", userId = "u1", eventId = "e1", status = "Uploaded", fileSizeBytes = 2048000L)
                    ),
                    guestSessions = emptyList(),
                    onSaveProfileSettings = { name, studio, driveConn, _, _, _, _ ->
                        savedName = name
                        savedStudio = studio
                        savedDriveConnected = driveConn
                    },
                    onDeleteGuestFaceData = {},
                    onOpenAdminDashboard = { adminOpened = true },
                    onSignOut = { signOutConfirmed = true },
                    initialReleaseInfo = mockRelease
                )
            }
        }
        composeTestRule.waitForIdle()

        val listNode = composeTestRule.onNodeWithTag("profile_screen_list")
        listNode.assertIsDisplayed()

        // 1. Hero Banner & Verified Badge & Change Profile Photo Button
        composeTestRule.onNodeWithTag("profile_hero_banner_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("profile_verified_badge").assertIsDisplayed()
        composeTestRule.onNodeWithTag("change_profile_photo_button").assertIsDisplayed()
        composeTestRule.onNodeWithText("Photographer Pro").assertIsDisplayed()

        // 2. Account Editing & Save Changes
        listNode.performScrollToNode(hasTestTag("account_display_name_input"))
        composeTestRule.onNodeWithText("Account").assertIsDisplayed()
        composeTestRule.onNodeWithTag("account_display_name_input").performTextReplacement("Meraj Sir")
        composeTestRule.onNodeWithTag("account_studio_name_input").performTextReplacement("Jummo Luxury Studio")
        listNode.performScrollToNode(hasTestTag("account_save_changes_button"))
        composeTestRule.onNodeWithTag("account_save_changes_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals("Meraj Sir", savedName)
        assertEquals("Jummo Luxury Studio", savedStudio)

        // 3. Google Drive Expand & Connect
        listNode.performScrollToNode(hasText("Google Drive"))
        composeTestRule.onNodeWithText("Google Drive").assertIsDisplayed()
        composeTestRule.onNodeWithTag("drive_section_header").performClick()
        composeTestRule.waitForIdle()
        listNode.performScrollToNode(hasTestTag("connect_google_drive_button"))
        composeTestRule.onNodeWithTag("connect_google_drive_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(savedDriveConnected)

        // 4. Upload Preferences
        listNode.performScrollToNode(hasText("Upload Preferences"))
        composeTestRule.onNodeWithText("Upload Preferences").assertIsDisplayed()
        listNode.performScrollToNode(hasTestTag("save_profile_button"))
        composeTestRule.onNodeWithTag("save_profile_button").assertIsDisplayed()

        // 5. Storage, Privacy, Permissions, Help & Support, About
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

        // 6. Admin Dashboard & Official Website Release Card
        listNode.performScrollToNode(hasTestTag("open_admin_dashboard_row"))
        composeTestRule.onNodeWithTag("open_admin_dashboard_row").performClick()
        assertTrue(adminOpened)

        listNode.performScrollToNode(hasTestTag("official_website_release_card"))
        composeTestRule.onNodeWithTag("official_website_release_card").assertIsDisplayed()

        // 7. Sign Out Confirmation Dialog
        listNode.performScrollToNode(hasTestTag("sign_out_button"))
        composeTestRule.onNodeWithTag("sign_out_button").assertIsDisplayed().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("sign_out_confirm_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithTag("confirm_sign_out_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(signOutConfirmed)
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
        composeTestRule.onNodeWithTag("guest_portal_list")
            .performScrollToNode(hasTestTag("guest_portal_access_blocked_card"))
        composeTestRule.onNodeWithTag("guest_portal_access_blocked_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Event QR Portal Paused").assertIsDisplayed()

        // When QR is expired, blocked card shows expired notice
        currentEventState.value = baseEvent.copy(qrEnabled = true, qrExpirationDate = "01 Jan 2020")
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("guest_portal_list")
            .performScrollToNode(hasTestTag("guest_portal_access_blocked_card"))
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

    @Test
    fun homeDashboardScreen_imageOnlyBannerCarouselRendersPurePhotographsWithoutTextOverlays() {
        val bannersState = androidx.compose.runtime.mutableStateOf(
            listOf(
                HomeBanner(
                    id = "bnr_1",
                    userId = "u1",
                    imageUri = BannerPresets.PRESET_WEDDING,
                    displayOrder = 0L,
                    isActive = true,
                    slideDurationSeconds = 4L
                ),
                HomeBanner(
                    id = "bnr_2",
                    userId = "u1",
                    imageUri = BannerPresets.PRESET_FESTIVAL,
                    displayOrder = 1L,
                    isActive = true,
                    slideDurationSeconds = 4L
                )
            )
        )
        val profileState = androidx.compose.runtime.mutableStateOf(
            PhotographerProfile(
                id = "u1",
                userId = "u1",
                displayName = "Meraj Alam",
                studioName = "Jummo Pro Studio",
                driveConnected = false
            )
        )

        composeTestRule.setContent {
            MyApplicationTheme {
                HomeDashboardScreen(
                    profile = profileState.value,
                    events = emptyList(),
                    uploads = emptyList(),
                    banners = bannersState.value,
                    onCreateNewEventClick = {},
                    onQuickCameraClick = {},
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

        // 1. Verify image-only banner pager is displayed with first active banner slide
        composeTestRule.onNodeWithTag("home_banner_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_banner_pager").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_banner_slide_bnr_1").assertIsDisplayed()
        composeTestRule.onNodeWithTag("home_banner_indicators").assertIsDisplayed()

        // 2. Verify NO promotional text or captions exist over the banner
        composeTestRule.onAllNodes(hasText("PRO STUDIO EDITION")).fetchSemanticsNodes().let {
            assertTrue("Banner must not contain PRO STUDIO EDITION text overlay", it.isEmpty())
        }
        composeTestRule.onAllNodes(hasText("Event Photography")).fetchSemanticsNodes().let {
            assertTrue("Banner must not contain Event Photography text overlay", it.isEmpty())
        }
        composeTestRule.onAllNodes(hasText("Powered by AI")).fetchSemanticsNodes().let {
            assertTrue("Banner must not contain Powered by AI text overlay", it.isEmpty())
        }

        // 3. Verify real Google Drive connection status (Not Connected -> Connect button)
        composeTestRule.onNodeWithTag("home_dashboard_list")
            .performScrollToNode(hasTestTag("drive_connection_card"))
        composeTestRule.onNodeWithText("• Not Connected").assertIsDisplayed()
        composeTestRule.onNodeWithText("Connect").assertIsDisplayed()

        // When Drive is connected, status updates to Connected -> Manage button
        profileState.value = profileState.value.copy(driveConnected = true)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("• Connected").assertIsDisplayed()
        composeTestRule.onNodeWithText("Manage").assertIsDisplayed()

        // 4. When all banners are deactivated, neutral image-only placeholder is shown
        bannersState.value = bannersState.value.map { it.copy(isActive = false) }
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_dashboard_list")
            .performScrollToNode(hasTestTag("home_banner_card"))
        composeTestRule.onNodeWithTag("home_banner_empty_placeholder").assertIsDisplayed()
    }

    @Test
    fun adminDashboardScreen_bannerManagementControlsHomeSliderOrderActiveStatusAndDuration() {
        val bannersState = androidx.compose.runtime.mutableStateOf(
            listOf(
                HomeBanner(
                    id = "bnr_alpha",
                    userId = "u1",
                    imageUri = BannerPresets.PRESET_WEDDING,
                    displayOrder = 0L,
                    isActive = true,
                    slideDurationSeconds = 4L
                ),
                HomeBanner(
                    id = "bnr_beta",
                    userId = "u1",
                    imageUri = BannerPresets.PRESET_FESTIVAL,
                    displayOrder = 1L,
                    isActive = true,
                    slideDurationSeconds = 4L
                )
            )
        )
        val showAdmin = androidx.compose.runtime.mutableStateOf(true)

        composeTestRule.setContent {
            MyApplicationTheme {
                if (showAdmin.value) {
                    AdminDashboardScreen(
                        profile = PhotographerProfile(displayName = "Meraj Alam"),
                        events = emptyList(),
                        uploads = emptyList(),
                        guestSessions = emptyList(),
                        banners = bannersState.value,
                        onAddPresetBanner = { preset ->
                            val nextIdx = bannersState.value.size
                            bannersState.value = bannersState.value + HomeBanner(
                                id = "bnr_added_$nextIdx",
                                userId = "u1",
                                imageUri = preset,
                                displayOrder = nextIdx.toLong(),
                                isActive = true,
                                slideDurationSeconds = bannersState.value.firstOrNull()?.slideDurationSeconds ?: 4L
                            )
                        },
                        onToggleBannerActive = { target ->
                            bannersState.value = bannersState.value.map {
                                if (it.id == target.id) it.copy(isActive = !it.isActive) else it
                            }
                        },
                        onReplaceBannerWithPreset = { target, newPreset ->
                            bannersState.value = bannersState.value.map {
                                if (it.id == target.id) it.copy(imageUri = newPreset) else it
                            }
                        },
                        onReorderBanners = { reordered ->
                            bannersState.value = reordered.mapIndexed { idx, b ->
                                b.copy(displayOrder = idx.toLong())
                            }
                        },
                        onUpdateSlideDuration = { sec ->
                            bannersState.value = bannersState.value.map {
                                it.copy(slideDurationSeconds = sec)
                            }
                        },
                        onDeleteBanner = { target ->
                            bannersState.value = bannersState.value
                                .filter { it.id != target.id }
                                .mapIndexed { idx, b -> b.copy(displayOrder = idx.toLong()) }
                        },
                        onNavigateToTab = {},
                        onClose = { showAdmin.value = false }
                    )
                } else {
                    HomeDashboardScreen(
                        profile = PhotographerProfile(displayName = "Meraj Alam"),
                        events = emptyList(),
                        uploads = emptyList(),
                        banners = bannersState.value,
                        onCreateNewEventClick = {},
                        onOpenEventQr = {},
                        onOpenGuestPortal = {},
                        onSelectEventAndOpenCamera = {},
                        onOpenDriveSettings = {},
                        onNavigateToEventsTab = {}
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        // 1. Verify Admin Banner Management card and live preview are displayed
        val adminList = composeTestRule.onNodeWithTag("admin_dashboard_list")
        adminList.performScrollToNode(hasTestTag("admin_banner_management_card"))
        composeTestRule.onNodeWithTag("admin_banner_management_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Home Banner Management").assertIsDisplayed()

        // 2. Change Auto Slide Duration to 5s
        composeTestRule.onNodeWithTag("admin_banner_duration_5s").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(5L, bannersState.value.first().slideDurationSeconds)

        // 3. Reorder: move bnr_beta up to position #1 (displayOrder = 0)
        composeTestRule.onNodeWithTag("admin_banner_move_up_bnr_beta").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals("bnr_beta", bannersState.value.minByOrNull { it.displayOrder }?.id)

        // 4. Replace bnr_beta studio shot
        val oldUri = bannersState.value.first { it.id == "bnr_beta" }.imageUri
        composeTestRule.onNodeWithTag("admin_banner_replace_bnr_beta").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        val newUri = bannersState.value.first { it.id == "bnr_beta" }.imageUri
        assertNotEquals(oldUri, newUri)

        // 5. Add a new studio banner
        composeTestRule.onNodeWithTag("admin_add_studio_banner_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(3, bannersState.value.size)

        // 6. Deactivate bnr_alpha and delete bnr_added_2
        composeTestRule.onNodeWithTag("admin_banner_toggle_bnr_alpha").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertFalse(bannersState.value.first { it.id == "bnr_alpha" }.isActive)

        composeTestRule.onNodeWithTag("admin_banner_delete_bnr_added_2").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        assertEquals(2, bannersState.value.size)

        // 7. Switch to HomeDashboardScreen and verify only active bnr_beta is displayed in the carousel
        showAdmin.value = false
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("home_banner_slide_bnr_beta").assertIsDisplayed()
    }

    @Test
    fun photographerWebsiteCustomizer_editsBrandingThemeLayoutBannersDraftPublishRollbackAndGuestPortal() {
        val sampleEvent = PhotoEvent(
            id = "evt_wedding_custom",
            userId = "u1",
            name = "Aarav & Meera Royal Wedding",
            category = "Wedding",
            eventDate = "20 Dec 2026",
            location = "Udaipur Palace",
            status = "Active",
            qrEnabled = true,
            qrExpirationDate = "01 Mar 2027",
            guestAccessMode = GuestAccessModes.VIEW_ALL_PERMITTED,
            guestAllowDownload = true
        )
        val sampleUploads = listOf(
            PhotoUploadRecord(
                id = "up_1",
                userId = "u1",
                eventId = sampleEvent.id,
                fileName = "DSC_ROYAL_01.JPG",
                localUri = BannerPresets.PRESET_WEDDING,
                fileSizeBytes = 204800L,
                status = "Uploaded"
            ),
            PhotoUploadRecord(
                id = "up_2",
                userId = "u1",
                eventId = sampleEvent.id,
                fileName = "DSC_ROYAL_02.JPG",
                localUri = BannerPresets.PRESET_FESTIVAL,
                fileSizeBytes = 198400L,
                status = "Uploaded"
            )
        )

        val customizationsState = androidx.compose.runtime.mutableStateOf<List<WebsiteCustomization>>(emptyList())
        val versionsState = androidx.compose.runtime.mutableStateOf<List<WebsiteVersionSnapshot>>(emptyList())
        val viewGuestPortalState = androidx.compose.runtime.mutableStateOf(false)

        composeTestRule.setContent {
            MyApplicationTheme {
                if (!viewGuestPortalState.value) {
                    PhotographerWebsiteCustomizerScreen(
                        profile = PhotographerProfile(
                            displayName = "Meraj Sir",
                            studioName = "Jummo Pro Studio"
                        ),
                        events = listOf(sampleEvent),
                        initialEvent = sampleEvent,
                        uploads = sampleUploads,
                        guestSessions = emptyList(),
                        customizations = customizationsState.value,
                        versions = versionsState.value,
                        onCopyUriToWebsiteStorage = { uri, _ -> uri.toString() },
                        onSaveDraft = { draft ->
                            val saved = draft.copy(hasDraftChanges = true)
                            customizationsState.value = listOf(saved)
                        },
                        onPublishWebsite = { draft, _ ->
                            val nextVer = draft.publishedVersion + 1L
                            val published = draft.copy(
                                isPublished = true,
                                hasDraftChanges = false,
                                publishedVersion = nextVer,
                                publicWebsiteUrl = QrCodeGenerator.buildVerifiedPagesGuestUrl(sampleEvent.id)
                            )
                            customizationsState.value = listOf(published)
                            versionsState.value = listOf(
                                WebsiteVersionSnapshot(
                                    id = "${draft.id}_v$nextVer",
                                    userId = "u1",
                                    eventId = sampleEvent.id,
                                    customizationId = draft.id,
                                    versionNumber = nextVer,
                                    studioName = published.studioName,
                                    photographerName = published.photographerName,
                                    themePreset = published.themePreset,
                                    websiteLayout = published.websiteLayout,
                                    bannerImageUris = published.bannerImageUris,
                                    aiFaceSearchEnabled = published.aiFaceSearchEnabled,
                                    photoGalleryEnabled = published.photoGalleryEnabled,
                                    photoDownloadsEnabled = published.photoDownloadsEnabled,
                                    guestAccessMode = published.guestAccessMode
                                )
                            ) + versionsState.value
                        },
                        onRollbackVersion = { current, snap, _ ->
                            val restored = current.copy(
                                studioName = snap.studioName,
                                themePreset = snap.themePreset,
                                websiteLayout = snap.websiteLayout,
                                publishedVersion = current.publishedVersion + 1L
                            )
                            customizationsState.value = listOf(restored)
                        },
                        onOpenPublicGuestPortal = { _, _ ->
                            viewGuestPortalState.value = true
                        },
                        onNotifyMessage = {},
                        onClose = {}
                    )
                } else {
                    GuestPortalModalScreen(
                        event = sampleEvent,
                        eventUploads = sampleUploads,
                        guestSessions = emptyList(),
                        websiteCustomization = customizationsState.value.firstOrNull(),
                        onRegisterConsentSession = { _, _, _ -> },
                        onDeleteFaceData = {},
                        onReportMismatch = { _, _ -> },
                        onNotifyDownload = {},
                        onClose = { viewGuestPortalState.value = false }
                    )
                }
            }
        }
        composeTestRule.waitForIdle()

        // 1. Verify Photographer Website Customizer screen & default Royal Purple + Editorial configuration
        composeTestRule.onNodeWithTag("photographer_website_customizer_screen").assertIsDisplayed()
        val editorList = composeTestRule.onNodeWithTag("website_customizer_editor_list")

        // 2. Customize Studio Name in Section A
        editorList.performScrollToNode(hasTestTag("website_studio_name_input"))
        composeTestRule.onNodeWithTag("website_studio_name_input")
            .performTextReplacement("Meraj Royal Studio")

        // 3. Switch Theme to Champagne Gold in Section C & Layout to Masonry in Section D
        editorList.performScrollToNode(hasTestTag("website_theme_${WebsiteThemes.CHAMPAGNE_GOLD}"))
        composeTestRule.onNodeWithTag("website_theme_${WebsiteThemes.CHAMPAGNE_GOLD}").performClick()

        editorList.performScrollToNode(hasTestTag("website_layout_${WebsiteLayouts.MASONRY}"))
        composeTestRule.onNodeWithTag("website_layout_${WebsiteLayouts.MASONRY}").performClick()

        // 4. Save Draft and verify draft is stored without publishing
        composeTestRule.onNodeWithTag("website_save_draft_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(1, customizationsState.value.size)
        assertEquals("Meraj Royal Studio", customizationsState.value.first().studioName)
        assertTrue(customizationsState.value.first().hasDraftChanges)

        // 5. Publish Website (v1) and verify version snapshot is recorded
        composeTestRule.onNodeWithTag("website_publish_button").performClick()
        composeTestRule.waitForIdle()
        assertTrue(customizationsState.value.first().isPublished)
        assertEquals(1L, customizationsState.value.first().publishedVersion)
        assertEquals(1, versionsState.value.size)

        // 6. Open Public Guest Event Website and verify all 6 sections & clean image-only banner
        viewGuestPortalState.value = true
        composeTestRule.waitForIdle()

        val guestList = composeTestRule.onNodeWithTag("guest_portal_list")
        composeTestRule.onNodeWithTag("guest_website_header_section").assertIsDisplayed()
        composeTestRule.onNodeWithTag("guest_website_studio_name").assertIsDisplayed()
        composeTestRule.onNodeWithText("Meraj Royal Studio").assertIsDisplayed()

        guestList.performScrollToNode(hasTestTag("guest_website_clean_banner_slider"))
        composeTestRule.onNodeWithTag("guest_website_clean_banner_slider").assertIsDisplayed()

        guestList.performScrollToNode(hasTestTag("guest_gallery_layout_${WebsiteLayouts.MASONRY}"))
        composeTestRule.onNodeWithTag("guest_gallery_layout_${WebsiteLayouts.MASONRY}").assertIsDisplayed()

        guestList.performScrollToNode(hasTestTag("guest_website_footer_section"))
        composeTestRule.onNodeWithTag("guest_website_footer_section").assertIsDisplayed()

        // 7. Verify Admin suspension immediately blocks the Guest Website
        customizationsState.value = customizationsState.value.map { it.copy(isSuspended = true) }
        composeTestRule.waitForIdle()
        guestList.performScrollToNode(hasTestTag("guest_portal_access_blocked_card"))
        composeTestRule.onNodeWithTag("guest_portal_access_blocked_card").assertIsDisplayed()
        composeTestRule.onNodeWithText("Event Website Temporarily Suspended").assertIsDisplayed()
    }

    @Test
    fun uploadsScreen_displaysHeaderTabsAutoUploadImportCardsEventDropdownAndQueueActions() {
        val sampleEvents = listOf(
            PhotoEvent(
                id = "evt_up_1",
                userId = "u1",
                name = "Royal Wedding Gala",
                category = "Wedding",
                eventDate = "15 Dec 2026",
                status = "Active",
                totalPhotos = 8L,
                uploadedPhotos = 4L,
                pendingPhotos = 4L
            ),
            PhotoEvent(
                id = "evt_up_2",
                userId = "u1",
                name = "Corporate Summit",
                category = "Corporate",
                eventDate = "22 Dec 2026",
                status = "Active",
                totalPhotos = 2L,
                uploadedPhotos = 2L,
                pendingPhotos = 0L
            )
        )

        val initialUploads = listOf(
            PhotoUploadRecord(
                id = "q_1",
                userId = "u1",
                eventId = "evt_up_1",
                eventName = "Royal Wedding Gala",
                fileName = "IMG_WED_001.JPG",
                localUri = BannerPresets.PRESET_WEDDING,
                sourceType = "ManualGallery",
                status = "Uploading",
                progress = 68L,
                fileSizeBytes = 3250000L
            ),
            PhotoUploadRecord(
                id = "q_2",
                userId = "u1",
                eventId = "evt_up_1",
                eventName = "Royal Wedding Gala",
                fileName = "DSC_RAW_002.NEF",
                localUri = BannerPresets.PRESET_FESTIVAL,
                sourceType = "DSLRImport",
                status = "Pending",
                progress = 12L,
                fileSizeBytes = 18400000L
            ),
            PhotoUploadRecord(
                id = "q_3",
                userId = "u1",
                eventId = "evt_up_1",
                eventName = "Royal Wedding Gala",
                fileName = "DRV_CLOUD_003.JPG",
                localUri = BannerPresets.PRESET_WEDDING,
                sourceType = "GoogleDriveImport",
                status = "Paused",
                progress = 40L,
                fileSizeBytes = 4100000L
            ),
            PhotoUploadRecord(
                id = "q_4",
                userId = "u1",
                eventId = "evt_up_1",
                eventName = "Royal Wedding Gala",
                fileName = "CAM_STUDIO_004.JPG",
                localUri = BannerPresets.PRESET_WEDDING,
                sourceType = "CameraCapture",
                status = "Pending",
                progress = 0L,
                fileSizeBytes = 2900000L
            ),
            PhotoUploadRecord(
                id = "done_1",
                userId = "u1",
                eventId = "evt_up_1",
                eventName = "Royal Wedding Gala",
                fileName = "IMG_DONE_101.JPG",
                localUri = BannerPresets.PRESET_WEDDING,
                sourceType = "ManualGallery",
                status = "Uploaded",
                progress = 100L,
                fileSizeBytes = 3100000L
            ),
            PhotoUploadRecord(
                id = "done_2",
                userId = "u1",
                eventId = "evt_up_1",
                eventName = "Royal Wedding Gala",
                fileName = "IMG_DONE_102.JPG",
                localUri = BannerPresets.PRESET_FESTIVAL,
                sourceType = "DSLRImport",
                status = "Uploaded",
                progress = 100L,
                fileSizeBytes = 5200000L
            ),
            PhotoUploadRecord(
                id = "done_3",
                userId = "u1",
                eventId = "evt_up_2",
                eventName = "Corporate Summit",
                fileName = "IMG_DONE_103.JPG",
                localUri = BannerPresets.PRESET_WEDDING,
                sourceType = "GoogleDriveImport",
                status = "Uploaded",
                progress = 100L,
                fileSizeBytes = 2800000L
            ),
            PhotoUploadRecord(
                id = "done_4",
                userId = "u1",
                eventId = "evt_up_2",
                eventName = "Corporate Summit",
                fileName = "IMG_DONE_104.JPG",
                localUri = BannerPresets.PRESET_WEDDING,
                sourceType = "CameraCapture",
                status = "Uploaded",
                progress = 100L,
                fileSizeBytes = 3400000L
            )
        )

        val profileState = androidx.compose.runtime.mutableStateOf(
            PhotographerProfile(
                userId = "u1",
                displayName = "Meraj Sir",
                studioName = "Jummo Pro Studio",
                driveConnected = true,
                driveFolderName = "Jummo_Cloud_2026",
                autoUploadEnabled = true,
                wifiOnlyUpload = true,
                batteryNotLowUpload = true
            )
        )
        val uploadsState = androidx.compose.runtime.mutableStateOf(initialUploads)
        val selectedEventIdState = androidx.compose.runtime.mutableStateOf("evt_up_1")
        var uploadNowTriggered = false

        composeTestRule.setContent {
            MyApplicationTheme {
                UploadsScreen(
                    profile = profileState.value,
                    events = sampleEvents,
                    selectedEventId = selectedEventIdState.value,
                    uploads = uploadsState.value,
                    onSelectEvent = { selectedEventIdState.value = it },
                    onCreateEventClick = {},
                    onImportPhotos = { _, _, _, _, _ -> },
                    onUploadAllPendingNow = { uploadNowTriggered = true },
                    onRetrySingleUpload = {},
                    onUpdateAutoUploadSettings = { auto, wifi, batt ->
                        profileState.value = profileState.value.copy(
                            autoUploadEnabled = auto,
                            wifiOnlyUpload = wifi,
                            batteryNotLowUpload = batt
                        )
                    },
                    onPauseAllUploads = {
                        uploadsState.value = uploadsState.value.map {
                            if (it.status == "Pending" || it.status == "Uploading") it.copy(status = "Paused") else it
                        }
                    },
                    onResumeAllUploads = {
                        uploadsState.value = uploadsState.value.map {
                            if (it.status == "Paused") it.copy(status = "Pending") else it
                        }
                    },
                    onCancelAllUploads = {
                        uploadsState.value = uploadsState.value.filter { it.status == "Uploaded" }
                    }
                )
            }
        }
        composeTestRule.waitForIdle()

        val listNode = composeTestRule.onNodeWithTag("uploads_screen_list")

        // 1. Verify Header & Title
        composeTestRule.onNodeWithTag("uploads_studio_header").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jummo Photos AI").assertIsDisplayed()
        composeTestRule.onNodeWithText("Smart Event Photography Suite").assertIsDisplayed()
        composeTestRule.onNodeWithText("Uploads").assertIsDisplayed()
        composeTestRule.onNodeWithText("Upload, monitor and manage photos").assertIsDisplayed()

        // 2. Verify Status Tabs: Queue (4) | Uploaded (4) | Failed (0)
        composeTestRule.onNodeWithText("Queue (4)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Uploaded (4)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Failed (0)").assertIsDisplayed()

        // 3. Verify Automatic Upload Card & Toggles
        listNode.performScrollToNode(hasTestTag("automatic_upload_card"))
        composeTestRule.onNodeWithText("Automatic Upload").assertIsDisplayed()
        composeTestRule.onNodeWithText("Background upload via WorkManager").assertIsDisplayed()
        composeTestRule.onNodeWithTag("wifi_only_toggle_pill").performClick()
        composeTestRule.waitForIdle()
        assertFalse(profileState.value.wifiOnlyUpload)

        // 4. Verify Three Import Cards: Select Photos, DSLR / SD Import, Google Drive Import
        listNode.performScrollToNode(hasTestTag("select_gallery_photos_button"))
        composeTestRule.onNodeWithTag("select_gallery_photos_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("dslr_saf_import_button").assertIsDisplayed()
        composeTestRule.onNodeWithTag("google_drive_import_button").assertIsDisplayed()

        // 5. Verify Destination Event Dropdown & Upload Now Action
        listNode.performScrollToNode(hasTestTag("uploads_event_dropdown_button"))
        composeTestRule.onNodeWithTag("uploads_event_dropdown_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("dropdown_event_evt_up_2").performClick()
        composeTestRule.waitForIdle()
        assertEquals("evt_up_2", selectedEventIdState.value)

        composeTestRule.onNodeWithTag("upload_now_button").performClick()
        assertTrue(uploadNowTriggered)

        // 6. Verify Queue Items & Pause All / Resume All / Switch to Uploaded Tab
        listNode.performScrollToNode(hasTestTag("upload_item_card_q_1"))
        composeTestRule.onNodeWithText("IMG_WED_001.JPG").assertIsDisplayed()

        listNode.performScrollToNode(hasTestTag("uploads_pause_all_button"))
        composeTestRule.onNodeWithTag("uploads_pause_all_button").performClick()
        composeTestRule.waitForIdle()
        assertEquals(4, uploadsState.value.count { it.status == "Paused" })

        listNode.performScrollToNode(hasTestTag("uploads_tab_Uploaded"))
        composeTestRule.onNodeWithTag("uploads_tab_Uploaded").performClick()
        composeTestRule.waitForIdle()
        listNode.performScrollToNode(hasTestTag("upload_item_card_done_1"))
        composeTestRule.onNodeWithText("IMG_DONE_101.JPG").assertIsDisplayed()
    }
}
