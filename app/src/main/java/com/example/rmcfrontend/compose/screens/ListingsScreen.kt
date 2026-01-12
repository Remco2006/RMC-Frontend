package com.example.rmcfrontend.compose.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.rmcfrontend.R
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.compose.components.ErrorPill
import com.example.rmcfrontend.compose.components.GradientBackground
import com.example.rmcfrontend.compose.components.ImageHeroHeader
import com.example.rmcfrontend.compose.viewmodel.CarsState

@Composable
fun ListingsScreen(
    state: CarsState,
    onRefresh: () -> Unit,
    onCarClick: (Long) -> Unit,
    onAddCar: () -> Unit
) {
    GradientBackground {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                ImageHeroHeader(
                    title = "Your listings",
                    subtitle = "Manage cars, view details, and add new items.",
                    imageRes = R.drawable.img_card_car
                )

                Spacer(Modifier.height(14.dp))

                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Listings", style = MaterialTheme.typography.headlineSmall)
                                Text(
                                    text = "Tap a car to view or edit.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = onRefresh, enabled = !state.isBusy) {
                                Icon(Icons.Outlined.Refresh, contentDescription = "Refresh")
                            }
                        }

                        if (state.isBusy) {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }

                        if (state.errorMessage != null) {
                            ErrorPill(message = state.errorMessage)
                        }

                        when {
                            !state.isBusy && state.errorMessage == null && state.cars.isEmpty() -> {
                                Text(
                                    "No cars found yet.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            state.cars.isNotEmpty() -> {
                                LazyColumn(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentPadding = PaddingValues(vertical = 6.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    items(state.cars) { car ->
                                        CarCardItem(car = car, onClick = { car.id?.let(onCarClick) })
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }

            FloatingActionButton(
                onClick = onAddCar,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(18.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Car")
            }
        }
    }
}

@Composable
private fun CarCardItem(car: Car, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = car.imageFileNames.firstOrNull()?.let { "http://10.0.2.2:8080/images/$it" } ?: R.drawable.car,
                contentDescription = "Car Image",
                modifier = Modifier
                    .size(width = 92.dp, height = 70.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
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
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}