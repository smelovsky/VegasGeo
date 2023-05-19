package com.example.vegas.screens


import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.vegas.*
import com.example.vegas.R


@Composable
fun HomeScreen( permissionsGranted: Boolean,
                INTERNET: Boolean,
                ACCESS_NETWORK_STATE: Boolean,
                WAKE_LOCK: Boolean,
                ACCESS_NOTIFICATION_POLICY: Boolean,
                ACCESS_COARSE_LOCATION: Boolean,
                ACCESS_FINE_LOCATION: Boolean,
                RECEIVE_BOOT_COMPLETED: Boolean,
                READ_EXTERNAL_STORAGE: Boolean,
                WRITE_EXTERNAL_STORAGE: Boolean,
                ACCESS_BACKGROUND_LOCATION: Boolean,
                POST_NOTIFICATIONS: Boolean,
                mapLatitude: Double,
                mapLongitude: Double,
                mapTrackPoints: Int,
                maxDistance: Float,
                currentDistance: Float,) {
    Column() {
        Image(
            painterResource(R.drawable.vegas_08),
            contentDescription = "",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
    if (permissionsGranted) {
        Column() {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "${stringResource(R.string.latitude)}: $mapLatitude",
                color = Color.Yellow,
                modifier = Modifier.padding(horizontal = 10.dp))
            Text(text = "${stringResource(R.string.longitude)}: $mapLongitude",
                color = Color.Yellow,
                modifier = Modifier.padding(horizontal = 10.dp))
            Text(text = "${stringResource(R.string.Points)}: ${mapTrackPoints}",
                color = Color.Yellow,
                modifier = Modifier.padding(horizontal = 10.dp))
            Text(text = "${stringResource(R.string.distance_to_start_pont)}: %.0f".format(currentDistance),
                color = Color.Yellow,
                modifier = Modifier.padding(horizontal = 10.dp))
            Text(text = "${stringResource(R.string.max_distance_from_start_pont)}: %.0f".format(maxDistance),
                color = Color.Yellow,
                modifier = Modifier.padding(horizontal = 10.dp))
        }
    } else {
        Column() {
            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "INTERNET",
                color = if (INTERNET) Color.Green else Color.Red,
                modifier = Modifier.padding(horizontal = 10.dp))
            Text(text = "ACCESS_NETWORK_STATE",
                color = if (ACCESS_NETWORK_STATE) Color.Green else Color.Red,
                modifier = Modifier.padding(horizontal = 10.dp))
            Text(text = "WAKE_LOCK",
                color = if (WAKE_LOCK) Color.Green else Color.Red,
                modifier = Modifier.padding(horizontal = 10.dp))
            Text(text = "ACCESS_NOTIFICATION_POLICY",
                color = if (ACCESS_NOTIFICATION_POLICY) Color.Green else Color.Red,
                modifier = Modifier.padding(horizontal = 10.dp))
            Text(text = "RECEIVE_BOOT_COMPLETED",
                color = if (RECEIVE_BOOT_COMPLETED) Color.Green else Color.Red,
                modifier = Modifier.padding(horizontal = 10.dp))
            Text(text = "ACCESS_COARSE_LOCATION",
                color = if (ACCESS_COARSE_LOCATION) Color.Green else Color.Red,
                modifier = Modifier.padding(horizontal = 10.dp))
            Text(text = "ACCESS_FINE_LOCATION",
                color = if (ACCESS_FINE_LOCATION) Color.Green else Color.Red,
                modifier = Modifier.padding(horizontal = 10.dp))

            Text(text = "ACCESS_BACKGROUND_LOCATION",
                color = if (ACCESS_BACKGROUND_LOCATION) Color.Green else Color.Red,
                modifier = Modifier.padding(horizontal = 10.dp))

            Text(text = "READ_EXTERNAL_STORAGE",
                color = if (READ_EXTERNAL_STORAGE) Color.Green else Color.Red,
                modifier = Modifier.padding(horizontal = 10.dp))
            Text(text = "WRITE_EXTERNAL_STORAGE",
                color = if (WRITE_EXTERNAL_STORAGE) Color.Green else Color.Red,
                modifier = Modifier.padding(horizontal = 10.dp))

            Text(text = "POST_NOTIFICATIONS",
                color = if (POST_NOTIFICATIONS) Color.Green else Color.Red,
                modifier = Modifier.padding(horizontal = 10.dp))
        }
    }



}

@Composable
fun MainActivity.BottomBarHome(trackEnabled: Boolean) {

    Row(
        horizontalArrangement = Arrangement.SpaceEvenly,
        modifier = Modifier.fillMaxWidth()
    ) {

        androidx.compose.material3.Button(
            //modifier = Modifier.fillMaxWidth(),
            onClick = {
                if (hasAllPermissions()) {
                    if (!isServiceRunning()) {
                        val intent = Intent(applicationContext, VegasLocationService::class.java)
                        intent.putExtra("app_request", "start")
                        startForegroundService(intent)
                        locationEnabled.value = true
                    } else {
                        if (!trackingEnabled.value) {
                            startTracking()
                        } else {
                            stopTracking()
                        }
                    }
                } else {
                    if (!hasBasePermissions()) {
                        requestBasePermissions()
                    } else {
                        if (!hasAdvancePermissions()) {
                            requestAdvancePermissions()
                        } else {
                            if (!hasStoragePermissions()) {
                                requestStoragePermissions()
                            } else {
                                if (!hasPostNotificationPermissions()) {
                                    requestPostNotificationPermissions()
                                }
                            }
                        }
                    }
                }
            }
        ) {
            if (!permissionsGranted.value) {
                androidx.compose.material3.Text(text = stringResource(R.string.permissions))
            } else {
                if (!locationEnabled.value) {
                    androidx.compose.material3.Text(text = stringResource(R.string.start_location))
                } else {
                    if (trackingEnabled.value) {
                        androidx.compose.material3.Text(text = stringResource(R.string.stop_tracking))
                    } else {
                        androidx.compose.material3.Text(text = stringResource(R.string.start_tracking))
                    }
                }

            }
        }

        androidx.compose.material3.Button(
            onClick = {
                viewModel.showSavedMarker = false
                viewModel.showSavedTrack = false

                viewModel.marker_index = -1
                viewModel.track_index = -1

                map()
            }
        ) {
            androidx.compose.material3.Text(text = stringResource(R.string.show_map))
        }

    }

}
