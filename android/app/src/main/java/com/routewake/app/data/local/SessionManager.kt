package com.routewake.app.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.routewake.app.domain.model.User

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "routewake_secure_session",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        // Fallback for environments where Keystore AES-SIV is restricted
        context.getSharedPreferences("routewake_session_prefs", Context.MODE_PRIVATE)
    }

    companion object {
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_NAME = "user_name"
        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_THEME = "app_theme" // "SYSTEM", "DARK", "LIGHT"
        private const val KEY_DEFAULT_ALARM_MODE = "default_alarm_mode" // "DISTANCE" or "TIME"
        private const val KEY_DEFAULT_DISTANCE_KM = "default_distance_km"
        private const val KEY_DEFAULT_ETA_MINUTES = "default_eta_minutes"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_SNOOZE_DURATION_MINS = "snooze_duration_mins"
        private const val KEY_ACTIVE_TRIP_ID = "active_trip_id"
    }

    fun saveUserSession(userId: Long, userName: String, email: String) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_EMAIL, email)
            .apply()
    }

    fun getUser(): User? {
        if (!isLoggedIn()) return null
        val id = prefs.getLong(KEY_USER_ID, -1L)
        val name = prefs.getString(KEY_USER_NAME, "") ?: ""
        val email = prefs.getString(KEY_USER_EMAIL, "") ?: ""
        return if (id > 0) User(id, name, email) else null
    }

    fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_IS_LOGGED_IN, false)

    fun clearSession() {
        prefs.edit()
            .remove(KEY_IS_LOGGED_IN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_ACTIVE_TRIP_ID)
            .apply()
    }

    fun saveActiveTripId(tripId: Long?) {
        if (tripId != null) {
            prefs.edit().putLong(KEY_ACTIVE_TRIP_ID, tripId).apply()
        } else {
            prefs.edit().remove(KEY_ACTIVE_TRIP_ID).apply()
        }
    }

    fun getActiveTripId(): Long? {
        val id = prefs.getLong(KEY_ACTIVE_TRIP_ID, -1L)
        return if (id > 0) id else null
    }

    // App Preferences
    fun getThemePreference(): String = prefs.getString(KEY_THEME, "SYSTEM") ?: "SYSTEM"
    fun setThemePreference(theme: String) = prefs.edit().putString(KEY_THEME, theme).apply()

    fun getDefaultAlarmMode(): String = prefs.getString(KEY_DEFAULT_ALARM_MODE, "DISTANCE") ?: "DISTANCE"
    fun setDefaultAlarmMode(mode: String) = prefs.edit().putString(KEY_DEFAULT_ALARM_MODE, mode).apply()

    fun getDefaultDistanceKm(): Int = prefs.getInt(KEY_DEFAULT_DISTANCE_KM, 1)
    fun setDefaultDistanceKm(km: Int) = prefs.edit().putInt(KEY_DEFAULT_DISTANCE_KM, km).apply()

    fun getDefaultEtaMinutes(): Int = prefs.getInt(KEY_DEFAULT_ETA_MINUTES, 10)
    fun setDefaultEtaMinutes(mins: Int) = prefs.edit().putInt(KEY_DEFAULT_ETA_MINUTES, mins).apply()

    fun isSoundEnabled(): Boolean = prefs.getBoolean(KEY_SOUND_ENABLED, true)
    fun setSoundEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()

    fun isVibrationEnabled(): Boolean = prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
    fun setVibrationEnabled(enabled: Boolean) = prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()

    fun getSnoozeDurationMins(): Int = prefs.getInt(KEY_SNOOZE_DURATION_MINS, 5)
    fun setSnoozeDurationMins(mins: Int) = prefs.edit().putInt(KEY_SNOOZE_DURATION_MINS, mins).apply()
}
