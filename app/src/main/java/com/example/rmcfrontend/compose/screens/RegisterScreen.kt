package com.example.rmcfrontend.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
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

    LaunchedEffect(state.lastAction) {
        if (state.lastAction == LastAction.REGISTER_SUCCESS) {
            onRegisterSuccess()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Create account")
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = first.value,
            onValueChange = { first.value = it },
            label = { Text("First name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = last.value,
            onValueChange = { last.value = it },
            label = { Text("Last name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = email.value,
            onValueChange = { email.value = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = password.value,
            onValueChange = { password.value = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            visualTransformation = PasswordVisualTransformation()
        )

        if (state.errorMessage != null) {
            Spacer(Modifier.height(12.dp))
            Text(text = state.errorMessage)
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                onRegister(
                    first.value.trim(),
                    last.value.trim(),
                    email.value.trim(),
                    password.value
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isBusy
        ) {
            if (state.isBusy) {
                CircularProgressIndicator(modifier = Modifier.height(18.dp))
            } else {
                Text("Register")
            }
        }

        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isBusy
        ) {
            Text("Back")
        }
    }
}
