package com.example.android_compose_restaurant

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ConnectException
import java.net.UnknownHostException

class RestaurantsViewModel(private val stateHandle: SavedStateHandle) : ViewModel() {
    private var restInterface: RestaurantsApiService
    private var restaurantDao = RestaurantsDb.getDaoInstance(RestaurantsApplication.getAppContext())
    val state = mutableStateOf(emptyList<Restaurant>())
    private val errorHandler = CoroutineExceptionHandler{
            _,exception -> exception.printStackTrace()
    }
    init {
        val retrofit: Retrofit = Retrofit.Builder()
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .baseUrl("https://android-compose-restaurants-db-default-rtdb.firebaseio.com/")
            .build()
        restInterface = retrofit.create(RestaurantsApiService::class.java)
        getRestaurants()
    }

    fun toggleFavorite(id: Int) {
        val restaurants = state.value.toMutableList();
        val itemIndex = restaurants.indexOfFirst { it.id == id }
        val item = restaurants[itemIndex]
        restaurants[itemIndex] = item.copy(isFavorite = !item.isFavorite)
        storeSelection(restaurants[itemIndex])
        viewModelScope.launch(errorHandler) {
            val updateRestaurants = toggleFavoriteRestaurant(id, item.isFavorite)
            state.value = restaurants
        }
    }

    private suspend fun toggleFavoriteRestaurant(id: Int, oldValue: Boolean) =
        withContext(Dispatchers.IO) {
            restaurantDao.update(
                PartialRestaurant(id = id, isFavorite = !oldValue)
            )
            restaurantDao.getAll()
        }


    /**
     *
    <ul>
    <li>I. We've obtained a Call object called Call<List<Restaurant>> from
    our Retrofit restInterface variable by calling the getRestaurants()
    interface method. The Call object represents the invocation of a Retrofit method
    that sends network requests and receives a response. The type parameter of the
    Call object matches the response type; that is, <List<Restaurant>>.</li>
    <li>II. On the previously obtained Call object, we called execute(). The
    execute() method is the most simple approach to starting a network request
    with Retrofit as it runs the request synchronously on the main thread (the UI
    thread) and blocks it until the response arrives. No network request should block
    the UI thread yet, though we will fix this soon.</li>
    <li>III.The execute() method returns a Retrofit Response object that allows us to
    see if the response was successful and obtain the resulting body.</li>\
    <li>IV. The body() accessor returns a nullable list of type List<Restaurant>>?.
    We apply the Kotlin let extension function and name the list restaurants.</li>
    <li>V. We pass the resulting restaurants list to our state object after restoring the
    selections in case of system-initiated process death, similar to what we did for the
    initial state value</li>
    </ul>
     */
    private fun getRestaurants() {
        viewModelScope.launch(errorHandler) {
            val restaurants = getAllRestaurants()
            state.value = restaurants.restoreSelections()
        }
    }
    private suspend fun getAllRestaurants(): List<Restaurant> {
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

    private fun storeSelection(restaurant: Restaurant) {
        val saveToggled = stateHandle.get<List<Int>?>(FAVORITES)
            .orEmpty().toMutableList()
        if(restaurant.isFavorite) saveToggled.add(restaurant.id)
        else saveToggled.remove(restaurant.id)
        stateHandle[FAVORITES] = saveToggled
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
        stateHandle.get<List<Int>?>(FAVORITES)?.let { selectedIds ->
            val restaurantsMap = this.associateBy { it.id }.toMutableMap()
            selectedIds.forEach { id ->
                val restaurant = restaurantsMap[id] ?: return@forEach
                restaurantsMap[id] = restaurant.copy(isFavorite = true)
            }
            return restaurantsMap.values.toList()
        }
        return this
    }
}

