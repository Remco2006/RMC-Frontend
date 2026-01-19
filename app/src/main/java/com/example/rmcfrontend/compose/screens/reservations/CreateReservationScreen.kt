package com.example.rmcfrontend.compose.screens.reservations

import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.rmcfrontend.api.models.Car
import com.example.rmcfrontend.api.models.CarSearchFilterRequest
import com.example.rmcfrontend.api.models.CreateReservationRequest
import com.example.rmcfrontend.api.models.response.GetTermResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import androidx.compose.material3.Text
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.CheckCircle
import com.example.rmcfrontend.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateReservationScreen(
    initialDate: LocalDate,
    userId: Long,
    availableCars: List<Car>,
    onCarSelected: (Long) -> Unit,
    terms: GetTermResponse?,
    unavailableTimeSlots: List<Pair<LocalTime, LocalTime>>,
    onCreateReservation: (CreateReservationRequest) -> Unit,
    onNavigateBack: () -> Unit,
    onSearchCars: (CarSearchFilterRequest) -> Unit = {},
    errorMessage: String? = null,
    successMessage: String? = null,
    isLoading: Boolean = false,
    onErrorDismiss: () -> Unit = {},
    onSuccessDismiss: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedCar by remember { mutableStateOf<Car?>(null) }
    var selectedDate by remember { mutableStateOf(initialDate) }
    var startTime by remember { mutableStateOf<LocalTime?>(null) }
    var endTime by remember { mutableStateOf<LocalTime?>(null) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }
    var showMapView by remember { mutableStateOf(false) }
    var showSearchDialog by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    val filteredCars = remember(availableCars, searchQuery) {
        if (searchQuery.isBlank()) {
            availableCars
        } else {
            availableCars.filter { car ->
                "${car.make} ${car.model}".contains(searchQuery, ignoreCase = true) ||
                        car.pickupLocation?.contains(searchQuery, ignoreCase = true) == true ||
                        car.category?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Long
            )
            onErrorDismiss()
        }
    }

    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(
                message = it,
                duration = SnackbarDuration.Short
            )
            onSuccessDismiss()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nieuwe Reservering") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Terug")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Datum sectie
            Text(
                "Datum",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            DatePickerCard(
                selectedDate = selectedDate,
                onDateChange = { selectedDate = it }
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Selecteer een auto",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Zoek knop
                    IconButton(
                        onClick = { showSearchDialog = true }
                    ) {
                        Icon(
                            Icons.Default.Search,
                            contentDescription = "Zoeken",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    // Map knop
                    IconButton(
                        onClick = { showMapView = true }
                    ) {
                        Icon(
                            Icons.Default.Map,
                            contentDescription = "Kaart weergave",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Zoekbalk (altijd zichtbaar)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Zoek op merk, model of locatie...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Wissen")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredCars.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Text(
                                if (searchQuery.isNotEmpty())
                                    "Geen auto's gevonden voor '$searchQuery'"
                                else
                                    "Geen auto's beschikbaar"
                            )
                        }
                    }
                }
            } else {
                // Aantal resultaten
                Text(
                    "${filteredCars.size} auto${if (filteredCars.size != 1) "'s" else ""} gevonden",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )

                // LazyColumn met vaste hoogte voor de auto lijst
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCars) { car ->
                            CarSelectionCard(
                                car = car,
                                isSelected = selectedCar?.id == car.id,
                                onClick = {
                                    selectedCar = car
                                    car.id?.let { onCarSelected(it) }
                                }
                            )
                        }
                    }
                }
            }

            // Tijd selectie (alleen als auto geselecteerd)
            if (selectedCar != null) {
                Text(
                    "Selecteer tijd",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                TimeSelectionSection(
                    startTime = startTime,
                    endTime = endTime,
                    unavailableSlots = unavailableTimeSlots,
                    onStartTimeClick = { showStartTimePicker = true },
                    onEndTimeClick = { showEndTimePicker = true }
                )

                // Terms & Conditions
                if (startTime != null && endTime != null && terms != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                "Algemene voorwaarden",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = { showTermsDialog = true }
                            ) {
                                Text("Lees de voorwaarden")
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = termsAccepted,
                                    onCheckedChange = { termsAccepted = it }
                                )
                                Text("Ik ga akkoord met de voorwaarden")
                            }
                        }
                    }
                }

                // Bevestig knop
                if (startTime != null && endTime != null && termsAccepted) {
                    Button(
                        onClick = {
                            selectedCar?.id?.let { carId ->
                                terms?.id?.let { termId ->
                                    val startDateTime = startTime.let {
                                        LocalDateTime.of(selectedDate, it).withSecond(0).withNano(0)
                                    }
                                    val endDateTime = endTime.let {
                                        LocalDateTime.of(selectedDate, it).withSecond(0).withNano(0)
                                    }

                                    val request = CreateReservationRequest(
                                        startTime = startDateTime.toString(),
                                        endTime = endDateTime.toString(),
                                        userId = userId,
                                        carId = carId,
                                        termId = termId,
                                        status = "CONFIRMED",
                                        startMileage = 0,
                                        endMileage = 0,
                                        costPerKm = (selectedCar?.costPerKilometer ?: 0.0).toString()
                                    )
                                    onCreateReservation(request)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bezig met reserveren...")
                        } else {
                            Icon(Icons.Default.Check, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Bevestig reservering")
                        }
                    }
                }
            }
        }
    }

    // Advanced Search Dialog
    if (showSearchDialog) {
        AdvancedSearchDialog(
            onDismiss = { showSearchDialog = false },
            onSearch = { filter ->
                onSearchCars(filter)
                showSearchDialog = false
            }
        )
    }

    // Map View Dialog
    if (showMapView) {
        CarMapDialog(
            cars = filteredCars,
            selectedCar = selectedCar,
            onDismiss = { showMapView = false },
            onCarSelected = { car ->
                selectedCar = car
                car.id?.let { onCarSelected(it) }
                showMapView = false
            }
        )
    }

    // Terms dialog
    if (showTermsDialog && terms != null) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text(terms.title ?: "Algemene voorwaarden") },
            text = {
                Text(terms.content ?: "")
            },
            confirmButton = {
                TextButton(onClick = {
                    showTermsDialog = false
                    termsAccepted = true
                }) {
                    Text("Akkoord")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTermsDialog = false }) {
                    Text("Sluiten")
                }
            }
        )
    }

    // Time pickers
    if (showStartTimePicker) {
        TimePickerDialog(
            onDismiss = { showStartTimePicker = false },
            onConfirm = { hour, minute ->
                startTime = LocalTime.of(hour, minute)
                showStartTimePicker = false
            }
        )
    }

    if (showEndTimePicker) {
        TimePickerDialog(
            onDismiss = { showEndTimePicker = false },
            onConfirm = { hour, minute ->
                endTime = LocalTime.of(hour, minute)
                showEndTimePicker = false
            }
        )
    }
}

@Composable
fun AdvancedSearchDialog(
    onDismiss: () -> Unit,
    onSearch: (CarSearchFilterRequest) -> Unit
) {
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var minPrice by remember { mutableStateOf("") }
    var maxPrice by remember { mutableStateOf("") }
    var minSeats by remember { mutableStateOf("") }
    var maxDistance by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Geavanceerd zoeken") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = make,
                    onValueChange = { make = it },
                    label = { Text("Merk") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("Model") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Categorie") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = minPrice,
                        onValueChange = { minPrice = it },
                        label = { Text("Min prijs") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = maxPrice,
                        onValueChange = { maxPrice = it },
                        label = { Text("Max prijs") },
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = minSeats,
                    onValueChange = { minSeats = it },
                    label = { Text("Min. aantal stoelen") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = maxDistance,
                    onValueChange = { maxDistance = it },
                    label = { Text("Max afstand (km)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val filter = CarSearchFilterRequest(
                        make = make.takeIf { it.isNotBlank() },
                        model = model.takeIf { it.isNotBlank() },
                        category = category.takeIf { it.isNotBlank() },
                        minPrice = minPrice.toDoubleOrNull(),
                        maxPrice = maxPrice.toDoubleOrNull(),
                        minSeats = minSeats.toIntOrNull(),
                        maxDistanceKm = maxDistance.toDoubleOrNull()
                    )
                    onSearch(filter)
                }
            ) {
                Text("Zoeken")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuleren")
            }
        }
    )
}

@Composable
fun CarMapDialog(
    cars: List<Car>,
    selectedCar: Car?,
    onDismiss: () -> Unit,
    onCarSelected: (Car) -> Unit
) {
    // Parse coordinaten van auto's
    val carLocations = remember(cars) {
        cars.mapNotNull { car ->
            car.pickupLocation?.let { location ->
                val coords = location.split(",")
                if (coords.size == 2) {
                    val lat = coords[0].trim().toDoubleOrNull()
                    val lon = coords[1].trim().toDoubleOrNull()
                    if (lat != null && lon != null) {
                        CarLocation(car, lat, lon)
                    } else null
                } else null
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Header
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        tonalElevation = 0.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Auto's op de kaart (${carLocations.size})",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = onDismiss) {
                                Icon(Icons.Default.Close, contentDescription = "Sluiten")
                            }
                        }
                    }

                    // Map - KRITIEK: geen extra Box eromheen
                    if (carLocations.isEmpty()) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            Icon(
                                Icons.Default.LocationOff,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "Geen locaties beschikbaar",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    } else {
                        // Map neemt alle beschikbare ruimte
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            LeafletMap(
                                carLocations = carLocations,
                                selectedCarId = selectedCar?.id,
                                onCarMarkerClick = { carId ->
                                    carLocations.find { it.car.id == carId }?.let {
                                        onCarSelected(it.car)
                                    }
                                }
                            )
                        }

                        // Selected car info overlay
                        AnimatedVisibility(
                            visible = selectedCar != null,
                            enter = slideInVertically(initialOffsetY = { it }),
                        ) {
                            selectedCar?.let { car ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.primaryContainer
                                    ),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                "${car.make} ${car.model}",
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                "€${car.bookingCost}/dag",
                                                style = MaterialTheme.typography.bodyMedium
                                            )
                                            car.pickupLocation?.let { location ->
                                                Text(
                                                    location,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                        Button(onClick = {
                                            onCarSelected(car)
                                            onDismiss()
                                        }) {
                                            Text("Selecteer")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

data class CarLocation(
    val car: Car,
    val latitude: Double,
    val longitude: Double
)

@Composable
fun LeafletMap(
    carLocations: List<CarLocation>,
    selectedCarId: Long?,
    onCarMarkerClick: (Long) -> Unit
) {
    val centerLat = carLocations.mapNotNull { it.latitude }.average().takeIf { !it.isNaN() } ?: 52.3702
    val centerLon = carLocations.mapNotNull { it.longitude }.average().takeIf { !it.isNaN() } ?: 4.8952

    val htmlContent = remember(carLocations, selectedCarId) {
        generateLeafletHTML(carLocations, centerLat, centerLon, selectedCarId)
    }

    AndroidView(
        factory = { context ->
            WebView(context).apply {
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true

                    // Verbeterde WebView instellingen
                    allowFileAccess = true
                    allowContentAccess = true

                    // Zoom instellingen
                    builtInZoomControls = false
                    displayZoomControls = false
                    setSupportZoom(true)

                    // Layout instellingen
                    loadWithOverviewMode = true
                    useWideViewPort = true

                    // Cache en mixed content
                    cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
                    mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW

                    // Hardware acceleration
                    setRenderPriority(android.webkit.WebSettings.RenderPriority.HIGH)
                }

                // Transparante achtergrond
                setBackgroundColor(android.graphics.Color.TRANSPARENT)

                // Belangrijk: Zet layer type voor hardware acceleration
                setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)

                webViewClient = object : WebViewClient() {
                    override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        Log.d("LeafletMap", "Page started loading")
                    }

                    override fun onPageFinished(view: WebView?, url: String?) {
                        super.onPageFinished(view, url)
                        Log.d("LeafletMap", "Page finished loading")

                        // Force een repaint na laden
                        view?.postDelayed({
                            view.invalidate()
                        }, 100)
                    }

                    override fun onReceivedError(
                        view: WebView?,
                        errorCode: Int,
                        description: String?,
                        failingUrl: String?
                    ) {
                        super.onReceivedError(view, errorCode, description, failingUrl)
                        Log.e("LeafletMap", "Error $errorCode: $description at $failingUrl")
                    }

                    override fun shouldOverrideUrlLoading(
                        view: WebView?,
                        request: android.webkit.WebResourceRequest?
                    ): Boolean {
                        return false // Laat WebView alle URLs laden
                    }
                }

                webChromeClient = object : android.webkit.WebChromeClient() {
                    override fun onConsoleMessage(msg: android.webkit.ConsoleMessage?): Boolean {
                        msg?.let {
                            val level = when (it.messageLevel()) {
                                android.webkit.ConsoleMessage.MessageLevel.ERROR -> "ERROR"
                                android.webkit.ConsoleMessage.MessageLevel.WARNING -> "WARN"
                                else -> "INFO"
                            }
                            Log.d("LeafletMap-JS", "[$level] ${it.message()} (${it.sourceId()}:${it.lineNumber()})")
                        }
                        return true
                    }

                    override fun onProgressChanged(view: WebView?, newProgress: Int) {
                        super.onProgressChanged(view, newProgress)
                        Log.d("LeafletMap", "Loading progress: $newProgress%")
                    }
                }

                addJavascriptInterface(object {
                    @android.webkit.JavascriptInterface
                    fun onMarkerClick(carId: Long) {
                        Log.d("LeafletMap", "Marker clicked: $carId")
                        android.os.Handler(android.os.Looper.getMainLooper()).post {
                            onCarMarkerClick(carId)
                        }
                    }
                }, "Android")
            }
        },
        update = { webView ->
            Log.d("LeafletMap", "Loading HTML with ${carLocations.size} locations")
            webView.loadDataWithBaseURL(
                "https://unpkg.com/",
                htmlContent,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = Modifier.fillMaxSize()
    )
}

fun generateLeafletHTML(
    carLocations: List<CarLocation>,
    centerLat: Double,
    centerLon: Double,
    selectedCarId: Long?
): String {
    Log.d("LeafletMap", "Generating HTML - Center: [$centerLat, $centerLon], Locations: ${carLocations.size}")

    val markers = carLocations.joinToString("\n") { location ->
        val isSelected = location.car.id == selectedCarId
        val iconColor = if (isSelected) "#ef4444" else "#3b82f6"
        val carInfo = "${location.car.make} ${location.car.model}".replace("'", "\\'")
        val price = "€${location.car.bookingCost}/dag"

        """
        (function() {
            try {
                var marker = L.marker([${location.latitude}, ${location.longitude}], {
                    icon: L.divIcon({
                        className: 'custom-marker',
                        html: '<div style="background-color: $iconColor; width: 32px; height: 32px; border-radius: 50%; border: 3px solid white; box-shadow: 0 3px 8px rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; cursor: pointer;"><span style="font-size: 18px;">🚗</span></div>',
                        iconSize: [32, 32],
                        iconAnchor: [16, 16]
                    })
                });
                
                marker.bindPopup('<div style="text-align: center;"><strong>$carInfo</strong><br/>$price</div>', {
                    closeButton: true,
                    autoClose: false
                });
                
                marker.addTo(map);
                
                marker.on('click', function() {
                    console.log('Marker clicked: ${location.car.id}');
                    try {
                        Android.onMarkerClick(${location.car.id});
                    } catch(e) {
                        console.error('Error calling Android:', e);
                    }
                });
                
                console.log('Added marker ${location.car.id} at [${location.latitude}, ${location.longitude}]');
            } catch (e) {
                console.error('Error adding marker ${location.car.id}:', e);
            }
        })();
        """.trimIndent()
    }

    val boundsCode = if (carLocations.isNotEmpty()) {
        val bounds = carLocations.joinToString(", ") { "[${it.latitude}, ${it.longitude}]" }
        """
        setTimeout(function() {
            try {
                var bounds = L.latLngBounds([$bounds]);
                map.fitBounds(bounds, {
                    padding: [50, 50],
                    maxZoom: 15
                });
                console.log('Bounds fitted');
            } catch (e) {
                console.error('Error fitting bounds:', e);
            }
        }, 500);
        """
    } else ""

    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="utf-8" />
        <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no, viewport-fit=cover">
        
        <link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css" 
              integrity="sha256-p4NxAoJBhIIN+hmNHrzRCf9tD/miZyoHS5obTRR9BMY="
              crossorigin="anonymous" />
        
        <style>
            * { 
                margin: 0; 
                padding: 0; 
                box-sizing: border-box;
                -webkit-tap-highlight-color: transparent;
            }
            html, body { 
                height: 100vh;
                width: 100vw;
                overflow: hidden;
                position: fixed;
                background: #e5e7eb;
            }
            #map { 
                position: absolute;
                top: 0;
                left: 0;
                bottom: 0;
                right: 0;
                width: 100%;
                height: 100%;
                z-index: 1;
            }
            .custom-marker { 
                background: none !important; 
                border: none !important; 
            }
            #loading {
                position: fixed;
                top: 50%;
                left: 50%;
                transform: translate(-50%, -50%);
                background: white;
                padding: 24px 32px;
                border-radius: 12px;
                box-shadow: 0 4px 12px rgba(0,0,0,0.15);
                z-index: 9999;
                font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                font-size: 16px;
                color: #374151;
            }
            .leaflet-container {
                background: #e5e7eb !important;
            }
        </style>
    </head>
    <body>
        <div id="loading">Kaart laden...</div>
        <div id="map"></div>
        
        <script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"
                integrity="sha256-20nQCchB9co0qIjJZRGuk2/Z9VM+kNiyxNV1lvTlZBo="
                crossorigin="anonymous"></script>
        
        <script>
            console.log('=== Script Starting ===');
            
            var map;
            var initAttempts = 0;
            var maxAttempts = 20;
            
            function tryInitMap() {
                initAttempts++;
                console.log('Init attempt ' + initAttempts + '/' + maxAttempts);
                
                if (typeof L === 'undefined') {
                    console.log('Leaflet not ready yet...');
                    if (initAttempts < maxAttempts) {
                        setTimeout(tryInitMap, 100);
                    } else {
                        document.getElementById('loading').innerHTML = '❌ Fout: Leaflet kon niet laden';
                        console.error('Leaflet failed to load after ' + maxAttempts + ' attempts');
                    }
                    return;
                }
                
                try {
                    console.log('✓ Leaflet loaded, version: ' + L.version);
                    console.log('Creating map at [$centerLat, $centerLon]...');
                    
                    // Maak map
                    map = L.map('map', {
                        center: [$centerLat, $centerLon],
                        zoom: 12,
                        zoomControl: true,
                        attributionControl: true,
                        scrollWheelZoom: true,
                        doubleClickZoom: true,
                        touchZoom: true,
                        dragging: true
                    });
                    
                    console.log('✓ Map object created');
                    
                    // Voeg tiles toe
                    var tileLayer = L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
                        attribution: '© OpenStreetMap',
                        maxZoom: 19,
                        minZoom: 3
                    });
                    
                    tileLayer.on('load', function() {
                        console.log('✓ Tiles loaded');
                    });
                    
                    tileLayer.on('tileerror', function(error) {
                        console.error('Tile error:', error);
                    });
                    
                    tileLayer.addTo(map);
                    console.log('✓ Tiles added to map');
                    
                    // Verwijder loading na korte delay
                    setTimeout(function() {
                        var loading = document.getElementById('loading');
                        if (loading) {
                            loading.style.display = 'none';
                            console.log('✓ Loading removed');
                        }
                    }, 300);
                    
                    // Voeg markers toe
                    console.log('Adding ${carLocations.size} markers...');
                    $markers
                    
                    // Fit bounds
                    $boundsCode
                    
                    // Force invalidate
                    setTimeout(function() {
                        map.invalidateSize();
                        console.log('✓ Map size invalidated');
                    }, 100);
                    
                    console.log('=== Map Initialization Complete! ===');
                    
                } catch (error) {
                    console.error('Map initialization error:', error);
                    document.getElementById('loading').innerHTML = '❌ Fout: ' + error.message;
                }
            }
            
            // Start initialization
            if (document.readyState === 'loading') {
                document.addEventListener('DOMContentLoaded', function() {
                    console.log('DOM loaded, starting init...');
                    setTimeout(tryInitMap, 50);
                });
            } else {
                console.log('DOM already loaded, starting init...');
                setTimeout(tryInitMap, 50);
            }
            
            // Zorg dat map responsive blijft
            window.addEventListener('resize', function() {
                if (map) {
                    map.invalidateSize();
                }
            });
        </script>
    </body>
    </html>
    """.trimIndent()
}

@Composable
fun DatePickerCard(
    selectedDate: LocalDate,
    onDateChange: (LocalDate) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${selectedDate.dayOfMonth}/${selectedDate.monthValue}/${selectedDate.year}",
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}

@Composable
fun CarSelectionCard(
    car: Car,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = car.imageFileNames.firstOrNull()?.let { "$it" }
                    ?: R.drawable.car,
                contentDescription = "Car Image",
                modifier = Modifier.size(88.dp, 64.dp),
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

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
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    "€${car.bookingCost}/dag + €${car.costPerKilometer}/km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Geselecteerd",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}



@Composable
fun TimeSelectionSection(
    startTime: LocalTime?,
    endTime: LocalTime?,
    unavailableSlots: List<Pair<LocalTime, LocalTime>>,
    onStartTimeClick: () -> Unit,
    onEndTimeClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedCard(
            modifier = Modifier.weight(1f),
            onClick = onStartTimeClick
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Start", style = MaterialTheme.typography.bodySmall)
                Text(
                    startTime?.let { "${it.hour}:${it.minute.toString().padStart(2, '0')}" } ?: "--:--",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }

        OutlinedCard(
            modifier = Modifier.weight(1f),
            onClick = onEndTimeClick
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Eind", style = MaterialTheme.typography.bodySmall)
                Text(
                    endTime?.let { "${it.hour}:${it.minute.toString().padStart(2, '0')}" } ?: "--:--",
                    style = MaterialTheme.typography.titleLarge
                )
            }
        }
    }

    if (unavailableSlots.isNotEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    "Niet beschikbare tijden:",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold
                )
                unavailableSlots.forEach { (start, end) ->
                    Text(
                        "${start.hour}:${start.minute.toString().padStart(2, '0')} - " +
                                "${end.hour}:${end.minute.toString().padStart(2, '0')}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val timePickerState = rememberTimePickerState()

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(timePickerState.hour, timePickerState.minute)
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuleer")
            }
        },
        text = {
            TimePicker(state = timePickerState)
        }
    )
}