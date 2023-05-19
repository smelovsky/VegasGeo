package com.example.vegas

import android.content.*
import android.os.Bundle
import android.os.PowerManager
import android.view.View
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.vegas.di.MapApi
import com.example.vegas.ui.theme.VegasTheme
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.compose.material3.IconButton
import androidx.compose.material3.*
import androidx.compose.ui.res.painterResource


@AndroidEntryPoint
class MapActivity : ComponentActivity() {

    lateinit var wakeLockPowerManager: PowerManager.WakeLock
    lateinit var mapApi: MapApi

    init {
        mapApi = if (viewModel.currentMap == viewModel.maps[0] ) { viewModel.mapKitApi }
        else { viewModel.openStreetMapApi }
    }

    class LocationReceiver(updateMap: () -> Unit) : BroadcastReceiver() {

        val updateMap = updateMap

        override fun onReceive(context: Context, intent: Intent) {

            if (intent.action == "UPDATE_MAP") {
                updateMap()
            }
        }
    }

    var receiver = LocationReceiver(::updateMap)

    fun onTap(latitude: Double, longitude: Double) {
        val alertDialog = android.app.AlertDialog.Builder(this)

        alertDialog.apply {
            setIcon(R.drawable.vegas_08)
            setTitle(getApplicationContext().getResources().getString(R.string.app_name))
            setMessage(getApplicationContext().getResources().getString(R.string.would_you_like_to_place_marker))
            setPositiveButton(getApplicationContext().getResources().getString(R.string.yes))
            { _: DialogInterface?, _: Int ->

                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                val time = LocalDateTime.now().format(formatter)

                mapApi.setMarker(time,latitude, longitude)
                viewModel.markerAddItem(time, latitude, longitude)

            }
            setNegativeButton(getApplicationContext().getResources().getString(R.string.no))
            { _, _ -> }

        }.create().show()
    }

    fun mapInit(context: Context, title: String): View {
        return mapApi.mapInit(context, title, ::onTap)
    }

    fun zoomOut(): Float {
        return mapApi.zoomOut()
    }

    fun zoomIn(): Float {
        return mapApi.zoomIn()
    }

    fun updateMap() {
        mapApi.updateMarker(getApplicationContext().getResources().getString(R.string.current_position))
        mapApi.updatePolyline()
        mapApi.invalidate()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerReceiver(receiver, IntentFilter("UPDATE_MAP"))

        if (viewModel.wakeLock) {
            val powerManager = getSystemService(POWER_SERVICE) as PowerManager
            wakeLockPowerManager = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK, "VegasMapKit")
        }

        setContent {

            VegasTheme(viewModel.currentTheme == 1) {
                androidx.compose.material3.Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MapScreen(::onBack, ::mapInit, ::zoomOut, ::zoomIn)
                }
            }
        }


    }


    override fun onPause() {
        super.onPause()

        if (viewModel.wakeLock) {
            //wakeLockPowerManager.release()
        }
    }

    override fun onResume() {
        super.onResume()

        if (viewModel.wakeLock) {
            wakeLockPowerManager.acquire()
        }
    }

    override fun onStop() {
        unregisterReceiver(receiver)
        mapApi.onStop()
        super.onStop()
    }

    override fun onStart() {
        super.onStart()

        mapApi.onStart()
    }

    fun onBack() {
        mapApi.saveMapSettings()
        onBackPressedDispatcher.onBackPressed()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MapScreen(onBack: () -> Unit, mapInit:(context:Context, title:String) -> View, zoomOut: () -> Float, zoomIn: () -> Float) {

        var zoom = remember { mutableStateOf(15.0F) }

        androidx.compose.material3.Scaffold(
            topBar = { MapTopBar(onBack) },
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {

                Box(
                    modifier = Modifier
                        .clickable {  }
                ) {

                    val title = stringResource(R.string.current_position)
                    AndroidView(
                        factory = { context ->
                            mapInit(context, title)
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                    if (!mapApi.isZoomEmbedded()) {
                        Column(modifier = Modifier.align(alignment = Alignment.TopEnd)) {
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .border(
                                        border = BorderStroke(width = 2.dp, color = Color.Black),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                IconButton(onClick = { zoom.value = zoomIn() } ) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_zoom_in_24),
                                        contentDescription = "ZoomIn",
                                        tint = Color.Black,
                                        modifier = Modifier.size(60.dp).padding(4.dp)
                                    )
                                }

                            }
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .border(
                                        border = BorderStroke(width = 2.dp, color = Color.Black),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                IconButton(onClick = { zoom.value = zoomOut() } ) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_zoom_out_24),
                                        contentDescription = "ZoomOut",
                                        tint = Color.Black,
                                        modifier = Modifier.size(60.dp).padding(4.dp)
                                    )
                                }
                            }
                        }
                    }

                }
            }
        }


    }
}



@Composable
fun MapTopBar(onBack: () -> Unit) {

    TopAppBar(
        backgroundColor = MaterialTheme.colorScheme.background,
        title = {
            Row() {
                androidx.compose.material3.Text(
                    text = stringResource(R.string.app_name),
                )
                if (viewModel.trackEnabled) {
                    Spacer(modifier = Modifier.width(20.dp))
                    androidx.compose.material3.Text(text = "(${stringResource(R.string.tracking)})",
                        color = Color.Red
                    )
                }
            }
        },

        actions = {
            androidx.compose.material3.IconButton(onClick = {
                AppFunction.exit.run()
                onBack()
            } ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = "Exit",
                )
            }
        },
        navigationIcon = {
            androidx.compose.material3.IconButton(onClick = { onBack() } ) {
                androidx.compose.material3.Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }

        },
    )

}

