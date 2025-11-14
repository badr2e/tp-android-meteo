package com.example.tp_android.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèle pour la réponse de l'API Météo
 * Accessible uniquement dans la couche Data
 */
data class WeatherApiResponse(
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("generationtime_ms") val generationTimeMs: Double,
    @SerializedName("utc_offset_seconds") val utcOffsetSeconds: Int,
    @SerializedName("timezone") val timezone: String,
    @SerializedName("timezone_abbreviation") val timezoneAbbreviation: String,
    @SerializedName("elevation") val elevation: Double,
    @SerializedName("hourly_units") val hourlyUnits: HourlyUnits,
    @SerializedName("hourly") val hourly: HourlyData
)

data class HourlyUnits(
    @SerializedName("time") val time: String,
    @SerializedName("temperature_2m") val temperature: String,
    @SerializedName("relative_humidity_2m") val humidity: String,
    @SerializedName("apparent_temperature") val apparentTemperature: String,
    @SerializedName("rain") val rain: String,
    @SerializedName("wind_speed_10m") val windSpeed: String
)

data class HourlyData(
    @SerializedName("time") val time: List<String>,
    @SerializedName("temperature_2m") val temperature: List<Double?>,
    @SerializedName("relative_humidity_2m") val humidity: List<Int?>,
    @SerializedName("apparent_temperature") val apparentTemperature: List<Double?>,
    @SerializedName("rain") val rain: List<Double?>,
    @SerializedName("wind_speed_10m") val windSpeed: List<Double?>
)
