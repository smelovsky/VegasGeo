package com.example.vegas

import androidx.compose.runtime.Composable
import com.example.vegas.screens.*
//import com.google.gson.annotations.SerializedName
//import com.yandex.runtime.Runtime.getApplicationContext

data class ScreenParams(
    val permissionsGranted: Boolean = false,
    val INTERNET: Boolean = false,
    val ACCESS_NETWORK_STATE: Boolean = false,
    val WAKE_LOCK: Boolean = false,
    val ACCESS_NOTIFICATION_POLICY: Boolean = false,
    val ACCESS_COARSE_LOCATION: Boolean = false,
    val ACCESS_FINE_LOCATION: Boolean = false,
    val RECEIVE_BOOT_COMPLETED: Boolean = false,
    val READ_EXTERNAL_STORAGE: Boolean = false,
    val WRITE_EXTERNAL_STORAGE: Boolean = false,
    val ACCESS_BACKGROUND_LOCATION: Boolean = false,
    val POST_NOTIFICATIONS: Boolean = false,
    val mapLatitude: Double = 0.0,
    val mapLongitude: Double = 0.0,
    val mapTrackPoints: Int = 0,
    val mapMaxDistance: Float = 0.0f,
    val mapCurrentDistance: Float = 0.0f,
    val markersShowUp: (Boolean, ()->Unit) -> Unit,
    val markersShowDown: (Boolean, ()->Unit) -> Unit,
    val tracksShowUp: (Boolean, ()->Unit) -> Unit,
    val tracksShowDown: (Boolean, ()->Unit) -> Unit,
    )
typealias ComposableFun = @Composable (screenParams: ScreenParams) -> Unit

sealed class TabItem(var icon: Int, var title: Int, var screen: ComposableFun) {
    object Home : TabItem(R.drawable.ic_label, R.string.tab_name_geo_location,
        { HomeScreen(
            it.permissionsGranted,
            it.INTERNET,
            it.ACCESS_NETWORK_STATE,
            it.WAKE_LOCK,
            it.ACCESS_NOTIFICATION_POLICY,
            it.ACCESS_COARSE_LOCATION,
            it.ACCESS_FINE_LOCATION,
            it.RECEIVE_BOOT_COMPLETED,
            it.READ_EXTERNAL_STORAGE,
            it.WRITE_EXTERNAL_STORAGE,
            it.ACCESS_BACKGROUND_LOCATION,
            it.POST_NOTIFICATIONS,
            it.mapLatitude,
            it.mapLongitude,
            it.mapTrackPoints,
            it.mapMaxDistance,
            it.mapCurrentDistance) })
    object Markers : TabItem(R.drawable.ic_label, R.string.tab_name_markers, {
        MarkersScreen(it.markersShowUp, it.markersShowDown) })
    object Tracks : TabItem(R.drawable.ic_label, R.string.tab_name_tracks, {
        TracksScreen(it.tracksShowUp, it.tracksShowDown) })
    object Settings : TabItem(R.drawable.ic_label, R.string.tab_name_settings, {
        SettingsScreen() })
}
