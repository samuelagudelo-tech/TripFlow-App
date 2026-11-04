package com.tripflow.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.tripflow.android.data.preferences.UserPreferencesManager
import com.tripflow.android.ui.navigation.AppNavigation
import com.tripflow.android.ui.navigation.Screen
import com.tripflow.android.ui.theme.TripFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val preferencesManager = UserPreferencesManager.getInstance(applicationContext)
        setContent {
            val isDarkMode by preferencesManager.isDarkMode.collectAsState()
            TripFlowTheme(darkTheme = isDarkMode) {
                AppNavigation(startDestination = Screen.Login.route)
            }
        }
    }
}
