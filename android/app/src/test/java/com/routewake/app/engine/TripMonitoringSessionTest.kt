package com.routewake.app.engine

import com.routewake.app.domain.model.MonitoringState
import com.routewake.app.domain.model.TripAlert
import com.routewake.app.domain.model.TripMonitoringSession
import org.junit.Assert.*
import org.junit.Test

class TripMonitoringSessionTest {

    @Test
    fun defaultSession_isIdle() {
        val session = TripMonitoringSession()
        assertEquals(MonitoringState.IDLE, session.state)
        assertFalse(session.isMonitoringActive)
        assertEquals("--", session.formattedRemainingDistance)
    }

    @Test
    fun activeMonitoringSession_formatsMetricsCorrectly() {
        val trip = TripAlert(
            id = 1L,
            userId = 1L,
            destinationName = "Metro Station",
            destinationLatitude = 13.0827,
            destinationLongitude = 80.2707,
            alarmType = "DISTANCE",
            distanceThresholdKm = 2,
            timeThresholdMinutes = null
        )

        val session = TripMonitoringSession(
            activeTrip = trip,
            distanceRemainingKm = 1.456,
            etaMinutes = 4.2,
            speedKmh = 25.4f,
            state = MonitoringState.MONITORING
        )

        assertTrue(session.isMonitoringActive)
        assertEquals("1.46 km", session.formattedRemainingDistance)
        assertEquals("4 mins", session.formattedEta)
        assertEquals("25.4 km/h", session.formattedSpeed)
    }

    @Test
    fun tripAlert_determinesModeAndThresholdCorrectly() {
        val distTrip = TripAlert(
            id = 1L,
            userId = 1L,
            destinationName = "Stop A",
            destinationLatitude = 13.0,
            destinationLongitude = 80.0,
            alarmType = "DISTANCE",
            distanceThresholdKm = 2,
            timeThresholdMinutes = null
        )
        assertTrue(distTrip.isDistanceMode)
        assertFalse(distTrip.isTimeMode)
        assertEquals("2 km distance pulse", distTrip.formattedThreshold)

        val timeTrip = TripAlert(
            id = 2L,
            userId = 1L,
            destinationName = "Stop B",
            destinationLatitude = 13.0,
            destinationLongitude = 80.0,
            alarmType = "TIME",
            distanceThresholdKm = null,
            timeThresholdMinutes = 15
        )
        assertTrue(timeTrip.isTimeMode)
        assertFalse(timeTrip.isDistanceMode)
        assertEquals("15 min ETA pulse", timeTrip.formattedThreshold)
    }
}
