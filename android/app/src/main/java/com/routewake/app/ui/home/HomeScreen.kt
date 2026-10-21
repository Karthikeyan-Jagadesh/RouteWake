package com.routewake.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import com.routewake.app.domain.model.MonitoringState
import com.routewake.app.domain.model.SavedPlace
import com.routewake.app.domain.model.TripAlert
import com.routewake.app.domain.model.TripMonitoringSession
import com.routewake.app.domain.model.User
import com.routewake.app.theme.*
import com.routewake.app.ui.components.NativeMapView
import com.routewake.app.ui.components.RouteWakeButton
import com.routewake.app.ui.components.RouteWakeCard
import com.routewake.app.ui.components.RouteWakeStatusBadge

@Composable
fun HomeScreen(
    user: User?,
    session: TripMonitoringSession,
    recentTrips: List<TripAlert>,
    savedPlaces: List<SavedPlace>,
    onPlanTripClick: () -> Unit,
    onActiveTripClick: () -> Unit,
    onSearchClick: () -> Unit,
    onSavedPlaceClick: (SavedPlace) -> Unit,
    onHistoryClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        // 1. Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Welcome, ${user?.fullName?.split(" ")?.firstOrNull() ?: "Passenger"}",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (session.isMonitoringActive) "Trip in progress" else "Ready for your next journey",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (session.isMonitoringActive) RouteWakeAccentLime else RouteWakeTextSecondaryDark
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                RouteWakeStatusBadge(
                    status = if (session.isMonitoringActive) "MONITORING" else "STANDBY",
                    isActive = session.isMonitoringActive
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // 2. Active Trip Banner (if monitoring is currently running)
        if (session.activeTrip != null) {
            Box(modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp)) {
                ActiveTripCard(
                    session = session,
                    onClick = onActiveTripClick
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        // 3. Prominent Search Destination Shortcut
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSearchClick() },
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = null
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = RouteWakeAccentLime,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Where is your stop or landmark?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = RouteWakeTextSecondaryDark
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 4. Interactive Live Map Preview
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            NativeMapView(
                currentLat = session.currentLocation?.latitude ?: 13.0827,
                currentLng = session.currentLocation?.longitude ?: 80.2707,
                destLat = session.activeTrip?.destinationLatitude,
                destLng = session.activeTrip?.destinationLongitude,
                destName = session.activeTrip?.destinationName,
                routePoints = session.routeCoordinates,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 5. Primary Action Button
        Box(modifier = Modifier.padding(horizontal = 20.dp)) {
            RouteWakeButton(
                text = "Plan a New Trip",
                onClick = onPlanTripClick,
                leadingIcon = Icons.Default.AddLocationAlt
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // 6. Quick Saved Destinations Section
        if (savedPlaces.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Saved Destinations",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(savedPlaces) { place ->
                    SavedPlaceChip(place = place, onClick = { onSavedPlaceClick(place) })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }

        // 7. Recent Trips Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Journeys",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "View All",
                style = MaterialTheme.typography.labelLarge,
                color = RouteWakeAccentLime,
                modifier = Modifier.clickable { onHistoryClick() }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (recentTrips.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No recent journeys recorded yet. Plan a trip to start.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = RouteWakeTextSecondaryDark
                )
            }
        } else {
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {
                recentTrips.take(3).forEach { trip ->
                    RecentTripItem(trip = trip)
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun ActiveTripCard(
    session: TripMonitoringSession,
    onClick: () -> Unit
) {
    val trip = session.activeTrip ?: return
    RouteWakeCard(
        backgroundColor = RouteWakeAccentLime.copy(alpha = 0.12f),
        borderColor = RouteWakeAccentLime.copy(alpha = 0.4f),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(RouteWakeAccentLime)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "LIVE MONITORING",
                        style = MaterialTheme.typography.labelSmall,
                        color = RouteWakeAccentLime,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = trip.destinationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Remaining: ${session.formattedRemainingDistance} • ETA: ${session.formattedEta}",
                    style = MaterialTheme.typography.bodySmall,
                    color = RouteWakeTextSecondaryDark
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open Active Trip",
                tint = RouteWakeAccentLime
            )
        }
    }
}

@Composable
private fun SavedPlaceChip(
    place: SavedPlace,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (place.label.lowercase()) {
                    "home" -> Icons.Default.Home
                    "office", "work" -> Icons.Default.Work
                    else -> Icons.Default.Place
                },
                contentDescription = null,
                tint = RouteWakeAccentLime,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = place.label,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun RecentTripItem(trip: TripAlert) {
    RouteWakeCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.destinationName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${trip.formattedThreshold} • ${trip.status}",
                    style = MaterialTheme.typography.bodySmall,
                    color = RouteWakeTextSecondaryDark
                )
            }
            Text(
                text = trip.createdAt?.split("T")?.firstOrNull() ?: "",
                style = MaterialTheme.typography.labelSmall,
                color = RouteWakeTextMutedDark
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Home - Active Monitoring (Dark)",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun HomeScreenActiveDarkPreview() {
    RouteWakeTheme(darkTheme = true) {
        HomeScreen(
            user = com.routewake.app.ui.components.PreviewSampleData.sampleUser,
            session = com.routewake.app.ui.components.PreviewSampleData.sampleActiveSession,
            recentTrips = com.routewake.app.ui.components.PreviewSampleData.sampleRecentTrips,
            savedPlaces = com.routewake.app.ui.components.PreviewSampleData.sampleSavedPlaces,
            onPlanTripClick = {},
            onActiveTripClick = {},
            onSearchClick = {},
            onSavedPlaceClick = {},
            onHistoryClick = {},
            onSettingsClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Home - Idle State (Light)",
    showBackground = true
)
@Composable
private fun HomeScreenIdleLightPreview() {
    RouteWakeTheme(darkTheme = false) {
        HomeScreen(
            user = com.routewake.app.ui.components.PreviewSampleData.sampleUser,
            session = com.routewake.app.ui.components.PreviewSampleData.sampleIdleSession,
            recentTrips = emptyList(),
            savedPlaces = com.routewake.app.ui.components.PreviewSampleData.sampleSavedPlaces,
            onPlanTripClick = {},
            onActiveTripClick = {},
            onSearchClick = {},
            onSavedPlaceClick = {},
            onHistoryClick = {},
            onSettingsClick = {}
        )
    }
}

