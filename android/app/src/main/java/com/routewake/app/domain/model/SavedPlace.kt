package com.routewake.app.domain.model

data class SavedPlace(
    val id: Long = 0,
    val label: String,
    val address: String,
    val latitude: Double,
    val longitude: Double,
    val isFavorite: Boolean = true
)
