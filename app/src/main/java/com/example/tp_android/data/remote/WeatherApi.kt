package com.example.tp_android.data.remote

import com.example.tp_android.data.model.GeocodingApiResponse
import com.example.tp_android.data.model.WeatherApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Interface Retrofit pour les appels API météo
 */
interface WeatherApi {

    /**
     * Recherche de ville par nom (Geocoding)
     */
    @GET("v1/search")
    suspend fun searchCity(
        @Query("name") cityName: String,
        @Query("count") count: Int = 10,
        @Query("language") language: String = "fr",
        @Query("format") format: String = "json"
    ): GeocodingApiResponse

    /**
     * Récupération des données météo
     */
    @GET("v1/forecast")
    suspend fun getWeatherForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("hourly") hourly: String = "temperature_2m,relative_humidity_2m,apparent_temperature,rain,wind_speed_10m",
        @Query("models") models: String = "meteofrance_seamless"
    ): WeatherApiResponse

    companion object {
        const val GEOCODING_BASE_URL = "https://geocoding-api.open-meteo.com/"
        const val WEATHER_BASE_URL = "https://api.open-meteo.com/"
    }
}
