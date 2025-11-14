package com.example.tp_android.ui.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tp_android.data.local.entity.FavoriteCityEntity
import com.example.tp_android.data.model.GeocodingResult
import com.example.tp_android.data.model.Weather
import com.example.tp_android.data.model.WeatherCondition
import com.example.tp_android.ui.viewmodel.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onCityClick: (String, Double, Double) -> Unit,
    onLocationRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()
    val searchError by viewModel.searchError.collectAsState()
    val favoriteCities by viewModel.favoriteCities.collectAsState()
    val favoritesWeather by viewModel.favoritesWeather.collectAsState()
    val isLoadingFavorites by viewModel.isLoadingFavorites.collectAsState()

    // Observer la navigation depuis la géolocalisation
    LaunchedEffect(Unit) {
        viewModel.navigateToLocation.collect { (cityName, latitude, longitude) ->
            onCityClick(cityName, latitude, longitude)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Météo") },
                actions = {
                    IconButton(onClick = onLocationRequest) {
                        Icon(Icons.Default.LocationOn, contentDescription = "Ma position")
                    }
                    IconButton(onClick = { viewModel.refreshFavoritesWeather() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Rechercher une ville...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Rechercher") },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearSearchResults() }) {
                            Icon(Icons.Default.Clear, contentDescription = "Effacer")
                        }
                    }
                },
                singleLine = true,
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { viewModel.searchCities(searchQuery) }
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                )
            )

            searchError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            if (isSearching) {
                Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            if (searchResults.isNotEmpty()) {
                Text(
                    "Résultats de recherche",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn {
                    items(searchResults) { result ->
                        SearchResultItem(
                            result = result,
                            onClick = {
                                onCityClick(result.name, result.latitude, result.longitude)
                                viewModel.clearSearchResults()
                            },
                            onAddToFavorites = { viewModel.addToFavorites(result) }
                        )
                    }
                }
            } else if (searchQuery.isEmpty()) {
                if (favoriteCities.isEmpty()) {
                    EmptyFavoritesList(Modifier.fillMaxSize().padding(16.dp))
                } else {
                    Text(
                        "Mes villes favorites",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    if (isLoadingFavorites) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else {
                        LazyColumn {
                            items(favoriteCities) { favorite ->
                                FavoriteCityItem(
                                    favorite = favorite,
                                    weather = favoritesWeather[favorite.cityKey],
                                    onClick = {
                                        onCityClick(favorite.cityName, favorite.latitude, favorite.longitude)
                                    },
                                    onRemove = {
                                        viewModel.removeFromFavorites(favorite.latitude, favorite.longitude)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SearchResultItem(
    result: GeocodingResult,
    onClick: () -> Unit,
    onAddToFavorites: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(result.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                result.country?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
            IconButton(onClick = onAddToFavorites) {
                Icon(Icons.Default.Star, contentDescription = "Ajouter aux favoris")
            }
        }
    }
}

@Composable
fun FavoriteCityItem(
    favorite: FavoriteCityEntity,
    weather: Weather?,
    onClick: () -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(favorite.cityName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                favorite.country?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                weather?.let {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("${it.currentTemperature.toInt()}°C", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                        Icon(getWeatherIcon(it.weatherCondition), contentDescription = it.weatherCondition.name)
                    }
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Supprimer", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun EmptyFavoritesList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Outlined.StarBorder, contentDescription = null, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Aucune ville favorite", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Recherchez une ville et ajoutez-la aux favoris", style = MaterialTheme.typography.bodyMedium)
    }
}

fun getWeatherIcon(condition: WeatherCondition) = when (condition) {
    WeatherCondition.SUNNY -> Icons.Outlined.WbSunny
    WeatherCondition.CLOUDY -> Icons.Outlined.Cloud
    WeatherCondition.RAINY -> Icons.Outlined.WaterDrop
    WeatherCondition.UNKNOWN -> Icons.Outlined.Help
}
