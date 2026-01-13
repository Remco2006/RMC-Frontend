package com.example.rmcfrontend.compose.screens.cars

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.api.models.CarSearchFilterRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarSearchScreen(
    cars: List<Car>,
    onSearch: (CarSearchFilterRequest) -> Unit,
    onCarClick: (Car) -> Unit,
    isLoading: Boolean = false,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var showFilters by remember { mutableStateOf(false) }
    var selectedMake by remember { mutableStateOf<String?>(null) }
    var maxDistance by remember { mutableStateOf("") }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }

    // Haal unieke merken uit de auto's
    val availableMakes = remember(cars) {
        cars.mapNotNull { it.make }.distinct().sorted()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Auto's zoeken") },
                actions = {
                    IconButton(onClick = { showFilters = !showFilters }) {
                        Icon(
                            if (showFilters) Icons.Default.FilterAltOff else Icons.Default.FilterAlt,
                            contentDescription = "Filters"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Zoekbalk
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Zoek op merk, model, kleur...") },
                leadingIcon = { Icon(Icons.Default.Search, "Zoeken") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, "Wissen")
                        }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = {
                        onSearch(
                            CarSearchFilterRequest(
                                searchQuery = searchQuery.ifEmpty { null },
                                make = selectedMake,
                                maxDistanceKm = maxDistance.toDoubleOrNull(),
                                minPrice = minPrice.toDoubleOrNull(),
                                maxPrice = maxPrice.toDoubleOrNull()
                            )
                        )
                    }
                ),
                singleLine = true
            )

            // Filter sectie
            if (showFilters) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            "Filters",
                            style = MaterialTheme.typography.titleMedium
                        )

                        // Merk dropdown
                        var expandedMake by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandedMake,
                            onExpandedChange = { expandedMake = it }
                        ) {
                            OutlinedTextField(
                                value = selectedMake ?: "Alle merken",
                                onValueChange = {},
                                readOnly = true,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                label = { Text("Merk") },
                                trailingIcon = {
                                    Row {
                                        if (selectedMake != null) {
                                            IconButton(onClick = { selectedMake = null }) {
                                                Icon(Icons.Default.Clear, "Wissen")
                                            }
                                        }
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedMake)
                                    }
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = expandedMake,
                                onDismissRequest = { expandedMake = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Alle merken") },
                                    onClick = {
                                        selectedMake = null
                                        expandedMake = false
                                    }
                                )
                                availableMakes.forEach { make ->
                                    DropdownMenuItem(
                                        text = { Text(make) },
                                        onClick = {
                                            selectedMake = make
                                            expandedMake = false
                                        }
                                    )
                                }
                            }
                        }

                        // Afstand filter
                        OutlinedTextField(
                            value = maxDistance,
                            onValueChange = { maxDistance = it.filter { char -> char.isDigit() || char == '.' } },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Max. afstand (km)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            leadingIcon = { Icon(Icons.Default.LocationOn, null) },
                            trailingIcon = {
                                if (maxDistance.isNotEmpty()) {
                                    IconButton(onClick = { maxDistance = "" }) {
                                        Icon(Icons.Default.Clear, "Wissen")
                                    }
                                }
                            },
                            singleLine = true
                        )

                        // Prijs range
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = minPrice,
                                onValueChange = { minPrice = it.filter { char -> char.isDigit() || char == '.' } },
                                modifier = Modifier.weight(1f),
                                label = { Text("Min. prijs (€)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                            OutlinedTextField(
                                value = maxPrice,
                                onValueChange = { maxPrice = it.filter { char -> char.isDigit() || char == '.' } },
                                modifier = Modifier.weight(1f),
                                label = { Text("Max. prijs (€)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                singleLine = true
                            )
                        }

                        // Zoek knop
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedMake = null
                                    maxDistance = ""
                                    minPrice = ""
                                    maxPrice = ""
                                    onSearch(CarSearchFilterRequest())
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Reset")
                            }
                            Button(
                                onClick = {
                                    onSearch(
                                        CarSearchFilterRequest(
                                            searchQuery = searchQuery.ifEmpty { null },
                                            make = selectedMake,
                                            maxDistanceKm = maxDistance.toDoubleOrNull(),
                                            minPrice = minPrice.toDoubleOrNull(),
                                            maxPrice = maxPrice.toDoubleOrNull()
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Search, null)
                                Spacer(Modifier.width(8.dp))
                                Text("Zoeken")
                            }
                        }
                    }
                }
            }

            // Resultaten
            Box(modifier = Modifier.fillMaxSize()) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (cars.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(16.dp))
                        Text(
                            "Geen auto's gevonden",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            "Pas je filters aan",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text(
                                "${cars.size} auto's gevonden",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        items(cars) { car ->
                            CarCard(car = car, onClick = { onCarClick(car) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CarCard(
    car: Car,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "${car.make} ${car.model}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    car.pickupLocation ?: "Locatie onbekend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "€${car.bookingCost}/dag",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        "€${car.costPerKilometer}/km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}