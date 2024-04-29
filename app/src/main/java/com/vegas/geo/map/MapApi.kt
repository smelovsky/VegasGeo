package com.vegas.geo.map

import android.content.Context
import android.graphics.drawable.Icon
import android.view.View
import org.osmdroid.util.GeoPoint

interface MapApi {
    fun mapInit(context: Context, onTap: (latitude: Double, longitude: Double) -> Unit): View
    fun addMarker(title: String, latitude: Double, longitude: Double)
    fun isZoomEmbedded(): Boolean
    fun zoomIn(): Float
    fun zoomOut(): Float
    fun saveMapSettings()
    fun onPause()
    fun onResume()
    fun onStart()
    fun onStop()
    fun invalidate()
    fun updateTracking()
    fun addRoute(startPoint: GeoPoint, endPoint: GeoPoint)
    fun removeRoute()
}