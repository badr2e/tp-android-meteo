package com.example.tp_android.data.repository

import com.example.tp_android.data.datasource.WeatherLocalDataSource
import com.example.tp_android.data.datasource.WeatherRemoteDataSource
import com.example.tp_android.data.model.GeocodingResult
import com.example.tp_android.data.model.Weather
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Repository pour la gestion des données météo
 * Stratégie : Cache-First avec fallback réseau
 */
class WeatherRepository(
    private val remoteDataSource: WeatherRemoteDataSource,
    private val localDataSource: WeatherLocalDataSource
) {

    /**
     * Recherche une ville par nom
     */
    fun searchCity(cityName: String): Flow<Result<List<GeocodingResult>>> = flow {
        val result = remoteDataSource.searchCity(cityName)
        emit(result)
    }

    /**
     * Récupère les données météo avec stratégie cache-first
     * 1. Vérifie le cache
     * 2. Si cache valide, retourne les données du cache
     * 3. Sinon, appel réseau et mise en cache
     */
    fun getWeather(
        cityName: String,
        latitude: Double,
        longitude: Double,
        forceRefresh: Boolean = false
    ): Flow<Result<Weather>> = flow {
        try {
            // 1. Vérifier le cache si pas de force refresh
            if (!forceRefresh) {
                val cachedWeather = localDataSource.getWeather(latitude, longitude)
                if (cachedWeather != null && localDataSource.isCacheValid(latitude, longitude)) {
                    // Cache valide, émettre les données
                    emit(Result.success(cachedWeather))
                    return@flow
                }
            }

            // 2. Cache invalide ou force refresh, appel réseau
            val result = remoteDataSource.getWeatherData(cityName, latitude, longitude)

            if (result.isSuccess) {
                val weather = result.getOrNull()!!
                // 3. Sauvegarder en cache
                localDataSource.saveWeather(weather)
                // 4. Émettre les données
                emit(Result.success(weather))
            } else {
                // Échec réseau, essayer de retourner le cache même expiré
                val cachedWeather = localDataSource.getWeather(latitude, longitude)
                if (cachedWeather != null) {
                    emit(Result.success(cachedWeather))
                } else {
                    emit(Result.failure(result.exceptionOrNull() ?: Exception("Erreur inconnue")))
                }
            }
        } catch (e: Exception) {
            // En cas d'erreur, essayer le cache
            val cachedWeather = localDataSource.getWeather(latitude, longitude)
            if (cachedWeather != null) {
                emit(Result.success(cachedWeather))
            } else {
                emit(Result.failure(e))
            }
        }
    }

    /**
     * Nettoie le cache ancien
     */
    suspend fun cleanOldCache() {
        localDataSource.cleanOldCache()
    }
}
