package com.routewake.app.data.remote.api

import com.routewake.app.data.remote.dto.OsrmRouteResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OsrmApi {
    @GET("route/v1/driving/{coordinates}")
    suspend fun getDrivingRoute(
        @Path("coordinates") coordinates: String, // "startLng,startLat;destLng,destLat"
        @Query("overview") overview: String = "full",
        @Query("geometries") geometries: String = "geojson"
    ): Response<OsrmRouteResponse>
}
