package com.vegas.geo.ui.screens.scanner

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vegas.geo.ui.navigation.AppNavigationDestination

object ScannerDestination : AppNavigationDestination {
    override val route = "scanner_route"
    override val destination = "scanner_destination"

    val fullRoute = "${route}"

    override fun routeTo() = "${route}"
}


fun NavGraphBuilder.scannerScreen(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToLocation: () -> Unit,
    navigateToTracks: () -> Unit,
    trackingEnabled: Boolean,
) {
    composable(
        route = ScannerDestination.fullRoute,

        ) {

        ScannerRoute(
            navigateBack = navigateBack,
            navigateToHome = navigateToHome,
            navigateToSettings = navigateToSettings,
            navigateToLocation = navigateToLocation,
            navigateToTracks = navigateToTracks,
            trackingEnabled = trackingEnabled,
        )
    }
}