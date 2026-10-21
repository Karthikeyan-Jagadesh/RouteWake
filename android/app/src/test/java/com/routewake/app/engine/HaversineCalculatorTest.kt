package com.routewake.app.engine

import com.routewake.app.domain.engine.HaversineCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HaversineCalculatorTest {

    @Test
    fun sameCoordinates_returnsZeroDistance() {
        val lat = 13.0827
        val lng = 80.2707
        val distance = HaversineCalculator.computeDistanceKm(lat, lng, lat, lng)
        assertEquals(0.0, distance, 0.0001)
    }

    @Test
    fun knownCoordinates_computesAccurateDistance() {
        // Paris (48.8566, 2.3522) to London (51.5074, -0.1278) ~ 343 km
        val distance = HaversineCalculator.computeDistanceKm(48.8566, 2.3522, 51.5074, -0.1278)
        assertTrue("Distance should be between 340 and 346 km, got: $distance", distance in 340.0..346.0)
    }

    @Test
    fun shortDistance_computesMetersAccurately() {
        // Chennai Central (13.0827, 80.2707) to Chennai Egmore (13.0784, 80.2608) ~ 1.18 km
        val distanceKm = HaversineCalculator.computeDistanceKm(13.0827, 80.2707, 13.0784, 80.2608)
        val distanceMeters = HaversineCalculator.computeDistanceMeters(13.0827, 80.2707, 13.0784, 80.2608)
        assertTrue("Distance should be around 1.1 to 1.3 km, got: $distanceKm", distanceKm in 1.1..1.3)
        assertEquals(distanceKm * 1000.0, distanceMeters, 0.001)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidLatitude_throwsException() {
        HaversineCalculator.computeDistanceKm(95.0, 80.0, 13.0, 80.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidLongitude_throwsException() {
        HaversineCalculator.computeDistanceKm(13.0, -190.0, 13.0, 80.0)
    }

    @Test(expected = IllegalArgumentException::class)
    fun nanCoordinates_throwsException() {
        HaversineCalculator.computeDistanceKm(Double.NaN, 80.0, 13.0, 80.0)
    }
}
