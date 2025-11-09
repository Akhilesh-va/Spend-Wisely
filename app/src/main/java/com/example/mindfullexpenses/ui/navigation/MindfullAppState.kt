package com.example.mindfullexpenses.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

@Stable
class MindfullAppState(
    val navController: NavHostController
) {
    val destinations: List<MainDestination> = MainDestination.entries

    val currentDestination: NavDestination?
        get() = navController.currentDestination

    fun navigateTo(destination: MainDestination) {
        if (destination.route == currentDestination?.route) return
        navController.navigate(destination.route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
        }
    }
}

@Stable
fun MindfullAppState.isDestinationSelected(destination: MainDestination): Boolean {
    return currentDestination?.route == destination.route
}

@Composable
fun rememberMindfullAppState(
    navController: NavHostController = rememberNavController()
): MindfullAppState = remember(navController) {
    MindfullAppState(navController)
}


