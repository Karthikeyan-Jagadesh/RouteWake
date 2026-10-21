package com.routewake.app.domain.engine

import com.routewake.app.domain.model.LocationPoint

enum class EtaConfidence {
    HIGH,
    MEDIUM,
    ESTIMATE
}

data class EtaResult(
    val etaMinutes: Double?,
    val confidence: EtaConfidence,
    val calculationTimestamp: Long = System.currentTimeMillis(),
    val isFallback: Boolean = false,
    val label: String
)

object EtaEstimator {
    private const val DEFAULT_TRANSIT_SPEED_KMH = 30.0f // Average city transit fallback speed

    fun estimateEta(
        currentLocation: LocationPoint,
        previousLocation: LocationPoint?,
        distanceKm: Double
    ): EtaResult {
        // Priority 1: Direct GPS hardware speed (if >= 2 km/h)
        val gpsSpeed = currentLocation.speedKmh
        if (gpsSpeed != null && gpsSpeed >= 3.0f) {
            val minutes = (distanceKm / gpsSpeed.toDouble()) * 60.0
            val confidence = if (currentLocation.accuracyMeters < 20f) EtaConfidence.HIGH else EtaConfidence.MEDIUM
            return EtaResult(
                etaMinutes = minutes,
                confidence = confidence,
                isFallback = false,
                label = "Live GPS Speed (${String.format("%.0f", gpsSpeed)} km/h)"
            )
        }

        // Priority 2: Moving window displacement between previous and current location
        if (previousLocation != null && previousLocation.timestamp < currentLocation.timestamp) {
            val deltaHours = (currentLocation.timestamp - previousLocation.timestamp) / 3600000.0
            if (deltaHours in 0.0001..0.05) { // Between ~0.3s and 3 mins
                val deltaKm = HaversineCalculator.computeDistanceKm(
                    previousLocation.latitude,
                    previousLocation.longitude,
                    currentLocation.latitude,
                    currentLocation.longitude
                )
                val calculatedSpeed = (deltaKm / deltaHours).toFloat()
                if (calculatedSpeed >= 3.0f && calculatedSpeed <= 250.0f) {
                    val minutes = (distanceKm / calculatedSpeed.toDouble()) * 60.0
                    return EtaResult(
                        etaMinutes = minutes,
                        confidence = EtaConfidence.MEDIUM,
                        isFallback = false,
                        label = "Displacement (${String.format("%.0f", calculatedSpeed)} km/h)"
                    )
                }
            }
        }

        // Priority 3: Clearly labelled fallback estimate
        val fallbackMinutes = (distanceKm / DEFAULT_TRANSIT_SPEED_KMH.toDouble()) * 60.0
        return EtaResult(
            etaMinutes = fallbackMinutes,
            confidence = EtaConfidence.ESTIMATE,
            isFallback = true,
            label = "Estimated at ~${DEFAULT_TRANSIT_SPEED_KMH.toInt()} km/h"
        )
    }
}
