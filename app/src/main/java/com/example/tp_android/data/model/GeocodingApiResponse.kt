package com.example.tp_android.data.model

import com.google.gson.annotations.SerializedName

/**
 * Modèle pour la réponse de l'API Geocoding
 */
data class GeocodingApiResponse(
    @SerializedName("results") val results: List<GeocodingResult>?
)

data class GeocodingResult(
    @SerializedName("id") val id: Long,
    @SerializedName("name") val name: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("country") val country: String?,
    @SerializedName("admin1") val admin1: String?, // Région
    @SerializedName("country_code") val countryCode: String?
)
