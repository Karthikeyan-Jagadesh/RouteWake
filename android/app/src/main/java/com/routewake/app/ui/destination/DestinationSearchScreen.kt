package com.routewake.app.ui.destination

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.routewake.app.data.remote.dto.NominatimPlaceDto
import com.routewake.app.data.repository.GeocodingRepository
import com.routewake.app.domain.model.LocationPoint
import com.routewake.app.theme.*
import com.routewake.app.ui.components.NativeMapView
import com.routewake.app.ui.components.RouteWakeButton
import com.routewake.app.ui.components.RouteWakeCard
import com.routewake.app.ui.components.RouteWakeTopBar
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DestinationSearchScreen(
    currentLocation: LocationPoint?,
    geocodingRepository: GeocodingRepository,
    onBackClick: () -> Unit,
    onDestinationSelected: (name: String, lat: Double, lng: Double) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<NominatimPlaceDto>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }

    var selectedName by remember { mutableStateOf<String?>(null) }
    var selectedLat by remember { mutableStateOf<Double?>(null) }
    var selectedLng by remember { mutableStateOf<Double?>(null) }

    val scope = rememberCoroutineScope()
    var searchJob by remember { mutableStateOf<Job?>(null) }

    DestinationSearchContent(
        searchQuery = searchQuery,
        onSearchQueryChange = { query ->
            searchQuery = query
            searchJob?.cancel()
            if (query.trim().length < 2) {
                searchResults = emptyList()
                isSearching = false
            } else {
                isSearching = true
                searchJob = scope.launch {
                    delay(400)
                    val result = geocodingRepository.searchPlaces(query)
                    isSearching = false
                    searchResults = result.getOrDefault(emptyList())
                }
            }
        },
        searchResults = searchResults,
        isSearching = isSearching,
        currentLocation = currentLocation,
        selectedName = selectedName,
        selectedLat = selectedLat,
        selectedLng = selectedLng,
        onSelectPlace = { name, lat, lng ->
            selectedName = name
            selectedLat = lat
            selectedLng = lng
            searchResults = emptyList()
            searchQuery = name.split(",").firstOrNull() ?: name
        },
        onClearSearch = {
            searchQuery = ""
            searchResults = emptyList()
        },
        onBackClick = onBackClick,
        onDestinationSelected = onDestinationSelected
    )
}

@Composable
fun DestinationSearchContent(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    searchResults: List<NominatimPlaceDto>,
    isSearching: Boolean,
    currentLocation: LocationPoint?,
    selectedName: String?,
    selectedLat: Double?,
    selectedLng: Double?,
    onSelectPlace: (String, Double, Double) -> Unit,
    onClearSearch: () -> Unit,
    onBackClick: () -> Unit,
    onDestinationSelected: (String, Double, Double) -> Unit
) {
    Scaffold(
        topBar = {
            RouteWakeTopBar(
                title = "Select Destination",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Input Field
            Box(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search landmark, station or address...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = RouteWakeAccentLime
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = onClearSearch) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
            }

            // Results List or Map View
            if (searchResults.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(searchResults) { place ->
                        RouteWakeCard(
                            backgroundColor = MaterialTheme.colorScheme.surface,
                            borderColor = MaterialTheme.colorScheme.outline,
                            onClick = {
                                place.latitude?.let { lat ->
                                    place.longitude?.let { lng ->
                                        onSelectPlace(place.displayName, lat, lng)
                                    }
                                }
                            }
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Place,
                                    contentDescription = null,
                                    tint = RouteWakeAccentLime,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = place.primaryTitle,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = place.displayName,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = RouteWakeTextSecondaryDark,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Interactive Map View with Pinning
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    NativeMapView(
                        currentLat = currentLocation?.latitude ?: 13.0827,
                        currentLng = currentLocation?.longitude ?: 80.2707,
                        destLat = selectedLat,
                        destLng = selectedLng,
                        destName = selectedName,
                        enablePinning = true,
                        onMapPinSelected = { lat, lng ->
                            onSelectPlace("Pinned Destination (${String.format("%.4f", lat)}, ${String.format("%.4f", lng)})", lat, lng)
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Overlay Tip
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(12.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        Text(
                            text = "Tap on map to pin destination",
                            style = MaterialTheme.typography.labelSmall,
                            color = RouteWakeAccentLime,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            // Bottom Selected Destination Confirmation Card
            if (selectedLat != null && selectedLng != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = RouteWakeAccentLime,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Selected Destination",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = selectedName ?: "Selected Destination",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RouteWakeTextSecondaryDark,
                            maxLines = 2
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        RouteWakeButton(
                            text = "Configure Alert Threshold",
                            onClick = {
                                onDestinationSelected(
                                    selectedName ?: "Destination Node",
                                    selectedLat!!,
                                    selectedLng!!
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Search - Suggestions (Dark)",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DestinationSearchSuggestionsDarkPreview() {
    RouteWakeTheme(darkTheme = true) {
        DestinationSearchContent(
            searchQuery = "Grand Central",
            onSearchQueryChange = {},
            searchResults = com.routewake.app.ui.components.PreviewSampleData.sampleSearchResults,
            isSearching = false,
            currentLocation = com.routewake.app.domain.model.LocationPoint(40.7484, -73.9857),
            selectedName = null,
            selectedLat = null,
            selectedLng = null,
            onSelectPlace = { _, _, _ -> },
            onClearSearch = {},
            onBackClick = {},
            onDestinationSelected = { _, _, _ -> }
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Search - Idle Map Selection (Light)",
    showBackground = true
)
@Composable
private fun DestinationSearchIdleLightPreview() {
    RouteWakeTheme(darkTheme = false) {
        DestinationSearchContent(
            searchQuery = "",
            onSearchQueryChange = {},
            searchResults = emptyList(),
            isSearching = false,
            currentLocation = com.routewake.app.domain.model.LocationPoint(40.7484, -73.9857),
            selectedName = "Grand Central Terminal",
            selectedLat = 40.7527,
            selectedLng = -73.9772,
            onSelectPlace = { _, _, _ -> },
            onClearSearch = {},
            onBackClick = {},
            onDestinationSelected = { _, _, _ -> }
        )
    }
}


