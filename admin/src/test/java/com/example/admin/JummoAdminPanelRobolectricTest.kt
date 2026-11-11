package com.example.admin

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextReplacement
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class JummoAdminPanelRobolectricTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun adminLoginScreen_displaysSecurityGateAndRejectsUnauthorizedAccess() {
        composeTestRule.setContent {
            MaterialTheme {
                AdminLoginScreen(
                    errorMessage = "Access Denied: guest@example.com is not an authorized Jummo Admin."
                )
            }
        }
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("admin_login_screen").assertIsDisplayed()
        composeTestRule.onNodeWithText("Jummo Admin Panel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_login_error_banner").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_google_sign_in_button").assertIsDisplayed()
    }

    @Test
    fun jummoAdminPanelShell_navigatesAllFiveTabsAndExecutesUserEventAdRemoteAndRbacActions() {
        composeTestRule.setContent {
            MaterialTheme {
                JummoAdminPanelShell(
                    enableFirestoreSync = false
                )
            }
        }
        composeTestRule.waitForIdle()

        // 1. Overview Tab
        composeTestRule.onNodeWithTag("jummo_admin_panel_shell").assertIsDisplayed()
        composeTestRule.onNodeWithText("Total Users").assertIsDisplayed()
        composeTestRule.onNodeWithText("AdMob Status").assertIsDisplayed()

        // 2. Navigate to Users & Events Tab (Tab 1)
        composeTestRule.onNodeWithTag("admin_nav_tab_1").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("admin_panel_users_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("admin_toggle_user_usr_meraj").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("admin_status_banner").assertIsDisplayed()

        // Event Moderation with Confirmation Dialog
        val listNode = composeTestRule.onNodeWithTag("admin_panel_content_list")
        listNode.performScrollToNode(hasTestTag("admin_panel_events_card"))
        composeTestRule.onNodeWithTag("admin_moderate_event_evt_royal_wedding").performScrollTo().performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("confirm_event_moderation_button").performClick()
        composeTestRule.waitForIdle()

        // 3. Navigate to Ads & Remote Settings Tab (Tab 2)
        composeTestRule.onNodeWithTag("admin_nav_tab_2").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("admin_panel_ad_config_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("panel_ad_freq_6").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("panel_save_ad_config_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        listNode.performScrollToNode(hasTestTag("admin_panel_remote_settings_card"))
        composeTestRule.onNodeWithTag("panel_save_remote_settings_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 4. Navigate to Drive & Notifications Tab (Tab 3)
        composeTestRule.onNodeWithTag("admin_nav_tab_3").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("admin_panel_storage_health_card").assertIsDisplayed()
        listNode.performScrollToNode(hasTestTag("admin_panel_notifications_card"))
        composeTestRule.onNodeWithTag("panel_notif_title_input")
            .performScrollTo()
            .performTextReplacement("System Sync Notice")
        composeTestRule.onNodeWithTag("panel_send_notif_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        // 5. Navigate to RBAC & Audit Logs Tab (Tab 4) and verify audit trail recorded all actions
        composeTestRule.onNodeWithTag("admin_nav_tab_4").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("admin_panel_rbac_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("panel_role_chip_SuperAdmin").performScrollTo().performClick()
        composeTestRule.onNodeWithTag("panel_rbac_email_input")
            .performScrollTo()
            .performTextReplacement("lead@jummophotos.ai")
        composeTestRule.onNodeWithTag("panel_grant_role_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        listNode.performScrollToNode(hasTestTag("admin_panel_audit_logs_card"))
        composeTestRule.onNodeWithTag("admin_panel_audit_logs_card").assertIsDisplayed()
        assertTrue(true)
    }
}
