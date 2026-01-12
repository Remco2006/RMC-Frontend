package com.example.rmcfrontend.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.rmcfrontend.R
import com.example.rmcfrontend.compose.components.*
import com.example.rmcfrontend.compose.viewmodel.AuthState
import com.example.rmcfrontend.compose.viewmodel.LastAction

@Composable
fun LoginScreen(
    state: AuthState,
    onLogin: (email: String, password: String) -> Unit,
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val scroll = rememberScrollState()

    LaunchedEffect(state.lastAction) {
        if (state.lastAction == LastAction.LOGIN_SUCCESS) onLoginSuccess()
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(20.dp)
        ) {
            Spacer(Modifier.height(10.dp))
            ImageHeroHeader(
                title = "Start your journey",
                subtitle = "Log in to track your progress and manage your listings.",
                imageRes = R.drawable.img_login_header,
                rightIcon = Icons.Outlined.DirectionsCar
            )
            Spacer(Modifier.height(18.dp))

            ElevatedCard(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(26.dp),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Welcome back", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Use your account to continue.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AppTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        label = "Email address",
                        leadingIcon = Icons.Outlined.Email
                    )
                    AppTextField(
                        value = password.value,
                        onValueChange = { password.value = it },
                        label = "Password",
                        leadingIcon = Icons.Outlined.Lock,
                        visualTransformation = PasswordVisualTransformation()
                    )

                    if (state.errorMessage != null) {
                        ErrorPill(message = state.errorMessage)
                    }

                    GradientButton(
                        text = "Log in",
                        onClick = { onLogin(email.value.trim(), password.value) },
                        loading = state.isBusy
                    )

                    Text(
                        text = "or continue with",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SocialRow(onApple = {}, onGoogle = {})

                    SecondaryPillButton(
                        text = "Create account",
                        onClick = onNavigateToRegister,
                        enabled = !state.isBusy
                    )
                }
            }
            Spacer(Modifier.height(26.dp))
        }
    }
}
