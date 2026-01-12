package com.example.rmcfrontend.compose.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.rmcfrontend.R
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.compose.viewmodel.CarsState

@Composable
fun ListingsScreen(
    state: CarsState,
    onRefresh: () -> Unit,
    onCarClick: (Long) -> Unit,
    onAddCar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header met titel en refresh knop
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Listings",
                style = MaterialTheme.typography.headlineMedium
            )
            Button(
                onClick = onRefresh,
                enabled = !state.isBusy
            ) {
                Text("Refresh")
            }
        }

        Spacer(Modifier.height(12.dp))

        // Content
        Box(modifier = Modifier.fillMaxSize()) {
            when {
                state.isBusy -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage,
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.cars.isEmpty() -> {
                    Text(
                        "Geen auto's gevonden",
                        modifier = Modifier.align(Alignment.Center),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.cars) { car ->
                            CarListItem(car = car, onClick = { onCarClick(car.id!!) })
                            HorizontalDivider()
                        }
                    }
                }
            }

            // Floating Action Button voor nieuwe auto toevoegen
            FloatingActionButton(
                onClick = onAddCar,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Car")
            }
        }
    }
}

@Composable
private fun CarListItem(car: Car, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp)
    ) {
        AsyncImage(
            model = car.imageFileNames.firstOrNull()?.let { "http://10.0.2.2:8080/images/$it" }
                ?: R.drawable.car,
            contentDescription = "Car Image",
            modifier = Modifier.size(88.dp, 64.dp),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )

        Spacer(Modifier.width(12.dp))

        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = car.model ?: "(unknown model)",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = buildString {
                    append(car.make ?: "")
                    if (car.modelYear != null) {
                        if (isNotEmpty()) append(" · ")
                        append(car.modelYear)
                    }
                    if (!car.licensePlate.isNullOrBlank()) {
                        if (isNotEmpty()) append(" · ")
                        append(car.licensePlate)
                    }
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (car.price != null) {
                Text(
                    text = "€${car.price}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}