package com.vegas.geo.ui.screens.home

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vegas.geo.ui.navigation.AppNavigationDestination

object HomeDestination : AppNavigationDestination {
    override val route = "home_route"
    override val destination = "home_destination"

    val fullRoute = "${route}"

    override fun routeTo() = "${route}"
}


fun NavGraphBuilder.homeScreen(
    navigateBack: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToScanner: () -> Unit,
    navigateToLocation: () -> Unit,
    navigateToTracks: () -> Unit,
    trackingEnabled: Boolean,
) {
    composable(
        route = HomeDestination.fullRoute,
    ) {

        HomeRoute(
            navigateBack = navigateBack,
            navigateToSettings = navigateToSettings,
            navigateToScanner = navigateToScanner,
            navigateToLocation = navigateToLocation,
            navigateToTracks = navigateToTracks,
            trackingEnabled = trackingEnabled,
        )
    }
}