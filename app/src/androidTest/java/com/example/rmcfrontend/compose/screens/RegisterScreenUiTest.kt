package com.example.rmcfrontend.compose.screens

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.rmcfrontend.compose.theme.RmcTheme
import com.example.rmcfrontend.compose.viewmodel.AuthState
import com.example.rmcfrontend.compose.viewmodel.LastAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RegisterScreenUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun register_click_callsOnRegister_withTrimmedNamesAndEmail() {
        var gotFirst = ""
        var gotLast = ""
        var gotEmail = ""
        var gotPassword = ""

        rule.setContent {
            RmcTheme {
                RegisterScreen(
                    state = AuthState(),
                    onRegister = { f, l, e, p ->
                        gotFirst = f
                        gotLast = l
                        gotEmail = e
                        gotPassword = p
                    },
                    onBack = {},
                    onRegisterSuccess = {}
                )
            }
        }

        rule.onNodeWithTag("register_first").performTextInput("  Jane  ")
        rule.onNodeWithTag("register_last").performTextInput("  Doe ")
        rule.onNodeWithTag("register_email").performTextInput("  jane@doe.com  ")
        rule.onNodeWithTag("register_password").performTextInput("pw")
        rule.onNodeWithTag("register_button").assertIsDisplayed().performClick()

        assertEquals("Jane", gotFirst)
        assertEquals("Doe", gotLast)
        assertEquals("jane@doe.com", gotEmail)
        assertEquals("pw", gotPassword)
    }

    @Test
    fun registerSuccess_state_triggersOnRegisterSuccessCallback() {
        var called = false

        rule.setContent {
            RmcTheme {
                RegisterScreen(
                    state = AuthState(lastAction = LastAction.REGISTER_SUCCESS),
                    onRegister = { _, _, _, _ -> },
                    onBack = {},
                    onRegisterSuccess = { called = true }
                )
            }
        }

        rule.waitForIdle()
        assertTrue(called)
    }
}
