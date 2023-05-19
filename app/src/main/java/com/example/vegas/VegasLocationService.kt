package com.example.vegas

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.content.Context
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.PRIORITY_MIN
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import com.yandex.mapkit.geometry.Point
import java.lang.Float.max


class VegasLocationService : Service() {

    private var locationCallback: LocationCallback? = null
    var fusedLocationClient: FusedLocationProviderClient? = null
    var latitude:Double = 0.0
    var longitude:Double = 0.0
    var track: MutableList<Point> = mutableListOf<Point>()
    var trackEanabled = false
    var isInited = false
    var minDistance = 100F

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        Log.d("zzz", "startId: $startId")

        var appRequest: String? = intent.getStringExtra("app_request")
        if (appRequest != null) {

            Log.d("zzz", appRequest)

            when (appRequest) {
                "start" -> {
                    isInited = true
                    startLocation()
                }
                "track_enable" -> {
                    var min_distance: Int? = intent.getIntExtra("min_distance", 100)
                    if (min_distance != null) {
                        minDistance = min_distance.toFloat()
                    }

                    track.clear()
                    trackEanabled = true
                }
                "track_disable" -> {
                    trackEanabled = false
                }
                "update_params" -> {
                    var min_distance: Int? = intent.getIntExtra("min_distance", 100)
                    if (min_distance != null) {
                        minDistance = min_distance.toFloat()
                    }
                }
                "status" -> {
                    sendMessageToActivity()
                }

            }

        }

        val channelId =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                createNotificationChannel("my_service", "My Background Service")
            } else {
                // If earlier version channel ID is not used
                // https://developer.android.com/reference/android/support/v4/app/NotificationCompat.Builder.html#NotificationCompat.Builder(android.content.Context)
                ""
            }

        val notificationBuilder = NotificationCompat.Builder(this, channelId )
        val notification = notificationBuilder
            .setOngoing(true)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(PRIORITY_MIN)
            .setCategory(Notification.CATEGORY_SERVICE)
            .build()

        startForeground(55, notification )

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()

        locationCallback?.let { fusedLocationClient?.removeLocationUpdates(it) }
        locationCallback = null
    }

    private fun createNotificationChannel(channelId: String, channelName: String): String{
        val chan = NotificationChannel(channelId,
            channelName, NotificationManager.IMPORTANCE_NONE)
        chan.lightColor = Color.BLUE
        chan.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
        val service = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        service.createNotificationChannel(chan)
        return channelId
    }

    @SuppressLint("MissingPermission")
    private fun startLocation() {

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(p0: LocationResult) {
                for (lo in p0.locations) {
                    //Log.d("zzz","onLocationResult: ${lo.latitude}, ${lo.longitude}")
                    latitude = lo.latitude
                    longitude = lo.longitude
                    updateTrack()
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

        if (trackEanabled) {
            var trackArray= DoubleArray(track.size * 2)
            var index = 0
            track.forEach {
                trackArray[index++] = it.latitude
                trackArray[index++] = it.longitude
            }

            var current_distance = floatArrayOf(0F)
            var max_distance = 0F

            if (track.size > 1) {
                Location.distanceBetween(
                    track.first().latitude,
                    track.first().longitude,
                    track.last().latitude,
                    track.last().longitude,
                    current_distance
                )


                track.forEach() {
                    var distance: FloatArray = floatArrayOf(0F)
                    Location.distanceBetween(
                        track.first().latitude,
                        track.first().longitude,
                        it.latitude,
                        it.longitude,
                        distance
                    )
                    max_distance = max(distance[0], max_distance)
                }


            }

            intent.putExtra("current_distance", current_distance[0])
            intent.putExtra("max_distance", max_distance)

            intent.putExtra("track", trackArray)

            //Log.d("zzz","sendMessageToActivity: ${minDistance}")
            Log.d("zzz","sendMessageToActivity: ${latitude}, ${longitude}, ${trackArray.size}")
        }

        sendBroadcast(intent)

    }

    fun updateTrack() {
        if (trackEanabled) {
            if (track.size == 0) {
                track.add(Point(latitude, longitude))
            } else {
                var distance: FloatArray = floatArrayOf(0F)
                Location.distanceBetween(
                    track.last().latitude,
                    track.last().longitude,
                    latitude,
                    longitude,
                    distance
                )
                if (distance.first() >= minDistance) {
                    track.add(Point(latitude, longitude))
                }
            }
        }
    }
}