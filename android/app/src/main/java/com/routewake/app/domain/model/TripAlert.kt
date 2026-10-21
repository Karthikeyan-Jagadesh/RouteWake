package com.routewake.app.domain.model

data class TripAlert(
    val id: Long,
    val userId: Long,
    val destinationName: String,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val alarmType: String, // "DISTANCE" or "TIME"
    val distanceThresholdKm: Int?,
    val timeThresholdMinutes: Int?,
    val status: String = "ACTIVE",
    val createdAt: String? = null
) {
    val isDistanceMode: Boolean
        get() = alarmType.equals("DISTANCE", ignoreCase = true)

    val isTimeMode: Boolean
        get() = alarmType.equals("TIME", ignoreCase = true)

    val formattedThreshold: String
        get() = if (isDistanceMode) {
            "${distanceThresholdKm ?: 1} km distance pulse"
        } else {
            "${timeThresholdMinutes ?: 10} min ETA pulse"
        }

    val isActive: Boolean
        get() = status.equals("ACTIVE", ignoreCase = true)
}
