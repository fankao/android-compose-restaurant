package com.example.android_compose_restaurant

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
            RestaurantsApplication.getAppContext())

    suspend fun toggleFavoriteRestaurant(id: Int, oldValue: Boolean) =
        withContext(Dispatchers.IO) {
            restaurantDao.update(
                PartialRestaurant(id = id, isFavorite = !oldValue)
            )
            restaurantDao.getAll()
        }

    suspend fun getAllRestaurants(): List<Restaurant> {
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
            return@withContext restaurantDao.getAll()
        }
    }

    private suspend fun refreshCache() {
        var remoteRestaurants = restInterface.getRestaurants()
        val favoriteRestaurants = restaurantDao.getAllFavorited()
        restaurantDao.addAll(remoteRestaurants)
        restaurantDao.updateAll(
            favoriteRestaurants.map{
                PartialRestaurant(it.id,true)
            }
        )
    }
}