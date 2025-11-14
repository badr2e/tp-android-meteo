package com.example.tp_android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tp_android.data.model.Weather
import com.example.tp_android.data.repository.FavoritesRepository
import com.example.tp_android.data.repository.WeatherRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran de détail météo
 * Gère l'affichage des données météo détaillées et le toggle favori
 */
class DetailViewModel(
    private val weatherRepository: WeatherRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    // Données météo actuelles
    private val _weather = MutableStateFlow<Weather?>(null)
    val weather: StateFlow<Weather?> = _weather.asStateFlow()

    // État de chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // État de rafraîchissement
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Erreurs
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // État favori
    private val _isFavorite = MutableStateFlow(false)
    val isFavorite: StateFlow<Boolean> = _isFavorite.asStateFlow()

    /**
     * Charge les données météo pour une ville
     */
    fun loadWeather(
        cityName: String,
        latitude: Double,
        longitude: Double,
        forceRefresh: Boolean = false
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            weatherRepository.getWeather(
                cityName = cityName,
                latitude = latitude,
                longitude = longitude,
                forceRefresh = forceRefresh
            ).collect { result ->
                _isLoading.value = false

                if (result.isSuccess) {
                    _weather.value = result.getOrNull()
                    _error.value = null
                } else {
                    _error.value = result.exceptionOrNull()?.message
                        ?: "Erreur lors du chargement des données météo"
                }
            }

            // Vérifier si la ville est en favoris
            checkFavoriteStatus(latitude, longitude)
        }
    }

    /**
     * Rafraîchit les données météo (force le chargement depuis l'API)
     */
    fun refreshWeather() {
        val currentWeather = _weather.value ?: return

        viewModelScope.launch {
            _isRefreshing.value = true
            _error.value = null

            weatherRepository.getWeather(
                cityName = currentWeather.cityName,
                latitude = currentWeather.latitude,
                longitude = currentWeather.longitude,
                forceRefresh = true
            ).collect { result ->
                _isRefreshing.value = false

                if (result.isSuccess) {
                    _weather.value = result.getOrNull()
                    _error.value = null
                } else {
                    _error.value = result.exceptionOrNull()?.message
                        ?: "Erreur lors du rafraîchissement"
                }
            }
        }
    }

    /**
     * Vérifie si la ville est en favoris
     */
    private fun checkFavoriteStatus(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isFavorite.value = favoritesRepository.isFavorite(latitude, longitude)
        }
    }

    /**
     * Toggle l'état favori de la ville actuelle
     */
    fun toggleFavorite() {
        val currentWeather = _weather.value ?: return

        viewModelScope.launch {
            if (_isFavorite.value) {
                // Supprimer des favoris
                favoritesRepository.removeFavorite(
                    currentWeather.latitude,
                    currentWeather.longitude
                )
                _isFavorite.value = false
            } else {
                // Ajouter aux favoris
                favoritesRepository.addFavorite(
                    cityName = currentWeather.cityName,
                    latitude = currentWeather.latitude,
                    longitude = currentWeather.longitude,
                    country = null
                )
                _isFavorite.value = true
            }
        }
    }

    /**
     * Efface l'erreur
     */
    fun clearError() {
        _error.value = null
    }

    /**
     * Réinitialise le ViewModel
     */
    fun reset() {
        _weather.value = null
        _isLoading.value = false
        _isRefreshing.value = false
        _error.value = null
        _isFavorite.value = false
    }
}

/**
 * Factory pour créer le DetailViewModel avec ses dépendances
 */
class DetailViewModelFactory(
    private val weatherRepository: WeatherRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DetailViewModel::class.java)) {
            return DetailViewModel(weatherRepository, favoritesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
