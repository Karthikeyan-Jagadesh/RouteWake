package com.routewake.app.ui.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.routewake.app.theme.RouteWakeAccentCyan
import com.routewake.app.theme.RouteWakeAccentLime
import com.routewake.app.theme.RouteWakeTextSecondaryDark
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onNavigateNext: (isLoggedIn: Boolean) -> Unit,
    isLoggedIn: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    LaunchedEffect(Unit) {
        delay(1800)
        onNavigateNext(isLoggedIn)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(scale)
                    .clip(CircleShape)
                    .background(RouteWakeAccentLime.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(RouteWakeAccentLime),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Navigation,
                        contentDescription = "RouteWake Logo",
                        tint = Color(0xFF022C22),
                        modifier = Modifier.size(36.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "RouteWake",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                letterSpacing = (-0.5).sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Intelligent Travel Alarm Companion",
                style = MaterialTheme.typography.bodyMedium,
                color = RouteWakeTextSecondaryDark
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Splash Screen (Dark)",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SplashScreenDarkPreview() {
    com.routewake.app.theme.RouteWakeTheme(darkTheme = true) {
        SplashScreen(onNavigateNext = {}, isLoggedIn = false)
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Splash Screen (Light)",
    showBackground = true
)
@Composable
private fun SplashScreenLightPreview() {
    com.routewake.app.theme.RouteWakeTheme(darkTheme = false) {
        SplashScreen(onNavigateNext = {}, isLoggedIn = true)
    }
}

