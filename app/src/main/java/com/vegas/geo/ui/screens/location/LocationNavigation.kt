package com.vegas.geo.ui.screens.location

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vegas.geo.ui.navigation.AppNavigationDestination

object LocationDestination : AppNavigationDestination {
    override val route = "location_route"
    override val destination = "location_destination"

    val fullRoute = "${route}"

    override fun routeTo() = "${route}"
}


fun NavGraphBuilder.locationScreen(
    navigateBack: () -> Unit,
    enableTracking: (enable: Boolean) -> Unit,
    navigateToHome: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToScanner: () -> Unit,
    navigateToTracks: () -> Unit,
    trackingEnabled: Boolean,
) {
    composable(
        route = LocationDestination.fullRoute,
    ) {

        LocationRoute(
            navigateBack = navigateBack,
            enableTracking = enableTracking,
            navigateToHome = navigateToHome,
            navigateToSettings = navigateToSettings,
            navigateToScanner = navigateToScanner,
            navigateToTracks = navigateToTracks,
            trackingEnabled = trackingEnabled,
        )
    }
}