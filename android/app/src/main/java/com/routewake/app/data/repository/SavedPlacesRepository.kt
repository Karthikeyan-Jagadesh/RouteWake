package com.routewake.app.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.routewake.app.domain.model.SavedPlace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SavedPlacesRepository(context: Context) {
    private val prefs = context.getSharedPreferences("routewake_saved_places", Context.MODE_PRIVATE)
    private val gson = Gson()

    private val _savedPlaces = MutableStateFlow<List<SavedPlace>>(loadInitialPlaces())
    val savedPlaces: StateFlow<List<SavedPlace>> = _savedPlaces.asStateFlow()

    private fun loadInitialPlaces(): List<SavedPlace> {
        val json = prefs.getString("places_json", null)
        if (!json.isNullOrBlank()) {
            val type = object : TypeToken<List<SavedPlace>>() {}.type
            return gson.fromJson(json, type) ?: defaultPlaces()
        }
        val defaults = defaultPlaces()
        savePlacesToPrefs(defaults)
        return defaults
    }

    private fun defaultPlaces(): List<SavedPlace> = listOf(
        SavedPlace(1L, "Home", "Central Residency, Main Avenue", 13.0827, 80.2707, true),
        SavedPlace(2L, "Tech Campus", "Innovation Tech Park, Sector 4", 12.9716, 80.2464, true),
        SavedPlace(3L, "Metro Central", "Central Metro Interchange Station", 13.0818, 80.2772, false)
    )

    private fun savePlacesToPrefs(list: List<SavedPlace>) {
        prefs.edit().putString("places_json", gson.toJson(list)).apply()
    }

    fun addPlace(label: String, address: String, latitude: Double, longitude: Double) {
        val newPlace = SavedPlace(
            id = System.currentTimeMillis(),
            label = label.ifBlank { "Saved Place" },
            address = address,
            latitude = latitude,
            longitude = longitude
        )
        val updated = listOf(newPlace) + _savedPlaces.value
        _savedPlaces.value = updated
        savePlacesToPrefs(updated)
    }

    fun deletePlace(id: Long) {
        val updated = _savedPlaces.value.filterNot { it.id == id }
        _savedPlaces.value = updated
        savePlacesToPrefs(updated)
    }
}
