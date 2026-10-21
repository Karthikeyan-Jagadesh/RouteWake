package com.routewake.app.data.remote.api

import com.routewake.app.data.remote.dto.NominatimPlaceDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NominatimApi {
    @GET("search")
    suspend fun searchPlaces(
        @Query("q") query: String,
        @Query("format") format: String = "jsonv2",
        @Query("limit") limit: Int = 10,
        @Header("User-Agent") userAgent: String = "RouteWake-Android-App"
    ): Response<List<NominatimPlaceDto>>
}
