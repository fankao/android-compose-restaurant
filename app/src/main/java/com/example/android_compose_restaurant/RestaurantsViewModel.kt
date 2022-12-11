package com.example.android_compose_restaurant

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RestaurantsViewModel(private val stateHandle: SavedStateHandle) : ViewModel() {
    private var restInterface: RestaurantsApiService
    private lateinit var restaurantsCall: Call<List<Restaurant>>
    val state = mutableStateOf(emptyList<Restaurant>())
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
        state.value = restaurants
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
        restaurantsCall = restInterface.getRestaurants()
        restInterface.getRestaurants().enqueue(
            object : Callback<List<Restaurant>> {
                override fun onResponse(
                    call: Call<List<Restaurant>>,
                    response: Response<List<Restaurant>>
                ) {
                    response.body()?.let { restaurants ->
                        state.value = restaurants.restoreSelections()
                    }
                }

                override fun onFailure(call: Call<List<Restaurant>>, t: Throwable) {
                    t.printStackTrace()
                }
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

    override fun onCleared() {
        super.onCleared()
        restaurantsCall.cancel()
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
            val restaurantsMap = this.associateBy { it.id }
            selectedIds.forEach { id ->
                restaurantsMap[id]?.isFavorite = true
            }
            return restaurantsMap.values.toList()
        }
        return this
    }
}

