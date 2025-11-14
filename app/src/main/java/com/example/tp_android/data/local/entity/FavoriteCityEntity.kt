package com.example.tp_android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity Room pour stocker les villes favorites
 */
@Entity(tableName = "favorite_cities")
data class FavoriteCityEntity(
    @PrimaryKey
    val cityKey: String, // "latitude_longitude"
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val country: String?,
    val addedAt: Long = System.currentTimeMillis()
)
