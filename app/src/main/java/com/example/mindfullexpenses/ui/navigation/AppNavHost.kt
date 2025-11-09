package com.example.mindfullexpenses.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.mindfullexpenses.ui.screens.dashboard.DashboardScreen
import com.example.mindfullexpenses.ui.screens.manual.ManualEntryScreen
import com.example.mindfullexpenses.ui.screens.reports.ReportsScreen

@Composable
fun AppNavHost(
    appState: MindfullAppState,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = appState.navController,
        startDestination = MainDestination.Dashboard.route,
        modifier = modifier
    ) {
        composable(MainDestination.Dashboard.route) {
            DashboardScreen()
        }
        composable(MainDestination.ManualEntry.route) {
            ManualEntryScreen()
        }
        composable(MainDestination.Reports.route) {
            ReportsScreen()
        }
    }
}


