package com.example.tp_android.data.model

/**
 * Modèle de données météo pour l'application
 * Accessible partout dans l'app (simplifié par rapport à l'API)
 */
data class Weather(
    val cityName: String,
    val latitude: Double,
    val longitude: Double,
    val currentTemperature: Double,
    val minTemperature: Double,
    val maxTemperature: Double,
    val humidity: Int,
    val windSpeed: Double,
    val weatherCondition: WeatherCondition,
    val lastUpdate: Long = System.currentTimeMillis()
)

/**
 * Condition météorologique simplifiée
 */
enum class WeatherCondition {
    SUNNY,      // Ensoleillé
    CLOUDY,     // Nuageux
    RAINY,      // Pluvieux
    UNKNOWN;    // Inconnu

    companion object {
        /**
         * Détermine la condition météo en fonction de la pluie et de l'humidité
         */
        fun fromData(rain: Double?, humidity: Int?): WeatherCondition {
            return when {
                rain != null && rain > 0.5 -> RAINY
                humidity != null && humidity > 80 -> CLOUDY
                humidity != null && humidity < 60 -> SUNNY
                else -> UNKNOWN
            }
        }
    }
}
