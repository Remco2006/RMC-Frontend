package com.example.rmcfrontend.compose.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rmcfrontend.api.models.response.UserResponse
import com.example.rmcfrontend.compose.theme.RmcTheme
import com.example.rmcfrontend.compose.viewmodel.UserState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UserSettingsScreenUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun userSettings_actions_callCallbacks() {
        var reloadCalled = false
        var disableCalled = false
        var logoutCalled = false
        var savedFirst = ""
        var savedLast = ""
        var savedEmail = ""

        val state = UserState(
            isBusy = false,
            user = UserResponse(id = 1L, firstName = "John", lastName = "Smith", email = "john@smith.com")
        )

        rule.setContent {
            RmcTheme {
                UserSettingsScreen(
                    state = state,
                    onReload = { reloadCalled = true },
                    onSave = { f, l, e ->
                        savedFirst = f
                        savedLast = l
                        savedEmail = e
                    },
                    onDisable = { disableCalled = true },
                    onLogout = { logoutCalled = true }
                )
            }
        }

        // Reload
        rule.onNodeWithTag("user_reload").assertIsDisplayed().performClick()
        assertTrue(reloadCalled)

        // Edit fields + Save
        rule.onNodeWithTag("user_first").performTextClearance()
        rule.onNodeWithTag("user_first").performTextInput("  Jane ")
        rule.onNodeWithTag("user_last").performTextClearance()
        rule.onNodeWithTag("user_last").performTextInput("  Doe ")
        rule.onNodeWithTag("user_email").performTextClearance()
        rule.onNodeWithTag("user_email").performTextInput("  jane@doe.com  ")

        rule.onNodeWithTag("user_save").assertIsDisplayed().performClick()
        assertEquals("Jane", savedFirst)
        assertEquals("Doe", savedLast)
        assertEquals("jane@doe.com", savedEmail)

        // Disable + Logout
        rule.onNodeWithTag("user_disable").assertIsDisplayed().performClick()
        rule.onNodeWithTag("user_logout").assertIsDisplayed().performClick()

        assertTrue(disableCalled)
        assertTrue(logoutCalled)
    }
}
