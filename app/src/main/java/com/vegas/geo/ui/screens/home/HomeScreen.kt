package com.vegas.geo.ui.screens.home

import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.vegas.geo.R
import com.vegas.geo.ui.common.MainScaffold

@Composable
fun HomeRoute(
    navigateBack: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToScanner: () -> Unit,
    navigateToLocation: () -> Unit,
    navigateToTracks: () -> Unit,
    trackingEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val allowedNavigateToSettings = remember() { navigateToSettings }
    val allowedNavigateToScanner = remember() { navigateToScanner }
    val allowedNavigateToLocation = remember() { navigateToLocation }
    val allowedNavigateToTracks = remember() { navigateToTracks }

    HomeScreen(
        navigateBack = navigateBack,
        navigateToSettings = { allowedNavigateToSettings() },
        navigateToScanner = { allowedNavigateToScanner() },
        navigateToLocation = { allowedNavigateToLocation() },
        navigateToTracks = { allowedNavigateToTracks() },
        trackingEnabled = trackingEnabled,
        modifier = modifier,
    )


}

@Composable
fun HomeScreen(
    navigateBack: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToScanner: () -> Unit,
    navigateToLocation: () -> Unit,
    navigateToTracks: () -> Unit,
    trackingEnabled: Boolean,
    modifier: Modifier = Modifier,
) {

    BackHandler(onBack = navigateBack)

    MainScaffold(
        titleRes = R.string.home_screen_title,
        modifier = modifier,
        navigateToSettings = navigateToSettings,
        navigateToScanner = navigateToScanner,
        navigateToLocation = navigateToLocation,
        navigateToTracks = navigateToTracks,
        trackingEnabled = trackingEnabled,
    ) { innerPadding ->

        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Image(
                painter = painterResource(R.drawable.vegas_01),
                contentDescription = "",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().padding(innerPadding),
            )
        }

    }
}
