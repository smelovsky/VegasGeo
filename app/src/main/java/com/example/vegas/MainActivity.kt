package com.example.vegas

import android.Manifest
import android.app.ActivityManager
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.NonNull
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.vegas.screens.BottomBarHome
import com.example.vegas.screens.BottomBarMarkers
import com.example.vegas.screens.BottomBarTracks
import com.example.vegas.ui.theme.VegasTheme
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.log

lateinit var viewModel: VegasViewModel

val tabs = listOf(
    TabItem.Home,
    TabItem.Markers,
    TabItem.Tracks,
    TabItem.Settings,
)

sealed class AppFunction(var run: () -> Unit) {

    object exit : AppFunction( {} )
    object putPreferences : AppFunction( {} )
    object updateDistance : AppFunction( {} )
    object startTracking : AppFunction( {} )
    object stopTracking : AppFunction( {} )
    object map : AppFunction( {} )
}

val basePermissions = arrayOf(
    Manifest.permission.INTERNET,
    Manifest.permission.ACCESS_NETWORK_STATE,
    Manifest.permission.WAKE_LOCK,
    Manifest.permission.ACCESS_NOTIFICATION_POLICY,
    Manifest.permission.RECEIVE_BOOT_COMPLETED,
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,

)

val storagePermissions = arrayOf(
    Manifest.permission.READ_EXTERNAL_STORAGE,
    Manifest.permission.WRITE_EXTERNAL_STORAGE,
)

val advancePermissions = arrayOf(
    Manifest.permission.ACCESS_BACKGROUND_LOCATION,

)

val postNotificatiionsPermissions = arrayOf(
    Manifest.permission.POST_NOTIFICATIONS,
)

lateinit var mapLatitude: MutableState<Double>
lateinit var mapLongitude: MutableState<Double>
lateinit var mapTrackPoints: MutableState<Int>
lateinit var mapMaxDistance: MutableState<Float>
lateinit var mapCurrentDistance: MutableState<Float>

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    lateinit var INTERNET: MutableState<Boolean>
    lateinit var ACCESS_NETWORK_STATE: MutableState<Boolean>
    lateinit var WAKE_LOCK: MutableState<Boolean>
    lateinit var ACCESS_NOTIFICATION_POLICY: MutableState<Boolean>
    lateinit var ACCESS_COARSE_LOCATION: MutableState<Boolean>
    lateinit var ACCESS_FINE_LOCATION: MutableState<Boolean>
    lateinit var RECEIVE_BOOT_COMPLETED: MutableState<Boolean>
    lateinit var READ_EXTERNAL_STORAGE: MutableState<Boolean>
    lateinit var WRITE_EXTERNAL_STORAGE: MutableState<Boolean>
    lateinit var ACCESS_BACKGROUND_LOCATION: MutableState<Boolean>
    lateinit var POST_NOTIFICATIONS: MutableState<Boolean>

    lateinit var prefs: SharedPreferences
    val APP_PREFERENCES_THEME = "theme"
    val APP_PREFERENCES_ASK_TO_EXIT_FROM_APP = "ask_to_exit_from_app"
    val APP_PREFERENCES_ASK_TO_DELETE_SELECTED_ITEM = "ask_to_delete_selectedItem"
    val APP_PREFERENCES_ASK_TO_SAVE_TRACKING = "ask_to_save_tracking"
    val APP_PREFERENCES_WAKE_LOCK = "wake_lock"
    val APP_PREFERENCES_CURRENT_MIN_DISTANCE = "current_min_distance"
    val APP_PREFERENCES_CURRENT_MAP = "current_map"

    lateinit var theme: MutableState<Boolean>
    lateinit var trackingEnabled: MutableState<Boolean>
    lateinit var locationEnabled: MutableState<Boolean>
    lateinit var permissionsGranted: MutableState<Boolean>

    lateinit var markersIsShowUp: MutableState<Boolean>
    lateinit var markersIsShowDown: MutableState<Boolean>
    lateinit var markersUp: MutableState<()->Unit>
    lateinit var markersDown: MutableState<()->Unit>

    lateinit var tracksIsShowUp: MutableState<Boolean>
    lateinit var tracksIsShowDown: MutableState<Boolean>
    lateinit var tracksUp: MutableState<()->Unit>
    lateinit var tracksDown: MutableState<()->Unit>

    var isAppInited: Boolean = false
    var isFistStart: Boolean = true

    internal class LocationReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            Log.d("zzz", "onReceive: ${intent.action}")

            when (intent.action) {
                "CURREN_LOCATION" -> {
                    val latitude = intent.getDoubleExtra("latitude", 0.0)
                    val longitude = intent.getDoubleExtra("longitude", 0.0)

                    val trackArray = intent.getDoubleArrayExtra("track")
                    if (trackArray != null) {

                        Log.d("zzz", "trackArray.size: ${trackArray.size}")

                        viewModel.track.clear()

                        var index = 0
                        while (index < trackArray.size) {
                            viewModel.track.add(MapPoint(trackArray[index], trackArray[index + 1]))
                            index += 2
                        }

                        if (trackArray.size > 1) {
                            mapMaxDistance.value = intent.getFloatExtra("max_distance", 0F)
                            mapCurrentDistance.value = intent.getFloatExtra("current_distance", 0F)
                        }

                    }

                    viewModel.locationLatitude = latitude
                    viewModel.locationLongitude = longitude

                    mapLatitude.value = latitude
                    mapLongitude.value = longitude
                    mapTrackPoints.value = viewModel.track.size

                    val intentMap = Intent()
                    intentMap.action = "UPDATE_MAP"
                    context.sendBroadcast(intentMap)
                }

            }


        }
    }


    @OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var receiver = LocationReceiver()
        registerReceiver(receiver, IntentFilter("CURREN_LOCATION"))

        prefs = getSharedPreferences("settings", Context.MODE_PRIVATE)

        AppFunction.exit.run = ::exitFromApp
        AppFunction.putPreferences.run = ::putPreferences
        AppFunction.updateDistance.run = ::updateDistance
        AppFunction.startTracking.run = ::startTracking
        AppFunction.stopTracking.run = ::stopTracking
        AppFunction.map.run = ::map



        setContent {

            viewModel = hiltViewModel()

            getPreferences()

            INTERNET = remember { mutableStateOf(false) }
            ACCESS_NETWORK_STATE = remember { mutableStateOf(false) }
            WAKE_LOCK = remember { mutableStateOf(false) }
            ACCESS_NOTIFICATION_POLICY = remember { mutableStateOf(false) }
            ACCESS_COARSE_LOCATION = remember { mutableStateOf(false) }
            ACCESS_FINE_LOCATION = remember { mutableStateOf(false) }
            RECEIVE_BOOT_COMPLETED = remember { mutableStateOf(false) }

            READ_EXTERNAL_STORAGE = remember { mutableStateOf(false) }
            WRITE_EXTERNAL_STORAGE = remember { mutableStateOf(false) }

            ACCESS_BACKGROUND_LOCATION = remember { mutableStateOf(false) }
            POST_NOTIFICATIONS = remember { mutableStateOf( if (Build.VERSION.SDK_INT >= 33) false else true ) }

            trackingEnabled = remember { mutableStateOf(isServiceRunning()) }
            locationEnabled = remember { mutableStateOf(isServiceRunning()) }
            theme = remember { mutableStateOf(viewModel.currentTheme == 1) }

            mapLatitude = remember { mutableStateOf(viewModel.locationLatitude) }
            mapLongitude = remember { mutableStateOf(viewModel.locationLongitude) }
            mapTrackPoints = remember { mutableStateOf(viewModel.track.size) }
            mapMaxDistance = remember { mutableStateOf(0F) }
            mapCurrentDistance = remember { mutableStateOf(0F) }

            markersIsShowUp  = remember { mutableStateOf(false) }
            markersIsShowDown  = remember { mutableStateOf(false) }
            markersUp  = remember { mutableStateOf({ }) }
            markersDown  = remember { mutableStateOf({ }) }

            tracksIsShowUp  = remember { mutableStateOf(false) }
            tracksIsShowDown  = remember { mutableStateOf(false) }
            tracksUp  = remember { mutableStateOf({ }) }
            tracksDown  = remember { mutableStateOf({ }) }

            permissionsGranted = remember { mutableStateOf(hasAllPermissions()) }

            isAppInited = true

            if (isFistStart) {
                if (permissionsGranted.value) {
                    if (!isServiceRunning()) {
                        val intent = Intent(applicationContext, VegasLocationService::class.java)
                        intent.putExtra("app_request", "start")
                        startForegroundService(intent)
                        locationEnabled.value = true
                    }

                    viewModel.markerReadAllItems()
                    viewModel.trackReadAllItems()

                    isFistStart = false
                }
            }



            VegasTheme(theme.value) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {

                    var pagerState: PagerState = rememberPagerState(0)

                    androidx.compose.material3.Scaffold(
                        //modifier = Modifier.nestedScroll(topAppBarScrollBehavior.nestedScrollConnection),
                        //contentWindowInsets = WindowInsets.Companion.safeDrawing,

                        topBar = {
                            TopAppBar(
                                backgroundColor = MaterialTheme.colorScheme.background,
                                title = {
                                    Row() {
                                        androidx.compose.material3.Text(
                                            text = stringResource(R.string.app_name),
                                        )
                                        if (trackingEnabled.value) {
                                            Spacer(modifier = Modifier.width(20.dp))
                                            androidx.compose.material3.Text(text = "(${stringResource(R.string.tracking)})",
                                                color = Color.Red
                                            )
                                        }
                                    }

                                },
                                modifier = Modifier.height(30.dp),
                                actions = {
                                    androidx.compose.material3.IconButton(onClick = {
                                        exitFromApp()
                                    }) {
                                        androidx.compose.material3.Icon(
                                            imageVector = Icons.Filled.ExitToApp,
                                            contentDescription = "Exit",
                                        )
                                    }
                                },
                                //scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(),
                            )
                        },
                        bottomBar = {
                            BottomAppBar(
                                modifier = Modifier.height(60.dp),
                                backgroundColor = MaterialTheme.colorScheme.background,
                                )
                            {
                                when (tabs[pagerState.currentPage]) {
                                    TabItem.Home -> BottomBarHome(trackingEnabled.value)
                                    TabItem.Markers -> BottomBarMarkers(markersIsShowUp.value,
                                        markersIsShowDown.value,
                                        markersUp.value,
                                        markersDown.value)
                                    TabItem.Tracks -> BottomBarTracks(tracksIsShowUp.value,
                                        tracksIsShowDown.value,
                                        tracksUp.value,
                                        tracksDown.value)
                                }
                            }
                        }
                    ) { padding ->

                        Column(modifier = Modifier.padding(padding)) {
                            Tabs(tabs = tabs, pagerState = pagerState)
                            TabsContent(
                                tabs = tabs, pagerState = pagerState,
                                permissionsGranted.value,
                                INTERNET.value,
                                ACCESS_NETWORK_STATE.value,
                                WAKE_LOCK.value,
                                ACCESS_NOTIFICATION_POLICY.value,
                                ACCESS_COARSE_LOCATION.value,
                                ACCESS_FINE_LOCATION.value,
                                RECEIVE_BOOT_COMPLETED.value,
                                READ_EXTERNAL_STORAGE.value,
                                WRITE_EXTERNAL_STORAGE.value,
                                ACCESS_BACKGROUND_LOCATION.value,
                                POST_NOTIFICATIONS.value,
                                mapLatitude.value,
                                mapLongitude.value,
                                mapTrackPoints.value,
                                mapMaxDistance.value,
                                mapCurrentDistance.value,
                                ::markersShowUpFun,
                                ::markersShowDownFun,
                                ::tracksShowUpFun,
                                ::tracksShowDownFun,
                            )
                        }
                    }
                }
            }
        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    fun Tabs(tabs: List<TabItem>, pagerState: PagerState) {
        val scope = rememberCoroutineScope()
        ScrollableTabRow(
            backgroundColor = MaterialTheme.colorScheme.background,
            selectedTabIndex = pagerState.currentPage,
        ) {
            tabs.forEachIndexed { index, tab ->
                androidx.compose.material.Tab(
                    //icon = { Icon(painter = painterResource(id = tab.icon), contentDescription = "") },
                    text = { Text(stringResource(tab.title)) },
                    selected = pagerState.currentPage == index,
                    onClick = {
                        scope.launch {
                            pagerState.animateScrollToPage(index)
                        }
                    },
                )
            }
        }
    }


    @OptIn(ExperimentalPagerApi::class)
    @Composable
    fun TabsContent(
        tabs: List<TabItem>, pagerState: PagerState,
        permissionsGranted: Boolean,
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
        mapMaxDistance: Float,
        mapCurrentDistance: Float,
        markersShowUpFun:(Boolean, ()->Unit) -> Unit,
        markersShowDownFun:(Boolean, ()->Unit) -> Unit,
        tracksShowUpFun:(Boolean, ()->Unit) -> Unit,
        tracksShowDownFun:(Boolean, ()->Unit) -> Unit
    ) {
        HorizontalPager(state = pagerState, count = tabs.size) { page ->

            var screenParams: ScreenParams = ScreenParams(
                permissionsGranted,
                INTERNET,
                ACCESS_NETWORK_STATE,
                WAKE_LOCK,
                ACCESS_NOTIFICATION_POLICY,
                ACCESS_COARSE_LOCATION,
                ACCESS_FINE_LOCATION,
                RECEIVE_BOOT_COMPLETED,
                READ_EXTERNAL_STORAGE,
                WRITE_EXTERNAL_STORAGE,
                ACCESS_BACKGROUND_LOCATION,
                POST_NOTIFICATIONS,
                mapLatitude,
                mapLongitude,
                mapTrackPoints,
                mapMaxDistance,
                mapCurrentDistance,
                markersShowUpFun,
                markersShowDownFun,
                tracksShowUpFun,
                tracksShowDownFun,
            )

            tabs[page].screen(screenParams)

        }
    }


    override fun onStart() {
        super.onStart()

    }

    override fun onStop() {
        super.onStop()
    }

    override fun onPause() {
        super.onPause()

        Log.d("zzz","onPause")
    }

    override fun onResume() {
        super.onResume()
        Log.d("zzz", "onResume")
        if (isAppInited) {
            permissionsGranted.value = hasAllPermissions()
        }
    }

    fun startTracking() {
        val intent = Intent(this, VegasLocationService::class.java)
        intent.putExtra("app_request", "track_enable")
        intent.putExtra("min_distance", viewModel.currentMinDistance)
        startForegroundService(intent)

        viewModel.trackEnabled = true
        viewModel.track.clear()

        mapLatitude.value = viewModel.locationLatitude
        mapLongitude.value = viewModel.locationLongitude
        mapTrackPoints.value = 0
        mapMaxDistance.value = 0F
        mapCurrentDistance.value = 0F
        trackingEnabled.value = true
    }

    fun stopTracking() {
        val intent = Intent(this, VegasLocationService::class.java)
        intent.putExtra("app_request", "track_disable")
        startForegroundService(intent)

        viewModel.trackEnabled = false
        trackingEnabled.value = false

        if (viewModel.track.size > 0) {
            if (viewModel.askToSaveTracking) {
                val alertDialog = android.app.AlertDialog.Builder(this)

                alertDialog.apply {
                    setIcon(R.drawable.vegas_08)
                    setTitle(getApplicationContext().getResources().getString(R.string.app_name))
                    setMessage(getApplicationContext().getResources().getString(R.string.would_you_like_to_save_tracking))
                    setPositiveButton(
                        getApplicationContext().getResources().getString(R.string.yes))
                    { _: DialogInterface?, _: Int -> saveTracking() }
                    setNegativeButton(
                        getApplicationContext().getResources().getString(R.string.no))
                    { _, _ -> }
                }.create().show()
            } else {
                saveTracking()
            }
        }
    }

    fun updateDistance() {
        val intent = Intent(this, VegasLocationService::class.java)
        intent.putExtra("app_request", "update_params")
        intent.putExtra("min_distance", viewModel.currentMinDistance)

        startForegroundService(intent)
    }

    fun saveTracking() {
        viewModel.trackAddItem()
    }

    fun map() {
        val intent = Intent(this, MapActivity::class.java)
        startActivity(intent)
    }


    fun markersShowDownFun(show: Boolean, downFun:()->Unit) {
        //Log.d("zzz", "showDownFun: ${show}")
        markersIsShowDown.value = show
        markersDown.value = downFun
    }

    fun markersShowUpFun(show: Boolean, upFun:()->Unit) {
        //Log.d("zzz", "showUpFun: ${show}")
        markersIsShowUp.value = show
        markersUp.value = upFun
    }

    fun tracksShowUpFun(show: Boolean, upFun:()->Unit) {
        //Log.d("zzz", "showUpFun: ${show}")
        tracksIsShowUp.value = show
        tracksUp.value = upFun
    }

    fun tracksShowDownFun(show: Boolean, downFun:()->Unit) {
        //Log.d("zzz", "showDownFun: ${show}")
        tracksIsShowDown.value = show
        tracksDown.value = downFun
    }

    override fun onBackPressed() {

        if (viewModel.askToExitFromApp) {

            val alertDialog = android.app.AlertDialog.Builder(this)

            alertDialog.apply {
                setIcon(R.drawable.vegas_08)
                setTitle(getApplicationContext().getResources().getString(R.string.app_name))
                setMessage(getApplicationContext().getResources().getString(R.string.do_you_really_want_to_close_the_application))
                setPositiveButton(getApplicationContext().getResources().getString(R.string.yes))
                { _: DialogInterface?, _: Int -> exitFromApp() }
                setNegativeButton(getApplicationContext().getResources().getString(R.string.no))
                { _, _ -> }

            }.create().show()
        }
        else {
            exitFromApp()
        }

    }

    fun exitFromApp() {
        if (!trackingEnabled.value) {
            val intent = Intent(this, VegasLocationService::class.java)
            stopService(intent)
        }

        onBackPressedDispatcher.onBackPressed()
    }

    fun isServiceRunning(): Boolean {

        val serviceClass: Class<*> = VegasLocationService::class.java
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


    fun hasAllPermissions(): Boolean{
        var result = true

        if (!hasBasePermissions()) {
            result = false
        }

        if (!hasAdvancePermissions()) {
            result = false
        }

        if (!hasPostNotificationPermissions()) {
            result = false
        }

        if (!hasStoragePermissions()) {
            result = false
        }

        return result
    }

    fun hasStoragePermissions(): Boolean{
        val permission =  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED)
        } else {
            true
        }

        READ_EXTERNAL_STORAGE.value = permission
        WRITE_EXTERNAL_STORAGE.value = permission

        if (permission) {
            Log.d("zzz","PERMISSION GRANTED android.permission.WRITE_EXTERNAL_STORAGE")
        } else {
            Log.d("zzz","PERMISSION DENIED android.permission.WRITE_EXTERNAL_STORAGE")
        }

        return permission
    }

    fun hasBasePermissions(): Boolean{
        var result = true
        basePermissions.forEach {

            val permission = ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            if ( !permission)
            {
                Log.d("zzz","PERMISSION DENIED ${it}")
                result = false
            } else {
                Log.d("zzz","PERMISSION GRANTED ${it}")
            }
            when (it) {
                Manifest.permission.INTERNET -> INTERNET.value = permission
                Manifest.permission.ACCESS_NETWORK_STATE -> ACCESS_NETWORK_STATE.value = permission
                Manifest.permission.WAKE_LOCK -> WAKE_LOCK.value = permission
                Manifest.permission.ACCESS_NOTIFICATION_POLICY -> ACCESS_NOTIFICATION_POLICY.value = permission
                Manifest.permission.ACCESS_COARSE_LOCATION -> ACCESS_COARSE_LOCATION.value = permission
                Manifest.permission.ACCESS_FINE_LOCATION -> ACCESS_FINE_LOCATION.value = permission
                Manifest.permission.RECEIVE_BOOT_COMPLETED -> RECEIVE_BOOT_COMPLETED.value = permission
            }
        }
        return result
    }

    fun hasAdvancePermissions(): Boolean {
        var result = true
        advancePermissions.forEach {
            val permission =
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            if (!permission) {
                Log.d("zzz", "PERMISSION DENIED ${it}")
                result = false
            } else {
                Log.d("zzz", "PERMISSION GRANTED ${it}")
            }

            when (it) {
                Manifest.permission.ACCESS_BACKGROUND_LOCATION -> ACCESS_BACKGROUND_LOCATION.value =
                    permission
            }

        }
        return result
    }

    fun hasPostNotificationPermissions(): Boolean {
        var result = true

        if (Build.VERSION.SDK_INT >= 33) {
            postNotificatiionsPermissions.forEach {
                val permission =
                    ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
                if (!permission) {
                    Log.d("zzz", "PERMISSION DENIED ${it}")
                    result = false
                } else {
                    Log.d("zzz", "PERMISSION GRANTED ${it}")
                }

                when (it) {
                    Manifest.permission.POST_NOTIFICATIONS -> POST_NOTIFICATIONS.value =
                        permission
                }

            }
        } else {
            Log.d("zzz", "PERMISSION GRANTED android.permission.POST_NOTIFICATIONS")
        }

        return result
    }

    fun requestBasePermissions() {
        ActivityCompat.requestPermissions(this, basePermissions,101)
    }

    fun requestAdvancePermissions() {
        ActivityCompat.requestPermissions(this, advancePermissions,101)
    }

    fun requestStoragePermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.addCategory("android.intent.category.DEFAULT")
                intent.data = Uri.parse(
                    java.lang.String.format(
                        "package:%s",
                        getPackageName()
                    )
                )
                startActivityForResult(intent, 55)
            } catch (e: Exception) {
                val intent = Intent()
                intent.action = Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION
                startActivityForResult(intent, 55)
            }
        } else {
            if (hasStoragePermissions()) {
                ActivityCompat.requestPermissions(this, storagePermissions,0)
            }
        }
    }

    fun requestPostNotificationPermissions() {
        ActivityCompat.requestPermissions(this, postNotificatiionsPermissions,101)
    }

    fun putPreferences() {
        val editor = prefs.edit()
        editor.putInt(APP_PREFERENCES_THEME, viewModel.currentTheme).apply()
        editor.putBoolean(APP_PREFERENCES_ASK_TO_EXIT_FROM_APP, viewModel.askToExitFromApp).apply()
        editor.putBoolean(APP_PREFERENCES_ASK_TO_DELETE_SELECTED_ITEM, viewModel.askToDeleteSelectedItem).apply()
        editor.putBoolean(APP_PREFERENCES_ASK_TO_SAVE_TRACKING, viewModel.askToSaveTracking).apply()
        editor.putBoolean(APP_PREFERENCES_WAKE_LOCK, viewModel.wakeLock).apply()
        editor.putInt(APP_PREFERENCES_CURRENT_MIN_DISTANCE, viewModel.currentMinDistance).apply()
        editor.putString(APP_PREFERENCES_CURRENT_MAP, viewModel.currentMap).apply()

        theme.value = (viewModel.currentTheme == 1)
    }

    fun getPreferences() {
        if(prefs.contains(APP_PREFERENCES_THEME)){
            viewModel.currentTheme = prefs.getInt(APP_PREFERENCES_THEME, 0)
        }
        if(prefs.contains(APP_PREFERENCES_ASK_TO_EXIT_FROM_APP)){
            viewModel.askToExitFromApp = prefs.getBoolean(APP_PREFERENCES_ASK_TO_EXIT_FROM_APP, true)
        }
        if(prefs.contains(APP_PREFERENCES_ASK_TO_DELETE_SELECTED_ITEM)){
            viewModel.askToDeleteSelectedItem = prefs.getBoolean(APP_PREFERENCES_ASK_TO_DELETE_SELECTED_ITEM, true)
        }
        if(prefs.contains(APP_PREFERENCES_ASK_TO_SAVE_TRACKING)){
            viewModel.askToSaveTracking = prefs.getBoolean(APP_PREFERENCES_ASK_TO_SAVE_TRACKING, false)
        }
        if(prefs.contains(APP_PREFERENCES_WAKE_LOCK)) {
            viewModel.wakeLock = prefs.getBoolean(APP_PREFERENCES_WAKE_LOCK, false)
        }

        if(prefs.contains(APP_PREFERENCES_CURRENT_MIN_DISTANCE)){
            viewModel.currentMinDistance = prefs.getInt(APP_PREFERENCES_CURRENT_MIN_DISTANCE, 100)
        }
        if(prefs.contains(APP_PREFERENCES_CURRENT_MAP)){
            viewModel.currentMap = prefs.getString(APP_PREFERENCES_CURRENT_MAP, "Yandex")!!
        }
    }

}

