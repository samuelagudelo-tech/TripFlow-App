package com.tripflow.android.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DateRange
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Receipt
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String = "", val icon: ImageVector? = null) {
    // Auth Screens
    object Login : Screen("login", "Iniciar Sesión")
    object Register : Screen("register", "Crear Cuenta")

    // Main BottomNav Screens
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Rounded.Home)
    object Trips : Screen("trips", "Mis Viajes", Icons.Rounded.DateRange)
    object Expenses : Screen("expenses", "Mis Gastos", Icons.Rounded.Receipt)

    // Form Screens
    object NewTrip : Screen("trips/new", "Nuevo Viaje")
    object NewExpense : Screen("expenses/new", "Nuevo Gasto")
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Trips,
    Screen.Expenses
)
