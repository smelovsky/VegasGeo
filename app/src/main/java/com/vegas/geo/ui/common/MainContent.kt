package com.vegas.geo.ui.common

import android.app.Activity
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.insets.ProvideWindowInsets
import com.vegas.geo.mainViewModel
import com.vegas.geo.ui.navigation.AppNavHost
import com.vegas.geo.ui.screens.home.HomeDestination
import com.vegas.geo.ui.screens.permissions.PermissionsDestination
import com.vegas.geo.ui.theme.VegasGeoTheme

@ExperimentalComposeUiApi
@ExperimentalAnimationApi
@Composable
fun MainContent(
    modifier: Modifier = Modifier,
    darkMode: Boolean,
    activity: Activity,
    onBackPressed: () -> Unit,
    enableTracking: (enable: Boolean) -> Unit,
) {

    val locationViewState = mainViewModel.locationViewState.collectAsState()

    VegasGeoTheme(
        darkTheme = darkMode,
    ) {

        ProvideWindowInsets {

            val navController = rememberNavController()

            AppNavHost(
                navController = navController,
                modifier = modifier.systemBarsPadding(),
                startDestination = if (mainViewModel.permissionsViewState.value.permissionsGranted) HomeDestination.route else PermissionsDestination.route,
                activity = activity,
                onBackPressed = onBackPressed,
                enableTracking = enableTracking,
                trackingEnabled = locationViewState.value.trackingEnabled,
            )
        }
    }
}