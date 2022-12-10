package com.example.android_compose_restaurant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.android_compose_restaurant.ui.theme.RestaurantsAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RestaurantsAppTheme {
               RestaurantsScreen()
            }
        }
    }
}