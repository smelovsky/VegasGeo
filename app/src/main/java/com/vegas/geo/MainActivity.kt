package com.vegas.geo

import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.hilt.navigation.compose.hiltViewModel
import com.vegas.geo.data.database.model.LocationEntity
import com.vegas.geo.ui.common.MainContent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

sealed class AppFunction(var run: () -> Unit) {

    object exit : AppFunction( {} )
    object map : AppFunction( {} )
}

lateinit var mainViewModel: MainViewModel

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    var isAppInited: Boolean = false
    var isFistStart: Boolean = true

////////////////////////////////////////////////////////////////////////////////////////////////////

    internal class LocationReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {

            when (intent.action) {
                "CURREN_LOCATION" -> {
                    val latitude = intent.getDoubleExtra("latitude", 0.0)
                    val longitude = intent.getDoubleExtra("longitude", 0.0)
                    val distance = intent.getDoubleExtra("distance", 0.0)
                    val points = intent.getIntExtra("points", 0)

                    val intentMap = Intent()
                    intentMap.action = "UPDATE_MAP"

                    GlobalScope.launch {
                        mainViewModel.setLocation(latitude, longitude, distance, points)
                        mainViewModel.locationList = mainViewModel.getDbApi().appDao().getLocationListById(mainViewModel.trackId)
                        context.sendBroadcast(intentMap)
                    }

                }
                "STATUS" -> {
                    val value = intent.getBooleanExtra("tracking_enabled", false)
                    mainViewModel.enableTracking(value)
                    Log.d("zzz", "onReceive: ${intent.action} ${value}")

                }

            }


        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var receiver = LocationReceiver()
        registerReceiver(receiver, IntentFilter("CURREN_LOCATION"))
        registerReceiver(receiver, IntentFilter("STATUS"))

        AppFunction.exit.run = ::exitFromApp
        AppFunction.map.run = ::map

        setContent {

            mainViewModel = hiltViewModel()

            val settingsViewState = mainViewModel.settingsViewState.collectAsState()

            if (mainViewModel.exitFromApp.value) { exitFromApp() }

            if (settingsViewState.value.keepScreenOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }

            val darkMode =
                if (settingsViewState.value.darkModeAutoSelected) isSystemInDarkTheme()
                else if (settingsViewState.value.darkModeDarkSelected) true else false


            mainViewModel.getPermissionsApi().hasAllPermissions(this)

            isAppInited = true

            if (isFistStart) {
                if (mainViewModel.getPermissionsApi().hasAllPermissions(this)) {
                    if (!isServiceRunning()) {
                        val intent = Intent(applicationContext, LocationService::class.java)
                        intent.putExtra("app_request", "start")
                        startForegroundService(intent)
                    } else {
                        val intent = Intent(applicationContext, LocationService::class.java)
                        intent.putExtra("app_request", "status")
                        startForegroundService(intent)
                    }

                    isFistStart = false
                }
            }

            MainContent(
                darkMode = darkMode,
                activity = this,
                onBackPressed = ::exitFromApponOnBackPressed,
                enableTracking = ::enableTracking,
            )
        }
    }

    fun isServiceRunning(): Boolean {

        val serviceClass: Class<*> = LocationService::class.java
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Loop through the running services
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                // If the service is running then return true
                return true
            }
        }
        return false
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun enableTracking(enable: Boolean) {

        val intent = Intent(this, LocationService::class.java)

        if (enable) {
            intent.putExtra("app_request", "tracking_enable")
            intent.putExtra("min_distance", 1)
        } else {
            intent.putExtra("app_request", "tracking_disable")
        }

        startForegroundService(intent)

        mainViewModel.enableTracking(enable)

    }

    override fun onStart() {
        super.onStart()
        if (isAppInited) {
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        if (isAppInited) {
            mainViewModel.getPermissionsApi().hasAllPermissions(this)
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()

        exitFromApponOnBackPressed()
    }


    fun exitFromApponOnBackPressed() {

        if (mainViewModel.settingsViewState.value.askToExitFromApp) {

            val alertDialog = android.app.AlertDialog.Builder(this)

            alertDialog.apply {
                setIcon(R.drawable.vegas_02)
                setTitle(getApplicationContext().getResources().getString(R.string.app_name))
                setMessage(
                    getApplicationContext().getResources()
                        .getString(R.string.do_you_really_want_to_close_the_application)
                )
                setPositiveButton(getApplicationContext().getResources().getString(R.string.yes))
                { _: DialogInterface?, _: Int -> exitFromApp() }
                setNegativeButton(getApplicationContext().getResources().getString(R.string.no))
                { _, _ -> }

            }.create().show()
        } else {
            exitFromApp()
        }
    }

    fun exitFromApp() {

        if (!mainViewModel.getLocationEnabledAfterExit()) {

            Log.d("zzz", "stopService")

            val intent = Intent(this, LocationService::class.java)
            stopService(intent)

        }

        this.finish()
    }

    fun map() {

        //val intent = Intent(this, XActivity::class.java)
        //startActivity(intent)

        val mainActivity = this

        GlobalScope.launch {

            mainViewModel.locationList = listOf()

            when (mainViewModel.mapMode) {
                MapMode.LOCATION ->
                    if (mainViewModel.trackingEanabled) {
                        mainViewModel.locationList = mainViewModel.getDbApi().appDao().getLocationListById(mainViewModel.trackId)
                    } else {
                        mainViewModel.locationList = listOf<LocationEntity>()
                    }
                MapMode.SAVED_TRACK ->
                        mainViewModel.locationList = mainViewModel.getDbApi().appDao().getLocationList()

                MapMode.SCANNER ->
                    mainViewModel.locationList = listOf<LocationEntity>()
            }


            val intent = Intent(mainActivity, MapActivity::class.java)
            startActivity(intent)
        }

    }


}

