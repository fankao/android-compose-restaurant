package com.example.android_compose_restaurant.restaurants.presentation.list

import com.example.android_compose_restaurant.restaurants.domain.Restaurant

data class RestaurantsScreenState(
    val restaurants: List<Restaurant>,
    val isLoading: Boolean,
    val error: String? = null
)
