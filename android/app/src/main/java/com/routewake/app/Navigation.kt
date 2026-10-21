package com.routewake.app

import android.content.Context
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.routewake.app.data.local.SessionManager
import com.routewake.app.data.repository.AuthRepository
import com.routewake.app.data.repository.GeocodingRepository
import com.routewake.app.data.repository.SavedPlacesRepository
import com.routewake.app.data.repository.TripRepository
import com.routewake.app.domain.model.MonitoringState
import com.routewake.app.service.LocationForegroundService
import com.routewake.app.ui.alarm.AlarmTriggerDialog
import com.routewake.app.ui.auth.LoginScreen
import com.routewake.app.ui.auth.RegisterScreen
import com.routewake.app.ui.destination.DestinationSearchScreen
import com.routewake.app.ui.history.TripHistoryScreen
import com.routewake.app.ui.home.HomeScreen
import com.routewake.app.ui.navigation.ActiveNavigationScreen
import com.routewake.app.ui.onboarding.OnboardingScreen
import com.routewake.app.ui.savedplaces.SavedPlacesScreen
import com.routewake.app.ui.settings.SettingsScreen
import com.routewake.app.ui.splash.SplashScreen
import com.routewake.app.ui.tripsetup.TripSetupScreen
import kotlinx.coroutines.launch

@Composable
fun MainNavigation() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    val authRepository = remember { AuthRepository(sessionManager) }
    val tripRepository = remember { TripRepository(sessionManager) }
    val savedPlacesRepository = remember { SavedPlacesRepository(context) }
    val geocodingRepository = remember { GeocodingRepository() }

    val monitoringSession by LocationForegroundService.monitoringSession.collectAsState()
    val tripHistory by tripRepository.tripHistoryFlow.collectAsState()
    val savedPlaces by savedPlacesRepository.savedPlaces.collectAsState()
    val scope = rememberCoroutineScope()

    val backStack = rememberNavBackStack(SplashNavKey)

    LaunchedEffect(Unit) {
        tripRepository.getActiveTrip()
        tripRepository.refreshHistory()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                if (backStack.size > 1) {
                    backStack.removeLastOrNull()
                }
            },
            entryProvider = entryProvider {
                entry<SplashNavKey> {
                    SplashScreen(
                        isLoggedIn = sessionManager.isLoggedIn(),
                        onNavigateNext = { isLoggedIn ->
                            backStack.clear()
                            if (isLoggedIn) {
                                backStack.add(HomeNavKey)
                            } else {
                                backStack.add(OnboardingNavKey)
                            }
                        }
                    )
                }

                entry<OnboardingNavKey> {
                    OnboardingScreen(
                        onGetStartedClick = {
                            backStack.clear()
                            backStack.add(LoginNavKey)
                        }
                    )
                }

                entry<LoginNavKey> {
                    LoginScreen(
                        authRepository = authRepository,
                        onLoginSuccess = {
                            backStack.clear()
                            backStack.add(HomeNavKey)
                        },
                        onNavigateToRegister = {
                            backStack.add(RegisterNavKey)
                        },
                        onContinueAsGuest = {
                            backStack.clear()
                            backStack.add(HomeNavKey)
                        }
                    )
                }

                entry<RegisterNavKey> {
                    RegisterScreen(
                        authRepository = authRepository,
                        onRegisterSuccess = {
                            backStack.clear()
                            backStack.add(HomeNavKey)
                        },
                        onNavigateToLogin = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                entry<HomeNavKey> {
                    HomeScreen(
                        user = authRepository.getLocalUser(),
                        session = monitoringSession,
                        recentTrips = tripHistory,
                        savedPlaces = savedPlaces,
                        onPlanTripClick = {
                            backStack.add(DestinationSearchNavKey)
                        },
                        onActiveTripClick = {
                            backStack.add(ActiveNavKey)
                        },
                        onSearchClick = {
                            backStack.add(DestinationSearchNavKey)
                        },
                        onSavedPlaceClick = { place ->
                            backStack.add(TripSetupNavKey(place.address, place.latitude, place.longitude))
                        },
                        onHistoryClick = {
                            backStack.add(TripHistoryNavKey)
                        },
                        onSettingsClick = {
                            backStack.add(SettingsNavKey)
                        }
                    )
                }

                entry<DestinationSearchNavKey> {
                    DestinationSearchScreen(
                        currentLocation = monitoringSession.currentLocation,
                        geocodingRepository = geocodingRepository,
                        onBackClick = { backStack.removeLastOrNull() },
                        onDestinationSelected = { name, lat, lng ->
                            backStack.add(TripSetupNavKey(name, lat, lng))
                        }
                    )
                }

                entry<TripSetupNavKey> { key ->
                    TripSetupScreen(
                        destName = key.destName,
                        destLat = key.destLat,
                        destLng = key.destLng,
                        tripRepository = tripRepository,
                        savedPlacesRepository = savedPlacesRepository,
                        onBackClick = { backStack.removeLastOrNull() },
                        onMonitoringStarted = { trip ->
                            LocationForegroundService.startService(context, trip)
                            backStack.removeLastOrNull()
                            backStack.add(ActiveNavKey)
                        }
                    )
                }

                entry<ActiveNavKey> {
                    ActiveNavigationScreen(
                        session = monitoringSession,
                        onPauseClick = {
                            val intent = android.content.Intent(context, LocationForegroundService::class.java).apply {
                                action = LocationForegroundService.ACTION_PAUSE_MONITORING
                            }
                            context.startService(intent)
                        },
                        onResumeClick = {
                            val intent = android.content.Intent(context, LocationForegroundService::class.java).apply {
                                action = LocationForegroundService.ACTION_RESUME_MONITORING
                            }
                            context.startService(intent)
                        },
                        onEndTripClick = {
                            val currentTripId = monitoringSession.activeTrip?.id
                            LocationForegroundService.stopService(context)
                            if (currentTripId != null) {
                                scope.launch {
                                    tripRepository.completeTrip(currentTripId)
                                }
                            }
                            backStack.clear()
                            backStack.add(HomeNavKey)
                        },
                        onBackClick = {
                            backStack.removeLastOrNull()
                        }
                    )
                }

                entry<TripHistoryNavKey> {
                    TripHistoryScreen(
                        tripRepository = tripRepository,
                        onBackClick = { backStack.removeLastOrNull() },
                        onReplanTrip = { trip ->
                            backStack.add(
                                TripSetupNavKey(
                                    trip.destinationName,
                                    trip.destinationLatitude,
                                    trip.destinationLongitude
                                )
                            )
                        }
                    )
                }

                entry<SavedPlacesNavKey> {
                    SavedPlacesScreen(
                        savedPlacesRepository = savedPlacesRepository,
                        onBackClick = { backStack.removeLastOrNull() },
                        onPlanTripWithPlace = { place ->
                            backStack.add(TripSetupNavKey(place.address, place.latitude, place.longitude))
                        }
                    )
                }

                entry<SettingsNavKey> {
                    SettingsScreen(
                        user = authRepository.getLocalUser(),
                        sessionManager = sessionManager,
                        authRepository = authRepository,
                        onBackClick = { backStack.removeLastOrNull() },
                        onLogoutClick = {
                            backStack.clear()
                            backStack.add(LoginNavKey)
                        }
                    )
                }
            }
        )

        // Global Arrival Alarm Trigger Dialog
        if (monitoringSession.state == MonitoringState.ARRIVED && monitoringSession.activeTrip != null) {
            AlarmTriggerDialog(
                trip = monitoringSession.activeTrip!!,
                distanceKm = monitoringSession.distanceRemainingKm,
                onStopAlarm = {
                    val tripId = monitoringSession.activeTrip?.id
                    val intent = android.content.Intent(context, LocationForegroundService::class.java).apply {
                        action = LocationForegroundService.ACTION_DISMISS_ALARM
                    }
                    context.startService(intent)
                    LocationForegroundService.stopService(context)
                    if (tripId != null) {
                        scope.launch {
                            tripRepository.completeTrip(tripId)
                        }
                    }
                },
                onSnooze = {
                    val intent = android.content.Intent(context, LocationForegroundService::class.java).apply {
                        action = LocationForegroundService.ACTION_SNOOZE_ALARM
                    }
                    context.startService(intent)
                }
            )
        }
    }
}
