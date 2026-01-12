package com.example.rmcfrontend.compose.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.compose.viewmodel.CarsState

@Composable
fun ListingsScreen(
    state: CarsState,
    onRefresh: () -> Unit
) {
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
            Text(text = "Listings")
            Button(onClick = onRefresh, enabled = !state.isBusy) {
                Text("Refresh")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (state.isBusy) {
            CircularProgressIndicator()
            return
        }

        if (state.errorMessage != null) {
            Text(text = state.errorMessage)
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(state.cars) { car ->
                CarRow(car)
                Divider()
            }
        }
    }
}

@Composable
private fun CarRow(car: Car) {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 10.dp)
    ) {
        Text(text = car.model ?: "(unknown model)")
        Text(text = buildString {
            append(car.make ?: "")
            if (!car.licensePlate.isNullOrBlank()) {
                if (isNotEmpty()) append(" · ")
                append(car.licensePlate)
            }
        })
    }
}
