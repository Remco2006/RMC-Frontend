package com.example.rmcfrontend.ui.theme.screens.cars

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
import com.example.rmcfrontend.compose.viewmodel.CarsViewModel

@Composable
fun CarsScreen(
    carsViewModel: CarsViewModel,
    onCarClick: (Long) -> Unit,
    onAddCar: () -> Unit
) {
    val cars by carsViewModel.cars.collectAsState()
    val isLoading by carsViewModel.loading.collectAsState()
    val error by carsViewModel.error.collectAsState()

    LaunchedEffect(Unit) {
        carsViewModel.fetchAllCars()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            error != null -> {
                Text("Error: $error", color = Color.Red, modifier = Modifier.align(Alignment.Center))
            }
            cars.isEmpty() -> {
                Text(
                    "Geen auto's gevonden",
                    modifier = Modifier.align(Alignment.Center),
                    style = MaterialTheme.typography.titleMedium
                )
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(cars) { car ->
                        CarListItem(car = car, onClick = { onCarClick(car.id!!) })
                    }
                }
            }
        }

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


@Composable
fun CarListItem(car: Car, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        AsyncImage(
            model = car.imageFileNames.firstOrNull()?.let { "http://10.0.2.2:8080/images/$it" }
                ?: R.drawable.car,
            contentDescription = "Car Image",
            modifier = Modifier.size(88.dp, 64.dp),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text("${car.make ?: "-"} ${car.model ?: "-"}", style = MaterialTheme.typography.titleMedium)
            Text("Year: ${car.modelYear ?: "-"} • Color: ${car.color ?: "-"}")
            Text("Price: €${car.price ?: "-"}")
        }
    }
}
