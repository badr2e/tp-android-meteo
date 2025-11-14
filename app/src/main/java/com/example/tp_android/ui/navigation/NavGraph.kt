package com.example.tp_android.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tp_android.ui.screen.DetailScreen
import com.example.tp_android.ui.screen.HomeScreen
import com.example.tp_android.ui.viewmodel.DetailViewModel
import com.example.tp_android.ui.viewmodel.HomeViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Detail : Screen("detail/{cityName}/{latitude}/{longitude}") {
        fun createRoute(cityName: String, latitude: Double, longitude: Double): String {
            return "detail/$cityName/$latitude/$longitude"
        }
    }
}

@Composable
fun NavGraph(
    navController: NavHostController,
    homeViewModel: HomeViewModel,
    detailViewModel: DetailViewModel,
    onLocationRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        modifier = modifier
    ) {
        composable(route = Screen.Home.route) {
            HomeScreen(
                viewModel = homeViewModel,
                onCityClick = { cityName, latitude, longitude ->
                    val route = Screen.Detail.createRoute(cityName, latitude, longitude)
                    navController.navigate(route)
                },
                onLocationRequest = onLocationRequest
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(
                navArgument("cityName") { type = NavType.StringType },
                navArgument("latitude") { type = NavType.StringType },
                navArgument("longitude") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val cityName = backStackEntry.arguments?.getString("cityName") ?: ""
            val latitude = backStackEntry.arguments?.getString("latitude")?.toDoubleOrNull() ?: 0.0
            val longitude = backStackEntry.arguments?.getString("longitude")?.toDoubleOrNull() ?: 0.0

            DetailScreen(
                viewModel = detailViewModel,
                cityName = cityName,
                latitude = latitude,
                longitude = longitude,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
