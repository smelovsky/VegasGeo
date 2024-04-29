package com.vegas.geo.ui.screens.settings

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.vegas.geo.ui.navigation.AppNavigationDestination

object SettingsDestination : AppNavigationDestination {
    override val route = "settings_route"
    override val destination = "settings_destination"

    val fullRoute = "${route}"

    override fun routeTo() = "${route}"
}


fun NavGraphBuilder.settingsScreen(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToScanner: () -> Unit,
    navigateToLocation: () -> Unit,
    navigateToTracks: () -> Unit,
    trackingEnabled: Boolean,
) {
    composable(
        route = SettingsDestination.fullRoute,

        ) {

        SettingsRoute(
            navigateBack = navigateBack,
            navigateToHome = navigateToHome,
            navigateToScanner = navigateToScanner,
            navigateToLocation = navigateToLocation,
            navigateToTracks = navigateToTracks,
            trackingEnabled = trackingEnabled,
        )
    }
}