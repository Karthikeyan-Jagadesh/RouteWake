package com.routewake.app.ui.savedplaces

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.routewake.app.data.repository.SavedPlacesRepository
import com.routewake.app.domain.model.SavedPlace
import com.routewake.app.theme.*
import com.routewake.app.ui.components.RouteWakeCard
import com.routewake.app.ui.components.RouteWakeTopBar

@Composable
fun SavedPlacesScreen(
    savedPlacesRepository: SavedPlacesRepository,
    onBackClick: () -> Unit,
    onPlanTripWithPlace: (SavedPlace) -> Unit
) {
    val places by savedPlacesRepository.savedPlaces.collectAsState()

    SavedPlacesContent(
        places = places,
        onBackClick = onBackClick,
        onAddPlace = { label, address, lat, lng ->
            savedPlacesRepository.addPlace(label, address, lat, lng)
        },
        onDeletePlace = { id ->
            savedPlacesRepository.deletePlace(id)
        },
        onPlanTripWithPlace = onPlanTripWithPlace
    )
}

@Composable
fun SavedPlacesContent(
    places: List<SavedPlace>,
    onBackClick: () -> Unit,
    onAddPlace: (label: String, address: String, lat: Double, lng: Double) -> Unit,
    onDeletePlace: (Long) -> Unit,
    onPlanTripWithPlace: (SavedPlace) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    var newLabel by remember { mutableStateOf("") }
    var newAddress by remember { mutableStateOf("") }
    var newLat by remember { mutableStateOf("") }
    var newLng by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            RouteWakeTopBar(
                title = "Saved Destinations",
                onBackClick = onBackClick,
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Place",
                            tint = RouteWakeAccentLime
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (places.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.BookmarkBorder,
                            contentDescription = null,
                            tint = RouteWakeTextMutedDark,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No saved places yet",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Save frequent destinations like Home, Campus, or Work for 1-tap trip planning.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RouteWakeTextSecondaryDark
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(places, key = { it.id }) { place ->
                        SavedPlaceCard(
                            place = place,
                            onPlanTrip = { onPlanTripWithPlace(place) },
                            onDelete = { onDeletePlace(place.id) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Save New Destination") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newLabel,
                        onValueChange = { newLabel = it },
                        label = { Text("Label (e.g. Home, Work)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newAddress,
                        onValueChange = { newAddress = it },
                        label = { Text("Destination Name / Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newLat,
                            onValueChange = { newLat = it },
                            label = { Text("Latitude") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = newLng,
                            onValueChange = { newLng = it },
                            label = { Text("Longitude") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val lat = newLat.toDoubleOrNull() ?: 13.0827
                        val lng = newLng.toDoubleOrNull() ?: 80.2707
                        onAddPlace(
                            newLabel.ifBlank { "Custom Place" },
                            newAddress.ifBlank { "Destination Node" },
                            lat,
                            lng
                        )
                        showAddDialog = false
                        newLabel = ""
                        newAddress = ""
                        newLat = ""
                        newLng = ""
                    }
                ) {
                    Text("Save", color = RouteWakeAccentLime, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SavedPlaceCard(
    place: SavedPlace,
    onPlanTrip: () -> Unit,
    onDelete: () -> Unit
) {
    RouteWakeCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(RouteWakeAccentLime.copy(alpha = 0.15f), shape = RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (place.label.lowercase()) {
                            "home" -> Icons.Default.Home
                            "office", "work" -> Icons.Default.Work
                            else -> Icons.Default.Place
                        },
                        contentDescription = null,
                        tint = RouteWakeAccentLime,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = place.label,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = place.address,
                        style = MaterialTheme.typography.bodySmall,
                        color = RouteWakeTextSecondaryDark,
                        maxLines = 1
                    )
                    Text(
                        text = "${String.format("%.4f", place.latitude)}, ${String.format("%.4f", place.longitude)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = RouteWakeTextMutedDark
                    )
                }
            }

            Row {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = RouteWakeErrorRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Button(
                    onClick = onPlanTrip,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = RouteWakeAccentLime),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Plan",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF022C22)
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Saved Places - Populated (Dark)",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SavedPlacesPopulatedDarkPreview() {
    RouteWakeTheme(darkTheme = true) {
        SavedPlacesContent(
            places = com.routewake.app.ui.components.PreviewSampleData.sampleSavedPlaces,
            onBackClick = {},
            onAddPlace = { _, _, _, _ -> },
            onDeletePlace = {},
            onPlanTripWithPlace = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Saved Places - Empty (Light)",
    showBackground = true
)
@Composable
private fun SavedPlacesEmptyLightPreview() {
    RouteWakeTheme(darkTheme = false) {
        SavedPlacesContent(
            places = emptyList(),
            onBackClick = {},
            onAddPlace = { _, _, _, _ -> },
            onDeletePlace = {},
            onPlanTripWithPlace = {}
        )
    }
}


