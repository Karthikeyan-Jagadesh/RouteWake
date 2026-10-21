package com.routewake.app.ui.components

import com.routewake.app.data.remote.dto.NominatimPlaceDto
import com.routewake.app.domain.model.*

object PreviewSampleData {
    val sampleUser = User(
        id = 42,
        fullName = "Karthikeyan Jagadesh",
        email = "karthikeyan@routewake.app"
    )

    val sampleRecentTrips = listOf(
        TripAlert(
            id = 101,
            userId = 42,
            destinationName = "Grand Central Terminal",
            destinationLatitude = 40.7527,
            destinationLongitude = -73.9772,
            alarmType = "DISTANCE",
            distanceThresholdKm = 1,
            timeThresholdMinutes = null,
            status = "ARRIVED",
            createdAt = "2026-10-20T08:30:00"
        ),
        TripAlert(
            id = 102,
            userId = 42,
            destinationName = "John F. Kennedy International Airport",
            destinationLatitude = 40.6413,
            destinationLongitude = -73.7781,
            alarmType = "TIME",
            distanceThresholdKm = null,
            timeThresholdMinutes = 15,
            status = "ARRIVED",
            createdAt = "2026-10-18T14:15:00"
        ),
        TripAlert(
            id = 103,
            userId = 42,
            destinationName = "Penn Station",
            destinationLatitude = 40.7506,
            destinationLongitude = -73.9935,
            alarmType = "DISTANCE",
            distanceThresholdKm = 1,
            timeThresholdMinutes = null,
            status = "CANCELLED",
            createdAt = "2026-10-16T19:45:00"
        )
    )

    val sampleActiveSession = TripMonitoringSession(
        activeTrip = sampleRecentTrips.first(),
        currentLocation = LocationPoint(latitude = 40.7484, longitude = -73.9857, accuracyMeters = 12.5f),
        distanceRemainingKm = 0.84,
        etaMinutes = 4.0,
        speedKmh = 24.5f,
        state = MonitoringState.MONITORING,
        gpsAccuracyOk = true
    )

    val sampleIdleSession = TripMonitoringSession(
        state = MonitoringState.IDLE,
        gpsAccuracyOk = true
    )

    val sampleSavedPlaces = listOf(
        SavedPlace(
            id = 1,
            label = "Home",
            address = "45 Ocean Avenue, Brooklyn, NY",
            latitude = 40.6558,
            longitude = -73.9602
        ),
        SavedPlace(
            id = 2,
            label = "Office",
            address = "One World Trade Center, Manhattan, NY",
            latitude = 40.7127,
            longitude = -74.0134
        ),
        SavedPlace(
            id = 3,
            label = "Gym",
            address = "Equinox Soho, 521 Broadway, NY",
            latitude = 40.7233,
            longitude = -73.9983
        )
    )

    val sampleSearchResults = listOf(
        NominatimPlaceDto(
            placeId = 1,
            displayName = "Grand Central Terminal, 89 E 42nd St, New York, NY 10017",
            lat = "40.7527",
            lon = "-73.9772"
        ),
        NominatimPlaceDto(
            placeId = 2,
            displayName = "Central Park, New York, NY",
            lat = "40.785091",
            lon = "-73.968285"
        ),
        NominatimPlaceDto(
            placeId = 3,
            displayName = "Times Square, Broadway, New York, NY 10036",
            lat = "40.7580",
            lon = "-73.9855"
        )
    )
}
