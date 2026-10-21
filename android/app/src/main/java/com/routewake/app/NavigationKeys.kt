package com.routewake.app

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object SplashNavKey : NavKey
@Serializable data object OnboardingNavKey : NavKey
@Serializable data object LoginNavKey : NavKey
@Serializable data object RegisterNavKey : NavKey
@Serializable data object HomeNavKey : NavKey
@Serializable data object DestinationSearchNavKey : NavKey
@Serializable data class TripSetupNavKey(
    val destName: String,
    val destLat: Double,
    val destLng: Double
) : NavKey
@Serializable data object ActiveNavKey : NavKey
@Serializable data object TripHistoryNavKey : NavKey
@Serializable data object SavedPlacesNavKey : NavKey
@Serializable data object SettingsNavKey : NavKey
