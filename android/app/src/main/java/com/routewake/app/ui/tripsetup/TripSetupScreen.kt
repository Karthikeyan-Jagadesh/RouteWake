package com.routewake.app.ui.tripsetup

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.routewake.app.data.repository.SavedPlacesRepository
import com.routewake.app.data.repository.TripRepository
import com.routewake.app.domain.model.TripAlert
import com.routewake.app.theme.*
import com.routewake.app.ui.components.RouteWakeButton
import com.routewake.app.ui.components.RouteWakeCard
import com.routewake.app.ui.components.RouteWakeTopBar
import kotlinx.coroutines.launch

@Composable
fun TripSetupScreen(
    destName: String,
    destLat: Double,
    destLng: Double,
    tripRepository: TripRepository,
    savedPlacesRepository: SavedPlacesRepository,
    onBackClick: () -> Unit,
    onMonitoringStarted: (TripAlert) -> Unit
) {
    var selectedMode by remember { mutableStateOf("DISTANCE") } // "DISTANCE" or "TIME"
    var selectedDistanceKm by remember { mutableIntStateOf(1) }
    var selectedEtaMinutes by remember { mutableIntStateOf(10) }
    var isSavingPlace by remember { mutableStateOf(false) }
    var placeLabel by remember { mutableStateOf("") }
    var isStartingTrip by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    TripSetupContent(
        destName = destName,
        destLat = destLat,
        destLng = destLng,
        selectedMode = selectedMode,
        onModeChange = { selectedMode = it },
        selectedDistanceKm = selectedDistanceKm,
        onDistanceChange = { selectedDistanceKm = it },
        selectedEtaMinutes = selectedEtaMinutes,
        onEtaChange = { selectedEtaMinutes = it },
        isSavingPlace = isSavingPlace,
        onSavingPlaceChange = { isSavingPlace = it },
        placeLabel = placeLabel,
        onPlaceLabelChange = { placeLabel = it },
        isStartingTrip = isStartingTrip,
        errorMessage = errorMessage,
        onBackClick = onBackClick,
        onStartTripClick = {
            isStartingTrip = true
            errorMessage = null
            scope.launch {
                if (isSavingPlace) {
                    savedPlacesRepository.addPlace(
                        label = placeLabel.ifBlank { destName.split(",").firstOrNull() ?: "Saved Place" },
                        address = destName,
                        latitude = destLat,
                        longitude = destLng
                    )
                }

                val result = tripRepository.createTrip(
                    destinationName = destName,
                    destinationLatitude = destLat,
                    destinationLongitude = destLng,
                    alarmType = selectedMode,
                    distanceThresholdKm = if (selectedMode == "DISTANCE") selectedDistanceKm else null,
                    timeThresholdMinutes = if (selectedMode == "TIME") selectedEtaMinutes else null
                )
                isStartingTrip = false
                result.onSuccess { trip ->
                    onMonitoringStarted(trip)
                }.onFailure { ex ->
                    errorMessage = ex.message ?: "Could not create trip"
                }
            }
        }
    )
}

@Composable
fun TripSetupContent(
    destName: String,
    destLat: Double,
    destLng: Double,
    selectedMode: String,
    onModeChange: (String) -> Unit,
    selectedDistanceKm: Int,
    onDistanceChange: (Int) -> Unit,
    selectedEtaMinutes: Int,
    onEtaChange: (Int) -> Unit,
    isSavingPlace: Boolean,
    onSavingPlaceChange: (Boolean) -> Unit,
    placeLabel: String,
    onPlaceLabelChange: (String) -> Unit,
    isStartingTrip: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onStartTripClick: () -> Unit
) {
    Scaffold(
        topBar = {
            RouteWakeTopBar(
                title = "Trip Configuration",
                onBackClick = onBackClick
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Destination Summary Header
            RouteWakeCard(
                backgroundColor = MaterialTheme.colorScheme.surface,
                borderColor = MaterialTheme.colorScheme.outline
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = RouteWakeAccentLime,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Target Destination",
                            style = MaterialTheme.typography.labelSmall,
                            color = RouteWakeTextSecondaryDark
                        )
                        Text(
                            text = destName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${String.format("%.4f", destLat)}, ${String.format("%.4f", destLng)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = RouteWakeTextMutedDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Alert Mode Switcher Tabs
            Text(
                text = "Select Alert Mode",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .padding(4.dp)
            ) {
                ModeTabItem(
                    title = "Distance Pulse",
                    subtitle = "Radius based",
                    isSelected = selectedMode == "DISTANCE",
                    modifier = Modifier.weight(1f),
                    onClick = { onModeChange("DISTANCE") }
                )
                ModeTabItem(
                    title = "ETA Pulse",
                    subtitle = "Time based",
                    isSelected = selectedMode == "TIME",
                    modifier = Modifier.weight(1f),
                    onClick = { onModeChange("TIME") }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Mode 1: Distance Radius Configuration
            if (selectedMode == "DISTANCE") {
                Text(
                    text = "Trigger Alert Within",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                val distanceOptions = listOf(1 to "1 km", 2 to "2 km", 3 to "3 km", 5 to "5 km")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    distanceOptions.forEach { (km, label) ->
                        OptionChip(
                            label = label,
                            isSelected = selectedDistanceKm == km,
                            modifier = Modifier.weight(1f),
                            onClick = { onDistanceChange(km) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Alarm triggers when your live GPS enters within $selectedDistanceKm km of your destination.",
                    style = MaterialTheme.typography.bodySmall,
                    color = RouteWakeTextSecondaryDark
                )
            } else {
                // Mode 2: ETA Lead Time Configuration
                Text(
                    text = "Lead Time Before Arrival",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(10.dp))

                val etaOptions = listOf(5 to "5 min", 10 to "10 min", 15 to "15 min", 20 to "20 min")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    etaOptions.forEach { (mins, label) ->
                        OptionChip(
                            label = label,
                            isSelected = selectedEtaMinutes == mins,
                            modifier = Modifier.weight(1f),
                            onClick = { onEtaChange(mins) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Alarm triggers approximately $selectedEtaMinutes minutes before arrival based on live moving speed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = RouteWakeTextSecondaryDark
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Destination Shortcut Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable { onSavingPlaceChange(!isSavingPlace) }
            ) {
                Checkbox(
                    checked = isSavingPlace,
                    onCheckedChange = onSavingPlaceChange,
                    colors = CheckboxDefaults.colors(checkedColor = RouteWakeAccentLime)
                )
                Text(
                    text = "Save this destination to my frequent places",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (isSavingPlace) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = placeLabel,
                    onValueChange = onPlaceLabelChange,
                    label = { Text("Label (e.g. Home, Office, Campus)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = RouteWakeErrorRed,
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            if (isStartingTrip) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = RouteWakeAccentLime)
                }
            } else {
                RouteWakeButton(
                    text = "Start Live Monitoring",
                    leadingIcon = Icons.Default.Navigation,
                    onClick = onStartTripClick
                )
            }
        }
    }
}

@Composable
private fun ModeTabItem(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) RouteWakeAccentLime else Color.Transparent
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF022C22) else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = if (isSelected) Color(0xFF064E3B) else RouteWakeTextSecondaryDark
            )
        }
    }
}

@Composable
private fun OptionChip(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        color = if (isSelected) RouteWakeAccentLime.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, RouteWakeAccentLime) else null
    ) {
        Box(
            modifier = Modifier.padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) RouteWakeAccentLime else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Trip Setup - Distance Mode (Dark)",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TripSetupDistanceDarkPreview() {
    RouteWakeTheme(darkTheme = true) {
        TripSetupContent(
            destName = "Grand Central Terminal, New York",
            destLat = 40.7527,
            destLng = -73.9772,
            selectedMode = "DISTANCE",
            onModeChange = {},
            selectedDistanceKm = 1,
            onDistanceChange = {},
            selectedEtaMinutes = 10,
            onEtaChange = {},
            isSavingPlace = false,
            onSavingPlaceChange = {},
            placeLabel = "",
            onPlaceLabelChange = {},
            isStartingTrip = false,
            errorMessage = null,
            onBackClick = {},
            onStartTripClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Trip Setup - ETA Mode (Light)",
    showBackground = true
)
@Composable
private fun TripSetupEtaLightPreview() {
    RouteWakeTheme(darkTheme = false) {
        TripSetupContent(
            destName = "John F. Kennedy International Airport",
            destLat = 40.6413,
            destLng = -73.7781,
            selectedMode = "TIME",
            onModeChange = {},
            selectedDistanceKm = 2,
            onDistanceChange = {},
            selectedEtaMinutes = 15,
            onEtaChange = {},
            isSavingPlace = true,
            onSavingPlaceChange = {},
            placeLabel = "Airport",
            onPlaceLabelChange = {},
            isStartingTrip = false,
            errorMessage = null,
            onBackClick = {},
            onStartTripClick = {}
        )
    }
}

