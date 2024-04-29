package com.vegas.geo.settings

import kotlinx.coroutines.flow.StateFlow

interface GlobalSettingsRepositoryApi {

    val settings: StateFlow<SettingsDb>

    fun setSettings(newSettings: SettingsDb)

    companion object {
        val DEFAULT_SETTINGS = SettingsDb(
            darkModeAuto = true,
            darkMode = false,
            askToExitFromApp = true,
            keepScreenOn = true,
            locationEnabledAfterExit = true,
            myStatus = "Занят важным делом",
            startLatitude = "0.0",
            startLongitude = "0.0",
            endLatitude = "0.0",
            endLongitude = "0.0",
            isRoadAdded = false,
            phoneNumber = "+79998887776",
            contactName = "",
            alarm = false,
        )
    }

    data class SettingsDb(
        val darkModeAuto: Boolean,
        val darkMode: Boolean,
        val askToExitFromApp: Boolean,
        val keepScreenOn: Boolean,
        val locationEnabledAfterExit: Boolean,
        val myStatus: String?,
        val startLatitude: String?,
        val startLongitude: String?,
        val endLatitude: String?,
        val endLongitude: String?,
        var isRoadAdded: Boolean,
        var phoneNumber: String?,
        var contactName: String?,
        var alarm: Boolean,
    )
}