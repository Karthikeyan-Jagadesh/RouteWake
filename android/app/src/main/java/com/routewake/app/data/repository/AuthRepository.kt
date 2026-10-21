package com.routewake.app.data.repository

import com.routewake.app.data.local.SessionManager
import com.routewake.app.data.remote.RetrofitClient
import com.routewake.app.data.remote.dto.LoginRequest
import com.routewake.app.data.remote.dto.RegisterRequest
import com.routewake.app.domain.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepository(private val sessionManager: SessionManager) {
    private val api = RetrofitClient.routeWakeApi

    suspend fun login(email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(LoginRequest(email.trim(), password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val userId = body.userId ?: 1L
                val userName = body.userName ?: "Passenger"
                val userEmail = body.email ?: email
                sessionManager.saveUserSession(userId, userName, userEmail)
                Result.success(User(userId, userName, userEmail))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Invalid email or password"
                Result.failure(Exception(extractErrorMessage(errorMsg)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(fullName: String, email: String, password: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = api.register(RegisterRequest(fullName.trim(), email.trim(), password))
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                val userId = body.userId ?: 1L
                val userName = body.userName ?: fullName
                val userEmail = body.email ?: email
                sessionManager.saveUserSession(userId, userName, userEmail)
                Result.success(User(userId, userName, userEmail))
            } else {
                val errorMsg = response.errorBody()?.string() ?: "Registration failed"
                Result.failure(Exception(extractErrorMessage(errorMsg)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            api.logout()
        } catch (_: Exception) {
            // Ignore network failure on logout
        } finally {
            sessionManager.clearSession()
            RetrofitClient.clearCookies()
        }
        Result.success(Unit)
    }

    suspend fun restoreSession(): Result<User?> = withContext(Dispatchers.IO) {
        val localUser = sessionManager.getUser()
        if (localUser == null) {
            return@withContext Result.success(null)
        }
        try {
            val response = api.getCurrentUser()
            if (response.isSuccessful && response.body()?.userId != null) {
                val body = response.body()!!
                val user = User(body.userId!!, body.userName ?: localUser.fullName, body.email ?: localUser.email)
                sessionManager.saveUserSession(user.id, user.fullName, user.email)
                Result.success(user)
            } else {
                // If backend session expired, fallback to local cached user session
                Result.success(localUser)
            }
        } catch (_: Exception) {
            // Offline fallback
            Result.success(localUser)
        }
    }

    fun getLocalUser(): User? = sessionManager.getUser()

    fun isLoggedIn(): Boolean = sessionManager.isLoggedIn()

    private fun extractErrorMessage(json: String): String {
        return try {
            val regex = """"error"\s*:\s*"([^"]+)"""".toRegex()
            regex.find(json)?.groupValues?.get(1) ?: "Request failed"
        } catch (_: Exception) {
            "Request failed"
        }
    }
}
