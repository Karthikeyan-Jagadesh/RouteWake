package com.routewake.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class NominatimPlaceDto(
    @SerializedName("place_id") val placeId: Long? = null,
    @SerializedName("display_name") val displayName: String,
    @SerializedName("lat") val lat: String,
    @SerializedName("lon") val lon: String,
    @SerializedName("type") val type: String? = null
) {
    val latitude: Double?
        get() = lat.toDoubleOrNull()

    val longitude: Double?
        get() = lon.toDoubleOrNull()

    val primaryTitle: String
        get() = displayName.split(",").firstOrNull()?.trim() ?: displayName
}
