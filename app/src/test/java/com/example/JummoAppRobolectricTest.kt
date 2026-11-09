package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToNode
import com.example.data.model.PhotoEvent
import com.example.data.model.PhotoUploadRecord
import com.example.data.model.PhotographerProfile
import com.example.ui.screens.EventsScreen
import com.example.ui.screens.GuestPortalModalScreen
import com.example.ui.screens.HomeDashboardScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.QrCodeGenerator
import org.junit.Assert.assertEquals
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
}
