package com.routewake.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.routewake.app.data.local.SessionManager
import com.routewake.app.data.remote.RetrofitClient
import com.routewake.app.data.repository.AuthRepository
import com.routewake.app.domain.model.User
import com.routewake.app.theme.*
import com.routewake.app.ui.components.RouteWakeCard
import com.routewake.app.ui.components.RouteWakeTopBar
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    user: User?,
    sessionManager: SessionManager,
    authRepository: AuthRepository,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var soundEnabled by remember { mutableStateOf(sessionManager.isSoundEnabled()) }
    var vibrationEnabled by remember { mutableStateOf(sessionManager.isVibrationEnabled()) }
    var defaultMode by remember { mutableStateOf(sessionManager.getDefaultAlarmMode()) }
    var defaultDistance by remember { mutableIntStateOf(sessionManager.getDefaultDistanceKm()) }
    var defaultEta by remember { mutableIntStateOf(sessionManager.getDefaultEtaMinutes()) }
    var backendUrl by remember { mutableStateOf(RetrofitClient.getBackendUrl()) }

    val scope = rememberCoroutineScope()

    SettingsContent(
        user = user,
        soundEnabled = soundEnabled,
        onSoundEnabledChange = {
            soundEnabled = it
            sessionManager.setSoundEnabled(it)
        },
        vibrationEnabled = vibrationEnabled,
        onVibrationEnabledChange = {
            vibrationEnabled = it
            sessionManager.setVibrationEnabled(it)
        },
        defaultMode = defaultMode,
        onDefaultModeChange = {
            defaultMode = it
            sessionManager.setDefaultAlarmMode(it)
        },
        defaultDistance = defaultDistance,
        onDefaultDistanceChange = {
            defaultDistance = it
            sessionManager.setDefaultDistanceKm(it)
        },
        defaultEta = defaultEta,
        onDefaultEtaChange = {
            defaultEta = it
            sessionManager.setDefaultEtaMinutes(it)
        },
        backendUrl = backendUrl,
        onBackendUrlChange = {
            backendUrl = it
            RetrofitClient.updateBaseUrl(it)
        },
        onBackClick = onBackClick,
        onLogoutClick = {
            scope.launch {
                authRepository.logout()
                sessionManager.clearSession()
                onLogoutClick()
            }
        }
    )
}

@Composable
fun SettingsContent(
    user: User?,
    soundEnabled: Boolean,
    onSoundEnabledChange: (Boolean) -> Unit,
    vibrationEnabled: Boolean,
    onVibrationEnabledChange: (Boolean) -> Unit,
    defaultMode: String,
    onDefaultModeChange: (String) -> Unit,
    defaultDistance: Int,
    onDefaultDistanceChange: (Int) -> Unit,
    defaultEta: Int,
    onDefaultEtaChange: (Int) -> Unit,
    backendUrl: String,
    onBackendUrlChange: (String) -> Unit,
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit
) {
    var showBackendUrlDialog by remember { mutableStateOf(false) }
    var tempUrl by remember(backendUrl) { mutableStateOf(backendUrl) }

    Scaffold(
        topBar = {
            RouteWakeTopBar(
                title = "Settings & Profile",
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
            // 1. Account Profile Header
            RouteWakeCard(
                backgroundColor = MaterialTheme.colorScheme.surface,
                borderColor = MaterialTheme.colorScheme.outline
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .background(RouteWakeAccentLime.copy(alpha = 0.15f), shape = RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = RouteWakeAccentLime,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = user?.fullName ?: "Guest Passenger",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = user?.email ?: "Offline mode",
                            style = MaterialTheme.typography.bodySmall,
                            color = RouteWakeTextSecondaryDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 2. Alarm Defaults Section
            SectionHeader(title = "Alert Defaults")
            Spacer(modifier = Modifier.height(8.dp))

            RouteWakeCard {
                SettingsSwitchRow(
                    icon = Icons.AutoMirrored.Filled.VolumeUp,
                    title = "Alarm Audio",
                    subtitle = "Play audible wake tone when approaching stop",
                    checked = soundEnabled,
                    onCheckedChange = onSoundEnabledChange
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = MaterialTheme.colorScheme.outlineVariant)

                SettingsSwitchRow(
                    icon = Icons.Default.Vibration,
                    title = "Vibration Alert",
                    subtitle = "Vibrate device using pulse sequence",
                    checked = vibrationEnabled,
                    onCheckedChange = onVibrationEnabledChange
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 3. Backend & Network Configuration
            SectionHeader(title = "Backend Server Integration")
            Spacer(modifier = Modifier.height(8.dp))

            RouteWakeCard(
                onClick = {
                    tempUrl = backendUrl
                    showBackendUrlDialog = true
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Server API Endpoint",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = backendUrl,
                            style = MaterialTheme.typography.bodySmall,
                            color = RouteWakeAccentCyan
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = RouteWakeTextSecondaryDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. Privacy & Location Guidance
            SectionHeader(title = "Privacy & Background Tracking")
            Spacer(modifier = Modifier.height(8.dp))

            RouteWakeCard {
                Text(
                    text = "Continuous GPS Protection",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "RouteWake uses an Android Foreground Service during active trips so your alarm fires even when your phone is locked in your pocket. Live GPS fixes are never sold or stored permanently in Oracle.",
                    style = MaterialTheme.typography.bodySmall,
                    color = RouteWakeTextSecondaryDark
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // 5. Logout Action Button
            Button(
                onClick = onLogoutClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = RouteWakeErrorRed.copy(alpha = 0.15f),
                    contentColor = RouteWakeErrorRed
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Log Out of Account",
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showBackendUrlDialog) {
        AlertDialog(
            onDismissRequest = { showBackendUrlDialog = false },
            title = { Text("Configure Backend URL") },
            text = {
                Column {
                    Text(
                        text = "Set the URL of your Spring Boot backend (e.g. http://10.0.2.2:8081 for emulator, or your PC IP for physical devices).",
                        style = MaterialTheme.typography.bodySmall,
                        color = RouteWakeTextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = tempUrl,
                        onValueChange = { tempUrl = it },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onBackendUrlChange(tempUrl)
                        showBackendUrlDialog = false
                    }
                ) {
                    Text("Save", color = RouteWakeAccentLime, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showBackendUrlDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = RouteWakeAccentLime,
        letterSpacing = 0.5.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
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
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = RouteWakeAccentLime,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = RouteWakeTextSecondaryDark
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = RouteWakeAccentLime,
                checkedTrackColor = RouteWakeAccentLime.copy(alpha = 0.3f)
            )
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Settings Screen (Dark)",
    showBackground = true,
    uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SettingsScreenDarkPreview() {
    RouteWakeTheme(darkTheme = true) {
        SettingsContent(
            user = com.routewake.app.ui.components.PreviewSampleData.sampleUser,
            soundEnabled = true,
            onSoundEnabledChange = {},
            vibrationEnabled = true,
            onVibrationEnabledChange = {},
            defaultMode = "DISTANCE",
            onDefaultModeChange = {},
            defaultDistance = 1,
            onDefaultDistanceChange = {},
            defaultEta = 10,
            onDefaultEtaChange = {},
            backendUrl = "http://10.0.2.2:8081",
            onBackendUrlChange = {},
            onBackClick = {},
            onLogoutClick = {}
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview(
    name = "Settings Screen (Light)",
    showBackground = true
)
@Composable
private fun SettingsScreenLightPreview() {
    RouteWakeTheme(darkTheme = false) {
        SettingsContent(
            user = com.routewake.app.ui.components.PreviewSampleData.sampleUser,
            soundEnabled = false,
            onSoundEnabledChange = {},
            vibrationEnabled = false,
            onVibrationEnabledChange = {},
            defaultMode = "TIME",
            onDefaultModeChange = {},
            defaultDistance = 2,
            onDefaultDistanceChange = {},
            defaultEta = 15,
            onDefaultEtaChange = {},
            backendUrl = "http://192.168.1.15:8081",
            onBackendUrlChange = {},
            onBackClick = {},
            onLogoutClick = {}
        )
    }
}

