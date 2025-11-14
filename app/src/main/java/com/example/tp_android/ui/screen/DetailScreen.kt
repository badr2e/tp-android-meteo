package com.example.tp_android.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.tp_android.data.model.Weather
import com.example.tp_android.data.model.WeatherCondition
import com.example.tp_android.ui.viewmodel.DetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    viewModel: DetailViewModel,
    cityName: String,
    latitude: Double,
    longitude: Double,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val weather by viewModel.weather.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val error by viewModel.error.collectAsState()
    val isFavorite by viewModel.isFavorite.collectAsState()

    LaunchedEffect(cityName, latitude, longitude) {
        viewModel.loadWeather(cityName, latitude, longitude)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(cityName) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Retour")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleFavorite() }) {
                        Icon(
                            if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (isFavorite) "Retirer des favoris" else "Ajouter aux favoris"
                        )
                    }
                    IconButton(onClick = { viewModel.refreshWeather() }, enabled = !isRefreshing) {
                        Icon(Icons.Default.Refresh, contentDescription = "Rafraîchir")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(modifier = modifier.fillMaxSize().padding(paddingValues)) {
            when {
                isLoading && weather == null -> LoadingView()
                error != null && weather == null -> ErrorView(
                    message = error ?: "Erreur inconnue",
                    onRetry = { viewModel.loadWeather(cityName, latitude, longitude, forceRefresh = true) }
                )
                weather != null -> WeatherContent(weather!!, isRefreshing, error)
            }
        }
    }
}

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Chargement des données...", style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Composable
fun ErrorView(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Icon(Icons.Default.Error, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Erreur", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
            Text(message, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onRetry) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Réessayer")
            }
        }
    }
}

@Composable
fun WeatherContent(weather: Weather, isRefreshing: Boolean, error: String?, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        error?.let {
            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        if (isRefreshing) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp))
        }

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(getWeatherIcon(weather.weatherCondition), contentDescription = weather.weatherCondition.name, modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("${weather.currentTemperature.toInt()}°C", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(getWeatherConditionText(weather.weatherCondition), style = MaterialTheme.typography.titleMedium)
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                WeatherDetailItem(Icons.Default.ArrowDownward, "Min", "${weather.minTemperature.toInt()}°C")
                Divider(modifier = Modifier.height(60.dp).width(1.dp))
                WeatherDetailItem(Icons.Default.ArrowUpward, "Max", "${weather.maxTemperature.toInt()}°C")
            }
        }

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Détails", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 16.dp))
                WeatherDetailRow(Icons.Default.Water, "Humidité", "${weather.humidity}%")
                Spacer(modifier = Modifier.height(12.dp))
                WeatherDetailRow(Icons.Default.Air, "Vent", "${weather.windSpeed} km/h")
            }
        }

        Text(
            "Dernière mise à jour: ${formatLastUpdate(weather.lastUpdate)}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
fun WeatherDetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = label)
        Spacer(modifier = Modifier.height(8.dp))
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun WeatherDetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, style = MaterialTheme.typography.bodyLarge)
        }
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
}

fun getWeatherConditionText(condition: WeatherCondition): String = when (condition) {
    WeatherCondition.SUNNY -> "Ensoleillé"
    WeatherCondition.CLOUDY -> "Nuageux"
    WeatherCondition.RAINY -> "Pluvieux"
    WeatherCondition.UNKNOWN -> "Indéterminé"
}

fun formatLastUpdate(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
