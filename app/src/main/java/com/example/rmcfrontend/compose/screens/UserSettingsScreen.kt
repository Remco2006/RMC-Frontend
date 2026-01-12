package com.example.rmcfrontend.compose.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Logout
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.ManageAccounts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rmcfrontend.compose.components.*
import com.example.rmcfrontend.compose.viewmodel.UserState

@Composable
fun UserSettingsScreen(
    state: UserState,
    onReload: () -> Unit,
    onSave: (firstName: String, lastName: String, email: String) -> Unit,
    onDisable: () -> Unit,
    onLogout: () -> Unit
) {
    val first = remember { mutableStateOf("") }
    val last = remember { mutableStateOf("") }
    val email = remember { mutableStateOf("") }
    val scroll = rememberScrollState()

    LaunchedEffect(state.user) {
        state.user?.let {
            first.value = it.firstName
            last.value = it.lastName
            email.value = it.email
        }
    }

    GradientBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp)
        ) {
            AuthHeader(
                title = "User settings",
                subtitle = "Manage your profile, access, and security.",
                icon = Icons.Outlined.ManageAccounts
            )

            Spacer(Modifier.height(16.dp))

            ElevatedCard(
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Profile", style = MaterialTheme.typography.headlineSmall)
                            Text(
                                text = "Update your details. Changes are saved to the API.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onReload, enabled = !state.isBusy) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "Reload")
                        }
                    }

                    if (state.isBusy) {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    if (state.errorMessage != null) {
                        ErrorPill(message = state.errorMessage)
                    }

                    AppTextField(
                        value = first.value,
                        onValueChange = { first.value = it },
                        label = "First name",
                        leadingIcon = Icons.Outlined.Person
                    )
                    AppTextField(
                        value = last.value,
                        onValueChange = { last.value = it },
                        label = "Last name",
                        leadingIcon = Icons.Outlined.Person
                    )
                    AppTextField(
                        value = email.value,
                        onValueChange = { email.value = it },
                        label = "Email address",
                        leadingIcon = Icons.Outlined.Email
                    )

                    GradientButton(
                        text = "Save changes",
                        leadingIcon = Icons.Outlined.Save,
                        onClick = { onSave(first.value.trim(), last.value.trim(), email.value.trim()) },
                        loading = state.isBusy
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            ElevatedCard(
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Account", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "You can disable your account. Disabled accounts cannot log in.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onDisable,
                        enabled = !state.isBusy,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.tertiary,
                            contentColor = MaterialTheme.colorScheme.onTertiary
                        )
                    ) {
                        Icon(Icons.Outlined.DeleteForever, contentDescription = null)
                        Spacer(Modifier.width(10.dp))
                        Text("Disable account", style = MaterialTheme.typography.labelLarge)
                    }

                    SecondaryPillButton(
                        text = "Logout",
                        onClick = onLogout,
                        enabled = !state.isBusy,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}
