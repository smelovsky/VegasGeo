package com.vegas.geo

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.*
import com.vegas.geo.data.database.model.LocationEntity
import com.vegas.geo.data.database.model.TrackEntity
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.GregorianCalendar


class LocationService : Service() {

    private var locationCallback: LocationCallback? = null
    var fusedLocationClient: FusedLocationProviderClient? = null

    var latitude:Double = 0.0
    var longitude:Double = 0.0

    var start_latitude:Double = 0.0
    var start_longitude:Double = 0.0

    var distance:Double = 0.0
    var points:Int = 0

    var isLocationDone:Boolean = false
    var trackingEanabled = false

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }


    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        Log.d("zzz", "startId: $startId")

        var appRequest: String? = intent.getStringExtra("app_request")
        if (appRequest != null) {

            Log.d("zzz", appRequest)

            when (appRequest) {
                "start" -> {
                    startLocation()
                }
                "tracking_enable" -> {

                    if (isLocationDone) {
                        start_latitude = latitude
                        start_longitude = longitude
                    }

                    trackingEanabled = true

                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
                    val time = LocalDateTime.now().format(formatter)

                    GlobalScope.launch {
                        mainViewModel.trackId = mainViewModel.getDbApi().appDao().insertTrack(TrackEntity(
                            trackName = time,
                            isSelected = false,
                            isEditMode = false))

                        if (isLocationDone) {
                            val locationEntity = LocationEntity(trackId = mainViewModel.trackId, latitude = latitude, longitude = longitude)
                            mainViewModel.getDbApi().appDao().insertLocation(locationEntity)
                            points = 1

                            mainViewModel.trackListEntity = mainViewModel.getDbApi().appDao().getTrackList()
                        }


                    }

                    sendMessageToActivity()
                }
                "tracking_disable" -> {
                    points = 0
                    trackingEanabled = false
                    mainViewModel.trackId = -1
                }
                "update_params" -> {

                }
                "status" -> {
                    Log.d("zzz", "status ${trackingEanabled}")
                    val intent = Intent()
                    intent.action = "STATUS"
                    intent.putExtra("tracking_enabled", trackingEanabled)
                    sendBroadcast(intent)
                }

            }

        }

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("Vegas", "Location Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId )

        val notification = notificationBuilder
            .setOngoing(true)
            .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.vegas_02))
            .setSmallIcon(R.drawable.baseline_location_pin_24)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .setContentTitle("Info")
            .setContentText("Location service enabled")
            .setWhen(GregorianCalendar.getInstance().getTimeInMillis())
            .setAutoCancel(true)
            .setColor(Color.BLUE)
            .build()

        startForeground(55, notification )

        return START_STICKY
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    private fun startLocation() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(p0: LocationResult) {
                for (lo in p0.locations) {

                    latitude = lo.latitude
                    longitude = lo.longitude

                    if (isLocationDone == false) {
                        start_latitude = latitude
                        start_longitude = longitude
                    }

                    isLocationDone = true

                    if (trackingEanabled) {
                        if (mainViewModel.trackId != -1L) {

                            GlobalScope.launch {
                                mainViewModel.getDbApi().appDao().insertLocation(LocationEntity(
                                    trackId = mainViewModel.trackId,
                                    latitude = latitude,
                                    longitude = longitude,
                                ))
                                points++
                            }

                        }
                    }


                    if (points > 1) {

                        var distance_between = floatArrayOf(0F)
                        Location.distanceBetween(
                            start_latitude,
                            start_longitude,
                            latitude,
                            longitude,
                            distance_between)

                        distance = distance_between[0].toDouble()

                    } else {
                        distance = 0.0
                    }

                    sendMessageToActivity()

                }
            }
        }
        locationCallback?.let {

            val locationRequest = LocationRequest.create().apply {
                interval = 5000
                fastestInterval = 5000
                priority = Priority.PRIORITY_HIGH_ACCURACY
            }

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                it,
                Looper.getMainLooper()
            )
        }
    }

    private fun sendMessageToActivity() {

        val intent = Intent()
        intent.action = "CURREN_LOCATION"
        intent.putExtra("latitude", latitude)
        intent.putExtra("longitude", longitude)
        intent.putExtra("distance", distance)
        intent.putExtra("points", points)

        sendBroadcast(intent)
    }

    override fun onDestroy() {
        super.onDestroy()

        Log.d("zzz", "onDestroy")

        locationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
        locationCallback = null
    }
}