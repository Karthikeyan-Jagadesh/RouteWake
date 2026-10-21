package com.routewake.app.domain.model

data class LocationPoint(
    val latitude: Double,
    val longitude: Double,
    val accuracyMeters: Float = 0f,
    val speedKmh: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
) {
    val isValid: Boolean
        get() = latitude in -90.0..90.0 && longitude in -180.0..180.0
}
