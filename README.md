# Application Météo Android

Application Android de consultation météo développée avec Jetpack Compose et architecture MVVM.

## Architecture

### Structure du projet

```
com.example.tp_android/
├── MainActivity.kt          # Point d'entrée et injection de dépendances
├── data/                    # Couche données
│   ├── datasource/          # Sources de données (API + BDD)
│   ├── local/               # Base de données Room
│   ├── model/               # Modèles de données
│   ├── remote/              # Clients API Retrofit
│   └── repository/          # Pattern Repository
└── ui/                      # Couche présentation
    ├── navigation/          # Navigation Compose
    ├── screen/              # Écrans Composables
    ├── theme/               # Thème Material 3
    └── viewmodel/           # ViewModels MVVM
```

### Pattern architectural : Clean Architecture + MVVM

```
┌─────────────────────────────────────────────┐
│  UI Layer (Jetpack Compose)                 │
│  - HomeScreen, DetailScreen                 │
└─────────────────┬───────────────────────────┘
                  ↓
┌─────────────────────────────────────────────┐
│  ViewModels (State Management)              │
│  - HomeViewModel, DetailViewModel           │
└─────────────────┬───────────────────────────┘
                  ↓
┌─────────────────────────────────────────────┐
│  Repositories (Business Logic)              │
│  - WeatherRepository, FavoritesRepository   │
└─────────────┬──────────────┬────────────────┘
              ↓              ↓
    ┌─────────────┐   ┌──────────────┐
    │ Remote DS   │   │  Local DS    │
    │ (Retrofit)  │   │  (Room)      │
    └─────────────┘   └──────────────┘
```

## Fonctionnalités

- **Recherche de ville** : API de géocodage Open-Meteo
- **Affichage météo détaillé** : Température, humidité, vent, conditions
- **Villes favorites** : Sauvegarde locale avec Room
- **Géolocalisation** : Obtenir la météo de sa position actuelle
- **Cache intelligent** : Stratégie cache-first avec validité 1h
- **Mode hors-ligne** : Utilisation des données en cache si pas de réseau
- **Actualisation manuelle** : Forcer la récupération des données

## Couche Données

### APIs (Open-Meteo)

#### Géocodage

```
GET https://geocoding-api.open-meteo.com/v1/search
Paramètres : name, count, language=fr
```

#### Météo

```
GET https://api.open-meteo.com/v1/forecast
Paramètres : latitude, longitude, hourly=temperature_2m,humidity,rain,wind_speed_10m
Modèle : meteofrance_seamless
```

### Base de données Room

**Tables :**

- `weather_cache` : Cache des données météo (clé : `"lat_lon"`, validité 1h)
- `favorite_cities` : Liste des villes favorites

**Stratégie de cache :**

1. Vérifier si cache valide (< 1h)
2. Si oui → retourner données en cache
3. Si non → appel API → sauvegarde en cache
4. En cas d'erreur réseau → utiliser cache expiré en fallback

### Modèles principaux

- **Weather** : Modèle domaine simplifié (ville, températures, conditions)
- **WeatherCondition** : Enum (SUNNY, CLOUDY, RAINY, UNKNOWN)
- **WeatherApiResponse** : Mapping de la réponse API Open-Meteo
- **GeocodingApiResponse** : Résultats de recherche de ville

## Couche UI

### Écrans Composables

#### HomeScreen

- Barre de recherche de ville
- Liste des résultats avec bouton "Ajouter aux favoris"
- Section favorites avec aperçu météo
- Bouton géolocalisation dans la barre d'app
- Bouton refresh pour les favoris

#### DetailScreen

- Affichage météo détaillé
- Icône condition météo (Material Icons)
- Températures min/max
- Humidité et vitesse du vent
- Horodatage de mise à jour
- Toggle favori
- Bouton actualisation

### ViewModels

#### HomeViewModel

- Gestion de la recherche (`searchCities`)
- Liste des favoris avec météo
- Géolocalisation (`loadWeatherForCurrentLocation`)
- Ajout/suppression de favoris
- Actualisation des favoris

#### DetailViewModel

- Chargement météo détaillée
- Actualisation forcée (bypass cache)
- Toggle statut favori
- Gestion états (loading, error, success)

### Navigation

Navigation Compose avec 2 écrans :

- `Home` : Écran d'accueil
- `Detail/{cityName}/{lat}/{lon}` : Détails météo avec paramètres

## Technologies utilisées

### Core

- **Kotlin** : Langage principal
- **Jetpack Compose** : UI déclarative
- **Material 3** : Design system

### Architecture & State

- **ViewModel** : Gestion d'état
- **Coroutines** : Programmation asynchrone
- **Flow** : Streams réactifs

### Réseau

- **Retrofit** : Client HTTP
- **OkHttp** : Intercepteurs et logging
- **Gson** : Sérialisation JSON

### Persistance

- **Room** : Base de données SQLite
- **DataStore** : Préférences

### Localisation

- **Play Services Location** : Géolocalisation

## Configuration

### Permissions (AndroidManifest.xml)

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

### Build

- **Target SDK** : 36 (Android 15)
- **Min SDK** : 27 (Android 8.1)
- **Kotlin** : 1.x avec support Compose
- **Build Tools** : KAPT pour Room

## Flux de données

### Exemple : Chargement météo

```
1. User clique sur ville → DetailScreen
2. DetailViewModel.loadWeather(lat, lon)
3. WeatherRepository.getWeatherData()
   ├─→ Cache valide ? → Retourne données
   └─→ Sinon :
       ├─→ WeatherRemoteDataSource.getWeatherData() (API)
       ├─→ Conversion WeatherApiResponse → Weather
       ├─→ WeatherLocalDataSource.cacheWeather() (Room)
       └─→ Retourne Weather
4. ViewModel met à jour StateFlow
5. DetailScreen recompose avec nouvelles données
```

## Gestion d'état réactive

- **StateFlow** : État UI (query, results, loading, weather)
- **Flow** : Observation continue des favoris (Room)
- **collectAsState()** : Collection dans Composables

## Injection de dépendances

Injection manuelle dans [MainActivity.kt](app/src/main/java/com/example/tp_android/MainActivity.kt) :

```kotlin
// Database
val database = AppDatabase.getInstance(this)

// Data Sources
val remoteDataSource = WeatherRemoteDataSource(...)
val localDataSource = WeatherLocalDataSource(...)

// Repositories
val weatherRepo = WeatherRepository(...)
val favoritesRepo = FavoritesRepository(...)

// ViewModels via Factory
val homeViewModel = viewModel<HomeViewModel>(factory = ...)
val detailViewModel = viewModel<DetailViewModel>(factory = ...)
```

## Installation

1. Cloner le repository
2. Ouvrir dans Android Studio
3. Sync Gradle
4. Lancer sur émulateur ou appareil (API 27+)

Badre El Mourabit - M2DWM
