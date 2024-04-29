package com.vegas.geo.ui.screens.tracks

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vegas.geo.ui.navigation.AppNavigationDestination

object TracksDestination : AppNavigationDestination {
    override val route = "tracks_route"
    override val destination = "tracks_destination"

    val fullRoute = "${route}"

    override fun routeTo() = "${route}"
}


fun NavGraphBuilder.tracksScreen(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToScanner: () -> Unit,
    navigateToLocation: () -> Unit,
    trackingEnabled: Boolean,
) {
    composable(
        route = TracksDestination.fullRoute,
    ) {

        TracksRoute(
            navigateBack = navigateBack,
            navigateToHome = navigateToHome,
            navigateToSettings = navigateToSettings,
            navigateToScanner = navigateToScanner,
            navigateToLocation = navigateToLocation,
            trackingEnabled = trackingEnabled,
        )
    }
}