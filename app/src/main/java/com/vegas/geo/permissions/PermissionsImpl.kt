package com.vegas.geo.permissions

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.vegas.geo.mainViewModel

val basePermissions = arrayOf(
    Manifest.permission.INTERNET,
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_NOTIFICATION_POLICY,
    Manifest.permission.RECEIVE_BOOT_COMPLETED,
    Manifest.permission.WAKE_LOCK,
    Manifest.permission.SEND_SMS,
    Manifest.permission.RECEIVE_SMS,
    Manifest.permission.READ_CONTACTS,
)

val postNotificationPermissions = arrayOf(
    Manifest.permission.POST_NOTIFICATIONS,
)

val accessBackgroundLocationPermissions = arrayOf(
    Manifest.permission.ACCESS_BACKGROUND_LOCATION,
)

data class PermissionsViewState(
    val INTERNET: Boolean = false,
    val ACCESS_COARSE_LOCATION: Boolean = false,
    val ACCESS_FINE_LOCATION: Boolean = false,
    val ACCESS_BACKGROUND_LOCATION: Boolean = false,
    val RECEIVE_BOOT_COMPLETED: Boolean = false,
    val WAKE_LOCK: Boolean = false,
    val POST_NOTIFICATIONS: Boolean = false,
    val ACCESS_NOTIFICATION_POLICY: Boolean = false,
    val SEND_SMS: Boolean = false,
    val RECEIVE_SMS: Boolean = false,
    val READ_CONTACTS: Boolean = false,

    val permissionsGranted: Boolean = false,
)

class PermissionsImpl(val context: Context): PermissionsApi {

    private fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }

    override fun hasAllPermissions(activity: Activity): Boolean{

        var result = true

        if (!hasBasePermissions(activity)) {
            result = false
        }

        if (!hasAccessBackgroundLocationPermissions(activity)) {
            result = false
        }

        if (!hasPostNotificationPermissions(activity)) {
            result = false
        }

        Log.d("zzz", "permissionsGranted ${result}" )
        mainViewModel.permissionsViewState.value =
            mainViewModel.permissionsViewState.value.copy(permissionsGranted = result)

        return result
    }

    override fun hasBasePermissions(activity: Activity): Boolean{
        var result = true
        basePermissions.forEach {

            val permission = ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            if ( !permission)
            {
                result = false
            }
            when (it) {

                Manifest.permission.INTERNET -> mainViewModel.permissionsViewState.value =
                    mainViewModel.permissionsViewState.value.copy(INTERNET = permission)

                Manifest.permission.ACCESS_COARSE_LOCATION -> mainViewModel.permissionsViewState.value =
                    mainViewModel.permissionsViewState.value.copy(ACCESS_COARSE_LOCATION = permission)
                Manifest.permission.ACCESS_FINE_LOCATION -> mainViewModel.permissionsViewState.value =
                    mainViewModel.permissionsViewState.value.copy(ACCESS_FINE_LOCATION = permission)

                Manifest.permission.WAKE_LOCK -> mainViewModel.permissionsViewState.value =
                    mainViewModel.permissionsViewState.value.copy(WAKE_LOCK = permission)

                Manifest.permission.ACCESS_NOTIFICATION_POLICY -> mainViewModel.permissionsViewState.value =
                    mainViewModel.permissionsViewState.value.copy(ACCESS_NOTIFICATION_POLICY = permission)

                Manifest.permission.RECEIVE_BOOT_COMPLETED -> mainViewModel.permissionsViewState.value =
                    mainViewModel.permissionsViewState.value.copy(RECEIVE_BOOT_COMPLETED = permission)

                Manifest.permission.SEND_SMS -> mainViewModel.permissionsViewState.value =
                    mainViewModel.permissionsViewState.value.copy(SEND_SMS = permission)

                Manifest.permission.RECEIVE_SMS -> mainViewModel.permissionsViewState.value =
                    mainViewModel.permissionsViewState.value.copy(RECEIVE_SMS = permission)

                Manifest.permission.READ_CONTACTS -> mainViewModel.permissionsViewState.value =
                    mainViewModel.permissionsViewState.value.copy(READ_CONTACTS = permission)

            }
        }

        return result
    }

    override fun hasAccessBackgroundLocationPermissions(activity: Activity): Boolean{
        var result = true
        accessBackgroundLocationPermissions.forEach {

            val permission = ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            if ( !permission)
            {
                result = false
            }
            when (it) {

                Manifest.permission.ACCESS_BACKGROUND_LOCATION -> mainViewModel.permissionsViewState.value =
                    mainViewModel.permissionsViewState.value.copy(ACCESS_BACKGROUND_LOCATION = permission)

            }
        }

        return result
    }

    override fun hasPostNotificationPermissions(activity: Activity): Boolean{

        val permission = //true // TODO

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                    == PackageManager.PERMISSION_GRANTED)
        } else {
            true
        }

        mainViewModel.permissionsViewState.value =
            mainViewModel.permissionsViewState.value.copy(POST_NOTIFICATIONS = permission)

        return permission
    }

    override fun requestPostNotificationPermissions(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ActivityCompat.requestPermissions(activity, postNotificationPermissions,101)
        } else {
        }
    }

    override fun requestBasePermissions(activity: Activity) {
        ActivityCompat.requestPermissions(activity, basePermissions,101)
    }

    override fun requestAccessBackgroundLocationPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(activity, accessBackgroundLocationPermissions,101)
    }

}
