package com.example.rmcfrontend.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.rmcfrontend.R
import com.example.rmcfrontend.compose.components.*
import com.example.rmcfrontend.compose.viewmodel.AuthState
import com.example.rmcfrontend.compose.viewmodel.LastAction

@Composable
fun RegisterScreen(
    state: AuthState,
    onRegister: (firstName: String, lastName: String, email: String, password: String) -> Unit,
    onBack: () -> Unit,
    onRegisterSuccess: () -> Unit
) {
    val first = remember { mutableStateOf("") }
    val last = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val password = remember { mutableStateOf("") }
    val scroll = rememberScrollState()

    LaunchedEffect(state.lastAction) {
        if (state.lastAction == LastAction.REGISTER_SUCCESS) onRegisterSuccess()
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
                title = "Thrilled to join the journey!",
                subtitle = "Create your account to start your driving lessons today.",
                imageRes = R.drawable.img_hero_drive,
                rightIcon = Icons.Outlined.PersonAdd
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
                    Text("Thrilled to join the journey!", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        "Create your account and start using the app.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    AppTextField(
                        value = first.value,
                        onValueChange = { first.value = it },
                        label = "First name",
                        leadingIcon = Icons.Outlined.Person,
                        modifier = Modifier.testTag("register_first")
                    )
                    AppTextField(
                        value = last.value,
                        onValueChange = { last.value = it },
                        label = "Last name",
                        leadingIcon = Icons.Outlined.Person,
                        modifier = Modifier.testTag("register_last")
                    )
                    AppTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        label = "Email address",
                        leadingIcon = Icons.Outlined.Email,
                        modifier = Modifier.testTag("register_email")
                    )
                    AppTextField(
                        value = password.value,
                        onValueChange = { password.value = it },
                        label = "Password",
                        leadingIcon = Icons.Outlined.Lock,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.testTag("register_password")
                    )

                    if (state.errorMessage != null) {
                        ErrorPill(message = state.errorMessage)
                    }

                    GradientButton(
                        text = "Sign up",
                        onClick = {
                            onRegister(
                                first.value.trim(),
                                last.value.trim(),
                                email.value.trim(),
                                password.value
                            )
                        },
                        loading = state.isBusy,
                        modifier = Modifier.testTag("register_button")
                    )

                    Text(
                        text = "or connect with",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SocialRow(onApple = {}, onGoogle = {})

                    SecondaryPillButton(
                        text = "Back to login",
                        onClick = onBack,
                        enabled = !state.isBusy,
                        modifier = Modifier.testTag("register_back")
                    )
                }
            }
            Spacer(Modifier.height(26.dp))
        }
    }
}
