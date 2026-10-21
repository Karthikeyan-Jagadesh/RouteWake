package com.routewake.app.ui.alarm

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.routewake.app.domain.model.TripAlert
import com.routewake.app.theme.*
import com.routewake.app.ui.components.RouteWakeButton

@Composable
fun AlarmTriggerDialog(
    trip: TripAlert,
    distanceKm: Double?,
    onStopAlarm: () -> Unit,
    onSnooze: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "alarmRing")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alarmScale"
    )

    Dialog(
        onDismissRequest = { /* Modal must be explicitly acknowledged */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(2.dp, RouteWakeAccentCyan),
            shadowElevation = 24.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Pulsing Warning Icon
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(RouteWakeAccentCyan.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(RouteWakeAccentCyan),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Alarm,
                            contentDescription = "Wake Pulse",
                            tint = Color(0xFF083344),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "ARRIVING AT STOP",
                    style = MaterialTheme.typography.labelLarge,
                    color = RouteWakeAccentCyan,
                    letterSpacing = 1.5.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = trip.destinationName,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                val reasonText = if (trip.isDistanceMode) {
                    val dist = distanceKm?.let { String.format("%.2f km", it) } ?: "target radius"
                    "You are within $dist of your stop. Prepare to get down."
                } else {
                    "Your estimated arrival lead time has been reached."
                }

                Text(
                    text = reasonText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RouteWakeTextSecondaryDark,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(28.dp))

                // Stop Alarm Primary Action
                RouteWakeButton(
                    text = "Stop Alarm",
                    isPrimary = true,
                    leadingIcon = Icons.Default.Close,
                    onClick = onStopAlarm
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Snooze Action
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Snooze,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Snooze (5 Minutes)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Alarm Dialog (Dark)",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AlarmTriggerDialogDarkPreview() {
    RouteWakeTheme(darkTheme = true) {
        AlarmTriggerDialog(
            trip = com.routewake.app.ui.components.PreviewSampleData.sampleRecentTrips[0],
            distanceKm = 0.85,
            onStopAlarm = {},
            onSnooze = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Alarm Dialog (Light)",
    showBackground = true
)
@Composable
private fun AlarmTriggerDialogLightPreview() {
    RouteWakeTheme(darkTheme = false) {
        AlarmTriggerDialog(
            trip = com.routewake.app.ui.components.PreviewSampleData.sampleRecentTrips[1],
            distanceKm = 1.2,
            onStopAlarm = {},
            onSnooze = {}
        )
    }
}


