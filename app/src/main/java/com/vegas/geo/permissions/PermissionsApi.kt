package com.vegas.geo.permissions

import android.app.Activity

interface PermissionsApi {

    fun hasAllPermissions(activity: Activity): Boolean

    fun hasAccessBackgroundLocationPermissions(activity: Activity): Boolean
    fun hasBasePermissions(activity: Activity): Boolean
    fun hasPostNotificationPermissions(activity: Activity): Boolean

    fun requestBasePermissions(activity: Activity)
    fun requestAccessBackgroundLocationPermissions(activity: Activity)
    fun requestPostNotificationPermissions(activity: Activity)
}
