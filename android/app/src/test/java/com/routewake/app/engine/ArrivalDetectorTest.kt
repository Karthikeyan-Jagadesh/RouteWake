package com.routewake.app.engine

import com.routewake.app.domain.engine.ArrivalDetector
import com.routewake.app.domain.model.LocationPoint
import com.routewake.app.domain.model.TripAlert
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class ArrivalDetectorTest {

    private lateinit var detector: ArrivalDetector
    private val destinationLat = 13.0827
    private val destinationLng = 80.2707

    private val sampleDistanceTrip = TripAlert(
        id = 101L,
        userId = 1L,
        destinationName = "Chennai Central",
        destinationLatitude = destinationLat,
        destinationLongitude = destinationLng,
        alarmType = "DISTANCE",
        distanceThresholdKm = 1,
        timeThresholdMinutes = null,
        status = "ACTIVE"
    )

    private val sampleTimeTrip = TripAlert(
        id = 102L,
        userId = 1L,
        destinationName = "Airport Terminal",
        destinationLatitude = destinationLat,
        destinationLongitude = destinationLng,
        alarmType = "TIME",
        distanceThresholdKm = null,
        timeThresholdMinutes = 10,
        status = "ACTIVE"
    )

    @Before
    fun setUp() {
        detector = ArrivalDetector(maxAcceptableAccuracyMeters = 50f, requiredConsecutiveReadings = 2)
    }

    @Test
    fun inaccurateGpsReading_isIgnoredAndDoesNotTriggerAlarm() {
        // Location very close (0.2 km), but accuracy is terrible (120 meters)
        val inaccurateFix = LocationPoint(
            latitude = destinationLat + 0.001,
            longitude = destinationLng + 0.001,
            accuracyMeters = 120f
        )

        val result = detector.evaluate(inaccurateFix, sampleDistanceTrip, speedKmh = 30f)
        assertFalse("Alarm should not trigger on inaccurate GPS reading", result.shouldTriggerAlarm)
        assertFalse("Accuracy should be flagged unacceptable", result.isAccuracyAcceptable)
        assertEquals(0, result.consecutiveHits)
    }

    @Test
    fun singleReadingInsideThreshold_doesNotTriggerAlarmDueToHysteresis() {
        // Point is ~0.5 km away (within 1 km threshold), good accuracy (10m)
        val validArrivalFix = LocationPoint(
            latitude = destinationLat + 0.003,
            longitude = destinationLng + 0.003,
            accuracyMeters = 10f
        )

        val firstResult = detector.evaluate(validArrivalFix, sampleDistanceTrip, speedKmh = 30f)
        assertFalse("First reading alone should not trigger due to hysteresis", firstResult.shouldTriggerAlarm)
        assertEquals(1, firstResult.consecutiveHits)
        assertTrue(firstResult.thresholdReached)
    }

    @Test
    fun twoConsecutiveReadingsInsideThreshold_triggersAlarm() {
        val validArrivalFix = LocationPoint(
            latitude = destinationLat + 0.003,
            longitude = destinationLng + 0.003,
            accuracyMeters = 10f
        )

        // First fix
        detector.evaluate(validArrivalFix, sampleDistanceTrip, speedKmh = 30f)

        // Second consecutive fix
        val secondResult = detector.evaluate(validArrivalFix, sampleDistanceTrip, speedKmh = 30f)
        assertTrue("Second consecutive reading within threshold must trigger alarm", secondResult.shouldTriggerAlarm)
        assertEquals(2, secondResult.consecutiveHits)
        assertTrue(detector.isAlarmTriggered())
    }

    @Test
    fun readingOutsideThreshold_resetsConsecutiveHits() {
        val arrivalFix = LocationPoint(
            latitude = destinationLat + 0.003,
            longitude = destinationLng + 0.003,
            accuracyMeters = 10f
        )
        // Point 5 km away
        val farFix = LocationPoint(
            latitude = destinationLat + 0.05,
            longitude = destinationLng + 0.05,
            accuracyMeters = 10f
        )

        // 1st hit
        detector.evaluate(arrivalFix, sampleDistanceTrip, speedKmh = 30f)

        // Glitch or moved away
        val farResult = detector.evaluate(farFix, sampleDistanceTrip, speedKmh = 30f)
        assertEquals(0, farResult.consecutiveHits)
        assertFalse(farResult.shouldTriggerAlarm)

        // New hit after glitch requires 2 fresh consecutive hits
        val retryResult = detector.evaluate(arrivalFix, sampleDistanceTrip, speedKmh = 30f)
        assertEquals(1, retryResult.consecutiveHits)
        assertFalse("Should not fire on 1st retry hit", retryResult.shouldTriggerAlarm)
    }

    @Test
    fun alarmDeduplication_preventsSubsequentDuplicateTriggers() {
        val arrivalFix = LocationPoint(
            latitude = destinationLat + 0.003,
            longitude = destinationLng + 0.003,
            accuracyMeters = 10f
        )

        // Hit 1
        detector.evaluate(arrivalFix, sampleDistanceTrip, speedKmh = 30f)
        // Hit 2 -> Fires
        val fireResult = detector.evaluate(arrivalFix, sampleDistanceTrip, speedKmh = 30f)
        assertTrue("Must fire on hit 2", fireResult.shouldTriggerAlarm)

        // Hit 3 -> Must NOT re-trigger
        val thirdResult = detector.evaluate(arrivalFix, sampleDistanceTrip, speedKmh = 30f)
        assertFalse("Subsequent fixes within threshold must not re-trigger alarm (deduplication)", thirdResult.shouldTriggerAlarm)
    }

    @Test
    fun etaMode_evaluatesTimeThresholdCorrectly() {
        // Point ~15 km away, speed = 60 km/h -> ETA = 15 mins (threshold is 10 mins, so false)
        val farFix = LocationPoint(
            latitude = destinationLat + 0.13,
            longitude = destinationLng + 0.01,
            accuracyMeters = 10f
        )
        val farResult = detector.evaluate(farFix, sampleTimeTrip, speedKmh = 60f)
        assertFalse("ETA of ~15 min should not trigger 10 min threshold", farResult.thresholdReached)

        // Point ~5 km away, speed = 60 km/h -> ETA = 5 mins (threshold is 10 mins, so true)
        val closeFix = LocationPoint(
            latitude = destinationLat + 0.04,
            longitude = destinationLng + 0.01,
            accuracyMeters = 10f
        )
        detector.evaluate(closeFix, sampleTimeTrip, speedKmh = 60f)
        val triggerResult = detector.evaluate(closeFix, sampleTimeTrip, speedKmh = 60f)
        assertTrue("ETA of 5 mins should trigger 10 min lead time alarm on 2nd hit", triggerResult.shouldTriggerAlarm)
    }
}
