package com.tripflow.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.tripflow.android.ui.navigation.AppNavigation
import com.tripflow.android.ui.theme.TripFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TripFlowTheme {
                AppNavigation()
            }
        }
    }
}
