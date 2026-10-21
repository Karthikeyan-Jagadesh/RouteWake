package com.routewake.app.data.remote.api

import com.routewake.app.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.*

interface RouteWakeApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("api/auth/logout")
    suspend fun logout(): Response<GenericMessageResponse>

    @GET("api/auth/me")
    suspend fun getCurrentUser(): Response<UserProfileResponse>

    @POST("api/trips")
    suspend fun createTrip(@Body request: CreateTripRequest): Response<TripAlertDto>

    @GET("api/trips/active")
    suspend fun getActiveTrip(): Response<TripAlertDto>

    @GET("api/trips/history")
    suspend fun getTripHistory(): Response<List<TripAlertDto>>

    @PUT("api/trips/{tripId}/complete")
    suspend fun completeTrip(@Path("tripId") tripId: Long): Response<GenericMessageResponse>

    @DELETE("api/trips/{tripId}")
    suspend fun deleteTrip(@Path("tripId") tripId: Long): Response<GenericMessageResponse>
}
