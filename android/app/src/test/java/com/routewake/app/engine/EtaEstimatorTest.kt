package com.routewake.app.engine

import com.routewake.app.domain.engine.EtaConfidence
import com.routewake.app.domain.engine.EtaEstimator
import com.routewake.app.domain.model.LocationPoint
import org.junit.Assert.*
import org.junit.Test

class EtaEstimatorTest {

    @Test
    fun liveGpsSpeed_computesHighConfidenceEta() {
        val current = LocationPoint(
            latitude = 13.0827,
            longitude = 80.2707,
            accuracyMeters = 10f,
            speedKmh = 60f
        )
        val distanceKm = 15.0 // at 60 km/h, 15 km takes 15 minutes

        val result = EtaEstimator.estimateEta(current, previousLocation = null, distanceKm = distanceKm)
        assertNotNull(result.etaMinutes)
        assertEquals(15.0, result.etaMinutes!!, 0.1)
        assertEquals(EtaConfidence.HIGH, result.confidence)
        assertFalse(result.isFallback)
    }

    @Test
    fun displacementSpeed_computesMediumConfidenceEta() {
        val t0 = System.currentTimeMillis() - 60000 // 1 minute ago
        val prev = LocationPoint(
            latitude = 13.0727,
            longitude = 80.2707,
            accuracyMeters = 15f,
            timestamp = t0
        )
        val current = LocationPoint(
            latitude = 13.0827,
            longitude = 80.2707,
            accuracyMeters = 15f,
            speedKmh = null, // GPS speed unavailable
            timestamp = System.currentTimeMillis()
        )

        val result = EtaEstimator.estimateEta(current, previousLocation = prev, distanceKm = 10.0)
        assertNotNull(result.etaMinutes)
        assertEquals(EtaConfidence.MEDIUM, result.confidence)
        assertFalse(result.isFallback)
    }

    @Test
    fun stationaryOrMissingSpeed_providesExplicitFallback() {
        val current = LocationPoint(
            latitude = 13.0827,
            longitude = 80.2707,
            accuracyMeters = 15f,
            speedKmh = 0f
        )

        val result = EtaEstimator.estimateEta(current, previousLocation = null, distanceKm = 10.0)
        assertNotNull(result.etaMinutes)
        assertEquals(EtaConfidence.ESTIMATE, result.confidence)
        assertTrue(result.isFallback)
        assertTrue("Fallback label should mention estimated", result.label.contains("Estimated", ignoreCase = true))
    }
}
