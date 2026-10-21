package com.routewake.app.domain.engine

import kotlin.math.*

object HaversineCalculator {
    const val EARTH_RADIUS_KM = 6371.0
    const val EARTH_RADIUS_METERS = 6371000.0

    /**
     * Computes the straight-line great-circle distance between two GPS coordinates using the Haversine formula.
     * @return Distance in kilometers, or throws IllegalArgumentException if coordinates are invalid.
     */
    fun computeDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        validateCoordinates(lat1, lon1)
        validateCoordinates(lat2, lon2)

        if (lat1 == lat2 && lon1 == lon2) {
            return 0.0
        }

        val phi1 = Math.toRadians(lat1)
        val phi2 = Math.toRadians(lat2)
        val deltaPhi = Math.toRadians(lat2 - lat1)
        val deltaLambda = Math.toRadians(lon2 - lon1)

        val sinDeltaPhi = sin(deltaPhi / 2.0)
        val sinDeltaLambda = sin(deltaLambda / 2.0)

        val a = (sinDeltaPhi * sinDeltaPhi) +
                cos(phi1) * cos(phi2) * (sinDeltaLambda * sinDeltaLambda)

        // Clamp 'a' to [0.0, 1.0] to prevent NaN due to floating point inaccuracies
        val clampedA = a.coerceIn(0.0, 1.0)
        val c = 2.0 * asin(sqrt(clampedA))

        return EARTH_RADIUS_KM * c
    }

    fun computeDistanceMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return computeDistanceKm(lat1, lon1, lat2, lon2) * 1000.0
    }

    private fun validateCoordinates(lat: Double, lon: Double) {
        require(!lat.isNaN() && !lon.isNaN()) { "Coordinates cannot be NaN" }
        require(lat in -90.0..90.0) { "Latitude must be between -90 and 90, got: $lat" }
        require(lon in -180.0..180.0) { "Longitude must be between -180 and 180, got: $lon" }
    }
}
