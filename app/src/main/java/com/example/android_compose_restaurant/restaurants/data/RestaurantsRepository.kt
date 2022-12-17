package com.example.android_compose_restaurant.restaurants.data

import com.example.android_compose_restaurant.RestaurantsApplication
import com.example.android_compose_restaurant.restaurants.data.local.LocalRestaurant
import com.example.android_compose_restaurant.restaurants.data.local.PartialLocalRestaurant
import com.example.android_compose_restaurant.restaurants.data.local.RestaurantsDb
import com.example.android_compose_restaurant.restaurants.data.remote.RestaurantsApiService
import com.example.android_compose_restaurant.restaurants.domain.Restaurant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.net.UnknownHostException

class RestaurantsRepository {
    private var restInterface: RestaurantsApiService = Retrofit.Builder()
        .addConverterFactory(
            GsonConverterFactory.create()
        )
        .baseUrl("https://android-compose-restaurants-db-default-rtdb.firebaseio.com/")
        .build()
        .create(RestaurantsApiService::class.java)
    private var restaurantDao = RestaurantsDb
        .getDaoInstance(
            RestaurantsApplication.getAppContext()
        )

    suspend fun toggleFavoriteRestaurant(id: Int, value: Boolean) =
        withContext(Dispatchers.IO) {
            restaurantDao.update(
                PartialLocalRestaurant(id = id, isFavorite = value)
            )
        }

    suspend fun loadRestaurants() {
        return withContext(Dispatchers.IO) {
            try {
                refreshCache()
            } catch (e: Exception) {
                when (e) {
                    is UnknownHostException,
                    is ConnectException,
                    is HttpException -> {
                        if (restaurantDao.getAll().isEmpty()) {
                            throw Exception(
                                "Something went wrong. " +
                                        "We have no data"
                            )
                        }
                    }
                    else -> throw e
                }
            }
        }
    }

    suspend fun getRestaurants() : List<Restaurant> {
        return withContext(Dispatchers.IO) {
            return@withContext restaurantDao.getAll().map {
                Restaurant(it.id,it.title,it.description,it.isFavorite)
            }
        }
    }

    private suspend fun refreshCache() {
        var remoteRestaurants = restInterface.getRestaurants()
        val favoriteRestaurants = restaurantDao.getAllFavorited()
        restaurantDao.addAll(remoteRestaurants.map {
            LocalRestaurant(it.id,it.title,it.description,false)
        })
        restaurantDao.updateAll(
            favoriteRestaurants.map{
                PartialLocalRestaurant(it.id,true)
            }
        )
    }
}