package com.vegas.geo.ui.screens.scanner

import android.telephony.SmsManager
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.MaterialTheme
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.vegas.geo.AppFunction
import com.vegas.geo.MapMode
import com.vegas.geo.R
import com.vegas.geo.ScannerMode
import com.vegas.geo.mainViewModel
import com.vegas.geo.ui.common.MainScaffold

@Composable
fun ScannerRoute(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToLocation: () -> Unit,
    navigateToTracks: () -> Unit,
    trackingEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val allowedNavigateToHome = remember() { navigateToHome }
    val allowedNavigateToSettings = remember() { navigateToSettings }
    val allowedNavigateToLocation = remember() { navigateToLocation }
    val allowedNavigateToTracks = remember() { navigateToTracks }

    ScannerScreen(
        navigateBack = navigateBack,
        navigateToHome = { allowedNavigateToHome() },
        navigateToSettings = { allowedNavigateToSettings() },
        navigateToLocation = { allowedNavigateToLocation() },
        navigateToTracks = { allowedNavigateToTracks() },
        trackingEnabled = trackingEnabled,
        modifier = modifier,
    )


}

@Composable
fun ScannerScreen(
    navigateBack: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToSettings: () -> Unit,
    navigateToLocation: () -> Unit,
    navigateToTracks: () -> Unit,
    trackingEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    val scannerViewState = mainViewModel.scannerViewState.collectAsState()

    BackHandler(onBack = navigateBack)

    MainScaffold(
        titleRes = R.string.scanner_screen_title,
        modifier = modifier,
        navigateToHome = navigateToHome,
        navigateToSettings = navigateToSettings,
        navigateToLocation = navigateToLocation,
        navigateToTracks = navigateToTracks,
        trackingEnabled = trackingEnabled,
    ) { innerPadding ->

        Box(modifier = Modifier
            .fillMaxWidth()
            .padding(innerPadding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                if (scannerViewState.value.scannerMode == ScannerMode.SHOW_MESSAGE) {

                    val point = "${mainViewModel.scannerPoint.latitude.toString()}, ${com.vegas.geo.mainViewModel.scannerPoint.longitude.toString()}"

                    Text("${stringResource(R.string.status)} ${scannerViewState.value.message}")
                    Text("${stringResource(R.string.location)} ${point}")
                }

                if (scannerViewState.value.scannerMode == ScannerMode.PLEASE_WAIT) {
                    Text(stringResource(R.string.please_wait))
                }

                if (scannerViewState.value.scannerMode == ScannerMode.SHOW_ERROR) {
                    Text(stringResource(R.string.error))
                }


                Spacer(Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    OutlinedButton(onClick = {

                        mainViewModel.setScannerMode(ScannerMode.PLEASE_WAIT)

                        val sms: SmsManager = SmsManager.getDefault()
                        val sender = mainViewModel.getPhoneNumber()

                        if (mainViewModel.getAlarm()) {
                            sms.sendTextMessage(sender, null, "*", null, null)
                        } else {
                            sms.sendTextMessage(sender, null, "?", null, null)
                        }



                    } ) {
                        Text(stringResource(R.string.send_sms))
                    }
/*
                    OutlinedButton(
                        onClick = {

mainViewModel.setScannerMode(ScannerMode.PLEASE_WAIT)

val sms: SmsManager = SmsManager.getDefault()
val sender = mainViewModel.getPhoneNumber()

sms.sendTextMessage(sender, null, "*", null, null)


} ) {
                        Text(
                            //color = Color.Red,
                            text = stringResource(R.string.alarm)
                        )
                    }
*/
                }

            }
        }

        if (scannerViewState.value.scannerMode == ScannerMode.SHOW_MESSAGE) {
            Box(modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(innerPadding),
                contentAlignment = Alignment.BottomEnd) {
                FloatingActionButton(
                    onClick = {
                        mainViewModel.mapMode = MapMode.SCANNER
                        AppFunction.map.run()
                    },
                    Modifier.padding(20.dp),
                    containerColor = MaterialTheme.colors.background,
                    contentColor = MaterialTheme.colors.primary,
                ) {
                    Icon(Icons.Filled.Map, "Open Map")
                }
            }
        }
    }
}