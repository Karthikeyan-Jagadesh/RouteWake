package com.routewake.app.data.remote.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String
)

data class AuthResponse(
    val message: String? = null,
    val userName: String? = null,
    val userId: Long? = null,
    val email: String? = null,
    val error: String? = null
)

data class UserProfileResponse(
    val userId: Long? = null,
    val userName: String? = null,
    val email: String? = null,
    val error: String? = null
)
