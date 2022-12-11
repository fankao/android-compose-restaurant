package com.example.android_compose_restaurant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.example.android_compose_restaurant.ui.theme.RestaurantsAppTheme

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
                RestaurantsScreen() { id ->
                    navController.navigate("restaurants/$id")
                }
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