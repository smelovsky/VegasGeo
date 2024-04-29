package com.vegas.geo.ui.screens.location

import android.telephony.SmsManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeFloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vegas.geo.AppFunction
import com.vegas.geo.MapMode
import com.vegas.geo.R
import com.vegas.geo.data.database.model.TrackEntity
import com.vegas.geo.mainViewModel
import com.vegas.geo.ui.common.MainScaffold

@Composable
fun LocationRoute(
    navigateBack: () -> Unit,
    enableTracking: (enable: Boolean) -> Unit,
    navigateToHome: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToScanner: () -> Unit,
    navigateToTracks: () -> Unit,
    modifier: Modifier = Modifier,
    trackingEnabled: Boolean,
) {

    val allowedNavigateToHome = remember() { navigateToHome }
    val allowedNavigateToSettings = remember() { navigateToSettings }
    val allowedNavigateToScanner = remember() { navigateToScanner }
    val allowedNavigateToTracks = remember() { navigateToTracks }

    LocationScreen(
        navigateBack = navigateBack,
        enableTracking = enableTracking,
        navigateToHome = { allowedNavigateToHome() },
        navigateToSettings = { allowedNavigateToSettings() },
        navigateToScanner = { allowedNavigateToScanner() },
        navigateToTracks = { allowedNavigateToTracks() },
        modifier = modifier,
        trackingEnabled = trackingEnabled,
    )


}

@Composable
fun LocationScreen(
    navigateBack: () -> Unit,
    enableTracking: (enable: Boolean) -> Unit,
    navigateToHome: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToScanner: () -> Unit,
    navigateToTracks: () -> Unit,
    modifier: Modifier = Modifier,
    trackingEnabled: Boolean,
) {

    val locationViewState = mainViewModel.locationViewState.collectAsState()
    
    BackHandler(onBack = navigateBack)

    MainScaffold(
        titleRes = R.string.location_screen_title,
        modifier = modifier,
        navigateToHome = navigateToHome,
        navigateToSettings = navigateToSettings,
        navigateToScanner = navigateToScanner,
        navigateToTracks = navigateToTracks,
        trackingEnabled = trackingEnabled,
    ) { innerPadding ->


        Box(modifier = Modifier.fillMaxWidth().padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {


                Text("${stringResource(R.string.latitude)} ${locationViewState.value.latitude}")

                Text("${stringResource(R.string.longitude)} ${locationViewState.value.longitude}")

                if (locationViewState.value.trackingEnabled) {
                    val distance = String.format("%.2f", (locationViewState.value.distance)/1000)
                    Text("${stringResource(R.string.distance)} ${distance} ${stringResource(R.string.km)}")
                    Text("${stringResource(R.string.points)} ${locationViewState.value.points}")
                }

                Spacer(Modifier.height(20.dp))

                if (locationViewState.value.trackingEnabled) {
                    OutlinedButton(onClick = {
                        enableTracking(false)
                    } ) {
                        Text(text = stringResource(R.string.start_tracking))
                    }
                } else {
                    OutlinedButton(onClick = {
                        enableTracking(true)
                    } ) {
                        Text(text = stringResource(R.string.stop_tracking))
                    }
                }
            }
        }

        if (locationViewState.value.isLocationDone) {
            Box(modifier = Modifier.fillMaxWidth().fillMaxHeight().padding(innerPadding),
                contentAlignment = Alignment.BottomEnd) {
                FloatingActionButton(
                    onClick = {
                        mainViewModel.mapMode = MapMode.LOCATION
                        mainViewModel.trackingEanabled = locationViewState.value.trackingEnabled
                        AppFunction.map.run()
                    },
                    Modifier.padding(20.dp),
                    containerColor = androidx.compose.material.MaterialTheme.colors.background,
                    contentColor = androidx.compose.material.MaterialTheme.colors.primary,
                ) {
                    Icon(Icons.Filled.Map, "Open Map")
                }
            }
        }


    }
}
