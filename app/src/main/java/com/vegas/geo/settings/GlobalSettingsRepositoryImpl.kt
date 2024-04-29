package com.vegas.geo.settings

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class GlobalSettingsRepositoryImpl(context: Context) : GlobalSettingsRepositoryApi {

    private val classTag = this::class.java.simpleName

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _settings = MutableStateFlow(GlobalSettingsRepositoryApi.DEFAULT_SETTINGS)
    override val settings: StateFlow<GlobalSettingsRepositoryApi.SettingsDb> = _settings

    init {
        load()
    }

    override fun setSettings(newSettings: GlobalSettingsRepositoryApi.SettingsDb) {
        save(newSettings)
        _settings.value = newSettings
    }

    private fun load() {
        Log.d(classTag, "load")
        _settings.value = try {
            GlobalSettingsRepositoryApi.SettingsDb(
                darkModeAuto = prefs.getBoolean(
                    DARK_MODE_AUTO,
                    GlobalSettingsRepositoryApi.DEFAULT_SETTINGS.darkModeAuto
                ),
                darkMode = prefs.getBoolean(DARK_MODE, GlobalSettingsRepositoryApi.DEFAULT_SETTINGS.darkMode),
                askToExitFromApp = prefs.getBoolean(
                    ASK_TO_EXT_FROM_APP,
                    GlobalSettingsRepositoryApi.DEFAULT_SETTINGS.askToExitFromApp
                ),
                keepScreenOn = prefs.getBoolean(
                    KEEP_SCREEN_ON,
                    GlobalSettingsRepositoryApi.DEFAULT_SETTINGS.keepScreenOn
                ),
                locationEnabledAfterExit = prefs.getBoolean(
                    LOCATION_ENABLED_AFTER_EXIT,
                    GlobalSettingsRepositoryApi.DEFAULT_SETTINGS.locationEnabledAfterExit
                ),
                myStatus = prefs.getString(MY_STATUS, GlobalSettingsRepositoryApi.DEFAULT_SETTINGS.myStatus),

                startLatitude = prefs.getString(START_LATITUDE, GlobalSettingsRepositoryApi.DEFAULT_SETTINGS.startLatitude),
                startLongitude = prefs.getString(START_LONGITUDE, GlobalSettingsRepositoryApi.DEFAULT_SETTINGS.startLongitude),
                endLatitude = prefs.getString(END_LATITUDE, GlobalSettingsRepositoryApi.DEFAULT_SETTINGS.endLatitude),
                endLongitude = prefs.getString(END_LONGITUDE, GlobalSettingsRepositoryApi.DEFAULT_SETTINGS.endLongitude),
                isRoadAdded = prefs.getBoolean(IS_ROAD_ADDED, GlobalSettingsRepositoryApi.DEFAULT_SETTINGS.isRoadAdded),

                phoneNumber = prefs.getString(PHONE_NUMBER, GlobalSettingsRepositoryApi.DEFAULT_SETTINGS.phoneNumber),
                contactName = prefs.getString(CONTACT_NAME, GlobalSettingsRepositoryApi.DEFAULT_SETTINGS.contactName),

                alarm = prefs.getBoolean(ALARM, GlobalSettingsRepositoryApi.DEFAULT_SETTINGS.alarm),

            )
        } catch (ignored: Exception) {
            GlobalSettingsRepositoryApi.DEFAULT_SETTINGS
        }
    }

    private fun save(settings: GlobalSettingsRepositoryApi.SettingsDb) {
        Log.d(classTag, "save")
        prefs.edit(commit = true) {
            putBoolean(DARK_MODE_AUTO, settings.darkModeAuto)
            putBoolean(DARK_MODE, settings.darkMode)
            putBoolean(ASK_TO_EXT_FROM_APP, settings.askToExitFromApp)
            putBoolean(KEEP_SCREEN_ON, settings.keepScreenOn)
            putBoolean(LOCATION_ENABLED_AFTER_EXIT, settings.locationEnabledAfterExit)
            putString(MY_STATUS, settings.myStatus)
            putString(START_LATITUDE, settings.startLatitude)
            putString(START_LONGITUDE, settings.startLongitude)
            putString(END_LATITUDE, settings.endLatitude)
            putString(END_LONGITUDE, settings.endLongitude)
            putBoolean(IS_ROAD_ADDED, settings.isRoadAdded)
            putString(PHONE_NUMBER, settings.phoneNumber)
            putString(CONTACT_NAME, settings.contactName)
            putBoolean(ALARM, settings.alarm)
        }
    }

    companion object {
        private const val PREFS_NAME = "global_settings"
        private const val DARK_MODE_AUTO = "dark_mode_auto"
        private const val DARK_MODE = "dark_mode"
        private const val ASK_TO_EXT_FROM_APP = "ask_to_ext_from_app"
        private const val KEEP_SCREEN_ON = "keep_screen_on"
        private const val LOCATION_ENABLED_AFTER_EXIT = "location_enabled_after_exit"
        private const val MY_STATUS = "my_status"
        private const val START_LATITUDE = "start_latitude"
        private const val START_LONGITUDE = "start_longitude"
        private const val END_LATITUDE = "end_latitude"
        private const val END_LONGITUDE = "end_longitude"
        private const val IS_ROAD_ADDED = "is_road_added"
        private const val PHONE_NUMBER = "phone_number"
        private const val CONTACT_NAME = "contact_name"
        private const val ALARM = "alarm"
    }
}
