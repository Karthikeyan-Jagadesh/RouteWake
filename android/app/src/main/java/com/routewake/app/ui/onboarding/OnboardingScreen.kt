package com.routewake.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.routewake.app.theme.*
import com.routewake.app.ui.components.RouteWakeButton
import com.routewake.app.ui.components.RouteWakeCard

@Composable
fun OnboardingScreen(
    onGetStartedClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Never miss your stop again.",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "RouteWake monitors your journey in the background and wakes you with precise distance and arrival pulses.",
                style = MaterialTheme.typography.bodyLarge,
                color = RouteWakeTextSecondaryDark
            )

            Spacer(modifier = Modifier.height(32.dp))

            FeatureCard(
                icon = Icons.Default.LocationOn,
                title = "Precise Location Tracking",
                description = "Monitors live GPS position with hysteresis filtering to eliminate false alerts."
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                icon = Icons.Default.Alarm,
                title = "Distance & ETA Alerts",
                description = "Choose between radius thresholds (500m, 1km, 2km) or estimated arrival lead time."
            )

            Spacer(modifier = Modifier.height(16.dp))

            FeatureCard(
                icon = Icons.Default.Shield,
                title = "Privacy & Safety First",
                description = "Your live location stays on your device. Only trip metadata is synchronized to your account."
            )
        }

        Column(modifier = Modifier.padding(top = 24.dp)) {
            RouteWakeButton(
                text = "Get Started",
                onClick = onGetStartedClick
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun FeatureCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    RouteWakeCard(
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outline
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(RouteWakeAccentLime.copy(alpha = 0.15f), shape = MaterialTheme.shapes.small),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = RouteWakeAccentLime,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = RouteWakeTextSecondaryDark
                )
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Onboarding Screen (Dark)",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun OnboardingScreenDarkPreview() {
    RouteWakeTheme(darkTheme = true) {
        OnboardingScreen(onGetStartedClick = {})
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Onboarding Screen (Light)",
    showBackground = true
)
@Composable
private fun OnboardingScreenLightPreview() {
    RouteWakeTheme(darkTheme = false) {
        OnboardingScreen(onGetStartedClick = {})
    }
}

