package com.example.tp_android.data.local.dao

import androidx.room.*
import com.example.tp_android.data.local.entity.WeatherEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO pour les opérations sur le cache météo
 */
@Dao
interface WeatherDao {

    /**
     * Insère ou met à jour une entrée météo en cache
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)

    /**
     * Récupère les données météo pour une ville spécifique
     */
    @Query("SELECT * FROM weather_cache WHERE cityKey = :cityKey LIMIT 1")
    suspend fun getWeatherByKey(cityKey: String): WeatherEntity?

    /**
     * Récupère toutes les données météo en cache sous forme de Flow
     */
    @Query("SELECT * FROM weather_cache ORDER BY lastUpdate DESC")
    fun getAllWeatherFlow(): Flow<List<WeatherEntity>>

    /**
     * Supprime les données météo d'une ville
     */
    @Query("DELETE FROM weather_cache WHERE cityKey = :cityKey")
    suspend fun deleteWeather(cityKey: String)

    /**
     * Supprime toutes les données en cache
     */
    @Query("DELETE FROM weather_cache")
    suspend fun clearAllWeather()

    /**
     * Supprime les données trop anciennes (plus de 24h)
     */
    @Query("DELETE FROM weather_cache WHERE lastUpdate < :maxAge")
    suspend fun deleteOldWeather(maxAge: Long)
}
