package com.routewake.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class OsrmRouteResponse(
    @SerializedName("code") val code: String? = null,
    @SerializedName("routes") val routes: List<OsrmRoute>? = null
)

data class OsrmRoute(
    @SerializedName("distance") val distanceMeters: Double? = null,
    @SerializedName("duration") val durationSeconds: Double? = null,
    @SerializedName("geometry") val geometry: OsrmGeometry? = null
)

data class OsrmGeometry(
    @SerializedName("coordinates") val coordinates: List<List<Double>>? = null
)
