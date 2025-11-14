package com.example.tp_android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.tp_android.data.model.Weather
import com.example.tp_android.data.model.WeatherCondition

/**
 * Entity Room pour stocker les données météo en cache
 */
@Entity(tableName = "weather_cache")
data class WeatherEntity(
    @PrimaryKey
    val cityKey: String, // Clé unique : "latitude_longitude"
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val currentTemperature: Double,
    val minTemperature: Double,
    val maxTemperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCondition: String, // Stocké comme String
    val lastUpdate: Long
) {
    companion object {
        /**
         * Convertit un objet Weather en WeatherEntity
         */
        fun fromWeather(weather: Weather): WeatherEntity {
            return WeatherEntity(
                cityKey = "${weather.latitude}_${weather.longitude}",
                cityName = weather.cityName,
                latitude = weather.latitude,
                longitude = weather.longitude,
                currentTemperature = weather.currentTemperature,
                minTemperature = weather.minTemperature,
                maxTemperature = weather.maxTemperature,
                humidity = weather.humidity,
                windSpeed = weather.windSpeed,
                weatherCondition = weather.weatherCondition.name,
                lastUpdate = weather.lastUpdate
            )
        }
    }

    /**
     * Convertit WeatherEntity en Weather
     */
    fun toWeather(): Weather {
        return Weather(
            cityName = cityName,
            latitude = latitude,
            longitude = longitude,
            currentTemperature = currentTemperature,
            minTemperature = minTemperature,
            maxTemperature = maxTemperature,
            humidity = humidity,
            windSpeed = windSpeed,
            weatherCondition = WeatherCondition.valueOf(weatherCondition),
            lastUpdate = lastUpdate
        )
    }
}
