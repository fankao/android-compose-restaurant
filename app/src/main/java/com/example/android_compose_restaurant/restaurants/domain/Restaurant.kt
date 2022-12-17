package com.example.android_compose_restaurant.restaurants.domain

data class Restaurant(
    val id: Int,
    val title: String,
    val description: String,
    val isFavorite: Boolean = false
)