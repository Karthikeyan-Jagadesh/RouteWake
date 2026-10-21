package com.routewake.app.data.remote.dto

import com.google.gson.annotations.SerializedName
import com.routewake.app.domain.model.TripAlert

data class CreateTripRequest(
    val destinationName: String,
    val destinationLatitude: Double,
    val destinationLongitude: Double,
    val alarmType: String, // "DISTANCE" or "TIME"
    val distanceThresholdKm: Int? = null,
    val timeThresholdMinutes: Int? = null
)

data class TripAlertDto(
    val id: Long? = null,
    val userId: Long? = null,
    val destinationName: String? = null,
    val destinationLatitude: Double? = null,
    val destinationLongitude: Double? = null,
    val alarmType: String? = null,
    val distanceThresholdKm: Int? = null,
    val timeThresholdMinutes: Int? = null,
    val status: String? = null,
    val createdAt: String? = null,
    val message: String? = null,
    val error: String? = null
) {
    fun toDomain(): TripAlert? {
        if (id == null || destinationName == null || destinationLatitude == null || destinationLongitude == null) {
            return null
        }
        return TripAlert(
            id = id,
            userId = userId ?: 0L,
            destinationName = destinationName,
            destinationLatitude = destinationLatitude,
            destinationLongitude = destinationLongitude,
            alarmType = alarmType ?: "DISTANCE",
            distanceThresholdKm = distanceThresholdKm,
            timeThresholdMinutes = timeThresholdMinutes,
            status = status ?: "ACTIVE",
            createdAt = createdAt
        )
    }
}

data class GenericMessageResponse(
    val message: String? = null,
    val error: String? = null
)
