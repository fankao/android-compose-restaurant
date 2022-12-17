package com.example.android_compose_restaurant.restaurants.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.android_compose_restaurant.restaurants.presentation.details.RestaurantDetailsScreen
import com.example.android_compose_restaurant.restaurants.presentation.list.RestaurantsScreen
import com.example.android_compose_restaurant.restaurants.presentation.list.RestaurantsViewModel
import com.example.android_compose_restaurant.ui.theme.RestaurantsAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RestaurantsAppTheme {
               RestaurantsApp()
            }
        }
    }

    @Composable
    private fun RestaurantsApp() {
        var navController = rememberNavController()
        NavHost(navController = navController, startDestination = "restaurants") {
            composable(route = "restaurants") {
                val viewModel: RestaurantsViewModel = hiltViewModel()
                RestaurantsScreen(
                    state = viewModel.state.value,
                    onItemClick = {
                        id -> navController.navigate("restaurants/$id")
                    },
                    onFavoriteClick = { id, oldValue ->
                        viewModel.toggleFavorite(id,oldValue)
                    }
                )
            }
            composable(
                route = "restaurants/{restaurant_id}",
                arguments =
                listOf(navArgument("restaurant_id") {
                    type = NavType.IntType
                }),
                deepLinks = listOf(navDeepLink {
                    uriPattern = "www.restaurantsapp.details.com/{restaurant_id}"
                })
            ) { navStackEntry ->
                val id = navStackEntry.arguments?.getInt("restaurant_id")
                RestaurantDetailsScreen()
            }
        }
    }
}