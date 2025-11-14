package com.example.tp_android.data.datasource

import com.example.tp_android.data.model.GeocodingResult
import com.example.tp_android.data.model.Weather
import com.example.tp_android.data.model.WeatherApiResponse
import com.example.tp_android.data.model.WeatherCondition
import com.example.tp_android.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Data Source pour les appels réseau météo
 */
class WeatherRemoteDataSource {

    /**
     * Recherche une ville par son nom
     */
    suspend fun searchCity(cityName: String): Result<List<GeocodingResult>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.geocodingApi.searchCity(cityName)
                if (response.results.isNullOrEmpty()) {
                    Result.failure(Exception("Aucune ville trouvée pour : $cityName"))
                } else {
                    Result.success(response.results)
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Récupère les données météo pour une ville
     */
    suspend fun getWeatherData(
        cityName: String,
        latitude: Double,
        longitude: Double
    ): Result<Weather> {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.weatherApi.getWeatherForecast(
                    latitude = latitude,
                    longitude = longitude
                )

                // Convertir la réponse API en objet Weather
                val weather = convertApiResponseToWeather(cityName, response)
                Result.success(weather)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * Convertit la réponse API en objet Weather utilisable dans l'app
     */
    private fun convertApiResponseToWeather(
        cityName: String,
        apiResponse: WeatherApiResponse
    ): Weather {
        val hourly = apiResponse.hourly

        // Prendre l'heure actuelle (premier index) pour les données actuelles
        val currentTemp = hourly.temperature.firstOrNull() ?: 0.0
        val currentHumidity = hourly.humidity.firstOrNull() ?: 0
        val currentRain = hourly.rain.firstOrNull() ?: 0.0
        val currentWind = hourly.windSpeed.firstOrNull() ?: 0.0

        // Calculer min/max sur les 24 prochaines heures
        val temps24h = hourly.temperature.take(24).filterNotNull()
        val minTemp = temps24h.minOrNull() ?: currentTemp
        val maxTemp = temps24h.maxOrNull() ?: currentTemp

        // Déterminer la condition météo
        val condition = WeatherCondition.fromData(currentRain, currentHumidity)

        return Weather(
            cityName = cityName,
            latitude = apiResponse.latitude,
            longitude = apiResponse.longitude,
            currentTemperature = currentTemp,
            minTemperature = minTemp,
            maxTemperature = maxTemp,
            humidity = currentHumidity,
            windSpeed = currentWind,
            weatherCondition = condition,
            lastUpdate = System.currentTimeMillis()
        )
    }
}
