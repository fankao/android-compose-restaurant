package com.example.android_compose_restaurant

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

class RestaurantsViewModel(private val stateHandle: SavedStateHandle): ViewModel(){
    val state = mutableStateOf(dummyRestaurants.restoreSelections())
    fun getRestaurants() = dummyRestaurants
    fun toggleFavorite(id: Int) {
        val restaurants = state.value.toMutableList();
        val itemIndex = restaurants.indexOfFirst { it.id == id  }
        val item = restaurants[itemIndex]
        restaurants[itemIndex] = item.copy(isFavorite = !item.isFavorite)
        storeSelection(restaurants[itemIndex])
        state.value = restaurants
    }

    private fun storeSelection(restaurant: Restaurant) {
        val saveToggled = stateHandle
            .get<List<Int>?>(FAVORITES)
    }
    companion object {
        const val FAVORITES = "favorites"
    }

    /**
     I. First, by obtaining the list with the unique identifiers of the previously favoritedrestaurants from stateHandle by accessing the FAVORITES key inside the
        map. If the list is not null, this means that a process death occurred, and it
        references the list as selectedIds; otherwise, it will return the list without any
        modifications.
    II. Then, by creating a map from the input list of restaurants with the key being the
        id value of the restaurant and the value the Restaurant object itself.
    III.By iterating over the unique identifiers of the favorited restaurants and for
        each of them, by trying to access the respective restaurant from our new list and
        sets its isFavorite value to true.
     IV. By returning the modified restaurants list from restaurantMap. This list
        should now contain the restored isFavorite values from before the death
        process occurred.
     */
    private fun List<Restaurant>.restoreSelections(): List<Restaurant> {
        stateHandle.get<List<Int>?>(FAVORITES)?.let {
            selectedIds -> val restaurantsMap = this.associateBy { it.id }
            selectedIds.forEach{
                id -> restaurantsMap[id]?.isFavorite = true
            }
            return restaurantsMap.values.toList()
        }
        return this
    }
}

