package com.routewake.app.domain.model

enum class MonitoringState {
    IDLE,
    PREPARING,
    MONITORING,
    PAUSED,
    ARRIVED,
    ERROR
}

data class TripMonitoringSession(
    val activeTrip: TripAlert? = null,
    val currentLocation: LocationPoint? = null,
    val distanceRemainingKm: Double? = null,
    val etaMinutes: Double? = null,
    val speedKmh: Float? = null,
    val state: MonitoringState = MonitoringState.IDLE,
    val arrivalTriggered: Boolean = false,
    val consecutiveReadingsInsideThreshold: Int = 0,
    val lastUpdateTimestamp: Long = 0L,
    val gpsAccuracyOk: Boolean = true,
    val routeCoordinates: List<Pair<Double, Double>> = emptyList(),
    val errorMessage: String? = null
) {
    val isMonitoringActive: Boolean
        get() = state == MonitoringState.MONITORING

    val formattedRemainingDistance: String
        get() = distanceRemainingKm?.let { String.format("%.2f km", it) } ?: "--"

    val formattedEta: String
        get() = etaMinutes?.let {
            val mins = Math.round(it).toInt()
            if (mins <= 1) "1 min" else "$mins mins"
        } ?: "Calculating..."

    val formattedSpeed: String
        get() = speedKmh?.let { String.format("%.1f km/h", it) } ?: "0.0 km/h"
}
