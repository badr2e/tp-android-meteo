package com.example.tp_android.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.tp_android.data.local.entity.FavoriteCityEntity
import com.example.tp_android.data.model.GeocodingResult
import com.example.tp_android.data.model.Weather
import com.example.tp_android.data.repository.FavoritesRepository
import com.example.tp_android.data.repository.WeatherRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel pour l'écran d'accueil
 * Gère la recherche de villes, l'affichage des favoris et leurs données météo
 */
class HomeViewModel(
    private val weatherRepository: WeatherRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModel() {

    // État de la recherche
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<GeocodingResult>>(emptyList())
    val searchResults: StateFlow<List<GeocodingResult>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private val _searchError = MutableStateFlow<String?>(null)
    val searchError: StateFlow<String?> = _searchError.asStateFlow()

    // Liste des villes favorites
    private val _favoriteCities = MutableStateFlow<List<FavoriteCityEntity>>(emptyList())
    val favoriteCities: StateFlow<List<FavoriteCityEntity>> = _favoriteCities.asStateFlow()

    // Données météo des villes favorites
    private val _favoritesWeather = MutableStateFlow<Map<String, Weather>>(emptyMap())
    val favoritesWeather: StateFlow<Map<String, Weather>> = _favoritesWeather.asStateFlow()

    private val _isLoadingFavorites = MutableStateFlow(false)
    val isLoadingFavorites: StateFlow<Boolean> = _isLoadingFavorites.asStateFlow()

    init {
        // Observer les favoris et charger leurs données météo
        viewModelScope.launch {
            favoritesRepository.getFavoritesFlow().collect { favorites ->
                _favoriteCities.value = favorites
                loadFavoritesWeather(favorites)
            }
        }
    }

    /**
     * Met à jour la requête de recherche
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Recherche des villes par nom
     */
    fun searchCities(cityName: String) {
        if (cityName.isBlank()) {
            _searchResults.value = emptyList()
            return
        }

        viewModelScope.launch {
            _isSearching.value = true
            _searchError.value = null

            weatherRepository.searchCity(cityName).collect { result ->
                _isSearching.value = false

                if (result.isSuccess) {
                    _searchResults.value = result.getOrNull() ?: emptyList()
                    _searchError.value = null
                } else {
                    _searchResults.value = emptyList()
                    _searchError.value = result.exceptionOrNull()?.message
                        ?: "Erreur lors de la recherche"
                }
            }
        }
    }

    /**
     * Efface les résultats de recherche
     */
    fun clearSearchResults() {
        _searchResults.value = emptyList()
        _searchQuery.value = ""
        _searchError.value = null
    }

    /**
     * Charge les données météo pour toutes les villes favorites
     */
    private fun loadFavoritesWeather(favorites: List<FavoriteCityEntity>) {
        viewModelScope.launch {
            _isLoadingFavorites.value = true

            val weatherMap = mutableMapOf<String, Weather>()

            favorites.forEach { favorite ->
                weatherRepository.getWeather(
                    cityName = favorite.cityName,
                    latitude = favorite.latitude,
                    longitude = favorite.longitude,
                    forceRefresh = false
                ).collect { result ->
                    if (result.isSuccess) {
                        result.getOrNull()?.let { weather ->
                            weatherMap[favorite.cityKey] = weather
                        }
                    }
                }
            }

            _favoritesWeather.value = weatherMap
            _isLoadingFavorites.value = false
        }
    }

    /**
     * Rafraîchit les données météo des favoris
     */
    fun refreshFavoritesWeather() {
        loadFavoritesWeather(_favoriteCities.value)
    }

    /**
     * Ajoute une ville aux favoris
     */
    fun addToFavorites(geocodingResult: GeocodingResult) {
        viewModelScope.launch {
            favoritesRepository.addFavorite(geocodingResult)
        }
    }

    /**
     * Supprime une ville des favoris
     */
    fun removeFromFavorites(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            favoritesRepository.removeFavorite(latitude, longitude)
        }
    }

    /**
     * Vérifie si une ville est en favoris
     */
    suspend fun isFavorite(latitude: Double, longitude: Double): Boolean {
        return favoritesRepository.isFavorite(latitude, longitude)
    }

    // État pour la navigation vers l'écran de détails depuis la géolocalisation
    private val _navigateToLocation = MutableSharedFlow<Triple<String, Double, Double>>()
    val navigateToLocation: SharedFlow<Triple<String, Double, Double>> = _navigateToLocation.asSharedFlow()

    /**
     * Charge les données météo pour la position actuelle (géolocalisation)
     * Navigue automatiquement vers l'écran de détails
     */
    fun loadWeatherForCurrentLocation(cityName: String, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _navigateToLocation.emit(Triple(cityName, latitude, longitude))
        }
    }
}

/**
 * Factory pour créer le HomeViewModel avec ses dépendances
 */
class HomeViewModelFactory(
    private val weatherRepository: WeatherRepository,
    private val favoritesRepository: FavoritesRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(weatherRepository, favoritesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
