package com.routewake.app.data.repository

import com.routewake.app.data.remote.RetrofitClient
import com.routewake.app.data.remote.dto.NominatimPlaceDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeocodingRepository {
    private val nominatimApi = RetrofitClient.nominatimApi
    private val osrmApi = RetrofitClient.osrmApi

    // In-memory cache for fast search queries
    private val searchCache = mutableMapOf<String, List<NominatimPlaceDto>>()

    suspend fun searchPlaces(query: String): Result<List<NominatimPlaceDto>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) {
            return@withContext Result.success(emptyList())
        }

        searchCache[trimmed.lowercase()]?.let {
            return@withContext Result.success(it)
        }

        try {
            val response = nominatimApi.searchPlaces(query = trimmed)
            if (response.isSuccessful && response.body() != null) {
                val results = response.body()!!
                searchCache[trimmed.lowercase()] = results
                Result.success(results)
            } else {
                Result.success(emptyList())
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun fetchRouteCoordinates(
        startLat: Double,
        startLng: Double,
        destLat: Double,
        destLng: Double
    ): List<Pair<Double, Double>> = withContext(Dispatchers.IO) {
        val fallback = listOf(Pair(startLat, startLng), Pair(destLat, destLng))
        try {
            val coordinateString = "$startLng,$startLat;$destLng,$destLat"
            val response = osrmApi.getDrivingRoute(coordinateString)
            if (response.isSuccessful && response.body() != null) {
                val route = response.body()!!.routes?.firstOrNull()
                val coordinates = route?.geometry?.coordinates
                if (!coordinates.isNullOrEmpty()) {
                    return@withContext coordinates.map { coord ->
                        // OSRM returns [longitude, latitude]
                        Pair(coord[1], coord[0])
                    }
                }
            }
            fallback
        } catch (_: Exception) {
            fallback
        }
    }
}
