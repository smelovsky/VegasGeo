package com.vegas.geo

import android.content.*
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ExitToApp
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
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.material3.IconButton
import androidx.compose.material3.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.painterResource
import androidx.core.content.res.ResourcesCompat
import com.vegas.geo.ui.theme.VegasGeoTheme
import org.osmdroid.util.GeoPoint


@AndroidEntryPoint
class MapActivity : ComponentActivity() {

    class LocationReceiver(val updateMap: () -> Unit) : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            if (intent.action == "UPDATE_MAP") {
                updateMap()
            }
        }
    }

    var receiver = LocationReceiver( {
        mainViewModel.getMapApi().updateTracking()
    })


    @RequiresApi(Build.VERSION_CODES.O)
    fun onTap(latitude: Double, longitude: Double) {
        val alertDialog = android.app.AlertDialog.Builder(this)

        alertDialog.apply {
            setIcon(R.drawable.vegas_02)
            setTitle(getApplicationContext().getResources().getString(R.string.app_name))
            setMessage(getApplicationContext().getResources().getString(R.string.build_route))
            setPositiveButton(getApplicationContext().getResources().getString(R.string.yes))
            { _: DialogInterface?, _: Int ->
                val startPoint = mainViewModel.currentPoint
                val endPoint = GeoPoint(latitude, longitude)

                mainViewModel.getMapApi().addRoute(startPoint, endPoint)

            }
            setNegativeButton(getApplicationContext().getResources().getString(R.string.no))
            { _, _ -> }

        }.create().show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun mapInit(context: Context): View {
        return mainViewModel.getMapApi().mapInit(context, ::onTap)
    }

    fun zoomOut(): Float {
        return mainViewModel.getMapApi().zoomOut()
    }

    fun zoomIn(): Float {
        return mainViewModel.getMapApi().zoomIn()
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerReceiver(receiver, IntentFilter("UPDATE_MAP"))

        val currentDraw = ResourcesCompat.getDrawable(getResources(), R.drawable.default_pin, null)


        setContent {

            val settingsViewState = mainViewModel.settingsViewState.collectAsState()
            if (settingsViewState.value.keepScreenOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
             }

            val darkMode =
                if (settingsViewState.value.darkModeAutoSelected) isSystemInDarkTheme()
                else if (settingsViewState.value.darkModeDarkSelected) true else false

            VegasGeoTheme(darkMode) {
                Surface(
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
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        unregisterReceiver(receiver)
        mainViewModel.getMapApi().onStop()

        Log.d("zzz", "onStop")
        super.onStop()
    }

    override fun onStart() {
        Log.d("zzz", "onStart")
        super.onStart()

        mainViewModel.getMapApi().onStart()
    }

    override fun onBackPressed() {
        super.onBackPressed()

        onBack()
    }

    fun onBack() {

        mainViewModel.getMapApi().saveMapSettings()
        mainViewModel.saveRouteSettings()

        onBackPressedDispatcher.onBackPressed()
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MapScreen(onBack: () -> Unit, mapInit:(context:Context) -> View, zoomOut: () -> Float, zoomIn: () -> Float) {

        val zoom = remember { mutableStateOf(15.0F) }
        val mapViewState = mainViewModel.mapViewState.collectAsState()

        androidx.compose.material3.Scaffold(
            topBar = { MapTopBar(onBack) },
        ) { padding ->
            Column(modifier = Modifier.padding(padding)) {

                Box(
                    modifier = Modifier
                        .clickable {  }
                ) {

                    AndroidView(
                        factory = { context ->
                            mapInit(context)
                        },
                        modifier = Modifier.fillMaxSize(),
                    )

                    if (mapViewState.value.isRouteAdded) {
                        Row(modifier = Modifier.align(alignment = Alignment.TopEnd)) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 20.dp, end = 20.dp)
                                    .border(
                                        border = BorderStroke(width = 2.dp, color = Color.Black),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                            ) {
                                IconButton(onClick = {
                                    mainViewModel.getMapApi().removeRoute()
                                    mainViewModel.setRoute(false)
                                    mainViewModel.isRoadAdded = false;
                                } ) {
                                    Icon(
                                        painter = painterResource(R.drawable.baseline_route_24),
                                        contentDescription = "route",
                                        tint = Color.Black,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .padding(4.dp)
                                    )
                                }

                            }

                        }
                    }

                    if (!mainViewModel.getMapApi().isZoomEmbedded()) {
                        Row(modifier = Modifier.align(alignment = Alignment.BottomCenter)) {
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 50.dp, end = 20.dp)
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
                                        modifier = Modifier
                                            .size(60.dp)
                                            .padding(4.dp)
                                    )
                                }

                            }
                            Box(
                                modifier = Modifier
                                    .padding(bottom = 50.dp, start = 20.dp)
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
                                        modifier = Modifier
                                            .size(60.dp)
                                            .padding(4.dp)
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

    val locationViewState = mainViewModel.locationViewState.collectAsState()

    androidx.compose.material.TopAppBar(
        backgroundColor = androidx.compose.material.MaterialTheme.colors.background,
        contentColor = androidx.compose.material.MaterialTheme.colors.primary,
        title = {
            Row() {
                androidx.compose.material.Text(
                    text = stringResource(R.string.app_name),
                )
                if (locationViewState.value.trackingEnabled) {
                    Spacer(modifier = Modifier.width(20.dp))
                    androidx.compose.material.Text(text = "(${stringResource(R.string.tracking)})",
                        color = Color.Red
                    )
                }
            }
        },

        actions = {
            IconButton(onClick = {
                AppFunction.exit.run()
                onBack()
            } ) {
                Icon(
                    imageVector = Icons.Filled.ExitToApp,
                    contentDescription = "Exit",
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = { onBack() } ) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Back",
                )
            }

        },
    )

}