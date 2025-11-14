package com.example.tp_android.data.local.dao

import androidx.room.*
import com.example.tp_android.data.local.entity.FavoriteCityEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les opérations sur les villes favorites
 */
@Dao
interface FavoriteCityDao {

    /**
     * Ajoute une ville aux favoris
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(city: FavoriteCityEntity)

    /**
     * Récupère toutes les villes favorites sous forme de Flow
     * (pour observer les changements en temps réel)
     */
    @Query("SELECT * FROM favorite_cities ORDER BY addedAt DESC")
    fun getAllFavoritesFlow(): Flow<List<FavoriteCityEntity>>

    /**
     * Récupère toutes les villes favorites (une seule fois)
     */
    @Query("SELECT * FROM favorite_cities ORDER BY addedAt DESC")
    suspend fun getAllFavorites(): List<FavoriteCityEntity>

    /**
     * Vérifie si une ville est en favoris
     */
    @Query("SELECT EXISTS(SELECT 1 FROM favorite_cities WHERE cityKey = :cityKey LIMIT 1)")
    suspend fun isFavorite(cityKey: String): Boolean

    /**
     * Supprime une ville des favoris
     */
    @Query("DELETE FROM favorite_cities WHERE cityKey = :cityKey")
    suspend fun deleteFavorite(cityKey: String)

    /**
     * Supprime tous les favoris
     */
    @Query("DELETE FROM favorite_cities")
    suspend fun clearAllFavorites()
}
