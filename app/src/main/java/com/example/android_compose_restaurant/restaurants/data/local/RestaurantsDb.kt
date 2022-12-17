package com.example.android_compose_restaurant.restaurants.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LocalRestaurant::class],
    version = 4,
    exportSchema = false
)
abstract class RestaurantsDb: RoomDatabase(){
    abstract val dao: RestaurantsDao
}