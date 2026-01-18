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
class LoginScreenUiTest {

    @get:Rule
    val rule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun login_click_callsOnLogin_withTrimmedEmail() {
        var gotEmail = ""
        var gotPassword = ""

        rule.setContent {
            RmcTheme {
                LoginScreen(
                    state = AuthState(isLoggedIn = false),
                    onLogin = { e, p ->
                        gotEmail = e
                        gotPassword = p
                    },
                    onNavigateToRegister = {},
                    onLoginSuccess = {}
                )
            }
        }

        rule.onNodeWithTag("login_email").performTextInput("  a@b.com  ")
        rule.onNodeWithTag("login_password").performTextInput("pw")
        rule.onNodeWithTag("login_button").assertIsDisplayed().performClick()

        assertEquals("a@b.com", gotEmail)
        assertEquals("pw", gotPassword)
    }

    @Test
    fun loginSuccess_state_triggersOnLoginSuccessCallback() {
        var called = false

        rule.setContent {
            RmcTheme {
                LoginScreen(
                    state = AuthState(isLoggedIn = true, lastAction = LastAction.LOGIN_SUCCESS),
                    onLogin = { _, _ -> },
                    onNavigateToRegister = {},
                    onLoginSuccess = { called = true }
                )
            }
        }

        rule.waitForIdle()
        assertTrue(called)
    }
}
