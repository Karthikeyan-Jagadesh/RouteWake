package com.routewake.app.ui.history

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.routewake.app.data.repository.TripRepository
import com.routewake.app.domain.model.TripAlert
import com.routewake.app.theme.*
import com.routewake.app.ui.components.RouteWakeCard
import com.routewake.app.ui.components.RouteWakeStatusBadge
import com.routewake.app.ui.components.RouteWakeTopBar
import kotlinx.coroutines.launch

@Composable
fun TripHistoryScreen(
    tripRepository: TripRepository,
    onBackClick: () -> Unit,
    onReplanTrip: (TripAlert) -> Unit
) {
    val history by tripRepository.tripHistoryFlow.collectAsState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        tripRepository.refreshHistory()
    }

    TripHistoryContent(
        trips = history,
        onBackClick = onBackClick,
        onDeleteTrip = { tripId ->
            scope.launch {
                tripRepository.deleteTrip(tripId)
            }
        },
        onReplanTrip = onReplanTrip
    )
}

@Composable
fun TripHistoryContent(
    trips: List<TripAlert>,
    onBackClick: () -> Unit,
    onDeleteTrip: (Long) -> Unit,
    onReplanTrip: (TripAlert) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") } // "ALL", "DISTANCE", "TIME"

    val filteredList = remember(trips, selectedFilter) {
        when (selectedFilter) {
            "DISTANCE" -> trips.filter { it.isDistanceMode }
            "TIME" -> trips.filter { it.isTimeMode }
            else -> trips
        }
    }

    Scaffold(
        topBar = {
            RouteWakeTopBar(
                title = "Journey History",
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
            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedFilter == "ALL",
                    onClick = { selectedFilter = "ALL" },
                    label = { Text("All (${trips.size})") }
                )
                FilterChip(
                    selected = selectedFilter == "DISTANCE",
                    onClick = { selectedFilter = "DISTANCE" },
                    label = { Text("Distance Radius") }
                )
                FilterChip(
                    selected = selectedFilter == "TIME",
                    onClick = { selectedFilter = "TIME" },
                    label = { Text("ETA Lead") }
                )
            }

            if (filteredList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = RouteWakeTextMutedDark,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No recorded journeys",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Your completed trips and alarm history will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = RouteWakeTextSecondaryDark
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredList, key = { it.id }) { trip ->
                        HistoryCard(
                            trip = trip,
                            onReplan = { onReplanTrip(trip) },
                            onDelete = { onDeleteTrip(trip.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryCard(
    trip: TripAlert,
    onReplan: () -> Unit,
    onDelete: () -> Unit
) {
    RouteWakeCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.destinationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${trip.formattedThreshold} • ${trip.createdAt?.replace("T", " ") ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = RouteWakeTextSecondaryDark
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Coords: ${String.format("%.4f", trip.destinationLatitude)}, ${String.format("%.4f", trip.destinationLongitude)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = RouteWakeTextMutedDark
                )
            }

            RouteWakeStatusBadge(
                status = trip.status,
                isActive = trip.isActive
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "Delete",
                    tint = RouteWakeErrorRed,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Delete",
                    color = RouteWakeErrorRed,
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedButton(
                onClick = onReplan,
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Plan Again",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "History - Populated (Dark)",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TripHistoryPopulatedDarkPreview() {
    RouteWakeTheme(darkTheme = true) {
        TripHistoryContent(
            trips = com.routewake.app.ui.components.PreviewSampleData.sampleRecentTrips,
            onBackClick = {},
            onDeleteTrip = {},
            onReplanTrip = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "History - Empty State (Light)",
    showBackground = true
)
@Composable
private fun TripHistoryEmptyLightPreview() {
    RouteWakeTheme(darkTheme = false) {
        TripHistoryContent(
            trips = emptyList(),
            onBackClick = {},
            onDeleteTrip = {},
            onReplanTrip = {}
        )
    }
}

