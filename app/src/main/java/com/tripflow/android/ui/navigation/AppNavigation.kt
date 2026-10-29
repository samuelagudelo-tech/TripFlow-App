package com.tripflow.android.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tripflow.android.ui.auth.LoginScreen
import com.tripflow.android.ui.auth.RegisterScreen
import com.tripflow.android.ui.dashboard.DashboardScreen
import com.tripflow.android.ui.expenses.ExpensesScreen
import com.tripflow.android.ui.expenses.NewExpenseScreen
import com.tripflow.android.ui.theme.BrandLight
import com.tripflow.android.ui.theme.BrandPrimary
import com.tripflow.android.ui.theme.TextSecondary
import com.tripflow.android.ui.trips.NewTripScreen
import com.tripflow.android.ui.trips.TripsScreen

@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Dashboard.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in listOf(
        Screen.Dashboard.route,
        Screen.Trips.route,
        Screen.Expenses.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = Color.White,
                    tonalElevation = 8.dp
                ) {
                    bottomNavItems.forEach { screen ->
                        val selected = currentRoute == screen.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                screen.icon?.let { icon ->
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = screen.title
                                    )
                                }
                            },
                            label = { Text(screen.title) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = BrandPrimary,
                                selectedTextColor = BrandPrimary,
                                indicatorColor = BrandLight,
                                unselectedIconColor = TextSecondary,
                                unselectedTextColor = TextSecondary
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Auth Module (Integrante 2)
            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = { navController.popBackStack() },
                    onRegisterSuccess = {
                        navController.navigate(Screen.Dashboard.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                )
            }

            // Dashboard Module (Integrante 5)
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    onNavigateToNewTrip = { navController.navigate(Screen.NewTrip.route) },
                    onNavigateToNewExpense = { navController.navigate(Screen.NewExpense.route) }
                )
            }

            // Trips Module (Integrante 3)
            composable(Screen.Trips.route) {
                TripsScreen(
                    onNavigateToNewTrip = { navController.navigate(Screen.NewTrip.route) }
                )
            }
            composable(Screen.NewTrip.route) {
                NewTripScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            // Expenses Module (Integrante 4)
            composable(Screen.Expenses.route) {
                ExpensesScreen(
                    onNavigateToNewExpense = { navController.navigate(Screen.NewExpense.route) }
                )
            }
            composable(Screen.NewExpense.route) {
                NewExpenseScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
