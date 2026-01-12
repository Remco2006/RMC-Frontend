package com.example.rmcfrontend.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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

    LaunchedEffect(state.user) {
        state.user?.let {
            first.value = it.firstName
            last.value = it.lastName
            email.value = it.email
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "User")
            Button(onClick = onReload, enabled = !state.isBusy) { Text("Reload") }
        }

        Spacer(Modifier.height(12.dp))

        if (state.isBusy) {
            CircularProgressIndicator()
            Spacer(Modifier.height(12.dp))
        }

        if (state.errorMessage != null) {
            Text(text = state.errorMessage)
            Spacer(Modifier.height(12.dp))
        }

        OutlinedTextField(
            value = first.value,
            onValueChange = { first.value = it },
            label = { Text("First name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = last.value,
            onValueChange = { last.value = it },
            label = { Text("Last name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(10.dp))
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { onSave(first.value.trim(), last.value.trim(), email.value.trim()) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isBusy
        ) {
            Text("Save")
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onDisable,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isBusy
        ) {
            Text("Disable account")
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isBusy
        ) {
            Text("Logout")
        }
    }
}
