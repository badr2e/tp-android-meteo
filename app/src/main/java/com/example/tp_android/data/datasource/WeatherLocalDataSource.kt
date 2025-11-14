package com.example.tp_android.data.datasource

import com.example.tp_android.data.local.dao.WeatherDao
import com.example.tp_android.data.local.entity.WeatherEntity
import com.example.tp_android.data.model.Weather
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Data Source pour le cache local (Room)
 */
class WeatherLocalDataSource(private val weatherDao: WeatherDao) {

    /**
     * Sauvegarde les données météo en cache
     */
    suspend fun saveWeather(weather: Weather) {
        withContext(Dispatchers.IO) {
            val entity = WeatherEntity.fromWeather(weather)
            weatherDao.insertWeather(entity)
        }
    }

    /**
     * Récupère les données météo depuis le cache
     */
    suspend fun getWeather(latitude: Double, longitude: Double): Weather? {
        return withContext(Dispatchers.IO) {
            val cityKey = "${latitude}_${longitude}"
            weatherDao.getWeatherByKey(cityKey)?.toWeather()
        }
    }

    /**
     * Récupère toutes les données météo en cache sous forme de Flow
     */
    fun getAllWeatherFlow(): Flow<List<Weather>> {
        return weatherDao.getAllWeatherFlow().map { entities ->
            entities.map { it.toWeather() }
        }
    }

    /**
     * Supprime les données météo d'une ville
     */
    suspend fun deleteWeather(latitude: Double, longitude: Double) {
        withContext(Dispatchers.IO) {
            val cityKey = "${latitude}_${longitude}"
            weatherDao.deleteWeather(cityKey)
        }
    }

    /**
     * Nettoie le cache (données de plus de 24h)
     */
    suspend fun cleanOldCache() {
        withContext(Dispatchers.IO) {
            val maxAge = System.currentTimeMillis() - (24 * 60 * 60 * 1000) // 24h
            weatherDao.deleteOldWeather(maxAge)
        }
    }

    /**
     * Vérifie si les données en cache sont encore valides (< 1h)
     */
    suspend fun isCacheValid(latitude: Double, longitude: Double): Boolean {
        return withContext(Dispatchers.IO) {
            val weather = getWeather(latitude, longitude)
            if (weather == null) {
                false
            } else {
                val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
                weather.lastUpdate > oneHourAgo
            }
        }
    }
}
