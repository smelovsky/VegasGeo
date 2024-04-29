package com.vegas.geo

import android.R.attr.phoneNumber
import android.content.Context
import android.net.Uri
import android.provider.ContactsContract
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.vegasbluetooth.sound.SoundApi
import com.vegas.geo.data.database.AppDatabase
import com.vegas.geo.data.database.model.LocationEntity
import com.vegas.geo.data.database.model.TrackEntity
import com.vegas.geo.map.MapApi
import com.vegas.geo.map.OpenStreetMapApi
import com.vegas.geo.permissions.PermissionsApi
import com.vegas.geo.permissions.PermissionsViewState
import com.vegas.geo.settings.GlobalSettingsRepositoryApi
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject


data class SettingsViewState(
    val darkModeAutoSelected: Boolean,
    val darkModeLightSelected: Boolean,
    val darkModeDarkSelected: Boolean,
    val askToExitFromApp: Boolean,
    val keepScreenOn: Boolean,
    val locationEnabled: Boolean,
    val myStatus: String?,
    val alarm: Boolean,
)

data class LocationViewState(
    val latitude:Double,
    val longitude:Double,
    val distance:Double,
    val points:Int,
    val trackingEnabled:Boolean,
    val isLocationDone:Boolean,
)

data class MapViewState(
    val isRouteAdded:Boolean,
)

data class ScannerViewState(
    val scannerMode:ScannerMode,
    val message: String,
)

enum class ScannerMode {
    INITIAL,
    PLEASE_WAIT,
    SHOW_MESSAGE,
    SHOW_ERROR,
}

enum class MapMode {
    LOCATION,
    SAVED_TRACK,
    SCANNER,
}


data class MapPoint(
    var latitude: Double,
    var longitude: Double,
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val permissionsApi: PermissionsApi,
    private val globalSettingsRepositoryApi: GlobalSettingsRepositoryApi,
    private val appDatabase: AppDatabase,
    val openStreetMapApi: OpenStreetMapApi,
    val soundApi: SoundApi,
    @ApplicationContext val context: Context,
    ) : ViewModel()

{
    ////////////////////////////////////////////////////////////////////////////////////////////////
    // contacts
        fun getContactNameByPhoneNumber(phoneNumber: String): String {
        val uri = Uri.withAppendedPath(
            ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(
                phoneNumber
            )
        )

        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        var contactName = ""
        val cursor = context.contentResolver.query(uri, projection, null, null, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0)
            }
            cursor.close()
        }

        return contactName
        }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // UI

    val exitFromApp = mutableStateOf(false)
    var current_tab_index = 0

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // DB

    var trackListEntity by mutableStateOf(listOf<TrackEntity>())

    fun getDbApi (): AppDatabase {
        return appDatabase
    }

    fun getTracks () {
        GlobalScope.launch {
            trackListEntity = appDatabase.appDao().getTrackList()
        }
    }

    fun getLocations () {
        GlobalScope.launch {
            val locations = appDatabase.appDao().getLocationList()
            Log.d("ddd", locations.toString())
        }
    }

    fun trackDeleteSelectedItems() {

        GlobalScope.launch {
            appDatabase.appDao().deleteLocationsSelectedTracks()
            appDatabase.appDao().deleteSelectedTracks()
            trackListEntity = appDatabase.appDao().getTrackList()
        }

    }

    fun trackSelectItemAndSetEditMode(trackId: Long) {
        GlobalScope.launch {
            appDatabase.appDao().unselectAllTracks()
            appDatabase.appDao().selectTrackById(trackId = trackId, isSelected = true, isEditMode = true)
            trackListEntity = appDatabase.appDao().getTrackList()
        }
    }

    fun trackUnselectAllItems() {
        GlobalScope.launch {
            appDatabase.appDao().unselectAllTracks()
            trackListEntity = appDatabase.appDao().getTrackList()
        }
    }

    fun trackSelectItem(trackId: Long, isSelected: Boolean) {
        GlobalScope.launch {
            appDatabase.appDao().unselectAllTracks()
            appDatabase.appDao().selectTrackById(trackId = trackId, isSelected = isSelected, isEditMode = false)
            trackListEntity = appDatabase.appDao().getTrackList()
        }
    }

    fun trackChangeEditMode(trackId: Long, isEditMode: Boolean) {
        GlobalScope.launch {
            appDatabase.appDao().unselectAllTracks()
            appDatabase.appDao().selectTrackById(trackId = trackId, isSelected = true, isEditMode = isEditMode)
            trackListEntity = appDatabase.appDao().getTrackList()
        }
    }

    fun trackRenameItem(trackId: Long, trackName: String) {
        GlobalScope.launch {
            appDatabase.appDao().updateTrack(trackId = trackId, trackName = trackName)
            appDatabase.appDao().selectTrackById(trackId = trackId, isSelected = false, isEditMode = false)
            trackListEntity = appDatabase.appDao().getTrackList()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // sounds

    fun play() {
        soundApi.play(soundApi.alarmSound)
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // permissions

    val permissionsViewState = mutableStateOf(PermissionsViewState())

    fun getPermissionsApi() : PermissionsApi {
        return permissionsApi
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // Scanner

    private val _scannerViewState = MutableStateFlow(ScannerViewState(
        scannerMode = ScannerMode.INITIAL,
        message = "",
    ))

    val scannerViewState: StateFlow<ScannerViewState> = _scannerViewState

    fun setMessage(message: String) {
        val value = _scannerViewState.value.copy(
            message = message,
        )
        _scannerViewState.value = value
    }

    fun setScannerMode(scannerMode:ScannerMode) {

        val value = _scannerViewState.value.copy(
            scannerMode = scannerMode,
        )
        _scannerViewState.value = value
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // location

    private val _locationViewState = MutableStateFlow(LocationViewState(
        latitude = 0.0,
        longitude = 0.0,
        distance = 0.0,
        points = 0,
        trackingEnabled = false,
        isLocationDone = false,
        ))

    val locationViewState: StateFlow<LocationViewState> = _locationViewState

    fun setLocation(latitude:Double, longitude:Double, distance:Double, points:Int) {

        currentPoint.latitude = latitude
        currentPoint.longitude = longitude

        val value = _locationViewState.value.copy(
            latitude = latitude,
            longitude = longitude,
            distance = distance,
            points = points,
            isLocationDone = true,
            )
        _locationViewState.value = value
    }

    fun enableTracking(enable:Boolean) {

        val value = _locationViewState.value.copy(
            trackingEnabled = enable,
            points = 0,
            distance = 0.0,
            )
        _locationViewState.value = value
    }


    ////////////////////////////////////////////////////////////////////////////////////////////////
    // settings

    private val _settingsViewState = MutableStateFlow(mapSettingsToViewState(globalSettingsRepositoryApi.settings.value))
    val settingsViewState: StateFlow<SettingsViewState> = _settingsViewState

    private fun mapSettingsToViewState(settings: GlobalSettingsRepositoryApi.SettingsDb): SettingsViewState {
        return SettingsViewState(
            darkModeAutoSelected = settings.darkModeAuto,
            darkModeLightSelected = !settings.darkModeAuto && !settings.darkMode,
            darkModeDarkSelected = !settings.darkModeAuto && settings.darkMode,
            askToExitFromApp = settings.askToExitFromApp,
            keepScreenOn = settings.keepScreenOn,
            locationEnabled = settings.locationEnabledAfterExit,
            myStatus = settings.myStatus,
            alarm = settings.alarm,
        )
    }

    fun onDarkModeAutoClicked() {
        viewModelScope.launch(Dispatchers.Default) {
            globalSettingsRepositoryApi.setSettings(
                globalSettingsRepositoryApi.settings.value.copy(
                    darkModeAuto = true,
                )
            )
        }
    }

    fun onDarkModeLightClicked() {
        viewModelScope.launch(Dispatchers.Default) {
            globalSettingsRepositoryApi.setSettings(
                globalSettingsRepositoryApi.settings.value.copy(
                    darkMode = false,
                    darkModeAuto = false,
                )
            )
        }
    }

    fun onDarkModeDarkClicked() {
        viewModelScope.launch(Dispatchers.Default) {
            globalSettingsRepositoryApi.setSettings(
                globalSettingsRepositoryApi.settings.value.copy(
                    darkMode = true,
                    darkModeAuto = false,
                )
            )
        }
    }

    fun onExitFromAppChecked(checked: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            globalSettingsRepositoryApi.setSettings(
                globalSettingsRepositoryApi.settings.value.copy(
                    askToExitFromApp = checked
                )
            )
        }
    }

    fun onKeepScreenOn(checked: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            globalSettingsRepositoryApi.setSettings(
                globalSettingsRepositoryApi.settings.value.copy(
                    keepScreenOn = checked
                )
            )
        }
    }

    fun onLocationEnabled(checked: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            globalSettingsRepositoryApi.setSettings(
                globalSettingsRepositoryApi.settings.value.copy(
                    locationEnabledAfterExit = checked
                )
            )
        }
    }



    fun onMyStatusChanged(status: String) {
        viewModelScope.launch(Dispatchers.Default) {
            globalSettingsRepositoryApi.setSettings(
                globalSettingsRepositoryApi.settings.value.copy(
                    myStatus = status
                )
            )
        }
    }

    fun onPhoneNumberChanged(phoneNumber: String) {
        viewModelScope.launch(Dispatchers.Default) {
            globalSettingsRepositoryApi.setSettings(
                globalSettingsRepositoryApi.settings.value.copy(
                    phoneNumber = phoneNumber
                )
            )
        }
    }

    fun onContactNameChanged(contactName: String) {
        viewModelScope.launch(Dispatchers.Default) {
            globalSettingsRepositoryApi.setSettings(
                globalSettingsRepositoryApi.settings.value.copy(
                    contactName = contactName
                )
            )
        }
    }

    fun onAlarmChecked(alarm: Boolean) {
        viewModelScope.launch(Dispatchers.Default) {
            globalSettingsRepositoryApi.setSettings(
                globalSettingsRepositoryApi.settings.value.copy(
                    alarm = alarm
                )
            )
        }
    }

    fun getLocationEnabledAfterExit() : Boolean {
        return globalSettingsRepositoryApi.settings.value.locationEnabledAfterExit
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // map

    val minDistance = listOf(1, 10, 100)
    val maps = listOf("Yandex", "Open Street")

    var locationList  = listOf<LocationEntity>()

    var zoom = 15.0F

    var scannerPoint = GeoPoint(0.0, 0.0)

    var currentPoint = GeoPoint(0.0, 0.0)

    var mapPoint = GeoPoint(0.0, 0.0)
    var mapPointInited = false

    var trackId:Long = -1

    var mapMode = MapMode.LOCATION

    var trackingEanabled = false

    var startPoint = GeoPoint(0.0, 0.0)
    var endPoint = GeoPoint(0.0, 0.0)
    var isRoadAdded = false

    fun getMapApi(): MapApi {
        return openStreetMapApi
    }

    private val _mapViewState = MutableStateFlow(MapViewState(isRouteAdded = false))
    val mapViewState: StateFlow<MapViewState> = _mapViewState

    fun setRoute(isRouteAdded: Boolean) {
        val value = _mapViewState.value.copy(
            isRouteAdded = isRouteAdded,
        )
        _mapViewState.value = value
    }

    fun saveRouteSettings() {
        viewModelScope.launch(Dispatchers.Default) {
            globalSettingsRepositoryApi.setSettings(
                globalSettingsRepositoryApi.settings.value.copy(
                    isRoadAdded = isRoadAdded,
                    startLatitude = startPoint.latitude.toString(),
                    startLongitude = startPoint.longitude.toString(),
                    endLatitude = endPoint.latitude.toString(),
                    endLongitude = endPoint.longitude.toString(),
                )
            )
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // sms

    fun getMyStatus () : String {
        return globalSettingsRepositoryApi.settings.value.myStatus.toString()
    }

    fun getPhoneNumber () : String {
        return globalSettingsRepositoryApi.settings.value.phoneNumber.toString()
    }

    fun getContactName () : String {
        return globalSettingsRepositoryApi.settings.value.contactName.toString()
    }

    fun getAlarm () : Boolean {
        return globalSettingsRepositoryApi.settings.value.alarm
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // init

    init {

        viewModelScope.launch(Dispatchers.Default) {
            globalSettingsRepositoryApi.settings.collect {
                _settingsViewState.value = mapSettingsToViewState(it)

                Log.d("zzz", "globalSettingsRepositoryApi.settings.collect")

                isRoadAdded = it.isRoadAdded
                mainViewModel.setRoute(isRoadAdded)

                val startLatitude = it.startLatitude?.toDouble()
                val startLongitude = it.startLongitude?.toDouble()
                val endLatitude = it.endLatitude?.toDouble()
                val endLongitude = it.endLongitude?.toDouble()
                if (startLatitude != null && startLongitude != null) {
                    startPoint = GeoPoint(startLatitude, startLongitude)
                }
                if (endLatitude != null && endLongitude != null) {
                    endPoint = GeoPoint(endLatitude, endLongitude)
                }


            }
        }

    }

}