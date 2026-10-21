package com.routewake.app.data.repository

import com.routewake.app.data.local.SessionManager
import com.routewake.app.data.remote.RetrofitClient
import com.routewake.app.data.remote.dto.CreateTripRequest
import com.routewake.app.domain.model.TripAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class TripRepository(private val sessionManager: SessionManager) {
    private val api = RetrofitClient.routeWakeApi

    // Local cached active trip state
    private val _activeTripFlow = MutableStateFlow<TripAlert?>(null)
    val activeTripFlow: StateFlow<TripAlert?> = _activeTripFlow.asStateFlow()

    // Local cached history
    private val _cachedHistory = MutableStateFlow<List<TripAlert>>(emptyList())
    val tripHistoryFlow: StateFlow<List<TripAlert>> = _cachedHistory.asStateFlow()

    suspend fun createTrip(
        destinationName: String,
        destinationLatitude: Double,
        destinationLongitude: Double,
        alarmType: String,
        distanceThresholdKm: Int?,
        timeThresholdMinutes: Int?
    ): Result<TripAlert> = withContext(Dispatchers.IO) {
        val request = CreateTripRequest(
            destinationName = destinationName,
            destinationLatitude = destinationLatitude,
            destinationLongitude = destinationLongitude,
            alarmType = alarmType,
            distanceThresholdKm = distanceThresholdKm,
            timeThresholdMinutes = timeThresholdMinutes
        )

        try {
            val response = api.createTrip(request)
            if (response.isSuccessful && response.body() != null) {
                val trip = response.body()!!.toDomain()
                    ?: throw IllegalStateException("Could not parse trip from server")
                _activeTripFlow.value = trip
                sessionManager.saveActiveTripId(trip.id)
                refreshHistory()
                Result.success(trip)
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Failed to create trip"
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            // Offline fallback: create local trip session if backend unreachable
            val localTrip = TripAlert(
                id = System.currentTimeMillis(),
                userId = sessionManager.getUser()?.id ?: 1L,
                destinationName = destinationName,
                destinationLatitude = destinationLatitude,
                destinationLongitude = destinationLongitude,
                alarmType = alarmType,
                distanceThresholdKm = distanceThresholdKm,
                timeThresholdMinutes = timeThresholdMinutes,
                status = "ACTIVE",
                createdAt = "Just now (Offline)"
            )
            _activeTripFlow.value = localTrip
            sessionManager.saveActiveTripId(localTrip.id)
            Result.success(localTrip)
        }
    }

    suspend fun getActiveTrip(): Result<TripAlert?> = withContext(Dispatchers.IO) {
        try {
            val response = api.getActiveTrip()
            if (response.isSuccessful && response.body() != null) {
                val trip = response.body()!!.toDomain()
                _activeTripFlow.value = trip
                sessionManager.saveActiveTripId(trip?.id)
                Result.success(trip)
            } else {
                Result.success(_activeTripFlow.value)
            }
        } catch (_: Exception) {
            Result.success(_activeTripFlow.value)
        }
    }

    suspend fun refreshHistory(): Result<List<TripAlert>> = withContext(Dispatchers.IO) {
        try {
            val response = api.getTripHistory()
            if (response.isSuccessful && response.body() != null) {
                val list = response.body()!!.mapNotNull { it.toDomain() }
                _cachedHistory.value = list
                Result.success(list)
            } else {
                Result.success(_cachedHistory.value)
            }
        } catch (e: Exception) {
            Result.success(_cachedHistory.value)
        }
    }

    suspend fun completeTrip(tripId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.completeTrip(tripId)
        } catch (_: Exception) {
            // Completed locally
        } finally {
            if (_activeTripFlow.value?.id == tripId) {
                _activeTripFlow.value = null
                sessionManager.saveActiveTripId(null)
            }
            refreshHistory()
        }
        Result.success(Unit)
    }

    suspend fun deleteTrip(tripId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.deleteTrip(tripId)
        } catch (_: Exception) {
            // Remove from local cache
        } finally {
            _cachedHistory.value = _cachedHistory.value.filterNot { it.id == tripId }
        }
        Result.success(Unit)
    }

    fun clearActiveTrip() {
        _activeTripFlow.value = null
        sessionManager.saveActiveTripId(null)
    }
}
