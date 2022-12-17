package com.example.android_compose_restaurant.restaurants.domain

import com.example.android_compose_restaurant.restaurants.data.RestaurantsRepository
import javax.inject.Inject

class GetSortedRestaurantsUseCase @Inject constructor(
    private val repository: RestaurantsRepository
) {
    suspend operator fun invoke(): List<Restaurant>{
        return repository.getRestaurants()
            .sortedBy { it.title }
    }
}