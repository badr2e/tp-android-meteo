package com.example.tp_android.data.repository

import com.example.tp_android.data.local.dao.FavoriteCityDao
import com.example.tp_android.data.local.entity.FavoriteCityEntity
import com.example.tp_android.data.model.GeocodingResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository pour la gestion des villes favorites
 */
class FavoritesRepository(private val favoriteCityDao: FavoriteCityDao) {

    /**
     * Récupère toutes les villes favorites sous forme de Flow
     */
    fun getFavoritesFlow(): Flow<List<FavoriteCityEntity>> {
        return favoriteCityDao.getAllFavoritesFlow()
    }

    /**
     * Récupère toutes les villes favorites (une seule fois)
     */
    suspend fun getFavorites(): List<FavoriteCityEntity> {
        return withContext(Dispatchers.IO) {
            favoriteCityDao.getAllFavorites()
        }
    }

    /**
     * Ajoute une ville aux favoris
     */
    suspend fun addFavorite(geocodingResult: GeocodingResult) {
        withContext(Dispatchers.IO) {
            val entity = FavoriteCityEntity(
                cityKey = "${geocodingResult.latitude}_${geocodingResult.longitude}",
                cityName = geocodingResult.name,
                latitude = geocodingResult.latitude,
                longitude = geocodingResult.longitude,
                country = geocodingResult.country
            )
            favoriteCityDao.insertFavorite(entity)
        }
    }

    /**
     * Ajoute une ville aux favoris (version manuelle)
     */
    suspend fun addFavorite(
        cityName: String,
        latitude: Double,
        longitude: Double,
        country: String? = null
    ) {
        withContext(Dispatchers.IO) {
            val entity = FavoriteCityEntity(
                cityKey = "${latitude}_${longitude}",
                cityName = cityName,
                latitude = latitude,
                longitude = longitude,
                country = country
            )
            favoriteCityDao.insertFavorite(entity)
        }
    }

    /**
     * Supprime une ville des favoris
     */
    suspend fun removeFavorite(latitude: Double, longitude: Double) {
        withContext(Dispatchers.IO) {
            val cityKey = "${latitude}_${longitude}"
            favoriteCityDao.deleteFavorite(cityKey)
        }
    }

    /**
     * Vérifie si une ville est en favoris
     */
    suspend fun isFavorite(latitude: Double, longitude: Double): Boolean {
        return withContext(Dispatchers.IO) {
            val cityKey = "${latitude}_${longitude}"
            favoriteCityDao.isFavorite(cityKey)
        }
    }

    /**
     * Supprime tous les favoris
     */
    suspend fun clearAllFavorites() {
        withContext(Dispatchers.IO) {
            favoriteCityDao.clearAllFavorites()
        }
    }
}
