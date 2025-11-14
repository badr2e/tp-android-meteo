package com.example.tp_android

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.tp_android.data.datasource.WeatherLocalDataSource
import com.example.tp_android.data.datasource.WeatherRemoteDataSource
import com.example.tp_android.data.local.AppDatabase
import com.example.tp_android.data.repository.FavoritesRepository
import com.example.tp_android.data.repository.WeatherRepository
import com.example.tp_android.ui.navigation.NavGraph
import com.example.tp_android.ui.theme.TpandroidTheme
import com.example.tp_android.ui.viewmodel.DetailViewModel
import com.example.tp_android.ui.viewmodel.DetailViewModelFactory
import com.example.tp_android.ui.viewmodel.HomeViewModel
import com.example.tp_android.ui.viewmodel.HomeViewModelFactory
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var homeViewModel: HomeViewModel
    private lateinit var detailViewModel: DetailViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true -> {
                requestCurrentLocation()
            }
            else -> {
                Toast.makeText(
                    this,
                    "Permission de localisation refusée",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialisation de la base de données
        val database = AppDatabase.getDatabase(applicationContext)

        // Initialisation des DataSources
        val weatherRemoteDataSource = WeatherRemoteDataSource()
        val weatherLocalDataSource = WeatherLocalDataSource(database.weatherDao())

        // Initialisation des Repositories
        val weatherRepository = WeatherRepository(weatherRemoteDataSource, weatherLocalDataSource)
        val favoritesRepository = FavoritesRepository(database.favoriteCityDao())

        // Initialisation des ViewModels avec Factories
        val homeViewModelFactory = HomeViewModelFactory(weatherRepository, favoritesRepository)
        homeViewModel = ViewModelProvider(this, homeViewModelFactory)[HomeViewModel::class.java]

        val detailViewModelFactory = DetailViewModelFactory(weatherRepository, favoritesRepository)
        detailViewModel = ViewModelProvider(this, detailViewModelFactory)[DetailViewModel::class.java]

        // Initialisation du client de localisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setContent {
            TpandroidTheme {
                val navController = rememberNavController()

                NavGraph(
                    navController = navController,
                    homeViewModel = homeViewModel,
                    detailViewModel = detailViewModel,
                    onLocationRequest = { requestLocationPermission() },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    private fun requestLocationPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                requestCurrentLocation()
            }
            else -> {
                locationPermissionRequest.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }
        }
    }

    private fun requestCurrentLocation() {
        try {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        CoroutineScope(Dispatchers.Main).launch {
                            homeViewModel.searchCities("${location.latitude},${location.longitude}")
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Impossible d'obtenir la position",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                "Erreur lors de la récupération de la position",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}