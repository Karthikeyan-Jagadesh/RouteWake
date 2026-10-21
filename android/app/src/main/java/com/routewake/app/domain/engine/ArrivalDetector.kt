package com.routewake.app.domain.engine

import com.routewake.app.domain.model.LocationPoint
import com.routewake.app.domain.model.TripAlert

class ArrivalDetector(
    private val maxAcceptableAccuracyMeters: Float = 50f,
    private val requiredConsecutiveReadings: Int = 2
) {
    private var alarmAlreadyTriggered: Boolean = false
    private var consecutiveHits: Int = 0

    data class DetectionResult(
        val shouldTriggerAlarm: Boolean,
        val isAccuracyAcceptable: Boolean,
        val distanceKm: Double,
        val etaMinutes: Double?,
        val consecutiveHits: Int,
        val thresholdReached: Boolean
    )

    fun reset() {
        alarmAlreadyTriggered = false
        consecutiveHits = 0
    }

    fun evaluate(
        currentLocation: LocationPoint,
        trip: TripAlert,
        speedKmh: Float?
    ): DetectionResult {
        // 1. Validate coordinates
        if (!currentLocation.isValid) {
            return DetectionResult(
                shouldTriggerAlarm = false,
                isAccuracyAcceptable = false,
                distanceKm = 0.0,
                etaMinutes = null,
                consecutiveHits = consecutiveHits,
                thresholdReached = false
            )
        }

        // 2. Accuracy check (reject unreliable GPS jitter)
        val isAccuracyAcceptable = currentLocation.accuracyMeters <= maxAcceptableAccuracyMeters
        if (!isAccuracyAcceptable && currentLocation.accuracyMeters > 0) {
            // Keep existing consecutiveHits, don't trigger alarm on inaccurate reading
            return DetectionResult(
                shouldTriggerAlarm = false,
                isAccuracyAcceptable = false,
                distanceKm = HaversineCalculator.computeDistanceKm(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    trip.destinationLatitude,
                    trip.destinationLongitude
                ),
                etaMinutes = null,
                consecutiveHits = consecutiveHits,
                thresholdReached = false
            )
        }

        // 3. Compute straight-line distance
        val distanceKm = HaversineCalculator.computeDistanceKm(
            currentLocation.latitude,
            currentLocation.longitude,
            trip.destinationLatitude,
            trip.destinationLongitude
        )

        // 4. Compute ETA if in TIME mode
        val etaMinutes: Double? = if (speedKmh != null && speedKmh > 1.0f) {
            (distanceKm / speedKmh.toDouble()) * 60.0
        } else {
            null
        }

        // 5. Evaluate threshold condition
        val thresholdConditionMet = if (trip.isDistanceMode) {
            val threshold = trip.distanceThresholdKm?.toDouble() ?: 1.0
            distanceKm <= threshold
        } else {
            val threshold = trip.timeThresholdMinutes?.toDouble() ?: 10.0
            etaMinutes != null && etaMinutes <= threshold
        }

        // 6. Apply hysteresis logic (consecutive readings)
        if (thresholdConditionMet) {
            consecutiveHits++
        } else {
            consecutiveHits = 0
        }

        // 7. Check if alarm should fire (and prevent duplicate fires)
        val shouldFire = !alarmAlreadyTriggered && (consecutiveHits >= requiredConsecutiveReadings)
        if (shouldFire) {
            alarmAlreadyTriggered = true
        }

        return DetectionResult(
            shouldTriggerAlarm = shouldFire,
            isAccuracyAcceptable = true,
            distanceKm = distanceKm,
            etaMinutes = etaMinutes,
            consecutiveHits = consecutiveHits,
            thresholdReached = thresholdConditionMet
        )
    }

    fun isAlarmTriggered(): Boolean = alarmAlreadyTriggered

    fun markAlarmDismissed() {
        // Remains triggered for this trip to prevent retriggering
        alarmAlreadyTriggered = true
    }
}
