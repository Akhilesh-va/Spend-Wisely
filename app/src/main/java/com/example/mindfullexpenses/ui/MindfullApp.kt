package com.example.mindfullexpenses.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.mindfullexpenses.R
import com.example.mindfullexpenses.ui.navigation.AppNavHost
import com.example.mindfullexpenses.ui.navigation.MainDestination
import com.example.mindfullexpenses.ui.navigation.MindfullAppState
import com.example.mindfullexpenses.ui.navigation.rememberMindfullAppState
import com.example.mindfullexpenses.ui.theme.MindfullExpensesTheme
import kotlinx.coroutines.delay

@Composable
fun MindfullExpensesApp() {
    MindfullExpensesTheme {
        val appState = rememberMindfullAppState()
        val navBackStackEntry by appState.navController.currentBackStackEntryAsState()
        val currentDestination = navBackStackEntry?.destination

        val showSplash = remember { mutableStateOf(true) }
        LaunchedEffect(Unit) {
            delay(2_000)
            showSplash.value = false
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = MaterialTheme.colorScheme.surface,
                bottomBar = {
                    MindfullBottomBar(
                        destinations = appState.destinations,
                        currentDestination = currentDestination,
                        onDestinationSelected = appState::navigateTo
                    )
                }
            ) { innerPadding ->
                AppNavHost(
                    appState = appState,
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AnimatedVisibility(
                visible = showSplash.value,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                SplashOverlay()
            }
        }
    }
}

@Composable
private fun SplashOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo_for_splash),
                contentDescription = null
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(id = R.string.app_tagline),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}

@Composable
private fun MindfullBottomBar(
    destinations: List<MainDestination>,
    currentDestination: NavDestination?,
    onDestinationSelected: (MainDestination) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        destinations.forEach { destination: MainDestination ->
            val selected = currentDestination?.route == destination.route
            NavigationBarItem(
                selected = selected,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                    selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                onClick = { onDestinationSelected(destination) },
                icon = {
                    NavigationBarIcon(
                        destination = destination,
                        selected = selected
                    )
                },
                label = {
                    Text(
                        text = stringResource(id = destination.labelRes),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            )
        }
    }
}

@Composable
private fun NavigationBarIcon(
    destination: MainDestination,
    selected: Boolean
) {
    androidx.compose.material3.Icon(
        imageVector = if (selected) destination.selectedIcon else destination.unselectedIcon,
        contentDescription = null
    )
}


