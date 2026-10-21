package com.routewake.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.routewake.app.domain.model.TripMonitoringSession
import com.routewake.app.theme.*
import com.routewake.app.ui.components.NativeMapView
import com.routewake.app.ui.components.RouteWakeButton
import com.routewake.app.ui.components.RouteWakeCard
import com.routewake.app.ui.components.RouteWakeStatusBadge

@Composable
fun ActiveNavigationScreen(
    session: TripMonitoringSession,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onEndTripClick: () -> Unit,
    onBackClick: () -> Unit
) {
    val trip = session.activeTrip
    var isExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Dashboard",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Column {
                            Text(
                                text = trip?.destinationName ?: "Active Route",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1
                            )
                            Text(
                                text = trip?.formattedThreshold ?: "Threshold Active",
                                style = MaterialTheme.typography.labelSmall,
                                color = RouteWakeAccentLime
                            )
                        }
                    }

                    RouteWakeStatusBadge(
                        status = session.state.name,
                        isActive = session.isMonitoringActive
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 1. Large Interactive Full Map
            NativeMapView(
                currentLat = session.currentLocation?.latitude ?: 13.0827,
                currentLng = session.currentLocation?.longitude ?: 80.2707,
                destLat = trip?.destinationLatitude,
                destLng = trip?.destinationLongitude,
                destName = trip?.destinationName,
                routePoints = session.routeCoordinates,
                modifier = Modifier.fillMaxSize()
            )

            // 2. GPS Accuracy & Speed Banner (Top Floating)
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(if (session.gpsAccuracyOk) RouteWakeAccentLime else RouteWakeWarningAmber)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (session.gpsAccuracyOk) "GPS Locked" else "Low GPS Accuracy",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "•",
                        color = RouteWakeTextMutedDark
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Speed: ${session.formattedSpeed}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // 3. Bottom Expandable Travel HUD Panel
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp)
                ) {
                    // Drag handle to expand/collapse
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .width(36.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.outline)
                            .clickable { isExpanded = !isExpanded }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Primary Travel Telemetry: Remaining Distance & ETA
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DISTANCE REMAINING",
                                style = MaterialTheme.typography.labelSmall,
                                color = RouteWakeTextSecondaryDark,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = session.formattedRemainingDistance,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = RouteWakeAccentLime
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "ARRIVAL ETA",
                                style = MaterialTheme.typography.labelSmall,
                                color = RouteWakeTextSecondaryDark,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = session.formattedEta,
                                style = MaterialTheme.typography.headlineLarge,
                                fontWeight = FontWeight.Bold,
                                color = RouteWakeAccentCyan
                            )
                        }
                    }

                    // Expanded Telemetry and Status
                    AnimatedVisibility(visible = isExpanded) {
                        Column(modifier = Modifier.padding(top = 16.dp)) {
                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                            Spacer(modifier = Modifier.height(12.dp))

                            TelemetryRow(
                                label = "Alarm Target",
                                value = trip?.destinationName ?: "Unknown"
                            )
                            TelemetryRow(
                                label = "Trigger Condition",
                                value = trip?.formattedThreshold ?: "Distance radius"
                            )
                            TelemetryRow(
                                label = "Consecutive Fixes",
                                value = "${session.consecutiveReadingsInsideThreshold} / 2 required"
                            )
                            TelemetryRow(
                                label = "Background Execution",
                                value = "Foreground Service Active"
                            )

                            Spacer(modifier = Modifier.height(12.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Controls: Pause/Resume and End Trip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        if (session.state == MonitoringState.MONITORING) {
                            OutlinedButton(
                                onClick = onPauseClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Pause")
                            }
                        } else {
                            Button(
                                onClick = onResumeClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RouteWakeAccentLime)
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Resume")
                            }
                        }

                        Button(
                            onClick = onEndTripClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RouteWakeErrorRed)
                        ) {
                            Icon(Icons.Default.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("End Trip")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = RouteWakeTextSecondaryDark
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Active Navigation - Monitoring (Dark)",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ActiveNavigationMonitoringDarkPreview() {
    RouteWakeTheme(darkTheme = true) {
        ActiveNavigationScreen(
            session = com.routewake.app.ui.components.PreviewSampleData.sampleActiveSession,
            onBackClick = {},
            onPauseClick = {},
            onResumeClick = {},
            onEndTripClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Active Navigation - Paused (Light)",
    showBackground = true
)
@Composable
private fun ActiveNavigationPausedLightPreview() {
    RouteWakeTheme(darkTheme = false) {
        ActiveNavigationScreen(
            session = com.routewake.app.ui.components.PreviewSampleData.sampleActiveSession.copy(
                state = com.routewake.app.domain.model.MonitoringState.PAUSED
            ),
            onBackClick = {},
            onPauseClick = {},
            onResumeClick = {},
            onEndTripClick = {}
        )
    }
}

