package com.vegas.geo.ui.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vegas.geo.ui.screens.home.HomeDestination
import com.vegas.geo.ui.screens.home.HomeRoute
import com.vegas.geo.ui.screens.home.homeScreen
import com.vegas.geo.ui.screens.location.LocationDestination
import com.vegas.geo.ui.screens.location.LocationScreen
import com.vegas.geo.ui.screens.location.locationScreen
import com.vegas.geo.ui.screens.permissions.PermissionsDestination
import com.vegas.geo.ui.screens.permissions.PermissionsRoute
import com.vegas.geo.ui.screens.scanner.ScannerDestination
import com.vegas.geo.ui.screens.scanner.scannerScreen
import com.vegas.geo.ui.screens.settings.SettingsDestination
import com.vegas.geo.ui.screens.settings.settingsScreen
import com.vegas.geo.ui.screens.tracks.TracksDestination
import com.vegas.geo.ui.screens.tracks.tracksScreen


@ExperimentalComposeUiApi
@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = HomeDestination.route,
    activity: Activity,
    onBackPressed: () -> Unit,
    enableTracking: (enable: Boolean) -> Unit,
    trackingEnabled: Boolean,
) {

    val navigateBack: () -> Unit = {
        onBackPressed()
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
    ) {

        composable(route = PermissionsDestination.route) {
            PermissionsRoute(
                activity = activity,
            )
        }

        composable(route = HomeDestination.route) {
            HomeRoute(
                navigateBack = navigateBack,
                navigateToSettings = { navController.switchTo(SettingsDestination) },
                navigateToScanner = { navController.switchTo(ScannerDestination) },
                navigateToLocation = { navController.switchTo(LocationDestination) },
                navigateToTracks = { navController.switchTo(TracksDestination) },
                trackingEnabled = trackingEnabled,
            )
        }
        homeScreen(
            navigateBack = navigateBack,
            navigateToSettings = { navController.switchTo(SettingsDestination) },
            navigateToScanner = { navController.switchTo(ScannerDestination) },
            navigateToLocation = { navController.switchTo(LocationDestination) },
            navigateToTracks = { navController.switchTo(TracksDestination) },
            trackingEnabled = trackingEnabled,
        )
        settingsScreen(
            navigateBack = navigateBack,
            navigateToHome = { navController.switchTo(HomeDestination) },
            navigateToScanner = { navController.switchTo(ScannerDestination) },
            navigateToLocation = { navController.switchTo(LocationDestination) },
            navigateToTracks = { navController.switchTo(TracksDestination) },
            trackingEnabled = trackingEnabled,
        )
        scannerScreen(
            navigateBack = navigateBack,
            navigateToHome = { navController.switchTo(HomeDestination) },
            navigateToSettings = { navController.switchTo(SettingsDestination) },
            navigateToLocation = { navController.switchTo(LocationDestination) },
            navigateToTracks = { navController.switchTo(TracksDestination) },
            trackingEnabled = trackingEnabled,
        )
        locationScreen(
            navigateBack = navigateBack,
            enableTracking = enableTracking,
            navigateToHome = { navController.switchTo(HomeDestination) },
            navigateToSettings = { navController.switchTo(SettingsDestination) },
            navigateToScanner = { navController.switchTo(ScannerDestination) },
            navigateToTracks = { navController.switchTo(TracksDestination) },
            trackingEnabled = trackingEnabled,
        )
        tracksScreen(
            navigateBack = navigateBack,
            navigateToHome = { navController.switchTo(HomeDestination) },
            navigateToSettings = { navController.switchTo(SettingsDestination) },
            navigateToScanner = { navController.switchTo(ScannerDestination) },
            navigateToLocation = { navController.switchTo(LocationDestination) },
            trackingEnabled = trackingEnabled,
        )


    }
}

fun NavController.switchTo(
    destination: AppNavigationDestination,
) = navigate(route = destination.routeTo())